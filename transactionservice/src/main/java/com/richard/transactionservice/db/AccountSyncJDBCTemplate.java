package com.richard.transactionservice.db;

import com.richard.transactionservice.model.AccountSync;

public class AccountSyncJDBCTemplate extends AbstractJDBCTemplate implements AccountSyncDao {

	public AccountSyncJDBCTemplate(JDBCResourceMgr jdbcResourceMgr) {
		super(jdbcResourceMgr);
	}

	@Override
	public AccountSync getByMessageKey(String messageKey) {
		final String SQL = "select msgkey, accountno, payload, status from accountsync where msgkey = ?"; 
		
		return getJdbcTemplate().queryForObject(SQL, (rs, rowNum) -> {
			AccountSync s = new AccountSync();
			s.setMsgkey(rs.getString("msgkey"));
			s.setAccountno(rs.getString("accountno"));
			s.setPayload(rs.getString("payload"));
			s.setStatus(AccountSync.STATUS_SUCCESS.equals(rs.getString("status")));
			return s;
		}, new Object[] {messageKey});
	}

	@Override
	public int createAccountSync(AccountSync sync) {
		final String SQL = "insert into accountsync (msgkey, accountno, payload, status) values (?,?,?,?)";
		return getJdbcTemplate().update(SQL, sync.getMsgkey(), sync.getAccountno()
				, sync.getPayload(), sync.getStatus());
	}

}
