package com.microservice.sample.common;

/**
 * トランザクションIDを管理するインタフェースです
 * 
 * @author yoshy0407
 *
 */
public interface TransactionIdRegistry {

	/**
	 * トランザクションIDを採番します
	 * 
	 * @param name サービス名などトランザクションIDの採番種別の文字列
	 * @return トランザクションID
	 */
	public String getNextVal(String name);
	
	/**
	 * トランザクションIDが取り込み済みかどうかチェックします
	 * @param name サービス名などトランザクションIDの採番種別の文字列
	 * @param transactionId トランザクションID
	 * @return 取り込み済みかどうか
	 */
	public boolean validateId(String name, String transactionId);
}
