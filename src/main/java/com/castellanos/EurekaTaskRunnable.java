package com.castellanos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.Map;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

/**
 * 
 * @author Castellanos Alvarez Alejandro Pendiente ejecutar y notificar cuando
 *         hay cambio.
 *
 */
public class EurekaTaskRunnable implements Callable {
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

	public boolean isReady() {
		return ready;
	}

	@Override
	public String toString() {
		return String.format("id: %s, start: %s, name: %s, log : %s", uuid, start, task.getName(), console.toString().replaceAll(task.getBasePath(), ""));
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

	@Override
	public Object call() throws Exception {
		this.start = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String command;
		String outPath = task.getBasePath() + File.separator + "result.xls";
		task.setResultFile(outPath);
		File script = new File(task.getBasePath() + File.separator + "script.txt");
		script.deleteOnExit();
		FileWriter fw;

		try {
			Parseable pbr = Parsers.newParseable(new StringReader(task.getScript()));
			Parser p = Parsers.newParser(Parsers.defaultConfiguration());
			Map<?, ?> map = (Map<?, ?>) p.nextValue(pbr);

			Keyword jobKey = Keyword.newKeyword("job"), queryKey = Keyword.newKeyword("query"),
					dbKey = Keyword.newKeyword("db-uri"), dbOut = Keyword.newKeyword("out-file");
			Map<?, ?> queryMap = (Map<?, ?>) map.get(queryKey);
			String path = (String) queryMap.get(dbKey);
			String out = (String) queryMap.get(dbOut);
			pbr.close();
			String string_script = task.getScript().replace(path, task.getDataset()).replace(out, outPath);
			
			fw = new FileWriter(script);

			fw.write(string_script);
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

		//console.add(Arrays.toString(order));
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
			e.printStackTrace();
		}

		int exitVal;
		try {
			exitVal = pr.waitFor();
			console.add("Exit value " + exitVal);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this;
	}

}
