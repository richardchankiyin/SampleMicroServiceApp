package com.richard.authenticationservice;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountJDBCTemplate;
import com.richard.authenticationservice.db.AccountLoginSessionDao;
import com.richard.authenticationservice.db.AccountLoginSessionJDBCTemplate;
import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.db.AccountSyncJDBCTemplate;
import com.richard.authenticationservice.msg.AccountSynchronizer;
import com.richard.authenticationservice.msg.AccountSynchronizerImpl;
import com.richard.authenticationservice.msg.MessageKeyGenerator;
import com.richard.authenticationservice.msg.MessageKeyGeneratorImpl;
import com.richard.authenticationservice.msg.MessagingConnectionFactoryImpl;
import com.richard.authenticationservice.process.AccountLogin;
import com.richard.authenticationservice.process.AccountLoginImpl;
import com.richard.authenticationservice.process.AccountMaintenance;
import com.richard.authenticationservice.process.AccountMaintenanceImpl;
import com.richard.authenticationservice.process.AccountSequence;
import com.richard.authenticationservice.process.AccountSequenceImpl;
import com.richard.authenticationservice.process.PasswordVerifier;
import com.richard.authenticationservice.process.PasswordVerifierImpl;
import com.richard.authenticationservice.process.SessionKeyGenerator;
import com.richard.authenticationservice.process.SessionKeyGeneratorImpl;

public class AuthenticationserviceAppResourceImpl implements AuthenticationserviceAppResource {
	private Logger logger = LoggerFactory.getLogger(AuthenticationserviceAppResourceImpl.class);
	
	private AccountMaintenance accountMaintenance;
	private AccountLogin accountLogin;
	private AccountSequence accountSequence;
	private AccountDao accountDao;
	private AccountSynchronizer accountSync;
	private MessageKeyGenerator msgKeyGenerator;
	private PasswordVerifier passwordVerify;
	private SessionKeyGenerator sessionKeyGen;
	private Clock clock;
	private AmqpTemplate amqp;
	private AccountSyncDao accountSyncDao;
	private AccountLoginSessionDao accountLoginSessionDao;
	
	private static AuthenticationserviceAppResourceImpl instance = new AuthenticationserviceAppResourceImpl();

	public static AuthenticationserviceAppResourceImpl getInstance() { return instance; }
	
	private AccountSequence createAccountSequence() {
		int maxSequence = 9999; //that is the limit of current sequence impl can support
		LocalDateTime now = LocalDateTime.now();
		// below day1 value should not be changed as accountno format
		// will rely on below minute diff result
		LocalDateTime day1 = LocalDateTime.of(2023, 1, 1, 0, 0);
		long daydiff = ChronoUnit.MINUTES.between(day1, now);
		return new AccountSequenceImpl(maxSequence, daydiff);
	}
	
	private long getValidSessionDurationMilliSeconds() {
		long builtindefault = 600000L;
		long result = builtindefault;
		String configStr = System.getProperty("session.valid.duration.millisecond");
		try {
			result = Long.parseLong(configStr);
		} catch (Exception e) {
			logger.error("Long.parseLong", e);
			logger.warn("failed to obtain ValidSessionDurationMilliSeconds. Using built-in default: {}", builtindefault);
		}
		logger.info("valid session duration milliseconds: {}", result);
		return result;
	}
	
	public AuthenticationserviceAppResourceImpl() {
		this.msgKeyGenerator = new MessageKeyGeneratorImpl();
		this.accountSequence = createAccountSequence();
		this.accountDao = new AccountJDBCTemplate();
		this.accountSyncDao = new AccountSyncJDBCTemplate();
		this.amqp = new RabbitTemplate(MessagingConnectionFactoryImpl.getInstance()
				.getConnectionFactory());
		this.accountSync = new AccountSynchronizerImpl(msgKeyGenerator,amqp,accountSyncDao);
		this.accountMaintenance = new AccountMaintenanceImpl(accountSequence, accountDao
				, accountSyncDao, accountSync);
		
		this.passwordVerify = new PasswordVerifierImpl();
		this.sessionKeyGen = new SessionKeyGeneratorImpl();
		this.clock = new ClockImpl();
		this.accountLoginSessionDao = new AccountLoginSessionJDBCTemplate();
		this.accountLogin = new AccountLoginImpl(passwordVerify, sessionKeyGen
				, clock, getValidSessionDurationMilliSeconds(), accountLoginSessionDao);
	}

	@Override
	public MessageKeyGenerator getMessageKeyGenerator() {
		return this.msgKeyGenerator;
	}

	@Override
	public AccountSequence getAccountSequence() {
		return this.accountSequence;
	}

	@Override
	public AccountDao getAccountDao() {
		return this.accountDao;
	}

	@Override
	public AccountSyncDao getAccountSyncDao() {
		return this.accountSyncDao;
	}

	@Override
	public AccountSynchronizer getAccountSynchronizer() {
		return this.accountSync;
	}

	@Override
	public AmqpTemplate getAmqpTemplate() {
		return this.amqp;
	}

	@Override
	public AccountMaintenance getAccountMaintenance() {
		return this.accountMaintenance;
	}

	@Override
	public PasswordVerifier getPasswordVerifier() {
		return this.passwordVerify;
	}

	@Override
	public SessionKeyGenerator getSessionKeyGenerator() {
		return this.sessionKeyGen;
	}

	@Override
	public Clock getClock() {
		return this.clock;
	}

	@Override
	public AccountLoginSessionDao getAccountLoginSessionDao() {
		return this.accountLoginSessionDao;
	}

	@Override
	public AccountLogin getAccountLogin() {
		return this.accountLogin;
	}

}
