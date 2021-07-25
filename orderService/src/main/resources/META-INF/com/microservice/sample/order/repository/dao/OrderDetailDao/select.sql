SELECT
	/*%expand */*
FROM
	ORDER_DETAIL
WHERE
	/*%if entity.orderId != null */
	ORDER_ID = /* entity.orderId */1
	/*%end*/
	/*%if entity.bookId != null */
	AND BOOK_ID = /* entity.bookId */1
	/*%end*/
	/*%if entity.orderCount != null */
	AND ORDER_COUNT = /* entity.orderCount */1
	/*%end*/
	/*%if @isNotEmpty(entity.txStatus) */
	AND TX_STATUS = /* entity.txStatus */''
	/*%end*/