package ru.at.test;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class Arguments {
	@Value("${backup.dir}")
	private String backupDirectory;
	@Value("${server.host}")
	private String serverHost;
	@Value("${server.portt}")
	private String serverPort;
}
