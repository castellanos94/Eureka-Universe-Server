package com.castellanos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 
 * @author Castellanos Alvarez Alejandro
 * Pendiente ejecutar y notificar cuando hay cambio.
 *
 */
public class EurekaTaskExecution extends Thread {
	private String uuid;
	private String start;
	private EurekaTask task;
	private ArrayList<String> console;
	private boolean ready;

	public EurekaTaskExecution(EurekaTask task) {
		this.task = task;
		this.uuid = task.getUuid();
	}

	public void run() {
		this.start = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		this.ready = true;
	}

	public boolean isReady() {
		return ready;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", uuid, start, task.getName());
	}

	public String getUuid() {
		return uuid;
	}

	public ArrayList<String> getConsole() {
		return console;
	}

}
