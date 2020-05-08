 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android.db;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxi.FFXIString;
 import com.github.kanata3249.ffxieq.Equipment;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.*;
 
 public class AugmentTable {
 	//
 	static final String TABLE_NAME = "Augment";
 	//
 	public static final String C_Id = "_id";
 	public static final String C_BaseId = "BaseEquipmentID";
 	public static final String C_Name = "Name";
 	public static final String C_Part = "Part";
 	public static final String C_Weapon = "Weapon";
 	public static final String C_Job = "Job";
 	public static final String C_Race = "Race";
 	public static final String C_Level = "Lv";
 	public static final String C_Rare = "Rare";
 	public static final String C_Ex = "Ex";
 	public static final String C_Description = "Description";
 	public static final String C_Augment = "Augment";
 	
 	public AugmentTable() { };
 
 	public void create_table(SQLiteDatabase db) {
 		StringBuilder createSql = new StringBuilder();
 		
 		createSql.append("create table " + TABLE_NAME + " (");
 		createSql.append(C_Id + " integer primary key autoincrement not null,");
 		createSql.append(C_BaseId + " integer not null,");
 		createSql.append(C_Name + " text not null,");
 		createSql.append(C_Part + " text not null,");
 		createSql.append(C_Weapon + " text not null,");
 		createSql.append(C_Job + " text not null,");
 		createSql.append(C_Race + " text not null,");
 		createSql.append(C_Level + " integer not null,");
 		createSql.append(C_Rare + " integer not null,");
 		createSql.append(C_Ex + " integer not null,");
 		createSql.append(C_Description + " text, ");
 		createSql.append(C_Augment + " text");
 		createSql.append(")");
 		
 		db.execSQL(createSql.toString());
 	}
 
 	// DA methods
 	public Equipment newInstance(FFXIDAO dao, SQLiteDatabase db, long id, long augId) {
 		Cursor cursor;
 		Equipment newInstance;
 		String []columns = { C_Id, C_Name, C_Part, C_Weapon, C_Job, C_Race, C_Level, C_Rare, C_Ex, C_Description, C_BaseId, C_Augment};
 
 		try {
 			cursor = db.query(TABLE_NAME, columns, C_Id + " = '" + augId + "'", null, null, null, null, null);
 		} catch (SQLiteException e) {
 			return null;
 		}
 		if (cursor.getCount() < 1) {
 			// no match
 			cursor.close();
 			return null;
 		}
 		cursor.moveToFirst();
 		newInstance = new Equipment(cursor.getLong(cursor.getColumnIndex(C_BaseId)), cursor.getString(cursor.getColumnIndex(C_Name)),
 									cursor.getString(cursor.getColumnIndex(C_Part)), cursor.getString(cursor.getColumnIndex(C_Weapon)),
 									cursor.getString(cursor.getColumnIndex(C_Job)), cursor.getString(cursor.getColumnIndex(C_Race)),
 									cursor.getInt(cursor.getColumnIndex(C_Level)), cursor.getInt(cursor.getColumnIndex(C_Rare)) != 0,
 									cursor.getInt(cursor.getColumnIndex(C_Ex)) != 0, cursor.getString(cursor.getColumnIndex(C_Description)));
 		if (newInstance != null) {
 			newInstance.setAugId(augId);
 			newInstance.setAugment(cursor.getString(cursor.getColumnIndex(C_Augment)));
 		}
 		cursor.close();
 		
 		return newInstance;
 	}
 
 	public Cursor getCursor(FFXIDAO dao, SQLiteDatabase db, int part, int race, int job, int level, String[] columns, String orderBy, String filter, String weaponType) {
 		Cursor cursor;
 		String partStr, jobStr, alljobStr;
 		String filterexp;
 		String weaponexp;
 		
 		partStr = dao.getString(FFXIString.PART_DB_MAIN + part);
 		jobStr = dao.getString(FFXIString.JOB_DB_WAR + job);
 		alljobStr = dao.getString(FFXIString.JOB_DB_ALL);
 		filterexp = "";
 		if (filter.length() > 0) {
			filterexp = " AND (" + C_Name + " LIKE '%" + filter + "%' OR " + C_Description + " LIKE '%" + filter + "%')";
 		}
 		weaponexp = "";
 		if (weaponType.length() > 0) {
 			weaponexp = " AND (" + C_Weapon + " LIKE '%" + weaponType + "%')";
 		}
 		
 		try {
 			cursor = db.query(TABLE_NAME, columns,
 					C_Part + " LIKE '%" + partStr + "%' AND " +
 					C_Level + " <= '" + level + "' AND " +
 					"(" + C_Job + " LIKE '%" + jobStr + "%' OR " + C_Job + " = '" + alljobStr + "')" + weaponexp + filterexp,
 					null, null, null, orderBy);
 		} catch (SQLiteException e) {
 			cursor = null;
 		}
 
 		return cursor;
 	}
 	
 	public Cursor getCursor(SQLiteDatabase db, String[] columns, String orderBy) {
 		Cursor cursor;
 		
 		try {
 			cursor = db.query(TABLE_NAME, columns, null, null, null, null, orderBy);
 		} catch (SQLiteException e) {
 			cursor = null;
 		}
 
 		return cursor;
 	}
 	
 	public String []getAvailableWeaponTypes(FFXIDAO dao, SQLiteDatabase db, int part, int race, int job, int level, String filter) {
 		Cursor cursor;
 		String partStr, jobStr, alljobStr;
 		String filterexp;
 		String result[];
 		String columns[] = { C_Weapon };
 		
 		partStr = dao.getString(FFXIString.PART_DB_MAIN + part);
 		jobStr = dao.getString(FFXIString.JOB_DB_WAR + job);
 		alljobStr = dao.getString(FFXIString.JOB_DB_ALL);
 		filterexp = "";
 		if (filter.length() > 0) {
			filterexp = " AND (" + C_Name + " LIKE '%" + filter + "%' OR " + C_Description + " LIKE '%" + filter + "%')";
 		}
 		
 		result = null;
 		try {
 			cursor = db.query(TABLE_NAME, columns,
 					C_Part + " LIKE '%" + partStr + "%' AND " +
 					C_Level + " <= '" + level + "' AND " +
 					"(" + C_Job + " LIKE '%" + jobStr + "%' OR " + C_Job + " = '" + alljobStr + "')" + filterexp,
 					null, C_Weapon, null, C_Weapon);
 			
 			if (cursor.getCount() > 0) {
 				result = new String[cursor.getCount()];
 				
 				cursor.moveToFirst();
 				for (int i = 0; i < result.length; i++) {
 					result[i] = cursor.getString(cursor.getColumnIndex(C_Weapon));
 					cursor.moveToNext();
 				}
 			}
 			
 			cursor.close();
 		} catch (SQLiteException e) {
 		}
 
 		return result;
 	}
 
 	public long saveAugment(SQLiteDatabase db, long id, String augment, Equipment base) {
 		ContentValues values = new ContentValues();;
 		long newId;
 
 		values.put(C_BaseId, base.getId());
 		values.put(C_Name, base.getName());
 		values.put(C_Part, base.getPart());
 		values.put(C_Weapon, base.getWeapon());
 		values.put(C_Job, base.getJob());
 		values.put(C_Race, base.getRace());
 		values.put(C_Level, base.getLevel());
 		values.put(C_Rare, base.isRare() ? 1 : 0);
 		values.put(C_Ex, base.isEx() ? 1 : 0);
 		values.put(C_Description, base.getDescription());
 		values.put(C_Augment, augment);
 
 		db.beginTransaction();
 		newId = id;
 		try {
 			create_table(db);
 		} catch (SQLiteException e) {
 			// Ignore
 		}
 		try {
 			if (id >= 0) {
 				db.update(TABLE_NAME, values, C_Id + " ='" + id + "'", null);
 			} else {
 				newId = db.insert(TABLE_NAME, null, values);
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		return newId;
 	}
 
 	public long saveAugment(SQLiteDatabase db, long id, long baseId, String name, String part, String weapon, String job, String race, int level, boolean rare, boolean ex, String description, String augment) {
 		ContentValues values = new ContentValues();;
 		long newId;
 
 		values.put(C_Id, id);
 		values.put(C_BaseId, baseId);
 		values.put(C_Name, name);
 		values.put(C_Part, part);
 		values.put(C_Weapon, weapon);
 		values.put(C_Job, job);
 		values.put(C_Race, race);
 		values.put(C_Level, level);
 		values.put(C_Rare, rare ? 1 : 0);
 		values.put(C_Ex, ex ? 1 : 0);
 		values.put(C_Description, description);
 		values.put(C_Augment, augment);
 
 		db.beginTransaction();
 		newId = id;
 		try {
 			create_table(db);
 		} catch (SQLiteException e) {
 			// Ignore
 		}
 		try {
 			boolean update;
 			
 			update = false;
 			if (id >= 0) {
 				String columns[] = { C_Id };
 				Cursor cursor = db.query(TABLE_NAME, columns, C_Id + " ='" + id + "'", null, null, null, null);
 				if (cursor != null) {
 					if (cursor.getCount() > 0) {
 						update = true;
 					}
 				}
 			}
 			if (update) {
 				db.update(TABLE_NAME, values, C_Id + " ='" + id + "'", null);
 			} else {
 				newId = db.insert(TABLE_NAME, null, values);
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		return newId;
 	}
 	
 	public void deleteAugment(SQLiteDatabase db, long id) {
 		try {
 			db.delete(TABLE_NAME, C_Id + " = '" + id + "'", null);
 		} catch (SQLiteException e) {
 			// Ignore
 		}
 
 		return;
 	}
 }
 
