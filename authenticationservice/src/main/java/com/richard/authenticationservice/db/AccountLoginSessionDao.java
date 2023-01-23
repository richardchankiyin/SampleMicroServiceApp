package com.richard.authenticationservice.db;

import com.richard.authenticationservice.model.AccountLoginSession;

public interface AccountLoginSessionDao {
	public int deleteByAccountno(String accountno);
	public int deleteBySessionKey(String sessionkey);
	public void createAccountLoginSession(AccountLoginSession session);
	public AccountLoginSession getSession(String sessionkey);
}
