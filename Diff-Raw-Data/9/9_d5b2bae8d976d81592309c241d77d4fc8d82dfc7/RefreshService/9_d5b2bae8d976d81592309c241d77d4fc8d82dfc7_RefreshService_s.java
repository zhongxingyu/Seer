 package com.marakana.yamba;
 
 import java.util.List;
 import com.marakana.android.yamba.clientlib.YambaClient.Status;
 import android.app.IntentService;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class RefreshService extends IntentService {	
 	private static final String TAG = RefreshService.class.getSimpleName();
 	
 	public RefreshService() {
 		super(TAG);
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d(TAG, "onCreate()");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		try {
 			Log.d(TAG, "onHandleIntent()");
 			//get all the posts from the timeline
 			List<Status> timeline = ((YambaApplication) getApplication()).getYambaClient().getTimeline(20);
 			
 			//get the database and prepare the insert
 			DbHelper dbHelper = new DbHelper(this);
 			SQLiteDatabase db = dbHelper.getWritableDatabase();
 			ContentValues values = new ContentValues();
 			
 			for (Status status:timeline)
 			{
 				Log.d(TAG, "createdAt: " + status.getCreatedAt() + ", user: " + status.getUser() + ", posted: " + status.getMessage());
 				
 				//insert into the tables values??
 				values.clear();
 				
 				values.put(StatusContract.Column.C_ID, status.getId());
 				values.put(StatusContract.Column.C_CREATED_AT, status.getCreatedAt().getTime());
 				values.put(StatusContract.Column.C_USER, status.getUser());
 				values.put(StatusContract.Column.C_TXT, status.getMessage());
				db.insert(StatusContract.TABLE, null, values);
 			}
 			
 			//close the db
 			db.close();
 			
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroy()");
 	}
 
 }
