package com.richard.authenticationservice.process;

public class PasswordVerifierImpl implements PasswordVerifier {


	private static final String COMMON_STATIC_PASSWORD = "notsecurepassword";

	/**
	 * This method is simply checking whether incoming password
	 * is equals to COMMON_STATIC_PASSWORD. This should not be 
	 * used in any enterprise solutions
	 */
	@Override
	public boolean verify(String accountno, char[] password) {
		String strPassword = new String(password);
		
		return COMMON_STATIC_PASSWORD.equals(strPassword);
	}

}
