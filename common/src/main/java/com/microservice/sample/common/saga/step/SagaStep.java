package com.microservice.sample.common.saga.step;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import com.microservice.sample.common.event.Event;
import com.microservice.sample.common.saga.AbstractSagaParam;

/**
 * Sagaのステップとなるインターフェースです
 * 
 * @author yoshy0407
 * @param <E> サービスに送信するイベント
 * @param <P> Saga全体で利用するパラメータオブジェクト
 * @param <R> サービスより戻ってくるイベントオブジェクト
 * @param <RE> ロールバック送信時のイベントオブジェクト
 * @param <CE> 完了処理送信時のイベントオブジェクト
 *
 */
public interface SagaStep<E, P extends AbstractSagaParam, R, RE, CE> {
	
	/**
	 * サービス呼び出しのロジックを登録します
	 * @param topicName 送信先のトピック名
	 * @param eventCreator サービス呼び出し時に必要なイベントを作成する{@link Function}
	 * @return このインスタンス
	 */
	public SagaStep<E, P, R, RE, CE> callService(String topicName, Function<P, E> eventCreator);

	/**
	 * サービスからのレスポンスを受信した時に実行するロジックを{@link Function}で登録します
	 * @param topicName 受信先のトピック名
	 * @param onReceiveFunction レスポンス受信時に処理する{@link Function}。戻り値は、受信の結果です。
	 * 
	 * @return このインスタンス
	 */
	public SagaStep<E, P, R, RE, CE> receiveReply(String topicName, Function<Event<R>, OnReplyStatus> onReceiveFunction);
	
	/**
	 * サービスのロールバック呼び出しロジックを登録します
	 * @param topicName ロールバックを送信するトピック名 
	 * @param eventCreator サービスのロールバック呼び出し時に必要なイベントオブジェクトを作成する{@link Function}
	 * @return このインスタンス
	 */
	public SagaStep<E, P, R, RE, CE> callRollback(String topicName, Function<P, RE> eventCreator);

	/**
	 * サービスの完了処理呼び出しロジックを登録します
	 * @param topicName 完了処理を送信するトピック名 
	 * @param eventCreator サービスの完了処理呼び出し時に必要なイベントオブジェクトを作成する{@link Function}
	 * @return このインスタンス
	 */
	public SagaStep<E, P, R, RE, CE> callComplete(String topicName, Function<P, CE> eventCreator);

	/**
	 * このステップのステータスを返却します
	 * @return ステータス
	 */
	public StepStatus getStatus();
	
	/**
	 * ステップ名を返却します
	 * @return ステップ名
	 */
	public String getStepName();
	
	/**
	 * このステップを実行します<p>
	 * サービスの呼び出し、
	 * @param kafkaTemplate {@link ReplyingKafkaTemplate}
	 * @param param Saga全体で利用するパラメータ
	 * @return 結果のイベントを受け取って判定した結果
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public OnReplyStatus call(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, P param) throws InterruptedException, ExecutionException;
	
	/**
	 * このステップのロールバックを実行します
	 * @param kafkaTemplate {@link ReplyingKafkaTemplate}
	 * @param param パラメータ
	 */
	public void rollback(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, P param);
	
	/**
	 * このステップの完了処理を実行します
	 * @param kafkaTemplate {@link ReplyingKafkaTemplate}
	 * @param param パラメータ
	 */
	public void complete(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, P param);
	
}
