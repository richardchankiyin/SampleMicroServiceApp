package com.richard.transactionservice.db;

import com.richard.transactionservice.model.AccountBalance;

public interface AccountBalanceDao {
	public void initAccountBalanceEntry(AccountBalance item);
	public AccountBalance getByAccountNo(String accountno);
	/**
	 * before is the image just before update
	 * if before image cannot be matched during update
	 * the update activity will fail
	 * @param before
	 * @param after
	 */
	public void updateAccountBalance(AccountBalance before, AccountBalance after);
}
