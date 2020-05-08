 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.team05.data;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 /**
  * This class is the checkpoint adapter class used to communicate with the "checkpoints"
  * table in the SQLite database. It contains information about the columns,
  * basic Create, Read, Delete operations and also the initial "create table"
  * statement.
  * 
  * @author Daniel Kvist
  * 
  */
 public class DBCheckPointAdapter extends DBAdapter
 {
 	public static final String TABLE_CHECKPOINTS = "checkpoints";
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_RID = "rid";
 	public static final String COLUMN_RADIUS = "radius";
 	public static final String COLUMN_NAME = "name";
 	public static final String COLUMN_LATITUDE = "latitude";
 	public static final String COLUMN_LONGITUDE = "longitude";
 
 	public static final String DATABASE_CREATE_CHECKPOINT_TABLE = "create table " + TABLE_CHECKPOINTS + "(" +
 														COLUMN_ID + " integer primary key autoincrement, " + 
 														COLUMN_RID + " integer not null," + 
 														COLUMN_RADIUS + " integer not null, " + 
														COLUMN_NAME + " text not null " +
														COLUMN_LATITUDE + "integer not null" +
														COLUMN_LONGITUDE + "integer not null);";
 
 	/**
 	 * The constructor of the class which creates a new instance of the database
 	 * helper and stores it as an instance of the class
 	 * 
 	 * @param context
 	 *            the context which to operate in
 	 */
 	public DBCheckPointAdapter(Context context)
 	{
 		super(context);
 	}
 
 	/**
 	 * Inserts a new checkpoint into the database
 	 * 
 	 * @param rid
 	 *            the route id
 	 * @param radius
 	 *            the radius that the checkpoint is active in
 	 * @param name
 	 *            the name of the checkpoint
 	 * @return the new id for the checkpoint
 	 */
 	public long insertCheckpoint(long rid, int radius, String name, int latitude, int longitude)
 	{
 		ContentValues values = new ContentValues();
 		values.put(COLUMN_RID, rid);
 		values.put(COLUMN_RADIUS, radius);
 		values.put(COLUMN_NAME, name);
 		values.put(COLUMN_LATITUDE, latitude);
 		values.put(COLUMN_LONGITUDE, longitude);
 		return db.insert(TABLE_CHECKPOINTS, null, values);
 	}
 
 	/**
 	 * Fetches a checkpoint from the database with the corresponding id.
 	 * 
 	 * @param id
 	 *            the id for the checkpoint
 	 * @return a Cursor pointing to the result set
 	 */
 	public Cursor fetchCheckPointById(long id)
 	{
 		return db.query(TABLE_CHECKPOINTS, null, COLUMN_ID + "=" + id, null, null, null, COLUMN_ID + " asc");
 	}
 
 	/**
 	 * Fetches all checkpoints from the database with the corresponding route
 	 * id.
 	 * 
 	 * @param rid
 	 *            the route id for the checkpoint
 	 * @return a Cursor pointing to the result set
 	 */
 	public Cursor fetchCheckPointByRid(long rid)
 	{
 		return db.query(TABLE_CHECKPOINTS, null, COLUMN_RID + "=" + rid, null, null, null, COLUMN_ID + " asc");
 	}
 
 	/**
 	 * Deletes a single checkpoint with the corresponding id from the database.
 	 * 
 	 * @param cid
 	 *            the id of the checkpoint
 	 * @return the number of rows affected
 	 */
 	public int deleteCheckPointById(long cid)
 	{
 		return db.delete(TABLE_CHECKPOINTS, COLUMN_ID + "=" + cid, null);
 	}
 
 	/**
 	 * Deletes all checkpoints with the corresponding route id from the
 	 * database.
 	 * 
 	 * @param rid
 	 *            the route id for the checkpoint
 	 * @return the number of rows affected
 	 */
 	public int deleteCheckPointByRid(long rid)
 	{
 		return db.delete(TABLE_CHECKPOINTS, COLUMN_RID + "=" + rid, null);
 	}
 }
