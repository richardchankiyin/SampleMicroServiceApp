package com.richard.transactionservice.db;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class AbstractJDBCTemplate {
	private JdbcTemplate jdbcTemplateObject;
	private PlatformTransactionManager txMgr;
	
	public AbstractJDBCTemplate() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("datasource.xml")) {
			DataSource ds = (DataSource)context.getBean("dataSource");
			jdbcTemplateObject = new JdbcTemplate(ds);
			txMgr = new JdbcTransactionManager(ds);
		} 
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplateObject;
	}
	
	protected PlatformTransactionManager getTransactionManager() {
		return txMgr;
	}
}
