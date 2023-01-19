package com.richard.authenticationservice.process;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountSequenceImplTest {

	private AccountSequenceImpl impl;
	
	@BeforeEach
	void setUp() throws Exception {
		impl = new AccountSequenceImpl(10, 101);
	}

	@Test
	void testGetNextSequence() {
		assertEquals(1010001L, impl.getNextSequence());
		assertEquals(1010002L, impl.getNextSequence());
	}
	
	@Test
	void testGetNextSequenceResetHappened() {
		// max sequence as 10, that means in 10th request this will be reset
		for (int i = 0; i < 9; i++)
			impl.getNextSequence();
		
		assertEquals(1010001L, impl.getNextSequence());
	}

}
