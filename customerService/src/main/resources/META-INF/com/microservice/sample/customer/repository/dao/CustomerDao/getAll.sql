SELECT
	/*%expand */*
FROM
	CUSTOMER
WHERE
	/*%if entity.customerId != null */
	CUSTOMER_ID = /* entity.customerId */1
	/*%end*/
	/*%if @isNotEmpty(entity.customerName) */
	AND CUSTOMER_NAME = /* entity.customerName */1
	/*%end*/
	/*%if @isNotEmpty(entity.customerNameKana) */
	AND CUSTOMER_NAME_KANA = /* entity.customerNameKana */1
	/*%end*/
	/*%if @isNotEmpty(entity.customerGender) */
	AND CUSTOMER_GENDER = /* entity.customerGender */1
	/*%end*/
	/*%if entity.bookCount != null */
	AND BOOK_COUNT = /* entity.bookCount */1
	/*%end*/
	/*%if @isNotEmpty(entity.customerBirthday) */
	AND CUSTOMER_BIRTHDAY = /* entity.customerBirthday */1
	/*%end*/
ORDER BY CUSTOMER_ID
	