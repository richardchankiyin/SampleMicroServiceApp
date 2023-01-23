package com.richard.authenticationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationserviceScheduledTasks {
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationserviceScheduledTasks.class);
	
	public AuthenticationserviceScheduledTasks() {
		
	}
	
	@Scheduled(fixedRate = 180000)
	public void heartBeat() {
		logger.info("This is a heart beat activity");
	}
}
