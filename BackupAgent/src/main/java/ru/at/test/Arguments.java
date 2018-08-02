package ru.at.test;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Data
public class Arguments {
	@Value("${backup.dir}")
	private String backupDirectory;
	@Value("${backup_server.host}")
	private String serverHost;
	@Value("${backup_server.port}")
	private String serverPort;

	@PostConstruct
	private void init() {
		backupDirectory = backupDirectory.replace("\\", "").replace("/", "");
	}
}
