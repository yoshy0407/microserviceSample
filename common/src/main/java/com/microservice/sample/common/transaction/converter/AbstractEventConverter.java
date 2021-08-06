package com.microservice.sample.common.transaction.converter;

import org.springframework.kafka.core.KafkaTemplate;

import com.microservice.sample.common.event.AbstractEvent;

/**
 * {@link EventConverter}の抽象クラスです
 * @author yoshy0407
 *
 * @param <E> エンティティクラス
 * @param <M> 送信するメッセージのデータオブジェクト
 */
public abstract class AbstractEventConverter<E, M extends AbstractEvent> implements EventConverter<E>{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(KafkaTemplate<String, Object> kafkaTemplate, E entity) {
		M mesage = createMessage(entity);
		kafkaTemplate.send(topicName(), mesage);
		
	}
	
	/**
	 * エンティティから送信するメッセージを作成します
	 * @param entity エンティティ
	 * @return 送信するメッセージ
	 */
	protected abstract M createMessage(E entity);
	
	/**
	 * 送信するトピック名
	 * @return トピック名
	 */
	protected abstract String topicName();

}
