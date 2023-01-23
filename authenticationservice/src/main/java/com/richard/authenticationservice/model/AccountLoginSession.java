package com.richard.authenticationservice.model;

import java.sql.Timestamp;

public class AccountLoginSession {
	private String id;
	private String sessionkey;
	private String accountno;
	private Timestamp expirytime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSessionkey() {
		return sessionkey;
	}
	public void setSessionkey(String sessionkey) {
		this.sessionkey = sessionkey;
	}
	public String getAccountno() {
		return accountno;
	}
	public void setAccountno(String accountno) {
		this.accountno = accountno;
	}
	public Timestamp getExpirytime() {
		return expirytime;
	}
	public void setExpirytime(Timestamp expirytime) {
		this.expirytime = expirytime;
	}
}
