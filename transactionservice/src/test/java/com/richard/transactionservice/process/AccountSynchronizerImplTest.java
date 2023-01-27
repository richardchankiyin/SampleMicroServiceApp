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

import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountDao;
import com.richard.transactionservice.db.AccountSyncDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountSync;

class AccountSynchronizerImplTest {

	private JDBCResourceMgr jdbcResourceMgr;
	private AccountDao acctDao;
	private AccountSyncDao acctSyncDao;
	private AccountBalanceDao acctBalanceDao;
	private AdminMonitor adminMonitor;
	private AccountSynchronizerImpl impl;
	
	private static final String SYSTEMERRORMSG = "[F001]System error";
	
	@BeforeEach
	void setup() {
		jdbcResourceMgr = mock(JDBCResourceMgr.class);
		acctDao = mock(AccountDao.class);
		acctSyncDao = mock(AccountSyncDao.class);
		acctBalanceDao = mock(AccountBalanceDao.class);
		adminMonitor = mock(AdminMonitor.class);
		impl = new AccountSynchronizerImpl(jdbcResourceMgr, acctDao, acctSyncDao, acctBalanceDao, adminMonitor);
	}
	
	@Test
	void testProcessAccountNoInconsistence() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000326490001");
		when(acctSync.getAccountno()).thenReturn("000000000326490002");
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		assertFalse(result.getValue0());
		assertEquals(SYSTEMERRORMSG, result.getValue1());		
	}
	
	@Test
	void testProcessDoneByBlank() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000326490001");
		when(acctSync.getAccountno()).thenReturn("000000000326490001");
		String doneBy = "";
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		assertFalse(result.getValue0());
		assertEquals(SYSTEMERRORMSG, result.getValue1());	
	}

	@Test
	void testProcessAccountSyncFound() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		when(adminMonitor.addDuplicateAccountSync(acctSync)).thenReturn(Triplet.with(true, "[A002]Duplicate Account Sync Message handled", acctSync));
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		when(acctSyncDao.getByMessageKey("13a45560-38e5-44b8-999b-248b3077d63e")).thenReturn(acctSync);
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		assertFalse(result.getValue0());
		assertEquals("[E002]Account sync request processed before", result.getValue1());	
	}
	
	@Test
	void testProcessCheckAccountSyncExistsFailed() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		doThrow(RuntimeException.class).when(acctSyncDao).getByMessageKey(any(String.class));
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		assertFalse(result.getValue0());
		assertEquals(SYSTEMERRORMSG, result.getValue1());
	}
	
	@Test
	void testProcessCreateAccountFailed() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000281980001");
		when(acct.getName()).thenReturn("Nancy");
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		doThrow(EmptyResultDataAccessException.class).when(acctSyncDao).getByMessageKey("13a45560-38e5-44b8-999b-248b3077d63e");
		doThrow(RuntimeException.class).when(acctDao).createAccount(acct);
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcResourceMgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcResourceMgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		verify(txMgr, times(1)).rollback(status);
		verify(txMgr, times(0)).commit(status);

		assertFalse(result.getValue0());
		assertEquals(SYSTEMERRORMSG, result.getValue1());
		assertEquals("000000000281980001", result.getValue2().getAccountno());
		assertEquals("13a45560-38e5-44b8-999b-248b3077d63e", result.getValue2().getMsgkey());
		assertEquals("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}", result.getValue2().getPayload());
	}
	
	@Test
	void testProcessInitAccountBalanceFailed() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000281980001");
		when(acct.getName()).thenReturn("Nancy");
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		doThrow(EmptyResultDataAccessException.class).when(acctSyncDao).getByMessageKey("13a45560-38e5-44b8-999b-248b3077d63e");
		doNothing().when(acctDao).createAccount(acct);
		doThrow(RuntimeException.class).when(acctBalanceDao).initAccountBalanceEntry(any(AccountBalance.class));
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcResourceMgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcResourceMgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		verify(txMgr, times(1)).rollback(status);
		verify(txMgr, times(0)).commit(status);

		assertFalse(result.getValue0());
		assertEquals(SYSTEMERRORMSG, result.getValue1());
		assertEquals("000000000281980001", result.getValue2().getAccountno());
		assertEquals("13a45560-38e5-44b8-999b-248b3077d63e", result.getValue2().getMsgkey());
		assertEquals("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}", result.getValue2().getPayload());
	}
	
	@Test
	void testProcessCreateAccountSyncFailed() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000281980001");
		when(acct.getName()).thenReturn("Nancy");
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		doThrow(EmptyResultDataAccessException.class).when(acctSyncDao).getByMessageKey("13a45560-38e5-44b8-999b-248b3077d63e");
		doNothing().when(acctDao).createAccount(acct);
		ArgumentCaptor<AccountBalance> valueCapture = ArgumentCaptor.forClass(AccountBalance.class);
		doNothing().when(acctBalanceDao).initAccountBalanceEntry(valueCapture.capture());
		doThrow(RuntimeException.class).when(acctSyncDao).createAccountSync(acctSync);
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcResourceMgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcResourceMgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		verify(txMgr, times(1)).rollback(status);
		verify(txMgr, times(0)).commit(status);
		
		assertEquals("000000000281980001", valueCapture.getValue().getAccountno());
		assertEquals(BigDecimal.ZERO, valueCapture.getValue().getBalance());
		assertEquals("98883414-02bf-4a30-9441-f7f76e598e1d", valueCapture.getValue().getDoneby());

		assertFalse(result.getValue0());
		assertEquals(SYSTEMERRORMSG, result.getValue1());
		assertEquals("000000000281980001", result.getValue2().getAccountno());
		assertEquals("13a45560-38e5-44b8-999b-248b3077d63e", result.getValue2().getMsgkey());
		assertEquals("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}", result.getValue2().getPayload());
	}
	
	@Test
	void testProcessSuccessfully() {
		Account acct = mock(Account.class);
		AccountSync acctSync = mock(AccountSync.class);
		when(acct.getAccountno()).thenReturn("000000000281980001");
		when(acct.getName()).thenReturn("Nancy");
		when(acctSync.getAccountno()).thenReturn("000000000281980001");
		when(acctSync.getMsgkey()).thenReturn("13a45560-38e5-44b8-999b-248b3077d63e");
		when(acctSync.getPayload()).thenReturn("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}");
		String doneBy = "98883414-02bf-4a30-9441-f7f76e598e1d";
		
		doThrow(EmptyResultDataAccessException.class).when(acctSyncDao).getByMessageKey("13a45560-38e5-44b8-999b-248b3077d63e");
		doNothing().when(acctDao).createAccount(acct);
		ArgumentCaptor<AccountBalance> valueCapture = ArgumentCaptor.forClass(AccountBalance.class);
		doNothing().when(acctBalanceDao).initAccountBalanceEntry(valueCapture.capture());
		when(acctSyncDao.createAccountSync(acctSync)).thenReturn(1);
		
		PlatformTransactionManager txMgr = mock(PlatformTransactionManager.class);
		TransactionDefinition definition = mock(TransactionDefinition.class);
		TransactionStatus status = mock(TransactionStatus.class);
		when(jdbcResourceMgr.getTransactionManager()).thenReturn(txMgr);
		when(jdbcResourceMgr.createTransactionDefinition()).thenReturn(definition);
		when(txMgr.getTransaction(definition)).thenReturn(status);
		
		Triplet<Boolean, String, AccountSync> result = impl.process(acct, acctSync, doneBy);
		verify(txMgr, times(0)).rollback(status);
		verify(txMgr, times(1)).commit(status);
		
		assertEquals("000000000281980001", valueCapture.getValue().getAccountno());
		assertEquals(BigDecimal.ZERO, valueCapture.getValue().getBalance());
		assertEquals("98883414-02bf-4a30-9441-f7f76e598e1d", valueCapture.getValue().getDoneby());
		
		assertTrue(result.getValue0());
		assertEquals("[M001]Account sync successfully", result.getValue1());
		assertEquals("000000000281980001", result.getValue2().getAccountno());
		assertEquals("13a45560-38e5-44b8-999b-248b3077d63e", result.getValue2().getMsgkey());
		assertEquals("{\"msgKey\":\"13a45560-38e5-44b8-999b-248b3077d63e\",\"account\":{\"accountNo\":\"000000000281980001\",\"name\":\"Nancy\"}}", result.getValue2().getPayload());

	}
}
