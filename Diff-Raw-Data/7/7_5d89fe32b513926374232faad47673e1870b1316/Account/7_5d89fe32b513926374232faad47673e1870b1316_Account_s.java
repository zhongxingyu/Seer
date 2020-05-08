 package hx.atm;
 
 public class Account {
 	
 	private int balance;
 
 	public int getBalance() {
		return 1000;
 	}
 
 	public void deposit(int i) {
        balance = balance + i;
 		
 	}
 
 }
