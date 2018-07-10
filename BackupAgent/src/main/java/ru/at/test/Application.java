package ru.at.test;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

@SpringBootApplication
public class Application {
	private static Logger logger = Logger.getLogger(Application.class);
	
	/**
	 * @param args [0] = backup dir,
	 *             [1] = server host,
	 *             [2] = server port
	 */
	public static void main(String[] args) throws IOException {
		try {
			checkArguments(args);
			args[0] = "--backup.dir=" + args[0];
			args[1] = "--server.host=" + args[1];
			args[2] = "--server.port=" + args[2];
			SpringApplication.run(Application.class, args);
		} catch (IllegalArgumentException e) {
			if (e instanceof NumberFormatException)
				logger.error("Invalid program arguments! Port must be integer value");
			else
				logger.error(e.getMessage());
			System.in.read();
		}
	}

	private static void checkArguments(String[] args) throws IllegalArgumentException {
		if (args.length != 3)
			throw new IllegalArgumentException("Invalid program arguments! There is should be 3 arguments: backup_directory, server ip and server port");

		if (!args[1].equalsIgnoreCase("localhost")) {
			// IP validation
			Pattern pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
			if (!pattern.matcher(args[1]).matches())
				throw new IllegalArgumentException("Invalid program arguments! Invalid server host");
		}

		int port = Integer.parseInt(args[2]);
		if (port < 1024 || port > 65535)
			throw new IllegalArgumentException("Invalid program arguments! Port must be between 1024 and 65535");

		File backupDir = new File(args[0]);
		boolean success = false;
		if (backupDir.exists()) {
			if (backupDir.isFile())
				throw new IllegalArgumentException("Invalid program arguments! Invalid path to backup directory: " + backupDir.getAbsolutePath());
			if (backupDir.isDirectory())
				success = true;
		}
		if (!success)
			success = backupDir.mkdirs();
		if (!success)
			throw new IllegalArgumentException("Can not create backup directory. Path: " + backupDir.getAbsolutePath());
	}
}
