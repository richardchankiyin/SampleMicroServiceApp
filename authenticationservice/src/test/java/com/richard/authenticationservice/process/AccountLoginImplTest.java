package com.richard.authenticationservice.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;

import com.richard.authenticationservice.Clock;
import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountLoginSessionDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountLoginSession;


class AccountLoginImplTest {

	private AccountLoginImpl impl;
	private PasswordVerifier passwordVerify;
	private SessionKeyGenerator sessionKeyGen;
	private Clock clock;
	private AccountLoginSessionDao dao;
	private AccountDao accountDao;
	private static final String UNABLE_TO_LOGIN_MSG = "[E003]Unable to login";
	@BeforeEach
	void setup() {
		passwordVerify = mock(PasswordVerifier.class);
		sessionKeyGen = mock(SessionKeyGenerator.class);
		clock = mock(Clock.class);
		dao = mock(AccountLoginSessionDao.class);
		accountDao = mock(AccountDao.class);
		impl = new AccountLoginImpl(passwordVerify, sessionKeyGen, clock, 300 * 1000, accountDao, dao);
	}
	
	@Test
	void testLoginAccountNotExists() {
		when(accountDao.getAccount("000000000326490001")).thenReturn(null);
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "badpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals("[E004]Incorrect Login Info", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginAccountNotExistsThrowEmptyResultDataAccessException() {
		doThrow(EmptyResultDataAccessException.class).when(accountDao).getAccount("000000000326490001");
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "badpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals("[E004]Incorrect Login Info", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginRetrieveAccountFailed() {
		doThrow(RuntimeException.class).when(accountDao).getAccount(any(String.class));
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "badpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	
	@Test
	void testLoginWrongPassword() {
		Account acct = new Account();
		acct.setAccountno("000000000326490001");
		acct.setName("Michael");
		when(accountDao.getAccount("000000000326490001")).thenReturn(acct);
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(false);
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "badpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals("[E004]Incorrect Login Info", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginVerifyPasswordFailed() {
		Account acct = new Account();
		acct.setAccountno("000000000326490001");
		acct.setName("Michael");
		when(accountDao.getAccount("000000000326490001")).thenReturn(acct);
		doThrow(RuntimeException.class).when(passwordVerify).verify(any(String.class), any(char[].class));
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginSessionKeyGenerationFailed() {
		Account acct = new Account();
		acct.setAccountno("000000000326490001");
		acct.setName("Michael");
		when(accountDao.getAccount("000000000326490001")).thenReturn(acct);
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(true);
		doThrow(RuntimeException.class).when(sessionKeyGen).generateSessionKey();
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginDeleteSessionFailed() {
		Account acct = new Account();
		acct.setAccountno("000000000326490001");
		acct.setName("Michael");
		when(accountDao.getAccount("000000000326490001")).thenReturn(acct);
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(true);
		when(sessionKeyGen.generateSessionKey()).thenReturn("40f2c614-67e6-4a21-b668-31bbde232ec1");
		doThrow(RuntimeException.class).when(dao).deleteByAccountno(any(String.class));
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L);
		doNothing().when(dao).createAccountLoginSession(any(AccountLoginSession.class));
		
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		verify(dao, times(0)).createAccountLoginSession(any(AccountLoginSession.class));
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginCreateLoginSessionFailed() {
		Account acct = new Account();
		acct.setAccountno("000000000326490001");
		acct.setName("Michael");
		when(accountDao.getAccount("000000000326490001")).thenReturn(acct);
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(true);
		when(sessionKeyGen.generateSessionKey()).thenReturn("40f2c614-67e6-4a21-b668-31bbde232ec1");
		when(dao.deleteByAccountno("000000000326490001")).thenReturn(1);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L);
		doThrow(RuntimeException.class).when(dao).createAccountLoginSession(any(AccountLoginSession.class));
		
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		verify(dao, times(1)).deleteByAccountno("000000000326490001");
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginSuccessfully() {
		Account acct = new Account();
		acct.setAccountno("000000000326490001");
		acct.setName("Michael");
		when(accountDao.getAccount("000000000326490001")).thenReturn(acct);
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(true);
		when(sessionKeyGen.generateSessionKey()).thenReturn("40f2c614-67e6-4a21-b668-31bbde232ec1");
		when(dao.deleteByAccountno("000000000326490001")).thenReturn(1);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L);
		ArgumentCaptor<AccountLoginSession> valueCaptureForCreate = ArgumentCaptor.forClass(AccountLoginSession.class);
		doNothing().when(dao).createAccountLoginSession(valueCaptureForCreate.capture());
		
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		
		verify(dao, times(1)).deleteByAccountno("000000000326490001");
		verify(dao, times(1)).createAccountLoginSession(any(AccountLoginSession.class));
		
		long expectedExpiredTimestamp = 1674466598627L + 300 * 1000;
		
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", valueCaptureForCreate.getValue().getSessionkey());
		assertEquals("000000000326490001", valueCaptureForCreate.getValue().getAccountno());
		assertEquals(expectedExpiredTimestamp, valueCaptureForCreate.getValue().getExpirytime().getTime());
		
		assertTrue(result.getValue0());
		assertEquals("[M004]Login successfully", result.getValue1());
		AccountLoginSession resultSession = result.getValue2();
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", resultSession.getSessionkey());
		assertEquals("000000000326490001", resultSession.getAccountno());
		assertEquals(expectedExpiredTimestamp, resultSession.getExpirytime().getTime());
		
	}
	
	@Test
	void testIsSessionValidFalseDueToSessionKeyNotFound() {
		when(dao.getSession(any(String.class))).thenReturn(null);
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		Triplet<Boolean,String,AccountLoginSession> result = impl.isSessionValid(s);
		assertFalse(result.getValue0());
		assertEquals("[M006]Invalid session", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}
	
	@Test
	void testIsSessionValidFalseDueToSessionKeyNotFoundThrowEmptyResultDataAccessException() {
		doThrow(EmptyResultDataAccessException.class).when(dao).getSession(any(String.class));
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		Triplet<Boolean,String,AccountLoginSession> result = impl.isSessionValid(s);
		assertFalse(result.getValue0());
		assertEquals("[M006]Invalid session", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}
	
	@Test
	void testIsSessionValidFalseDueToExpired() {
		AccountLoginSession sessionInDB = new AccountLoginSession();
		sessionInDB.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		sessionInDB.setAccountno("000000000326490001");
		sessionInDB.setExpirytime(new Timestamp(1674466598627L));
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		when(dao.getSession("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(sessionInDB);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L + 1000);
		when(dao.deleteBySessionKey("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(1);
		Triplet<Boolean,String,AccountLoginSession> result = impl.isSessionValid(s);
		assertFalse(result.getValue0());
		assertEquals("[M006]Invalid session", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}
	
	@Test
	void testIsSessionValidFalseExpiredSessionWhenSelectAndWasDeletedByAnotherRequest() {
		AccountLoginSession sessionInDB = new AccountLoginSession();
		sessionInDB.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		sessionInDB.setAccountno("000000000326490001");
		sessionInDB.setExpirytime(new Timestamp(1674466598627L));
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		when(dao.getSession("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(sessionInDB);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L + 1000);
		when(dao.deleteBySessionKey("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(0);
		Triplet<Boolean,String,AccountLoginSession> result = impl.isSessionValid(s);
		assertFalse(result.getValue0());
		assertEquals("[M006]Invalid session", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}
	
	@Test
	void testIsSessionValidTrue() {
		AccountLoginSession sessionInDB = new AccountLoginSession();
		sessionInDB.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		sessionInDB.setAccountno("000000000326490001");
		sessionInDB.setExpirytime(new Timestamp(1674466598627L));
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		when(dao.getSession("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(sessionInDB);
		when(clock.getCurrentTimestamp()).thenReturn(1674466598627L - 10000);
		when(dao.deleteBySessionKey("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(1);
		Triplet<Boolean,String,AccountLoginSession> result = impl.isSessionValid(s);
		verify(dao, times(0)).deleteBySessionKey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		assertTrue(result.getValue0());
		assertEquals("[M007]Valid session", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
		assertEquals("000000000326490001", result.getValue2().getAccountno());
	}
	
	@Test
	void testLogoutByInvalidSessionKey() {
		when(dao.deleteBySessionKey("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(0);
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		Triplet<Boolean,String,AccountLoginSession> result = impl.logout(s);
		assertFalse(result.getValue0());
		assertEquals("[W002]Attempt to logout using invalid session key", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}
	
	@Test
	void testLogoutDeleteSessionFailed() {
		doThrow(RuntimeException.class).when(dao).deleteBySessionKey(any(String.class));
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		Triplet<Boolean,String,AccountLoginSession> result = impl.logout(s);
		assertFalse(result.getValue0());
		assertEquals("[F001]System error", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}
	
	@Test
	void testLogoutSuccessfully() {
		when(dao.deleteBySessionKey("40f2c614-67e6-4a21-b668-31bbde232ec1")).thenReturn(1);
		AccountLoginSession s = new AccountLoginSession();
		s.setSessionkey("40f2c614-67e6-4a21-b668-31bbde232ec1");
		Triplet<Boolean,String,AccountLoginSession> result = impl.logout(s);
		assertTrue(result.getValue0());
		assertEquals("[M005]Logout successfully", result.getValue1());
		assertEquals("40f2c614-67e6-4a21-b668-31bbde232ec1", result.getValue2().getSessionkey());
	}

}
