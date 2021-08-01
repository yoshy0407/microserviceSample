package com.microservice.sample.common.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import com.microservice.sample.common.transaction.converter.EventConverter;

/**
 * テーブルをポーリングして、別サービスへデータの更新情報を送る抽象クラスです
 * 
 * @author yoshy0407
 *
 * @param <E> テーブルの検索結果のエンティティ
 */
public abstract class AbstractTablePoolingTransactionMiner<E> {

	@Autowired
	protected KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	protected ThreadPoolTaskExecutor threadPool;

	private final long sleepTime;

	private Boolean isLoop;	

	private List<EventConverter<E>> converters = new ArrayList<>();

	protected AbstractTablePoolingTransactionMiner(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * 初期化します
	 */
	@PostConstruct
	public void init() {
		synchronized (isLoop) {
			isLoop = Boolean.TRUE;			
		}
		threadPool.execute(() -> doPolling(sleepTime));
	}

	/**
	 * 終了処理を行います
	 */
	@PreDestroy
	public void destory() {
		synchronized (isLoop) {
			isLoop = Boolean.FALSE;
		}
	}

	/**
	 * コンバーターを追加します
	 * @param converter
	 */
	public void addConverter(EventConverter<E> converter) {
		converters.add(converter);
	}

	/**
	 * ループ処理を実行します
	 * @param sleepTime ループの間のスリープ時間
	 */
	protected void doPolling(long sleepTime) {
		while(isLoop) {
			Optional<List<E>> dataList = checkNewData();
			dataList.ifPresent(l -> {
				l.forEach(e -> {
					Optional<EventConverter<E>> converter = converters.stream()
							.filter(c -> c.matchs(e))
							.findFirst();
					converter.ifPresent(c -> {
						c.send(kafkaTemplate, e);
					});
				});
			});

			sleep(sleepTime);
		}
	}

	/**
	 * 検索処理を実行します
	 * @return 検索結果
	 */
	protected abstract Optional<List<E>> checkNewData();

	/**
	 * スリープを実行します
	 * @param sleepTime スリープ時間
	 */
	private void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
	}


}
