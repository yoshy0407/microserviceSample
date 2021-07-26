package com.microservice.sample.order.saga;

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
	 * @param param Sagaに渡すパラメータ
	 * @param sagaFunction Sagaを構築する{@link Function}
	 * @return {@link CompletableFuture}
	 */
	public <P> String start(P param, Function<P, Saga<P>> sagaFunction);

	/**
	 * 指定されたトランザクションIDに紐づく{@link Saga}を取得します
	 * 
	 * @param transactionId 取得対象のトランザクションID
	 * @return {@link CompletableFuture}
	 */
	public Saga<?> get(String transactionId);

}
