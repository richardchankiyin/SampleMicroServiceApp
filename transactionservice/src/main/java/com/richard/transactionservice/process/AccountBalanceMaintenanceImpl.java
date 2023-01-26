package com.richard.transactionservice.process;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import com.richard.transactionservice.TransactionserviceMessageCode;
import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountTransfer;

public class AccountBalanceMaintenanceImpl implements AccountBalanceMaintenance {
	private static final Logger logger = LoggerFactory.getLogger(AccountBalanceMaintenanceImpl.class);
	
	private JDBCResourceMgr jdbcresourcemgr;
	private AccountBalanceDao accountBalanceDao;
	
	public AccountBalanceMaintenanceImpl(JDBCResourceMgr jdbcresourcemgr, AccountBalanceDao accountBalanceDao) {
		this.jdbcresourcemgr = jdbcresourcemgr;
		this.accountBalanceDao = accountBalanceDao;
	}
	
	@Override
	public Triplet<Boolean, String, AccountBalance> enquireBalance(String accountno) {
		try {
			AccountBalance accountBalance = accountBalanceDao.getByAccountNo(accountno);
			return Triplet.with(true, TransactionserviceMessageCode.getInstance().getMessage("M004")
					, accountBalance);
		} catch (EmptyResultDataAccessException er) {
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("E003")
					, null);
		} catch (Exception e) {
			logger.error("dao error", e);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("F001")
					, null);
		}
	}

	@Override
	public Triplet<Boolean, String, AccountBalance> transfer(AccountTransfer transfer) {
		// TODO Auto-generated method stub
		return null;
	}

}
