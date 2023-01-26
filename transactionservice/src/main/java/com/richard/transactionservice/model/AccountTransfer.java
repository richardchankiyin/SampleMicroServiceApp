package com.richard.transactionservice.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class AccountTransfer {
	private String id;
	private String accountno;
	private BigDecimal amount;
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
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
		return String.format("AcountTransfer[id=%s,accountno=%s,amount=%s,doneby=%s,uptime=%s]"
				, id, accountno, amount, doneby, uptime);
	}
}
