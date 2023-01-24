package com.richard.transactionservice.model;

public class Account {
	private String id;
	private String accountno;
	private String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccountno() {
		return accountno;
	}
	public String getName() {
		return name;
	}
	public void setAccountno(String accountno) {
		this.accountno = accountno;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "Account[id=" + this.id + ",accountno=" + this.accountno + ",name=" + this.name + "]";
	}
}
