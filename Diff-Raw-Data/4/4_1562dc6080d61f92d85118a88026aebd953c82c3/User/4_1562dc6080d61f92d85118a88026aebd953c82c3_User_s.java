 package uk.ac.cam.signups.models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.DashboardApiWrapper;
 import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
 import uk.ac.cam.signups.exceptions.NotADosException;
 import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;
 
 import com.google.common.collect.ImmutableMap;
 
 @Entity
 @Table(name = "USERS")
 public class User implements Mappable {
 	@Transient
 	private Logger logger = LoggerFactory.getLogger(User.class);
 
 	@Id
 	private String crsid;
 
 	private String instID;
 
 	@OneToMany(mappedBy = "owner")
 	private List<Event> events = new ArrayList<Event>();
 
 	@OneToMany(mappedBy = "owner")
 	private Set<Slot> slots = new HashSet<Slot>(0);
 
 	public User() {
 	}
 
 	public User(String crsid, String instID) {
 		this.crsid = crsid;
 		this.instID = instID;
 	}
 
 	public Dos getDos(DashboardApiWrapper apiWrapper) throws NotADosException {
 		@SuppressWarnings("unchecked")
 		List<String> colleges = (List<String>) apiWrapper
 				.getUserSettings(crsid).getSettings().get("dosColleges");
 
 		if (colleges.isEmpty()) {
 			throw new NotADosException();
 		}
 
 		return new Dos(colleges);
 	}
 
 	public String getCollegeName() {
 		try {
 			return LDAPQueryManager.getUser(crsid).getCollegeName();
 		} catch (LDAPObjectNotFoundException e) {
 			return "Unknown";
 		}
 	}
 	
 	public String getName() {
 		try {
 			return LDAPQueryManager.getUser(crsid).getDisplayName();
 		} catch (LDAPObjectNotFoundException e) {
 			return "Unknown";
 		}
 	}
 
 	public String getNameCrsid() {
 		return getName() + " (" + getCrsid() + ")";
 	}
 
 	public String getInstID() {
 		return this.instID;
 	}
 
 	public String getCrsid() {
 		return crsid;
 	}
 
 	public void setCrsid(String crsid) {
 		this.crsid = crsid;
 	}
 
 	public List<Event> getEvents() {
 		return events;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Event> getDatetimeEvents() {
 		Session session = HibernateUtil.getInstance().getSession();
 		return (List<Event>) session.createCriteria(Event.class)
				.add(Restrictions.eq("sheetType", "datetime")).list();
 	}
 
 	public void addEvents(List<Event> events) {
 		this.events.addAll(events);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Row> getRowsWithDatetimeSignedUp() {
 		Session session = HibernateUtil.getInstance().getSession();
 		return session.createCriteria(Row.class).createAlias("event", "event")
 				.createAlias("slots", "slots")
 				.add(Restrictions.eq("slots.owner", this))
 				.add(Restrictions.eq("event.sheetType", "datetime")).list();
 	}
 
 	@SuppressWarnings("unchecked")
 	public ImmutableMappableExhaustedPair<Row> getRowsSignedUp(int page,
 			String mode) {
 		// Process page into offset
 		int offset = page * 10;
 
 		// Query rows that the user has signed up
 		Session session = HibernateUtil.getInstance().getSession();
 		Date now = new Date();
 		Criteria q = session.createCriteria(Row.class)
 				.createAlias("slots", "slots").createAlias("event", "event")
 				.add(Restrictions.eq("slots.owner", this));
 		if (mode.equals("contemporary")) {
 			q = q.add(Restrictions.gt("time", now)).addOrder(
 					Order.asc("time"));
 		} else if (mode.equals("archive")) {
 			q = q.add(
 					Restrictions.or(Restrictions.le("time", now),
 							Restrictions.and(Restrictions.eq("event.sheetType",
 									"manual"), Restrictions.le(
 									"event.expiryDate", now)))).addOrder(
 					Order.desc("event.expiryDate"));
 		} else if (mode.equals("no-time")) {
 			q = q.add(
 					Restrictions.and(
 							Restrictions.eq("event.sheetType", "manual"),
 							Restrictions.gt("event.expiryDate", now)))
 					.addOrder(Order.desc("id"));
 		} else if (mode.equals("timed")) {
 			q = q.add(Restrictions.eq("sheetType", "datetime")).addOrder(
 					Order.desc("time"));
 		} else if (mode.equals("dos")) {
 			q.add(Restrictions.eq("event.dosVisibility", true)).addOrder(
 					Order.desc("event.expiryDate"));
 		}
 
 		// Check if the row list is exhausted
 		List<Row> rows = (List<Row>) q.setMaxResults(10).setFirstResult(offset)
 				.list();
 
 		Boolean exhausted = false;
 		if (rows.size() % 10 != 0) {
 			exhausted = true;
 		} else if (q.setFirstResult(offset + 10).setMaxResults(1).list().size() == 0) {
 			exhausted = true;
 		}
 
 		return new ImmutableMappableExhaustedPair<Row>(rows, exhausted);
 	}
 
 	@SuppressWarnings("unchecked")
 	public ImmutableMappableExhaustedPair<Event> getMyEvents(int page) {
 		// Process page into offset
 		int offset = page * 10;
 
 		// Query events the user has created
 		Session session = HibernateUtil.getInstance().getSession();
 		Query q = session
 				.createQuery(
 						"from Event as event where event.owner = :user order by id desc")
 				.setParameter("user", this).setFirstResult(offset)
 				.setMaxResults(10);
 
 		// Check if the events list is exhausted
 		Boolean exhausted = false;
 		if (session
 				.createQuery("from Event as event where event.owner = :user")
 				.setParameter("user", this).setFirstResult(offset + 10)
 				.setMaxResults(1).list().isEmpty())
 			exhausted = true;
 
 		return new ImmutableMappableExhaustedPair<Event>(
 				(List<Event>) q.list(), exhausted);
 	}
 
 	public Set<Slot> getSlots() {
 		return slots;
 	}
 
 	public void addSlots(Set<Slot> slots) {
 		this.slots.addAll(slots);
 	}
 	
 	// Register user from CRSID
 	public static User registerUser(String crsid) {
 		// Add user to database if necessary
 		// Begin hibernate session
 		Session session = HibernateUtil.getInstance().getSession();
 
 		// Does the user already exist?
 		Query userQuery = session.createQuery("from User where id = :id")
 				.setParameter("id", crsid);
 		User user = (User) userQuery.uniqueResult();
 
 		// If no, check if they exist in LDAP and create them if so
 		if (user == null) {
 			List<String> instIDs = null;
 			String instID = null;
 			try {
 				instIDs = LDAPQueryManager.getUser(crsid).getInstID();
 				for (String instIDTemp : instIDs) {
 					if (instIDTemp.endsWith("UG")) {
 						instID = instIDTemp;
 					}
 				}
 			} catch (LDAPObjectNotFoundException e) {
 				return null;
 			}
 
 			User newUser = new User(crsid, instID);
 			session.save(newUser);
 			return newUser;
 		}
 
 		return user;
 	}
 
 	// equals
 	@Override
 	public boolean equals(Object object) {
 		// check for self-comparison
 		if (this == object)
 			return true;
 
 		// check that the object is a user
 		if (!(object instanceof User))
 			return false;
 
 		// compare crsids
 		return (((User) object).getCrsid().equals(this.crsid));
 	}
 
 	public Map<String, ?> toMap() {
 		return ImmutableMap.of("crsid", crsid, "name", getName(), "anySlots",
 				getSlots().size() > 0,"collegename",getCollegeName());
 	}
 
 	@Override
 	public int getId() {
 		throw new UnsupportedOperationException();
 	}
 }
