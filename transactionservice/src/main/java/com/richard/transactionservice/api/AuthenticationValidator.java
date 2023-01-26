package com.richard.transactionservice.api;

import org.javatuples.Triplet;

public interface AuthenticationValidator {
	public Triplet<Boolean, String, String> authenticate(String sessionkey);
}
