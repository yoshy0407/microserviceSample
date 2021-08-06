package com.microservice.sample.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.rest.InnerJoinApiCaller;
import com.microservice.sample.common.api.rest.OuterJoinApipCaller;
import com.microservice.sample.common.api.spi.JoinApiCaller;
import com.microservice.sample.common.api.spi.QueryApiComposition;

/**
 * {@link QueryApiComposition}の実装クラス
 * 
 * @author yoshy0407
 *
 * @param <BE> ベースとなるエンティティ
 * @param <RE> 結合するエンティティ
 */
public class QueryApiCompositionImpl<BE, RE> implements QueryApiComposition<BE, RE> {

	private List<BE> baseEntities;
	
	private Function<BE, RE> mapToResult;
	
	private List<JoinApiCaller<BE, ?, RE>> apis = new ArrayList<>();
	
	/**
	 * インスタンスを生成します
	 * 
	 * @param baseEntities ベースとなるエンティティのリスト
	 */
	public QueryApiCompositionImpl(List<BE> baseEntities) {
		this.baseEntities = baseEntities;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> JoinApiCaller<BE, R, RE> innerJoin(Class<R> response, Function<List<RE>, List<R>> function) {
		InnerJoinApiCaller<BE, R, RE> inner =  new InnerJoinApiCaller<>(response, function, this);
		apis.add(inner);
		return inner;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> JoinApiCaller<BE, R, RE> outerJoin(Class<R> response, Function<List<RE>, List<R>> function) {
		OuterJoinApipCaller<BE, R, RE> outer = new OuterJoinApipCaller<>(response, function, this);
		apis.add(outer);
		return outer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryApiComposition<BE, RE> mapToResult(Function<BE, RE> function) {
		this.mapToResult = function;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RE> execute() {
		Assert.notNull(mapToResult, "mapToResult is not null");
		
		apis.forEach(api -> {
			api.validate();
		});
		
		List<RE> resultList = baseEntities.stream()
					.map(mapToResult).collect(Collectors.toList());
		
		apis.forEach(n -> {
			n.execute(resultList);
		});
		return resultList;
	}

}
