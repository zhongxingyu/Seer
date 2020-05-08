package org.jboss.as.quickstarts.kitchensink.rest;
 
 
 public class TransactionRequest {
 	
 	private String posId;
 	private double amount;
 	
 	public String getPosId() {
 		return posId;
 	}
 	public void setPosId(String posId) {
 		this.posId = posId;
 	}
 	public double getAmount() {
 		return amount;
 	}
 	public void setAmount(double amount) {
 		this.amount = amount;
 	}
 	
 
 }
