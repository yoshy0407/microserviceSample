package com.microservice.sample.common.api.spi;

import java.util.List;
import java.util.function.Function;

/**
 * SQLのクエリのようにAPIコンポジションを行うインタフェースです
 * @author yoshy0407
 *
 * @param <BE>
 * @param <RE>
 */
public interface QueryApiCompositionBuilder<BE, RE> {

	/**
	 * 渡されたベースエンティティを結果エンティティにマッピングします
	 * 
	 * @param resultClass 合成結果のエンティティクラス
	 * @param function ベースエンティティを結果エンティティにマッピングする{@link Function}
	 * @return このインスタンス
	 */
	public QueryApiCompositionBuilder<BE, RE> select(Class<RE> resultClass, Function<BE, RE> function);

	/**
	 * 渡されたベースエンティティを結果エンティティにマッピングします
	 * 
	 * @param resultClass 合成結果のエンティティクラス
	 * @return このインスタンス
	 */
	public QueryApiCompositionBuilder<BE, RE> select(Class<RE> resultClass);

	/**
	 * ベースエンティティを設定します
	 * @param baseEntities ベースエンティティ
	 * @return このインスタンス
	 */
	public QueryApiCompositionBuilder<BE, RE> from(List<BE> baseEntities);
	
	/**
	 * APIのレスポンスを受け取ってベースのエンティティをINNER JOINします
	 * 
	 * @param <R> レスポンスオブジェクト
	 * @param response レスポンスクラス
	 * @param callApi APIを実行するロジック
	 * @return {@link JoinApiCaller}
	 */
	public <R> JoinApiCaller<BE, R, RE> innerJoin(Class<R> response, Function<List<BE>, List<R>> callApi);
	
	/**
	 * APIのレスポンスを受け取ってベースのエンティティをOUTER JOINします
	 * 
	 * @param <R> レスポンスオブジェクト
	 * @param response レスポンスクラス
	 * @param callApi APIのレスポンスを受け取ってベースのエンティティをINNER JOINします
	 * @return {@link JoinApiCaller}
	 */
	public <R> JoinApiCaller<BE, R, RE> outerJoin(Class<R> response, Function<List<BE>, List<R>> callApi);
	
}
