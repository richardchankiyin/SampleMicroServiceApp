package com.richard.authenticationservice.process;

import org.javatuples.Pair;

public interface AdminMonitor {
	public Pair<Boolean, String> checkStatus(char[] adminpassword);
}
