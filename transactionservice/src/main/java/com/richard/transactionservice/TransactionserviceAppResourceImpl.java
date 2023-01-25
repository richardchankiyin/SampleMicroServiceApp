package com.richard.transactionservice;

import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountBalanceJDBCTemplate;
import com.richard.transactionservice.db.AccountDao;
import com.richard.transactionservice.db.AccountJDBCTemplate;
import com.richard.transactionservice.db.AccountSyncDao;
import com.richard.transactionservice.db.AccountSyncJDBCTemplate;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.db.JDBCResourceMgrImpl;
import com.richard.transactionservice.process.AccountSyncMessagePayloadParser;
import com.richard.transactionservice.process.AccountSyncMessagePayloadParserImpl;
import com.richard.transactionservice.process.AccountSynchronizer;
import com.richard.transactionservice.process.AccountSynchronizerImpl;

public class TransactionserviceAppResourceImpl implements TransactionserviceAppResource {
	private JDBCResourceMgr jdbcResourceMgr;
	private AccountDao accountDao;
	private AccountSyncDao accountSyncDao;
	private AccountBalanceDao accountBalanceDao;
	private AccountSynchronizer accountSynchronizer;
	private AccountSyncMessagePayloadParser accountSyncMessagePayloadParser;
	
	private static TransactionserviceAppResourceImpl instance;
	static {
		instance = new TransactionserviceAppResourceImpl();
	}
	public static TransactionserviceAppResourceImpl getInstance() { return instance; }
	
	public TransactionserviceAppResourceImpl() {
		this.jdbcResourceMgr = JDBCResourceMgrImpl.getInstance();
		this.accountDao = new AccountJDBCTemplate(jdbcResourceMgr);
		this.accountSyncDao = new AccountSyncJDBCTemplate(jdbcResourceMgr);
		this.accountBalanceDao = new AccountBalanceJDBCTemplate(jdbcResourceMgr);
		this.accountSynchronizer = new AccountSynchronizerImpl(jdbcResourceMgr
				, accountDao, accountSyncDao, accountBalanceDao);
		this.accountSyncMessagePayloadParser = new AccountSyncMessagePayloadParserImpl();
	}

	@Override
	public AccountDao getAccountDao() {
		return accountDao;
	}

	@Override
	public AccountSyncDao getAccountSyncDao() {
		return accountSyncDao;
	}

	@Override
	public AccountBalanceDao getAccountBalanceDao() {
		return accountBalanceDao;
	}

	@Override
	public JDBCResourceMgr getJDBCResourceMgr() {
		return jdbcResourceMgr;
	}

	@Override
	public AccountSynchronizer getAccountSynchronizer() {
		return accountSynchronizer;
	}

	@Override
	public AccountSyncMessagePayloadParser getAccountSyncMessagePayloadParser() {
		return accountSyncMessagePayloadParser;
	}

}
