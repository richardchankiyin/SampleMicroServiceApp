package com.richard.authenticationservice.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;


import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountSync;

public class AccountSynchronizerImpl implements AccountSynchronizer {
	private Logger logger = LoggerFactory.getLogger(AccountSynchronizerImpl.class);
	private AmqpTemplate template;
	private MessageKeyGenerator msgKeyGenerator;
	private AccountSyncDao dao;
	private static final String QUEUE = "accountsync";
	
	
	public AccountSynchronizerImpl(MessageKeyGenerator msgKeyGenerator, AmqpTemplate amqp, AccountSyncDao dao) {
		this.template = amqp;
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
		
		logger.debug("prepareAccountSyncFromAccount returning: {}", sync);
		return sync;
	}
	
	
	@Override
	public void synchronize(Account info) {
		AccountSync sync = prepareAccountSyncFromAccount(info);
		try {
			template.convertAndSend(QUEUE, sync.getPayload());
			sync.setStatus(true);
		} catch (Exception e) {
			sync.setStatus(false);
			logger.error("template.convertAndSend", e);
		} finally {
			// it is supposed to be the first time attempt to sync
			// and no records found for this message in accountsync
			// table. Therefore only create will be called
			dao.createAccountSync(sync);
		}
	}

	@Override
	public void resynchronize(AccountSync sync) {
		// TODO Auto-generated method stub
		try {
			template.convertAndSend(QUEUE, sync.getPayload());
			sync.setStatus(true);
		} catch (Exception e) {
			sync.setStatus(false);
			logger.error("template.convertAndSend", e);
		} finally {
			// it is supposed an entry in accountsync table found before
			// therefore there should be simply update based on
			// msgkey
			dao.updateAccountSyncStatus(sync);
		}
	}

}
