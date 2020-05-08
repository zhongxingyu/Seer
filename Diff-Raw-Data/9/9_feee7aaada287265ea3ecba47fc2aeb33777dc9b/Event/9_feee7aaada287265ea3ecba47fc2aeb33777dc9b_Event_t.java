 package billsplit.engine;
 
 import java.util.*;
 
 
 public class Event {
 	private String creatorGID;
 	private String name;
 	private ArrayList<Participant> participants;
 	private ArrayList<BalanceChange> txns;
 	private String category;
 	private Date dateCreated;
 	private Date lastModified;
 	public static Event currentEvent;
 	/* 
 	 * requires: creatorGID != null
 	 *           name != null
 	 */
 	public Event(String creatorGID, String name){
 		assert(creatorGID != null && name != null);
 		this.creatorGID = creatorGID;
 		this.name = name;
 		participants = new ArrayList<Participant>();
 		txns = new ArrayList<BalanceChange>();
 		category = "None";
 		dateCreated = new Date();
 		lastModified = dateCreated;
 	}
 	
 	/* 
 	 * requires: creatorGID != null
 	 *           name != null
 	 *           category != null
 	 */
 	public Event(String creatorGID, String name, String category){
 		assert(creatorGID != null && name != null && category != null);
 		this.creatorGID = creatorGID;
 		this.name = name;
 		participants = new ArrayList<Participant>();
 		txns = new ArrayList<BalanceChange>();
 		this.category = category;
 		dateCreated = new Date();
 		lastModified = dateCreated;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public String getCreatorGID(){
 		return creatorGID;
 	}
 	
 	
 	public Collection<Participant> getParticipants(){
 		return (Collection<Participant>) participants;
 	}
 	
 	/*
 	 * requires name != null
 	 */
 	public boolean isParticipant(String name){
 		assert(name != null);
 		for(Participant p : participants){
 			if(p.getName().equals(name)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	
 	/*
 	 * requires name != null
 	 */
 	public boolean isParticipantByGID(String gid){
 		assert(gid != null);
 		for(Participant p : participants){
 			/*
 			 * dacashman - getGID() missing from Account contract 
 			 */
 			if(p.getAccount().getGID().equals(gid)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isBalanceChange(String name){
 		assert(name != null);
 		for(BalanceChange b : txns){
 			if(b.getName().equals(name)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public Collection<BalanceChange> getBalanceChanges(){
 		return (Collection<BalanceChange>) txns;
 	}
 	
 	public String getCategory(){
 		return category;
 	}
 	
 	
 	
 	/*
 	 * dacashman - contract requires date to be non-null, but 
 	 * 	this is wrong.  null should mean "infinity."  A null 
 	 * start_date would mean "get all dates from beginning of event."
 	 *  requires: beginDate be before endDate.
 	 */
 	public Collection<BalanceChange> getBalanceChangesByDate(Date beginDate, Date endDate){
 		assert(beginDate.before(endDate));
 		ArrayList<BalanceChange> matching = new ArrayList<BalanceChange>();
 		if(beginDate == null){
 			if(endDate == null){
 				return (Collection<BalanceChange>) txns;
 			}else{
 				for(BalanceChange b : txns){
 					if(endDate.after(b.getDate())){
 						matching.add(b);
 					}
 				}
 			}
 		}else if(endDate == null){
 			for(BalanceChange b : txns){
 				if(beginDate.before(b.getDate())){
 					matching.add(b);
 				}
 			}
 		}else{
 			for(BalanceChange b : txns){
 				Date bDate = b.getDate();
 				if(beginDate.before(bDate) && endDate.after(bDate)){
 					matching.add(b);
 				}
 			}
 		}
 		return (Collection<BalanceChange>) matching;
 	}
 	
 	/*
 	 * require isParticipant(name).
 	 */
 	public Participant getParticipantByName(String name){
 		assert(isParticipant(name));
 		for(Participant p : participants){
 			if(p.getName().equals(name)){
 				return p;
 			}
 		}
 		/* should never get here: complain if so.*/
 		assert(false);
 		return null;
 	}
 	
 	/*
 	 * require isParticipantByGID(gid).
 	 */
 	public Participant getParticipantByGID(String gid){
 		assert(gid != null && isParticipantByGID(gid));
 		for(Participant p : participants){
 			if(p.getAccount().getGID().equals(gid)){
 				return p;
 			}
 		}
 		/* should never get here: complain if so.*/
 		assert(false);
 		return null;
 	}
 	
 	public void addBalanceChange(BalanceChange newBalanceChange){
 		assert(newBalanceChange != null);
 		txns.add(newBalanceChange);
 		lastModified = new Date();
 		updateBalances();
 		return;
 	}
 	
 	/*
 	 * requires: participant not already in event (by name and 
 	 * thus GID).
 	 */
 	public void addParticipant(Participant newParticipant){
 		assert(!isParticipant(newParticipant.getName()));
 		participants.add(newParticipant);
 		lastModified = new Date();
 		return;
 	}
 	
 	public void removeParticipant(Participant ParticipantToRemove){
 		assert(isParticipant(ParticipantToRemove.getName()));
 		participants.remove(ParticipantToRemove);
 		lastModified = new Date();
 		return;
 	}
 	
 		
 	/* dacashman - addBalanceChange, removeBalanceChange need to be 
 	 * 	added to the contract.
 	 */
 	public void removeBalanceChange(BalanceChange balanceChangeToRemove){
 		assert(isBalanceChange(balanceChangeToRemove.getName()));
 		txns.remove(balanceChangeToRemove);
 		lastModified = new Date();
 		return;
 	}
 	
 	public void setCategory(String category){
 		assert(category != null);
 		this.category = category;
 		lastModified = new Date();
 		return;
 	}
 	
 	/*
 	 * setCreatorGID should not exist or be in contract 
 	 */
 	
 	public void setName(String name){
 		assert(name != null);
 		this.name = name;
 		lastModified = new Date();
 		return;
 	}
 	
 	/* update - recaclulates balances based on all transactions 
 	 *  (eventually this could be replaced by iterative updates, but 
 	 *   that may introduce cross-cutting concerns.)
 	 */
 	public void updateBalances(){
 		HashMap<Participant, Double> retAmounts;
		
		/* kludgy way of dealing with participant clearing*/
		for(Participant p : participants){
			double currentBalance = p.getBalance();
			p.addToBalance(-currentBalance);
		}
 		for(BalanceChange b : txns){
 			retAmounts = b.getAmounts();
 			for(Participant p : participants){
 				//fetch value for participant
 				Double addAmount = retAmounts.get(p);
 				if(addAmount != null){
 					p.addToBalance((double) addAmount);
 				}
 			}
 		}
 		return;
 	}
 	
 	
 	/*
 	 * updateEvents() - currently just updates balance.
 	 *     Should we do more here?
 	 */
	public void updateEvent(){
 		updateBalances();
 		return;
 	}
 	
 }
