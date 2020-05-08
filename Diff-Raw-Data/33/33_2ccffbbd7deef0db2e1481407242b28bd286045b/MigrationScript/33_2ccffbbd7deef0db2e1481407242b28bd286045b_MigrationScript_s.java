 package se.purplescout.purplemow.onboard.db.migration;
 
 import com.j256.ormlite.support.ConnectionSource;
 
 import android.database.sqlite.SQLiteDatabase;
 
 public class MigrationScript  {
 
	public static void execute(SQLiteDatabase db, ConnectionSource conn, int databaseVersion) {
		if (databaseVersion < 1) {
			//testing
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-22 12:30:00.000', '2012-07-22 10:00:00.000', 1, 1)");
		}
		
		if (databaseVersion < 4) {
			//testing
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-23 12:30:00.000', '2012-07-23 10:00:00.000', 1, 2)");
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-24 12:30:00.000', '2012-07-24 10:00:00.000', 1, 3)");
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-25 12:30:00.000', '2012-07-25 10:00:00.000', 1, 4)");
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-26 12:30:00.000', '2012-07-26 10:00:00.000', 1, 5)");
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-27 12:30:00.000', '2012-07-27 10:00:00.000', 1, 6)");
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-28 12:30:00.000', '2012-07-28 10:00:00.000', 1, 7)");
 		}
 	}
	
 	public static void insertData(SQLiteDatabase db) {
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-22 12:30:00.000', '2012-07-22 10:00:00.000', 1, 1)");
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-23 12:30:00.000', '2012-07-23 10:00:00.000', 1, 2)");
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-24 12:30:00.000', '2012-07-24 10:00:00.000', 1, 3)");
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-25 12:30:00.000', '2012-07-25 10:00:00.000', 1, 4)");
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-26 12:30:00.000', '2012-07-26 10:00:00.000', 1, 5)");
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-27 12:30:00.000', '2012-07-27 10:00:00.000', 1, 6)");
 		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-28 12:30:00.000', '2012-07-28 10:00:00.000', 1, 7)");
 	}
 }
