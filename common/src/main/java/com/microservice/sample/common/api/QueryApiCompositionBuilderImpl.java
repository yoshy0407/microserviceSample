package com.microservice.sample.common.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import com.microservice.sample.common.api.rest.AbstractJoinApiCaller;
import com.microservice.sample.common.api.rest.InnerJoinApiCaller;
import com.microservice.sample.common.api.rest.OuterJoinApipCaller;
import com.microservice.sample.common.api.spi.JoinApiCaller;
import com.microservice.sample.common.api.spi.QueryApiCompositionBuilder;

/**
 * {@link QueryApiCompositionBuilder}の実装クラス
 * 
 * @author yoshy0407
 *
 * @param <BE> ベースとなるエンティティ
 * @param <RE> 結合するエンティティ
 */
public class QueryApiCompositionBuilderImpl<BE, RE> implements QueryApiCompositionBuilder<BE, RE> {

	private List<BE> baseEntities;

	private Class<RE> resultClass;

	private Optional<Function<BE, RE>> mapToResult = Optional.empty();

	private ModelMapper modelMapper = new ModelMapper();

	private List<AbstractJoinApiCaller<BE, ?, RE>> apis = new ArrayList<>();
	
	private ThreadPoolTaskExecutor threadPool;

	/**
	 * インスタンスを生成します
	 * 
	 * @param threadPool
	 */
	public QueryApiCompositionBuilderImpl(ThreadPoolTaskExecutor threadPool) {
		this.threadPool = threadPool;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryApiCompositionBuilder<BE, RE> select(Class<RE> resultClass, Function<BE, RE> function) {
		this.resultClass = resultClass;
		this.mapToResult = Optional.of(function);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryApiCompositionBuilder<BE, RE> select(Class<RE> resultClass) {
		this.resultClass = resultClass;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryApiCompositionBuilder<BE, RE> from(List<BE> baseEntities){
		this.baseEntities = baseEntities;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> JoinApiCaller<BE, R, RE> innerJoin(Class<R> response, Function<List<BE>, List<R>> function) {
		InnerJoinApiCaller<BE, R, RE> inner =  new InnerJoinApiCaller<>(response, function, this);
		apis.add(inner);
		return inner;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> JoinApiCaller<BE, R, RE> outerJoin(Class<R> response, Function<List<BE>, List<R>> function) {
		OuterJoinApipCaller<BE, R, RE> outer = new OuterJoinApipCaller<>(response, function, this);
		apis.add(outer);
		return outer;
	}



	/**
	 * API Compositionを実行します
	 * @return 処理結果
	 */
	public List<RE> execute() {

		apis.forEach(api -> {
			api.validate();
		});
		
		Map<Class<?>, ListenableFuture<List<?>>> resultMap = new HashMap<>();
		apis.forEach(api -> {
			ListenableFuture<List<?>> listenable = threadPool.submitListenable(() -> api.call(baseEntities));
			resultMap.put(api.getClass(), listenable);
		});

		List<RE> resultList = baseEntities.stream()
				.map(e -> {
					if (mapToResult.isPresent()) {
						return mapToResult.get().apply(e);
					} else {
						return modelMapper.map(e, resultClass);
					}
				}).collect(Collectors.toList());

		apis.forEach(api -> {
			ListenableFuture<List<?>> listenable = resultMap.get(api.getClass());
			getResponse(listenable);
			api.mapToResult(resultList);
		});
		return resultList;
	}
	
	private List<?> getResponse(ListenableFuture<List<?>> listenable){
		List<?> list = null;
		try {
			//ここで処理が終了していなければ、呼び出し待ちするはず・・・
			list = listenable.completable().get();
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		return list;
	}

}
