package com.richard.authenticationservice;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationserviceMessageCode {
	
	private Map<String,String> code2Message = new HashMap<>();
	{
		code2Message.put("E001", "Wrong Request Content");
		code2Message.put("E002", "Insufficient Account Info");
		
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
