package com.richard.transactionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionserviceScheduledTasks {	
	private static final Logger logger = LoggerFactory.getLogger(TransactionserviceScheduledTasks.class);
	
	@Scheduled(fixedRate = 180000)
	public void heartBeat() {
		logger.info("This is a heart beat activity");
	}
}
