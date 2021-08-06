package com.microservice.sample.common.consumer;

import org.apache.kafka.clients.consumer.internals.ConsumerMetadata;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;

import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.common.consumer.validation.MessageValidaiton;
import com.microservice.sample.common.event.AbstractEvent;

/**
 * Kafkaを利用してメッセージから取り出したイベントを処理して、
 * 指定されたトピックに返す抽象クラスです
 * @author yoshy0407
 *
 * @param <E> 受信するイベントオブジェクト
 * @param <RE> 送信するイベントオブジェクト
 */
public abstract class AbstractReplyConsumer<E extends AbstractEvent, RE extends AbstractEvent> {

	private final TransactionIdRegistry transactionIdRegistry;

	protected AbstractReplyConsumer(TransactionIdRegistry transactionIdRegistry) {
		this.transactionIdRegistry = transactionIdRegistry;
	}

	/**
	 * Spring Kafkaから呼び出されてメッセージを処理するメソッドです
	 * 
	 * @param event {@link AbstractEvent}
	 * @param metadata {@link ConsumerMetadata}
	 * @return 返信するメッセージ
	 */
	@KafkaHandler(isDefault = true)
	@SendTo
	public Message<RE> consume(@Payload @Validated(value = MessageValidaiton.class) E event, 
			ConsumerMetadata metadata) {
		if (!checkTransactionId(event.getTransactionId(), metadata)) {
			
			ReplyTopic<RE> replyTopic = doConsume(event, metadata);
			
			if (replyTopic.getEvent().isPresent()) {
				RE resultEvent = replyTopic.getEvent().get();
				resultEvent.setTransactionId(event.getTransactionId());
				
				MessageBuilder<RE> builder = MessageBuilder.withPayload(resultEvent)
						.setHeader(KafkaHeaders.TOPIC, replyTopic.getTopicName());
				replyTopic.getMessageKey().ifPresent(key -> {
					builder.setHeader(KafkaHeaders.MESSAGE_KEY, key);
				});
				return builder.build();
			} else {
				//:TODO レスポンスを送らないパターンは例外を発動して、処理を終了させる。
				//:TODO 用意した、無害なエラーを作成して、ErrorHandlerでキャッチしてログなどに出力する
				throw new RuntimeException();
			}
			
		} else {
			//:TODO レスポンスを送らないパターンは例外を発動して、処理を終了させる。
			//:TODO 用意した、無害なエラーを作成して、ErrorHandlerでキャッチしてログなどに出力する
			throw new RuntimeException();
		}
	}

	/**
	 * 受け取ったメッセージの処理を行います
	 * @param event イベントオブジェクト
	 * @param metaData {@link ConsumerMetadata}
	 */
	protected abstract ReplyTopic<RE> doConsume(E event, ConsumerMetadata metaData);

	/**
	 * {@link TransactionIdRegistry#validateId(String, String)}に利用する採番種別を解決します
	 * @param metaData {@link ConsumerMetadata}
	 * @return 採番種別
	 */
	protected abstract String resolveName(ConsumerMetadata metaData);

	protected void handleNoReply() throws Exception{
		throw new RuntimeException();
	}

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
