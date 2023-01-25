package com.richard.transactionservice.process;

import com.richard.transactionservice.model.AccountSync;

public interface AccountSynchronizer {
	public void processAccountSync(AccountSync item);
}
