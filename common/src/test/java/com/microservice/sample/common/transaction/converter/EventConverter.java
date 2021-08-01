package com.microservice.sample.common.transaction.converter;

import org.springframework.kafka.core.KafkaTemplate;

/**
 * 更新データしたデータをイベントに変換するインタフェースです
 * 
 * @author yoshy0407
 *
 * @param <E> エンティティクラス
 */
public interface EventConverter<E> {

	/**
	 * 渡されたデータがこのクラスの処理対象かチェックします
	 * @param entity データレコード
	 * @return 処理対象かどうか
	 */
	public boolean matchs(E entity);
	
	/**
	 * データを送信します
	 * @param kafkaTemplate {@link KafkaTemplate}
	 * @param entity データレコード
	 */
	public void send(KafkaTemplate<String, Object> kafkaTemplate,  E entity);
	
}
