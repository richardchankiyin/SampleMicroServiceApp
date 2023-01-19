package com.richard.authenticationservice.db;

import com.richard.authenticationservice.model.Account;
import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class AccountJDBCTemplate implements AccountDao {

	private JdbcTemplate jdbcTemplateObject;
	
	public AccountJDBCTemplate() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("accountdao.xml")) {
			jdbcTemplateObject = new JdbcTemplate((DataSource)context.getBean("dataSource"));
		} 
	}

	@Override
	public void createAccount(Account account) {
		String SQL = "insert into account (accountno, name) values (?,?)";
		jdbcTemplateObject.update(SQL, account.getAccountno(), account.getName());
	}

}
