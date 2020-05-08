 package ch.ubx.startlist.server;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import ch.ubx.startlist.shared.FeDate;
 import ch.ubx.startlist.shared.FeFlightEntry;
 import ch.ubx.startlist.shared.FeNode;
 import ch.ubx.startlist.shared.FePlace;
 import ch.ubx.startlist.shared.FeStore;
 import ch.ubx.startlist.shared.FeYear;
 import ch.ubx.startlist.shared.LoginInfo;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.utils.SystemProperty;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 import com.googlecode.objectify.util.DAOBase;
 
 public class FlightEntryDAOobjectify2 extends DAOBase implements FlightEntryDAO2 {
 
 	static {
 		try { // TODO -- eliminate duplicate register error
 			ObjectifyService.register(FeFlightEntry.class);
 		} catch (Exception e) {
 		}
 		try { // TODO -- eliminate duplicate register error
 			ObjectifyService.register(LoginInfo.class);
 		} catch (Exception e) {
 		}
 
 	}
 	static boolean test;
 
 	private static final String PARENT = "parents";
 
 	private LoginInfo loginInfo = null;
 	private UserService userService;
 	private User adminUser;
 
 	private final IFeGenDAO<FeStore, String> storeDAO = new FeGenDAOobjectify<FeStore, String>(FeStore.class);
 	private final IFeGenDAO<FeDate, Long> dateDAO = new FeGenDAOobjectify<FeDate, Long>(FeDate.class);
 	private final IFeGenDAO<FeYear, Long> yearDAO = new FeGenDAOobjectify<FeYear, Long>(FeYear.class);
 	private final IFeGenDAO<FePlace, String> placeDAO = new FeGenDAOobjectify<FePlace, String>(FePlace.class);
 	private final IFeGenDAO<FeNode<?>, ?> gDAO = new FeGenDAOobjectify(FeNode.class);
 	private static Key<FeStore> storeActiveKey;
 
 	public FlightEntryDAOobjectify2() {
 		FlightEntryDAOobjectify2.test = false;
 		storeActiveKey = storeDAO.getOrCreateKey("Active");
 		userService = UserServiceFactory.getUserService();
 		adminUser = new User("admin.cron@" + SystemProperty.applicationId.get() + ".appspotmail.com", "gmail.com");
 	}
 
 	public FlightEntryDAOobjectify2(boolean test) {
 		this();
 		FlightEntryDAOobjectify2.test = test;
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry() {
 		return ofy().query(FeFlightEntry.class).list();
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(FeDate date, int startIndex, int maxCount) {
 		Query<FeFlightEntry> query = ofy().query(FeFlightEntry.class).filter(PARENT, date);
 		List<FeFlightEntry> list = query.list();
 		// TODO - use Query methods limit/ offset
 		list = list.subList(startIndex, Math.min(startIndex + maxCount, list.size()));
 		for (FeFlightEntry flightEntry : list) {
 			doPostLoad(flightEntry);
 		}
 		return list;
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(FeDate date) {
 		return listflightEntry(date, 0, 100000);
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(int yearInt) {
 		List<FeFlightEntry> fes = new ArrayList<FeFlightEntry>();
 		for (FePlace fePlace : listAirfield(yearInt)) {
 			for (FeDate feDate : listDate(fePlace)) {
 				fes.addAll(listflightEntry(feDate));
 			}
 		}
 		return fes;
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(int startYearInt, int endYearInt, String placeStr) {
 		List<FeFlightEntry> fes = new ArrayList<FeFlightEntry>();
 		if (startYearInt <= endYearInt) {
 			List<FePlace> ys = new ArrayList<FePlace>();
 			for (int y = startYearInt; y <= endYearInt; y++) {
 				ys.addAll(listAirfield(y));
 			}
 			List<FeDate> ds = new ArrayList<FeDate>();
 			for (FePlace fePlace : ys) {
 				if (fePlace.getValue().equals(placeStr)) {
 					ds.addAll(listDate(fePlace));
 				}
 			}
 			for (FeDate feDate : ds) {
 				fes.addAll(listflightEntry(feDate));
 			}
 		}
 		return fes;
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(long startDateLong, long endDateLong, String placeStr) {
 		List<FeFlightEntry> fes = new ArrayList<FeFlightEntry>();
 		if (startDateLong <= endDateLong) {
 			List<FeDate> ds = new ArrayList<FeDate>();
 			for (FePlace fePlace : placeDAO.list()) {
 				if (fePlace.getValue().equals(placeStr)) {
 					ds.addAll(dateDAO.list(startDateLong, endDateLong, fePlace));
 				}
 			}
 			for (FeDate feDate : ds) {
 				fes.addAll(listflightEntry(feDate));
 			}
 		}
 		return fes;
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(int yearInt, int month, int day, String placeStr) {
 		List<FeFlightEntry> fes = new ArrayList<FeFlightEntry>();
 		Calendar date = Calendar.getInstance();
 		date.setTimeInMillis(0);
 		date.set(Calendar.YEAR, yearInt);
 		date.set(Calendar.MONTH, month);
 		date.set(Calendar.DAY_OF_MONTH, day);
 		List<FeDate> ds = new ArrayList<FeDate>();
 		for (FePlace fePlace : listAirfield(yearInt)) {
 			if (fePlace.getValue().equals(placeStr)) {
 				ds.addAll(listDate(fePlace));
 			}
 		}
 		for (FeDate feDate : ds) {
 			if (feDate.getValue() == date.getTimeInMillis()) {
 				fes.addAll(listflightEntry(feDate));
 			}
 		}
 		return fes;
 	}
 
 	@Override
 	public FeFlightEntry removeFlightEntry(FeFlightEntry flightEntry) {
 		Key<?> dk = flightEntry.getParent();
 		ofy().delete(flightEntry);
 		Query<FeFlightEntry> query = ofy().query(FeFlightEntry.class).filter(PARENT, dk);
 		if (query.list().size() == 0) {
 			remove(dk);
 		}
 		return flightEntry;
 	}
 
 	private void remove(Key<?> key) {
 		Key<?> pkey = gDAO.getG(key).getParent();
 		if (pkey != null) {
 			gDAO.deleteG(key);
 			if (gDAO.listG(pkey).size() == 0) {
 				remove(pkey);
 			}
 		}
 	}
 
 	@Override
 	public FeFlightEntry createOrUpdateFlightEntry(FeFlightEntry flightEntry) {
 		if (flightEntry.getParent() != null) {
 			removeFlightEntry(flightEntry);
 		}
 		doPrePersist(flightEntry);
 		Key<FeYear> yearKey = yearDAO.getOrCreateKey(evalYear(flightEntry), storeActiveKey);
 		Key<FePlace> placeKey = placeDAO.getOrCreateKey(evalPlace(flightEntry), yearKey);
 		Key<FeDate> dateKey = dateDAO.getOrCreateKey(evalDate(flightEntry), placeKey);
 		flightEntry.setParent(dateKey);
 		ofy().put(flightEntry);
 		return flightEntry;
 	}
 
 	@Override
 	public List<FeDate> listDate(FePlace place) {
 		return dateDAO.list(place);
 	}
 
 	@Override
 	public List<FeDate> listDate(String placeStr, int yearInt) {
 		List<FeDate> ds = new ArrayList<FeDate>();
 		for (FePlace fePlace : listAirfield(yearDAO.get(new Long(yearInt), storeActiveKey))) {
 			ds.addAll(listDate(fePlace));
 		}
 		return ds;
 	}
 
 	@Override
 	public List<FePlace> listAirfield(FeYear year) {
 		return placeDAO.list(year);
 	}
 
 	@Override
 	public List<FePlace> listAirfield(int yearInt) {
 		return listAirfield(yearDAO.get(new Long(yearInt), storeActiveKey));
 	}
 
 	@Override
 	public List<FeYear> listYear() {
 		return yearDAO.list(storeActiveKey);
 	}
 
 	@Override
 	public void addFlightEntries(List<FeFlightEntry> flightEntries) {
 		if (flightEntries.size() > 0) {
 			for (FeFlightEntry flightEntry : flightEntries) {
 				createOrUpdateFlightEntry(flightEntry);
 			}
 		}
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(Calendar date, String place) {
 		Calendar dateStart = Calendar.getInstance(); // TODO - set timezone UTC?
 		dateStart.setTimeInMillis(date.getTimeInMillis());
 		dateStart.set(Calendar.HOUR_OF_DAY, 0);
 		dateStart.set(Calendar.MINUTE, 0);
 		dateStart.set(Calendar.SECOND, 0);
 		dateStart.set(Calendar.MILLISECOND, 0);
 		Calendar dateEnd = Calendar.getInstance(); // TODO - set timezone UTC?
 		dateEnd.setTimeInMillis(dateStart.getTimeInMillis());
 		dateEnd.add(Calendar.DAY_OF_MONTH, 1);
 
 		Query<FeFlightEntry> query = ofy().query(FeFlightEntry.class).filter("place ==", place).filter("startTimeInMillis >=", dateStart.getTimeInMillis())
 				.filter("startTimeInMillis <", dateEnd.getTimeInMillis()).order("startTimeInMillis");
 		return query.list();
 	}
 
 	@Override
 	public List<FeFlightEntry> listflightEntry(Calendar startDate, Calendar endDate, String place) {
 		Calendar dateStart = Calendar.getInstance(); // TODO - set timezone UTC?
 		dateStart.setTimeInMillis(startDate.getTimeInMillis());
 		dateStart.set(Calendar.HOUR_OF_DAY, 0);
 		dateStart.set(Calendar.MINUTE, 0);
 		dateStart.set(Calendar.SECOND, 0);
 		dateStart.set(Calendar.MILLISECOND, 0);
 
 		Calendar dateEnd = Calendar.getInstance(); // TODO - set timezone UTC?
 		dateEnd.setTimeInMillis(endDate.getTimeInMillis());
 		Query<FeFlightEntry> query = ofy().query(FeFlightEntry.class).filter("place", place).filter("startTimeInMillis >=", dateStart.getTimeInMillis())
 				.filter("startTimeInMillis <=", dateEnd.getTimeInMillis());
 		return query.list();
 	}
 
 	/*
 	 * NOTE: this method should be used only for testing!
 	 */
 	@Override
 	public void addFlightEntries4Test(List<FeFlightEntry> flightEntries) {
 		for (FeFlightEntry flightEntry : flightEntries) {
 			createOrUpdateFlightEntry(flightEntry);
 		}
 	}
 
 	// --------------------------------------------------------------------------------------
 	// private methods
 
 	private void doPostLoad(FeFlightEntry flightEntry) {
 		if (userService.isUserLoggedIn()) {
 			boolean deletable = false;
 			boolean modifiable = false;
 			if (userService.isUserAdmin()) {
 				deletable = true;
 				modifiable = true;
 			} else {
 				if (loginInfo == null) {
 					loginInfo = ofy().find(LoginInfo.class, userService.getCurrentUser().getEmail());
 				}
 				if (loginInfo != null) {
 					modifiable = loginInfo.isCanModFlightEntry() || userService.getCurrentUser().getEmail().equals(flightEntry.getCreator());
 					deletable = loginInfo.isCanDelFlightEntry() || userService.getCurrentUser().getEmail().equals(flightEntry.getCreator());
 				}
 			}
 			flightEntry.setModifiable(modifiable);
 			flightEntry.setDeletable(deletable);
 		}
 	}
 
 	private void doPrePersist(FeFlightEntry flightEntry) {
 		User currentUser = userService.getCurrentUser();
 		// If we are called from a cron job, we need to create one!
 		// NOTE: for security reasons this should be done only if we running in a cron job!
 		// TODO - find a way to implement
 		if (currentUser == null) {
 			currentUser = adminUser;
 		}
 		flightEntry.setModifier(currentUser.getEmail());
 		if (!test) {
 			flightEntry.setModified(System.currentTimeMillis());
 		}
 		if (flightEntry.getId() == null) {
 			flightEntry.setCreator(flightEntry.getModifier());
 			flightEntry.setCreated(flightEntry.getModified());
 		}
 	}
 
 	private Long evalYear(FeFlightEntry flightEntry) {
 		Calendar date = Calendar.getInstance();
 		date.setTimeInMillis(flightEntry.getStartTimeInMillis());
 		return new Long(date.get(Calendar.YEAR));
 	}
 
 	private Long evalDate(FeFlightEntry flightEntry) {
 		Calendar date = Calendar.getInstance();
 		date.setTimeInMillis(flightEntry.getStartTimeInMillis());
 		date.set(Calendar.HOUR_OF_DAY, 0);
 		date.set(Calendar.MINUTE, 0);
 		date.set(Calendar.SECOND, 0);
 		date.set(Calendar.MILLISECOND, 0);
 		return new Long(date.getTimeInMillis());
 	}
 
 	private String evalPlace(FeFlightEntry flightEntry) {
 		return flightEntry.getPlace();
 	}
 
 }
