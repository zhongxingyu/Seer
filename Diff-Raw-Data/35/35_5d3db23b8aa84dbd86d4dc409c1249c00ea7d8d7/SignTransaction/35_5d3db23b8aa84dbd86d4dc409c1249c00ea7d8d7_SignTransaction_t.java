 package com.dustyneuron.bitprivacy.exchanger.messages;
 
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 
 public class SignTransaction {
 	final public String type = "sign";
 	
	public String tradeId;
	public String signOutput;
 	public String hexData;
 	
 	public SignTransaction(Sha256Hash tradeKey, Address signOutput, String hex) {
		tradeId = tradeKey.toString();
		this.signOutput = signOutput.toString();
		hexData = hex;
	}
	
	public SignTransaction(String tradeKey, String signOutput, String hex) {
		tradeId = tradeKey.toString();
		this.signOutput = signOutput.toString();
 		hexData = hex;
 	}
 }
