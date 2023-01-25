package com.richard.transactionservice.msg;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.richard.transactionservice.TransactionserviceAppResource;
import com.richard.transactionservice.TransactionserviceAppResourceImpl;
import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountSync;
import com.richard.transactionservice.process.AccountSyncMessagePayloadParser;
import com.richard.transactionservice.process.AccountSynchronizer;

@Component
public class AccountSyncMessageListener {
	private Logger logger = LoggerFactory.getLogger(AccountSyncMessageListener.class);
	private TransactionserviceAppResource resource;
	private AccountSyncMessagePayloadParser accountSyncMessagePayloadParser;
	private AccountSynchronizer accountSynchronizer;
	private static final String DONEBY = "accountsyncmessagelistener";
	private static final String RETURNCODE_PROCESSED = "[E002]";
	
	public AccountSyncMessageListener() {
		this.resource = TransactionserviceAppResourceImpl.getInstance();
		this.accountSyncMessagePayloadParser = resource.getAccountSyncMessagePayloadParser();
		this.accountSynchronizer = resource.getAccountSynchronizer();
	}
	
	
	@RabbitListener(queues = {"accountsync"})
	public void listen(String incoming) {
		logger.debug("incoming message: {}", incoming);
		Pair<Account, AccountSync> parsedResult = null;
		try {
			parsedResult = accountSyncMessagePayloadParser.parse(incoming);
		} catch (Exception e) {
			logger.error("failed to parse message: {}", incoming);
			// do not throw exception as not required to requeue
		}
		
		if (parsedResult == null) {
			logger.error("cannot obtain parsing result for message: {}", incoming);
			// do not throw exception as not required to requeue
		}
		Account account = parsedResult.getValue0();
		AccountSync accountSync = parsedResult.getValue1();
		logger.debug("Account: {} AccountSync: {}", account, accountSync);
		
		try {
			Triplet<Boolean, String, AccountSync> result = accountSynchronizer
					.process(account, accountSync, DONEBY);
			boolean isSuccess = result.getValue0();
			String returnMsg = result.getValue1();
			if (isSuccess) {
				logger.info("{} - {}", returnMsg, accountSync);
			} else {
				
				if (returnMsg.startsWith(RETURNCODE_PROCESSED)) {
					logger.debug("{} - {}", returnMsg, accountSync);
				} else {
					logger.error(returnMsg);
					throw new Exception(returnMsg);
				}
			}
		} catch (Exception e) {
			logger.error("severe issue encountered", e);
			throw new RuntimeException(e);
		}		
	}

	
	
}
