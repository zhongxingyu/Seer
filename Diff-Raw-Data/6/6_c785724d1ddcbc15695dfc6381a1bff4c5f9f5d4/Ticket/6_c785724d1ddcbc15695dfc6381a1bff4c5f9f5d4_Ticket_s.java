 
 package assets;
 
 /**
  * Represents a ticket line from an available tickets file, containing
  * a quantity of tickets from being sold from some seller, to some event.
  */
 public class Ticket {
 	private String 	eventName;
 	private int 	quantity;
 	private int 	price;
 	private String 	username;
 	
 	public static final int eventName_size = 19;
 	public static final int quantity_size  =  3;
 	public static final int price_size     =  6;
 	public static final int username_size  = 15;
 	
 	/**
 	 * Constructs a new Ticket object, from the given line of text
      * from the available tickets file.
      */
 	public Ticket(String ATFLine) {
 		this.eventName = ATFLine.substring(0, eventName_size-1);
		this.username  = ATFLine.substring(eventName+1, eventName+1+username_size-1);
		this.quantity  = Integer.parseInt( ATFLine.substring(eventName_size + 1 + username_size + 1, eventName_size + 1 + username_size + 1 + quantity_size -1 ));
		this.price     = Integer.parseInt( ATFLine.substring(eventName_size + 1 + username_size + 1 + quantity_size + 1, eventName_size + 1 + username_size + 1 + quantity_size + 1 + price_size -1 ));
 	}
 
 	/**
      *  Returns the string representation of this ticket, in a format
      *  suitable to be written to an available tickets file.
      */
 	public String toString() {
 		
 		return "";
 	}	
 
 }
 
 
 
