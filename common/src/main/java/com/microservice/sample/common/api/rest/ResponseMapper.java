package com.microservice.sample.common.api.rest;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.microservice.sample.common.api.Key;

/**
 * レスポンスをエンティティにマッピングするオブジェクトです
 * 
 * @author yoshy0407
 *
 * @param <RE> エンティティクラス
 * @param <R> レスポンスクラス
 */
public class ResponseMapper<RE, R> {

	private Function<RE, Key> fromEntity;
	
	private Function<R, Key> fromResponse;
	
	private BiConsumer<RE, R> mapToEntity;
	
	/**
	 * エンティティからレスポンスと結合する際に必要なキーを取り出すロジックを設定します
	 * 
	 * @param function キーを取り出す{@link Function}
	 * @return 取り出したキー
	 */
	public ResponseMapper<RE, R> keyFromEntity(Function<RE, Key> function) {
		this.fromEntity = function;
		return this;
	}
	
	/**
	 * レスポンスからエンティティと結合する際に櫃おうなキーを取り出すロジックを設定します
	 * 
	 * @param function キーを取り出す{@link Function}
	 * @return 取り出したキー
	 */
	public ResponseMapper<RE, R> keyFromResponse(Function<R, Key> function){
		this.fromResponse = function;
		return this;
	}
	
	/**
	 * レスポンスからエンティティにマッピングする処理を設定します
	 * 
	 * @param function レスポンスからエンティティにマッピングする処理
	 */
	public void mapToEntity(BiConsumer<RE, R> function) {
		this.mapToEntity = function;
	}
	
	protected Key getEntityKey(RE entity) {
		return fromEntity.apply(entity);
	}
	
	protected Key getResponseKey(R response) {
		return fromResponse.apply(response);
	}
	
	protected void map(RE entity, R response) {
		mapToEntity.accept(entity, response);
	}
	
}
