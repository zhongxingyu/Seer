 package identity.server;
 
 import identity.calendar.CalendarDBServer;
 import identity.calendar.CalendarEntry;
 
 import java.io.File;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.RMIClientSocketFactory;
 import java.rmi.server.RMIServerSocketFactory;
 import java.rmi.server.ServerNotActiveException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.rmi.ssl.SslRMIClientSocketFactory;
 import javax.rmi.ssl.SslRMIServerSocketFactory;
 
 //import javax.rmi.ssl.SslRMIClientSocketFactory;
 
 /*
 	Arguements for Text Editor run script.
 */
 
 //args 127.0.0.1
 //jargs
 //classpath
 //class
 
 
 /**
  * @author jaremy
  *
  */
 public class IdentityServer implements IdentityUUID
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2503049572587527151L;
 	private static int registryPort = 5299;
 	
 	private String name;
 	private UserInfoDataBase userDBwrapper;
 	private ConcurrentHashMap<UUID, UserInfo> userdb;
 	private CalendarDBServer caldb;
 	/**
 	 * Default Constructor
 	 */
 	public IdentityServer() throws RemoteException
 	{
 		super();
 		System.out.println("Creating new RMI agent!");
 		userDBwrapper = new UserInfoDataBase( "defaultUserInfo.db");
 		userdb = userDBwrapper.dbHash;
 		caldb = new CalendarDBServer("CALENDAR");
 		
 	}
 
 	/**
 	 * Method shamelessly copied from Amit's code. Modified slightly to include a
 	 * port number. I don't know what is with the difference in naming
 	 * from the Program PDF specification, but it seems to bind just fine.
 	 * @param name bind to the given name
 	 */
 	public void bind(String name)
 	{
 		this.name = name;
 		try {
 			RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
 			RMIServerSocketFactory rmiServerSockeyFactory = new SslRMIServerSocketFactory();
 			IdentityUUID ccAuth = (IdentityUUID) UnicastRemoteObject.exportObject(this, 0,
 			                    rmiClientSocketFactory, rmiServerSockeyFactory);
 			Registry registry = LocateRegistry.createRegistry(registryPort);
 
 			registry.rebind(name, ccAuth);
 			System.out.println(name + " bound in registry");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Exception occurred: " + e);
 		}
 	}
 
 	/**
 	 * Main method. Yep. Main.
 	 * @throws IOException 
 	 */
 	public static void main(String args[]) throws IOException
 	{
 		// parse the port number
 		if (args.length > 0) {
 			registryPort = Integer.parseInt(args[0]);
 		}
 		
 		String certName = "./resources/Server_Keystore";
 		File cert = new File(certName);
 		System.out.println("Can read cert file? "+cert.canRead());
 		System.out.println("cert file location: "+cert.getCanonicalPath());
 
 		// setup the properties
 		System.out.println("Setting System Properties....");
 		System.setProperty("javax.net.ssl.keyStore", certName);
 		System.setProperty("javax.net.ssl.keyStorePassword", "server");
 		System.setProperty("java.security.policy", "./resources/mysecurity.policy");
 		
 		// setup the server and bind it.
 		try {
 			IdentityServer server = new IdentityServer();
 			server.bind("IdentityServer");
 		}
 		catch (Throwable th) {
 			th.printStackTrace();
 			System.out.println("Exception occurred: " + th);
 		}
 		
 	}
 
 
 	/**
 	 * A test method. 
 	 * @deprecated
 	 */
 	public String sayHello() throws RemoteException
 	{
 		try {
 			System.out.println( UnicastRemoteObject.getClientHost() );
 		}
 		catch (Exception e) {
 			System.err.println( "client error: " + e);
 		}
 		return  "Hello World! I am alive!\nname: " + name;
 	}
 
 	/*
 		The rest of these methods contain the functional code
 	*/
 
 	/**
 	* createUUID create a user with given parameters and given UUID. A unique UUID should be retreived from getUniqueUUID.
 	* @param req UserInfo containing username, an optional real name, and an md5password salted with the given UUID.
 	* @return returns a copy of the newly created UserInfo data sans password.
 	*/
 	public UserInfo createUUID( UserInfo req ) throws UserInfoException, java.rmi.RemoteException
 	{
 		UserInfo newUser;
 		String ipaddr = clientaddr();
 		
 		if (userDBwrapper.containsUserName(req.username)) {
 			System.err.println("Username already present: " + req.username +" req: "+req);
 			throw new UserInfoException("Username already present: " + req.username, 1);
 		}
 
 		// create a new user id, just because. Set the ipaddr and current date.
 		newUser = new UserInfo(req, ipaddr);
 		// TODO check the password to make sure it is correct length.
 		
 		// check if UUID is already in database
 		if (userdb.containsKey(newUser.uuid))
 			throw new UserInfoException("UUID already in database. Please call getUniqueUUID() then retry. ",2);
 			
 		System.out.println("Added user to userdb");
 		userdb.put(newUser.uuid, newUser);
 		UserInfo test = userdb.get(newUser.uuid); 
 		System.err.println("Created: " + newUser);
 
 		return UserInfo.scrubPassword(test);
 	}
 
 	/**
 	 * perform a lookup with a given username
 	 * @param username to search for
 	 * @return returns the UserInfo if found, null if not found.
 	 */
 	public UserInfo lookupUUID( String username ) throws UserInfoException, java.rmi.RemoteException
 	{
 		//ConcurrentHashMap<UUID, UserInfo> dbhash = userdb.dbHash;
 		UserInfo val = userDBwrapper.getUserName(username);
 		return UserInfo.scrubPassword(val);
 	}
 
 	/**
 	 * reverse lookup a user based on a UUID
 	 * @param requuid UUID object containing the UUID to lookup
 	 * @return returns the UserInfo if found, null if not found.
 	 */
 	public UserInfo revlookupUUID(UUID requuid) throws UserInfoException, java.rmi.RemoteException
 	{
 		UserInfo tmp = userdb.get(requuid);
 		return UserInfo.scrubPassword(tmp);
 	}
 
 	/**
 	 * This method should be merged with modifyUserName method to be more generic. 
 	 * Since these pass UserInfo objects for request and the new data 
 	 * what to be modified can be determined from these.
 	 * @deprecated This method should be merged with modifyUserName method to be more generic. 
 	 */
 	public UserInfo modifyUUID( UserInfo reqUserInfo, UserInfo newUser)
 		throws UserInfoException, java.rmi.RemoteException
 	{
 		String ipaddr = clientaddr();
 		UserInfo orig = userdb.get(reqUserInfo.uuid);
 		
 		if (orig == null)
 			throw new UserInfoException("Cannot find given UUID",2);
 		if (!reqUserInfo.username.equals( orig.username))
 			throw new UserInfoException("Mismatch in username request and stored username",1);
 		if (!reqUserInfo.md5passwd.equals( orig.md5passwd))
 			throw new UserInfoException("Incorrect Password! (DEBUG) "+reqUserInfo.md5passwd+" orig: "+orig.md5passwd,0);
 		/*
 		 * 	public UserInfo(final UUID uuid, final String username, final String md5passwd, 
 				final String realname, final String ipaddr, final Date lastdate) 
 		 */
 		UserInfo noveluser = new UserInfo(
 				orig.uuid,
 				(newUser.username == null) ? orig.username : newUser.username,
 				orig.md5passwd,
 				(newUser.realname == null) ? orig.realname : newUser.realname,
 				ipaddr,
 				new Date()
 				);
 		
 		userdb.put(noveluser.uuid, noveluser);
 		UserInfo result = userdb.get(newUser.uuid); 
 		
 		return UserInfo.scrubPassword(result);
 	}
 	
 	/**
 	 * This modifies a username as retrieved from reqUserInfo with the information found
 	 * in newUser
 	 * @param reqUserInfo input user information, basically just a username and password
 	 * @param newUser information for the new user information. 
 	 */
 	public UserInfo modifyUserName( UserInfo reqUserInfo, UserInfo newUser)
 		throws UserInfoException, java.rmi.RemoteException
 	{
 		String ipaddr = clientaddr();
 		UserInfo orig = userDBwrapper.getUserName(reqUserInfo.username);
 		
 		if (orig == null)
 			throw new UserInfoException("Cannot find given Username",2);
 		if (!reqUserInfo.md5passwd.equals( orig.md5passwd))
 			throw new UserInfoException("Incorrect Password! (DEBUG) "+reqUserInfo.md5passwd+" orig: "+orig.md5passwd,0);
 		// now create new UserInfo with merged data.
 		UserInfo noveluser = new UserInfo(
 				orig.uuid,
 				(newUser.username == null) ? orig.username : newUser.username,
 				orig.md5passwd,
 				(newUser.realname == null) ? orig.realname : newUser.realname,
 				ipaddr,
 				new Date()
 				);
 		// modify the database with the new data
 		userdb.put(noveluser.uuid, noveluser);
 		UserInfo result = userdb.get(noveluser.uuid);
 		return UserInfo.scrubPassword(result);
 	}
 	
 	/**
 	 * Retrieve a dump of all users in the database. We just do a dump of the database
 	 * and scrub the passwords for each element. This method isn't very efficient. 
 	 * @return array of UserInfo with passwords scrubbed.
 	 */
 	public UserInfo[] dumpContents() throws UserInfoException, java.rmi.RemoteException
 	{
 		// we dump the entire array
 		// then scrub the passwords
 		// then return them.
 		UserInfo[] tmp = userDBwrapper.toArray(); 
 		for (int i=0; i<tmp.length; ++i) {
 			tmp[i] = UserInfo.scrubPassword(tmp[i]);
 		}
 		return tmp;
 	}
 	
 	/**
 	 * Debugging method, to provide clean database for a new test run
 	 */
 	@SuppressWarnings("unused")
 	private void empty() throws UserInfoException, java.rmi.RemoteException {
 		System.err.println("Resetting Database");
 		userDBwrapper = new UserInfoDataBase("defaultUserInfo.db");
 		userdb = userDBwrapper.dbHash;
 	}
 	
 	/**
 	 * getUniqueUUID returns a UUID not in the database. The name is not reservered, but createUUID will check again.
 	 *
 	 * @return returns a unique UUID
 	 */
 	public UUID getUniqueUUID( ) {
 		// public UserInfo(String username, String passwd, String realname) {
 		UserInfo tmp = new UserInfo(null,null,null);
 		while (userdb.containsKey(tmp.uuid))
 			tmp = tmp.setRandomUUID();
 		// reserver uuid with null placement?
 		//userdb.put(tmp.uuid, tmp);
 		return tmp.uuid;
 	}
 	
 	/**
 	 * returns server side full copy of user info base on either UUID or username.
 	 * @param user contains either name or UUID to lookup user with.
 	 * @return
 	 * @throws UserInfoException 
 	 */
 	private UserInfo getServerUserInfo(UserInfo user) throws UserInfoException {
 		if (user == null)
 			return null;
 		UserInfo serveruser;			
 		
 		if (user.username != null)
 			serveruser = userDBwrapper.getUserName(user.username);
 		else if (user.uuid != null)
 			serveruser = userDBwrapper.getUUID(user.uuid);
 		else 
 			serveruser = null;
 		
 		if (user.username != null && user.uuid != null &&
 				!user.username.equals(serveruser.username) && 
 				!user.uuid.equals(serveruser.uuid) )
 			throw new UserInfoException("No User Authentication given!",2);
 
 		return serveruser;
 	}
 	
 	/**
 	 * Tries to authenticate a user with either UUID or username. 
 	 * @param auth contains information from client to use for authenticating.
 	 * @return a full copy of the UserInfo or null on error.
 	 * @throws UserInfoException in case both uuid and name are given and don't match
 	 */
 	private UserInfo authenitcate(UserInfo auth) throws UserInfoException {
 		UserInfo serverauth = null;
 		if (auth == null)
 			throw new UserInfoException("No User Authentication given!",2);
 		
 		// get user
 		serverauth = getServerUserInfo(auth);
 		
 		if ( serverauth == null )
 			throw new UserInfoException("No UserInfo found!",2);
 		
 		if (!serverauth.md5passwd.equals( auth.md5passwd))
 			throw new UserInfoException("Incorrect Password! (DEBUG) "+serverauth.md5passwd+" auth: "+auth.md5passwd,0);
 		else
 			return serverauth;
 	}
 	
 	/**
 	* Returns the current clients hosts.
 	* @return the current clients ip address
 	*/
 	private String clientaddr() {
 		String ipaddr;
 		try {
 			ipaddr = UnicastRemoteObject.getClientHost();
 		}
 		catch (ServerNotActiveException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			ipaddr = null;
 		}
 		return ipaddr;
 	}
 
 	public boolean addCalendarEntry(CalendarEntry calEntry, UserInfo auth)
 			throws UserInfoException, RemoteException 
 	{
 		boolean retval = true;
 		UserInfo orig = authenitcate(auth);
 		if ( orig == null)
 			return false;
 		
 		if (calEntry.uuid == null)
 			calEntry.setUuid(orig.uuid);
 		if (calEntry.id == null)
 			throw new UserInfoException("Null ident",0);
 		
 		caldb.addEntry(calEntry);
 		
 		System.out.println("SDEBUG: add calEntry: "+calEntry);
 		System.out.println("SDEBUG: db: "+caldb.getEntry(calEntry));
 		
 		return retval;
 	}
 
 	public boolean deleteCalendarEntry(CalendarEntry calEntry, UserInfo auth)
 			throws UserInfoException, RemoteException {
 		// TODO Auto-generated method stub
		boolean retval = false;
 		// TODO Auto-generated method stub
 		UserInfo orig = authenitcate(auth);
 		
 		if (orig == null)
 		{
 			retval = false;
 			throw new UserInfoException("Cannot find given Username",2);
 		}
 	
 		if (calEntry.uuid == null)
 			calEntry.setUuid(orig.uuid);
 		if (calEntry.id == null)
 			throw new UserInfoException("Null ident",0);
 		
 		retval = caldb.delEntry(calEntry);
 		
//		System.out.println("");
		
		System.out.println("debug called delete entry: "+calEntry);
 		return retval;
 	}
 
 	public CalendarEntry[] displayCalendarEntries(UserInfo newUserinfo, UserInfo auth, boolean modePublic) 
 		throws UserInfoException, RemoteException {
 		UserInfo orig = authenitcate(auth);
 		
 		if (orig == null)
 			throw new UserInfoException("Cannot find given Username",2);
 		
 		UserInfo val = userDBwrapper.getUserName(newUserinfo.username);
 		if(val!=null)
 		{
 			
 			
 		ArrayList<CalendarEntry> tmp = new ArrayList<CalendarEntry>(200);
 		ConcurrentHashMap<Integer, CalendarEntry> userhm = caldb.getHashUUID(val.uuid);
 		for ( CalendarEntry entry : userhm.values() ) {
 			// check for modePublic and event is public
 			// or modePublic is not public (is false) then add
 			if ( (modePublic && entry.status.equals("public")) || !modePublic )
 				tmp.add(entry);
 		}
 			
 		return tmp.toArray(new CalendarEntry[0]);
 		}
 		else
 			return null;
 	
 	}
 
 	
 }
 
 
 
