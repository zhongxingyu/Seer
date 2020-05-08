 package com.reindeermobile.reindeerutils.db;
 
 import com.reindeermobile.reindeerutils.view.StringUtils;
 import com.reindeermobile.reindeerutils.view.ViewUtils;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class DataBaseHelper extends SQLiteOpenHelper {
 	public static final String TAG = "DataBaseHelper";
 	private static final String DATA_SLASH_DATA_PATH = "/data/data/";
 	private String packagePath;
 	private String databaseFileName;
 	// private SqlResource sqlFiles;
 	private Context context;
 
 	public DataBaseHelper(final Context context, final String databaseFileName,
 			final int version) {
 		super(context, databaseFileName, null, version);
 		Log.i(TAG, "DataBaseHelper - version: " + version);
 		this.setContext(context);
 		// this.setSqlFiles(sqlResource);
 		this.setPackagePath(context.getPackageName());
 
 		String databaseFullPath = DATA_SLASH_DATA_PATH + getPackagePath()
 				+ StringUtils.PER_STRING + databaseFileName;
 		Log.i(TAG, "DataBaseHelper - Database path: " + databaseFullPath);
 		this.setDatabaseFileName(databaseFullPath);
 	}
 
 	/**
 	 * A create_<db_version>.sql és az insert_<db_version>.sql fut le.
 	 */
 	@Override
 	public final void onCreate(final SQLiteDatabase db) {
		Log.i(TAG, "onCreate - v" + db.getVersion());
		// String sqlFile = this.sqlFiles.getCreateScript();
		String sqlFile = "create_v" + db.getVersion() + ".sql";
 		Log.i(TAG, "onCreate - create tables: "
 				+ sqlFile);
 		loadSqlFile(db, sqlFile);
 
		// sqlFile = this.sqlFiles.getInsertScript();
		sqlFile = "insert_v" + db.getVersion() + ".sql";
 		Log.i(TAG, "onCreate - insert datas: "
 				+ sqlFile);
 		loadSqlFile(db, sqlFile);
 	}
 
 	/**
 	 * A oldVersion+1 -től newVersion-ig fut le. drop_<version_iterator>.sql,
 	 * alter_<version_iterator>.sql, insert_<version_iterator>.sql.
 	 * 
 	 * @param oldVersion
 	 * @param newVersion
 	 */
 	@Override
 	public final void onUpgrade(final SQLiteDatabase db, final int oldVersion,
 			final int newVersion) {
 		Log.i(TAG, "onUpgrade - DataBaseHelper.onUpgrade - v" + db.getVersion()
 				+ "(" + oldVersion + "," + newVersion + ")");
 
 		for (int i = oldVersion + 1; i < newVersion + 1; i++) {
 			String sqlFile = "drop_v" + newVersion + ".sql";
 			Log.i(TAG, "onUpgrade - drop tables: " + sqlFile);
 			loadSqlFile(db, sqlFile);
 
 			sqlFile = "alter_v" + newVersion + ".sql";
 			Log.i(TAG, "onUpgrade - alter tables: " + sqlFile);
 			loadSqlFile(db, sqlFile);
 
 			sqlFile = "insert_v" + newVersion + ".sql";
 			Log.i(TAG, "onUpgrade - insert tables: " + sqlFile);
 			loadSqlFile(db, sqlFile);
 		}
 
 		// String sqlFile = this.sqlFiles.getDropScript();
 		// Log.i(LOG_TAG_DATABASE, "DataBaseHelper.onCreate - drop tables: " +
 		// sqlFile);
 		// loadSqlFile(db, sqlFile);
 
 		// sqlFile = this.sqlFiles.getAlterScript();
 		// Log.i(LOG_TAG_DATABASE, "DataBaseHelper.onCreate - alter tables: " +
 		// sqlFile);
 		// loadSqlFile(db, sqlFile);
 		//
 		// sqlFile = this.sqlFiles.getInsertScript();
 		// Log.i(LOG_TAG_DATABASE, "DataBaseHelper.onCreate - insert datas: " +
 		// sqlFile);
 		// loadSqlFile(db, sqlFile);
 	}
 
 	private void loadSqlFile(final SQLiteDatabase db, String sqlFileName) {
 		InputStream inputStream = null;
 		BufferedReader bufferedReader = null;
 		String line = StringUtils.EMPTY_STRING;
 		try {
 			inputStream = ViewUtils.getInputStreamFromAssets(this.getContext(),
 					sqlFileName);
 			bufferedReader = new BufferedReader(new InputStreamReader(
 					inputStream));
 
 			while ((line = bufferedReader.readLine()) != null) {
 				if (line.length() > 0) {
 					Log.d(TAG, "loadSqlFile - loadSqlFile: loaded line - "
 							+ line);
 					db.execSQL(line);
 				}
 			}
 		} catch (IOException e) {
 			Log.w(TAG, "loadSqlFile - IO error during", e);
 		} catch (SQLException e) {
 			Log.w(TAG, e.getMessage() + "(" + line + ")", e);
 		} finally {
 			if (bufferedReader != null) {
 				try {
 					bufferedReader.close();
 				} catch (IOException e) {
 					Log.w(TAG, "loadSqlFile - Can't close buffered reader: :",
 							e);
 				}
 			}
 			if (inputStream != null) {
 				try {
 					inputStream.close();
 				} catch (IOException e) {
 					Log.w(TAG, "loadSqlFile - Can't close buffered reader: :",
 							e);
 				}
 			}
 		}
 	}
 
 	public final String getDatabaseFileName() {
 		return databaseFileName;
 	}
 
 	public final void setDatabaseFileName(final String databaseFileName) {
 		this.databaseFileName = databaseFileName;
 	}
 
 	public final String getPackagePath() {
 		return packagePath;
 	}
 
 	public final void setPackagePath(final String packagePath) {
 		this.packagePath = packagePath;
 	}
 
 	// public final SqlResource getSqlFiles() {
 	// return sqlFiles;
 	// }
 	//
 	// public final void setSqlFiles(final SqlResource sqlFileName) {
 	// this.sqlFiles = sqlFileName;
 	// }
 
 	public final Context getContext() {
 		return context;
 	}
 
 	public final void setContext(final Context context) {
 		this.context = context;
 	}
 
 }
