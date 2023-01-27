package com.richard.transactionservice;

import java.util.HashMap;
import java.util.Map;

public class TransactionserviceMessageCode {
	private static final TransactionserviceMessageCode INSTANCE = new TransactionserviceMessageCode();
	
	public static TransactionserviceMessageCode getInstance() { return INSTANCE; }
	
	private Map<String,String> code2Message = new HashMap<>();
	{
		// fatal
		code2Message.put("F001", "System error");
		
		// error
		code2Message.put("E001", "Wrong Request Content");
		code2Message.put("E002", "Account sync request processed before");
		code2Message.put("E003", "No account balance found");
		
		
		// message
		code2Message.put("M001", "Account sync successfully");
		code2Message.put("M002", "Unauthorized access");
		code2Message.put("M003", "Authorized");
		code2Message.put("M004", "Account Balance retrieved");
		code2Message.put("M005", "Transfer Amount not accepted");
		code2Message.put("M006", "Transfer rejected");
		code2Message.put("M007", "Transfer complete");
		
		
		// admin message
		code2Message.put("A001", "Service is ready");
		code2Message.put("A002", "Duplicate Account Sync Message handled");
		code2Message.put("A003", "No duplicate account sync found");
		code2Message.put("A004", "Duplicate account sync found");
	}
	
	public String getMessage(String code) {
		String msg = code2Message.get(code);
		if (msg != null) {
			return "[" + code + "]" + msg; 
		} else {
			return "";
		}
	}
}
