package com.richard.authenticationservice.db;

import java.util.List;

import com.richard.authenticationservice.model.AccountSync;

public interface AccountSyncDao {
	public void createAccountSync(AccountSync sync);
	public List<AccountSync> findFailedAccountSyncEntries();
	public void updateAccountSyncStatus(AccountSync sync);
}
