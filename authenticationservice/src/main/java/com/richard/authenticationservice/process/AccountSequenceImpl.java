package com.richard.authenticationservice.process;

import java.util.concurrent.atomic.AtomicInteger;

public class AccountSequenceImpl implements AccountSequence{

	private AtomicInteger ai;
	private int maxSequence;
	private int sequenceprefix;
	
	private static final int INTERNAL_MAX_SEQ = 10000;
	
	public AccountSequenceImpl(int maxSequence, int sequenceprefix) {
		this.ai = new AtomicInteger(0);
		if (maxSequence >= INTERNAL_MAX_SEQ) {
			throw new IllegalArgumentException("maxSequence too big");
		}
		if (sequenceprefix <= 0) {
			throw new IllegalArgumentException("sequenceprefix too small");
		}
		this.maxSequence = maxSequence;
		this.sequenceprefix = sequenceprefix;
	}
	
	@Override
	public long getNextSequence() {
		int s = ai.addAndGet(1);
		if (s >= maxSequence) {
			ai.set(0);
			s = ai.addAndGet(1);
		}
		return sequenceprefix * INTERNAL_MAX_SEQ + s;
	}
	
}
