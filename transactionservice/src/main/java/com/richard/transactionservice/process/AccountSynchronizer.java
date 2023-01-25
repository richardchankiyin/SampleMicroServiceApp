package com.richard.transactionservice.process;

import org.javatuples.Triplet;

import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountSync;

public interface AccountSynchronizer {
	public Triplet<Boolean, String, AccountSync> process(Account accItem, AccountSync syncItem, String doneBy);
}
