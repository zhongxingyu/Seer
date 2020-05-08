 package rs.pedjaapps.tvshowtracker.utils;
 
 import android.content.*;
 import android.database.*;
 import android.database.sqlite.*;
 import android.util.Log;
 
 import java.util.*;
 
 import rs.pedjaapps.tvshowtracker.model.Actor;
 import rs.pedjaapps.tvshowtracker.model.EpisodeItem;
 import rs.pedjaapps.tvshowtracker.model.Show;
 
 public class DatabaseHandler extends SQLiteOpenHelper
 {
 
     /*public static enum SORT
 	{
 		series_name("series_name"),
 		network("network"),
 		rating("rating"),
 		runtime("runtime"),
 		status("status"),
 		id("id");
 		
 		String mValue;
 		
 		SORT(String value)
 		{
 			mValue = value;
 		}
 		
 		public String value()
 		{
 			return mValue;
 		}
 		
 	}*/
 	// All Static variables
 	// Database Version
 	private static final int DATABASE_VERSION = 2;
 
 	// Database Name
 	private static final String DATABASE_NAME = "tvst.db";
 
 	// table names
 	private static final String TABLE_SERIES = "series";
 	private static final String TABLE_PROFILES = "profiles";
 	private static final String TABLE_EPISODES = "episodes";
 	private static final String TABLE_ACTORS = "actors";
 
 	// private static final String TABLE_ITEM = "item_table";
 	// Table Columns names
 
 	private static final String[] show_filds = {"series_name",
 			"series_id", "language", "banner", "network", "first_aired",
 			"imdb_id", "overview", "rating", "runtime", "status", "fanart",
 			"ignore_agenda", "hide_from_list", "updated", "actors",
 			"profile_name", "id" };
 	private static final String[] episode_filds = {"episode", "season",
 			"episode_name", "first_aired", "imdb_id", "overview", "rating",
 			"watched", "episode_id", "seriesId", "profile_name", "id" };
 	private static final String[] actors_filds = {"actor_id", "name",
 			"role", "image", "seriesId", "profile_name" };
 	private static final String[] profile_filds = { "_id", "name" };
 
 	SQLiteDatabase db;
 	static DatabaseHandler databaseHandler = null;
 	
 	public DatabaseHandler(Context context)
 	{
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	public static synchronized DatabaseHandler getInstance(Context context)
 	{
 		if(databaseHandler == null)
 		{
 			databaseHandler = new DatabaseHandler(context);
 		}
 		return databaseHandler;
 	}
 	
 	public boolean open()
 	{
 		try
 		{
 		     db = this.getWritableDatabase();
 			 return true;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public void close()
 	{
 		try
 		{
 			db.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		super.close();
 	}
 	
 	// Creating Tables
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 
 		String CREATE_PROFILES_TABLE = "CREATE TABLE " + TABLE_PROFILES + "("
 				+ profile_filds[0] + " INTEGER PRIMARY KEY," + profile_filds[1]
 				+ " TEXT" + ")";
 
 		String CREATE_SERIES_TABLE = "CREATE TABLE " + TABLE_SERIES 
 		        + "("
 				+ show_filds[0] + " TEXT,"
 				+ show_filds[1] + " INTEGER,"
 				+ show_filds[2] + " TEXT,"
 				+ show_filds[3] + " TEXT,"
 				+ show_filds[4] + " TEXT,"
 				+ show_filds[5] + " TEXT,"
 				+ show_filds[6] + " TEXT,"
 				+ show_filds[7] + " TEXT,"
 				+ show_filds[8] + " DOUBLE,"
 				+ show_filds[9] + " INTEGER,"
 				+ show_filds[10] + " TEXT,"
 				+ show_filds[11] + " TEXT,"
 				+ show_filds[12] + " BOOLEAN,"
 				+ show_filds[13] + " BOOLEAN,"
 				+ show_filds[14] + " TEXT,"
 				+ show_filds[15] + " TEXT,"
 			    + show_filds[16] + " TEXT,"
 			    + show_filds[17] + " INTEGER NOT NULL,"
 			+ "PRIMARY KEY ( " + show_filds[1] + ", " + show_filds[16] + ", " + show_filds[17] + " )"
 				+ ")";
 
 		String CREATE_INDEXES_ON_SERIES_TABLE = "CREATE INDEX series_idx ON " + TABLE_SERIES + "(series_id, profile_name, id)";
 
         String CREATE_EPISODE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EPISODES
 		        + "("
 			    + episode_filds[0] + " INTEGER,"
                 + episode_filds[1] + " INTEGER,"
                 + episode_filds[2] + " TEXT,"
                 + episode_filds[3] + " TEXT,"
                 + episode_filds[4] + " TEXT,"
                 + episode_filds[5] + " TEXT,"
                 + episode_filds[6] + " DOUBLE,"
                 + episode_filds[7] + " BOOLEAN,"
                 + episode_filds[8] + " INTEGER,"
 			    + episode_filds[9] + " TEXT,"
 			    + episode_filds[10] + " TEXT,"
 			    + episode_filds[11] + " INTEGER NOT NULL,"
 			+ "PRIMARY KEY ( " + episode_filds[8] + ", " + episode_filds[9] + ", " + episode_filds[10] + ")"
                 + ")";
 
         String CREATE_INDEXES_ON_EPISODES_TABLE = "CREATE INDEX episodes_idx ON " + TABLE_EPISODES + "(seriesId, profile_name, id)";
 
         String CREATE_ACTORS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ACTORS
 			    + "("
 			    + actors_filds[0] + " TEXT,"
                 + actors_filds[1] + " TEXT,"
 			    + actors_filds[2] + " TEXT,"
                 + actors_filds[3] + " TEXT,"
 			    + actors_filds[4] + " TEXT,"
                 + actors_filds[5] + " TEXT,"
                 + "PRIMARY KEY ( " + actors_filds[0] + ", " + actors_filds[4] + ", " + actors_filds[5] + ")"
                 + ")";
 
         String CREATE_INDEXES_ON_ACTORS_TABLE = "CREATE INDEX actors_idx ON " + TABLE_ACTORS + "(seriesId, profile_name)";
 
         db.execSQL(CREATE_PROFILES_TABLE);
 		ContentValues values = new ContentValues();
 		values.put(profile_filds[1], "Default");
 
 		// Inserting Row
 		db.insert(TABLE_PROFILES, null, values);
 		db.execSQL(CREATE_SERIES_TABLE);
 		db.execSQL(CREATE_INDEXES_ON_SERIES_TABLE);
 
 		db.execSQL(CREATE_EPISODE_TABLE);
         db.execSQL(CREATE_INDEXES_ON_EPISODES_TABLE);
 
 		db.execSQL(CREATE_ACTORS_TABLE);
         db.execSQL(CREATE_INDEXES_ON_ACTORS_TABLE);
 	}
 
 	// Upgrading database
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		// Drop older table if existed
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERIES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTORS);
 
 		// Create tables again
 		onCreate(db);
 	}
 
 	/**
 	 * All CRUD(Create, Read, Update, Delete) Operations
 	 */
 
 	public void insertShows(List<Show> shows)
 	{
 	    final SQLiteStatement statement = db.compileStatement("INSERT OR REPLACE INTO " + TABLE_SERIES + " (id, series_name, first_aired, imdb_id, overview, rating, series_id, language, banner, fanart, network, runtime, status, updated, profile_name) VALUES((SELECT IFNULL(MAX(id), 0) + 1 FROM series), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 	    db.beginTransaction();
 	    try 
 	    {
 	        for(Show show : shows)
 	        {
 	            statement.clearBindings();
 	            statement.bindString(1, show.getSeriesName());
 	            statement.bindString(2, show.getFirstAired());
 	            statement.bindString(3, show.getImdbId());
 	            statement.bindString(4, show.getOverview());
 	            statement.bindDouble(5, show.getRating());
 	            statement.bindLong(6, show.getSeriesId());
 	            statement.bindString(7, show.getLanguage());
 	            statement.bindString(8, show.getBanner());
 	            statement.bindString(9, show.getFanart());
 	            statement.bindString(10, show.getNetwork());
 	            statement.bindLong(11, show.getRuntime());
 	            statement.bindString(12, show.getStatus());
 	            statement.bindString(13, show.getUpdated());
 	            statement.bindString(14, show.getProfileName());
 	            statement.execute();
 	        }
 	        db.setTransactionSuccessful();
 	    } 
 	    finally 
 	    {
 	        db.endTransaction();
 	    }
 	    //db.close();
 	}
 	 
 	 public void wipeDatabase()
 	 {
 		 db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERIES);
 		 db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
 		 db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODES);
 		 db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTORS);
 		 onCreate(db);
 	 }
 	 
 	public synchronized void addProfile(String profileName)
 	{
 		ContentValues values = new ContentValues();
 		values.put(profile_filds[1], profileName);
 
 		// Inserting Row
 		db.insert(TABLE_PROFILES, null, values);
 	}
 
 	/*public synchronized void addShow(Show s)
 	{
 		ContentValues values = new ContentValues();
 		values.put(show_filds[1], s.getSeriesName());
 		values.put(show_filds[2], s.getSeriesId());
 		values.put(show_filds[3], s.getLanguage());
 		values.put(show_filds[4], s.getBanner());
 		values.put(show_filds[5], s.getNetwork());
 		values.put(show_filds[6], s.getFirstAired());
 		values.put(show_filds[7], s.getImdbId());
 		values.put(show_filds[8], s.getOverview());
 		values.put(show_filds[9], s.getRating());
 		values.put(show_filds[10], s.getRuntime());
 		values.put(show_filds[11], s.getStatus());
 		values.put(show_filds[12], s.getFanart());
 		values.put(show_filds[13], s.isIgnore());
 		values.put(show_filds[14], s.isHide());
 		values.put(show_filds[15], s.getUpdated());
 		values.put(show_filds[16], s.getActors());
 		values.put(show_filds[17], s.getProfileName());
 
 		// Inserting Row
 		db.insert(TABLE_SERIES, null, values);
 	}
 
 	public synchronized int updateShow(Show s)
 	{
 		ContentValues values = new ContentValues();
 		values.put(show_filds[1], s.getSeriesName());
 		values.put(show_filds[2], s.getSeriesId());
 		values.put(show_filds[3], s.getLanguage());
 		values.put(show_filds[4], s.getBanner());
 		values.put(show_filds[5], s.getNetwork());
 		values.put(show_filds[6], s.getFirstAired());
 		values.put(show_filds[7], s.getImdbId());
 		values.put(show_filds[8], s.getOverview());
 		values.put(show_filds[9], s.getRating());
 		values.put(show_filds[10], s.getRuntime());
 		values.put(show_filds[11], s.getStatus());
 		values.put(show_filds[12], s.getFanart());
 		values.put(show_filds[13], s.isIgnore());
 		values.put(show_filds[14], s.isHide());
 		values.put(show_filds[15], s.getUpdated());
 		values.put(show_filds[16], s.getActors());
 		values.put(show_filds[17], s.getProfileName());
 
 		return db.update(TABLE_SERIES, values, show_filds[2] + " = ?",
 				new String[] { s.getSeriesId()+"" });
 	}*/
 
 	/**
 	 * @param filter
 	 *            Can be either all, continuing, or ended
 	 */
 	public synchronized List<Show> getAllShows(String filter, String profile, String sortOrder, String sortType)
 	{
 		long startTime = System.currentTimeMillis();
 		List<Show> shows = new ArrayList<Show>();
 		// Select All Query
 		StringBuilder builder = new StringBuilder();
 		builder.append("SELECT  * FROM " + TABLE_SERIES + " WHERE");
 
 		if (filter.equals("ended"))
 		{
 			builder.append(" status LIKE \"%Ended%\"");
 		}
 		else if (filter.equals("continuing"))
 		{
 			builder.append(" status LIKE \"%Continuing%\"");
 		}
 		else
 		{
 			builder.append(" status LIKE \"%\"");
 		}
 		builder.append(" and profile_name LIKE \"%" + profile + "%\" ");
 		if(sortOrder.length() != 0 && columnExists(show_filds, sortOrder))
 		{
 			builder.append("ORDER BY " + sortOrder + " " + sortType);
 		}
 		String selectQuery = builder.toString();// "SELECT  * FROM " +
 												// TABLE_SERIES;
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		// looping through all rows and adding to list
 		if (cursor.moveToFirst())
 		{
 			do
 			{
 				Show show = new Show();
 				show.setSeriesName(cursor.getString(0));
 				show.setSeriesId(cursor.getInt(1));
 				show.setLanguage(cursor.getString(2));
 				show.setBanner(cursor.getString(3));
 				show.setNetwork(cursor.getString(4));
 				show.setFirstAired(cursor.getString(5));
 				show.setImdbId(cursor.getString(6));
 				show.setOverview(cursor.getString(7));
 				show.setRating(cursor.getDouble(8));
 				show.setRuntime(cursor.getInt(9));
 				show.setStatus(cursor.getString(10));
 				show.setFanart(cursor.getString(11));
 				show.setIgnore(intToBool(cursor.getInt(12)));
 				show.setHide(intToBool(cursor.getInt(13)));
 				show.setUpdated(cursor.getString(14));
 				show.setActors(cursor.getString(15));
 				show.setProfileName(cursor.getString(16));
 
 				shows.add(show);
 			}
 			while (cursor.moveToNext());
 		}
 
 		// return list
 		cursor.close();
 		Log.d(Constants.LOG_TAG,
 				"DatabaseHandler.java > getAllShows(): "
 						+ (System.currentTimeMillis() - startTime) + "ms");
 		return shows;
 	}
 
 	public synchronized boolean showExists(String seriesName, String profile)
 	{
 		Cursor cursor = db.query(TABLE_SERIES, show_filds, show_filds[0]
 				+ "=? and profile_name LIKE \"%" + profile + "%\"",
 				new String[] { seriesName }, null, null, null, null);
 		boolean exists = (cursor.getCount() > 0);
 		cursor.close();
 		return exists;
 	}
 
 	public synchronized void deleteSeries(String seriesId, String profile)
 	{
 		db.delete(TABLE_SERIES, show_filds[1]
 				+ " = ? and profile_name LIKE \"%" + profile + "%\"",
 				new String[] { seriesId });
 
 		deleteEpisodes(seriesId, profile);
 		deleteActors(seriesId, profile);
 	}
 
 	public synchronized void deleteEpisodes(String seriesId, String profile)
 	{
 		db.delete(TABLE_EPISODES, "profile_name = ? and seriesId = ?",
 				new String[] { profile, seriesId });
 	}
 
 	public synchronized void deleteActors(String seriesId, String profile)
 	{
 		db.delete(TABLE_ACTORS, "profile_name = ? and seriesId = ?",
 				new String[] { profile, seriesId });
 	}
 
 	public synchronized void deleteProfile(String profile)
 	{
 		db.delete(TABLE_PROFILES, profile_filds[1] + " = ?",
 				new String[] { profile });
 	}
 
 	public synchronized Show getShow(String seriesId, String profile)
 	{
 		Cursor cursor = db.query("series", show_filds, show_filds[1]
 				+ "=? and profile_name LIKE \"%" + profile + "%\"",
 				new String[] { seriesId }, null, null, null, null);
 		if (cursor != null)
 			cursor.moveToFirst();
 		Show e = new Show(cursor.getString(0),
 				cursor.getString(5), cursor.getString(6), cursor.getString(7),
 				cursor.getDouble(8), cursor.getInt(1), cursor.getString(2),
 				cursor.getString(3), cursor.getString(11), cursor.getString(4),
 				cursor.getInt(9), cursor.getString(10),
 				intToBool(cursor.getInt(12)), intToBool(cursor.getInt(13)),
 				cursor.getString(14), cursor.getString(15), cursor.getString(16));
 		// return list
 		cursor.close();
 		return e;
 	}
 
 	/*public synchronized void addEpisode(EpisodeItem e)
 	{
 	    ContentValues values = new ContentValues();
 		values.put(episode_filds[1], e.getEpisode());
 		values.put(episode_filds[2], e.getSeason());
 		values.put(episode_filds[3], e.getEpisodeName());
 		values.put(episode_filds[4], e.getFirstAired());
 		values.put(episode_filds[5], e.getImdbId());
 		values.put(episode_filds[6], e.getOverview());
 		values.put(episode_filds[7], e.getRating());
 		values.put(episode_filds[8], e.isWatched());
 		values.put(episode_filds[9], e.getEpisodeId());
 		values.put(episode_filds[10], e.getSeriesId());
 		values.put(episode_filds[11], e.getProfile());
 
 		// Inserting Row
 		db.insert(TABLE_EPISODES, null, values);
 	}*/
 
     public void insertEpisodes(List<EpisodeItem> episodeItems)
     {
         final SQLiteStatement statement = db.compileStatement("INSERT OR REPLACE INTO " + TABLE_EPISODES
                 + " (id, episode_name, episode, season, first_aired, imdb_id, overview, rating, watched, " +
                 "episode_id, profile_name, seriesId) " +
 				"VALUES((SELECT IFNULL(MAX(id), 0) + 1 FROM episodes), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         db.beginTransaction();
         try
         {
             for(EpisodeItem episodeItem : episodeItems)
             {
                 statement.clearBindings();
                 statement.bindString(1, episodeItem.getEpisodeName());
                 statement.bindLong(2, episodeItem.getEpisode());
                 statement.bindLong(3, episodeItem.getSeason());
                 statement.bindString(4, episodeItem.getFirstAired());
                 statement.bindString(5, episodeItem.getImdbId());
                 statement.bindString(6, episodeItem.getOverview());
                 statement.bindDouble(7, episodeItem.getRating());
                 statement.bindLong(8, boolToInt(episodeItem.isWatched()));
                 statement.bindLong(9, episodeItem.getEpisodeId());
                 statement.bindString(10, episodeItem.getProfile());
                 statement.bindString(11, episodeItem.getSeriesId());
                 statement.execute();
             }
             db.setTransactionSuccessful();
         }
         finally
         {
             db.endTransaction();
         }
         //db.close();
     }
 /*private String actorId;
     private String name;
     private String role;
     private String image;
     private String profile;
     private String seriesId;*/
     public void insertActors(List<Actor> actors)
     {
         final SQLiteStatement statement = db.compileStatement("INSERT OR REPLACE INTO " + TABLE_ACTORS
                 + " (actor_id, name, role, image, profile_name, seriesId) " +
                 "VALUES(?, ?, ?, ?, ?, ?)");
         db.beginTransaction();
         try
         {
             for(Actor actor : actors)
             {
                 statement.clearBindings();
                 statement.bindString(1, actor.getActorId());
                 statement.bindString(2, actor.getName());
                 statement.bindString(3, actor.getRole());
                 statement.bindString(4, actor.getImage());
                 statement.bindString(5, actor.getProfile());
                 statement.bindString(6, actor.getSeriesId());
                 statement.execute();
             }
             db.setTransactionSuccessful();
         }
         finally
         {
             db.endTransaction();
         }
         //db.close();
     }
 
 	/*public synchronized void addActor(Actor a)
 	{
 		ContentValues values = new ContentValues();
 		values.put(actors_filds[1], a.getActorId());
 		values.put(actors_filds[2], a.getName());
 		values.put(actors_filds[3], a.getRole());
 		values.put(actors_filds[4], a.getImage());
 		values.put(actors_filds[5], a.getSeriesId());
 		values.put(actors_filds[6], a.getProfile());
 
 		// Inserting Row
 		db.insert(TABLE_ACTORS, null, values);
 	}*/
 
 	public synchronized EpisodeItem getEpisode(String seriesId, String episodeId,
 			String profile)
 	{
 		Cursor cursor = db.query(TABLE_EPISODES, episode_filds,
 				episode_filds[8] + "=? and profile_name LIKE \"%" + profile
 						+ "%\" and seriesId = " + seriesId , new String[] { episodeId }, null, null, null,
 				null);
 		if (cursor != null)
 			cursor.moveToFirst();
 
 		EpisodeItem e = new EpisodeItem(
 				cursor.getString(0), cursor.getInt(1), cursor.getInt(2),
 				cursor.getString(3), cursor.getString(4), cursor.getString(5),
 				cursor.getDouble(6), intToBool(cursor.getInt(7)),
 				cursor.getInt(8), cursor.getString(10), cursor.getString(9));
 		// return list
 		cursor.close();
 		return e;
 	}
 
 	public synchronized Actor getActor(String seriesId, String actorId, String profile)
 	{
 		Cursor cursor = db.query(TABLE_ACTORS, actors_filds,
 				actors_filds[0] + "=? and profile_name LIKE \"%" + profile
 						+ "%\" and seriesId = " + seriesId, new String[] { actorId }, null, null, null,
 				null);
 		if (cursor != null)
 			cursor.moveToFirst();
 
 		Actor a = new Actor(
 				cursor.getString(0), cursor.getString(1), cursor.getString(2),
 				cursor.getString(3), cursor.getString(5), cursor.getString(6));
 		// return list
 		cursor.close();
 		return a;
 	}
 
 	public synchronized List<String> getAllProfiles()
 	{
 		List<String> profiles = new ArrayList<String>();
 		// Select All Query
 		String selectQuery = "SELECT  * FROM profiles";
 
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst())
 		{
 			do
 			{
 				profiles.add(cursor.getString(1));
 			}
 			while (cursor.moveToNext());
 		}
 
 		// return list
 		cursor.close();
 		return profiles;
 	}
 
 	public synchronized List<EpisodeItem> getAllEpisodes(String seriesId, String profile)
 	{
 		List<EpisodeItem> episodeItems = new ArrayList<EpisodeItem>();
 		// Select All Query
 		String selectQuery = "SELECT  * FROM " + TABLE_EPISODES
				+ " WHERE profile_name LIKE \"%" + profile + "%\" and seriesId = "+seriesId + " ORDER BY season";
 
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst())
 		{
 			do
 			{
 				EpisodeItem episodeItem = new EpisodeItem();
 				episodeItem.setEpisode(cursor.getInt(0));
 				episodeItem.setSeason(cursor.getInt(1));
 				episodeItem.setEpisodeName(cursor.getString(2));
 				episodeItem.setFirstAired(cursor.getString(3));
 				episodeItem.setImdbId(cursor.getString(4));
 				episodeItem.setOverview(cursor.getString(5));
 				episodeItem.setRating(cursor.getDouble(6));
 				episodeItem.setWatched(intToBool(cursor.getInt(7)));
 				episodeItem.setEpisodeId(cursor.getInt(8));
 				episodeItem.setSeriesId(cursor.getString(9));
 				episodeItem.setProfile(cursor.getString(10));
 
 				// Adding to list
 				episodeItems.add(episodeItem);
 			}
 			while (cursor.moveToNext());
 		}
 
 		// return list
 		cursor.close();
 		return episodeItems;
 	}
 
 	public synchronized List<Actor> getAllActors(String seriesId, String profile)
 	{
 		List<Actor> actors = new ArrayList<Actor>();
 		// Select All Query
 		String selectQuery = "SELECT  * FROM " + TABLE_ACTORS
 				+ " WHERE profile_name LIKE \"%" + profile + "%\" and seriesId = " + seriesId;
 
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		if (cursor.moveToFirst())
 		{
 			do
 			{
 				Actor actor = new Actor();
 				actor.setActorId(cursor.getString(0));
 				actor.setName(cursor.getString(1));
 				actor.setRole(cursor.getString(2));
 				actor.setImage(cursor.getString(3));
 				actor.setSeriesId(cursor.getString(4));
 				actor.setProfile(cursor.getString(5));
 
 				// Adding to list
 				actors.add(actor);
 			}
 			while (cursor.moveToNext());
 		}
 
 		// return list
 		cursor.close();
 		return actors;
 	}
 
 	/*
 	 * public void deleteEpisode(EpisodeItem e, String seriesId) {
 	 * SQLiteDatabase db = this.getWritableDatabase();
 	 * db.delete("episodes_"+seriesId, episode_filds[9] + " = ?", new String[] {
 	 * String.valueOf(e.getEpisodeId()) });
 	 * 
 	 * db.close(); }
 	 */
 
 	public synchronized boolean episodeExists(String seriesId, String episodeId,
 			String profile)
 	{
 		Cursor cursor = db.query(TABLE_EPISODES, episode_filds,
 				episode_filds[8] + "=? and profile_name LIKE \"%" + profile
 						+ "%\" and seriesId = "+ seriesId, new String[] { episodeId }, null, null, null,
 				null);
 		boolean exists = (cursor.getCount() > 0);
 		cursor.close();
 		return exists;
 	}
 
 	public synchronized boolean actorExists(String seriesId, String actorId, String profile)
 	{
 		Cursor cursor = db.query(TABLE_ACTORS, actors_filds,
 				actors_filds[0] + "=? and profile_name LIKE \"%" + profile
 						+ "%\" and seriesId = " + seriesId, new String[] { actorId }, null, null, null,
 				null);
 		boolean exists = (cursor.getCount() > 0);
 		cursor.close();
 		return exists;
 	}
 
 	/*public synchronized int updateEpisode(EpisodeItem e)
 	{
 		ContentValues values = new ContentValues();
 		values.put(episode_filds[1], e.getEpisode());
 		values.put(episode_filds[2], e.getSeason());
 		values.put(episode_filds[3], e.getEpisodeName());
 		values.put(episode_filds[4], e.getFirstAired());
 		values.put(episode_filds[5], e.getImdbId());
 		values.put(episode_filds[6], e.getOverview());
 		values.put(episode_filds[7], e.getRating());
 		values.put(episode_filds[8], e.isWatched());
 		values.put(episode_filds[9], e.getEpisodeId());
 		values.put(episode_filds[10], e.getSeriesId());
 		values.put(episode_filds[11], e.getProfile());
 
 		int count = db.update(TABLE_EPISODES, values, episode_filds[9]
 				+ " = ?  and profile_name LIKE \"%" + e.getProfile() + "%\" and seriesId = " + e.getSeriesId(),
 				new String[] { e.getEpisodeId()+"" });
 		return count;
 	}*/
 
 	/*public synchronized int updateActor(Actor a)
 	{
 		ContentValues values = new ContentValues();
 		values.put(actors_filds[1], a.getActorId());
 		values.put(actors_filds[2], a.getName());
 		values.put(actors_filds[3], a.getRole());
 		values.put(actors_filds[4], a.getImage());
 		values.put(actors_filds[5], a.getSeriesId());
 		values.put(actors_filds[6], a.getProfile());
 
 		int count = db.update(TABLE_ACTORS, values, actors_filds[1]
 				+ " = ?  and profile_name LIKE \"%" + a.getProfile() + "%\" and seriesId = "+a.getSeriesId(),
 				new String[] { a.getActorId() });
 		return count;
 	}*/
 
 	public synchronized int getEpisodesCount(String seriesId, String profile)
 	{
 		String countQuery = "SELECT  * FROM "+ TABLE_EPISODES
 				+ " WHERE profile_name LIKE \"%" + profile + "%\" and seriesId = " + seriesId;
 		Cursor cursor = db.rawQuery(countQuery, null);
 		int count = cursor.getCount();
 		cursor.close();
 		return count;
 	}
 
 	private boolean intToBool(int i)
 	{
 		return i == 1;
 	}
 
     private int boolToInt(boolean bool)
     {
         return bool ? 1 : 0;
     }
 	
 	private boolean columnExists(String[] columns, String column)
 	{
 		boolean exists = false;
 		for(int i = 0; i < columns.length; i++)
 		{
 			if(columns[i].equals(column))
 			{
 				exists = true;
 				break;
 			}
 		}
 		return exists;
 	}
 
 }
