 /**
  * 
  */
 package identity.calendar;
 
 import java.security.InvalidParameterException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author jaremy
  *
  */
 public class CalendarDBServer extends CalendarDB {
 	public ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CalendarEntry>> db;
     private boolean isServer = true;
     
 	/**
 	 * @param filename
 	 */
 	public CalendarDBServer(String filename) {
 		super();
 		
 		db = new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CalendarEntry>>(50);
 		
 		filename = "CALENDAR";
 		// read in entries
 		parseFile(filename);
 		
 
 		int delay = 7*1000; // delay for 5 sec.
 		int period = 49*1000; // repeatTimer timer = new Timer();
 		
 	    Timer timer = new Timer();
 		timer.scheduleAtFixedRate(new TimerTask()
 		{
 			public void run()
 			{
 				//method to write the objects from hashtable to a file
 				writeFile();
 			}
 		}, delay, period);
 
 		//CheckPointer checkpointer = new CheckPointer(this,60);
 		//new Thread(checkpointer).start();
 	}
 	
 	@Override
 	public boolean addEntry(CalendarEntry entry) {
 		UUID key = entry.uuid;
 		Integer keyid = entry.id;		
 		ConcurrentHashMap<Integer, CalendarEntry> userdb;
 		
 		if (entry == null || key == null || keyid == null )
 			return false;
 		// if the uuid doesn't exist in the database, create a new HashMap to add the entry
 		db.putIfAbsent(key, new ConcurrentHashMap<Integer, CalendarEntry>(20));
 		
 		userdb = db.get(key);
 		if ( !userdb.containsKey(keyid)) {
 			userdb.put(keyid, entry);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 
 	/**
 	 * This method wraps deleting objects from the HashMap in a generic fashion. Overwrite this 
 	 * to change HashMap key types for a server class.
 	 * @param delete this entry from the hashmap
 	 * @throws IllegalArgumentException
 	 */
 	@Override
 	public boolean delEntry(CalendarEntry entry) throws InvalidParameterException {
 		UUID uuid = entry.uuid;
 		Integer keyid = entry.id;
 		return delEntry(uuid, keyid);
 	}
 
 	@Override
 	public boolean delEntry(Integer id)
 	{
 		throw new IllegalArgumentException("delEntry for Integer IDs not supported for server code!");
 	}
 	
 	public boolean delEntry(UUID uuid, Integer keyid)
 	{
 		if (uuid == null || keyid == null ) 
 			throw new InvalidParameterException("cannot delete entry w");
 		ConcurrentHashMap<Integer, CalendarEntry> userdb;
		CalendarEntry tmp;
		userdb = db.get(uuid);
		if ( userdb == null )
			return false;
		else if ( (tmp = userdb.remove(keyid)) != null) {
			System.out.println("DEBUG: CalendarEntry tmp "+tmp);
 			return false;
 		}
		else 
			return true;
		
 	}
 	
 	@Override
 	public CalendarEntry getEntry(CalendarEntry entry) {
 		if (entry == null)
 			return null;
 		return getEntry(entry.uuid, entry.id);
 	}
 
 	public CalendarEntry getEntry(UUID uid, Integer key) {
 		if (key == null || uid == null)
 			return null;
 		return db.get(uid).get(key);
 	}
 	
 	/**
 	*  Returns a synchronizedList containing the UUID database.
 	* @return List synchronizedList containing the UUID database
 	 */
 	public ConcurrentHashMap<Integer, CalendarEntry> getHashUUID(UUID uid)
 	{
 		return db.get(uid);
 	}
 
 	
 	@Override
 	public CalendarEntry[] toArray()
 	{
 		//ConcurrentHashMap<Integer, CalendarEntry> all = new ConcurrentHashMap<Integer, CalendarEntry>(200);
 		ArrayList<CalendarEntry> all = new ArrayList<CalendarEntry>(200);
 		
 		for ( ConcurrentHashMap<Integer, CalendarEntry> hm : db.values() ) {
 			//System.err.println("debug toarray hm: "+hm);
 			all.addAll(hm.values());
 		}
 		return all.toArray(new CalendarEntry[0]);
 	}
 	
 
 	public static void main(String[] args) throws ParseException
 	{
 		//classpath /Users/jaremy/proj/ds/jaremy-vamsi/bin/
 		//classpath /Users/jaremy/proj/ds/jaremy-vamsi/bin/
 		String foodate = "4/11/2006 4:55pm";
 
 		int i = 0;
 		
 		CalendarEntry bar[] = new CalendarEntry[5]; 
 		
 		for(i=0;i<bar.length; ++i) {
 			bar[i] = new CalendarEntry( 
 					UUID.randomUUID(),
 					i*100, 
 					new Date(), 
 					(i%2==0) ? "public" : "private", 
 					"some thing", 
 					i*10 );
 			bar[i].privatizeDescr();
 			System.out.println("bar i:"+i+" :"+bar[i]);
 		}
 		
 		CalendarDBServer test = new CalendarDBServer("serveremptycalendar.scal");
 		System.out.println("created new CalendarDB");
 
 		System.out.println("dumping newly created files::");
 		for ( CalendarEntry entry : test.toArray() ) {
 			System.out.println("\tdump: "+entry);
 		}
 		System.out.println("old size: "+test.db.size());
 
 		
 		System.out.println("adding new entries: ");
 		for ( i = 0; i<2*bar.length; ++i ) {
 			System.out.print(" "+ test.addEntry(bar[i%bar.length]));
 		}
 		System.out.println(" :: new size: "+test.db.size() +"\n");
 		
 		test.writeFile();
 		
 		System.out.println("dumping files::");
 		for ( CalendarEntry entry : test.toArray() ) {
 			System.out.println("\tdump: "+entry);
 		}
 		
 		System.out.println("del: "+test.delEntry(bar[0]));
 		System.out.println("del: "+test.delEntry(bar[1]));
 		System.out.println("new size: "+test.db.size());
 
 		test.writeFile();
 		
 		for ( CalendarEntry entry: test.toArray()) {
 			System.out.println("toArray: "+entry);
 		}
 		
 		
 //		System.exit(0);
 	}
 
 	@Override
 	public boolean isServer() {
 		return isServer;
 	}
 
 	@Override
 	public void setServer(boolean isServer) {
 		this.isServer = isServer;
 	}
 
 
 }
