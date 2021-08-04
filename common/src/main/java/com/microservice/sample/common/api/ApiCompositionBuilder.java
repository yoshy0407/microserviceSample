package com.microservice.sample.common.api;

import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.spi.ApiComposition;

/**
 * {@link ApiComposition}を構築するビルダークラスです
 * 
 * @author yoshy0407
 *
 */
public class ApiCompositionBuilder {

	RestTemplate restTemaplte;
	
	/**
	 * インスタンスを生成します
	 * @param restTemplate {@link RestTemplate}
	 */
	public ApiCompositionBuilder(RestTemplate restTemplate) {
		this.restTemaplte = restTemplate;
	}
	
	/**
	 * {@link ApiComposition}を構築します
	 * @param <E> ベースとなるエンティティクラス
	 * @param <ER> 結果となるエンティティクラス
	 * @return {@link ApiComposition}
	 */
	public <E, ER> ApiComposition<E, ER> create(){
		return new ApiCompositionImpl<>(restTemaplte);
	}
}
