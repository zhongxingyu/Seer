 package info.voidptr.nomads;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.http.AndroidHttpClient;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.widget.TextView;
 
 public class NomadViewer extends Activity {
 	public static final String TAG = "NomadViewer";
 	private static String DATABASE_NAME = "nomad_db";
 	private static int DATABASE_VERSION = 1;
 	private static String ACTIVITY_URL = "http://nomad.heroku.com/activity/plain.json";
 	private static String USERS_URL = "http://nomad.heroku.com/users.json";
 	private static String SUGGESTIONS_URL = "http://nomad.heroku.com/suggestions.json";
 	
     /**
      * Suggestions table
      */
     public static final class Suggestions implements BaseColumns {
         // This class cannot be instantiated
         private Suggestions() {}
         public static final String DEFAULT_SORT_ORDER = "modified DESC";
         
         // Columns
         public static final String TITLE = "title";
         public static final String CONTENT = "content";
         public static final String LAT = "lat";
        public static final String LON = "lon";
         public static final String CREATED_AT = "created_at";
         public static final String UPDATED_AT = "updated_at";
         public static final String ICON_ID = "icon_id";
         public static final String USER_ID = "user_id";
     }
     
     /**
      * Users table
      */
     public static final class Users implements BaseColumns {
         // This class cannot be instantiated
         private Users() {}
         public static final String DEFAULT_SORT_ORDER = "_id DESC";
         
         // Columns
         public static final String NAME = "name";
         public static final String EMAIL = "email";
         public static final String FULLNAME = "fullname";
         public static final String CREATED_AT = "created_at";
         public static final String UPDATED_AT = "updated_at";
         public static final String ADMIN = "admin";
     }
 	
     private static class DatabaseHelper extends SQLiteOpenHelper {
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL("CREATE TABLE users ("
             		+ Users._ID + " INTEGER PRIMARY KEY,"
             		+ Users.NAME + " VARCHAR(255),"
             	    + Users.EMAIL + " VARCHAR(255),"
             	    + Users.FULLNAME + " VARCHAR(255),"
             	    + Users.CREATED_AT + " TIMESTAMP,"
             	    + Users.UPDATED_AT + " TIMESTAMP,"
             	    + Users.ADMIN + " BOOLEAN"
                     + ");");
 
             db.execSQL("CREATE TABLE suggestions ("
                     + Suggestions._ID + " INTEGER PRIMARY KEY,"
                     + Suggestions.TITLE + " VARCHAR(255),"
                     + Suggestions.CONTENT + " TEXT,"
                     + Suggestions.LAT + " FLOAT,"
                     + Suggestions.LON + " FLOAT,"
                     + Suggestions.CREATED_AT + " TIMESTAMP,"
                     + Suggestions.UPDATED_AT + " TIMESTAMP,"
                     + Suggestions.ICON_ID + " INTEGER,"
                    + Suggestions.USER_ID + " INTEGER"
                     + ");");
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS suggestions");
             onCreate(db);
         }
     }
 
 	Handler handler;
 	DatabaseHelper db;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         handler = new Handler();
         db = new DatabaseHelper(this);
        SQLiteDatabase sdb =  db.getWritableDatabase();
         setContentView(R.layout.main);
         fetchUpdates();
     }
 
 	private void fetchUpdates() {
 		new Thread(new Runnable() { @Override
 		public void run() {
 			showText("Fetching activity from " + ACTIVITY_URL);
 			AndroidHttpClient client = AndroidHttpClient.newInstance("Nomad App");
 			
 			// Fetch recent activity
 			try {
 				HttpResponse res = client.execute((HttpUriRequest) new HttpGet(ACTIVITY_URL));
 				showText("Got statuses...");
 				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
 				String actions = reader.readLine();
 
 				Log.i(TAG, "Actions: " + actions);
 				showText(formatActions(actions));
 			} catch (IOException e) {
 				showText("Failure fetching recent activity: " + e.getClass().getName() + " -- " + e.getMessage());
 				Log.w(TAG, e.getMessage());
 			}
 			
 			// Fetch Users table
 			try {
 				HttpResponse res = client.execute((HttpUriRequest) new HttpGet(USERS_URL));
 				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
 				StringBuffer content_buffer = new StringBuffer();
 				String line = reader.readLine();
 				while(line != null) {
 					content_buffer.append(line + "\n");
 					line = reader.readLine();
 				}
 				String json_str = content_buffer.toString();
 
 				Log.i(TAG, json_str);
 			} catch (IOException e) {
 				showText("Failure fetching users: " + e.getClass().getName() + " -- " + e.getMessage());
 				Log.w(TAG, e.getMessage());
 			}
 			
 			// Fetch Suggestions table
 			try {
 				HttpResponse res = client.execute((HttpUriRequest) new HttpGet(SUGGESTIONS_URL));
 				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
 				StringBuffer content_buffer = new StringBuffer();
 				String line = reader.readLine();
 				while(line != null) {
 					content_buffer.append(line + "\n");
 					line = reader.readLine();
 				}
 				String json_str = content_buffer.toString();
 
 				Log.i(TAG, json_str);
 			} catch (IOException e) {
 				showText("Failure: " + e.getClass().getName() + " -- " + e.getMessage());
 				Log.w(TAG, e.getMessage());
 			}
 			
 			client.close();
 		} }).start();
 	}
 
 	private String formatActions(String actions) {
 		String ret = "<error parsing suggestions>";
 		try {
 			JSONArray json = new JSONArray(actions);
 			ret = "";
 			for(int i = 0; i < json.length(); i++) {
 				ret = ret + json.getString(i) + "\n\n";
 			}
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to parse actions: " + e.getClass().getName() + " -- " + e.getMessage());
 		}
 		return ret;
 	}
 
 	private void showText(final String txt) {
 		// Don't you just love Java? 
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				TextView display = (TextView) findViewById(R.id.display);
 				display.setText(txt);
 			}
 		};
 		handler.post(r);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		fetchUpdates();
 	}
 }
