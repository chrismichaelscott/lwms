package com.underiteration.lwms.jobpool;

import com.underiteration.lwms.config.ConfigAddresses;
import com.underiteration.lwms.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolManager {

	private static Map<String, ExecutorService> pools = new HashMap<>();

	public static ExecutorService getPool(String poolName, int poolSize) {
		return pools.computeIfAbsent(poolName, __ -> Executors.newFixedThreadPool(poolSize));
	}

	public static ExecutorService getDefaultPool() {

		Integer poolSize = ConfigManager.instance().getInteger(ConfigAddresses.DEFAULT_THREAD_POOL_SIZE).orElse(Runtime.getRuntime().availableProcessors() * 2);
		return getPool("_DEFAULT", poolSize);
	}

}
