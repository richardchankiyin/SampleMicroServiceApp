package com.richard.transactionservice.db;

import com.richard.transactionservice.model.AccountTransfer;

public class AccountTransferJDBCTemplate extends AbstractJDBCTemplate implements AccountTransferDao {

	public AccountTransferJDBCTemplate(JDBCResourceMgr jdbcResourceMgr) {
		super(jdbcResourceMgr);
	}

	@Override
	public void createAccountTransfer(AccountTransfer item) {
		final String SQL = "insert into accounttransfer (accountno, amount, doneby) values (?,?,?)";
		getJdbcTemplate().update(SQL, item.getAccountno(), item.getAmount(), item.getDoneby());
	}

}
