package com.richard.authenticationservice.process;


import org.javatuples.Triplet;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.model.Account;

public class AccountMaintenanceImpl implements AccountMaintenance{
	
	private static final String ACCOUNT_FORMAT = "%18d";
	private AccountSequence accountSequence;
	private AccountDao accountDao;
	public AccountMaintenanceImpl(AccountSequence accountSequence
			, AccountDao accountDao) {
		this.accountSequence = accountSequence;
		this.accountDao = accountDao;
	}
	
	private String assignAccountNoToNewAccount() {
		long seq = this.accountSequence.getNextSequence();
		return String.format(ACCOUNT_FORMAT, seq);
	}
	
	public Triplet<Boolean,String,Account> createAccount(Account info) {
		//TODO to be implemented
		return null;
	}
	
}
