package com.richard.transactionservice.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import com.richard.transactionservice.Clock;
import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountTransferDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountTransfer;

class AccountBalanceMaintenanceImplTest {

	private AccountBalanceMaintenanceImpl impl;
	private Clock clock;
	private JDBCResourceMgr jdbcresourcemgr;
	private AccountBalanceDao accountBalanceDao;
	private AccountBalance accountBalance;
	private AccountTransferDao accountTransferDao;
	private AccountTransfer accountTransfer;
	private static final String ACCOUNT_BALANCE_NOT_FOUND_CONTENT = "[E003]No account balance found";
	private static final String WRONG_REQUEST_CONTENT = "[E001]Wrong Request Content";
	private static final String TRANSFER_REJECT_CONTENT = "[M006]Transfer rejected";
	private static final String TRANSFER_COMPLETE_CONTENT = "[M007]Transfer complete";
	
	@BeforeEach
	void setup() {
		clock = mock(Clock.class);
		accountBalance = mock(AccountBalance.class);
		accountTransfer = mock(AccountTransfer.class);
		jdbcresourcemgr = mock(JDBCResourceMgr.class);
		accountBalanceDao = mock(AccountBalanceDao.class);
		accountTransferDao = mock(AccountTransferDao.class);
		impl = new AccountBalanceMaintenanceImpl(clock, jdbcresourcemgr
				, accountBalanceDao, accountTransferDao);
	}
	
	@Test
	void testEnquireBalanceNoAccountFound() {
		doThrow(EmptyResultDataAccessException.class).when(accountBalanceDao).getByAccountNo("000000000354820001");
		Triplet<Boolean, String, AccountBalance> result = impl.enquireBalance("000000000354820001");
		
		assertFalse(result.getValue0());
		assertEquals(ACCOUNT_BALANCE_NOT_FOUND_CONTENT, result.getValue1());
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
		Timestamp timestamp = new Timestamp(1674466598627L);
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
	
	@Test
	void testTransferNullArg() {
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(null);
		assertFalse(result.getValue0());
		assertEquals(WRONG_REQUEST_CONTENT, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testTransferMissingFields() {
		when(accountTransfer.getAccountno()).thenReturn("000000000354820001");
		when(accountTransfer.getDoneby()).thenReturn(null);
		when(accountTransfer.getAmount()).thenReturn(null);
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(accountTransfer);
		assertFalse(result.getValue0());
		assertEquals(WRONG_REQUEST_CONTENT, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testTransferZeroAmount() {
		when(accountTransfer.getAccountno()).thenReturn("000000000354820001");
		when(accountTransfer.getDoneby()).thenReturn("c739ace0-ac56-41f3-b9c7-a225add955fb");
		when(accountTransfer.getAmount()).thenReturn(BigDecimal.ZERO);
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(accountTransfer);
		assertFalse(result.getValue0());
		assertEquals("[M005]Transfer Amount not accepted", result.getValue1());
		assertNull(result.getValue2());		
	}
	
	@Test
	void testTransferRetrieveBalanceFailed() {
		when(accountTransfer.getAccountno()).thenReturn("000000000354820001");
		when(accountTransfer.getDoneby()).thenReturn("c739ace0-ac56-41f3-b9c7-a225add955fb");
		when(accountTransfer.getAmount()).thenReturn(BigDecimal.valueOf(100));
		doThrow(RuntimeException.class).when(accountBalanceDao).getByAccountNo(any(String.class));
		
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(accountTransfer);
		assertFalse(result.getValue0());
		assertEquals(ACCOUNT_BALANCE_NOT_FOUND_CONTENT, result.getValue1());
		assertNull(result.getValue2());
		
	}
	
	@Test
	void testTransferUpdateBalanceFailed() {
		Timestamp timestamp = new Timestamp(1674466598626L);
		when(accountTransfer.getAccountno()).thenReturn("000000000354820001");
		when(accountTransfer.getDoneby()).thenReturn("c739ace0-ac56-41f3-b9c7-a225add955fb");
		when(accountTransfer.getAmount()).thenReturn(BigDecimal.valueOf(100));
		
		when(accountBalance.getAccountno()).thenReturn("000000000354820001");
		when(accountBalance.getBalance()).thenReturn(BigDecimal.valueOf(20));
		when(accountBalance.getDoneby()).thenReturn("00b24637-e806-47e1-a0d7-de1a4ba290bd");
		when(accountBalance.getUptime()).thenReturn(timestamp);
		
		when(accountBalanceDao.getByAccountNo("000000000354820001")).thenReturn(accountBalance);
		ArgumentCaptor<AccountBalance> valueCapture = ArgumentCaptor.forClass(AccountBalance.class);
		doThrow(RuntimeException.class).when(accountBalanceDao).updateAccountBalance(any(AccountBalance.class)
				, valueCapture.capture());
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L);
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcresourcemgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcresourcemgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(accountTransfer);
		
		
		assertEquals("000000000354820001", valueCapture.getValue().getAccountno());
		assertEquals(BigDecimal.valueOf(120), valueCapture.getValue().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", valueCapture.getValue().getDoneby());
		assertEquals(new Timestamp(1674466598627L), valueCapture.getValue().getUptime());
		
		verify(txMgr, times(1)).rollback(status);
		verify(txMgr, times(0)).commit(status);
		
		assertFalse(result.getValue0());
		assertEquals(TRANSFER_REJECT_CONTENT, result.getValue1());
		assertEquals("000000000354820001",result.getValue2().getAccountno());
		assertEquals(BigDecimal.valueOf(120), result.getValue2().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2().getDoneby());
		assertEquals(new Timestamp(1674466598627L), result.getValue2().getUptime());		
	}
	
	@Test
	void testTransferCreateAccountTransferFailed() {
		Timestamp timestamp = new Timestamp(1674466598626L);
		when(accountTransfer.getAccountno()).thenReturn("000000000354820001");
		when(accountTransfer.getDoneby()).thenReturn("c739ace0-ac56-41f3-b9c7-a225add955fb");
		when(accountTransfer.getAmount()).thenReturn(BigDecimal.valueOf(100));
		
		when(accountBalance.getAccountno()).thenReturn("000000000354820001");
		when(accountBalance.getBalance()).thenReturn(BigDecimal.valueOf(20));
		when(accountBalance.getDoneby()).thenReturn("00b24637-e806-47e1-a0d7-de1a4ba290bd");
		when(accountBalance.getUptime()).thenReturn(timestamp);
		
		when(accountBalanceDao.getByAccountNo("000000000354820001")).thenReturn(accountBalance);
		ArgumentCaptor<AccountBalance> valueCapture = ArgumentCaptor.forClass(AccountBalance.class);
		doNothing().when(accountBalanceDao).updateAccountBalance(any(AccountBalance.class)
				, valueCapture.capture());
		doThrow(RuntimeException.class).when(accountTransferDao).createAccountTransfer(accountTransfer);
		
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L);
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcresourcemgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcresourcemgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(accountTransfer);
		
		
		assertEquals("000000000354820001", valueCapture.getValue().getAccountno());
		assertEquals(BigDecimal.valueOf(120), valueCapture.getValue().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", valueCapture.getValue().getDoneby());
		assertEquals(new Timestamp(1674466598627L), valueCapture.getValue().getUptime());
		
		verify(txMgr, times(1)).rollback(status);
		verify(txMgr, times(0)).commit(status);
		
		assertFalse(result.getValue0());
		assertEquals(TRANSFER_REJECT_CONTENT, result.getValue1());
		assertEquals("000000000354820001",result.getValue2().getAccountno());
		assertEquals(BigDecimal.valueOf(120), result.getValue2().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2().getDoneby());
		assertEquals(new Timestamp(1674466598627L), result.getValue2().getUptime());		
		
	}
	
	private void transferSuccessfulCaseTemplate(BigDecimal balance, BigDecimal amount, BigDecimal expectedBalance) {
		Timestamp timestamp = new Timestamp(1674466598626L);
		when(accountTransfer.getAccountno()).thenReturn("000000000354820001");
		when(accountTransfer.getDoneby()).thenReturn("c739ace0-ac56-41f3-b9c7-a225add955fb");
		when(accountTransfer.getAmount()).thenReturn(amount);
		
		when(accountBalance.getAccountno()).thenReturn("000000000354820001");
		when(accountBalance.getBalance()).thenReturn(balance);
		when(accountBalance.getDoneby()).thenReturn("00b24637-e806-47e1-a0d7-de1a4ba290bd");
		when(accountBalance.getUptime()).thenReturn(timestamp);
		
		when(accountBalanceDao.getByAccountNo("000000000354820001")).thenReturn(accountBalance);
		ArgumentCaptor<AccountBalance> valueCapture = ArgumentCaptor.forClass(AccountBalance.class);
		doNothing().when(accountBalanceDao).updateAccountBalance(any(AccountBalance.class)
				, valueCapture.capture());
		doNothing().when(accountTransferDao).createAccountTransfer(accountTransfer);
		
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L);
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcresourcemgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcresourcemgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		Triplet<Boolean, String, AccountBalance> result = impl.transfer(accountTransfer);
		
		
		assertEquals("000000000354820001", valueCapture.getValue().getAccountno());
		assertEquals(expectedBalance, valueCapture.getValue().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", valueCapture.getValue().getDoneby());
		assertEquals(new Timestamp(1674466598627L), valueCapture.getValue().getUptime());
		
		verify(txMgr, times(0)).rollback(status);
		verify(txMgr, times(1)).commit(status);
		
		assertTrue(result.getValue0());
		assertEquals(TRANSFER_COMPLETE_CONTENT, result.getValue1());
		assertEquals("000000000354820001",result.getValue2().getAccountno());
		assertEquals(expectedBalance, result.getValue2().getBalance());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2().getDoneby());
		assertEquals(new Timestamp(1674466598627L), result.getValue2().getUptime());
	}
	
	
	@Test
	void testTransferSuccessfulPositiveBalanceIncreaseAmountToPositiveBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(120));	
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(100.5), BigDecimal.valueOf(10.5), BigDecimal.valueOf(111.0));
	}
	
	@Test
	void testTransferSuccessfulPositiveBalanceDecreaseAmountToPositiveBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(100), BigDecimal.valueOf(-20), BigDecimal.valueOf(80));
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(100.5), BigDecimal.valueOf(-10.5), BigDecimal.valueOf(90.0));
	}
	
	@Test
	void testTransferSuccessfulPositiveBalanceDecreaseAmountToNegativeBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(100), BigDecimal.valueOf(-200), BigDecimal.valueOf(-100));
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(100.5), BigDecimal.valueOf(-300.1), BigDecimal.valueOf(-199.6));
	}
	
	
	@Test
	void testTransferSuccessfulZeroBalanceIncreaseAmountToPositiveBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.ZERO, BigDecimal.valueOf(20), BigDecimal.valueOf(20));
		transferSuccessfulCaseTemplate(
				BigDecimal.ZERO, BigDecimal.valueOf(20.1), BigDecimal.valueOf(20.1));
	}
	
	@Test
	void testTransferSuccessfulZeroBalanceDecreaseAmountToNegativeBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.ZERO, BigDecimal.valueOf(-20.1), BigDecimal.valueOf(-20.1));
	}
	
	@Test
	void testTransferSuccessfulNegativeBalanceIncreaseAmountToPositiveBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(-100), BigDecimal.valueOf(150), BigDecimal.valueOf(50));
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(-100), BigDecimal.valueOf(120.5), BigDecimal.valueOf(20.5));
	}
	
	@Test
	void testTransferSuccessfulNegativeBalanceIncreaseAmountToNegativeBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(-100), BigDecimal.valueOf(20), BigDecimal.valueOf(-80));
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(-100), BigDecimal.valueOf(20.5), BigDecimal.valueOf(-79.5));
	}
	
	@Test
	void testTransferSuccessfulNegativeBalanceDecreaseAmountToNegativeBalance() {
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(-100), BigDecimal.valueOf(-20), BigDecimal.valueOf(-120));
		transferSuccessfulCaseTemplate(
				BigDecimal.valueOf(-100), BigDecimal.valueOf(-20.5), BigDecimal.valueOf(-120.5));
	}
	
}
