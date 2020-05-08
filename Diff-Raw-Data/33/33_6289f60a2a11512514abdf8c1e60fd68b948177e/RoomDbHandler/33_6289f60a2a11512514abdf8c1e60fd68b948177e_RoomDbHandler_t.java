 package se.mah.kd330a.project.find.data;
 
 import java.util.List;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 //import android.util.Log;
 import android.provider.BaseColumns;
 
 /*
  *	Database handler class for searching for rooms 
  *	The database created when the app runs the first time 
  *	and it adds all the data for the rooms too. (Need to update)
  *	With the function isRoomExists(String roomNr) can make a search
  *	in the database. If it returns true then with the appropriate 
  *	function you can access the stored data for the room.
  *	functions are: getRoomPathPics, getPathPicTexts, getMapName ...
  */
 
 public class RoomDbHandler extends SQLiteOpenHelper {
 
 	//private static final String LOG = "MAH RoomDbHandler";
 
 	private static final String DATABASE_NAME = "find_roomsDB";
 	private static final int DATABASE_VERSION = 1;
 
 	private static final String TABLE_ROOMS = "rooms";
 
 	private static final String ROW_ROOMNR = "roomNr";
 	private static final String ROW_PATH = "path";
 	private static final String ROW_TEXTS = "texts";
 	private static final String ROW_ARROWS = "arrows";
 	private static final String ROW_X = "x";
 	private static final String ROW_Y = "y";
 	private static final String ROW_MAP = "map";
 
 	static final String TABLE_CREATE = "CREATE TABLE rooms (" + BaseColumns._ID + " int primary key, roomNr TEXT, path TEXT, texts TEXT, arrows TEXT," + 
 			" x INTEGER, y INTEGER, map TEXT);";
 
 	private PathToRoom room;
 
 	public RoomDbHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(TABLE_CREATE);
 
 		//addRow(db, "K2C107", "K2_E1_C_S1", "first_sec_third_last", null, null, null);
 
 		addRow(db, "K2C107", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftlefthall", "k2_k2e1centerleft_k2e1cdirectleftleft_k2e1cdirectleftlefthall", "icarrowhere_icarrowleft_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C108", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftlefthall", "k2_k2e1centerleft_k2e1cdirectleftleft_k2e1cdirectleftlefthall", "icarrowhere_icarrowleft_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C109", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftlefthall", "k2_k2e1centerleft_k2e1cdirectleftleft_k2e1cdirectleftlefthall", "icarrowhere_icarrowleft_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C110", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftlefthall", "k2_k2e1centerleft_k2e1cdirectleftleft_k2e1cdirectleftlefthall", "icarrowhere_icarrowleft_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C117", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C119", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C121", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C122", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C123", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C124A", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C124B", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C133", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C135A", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C135B", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C136", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C138", "k2_k2e1centerleft_k2e1cdirectleft_k2e1cdirectleftrighthall", "k2_k2e1centerleft_k2e1cdirectleftright_k2e1cdirectleftrighthall", "icarrowhere_icarrowleft_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C307", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C308", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C309", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C310", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C311", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C314", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C316", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C320", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C323", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C325", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C332", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C333", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C339", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C340", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C341", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C342", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C343", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightrighthall", "k2_k2e1centerright_k2e1cdirectrightright_k2e1cdirectrightrighthall", "icarrowhere_icarrowright_icarrowup_icarrowright", null, null, "k2_c");
 		addRow(db, "K2C345", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C346", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C347", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C348", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C349", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C350", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C351", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C352", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2C353", "k2_k2e1centerright_k2e1cdirectright_k2e1cdirectrightlefthall", "k2_k2e1centerright_k2e1cdirectrightleft_k2e1cdirectrightlefthall", "icarrowhere_icarrowright_icarrowup_icarrowleft", null, null, "k2_c");
 		addRow(db, "K2B105", "k2_k2e2_k2e2benter_k2e2bright", "k2_k2e2_k2e2benter_k2e2bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B106", "k2_k2e2_k2e2benter_k2e2bleft", "k2_k2e2_k2e2benter_k2e2bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B107", "k2_k2e2_k2e2benter_k2e2bleft", "k2_k2e2_k2e2benter_k2e2bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B108", "k2_k2e2_k2e2benter_k2e2bleft", "k2_k2e2_k2e2benter_k2e2bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B109", "k2_k2e2_k2e2benter_k2e2bleft", "k2_k2e2_k2e2benter_k2e2bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B110", "k2_k2e3_k2e3benter_k2e3bright", "k2_k2e3_k2e3benter_k2e3bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B111", "k2_k2e3_k2e3benter_k2e3bright", "k2_k2e3_k2e3benter_k2e3bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B117", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B118", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B119", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B202", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B203", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B204", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B210", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B211", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B212", "k2_k2e3_k2e3benter_k2e3bleft", "k2_k2e3_k2e3benter_k2e3bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B205", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B206", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B207", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B208", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B209", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B302", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B303", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B304", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B305", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B306", "k2_k2e4_k2e4benter_k2e4bright", "k2_k2e4_k2e4benter_k2e4bright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_b");
 		addRow(db, "K2B352", "k2_k2e4_k2e4benter_k2e4bleft", "k2_k2e4_k2e4benter_k2e4bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2B353", "k2_k2e4_k2e4benter_k2e4bleft", "k2_k2e4_k2e4benter_k2e4bleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_b");
 		addRow(db, "K2A126", "k2_k2e2_k2e2aenter_k2e2astraight", "k2_k2e2_k2e2aenter_k2e2astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A129", "k2_k2e2_k2e2aenter_k2e2astraight", "k2_k2e2_k2e2aenter_k2e2astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A130", "k2_k2e2_k2e2aenter_k2e2astraight", "k2_k2e2_k2e2aenter_k2e2astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A131", "k2_k2e2_k2e2aenter_k2e2astraight", "k2_k2e2_k2e2aenter_k2e2astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A132", "k2_k2e2_k2e2aenter_k2e2astraight", "k2_k2e2_k2e2aenter_k2e2astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A104", "k2_k2e2_k2e2aenter_k2e2aleft", "k2_k2e2_k2e2aenter_k2e2aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A134", "k2_k2e2_k2e2aenter_k2e2aleft", "k2_k2e2_k2e2aenter_k2e2aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A136", "k2_k2e2_k2e2aenter_k2e2aleft", "k2_k2e2_k2e2aenter_k2e2aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A133", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A135", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A137", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A138", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A139", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A140", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A146", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A150", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A164", "k2_k2e3_k2e3aenter_k2e3aright", "k2_k2e3_k2e3aenter_k2e3aright", "icarrowhere_icarrowhere_icarrowup_icarrowright", null, null, "k2_a");
 		addRow(db, "K2A142", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A160", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A161", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A162", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A163", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A202", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A203", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A204", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A209", "k2_k2e3_k2e3aenter_k2e3aleft", "k2_k2e3_k2e3aenter_k2e3aleft", "icarrowhere_icarrowhere_icarrowup_icarrowleft", null, null, "k2_a");
 		addRow(db, "K2A205", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A206", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A207", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A302", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A303", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A304", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A305", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A306", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A307", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A308", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2A309", "k2_k2e4_k2e4aenter_k2e4astraight", "k2_k2e4_k2e4aenter_k2e4astraight", "icarrowhere_icarrowhere_icarrowup_icarrowup", null, null, "k2_a");
 		addRow(db, "K2D155", "k2_k2e1centerright_k2e1dstairs2_k2e1dstairs2bridge", "k2_k2e1dright_k2e1dstairs2straight_k2e1dstairs2bridge", "icarrowhere_icarrowright_icarrowup_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D156", "k2_k2e1centerright_k2e1dstairs2_k2e1dstairs2bridge", "k2_k2e1dright_k2e1dstairs2straight_k2e1dstairs2bridge", "icarrowhere_icarrowright_icarrowup_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D157", "k2_k2e1centerright_k2e1dstairs2_k2e1dstairs2bridge", "k2_k2e1dright_k2e1dstairs2straight_k2e1dstairs2bridge", "icarrowhere_icarrowright_icarrowup_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D202", "k2_k2e1centerright_k2e1dstairs2", "k2_k2e1dright_k2e1dstairs2upright", "icarrowhere_icarrowright_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D204", "k2_k2e1centerright_k2e1dstairs2", "k2_k2e1dright_k2e1dstairs2upleft", "icarrowhere_icarrowright_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D205", "k2_k2e1centerright_k2e1dstairs2", "k2_k2e1dright_k2e1dstairs2upleft", "icarrowhere_icarrowright_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D213", "k2_k2e1centerright_k2e1dstairs1", "k2_k2e1dright_k2e1dstairs1up", "icarrowhere_icarrowright_icarrowup", null, null, "k2_d");
 		addRow(db, "K2D214", "k2_k2e1centerright_k2e1dstairs1", "k2_k2e1dright_k2e1dstairs1up", "icarrowhere_icarrowright_icarrowup", null, null, "k2_d");
 
 		addRow(db, "ORD131", null, null, null, null, null, "or_1");
 		addRow(db, "ORD138", null, null, null, null, null, "or_1");
 
 		addRow(db, "ORC236", null, null, null, null, null, "or_2");
 		addRow(db, "ORD222", null, null, null, null, null, "or_2");
 		addRow(db, "ORE222", null, null, null, null, null, "or_2");
 		addRow(db, "ORE223", null, null, null, null, null, "or_2");
 		addRow(db, "ORE235", null, null, null, null, null, "or_2");
 		addRow(db, "ORE239", null, null, null, null, null, "or_2");
 		addRow(db, "ORE240", null, null, null, null, null, "or_2");
 		addRow(db, "ORE477", null, null, null, null, null, "or_2");
 		addRow(db, "ORF206", null, null, null, null, null, "or_2");
 		addRow(db, "ORF208", null, null, null, null, null, "or_2");
 		addRow(db, "ORF209", null, null, null, null, null, "or_2");
 		addRow(db, "ORF211", null, null, null, null, null, "or_2");
 		addRow(db, "ORF215", null, null, null, null, null, "or_2");
 		addRow(db, "ORF219", null, null, null, null, null, "or_2");
 		addRow(db, "ORF220", null, null, null, null, null, "or_2");
 		
 		addRow(db, "ORC323", null, null, null, null, null, "or_3");
 		addRow(db, "ORC333", null, null, null, null, null, "or_3");
 		addRow(db, "ORC334", null, null, null, null, null, "or_3");
 		addRow(db, "ORC335", null, null, null, null, null, "or_3");
 		addRow(db, "ORC336", null, null, null, null, null, "or_3");
 		addRow(db, "ORC339", null, null, null, null, null, "or_3");
 		addRow(db, "ORC340", null, null, null, null, null, "or_3");
 		addRow(db, "ORC341", null, null, null, null, null, "or_3");
 		addRow(db, "ORC344", null, null, null, null, null, "or_3");
 		addRow(db, "ORC345", null, null, null, null, null, "or_3");
 		addRow(db, "ORC377", null, null, null, null, null, "or_3");
 		addRow(db, "ORD326", null, null, null, null, null, "or_3");
 		addRow(db, "ORD328", null, null, null, null, null, "or_3");
 		addRow(db, "ORD331", null, null, null, null, null, "or_3");
 		addRow(db, "ORD337", null, null, null, null, null, "or_3");
 		addRow(db, "ORD377", null, null, null, null, null, "or_3");
 		addRow(db, "ORE323", null, null, null, null, null, "or_3");
 		addRow(db, "ORE336", null, null, null, null, null, "or_3");
 		addRow(db, "ORE337", null, null, null, null, null, "or_3");
 		addRow(db, "ORE340", null, null, null, null, null, "or_3");
 		addRow(db, "ORE341", null, null, null, null, null, "or_3");
 		addRow(db, "ORF306", null, null, null, null, null, "or_3");
 		addRow(db, "ORF307", null, null, null, null, null, "or_3");
 		addRow(db, "ORF309", null, null, null, null, null, "or_3");
 		addRow(db, "ORF312", null, null, null, null, null, "or_3");
 		addRow(db, "ORF314", null, null, null, null, null, "or_3");
 		addRow(db, "ORF315", null, null, null, null, null, "or_3");
 		
 		addRow(db, "ORC435", null, null, null, null, null, "or_4");
 		addRow(db, "ORC436", null, null, null, null, null, "or_4");
 		addRow(db, "ORC440", null, null, null, null, null, "or_4");
 		addRow(db, "ORD436", null, null, null, null, null, "or_4");
 		addRow(db, "ORE436", null, null, null, null, null, "or_4");
 		addRow(db, "ORE439", null, null, null, null, null, "or_4");
 		addRow(db, "ORE477", null, null, null, null, null, "or_4");
 		
 		addRow(db, "ORC525", null, null, null, null, null, "or_5");
 		addRow(db, "ORC526", null, null, null, null, null, "or_5");
 		addRow(db, "ORC527", null, null, null, null, null, "or_5");
 		addRow(db, "ORC528", null, null, null, null, null, "or_5");
 		
 		addRow(db, "K8U042", null, null, null, null, null, "k8_1");
 		
 		addRow(db, "K8U301", null, null, null, null, null, "k8_3");
 		addRow(db, "K8U302", null, null, null, null, null, "k8_3");
 		addRow(db, "K8U303", null, null, null, null, null, "k8_3");
 		addRow(db, "K8U304", null, null, null, null, null, "k8_3");
 		addRow(db, "K8U305", null, null, null, null, null, "k8_3");
 		addRow(db, "K8U306", null, null, null, null, null, "k8_3");
 		addRow(db, "K8U307", null, null, null, null, null, "k8_3");
 		
 		addRow(db, "K8U401", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U402", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U403", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U404", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U405", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U406", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U407", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U408", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U428", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U429", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U430", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U431", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U432", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U433", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U434", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U455", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U456", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U457", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U458", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U459", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U460", null, null, null, null, null, "k8_4");
 		addRow(db, "K8U461", null, null, null, null, null, "k8_4");
 		
 		addRow(db, "K8U520", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U521", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U522", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U523", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U524", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U527", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U528", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U529", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U530", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U531", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U532", null, null, null, null, null, "k8_5");
 		addRow(db, "K8U533", null, null, null, null, null, "k8_5");
 
 		addRow(db, "G8104", null, null, null, null, null, "g8_1");
 		addRow(db, "G8107", null, null, null, null, null, "g8_1");
 		addRow(db, "G8124", null, null, null, null, null, "g8_1");
 		addRow(db, "G8125", null, null, null, null, null, "g8_1");
 		addRow(db, "G8174", null, null, null, null, null, "g8_1");
 		
 		addRow(db, "G8305", null, null, null, null, null, "g8_3");
 		addRow(db, "G8307", null, null, null, null, null, "g8_3");
 		addRow(db, "G8308", null, null, null, null, null, "g8_3");
 		addRow(db, "G8323", null, null, null, null, null, "g8_3");
 		addRow(db, "G8324", null, null, null, null, null, "g8_3");
 		addRow(db, "G8325", null, null, null, null, null, "g8_3");
 		addRow(db, "G8355", null, null, null, null, null, "g8_3");
 		addRow(db, "G8360", null, null, null, null, null, "g8_3");
 		
 		addRow(db, "G8405", null, null, null, null, null, "g8_4");
 		addRow(db, "G8407", null, null, null, null, null, "g8_4");
 		addRow(db, "G8408", null, null, null, null, null, "g8_4");
 		addRow(db, "G8423", null, null, null, null, null, "g8_4");
 		addRow(db, "G8424", null, null, null, null, null, "g8_4");
 		addRow(db, "G8425", null, null, null, null, null, "g8_4");
 		addRow(db, "G8471", null, null, null, null, null, "g8_4");
 		
 		addRow(db, "G8505", null, null, null, null, null, "g8_5");
 		addRow(db, "G8507", null, null, null, null, null, "g8_5");
 		addRow(db, "G8508", null, null, null, null, null, "g8_5");
 		addRow(db, "G8523", null, null, null, null, null, "g8_5");
 		addRow(db, "G8524", null, null, null, null, null, "g8_5");
 		addRow(db, "G8525", null, null, null, null, null, "g8_5");
 		addRow(db, "G8549", null, null, null, null, null, "g8_5");
 		
 		addRow(db, "AS9U103", null, null, null, null, null, "as_1");
 		addRow(db, "AS9U104", null, null, null, null, null, "as_1");
 		addRow(db, "AS9U106", null, null, null, null, null, "as_1");
 		addRow(db, "AS9U107", null, null, null, null, null, "as_1");
 		addRow(db, "AS9U108", null, null, null, null, null, "as_1");
 		addRow(db, "AS9U123", null, null, null, null, null, "as_1");
 		
 		addRow(db, "AS9U202", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U204", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U206", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U208", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U210", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U211", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U212", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U214", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U216", null, null, null, null, null, "as_2");
 		addRow(db, "AS9U217", null, null, null, null, null, "as_2");
 
 		addRow(db, "AS9U306", null, null, null, null, null, "as_3");
 		addRow(db, "AS9U310", null, null, null, null, null, "as_3");
 		addRow(db, "AS9U316", null, null, null, null, null, "as_3");
 		addRow(db, "AS9U317", null, null, null, null, null, "as_3");
 		
 		addRow(db, "AS9U402", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U404", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U406", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U407", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U408", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U410", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U416", null, null, null, null, null, "as_4");
 		addRow(db, "AS9U417", null, null, null, null, null, "as_4");
 		
 		addRow(db, "KL1097", null, null, null, null, null, "kl_1");
 		addRow(db, "KL1151", null, null, null, null, null, "kl_1");
 		addRow(db, "KL1237", null, null, null, null, null, "kl_1");
 		addRow(db, "KL1382", null, null, null, null, null, "kl_1");
 		
 		addRow(db, "KL2110", null, null, null, null, null, "kl_2");
 		addRow(db, "KL2370", null, null, null, null, null, "kl_2");
 		addRow(db, "KL2690", null, null, null, null, null, "kl_2");
 		
 		addRow(db, "KL3047", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3080", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3181", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3450", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3451", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3482", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3520", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3521", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3540", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3541", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3550", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3560", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3561", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3580", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3583", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3600", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3601", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3620", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3621", null, null, null, null, null, "kl_3");
 		addRow(db, "KL3690", null, null, null, null, null, "kl_3");
 		
 		addRow(db, "KL4091", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4146", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4201", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4321", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4391", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4400", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4410", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4511", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4561", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4621", null, null, null, null, null, "kl_4");
 		addRow(db, "KL4690", null, null, null, null, null, "kl_4");
 		
 		addRow(db, "KL5091", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5231", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5301", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5511", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5531", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5540", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5643", null, null, null, null, null, "kl_5");
 		addRow(db, "KL5690", null, null, null, null, null, "kl_5");
 	
 	} 
 
 
 	private void addRow(SQLiteDatabase db, String roomNr, String path, String texts, String arrows, String x, String y, String map) {
 
 		ContentValues values = new ContentValues();
 		values.put(ROW_ROOMNR, roomNr); 
 		values.put(ROW_PATH, path); 
 		values.put(ROW_TEXTS, texts); 
 		values.put(ROW_ARROWS, arrows); 
 		values.put(ROW_X, x); 
 		values.put(ROW_Y, y); 
 		values.put(ROW_MAP, map); 
 
 		// Inserting Row
 		db.insert(TABLE_ROOMS, null, values);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 
 	}
 
	public boolean isRoomExists(String roomNr) {
 
 		String selectQuery = "SELECT  * FROM " + TABLE_ROOMS + " WHERE "
 				+ ROW_ROOMNR + " = '" + roomNr.toUpperCase(Locale.getDefault()) + "'";
 
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		try {
 			Cursor c = db.rawQuery(selectQuery, null);
 
 			if (c != null) {
 				c.moveToFirst();
 				room = new PathToRoom(roomNr);
 				room.setPath(c.getString(c.getColumnIndex(ROW_PATH)));
 				room.setTextList(c.getString(c.getColumnIndex(ROW_TEXTS)));
 				room.setArrowList(c.getString(c.getColumnIndex(ROW_ARROWS)));
 				room.mMapPic = c.getString(c.getColumnIndex(ROW_MAP));
 				room.mCoord_x = c.getInt(c.getColumnIndex(ROW_X));
 				room.mCoord_y = c.getInt(c.getColumnIndex(ROW_Y));
 				db.close();
 				return true;
 			}
 		}
 		catch (Exception e) {
 			db.close();
 			e.printStackTrace();
 		}
 		return false;
 	}
 
	public boolean isRoomExistsAll(String roomNr) {
 
 		String selectQuery = "SELECT  * FROM " + TABLE_ROOMS + " WHERE "
 				+ ROW_ROOMNR + " = '" + roomNr.toUpperCase(Locale.getDefault()) + "'";
 
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		try {
 			Cursor c = db.rawQuery(selectQuery, null);
 
 			if (c != null) {
 				c.moveToFirst();
 				room = new PathToRoom(roomNr);
 				room.mMapPic = c.getString(c.getColumnIndex(ROW_MAP));
 				room.mCoord_x = c.getInt(c.getColumnIndex(ROW_X));
 				room.mCoord_y = c.getInt(c.getColumnIndex(ROW_Y));
 				db.close();
 				return true;
 			}
 		}
 		catch (Exception e) {
 			db.close();
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public PathToRoom getRoomDetails() {
 		return room;
 	}
 
 	public List<String> getPathImg() {
 		if (room != null)
 			return room.getPath();
 		else
 			return null;
 	}
 
 	public List<String> getPathImgTexts() {
 		if (room != null)
 			return room.getTexts();
 		else
 			return null;
 	}
 
 	public List<String> getArrows() {
 		if (room != null)
 			return room.getArrows();
 		else
 			return null;
 	}
 
 	public String getMapName() {
 		if (room != null)
 			return room.mMapPic;
 		else
 			return null;
 	}
 
 	public String getRoomNr() {
 		if (room != null)
 			return room.mRoomNr;
 		else
 			return null;
 	}
 
 	public int getCoordX() {
 		if (room != null)
 			return room.mCoord_x;
 		else
 			return -1;
 	}
 
 	public int getCoordY() {
 		if (room != null)
 			return room.mCoord_y;
 		else 
 			return -1;
 	}
 }
