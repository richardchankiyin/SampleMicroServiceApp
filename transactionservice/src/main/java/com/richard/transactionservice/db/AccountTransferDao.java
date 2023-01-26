package com.richard.transactionservice.db;

import com.richard.transactionservice.model.AccountTransfer;

public interface AccountTransferDao {
	public void createAccountTransfer(AccountTransfer item);
}
