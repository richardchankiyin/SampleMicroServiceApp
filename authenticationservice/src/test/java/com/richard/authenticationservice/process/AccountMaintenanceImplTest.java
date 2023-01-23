package com.richard.authenticationservice.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountSync;
import com.richard.authenticationservice.msg.AccountSynchronizer;

class AccountMaintenanceImplTest {

	private AccountMaintenanceImpl impl; 
	private AccountSequence seq;
	private AccountDao dao;
	private AccountSyncDao acctSyncDao;
	private AccountSynchronizer sync;
	@BeforeEach
	void setup() {
		seq = mock(AccountSequence.class);
		dao = mock(AccountDao.class);
		acctSyncDao = mock(AccountSyncDao.class);
		sync = mock(AccountSynchronizer.class);
		impl = new AccountMaintenanceImpl(seq, dao, acctSyncDao, sync);
	}
	
	@Test
	void testCreateAccountArgNull() {
		Triplet<Boolean, String, Account> result = impl.createAccount(null);
		assertEquals(Triplet.with(false, "[E002]Insufficient Account Info", null), result);
	}

	@Test
	void testCreateAccountNameNull() {
		Triplet<Boolean, String, Account> result = impl.createAccount(new Account());
		assertEquals(Triplet.with(false, "[E002]Insufficient Account Info", null), result);
	}
	
	@Test
	void testCreateAccountNameBlank() {
		Account a = new Account();
		a.setName("");
		Triplet<Boolean, String, Account> result = impl.createAccount(a);
		assertEquals(Triplet.with(false, "[E002]Insufficient Account Info", null), result);
	}
	
	@Test
	void testCreateAccountNameTooShort() {
		Account a = new Account();
		a.setName("ab");
		Triplet<Boolean, String, Account> result = impl.createAccount(a);
		assertEquals(Triplet.with(false, "[E002]Insufficient Account Info", null), result);
	}
	
	@Test
	void testCreateAccountSuccessfully() {
		Account a = new Account();
		a.setName("Jonathon Ray");
		when(seq.getNextSequence()).thenReturn(1L);
		doNothing().when(dao).createAccount(any(Account.class));
		doNothing().when(sync).synchronize(any(Account.class));
		Triplet<Boolean, String, Account> result = impl.createAccount(a);
		String accountno = StringUtils.leftPad(String.valueOf(1L), 18, '0');
		assertTrue(result.getValue0());
		assertEquals("[M001]Account created successfully", result.getValue1());
		assertEquals(accountno, result.getValue2().getAccountno());
		assertEquals("Jonathon Ray", result.getValue2().getName());
	}
	
	@Test
	void testResynchronizeAccountFailedDueToDBIssue() {
		doThrow(RuntimeException.class).when(acctSyncDao).findFailedAccountSyncEntries();
		
		Triplet<Boolean, String, List<Account>> result = impl.resynchronizeAccount();
		assertFalse(result.getValue0());
		assertEquals("[F001]System error", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testResychronizeAccountNotRequired() {
		when(acctSyncDao.findFailedAccountSyncEntries()).thenReturn(new ArrayList<AccountSync>(0));
		Triplet<Boolean, String, List<Account>> result = impl.resynchronizeAccount();
		assertTrue(result.getValue0());
		assertEquals("[M003]No accounts resynchronized required", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testResynchronizeAllFailed() {
		AccountSync sync1 = mock(AccountSync.class);
		AccountSync sync2 = mock(AccountSync.class);
		when(sync1.getMsgkey()).thenReturn("7db0691a-7f28-4ca0-ad95-109e2683a0f7");
		when(sync1.getAccountno()).thenReturn("000000000282310001");
		when(sync1.getPayload()).thenReturn("{\"msgKey\":\"7db0691a-7f28-4ca0-ad95-109e2683a0f7\",\"account\":{\"accountNo\":\"000000000282310001\",\"name\":\"Jennifer\"}}");
		when(sync1.getStatus()).thenReturn("F");
		when(sync2.getMsgkey()).thenReturn("116782d1-ad31-4c5a-bbf9-c3b3519e3681");
		when(sync2.getAccountno()).thenReturn("000000000286700001");
		when(sync2.getPayload()).thenReturn("{\"msgKey\":\"116782d1-ad31-4c5a-bbf9-c3b3519e3681\",\"account\":{\"accountNo\":\"000000000286700001\",\"name\":\"Richard\"}}");
		when(sync2.getStatus()).thenReturn("F");
		List<AccountSync> accountSyncItems = new ArrayList<>(2);
		accountSyncItems.add(sync1);
		accountSyncItems.add(sync2);
		when(acctSyncDao.findFailedAccountSyncEntries()).thenReturn(accountSyncItems);
		when(sync.resynchronize(any(AccountSync.class))).thenReturn(false);
		
		Triplet<Boolean, String, List<Account>> result = impl.resynchronizeAccount();
		
		assertFalse(result.getValue0());
		assertEquals("[W001]Account synchronization with failures", result.getValue1());
		List<Account> failedAccts = result.getValue2();
		assertEquals(2, failedAccts.size());
		assertEquals("000000000282310001" , failedAccts.get(0).getAccountno());
		assertEquals("000000000286700001" , failedAccts.get(1).getAccountno());
	}
	
	@Test
	void testResynchronizePartialFailed() {
		AccountSync sync1 = mock(AccountSync.class);
		AccountSync sync2 = mock(AccountSync.class);
		when(sync1.getMsgkey()).thenReturn("7db0691a-7f28-4ca0-ad95-109e2683a0f7");
		when(sync1.getAccountno()).thenReturn("000000000282310001");
		when(sync1.getPayload()).thenReturn("{\"msgKey\":\"7db0691a-7f28-4ca0-ad95-109e2683a0f7\",\"account\":{\"accountNo\":\"000000000282310001\",\"name\":\"Jennifer\"}}");
		when(sync1.getStatus()).thenReturn("F");
		when(sync2.getMsgkey()).thenReturn("116782d1-ad31-4c5a-bbf9-c3b3519e3681");
		when(sync2.getAccountno()).thenReturn("000000000286700001");
		when(sync2.getPayload()).thenReturn("{\"msgKey\":\"116782d1-ad31-4c5a-bbf9-c3b3519e3681\",\"account\":{\"accountNo\":\"000000000286700001\",\"name\":\"Richard\"}}");
		when(sync2.getStatus()).thenReturn("F");
		List<AccountSync> accountSyncItems = new ArrayList<>(2);
		accountSyncItems.add(sync1);
		accountSyncItems.add(sync2);
		when(acctSyncDao.findFailedAccountSyncEntries()).thenReturn(accountSyncItems);
		when(sync.resynchronize(sync1)).thenReturn(false);
		when(sync.resynchronize(sync2)).thenReturn(true);
		
		Triplet<Boolean, String, List<Account>> result = impl.resynchronizeAccount();
		
		assertFalse(result.getValue0());
		assertEquals("[W001]Account synchronization with failures", result.getValue1());
		List<Account> failedAccts = result.getValue2();
		assertEquals(1, failedAccts.size());
		assertEquals("000000000282310001" , failedAccts.get(0).getAccountno());

	}
	
	@Test
	void testResynchronizeSuccessfully() {
		AccountSync sync1 = mock(AccountSync.class);
		AccountSync sync2 = mock(AccountSync.class);
		when(sync1.getMsgkey()).thenReturn("7db0691a-7f28-4ca0-ad95-109e2683a0f7");
		when(sync1.getAccountno()).thenReturn("000000000282310001");
		when(sync1.getPayload()).thenReturn("{\"msgKey\":\"7db0691a-7f28-4ca0-ad95-109e2683a0f7\",\"account\":{\"accountNo\":\"000000000282310001\",\"name\":\"Jennifer\"}}");
		when(sync1.getStatus()).thenReturn("F");
		when(sync2.getMsgkey()).thenReturn("116782d1-ad31-4c5a-bbf9-c3b3519e3681");
		when(sync2.getAccountno()).thenReturn("000000000286700001");
		when(sync2.getPayload()).thenReturn("{\"msgKey\":\"116782d1-ad31-4c5a-bbf9-c3b3519e3681\",\"account\":{\"accountNo\":\"000000000286700001\",\"name\":\"Richard\"}}");
		when(sync2.getStatus()).thenReturn("F");
		List<AccountSync> accountSyncItems = new ArrayList<>(2);
		accountSyncItems.add(sync1);
		accountSyncItems.add(sync2);
		when(acctSyncDao.findFailedAccountSyncEntries()).thenReturn(accountSyncItems);
		when(sync.resynchronize(any(AccountSync.class))).thenReturn(true);
		
		Triplet<Boolean, String, List<Account>> result = impl.resynchronizeAccount();
		
		assertTrue(result.getValue0());
		assertEquals("[M002]Account Resynchronized successfully", result.getValue1());
		assertNull(result.getValue2());
		
	}
}
