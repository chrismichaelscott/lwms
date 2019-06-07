package com.underiteration.lwms.service;

import com.underiteration.lwms.Constants;
import com.underiteration.lwms.config.ConfigAddresses;
import com.underiteration.lwms.config.ConfigManager;
import com.underiteration.lwms.jobpool.PoolManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

	private static final Server singleton = new Server();

	private static Logger logger = Logger.getLogger(Server.class.getCanonicalName());

	private final ExecutorService pool;
	private Integer listeningPort;

	static void startServer() {

		logger.info("Starting micro-service(s)");
		singleton.listen();
	}

	private Server() {

		pool = PoolManager.getDefaultPool();
	}

	private void listen() {

		var lowPort = ConfigManager.instance().getInteger(ConfigAddresses.MICRO_SERVICE_LOW_PORT).orElse(Constants.DEFAULT_MICRO_SERVICE_LOW_PORT);
		var highPort = ConfigManager.instance().getInteger(ConfigAddresses.MICRO_SERVICE_HIGH_PORT).orElse(Constants.DEFAULT_MICRO_SERVICE_HIGH_PORT);

		for (var port = lowPort; port <= highPort; port++) {

			try {
				var listener = new ServerSocket(port);
				this.listeningPort = port;

				PoolManager.getPool("_CORE", Constants.NUMBER_OF_CORE_LISTENERS)
						.execute(() -> listenInBackground(listener));

				logger.info(String.format("Listening for service calls on %s", port));
				break;
			} catch (IOException e) {
				logger.log(Level.FINE, e.getLocalizedMessage());
			}
		}

	}

	private void listenInBackground(ServerSocket listener) {
		while (true) {
			try {
				RequestHandler handler = new RequestHandler(listener.accept());
				pool.execute(handler);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage());
			}
		}
	}

	public static Integer getListeningPort() {

		return singleton.listeningPort;
	}
}
