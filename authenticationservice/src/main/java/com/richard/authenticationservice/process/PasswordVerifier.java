package com.richard.authenticationservice.process;

public interface PasswordVerifier {
	public boolean verify(String accountno, char[] password);
}
