package com.richard.transactionservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionserviceMessageCodeTest {

	private TransactionserviceMessageCode msgCode;
	@BeforeEach
	void setup() {
		msgCode = new TransactionserviceMessageCode();
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
		assertEquals("[E002]Account sync request processed before", msgCode.getMessage("E002"));
	}

	@Test
	void testE003() {
		assertEquals("[E003]No account balance found", msgCode.getMessage("E003"));
	}
	
	@Test
	void testM001() {
		assertEquals("[M001]Account sync successfully", msgCode.getMessage("M001"));
	}
	
	@Test
	void testM002() {
		assertEquals("[M002]Unauthorized access", msgCode.getMessage("M002"));
	}
	
	@Test
	void testM003() {
		assertEquals("[M003]Authorized", msgCode.getMessage("M003"));
	}
	
	@Test
	void testM004() {
		assertEquals("[M004]Account Balance retrieved", msgCode.getMessage("M004"));
	}
	
	@Test
	void testM005() {
		assertEquals("[M005]Transfer Amount not accepted", msgCode.getMessage("M005"));
	}
	
	@Test
	void testM006() {
		assertEquals("[M006]Transfer rejected", msgCode.getMessage("M006"));
	}
	
	@Test
	void testM007() {
		assertEquals("[M007]Transfer complete", msgCode.getMessage("M007"));
	}
}
