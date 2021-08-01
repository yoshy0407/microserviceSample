package com.microservice.sample.common.saga.step;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyTypedMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

import com.microservice.sample.common.event.Event;
import com.microservice.sample.common.saga.AbstractSagaParam;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link SagaStep}の実装クラスです
 * 
 * @author yoshy0407
 *
 * @param <E> サービスに送信するイベント
 * @param <P> Saga全体で利用するパラメータ
 * @param <R> サービスから返却されるイベント
 * @param <RE> ロールバック時に送信するイベント
 * @param <CE> 完了処理時に送信するイベント
 */
@Slf4j
public class SagaStepImpl<E, P extends AbstractSagaParam, R, RE, CE> 
	implements SagaStep<E, P, R, RE, CE>{

	private StepStatus status = StepStatus.BEFORE_EXECUTE;
	
	private final String stepName;
	
	private TopicMeta<P, E> callTopicMeta;
	
	private Optional<String> receiveTopicName = Optional.empty();
	
	private Optional<Function<Event<R>, OnReplyStatus>> onReceiveFunction = Optional.empty();
	
	private Optional<TopicMeta<P, RE>> rollbackTopicMeta;
	
	private Optional<TopicMeta<P, CE>> completeTopicMeta;
	
	/**
	 * インスタンスを生成します
	 * 
	 * @param stepName ステップ名
	 */
	public SagaStepImpl(String stepName) {
		this.stepName = stepName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SagaStep<E, P, R, RE, CE> callService(String topicName, Function<P, E> eventCreator) {
		Assert.notNull(topicName, "topicName must not be null");
		Assert.notNull(eventCreator, "eventCreator must not be null");
		callTopicMeta = new TopicMeta<>(topicName, eventCreator);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SagaStep<E, P, R, RE, CE> receiveReply(String topicName,
			Function<Event<R>, OnReplyStatus> onReceiveFunction) {
		Assert.notNull(topicName, "topicName must not be null");
		Assert.notNull(onReceiveFunction, "onReceiveFunction must not be null");
		this.receiveTopicName = Optional.ofNullable(topicName);
		this.onReceiveFunction = Optional.ofNullable(onReceiveFunction);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SagaStep<E, P, R, RE, CE> callRollback(String topicName, Function<P, RE> eventCreator) {
		Assert.notNull(topicName, "topicName must not be null");
		Assert.notNull(eventCreator, "eventCreator must not be null");
		TopicMeta<P, RE> topicMeta = new TopicMeta<>(topicName, eventCreator);
		this.rollbackTopicMeta = Optional.ofNullable(topicMeta);
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SagaStep<E, P, R, RE, CE> callComplete(String topicName, Function<P, CE> eventCreator) {
		Assert.notNull(topicName, "topicName must not be null");
		Assert.notNull(eventCreator, "eventCreator must not be null");
		TopicMeta<P, CE> topicMeta = new TopicMeta<>(topicName, eventCreator);
		this.completeTopicMeta = Optional.ofNullable(topicMeta);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StepStatus getStatus() {
		return status;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStepName() {
		return stepName;
	}

	@Override
	public OnReplyStatus call(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, P param) throws InterruptedException, ExecutionException {
		// 送信する情報を設定
		Event<E> event = createEvent(callTopicMeta.getEventCreator().apply(param), param);
		MessageBuilder<Event<E>> messageBuilder = MessageBuilder.withPayload(event)
				.setHeader(KafkaHeaders.TOPIC, callTopicMeta.getTopicName());
				
		
		//結果を受信する情報が設定されている場合は受信を待ち受けるが、
		//設定されていない場合、結果が返ってこないと判断し、送信した結果正常終了とする
		if (receiveTopicName.isPresent() && onReceiveFunction.isPresent()) {
			//結果受信のトピックの情報を設定する
			messageBuilder
				.setHeader(KafkaHeaders.REPLY_TOPIC, receiveTopicName.get().getBytes());
			
			RequestReplyTypedMessageFuture<String, Object, Event<R>> replyFuture = 
					kafkaTemplate.sendAndReceive(messageBuilder.build(), 
							new ParameterizedTypeReference<Event<R>>() {});
			
			log.info(
					String.format("step %s send topic for call %s event: %s", 
							stepName, callTopicMeta.getTopicName(), event));
			
			this.status = StepStatus.EXECUTE;
			
			//応答した結果を受信し、判定を行う
			Message<Event<R>> result = replyFuture.get();
			
			log.info(
					String.format("step %s receive result %s event: %s", 
							stepName, receiveTopicName.get(), result.toString()));

			OnReplyStatus status = onReceiveFunction.get().apply(result.getPayload());
			status(status);
			return status;		
		} else {
			kafkaTemplate.send(messageBuilder.build());
			
			log.info(
					String.format("step %s send topic for call %s event: %s", 
							stepName, callTopicMeta.getTopicName(), event.toString()));

			OnReplyStatus status = OnReplyStatus.FORWORD_NEXT;
			status(status);
			return status;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, P param) {
		rollbackTopicMeta.ifPresentOrElse(
				t -> {
					Event<RE> event = createEvent(t.getEventCreator().apply(param),param);
					String topicName = t.getTopicName();
					log.info(
							String.format("step %s send topic for rollback %s event: %s", 
									stepName, topicName, event));
					kafkaTemplate.send(topicName, event);
					status = StepStatus.ROLLBACK;
				}, 
				() -> {
					log.info("no send rollback topic");
				});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate, P param) {
		completeTopicMeta.ifPresentOrElse(
				t -> {
					Event<CE> event = createEvent(t.getEventCreator().apply(param), param);
					String topicName = t.getTopicName();
					
					log.info(
							String.format("step %s send topic for complete %s event: %s", 
									stepName, topicName, event));
					kafkaTemplate.send(topicName, event);
					status = StepStatus.COMPLETE;
				}, 
				() -> {
					log.info("no send compolete topic");
				});
		
	}
	
	/**
	 * イベントをラップします
	 * 
	 * @param <T> データとなるイベント
	 * @param data データとなるイベント
	 * @param param Saga前提で利用するパラメータ
	 * @return
	 */
	private <T> Event<T> createEvent(T data, P param){
		Event<T> event = new Event<>();
		event.setEvent(data);
		event.setTransactionId(param.getTransactionId());
		return event;
	}
	
	/**
	 * ステータスを変更します
	 * @param status ステータス
	 */
	public void status(OnReplyStatus status) {
		switch(status) {
			case FORWORD_NEXT -> this.status = StepStatus.SUCCESS;
			case ROLLBACK -> this.status = StepStatus.FAILURE;
		}
	}

}
