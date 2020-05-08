 package com.cse769.EJB.Service;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import com.cse769.EJB.Entity.Event;
 import com.cse769.EJB.Entity.EventCategory;
 import com.cse769.EJB.Entity.Ticket;
 import com.cse769.EJB.Entity.Venue;
 
 @Stateless
 public class EventService {
 
 	@PersistenceContext(unitName = "examples-769-EJB")
 	EntityManager em;
 
 	public void createEvent(String name, EventCategory category,
 			String description, int cost, Date date, int quantity, Venue venue) {
 		Event event = new Event();
 		event.setName(name);
 		event.setCategory(category);
 		event.setDescription(description);
 		event.setCost(cost);
 		event.setDate(date);
 		event.setQuantity(quantity);
 		event.setVenue(venue);
 		List<Ticket> tickets = new ArrayList<Ticket>();
 		for (int i = 0; i < quantity; i++) {
 			Ticket t = new Ticket();
 			t.setEvent(event);
 			t.setSoldFlag(false);
 			t.setTransaction(null);
 			// em.persist(t);
 			tickets.add(t);
 		}
 		event.setTickets(tickets);
 		em.persist(event);
 	}
 
 	public Event getEventById(Long id) {
 		return em.find(Event.class, id);
 	}
 
 	public void removeEvent(Long id) {
 		Event ev = em.find(Event.class, id);
 		em.remove(ev);
 	}
 
 	public void updateEvent(Event ev) {
 		em.getTransaction().begin();
 		Event newEvent = em.find(Event.class, ev.getEventId());
 		newEvent.setCategory(ev.getCategory());
 		newEvent.setCost(ev.getCost());
 		newEvent.setDate(ev.getDate());
 		newEvent.setDescription(ev.getDescription());
 		newEvent.setName(ev.getName());
 		newEvent.setQuantity(ev.getQuantity());
 		newEvent.setVenue(ev.getVenue());
 		em.getTransaction().commit();
 	}
 
 	public List<Event> getAllEvents() {
 		List<Event> events = em.createQuery("SELECT e from Event e",
 				Event.class).getResultList();
 		return events;
 	}
 
 	public List<Event> findEventsByName(String event) {		
 		final String query = "SELECT e FROM Event e WHERE e.name = :name";
 		final String name = event;
 		TypedQuery<Event> query1 = em.createQuery(query, Event.class);
 		query1.setParameter("name", name);
 		final List<Event> events = query1.getResultList();
 		return events;
 	}
 	
 	public List<Event> searchEventsByName(String search) {		
 		final String query = "SELECT e FROM Event e WHERE upper(e.name) LIKE upper(:name)";
 		final String name = "%" + search + "%";
 		TypedQuery<Event> query1 = em.createQuery(query, Event.class);
 		query1.setParameter("name", name);
 		final List<Event> events = query1.getResultList();
 		return events;
 	}
 
 	public boolean createDemoEvents() {
 		EventCategory sports = new EventCategory();
 		sports.setCategory("Sports");
 		EventCategory music = new EventCategory();
 		music.setCategory("Music");
 		Venue stadium = new Venue();
 		stadium.setAddress("411 Woody Hayes Drive");
 		stadium.setCity("Columbus");
 		stadium.setDescription("The Horseshoe");
 		stadium.setName("Ohio Stadium");
 		stadium.setSize(102329);
 		stadium.setState("OH");
 		stadium.setZipCode("43210");
 
 		try {
 			em.persist(sports);
 			em.persist(music);
 			em.persist(stadium);
 			em.flush();
 
 			Calendar cal = Calendar.getInstance();
 			cal.set(2013, 10, 30);
 			createEvent("Ohio State vs Michigan Football", sports,
 					"A game to end all games", 5324, cal.getTime(), 100,
 					stadium);
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	public List<Event> findEventsByCategory(String category) {		
 		final String query = "SELECT e FROM Event e WHERE e.category.category = :categoryName";
 		final String categoryName = category;
 		TypedQuery<Event> query1 = em.createQuery(query, Event.class);
 		query1.setParameter("categoryName", categoryName);
 		final List<Event> events = query1.getResultList();
 		return events;
 	}
 	
 	public List<Event> findEventsByCategoryId(Long id) {		
 		final String query = "SELECT e FROM Event e WHERE e.category.categoryId = :categoryId";
 		final Long categoryId = id;
 		TypedQuery<Event> query1 = em.createQuery(query, Event.class);
 		query1.setParameter("categoryId", categoryId);
 		final List<Event> events = query1.getResultList();
 		return events;
 	}
 	
 	public List<Event> findEventsByVenue(String venue) {		
 		final String query = "SELECT e FROM Event e WHERE e.venue.name = :venueName";
 		final String venueName = venue;
 		TypedQuery<Event> query1 = em.createQuery(query, Event.class);
 		query1.setParameter("venueName", venueName);
 		final List<Event> events = query1.getResultList();
 		return events;
 	}
 	
	public int getNumOfAvailableTickets(Long id) {
 		final String query = "SELECT COUNT(t) FROM Ticket t WHERE t.event.eventId = :eventId AND t.soldFlag = false";
 		final String eventId = id.toString();
		TypedQuery<Integer> query1 = em.createQuery(query, Integer.class);
 		query1.setParameter("eventId", eventId);
 		return query1.getSingleResult();
 	}
 }
