package com.richard.authenticationservice.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.richard.authenticationservice.Clock;
import com.richard.authenticationservice.db.AccountLoginSessionDao;
import com.richard.authenticationservice.model.AccountLoginSession;


class AccountLoginImplTest {

	private AccountLoginImpl impl;
	private PasswordVerifier passwordVerify;
	private SessionKeyGenerator sessionKeyGen;
	private Clock clock;
	private AccountLoginSessionDao dao;
	private static final String UNABLE_TO_LOGIN_MSG = "[E003]Unable to login";
	@BeforeEach
	void setup() {
		passwordVerify = mock(PasswordVerifier.class);
		sessionKeyGen = mock(SessionKeyGenerator.class);
		clock = mock(Clock.class);
		dao = mock(AccountLoginSessionDao.class);
		impl = new AccountLoginImpl(passwordVerify, sessionKeyGen, clock, 300 * 1000, dao);
	}
	
	
	@Test
	void testLoginWrongPassword() {
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(false);
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "badpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals("[E004]Incorrect Login Info", result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginVerifyPasswordFailed() {
		doThrow(RuntimeException.class).when(passwordVerify).verify(any(String.class), any(char[].class));
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginSessionKeyGenerationFailed() {
		when(passwordVerify.verify(any(String.class), any(char[].class))).thenReturn(true);
		doThrow(RuntimeException.class).when(sessionKeyGen).generateSessionKey();
		Triplet<Boolean,String,AccountLoginSession> result = impl.login("000000000326490001", "goodpassword".toCharArray());
		assertFalse(result.getValue0());
		assertEquals(UNABLE_TO_LOGIN_MSG, result.getValue1());
		assertNull(result.getValue2());
	}
	
	@Test
	void testLoginDeleteSessionFailed() {
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

}
