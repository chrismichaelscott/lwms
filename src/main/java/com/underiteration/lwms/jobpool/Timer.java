package com.underiteration.lwms.jobpool;

import com.underiteration.lwms.Constants;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class Timer {

	private final static ExecutorService executorService = PoolManager.getPool("_TIMER", 1);

	private static Timer singleton = new Timer();
	private static LinkedList<FutureExecution> jobs;

	private Timer() {
		jobs = new LinkedList<FutureExecution>();

		executorService.execute(this::checkQueueIteratively);
	}

	private void checkQueueIteratively() {

		while (true) {
			checkQueue();

			try {
				Thread.sleep(Constants.TIMER_GRANULARITY);
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkQueue() {

		jobs.sort((a, b) -> Long.valueOf(a.runAfter - b.runAfter).intValue());

		long now = new Date().getTime();
		if (! jobs.isEmpty() && jobs.peek().runAfter < now) {

			FutureExecution nextJob = jobs.pop();
			nextJob.executorService.execute(nextJob.runnable);
		}
	}

	public static void runAfter(Runnable runnable, ExecutorService executorService, long milliseconds) {

		FutureExecution executorAndRunnable = new FutureExecution();
		executorAndRunnable.executorService = executorService;
		executorAndRunnable.runnable = runnable;
		executorAndRunnable.runAfter = new Date().getTime() + milliseconds;

		jobs.add(executorAndRunnable);
	}

	private static class FutureExecution {
		public Runnable runnable;
		public ExecutorService executorService;
		public Long runAfter;
	}
}
