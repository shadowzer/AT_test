package ru.at.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;

@Component
public class HttpRequestSender {
	private final Arguments arguments;

	@Autowired
	public HttpRequestSender(Arguments arguments) {
		this.arguments = arguments;
	}

	boolean registerWatchDirectory() {
		RestTemplate restTemplate = new RestTemplate();
		String URL = String.format("http://%s:%s/register/%s", arguments.getServerHost(), arguments.getServerPort(), arguments.getBackupDirectory());
		ResponseEntity response = restTemplate.postForEntity(URL, null, ResponseEntity.class);

		return response.getStatusCode().is2xxSuccessful();
	}

	boolean uploadFile(Path path) {
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file", new FileSystemResource(path.toAbsolutePath().toFile()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
		RestTemplate restTemplate = new RestTemplate();
		String URL = String.format("http://%s:%s/upload/%s", arguments.getServerHost(), arguments.getServerPort(), arguments.getBackupDirectory());
		ResponseEntity<String> response = restTemplate.postForEntity(URL, requestEntity, String.class);

		return response.getStatusCode().is2xxSuccessful();
	}

	void deleteRemoteFile(Path path) {
		RestTemplate restTemplate = new RestTemplate();
		String URL = String.format("http://%s:%s/delete/%s?file=%s", arguments.getServerHost(),
				arguments.getServerPort(), arguments.getBackupDirectory(), path.getFileName().toString());
		restTemplate.delete(URL);
	}
}
