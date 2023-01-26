package com.richard.transactionservice.process;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.TransactionStatus;

import com.richard.transactionservice.Clock;
import com.richard.transactionservice.TransactionserviceMessageCode;
import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountTransferDao;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountTransfer;

public class AccountBalanceMaintenanceImpl implements AccountBalanceMaintenance {
	private static final Logger logger = LoggerFactory.getLogger(AccountBalanceMaintenanceImpl.class);
	
	private Clock clock;
	private JDBCResourceMgr jdbcresourcemgr;
	private AccountBalanceDao accountBalanceDao;
	private AccountTransferDao accountTransferDao;
	
	public AccountBalanceMaintenanceImpl(Clock clock, JDBCResourceMgr jdbcresourcemgr
			, AccountBalanceDao accountBalanceDao, AccountTransferDao accountTransferDao) {
		this.clock = clock;
		this.jdbcresourcemgr = jdbcresourcemgr;
		this.accountBalanceDao = accountBalanceDao;
		this.accountTransferDao = accountTransferDao;
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
	
	private void validateIncomingAccountTransfer(AccountTransfer transfer) {
		try {
			if (transfer == null) {
				logger.debug("transfer object null");
				throw new IllegalArgumentException("null object");
			}
			String accountno = transfer.getAccountno();
			String doneBy = transfer.getDoneby();
			if (StringUtils.isAnyBlank(accountno, doneBy)) {
				throw new IllegalArgumentException("accountno: " + accountno + " doneby: " + doneBy);
			}
			BigDecimal amount = transfer.getAmount();
			if (amount == null) {
				throw new IllegalArgumentException("amount null");
			}
		} catch(IllegalArgumentException ie) {
				throw ie;
		} catch (Exception e) {
			logger.error("validateIncomingAccountTransfer issue", e);
			throw new IllegalArgumentException(e);
		}
	}
	
	private boolean validateAmount(BigDecimal amount) {
		return !BigDecimal.ZERO.equals(amount);
	}

	@Override
	public Triplet<Boolean, String, AccountBalance> transfer(AccountTransfer transfer) {
		try {
			validateIncomingAccountTransfer(transfer);
		} catch (Exception e) {
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("E001")
					, null);
		}
		
		BigDecimal amount = transfer.getAmount();
		
		if (!validateAmount(amount)) {
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("M005")
					, null);
		}
		
		AccountBalance beforeUpdateBalance = null;
		try {
			beforeUpdateBalance = accountBalanceDao.getByAccountNo(transfer.getAccountno());
			if (beforeUpdateBalance == null) {
				throw new IllegalStateException("beforeUpdateBalance is null");
			}
		} catch (Exception e) {
			logger.error("dao issue", e);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("E003")
					, null);
		}
		
		AccountBalance afterUpdateBalance = new AccountBalance();
		afterUpdateBalance.setAccountno(beforeUpdateBalance.getAccountno());
		afterUpdateBalance.setBalance(beforeUpdateBalance.getBalance().add(amount));
		afterUpdateBalance.setDoneby(transfer.getDoneby());
		afterUpdateBalance.setUptime(new Timestamp(clock.getCurrentTimestamp()));
		
		logger.debug("beforeUpdateBalance: {} afterUpdateBalance:{}", beforeUpdateBalance, afterUpdateBalance);
		
		TransactionStatus status = null;
		try {
			status = jdbcresourcemgr.getTransactionManager().getTransaction(
					jdbcresourcemgr.createTransactionDefinition());
			
			accountBalanceDao.updateAccountBalance(beforeUpdateBalance, afterUpdateBalance);
			accountTransferDao.createAccountTransfer(transfer);
			jdbcresourcemgr.getTransactionManager().commit(status);
			return Triplet.with(true, TransactionserviceMessageCode.getInstance().getMessage("M007")
					, afterUpdateBalance);
		} catch (Exception e) {
			logger.error("dao error", e);
			jdbcresourcemgr.getTransactionManager().rollback(status);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("M006")
					, afterUpdateBalance);
		}
	}

}
