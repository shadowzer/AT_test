package ru.at.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws IOException {
        try {
            checkArguments(args);
            args[0] = "--server.port=" + args[0];
            args[1] = "--backup.dir=" + args[1];
            SpringApplication.run(Application.class, args);
        } catch (IllegalArgumentException e) {
            if (e instanceof NumberFormatException)
                System.err.println("Invalid program arguments!\nPort must be integer value");
            else
                System.err.println(e.getMessage());
            System.out.println("Press [ENTER] to exit program.");
            System.in.read();
        }
    }

    private static void checkArguments(String[] args) throws IllegalArgumentException {
        if (args.length != 2)
            throw new IllegalArgumentException("Invalid program arguments!\nThere is should be 2 arguments: port and backup_directory");
        int port = Integer.parseInt(args[0]);
        if (port < 1024 || port > 65535)
            throw new IllegalArgumentException("Invalid program arguments!\nPort must be between 1024 and 65535");

        File backupDir = new File(args[1]);
        boolean success = false;
        if (backupDir.exists()) {
            if (backupDir.isFile())
                throw new IllegalArgumentException("Invalid program arguments!\nInvalid path to backup directory: " + backupDir.getAbsolutePath());
            if (backupDir.isDirectory())
                success = true;
        }
        if (!success)
            success = backupDir.mkdirs();
        if (!success)
            throw new IllegalArgumentException("Can not create backup directory. Path: " + backupDir.getAbsolutePath());
    }
}
