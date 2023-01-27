package com.richard.transactionservice.process;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.transactionservice.Clock;
import com.richard.transactionservice.TransactionserviceMessageCode;
import com.richard.transactionservice.model.AccountSync;

public class AdminMonitorImpl implements AdminMonitor {
	private Logger logger = LoggerFactory.getLogger(AdminMonitorImpl.class);
	
	private static final String ADMINPASSWD = "iamadmin";
	
	private Map<String,AccountSync> duplicateAccountSyncEntry;
	private Clock clock;
	public AdminMonitorImpl(Clock clock) {
		this.clock = clock;
		this.duplicateAccountSyncEntry = new ConcurrentHashMap<>(5);
	}
	
	private boolean isPasswordValid(char[] password) {
		return ADMINPASSWD.equals(new String(password));
	}
	
	@Override
	public Pair<Boolean, String> checkStatus(char[] adminpassword) {
		if (isPasswordValid(adminpassword)) {
			return Pair.with(true, TransactionserviceMessageCode.getInstance().getMessage("A001"));
		} else {
			return Pair.with(false, TransactionserviceMessageCode.getInstance().getMessage("M002"));
		}
	}

	@Override
	public Triplet<Boolean, String, AccountSync> addDuplicateAccountSync(AccountSync item) {
		AccountSync itemTobestored = new AccountSync();
		itemTobestored.setAccountno(item.getAccountno());
		itemTobestored.setId(item.getId());
		itemTobestored.setMsgkey(item.getMsgkey());
		itemTobestored.setPayload(item.getPayload());
		itemTobestored.setStatus(false);
		itemTobestored.setUptime(new Timestamp(clock.getCurrentTimestamp()));
		
		duplicateAccountSyncEntry.put(itemTobestored.getMsgkey(), itemTobestored);
		logger.debug("addDuplicateAccountSync handled: {}", itemTobestored);
		return Triplet.with(true, TransactionserviceMessageCode.getInstance().getMessage("A002")
				, itemTobestored);
	}


	@Override
	public Triplet<Boolean, String, AccountSync> retrieveDuplicateAccountSync(char[] adminpassword, String msgKey) {
		if (isPasswordValid(adminpassword)) {
			AccountSync item = duplicateAccountSyncEntry.get(msgKey);
			logger.debug("retrieveDuplicateAccountSync with msgkey: {} is {}", msgKey, item);
			if (item != null) {
				return Triplet.with(true, TransactionserviceMessageCode.getInstance().getMessage("A004")
						, item);
			} else {
				return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("A003")
						, null);
			}
		} else {
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("M002"), null);
		}
	}

}
