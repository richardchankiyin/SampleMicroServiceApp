package com.richard.authenticationservice.process;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordVerifierImplTest {

	private PasswordVerifierImpl impl;
	@BeforeEach
	void setup() {
		impl = new PasswordVerifierImpl();
	}
	
	@Test
	void testVerifiedTrue() {
		assertTrue(impl.verify("testing", "notsecurepassword".toCharArray()));
	}
	
	@Test
	void testVerifiedFalse() {
		assertFalse(impl.verify("testing", "unknown".toCharArray()));
	}

}
