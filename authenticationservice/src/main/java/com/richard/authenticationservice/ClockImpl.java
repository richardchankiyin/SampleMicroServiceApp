package com.richard.authenticationservice;

public class ClockImpl implements Clock {

	@Override
	public long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

}
