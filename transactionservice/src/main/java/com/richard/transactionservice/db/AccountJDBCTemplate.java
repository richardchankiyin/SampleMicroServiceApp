package com.richard.transactionservice.db;

import com.richard.transactionservice.model.Account;

public class AccountJDBCTemplate extends AbstractJDBCTemplate implements AccountDao {

	public AccountJDBCTemplate(JDBCResourceMgr jdbcResourceMgr) {
		super(jdbcResourceMgr);
	}

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
