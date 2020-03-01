package com.castellanos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

/**
 * 
 * @author Castellanos Alvarez Alejandro Pendiente ejecutar y notificar cuando
 *         hay cambio.
 *
 */
public class EurekaTaskRunnable implements Runnable {
	private String uuid;
	private String start;
	private EurekaTask task;
	private ArrayList<String> console;
	private boolean ready;
	private static final String[] program = { "universe-cmd.exe", "unixProgram" };
	private String[] order;
	private String core_path;

	public EurekaTaskRunnable(String core_path, EurekaTask task) {
		this.task = task;
		this.uuid = task.getUuid();
		this.console = new ArrayList<>();
		this.core_path = core_path;
	}

	@Override
	public void run() {
		this.start = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String command;
		String outPath = task.getBasePath() + File.separator + "result.xls";
		task.setResultFile(outPath);
		File script = new File(task.getBasePath() + File.separator + "script.txt");
		script.deleteOnExit();
		FileWriter fw;

		try {
			fw = new FileWriter(script);

			fw.write(task.getScript());
			fw.close();
			String st = "";
			Scanner sc = new Scanner(script);
			while (sc.hasNext()) {
				String line = sc.nextLine();
				if (line.contains(":db-uri")) {
					String before = line.substring(0, line.indexOf(":db-uri"));
					st += before + ":db-uri " + '"'+task.getDataset()+'"';

				} else if (line.contains(":out-file")) {
					st += line.substring(0, line.indexOf(":out-file")) + ":out-file " + '"'+outPath+'"';
				} else {
					st += line;
				}
			}
			sc.close();
			fw = new FileWriter(script);
			fw.write(st);
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		order = new String[3];
		if (core_path.contains(program[1])) {
			command = core_path + " " + script.getAbsolutePath();
			order[0] = "/bin/bash";
			order[1] = "-c";
			order[2] = command;
		} else {
			command = core_path + " " + script.getAbsolutePath();
			order[0] = "cmd.exe";
			order[1] = "/c";
			order[2] = command;

		}
		this.ready = true;

		console.add(Arrays.toString(order));
		System.out.println(Arrays.toString(order));
		Runtime runtime = Runtime.getRuntime();
		Process pr = null;
		try {
			pr = runtime.exec(order);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

		String line;

		try {
			while ((line = input.readLine()) != null) {
				System.out.println(line);
				console.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int exitVal;
		try {
			exitVal = pr.waitFor();
			console.add("Exit value " + exitVal);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	public boolean containsError() {
		return console.toString().contains("error") || console.toString().contains("exception")
				|| !console.toString().contains("results saved");
	}

	public String getStart() {
		return start;
	}

}
