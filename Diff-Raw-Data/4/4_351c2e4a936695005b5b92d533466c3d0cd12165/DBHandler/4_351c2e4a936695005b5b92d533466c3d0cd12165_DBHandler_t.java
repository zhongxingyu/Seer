 package coupling.app.data;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DBHandler extends SQLiteOpenHelper {
 
 	public DBHandler(Context context) {
		super(context,"coupling.db", null, 15);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		database.execSQL(
 				"CREATE TABLE ShopListOverview ( " +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"UId INTEGER NULL," + 
 						"Title VARCHAR(50), " +
 						"TotalItems Int DEFAULT 0, " +
 						"IsMine INTEGER DEFAULT 1, " +
 						"CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP " +
 						");"
 				);
 		database.execSQL(
 				"CREATE TABLE ShopList ( " +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"UId INTEGER ," + 
 						"ShopListId INTEGER, " +
 						"ItemName VARCHAR(50), " +
 						"ItemQuantity INTEGER DEFAULT 1, " +
 						"ItemStatus INTEGER DEFAULT 0, " +
 						"IsMine INTEGER DEFAULT 1, " +
 						"FOREIGN KEY(ShopListId) REFERENCES ShopListOverview(_id)" +
 						");"
 				);
 		database.execSQL(
 				"CREATE TABLE GroceryList ( " +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"ItemName VARCHAR(50)" + 
 						");"
 				);
 		database.execSQL(
 				"CREATE TABLE CalendarEvents ( " +
 						"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"UId INTEGER ," + 
 						"Title VARCHAR(100), " +
 						"Description TEXT, " +
 						"StartTime INTEGER, " +
 						"EndTime INTEGER, " +
 						"IsMine INTEGER DEFAULT 1 " +
 						");"
 				);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(DBHandler.class.getName(),
 				"Upgrading database from version " + oldVersion + " to "
 						+ newVersion + ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS ShopList");
 		db.execSQL("DROP TABLE IF EXISTS ShopListOverview");
 		db.execSQL("DROP TABLE IF EXISTS CalendarEvents");
 		onCreate(db);
 	}
 } 
