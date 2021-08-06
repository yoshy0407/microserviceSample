package com.microservice.sample.book.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.microservice.sample.book.TopicConstant;
import com.microservice.sample.common.TransactionIdRegistry;

@Component
public class TransactionIdRegistryImpl implements TransactionIdRegistry {

	int index = 0;
	
	Map<String, Set<String>> map = new HashMap<>();
	
	public TransactionIdRegistryImpl() {
		map.put(TopicConstant.BOOK_STOCK, new HashSet<>());
		map.put(TopicConstant.BOOK_STOCK_COMPLETE, new HashSet<>());
		map.put(TopicConstant.BOOK_STOCK_RESULT, new HashSet<>());
		map.put(TopicConstant.BOOK_STOCK_ROLLBACK, new HashSet<>());
	}
	
	@Override
	public String getNextVal(String name) {
		index++;
		return String.format("%07d", index);
		
	}

	@Override
	public boolean validateId(String name, String transactionId) {
		Set<String> transactionIds = map.get(name);
		 if (transactionIds.contains(transactionId)) {
			 return false;
		 } else {
			 transactionIds.add(transactionId);
			 return true;
		 }
	}

}
