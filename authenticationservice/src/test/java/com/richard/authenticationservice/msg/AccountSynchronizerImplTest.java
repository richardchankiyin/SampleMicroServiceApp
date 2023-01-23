package com.richard.authenticationservice.msg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;

import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountSync;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountSynchronizerImplTest {

	private AccountSynchronizerImpl impl;
	private MessageKeyGenerator gen;
	private AmqpTemplate amqp;
	private AccountSyncDao dao;
	private Account account;

	@BeforeEach
	void setup() {
		gen = mock(MessageKeyGenerator.class);
		amqp = mock(AmqpTemplate.class);
		dao = mock(AccountSyncDao.class);
		account = mock(Account.class);
		impl = new AccountSynchronizerImpl(gen, amqp, dao);
	}
	
	@Test
	void testSynchronizeSuccess() {
		when(account.getAccountno()).thenReturn("000000000281880001");
		when(account.getName()).thenReturn("Sam Chan");
		when(gen.generateUniqueKey()).thenReturn("e0011e6a-e0a1-434a-9345-cf427f08df59");
		doNothing().when(amqp).convertAndSend(any(String.class), any(Object.class));
		ArgumentCaptor<AccountSync> valueCaptureForAccountSync = ArgumentCaptor.forClass(AccountSync.class);
		doNothing().when(dao).createAccountSync(valueCaptureForAccountSync.capture());
		
		impl.synchronize(account);
		
		final String queuename = "accountsync";
		final String expectedPayload = 
				"{\"msgKey\":\"e0011e6a-e0a1-434a-9345-cf427f08df59\",\"account\":{\"accountNo\":\"000000000281880001\",\"name\":\"Sam Chan\"}}";
		
		verify(amqp, times(1)).convertAndSend(queuename, expectedPayload);
		verify(dao, times(1)).createAccountSync(any(AccountSync.class));
		assertEquals("e0011e6a-e0a1-434a-9345-cf427f08df59", valueCaptureForAccountSync.getValue().getMsgkey());
		assertEquals("000000000281880001", valueCaptureForAccountSync.getValue().getAccountno());
		assertEquals(expectedPayload, valueCaptureForAccountSync.getValue().getPayload());
		assertEquals("S", valueCaptureForAccountSync.getValue().getStatus());
	}
	
	@Test
	void testSynchronizeFailDueToAMQPIssue() {
		when(account.getAccountno()).thenReturn("000000000281880001");
		when(account.getName()).thenReturn("Sam Chan");
		when(gen.generateUniqueKey()).thenReturn("e0011e6a-e0a1-434a-9345-cf427f08df59");
		doThrow(AmqpException.class).when(amqp).convertAndSend(any(String.class), any(Object.class));
		ArgumentCaptor<AccountSync> valueCaptureForAccountSync = ArgumentCaptor.forClass(AccountSync.class);
		doNothing().when(dao).createAccountSync(valueCaptureForAccountSync.capture());
		
		impl.synchronize(account);

		final String expectedPayload = 
				"{\"msgKey\":\"e0011e6a-e0a1-434a-9345-cf427f08df59\",\"account\":{\"accountNo\":\"000000000281880001\",\"name\":\"Sam Chan\"}}";
		
		verify(dao, times(1)).createAccountSync(any(AccountSync.class));
		assertEquals("e0011e6a-e0a1-434a-9345-cf427f08df59", valueCaptureForAccountSync.getValue().getMsgkey());
		assertEquals("000000000281880001", valueCaptureForAccountSync.getValue().getAccountno());
		assertEquals(expectedPayload, valueCaptureForAccountSync.getValue().getPayload());
		assertEquals("F", valueCaptureForAccountSync.getValue().getStatus());		
	}

	@Test
	void testResynchronizeSuccess() {
		AccountSync acctSync = new AccountSync();
		acctSync.setMsgkey("e0011e6a-e0a1-434a-9345-cf427f08df59");
		acctSync.setAccountno("000000000281880001");
		acctSync.setPayload("{\"msgKey\":\"e0011e6a-e0a1-434a-9345-cf427f08df59\",\"account\":{\"accountNo\":\"000000000281880001\",\"name\":\"Sam Chan\"}}");
		acctSync.setStatus(false);
		
		doNothing().when(amqp).convertAndSend(any(String.class), any(Object.class));
		doNothing().when(dao).updateAccountSyncStatus(any(AccountSync.class));
		
		impl.resynchronize(acctSync);
		
		final String queuename = "accountsync";
		final String expectedPayload = 
				"{\"msgKey\":\"e0011e6a-e0a1-434a-9345-cf427f08df59\",\"account\":{\"accountNo\":\"000000000281880001\",\"name\":\"Sam Chan\"}}";
		
		verify(amqp, times(1)).convertAndSend(queuename, expectedPayload);
		verify(dao, times(1)).updateAccountSyncStatus(any(AccountSync.class));
		assertEquals("e0011e6a-e0a1-434a-9345-cf427f08df59", acctSync.getMsgkey());
		assertEquals("000000000281880001", acctSync.getAccountno());
		assertEquals(expectedPayload, acctSync.getPayload());
		assertEquals("S", acctSync.getStatus());		
		
	}
	
	@Test
	void testResynchronizeFailDueToAMQPIssue() {
		AccountSync acctSync = new AccountSync();
		acctSync.setMsgkey("e0011e6a-e0a1-434a-9345-cf427f08df59");
		acctSync.setAccountno("000000000281880001");
		acctSync.setPayload("{\"msgKey\":\"e0011e6a-e0a1-434a-9345-cf427f08df59\",\"account\":{\"accountNo\":\"000000000281880001\",\"name\":\"Sam Chan\"}}");
		acctSync.setStatus(false);
		
		doThrow(AmqpException.class).when(amqp).convertAndSend(any(String.class), any(Object.class));
		doNothing().when(dao).updateAccountSyncStatus(any(AccountSync.class));
		
		impl.resynchronize(acctSync);
		
		final String expectedPayload = 
				"{\"msgKey\":\"e0011e6a-e0a1-434a-9345-cf427f08df59\",\"account\":{\"accountNo\":\"000000000281880001\",\"name\":\"Sam Chan\"}}";
		
		
		verify(dao, times(1)).updateAccountSyncStatus(any(AccountSync.class));
		assertEquals("e0011e6a-e0a1-434a-9345-cf427f08df59", acctSync.getMsgkey());
		assertEquals("000000000281880001", acctSync.getAccountno());
		assertEquals(expectedPayload, acctSync.getPayload());
		assertEquals("F", acctSync.getStatus());		
		
	}
	
}
