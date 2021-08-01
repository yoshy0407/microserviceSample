package com.microservice.sample.common.saga;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.util.ReflectionUtils;

import com.microservice.sample.common.saga.step.OnReplyStatus;
import com.microservice.sample.common.saga.step.SagaStep;
import com.microservice.sample.common.saga.step.StepStatus;

/**
 * Sagaのオブジェクトです<p>
 * 各ステップを管理して、ロールバックや完了処理などを実行します
 * 
 * @author yoshy0407
 *
 * @param <P>
 */
public class Saga<P extends AbstractSagaParam> {

	/**
	 * Sagaのステップ
	 */
	private LinkedHashMap<String, SagaStep<?, P, ?, ?, ?>> steps = new LinkedHashMap<>();
	
	/**
	 * Saga全体で利用するパラメータオブジェクト
	 */
	private P param;
	
	/**
	 * Kafka Producer
	 */
	private ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate;
	
	/**
	 * 完了処理の完了後に呼び出すロジック
	 * （Sagaのオーケストレーターサービスで全て完了した後に更新する処理）
	 */
	private Optional<Consumer<P>> completeLogic = Optional.empty();

	/**
	 * ロールバック後に呼び出すロジック
	 * （Sagaのオーケストレーターサービスで全て完了した後にデータのロールバックを行う処理）
	 */
	private Optional<Consumer<P>> rollbackLogic = Optional.empty();
	
	/**
	 * インスタンスを生成します
	 * 
	 * @param param Sagaのパラメータ
	 */
	protected Saga(P param) {
		this.param = param;
	}
	
	/**
	 * ステップを取得します
	 * @param stepName ステップ名
	 * @return ステップ
	 */
	public SagaStep<?, P, ?, ?, ?> getStep(String stepName){
		return steps.get(stepName);
	}
	
	/**
	 * 保持するステップ全てを取得します
	 * @return 保持するステップ全て
	 */
	public List<SagaStep<?, P, ?, ?, ?>> getSteps(){
		return steps.entrySet().stream()
				.map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
	}
	
	/**
	 * Sagaを開始します
	 */
	public void start() {
		if (steps.size() == 0) {
			throw new IllegalArgumentException("stepが設定されていません");
		}
		if (kafkaTemplate == null) {
			throw new IllegalArgumentException("kafaka producerが設定されていません");
		}
		doLoop();
	}

	/**
	 * {@link ReplyingKafkaTemplate}を設定します
	 * @param kafkaTemplate {@link ReplyingKafkaTemplate}
	 */
	public void setKafkaTemplate(ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	/**
	 * ステップを追加します
	 * @param step ステップ
	 */
	protected void addStep(SagaStep<?, P, ?, ?, ?> step) {
		steps.put(step.getStepName(), step);
	}
	
	/**
	 * Saga完了時に実行するロジックを設定します
	 * @param complete 実行するロジック
	 */
	protected void setCompoleteLogic(Consumer<P> complete) {
		completeLogic = Optional.ofNullable(complete);
	}
	
	/**
	 * Sagaロールバック完了時に実行するロジックを設定します
	 * @param rollback 実行するロジック
	 */
	protected void setRollbackLogic(Consumer<P> rollback) {
		rollbackLogic = Optional.ofNullable(rollback);
	}
		
	/**
	 * Sagaの処理を実行します
	 */
	protected void doLoop() {
		for (Entry<String, SagaStep<?, P, ?, ?, ?>> stepEntry : steps.entrySet()) {
			SagaStep<?, P, ?, ?, ?> step = stepEntry.getValue();
			//サービス呼び出しを実行
			
			OnReplyStatus status = null;
			try {
				status = step.call(kafkaTemplate, param);
			} catch (InterruptedException | ExecutionException e) {
				ReflectionUtils.rethrowRuntimeException(e);
			}
			
			//ステップが正常終了
			if (status.equals(OnReplyStatus.FORWORD_NEXT)) {
				if (doCheckComplete()) {
					//完了処理
					doComplete();
					break;
				} else {
					//次のステップのサービス呼び出し
					continue;
				}
			//ステップが異常終了
			} else if (status.equals(OnReplyStatus.ROLLBACK)) {
				doRollback();
				break;
			} else {
				throw new IllegalStateException("返却されたステータスが想定されていないステータスです。:" + status);
			}
		}
	}
	
	/**
	 * ロールバック処理を実行します
	 */
	protected void doRollback() {
		LinkedHashMap<String, SagaStep<?, P, ?, ?, ?>> rollbackMap = getReverseStep();
		rollbackMap.entrySet()
			.forEach(e -> {
				SagaStep<?, P, ?, ?, ?> step = e.getValue();
				step.rollback(kafkaTemplate, param);
			});
		//ロールバックのメソッドを呼び出す
		rollbackLogic.ifPresent(t -> t.accept(param));
	}
	
	/**
	 * 保持しているステップが全て処理成功かチェックして、
	 * 完了処理の呼び出しフェーズになっているかどうか判定します
	 * 
	 * @return 保持しているステップが全て処理成功か
	 */
	protected boolean doCheckComplete() {
		for (Entry<String, SagaStep<?, P, ?, ?, ?>> entry : steps.entrySet()) {
			SagaStep<?, P, ?, ?, ?> step = entry.getValue();
			//１個でも未呼び出し、実行中があれば、処理成功でないためfalse
			//failureやロールバックは、その時点でロールバックが行われるので、ここではチェックしない
			if (step.getStatus().equals(StepStatus.BEFORE_EXECUTE) 
					|| step.getStatus().equals(StepStatus.EXECUTE) ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 完了処理を順番に呼び出します
	 */
	protected void doComplete() {
		steps.entrySet().forEach(e -> {
			e.getValue().complete(kafkaTemplate, param);
		});
		//完了処理を呼び出す
		completeLogic.ifPresent(t -> t.accept(param));
	}
	
	/**
	 * 保持しているステップで処理成功のステップを逆順にしたリストを返却します
	 * @return 処理成功のステップを逆順にしたリスト
	 */
	protected LinkedHashMap<String, SagaStep<?, P, ?, ?, ?>> getReverseStep(){
		List<String> list = new ArrayList<>(steps.keySet());
		int max = list.size() - 1;
		LinkedHashMap<String, SagaStep<?, P, ?, ?, ?>> rtnMap = new LinkedHashMap<>();
		for (int i = max; i >= 0 ; i--) {
			SagaStep<?, P, ?, ?, ?> step = steps.get(list.get(i));
			if (step.getStatus().equals(StepStatus.SUCCESS)) {
				rtnMap.put(list.get(i), step);
			}
		}
		return rtnMap;
	}
		
}
