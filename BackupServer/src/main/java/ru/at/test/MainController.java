package ru.at.test;

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
	@Value("${backup.dir}")
	private String rootDirectory;
	@Value("0:0:0:0:0:0:0:1")
	private String localhostIPV6;

	@GetMapping("/")
	public String index(HttpServletRequest request) {
		return "Your public IP is " + request.getRemoteAddr();
	}

	@PostMapping("/register/{directory}")
	public ResponseEntity registerDirectory(@PathVariable("directory") String directory, HttpServletRequest request) {
		String client = request.getRemoteAddr().equals(localhostIPV6) ? "localhost" : request.getRemoteAddr();
		File destinationDirectory = new File(rootDirectory + "/" + client + "/" + directory + "/");
		if (destinationDirectory.exists())
			destinationDirectory.delete();
		destinationDirectory.mkdirs();

		return ResponseEntity.ok().build();
	}

	@PostMapping("/upload/{directory}")
	public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file, @PathVariable("directory") String directory, HttpServletRequest request) throws IOException {
		String client = request.getRemoteAddr().equals(localhostIPV6) ? "localhost" : request.getRemoteAddr();
		String destinationDirectoryPath = rootDirectory + "/" + client + "/" + directory + "/";
		File destinationDirectory = new File(destinationDirectoryPath);
		if (!destinationDirectory.exists())
			throw new IOException("Destination directory does not exists. Path: " + destinationDirectory.getAbsolutePath());
		String originalFilename = file.getOriginalFilename();
		File destinationFile = new File(destinationDirectoryPath + originalFilename);
		if (destinationFile.exists()) {
			System.out.println("DELETING EXISTING FILE " + destinationFile.getAbsolutePath());
			System.out.println(destinationFile.delete());
		}
		Files.copy(file.getInputStream(), Paths.get(destinationDirectoryPath).resolve(originalFilename));
//		file.transferTo(destinationFile);
		System.out.println("UPLOADING " + destinationDirectoryPath + originalFilename);
//		Files.copy(file.getInputStream(), new File(destinationDirectoryPath + file.getOriginalFilename()).toPath());

		return ResponseEntity.ok().body("Successfully uploaded " + originalFilename);
	}

	@DeleteMapping("/delete/{directory}")
	public ResponseEntity deleteFile(@PathVariable("directory") String directory, @RequestParam("file") String file, HttpServletRequest request) throws IOException {
		String client = request.getRemoteAddr().equals(localhostIPV6) ? "localhost" : request.getRemoteAddr();
		String destinationDirectoryPath = rootDirectory + "/" + client + "/" + directory + "/";
		File destinationDirectory = new File(destinationDirectoryPath);
		if (!destinationDirectory.exists())
			throw new IOException("Destination directory does not exists. Path: " + destinationDirectory.getAbsolutePath());
		String pathname = destinationDirectoryPath + file;
		File fileToDelete = new File(pathname);
		if (fileToDelete.exists()) {
			System.out.println("DELETING " + pathname);
			fileToDelete.delete();
		}

		return ResponseEntity.ok().build();
	}

	@Bean
	public MultipartFilter multipartFilter() {
		return new MultipartFilter();
	}
}
