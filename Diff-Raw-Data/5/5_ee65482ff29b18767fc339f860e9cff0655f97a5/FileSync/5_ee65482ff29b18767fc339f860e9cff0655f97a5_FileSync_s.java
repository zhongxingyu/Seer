 /*
  * Chris card
  * Nathan Harvey
  * 11/15/12
  * This class provides code to sync with google calandars or other files
  * and will be started by a intent service and will be using a singleton pattern
  */
 
 package csci422.CandN.to_dolist;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.database.Cursor;
 import android.net.MailTo;
 import android.net.Uri;
 import android.provider.CalendarContract;
 import android.text.format.DateFormat;
 import android.text.method.DateTimeKeyListener;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 
 public class FileSync {
 
	//Singleton patter stores its own instance
 	private static FileSync instance = null;
 
 	//this flag will be set true if the user updated preferences to
 	//sync with google cal
 	private boolean toSyncCal;
 
 	//to be used later but for sync the data base
 	//with the todo.txt file format or just a file
 	private boolean toSaveFile;
 
 	//this tells other methods that sync is running so that they will not
 	//with the state of this class
 	private static boolean isRunning;
 
 	/**
 	 * Private constructor so that this class controls
 	 * and ensures only one instance of the class
 	 */
 	private FileSync()
 	{
 		toSyncCal = false;
 		toSaveFile = false;
 		isRunning = false;
 	}
 
 	/**
 	 * This class returns an instance of its self if instance exists
 	 * or creates a new instance (enforces the singleton pattern)
 	 * @return: new instance if one doesn't exist or a single instance
 	 * 			of the class
 	 */
 	public static FileSync getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new FileSync();
 		}
 
 		return instance;
 	}
 
 	/**
 	 * this method loads the past state of this class from synchelper
 	 * @param h data base from which it will get the previous state
 	 */
 	public void load(SyncHelper h)
 	{
 		Cursor c = h.getAll();
 		if(c.moveToFirst())
 		{
 			if(h.getSync(c).equals("true"))
 			{
 				toSyncCal = true;
 			}
 
 			if(h.getSave(c).equals("true"))
 			{
 				toSaveFile = true;
 			}
 		}
 		c.close();
 	}
 
 	/**
 	 * This saves the current state of the flags to SyncHelper data base
 	 * @param h place to save to
 	 */
 	public void save(SyncHelper h)
 	{
 		Cursor c = h.getAll();
 		if(c.moveToFirst())
 		{//if the data base has at least one only needs one entry
 			h.update(c.getString(0), toSyncCal, toSaveFile);
 		}
 		else
 		{//other wise insert into the data base
 			h.insert(toSyncCal, toSaveFile);
 		}
 		c.close();
 	}
 
 
 	/**
 	 * Toggles toSyncCal to the opposite value of what it is at the moment
 	 */
 	public void toggleCalSync()
 	{
 		//toggles toSync to the opposite of what it is 
 		toSyncCal = (toSyncCal ? false : true);
 	}
 
 	/**
 	 * This method returns if it is to sync with google calender
 	 * @return: true if it is to sync with calendar
 	 */
 	public boolean isSyncCal()
 	{
 		return toSyncCal;
 	}
 
 	/**
 	 * This method returns if it is to sync with a todo.txt file
 	 * @return: true if it is to sync with file
 	 */
 	public boolean isSaveFile()
 	{
 		return toSaveFile;
 	}
 
 	/**
 	 * Toggles toSaveFile to the opposite value of what it is at the moment
 	 * This alows us to set the value from another file with out having to know its value
 	 */
 	public void toggleSaveFile()
 	{
 		toSaveFile = (toSaveFile ? false : true);
 	}
 
 
 	/**
 	 * WARNING: ONLY CALL THIS FUNCTION FROM AN INTENTSERVICE OR AYSNCTASK OR RISK SLOWING DOWN
 	 *			THE UI THREAD OR HAVE IT STOP BECAUSE USER CLOSES APP
 	 *
 	 * This function saves the entire data base stored in ToDoHelper to the
 	 * CalenderContract api (aka google calendar)
 	 * @param cr 
 	 * 
 	 * @param: ToDoHelper that has the data base since this class can't get its
 	 *			calling context.
 	 * @throws ParseException 
 	 */
 	@Deprecated
 	public void syncWithCal(ToDoHelper help, ContentResolver cr) throws ParseException
 	{
 		//if (toSyncCal)  We already checked for this.
 		Cursor c = help.getAll("title");
 		SimpleDateFormat df = new SimpleDateFormat();
 		df.setLenient(true);
		for(c.moveToFirst(); !c.isLast(); c.moveToNext()){
 			ContentValues cvEvent = new ContentValues();
 			cvEvent.put("calendar_id", 1);
 			cvEvent.put("title", help.getTitle(c));
 			cvEvent.put("dtstart", Date.parse(help.getDate(c)));
 			//cvEvent.put("hasAlarm", 1);
 			cvEvent.put("description",help.getNotes(c));//TODO expand this later to include progress,etc.
 			cvEvent.put("dtend", Date.parse(help.getDate(c))+36E5);
 			cr.insert(Uri.parse("content://com.android.calendar/events"), cvEvent);			
 		}
 		c.close();
 	}
 
 	/**
 	 * WARNING: ONLY CALL THIS FUNCTION FROM AN INTENTSERVICE OR AYSNCTASK OR RISK SLOWING DOWN
 	 *			THE UI THREAD OR HAVE IT STOP BECAUSE USER CLOSES APP
 	 *
 	 * This function saves the entire data base stored in ToDoHelper to a
 	 * file the might be able to be moved to another app (to be implemented latter mabe)
 	 * 
 	 * @param: ToDoHelper that has the data base since this class can't get its
 	 *			calling context.
 	 */
 	public void saveToFile(ToDoHelper help)
 	{
 		if (toSaveFile) 
 		{
 			Cursor c = help.getAll("title");
 			c.moveToFirst();
 
 			//TODO: Nathan put function to save to the file LATER NOT NOW
 
 			c.close();
 		}//else do nothing
 	}
 
 
 }
