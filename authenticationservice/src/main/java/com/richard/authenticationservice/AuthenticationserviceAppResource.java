package com.richard.authenticationservice;

import org.springframework.amqp.core.AmqpTemplate;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountLoginSessionDao;
import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.msg.AccountSynchronizer;
import com.richard.authenticationservice.msg.MessageKeyGenerator;
import com.richard.authenticationservice.process.AccountLogin;
import com.richard.authenticationservice.process.AccountMaintenance;
import com.richard.authenticationservice.process.AccountSequence;
import com.richard.authenticationservice.process.PasswordVerifier;
import com.richard.authenticationservice.process.SessionKeyGenerator;

public interface AuthenticationserviceAppResource {
	public MessageKeyGenerator getMessageKeyGenerator();
	public PasswordVerifier getPasswordVerifier();
	public SessionKeyGenerator getSessionKeyGenerator();
	public Clock getClock();
	public AccountSequence getAccountSequence();
	public AccountDao getAccountDao();
	public AccountSyncDao getAccountSyncDao();
	public AccountLoginSessionDao getAccountLoginSessionDao();
	public AccountSynchronizer getAccountSynchronizer();
	public AmqpTemplate getAmqpTemplate();
	public AccountMaintenance getAccountMaintenance();
	public AccountLogin getAccountLogin();
}
