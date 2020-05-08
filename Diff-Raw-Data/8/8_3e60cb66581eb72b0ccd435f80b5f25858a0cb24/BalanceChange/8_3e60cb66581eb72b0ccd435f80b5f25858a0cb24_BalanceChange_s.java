 package billsplit.engine;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Collection;
 
 
 public abstract class BalanceChange {
 	private Date date;
 	private String name;
 	protected HashMap<Participant,Double> amounts; //unordered (its a hash)
 	protected HashMap<Participant,Double> debits; //unordered (its a hash)
 	protected HashMap<Participant,Double> credits; //unordered (its a hash)
 	protected Collection<Participant> participants; //ordered list of participants
 	
 	protected BalanceChange(String name, Collection<Participant> participants) {
 		// The 'real' constructor that all other constructors call
 		this.name = name;
 		this.amounts = new HashMap<Participant,Double>();
 		this.debits = new HashMap<Participant,Double>();  //values represent amount participant owes (- value)
 		this.credits = new HashMap<Participant,Double>(); //values represent amount participant is paying (+ value)
 		this.participants = participants;
 		for(Participant p : participants){
 			this.amounts.put(p, 0.0);  //initialize amounts to 0 for each participant
 			this.debits.put(p, 0.0);  //initialize debits to 0 for each participant
 			this.credits.put(p, 0.0);  //initialize credits to 0 for each participant
 		}
 	}
 
 	public boolean containsParticipant(Participant p) {
 		return this.participants.contains(p);
 	}
 	
 	public Collection<Participant> getParticipants(){
 		return this.participants;
 	}
 	
 	/* require total debit to equal total credit */
 	public HashMap<Participant,Double> getAmounts(){
 		if(getDebitsTotal() != getCreditsTotal()){
 			return null;
 		}else{
 			for(Participant p : participants){
 				/* credit is positive amount, debit is amount to remove */
 				amounts.put(p, getCredit(p) - getDebit(p));
 			}
 		}
 		return amounts;
 	}
 	
 	
 	public double getAmount(Participant p){
 		return this.amounts.get(p);
 	}
 	
 	public HashMap<Participant,Double> getDebits(){
 		return debits;
 	}
 	
 	
 	public double getDebit(Participant p){
		return this.amounts.get(p);
 	}
 	
 	public double getDebitsTotal(){
 		double returnAmount=0;
 		for(Participant p : participants){
 			returnAmount += getDebit(p);
 		}
 		return returnAmount;
 	}
 	
 	public HashMap<Participant,Double> getCredits(){
		return amounts;
 	}
 	
 	
 	public double getCredit(Participant p){
		return this.amounts.get(p);
 	}
 	
 	public double getCreditsTotal(){
 		double returnAmount=0;
 		for(Participant p : participants){
 			returnAmount += getCredit(p);
 		}
 		return returnAmount;
 	}
 	
 	public Date getDate(){
 		return this.date;
 	}
 	
 	public void setDate(Date date){
 		this.date = date;
 	}
 	
 	public String getName(){
 		return this.name;
 	}
 	
 	/* debits minus credits.  Indicates how much has yet 
 	 * to be paid.  Allows it to be plugged into UI directly
 	 */
 	public double getDebitCreditDiff(){
 		return getDebitsTotal() - getCreditsTotal();
 	}
 	
 	public boolean isPaymentComplete(){
 		if(getDebitCreditDiff() == 0){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	public void setDebit(Participant p, double amount){
 		debits.put(p, amount);
 	}
 	
 	public void setCredit(Participant p, double amount){
 		credits.put(p, amount);
 	}
 	/*******************************************************
 	 *  removed for version 1.0  
 	 *     All of the below code has been removed for version
 	 *     1.0.  These features are not supported.
 	 *******************************************************/
 	/*
 	public int addParticipant(Participant p){
 		int newidx = this.participants.size();
 		this.participants.add(p);
 		this.amounts.put(p, 0.0);
 		return newidx;
 	} 
 
 	public void removeParticipant(Participant p){
 		this.participants.remove(p);
 		this.amounts.remove(p);
 	}
 	
 	public void setAmountForPerson(Participant p, double amount){
 		this.amounts.put(p, amount);
 	}
 	
 	public void setDate(Date date){
 		this.date = date;
 	}
 	*/
 }
