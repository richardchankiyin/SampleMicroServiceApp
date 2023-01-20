package com.richard.authenticationservice.db;

import java.util.List;

import com.richard.authenticationservice.model.AccountSync;

public class AccountSyncJDBCTemplate extends AsbtractJDBCTemplate implements AccountSyncDao {

	@Override
	public void createAccountSync(AccountSync sync) {
		String SQL = "insert into accountsync (msgkey, accountno, payload, status) values (?,?,?,?)";
		getJdbcTemplate().update(SQL, sync.getMsgkey(), sync.getAccountno()
				, sync.getPayload(), sync.getStatus());
	}

	@Override
	public List<AccountSync> findFailedAccountSyncEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateAccountSyncStatus(AccountSync sync) {
		// TODO Auto-generated method stub

	}

}
