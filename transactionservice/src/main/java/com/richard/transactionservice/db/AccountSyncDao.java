package com.richard.transactionservice.db;

import com.richard.transactionservice.model.AccountSync;

public interface AccountSyncDao {
	public AccountSync getByMessageKey(String messageKey);
	public int createAccountSync(AccountSync item);
}
