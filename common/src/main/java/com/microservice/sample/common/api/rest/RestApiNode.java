package com.microservice.sample.common.api.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.Key;
import com.microservice.sample.common.api.spi.ApiNode;

/**
 * RestAPIを実行する{@link ApiNode}です
 * 
 * @author yoshy0407
 *
 * @param <R> レスポンスオブジェクト
 * @param <E> 結果となるとなるエンティティクラス
 */
public class RestApiNode<R, E> implements ApiNode<R, E> {

	private Class<R> response;
	
	private Function<List<E>, URI> urlFromEntity;
	
	private ResponseMapper<E, R> mapper;
	
	private RestTemplate restTemplate;
	
	/**
	 * インスタンスを生成します
	 * @param restTemplate {@link RestTemplate}
	 */
	public RestApiNode(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApiNode<R, E> callFor(Class<R> response, Function<List<E>, URI> function) {
		this.response = response;
		this.urlFromEntity = function;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseMapper<E, R> map() {
		this.mapper = new ResponseMapper<>();
		return this.mapper;
	}
	
	/**
	 * 
	 */
	@Override
	public void validate() {
		Assert.notNull(response, "response Class must not null");
		Assert.notNull(urlFromEntity, "callFor Function must not null");
		Assert.notNull(mapper, "mapper must not null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(List<E> resultEntities) {
		List<R> response = get(urlFromEntity.apply(resultEntities));
		
		Map<Key, R> responseMap = prepareResponseMap(response);
		resultEntities.forEach(e -> {
			Key key = mapper.getEntityKey(e);
			R res = responseMap.get(key);
			mapper.map(e, res);
		});		
	}
	
	private List<R> get(URI uri){
		RequestEntity<List<R>> entity = new RequestEntity<>(HttpMethod.GET, uri);
		ResponseEntity<List<R>> response = restTemplate
				.exchange(entity, new ParameterizedTypeReference<List<R>>() {});
		return response.getBody();
	}
	
	private Map<Key, R> prepareResponseMap(List<R> list){
		return list.stream()
			.collect(Collectors.toMap(r -> mapper.getResponseKey(r), r -> r));
	}

}
