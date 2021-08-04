package com.microservice.sample.common.api.spi;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

/**
 * API Compositionパターンを行うインタフェースです<p>
 * ベースとなるデータに対するWebAPI呼び出しを定義して、実行します
 * 
 * @author yoshy0407
 * @param <E> ベースとなるエンティティの型（ベースエンティティ）
 * @param <RE> 結果として返却するエンティティの型（結果エンティティ）
 *
 */
public interface ApiComposition<E, RE> {

	/**
	 * API呼び出し定義を追加します
	 * @param responseClass APIを受け取るレスポンスクラス
	 * @param function APIを送信するURIを構築します
	 * @param <R> 
	 * @return {@link ApiNode}
	 */
	public <R> ApiNode<R, RE> apiCall(Class<R> responseClass, Function<List<RE>, URI> function);
	
	/**
	 * 渡されたベースエンティティを結果エンティティにマッピングします
	 * 
	 * @param function ベースエンティティを結果エンティティにマッピングする{@link Function}
	 * @return このインスタンス
	 */
	public ApiComposition<E, RE> mapToResult(Function<E, RE> function);
	
	/**
	 * 渡されたエンティティクラスのリストを元に一連のAPIの実行処理を行います
	 * 
	 * @param entities ベースとなるエンティティのリスト
	 * @return 処理結果
	 */
	public List<RE> execute(List<E> entities);
	
}
