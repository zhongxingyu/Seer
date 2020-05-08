 //    LS App - LoadSensing Application - https://github.com/Skamp/LS-App
 //    
 //    Copyright (C) 2011-2012
 //    Authors:
 //        Sergio Gonzlez Dez        [sergio.gd@gmail.com]
 //        Sergio Postigo Collado      [spostigoc@gmail.com]
 //
 //    This program is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    This program is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.lsn.LoadSensing.SQLite;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class LSNSQLiteHelper extends SQLiteOpenHelper {
 
 	// Sentencia SQL para crear la tabla de Usuarios
 	String sqlCreateNetwork = "CREATE TABLE Network (user TEXT, poblacio TEXT, name TEXT, "
			+ "idNetwork TEXT, sensors INTEGER, lat TEXT, lon TEXT);";
 
 	String sqlCreateSensor = "CREATE TABLE Sensor (user TEXT, poblacio TEXT, name TEXT, " +
 			"serial, idSensor TEXT, idNetwork TEXT, type TEXT, channel TEXT, " +
			"description TEXT, lat TEXT, lon TEXT, image TEXT, faves INTEGER);";
 
 	String sqlCreateImage = "CREATE TABLE Image (user TEXT, poblacio TEXT, name TEXT, "
			+ "idImage TEXT, idNetwork TEXT, imageFile TEXT);";
 
 	public LSNSQLiteHelper(Context contexto, String nombre,
 			CursorFactory factory, int version) {
 		super(contexto, nombre, factory, version);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// Se ejecuta la sentencia SQL de creacin de la tabla
 		db.execSQL(sqlCreateNetwork);
 		db.execSQL(sqlCreateSensor);
 		db.execSQL(sqlCreateImage);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 
 	}
 }
