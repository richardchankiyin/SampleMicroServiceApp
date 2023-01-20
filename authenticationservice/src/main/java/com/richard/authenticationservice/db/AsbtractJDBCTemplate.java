package com.richard.authenticationservice.db;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AsbtractJDBCTemplate {
	private JdbcTemplate jdbcTemplateObject;
	
	public AsbtractJDBCTemplate() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("datasource.xml")) {
			jdbcTemplateObject = new JdbcTemplate((DataSource)context.getBean("dataSource"));
		} 
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplateObject;
	}
}
