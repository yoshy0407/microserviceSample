package com.microservice.sample.common.api.spi;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.microservice.sample.common.api.Key;

/**
 * SQLのJOINのようにAPIを呼び出して結合を行うインタフェースです
 * 
 * @author yoshy0407
 * @param <BE> ベースとなるエンティティクラス
 * @param <R> APIの実行した結果のレスポンス
 * @param <RE> 結合後のエンティティクラス
 */
public interface JoinApiCaller<BE, R, RE> {

	/**
	 * Joinの条件を設定します
	 * 
	 * @param resultFunction ベース情報がコピーされた結合エンティティから結合キーを取り出す{@link Function}
	 * @param responseFunction レスポンスから結合キーを取り出す{@link Function}
	 * @return {@link JoinApiCaller}
	 */
	public JoinApiCaller<BE, R, RE> onEqual(Function<RE, Key> resultFunction, Function<R, Key> responseFunction);
	
	/**
	 * 条件に一致したレスポンスからエンティティにデータをコピーします
	 * 
	 * @param function コピーする{@link BiConsumer}
	 * @return {@link JoinApiCaller}
	 */
	public JoinApiCaller<BE, R, RE> mapToEntity(BiConsumer<RE, R> function);
	
	/**
	 * ANDで次の処理を記述するためのインスタンスを返却します
	 * @return {@link QueryApiCompositionBuilder}
	 */
	public QueryApiCompositionBuilder<BE, RE> and();
	
	/**
	 * インスタンスのチェックを実行します
	 */
	public void validate();
	
}
