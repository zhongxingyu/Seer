 package com.ijmacd.gpstools.mobile;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.SystemClock;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Iain
  * Date: 27/06/13
  * Time: 22:35
  * To change this template use File | Settings | File Templates.
  */
 public class Track {
    private static final String[] mTrackColumns = new String[]{
            DatabaseHelper.ID_COLUMN,
            DatabaseHelper.NAME_COLUMN,
            DatabaseHelper.DATE_COLUMN,
            DatabaseHelper.DISTANCE_COLUMN,
            DatabaseHelper.DURATION_COLUMN,
            DatabaseHelper.COMPLETE_COLUMN
    };
     private final List<Point> mPoints = new ArrayList<Point>(BUFFER_POINTS);

     private float mDistance;
     private float mDuration;
     private long mStartTime;
     private String mName;
 
     private long mTrackId;
 
     private Context mContext;
     private DatabaseHelper mDatabase;
 
     private Point mLastPoint;
 
     private static final int BUFFER_POINTS = 0;

     private int mComplete = 0;
 
     public Track(Context context){
         // I've heard this helps mitigate leaking activity contexts
         mContext = context.getApplicationContext();
 
         mDatabase = new DatabaseHelper(mContext);
 
         mStartTime = System.currentTimeMillis();
 
         createDatabaseTrack();
     }
 
     private Track(long trackId) {
         mTrackId = trackId;
     }
 
     public void addPoint(Point point){
         if(mLastPoint != null){
             mDistance += mLastPoint.distanceTo(point);
         }
 
         mPoints.add(point);
         if(mPoints.size() >= BUFFER_POINTS){
             commitPointsToDatabase();
         }
 
         mLastPoint = point;
 
         notifyTrackChanged();
     }
 
     public void setName(String name){
         mName = name;
         updateDatabaseTrack(null);
     }
 
     public void save() {
         commitPointsToDatabase();
     }
 
     public void close() {
         commitPointsToDatabase();
         mComplete = 1;
         updateDatabaseTrack(null);
     }
 
     /**
      * Returns overall distance of the track in metres
      * @return track distance in metres
      */
     public float getDistance(){
         return mDistance;
     }
 
     /**
      * Returns overall duration of the track in seconds
      * @return track duration in seconds
      */
     public float getDuration(){
         if(mLastPoint != null){
             return (mLastPoint.getTime() - mStartTime)/1000f;
         }
         return mDuration;
     }
     public Date getStartDate(){
         return new Date(mStartTime);
     }
     public Date getEndDate(){
         if(mLastPoint != null){
             return new Date(mLastPoint.getTime());
         }
         return new Date();
     }
 
     public boolean isComplete(){
         return mComplete > 0;
     }
 
 
     private void createDatabaseTrack(){
         if(mStartTime == 0){
             mStartTime = System.currentTimeMillis();
         }
         SQLiteDatabase db = mDatabase.getWritableDatabase();
         ContentValues cv = new ContentValues();
         cv.put(DatabaseHelper.DATE_COLUMN, mStartTime);
         mTrackId = db.insert(DatabaseHelper.TRACK_TABLE_NAME, DatabaseHelper.DATE_COLUMN, cv);
         db.close();
     }
 
     private void updateDatabaseTrack(SQLiteDatabase db){
         if(db == null){
             db = mDatabase.getWritableDatabase();
         }
         ContentValues cv = new ContentValues();
         cv.put(DatabaseHelper.NAME_COLUMN, getName());
         cv.put(DatabaseHelper.DISTANCE_COLUMN, mDistance);
         cv.put(DatabaseHelper.DURATION_COLUMN, getDuration());
         cv.put(DatabaseHelper.COMPLETE_COLUMN, mComplete);
         db.update(DatabaseHelper.TRACK_TABLE_NAME,
                 cv,
                 DatabaseHelper.ID_COLUMN + " = ?",
                 new String[]{ String.valueOf(mTrackId) });
         db.close();
     }
 
     private void commitPointsToDatabase(){
         SQLiteDatabase db = mDatabase.getWritableDatabase();
         ContentValues cv;
         for(Point point : mPoints){
             cv = new ContentValues();
             cv.put(DatabaseHelper.TRACK_ID_COLUMN, mTrackId);
             cv.put(DatabaseHelper.DATE_COLUMN, point.getTime());
             cv.put(DatabaseHelper.LAT_COLUMN, point.getLatitude());
             cv.put(DatabaseHelper.LON_COLUMN, point.getLongitude());
             cv.put(DatabaseHelper.ALTITUDE_COLUMN, point.getAltitude());
             cv.put(DatabaseHelper.ACCURACY_COLUMN, point.getAccuracy());
             cv.put(DatabaseHelper.SPEED_COLUMN, point.getSpeed());
             cv.put(DatabaseHelper.HEADING_COLUMN, point.getHeading());
             db.insert(DatabaseHelper.TRACKPOINT_TABLE_NAME, DatabaseHelper.TRACK_ID_COLUMN, cv);
         }
         updateDatabaseTrack(db);
         db.close();
         mPoints.clear();
     }
 
     private final ArrayList<OnTrackChangedListener> mTrackChangedListeners = new ArrayList<OnTrackChangedListener>();
 
     private void notifyTrackChanged(){
         for(OnTrackChangedListener listener : mTrackChangedListeners){
             listener.onTrackChanged(this);
         }
     }
 
     public void registerForTrackChanges(OnTrackChangedListener listener){
         mTrackChangedListeners.add(listener);
     }
 
     public void unregisterForTrackChanges(OnTrackChangedListener listener){
         mTrackChangedListeners.remove(listener);
     }
 
     public String getName() {
         if(mName == null){
             mName = "Track " + mTrackId;
         }
         return mName;
     }
 
     public long getID() {
         return mTrackId;
     }
 
     public void delete() {
         SQLiteDatabase db = mDatabase.getWritableDatabase();
         db.delete(DatabaseHelper.TRACK_TABLE_NAME,
                 DatabaseHelper.ID_COLUMN + " = ?",
                 new String[]{ String.valueOf(mTrackId) });
         db.close();
     }
 
     public Cursor getPoints() {
         commitPointsToDatabase();
         SQLiteDatabase db = mDatabase.getReadableDatabase();
         return db.query(DatabaseHelper.TRACKPOINT_TABLE_NAME,
                 new String[]{
                         DatabaseHelper.DATE_COLUMN,
                         DatabaseHelper.LAT_COLUMN,
                         DatabaseHelper.LON_COLUMN,
                         DatabaseHelper.ALTITUDE_COLUMN
                 },
                 DatabaseHelper.TRACK_ID_COLUMN + " = ?",
                 new String[]{ String.valueOf(mTrackId) },
                 null, null,
                 DatabaseHelper.DATE_COLUMN);
     }
 
     public interface OnTrackChangedListener{
         void onTrackChanged(Track target);
     }
 
     public static Track getTrack(Context context, long trackId) {
         DatabaseHelper helper = new DatabaseHelper(context);
         SQLiteDatabase db = helper.getReadableDatabase();
         Cursor trackCursor = db.query(
                 DatabaseHelper.TRACK_TABLE_NAME,
                mTrackColumns,
                 DatabaseHelper.ID_COLUMN + " = ?",
                 new String[]{
                         String.valueOf(trackId)
                 },
                 null, null, null);
 
         return cursorToTrack(context, helper, trackCursor);
 
     }
 
     public static Track getLatestTrack(Context context) {
         DatabaseHelper helper = new DatabaseHelper(context);
         SQLiteDatabase db = helper.getReadableDatabase();
         Cursor trackCursor = db.query(
                 DatabaseHelper.TRACK_TABLE_NAME,
                mTrackColumns,
                 null, null,
                 null, null, DatabaseHelper.ID_COLUMN + " DESC");
 
         return cursorToTrack(context, helper, trackCursor);
     }
 
     private static Track cursorToTrack(Context context, DatabaseHelper helper, Cursor cursor){
         if(cursor.moveToFirst()){
            final long trackId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID_COLUMN));
             Track track = new Track(trackId);
             track.mName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COLUMN));
             track.mStartTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.DATE_COLUMN));
             track.mDistance = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.DISTANCE_COLUMN));
             track.mDuration = cursor.getFloat(cursor.getColumnIndex(DatabaseHelper.DURATION_COLUMN));
             track.mComplete = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COMPLETE_COLUMN));
 
             track.mContext = context;
             track.mDatabase = helper;
             return track;
         }
         throw new TrackException("Tried to get a track which doesn't exist");
     }
 
     public static class TrackException extends RuntimeException {
         public TrackException(String message){
             super(message);
         }
     }
 }
