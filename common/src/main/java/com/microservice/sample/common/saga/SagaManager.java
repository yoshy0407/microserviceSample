package com.microservice.sample.common.saga;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Sagaを管理するインタフェースです
 * 
 * @author Hiroshi Yoshioka
 *
 */
public interface SagaManager {

	/**
	 * Sagaを開始します
	 * 
	 * @param <P> Sagaに渡すパラメータ
	 * @param name トランザクションID取得時に利用する採番種別
	 * @param param Sagaに渡すパラメータ
	 * @param sagaFunction Sagaを構築する{@link Function}
	 * @return {@link CompletableFuture}
	 */
	public <P extends AbstractSagaParam> String start(String name, P param, Function<P, Saga<P>> sagaFunction);

	/**
	 * 指定されたトランザクションIDに紐づく{@link Saga}を取得します
	 * 
	 * @param transactionId 取得対象のトランザクションID
	 * @return {@link CompletableFuture}
	 */
	public Optional<Saga<?>> get(String transactionId);

}
