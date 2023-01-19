package com.richard.authenticationservice.model;

public class Account {
	private String accountId;
	private String name;
	public String getAccountId() {
		return accountId;
	}
	public String getName() {
		return name;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "Account[accountId=" + this.accountId + ",name=" + this.name + "]";
	}
}
