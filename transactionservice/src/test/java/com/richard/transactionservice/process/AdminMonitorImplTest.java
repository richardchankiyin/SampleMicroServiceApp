package com.richard.transactionservice.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.richard.transactionservice.Clock;
import com.richard.transactionservice.model.AccountSync;

class AdminMonitorImplTest {

	private AdminMonitorImpl impl;
	private Clock clock;
	private final String validpassword = "iamadmin";
	private final String invalidpassword = "unknown";
	private final String UNAUTHORIZED_MSG = "[M002]Unauthorized access";
	@BeforeEach
	void setup() {
		clock = mock(Clock.class);
		impl = new AdminMonitorImpl(clock);
	}
	
	@Test
	void testCheckStatus() {
		Pair<Boolean, String> validresult = impl.checkStatus(validpassword.toCharArray());
		assertTrue(validresult.getValue0());
		assertEquals("[A001]Service is ready", validresult.getValue1());
		
		Pair<Boolean, String> invalidresult = impl.checkStatus(invalidpassword.toCharArray());
		assertFalse(invalidresult.getValue0());
		assertEquals(UNAUTHORIZED_MSG, invalidresult.getValue1());
	}

	@Test
	void testAddAccountSyncDuplicateAndRetrieveSuccessfully() {
		Timestamp timestamp = new Timestamp(1674466598626L);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598626L);
		AccountSync acctSync = mock(AccountSync.class);
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		Triplet<Boolean, String, AccountSync> result = impl.addDuplicateAccountSync(acctSync);
		assertTrue(result.getValue0());
		assertEquals("[A002]Duplicate Account Sync Message handled", result.getValue1());
		AccountSync returnedSync = result.getValue2();
		assertEquals(acctSync.getAccountno(), returnedSync.getAccountno());
		assertEquals(acctSync.getMsgkey(), returnedSync.getMsgkey());
		assertEquals(acctSync.getPayload(), returnedSync.getPayload());
		assertEquals(timestamp, returnedSync.getUptime());
		assertFalse(acctSync == returnedSync);
		
		Triplet<Boolean, String, AccountSync> result2 = impl.retrieveDuplicateAccountSync(validpassword.toCharArray(), "13a45560-38e5-44b8-999b-248b3077d63e");
		assertTrue(result2.getValue0());
		assertEquals("[A004]Duplicate account sync found", result2.getValue1());
		AccountSync returnedSync2 = result2.getValue2();
		assertEquals(returnedSync.getAccountno(), returnedSync2.getAccountno());
		assertEquals(returnedSync.getMsgkey(), returnedSync2.getMsgkey());
		assertEquals(returnedSync.getPayload(), returnedSync2.getPayload());
		assertEquals(timestamp, returnedSync2.getUptime());
		assertTrue(returnedSync2 == returnedSync);
	}
	
	@Test
	void testAddAccountSyncDuplicateAndRetrieveFailedDuetowrongpassword() {
		Timestamp timestamp = new Timestamp(1674466598626L);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598626L);
		AccountSync acctSync = mock(AccountSync.class);
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		Triplet<Boolean, String, AccountSync> result = impl.addDuplicateAccountSync(acctSync);
		assertTrue(result.getValue0());
		assertEquals("[A002]Duplicate Account Sync Message handled", result.getValue1());
		AccountSync returnedSync = result.getValue2();
		assertEquals(acctSync.getAccountno(), returnedSync.getAccountno());
		assertEquals(acctSync.getMsgkey(), returnedSync.getMsgkey());
		assertEquals(acctSync.getPayload(), returnedSync.getPayload());
		assertEquals(timestamp, returnedSync.getUptime());
		assertFalse(acctSync == returnedSync);
		
		Triplet<Boolean, String, AccountSync> result2 = impl.retrieveDuplicateAccountSync(invalidpassword.toCharArray(), "13a45560-38e5-44b8-999b-248b3077d63e");
		assertFalse(result2.getValue0());
		assertEquals(UNAUTHORIZED_MSG, result2.getValue1());
		assertNull(result2.getValue2());
	}
	
	@Test
	void testRetrieveNoItem() {
		Triplet<Boolean, String, AccountSync> result = impl.retrieveDuplicateAccountSync(validpassword.toCharArray(), "???");
		assertFalse(result.getValue0());
		assertEquals("[A003]No duplicate account sync found", result.getValue1());
		assertNull(result.getValue2());
	}
}
