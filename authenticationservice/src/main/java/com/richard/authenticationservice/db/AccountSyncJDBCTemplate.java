package com.richard.authenticationservice.db;

import java.util.List;

import com.richard.authenticationservice.model.AccountSync;

public class AccountSyncJDBCTemplate extends AsbtractJDBCTemplate implements AccountSyncDao {

	private static final int FINDFAILACCOUNTSYNCENTRIESLIMIT = 500;
	
	@Override
	public void createAccountSync(AccountSync sync) {
		String SQL = "insert into accountsync (msgkey, accountno, payload, status) values (?,?,?,?)";
		getJdbcTemplate().update(SQL, sync.getMsgkey(), sync.getAccountno()
				, sync.getPayload(), sync.getStatus());
	}

	@Override
	public List<AccountSync> findFailedAccountSyncEntries() {
		String SQL = "select msgkey, accountno, payload, status from accountsync where status = ? order by uptime asc limit " 
				+ FINDFAILACCOUNTSYNCENTRIESLIMIT;
		
		return getJdbcTemplate().query(SQL, (rs, rowNum) -> {
			AccountSync s = new AccountSync();
			s.setMsgkey(rs.getString("msgkey"));
			s.setAccountno(rs.getString("accountno"));
			s.setPayload(rs.getString("payload"));
			s.setStatus(AccountSync.STATUS_SUCCESS.equals(rs.getString("status")));
			return s;
		});
	}

	@Override
	public void updateAccountSyncStatus(AccountSync sync) {
		// TODO Auto-generated method stub

	}

}
