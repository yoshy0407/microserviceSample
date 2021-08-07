package com.microservice.sample.common.api.rest;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.microservice.sample.common.api.Key;
import com.microservice.sample.common.api.spi.JoinApiCaller;
import com.microservice.sample.common.api.spi.QueryApiCompositionBuilder;

/**
 * {@link JoinApiCaller}の抽象クラス
 * @author yoshy0407
 *
 * @param <BE> ベースエンティティ
 * @param <R> レスポンスクラス
 * @param <RE> 結合エンティティ
 */
public abstract class AbstractJoinApiCaller<BE, R, RE> implements JoinApiCaller<BE, R, RE>{

	private Function<List<BE>, List<R>> apiCall;
	
	private Function<RE, Key> resultFunction;
	
	private Function<R, Key> responseFunction;
	
	private BiConsumer<RE, R> mapToEntity;
	
	private List<R> responseList;
	
	private QueryApiCompositionBuilder<BE, RE> parent;
	
	/**
	 * コンストラクタ
	 * 
	 * @param response レスポンスクラス
	 * @param apiCall URIを解決する{link {@link Function}
	 * @param parent 親のインスタンス
	 */
	public AbstractJoinApiCaller(Class<R> response, Function<List<BE>, List<R>> apiCall, 
			QueryApiCompositionBuilder<BE, RE> parent) {
		this.apiCall = apiCall;
		this.parent = parent;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JoinApiCaller<BE, R, RE> onEqual(Function<RE, Key> resultFunction, Function<R, Key> responseFunction) {
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
	public QueryApiCompositionBuilder<BE, RE> and() {
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
	 * APIを呼び出します
	 * @param baseEntities ベースとなるエンティティのリスト
	 * @return APIの呼び出し結果
	 */
	public List<R> call(List<BE> baseEntities) {
		this.responseList = apiCall.apply(baseEntities);
		return this.responseList;
	}
	
	/**
	 * レスポンスから結合エンティティにマッピングします
	 * 
	 * @param resultEntities 結合エンティティ
	 */
	public void mapToResult(List<RE> resultEntities) {
		Map<Key, R> responseMap = responseList.stream()
				.collect(Collectors.toMap(r -> responseFunction.apply(r), r -> r));

		int i = 0;
		for (RE re : resultEntities) {
			Key key = getResultEntityKey(re);
			R response = responseMap.get(key);
			
			if (response == null) {
				doResponseNull(resultEntities, i);
			} else {
				mapToEntity.accept(re, response);
			}
			i++;
		}
	}
	
	protected abstract void doResponseNull(List<RE> resultList, int index);
	
	
	protected Key getResultEntityKey(RE entity) {
		return resultFunction.apply(entity);
	}
	
}
