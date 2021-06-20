SELECT
	/*%expand */*
FROM
	BOOK
WHERE
	/*%if entity.bookId != null */
	BOOK_ID = /* entity.bookId */1
	/*%end*/
	/*%if @isNotEmpty(entity.bookName) */
	AND BOOK_NAME = /* entity.bookName */''
	/*%end*/
	/*%if entity.bookStock != null */
	AND BOOK_STOCK = /* entity.bookStock */1
	/*%end*/
	/*%if @isNotEmpty(entity.txStatus) */
	AND TX_STATUS = /* entity.txStatus */''
	/*%end*/
	