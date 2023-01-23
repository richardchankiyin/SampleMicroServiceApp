package com.richard.authenticationservice;

import java.util.List;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.process.AccountMaintenance;

@Component
public class AuthenticationserviceScheduledTasks {
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationserviceScheduledTasks.class);
	
	private AccountMaintenance accountMaintenance;
	private AuthenticationserviceAppResource resource;
	
	public AuthenticationserviceScheduledTasks() {
		this.resource = AuthenticationserviceAppResourceImpl.getInstance();
		this.accountMaintenance = this.resource.getAccountMaintenance();
	}
	
	@Scheduled(fixedRate = 180000)
	public void heartBeat() {
		logger.info("This is a heart beat activity");
	}
	
	@Scheduled(fixedRate = 30000)
	public void resynchronizeAccounts() {
		Triplet<Boolean, String, List<Account>> result = accountMaintenance.resynchronizeAccount();
		if (result.getValue0()) {
			logger.info("resynchronizeAccounts result -- {}", result.getValue1());
		} else {
			logger.warn("resynchronizeAccounts with issues -- {}", result.getValue1() + "Accounts: " + result.getValue2());
		}
	}
}
