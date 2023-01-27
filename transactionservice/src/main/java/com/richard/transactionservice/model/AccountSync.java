package com.richard.transactionservice.model;

import java.sql.Timestamp;

public class AccountSync {
	private String id;
	private String msgkey;
	private String accountno;
	private String payload;
	private String status;
	private Timestamp uptime;


	public static final String STATUS_SUCCESS = "S";
	public static final String STATUS_FAILED = "F";
	
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
	
	public Timestamp getUptime() {
		return uptime;
	}
	public void setUptime(Timestamp uptime) {
		this.uptime = uptime;
	}
	
	public String toString() {
		return String.format("AccountSync[id=%s,msgkey=%s,accountno=%s,payload=%s,status=%s,uptime=%s]", id, msgkey, accountno, payload, status, uptime);
	}
}
