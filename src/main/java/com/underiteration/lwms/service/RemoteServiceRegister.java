package com.underiteration.lwms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.underiteration.lwms.api.RemoteServiceDescription;
import com.underiteration.lwms.api.exception.JsonMarshallingException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.underiteration.lwms.networking.TcpServiceRequest.sendOptionsRequest;

public class RemoteServiceRegister {

	private static Logger logger = Logger.getLogger(RemoteServiceRegister.class.getCanonicalName());

	private static final RemoteServiceRegister singleton = new RemoteServiceRegister();
	private static final Collection<Consumer<String>> serviceDiscoverySubscribers = new HashSet<>();

	private final Set<String> nodes = new HashSet<>();
	private final Map<String, List<Node>> serviceProviders = new HashMap<>();
	private final Map<String, RemoteServiceDescription> serviceDescriptions = new HashMap<>();

	private int indexCounter = 0;

	private RemoteServiceRegister() {

	}

	public static RemoteServiceRegister instance() {
		return singleton;
	}

	public void registerNode(Node node) {
		if (nodes.contains(node.getId())) return;
		if (BroadcastServiceDiscovery.nodeId.equals(node.getId())) return;

		try {
			requestServiceDescription(node.getAddress(), node.getServicePort())
					.stream()
					.forEach(service -> {
						serviceProviders.computeIfAbsent(service.getServiceName(), __ -> new ArrayList<>()).add(node);

						if (! serviceDescriptions.containsKey(service.getServiceName())) {
							serviceDiscoverySubscribers.forEach(sub -> sub.accept(service.getServiceName()));
						}

						serviceDescriptions.put(service.getServiceName(), service);
					});

			logger.info(String.format(">>> %s is connecting to the cluster (kind of) from %s", node.getId(), node.getAddress()));

			nodes.add(node.getId()); // Locally, there are often several interfaces that will start this from a broadcast. It's possible that this dumb first-come-first-registered may be problematic. If we allow all to run that will upset round-robin request allocation.
		} catch (IOException e) {
			nodes.remove(node.getId());
		}

	}

	public Optional<Node> getServiceProvider(String serviceName) {
		return Optional.ofNullable(serviceProviders.get(serviceName)).map(list -> list.get(indexCounter++ % list.size()));
	}

	public Optional<Boolean> checkServiceInputType(String serviceName, Object input) {

		Boolean isCorrect = null;

		if (serviceDescriptions.containsKey(serviceName)) {
			String typeHint = serviceDescriptions.get(serviceName).getInputTypeHint();
			isCorrect = input.getClass().getCanonicalName().equals(typeHint);
		}

		return Optional.ofNullable(isCorrect);
	}

	public Optional<Marshaller> marshaller(String serviceName) {

		if (serviceDescriptions.containsKey(serviceName)) {
			return Optional.of(new Marshaller(serviceDescriptions.get(serviceName)));
		} else {
			return Optional.empty();
		}
	}

	private Collection<RemoteServiceDescription> requestServiceDescription(InetAddress address, Integer servicePort) throws IOException {

		byte[] response = sendOptionsRequest(address, servicePort);

		ObjectMapper mapper = new ObjectMapper();
		CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, RemoteServiceDescription.class);

		return mapper.readValue(response, collectionType);
	}

	public static class Marshaller {

		private final RemoteServiceDescription serviceDescription;

		Marshaller(RemoteServiceDescription serviceDescription) {
			this.serviceDescription = serviceDescription;
		}

		public <T> T marshall(String json) throws JsonMarshallingException {

			try {
				String typeHint = serviceDescription.getOutputTypeHint();
				Class<?> outputType = Class.forName(typeHint);
				ObjectMapper mapper = new ObjectMapper();
				return (T) mapper.readValue(json, outputType);

			} catch (ClassNotFoundException | IOException e) {
				throw new JsonMarshallingException(e);
			}
		}
	}

	public static void subscribeToServices(Consumer<String> consumer) {
		RemoteServiceRegister.serviceDiscoverySubscribers.add(consumer);
	}

}
