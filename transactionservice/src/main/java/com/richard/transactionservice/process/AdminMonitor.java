package com.richard.transactionservice.process;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.richard.transactionservice.model.AccountSync;

public interface AdminMonitor {
	public Pair<Boolean, String> checkStatus(char[] adminpassword);
	public Triplet<Boolean, String, AccountSync> addDuplicateAccountSync(AccountSync item);
	public Triplet<Boolean, String, AccountSync> retrieveDuplicateAccountSync(char[] adminpassword, String msgKey);
}
