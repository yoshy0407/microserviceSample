package com.microservice.sample.order.saga.step;

public enum StepStatus {
	/**
	 * サービスが呼び出されていないステータス
	 */
	BEFORE_EXECUTE,
	/**
	 * サービス呼び出し中のステータス
	 */
	EXECUTE,
	/**
	 * サービスの呼び出し結果が正常終了のステータス
	 */
	SUCCESS,
	/**
	 * サービスの呼び出し結果が異常終了のステータス
	 */
	FAILURE,
	/**
	 * サービスの完了処理を呼び出しのステータス
	 */
	COMPLETE,
	/**
	 * サービスのロールバック呼び出しのステータス
	 */
	ROLLBACK;
}
