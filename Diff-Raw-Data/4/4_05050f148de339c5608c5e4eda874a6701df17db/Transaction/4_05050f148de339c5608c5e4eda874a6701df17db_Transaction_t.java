 package billsplit.engine;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 public class Transaction extends BalanceChange {
 	private Hashtable<Participant,Item> paymentMatrix;
 	private Hashtable<Item,Participant> itemMatrix;
 	
 	private Hashtable<Participant,Integer> pidLookup;
 	private Hashtable<Item,Integer> itemLookup;
 	
 	private Event eventOwner;
 	private ArrayList<Item> items;
 	
 	private ArrayList<ArrayList<Double>> matrix;
 	
 	
 	public Transaction(ArrayList<Participant> participants) {
 		this.participants = participants;
 	}
 	
 	public boolean containsItem(Item item) {
 		  return this.items.contains(item);
 		  
 	}
 	
 	public ArrayList<Item> getItems() {
 		return this.items;
 	}
 	
 	public void removeItem(Item item) {
 		this.items.remove(item);
 		//todo: also remove from matrices
 	}
 	
 	public void removeItem(int itemIndex) {
 		this.items.remove(itemIndex);
 		//todo: also remove from matrices
 	}
 	
 	public int addItem(Item item) {
 		this.items.add(item);
 		return this.items.size();
 	}
 	
 	public void setPayers(Item item, ArrayList<Participant> participants) {
 		throw new UnsupportedOperationException("Not implemented yet.");
 	}
 	
 	public void addPayer(Item item, Participant p) {
 		throw new UnsupportedOperationException("Not implemented yet.");
 	}
 	
 	public void removePayer(Item item, Participant p) {
 		throw new UnsupportedOperationException("Not implemented yet.");
 	}
 	
 	public void splitAllEvenly() {
		for (int i=0;i<participants.size();i++) {
			matrix.get(i);
 		}
 	}
 	
 	public void clearPayers(Item item) {
 		throw new UnsupportedOperationException("Not implemented yet.");
 	}
 	
 	public void fromExternal() {
 		throw new UnsupportedOperationException("Not implemented yet.");
 	}
 
 	/**
 	 * @return the eventOwner
 	 */
 	public Event getEventOwner() {
 		return eventOwner;
 	}
 
 	/**
 	 * @param eventOwner the eventOwner to set
 	 */
 	public void setEventOwner(Event eventOwner) {
 		this.eventOwner = eventOwner;
 	}
 }
