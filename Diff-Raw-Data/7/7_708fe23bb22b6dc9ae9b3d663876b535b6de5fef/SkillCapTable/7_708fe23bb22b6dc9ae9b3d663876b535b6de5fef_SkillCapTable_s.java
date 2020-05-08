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
 
 import android.database.Cursor;
 import android.database.sqlite.*;
 
 public class SkillCapTable {
 	//
 	static final String TABLE_NAME = "SkillCap";
 	//
 	static final String C_Id = "_id";
 	static final String C_Rank = "Rank";
 	static final String C_Level = "Level";
 	static final String C_Value = "Value";
 
 	public SkillCapTable() { };
 
 	// DA methods
 	public int getSkillCap(SQLiteDatabase db, String jobrank, int joblevel, String subjobrank, int subjoblevel) {
 		Cursor cursor;
 		int value;
 		String []columns = { C_Value };
 
 		value = 0;
 		cursor = db.query(TABLE_NAME, columns,
 							C_Rank + " = '" + jobrank + "' AND " + C_Level + " = '" + joblevel + "'",
 							null, null, null, null, null);
 		
 		if (cursor.getCount() > 0) {
 			cursor.moveToFirst();
 			value = cursor.getInt(0);
			cursor.close();
 		}
 
 		if (subjoblevel > 0 && !subjobrank.equals("-")) {
 			cursor = db.query(TABLE_NAME, columns,
 					C_Rank + " = '" + subjobrank + "' AND " + C_Level + " = '" + subjoblevel + "'",
 					null, null, null, null, null);
 	
 			if (cursor.getCount() > 0) {
 				cursor.moveToFirst();
 				value = Math.max(value, cursor.getInt(0));
				cursor.close();
 			}
 		}
 
 		return value;
 	}
 }
 
