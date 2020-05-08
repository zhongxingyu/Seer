 package se.purplescout.purplemow.onboard.db.sqlhelper;
 
 import java.sql.SQLException;
 
 import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;
 import se.purplescout.purplemow.onboard.db.migration.MigrationScript;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 public class PurpleMowSqliteOpenHelper extends OrmLiteSqliteOpenHelper {
 
 	private static final String DATABASE_NAME = "purplemow.db";
	private static final int DATABASE_VERSION = 5;
 
 	public PurpleMowSqliteOpenHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		Log.i(PurpleMowSqliteOpenHelper.class.getName(), "constructor");
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db, ConnectionSource conn) {
 		Log.i(PurpleMowSqliteOpenHelper.class.getName(), "onCreate");
 		try {
 			TableUtils.createTable(conn, ScheduleEvent.class);
 			MigrationScript.insertData(db);
 		} catch (SQLException e) {
 			Log.e(getClass().getCanonicalName(), e.getMessage(), e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, ConnectionSource conn, int currentVersion, int targetVersion) {
 		Log.i(PurpleMowSqliteOpenHelper.class.getName(), "onUpgrade");
 		MigrationScript.execute(db, conn, currentVersion);
 	}
 }
