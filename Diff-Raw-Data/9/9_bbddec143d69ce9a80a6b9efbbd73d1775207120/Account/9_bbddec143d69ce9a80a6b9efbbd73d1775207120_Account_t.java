 package ch.fhnw.mscbis.premscis.handson6.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "ACCOUNT_HANDS_ON_6")
 public class Account {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
 	private int ID;
 	private String name;
 	private long balance;
 	@ManyToOne
	private Customer customer;
 	
 	public int getID() {
 		return ID;
 	}
 	public void setID(int iD) {
 		ID = iD;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public long getBalance() {
 		return balance;
 	}
 	public void setBalance(long balance) {
 		this.balance = balance;
 	}
 	public Customer getCustomer() {
 		return customer;
 	}
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 	
 }
