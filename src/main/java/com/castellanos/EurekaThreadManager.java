package com.castellanos;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author Castellanos Alvarez Alejandro
 *
 */
public class EurekaThreadManager {
	private static EurekaThreadManager INSTANCE;
	private int nThreads = 10;
	private ConcurrentHashMap<String, EurekaTaskExecution> map;
	private ExecutorService pool;

	private EurekaThreadManager() {
		setMap(new ConcurrentHashMap<String, EurekaTaskExecution>());
		pool = Executors.newFixedThreadPool(nThreads);
		// Monitor elimina elemento que ya este listo del map
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Iterator<Entry<String, EurekaTaskExecution>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, EurekaTaskExecution> entry = iterator.next();
					if (entry.getValue().isReady()) {
						iterator.remove();
					}
				}
			}
		}).start();
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

	public ConcurrentHashMap<String, EurekaTaskExecution> getMap() {
		return map;
	}

	private void setMap(ConcurrentHashMap<String, EurekaTaskExecution> map) {
		this.map = map;
	}

	public String executeTask(EurekaTask t) {
		EurekaTaskExecution r = new EurekaTaskExecution(t);
		map.put(t.getUuid(), r);
		pool.execute(r);
		return r.getUuid();
	}

	public int getnThreads() {
		return nThreads;
	}

	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}

	public ExecutorService getPool() {
		return pool;
	}

	public void setPool(ExecutorService pool) {
		this.pool = pool;
	}

}
