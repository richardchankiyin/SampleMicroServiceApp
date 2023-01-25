package com.richard.transactionservice.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

public interface JDBCResourceMgr {
	public JdbcTemplate getJdbcTemplate();
	public PlatformTransactionManager getTransactionManager();
}
