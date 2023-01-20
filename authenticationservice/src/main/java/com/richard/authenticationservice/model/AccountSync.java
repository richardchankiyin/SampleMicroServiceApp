package com.richard.authenticationservice.model;

public class AccountSync {
	private String id;
	private String msgkey;
	private String accountno;
	private String payload;
	private String status;
	private static final String STATUS_SUCCESS = "S";
	private static final String STATUS_FAILED = "F";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMsgkey() {
		return msgkey;
	}
	public void setMsgkey(String msgkey) {
		this.msgkey = msgkey;
	}
	public String getAccountno() {
		return accountno;
	}
	public void setAccountno(String accountno) {
		this.accountno = accountno;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(boolean isSuccess) {
		if (isSuccess) {
			setStatus(STATUS_SUCCESS);
		} else {
			setStatus(STATUS_FAILED);
		}
	}
	private void setStatus(String s) {
		this.status = s;
	}
}
