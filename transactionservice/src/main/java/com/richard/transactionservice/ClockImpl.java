package com.richard.transactionservice;

public class ClockImpl implements Clock {

	@Override
	public long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

}
