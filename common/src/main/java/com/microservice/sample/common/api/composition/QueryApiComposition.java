package com.microservice.sample.common.api.composition;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.microservice.sample.common.api.QueryApiCompositionBuilderImpl;
import com.microservice.sample.common.api.spi.QueryApiCompositionBuilder;

/**
 * {link {@link QueryApiComposition}を構築するビルダークラスです
 * 
 * @author yoshy0407
 *
 */
public class QueryApiComposition implements ApiComposition {
	
	private ThreadPoolTaskExecutor threadPool;
	
	/**
	 * インスタンスを生成します
	 * 
	 * @param threadPool {@link ThreadPoolTaskExecutor}
	 */
	public QueryApiComposition(ThreadPoolTaskExecutor threadPool) {
		this.threadPool = threadPool;
	}
	
	/**
	 * {@link QueryApiComposition}を構築します
	 * 
	 * @param <BE> ベースのエンティティ
	 * @param <RE> 結合結果のエンティティ
	 * @param builderFunction {@link QueryApiCompositionBuilder}の設定を行うロジック
	 * @return 合成したエンティティのリスト
	 */
	@Override
	public <BE, RE> List<RE> execute(Class<BE> baseClass, Class<RE> resultClass, Consumer<QueryApiCompositionBuilder<BE, RE>> builderFunction){
		QueryApiCompositionBuilderImpl<BE, RE> builder = new QueryApiCompositionBuilderImpl<BE, RE>(threadPool);
		builderFunction.accept(builder);
		return builder.execute();
	}
}
