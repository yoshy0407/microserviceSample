package com.microservice.sample.common.saga.step;

/**
 * サービスから受信したときの判定結果のステータスを表す列挙型です
 * 
 * @author yoshy0407
 *
 */
public enum OnReplyStatus {
	/**
	 * 次のサービスの呼び出しができるステータス
	 */
	FORWORD_NEXT,
	/**
	 * エラーがあったため、ロールバックを実行するステータス
	 */
	ROLLBACK;
}
