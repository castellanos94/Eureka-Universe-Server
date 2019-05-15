package com.castellanos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

	private EurekaThreadManager() {
		taskQueue = new LinkedBlockingQueue<Runnable>();
		running = Collections.synchronizedList(new ArrayList<Runnable>());
		poolExecutor = new ThreadPoolExecutor(4, nThreads, 60, TimeUnit.SECONDS, taskQueue) {
			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				// TODO Auto-generated method stub
				super.beforeExecute(t, r);
				running.add(r);
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				// TODO Auto-generated method stub
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

	public void taskExecute(EurekaTask task) {
		poolExecutor.execute(new EurekaTaskRunnable(task));
	}
	/**
	 * See if the task ID exists in the execution of tasks or the task queue.
	 * @param id Eureka task id
	 * @return EurekaTaskRunnable from running/taskQueue
	 */
	public EurekaTaskRunnable getStatus(String id) {
		EurekaTaskRunnable t;
		for (Runnable r : running) {
			if (r instanceof EurekaTaskRunnable) {
				t = (EurekaTaskRunnable) r;
				if (t.getUuid().equals(id)) {
					return t;
				}
			}
		}
		Iterator<Runnable> iterator = taskQueue.iterator();
		while (iterator.hasNext()) {
			Runnable r = iterator.next();
			if (r instanceof EurekaTaskRunnable) {
				t = (EurekaTaskRunnable) r;
				if (t.getUuid().equals(id)) {
					return t;
				}
			}
		}

		return null;
	}
	
	/**
	 * Send an email notification to the user with the url to the results file or 
	 * the reasons for error so that the task will not be completed.
	 * @param r EurekaTaskRunnable
	 */
	private void postExecute(Runnable r) {
		if(r instanceof EurekaTaskRunnable) {
			EurekaTaskRunnable t = (EurekaTaskRunnable) r;
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
	 * @return List 
	 */
	public List<Runnable> getRunning() {
		return running;
	}

}
