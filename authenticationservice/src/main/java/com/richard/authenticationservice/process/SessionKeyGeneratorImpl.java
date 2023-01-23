package com.richard.authenticationservice.process;

import java.util.UUID;

public class SessionKeyGeneratorImpl implements SessionKeyGenerator {

	@Override
	public String generateSessionKey() {
		return UUID.randomUUID().toString();
	}

}
