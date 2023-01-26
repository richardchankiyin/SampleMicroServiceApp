package com.richard.transactionservice.process;

import java.util.UUID;

public class AccountTransferRequestIdGeneratorImpl implements AccountTransferRequestIdGenerator {
	@Override
	public String generateUniqueRequestId() {
		return UUID.randomUUID().toString();
	}

}
