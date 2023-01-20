package com.richard.authenticationservice.msg;

import java.util.UUID;

public class MessageKeyGeneratorImpl implements MessageKeyGenerator {

	@Override
	public String generateUniqueKey() {
		return UUID.randomUUID().toString();
	}

}
