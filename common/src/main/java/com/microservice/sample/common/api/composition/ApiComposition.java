package com.microservice.sample.common.api.composition;

import java.util.List;
import java.util.function.Consumer;

import com.microservice.sample.common.api.spi.QueryApiCompositionBuilder;

/**
 * API Compositionを管理するインタフェースです<p>
 * 
 * @author yoshy0407
 *
 */
public interface ApiComposition {

	/**
	 * {@link QueryApiComposition}を構築します
	 * 
	 * @param <BE> ベースのエンティティ
	 * @param <RE> 結合結果のエンティティ
	 * @param baseClass ベースエンティティのクラス
	 * @param resultClass 結合結果のエンティティのクラス
	 * @param builderFunction {@link QueryApiCompositionBuilder}の設定を行うロジック
	 * @return 合成したエンティティのリスト
	 */
	public <BE, RE> List<RE> execute(Class<BE> baseClass, Class<RE> resultClass, Consumer<QueryApiCompositionBuilder<BE, RE>> builderFunction);

}