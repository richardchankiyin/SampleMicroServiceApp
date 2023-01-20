package com.richard.authenticationservice.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountSync;

public class AccountSynchronizerImpl implements AccountSynchronizer {
	Logger logger = LoggerFactory.getLogger(AccountSynchronizerImpl.class);
	private MessagingConnectionFactory factory;
	private RabbitTemplate template;
	private MessageKeyGenerator msgKeyGenerator;
	private AccountSyncDao dao;
	
	
	public AccountSynchronizerImpl(MessageKeyGenerator msgKeyGenerator, AccountSyncDao dao) {
		this.factory = MessagingConnectionFactoryImpl.getInstance();
		this.template = new RabbitTemplate(factory.getConnectionFactory());
		this.msgKeyGenerator = msgKeyGenerator;
		this.dao = dao;
	}
	
	private String preparePayload(String msgKey, Account info) {
		return String.format("{\"msgKey\":\"%s\",\"account\":{\"accountNo\":\"%s\",\"name\":\"%s\"}}", msgKey
				, info.getAccountno(), info.getName());
	}
	
	
	private AccountSync prepareAccountSyncFromAccount(Account info) {
		String msgKey = msgKeyGenerator.generateUniqueKey();
		String accountNo = info.getAccountno();
		String payload = preparePayload(msgKey, info);
		// temporary set status false
		boolean isSuccess = false;
		
		AccountSync sync = new AccountSync();
		sync.setMsgkey(msgKey);
		sync.setAccountno(accountNo);
		sync.setPayload(payload);
		sync.setStatus(isSuccess);
		
		return sync;
	}
	
	
	@Override
	public void synchronize(Account info) {
		// TODO to be updated with serializing account with unique message key
		//. Also calling dao to update the messaging sending result
		AccountSync sync = prepareAccountSyncFromAccount(info);
		try {
			template.convertAndSend("accountsync", sync.getPayload());
			sync.setStatus(true);
		} catch (Exception e) {
			sync.setStatus(false);
			logger.error("template.convertAndSend", e);
		} finally {
			dao.createAccountSync(sync);
		}
	}

}
