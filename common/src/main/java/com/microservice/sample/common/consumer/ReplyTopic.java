package com.microservice.sample.common.consumer;

import java.util.Optional;

/**
 * Kafkaのレスポンスに関するオブジェクトです
 * 
 * @author yoshy0407
 *
 * @param <RE> イベントオブジェクト
 */
public class ReplyTopic<RE> {

	private String topicName;
	
	private String messageKey;
	
	private RE event;

	/**
	 * @return topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * @param topicName セットする topicName
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @return messageKey
	 */
	public Optional<String> getMessageKey() {
		return Optional.ofNullable(messageKey);
	}

	/**
	 * @param messageKey セットする messageKey
	 */
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	/**
	 * @return event
	 */
	public Optional<RE> getEvent() {
		return Optional.ofNullable(event);
	}

	/**
	 * @param event セットする event
	 */
	public void setEvent(RE event) {
		this.event = event;
	}
}
