package ru.at.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MultipartFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class MainController {
	private final FileSystemHelper fileSystemHelper;
	private final String localhostIPV6;
	private final String localhostIPV4;

	@Autowired
	public MainController(FileSystemHelper fileSystemHelper) {
		this.fileSystemHelper = fileSystemHelper;
		this.localhostIPV6 = "0:0:0:0:0:0:0:1";
		this.localhostIPV4 = "127.0.0.1";
	}

	@GetMapping("/")
	public String index(HttpServletRequest request) {
		return "Your public IP is " + request.getRemoteAddr();
	}

	@PostMapping("/register/{directory}")
	public ResponseEntity registerDirectory(@PathVariable("directory") String directory, HttpServletRequest request) {
		String client = request.getRemoteAddr().equals(localhostIPV6) ? localhostIPV4 : request.getRemoteAddr();
		if (fileSystemHelper.createDirectory(client, directory))
			return ResponseEntity.ok().build();
		else
			return ResponseEntity.badRequest().body("Can not register directory " + directory);
	}

	@PostMapping("/upload/{directory}")
	public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file, @PathVariable("directory") String directory, HttpServletRequest request) throws IOException {
		String client = request.getRemoteAddr().equals(localhostIPV6) ? localhostIPV4 : request.getRemoteAddr();
		if (fileSystemHelper.saveFile(client, directory, file))
			return ResponseEntity.ok("Successfully uploaded " + file.getOriginalFilename());
		else
			return ResponseEntity.badRequest().body("Can not save file " + file.getOriginalFilename());
	}

	@DeleteMapping("/delete/{directory}")
	public ResponseEntity<String> deleteFile(@PathVariable("directory") String directory, @RequestParam("file") String file, HttpServletRequest request) throws IOException {
		String client = request.getRemoteAddr().equals(localhostIPV6) ? localhostIPV4 : request.getRemoteAddr();
		if (fileSystemHelper.deleteFile(client, directory, file))
			return ResponseEntity.ok("Successfully deleted " + file);
		else
			return ResponseEntity.badRequest().body("Can not delete file " + file);
	}

	@Bean
	public MultipartFilter multipartFilter() {
		return new MultipartFilter();
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity handleException(IOException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
}
