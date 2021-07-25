package com.microservice.sample.order.repository.dao;

import java.util.List;

import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.seasar.doma.jdbc.SelectOptions;

import com.microservice.sample.order.repository.entity.OrderDetailEntity;

@Dao
@ConfigAutowireable
public interface OrderDetailDao {

	@Insert
	public int insert(OrderDetailEntity entity);

	@Update
	public int update(OrderDetailEntity entity);

	@Delete
	public int delete(OrderDetailEntity entity);
	
	@BatchInsert
	public int[] batchInsert(List<OrderDetailEntity> entities);
	
	@Select
	public List<OrderDetailEntity> selectList(OrderDetailEntity entity);

	@Select
	public OrderDetailEntity select(OrderDetailEntity entity, SelectOptions option);
}
