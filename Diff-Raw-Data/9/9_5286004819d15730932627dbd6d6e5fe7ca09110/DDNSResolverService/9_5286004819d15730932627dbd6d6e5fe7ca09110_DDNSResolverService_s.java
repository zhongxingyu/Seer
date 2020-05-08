 package edu.uw.cs.cse461.Net.DDNS;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.HTTP.HTTPProviderInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.ARecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.SOARecord;
 import edu.uw.cs.cse461.Net.RPC.RPCCall;
 import edu.uw.cs.cse461.Net.RPC.RPCServiceInterface;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.IPFinder;
 
 
 public class DDNSResolverService extends NetLoadableService implements HTTPProviderInterface, DDNSResolverServiceInterface {
 	private static String TAG="DDNSResolverService";
 	
 	private String myIP;						/* the IP of the machine currently running this resolver */
 	private String myName;						/* name of the machine currently running this resolver */
 	private String password;					/* the password to use when registering/unregistering names */
 	private int maxResolveAttempts; 			/* the max number of rpc calls to make in attempt to resolve a name (to prevent loops) */
 	
 	private String rootServerIP;				/* the IP of the root server to contact when resolving names */
 	private int rootPort;						/* the port of the root server to contact when resolving names */
 	
 	boolean hasCaching;							/* whether or not caching has been turned on */
 	int cacheTimeout;							/* how long to wait before booting elements out of the cache */
 	private Map<String, CacheRecord> cache;		/* a cache of resolved names -> IP/ports, an invalid/unregistered name maps to NULL */
 	
 	private Timer timer;						/* timer for scheduling events */
 
 	public DDNSResolverService() throws DDNSException {
 		super("ddnsresolver", true);
 		
 		// set "my" IP 
 		myIP = IPFinder.getMyIP();
 		
 		// set up root server ip and port
 		ConfigManager config = NetBase.theNetBase().config();
 		rootServerIP = config.getProperty("ddns.rootserver");
 		if (rootServerIP == null) {
 			throw new DDNSException("No ddns.rootserver entry in config file.");
 		}
 		String rport = config.getProperty("ddns.rootport");
 		if (rport == null) {
 			throw new DDNSException("No ddns.rootport entry in config file.");
 		}
 		rootPort = Integer.parseInt(rport);
 		
 		// set up caching
 		String cachettl = config.getProperty("ddnsresolver.cachettl");
 		if (cachettl == null) {
 			throw new DDNSException("No ddns.cachettl entry in config file.");
 		}
 		cacheTimeout = Integer.parseInt(cachettl);
 		if (cacheTimeout > 0) {
 			hasCaching = true;
 		}
 		cache = new HashMap<String, CacheRecord>();
 		
 		// setup password
 		password = config.getProperty("ddnsresolver.password");
 		if (password == null) {
 			throw new DDNSException("No ddnsresolver.password entry in config file.");
 		}
 
 		// maximum # of RPC calls to make 
 		String max = config.getProperty("ddnsresolver.serverttl");
 		if (max == null) {
 			throw new DDNSException("No ddnsresolver.serverttl entry in config file.");
 		}
 		maxResolveAttempts = Integer.parseInt(max);
 		
 		// for scheduling new tasks
 		timer = new Timer();
 		
 		// finally - REGISTER OURSELVES
 		int port = Integer.parseInt(config.getProperty("rpc.serverport"));
 		myName = config.getProperty("net.hostname");
 		try {
 			register(new DDNSFullName(myName), ((RPCServiceInterface)(NetBase.theNetBase().getService("rpc"))).localPort());
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Called to end execution.  Specifically, need to terminate any threads we've created.
 	 */
 	@Override
 	public void shutdown() {
 		// stop any scheduled TIMER events
 		timer.cancel();
 		
 		// UNregister ourselves
 		try {
 			unregister(new DDNSFullName(myName));
 		} catch (DDNSException e) {
 			// do nothing	
 		} catch (JSONException e) {
 			// do nothing
 		}
 		super.shutdown();
 	}
 		
 	/**
 	 * Serves web pages.  The 0th element of uriArray is always null.
 	 * The next element names this service ("ddnsresolver").  The optional third
 	 * component is a name to be resolved before dumping the cache.
 	 */
 	@Override
 	public String httpServe(String[] uriArray) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Host:  ").append(NetBase.theNetBase().hostname()).append("\n");
 		if ( uriArray.length > 2 ) {
 			sb.append("Resolving: ").append(uriArray[2]).append("\n");
 			// third component
 			
 			//  resolve uriArray[2] to an address and append the address to the stringbuilder
 			// ....
 			sb.append("You haven't updated DDNSResolverServer.httpServer()");
 		}
 		// add any additional information you want here
 		return sb.toString();
 	}
 
 	/**
 	 * Unregisters a name.  
 	 * @param name
 	 * @throws DDNSException
 	 */
 	@Override
 	public void unregister(DDNSFullNameInterface name) throws DDNSException, JSONException {
 		// record in cache that there is no address associated with this name anymore
		if (cache.containsKey(name.toString())) {
			cache.get(name.toString()).cancelRegistration();
 		}
 		cachePutLocal(name.toString(), new CacheRecord(new DDNSException.DDNSNoAddressException(name)));
 		
 		// create JSON object to send to RPC
 		JSONObject unregisterObj = new JSONObject();
 		try {
 			unregisterObj.put("name", name.toString());
 			unregisterObj.put("password", password);
 
 			// send to RPC		
 			JSONObject response = resolverHelper(name.toString(), "unregister", rootServerIP, rootPort, unregisterObj, 0);
 			
 			// if the response was an error, then process that
 			if (response.get("resulttype").equals("ddnsexception")) {
 				int exceptiontype = response.getInt("exceptionnum");
 				switch(exceptiontype) {
 				case 1: 
 					// record as invalid name in cache
 					CacheRecord badRecord = new CacheRecord(new DDNSException.DDNSNoSuchNameException(name));
 					cachePutLocal(name.toString(), badRecord);
 					throw new DDNSException.DDNSNoSuchNameException(name);
 				case 2: 
 					// already recorded above
 					throw new DDNSException.DDNSNoAddressException(name);
 				case 3:
 					throw new DDNSException.DDNSAuthorizationException(name);
 				case 4: 
 					long start = System.currentTimeMillis();
 					long end = start + 10000;	// ten seconds
 					int sleeptime = 5;
 					while (System.currentTimeMillis() < end && response.get("resulttype").equals("ddnsexception")) {
 						// sleep for x ms
 						try {
 							Thread.sleep(sleeptime);
 						} catch (InterruptedException e) {
 							// do nothing
 						}
 						
 						// try again				
 						response = resolverHelper(name.toString(), "unregister", rootServerIP, rootPort, unregisterObj, 0);
 						sleeptime *= 2;	// backoff
 					}
 					if (response.get("resulttype").equals("ddnsexception")) { // still an error
 						throw new DDNSException.DDNSRuntimeException(response.getString("message"));
 					}
 					break;
 				case 5: 
 					throw new DDNSException.DDNSTTLExpiredException(name);
 				case 6:
 					throw new DDNSException.DDNSZoneException(name, new DDNSFullName(response.getString("zone")));
 				default:
 					break;				
 				}
 				throw new DDNSException(response.getString("message"));
 			} 
 			// else everything is A-OK and we are done
 		} catch (JSONException e) {
 			throw new DDNSException("unregister encountered a JSON exception: " + e.getMessage());
 		} catch (IOException e) {
 			throw new DDNSException("unregister encountered an IO exception: " + e.getMessage());
 		} 
 	}
 	
 	/**
 	 * Registers a name as being on this host (IP) at the given port.
 	 * If the name already exists, update its address mapping.  If it doesn't exist, create it (as an ARecord).
 	 * @param name
 	 * @param ip
 	 * @param port
 	 * @throws DDNSException
 	 */
 	@Override
 	public void register(DDNSFullNameInterface name, int port) throws DDNSException {
 		// want to record in cache even if it fails
 		ARecord record = new ARecord(myIP, port);
		//TODO: make sure extra timer thread isn't created if already registered
 		CacheRecord cacherecord = new CacheRecord(record, new RegisterTask(name, port), true);
 		cachePutLocal(name.toString(), cacherecord);
 		
 		// create JSON object to send to RPC
 		JSONObject registerObj = new JSONObject();
 		try {
 			registerObj.put("name", name.toString());
 			registerObj.put("ip", myIP);
 			registerObj.put("port", port);
 			registerObj.put("password", password);
 			
 			// send to RPC		
 			JSONObject response = resolverHelper(name.toString(), "register", rootServerIP, rootPort, registerObj, 0);
 			
 			// process response
 			if (response.get("resulttype").equals("ddnsexception")) {	// FAILURE
 				int exceptiontype = response.getInt("exceptionnum");
 				switch(exceptiontype) {
 				case 1:
 					CacheRecord badrecord1 = new CacheRecord(new DDNSException.DDNSNoSuchNameException(name));
 					cachePutLocal(name.toString(), badrecord1);
 					throw new DDNSException.DDNSNoSuchNameException(name);
 				case 2: 
 					// should never get this kind of error as a response to register
 					CacheRecord badrecord2 = new CacheRecord(new DDNSException.DDNSNoAddressException(name));
 					cachePutLocal(name.toString(), badrecord2);
 					throw new DDNSException.DDNSNoAddressException(name);
 				case 3:
 					throw new DDNSException.DDNSAuthorizationException(name);
 				case 4: 
 					long start = System.currentTimeMillis();
 					long end = start + 10000;	// ten seconds
 					int sleeptime = 5;
 					while (System.currentTimeMillis() < end && response.get("resulttype").equals("ddnsexception")) {
 						// sleep for sleeptime ms
 						try {
 							Thread.sleep(sleeptime);
 						} catch (InterruptedException e) {
 							// do nothing
 						}
 						
 						// try again				
 						response = resolverHelper(name.toString(), "register", rootServerIP, rootPort, registerObj, 0);
 						sleeptime *= 2;	// backoff
 					}
 					if (response.get("resulttype").equals("ddnsexception")) { // still an error
 						throw new DDNSException.DDNSRuntimeException(response.getString("message"));
 					}
 					break;
 				case 5: 
 					throw new DDNSException.DDNSTTLExpiredException(name);
 				case 6:
 					throw new DDNSException.DDNSZoneException(name, new DDNSFullName(response.getString("zone")));
 				default:
 					throw new DDNSException(response.getString("message"));				
 				}
 			} else {													// SUCCESS
 				int timeToLive = 1000*response.getInt("lifetime");		// given in seconds, convert to ms
 				cacherecord.scheduleRegistration(Math.max((90*timeToLive/100),500));	// only wait 90% of timetolive before trying again (or half a second if ttl=0)
 			}
 		} catch (JSONException e) {
 			throw new DDNSException("register encountered a JSON exception: " + e.getMessage());
 		} catch (IOException e) {
 			throw new DDNSException("register encountered an IO exception: " + e.getMessage());
 		} 
 	}
 	
 	/**
 	 * Resolves a name to an ARecord containing an address.  Throws an exception if no ARecord w/ address can be found.
 	 * @param name
 	 * @return The ARecord for the name, if one is found
 	 * @throws DDNSException
 	 */
 	@Override
 	public ARecord resolve(String nameStr) throws DDNSException, JSONException {
 		// check cache first
 		synchronized (cache) {
 			if (cache.containsKey(nameStr)) {
 				//System.out.println("found in cache!");
 				return cache.get(nameStr).getRecord();
 			}
 		}
 		
 		try {
 			// create JSON object to send to RPC
 			JSONObject resolveObj = new JSONObject();
 			resolveObj.put("name", nameStr);
 			JSONObject response = resolverHelper(nameStr, "resolve", rootServerIP, rootPort, resolveObj, 0);
 
 			if (response.getString("resulttype").equals("ddnsexception")) {	// FAILURE
 				int exceptiontype = response.getInt("exceptionnum");
 				switch(exceptiontype) {
 				case 1:
 					// record invalid name in cache
 					CacheRecord badrecord1 = new CacheRecord(new DDNSException.DDNSNoSuchNameException(new DDNSFullName(nameStr)), new CacheTask(nameStr));
 					cachePutGlobal(nameStr, badrecord1);
 					throw new DDNSException.DDNSNoSuchNameException(new DDNSFullName(nameStr));
 				case 2: 
 					// record no address in cache
 					CacheRecord badrecord2 = new CacheRecord(new DDNSException.DDNSNoAddressException(new DDNSFullName(nameStr)), new CacheTask(nameStr));
 					cachePutGlobal(nameStr, badrecord2);
 					throw new DDNSException.DDNSNoAddressException(new DDNSFullName(nameStr));
 				case 3:
 					throw new DDNSException.DDNSAuthorizationException(new DDNSFullName(nameStr));
 				case 4: 
 					long start = System.currentTimeMillis();
 					long end = start + 10000;	// ten seconds
 					int sleeptime = 5;
 					while (System.currentTimeMillis() < end && response.get("resulttype").equals("ddnsexception")) {
 						// sleep for sleeptime ms
 						try {
 							Thread.sleep(sleeptime);
 						} catch (InterruptedException e) {
 							// do nothing
 						}
 						
 						// try again				
 						response = resolverHelper(nameStr, "resolve", rootServerIP, rootPort, resolveObj, 0);
 						sleeptime *= 2;	// backoff
 					}
 					if (response.get("resulttype").equals("ddnsexception")) { 	// still an error
 						throw new DDNSException.DDNSRuntimeException(response.getString("message"));
 						
 					} else {													// success finally
 						JSONObject node = response.getJSONObject("node");
 						// either A or SOA
 						if (node.getString("type").equals("SOA")) {
 							SOARecord result = (SOARecord) DDNSRRecord.unmarshall(node);
 							return result;
 						}
 						// if wasn't SOA return A 
 						ARecord result = (ARecord) DDNSRRecord.unmarshall(node);
 						return result;
 					}
 				case 5: 
 					throw new DDNSException.DDNSTTLExpiredException(new DDNSFullName(nameStr));
 				case 6:
 					throw new DDNSException.DDNSZoneException(new DDNSFullName(nameStr), new DDNSFullName(response.getString("zone")));
 				default:
 					throw new DDNSException(response.getString("message"));
 				}
 			} else {  														// SUCCESS
 				JSONObject node = response.getJSONObject("node");
 				// either A or SOA
 				if (node.getString("type").equals("SOA")) {
 					SOARecord result = (SOARecord) DDNSRRecord.unmarshall(node);
 					return result;
 				}
 				// if wasn't SOA return A 
 				ARecord result = (ARecord) DDNSRRecord.unmarshall(node);
 				return result;
 			}
 		} catch (IOException e) {
 			throw new DDNSException("resolve encountered an IO exception: " + e.getMessage());
 		}
 	}
 	
 	/* Recursively attempts to call the requested RPC method until one of the following occurs:
 	 * - the returned node has done: true
 	 * - the returned node indicates an error occurred
 	 * - an exception is thrown
 	 * - more than maxResolveAttempts calls are made
 	 * 
 	 * Returns the JSONObject returned by the requested RPC method call. 
 	 * (This object will either indicate an error, or give the ip/port of the requested name)
 	 */
 	private JSONObject resolverHelper(String name, String rpccall, String serviceIP, int servicePort, JSONObject obj, int attempts) 
 																						throws DDNSException, JSONException, IOException {		
 		if (attempts >= maxResolveAttempts) {
 			throw new DDNSException.DDNSTTLExpiredException(new DDNSFullName(name));
 		}
 
 		JSONObject response = RPCCall.invoke(serviceIP, servicePort, "ddns", rpccall, obj);
 
 		if (response.getString("resulttype").equals("ddnsexception") || response.getBoolean("done")) {
 			return response;
 		}
 		// set up variables for next attempt
 		String newIP;
 		int newPort;
 		JSONObject node = response.getJSONObject("node");
 		String nodeType = node.getString("type");
 		if (nodeType.equals("CNAME")) {
 			String nameToReplace = node.getString("name");
 			String alias = node.getString("alias");
 			name = name.substring(0, name.indexOf(nameToReplace)) + alias;
 			obj.put("name", name);
 			newIP = rootServerIP;
 			newPort = rootPort;
 		} else {
 			newIP = node.getString("ip");
 			newPort = node.getInt("port");
 		}
 		return resolverHelper(name, rpccall, newIP, newPort, obj, attempts+1);
 	}
 
 	
 	@Override
 	public String dumpState() {
 		return "whatever you want";
 	}
 	
 	
 	private void cachePutLocal(String name, CacheRecord record) {
 		synchronized (cache) {
 			cache.put(name, record);
 		}
 	}
 	
 	// Adds the given name -> ip&port mapping to the cache 
 	private void cachePutGlobal(String name, CacheRecord record) {
 		if (hasCaching) {
 			synchronized (cache) {
 				cache.put(name, record);
 			}
 			record.scheduleCacheRemoval();
 		}
 	}
 	
 	
 	// A timer task for scheduling cache cleaning events
 	class CacheTask extends TimerTask {
 		private String nameToRemove;
 		
 		public CacheTask(String name) {
 			nameToRemove = name;
 		}
 		
 		@Override
 		public void run() {
 			// remove item from cache
 			synchronized (cache) {			
 				if (cache.containsKey(nameToRemove)) {
 					cache.remove(nameToRemove);
 				}
 			}
 		}
 	}
 	
 	// A timer task for scheduling re-registering events
 	class RegisterTask extends TimerTask {
 		private DDNSFullNameInterface name;
 		private int port;
 		
 		
 		public RegisterTask(DDNSFullNameInterface registername, int registerport) {
 			name = registername;
 			port = registerport;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				register(name, port);
 			} catch (DDNSException e) {
 				// what to do here??
 			}
 		}
 		
 	}
 	
 	
 	private class CacheRecord {
 		private ARecord nRecord;			/* the ARecord/SOARecord this CacheRecord represents */
 		private DDNSException nException;	/* used to indicate that this record represents either nosuchname or noaddress */
 		private TimerTask nTask;				/* used to re-register local records, or remove global records from the cache */
 		private boolean nLocal;			/* indicator of whether or not this record represents a local name/address */
 		
 		/* Constructs a record to associate with a legal name
 		 * TimerTask will be a CacheTask for a global entry, or a RegisterTask if this is a local entry
 		 */
 		public CacheRecord(ARecord record, TimerTask task, boolean isLocal) {
 			nRecord = record;
 			nTask = task;
 			nLocal = isLocal;
 		}
 		
 		/* Constructs a record to associate with an invalid name, or one that has no address. 
 		 * Intended for use with global entries (still need a TimerTask in order to remove from the cache)
 		 * isLocal is set to false when using this constructor.
 		 */
 		public CacheRecord(DDNSException ex, TimerTask cachetask) {
 			nRecord = null;
 			nLocal = false;
 			nException = ex;
 			nTask = cachetask;
 		}
 		
 		/* Constructs a record to associate with an invalid name, or one that has no address.
 		 * Intended for use with local entries (local entries are never removed from the cache, and if they are invalid shouldn't reregister).
 		 * isLocal is set to true when using this constructor.
 		 */
 		public CacheRecord(DDNSException ex) {
 			nRecord = null;
 			nTask = null;
 			nLocal = true;
 			nException = ex;
 		}
 		
 		/* returns the record containing the IP and port */
 		public ARecord getRecord() throws DDNSException {
 			if (nException != null)
 				throw nException;
 			return nRecord;
 		}
 		
 		/* returns true if this represents a local record, false otherwise */
 		public boolean isLocal() {
 			return nLocal;
 		}
 		
 		/* trusts that the timertask associated with this record is for registration, and cancels it */
 		public void cancelRegistration() {
 			if (nTask != null) {
 				nTask.cancel();
 			}
 		}
 		
 		/* trusts that the timertask is for registration, and schedules it to reregister */
 		public void scheduleRegistration(long delay) {
 			if (nTask != null) 
 				timer.schedule(nTask, delay);
 		}
 		
 		public void scheduleCacheRemoval() {
 			if (nTask != null)
 				timer.schedule(nTask, cacheTimeout);
 		}
 		
 	}
 
 
 }
