package com.richard.transactionservice.process;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.transactionservice.model.Account;
import com.richard.transactionservice.model.AccountSync;

public class AccountSyncMessagePayloadParserImpl implements AccountSyncMessagePayloadParser {
	private Logger logger = LoggerFactory.getLogger(AccountSyncMessagePayloadParserImpl.class);
	private JSONParser parser;
	public AccountSyncMessagePayloadParserImpl() {
		parser = new JSONParser();
		
	}
	
	// sample message:
	// {"msgKey":"53380438-9f07-4190-93bf-388a95c820b5","account":{"accountNo":"000000000326490001","name":"Susan"}}
	@Override
	public Pair<Account, AccountSync> parse(String payload) {
		try {
			JSONObject jo = (JSONObject)parser.parse(payload);
			String msgKey = (String)jo.get("msgKey");
			JSONObject joaccount = (JSONObject)jo.get("account");
			if (joaccount == null) {
				logger.error("account node missing");
				return null;
			}
			String accountNo = (String)joaccount.get("accountNo");
			String name = (String)joaccount.get("name");
			if (StringUtils.isAnyBlank(msgKey, accountNo, name)) {
				logger.error("Blank found for msgKey: {} accountNo: {} name: {}"
						, msgKey, accountNo, name);
				return null;				
			}
			Account account = new Account();
			account.setAccountno(accountNo);
			account.setName(name);
			AccountSync accountSync = new AccountSync();
			accountSync.setAccountno(accountNo);
			accountSync.setMsgkey(msgKey);
			accountSync.setPayload(payload);
			
			return Pair.with(account, accountSync);
		}
		catch (ParseException pe) {
			logger.error("parser.parse", pe);
			return null;
		}
	}

}
