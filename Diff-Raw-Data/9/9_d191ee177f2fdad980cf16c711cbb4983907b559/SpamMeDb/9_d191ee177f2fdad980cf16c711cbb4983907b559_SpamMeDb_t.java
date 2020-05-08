 package spam.me;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SpamMeDb extends SQLiteOpenHelper{
 	//for logging/debugging purposes:
 	private static final String TAG = "SpamMeDB";
 
 
 	private static SQLiteDatabase Db;
	private static final int DATABASE_VERSION = 45;
 	private static final String DATABASE_NAME = "spamMeDB";
 	private final Context spamMeCtx;
 
 	/**
 	 * Table creation statements
 	 * Creating 3 tables: Group, Messages, and GroupMembers
 	 */
 	private static final String TABLE_CREATE_GROUPS =
 		"create table groups (groupsID integer primary key autoincrement, "
 		+ "name text not null);";
 
 	private static final String TABLE_CREATE_MESSAGES =
 		"create table messages (id integer primary key autoincrement, "
 		+ "groupID integer not null,"
 		+ "sender text not null,"
 		+ "message text not null,"
 		+ "timeAdded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
 		+ "FOREIGN KEY(groupID) REFERENCES groups(groupsID));";
 
 	private static final String TABLE_CREATE_GROUPMEMBERS =
 		"create table groupmembers (id integer primary key autoincrement, "
 		+ "groupID integer not null, "
 		+ "member text not null, "
 		+ "phoneNumber text not null, "
 		+ "FOREIGN KEY(groupID) REFERENCES groups(groupsID));";
 
 	/**
 	 * SpamMe Database creation statement
 	 *
 	 */
 	private static final String DATABASE_CREATE =
 		TABLE_CREATE_GROUPS +";"+ TABLE_CREATE_MESSAGES + ";" + TABLE_CREATE_GROUPMEMBERS + ";";
 
 	private static final String TABLE_GROUPS = "groups";
 	private static final String TABLE_MESSAGES = "messages";
 	private static final String TABLE_GROUPMEMBERS = "groupmembers";
 	private static final String KEY_NAME = "name";
 	private static final String KEY_GROUPSID = "groupsID";
 	private static final String KEY_ID = "id";
 	private static final String KEY_MEMBER = "member";
 	private static final String KEY_PHONENUMBER = "phoneNumber";
 	private static final String KEY_GROUPID = "groupID";
 	private static final String KEY_SENDER = "sender";
 	private static final String KEY_MESSAGE = "message";
 	private static final String KEY_TIMEADDED = "timeAdded";
 
 	@Override
 	public void onCreate(SQLiteDatabase db){
 		try{
 			db.execSQL(TABLE_CREATE_GROUPS);
 			db.execSQL(TABLE_CREATE_MESSAGES);
 			db.execSQL(TABLE_CREATE_GROUPMEMBERS);
 			//DEBUG
 			Log.i(TAG, "DB TABLE Successfully");
 		}
 		catch (SQLException e){
 			//DEBUG
 			Log.i(TAG, "DB Create failed " + e.getMessage() );
 		}
 
 	}
 
 	@Override
 	/**
 	 * Called when the database needs to be updated. All tables will be dropped.
 	 *
 	 */
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		//DEBUG
 		Log.w(TAG, "Upgrading database from  " + oldVersion + " to "
 				+ newVersion + ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS groups");
 		db.execSQL("DROP TABLE IF EXISTS messages");
 		db.execSQL("DROP TABLE IF EXISTS groupmembers");
 		onCreate(db);
 	}
 
 	/**
 	 * Constructor
 	 */
 	public SpamMeDb(Context ctx){
 		super(ctx, DATABASE_NAME, null, DATABASE_VERSION); 
 		try{open();} //Opens or creates the database  
 		catch(Exception e1)
 		{
 			Log.i(TAG, "DB open EXCEPTION");  
 			e1.printStackTrace();  
 		}
 		this.spamMeCtx = ctx;
 	}
 
 	public void open() throws SQLException {
 		setDb(getWritableDatabase());
 	}
 
 	public void close() {
 		getDb().close();
 	}
 
 	/**
 	 * Setter for Db
 	 * @return Db
 	 */
 	private static void setDb(SQLiteDatabase database) {
 		Db = database;
 	}
 
 	/**
 	 * Getter for Db
 	 * @return the mDb
 	 */
 	public static SQLiteDatabase getDb() {
 		return Db;
 	}
 	
 	/**
 	 * Method adds message to the database
 	 */
 	public void addMessage(Message m) throws SQLException{
 		long rowID;
 		ContentValues inputValue = new ContentValues();
 		inputValue.put(KEY_GROUPID, m.getGroupID());
 		inputValue.put(KEY_SENDER, m.getOwner().getName());
 		inputValue.put(KEY_MESSAGE, m.getContent());
 		rowID = getDb().insert(TABLE_MESSAGES, null, inputValue);
 		if (rowID >= 0){
 			System.out.println("Added message succesfully");
 		}
 		else{
 			throw new SQLException();
 		}	
 	}
 	
 	/**
 	 * Method adds the new group name to the database
 	 * If the name already exists or empty string returns -1
 	 * If the name was added successfully returns 1
 	 * If the there was an error throws SQLException
 	 */
 	public long addGroupChat(String name)throws SQLException{
 		Log.i("SpamMeDB: ", "addNewGroupChat name: " + name);
 		if (name == ""){
 			return -1;
 		}
 		
 		//Check to see if the name is already in the database
 		Cursor mCursor = getDb().query(true, TABLE_GROUPS, new String[] {KEY_GROUPSID}, KEY_NAME + "=" + "'" + name + "'",
 				null, null, null, null, null);
 
 		//Name already exists don't create a new entry
 		if (mCursor != null && mCursor.moveToFirst()){
 			mCursor.close();
 			mCursor.deactivate();
 			return -1;
 		}
 		//Name doesn't exist, create a new entry
 		else{
 			long rowID;
 			ContentValues inputValue = new ContentValues();
 			inputValue.put(KEY_NAME, name);
 			rowID = getDb().insert(TABLE_GROUPS, null, inputValue);
 			mCursor.close();
 			mCursor.deactivate();
 			if (rowID >= 0){
 				return rowID;
 			}
 			else{
 				throw new SQLException();
 			}
 		}
 
 	}
 	
 	/**
 	 * Removes the name from the Groups table
 	 * Returns true on success and false on failure
 	 */
 	public boolean removeGroupChat(long removeId){
 		Cursor mCursor = null;
 		int deleteSuccess = getDb().delete(TABLE_GROUPS, KEY_GROUPSID + "==" + (removeId), null);
 		
 		if (deleteSuccess == 1){
 			return true;
 		}
 		return false;
 	}
 
 	public boolean removeMember(String name, String groupName){
 		long id = -1;
 		//Query to find the groupID
 		Cursor mCursor = getDb().query(false, 
 				TABLE_GROUPS, 
 				new String[] {KEY_GROUPSID, KEY_NAME}, 
 				KEY_NAME+ "==" + "'" + groupName + "'",
 				null, null, null, null, null);
 		
 		if (mCursor != null && mCursor.moveToFirst()){
 			id = mCursor.getInt(mCursor.getColumnIndex(KEY_GROUPSID));
 		}
 		System.out.println("DABATABASE REMOVEMEMBER ID is " + id);
 		System.out.println("DATABASE removeMember name is " + name);
 		int deleteSuccess = getDb().delete(TABLE_GROUPMEMBERS, KEY_GROUPID + "==" + id + " and " + KEY_MEMBER + "==" + "'" + name + "'", null);
 		System.out.println("deleteSuccess is " + deleteSuccess);
 		if (deleteSuccess == 1){
 			return true;
 		}
 		mCursor.close();
 		mCursor.deactivate();
 		return false;
 	}
 	/**
 	 * Method adds the new member to the database (Table_GroupMembers)
 	 * If the name already exists or empty string returns -1
 	 * If the name was added successfully returns 1
 	 * If the there was an error throws SQLException
 	 */
 	public int addMember(GroupChat group, Person newPerson){
 		Cursor mCursor = null;
 		newPerson.setPhoneNum(newPerson.getPhoneNum().replace("-", ""));
 
 		//Check to see if the name is already in the database
 		mCursor = getDb().query(false, 
 				TABLE_GROUPMEMBERS, 
 				new String[] {KEY_ID}, 
 				KEY_MEMBER + "=" + "'" + newPerson.getName() + "'" + "and " + KEY_GROUPID + "=" + group.getGroupId(),
 				null, null, null, null, null);
 
 		//Name already exists don't create a new entry
 		if (mCursor != null && mCursor.moveToFirst()){
 			mCursor.close();
 			mCursor.deactivate();
 			return -1;
 		}
 
 		//Name doesn't exist, create a new entry
 		else{
 			long rowID;
 			ContentValues inputValue = new ContentValues();
 			inputValue.put(KEY_MEMBER, newPerson.getName());
 			inputValue.put(KEY_PHONENUMBER, newPerson.getPhoneNum());
 			inputValue.put(KEY_GROUPID, group.getGroupId());
 			rowID = getDb().insert(TABLE_GROUPMEMBERS, null, inputValue);
 			mCursor.close();
 			mCursor.deactivate();
 			if (rowID >= 0){
 				return 1;
 			}
 			else{
 				throw new SQLException();
 			}
 		}
 	}
 	
 	public void removeMember(Person removePerson){
 
 	}
 	public GroupChat getGroupChat(int groupId){
 		return null;	
 	}
 
 	/**
 	 * Method returns a GroupChat object of the given groupName
 	 * @param groupName
 	 * @return GroupChat
 	 */
 	public GroupChat getGroupChat(long id){	
 		GroupChat group = new GroupChat();
 		List <Person> members = new ArrayList(); 
 		List <Message> messages = new ArrayList();
 		Person p; 
 		Message m;
 		String phoneNumber;
 
 		//Set groupID for group
 		group.setGroupId(id);
 
 		//Query Groups table with the groupID to get groupName
 		Cursor mCursor = getDb().query(true, TABLE_GROUPS, new String[]{KEY_NAME}, KEY_GROUPSID + "=" + id,
 				null, null, null, null, null);
 		if (mCursor != null && mCursor.moveToFirst()){
 			String groupName = mCursor.getString(mCursor.getColumnIndex(KEY_NAME));
 			//Set groupName
 			group.setGroupName(groupName);
 		}
 
 		//Use the ID to get the members 
 		mCursor = getDb().query(true, TABLE_GROUPMEMBERS, new String[]{KEY_MEMBER, KEY_PHONENUMBER}, KEY_GROUPID + "=" + id,
 				null, null, null, null, null);
 		int membersCount = mCursor.getCount();
 		//Set the members in groupChat object
 		if (mCursor != null && mCursor.moveToFirst()){
 			for (int i = 0; i < membersCount; i++){
 				p = new Person(); 
 				
 				//Set the name and phone number for person
 				p.setName(mCursor.getString(mCursor.getColumnIndex(KEY_MEMBER)));
 				p.setPhoneNum(mCursor.getString(mCursor.getColumnIndex(KEY_PHONENUMBER)));
 				//Add person to the members list
 				members.add(p);
 				
 				mCursor.moveToNext();
 			}
 			//Set members for group
 			group.setMembersList(members);
 		}
 
 		//Use ID to get the messages
 		mCursor = getDb().query(true, TABLE_MESSAGES, new String[]{KEY_MESSAGE, KEY_SENDER}, KEY_GROUPID + "=" + id,
 				null, null, null, KEY_TIMEADDED, null);
 
 		int messageCount = mCursor.getCount();
 		//Set the messages in groupChat object
 		if (mCursor != null && mCursor.moveToFirst()){
 			for (int i = 0; i<messageCount; i++){
 				//Create a new message
 				m = new Message();
 				//Set the content, owner, and groupID for message
 				m.setContent(mCursor.getString(mCursor.getColumnIndex(KEY_MESSAGE)));
 
 				//DEBUG
 				System.out.println("DB msg: " + mCursor.getString(mCursor.getColumnIndex(KEY_MESSAGE)));
 
 
 				phoneNumber = fetchPhoneNum(mCursor.getString(mCursor.getColumnIndex(KEY_SENDER)));
 				m.setOwner(mCursor.getString(mCursor.getColumnIndex(KEY_SENDER)), phoneNumber);
 				m.setGroupID(id);
 				//Add message to message chain
 				messages.add(m);
 				//Move to next message
 				mCursor.moveToNext();
 			}
 			//Set members for group
 			group.setMessageChain(messages);
 		}
 		
 		mCursor.close();
 		mCursor.deactivate();
 		return group;
 	}
 
 	public String fetchPhoneNum (String name){
 		String number = "";
 		Cursor mCursor = getDb().query(true, TABLE_GROUPMEMBERS, new String[]{KEY_PHONENUMBER}, KEY_MEMBER + "=" + "'" + name + "'",
 				null, null, null, null, null);
 		if (mCursor != null && mCursor.moveToFirst()){
 			number = mCursor.getString(mCursor.getColumnIndex(KEY_PHONENUMBER));
 		}
 		
 		mCursor.close();
 		mCursor.deactivate();
 		return number;
 
 	}
 
 	/**
 	 * Method returns an array of all the group names in the database
 	 * @return GroupChat[]
 	 */
 	public GroupChat[] getAllGroupChatNames (){
 		//Query for any non-null entries in the Groups table
 		Cursor mCursor = getDb().query(true, TABLE_GROUPS, new String[] {KEY_GROUPSID, KEY_NAME},  KEY_GROUPSID +">" + 0 , null,
 				null, null, null, null); 
 
 		//Save number of groups in Groups table
 		int count = mCursor.getCount();
 
 		GroupChat[] names = new GroupChat[count+1];
 		GroupChat defaultGC = new GroupChat();
 		defaultGC.setGroupName("Select a Group Chat");
 		names[0]=defaultGC;
 
 		if (mCursor != null && 	mCursor.moveToFirst()){
 			for (int i = 1; i<count+1; i++){
 				GroupChat tempGC = new GroupChat();
 				tempGC.setGroupName(mCursor.getString(mCursor.getColumnIndex(KEY_NAME)));
 				tempGC.setGroupId(mCursor.getInt(mCursor.getColumnIndex(KEY_GROUPSID)));
 				names[i]=tempGC;
 				mCursor.moveToNext();
 			}
 		}
 		
 		mCursor.close();
 		mCursor.deactivate();
 		return names;
 	}
 
 	/**
 	 * Method returns the name associated with the phone number from the GroupMembers table
 	 * @param String number
 	 * @return String member
 	 */
 	public String getMember(String number){
 		String memberName = "UNKNOWN";
 		Cursor mCursor = getDb().query(true, TABLE_GROUPMEMBERS, new String[] {KEY_MEMBER}, KEY_PHONENUMBER + "=" + "'" + number + "'",
 				null, null, null, null, null);
 
 		if (mCursor != null && mCursor.moveToFirst()){
 			memberName = mCursor.getString(mCursor.getColumnIndex(KEY_MEMBER));
 		}
 		
 		mCursor.close();
 		mCursor.deactivate();
 		return memberName;
 	}
 
 	/**
 	 * Takes a groupName and returns the groupID corresponding to that groupName
 	 * 
 	 * @param String groupName
 	 * @return long groupID
 	 */
 	public long getGroupIDFromGroupName(String groupName) {
 		long groupID = (long)-1;
 		Cursor gCursor = getDb().query(true, TABLE_GROUPS, new String[] {KEY_GROUPSID}, KEY_NAME + "=" + "'" + groupName + "'",
 				null, null, null, null, null);
 
 		if (gCursor != null && gCursor.moveToFirst()){
 			groupID = gCursor.getLong(gCursor.getColumnIndex(KEY_GROUPSID));
 		}
 		
 		gCursor.close();
 		gCursor.deactivate();
 		return groupID;
 	}
 
 }
