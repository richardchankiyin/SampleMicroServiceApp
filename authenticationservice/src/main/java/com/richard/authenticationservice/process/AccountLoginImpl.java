package com.richard.authenticationservice.process;

import java.sql.Timestamp;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.richard.authenticationservice.AuthenticationserviceMessageCode;
import com.richard.authenticationservice.Clock;
import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountLoginSessionDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountLoginSession;

public class AccountLoginImpl implements AccountLogin {
	
	private Logger logger = LoggerFactory.getLogger(AccountLoginImpl.class);

	private PasswordVerifier passwordVerify;
	private SessionKeyGenerator sessionKeyGen;
	private Clock clock;
	private AccountLoginSessionDao dao;
	private AccountDao accountDao;
	private long sessionValidDurationMillisec;
	public AccountLoginImpl(PasswordVerifier passwordVerify, SessionKeyGenerator sessionKeyGen, Clock clock, long sessionValidDuration, AccountDao accountDao, AccountLoginSessionDao dao) {
		this.passwordVerify = passwordVerify;
		this.sessionKeyGen = sessionKeyGen;
		this.clock = clock;
		this.sessionValidDurationMillisec = sessionValidDuration;
		this.accountDao = accountDao;
		this.dao = dao;
	}
	
	@Override
	public Triplet<Boolean, String, AccountLoginSession> login(String accountno, char[] password) {
		Account incomingAccount = null;
		try {
			incomingAccount = accountDao.getAccount(accountno);
		} catch (EmptyResultDataAccessException erde) {
			logger.debug("no account found for: {}", accountno);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E004"), null);
		} catch (Exception e) {
			logger.error("accountDao.getAccount with accountno:" + accountno, e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E003"), null);
		}
		
		if (incomingAccount == null) {
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E004"), null);
		}
		
		logger.debug("Incoming Account attempting login:{}", incomingAccount);
		
		boolean passwordVerified = false;
		try {
			passwordVerified = passwordVerify.verify(accountno, password);
		}
		catch (Exception e) {
			logger.error("passwordVerify.verify with account:" + accountno, e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E003"), null);
		}
		
		if (!passwordVerified) {
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E004"), null);
		}
		
		logger.debug("Incoming Account password verified:{}", incomingAccount);
		
		String sessionKey = null;
		try {
			sessionKey = sessionKeyGen.generateSessionKey();
			logger.debug("session key generated: {}", sessionKey);
		} catch (Exception e) {
			logger.error("sessionKeyGen.generateSessionKey", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E003"), null);
		}
		
		AccountLoginSession session = new AccountLoginSession();
		session.setSessionkey(sessionKey);
		session.setAccountno(accountno);
		session.setExpirytime(new Timestamp(clock.getCurrentTimestamp() + sessionValidDurationMillisec));
		
		try {
			int deleteCount = dao.deleteByAccountno(accountno);
			logger.debug("{} session(s) deleted for account {}", deleteCount, accountno);
			dao.createAccountLoginSession(session);
		} catch (Exception e) {
			logger.error("dao issue", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E003"), null);
		}
		
		logger.debug("Incoming Account {} with Session Created: {}", incomingAccount, session);
		
		return Triplet.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("M004"), session);
	}

	@Override
	public Triplet<Boolean, String, AccountLoginSession> isSessionValid(AccountLoginSession session) {
		try {
			String sessionKey = session.getSessionkey();
			AccountLoginSession s = dao.getSession(sessionKey);
			if (s == null) {
				logger.debug("cannot find session {} stored", sessionKey);
				throw new IllegalArgumentException("cannot find session key: " + sessionKey);
			} else {
				long expiredTimeEpoch = s.getExpirytime().getTime();
				long currentTime = clock.getCurrentTimestamp();
				if (currentTime >= expiredTimeEpoch) {
					logger.debug("session key: {} expired. Expired Time: {} Current Time: {}", sessionKey, expiredTimeEpoch, currentTime);
					try {
						int deleteCount = dao.deleteBySessionKey(sessionKey);
						logger.debug("delete count {} for deleteBySessionKey: {}", deleteCount, sessionKey);
						throw new IllegalStateException("expired session key: " + sessionKey);
					} catch (IllegalStateException ie) {
						throw ie;
					} catch (Exception e2) {
						throw new IllegalStateException("fail to delete by session key: " + sessionKey);
					}
				} else {
					return Triplet.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("M007"), s);
				}
			}
		} catch(EmptyResultDataAccessException erde) {
			logger.debug("no session: {} found", session);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("M006"), session);
		}
		catch (Exception e) {
			logger.error("invalid session/dao error", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("M006"), session);
		}
	}

	@Override
	public Triplet<Boolean, String, AccountLoginSession> logout(AccountLoginSession session) {
		try {
			int deleteCount = dao.deleteBySessionKey(session.getSessionkey());
			if (deleteCount != 1) {
				// deleteCount not equals to 1, that means the sessionkey is not valid
				return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("W002"), session);
			}
		} catch (Exception e) {
			logger.error("dao.deleteBySessionKey", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("F001"), session);
		}
		return Triplet.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("M005"), session);
	}

}
