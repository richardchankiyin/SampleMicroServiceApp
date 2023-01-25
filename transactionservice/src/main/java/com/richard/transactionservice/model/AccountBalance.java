package com.richard.transactionservice.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class AccountBalance {
	private String id;
	private String accountno;
	private BigDecimal balance;
	private String doneby;
	private Timestamp uptime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccountno() {
		return accountno;
	}
	public void setAccountno(String accountno) {
		this.accountno = accountno;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public String getDoneby() {
		return doneby;
	}
	public void setDoneby(String doneby) {
		this.doneby = doneby;
	}
	public Timestamp getUptime() {
		return uptime;
	}
	public void setUptime(Timestamp uptime) {
		this.uptime = uptime;
	}
	
	public String toString() {
		return String.format("AccountBalance[id=%s,accountno=%s,balance=%s,doneby=%s,uptime=%s]"
				, id, accountno, balance, doneby, uptime);
	}
}
