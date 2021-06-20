package com.microservice.sample.customer.repository.dao;

import java.util.List;
import java.util.Optional;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.seasar.doma.jdbc.SelectOptions;

import com.microservice.sample.customer.repository.entity.CustomerEntity;

@ConfigAutowireable
@Dao
public interface CustomerDao {

	@Select
	public Optional<CustomerEntity> selectById(Integer customerId, String txStatus, SelectOptions option);
	
	@Select
	public List<CustomerEntity> getAll(CustomerEntity entity);
	
	@Update
	public int update(CustomerEntity entity);
}
