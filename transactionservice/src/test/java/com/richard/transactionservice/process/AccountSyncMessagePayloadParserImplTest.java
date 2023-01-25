package com.richard.transactionservice.process;

import static org.junit.jupiter.api.Assertions.*;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountSync;

class AccountSyncMessagePayloadParserImplTest {

	private AccountSyncMessagePayloadParserImpl impl;
	
	@BeforeEach
	void setup() {
		impl = new AccountSyncMessagePayloadParserImpl();
	}
	
	@Test
	void testParseInvalidJson() {
		String payload = "xxx";
		assertNull(impl.parse(payload));
	}
	
	@Test
	void testParseMessageKeyMissing() {
		String payload = "{\"account\":{\"accountNo\":\"000000000326490001\",\"name\":\"Susan\"}}";
		assertNull(impl.parse(payload));
	}
	
	@Test
	void testParseAccountNodeMissing() {
		String payload = "{\"msgKey\":\"53380438-9f07-4190-93bf-388a95c820b5\"}";
		assertNull(impl.parse(payload));
	}
	
	@Test
	void testParseAccountNoMissing() {
		String payload = "{\"msgKey\":\"53380438-9f07-4190-93bf-388a95c820b5\",\"account\":{\"name\":\"Susan\"}}";
		assertNull(impl.parse(payload));
	}
	
	@Test
	void testParseNameMissing() {
		String payload = "{\"msgKey\":\"53380438-9f07-4190-93bf-388a95c820b5\",\"account\":{\"accountNo\":\"000000000326490001\"}}";
		assertNull(impl.parse(payload));
	}
	
	@Test
	void testParseSuccessfully() {
		String payload = "{\"msgKey\":\"53380438-9f07-4190-93bf-388a95c820b5\",\"account\":{\"accountNo\":\"000000000326490001\",\"name\":\"Susan\"}}";
		Pair<Account, AccountSync> result = impl.parse(payload);
		assertEquals("000000000326490001", result.getValue0().getAccountno());
		assertEquals("Susan", result.getValue0().getName());
		assertEquals("000000000326490001", result.getValue1().getAccountno());
		assertEquals("53380438-9f07-4190-93bf-388a95c820b5", result.getValue1().getMsgkey());
	}

}
