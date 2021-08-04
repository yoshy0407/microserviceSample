package com.microservice.sample.common.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.rest.RestApiNode;
import com.microservice.sample.common.api.spi.ApiComposition;
import com.microservice.sample.common.api.spi.ApiNode;

/**
 * RestAPIを実行する{@link ApiComposition}の実装クラスです
 * 
 * @author yoshy0407
 *
 * @param <E> ベースとなるエンティティクラス（ベースエンティティ）
 * @param <RE> 結果となるエンティティクラス（結果エンティティ）
 */
public class ApiCompositionImpl<E, RE> implements ApiComposition<E, RE> {

	private List<ApiNode<?, RE>> apinodes = new ArrayList<>();
	
	private Function<E, RE> mapToResult;
	
	private RestTemplate restTemplate;
	
	/**
	 * インスタンスを生成します
	 * @param restTemplate {@link RestTemplate}
	 */
	public ApiCompositionImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> ApiNode<R, RE> apiCall(Class<R> responseClass, Function<List<RE>, URI> function) {
		RestApiNode<R, RE> node = new RestApiNode<>(restTemplate);
		node.callFor(responseClass, function);
		apinodes.add(node);
		return node;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApiComposition<E, RE> mapToResult(Function<E, RE> function) {
		this.mapToResult = function;
		return this;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RE> execute(List<E> entities) {
		Assert.notNull(mapToResult, "mapToResult is not null");
		
		List<RE> resultList = entities.stream()
					.map(mapToResult).collect(Collectors.toList());
		
		apinodes.forEach(n -> {
			n.validate();
		});
		
		apinodes.forEach(n -> {
			n.execute(resultList);
		});
		return resultList;
	}

}
