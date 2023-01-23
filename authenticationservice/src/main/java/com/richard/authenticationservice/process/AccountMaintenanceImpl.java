package com.richard.authenticationservice.process;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.authenticationservice.AuthenticationserviceMessageCode;
import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountSync;
import com.richard.authenticationservice.msg.AccountSynchronizer;


public class AccountMaintenanceImpl implements AccountMaintenance{
	
	private Logger logger = LoggerFactory.getLogger(AccountMaintenanceImpl.class);
	private AccountSequence accountSequence;
	private AccountDao accountDao;
	private AccountSyncDao accountSyncDao;
	private AccountSynchronizer accountSync;
	public AccountMaintenanceImpl(AccountSequence accountSequence
			, AccountDao accountDao, AccountSyncDao accountSyncDao, AccountSynchronizer accountSync) {
		this.accountSequence = accountSequence;
		this.accountDao = accountDao;
		this.accountSyncDao = accountSyncDao;
		this.accountSync = accountSync;
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
		
		//calling rabbitmq to sync account data to other services
		try {
			accountSync.synchronize(info);
		} catch (Exception e) {
			logger.error("accountSync.synchronize", e);
		}
		
		return Triplet.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("M001"), info);
	}

	@Override
	public Triplet<Boolean, String, List<Account>> resynchronizeAccount() {
		List<AccountSync> acctSyncList = null;
		try {
			acctSyncList = accountSyncDao.findFailedAccountSyncEntries();
		} catch (Exception e) {
			logger.error("accountSyncDao.findFailedAccountSyncEntries", e);
			return Triplet.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("F001"), null);
		}

		if (acctSyncList != null && !acctSyncList.isEmpty()) {
			boolean result = true;
			List<Account> failedAccounts = new ArrayList<>();
			for (AccountSync item: acctSyncList) {
				boolean thisResult = accountSync.resynchronize(item);
				result &= thisResult;
				if (!thisResult) {
					Account acct = new Account();
					acct.setAccountno(item.getAccountno());
					failedAccounts.add(acct);
				}
			}
			if (result) {
				return Triplet.with(true, AuthenticationserviceMessageCode
						.getInstance().getMessage("M002"), null);
			} else {
				return Triplet.with(false, AuthenticationserviceMessageCode
						.getInstance().getMessage("W001"), failedAccounts);
			}
		} else {
			return Triplet.with(true, AuthenticationserviceMessageCode
					.getInstance().getMessage("M003"), null);
		}
		
	}
	
}
