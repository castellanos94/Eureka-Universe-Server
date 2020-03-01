package com.castellanos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 
 * @author Castellanos Alvarez Alejandro
 *
 */
public class EurekaThreadManager {
	private static EurekaThreadManager INSTANCE;
	private int nThreads = 10;
	private ThreadPoolExecutor poolExecutor;
	private LinkedBlockingQueue<Runnable> taskQueue;
	private List<Runnable> running;
	private List<EurekaTaskRunnable> monitor;

	private EurekaThreadManager() {
		taskQueue = new LinkedBlockingQueue<Runnable>();
		running = Collections.synchronizedList(new ArrayList<Runnable>());
		monitor = Collections.synchronizedList(new ArrayList<EurekaTaskRunnable>());
		poolExecutor = new ThreadPoolExecutor(4, nThreads, 60, TimeUnit.SECONDS, taskQueue) {
			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);

				running.add(r);
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				running.remove(r);
				postExecute(r);
			}
		};
	}

	private synchronized static void createInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EurekaThreadManager();
		}
	}

	public static EurekaThreadManager getInstance() {
		if (INSTANCE == null) {
			createInstance();
		}
		return INSTANCE;
	}

	public void taskExecute(String core_path, EurekaTask task) {
		EurekaTaskRunnable t = new EurekaTaskRunnable(core_path, task);
		monitor.add(t);
		poolExecutor.submit(t);

	}

	public void onDestroy() {
		System.out.println("Method invoke: destroy");
		poolExecutor.shutdown();
	}

	/**
	 * See if the task ID exists in the execution of tasks or the task queue.
	 * 
	 * @param id Eureka task id
	 * @return Status from monitor
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public String getStatus(String id) throws InterruptedException, ExecutionException {
		EurekaTaskRunnable t;
		Iterator<EurekaTaskRunnable> iterator = monitor.iterator();
		while (iterator.hasNext()) {
			t = iterator.next();
			if (t.getUuid().equals(id)) {
				return t.toString();
			}
		}

		return null;
	}

	/**
	 * Send an email notification to the user with the url to the results file or
	 * the reasons for error so that the task will not be completed.
	 * 
	 * @param r EurekaTaskRunnable
	 */
	private void postExecute(Runnable r) {
		EurekaTaskRunnable t = null;
		try {
			t = (EurekaTaskRunnable) ((FutureTask) r).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (t != null) {
			monitor.remove(t);

		}

	}

	public int getnThreads() {
		return nThreads;
	}

	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}

	/**
	 * Return all running task
	 * 
	 * @return List
	 */
	public List<Runnable> getRunning() {
		return running;
	}

	public List<EurekaTaskRunnable> getMonitor() {
		return monitor;
	}
}
