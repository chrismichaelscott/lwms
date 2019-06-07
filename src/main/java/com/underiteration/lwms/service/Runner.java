package com.underiteration.lwms.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.underiteration.lwms.service.BroadcastServiceDiscovery.broadcastService;
import static com.underiteration.lwms.service.Server.startServer;
import static com.underiteration.lwms.service.ServiceDiscoveryListener.listenForServices;

public class Runner {

	private static Logger logger = Logger.getLogger(Runner.class.getCanonicalName());

	protected static void start() {
		logger.info("Starting...");

		startServer();
		broadcastService();
		listenForServices();
	}
}
