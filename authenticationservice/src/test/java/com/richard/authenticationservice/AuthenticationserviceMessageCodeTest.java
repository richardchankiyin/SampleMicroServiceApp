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
	void testE003() {
		assertEquals("[E003]Unable to login", msgCode.getMessage("E003"));
	}

	@Test
	void testE004() {
		assertEquals("[E004]Incorrect Login Info", msgCode.getMessage("E004"));
	}
	
	@Test
	void testW001() {
		assertEquals("[W001]Account synchronization with failures", msgCode.getMessage("W001"));
	}
	
	@Test
	void testW002() {
		assertEquals("[W002]Attempt to logout using invalid session key", msgCode.getMessage("W002"));
	}
	
	@Test
	void testM001() {
		assertEquals("[M001]Account created successfully", msgCode.getMessage("M001"));
	}
	
	@Test
	void testM002() {
		assertEquals("[M002]Account Resynchronized successfully", msgCode.getMessage("M002"));
	}
	
	@Test
	void testM003() {
		assertEquals("[M003]No accounts resynchronized required", msgCode.getMessage("M003"));
	}
	
	@Test
	void testM004() {
		assertEquals("[M004]Login successfully", msgCode.getMessage("M004"));
	}
	
	@Test
	void testM005() {
		assertEquals("[M005]Logout successfully", msgCode.getMessage("M005"));
	}
}
