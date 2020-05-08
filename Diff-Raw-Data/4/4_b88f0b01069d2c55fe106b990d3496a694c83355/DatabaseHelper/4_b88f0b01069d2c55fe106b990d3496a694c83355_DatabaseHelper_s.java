 /*
  * Copyright (C) 2013 InventIt Inc.
  * 
  * See https://github.com/inventit/moatandroid-examples
  */
 package com.yourinventit.moat.android.example;
 
 import java.sql.SQLException;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 /**
  * 
  * @author dbaba@yourinventit.com
  * 
  */
 public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
 
 	/**
 	 * The name of the database file
 	 */
 	private static final String DATABASE_NAME = "moatexample.db";
 
 	/**
 	 * The version of the database schema
 	 */
 	private static final int DATABASE_VERSION = 1;
 
 	/**
 	 * {@link RuntimeExceptionDao} for {@link ShakeEvent}
 	 */
 	private RuntimeExceptionDao<ShakeEvent, String> shakeEventDao = null;
 
 	/**
 	 * @param context
 	 */
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	/**
 	 * @return the shakeEventDao
 	 */
 	public RuntimeExceptionDao<ShakeEvent, String> getShakeEventDao() {
 		if (this.shakeEventDao == null) {
			this.shakeEventDao = getRuntimeExceptionDao(ShakeEvent.class);
 		}
 		return shakeEventDao;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase,
 	 *      com.j256.ormlite.support.ConnectionSource)
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
 		try {
 			TableUtils.createTable(connectionSource, ShakeEvent.class);
 
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
 	 *      com.j256.ormlite.support.ConnectionSource, int, int)
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
 			int oldVersion, int newVersion) {
 		try {
 			TableUtils.dropTable(connectionSource, ShakeEvent.class, true);
 			onCreate(db, connectionSource);
 
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#close()
 	 */
 	@Override
 	public void close() {
 		super.close();
 		this.shakeEventDao = null;
 	}
 
 }
