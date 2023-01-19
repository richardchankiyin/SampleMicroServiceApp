package com.richard.authenticationservice.process;


import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.authenticationservice.AuthenticationserviceMessageCode;
import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.model.Account;


public class AccountMaintenanceImpl implements AccountMaintenance{
	
	Logger logger = LoggerFactory.getLogger(AccountMaintenanceImpl.class);
	private AccountSequence accountSequence;
	private AccountDao accountDao;
	public AccountMaintenanceImpl(AccountSequence accountSequence
			, AccountDao accountDao) {
		this.accountSequence = accountSequence;
		this.accountDao = accountDao;
	}
	
	private String assignAccountNoToNewAccount() {
		long seq = this.accountSequence.getNextSequence();
		return StringUtils.leftPad(String.valueOf(seq), 18, '0');
	}
	
	private void checkInfoForCreateAccount(Account info) {
		if (info == null) {
			throw new IllegalArgumentException("account object is null");
		}
		
		String name = info.getName();
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name is blank");
		}
		
		if (name.length() < 3) {
			throw new IllegalArgumentException("name too short");
		}
	}
	
	public Triplet<Boolean,String,Account> createAccount(Account info) {
		// validate arg
		try {
			checkInfoForCreateAccount(info);
		} catch (Exception e) {
			logger.error("checkInfoForCreateAccount", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E002"), null);
		}
		
		info.setAccountno(assignAccountNoToNewAccount());
		
		try {
			accountDao.createAccount(info);
		} catch (Exception e) {
			logger.error("accountDao.createAccount", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("F001"), info);
		}
		
		//TODO calling rabbitmq to sync account data to other services
		
		return Triplet.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("M001"), info);
	}
	
}
