package com.microservice.sample.common.api.rest;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.Key;
import com.microservice.sample.common.api.spi.JoinApiCaller;
import com.microservice.sample.common.api.spi.QueryApiComposition;

/**
 * {@link JoinApiCaller}の抽象クラス
 * @author yoshy0407
 *
 * @param <BE> ベースエンティティ
 * @param <R> レスポンスクラス
 * @param <RE> 結合エンティティ
 */
public abstract class AbstractJoinApiCaller<BE, R, RE> implements JoinApiCaller<BE, R, RE>{

	private Class<R> response;
	
	private Function<List<RE>, List<R>> apiCall;
	
	private Function<RE, Key> resultFunction;
	
	private Function<R, Key> responseFunction;
	
	private BiConsumer<RE, R> mapToEntity;
	
	private QueryApiComposition<BE, RE> parent;
	
	/**
	 * コンストラクタ
	 * 
	 * @param response レスポンスクラス
	 * @param apiCall URIを解決する{link {@link Function}
	 * @param parent 親のインスタンス
	 */
	public AbstractJoinApiCaller(Class<R> response, Function<List<RE>, List<R>> apiCall, 
			QueryApiComposition<BE, RE> parent) {
		this.response = response;
		this.apiCall = apiCall;
		this.parent = parent;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JoinApiCaller<BE, R, RE> on(Function<RE, Key> resultFunction, Function<R, Key> responseFunction) {
		this.resultFunction = resultFunction;
		this.responseFunction = responseFunction;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JoinApiCaller<BE, R, RE> mapToEntity(BiConsumer<RE, R> function) {
		this.mapToEntity = function;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryApiComposition<BE, RE> and() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		Assert.notNull(resultFunction, "resultFunction must not null");
		Assert.notNull(responseFunction, "responseFunction must not null");
		Assert.notNull(mapToEntity, "mapToEntity must not null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(List<RE> resultList) {
		Map<Key, R> responseMap = get(resultList);
		
		int i = 0;
		for (RE re : resultList) {
			Key key = getResultEntityKey(re);
			R response = responseMap.get(key);
			
			if (response == null) {
				doResponseNull(resultList, i);
			} else {
				mapToEntity.accept(re, response);
			}
			i++;
		}		
	}
	
	protected abstract void doResponseNull(List<RE> resultList, int index);
	
	protected Map<Key, R> get(List<RE> list){
		List<R> responses = apiCall.apply(list);
		return responses.stream()
					.collect(Collectors.toMap(r -> responseFunction.apply(r), r -> r));
	}
	
	protected Key getResultEntityKey(RE entity) {
		return resultFunction.apply(entity);
	}
	
	
}
