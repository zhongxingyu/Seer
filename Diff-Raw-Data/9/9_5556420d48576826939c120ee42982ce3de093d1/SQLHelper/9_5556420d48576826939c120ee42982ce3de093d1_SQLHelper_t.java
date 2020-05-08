 package com.tihonchik.lenonhonor360.db;
 
 public interface SQLHelper {
 	public static final int DATABASE_VERSION = 1;
 	public static final String DATABASE_NAME = "blogDb";
 	public static final String TABLE_BLOG_ENTRIES = "blogEntries";
 	public static final String TABLE_IMAGES = "blogImages";
 
 	/* BLOG ENTRIES table */
 	public static final String KEY_BLOG_ID = "id";
 	public static final String KEY_BLOG_CREATED = "created";
 	public static final String KEY_BLOG_TITLE = "title";
 	public static final String KEY_BLOG = "blog";
 	public static final String KEY_BLOG_DATE = "date";
 
 	/* BLOG IMAGES table */
 	public static final String KEY_IMAGE_ID = "id";
 	public static final String KEY_IMAGE_CREATED = "created";
 	public static final String KEY_IMAGE = "image";
 	public static final String KEY_BLOG_ID_FK = "blogIdFk";
 
 	public static final String CREATE_BLOG_TABLE = "CREATE TABLE "
 			+ TABLE_BLOG_ENTRIES + "(" + KEY_BLOG_ID + " INTEGER PRIMARY KEY, "
 			+ KEY_BLOG_CREATED + " TEXT, " + KEY_BLOG_TITLE + " TEXT, "
 			+ KEY_BLOG + " TEXT, " + KEY_BLOG_DATE + " TEXT)";
 
 	public static final String CREATE_IMAGES_TABLE = "CREATE TABLE "
 			+ TABLE_IMAGES + "(" + KEY_IMAGE_ID
 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_IMAGE_CREATED
 			+ " TEXT, " + KEY_IMAGE + " TEXT, " + KEY_BLOG_ID_FK
 			+ " INTEGER, FOREIGN KEY (" + KEY_BLOG_ID_FK + ") REFERENCES "
 			+ TABLE_BLOG_ENTRIES + " (" + KEY_BLOG_ID + "))";
 
 	public static final String SELECT_ENTRIES = "SELECT * FROM "
			+ TABLE_BLOG_ENTRIES + " ORDER BY " + KEY_BLOG_ID + " DESC ";
 
 	public static final String GET_NEWEST_ID = "SELECT " + KEY_BLOG_ID
 			+ " FROM " + TABLE_BLOG_ENTRIES + " ORDER BY " + KEY_BLOG_CREATED
 			+ " DESC Limit 1";
 
 	public static final String GET_BLOG_IMAGE = "SELECT " + KEY_IMAGE
 			+ " FROM " + TABLE_IMAGES + " WHERE " + KEY_BLOG_ID_FK
 			+ " = ? ORDER BY " + KEY_IMAGE_CREATED + " DESC Limit 1";
 
 	public static final String GET_BLOG_IMAGES = "SELECT " + KEY_IMAGE
 			+ " FROM " + TABLE_IMAGES + " WHERE " + KEY_BLOG_ID_FK + " = ? ";
 
 	public static final String DROP_BLOG_TABLE = "DROP TABLE IF EXISTS "
 			+ TABLE_BLOG_ENTRIES;
 
 	public static final String DROP_IMAGE_TABLE = "DROP TABLE IF EXISTS "
 			+ TABLE_IMAGES;
 }
