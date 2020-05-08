 package com.lordMap.datastore;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import sun.util.resources.CalendarData;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.lordMap.models.Inventory;
 import com.lordMap.models.Land;
 import com.lordMap.models.User;
 
 public class DataStore {
 
 	private DatastoreService datastore;
 	private final static double RADIUS = 1;
 	private final static double PRECISION = 0.0000001;
 	private final static double TASK1DIS = 0.0001;
 	private final static double TASK1MONEY = 5000;
 
 	public DataStore() {
 		datastore = DatastoreServiceFactory.getDatastoreService();
 	}
 	
 	//check whether userId exists
 	public boolean checkUserId(String userId) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		List<Entity> users = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		return !users.isEmpty();
 	}
 	
 	//check whether password is correct
 	public boolean checkUserPwd(String userId, String userPwd) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		List<Entity> users = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		Entity user = users.get(0);
 		String pwd = (String) user.getProperty("userPwd");
 		return pwd.equals(userPwd);
 	}
 	
 	// store user on google datastore
 	public void storeUser(User newUser) {
 		Key key = KeyFactory.createKey("user", "default");
 		Entity user = new Entity(newUser.getUserId(), key);
 		user.setProperty("userId", newUser.getUserId());
 		user.setProperty("userPwd", newUser.getUserPwd());
 		user.setProperty("atk", newUser.getAtk());
 		user.setProperty("money", newUser.getMoney());
 		datastore.put(user);		
 	}
 	
 	public void storeLand(Land newLand) {
 		Key key = KeyFactory.createKey("land", "default");
 		Entity land = new Entity(newLand.getOwner(), key);
 		land.setProperty("owner", newLand.getOwner());
 		land.setProperty("lat0", newLand.getLats()[0]);
 		land.setProperty("lat1", newLand.getLats()[1]);
 		land.setProperty("long0", newLand.getLongs()[0]);
 		land.setProperty("long1", newLand.getLongs()[1]);
 		//land.setProperty("price", newLand.getPrice());
 		//land.setProperty("defence", newLand.getDefence());
 		newLand.setId(findAllLands().size());
 		land.setProperty("id", newLand.getId());
 		newLand.setDefence(1000);
 		land.setProperty("defence", newLand.getDefence());
 		
 		datastore.put(land);
 
 	}
 	
 	//check whether two squares overlap
 	public boolean checkOverlap(double[] lats1, double[] longs1, double[] lats2, double[] longs2) {
 		if (lats1[0] > lats2[1] || lats1[1] < lats2[0] || longs1[0] < longs2[1] || longs1[	1] > longs2[0]) 
 			return false;
 		else
 			return true;
 	}
 	
 	//check whether a land overlap with all other lands buyed
 	public boolean checkOverlaps(double[] lats, double[] longs) {
 		ArrayList<Land> lands = findAllLands();
 		for (Land l : lands) {
 			if (checkOverlap(l.getLats(), l.getLongs(), lats, longs))
 				return true;
 		}
 		return false;
 	}
 	
 	// show userId's land
 	public ArrayList<Land> showLands(String userId) {
 		Key key = KeyFactory.createKey("land", "default");
 		ArrayList<Land> lands = new ArrayList<Land>();
 		Query query = new Query(userId, key);
 		List<Entity> ls = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity l : ls) {
 			Land nl = new Land();
 			nl.setOwner(userId);
			//nl.setId((Integer) l.getProperty("id"));
			//nl.setPrice((Integer) l.getProperty("price"));
			//nl.setDefence((Integer) l.getProperty("defence"));
 			double[] lats = new double[2];
 			double[] longs = new double[2];
 			lats[0] = (Double) l.getProperty("lat0");
 			lats[1] = (Double) l.getProperty("lat1");
 			longs[0] = (Double) l.getProperty("long0");
 			longs[1] = (Double) l.getProperty("long1");
 			nl.setLats(lats);
 			nl.setLongs(longs);
 			
 			lands.add(nl);
 		}
 		return lands;
 	}
 	
 	public ArrayList<Land> findAllLands() {
 		Key key = KeyFactory.createKey("land", "default");
 		ArrayList<Land> lands = new ArrayList<Land>();
 		Query query = new Query(key);
 		List<Entity> ls = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity l : ls) {
 			Land nl = new Land();
 			nl.setOwner((String) l.getProperty("owner"));
 			//nl.setId((Integer) l.getProperty("id"));
 			//nl.setPrice((Integer) l.getProperty("price"));
 			//nl.setDefence((Integer) l.getProperty("defence"));
 			double[] lats = new double[2];
 			double[] longs = new double[2];
 			lats[0] = (Double) l.getProperty("lat0");
 			lats[1] = (Double) l.getProperty("lat1");
 			longs[0] = (Double) l.getProperty("long0");
 			longs[1] = (Double) l.getProperty("long1");
 			nl.setLats(lats);
 			nl.setLongs(longs);
 			lands.add(nl);
 		}
 	    return lands;
 	}
 	
 	//find lands that are within radius or belongs to userId
 	public ArrayList<Land> findLands(String userId, double lat, double lng) {
 //		Key key = KeyFactory.createKey("land", "default");
 //		ArrayList<Land> lands = new ArrayList<Land>();
 //		Query query = new Query(key);
 //		List<Entity> ls = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 //		// Get the results and process them, casting to the required types 
 //	    for (Entity ent : ls) {
 //	    	double[] lats = new double[2];
 //	    	double[] lngs = new double[2];
 //	    	double[] center = new double[2];
 //	    	center[0] = lat;
 //	    	center[1] = lng;
 //	    	lats[0] = (Double)ent.getProperty("lat0");
 //	    	lngs[0] = (Double)ent.getProperty("long0");
 //	    	lats[1] = (Double)ent.getProperty("lat1");
 //	    	lngs[1] = (Double)ent.getProperty("long1");	    	
 //	    	if (rectAndCircleAreOverlap(lats, lngs, center)) {
 //	    		Land land = new Land();
 //	    		land.setLats(lats);
 //	    		land.setLongs(lngs);
 //	    		land.setOwner((String)ent.getProperty("userId"));
 //	    		land.setId((Integer) ent.getProperty("id"));
 //	    		land.setPrice((Integer) ent.getProperty("price"));
 //	    		land.setDefence((Integer) ent.getProperty("defence"));
 //	    		lands.add(land);
 //	    	}
 //	    	
 //	    }
 		ArrayList<Land> allLands = findAllLands();
 		ArrayList<Land> lands = new ArrayList<Land>();
 		for (Land land: allLands) {
 	    	double[] center = new double[2];
 	    	center[0] = lat;
 	    	center[1] = lng;
 	    	if (land.getOwner().equals(userId) || rectAndCircleAreOverlap(land.getLats(), land.getLongs(), center)) {
 	    		lands.add(land);
 	    	}
 //	    	lands.add(land);
 		}
 		
 	    return lands;
 	}
 	
 	private void initRect(double[] rectLats, double[] rectLngs, double[] lats, double[] lngs) {
 		rectLats[0] = lats[0];
 		rectLngs[0] = lngs[0];		
 		rectLats[1] = lats[1];
 		rectLngs[1] = lngs[1];
 		rectLats[2] = lats[1];
 		rectLngs[2] = lngs[0];
 		rectLats[3] = lats[0];
 		rectLngs[3] = lngs[1];
 	}
 	
 	private void initCircle(double[] circleLats, double[] circleLngs, double[] center) {
 		circleLats[0] = center[0];
 		circleLngs[0] = center[1] + RADIUS;
 		circleLats[1] = center[0] + RADIUS;
 		circleLngs[1] = center[1];
 		circleLats[2] = center[0];
 		circleLngs[2] = center[1] - RADIUS;
 		circleLats[3] = center[0] - RADIUS;
 		circleLngs[3] = center[1];
 	}
 	
 	private boolean rectAndCircleAreOverlap(double[] lats, double[] lngs, double[] center) {
 		double[] rectLats = new double[4];
 		double[] rectLngs = new double[4];
 		double[] circleLats = new double[4];
 		double[] circleLngs = new double[4];
 		
 		initRect(rectLats, rectLngs, lats, lngs);
 		initCircle(circleLats, circleLngs, center);
 		
 		if (cornerIsInsideCircle(rectLats, rectLngs, center) 
 				|| cornerIsInsideRect(circleLats, circleLngs, rectLats, rectLngs))
 			return true;
 		
 		return false;
 	}
 	
 	private boolean cornerIsInsideCircle(double[] rectLats, double[] rectLngs, double[] center) {
 		for (int i = 0; i < 4; i++) {
 			double[] point = new double[2];
 			point[0] = rectLats[i];
 			point[1] = rectLngs[i];
 			if (pointIsInsideCircle(point, center))
 				return true;
 		}
 		return false;
 	}
 	
 	private boolean cornerIsInsideRect(double[] circleLats, double[] circleLngs, double[] rectLats, double[] rectLngs) {
 		for (int i = 0; i < 4; i++) {
 			double[] point = new double[2];
 			point[0] = circleLats[i];
 			point[1] = circleLngs[i];
 			if (pointIsInsideRect(point, rectLats, rectLngs))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean pointIsInsideRect(double[] point, double[] rectLats, double[] rectLngs) {
 		if (point[0] - rectLats[0] > PRECISION
 				&& rectLats[1] - point[0] > PRECISION
 				&& point[1] - rectLngs[1] > PRECISION
 				&& rectLngs[0] - point[1] > PRECISION)
 			return true;
 		
 		return false;
 	}
 	
 	private boolean pointIsInsideCircle(double[] point, double[] center) {
 		double dis = (point[0] - center[0]) * (point[0] - center[0]) + (point[1] - center[1]) * (point[1] - center[1]);    
 		if (Math.sqrt(dis) - RADIUS < PRECISION)
 			return true;
 		
 		return false;
 	}
 	
 	//check whether request before
 	public boolean checkRequest(String userId1, String userId2) {
 		Key key = KeyFactory.createKey("request", "default");
 		Query query = new Query(userId1, key);
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity r : rs) {
 			String requester = (String) r.getProperty("requester");
 			if (requester.equals(userId2))
 				return true;
 		}
 		return false;
 	}
 	
 	//add the add friend reqeust to datastore
 	public void addRequest(String userId1, String userId2) {
 		Key key = KeyFactory.createKey("request", "default");
 		Entity request = new Entity(userId1, key);
 		request.setProperty("requester", userId2);
 		datastore.put(request);
 	}
 	
 	//add friend relationship to datastore
 	public void confirmRequest(String userId1, String userId2) {
 		Key key = KeyFactory.createKey("friend", "default");
 		//add to userId1's friend list
 		Entity friend1 = new Entity(userId1, key);
 		friend1.setProperty("friend", userId2);
 		datastore.put(friend1);
 		//add to userId2's friend list
 		Entity friend2 = new Entity(userId2, key);
 		friend2.setProperty("friend", userId1);
 		datastore.put(friend2);
 		//remove from userId1's request list
 		Key rkey = KeyFactory.createKey("request", "default");
 		Query query = new Query(userId1, rkey);
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity r : rs) {
 			String requester = (String) r.getProperty("requester");
 			if (requester.equals(userId2))
 				datastore.delete(r.getKey());
 		}
 	}
 	
 	//show all friend requests
 	public ArrayList<String> showRequests(String userId) {
 		Key key = KeyFactory.createKey("request", "default");
 		Query query = new Query(userId, key);
 		ArrayList<String> friends = new ArrayList<String>();
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity r : rs) {
 			String requester = (String) r.getProperty("requester");
 			friends.add(requester);
 		}
 		return friends;
 	}
 	
 	//show all friends of userId
 	public ArrayList<String> showFriends(String userId) {
 		Key key = KeyFactory.createKey("friend", "default");
 		Query query = new Query(userId, key);
 		List<Entity> rs = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		ArrayList<String> friends = new ArrayList<String>();
 		for (Entity ent : rs) {
 			friends.add((String)ent.getProperty("friend"));
 		}
 		
 		return friends;
 	}
 	
 	//check whether an item is affordable
 	public boolean isAffordable(String userId, int inventoryIndex) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		Entity user = datastore.prepare(query).asSingleEntity();
 		long balance = (Long)user.getProperty("money");
 		if (balance >= Inventory.price[inventoryIndex]) 
 			return true;
 		
 		return false;
 	}
 	
 	//purchase an item
 	public long purchaseItem(String userId, int inventoryIndex) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		Entity user = datastore.prepare(query).asSingleEntity();
 		long balance = (Long)user.getProperty("money");
 		balance -= Inventory.price[inventoryIndex];
 		user.setProperty("money", balance);
 		storeCommodity(userId, inventoryIndex);
 		datastore.put(user);
 		
 		return balance;
 	}
 	
 	//store Commodity
 	public void storeCommodity(String userId, int inventoryIndex) {
 		Key key = KeyFactory.createKey("commodity", "default");
 		Entity commodity = new Entity(userId, key);
 		commodity.setProperty("index", inventoryIndex);
 		datastore.put(commodity);
 	}
 
 	//check whether one date is one day after than another date
 	private boolean afterOneDay(Date date, Date lastAtk) {
 		if (date.getYear() < lastAtk.getYear())
 			return false;
 		else if (date.getYear() > lastAtk.getYear())
 			return true;
 		else if (date.getMonth() < lastAtk.getMonth() )
 			return false;
 		else if (date.getMonth() > lastAtk.getMonth())
 			return true;
 		else if (date.getDate() < lastAtk.getDate())
 			return false;
 		else if (date.getDate() > lastAtk.getDate())
 			return true;
 		else 
 			return false;
 	}
 	
 	//change owner of the land
 	private void changeOwner(String userId, long landId) {
 		Key lkey = KeyFactory.createKey("land", "default");
 		Query lquery = new Query(lkey);
 		List<Entity> ls = datastore.prepare(lquery).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity l : ls) {
 			long lId = (Long) l.getProperty("id");
 			if (lId == landId) {
 				Entity nl = new Entity(userId, lkey);
 				nl.setProperty("id", l.getProperty("id"));
 				nl.setProperty("owner", userId);
 				nl.setProperty("lat0", l.getProperty("lat0"));
 				nl.setProperty("lat1", l.getProperty("lat1"));
 				nl.setProperty("long0", l.getProperty("long0"));
 				nl.setProperty("long1", l.getProperty("long1"));
 				datastore.delete(l.getKey());
 				datastore.put(nl);
 				break;
 			}
 		}
 	}
 	
 	//check whether the attacker can win
 	private boolean canWin(String userId, long landId) {
 		Key ukey = KeyFactory.createKey("user", "default");
 		Key lkey = KeyFactory.createKey("land", "default");
 		Query uquery = new Query(userId, ukey);
 		List<Entity> us = datastore.prepare(uquery).asList(FetchOptions.Builder.withLimit(100));
 		Entity user = us.get(0);
 		long atk = (Long) user.getProperty("atk");
 		long defence = 0;
 		Query lquery = new Query(lkey);
 		List<Entity> ls = datastore.prepare(lquery).asList(FetchOptions.Builder.withLimit(100));
 		for (Entity l : ls) {
 			long lId = (Long) l.getProperty("id");
 			if (lId == landId) {
 				defence = (Long) l.getProperty("defence");
 				break;
 			}
 		}
 		if (atk > defence)
 			return true;
 		else
 			return false;
 	}
 		
 	//return true if succeed, false otherwise
 	public String attack(String userId, int landId) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		List<Entity> us = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
 		Entity user = us.get(0); 
 		//System.out.println(user.getProperty("userId"));
 		Date date = new Date();
 		Date lastAtk = (Date) user.getProperty("lastAtk");
 		String result = null;
 		if (lastAtk == null || afterOneDay(date, lastAtk)) {
 			user.setProperty("lastAtk", date);
 			datastore.put(user);
 			if (canWin(userId, landId)) {
 				changeOwner(userId, landId);
 				result = "succeeded";
 			}
 			else {
 				result = "failed";
 			}
 		}
 		else
 			result = "not yet";
 		return result;
 	}
 	
 	// get a user's personal info
 	public User getUserInfo(String userId) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		Entity ent = datastore.prepare(query).asSingleEntity();
 		User user = new User();
 		user.setUserId(userId);
 		user.setAtk((Long)ent.getProperty("atk"));
 		user.setMoney((Long)ent.getProperty("money"));
 			
 		return user;
 	}
 	
 	//store task1 info, return true if a brand new task is started, 
 	//false if the old task was given up and a new one is started
 	public boolean storeTask1Info(String userId, double lat, double lng) {
 		Key key = KeyFactory.createKey("task1", "default");
 		Entity task1 = isTask1Started(userId);
 		boolean flag = false;
 		if (task1 == null) { 
 			task1 = new Entity(userId, key);
 			flag = true;
 		}
 		task1.setProperty("startLat", lat);
 		task1.setProperty("startLng", lng);
 		datastore.put(task1);
 		
 		return flag;
 	}
 	
 	//check whether task1 has started, return null if task1 has not been started
 	public Entity isTask1Started(String userId) {
 		Key key = KeyFactory.createKey("task1", "default");
 		Query query = new Query(userId, key);
 		Entity task1 = datastore.prepare(query).asSingleEntity();
 		return task1;
 	}
 	
 	//check whether task1 has finished
 	public boolean checkTask1Status(String userId, double lat, double lng) {
 		Key key = KeyFactory.createKey("task1", "default");
 		Query query = new Query(userId, key);
 		Entity ent = datastore.prepare(query).asSingleEntity();
 		double startLat = (Double)ent.getProperty("startLat");
 		double startLng = (Double)ent.getProperty("startLng");
 		double dis = (lat - startLat) * (lat - startLat) + (lng - startLng) * (lng - startLng);
 		dis = Math.sqrt(dis);
 		
 		if (dis - TASK1DIS > PRECISION) {
 			datastore.delete(ent.getKey());
 			increaseMoney(userId);
 			return true;
 		}
 		return false;
 	}
 	
 	private void increaseMoney(String userId) {
 		Key key = KeyFactory.createKey("user", "default");
 		Query query = new Query(userId, key);
 		Entity user = datastore.prepare(query).asSingleEntity();
 		long money = (Long)user.getProperty("money");
 		money += TASK1MONEY;
 		user.setProperty("money", money);
 		datastore.put(user);
 	}
 }
