package com.richard.transactionservice.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountSyncMessageListener {
	private Logger logger = LoggerFactory.getLogger(AccountSyncMessageListener.class);
	
	@RabbitListener(queues = {"accountsync"})
	public void listen(String incoming) {
		logger.info("incoming message: {}", incoming);
	}
	
}
