package com.richard.transactionservice.process;

import org.javatuples.Pair;

import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountSync;

public interface AccountSyncMessagePayloadParser {
	public Pair<Account, AccountSync> parse(String payload);
}
