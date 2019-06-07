package com.underiteration.lwms.service;

import com.underiteration.lwms.Constants;
import com.underiteration.lwms.config.ConfigAddresses;
import com.underiteration.lwms.config.ConfigManager;
import com.underiteration.lwms.jobpool.PoolManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceDiscoveryListener {

	private static final ServiceDiscoveryListener singleton = new ServiceDiscoveryListener();

	private static Logger logger = Logger.getLogger(ServiceDiscoveryListener.class.getCanonicalName());

	private ExecutorService pool;

	static void listenForServices() {

		logger.info("Starting service discovery listener");
		singleton.start();
	}

	private ServiceDiscoveryListener() {
		pool = PoolManager.getPool(ServiceDiscoveryListener.class.getCanonicalName(), 1);
	}

	private void start() {

		var lowPort = ConfigManager.instance().getInteger(ConfigAddresses.SERVICE_DISCOVERY_LOW_PORT).orElse(Constants.DEFAULT_SERVICE_DISCOVERY_LOW_PORT);
		var highPort = ConfigManager.instance().getInteger(ConfigAddresses.SERVICE_DISCOVERY_HIGH_PORT).orElse(Constants.DEFAULT_SERVICE_DISCOVERY_HIGH_PORT);

		for (var port = lowPort; port <= highPort; port++) {

			try {
				var socket = new DatagramSocket(port);

				PoolManager.getPool("_CORE", Constants.NUMBER_OF_CORE_LISTENERS)
						.execute(() -> listenInBackground(socket));
				break;
			} catch (SocketException e) {
				logger.log(Level.FINE, e.getLocalizedMessage());
			}
		}

	}

	private void listenInBackground(DatagramSocket socket) {
		while (true) {
			DatagramPacket datagram = new DatagramPacket(new byte[Constants.SERVICE_DISCOVERY_BROADCAST_PAYLOAD_SIZE], Constants.SERVICE_DISCOVERY_BROADCAST_PAYLOAD_SIZE);

			try {
				socket.receive(datagram);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage());
			}

			RemoteServiceRegistrar registrar = new RemoteServiceRegistrar(datagram.getAddress(), new String(datagram.getData()));
			pool.execute(registrar);
		}
	}

	private static class RemoteServiceRegistrar implements  Runnable {

		private final InetAddress peerAddress;
		private final String peerId;
		private final Integer peerServicePort;

		public RemoteServiceRegistrar(InetAddress socketAddress, String payload) {
			this.peerAddress = socketAddress;
			this.peerId = payload.substring(0, 36);
			this.peerServicePort = Integer.valueOf(payload.substring(37));
		}

		@Override
		public void run() {
			RemoteServiceRegister.instance().registerNode(new Node(peerId, peerAddress, peerServicePort));
		}
	}

}
