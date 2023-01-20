package com.richard.authenticationservice.msg;

import com.richard.authenticationservice.model.Account;

public interface AccountSynchronizer {
	public void synchronize(Account info);
}
