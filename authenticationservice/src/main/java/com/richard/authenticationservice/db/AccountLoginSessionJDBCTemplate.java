package com.richard.authenticationservice.db;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.authenticationservice.model.AccountLoginSession;

public class AccountLoginSessionJDBCTemplate extends AbstractJDBCTemplate implements AccountLoginSessionDao {
	private Logger logger = LoggerFactory.getLogger(AccountLoginSessionJDBCTemplate.class);
	
	@Override
	public int deleteByAccountno(String accountno) {
		final String SQL = "delete from accountloginsession where accountno = ?";
		int deleteCount = getJdbcTemplate().update(SQL, accountno);
		logger.debug("SQL:[{}, {}] with deleteCount: {}", SQL, accountno, deleteCount);
		return deleteCount;
	}

	@Override
	public int deleteBySessionKey(String sessionkey) {
		final String SQL = "delete from accountloginsession where sessionkey = ?";
		int deleteCount = getJdbcTemplate().update(SQL, sessionkey);
		logger.debug("SQL:[{}, {}] with deleteCount: {}", SQL, sessionkey, deleteCount);
		return deleteCount;
	}

	@Override
	public void createAccountLoginSession(AccountLoginSession session) {
		final String SQL = "insert into accountloginsession (sessionkey, accountno, expirytime) values (?, ?, ?)";
		String sessionkey = session.getSessionkey();
		//TODO explicitly make this null to fail
		//String accountno = session.getAccountno();
		String accountno = null;
		
		Timestamp expirytime = session.getExpirytime();
		getJdbcTemplate().update(SQL, sessionkey, accountno, expirytime);
	}

	@Override
	public AccountLoginSession getSession(String sessionkey) {
		final String SQL = "select sessionkey, accountno, expirytime from accountloginsession where sessionkey = ?";
		return getJdbcTemplate().queryForObject(SQL, (rs, rowNum) -> {
			AccountLoginSession s = new AccountLoginSession();
			s.setSessionkey(rs.getString("sessionkey"));
			s.setAccountno(rs.getString("accountno"));
			s.setExpirytime(rs.getTimestamp("expirytime"));
			return s;
		}, new Object[] {sessionkey});
	}

}
