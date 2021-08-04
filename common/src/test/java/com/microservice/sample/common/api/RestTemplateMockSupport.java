package com.microservice.sample.common.api;


import java.net.URI;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author yoshy0407
 *
 */
public class RestTemplateMockSupport {

	/**
	 * {@link RestTemplate#getForObject(URI, Class)}のモックを生成します
	 * @param <T>
	 * @param mock
	 * @param responseClass
	 * @param response
	 */
	public static <T> void mockGeyForObject(RestTemplate mock,  Class<T> responseClass, T response) {
		Mockito
			.when(mock.getForObject(Mockito.any(URI.class), responseClass))
			.thenReturn(response);
		
	}
	
	/**
	 * {@link RestTemplate#exchange(RequestEntity, ParameterizedTypeReference)}のモックを生成します
	 * 
	 * @param <T>
	 * @param uri
	 * @param mock
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	public static <T> void mockExchange(URI uri, RestTemplate mock, List<T> result) {
		RequestEntity<List<T>> req = new RequestEntity<>(HttpMethod.GET, uri);
		ResponseEntity<List<T>> res = new ResponseEntity<>(result, HttpStatus.OK);
		Mockito
			.when(mock.exchange(Mockito.eq(req), Mockito.any(ParameterizedTypeReference.class))).thenReturn(res);
	}
	
	/**
	 * {@link RestTemplate}のモックを作成します
	 * @return {@link RestTemplate}
	 */
	public static RestTemplate mock() {
		return Mockito.mock(RestTemplate.class);
	}
}
