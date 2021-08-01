package com.microservice.sample.common.saga.step;

import java.util.function.Function;


/**
 * トピックに関する情報を保持するオブジェクトです
 * @author yoshy0407
 *
 * @param <P> パラメータの型
 * @param <E> 送信するイベントの型
 */
public class TopicMeta<P, E> {

	private final String topicName;
	
	private final Function<P, E> eventCreator;

	/**
	 * インスタンスを生成します
	 * @param topicName トピック名
	 * @param eventCreator イベントオブジェクトを生成する{@link Function}
	 */
	public TopicMeta(String topicName, Function<P, E> eventCreator) {
		this.topicName = topicName;
		this.eventCreator = eventCreator;
	}
	
	/**
	 * 送信するトピック名を返却します
	 * @return トピック名
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * イベントオブジェクトを生成する{@link Function}を返却します
	 * @return イベントオブジェクトを生成する{@link Function}
	 */
	public Function<P, E> getEventCreator() {
		return eventCreator;
	}

}
