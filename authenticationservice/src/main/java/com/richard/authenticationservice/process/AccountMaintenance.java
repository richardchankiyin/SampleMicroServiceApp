package com.richard.authenticationservice.process;

import java.util.List;

import org.javatuples.Triplet;

import com.richard.authenticationservice.model.Account;

public interface AccountMaintenance {
	public Triplet<Boolean,String,Account> createAccount(Account info);
	
	public Triplet<Boolean,String,List<Account>> resynchronizeAccount();
}
