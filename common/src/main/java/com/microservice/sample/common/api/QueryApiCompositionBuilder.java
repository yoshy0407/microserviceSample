package com.microservice.sample.common.api;

import java.util.List;


import com.microservice.sample.common.api.spi.QueryApiComposition;

/**
 * {link {@link QueryApiComposition}を構築するビルダークラスです
 * 
 * @author yoshy0407
 *
 */
public class QueryApiCompositionBuilder {
	
	/**
	 * {@link QueryApiComposition}を構築します
	 * 
	 * @param <BE> ベースのエンティティ
	 * @param <RE> 結合結果の縁エンティテ
	 * @param baseList
	 * @return
	 */
	public <BE, RE> QueryApiComposition<BE, RE> fromTo(List<BE> baseList, Class<RE> resultClass){
		return new QueryApiCompositionImpl<>(baseList);
	}
}
