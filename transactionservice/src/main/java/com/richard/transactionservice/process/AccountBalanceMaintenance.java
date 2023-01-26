package com.richard.transactionservice.process;

import org.javatuples.Triplet;

import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountTransfer;

public interface AccountBalanceMaintenance {
	public Triplet<Boolean, String, AccountBalance> enquireBalance(String accountno);
	public Triplet<Boolean, String, AccountBalance> transfer(AccountTransfer transfer);
}
