 package org.fr.ykatchou.paillardes;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	// The Android's default system path of your application database.
 	private static String DB_PATH = "/data/data/org.fr.ykatchou.paillardes/databases/";
 	private static String DB_NAME = "paillardes.db";
 	private SQLiteDatabase myDatabase;
 	private final Context myContext;
 
 	public DatabaseHelper(Context context) {
 		super(context, DB_NAME, null, 1);
 		this.myContext = context;
 
 		try {
 			this.init();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void close() {
 		if (myDatabase != null)
 			myDatabase.close();
 		super.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 
 	private void createDataBase() throws IOException {
 		boolean dbExist = checkDatabase();
 		if (dbExist) {
 			// do nothing - database already exist
 		} else {
 
 			// By calling this method and empty database will be created into
 			// the default system path
 			// of your application so we are gonna be able to overwrite that
 			// database with our database.
 			this.getReadableDatabase();
 			try {
 
 				copyDataBase();
 
 			} catch (IOException e) {
 				throw new Error("Error copying database");
 			}
 		}
 	}
 
 	private boolean checkDatabase() {
 		SQLiteDatabase checkDB = null;
 		try {
 			String myPath = DB_PATH + DB_NAME;
 			checkDB = SQLiteDatabase.openDatabase(myPath, null,
 					SQLiteDatabase.OPEN_READONLY);
 		} catch (SQLiteException e) {

			// database does't exist yet.
 		}
 		if (checkDB != null) {
 			checkDB.close();
 		}
 		return checkDB != null ? true : false;
 	}
 
 	private void copyDataBase() throws IOException {
 		// Open your local db as the input stream
 		InputStream myInput = myContext.getAssets().open(DB_NAME);
 		// Path to the just created empty db
 		String outFileName = DB_PATH + DB_NAME;
 		// Open the empty db as the output stream
 		OutputStream myOutput = new FileOutputStream(outFileName);
 		// transfer bytes from the inputfile to the outputfile
 		byte[] buffer = new byte[1024];
 		int length;
 		while ((length = myInput.read(buffer)) > 0) {
 			myOutput.write(buffer, 0, length);
 		}
 		// Close the streams
 		myOutput.flush();
 		myOutput.close();
 		myInput.close();
 	}
 
 	private void openDataBase() throws SQLException {
 		// Open the database
 		String myPath = DB_PATH + DB_NAME;
 		myDatabase = SQLiteDatabase.openDatabase(myPath, null,
 				SQLiteDatabase.OPEN_READONLY);
 	}
 
 	public void init() throws IOException {
 		if (!checkDatabase())
 			createDataBase();
 		openDataBase();
 	}
 
 	public List<Chanson> getTitres() {
 		List<Chanson> data = new LinkedList<Chanson>();
 		String allTitresQuery = "select ch.id, ch.titre from chanson ch order by ch.titre";
 		Cursor d = myDatabase.rawQuery(allTitresQuery, null);
 		while (d.moveToNext()) {
 			Chanson c = new Chanson(d.getLong(0), d.getString(1));
 			data.add(c);
 		}
 		return data;
 	}
 
 	public List<Chanson> getTitres(String filter) {
 		if (filter == null || filter == "")
 			return getTitres();
 
 		List<Chanson> data = new LinkedList<Chanson>();
 
 		String allTitresQuery = "select ch.* from chanson ch where ch.titre like ? or ch.paroles like ? order by ch.titre";
 		String[] params = new String[2];
 
 		params[0] = "%" + filter + "%";
 		params[1] = "%" + filter + "%";
 
 		Cursor d = myDatabase.rawQuery(allTitresQuery, params);
 
 		while (d.moveToNext()) {
 			Chanson c = new Chanson(d.getLong(0), d.getString(1));
 			data.add(c);
 		}
 		return data;
 	}
 
 	public Chanson getChanson(Long id) {
 		Chanson data = new Chanson();
 		String[] params = new String[1];
 		String getChansonQuery = "select ch.* from chanson ch where ch.id = ?";
 		params[0] = String.valueOf(id);
 		Cursor d = myDatabase.rawQuery(getChansonQuery, params);
 
 		if (d.moveToNext()) {
 			data.put(Chanson.Id, d.getString(0));
 			data.put(Chanson.Titre, d.getString(1));
 			data.put(Chanson.Paroles, d.getString(2));
 			data.put(Chanson.url, d.getString(3));
 		}
 		return data;
 	}
 
 	public Long getChansonCount() {
 		String getCountQuery = "select count(*) from chanson";
 		Cursor d = myDatabase.rawQuery(getCountQuery, null);
 
 		if (d.moveToNext())
 			return d.getLong(0);
 
 		return Long.valueOf(0);
 	}
 }
