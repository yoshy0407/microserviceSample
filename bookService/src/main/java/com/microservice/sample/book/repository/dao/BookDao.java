package com.microservice.sample.book.repository.dao;

import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.Update;
import org.seasar.doma.boot.ConfigAutowireable;
import org.seasar.doma.jdbc.SelectOptions;

import com.microservice.sample.book.repository.entity.BookEntity;

@Dao
@ConfigAutowireable
public interface BookDao {

	@Select
	public BookEntity select(BookEntity entity, SelectOptions option);
	
	@Select
	public List<BookEntity> selectList(BookEntity entity, SelectOptions option);
	
	@Insert
	public int insert(BookEntity entity);

	@Update
	public int update(BookEntity entity);
	
	@Delete
	public int delete(BookEntity entity);
}
