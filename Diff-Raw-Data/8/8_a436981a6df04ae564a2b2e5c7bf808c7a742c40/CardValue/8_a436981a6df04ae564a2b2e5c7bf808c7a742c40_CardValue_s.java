 package com.max.algs.cards;
 
 public enum CardValue {
 
 	Six("6"),
 	Seven("7"),
 	Eight("8"),
 	Nine("9"),
 	Ten("10"),
	Jack(""),
	Queen(""), 
	King(""),
	Axe("");
 	
 	private final String simbol;
 
 	private CardValue(String simbol) {
 		this.simbol = simbol;
 	}
 	
 	public String toString(){
 		return simbol;
 	}
 	
 }
