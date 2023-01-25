package com.richard.transactionservice.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public interface JDBCResourceMgr {
	public JdbcTemplate getJdbcTemplate();
	public PlatformTransactionManager getTransactionManager();
	public TransactionDefinition createTransactionDefinition();
}
