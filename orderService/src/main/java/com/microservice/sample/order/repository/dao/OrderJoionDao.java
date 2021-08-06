package com.microservice.sample.order.repository.dao;

import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import com.microservice.sample.order.model.order.OrderDto;

@Dao
@ConfigAutowireable
public interface OrderJoionDao {

	@Select
	public List<OrderDto> selectList();
}
