package com.richard.authenticationservice.process;

import org.javatuples.Pair;

import com.richard.authenticationservice.AuthenticationserviceMessageCode;

public class AdminMonitorImpl implements AdminMonitor {

	private static final String ADMINPASSWD = "iamadmin";
	
	@Override
	public Pair<Boolean, String> checkStatus(char[] adminpassword) {
		if (ADMINPASSWD.equals(new String(adminpassword))) {
			return Pair.with(true, AuthenticationserviceMessageCode.getInstance().getMessage("A001"));
		} else {
			return Pair.with(false, AuthenticationserviceMessageCode.getInstance().getMessage("E004"));
		}
	}

}
