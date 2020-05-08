 package domain;
 
 import java.util.List;
 
 import persistence.EventMapper;
 import persistence.TicketMapper;
 import domain.model.Event;
 import domain.model.Ticket;
 
 
 
 /**
  * @author Matthew Tam
  * Catalog that maintains the list of events. Also allows clients to book tickets to events
  */
 public class Catalog {
 	
 	private List<Event> eventRepository;
 	private static Catalog catalog = null;
 	
 	private Catalog() {
 		eventRepository = EventMapper.findAll();
 	}
 	
 	public static Catalog getInstance() {
 		if (catalog == null) {
 			catalog = new Catalog();
 		}
 		return catalog;
 	}
 	
 	public void viewAllEvents() {		
 		System.out.println("Event List");
 		System.out.println("======================================================================================");
 		System.out.println("Unique ID | Date 	| Hall | Showing | Number Of Seats | Event Type | Title ");
 		for (Event e: eventRepository) {
 			System.out.print(e.getUniqueId() + "	    ");			
 			System.out.print(e.getDate() + " 	    ");
 			System.out.print(e.getHall() + " 	   ");
 			System.out.print(e.getShowing() + " 	        ");
 			System.out.print(e.getNumberOfSeats() + "	          ");
 			System.out.print(e.getEventType() + "       ");
 			System.out.println(e.getTitle());
 		}
 		
 		System.out.println("======================================================================================");
 	}
 
 	public void bookTicket(Ticket t) {
 		boolean isSuccessful = TicketMapper.insert(t);
 		
 		if(isSuccessful) {
 			System.out.println("Succesfully booked tickets to " + search(t.getEvent().getUniqueId()).getTitle() + " for " + t.getFirstName() + " " + t.getLastName());
 		}
 		else {
 			System.out.println("Could not book tickets for " + t.getFirstName() + " " + t.getLastName());
 		}
 	}
 	
 	public void viewAllTicketsForEvent(int eventId) {
 		List<Ticket> tickets = TicketMapper.find(search(eventId));
 		
 		System.out.println("Ticket List");
		System.out.println("=======================================================");
 		System.out.println("FirstName\t\tLast Name\t\tNumber Of Seats ");
 		for (Ticket t: tickets) {
 			System.out.print(t.getFirstName() + "\t\t\t");			
 			System.out.print(t.getLastName() + "\t\t\t");			
 			System.out.println(t.getNumberOfSeats());
 		}
		System.out.println("=======================================================");
 		
 	}
 	
 	
 	// HELPERS
 	
 	public Event search(int eventId) {
 		Event e = null;
 		for(Event er: eventRepository) {
 			if (er.getUniqueId() == eventId) {
 				e = er;
 				break;
 			}			
 		}
 		return e;
 	}
 }
