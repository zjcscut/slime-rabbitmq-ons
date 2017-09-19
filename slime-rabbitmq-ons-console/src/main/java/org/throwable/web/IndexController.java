package org.throwable.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.throwable.common.model.dto.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/5 13:51
 */
@RestController
public class IndexController {

	@GetMapping(value = "/index", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Response<Object> index() {
		Map<String, Object> result = new LinkedHashMap<>();
		Runtime runtime = Runtime.getRuntime();
		result.put("os", System.getProperty("os.name", "UNKNOWN"));
		result.put("osVersion", System.getProperty("os.version", "UNKNOWN"));
		result.put("availableProcessors", runtime.availableProcessors());
		result.put("maxMemory", runtime.maxMemory());
		result.put("freeMemory", runtime.freeMemory());
		result.put("totalMemory", runtime.totalMemory());
		return new Response<>(result);
	}
}
