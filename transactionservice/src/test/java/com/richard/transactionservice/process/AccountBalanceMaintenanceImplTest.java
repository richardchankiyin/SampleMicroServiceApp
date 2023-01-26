package com.richard.transactionservice.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.model.AccountBalance;

class AccountBalanceMaintenanceImplTest {

	private AccountBalanceMaintenanceImpl impl;
	private JDBCResourceMgr jdbcresourcemgr;
	private AccountBalanceDao accountBalanceDao;
	private AccountBalance accountBalance;
	@BeforeEach
	void setup() {
		accountBalance = mock(AccountBalance.class);
		jdbcresourcemgr = mock(JDBCResourceMgr.class);
		accountBalanceDao = mock(AccountBalanceDao.class);
		impl = new AccountBalanceMaintenanceImpl(jdbcresourcemgr, accountBalanceDao);
	}
	
	@Test
	void testEnquireBalanceNoAccountFound() {
		doThrow(EmptyResultDataAccessException.class).when(accountBalanceDao).getByAccountNo("000000000354820001");
		Triplet<Boolean, String, AccountBalance> result = impl.enquireBalance("000000000354820001");
		
		assertFalse(result.getValue0());
		assertEquals("[E003]No account balance found", result.getValue1());
		assertNull(result.getValue2());
	}

	@Test
	void testEnqireBalanceDaoIssue() {
		doThrow(RuntimeException.class).when(accountBalanceDao).getByAccountNo("000000000354820001");
		Triplet<Boolean, String, AccountBalance> result = impl.enquireBalance("000000000354820001");
		
		assertFalse(result.getValue0());
		assertEquals("[F001]System error", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testEnquireBalanceSuccessful() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		when(accountBalance.getAccountno()).thenReturn("000000000354820001");
		when(accountBalance.getBalance()).thenReturn(BigDecimal.valueOf(100.5));
		when(accountBalance.getDoneby()).thenReturn("c739ace0-ac56-41f3-b9c7-a225add955fb");
		when(accountBalance.getUptime()).thenReturn(timestamp);
		
		when(accountBalanceDao.getByAccountNo("000000000354820001")).thenReturn(accountBalance);
		
		Triplet<Boolean, String, AccountBalance> result = impl.enquireBalance("000000000354820001");
		
		assertTrue(result.getValue0());
		assertEquals("[M004]Account Balance retrieved", result.getValue1());
		assertEquals("000000000354820001", result.getValue2().getAccountno());
		assertEquals(BigDecimal.valueOf(100.5), result.getValue2().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2().getDoneby());
		assertEquals(timestamp, result.getValue2().getUptime());
	}
	
}
