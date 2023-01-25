package com.richard.transactionservice.db;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class JDBCResourceMgrImpl implements JDBCResourceMgr{
	private JdbcTemplate jdbcTemplateObject;
	private PlatformTransactionManager txMgr;
	
	private static JDBCResourceMgrImpl instance;
	static {
		instance = new JDBCResourceMgrImpl();
	}
	
	public static JDBCResourceMgrImpl getInstance() { return instance; }
	
	public JDBCResourceMgrImpl() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("datasource.xml")) {
			DataSource ds = (DataSource)context.getBean("dataSource");
			jdbcTemplateObject = new JdbcTemplate(ds);
			txMgr = new JdbcTransactionManager(ds);
		} 
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplateObject;
	}
	
	public PlatformTransactionManager getTransactionManager() {
		return txMgr;
	}

	@Override
	public TransactionDefinition createTransactionDefinition() {
		return new DefaultTransactionDefinition();
	}
}
