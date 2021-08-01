package com.microservice.sample.common.saga;

import lombok.Getter;
import lombok.Setter;

/**
 * Saga内で利用するパラメータオブジェクトの抽象クラスです
 * @author yoshy0407
 *
 */
public class AbstractSagaParam {

	@Getter
	@Setter
	private String transactionId;	
	
}
