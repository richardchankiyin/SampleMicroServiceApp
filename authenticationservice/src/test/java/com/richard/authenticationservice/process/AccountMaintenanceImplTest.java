package com.richard.authenticationservice.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.msg.AccountSynchronizer;

class AccountMaintenanceImplTest {

	private AccountMaintenanceImpl impl; 
	private AccountSequence seq;
	private AccountDao dao;
	private AccountSynchronizer sync;
	@BeforeEach
	void setup() {
		seq = mock(AccountSequence.class);
		dao = mock(AccountDao.class);
		sync = mock(AccountSynchronizer.class);
		impl = new AccountMaintenanceImpl(seq, dao, sync);
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
	
}
