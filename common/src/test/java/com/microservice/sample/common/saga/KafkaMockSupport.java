package com.microservice.sample.common.saga;

import java.util.concurrent.ExecutionException;

import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyTypedMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.microservice.sample.common.event.AbstractEvent;

/**
 * Kafkaのモックに関するユーティリティです
 * @author yoshy0407
 *
 */
public class KafkaMockSupport {

	/**
	 * {@link KafkaTemplate#send(Message)}にモックを仕込みます
	 * @param <T> ペイロードオブジェクト
	 * @param kafkaTemplate モック化した{@link KafkaTemplate}
	 * @param message 送信するメッセージオブジェクト
	 */
	public static <T> void mockSend(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, 
			Message<T> message) {
		Mockito.when(kafkaTemplate.send(message)).thenReturn(null);
	}
	
	/**
	 * {@link Message}を作成します
	 * 
	 * @param <T> ペイロードオブジェクト
	 * @param payload ペイロードオブジェクト
	 * @param topicName トピック名
	 * @return {@link Message}
	 */
	public static <T> Message<T> createMessage(T payload, String topicName){
		return MessageBuilder.withPayload(payload)
					.setHeader(KafkaHeaders.TOPIC, topicName)
					.build();
	}
	
	/**
	 * {@link ReplyingKafkaTemplate#sendAndReceive(Message, ParameterizedTypeReference)}をモック化します
	 * @param <R> 返却されるオブジェクト
	 * @param kafkaTemplate モック化した{@link ReplyingKafkaTemplate}
	 * @param result 返却される{@link Message}
	 */
	@SuppressWarnings("unchecked")
	public static <R> void mockSendAndReceive(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, 
			Message<R> result) {
		RequestReplyTypedMessageFuture<String, Object, R> reply = Mockito.mock(RequestReplyTypedMessageFuture.class);
		try {
			Mockito.when(reply.get()).thenReturn(result);
		} catch (InterruptedException | ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		Mockito
			.when(kafkaTemplate.sendAndReceive(Mockito.any(Message.class), Mockito.any(ParameterizedTypeReference.class)))
			.thenReturn(reply);
	}
	
	/**
	 * {@link Message}を作成します
	 * 
	 * @param <T> ペイロードオブジェクト
	 * @param payload ペイロードオブジェクト
	 * @param topicName トピック名
	 * @param replyTopic レスポンスを受けるトピック名
	 * @return {@link Message}
	 */
	public static <T> Message<T> createMessage(T payload, String topicName, String replyTopic){
		return MessageBuilder.withPayload(payload)
					.setHeader(KafkaHeaders.TOPIC, topicName)
					.setHeader(KafkaHeaders.REPLY_TOPIC, replyTopic)
					.build();
	}

}
