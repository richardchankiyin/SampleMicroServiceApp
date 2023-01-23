package com.richard.authenticationservice.msg;

import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountSync;

public interface AccountSynchronizer {
	public void synchronize(Account info);
	public void resynchronize(AccountSync item);
}
