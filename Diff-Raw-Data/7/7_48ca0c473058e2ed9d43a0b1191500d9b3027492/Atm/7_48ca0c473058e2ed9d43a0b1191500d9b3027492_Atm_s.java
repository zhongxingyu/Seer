 package org.common.atms;
 
 public class Atm {
 
 	private String id;
 	private String sslKeyMark;
 	private Double balance;
 	private boolean status;
 	
	//private HashMap<int, int> bills = new HashMap<int, int>();
 	
 	public String getId() {
 		return id;
 	}
 	
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	public String getSslKeyMark() {
 		return sslKeyMark;
 	}
 	
 	public void setSslKeyMark(String sslKeyMark) {
 		this.sslKeyMark = sslKeyMark;
 	}
 	
 	public Double getBalance() {
 		return balance;
 	}
 	
 	public void setBalance(Double balance) {
 		this.balance = balance;
 	
 	}
 	public boolean getStatus() {
 		return status;
 	}
 	
 	public void setStatus(boolean status) {
 		this.status = status;
 	}
 	
	
 }
