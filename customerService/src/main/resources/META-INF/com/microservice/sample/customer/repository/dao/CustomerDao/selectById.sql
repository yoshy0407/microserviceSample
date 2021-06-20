 SELECT
	/*%expand */*
FROM
	CUSTOMER
WHERE
	CUSTOMER_ID = /* customerId */0
	AND TX_STATUS = /* txStatus */''