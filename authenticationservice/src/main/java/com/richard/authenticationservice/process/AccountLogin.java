package com.richard.authenticationservice.process;

import org.javatuples.Triplet;

import com.richard.authenticationservice.model.AccountLoginSession;

public interface AccountLogin {
	public Triplet<Boolean,String,AccountLoginSession> login(String accountno, char[] password);
	
	public Triplet<Boolean,String,AccountLoginSession> isSessionValid(AccountLoginSession session);
	
	public Triplet<Boolean,String,AccountLoginSession> logout(AccountLoginSession session);
}
