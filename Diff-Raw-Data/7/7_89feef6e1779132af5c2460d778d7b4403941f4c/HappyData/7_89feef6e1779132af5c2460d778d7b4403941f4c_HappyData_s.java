 package org.jbs.happysad;
 
 import static android.provider.BaseColumns._ID;
 import static org.jbs.happysad.Constants.EMO;
 import static org.jbs.happysad.Constants.LAT;
 import static org.jbs.happysad.Constants.LONG;
 import static org.jbs.happysad.Constants.SYNC;
 import static org.jbs.happysad.Constants.MSG;
 import static org.jbs.happysad.Constants.TABLE_NAME;
 import static org.jbs.happysad.Constants.TIME;
 import static org.jbs.happysad.Constants.UID;
 import java.util.ArrayList;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import org.jbs.happysad.HappyBottle;
 import android.util.Log;
 /**
  * helper class that serves as a layer of abstraction between the user and the database.
  * @author HappyTrack
  */
 public class HappyData {
 	//for debugging purposes, delete when no longer needed
 	private static final String TAG = "HappyData";
 
 	//fields
 	private HappyDB h;
 	//private static final long MyUserID = 1; //this should not be hardcoded to 1
 	private UIDhelper UIDh =  new UIDhelper();
 	
 
 	long myID = -1; 
 	private static String[] FROM = { _ID, UID, LAT, LONG, EMO, MSG, TIME, SYNC };
 	private static String ORDER_BY = TIME + " DESC";
 	private NetHelper net = new NetHelper();
 
 	public HappyData(Context ctx){
 		h = new HappyDB(ctx);
 		//mainThread = new Handler();
 		
 		myID = UIDh.getUID();
 	}
 
 	//Return true/false if the application can add HappyBottle b
 	protected boolean addBottle(HappyBottle b){
 		return addBottle(b, false);
 	}
 
 	/**
 	 * Tries to add a bottle to the local db. Return true/false if it was successful or not.
 	 * @param b HappyBottle
 	 * @param isSynced Has this bottle been uploaded to the server?
 	 * @return
 	 */
 	public synchronized boolean addBottle(HappyBottle b, boolean isSynced){
 		boolean toreturn = false;
 		//get the db
 		SQLiteDatabase db = h.getWritableDatabase();
 		ContentValues values = b.getAll();
 		int z = isSynced?1:0;
 		values.put(SYNC, z);
 		//add a bottle. 
 		try {
 			db.insertOrThrow(TABLE_NAME, null, values);
 			toreturn = true;	
 		}
 		catch(Exception e){
 			Log.e(TAG, e.toString());
 			toreturn = false;
 		}
 		finally{
 			h.close();
 		}	
 		return toreturn;
 	}
 
 	
 	//for each entry that isn't synced, send to database. 
 	protected void syncUp(){
 		SQLiteDatabase db = h.getReadableDatabase();
		Cursor cursor = getCursor(db);
 		//iterate through the database
 		while (cursor.moveToNext() ){
 			long id = cursor.getLong(1);
 			int synced = cursor.getInt(7);
 			//now we check each row in the database to see if it's been synced yet.
 			if ((id == myID) && (synced == 0)) {
 				//if we hit a bottle that has not been synced, and is ours, we upload it.
 				HappyBottle b = createBottle(cursor);
 				Log.e(TAG, "NETSEND");
 				int rowid = cursor.getInt(0);
 
 				net.doTask(Task.SEND, b);
 				Log.e(TAG, "REMOVEBYID");
 				SQLiteDatabase dbwrite = h.getWritableDatabase();
 				ContentValues c = new ContentValues();
 				c.put(SYNC, 1);
 				Log.d(TAG, "update query is: "+ _ID+"="+rowid);
 				Log.d(TAG, "for msg: " + cursor.getString(5));
 				dbwrite.update(TABLE_NAME, c, _ID+"="+rowid , null);
 				dbwrite.close();
				//we do two HORRIBLY WRONG things here.
				//a. DONE instead of deleting a row in the db only to insert an almost-identical one, we should just alter the row in the db instead. 
				//b. instead of looping through the cursor, we should make the appropriate call and let the server find the matching rows for us.
 			}
 
 		} 
 		cursor.close();
 		db.close(); 
 	}
 
 	//syncs down everything.
 	protected void syncDown(){
 		//syncMyDown();
 		syncAllDown();
 		Log.d(TAG, "synced down");
 
 	}
 
 	//same as syncAllDown, but just for my bottles
 	protected void syncMyDown(){
 		ArrayList<HappyBottle> b = net.doTask(Task.GETMINE, null);
 		addAvoidDupes(b);
 	}
 
 	//sync all down, then add the bottles to the database, avoiding duplicates
 	private void syncAllDown(){
 		ArrayList<HappyBottle> b = net.doTask(Task.GETALL, null);
 		addAvoidDupes(b);
 	}
 
 	//given the rowid of a row in the local database of a bottle, delete it.
 	private void removeByID(long rowid){
 		Log.e(TAG, "REMOVEBYID STARTED");
 		SQLiteDatabase db = h.getWritableDatabase();
 		db.delete(TABLE_NAME, "_ID==" + rowid, null );
 		db.close();
 	}
 
 	//add bottles to the database but only if they are not dupes
 	private void addAvoidDupes(ArrayList<HappyBottle> a){
 		for (HappyBottle b : a){
 			long time = b.getTime();
 			long userId = b.getUID();
 			SQLiteDatabase db = h.getReadableDatabase();
 			String[] columns = {_ID, MSG};
 			//if there is nothing with the same uid and time as the bottle in your localdb add it to the db
 			Cursor c = db.query(TABLE_NAME, columns, UID+"=\'"+userId+"\' AND "+TIME+"="+time, null, null, null, null);
 			if (c.getCount() == 0){
 
 				Log.d(TAG, "adding bottle fromthe internets!");
 				this.addBottle(b, true); //was true
 			}
 			c.close();
 			db.close();
 		}
 	}
 
 	/**
 	 * This method creates an ArrayList of HappyBottles that id == MyUserID form the local database
 	 * @return the ArrayList of HappyBottles
 	 */
 	public ArrayList<HappyBottle> getMyHistory(){
 		SQLiteDatabase db = h.getReadableDatabase();
 		Cursor cursor = getCursor(db);
 		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
 		//call the database 
 		while (cursor.moveToNext() ){
 			long id = cursor.getLong(1);
 			if (id == myID) {
 				//for each entry in the database, where the user id == MyUserID
 				HappyBottle b = createBottle(cursor);
 				a.add(b);
 			}
 		}
 		cursor.close();
 		db.close();
 		return a;
 	}
 
 	/**
 	 * This method creates an ArrayList of HappyBottles to create AllHistory from the local database
 	 * @return an ArrayList of HappyBottles
 	 */
 	public ArrayList<HappyBottle> getAllHistory(){
 		//see getMyHistory. Same idea.
 		SQLiteDatabase db = h.getReadableDatabase();
 		Cursor cursor = getCursor(db);
 		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
 		while (cursor.moveToNext() ){
 			HappyBottle b = createBottle(cursor);
 			a.add(b);
 		}
 		cursor.close();
 		db.close();
 		return a;
 	}
 
 	/* 
 	 * Given the cursor that is pointing to a row in the database
 	 * Pull the info from that row and create a bottle object
 	 */
 	private HappyBottle createBottle(Cursor cursor){
 		long uid = cursor.getLong(1);
 		int latitude = cursor.getInt(2);
 		int longitude = cursor.getInt(3);
 		short emo = cursor.getShort(4);
 		String msg = cursor.getString(5);
 		long time = cursor.getLong(6);
 
 		HappyBottle b = new HappyBottle(uid, latitude, longitude, emo, msg, time);
 		return b;
 	}
 
 	//Provides random read-write access to the result set returned by a database query.
 	private Cursor getCursor(SQLiteDatabase db){
 		Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null,
 				null, ORDER_BY);
 		return cursor;
 	}
 
 
 }
