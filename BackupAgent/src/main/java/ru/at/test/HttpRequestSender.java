package ru.at.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpRequestSender {
	private final Arguments arguments;

	@Autowired
	public HttpRequestSender(Arguments arguments) {
		this.arguments = arguments;
	}

	boolean registerWatchDirectory() {
		RestTemplate restTemplate = new RestTemplate();
		String directory = arguments.getBackupDirectory().replace("\\", "").replace("/", "");
		ResponseEntity response = restTemplate.exchange(
				String.format("http://%s:%s/register/%s", arguments.getServerHost(), arguments.getServerPort(), directory),
				HttpMethod.POST, null, ResponseEntity.class);

		return response.getStatusCode().is2xxSuccessful();
	}

	boolean uploadFile(Path path) {
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file", new FileSystemResource(path.toAbsolutePath().toFile()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
		RestTemplate restTemplate = new RestTemplate();
		String directory = new File(arguments.getBackupDirectory()).getName();
		String URL = String.format("http://%s:%s/upload/%s", arguments.getServerHost(), arguments.getServerPort(), directory);
		System.out.println(URL + "\t\t" + path);
		ResponseEntity<String> response = restTemplate.postForEntity(URL, requestEntity, String.class);

		return response.getStatusCode().is2xxSuccessful();
	}

	void deleteRemoteFile(Path path) {
		RestTemplate restTemplate = new RestTemplate();
		String directory = arguments.getBackupDirectory().replace("\\", "").replace("/", "");
		Map<String, String> map = new HashMap<>();
		map.put("file", path.getFileName().toString());
		restTemplate.delete(String.format("http://%s:%s/delete/%s", arguments.getServerHost(), arguments.getServerPort(), directory), map);
	}
}
