 package org.minyanmate.minyanmate.models;
 
 import android.content.Context;
 import android.database.Cursor;
 
 import org.minyanmate.minyanmate.MinyanScheduleSettingsActivity;
 import org.minyanmate.minyanmate.database.MinyanSchedulesTable;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 /**
  * A model class containing the attributes of objects from the 
  * {@link MinyanSchedulesTable#TABLE_MINYAN_SCHEDULES} table. Contains 
  * {@link MinyanSchedule#schedFromCursor(Cursor)} and {@link MinyanSchedule#cursorToScheduleList(Cursor)}
  * static helper methods to pull Prayer objects from the cursors.
  */
 public class MinyanSchedule {
 
     // TODO implement Parcelable
 
     public static final String RESPONSE_API_INSTRUCTIONS = ". Can you come? Please " +
            "respond either \"accept\" or \"decline\". Thank you.";
    public static final int SCHEDULE_MESSAGE_SIZE_LIMIT = 52;
 
     private int _id;
 	private String day;
 	private String prayerName;
 	private int dayNum;
 	private int prayerNum;
 	private int hour;
 	private int minute;
 	private long scheduleWindowLength;
 	private boolean isActive;
 	private String inviteMessage;
 	
 	public MinyanSchedule(int id, String day, int dayNum, 
 			int prayerNum, String prayerName, long winLen, int hour, int minute, 
 			boolean isActive, String msg) {
 		this(day, dayNum, prayerNum, prayerName, winLen, hour, minute, isActive, msg);
 		this._id = id;
 	}
 	
 	public MinyanSchedule(String day, int dayNum, int prayerNum, String prayerName, long winLen, int hour,
 			int minute, boolean isActive, String msg) {
 		this.day = day;
 		this.dayNum = dayNum;
 		this.prayerNum = prayerNum;
 		this.hour = hour;
 		this.minute = minute;
 		this.isActive = isActive;
 		this.scheduleWindowLength = winLen;
 		this.prayerName = prayerName;
 		this.inviteMessage = msg;
 	}
 
     /**
      * A helper method for formatting the final content of the SMS to be sent to an
      * {@link org.minyanmate.minyanmate.models.InvitedMinyanGoer}.
      * @param context a context passed to
      *      {@link org.minyanmate.minyanmate.MinyanScheduleSettingsActivity#formatTimeTextView(android.content.Context, int, int)}
      *                to facilitate time localization
      * @param userCustomMsg a user's personalized custom message to be prepended to the default message
      * @param prayerName the name of the prayer, see
      *      {@link org.minyanmate.minyanmate.database.MinyanSchedulesTable#COLUMN_PRAYER_NAME}
      * @param prayerHour the hour of the prayer, see
      *                   {@link org.minyanmate.minyanmate.database.MinyanSchedulesTable#COLUMN_PRAYER_HOUR}
      * @param prayerMinute the minute of the prayer, see
      *                  {@link org.minyanmate.minyanmate.database.MinyanSchedulesTable#COLUMN_PRAYER_MIN}
      * @return a String with the formatted final message
      */
     public static String formatInviteMessage(Context context, String userCustomMsg, String prayerName, int prayerHour, int prayerMinute) {
         String truncatedUserCustomMsg = userCustomMsg.substring(0, Math.min(userCustomMsg.length(),
                 SCHEDULE_MESSAGE_SIZE_LIMIT));
 
         return truncatedUserCustomMsg + (truncatedUserCustomMsg.trim().length() > 0 ? " " : "")
                 + prayerName +  " will be at " +
                 MinyanScheduleSettingsActivity.formatTimeTextView(context, prayerHour, prayerMinute)
                 + RESPONSE_API_INSTRUCTIONS;
     }
 
     public int getPrayerNum() {
 		return prayerNum;
 	}
 	
 	public String getDay() {
 		return day;
 	}
 	
 	public int getDayNum() {
 		return dayNum;
 	}
 	
 	public int getHour() {
 		return hour;
 	}
 	
 	public int getMinute() {
 		return minute;
 	}
 	
 	public boolean isActive() {
 		return isActive;
 	}
 	
 	public int getId() {
 		return _id;
 	}
 	
 	public long getSchedulingWindowLength() {
 		return scheduleWindowLength;
 	}
 	
 	public int getSchedulingWindowHours() {
 		return (int) TimeUnit.MILLISECONDS.toHours(scheduleWindowLength);
 	}
 	
 	public int getSchedulingWindowMinutes() {
 		return (int) TimeUnit.MILLISECONDS.toMinutes(scheduleWindowLength) % 60;
 	}
 	
 	public String getPrayerName() {
 		return prayerName;
 	}
 	
 	public String getInviteMessage() {
 		return inviteMessage;
 	}
 	
 	/**
 	 * A helper function to extract the columns from a cursor over the 
 	 * {@link MinyanSchedulesTable#TABLE_MINYAN_SCHEDULES} and returns a new {@link MinyanSchedule} object
 	 * based upon it. 
 	 * <p>
 	 * @param cursor a cursor over the {@link MinyanSchedulesTable#TABLE_MINYAN_SCHEDULES} table
 	 * which is already moved to a specific row. 
 	 * @return prayer, a new {@link MinyanSchedule} object
 	 */
 	public static MinyanSchedule schedFromCursor(Cursor cursor) {
 		
 		int id = cursor.getInt(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_SCHEDULE_ID));
 		int dayNum = cursor.getInt(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_DAY_NUM));
 		long winLen = cursor.getLong(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_SCHEDULE_WINDOW));
 		int hour = cursor.getInt(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_PRAYER_HOUR));
 		int min = cursor.getInt(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_PRAYER_MIN));
 		int prayerNum = cursor.getInt(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_PRAYER_NUM));
 		boolean isActive = (cursor.getInt(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_IS_ACTIVE)) == 1);
 		String dayName = cursor.getString(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_DAY_NAME));
 		String prayerName = cursor.getString(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_PRAYER_NAME));
 		String invMsg = cursor.getString(cursor.getColumnIndex(MinyanSchedulesTable.COLUMN_SCHEDULE_MESSAGE));
 		
 		return new MinyanSchedule(id, dayName, dayNum, prayerNum, prayerName, winLen,hour, min, isActive, invMsg);
 	}
 	
 	/**
 	 * Given a cursor over a set of multiple results from the 
 	 * {@link MinyanSchedulesTable#TABLE_MINYAN_SCHEDULES} table, iterate over the cursor using
 	 * {@link MinyanSchedule#schedFromCursor(Cursor)} to generate a list of {@link MinyanSchedule} objects.
 	 * <p>
 	 * @param cursor a new cursor over the {@link MinyanSchedulesTable#TABLE_MINYAN_SCHEDULES} table.
 	 * @return prayerList, a list of new {@link MinyanSchedule} objects
 	 */
 	public static List<MinyanSchedule> cursorToScheduleList(Cursor cursor) {
 		
 		List<MinyanSchedule> prayerList = new ArrayList<MinyanSchedule>();
 		while(cursor.moveToNext()) {
 			prayerList.add(schedFromCursor(cursor));
 		}
 		
 		return prayerList;
 	}
 }
