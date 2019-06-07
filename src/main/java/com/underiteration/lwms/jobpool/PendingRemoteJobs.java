package com.underiteration.lwms.jobpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PendingRemoteJobs {

	private static PendingRemoteJobs singleton = new PendingRemoteJobs();

	private final Map<String, CompletableFuture<String>> jobs = new HashMap<>();

	private PendingRemoteJobs() {

	}

	public static PendingRemoteJobs instance() {
		return singleton;
	}

	public CompletableFuture<String> recordPendingJob(String jobReference) {
		CompletableFuture<String> future = new CompletableFuture<>();
		jobs.put(jobReference, future);

		return future;
	}

	public void resolveFuture(String jobReference, String result) {

		jobs.remove(jobReference).complete(result);
	}

}
