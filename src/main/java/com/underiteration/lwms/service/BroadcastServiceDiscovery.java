package com.underiteration.lwms.service;

import com.underiteration.lwms.Constants;
import com.underiteration.lwms.config.ConfigAddresses;
import com.underiteration.lwms.config.ConfigManager;
import com.underiteration.lwms.jobpool.PoolManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.underiteration.lwms.jobpool.Timer.runAfter;

public class BroadcastServiceDiscovery {

	private static final BroadcastServiceDiscovery singleton = new BroadcastServiceDiscovery();

	private static Logger logger = Logger.getLogger(BroadcastServiceDiscovery.class.getCanonicalName());

	static String nodeId = UUID.randomUUID().toString();

	static void broadcastService() {

		logger.info("Broadcasting service");
		singleton.start();
	}

	private BroadcastServiceDiscovery() {

	}

	private void start() {

		findNetworkInterfaces().stream()
				.filter(this::isInterfaceUp)
				.map(this::broadcastAddressFromInterface)
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.forEach(this::broadcast);

		runAfter(this::start, PoolManager.getDefaultPool(), 10_000);

	}

	private Collection<NetworkInterface> findNetworkInterfaces() {

		Collection<NetworkInterface> interfaces = new ArrayList<>();
		try {
			NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(interfaces::add);
		} catch (SocketException e) {
			logger.log(Level.SEVERE, "Could not get network interfaces for this machine - check your networking");
			logger.info(e.getLocalizedMessage());
		}
		return interfaces;
	}

	// TODO: better name needed
	private boolean isInterfaceUp(NetworkInterface networkInterface) {

		try {
			return networkInterface.isUp();
		} catch (SocketException e) {
			return false;
		}
	}

	private Optional<InetAddress> broadcastAddressFromInterface(NetworkInterface networkInterface) {
		return networkInterface.getInterfaceAddresses().stream()
				.map(address -> address.getBroadcast())
				.filter(address -> address != null)
				.findFirst();
	}

	private void broadcast(InetAddress inetAddress) {

		try {
			var lowPort = ConfigManager.instance().getInteger(ConfigAddresses.SERVICE_DISCOVERY_LOW_PORT).orElse(Constants.DEFAULT_SERVICE_DISCOVERY_LOW_PORT);
			var highPort = ConfigManager.instance().getInteger(ConfigAddresses.SERVICE_DISCOVERY_HIGH_PORT).orElse(Constants.DEFAULT_SERVICE_DISCOVERY_HIGH_PORT);

			for (var port = lowPort; port <= highPort; port++) {
				byte[] serviceBroadcastPayload = getBroadcastPayload();
				DatagramPacket packet = new DatagramPacket(serviceBroadcastPayload, Constants.SERVICE_DISCOVERY_BROADCAST_PAYLOAD_SIZE, inetAddress, port);

				DatagramSocket socket = new DatagramSocket();
				socket.setBroadcast(true);
				socket.send(packet);
				socket.close();
			}

			logger.info(String.format("Broadcast message sent to %s", inetAddress));
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage());
		}
	}

	private byte[] getBroadcastPayload() {

		String port = Server.getListeningPort().toString();

		while (port.length() < 5) {
			port = "0" + port;
		}

		return new StringBuilder().append(nodeId).append(":").append(port).toString().getBytes();
	}

}
