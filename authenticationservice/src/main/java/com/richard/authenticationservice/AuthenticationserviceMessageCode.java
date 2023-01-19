package com.richard.authenticationservice;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationserviceMessageCode {
	
	private static final AuthenticationserviceMessageCode INSTANCE = new AuthenticationserviceMessageCode();
	
	public static AuthenticationserviceMessageCode getInstance() { return INSTANCE; }
	
	private Map<String,String> code2Message = new HashMap<>();
	{
		// fatal
		code2Message.put("F001", "System error");
		
		// error
		code2Message.put("E001", "Wrong Request Content");
		code2Message.put("E002", "Insufficient Account Info");
		
		// message
		code2Message.put("M001", "Account created successfully");
		
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
