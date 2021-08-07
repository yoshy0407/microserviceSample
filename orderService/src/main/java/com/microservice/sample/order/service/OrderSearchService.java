package com.microservice.sample.order.service;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import com.microservice.sample.common.api.Key;
import com.microservice.sample.common.api.composition.ApiComposition;
import com.microservice.sample.common.api.composition.QueryApiComposition;
import com.microservice.sample.order.URLConstant;
import com.microservice.sample.order.model.book.BookData;
import com.microservice.sample.order.model.customer.CustomerData;
import com.microservice.sample.order.model.order.OrderDto;
import com.microservice.sample.order.model.order.OrderResultDto;
import com.microservice.sample.order.repository.dao.OrderJoionDao;

@Service
@Transactional(noRollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
public class OrderSearchService {

	@Autowired
	OrderJoionDao dao;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	ApiComposition apiComposition;
	
	public List<OrderResultDto> selectApiComposition(){
		List<OrderDto> orders = dao.selectList();
		
		return apiComposition
				.execute(
						OrderDto.class, 
						OrderResultDto.class,
						builder -> {
							builder
								.select(OrderResultDto.class)
								.from(orders)
								.innerJoin(BookData.class, r -> {
									RequestEntity<BookData> req = 
											new RequestEntity<>(HttpMethod.GET, uri(URLConstant.BOOK_SERVICE));
									return restTemplate
												.exchange(req, new ParameterizedTypeReference<List<BookData>>() {})
												.getBody();
								})
									.onEqual(o -> new Key(o.getBookId()), r -> new Key(r.getBookId()))
									.mapToEntity((o, b) -> {
										o.setBookName(b.getBookName());
									})
							.and()
								.innerJoin(CustomerData.class, r -> {
									RequestEntity<CustomerData> req = 
											new RequestEntity<>(HttpMethod.GET, uri(URLConstant.CUSTOMER_SERVICE));
									return restTemplate
												.exchange(req, new ParameterizedTypeReference<List<CustomerData>>() {})
												.getBody();
								})
									.onEqual(o -> new Key(o.getCustomerId()), r -> new Key(r.getCustomerId()))
									.mapToEntity((o, c) -> {
										o.setCustomerName(c.getCustomerName());
										o.setCustomerNameKana(c.getCustomerNameKana());
									});
						});				
	}
	
	private URI uri(String uri) {
		URI result = null;
		try {
			result = new URI(uri);			
		} catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
		return result;
	}
}
