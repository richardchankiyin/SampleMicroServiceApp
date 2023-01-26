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
		
		// message
		code2Message.put("M001", "Account sync successfully");
		code2Message.put("M002", "Unauthorized access");
		code2Message.put("M003", "Authorized");
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
