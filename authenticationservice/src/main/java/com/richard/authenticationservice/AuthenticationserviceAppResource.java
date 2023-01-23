package com.richard.authenticationservice;

import org.springframework.amqp.core.AmqpTemplate;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.msg.AccountSynchronizer;
import com.richard.authenticationservice.msg.MessageKeyGenerator;
import com.richard.authenticationservice.process.AccountMaintenance;
import com.richard.authenticationservice.process.AccountSequence;

public interface AuthenticationserviceAppResource {
	public MessageKeyGenerator getMessageKeyGenerator();
	public AccountSequence getAccountSequence();
	public AccountDao getAccountDao();
	public AccountSyncDao getAccountSyncDao();
	public AccountSynchronizer getAccountSynchronizer();
	public AmqpTemplate getAmqpTemplate();
	public AccountMaintenance getAccountMaintenance();
}
