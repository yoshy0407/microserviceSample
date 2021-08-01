package com.microservice.sample.common.saga;

import java.util.function.Consumer;

import com.microservice.sample.common.saga.step.SagaStep;
import com.microservice.sample.common.saga.step.SagaStepImpl;

/**
 * {@link Saga}を構築するビルダークラスです
 * 
 * @author yoshy0407
 *
 * @param <P> Sagaに渡すパラメータ
 */
public class SagaBuilder<P extends AbstractSagaParam> {

	private Saga<P> saga;
	
	/**
	 * インスタンスを生成します
	 * @param param Sagaのパラメータ
	 */
	private SagaBuilder(P param) {
		this.saga = new Saga<P>(param);
	}
	
	/**
	 * ステップを追加します
	 * @param <E> サービス送信時のイベントオブジェクト
	 * @param <R> サービス送信結果を受信するイベントオブジェクト
	 * @param <RE> サービスへのロールバック実行時に送信するイベントオブジェクト
	 * @param <CE> サービスへの完了処理実行時に送信するイベントオブジェクト
	 * @param stepName ステップ名
	 * @return {@link SagaStep}
	 */
	public <E, R, RE, CE> SagaStep<E, P, R, RE, CE> step(String stepName){
		SagaStep<E, P, R, RE, CE> step = new SagaStepImpl<>(stepName);
		saga.addStep(step);
		return step;
	}
	
	/**
	 * ロールバック完了後に呼び出すロジック
	 * @param logic ロジック
	 * @return このインスタンス
	 */
	public SagaBuilder<P> finishRollback(Consumer<P> logic){
		this.saga.setRollbackLogic(logic);
		return this;
	}

	/**
	 * 完了処理完了後に呼び出すロジック
	 * @param logic ロジック
	 * @return このインスタンス
	 */
	public SagaBuilder<P> finishComplete(Consumer<P> logic){
		this.saga.setCompoleteLogic(logic);
		return this;
	}
	
	/**
	 * {@link Saga}を構築します
	 * @return {@link Saga}
	 */
	public Saga<P> build(){
		return this.saga;
	}

	/**
	 * {@link SagaBuilder}を構築します
	 * @param <P> Sagaに渡すパラメータ
	 * @param param パラメータ
	 * @return {@link SagaBuilder}
	 */
	public static <P extends AbstractSagaParam> SagaBuilder<P> saga(P param) {
		return new SagaBuilder<>(param);
	}
	
}
