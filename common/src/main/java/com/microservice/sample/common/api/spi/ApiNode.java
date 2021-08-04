package com.microservice.sample.common.api.spi;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import com.microservice.sample.common.api.rest.ResponseMapper;

/**
 * API呼び出しを行う処理を定義するインタフェースです
 * 
 * @author yoshy0407
 *
 * @param <E> 作成するエンティティクラス
 * @param <R> レスポンス
 */
public interface ApiNode<R, E> {

	/**
	 * 送信するAPIの情報を登録します
	 * @param response レスポンスを受け取るオブジェクト
	 * @param function ベースとなるエンティティから
	 * @return このインスタンス
	 */
	public ApiNode<R, E> callFor(Class<R> response, Function<List<E>, URI> function);
	
	/**
	 * レスポンスを返却するエンティティクラスにマッピングの設定をします
	 * @return {@link ResponseMapper}
	 */
	public ResponseMapper<E, R> map();
	
	
	/**
	 * 設定されている定義情報が正しいかどうかチェックします
	 */
	public void validate();
	
	/**
	 * このインスタンスで定義されているAPI処理を実行します
	 * 
	 * @param resultEntities 加工対象となるエンティティクラスのリスト
	 */
	public void execute(List<E> resultEntities);
	
}
