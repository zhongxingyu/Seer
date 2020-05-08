 package com.tort.trade.journals;
 
 import com.tort.trade.model.Good;
 
 public class GoodBalance {
 	private final Good _good;
 	private final long _balance;
 	
 	public GoodBalance(Good good, long balance) {
 		super();
 		_good = good;
 		_balance = balance;
 	}
 
	public Good get_good() {
 		return _good;
 	}
 
	public long get_balance() {
 		return _balance;
 	}
 }
