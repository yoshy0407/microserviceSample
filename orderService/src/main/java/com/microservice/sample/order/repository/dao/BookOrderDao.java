package com.microservice.sample.order.repository.dao;

import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.seasar.doma.jdbc.SelectOptions;

import com.microservice.sample.order.repository.entity.BookOrderEntity;

@ConfigAutowireable
@Dao
public interface BookOrderDao {

	@Insert
	public int insert(BookOrderEntity entity);

	@Update
	public int update(BookOrderEntity entity);

	@Delete
	public int delete(BookOrderEntity entity);
	
	@Select
	public List<BookOrderEntity> selectList(BookOrderEntity entity);
	
	@Select
	public BookOrderEntity select(BookOrderEntity entity, SelectOptions option);

}
