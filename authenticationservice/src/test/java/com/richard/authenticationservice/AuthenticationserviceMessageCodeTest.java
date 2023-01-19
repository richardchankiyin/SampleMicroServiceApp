package com.richard.authenticationservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationserviceMessageCodeTest {

	private AuthenticationserviceMessageCode msgCode;
	@BeforeEach
	void setup() {
		msgCode = new AuthenticationserviceMessageCode();
	}

	@Test
	void testCodeNotExist() {
		assertEquals("", msgCode.getMessage("E000"));
	}
	
	@Test
	void testF001() {
		assertEquals("[F001]System error", msgCode.getMessage("F001"));
	}
	
	@Test
	void testE001() {
		assertEquals("[E001]Wrong Request Content", msgCode.getMessage("E001"));
	}

	
	@Test
	void testE002() {
		assertEquals("[E002]Insufficient Account Info", msgCode.getMessage("E002"));
	}

	@Test
	void testM001() {
		assertEquals("[M001]Account created successfully", msgCode.getMessage("M001"));
	}
}
