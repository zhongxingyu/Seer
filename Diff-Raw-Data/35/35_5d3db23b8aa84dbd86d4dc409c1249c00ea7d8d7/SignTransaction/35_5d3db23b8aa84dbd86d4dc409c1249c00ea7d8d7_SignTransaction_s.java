 package com.dustyneuron.bitprivacy.exchanger.messages;
 
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 
 public class SignTransaction {
 	final public String type = "sign";
 	
	public Sha256Hash tradeId;
	public Address signOutput;
 	public String hexData;
 	
 	public SignTransaction(Sha256Hash tradeKey, Address signOutput, String hex) {
		tradeId = tradeKey;
		this.signOutput = signOutput;
 		hexData = hex;
 	}
 }
