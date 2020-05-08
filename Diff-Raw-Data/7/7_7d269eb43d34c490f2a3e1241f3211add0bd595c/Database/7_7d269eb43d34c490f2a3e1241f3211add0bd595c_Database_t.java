 package org.touchirc.db;
 
 import java.util.ArrayList;
 
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class Database extends SQLiteOpenHelper {
 
 	private static final String DB_NAME = "touchirc.db";
 	private static final int DB_VERSION = 1;
 
 	public Database(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 
 		db.execSQL("CREATE TABLE servers (" + DBConstants.SERVER_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DBConstants.SERVER_TITLE + " TEXT NOT NULL UNIQUE,"
				+ DBConstants.SERVER_HOST + " TEXT NOT NULL,"
 				+ DBConstants.SERVER_PORT + " INTEGER,"
 				+ DBConstants.SERVER_PASSWORD + " TEXT, "
 				+ DBConstants.SERVER_USE_SSL + " BOOLEAN, "
 				+ DBConstants.SERVER_CHARSET + " TEXT,"
 				+ DBConstants.SERVER_AUTOCONNECT + " BOOLEAN );");
 
 		db.execSQL("CREATE TABLE " + DBConstants.PROFILE_TABLE_NAME + "(" + DBConstants.PROFILE_ID
 				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ DBConstants.PROFILE_NAME + " TEXT NOT NULL UNIQUE,"
 				+ DBConstants.PROFILE_FIRST_NICKNAME + " TEXT NOT NULL,"
 				+ DBConstants.PROFILE_SCD_NICKNAME + " TEXT,"
 				+ DBConstants.PROFILE_THIRD_NICKNAME + " TEXT,"
 				+ DBConstants.PROFILE_USERNAME + " TEXT NOT NULL,"
 				+ DBConstants.PROFILE_REALNAME + " TEXT NOT NULL,"
 				+ DBConstants.DEFAULT_PROFILE + " BOOLEAN,"
 				+ DBConstants.PROFILE_SERVER_LIST + " TEXT );");
 		
 		db.execSQL("CREATE TABLE " + DBConstants.LINKED_SERVERS_TO_PROFILE_NAME + "("
				+ DBConstants.LINKED_PROFILE_ID + " INTEGER,"
				+ DBConstants.LINKED_SERVER_ID + " INTEGER PRIMARY KEY,"
 				+ " FOREIGN KEY (" + DBConstants.LINKED_PROFILE_ID + ") REFERENCES " 
 					+ DBConstants.PROFILE_TABLE_NAME + "(" + DBConstants.PROFILE_ID + "),"
 				+ " FOREIGN KEY (" + DBConstants.LINKED_SERVER_ID + ") REFERENCES " 
 					+ DBConstants.SERVER_TABLE_NAME + "(" + DBConstants.SERVER_ID + "));");
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Add the given server to the database
 	 * 
 	 * @param server
 	 */
 
 	public void addServer(Server server) {
 
 		ContentValues values = new ContentValues();
 
 		values.put(DBConstants.SERVER_TITLE, server.getName());
 		values.put(DBConstants.SERVER_HOST, server.getHost());
 		values.put(DBConstants.SERVER_PORT, server.getPort());
 		values.put(DBConstants.SERVER_PASSWORD, server.getPassword());
 		values.put(DBConstants.SERVER_USE_SSL, server.useSSL());
 		values.put(DBConstants.SERVER_CHARSET, server.getEncoding());
 		values.put(DBConstants.SERVER_AUTOCONNECT, false);
 
 		this.getWritableDatabase().insert(
 											DBConstants.SERVER_TABLE_NAME,
 											null,
 											values);
 
 	}
 	
 	/**
 	 * Delete the Server from the database
 	 * the method delete returns the number of rows deleted inside the database
 	 * 
 	 * @param serverName
 	 * @return if the deletion has succeeded
 	 */
 
 	public boolean deleteServer(String serverName){
 		
 		return this.getWritableDatabase().delete(
 												DBConstants.SERVER_TABLE_NAME, 
 												DBConstants.SERVER_TITLE + "=\"" + serverName + "\"", 
 												null
 			) > 0;
 	}
 	
 	/**
 	 * Update the values of the current server
 	 * 
 	 * @param s
 	 * @return if the update succeeds
 	 */
 
 	public boolean updateServer(Server s, String oldNameServer){
 		ContentValues newValues = new ContentValues();
 		newValues.put(DBConstants.SERVER_TITLE, s.getName());
 		newValues.put(DBConstants.SERVER_HOST, s.getHost());
 		newValues.put(DBConstants.SERVER_PORT, s.getPort());
 		newValues.put(DBConstants.SERVER_PASSWORD, s.getPassword());
 
 		if(this.getWritableDatabase().update(	
 											DBConstants.SERVER_TABLE_NAME,
 											newValues, 
 											DBConstants.SERVER_TITLE + "=" + "\"" + oldNameServer + "\"",
 											null
 		) > 0){
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Return the ID of the server spent in argument
 	 * 
 	 * @param server
 	 * @return the ID of the server
 	 */
 	
 	public int getIdServer(Server server){
 		
 		// SELECT id FROM servers WHERE name = serverName
 		Cursor c = this.getReadableDatabase().query(
 													DBConstants.SERVER_TABLE_NAME, 
 													new String [] {DBConstants.SERVER_ID}, 
 													DBConstants.SERVER_TITLE + " = " + "\"" + server.getName() + "\"",
 													null,
 													null,
 													null,
 													null);
 		
 		c.moveToNext();
 		return c.getInt(0);
 	}
 
 	/**
 	 * Return a server object with the given serverId
 	 * 
 	 * @param serverId
 	 * @return the corresponding Server
 	 */
 
 	public Server getServerById(int serverId) {
 		Server server = null;
 
 		// SELECT ALL FROM servers WHERE id = serverId ORDER BY title ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.SERVER_TABLE_NAME, 
 														DBConstants.SERVER_ALL,
 														DBConstants.SERVER_ID + " = " + serverId, 
 														null, 
 														null, 
 														null,
 														DBConstants.SERVER_TITLE + " ASC");
 
 		if (cursor.moveToNext()) {
 			server = createServer(cursor);
 		}
 
 		cursor.close();
 
 		return server;
 	}
 
 	/**
 	 * Return a server object with the given serverName
 	 * 
 	 * @param serverName
 	 * @return the corresponding Server
 	 */
 
 	public Server getServerByName(String serverName) {
 		Server server = null;
 
 		// SELECT ALL FROM servers WHERE title = serverName ORDER BY title ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.SERVER_TABLE_NAME, 
 														DBConstants.SERVER_ALL,
 														DBConstants.SERVER_TITLE + " = " + "\"" + serverName + "\"", 
 														null, 
 														null, 
 														null,
 														DBConstants.SERVER_TITLE + " ASC");
 
 		if (cursor.moveToNext()) {
 			server = createServer(cursor);
 		}
 
 		cursor.close();
 
 		return server;
 	}
 	
 	/**
 	 * Allows to collect the servers'list
 	 * 
 	 * @return An ArrayList of all the available Server inside the Database
 	 */
 
 	public ArrayList<Server> getServerList() {
 
 		ArrayList<Server> listServer = new ArrayList<Server>();
 
 		// SELECT ALL FROM servers ORDER BY title ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.SERVER_TABLE_NAME, 
 														DBConstants.SERVER_ALL, 
 														null,
 														null, 
 														null, 
 														null, 
 														DBConstants.SERVER_TITLE + " ASC");
 
 		while (cursor.moveToNext()) {
 			listServer.add(createServer(cursor));
 		}
 
 		cursor.close();
 
 		return listServer;
 	}
 
 	/**
 	 * Create a server object with the given database cursor
 	 * 
 	 * @param cursor
 	 * @return A Server object with the cursor spent in arguments
 	 */
 
 	private Server createServer(Cursor cursor) {
 
 		Server server = new Server(
 			cursor.getString(cursor.getColumnIndex((DBConstants.SERVER_TITLE))),
 			cursor.getString(cursor.getColumnIndex((DBConstants.SERVER_HOST))),
 			cursor.getInt(cursor.getColumnIndex((DBConstants.SERVER_PORT))),
 			cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_PASSWORD)),
 			cursor.getString(cursor.getColumnIndex(DBConstants.SERVER_CHARSET)));
 
 		// TODO SSL Support
 
 		return server;
 	}
 
 	/**
 	 * Return the name of the default profile
 	 * SQLite compare the booleans thanks to String
 	 * that is why the query used  "\"" + true + "\""
 	 * 
 	 * @return The name of the auto-connected server
 	 */
 
 	public String nameAutoConnectedServer(){
 
 		Server autoConnectedServer = null;
 
 		// SELECT ALL FROM servers WHERE autoconnect="true" ORDER BY title ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.SERVER_TABLE_NAME, 
 														DBConstants.SERVER_ALL,
 														DBConstants.SERVER_AUTOCONNECT + " = " + "\"" + true + "\"", 
 														null, 
 														null, 
 														null,
 														DBConstants.SERVER_TITLE + " ASC");
 
 		// if the query returns 1 row
 		if (cursor.getCount() == 1) {
 			cursor.moveToNext(); // To avoid out of bounds exception
 			autoConnectedServer = createServer(cursor);
 			return autoConnectedServer.getName();
 		}
 
 		return null;
 	}
 	
 	/**
 	 * 
 	 * The current Server is auto-connected when the app is launched
 	 * 
 	 * @param newAutoConnectedServerName
 	 * @return if the changing has succeeded
 	 */
 
 	public boolean setAutoConnect(String newAutoConnectedServerName){
 
 		// Checking the presence of an auto-connected server
 		// SELECT ALL FROM servers WHERE autoconnect = "tru" ORDER BY title ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.SERVER_TABLE_NAME, 
 														DBConstants.SERVER_ALL,
 														DBConstants.SERVER_AUTOCONNECT + " = " + "\"" + true + "\"", 
 														null, 
 														null,
 														null,
 														DBConstants.SERVER_TITLE + " ASC");
 
 		// if an auto-connected server already exists
 		if(cursor.getCount() > 0){
 
 			// The old auto-connected server becomes a standard server
 			ContentValues oldAutoConnectedServer = new ContentValues();
 			oldAutoConnectedServer.put(DBConstants.SERVER_AUTOCONNECT, "false");
 			this.getWritableDatabase().update(	
 											DBConstants.SERVER_TABLE_NAME,
 											oldAutoConnectedServer,
 											DBConstants.SERVER_AUTOCONNECT + "=" + "\"" + true + "\"",
 											null);
 		}
 
 		// Setting the new auto-connected server
 		ContentValues newAutoConnectedServer = new ContentValues();
 		newAutoConnectedServer.put(DBConstants.SERVER_AUTOCONNECT, "true");
 		if(this.getWritableDatabase().update(	
 											DBConstants.SERVER_TABLE_NAME,
 											newAutoConnectedServer, 
 											DBConstants.SERVER_TITLE + "=" + "\"" + newAutoConnectedServerName + "\"",
 											null
 		) > 0){
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Add the given profile to the database
 	 * 
 	 * @param profile
 	 */
 
 	public void addProfile(Profile profile) {
 
 		ContentValues values = new ContentValues();
 
 		values.put(DBConstants.PROFILE_NAME, profile.getProfile_name());
 		values.put(DBConstants.PROFILE_FIRST_NICKNAME, profile.getFirstNick());
 		values.put(DBConstants.PROFILE_SCD_NICKNAME, profile.getSecondNick());
 		values.put(DBConstants.PROFILE_THIRD_NICKNAME, profile.getThirdNick());
 		values.put(DBConstants.PROFILE_USERNAME, profile.getUsername());
 		values.put(DBConstants.PROFILE_REALNAME, profile.getRealname());
 		values.put(DBConstants.DEFAULT_PROFILE, "false");
 
 		// TODO ListServer Support
 
 		values.put(DBConstants.PROFILE_SERVER_LIST, profile.getListServer().toString());
 
 		this.getWritableDatabase().insert(
 										DBConstants.PROFILE_TABLE_NAME,
 										null,
 										values);
 
 	}
 	
 	/**
 	 * Delete the Profile from the database
 	 * the method delete returns the number of rows deleted inside the database
 	 * 
 	 * @param profileName
 	 * @return if the deletion has succeeded
 	 */
 
 	public boolean deleteProfile(String profileName){
 		
 		return this.getWritableDatabase().delete(
 												DBConstants.PROFILE_TABLE_NAME, 
 												DBConstants.PROFILE_NAME + "=\"" + profileName + "\"", 
 												null
 				) > 0;
 	}
 	
 	/**
 	 * Update the values of the current profile
 	 * 
 	 * @param p
 	 * @return if the update has succeeded
 	 */
 
 	public boolean updateProfile(Profile p, String oldeNameProfile){
 		
 		ContentValues newValues = new ContentValues();
 		newValues.put(DBConstants.PROFILE_NAME, p.getProfile_name());
 		newValues.put(DBConstants.PROFILE_FIRST_NICKNAME, p.getFirstNick());
 		newValues.put(DBConstants.PROFILE_SCD_NICKNAME, p.getSecondNick());
 		newValues.put(DBConstants.PROFILE_THIRD_NICKNAME, p.getThirdNick());
 		newValues.put(DBConstants.PROFILE_USERNAME, p.getUsername());
 		newValues.put(DBConstants.PROFILE_REALNAME, p.getRealname());
 		
 		if(this.getWritableDatabase().update(	
 												DBConstants.PROFILE_TABLE_NAME,
 												newValues, 
 												DBConstants.PROFILE_NAME + "=" + "\"" + oldeNameProfile + "\"",
 												null
 		) > 0){
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Return a profile object with the given profileId
 	 * 
 	 * @param profileId
 	 * @return The corresponding Profile
 	 */
 
 	public Profile getProfileById(int profileId) {
 		
 		Profile profile = null;
 
 		// SELECT ALL FROM profiles WHERE id = profileId ORDER BY name ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.PROFILE_TABLE_NAME,
 														DBConstants.PROFILE_ALL,
 														DBConstants.PROFILE_ID + " = " + profileId, 
 														null, 
 														null, 
 														null,
 														DBConstants.PROFILE_NAME + " ASC");
 
 		if (cursor.moveToNext()) {
 			profile = createProfile(cursor);
 		}
 
 		cursor.close();
 
 		return profile;
 	}
 
 	/**
 	 * Return a profile object with the given profileName
 	 * 
 	 * @param profileName
 	 * @return The corresponding Profile
 	 */
 
 	public Profile getProfileByName(String profileName) {
 		
 		Profile profile = null;
 
 		// SELECT ALL FROM profiles WHERE name = profileName ORDER BY name ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.PROFILE_TABLE_NAME, 
 														DBConstants.PROFILE_ALL,
 														DBConstants.PROFILE_NAME + " = " + "\"" + profileName + "\"", 
 														null, 
 														null, 
 														null,
 														DBConstants.PROFILE_NAME + " ASC");
 
 		if (cursor.moveToNext()) {
 			profile = createProfile(cursor);
 		}
 
 		cursor.close();
 
 		return profile;
 	}
 	
 	/**
 	 * Return the ID of the profile spent in argument
 	 * 
 	 * @param profile
 	 * @return the ID of the profile
 	 */
 	
 	public int getIdProfile (Profile profile){
 		
 		// SELECT ALL id profiles WHERE name = profileName ORDER BY name ASC
 		Cursor c = this.getReadableDatabase().query(
 													DBConstants.PROFILE_TABLE_NAME, 
 													new String [] {DBConstants.PROFILE_ID}, 
 													DBConstants.PROFILE_NAME + " = " + "\"" + profile.getProfile_name() + "\"",
 													null, 
 													null, 
 													null, 
 													null);
 		c.moveToNext();
 		return c.getInt(0);
 	}
 	
 	/**
 	 * Allows to collect the profiles' list
 	 * 
 	 * @return The list of available Profiles inside the Database
 	 */
 
 	public ArrayList<Profile> getProfileList() {
 
 		ArrayList<Profile> listProfile = new ArrayList<Profile>();
 
 		// SELECT ALL FROM profiles ORDER BY name ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.PROFILE_TABLE_NAME, 
 														DBConstants.PROFILE_ALL, 
 														null,
 														null, 
 														null, 
 														null, 
 														DBConstants.PROFILE_NAME + " ASC");
 
 		while (cursor.moveToNext()) {
 			listProfile.add(createProfile(cursor));
 		}
 		cursor.close();
 
 		return listProfile;
 	}
 
 	/**
 	 * Create a profile object with the given database cursor
 	 * 
 	 * @param cursor
 	 * @return Profile created thanks to the cursor spend in arguments
 	 */
 
 	private Profile createProfile(Cursor cursor) {
 
 		Profile profile = new Profile(
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_NAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_FIRST_NICKNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_SCD_NICKNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_THIRD_NICKNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_USERNAME)),
 				cursor.getString(cursor.getColumnIndex(DBConstants.PROFILE_USERNAME))
 				);
 		return profile;
 	}
 
 	/**
 	 * Return the name of the default profile
 	 * SQLite compare the booleans thanks to String
 	 * that is why the query used  "\"" + true + "\""
 	 * 
 	 * @return The name of the Profile used by default
 	 */
 
 	public String nameDefaultProfile(){
 
 		Profile defaultProfile = null;
 
 		// SELECT ALL FROM profiles WHERE profile_by_default="true" ORDER BY name ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.PROFILE_TABLE_NAME, 
 														DBConstants.PROFILE_ALL,
 														DBConstants.DEFAULT_PROFILE + " = " + "\"" + true + "\"",
 														null,
 														null,
 														null,
 														DBConstants.PROFILE_NAME + " ASC");
 
 		// if the query returns 1 row
 		if (cursor.getCount() == 1) {
 			cursor.moveToNext(); // To avoid out of bounds exception
 			defaultProfile = createProfile(cursor);
 			return defaultProfile.getProfile_name();
 		}
 
 		return null;
 	}
 	
 	/**
 	 *
 	 * The current Profile becomes the profile by default
 	 *
 	 * @param newDefaultProfileName
 	 * @return if the changing has succeeded
 	 */
 
 	public boolean setDefaultProfile(String newDefaultProfileName){
 
 		// Checking the presence of a default profile
 		// SELECT ALL FROM profiles WHERE default_profile = "true" ORDER BY name ASC
 		Cursor cursor = this.getReadableDatabase().query(
 														DBConstants.PROFILE_TABLE_NAME, 
 														DBConstants.PROFILE_ALL,
 														DBConstants.DEFAULT_PROFILE + " = " + "\"" + true + "\"", 
 														null,
 														null,
 														null,
 														DBConstants.PROFILE_NAME + " ASC");
 
 		// if a default profile already exists
 		if(cursor.getCount() > 0){
 
 			// The old default profile becomes a standard profile
 			ContentValues oldDefaultProfile = new ContentValues();
 			oldDefaultProfile.put(DBConstants.DEFAULT_PROFILE, "false");
 			this.getWritableDatabase().update(	
 												DBConstants.PROFILE_TABLE_NAME,
 												oldDefaultProfile,
 												DBConstants.DEFAULT_PROFILE + "=" + "\"" + true + "\"",
 												null);
 		}
 
 		// Setting the new default Profile
 		ContentValues newDefaultProfile = new ContentValues();
 		newDefaultProfile.put(DBConstants.DEFAULT_PROFILE, "true");
 		if(this.getWritableDatabase().update(
 												DBConstants.PROFILE_TABLE_NAME,
 												newDefaultProfile, 
 												DBConstants.PROFILE_NAME + "=" + "\"" + newDefaultProfileName + "\"",
 												null
 		) > 0){
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Add the given link between Profile and server to the database
 	 * 
 	 * @param profile, server
 	 */
 
 	public void addLinkProfileServer(Profile profile, Server server) {
 		
 		// TODO If problem on the primary key : add an idLink (primary key) ?
 		
 		ContentValues values = new ContentValues();
 		values.put(DBConstants.LINKED_PROFILE_ID, this.getIdProfile(profile));
 		values.put(DBConstants.LINKED_SERVER_ID, this.getIdServer(server));
 
 		this.getWritableDatabase().insert(	
 											DBConstants.LINKED_SERVERS_TO_PROFILE_NAME, 
 											null,
 											values);
 	}
 	
 	/**
 	 * Delete the given link between Profile and server to the database
 	 * 
 	 * @param profile, server
 	 */
 
 	public boolean deleteLinkProfileServer(Profile profile, Server server) {
 
 		return this.getWritableDatabase().delete(	
 													DBConstants.LINKED_SERVERS_TO_PROFILE_NAME, 
 													DBConstants.LINKED_PROFILE_ID + " = " + this.getIdProfile(profile) +
 													" AND " +
 													DBConstants.LINKED_SERVER_ID + " = " + this.getIdServer(server), 
 													null
 				) > 0;
 	}
 	
 	/**
 	 * Check if a link already exists between the profile & server spent in arguments
 	 * 
 	 * @param profile
 	 * @param server
 	 * @return The presence or not of the link inside the database.
 	 */
 	
 	public boolean linkAlreadyExisting(Profile profile, Server server){
 		
 		int idProf = this.getIdProfile(profile);
 		int idServ = this.getIdServer(server);
 		
 		// SELECT ALL FROM servers_linked_to_profile WHERE idProfile = idProf AND idServer = idServ
 		if(this.getReadableDatabase().query(	
 												DBConstants.LINKED_SERVERS_TO_PROFILE_NAME, 
 												DBConstants.LINKED_SERVERS_TO_PROFILE_ALL,
 												DBConstants.LINKED_PROFILE_ID + "=" + idProf + 
 												" AND " +
 												DBConstants.LINKED_SERVER_ID + "=" + idServ, 
 												null, 
 												null, 
 												null, 
 												null)
 		.getCount()	> 0){
 			return true;
 		}
 		
 		return false;
 	}
 }
