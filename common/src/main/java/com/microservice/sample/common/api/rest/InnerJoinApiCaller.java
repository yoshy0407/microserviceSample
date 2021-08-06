package com.microservice.sample.common.api.rest;

import java.util.List;
import java.util.function.Function;

import com.microservice.sample.common.api.spi.JoinApiCaller;
import com.microservice.sample.common.api.spi.QueryApiComposition;

/**
 * INNER JOINを行う{@link JoinApiCaller}です
 * @author yoshy0407
 * @param <BE> ベースとなるエンティティクラス
 * @param <R> レスポンスオブジェクト
 * @param <RE> 結合エンティティ
 */
public class InnerJoinApiCaller<BE, R, RE> extends AbstractJoinApiCaller<BE, R, RE> {

	/**
	 * インスタンスを生成します
	 * 
	 * @param response レスポンス
	 * @param apiCall URIを解決する{@link Function}
	 * @param parent 
	 */
	public InnerJoinApiCaller(Class<R> response, Function<List<RE>, List<R>> apiCall
			,QueryApiComposition<BE, RE> parent) {
		super(response, apiCall, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doResponseNull(List<RE> resultList, int index) {
		resultList.remove(index);		
	}

}
