package com.richard.transactionservice.db;

import com.richard.transactionservice.model.Account;

public interface AccountDao {
	public void createAccount(Account account);
	
	public Account getAccount(String accountno);
}
