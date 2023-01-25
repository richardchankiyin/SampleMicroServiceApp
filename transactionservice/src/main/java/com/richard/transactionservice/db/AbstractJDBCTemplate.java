package com.richard.transactionservice.db;

import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractJDBCTemplate {
	
	private JDBCResourceMgr jdbcResourceMgr;
	public AbstractJDBCTemplate(JDBCResourceMgr jdbcResourceMgr) {
		this.jdbcResourceMgr = jdbcResourceMgr;
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcResourceMgr.getJdbcTemplate();
	}
}
