SELECT
	/*%expand */*
FROM
	BOOK_ORDER
WHERE
	/*%if entity.orderId != null */
	ORDER_ID = /* entity.orderId */1
	/*%end*/
	/*%if entity.customerId != null */
	AND CUSTOMER_ID = /* entity.customerId */1
	/*%end*/
	/*%if entity.orderDate != null */
	AND ORDER_DATE = /* entity.orderDate */1
	/*%end*/
	/*%if @isNotEmpty(entity.txStatus) */
	AND TX_STATUS = /* entity.txStatus */''
	/*%end*/
ORDER BY 
	ORDER_ID	