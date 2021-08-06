package com.microservice.sample.common.consumer;

import org.apache.kafka.clients.consumer.internals.ConsumerMetadata;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;

import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.common.consumer.validation.MessageValidaiton;
import com.microservice.sample.common.event.AbstractEvent;

/**
 * Kafkaを利用してメッセージから取り出したイベントを処理する抽象クラスです
 * @author yoshy0407
 * @param <E> データ部となるイベントオブジェクト
 *
 */
public abstract class AbstractConsumer<E extends AbstractEvent> {

	protected final TransactionIdRegistry transactionIdRegistry;
	
	/**
	 * コンストラクター
	 * @param transactionIdRegistry {@link TransactionIdRegistry}
	 */
	protected AbstractConsumer(TransactionIdRegistry transactionIdRegistry) {
		this.transactionIdRegistry = transactionIdRegistry;
	}
	
	/**
	 * Spring Kafkaから呼び出されてメッセージを処理するメソッドです
	 * 
	 * @param event {@link AbstractEvent}
	 * @param metadata {@link ConsumerMetadata}
	 */
	@KafkaHandler(isDefault = true)
	public void consume(@Payload @Validated(value = MessageValidaiton.class) E event, 
			ConsumerMetadata metadata) {
		if (!checkTransactionId(event.getTransactionId(), metadata)) {
			doConsume(event, metadata);
		}
	}
	
	/**
	 * 受け取ったメッセージの処理を行います
	 * @param event イベントオブジェクト
	 * @param metaData {@link ConsumerMetadata}
	 */
	protected abstract void doConsume(E event, ConsumerMetadata metaData);
	
	/**
	 * {@link TransactionIdRegistry#validateId(String, String)}に利用する採番種別を解決します
	 * @param metaData {@link ConsumerMetadata}
	 * @return 採番種別
	 */
	protected abstract String resolveName(ConsumerMetadata metaData);
	
	/**
	 * トランザクションIDが取り込み済みかどうかチェックします
	 * 
	 * @param transactionId トランザクションID
	 * @param metaData {@link ConsumerMetadata}
	 * @return トランザクションIDが取り込み済みかどうか
	 */
	private boolean checkTransactionId(String transactionId, ConsumerMetadata metaData) {
		return transactionIdRegistry.validateId(resolveName(metaData), transactionId);
	}
}
