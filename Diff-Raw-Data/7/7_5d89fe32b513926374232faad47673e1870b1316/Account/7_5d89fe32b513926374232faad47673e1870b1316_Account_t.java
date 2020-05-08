 package hx.atm;
 
 public class Account {
 	
 	private int balance;
	
	public Account(){
		this.balance = 1000;
	}
 
 	public int getBalance() {
		return this.balance;
 	}
 
 	public void deposit(int i) {
        balance = balance + i;
 		
 	}
 
 }
