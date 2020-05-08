 package ca.jcnc.vroomvroom;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	public static final String DATABASE_NAME = "transit_info";
 	public static final int DATABASE_VERSION = 1;
 	private static Context mContext;
 	
 	// Data in these tables MAY NOT BE CHANGED BY USER
 	// Must all be the official information from the transit authority
	private static TransitDbAdapter[] transitAdapters;
 	
 	DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		mContext = context;
		transitAdapters = new TransitDbAdapter[]{new Stops(mContext)};
 	}
 	
 	public void onCreate(SQLiteDatabase db) {
 		for (int i = 0; i < transitAdapters.length; i += 1) {
 			db.execSQL(transitAdapters[i].getCreateSql());
 			transitAdapters[i].importTransitInformation();
 		}
 	}
 	
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		for (int i = 0; i < transitAdapters.length; i += 1)
 			db.execSQL("DROP TABLE IF EXISTS " + transitAdapters[i].getTableName());
 		
 		onCreate(db);
 	}
 }
