package com.richard.transactionservice;

import com.richard.transactionservice.api.AuthenticationValidator;
import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountDao;
import com.richard.transactionservice.db.AccountSyncDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.process.AccountBalanceMaintenance;
import com.richard.transactionservice.process.AccountSyncMessagePayloadParser;
import com.richard.transactionservice.process.AccountSynchronizer;

public interface TransactionserviceAppResource {
	public AccountDao getAccountDao();
	public AccountSyncDao getAccountSyncDao();
	public AccountBalanceDao getAccountBalanceDao();
	public JDBCResourceMgr getJDBCResourceMgr();
	public AccountSynchronizer getAccountSynchronizer();
	public AuthenticationValidator getAuthenticationValidator();
	public AccountSyncMessagePayloadParser getAccountSyncMessagePayloadParser();
	public AccountBalanceMaintenance getAccountBalanceMaintenance();

}
