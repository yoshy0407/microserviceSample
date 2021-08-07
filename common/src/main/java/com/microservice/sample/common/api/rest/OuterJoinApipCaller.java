package com.microservice.sample.common.api.rest;

import java.util.List;
import java.util.function.Function;

import com.microservice.sample.common.api.spi.JoinApiCaller;
import com.microservice.sample.common.api.spi.QueryApiCompositionBuilder;

/**
 * OUTER JOINを行う{@link JoinApiCaller}です
 * @author yoshy0407
 * @param <BE> ベースとなるエンティティ
 * @param <R> レスポンスオブジェクト
 * @param <RE> 結合エンティティ
 */

public class OuterJoinApipCaller<BE, R, RE> extends AbstractJoinApiCaller<BE, R, RE>{

	/**
	 * インスタンスを生成します
	 * 
	 * @param response レスポンス
	 * @param apiCall URIを解決する{@link Function}
	 * @param parent 親のインスタンス
	 */
	public OuterJoinApipCaller(Class<R> response, Function<List<BE>, List<R>> apiCall, 
			QueryApiCompositionBuilder<BE, RE> parent) {
		super(response, apiCall, parent);
	}

	@Override
	protected void doResponseNull(List<RE> resultList, int index) {
		
	}

}
