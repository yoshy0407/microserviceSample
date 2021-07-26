package com.microservice.sample.order.saga;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.springframework.kafka.core.KafkaTemplate;

import com.microservice.sample.order.saga.step.SagaStep;
import com.microservice.sample.order.saga.step.StepStatus;
import lombok.Setter;

public class Saga<PARAM> {

	/**
	 * Sagaのステップ
	 */
	private LinkedHashMap<String, SagaStep<PARAM>> steps = new LinkedHashMap<>();
	
	/**
	 * Saga全体で利用するパラメータオブジェクト
	 */
	private PARAM param;
	
	/**
	 * Kafka Producer
	 */
	@Setter
	private KafkaTemplate<String, Object> kafkaTemplate;
	
	/**
	 * 完了処理の完了後に呼び出すロジック
	 * （Sagaのオーケストレーターサービスで全て完了した後に更新する処理）
	 */
	@Setter
	private Consumer<PARAM> completeLogic;

	/**
	 * ロールバック後に呼び出すロジック
	 * （Sagaのオーケストレーターサービスで全て完了した後にデータのロールバックを行う処理）
	 */
	@Setter
	private Consumer<PARAM> rollbackLogic;
	
	/**
	 * インスタンスを生成します
	 * 
	 * @param kafkaTemplate {@link KafkaTemplate}
	 */
	public Saga(PARAM param) {
		this.param = param;
	}
	
	public void addStep(String stepName, SagaStep<PARAM> step) {
		steps.put(stepName, step);
	}
	
	public SagaStep<PARAM> getStep(String stepName){
		return steps.get(stepName);
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
		callNext(true);
	}
	
	/**
	 * 次の処理を呼び出します
	 * @param beforeStepSuccess 事前の呼び出し処理が正常終了かどうかです
	 */
	public void callNext(boolean beforeStepSuccess) {
		//事前のステップが正常終了の場合
		if (beforeStepSuccess) {
			if (doCheckComplete()) {
				//完了処理
				doComplete();
			} else {
				//次のステップのサービス呼び出し
				SagaStep<PARAM> step = getNext();
				step.callService(kafkaTemplate, param);
			}
		//事前のステップが異常終了の場合
		} else {
			LinkedHashMap<String, SagaStep<PARAM>> rollbackMap = getReverseStep();
			rollbackMap.entrySet()
				.forEach(e -> {
					SagaStep<PARAM> step = e.getValue();
					step.callRollbackService(kafkaTemplate, param);
				});
			//ロールバックのメソッドを呼び出す
			if (rollbackLogic != null) {
				rollbackLogic.accept(param);				
			}
		}
	}
	
	/**
	 * ステップの中で未処理のステップの最初のステップを取得します
	 * @return 未処理のステップの最初のステップ
	 */
	protected SagaStep<PARAM> getNext(){
		return steps.entrySet().stream()
				.filter(e -> {
					SagaStep<PARAM> step = e.getValue();
					return step.getStatus().equals(StepStatus.BEFORE_EXECUTE);
				})
				.map(e -> e.getValue())
				.findFirst()
				.get();
				
	}
	
	/**
	 * 完了処理を順番に呼び出します
	 */
	protected void doComplete() {
		steps.entrySet().forEach(e -> {
			e.getValue().callCompleteService(kafkaTemplate, param);
		});
		if (completeLogic != null) {
			completeLogic.accept(param);			
		}
	}
	
	
	/**
	 * 保持しているステップが全て処理成功かチェックして、
	 * 完了処理の呼び出しフェーズになっているかどうか判定します
	 * 
	 * @return 保持しているステップが全て処理成功か
	 */
	protected boolean doCheckComplete() {
		for (Entry<String, SagaStep<PARAM>> entry : steps.entrySet()) {
			SagaStep<PARAM> step = entry.getValue();
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
	 * 保持しているステップで処理成功のステップを逆順にしたリストを返却します
	 * @return 処理成功のステップを逆順にしたリスト
	 */
	protected LinkedHashMap<String, SagaStep<PARAM>> getReverseStep(){
		List<String> list = new ArrayList<>(steps.keySet());
		int max = list.size();
		LinkedHashMap<String, SagaStep<PARAM>> rtnMap = new LinkedHashMap<>();
		for (int i = max; i >=0 ; i--) {
			SagaStep<PARAM> step = steps.get(list.get(i));
			if (step.getStatus().equals(StepStatus.SUCCESS)) {
				rtnMap.put(list.get(i), step);
			}
		}
		return rtnMap;
	}
		
}
