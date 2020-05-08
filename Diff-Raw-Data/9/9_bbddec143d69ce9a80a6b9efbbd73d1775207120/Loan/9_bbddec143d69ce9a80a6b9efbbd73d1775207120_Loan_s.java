 package ch.fhnw.mscbis.premscis.handson6.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "LOAN_HANDS_ON_6")
 public class Loan {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
 	private int ID;
 	private String loanType;
 	private String term;
 	private long amount;
 	@ManyToOne
	public Customer customer;
 	
 	public int getID() {
 		return ID;
 	}
 	public void setID(int iD) {
 		ID = iD;
 	}
 	public String getLoanType() {
 		return loanType;
 	}
 	public void setLoanType(String loanType) {
 		this.loanType = loanType;
 	}
 	public String getTerm() {
 		return term;
 	}
 	public void setTerm(String term) {
 		this.term = term;
 	}
 	public long getAmount() {
 		return amount;
 	}
 	public void setAmount(long amount) {
 		this.amount = amount;
 	}
 	public Customer getCustomer() {
 		return customer;
 	}
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 }
