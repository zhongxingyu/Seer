 package name.apb.android.ponynote.java.db;
 
 /*
  * Pony Note
  * Copyright (C) 2013  Andre-Patrick Bubel <code@andre-bubel.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 import java.sql.SQLException;
 
 /**
  * Database helper which creates and upgrades the database and provides the DAOs for the app.
  * <p/>
  * TODO: Tests and License
  */
 public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
 
     private static final String DATABASE_NAME = "ponynote.db";
     private static final int DATABASE_VERSION = 1;
 
     private Dao<Note, Integer> noteDao;
 
     public DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
         try {
            TableUtils.createTable(connectionSource, Note.class);
         } catch (SQLException e) {
             Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
         try {
             TableUtils.dropTable(connectionSource, Note.class, true);
             onCreate(sqliteDatabase, connectionSource);
         } catch (SQLException e) {
             Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
                     + newVer, e);
         }
     }
 
     public Dao<Note, Integer> getNoteDao() throws SQLException {
         if (noteDao == null) {
             noteDao = getDao(Note.class);
         }
         return noteDao;
     }
 }
