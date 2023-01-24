package com.richard.authenticationservice.db;

import com.richard.authenticationservice.model.Account;

/**
 * AccountDao Implementation. Schema refers to
 * create_table_account.sql
 * 
 * @author richard
 *
 */
public class AccountJDBCTemplate extends AbstractJDBCTemplate implements AccountDao {

	@Override
	public void createAccount(Account account) {
		final String SQL = "insert into account (accountno, name) values (?,?)";
		getJdbcTemplate().update(SQL, account.getAccountno(), account.getName());
	}

}
