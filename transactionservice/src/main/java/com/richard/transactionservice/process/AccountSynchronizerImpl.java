package com.richard.transactionservice.process;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.TransactionStatus;

import com.richard.transactionservice.TransactionserviceMessageCode;
import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountDao;
import com.richard.transactionservice.db.AccountSyncDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountSync;

public class AccountSynchronizerImpl implements AccountSynchronizer {
	private Logger logger = LoggerFactory.getLogger(AccountSynchronizerImpl.class);
	
	private JDBCResourceMgr jdbcresourcemgr;
	private AccountDao accountDao;
	private AccountSyncDao accountSyncDao;
	private AccountBalanceDao accountBalanceDao;
	private AdminMonitor adminMonitor;
	
	public AccountSynchronizerImpl(JDBCResourceMgr jdbcresourcemgr, AccountDao accountDao
			, AccountSyncDao accountSyncDao, AccountBalanceDao accountBalanceDao, AdminMonitor adminMonitor) {
		this.jdbcresourcemgr = jdbcresourcemgr;
		this.accountDao = accountDao;
		this.accountSyncDao = accountSyncDao;
		this.accountBalanceDao = accountBalanceDao;
		this.adminMonitor = adminMonitor;
	}

	@Override
	public Triplet<Boolean, String, AccountSync> process(Account acctItem, AccountSync syncItem, String doneBy) {
		// checking consistency of args
		try {
			String accountNoFromFirstArg = acctItem.getAccountno();
			String accountNoFromSecondArg = syncItem.getAccountno();
			if (!accountNoFromFirstArg.equals(accountNoFromSecondArg)) {
				throw new IllegalArgumentException("accountno inconsistent");
			}
			if (StringUtils.isBlank(doneBy)) {
				throw new IllegalArgumentException("doneBy is blank");
			}
		} catch (Exception e) {
			logger.error("incoming data not consistent", e);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("F001")
					, syncItem);
		}
		
		// check account sync exists or not
		AccountSync existingItem = null;
		try {
			existingItem = accountSyncDao.getByMessageKey(syncItem.getMsgkey());
			logger.debug("existing accountsync entry: {}", existingItem);
			if (existingItem != null) {
				Triplet<Boolean, String, AccountSync> handleResult = 
						adminMonitor.addDuplicateAccountSync(existingItem);
				logger.info("duplicate account sync entry found and handled: {}", handleResult);
				throw new IllegalStateException("existing accountsync entry found");
			}
		} catch (IllegalStateException ie) {
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("E002")
					, syncItem);
		} catch (EmptyResultDataAccessException erde) {
			logger.debug("no accountsync entry found. {}", syncItem);
		} catch (Exception e) {
			logger.error("accountSyncDao.getByMessageKey", e);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("F001")
					, syncItem);
		}
		
		TransactionStatus status = null;
		try {
			status = jdbcresourcemgr.getTransactionManager().getTransaction(
					jdbcresourcemgr.createTransactionDefinition());
			accountDao.createAccount(acctItem);
			AccountBalance balance = new AccountBalance();
			balance.setAccountno(acctItem.getAccountno());
			balance.setBalance(BigDecimal.ZERO);
			balance.setDoneby(doneBy);
			accountBalanceDao.initAccountBalanceEntry(balance);
			// finalize the account sync status
			syncItem.setStatus(true);
			int accountSyncDaoResult = accountSyncDao.createAccountSync(syncItem);
			logger.debug("accountSyncDaoResult: {}", accountSyncDaoResult);
			jdbcresourcemgr.getTransactionManager().commit(status);			
		} catch (Exception e) {
			logger.error("dao error", e);
			jdbcresourcemgr.getTransactionManager().rollback(status);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("F001"), syncItem);
		}
		
		return Triplet.with(true, TransactionserviceMessageCode.getInstance().getMessage("M001"), syncItem);
	}

}
