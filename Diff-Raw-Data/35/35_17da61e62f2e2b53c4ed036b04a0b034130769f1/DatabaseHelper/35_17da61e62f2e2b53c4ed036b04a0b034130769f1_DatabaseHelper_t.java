 package bu.edu.cs673.edukid.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * This class defines the database schema and contract.
  * 
  * @author Kevin Graue
  * 
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static final String EDUKID_DATABASE = "EDUkid.db";
	private static final int EDUKID_DATABASE_VERSION = 29;
 
 	private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
 
 	public static final String TABLE_CATEGORIES = "categories";
 	public static final String COLUMN_CATEGORY_ID = "_id";
 	public static final String COLUMN_CATEGORY_NAME = "category";
 	public static final String COLUMN_CATEGORY_IMAGE = "image";
 
 	public static final String TABLE_USER_ACCOUNT = "useraccount";
 	public static final String COLUMN_USER_ID = "uid";
 	public static final String COLUMN_USER_NAME = "username";
 	public static final String COLUMN_USER_IMAGE = "userimage";
 	public static final String COLUMN_USER_SOUND = "usersound";
 
 	public static final String TABLE_LETTERS = "letter";
 	public static final String COLUMN_LETTERS_ID = "letterid";
 	public static final String COLUMN_LETTERS_WORD = "letterword";
 	public static final String COLUMN_LETTERS_SOUND = "lettersound";
 
 	public static final String TABLE_THEME = "theme";
 	public static final String COLUMN_THEME_ID = "themeid";
 	public static final String COLUMN_THEME_NAME = "themename";
 
 	public static final String TABLE_ALPHABET = "alphabet";
 	public static final String COLUMN_LID = "lid";
 	public static final String COLUMN_TID = "tid";
 	public static final String COLUMN_WORDS = "words";
 	public static final String COLUMN_WORDS_SOUND = "wordssound";
 	public static final String COLUMN_WORDS_IMAGE = "wordsimage";
 	
 	public static final String TABLE_NUMBER = "number";
 	public static final String COLUMN_NUMBER_ID = "numberid";
 	public static final String COLUMN_NUMBER_WORD = "num";
 	public static final String COLUMN_NUMBER_SOUND = "numbersound";
 
 	public static final String TABLE_NUM_TYPE = "ntype";
 	public static final String COLUMN_TYPE_ID = "ntypeid";
 	public static final String COLUMN_TYPE_NAME = "ntypename";
 	
 	public static final String TABLE_NUMBERS = "numbers";
 	public static final String COLUMN_NID = "nid";
 	public static final String COLUMN_NTID = "ntid";
 	public static final String COLUMN_NUMBERS = "numbers";
 	public static final String COLUMN_NUMBERS_SOUND = "numberssound";
 	public static final String COLUMN_NUMBERS_IMAGE = "numberimage";
 	
 	public static final String TABLE_SHAPE = "shape";
 	public static final String COLUMN_SHAPE_ID = "shapeid";
 	public static final String COLUMN_SHAPE_WORD = "shapeword";
 	public static final String COLUMN_SHAPE_IMAGE = "shapeimage";
 	public static final String COLUMN_SHAPE_SOUND = "shapesound";
 	
 	public static final String TABLE_COLOUR = "colour";
 	public static final String COLUMN_COLOUR_ID = "colourid";
 	public static final String COLUMN_COLOUR_WORD = "colourword";
 	public static final String COLUMN_COLOUR_IMAGE = "colourimage";
 	public static final String COLUMN_COLOUR_SOUND = "coloursound";
 	
 	private static final String CREATE_CATEGORIES_TABLE = "create table "
 			+ TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID
 			+ " integer primary key autoincrement, " + COLUMN_CATEGORY_NAME
 			+ " text not null, " + COLUMN_CATEGORY_IMAGE + " text not null);";
 
 	// TODO: sound is text. investigate this.
 	private static final String CREATE_USER_ACCOUNT_TABLE = "create table "
 			+ TABLE_USER_ACCOUNT + "(" + COLUMN_USER_ID
 			+ " integer primary key autoincrement, " + COLUMN_USER_NAME
 			+ " text not null, " + COLUMN_USER_IMAGE + " text not null, "
 			+ COLUMN_USER_SOUND + " text not null);";
 
 	private static final String CREATE_LETTERS_TABLE = "create table "
 			+ TABLE_LETTERS + "(" + COLUMN_LETTERS_ID
 			+ " integer primary key autoincrement, " + COLUMN_LETTERS_WORD
 			+ " text , " + COLUMN_LETTERS_SOUND + " text );";
 
 	private static final String CREATE_THEME_TABLE = "create table "
 			+ TABLE_THEME + "(" + COLUMN_THEME_ID
 			+ " integer primary key autoincrement, " + COLUMN_THEME_NAME
 			+ " text not null);";
 
 	private static final String CREATE_ALPHABET_TABLE = "create table "
 			+ TABLE_ALPHABET + "(" + COLUMN_LID + " integer, " + COLUMN_TID
 			+ " integer , " + COLUMN_WORDS + " text , " + COLUMN_WORDS_SOUND
 			+ " text, " + COLUMN_WORDS_IMAGE + " text );";
 	/*
 	 * SHAPES TABLE
 	 * 
 	 */
 	
 	private static final String CREATE_SHAPE_TABLE = "create table "
 			+ TABLE_SHAPE + "(" + COLUMN_SHAPE_ID
 			+ " integer primary key autoincrement, " + COLUMN_SHAPE_WORD
 			+ " text , " +COLUMN_SHAPE_IMAGE+" text , "+ COLUMN_SHAPE_SOUND + " text );";
 	/*
 	 * COLOURS TABLE
 	 * 
 	 */
 	
 	
 	private static final String CREATE_COLOUR_TABLE = "create table "
 			+ TABLE_COLOUR + "(" + COLUMN_COLOUR_ID
 			+ " integer primary key autoincrement, " + COLUMN_COLOUR_WORD
 			+ " text , " +COLUMN_COLOUR_IMAGE+" text , "+ COLUMN_COLOUR_SOUND + " text );";
 	
 	/**
 	 * 
 	 * NUMBERS DATABASE
 	 * 
 	 */
 
 	private static final String CREATE_NUMBER_TABLE = "create table "
 			+ TABLE_NUMBER + "(" + COLUMN_NUMBER_ID
 			+ " integer primary key autoincrement, " + COLUMN_NUMBER_WORD
 			+ " text , " + COLUMN_NUMBER_SOUND + " text );";
 
 	private static final String CREATE_NUMTYPE_TABLE = "create table "
 			+ TABLE_NUM_TYPE + "(" + COLUMN_TYPE_ID
 			+ " integer primary key autoincrement, " + COLUMN_TYPE_NAME
 			+ " text not null);";
 
 	private static final String CREATE_NUMBERS_TABLE = "create table "
 			+ TABLE_NUMBERS + "(" + COLUMN_NID + " integer, " + COLUMN_NTID
 			+ " integer , " + COLUMN_NUMBERS + " text , " + COLUMN_NUMBERS_SOUND
 			+ " text, " + COLUMN_NUMBERS_IMAGE + " text );";
 	/**
 	 * Constructor.
 	 * 
 	 * @param context
 	 *            the context.
 	 */
 	public DatabaseHelper(Context context) {
 		super(context, EDUKID_DATABASE, null, EDUKID_DATABASE_VERSION);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_CATEGORIES_TABLE);
 		db.execSQL(CREATE_USER_ACCOUNT_TABLE);
 		db.execSQL(CREATE_LETTERS_TABLE);
 		db.execSQL(CREATE_THEME_TABLE);
 		db.execSQL(CREATE_ALPHABET_TABLE);
 		db.execSQL(CREATE_NUMBER_TABLE);
 		db.execSQL(CREATE_NUMTYPE_TABLE);
 		db.execSQL(CREATE_NUMBERS_TABLE);
 		db.execSQL(CREATE_SHAPE_TABLE);
 		db.execSQL(CREATE_COLOUR_TABLE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL(DROP_TABLE + TABLE_CATEGORIES);
 		db.execSQL(DROP_TABLE + TABLE_USER_ACCOUNT);
 		db.execSQL(DROP_TABLE + TABLE_LETTERS);
 		db.execSQL(DROP_TABLE + TABLE_THEME);
 		db.execSQL(DROP_TABLE + TABLE_ALPHABET);
 		db.execSQL(DROP_TABLE + TABLE_NUMBER);
 		db.execSQL(DROP_TABLE + TABLE_NUM_TYPE);
 		db.execSQL(DROP_TABLE + TABLE_NUMBERS);
 		db.execSQL(DROP_TABLE + TABLE_SHAPE);
 		db.execSQL(DROP_TABLE + TABLE_COLOUR);
 
 		onCreate(db);
 	}
 }
