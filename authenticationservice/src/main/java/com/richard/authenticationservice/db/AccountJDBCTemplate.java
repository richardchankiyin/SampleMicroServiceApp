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

	@Override
	public Account getAccount(String accountno) {
		final String SQL = "select accountno, name from account where accountno = ?";
		return getJdbcTemplate().queryForObject(SQL, (rs, rowNum) -> {
			Account s = new Account();
			s.setAccountno(rs.getString("accountno"));
			s.setName(rs.getString("name"));
			return s;
		}, new Object[] {accountno});
	}

}
