package ru.at.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by A.Tregubenkov on 02.08.2018.
 */
@Component
public class FileSystemHelper {
	@Value("${backup.dir}")
	private String rootDirectory;
	private static Logger logger = LoggerFactory.getLogger(FileSystemHelper.class);

	public boolean createDirectory(String client, String directory) {
		File destinationDirectory = new File(rootDirectory + "/" + client + "/" + directory + "/");
		if (destinationDirectory.exists())
			destinationDirectory.delete();

		if (destinationDirectory.mkdirs()) {
			logger.info("Registered client's " + client + " directory " + directory);
			return true;
		} else {
			logger.error("Failed to register client's " + client + " directory " + directory);
			return false;
		}
	}

	public boolean saveFile(String client, String directory, MultipartFile file) throws IOException {
		String destinationDirectoryPath = rootDirectory + "/" + client + "/" + directory + "/";
		File destinationDirectory = new File(destinationDirectoryPath);
		if (!destinationDirectory.exists())
			throw new IOException("Destination directory does not exists. Path: " + destinationDirectory.getAbsolutePath());
		String originalFilename = file.getOriginalFilename();
		File destinationFile = new File(destinationDirectoryPath + originalFilename);
		if (destinationFile.exists()) {
			logger.info("Deleting existing file " + destinationFile.getAbsolutePath());
			destinationFile.delete();
		}
		Path target = Paths.get(destinationDirectoryPath).resolve(originalFilename);
		Files.copy(file.getInputStream(), target);
		logger.info("Uploaded " + destinationDirectoryPath + originalFilename);

		return target.toFile().exists();
	}

	public boolean deleteFile(String client, String directory, String file) throws IOException {
		String destinationDirectoryPath = rootDirectory + "/" + client + "/" + directory + "/";
		File destinationDirectory = new File(destinationDirectoryPath);
		if (!destinationDirectory.exists())
			throw new IOException("Destination directory does not exists. Path: " + destinationDirectory.getAbsolutePath());
		String pathname = destinationDirectoryPath + file;
		File fileToDelete = new File(pathname);
		if (fileToDelete.exists()) {
			logger.info("Deleting " + pathname);
			return fileToDelete.delete();
		} else
			throw new IOException("File " + file + " does not exist");
	}
}
