package ru.at.test;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class FileChangeWatcher {
	private static Logger logger = Logger.getLogger(FileChangeWatcher.class);

	private Arguments arguments;
	private HttpRequestSender sender;

	@Autowired
	public FileChangeWatcher(Arguments arguments, HttpRequestSender sender) {
		this.arguments = arguments;
		this.sender = sender;
	}

	@PostConstruct
	public void watchDirectory() {
		Path path = new File(arguments.getBackupDirectory()).toPath();
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(path,"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder)
				throw new IllegalArgumentException("Path: " + path + " is not a folder");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (!sender.registerWatchDirectory())
			throw new IllegalArgumentException("Can not register new watch directory.");
		for (File file : Objects.requireNonNull(new File(arguments.getBackupDirectory()).listFiles())) {
			if (sender.uploadFile(file.toPath()))
				logger.info("Successfully uploaded " + file);
			else
				logger.error("Failed to upload " + file);
		}

		logger.info("Watching path: " + path);
		FileSystem fs = path.getFileSystem();
		try (WatchService service = fs.newWatchService()) {
			path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

			// start the infinite polling loop
			WatchKey key;
			WatchEvent.Kind<?> eventType;
			boolean repeatFixFlag = false;
			while (true) {
				key = service.take();
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					eventType = watchEvent.kind();
					Path file = path.resolve(((WatchEvent<Path>) watchEvent).context().getFileName());
					if (eventType == ENTRY_CREATE) {
						repeatFixFlag = true;
						logger.info("Created local file " + file);
						if (sender.uploadFile(file))
							logger.info("Successfully uploaded " + file);
						else
							logger.error("Failed to upload " + file);
						break;
					} else if (eventType == ENTRY_MODIFY) {
						if (!repeatFixFlag) {
							logger.info("Modified local file " + file);
							if (sender.uploadFile(file))
								logger.info("Successfully uploaded " + file);
							else
								logger.error("Failed to upload " + file);
						}
						repeatFixFlag = !repeatFixFlag;
						break;
					} else if (eventType == ENTRY_DELETE) {
						logger.info("Deleted local file " + file);
						sender.deleteRemoteFile(file);
						logger.info("Successfully deleted remote file " + file);
						break;
					}
				}

				if (!key.reset())
					break;
			}
		} catch (IOException | InterruptedException ioe) {
			ioe.printStackTrace();
		}
	}
}
