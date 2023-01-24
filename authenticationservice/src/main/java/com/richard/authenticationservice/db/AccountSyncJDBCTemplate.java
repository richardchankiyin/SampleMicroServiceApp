package com.richard.authenticationservice.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.authenticationservice.model.AccountSync;

public class AccountSyncJDBCTemplate extends AbstractJDBCTemplate implements AccountSyncDao {
	private Logger logger = LoggerFactory.getLogger(AccountSyncJDBCTemplate.class);
	
	@Override
	public void createAccountSync(AccountSync sync) {
		final String SQL = "insert into accountsync (msgkey, accountno, payload, status) values (?,?,?,?)";
		getJdbcTemplate().update(SQL, sync.getMsgkey(), sync.getAccountno()
				, sync.getPayload(), sync.getStatus());
	}

	@Override
	public List<AccountSync> findFailedAccountSyncEntries() {
		final String SQL = "select msgkey, accountno, payload, status from accountsync where status = ? order by uptime asc"; 
		
		return getJdbcTemplate().query(SQL, (rs, rowNum) -> {
			AccountSync s = new AccountSync();
			s.setMsgkey(rs.getString("msgkey"));
			s.setAccountno(rs.getString("accountno"));
			s.setPayload(rs.getString("payload"));
			s.setStatus(AccountSync.STATUS_SUCCESS.equals(rs.getString("status")));
			return s;
		}, new Object[] {AccountSync.STATUS_FAILED});
	}

	@Override
	public void updateAccountSyncStatus(AccountSync sync) {
		final String SQL = "update accountsync set status = ? where msgkey = ?";
		String status = sync.getStatus();
		String msgkey = sync.getMsgkey();
		
		int updateCount = getJdbcTemplate().update(SQL, status, msgkey);
		
		logger.debug("SQL:[{}, {}, {}] with updateCount: {}", SQL, status, msgkey, updateCount);
	}

}
