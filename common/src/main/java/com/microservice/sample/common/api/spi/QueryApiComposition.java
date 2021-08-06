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
public interface QueryApiComposition<BE, RE> {

	/**
	 * APIのレスポンスを受け取ってベースのエンティティをINNER JOINします
	 * 
	 * @param <R> レスポンスオブジェクト
	 * @param response レスポンスクラス
	 * @param callApi APIを実行するロジック
	 * @return {@link JoinApiCaller}
	 */
	public <R> JoinApiCaller<BE, R, RE> innerJoin(Class<R> response, Function<List<RE>, List<R>> callApi);
	
	/**
	 * APIのレスポンスを受け取ってベースのエンティティをOUTER JOINします
	 * 
	 * @param <R> レスポンスオブジェクト
	 * @param response レスポンスクラス
	 * @param callApi APIのレスポンスを受け取ってベースのエンティティをINNER JOINします
	 * @return {@link JoinApiCaller}
	 */
	public <R> JoinApiCaller<BE, R, RE> outerJoin(Class<R> response, Function<List<RE>, List<R>> callApi);
	
	/**
	 * 渡されたベースエンティティを結果エンティティにマッピングします
	 * 
	 * @param function ベースエンティティを結果エンティティにマッピングする{@link Function}
	 * @return このインスタンス
	 */
	public QueryApiComposition<BE, RE> mapToResult(Function<BE, RE> function);
	
	/**
	 * API Compositionを実行します
	 * @return 処理結果
	 */
	public List<RE> execute();
	
	
}
