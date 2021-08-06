package com.microservice.sample.customer.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.microservice.sample.common.TransactionIdRegistry;
import com.microservice.sample.customer.CustomerTopicConstant;

@Component
public class TransactionIdRegistryImpl implements TransactionIdRegistry {

	int index = 0;
	
	Map<String, Set<String>> map = new HashMap<>();
	
	public TransactionIdRegistryImpl() {
		map.put(CustomerTopicConstant.CUSTOMER, new HashSet<>());
		map.put(CustomerTopicConstant.CUSTOMER_COMPLETE, new HashSet<>());
		map.put(CustomerTopicConstant.CUSTOMER_ROLLBACK, new HashSet<>());
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
