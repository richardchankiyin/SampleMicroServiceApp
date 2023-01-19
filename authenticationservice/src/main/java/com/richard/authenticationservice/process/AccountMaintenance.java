package com.richard.authenticationservice.process;

import org.javatuples.Triplet;

import com.richard.authenticationservice.model.Account;

public interface AccountMaintenance {
	public Triplet<Boolean,String,Account> createAccount(Account info);
}
