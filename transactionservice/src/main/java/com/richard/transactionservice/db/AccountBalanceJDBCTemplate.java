package com.richard.transactionservice.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.transactionservice.model.AccountBalance;


public class AccountBalanceJDBCTemplate extends AbstractJDBCTemplate implements AccountBalanceDao {
	public AccountBalanceJDBCTemplate(JDBCResourceMgr jdbcResourceMgr) {
		super(jdbcResourceMgr);
	}

	private Logger logger = LoggerFactory.getLogger(AccountBalanceJDBCTemplate.class);
	
	@Override
	public void initAccountBalanceEntry(AccountBalance item) {
		final String SQL = "insert into accountbalance (accountno, balance) values (?,?)";
		getJdbcTemplate().update(SQL, item.getAccountno(), item.getBalance());
	}

	@Override
	public AccountBalance getByAccountNo(String accountno) {
		final String SQL = "select id, accountno, balance, doneby, uptime from accountbalance where accountno = ?"; 
		
		return getJdbcTemplate().queryForObject(SQL, (rs, rowNum) -> {
			AccountBalance s = new AccountBalance();
			s.setId(rs.getString("id"));
			s.setAccountno(rs.getString("accountno"));
			s.setBalance(rs.getBigDecimal("balance"));
			s.setDoneby(rs.getString("doneby"));
			s.setUptime(rs.getTimestamp("uptime"));
			return s;
		}, new Object[] {accountno});
	}

	@Override
	public void updateAccountBalance(AccountBalance before, AccountBalance after) {
		final String SQL = "update accountbalance set balance = ?, doneby = ?, uptime = ? where accountno = ? and doneby = ? and uptime = ?";
		int updateCount = getJdbcTemplate().update(SQL, after.getBalance(), after.getDoneby(), after.getUptime(), before.getAccountno(), before.getDoneby(), before.getUptime());
		logger.debug("SQL: {} before: {} after: {} updateCount: {}", SQL, before, after, updateCount);
		if (updateCount != 1) {
			throw new RuntimeException("updateCount not equals to 1. Update failed");
		}
	}

}
