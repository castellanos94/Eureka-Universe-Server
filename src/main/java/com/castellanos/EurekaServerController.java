package com.castellanos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
/**
 * 
 * @author Castellanos Alvarez Alejandro
 *
 */
@RestController
@RequestMapping(path = "/demo")
public class EurekaServerController {
	private EurekaThreadManager manager = EurekaThreadManager.getInstance();
	private HashMap<Long, UserDao> users = new HashMap<Long, UserDao>();
	private long ids = 0;

	@PostMapping(path = "/users")
	public @ResponseBody UserDao createUser(@RequestParam String name, @RequestParam String email,
			@RequestParam String password) {
		UserDao user = new UserDao(ids++, name, email, password);
		users.put(user.getId(), user);
		return user;
	}

	@GetMapping(path = "/users")
	public @ResponseBody Iterable<UserDao> getAllUsers() {
		return users.values();
	}

	@GetMapping(path = "/tasks")
	public @ResponseBody List<String> getAllTasks() {
		List<String> l = new ArrayList<String>();
		manager.getMap().forEach((k, v) -> l.add(v.toString()));
		return l;
	}

	@GetMapping(path = "/tasks/{id}")
	public @ResponseBody BaseResponse checkStatus(@PathVariable("id") String id) {
		EurekaTaskExecution t = manager.getMap().get(id);
		BaseResponse br = new BaseResponse();
		br.setCode(100);
		br.setStatus((t.isAlive()) ? "Running..." : "Finished");
		br.setMsg((t.getConsole().isEmpty()) ? "" : t.getConsole().get(t.getConsole().size() - 1));
		return br;
	}

	@PostMapping(path = "/tasks")
	public @ResponseBody String runTask(@RequestParam String name, @RequestParam String script) {
		EurekaTask task = new EurekaTask();
		task.setName(name);
		task.setScript(script);
		task.setDataset(null);
		task.setUserId(0);
		// guardar en base de datos?
		manager.executeTask(task);
		return task.getUuid();
	}

}
