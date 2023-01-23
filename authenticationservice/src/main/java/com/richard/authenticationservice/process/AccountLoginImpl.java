package com.richard.authenticationservice.process;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.richard.authenticationservice.AuthenticationserviceMessageCode;
import com.richard.authenticationservice.Clock;
import com.richard.authenticationservice.db.AccountLoginSessionDao;
import com.richard.authenticationservice.model.AccountLoginSession;

public class AccountLoginImpl implements AccountLogin {
	
	private Logger logger = LoggerFactory.getLogger(AccountLoginImpl.class);

	private PasswordVerifier passwordVerify;
	private SessionKeyGenerator sessionKeyGen;
	private Clock clock;
	private AccountLoginSessionDao dao;
	private long sessionValidDurationMillisec;
	public AccountLoginImpl(PasswordVerifier passwordVerify, SessionKeyGenerator sessionKeyGen, Clock clock, long sessionValidDuration, AccountLoginSessionDao dao) {
		this.passwordVerify = passwordVerify;
		this.sessionKeyGen = sessionKeyGen;
		this.clock = clock;
		this.sessionValidDurationMillisec = sessionValidDuration;
		this.dao = dao;
	}
	
	@Override
	@Transactional
	public Triplet<Boolean, String, AccountLoginSession> login(String accountno, char[] password) {
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
		
		return Triplet.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("M004"), session);
	}

	@Override
	public Triplet<Boolean, String, AccountLoginSession> isSessionValid(AccountLoginSession session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Triplet<Boolean, String, AccountLoginSession> logout(AccountLoginSession session) {
		// TODO Auto-generated method stub
		return null;
	}

}
