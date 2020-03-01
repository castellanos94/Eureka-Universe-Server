package com.castellanos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 
 * @author Castellanos Alvarez Alejandro
 *
 */
@RestController
@RequestMapping(path = "/demo")
public class EurekaServerController {
	private static final Logger logger = LoggerFactory.getLogger(EurekaServerController.class);

	@Autowired
	private FileStorageService fileStorageService;

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
		EurekaTaskRunnable t = null;
		for (Runnable r : manager.getRunning()) {
			if (r instanceof EurekaTaskRunnable) {
				t = (EurekaTaskRunnable) r;
				l.add(t.toString());

			}
		}
		return l;
	}

	@GetMapping(path = "/tasks/{id}")
	public @ResponseBody String checkStatus(@PathVariable("id") String id) {

		String t = null;
		try {
			t = manager.getStatus(id);
			if (t != null){
				return t;
			}
			else if (fileStorageService.checkResults(id)){
				return "/downloadFile/"+id;
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return id + " not found";
	}

	@PostMapping(path = "/tasks")
	public @ResponseBody String runTask(@RequestParam String name, @RequestParam String script,
			@RequestParam MultipartFile file, ModelMap modelMap) {
		modelMap.addAttribute("name", name);
		modelMap.addAttribute("script", script);
		modelMap.addAttribute("file", file);

		EurekaTask task = new EurekaTask();
		String path = fileStorageService.storeFile(task.getUuid(), file);

		task.setName(name);
		task.setScript(script);
		task.setDataset(path + File.separator + file.getOriginalFilename());
		task.setUserId(0);
		task.setBasePath(path);
		manager.taskExecute(fileStorageService.getCore(), task);
		return task.getUuid();
	}

	@GetMapping("/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@PreDestroy
	private void onDestroy() {
		manager.onDestroy();
	}
}
