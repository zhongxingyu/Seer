 /**
  * @contributor(s): Christian Skjetne (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:		08 December 2011
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 
 /**
  * @description:
  *
  *
  */
 
 package org.ubicompforall.CityExplorer.data;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.ubicompforall.CityExplorer.CityExplorer;
import org.ubicompforall.CityExplorer.gui.MyPreferencesActivity;
 
 import android.content.ContentValues;
 import android.content.Context;
import android.content.SharedPreferences;
 //import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.SQLException;
 import android.database.sqlite.*;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.widget.Toast;
 
 /***
  * How to debug SQL DB with ADB-shell to Phone:
  * http://developer.android.com/guide/topics/data/data-storage.html#netw
  */
 
 /**
  * The Class SQLiteConnector.
  */
 public class SQLiteConnector extends SQLiteOpenHelper implements DatabaseInterface{
 	/** The Constant DB_PATH, which is the path to were the database is saved. */
 
 	// private static final String	DB_PATH = "/data/data/org.ubicompforall.CityExplorer/databases/";
 	private String DB_PATH =""; /*, WEB_DB_PATH = ""; */
 
 	/** The Constant DB_NAME, which is our database name. */
 	//private static final String	DB_NAME = "CityExplorer.backup.db";
 	private static String	DB_NAME = "CityExplorer.sqlite";
 
 
 	/** The SQLiteDatabase object we are using. */
 	private SQLiteDatabase		myDataBase;
 
 	/** The context. */
 	private Context				myContext;
 
 	/** The whole path to our database. */
 	private String				myPath;
 
 	private static final String POI_TABLE = "poi";
 	//private static final String COUNT_ALL_POIS = "SELECT Count(*) FROM "+POI_TABLE;
 
 	/** The Constant SELECT_ALL_POIS, which is a SQL-query for selecting all POIs. */
 	private static final String SELECT_ALL_POIS=	// Change this to String[] columns, see other examples below (RS-120124)
 			"SELECT " +
 					"POI._id," +
 					"POI.title," +
 					"POI.description," +
 					"ADDR.street_name," +
 // ZIP code removed "ADDR.zipcode," +
 					"ADDR.city," +
 					"ADDR.lat," +
 					"ADDR.lon," +
 					"CAT.title, " +
 					"POI.favourite, " +
 					"POI.openingHours, " +
 					"POI.web_page, " +
 					"POI.telephone, " +
 					"POI.image_url, " +
 					"POI.global_id " +
 					"FROM poi as POI, address as ADDR, category as CAT " +
 					"WHERE POI.address_id = ADDR._id AND POI.category_id = CAT._id";
 
 	/**
 	 * Public constructor that takes and keeps a reference of the passed context
 	 * in order to access the application assets and resources.
 	 *
 	 * @param context The context
 	 */
 	public SQLiteConnector(Context context) {
 		super(context, DB_NAME, null, 2);
 		this.myContext = context;
 		debug(0, myPath+" starting up");
 
 		//code from students: myPath = DB_PATH + DB_NAME;
 		File dbName = context.getDatabasePath(DB_NAME);
 		DB_PATH = dbName.getParent();
 		myPath = dbName.toString();
 
		SharedPreferences settings = context.getSharedPreferences( CityExplorer.GENERAL_SETTINGS, 0);
 //		DB_PATH = MyPreferencesActivity.getDbPath( settings ); JF: is set to Web URL if the user has not chosen settings
 		debug(0, "DB_PATH IS "+DB_PATH );
 		
 //		debug(0, "WEB_DB_PATH IS "+ WEB_DB_PATH );
 	}//SQLiteConnector CONSTRUCTOR
 
 
 	
 	private static void debug(int level, String message ){
 		CityExplorer.debug( level, message );
 	} // debug
 
 	
 
 	@Override
 	public boolean addPoiToTrip(Trip t, Poi poi) {
 		Trip trip = t;
 
 		ContentValues values = new ContentValues();
 		values.put("trip_id", trip.getIdPrivate());
 		values.put("poi_id", poi.getIdPrivate());
 		values.put("poi_number", trip.getPois().indexOf(poi)+1);
 
 		try	{
 			myDataBase.insertOrThrow("trip_poi", null, values);
 			System.out.println("POI added to DB: "+
 					"trip_id("+trip.getIdPrivate()+") poi_id("+poi.getIdPrivate()+") poi_number("+(trip.getPois().indexOf(poi)+1)+")");
 			return true;
 		} catch (Exception e) {
 			System.out.println("ERROR in SQL: "+e.getMessage()+" SQL values: "+
 					"trip_id("+trip.getIdPrivate()+") poi_id("+poi.getIdPrivate()+") poi_number("+(trip.getPois().indexOf(poi)+1)+")");
 			e.printStackTrace();
 			return false;
 		}
 	}//addPoiToTrip
 
 	@Override
 	public void addTimesToTrip(Trip trip)
 	{
 		ContentValues values = new ContentValues();
 		values.put("trip_id", trip.getIdPrivate());
 
 		for (Poi poi : trip.getFixedTimes().keySet())
 		{
 			values.put("poi_id", poi.getIdPrivate());
 			values.put("poi_number", trip.getPois().indexOf(poi)+1);
 			values.put("hour", trip.getFixedTimes().get(poi).hour);
 			values.put("minute", trip.getFixedTimes().get(poi).minute);
 			myDataBase.update("trip_poi", values, "trip_id = ? and poi_id = ?", new String[]{Integer.toString(trip.getIdPrivate()), Integer.toString(poi.getIdPrivate())});
 		}
 	} //addTimesToTrip
 
 	@Override
 	public synchronized void close() {
 		if (myDataBase != null){
 			myDataBase.close();
 		}
 		super.close();
 	}//close
 
 	/**
 	 * Creates an empty database on the data/data/project-folder and rewrites it with your own database.
 	 *
 	 * @throws IOException when the asset database file can not be read,
 	 * or the database-destination file can not be written to,
 	 * or when parent directories to the database-destination file do not exist.
 	 */
  	public void createDataBase() throws IOException {
 
 		OutputStream 	osDbPath;
 		InputStream 	isAssetDb 	= myContext.getAssets().open(DB_NAME);
 		byte[] 			buffer 		= new byte[1024 * 64];
 		int 			bytesRead;
 
 		debug(0, "Make copy of default "+DB_NAME+" to "+myPath);
 		try {
 			osDbPath = new FileOutputStream(myPath);
 
 			while ((bytesRead = isAssetDb.read(buffer))>0){
 				try {
 					osDbPath.write(buffer, 0, bytesRead);
 				} catch (IOException io) {
 					debug(0, "Failed to write to " + DB_PATH);
 					io.printStackTrace();
 				}
 				debug(0, "copyDataBase(): wrote " + bytesRead + " bytes");
 			}//while more bytes to copy
 			osDbPath.flush();
 			osDbPath.close();
 			buffer = null;
 			debug(0, DB_NAME+" successufully copied");
 
 			myDataBase = this.getReadableDatabase();
 		} catch (IOException io) {
 			debug(0, "Failed to copy "+ DB_NAME + " to " + DB_PATH);
 			io.printStackTrace();
 		}//try catch (making copy)
 		return;
 	}//createDataBase
 
 	@Override
 	public void deleteFromTrip(Trip trip, Poi poi){
 		myDataBase.delete("trip_poi", "poi_id = ? AND trip_id = ?", new String[]{""+poi.getIdPrivate(), ""+trip.getIdPrivate()});
 		Toast.makeText(myContext, poi.getLabel() + " deleted from tour.", Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void deleteTrip(Trip trip){
 		myDataBase.delete("trip_poi", "trip_id = ?", new String[]{""+trip.getIdPrivate()});
 		myDataBase.delete("trip", "_id = ?", new String[]{""+trip.getIdPrivate()});
 		Toast.makeText(myContext, trip.getLabel() + " deleted.", Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public boolean deletePoi(Poi poi){
 		//Check if the poi is in a trip first.
 		Cursor c = myDataBase.query("trip_poi", new String[]{"trip_id"}, "poi_id = ?", new String[]{""+poi.getIdPrivate()}, null, null, null);
 
 		if(c.getCount() > 0)
 		{
 			//the poi is in a trip. abort!
 			StringBuilder trips = new StringBuilder();
 			while(c.moveToNext()) //make a list of all the trips this poi is in.
 			{
 				if(trips.length() != 0)
 					trips.append(", ");
 				trips.append(this.getTrip(c.getInt(0)).getLabel());
 			}
 
 			Toast.makeText(myContext, poi.getLabel() + " not deleted, because it is in "+c.getCount()+" tour"+((c.getCount() > 1) ? "s":"")+" ("+trips.toString()+")", Toast.LENGTH_LONG).show();
 			c.close();
 			return false;
 		}
 
 		if(myDataBase.delete("poi", "_id = ?", new String[]{""+poi.getIdPrivate()}) > 0)
 		{
 			//poi deleted
 			Toast.makeText(myContext, poi.getLabel() + " deleted", Toast.LENGTH_LONG).show();
 			return true;
 		}
 		else
 		{
 			//the poi is not found
 			Toast.makeText(myContext, poi.getLabel() + " not deleted, because it is not found", Toast.LENGTH_LONG).show();
 			return false;
 		}
 	}//deletePoi
 
 
 	@Override
 	public ArrayList<Poi> getAllPois() {
 		return getPoisFromCursor( myDataBase.rawQuery( SELECT_ALL_POIS, null));
 	}
 
 	@Override
 	public ArrayList<Poi> getAllPois(String category) {
 		return getPoisFromCursor(
 				myDataBase.rawQuery(
 						SELECT_ALL_POIS + " AND CAT.title = ?",
 						new String[]{category}					// replaces ?s
 				)
 		);
 	}//getAllPois(category)
 
 	@Override
 	public ArrayList<Poi> getAllPois(Boolean favourite){
 		return 	getPoisFromCursor(
 				myDataBase.rawQuery(
 						SELECT_ALL_POIS + " AND POI.favourite = ?",
 						new String[]{"" + (favourite? 1 : 0)}	// replaces ?s
 				)
 		);
 	}//getAllPois(favorite)
 
 
 	
 	/***
 	 * Return all Trips of the given TYPE
 	 * @param mode TYPE_ALL, TYPE_FREE, or TYPE_FIXED
 	 */
 	@Override
 	//public ArrayList<Trip> getAllTrips( Boolean free ){
 	public ArrayList<Trip> getTripsWithPOIs( int type ){
 		ArrayList<Trip> trips = new ArrayList<Trip>();
 
 		//CONSTANT key-index mappings
 		final String[] columns = {
 				"TRIP._id",			"TRIP.title",	"TRIP.description",	"TRIP.free_trip",	"TRIP.global_id",
 				"POI._id",			"POI.title",	"POI.description",	"POI.favourite",	"POI.image_url",
 				"POI.global_id",	"POI.telephone",
 				"ADDR.street_name", //ZIP removed:	"ADDR.zipcode",
 				"ADDR.city",		"ADDR.lat",		"ADDR.lon",
 				"CAT.title",
 				"TP.poi_number",	"TP.hour",		"TP.minute"
 		};
 		final Map<String,Integer> key = //new HashMap<String,Integer>(); 		getKeys( key, columns );
 				getKeys( columns );
 		String sqlStr, selectStr, fromStr, whereStr, freeStr, orderStr;
 		selectStr = getSelectStr( columns );
 		fromStr =	" FROM poi as POI, address as ADDR, category as CAT, trip as TRIP, trip_poi as TP ";
 		whereStr =	" WHERE POI._id = TP.poi_id AND TRIP._id = TP.trip_id AND POI.address_id = ADDR._id AND POI.category_id = CAT._id ";
 		freeStr =	" AND TRIP.free_trip = ? ";
 		orderStr =	" ORDER BY TRIP._id, TP.poi_number";
 		
 		Cursor c;
 		if ( type==CityExplorer.TYPE_ALL ){
 			sqlStr = selectStr + fromStr + whereStr + orderStr;
 			c = myDataBase.rawQuery( sqlStr, null );
 		}else{
 			sqlStr = selectStr + fromStr + whereStr + freeStr + orderStr;
 			c = myDataBase.rawQuery(sqlStr, new String[]{"" + (type==CityExplorer.TYPE_FREE? 1 : 0)}); // Fill the "?" in the select with 1 or 0
 		}//if ALL
 		
 		debug(0, "TRIPS: " + c.getCount() );
 		int currentTripId = -1;
 		Trip trip = new Trip.Builder("").build();
 		while(c.moveToNext()){
 			if(c.getInt( key.get("TRIP._id") ) != currentTripId) { //make new trip
 				debug(0, "Got trip: "+c.getString( key.get("TRIP.title") ) );
 				if(currentTripId != -1) { //next trip on the list
 					trips.add(trip);
 				}
 				trip = new Trip.Builder(c.getString( key.get("TRIP.title") ))
 				.idPrivate(c.getInt( key.get("TRIP._id") ))
 				.description(c.getString( key.get("TRIP.description") ))
 				.freeTrip(1==c.getInt( key.get("TRIP.free_trip") ))
 				.idGlobal(c.getInt( key.get("TRIP.global_id") ))
 				.build();
 				currentTripId = trip.getIdPrivate();
 			}//if not current trip - Make new
 
 			Poi poi = new Poi.Builder( c.getString( key.get("POI.title") ),
 				new PoiAddress.Builder( c.getString( key.get("ADDR.city") )
 				)
 //				.zipCode(c.getInt( key.get("ADDR.zipcode") ))	// ZIP code removed, but needed for Google Maps
 				.street(c.getString( key.get("ADDR.street_name") ))
 				.longitude(c.getDouble( key.get("ADDR.lon") )).latitude(c.getDouble( key.get("ADDR.lat") ))
 				.build()
 			).description(c.getString( key.get("POI.description") ))
 			.category(c.getString( key.get("CAT.title") ))
 			.favourite((1==c.getInt( key.get("POI.favourite") )))
 			.imageURL(c.getString( key.get("POI.image_url") ))
 			.telephone(Integer.toString(c.getInt( key.get("POI.telephone") )))
 			.idPrivate(c.getInt( key.get("POI._id") ))
 			.idGlobal(c.getInt( key.get("POI.global_id") )).build();
 			trip.addPoi(poi);
 			if(c.getInt( key.get("TP.hour") ) != -1 && c.getInt( key.get("TP.minute") ) != -1)//add time if it is not -1
 				trip.setTime(poi, new Time(c.getInt( key.get("TP.hour") ), c.getInt( key.get("TP.minute") )));
 		}//while more trips
 		if(currentTripId != -1) { //next trip on the list
 			trips.add(trip);
 		}
 		c.close();
 		
 		trips.addAll( this.getTripsWithoutPOIs( type ) );
 		return trips;
 	}//getAllTrips(free)
 
 	@Override
 	public ArrayList<String>
 	 getCategoryNames() {
 		ArrayList<String> categories = new ArrayList<String>();
 		Cursor c = myDataBase.query("category", new String[]{"title"}, null, null, null, null, null);
 		while (c.moveToNext()) {
 			categories.add(c.getString(0));
 		}
 		c.close();
 		return categories;
 	}//getCategoryNames
 
 
 	@Override
 	public Poi getPoi(int privateId){
 		return 	getPoisFromCursor(
 				myDataBase.rawQuery(
 						SELECT_ALL_POIS + " AND POI._id = ?",
 						new String[]{""+privateId}	// replaces ?s
 				)
 		).get(0);
 	}//getPoi(privId)
 
 	/**
 	 * Gets the pois from the cursor.
 	 *
 	 * @param c The cursor to fetch pois from the database using the query based on SELECT_ALL_POIS.
 	 * @return The pois from the cursor.
 	 * 
 	 * The order of attributes depends of the formulation of the query SELECT_ALL_POIS
 	 *
 	 * 0  _id; 
 	 * 1  title;
 	 * 2  description;
 	 * 3  street_name;
      * x  ZIP code removed  - 4  zipcode;
 	 * 4  city;
 	 * 5  lat;
 	 * 6  lon;
 	 * 7  category_title;
 	 * 8  favourite
 	 * 9  openingHours;
 	 * 10 web_page;
 	 * 11 telephone;
 	 * 12 image_url
 	 * 13 global_id
 	 */
 	private ArrayList<Poi>
 	 getPoisFromCursor(Cursor c){
 		ArrayList<Poi> pois = new ArrayList<Poi>();
 		while(c.moveToNext()){
 			pois.add(
 				new Poi.Builder(
 					c.getString(1),								// POI.title
 					new PoiAddress.Builder(
 						c.getString(4)							// ADDR.city
 					)
 					// ZIP code removed
 //					.zipCode(c.getInt(-1))						// ADDR.zipcode
 					.street(c.getString(3))						// ADDR.street_name
 					.longitude(c.getDouble(6))					// ADDR.lon
 					.latitude(c.getDouble(5))					// ADDR.lat
 					.build()
 				).description(c.getString(2))					// POI.description
 				.category(c.getString(7))						// CAT.title
 				.favourite((1==c.getInt(8)))					// POI.favourite
 				.openingHours(c.getString(9))					// POI.openingHours
 				.webPage(c.getString(10))						// POI.web_page
 				.telephone(c.getString(11))						// POI.telephone
 				.idPrivate(c.getInt(0))							//POI.id (private)
 				.imageURL(c.getString(12))						//POI.image_url
 				.idGlobal(c.isNull(13) ? -1:c.getInt(13))
 				.build()
 			);
 		}//while more pois
 		c.close();
 		return pois;
 	}//getPoisFromCursor
 
 	
 	@Override
 	public Trip getTrip(int privateId){
 		ArrayList<Trip> trips = new ArrayList<Trip>();
 
 		String sqlstr = "SELECT " +
 		"TRIP._id," +			//0
 		"TRIP.title," +			//1
 		"TRIP.description," +	//2
 		"POI._id," +			//3
 		"POI.title," +			//4
 		"POI.description," +	//5
 		"ADDR.street_name," +	//6
 // ZIP code removed
 //		"ADDR.zipcode," +		//7
 		"ADDR.city," +			//8
 		"ADDR.lat," +			//9
 		"ADDR.lon," +			//10
 		"CAT.title," +			//11
 		"POI.favourite," +		//12
 		"TP.poi_number," +		//13
 		"Trip.free_trip, " +	//14
 		"Poi.image_url, " +		//15
 		"TP.hour, "+			//16
 		"TP.minute, "+			//17
 		"TRIP.global_id, "+
 		"POI.global_id, "+
 		"POI.telephone "+
 		"FROM poi as POI, address as ADDR, category as CAT, trip as TRIP, trip_poi as TP " +
 		"WHERE POI._id = TP.poi_id AND TRIP._id = TP.trip_id AND POI.address_id = ADDR._id AND POI.category_id = CAT._id AND TRIP._id = ? "+
 		"ORDER BY TRIP._id, TP.poi_number";
 
 		String sqlstrEMPTY = "SELECT " +
 		"TRIP._id," +				//0
 		"TRIP.title," +				//1
 		"Trip.free_trip," +			//2
 		"TRIP.description, " +		//3
 		"TRIP.global_id "+
 		"FROM trip as TRIP " +
 		"WHERE TRIP._id = ? AND TRIP._id NOT IN (" +
 		"SELECT " +
 		"TRIP._id " +
 		"FROM trip as TRIP, poi as POI, trip_poi as TP " +
 		"WHERE POI._id = TP.poi_id AND TRIP._id = TP.trip_id)";
 
 		Cursor c = myDataBase.rawQuery(sqlstr, new String[]{""+privateId});
 
 		if(c.getCount() == 0) { //the trip may be empty
 			c = myDataBase.rawQuery(sqlstrEMPTY, new String[]{""+privateId});
 			if( c.moveToNext() ) {
 				Trip trip = new Trip.Builder(c.getString(1/*"TRIP.title"*/))
 				.idPrivate(c.getInt(0/*"TRIP.id"*/))
 				.description(c.getString(3/*"TRIP.description"*/))
 				.freeTrip(1==c.getInt(2))
 				.idGlobal(c.getInt(4))
 				.build();
 				trips.add(trip);
 			}
 		}else{ // trip is not empty
 			int currentTripId = -1;
 			Trip trip = new Trip.Builder("").build();
 			while(c.moveToNext()){
 				if(c.getInt(0/*"TRIP.id"*/) != currentTripId) { //make new trip
 					debug(0, "Got trip: "+c.getString(1/*"TRIP.title"*/));
 					if(currentTripId != -1)	{ //next trip on the list
 						trips.add(trip);
 					}
 					trip = new Trip.Builder(c.getString(1/*"TRIP.title"*/))
 					.idPrivate(c.getInt(0/*"TRIP.id"*/))
 					.description(c.getString(2/*"TRIP.description"*/))
 					.freeTrip(1==c.getInt(14))
 					.idGlobal(c.getInt(18))
 					.build();
 					currentTripId = trip.getIdPrivate();
 				}
 				Poi poi = new Poi.Builder(c.getString(4/*"POI.title"*/),new PoiAddress.Builder
 						(c.getString(8/*"ADDR.city"*/))
 // ZIP code removed
 //				.zipCode(c.getInt(7/*"ADDR.zipcode"*/))
 				.street(c.getString(6/*"ADDR.street_name"*/))
 				.longitude(c.getDouble(10/*"ADDR.lon"*/)).latitude(c.getDouble(9/*"ADDR.lat"*/))
 				.build()
 				).description(c.getString(5/*"POI.description"*/))
 				.category(c.getString(11/*"CAT.title"*/))
 				.favourite((1==c.getInt(12/*"POI.favourite"*/)))
 				.imageURL(c.getString(15))
 				.telephone(Integer.toString(c.getInt(19))) // RS-111208, Changed 20->19
 				.idPrivate(c.getInt(3))
 				.idGlobal(c.getInt(19))
 				.build();
 				trip.addPoi(poi);
 				trip.setTime(poi, new Time(c.getInt(16), c.getInt(17)));
 			}//while more trips
 			if(currentTripId != -1)//next trip on the list
 			{
 				trips.add(trip);
 			}
 		}//if empty trip - else trip with pois
 		c.close();
 		if(trips.size() == 0) return null;
 		return trips.get(0);
 	}//getTrip
 
 	/***
 	 * Get trips (without POIs).
 	 * @param mode TYPE_ALL, TYPE_FREE, or TYPE_FIXED
 	 * @return ArrayList of selected Trips
 	 */
 	@Override
 	public ArrayList<Trip> getTripsWithoutPOIs( int type ){
 		ArrayList<Trip> trips = new ArrayList<Trip>();
 
 		String[] columns = {
 			"TRIP._id",	"TRIP.title", "TRIP.free_trip", "TRIP.description", "TRIP.global_id"
 		};
 		final Map<String,Integer> key = //new HashMap<String,Integer>();		getKeys( key, columns );
 				getKeys( columns );
 		String sqlStr, selectStr, fromStr, whereStr, freeStr;
 
 		selectStr = getSelectStr( columns );
 		fromStr = " FROM trip as TRIP ";
 		whereStr = " WHERE TRIP._id NOT IN (" +
 				"SELECT " +
 				"TRIP._id " +
 				"FROM trip as TRIP, poi as POI, trip_poi as TP " +
 				"WHERE POI._id = TP.poi_id AND TRIP._id = TP.trip_id)";
 		freeStr = " AND TRIP.free_trip = ? ";
 		
 		Cursor c;
 		if ( type==CityExplorer.TYPE_ALL ){
 			sqlStr = selectStr + fromStr;
 			c = myDataBase.rawQuery( sqlStr, null );
 		}else{ //Filter for free or fixed
 			sqlStr = selectStr + fromStr + whereStr + freeStr;
 			c = myDataBase.rawQuery(sqlStr, new String[]{"" + (type==CityExplorer.TYPE_FREE? 1 : 0)}); // Fill the "?" in the select with 1 or 0
 		}//if ALL
 		sqlStr = selectStr + fromStr + whereStr;
 		debug (2, "sql string is "+sqlStr );
 
 		int currentTripId = -1;
 		Trip trip = new Trip.Builder("").build();
 		while( c.moveToNext() ){
 			if(c.getInt( key.get("TRIP._id") ) != currentTripId) { //make new trip
 				debug(2, "Got trip: "+c.getString( key.get("TRIP.title") ) );
 				if(currentTripId != -1) { //next trip on the list
 					trips.add(trip);
 				}
 				trip = new Trip.Builder(c.getString( key.get("TRIP.title") ))
 				.idPrivate(c.getInt( key.get("TRIP._id") ))
 				.description(c.getString( key.get("TRIP.description") ))
 				.freeTrip(1==c.getInt( key.get("TRIP.free_trip") ))
 				.idGlobal(c.getInt( key.get("TRIP.global_id") ))
 				.build();
 				debug(2, "Trip is "+trip );
 
 				currentTripId = trip.getIdPrivate();
 			} // while same trip
 		}// while more empty trips
 		if(currentTripId != -1) { //next trip on the list
 			trips.add(trip);
 		}
 		c.close();
 		debug(2, "FOUND size="+trips.size()+" empty trips" );
 		return trips;
 	}//getTrips
 
 	///////////////////////////////////
 	
 	public static final Map<String,Integer>
 	 getKeys( String[] columns ){
 		Map<String, Integer> key = new HashMap<String, Integer>();
 		for(int i=0; i<columns.length; i++){
 			key.put(columns[i], i);
 		} // for each column, store key index
 		return key;
 	} // getKeys
 	
 	public static final String
 	 getSelectStr( String[] columns ){
 		String selectStr =  "SELECT ";
 		String prefix="";
 		for(int i=0; i<columns.length; i++){
 			selectStr += prefix + columns[i];
 			prefix=",";
 		} // for each column, store key index
 		debug(2, "sqlstr is "+selectStr);
 		return selectStr;
 	} // getKeys
 
 	///////////////////////////////////
 
 	@Override
 	public ArrayList<String>
 	 getUniqueCategoryNames() {
 
 		ArrayList<String> categories = new ArrayList<String>();
 		Cursor c = myDataBase.rawQuery("SELECT DISTINCT category.title from category, poi WHERE poi.category_id = category._id ORDER BY category.title", null);
 		while (c.moveToNext()) {
 			categories.add(c.getString(0));
 		}
 		c.close();
 		return categories;
 	}//getUniqueCategoryNames
 
 	/***
 	 * Reading category names from the DB,
 	 * and images from the Assets folder.
 	 */
 	@Override
 	public HashMap<String, Bitmap>
 	 getUniqueCategoryNamesAndIcons() {
 		HashMap<String, Bitmap> categories = new HashMap<String, Bitmap>();
 		//Cursor c = myDataBase.rawQuery("SELECT DISTINCT category.title, category.icon FROM category, poi WHERE poi.category_id = category._id ORDER BY category.title", null);
 		Cursor c = myDataBase.rawQuery("SELECT DISTINCT category.title, category._id FROM category, poi WHERE poi.category_id = category._id ORDER BY category.title", null);
 		while (c.moveToNext()) {
 			Bitmap bmp = null;//BitmapFactory.decodeResource(myContext.getResources(), R.drawable.new_location);
 			String filename = "icons/"+c.getInt(1)+"_"+c.getString(0)+".bmp";
 			filename = filename.replace(' ', '_');
 			debug(2, "Getting bmp for category: "+filename );
 			try{
 				InputStream isAssetPNG	= myContext.getAssets().open(filename);
 				bmp = BitmapFactory.decodeStream( isAssetPNG);
 			}catch (IOException e){
 				debug(0, "No bmp in assets, for category "+filename+". Error: "+e);
 				filename = filename.replace(".bmp", ".png");
 				try{
 					InputStream isAssetPNG	= myContext.getAssets().open(filename);
 					bmp = BitmapFactory.decodeStream( isAssetPNG);
 				}catch (IOException e2){
 					debug(0, "No png in assets, for category "+filename+". Error: "+e2);
 					categories.put(c.getString(0), bmp);
 				}
 				categories.put(c.getString(0), bmp);
 			}
 			categories.put(c.getString(0), bmp);
 		}//while more categories
 		debug(1, "Got bmp for "+categories.size()+" categories" );
 		c.close();
 		return categories;
 	}//getUniqueCategoryNamesAndIcons
 	
 	/* RS-111208
 	 * Old code using blobs, Should be re-used for maintainability
 	 *  (tight connection in DB is much BETTER than loose connection to filename!)
 	 * byte[] imgData = c.getBlob(1);
 	Bitmap bmp = defaultBmp;
 	if (imgData.length>20){
 		debug(0, "imgData.length is "+imgData.length);
 		bmp = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
 	}else{
 		bmp = defaultBmp;
 	}
 	*/
 
 	@Override
 	public int getCategoryId(String title) {
 		Cursor c = myDataBase.query("category", new String[]{"_id"}, "title = ?", new String[]{title}, null, null, null);
 		while (c.moveToNext()) {
 			int catID = c.getInt(0);
 			c.close();
 			return catID;
 		}
 		c.close();
 		return -1;
 	}//getCategoryId
 
 	@Override
 	public int newPoi(Poi poi) {
 		//Category:
 		int categoryId = -1;
 		if(getCategoryNames().contains(poi.getCategory()))//category exists
 			categoryId = getCategoryId(poi.getCategory());
 		else //does not exist, create it:
 		{
 			ContentValues values1 = new ContentValues();
 			values1.put("title", poi.getCategory());
 			myDataBase.insertOrThrow("category", null, values1);
 
 			categoryId = getCategoryId(poi.getCategory());
 		}
 
 		int AddressId = -1;
 		//does the address exist?
 		Cursor c = myDataBase.query("address", new String[]{"_id"},
 				"street_name = ? AND " +
 // ZIP code removed
 //				"zipcode = ? AND " +
 				"city = ? AND " +
 				"lat = ? AND " +
 				"lon = ?",
 // ZIP code removed
 				new String[]{poi.getAddress().getStreet()/*,""+poi.getAddress().getZipCode()*/,poi.getAddress().getCity(),""+poi.getAddress().getLatitude(),""+poi.getAddress().getLongitude()},
 				null, null, null);
 		if(c.moveToFirst())
 			AddressId = c.getInt(0);
 		else
 		{//add the address
 			ContentValues values2 = new ContentValues();
 			values2.put("street_name", poi.getAddress().getStreet());
 // ZIP code removed
 //			values2.put("zipcode", poi.getAddress().getZipCode());
 			values2.put("city", poi.getAddress().getCity());
 			values2.put("lat", poi.getAddress().getLatitude());
 			values2.put("lon", poi.getAddress().getLongitude());
 			myDataBase.insertOrThrow("address", null, values2);
 
 			//get the id
 			c = myDataBase.query("address", new String[]{"_id"},
 					"street_name = ? AND " +
 // ZIP code removed
 //					"zipcode = ? AND " +
 					"city = ? AND " +
 					"lat = ? AND " +
 					"lon = ?",
 // ZIP code removed
 					new String[]{poi.getAddress().getStreet()/*,""+poi.getAddress().getZipCode()*/,poi.getAddress().getCity(),""+poi.getAddress().getLatitude(),""+poi.getAddress().getLongitude()},
 					null, null, null);
 			if(c.moveToFirst())
 				AddressId = c.getInt(0);
 		}
 
 		if(AddressId == -1)
 			System.out.println("Error getting the address_id");
 
 		//POI:
 		ContentValues values3= new ContentValues();
 		if(poi.getIdGlobal() != -1) values3.put("global_id", poi.getIdGlobal());
 		values3.put("title", poi.getLabel());
 		values3.put("description", poi.getDescription());
 		values3.put("category_id", categoryId);
 		values3.put("favourite", poi.isFavourite());
 		values3.put("address_id", AddressId);
 		values3.put("web_page", poi.getWebPage());
 		values3.put("openingHours", poi.getOpeningHours());
 		values3.put("telephone",poi.getTelephone());
 		values3.put("image_url", poi.getImageURL());
 
 		try {
 			myDataBase.insertOrThrow("poi", null, values3);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 
 		c.close();
 		return 1;
 	}//newPoi
 
 	@Override
 	public void newTrip(Trip t) {
 		//TRIP:
 		ContentValues values = new ContentValues();
 		if(t.getIdGlobal() != -1) values.put("global_id", t.getIdGlobal());
 		values.put("title", t.getLabel());
 		values.put("description", t.getDescription());
 		values.put("free_trip", t.isFreeTrip());
 
 		try {
 			myDataBase.insertOrThrow("trip", null, values);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get the poi's private id from global id.
 	 *
 	 * @param global_id The global id
 	 * @return The private id for the poi with the given global id.
 	 * Returns -1 if the poi is not in the database.
 	 */
 	@Override
 	public int getPoiPrivateIdFromGlobalId(int global_id)
 	{
 		Cursor c = myDataBase.query("poi", new String[]{"_id"},
 				"global_id = ?", new String[]{""+global_id}, null, null, null);
 
 		debug(0, "ids found: "+c.getCount());
 		while (c.moveToNext()) {
 			int private_id = c.getInt(0);
 
 			c.close();
 			return private_id;
 		}
 		c.close();
 		return -1;
 	}//getPoiPrivateIdFromGlobalId
 
 	/**
 	 * Get the trip's private id from global id.
 	 *
 	 * @param global_id The global id
 	 * @return The private id for the trip with the given global id.
 	 * Returns -1 if the trip is not in the database.
 	 */
 	@Override
 	public int getTripPrivateIdFromGlobalId(int global_id){
 		Cursor c = myDataBase.query("trip", new String[]{"_id"},
 				"global_id = ?", new String[]{""+global_id}, null, null, null);
 
 		debug(0, "ids found: "+c.getCount());
 		while (c.moveToNext()) {
 			int private_id = c.getInt(0);
 
 			c.close();
 			return private_id;
 		}
 		c.close();
 		return -1;
 	}//getTripPrivateIdFromGlobalId
 
 	/**
 	 * Method for editing an already existing PoI.
 	 *
 	 * @param poi The modified poi, that is going to be saved in the database
 	 */
 	@Override
 	public void editPoi(Poi poi){
 		ContentValues poiValues = new ContentValues();
 
 		int addressId = -1;
 		Cursor c = myDataBase.query("address", new String[]{"_id"},
 				"street_name = ? AND " +
 // ZIP code removed
 //				"zipcode = ? AND " +
 				"city = ? AND " +
 				"lat = ? AND " +
 				"lon = ?",
 // ZIP code removed
 				new String[]{poi.getAddress().getStreet()/*,""+poi.getAddress().getZipCode()*/,poi.getAddress().getCity(),""+poi.getAddress().getLatitude(),""+poi.getAddress().getLongitude()},
 				null, null, null);
 		if(c.moveToFirst()){
 			addressId = c.getInt(0);
 		}
 		else{//add the address
 			ContentValues addrValues = new ContentValues();
 			addrValues.put("street_name", poi.getAddress().getStreet());
 // ZIP code removed
 //			addrValues.put("zipcode", poi.getAddress().getZipCode());
 			addrValues.put("city", poi.getAddress().getCity());
 			addrValues.put("lat", poi.getAddress().getLatitude());
 			addrValues.put("lon", poi.getAddress().getLongitude());
 			myDataBase.insertOrThrow("address", null, addrValues);
 
 			//get the id
 			c = myDataBase.query("address", new String[]{"_id"},
 					"street_name = ? AND " +
 // ZIP code removed
 //					"zipcode = ? AND " +
 					"city = ? AND " +
 					"lat = ? AND " +
 					"lon = ?",
 // ZIP code removed
 					new String[]{poi.getAddress().getStreet()/*,""+poi.getAddress().getZipCode()*/,poi.getAddress().getCity(),""+poi.getAddress().getLatitude(),""+poi.getAddress().getLongitude()},
 					null, null, null);
 			if(c.moveToFirst()){
 				addressId = c.getInt(0);
 			}
 		}
 
 		//Category
 		int categoryId = -1;
 		if(getCategoryNames().contains(poi.getCategory()))//category exists
 			categoryId = getCategoryId(poi.getCategory());
 		else //does not exist, create it:
 		{
 			ContentValues catValues = new ContentValues();
 			catValues.put("title", poi.getCategory());
 			myDataBase.insertOrThrow("category", null, catValues);
 
 			categoryId = getCategoryId(poi.getCategory());
 		}
 
 		if(categoryId == -1){
 			System.out.println("Error getting the category_id");
 		}
 
 		int poiID = poi.getIdPrivate();
 		if(poi.getIdGlobal() != -1) poiValues.put("global_id", poi.getIdGlobal());
 		poiValues.put("favourite", poi.isFavourite());
 		poiValues.put("description", poi.getDescription());
 		poiValues.put("title", poi.getLabel());
 		poiValues.put("telephone", poi.getTelephone());
 		poiValues.put("openingHours", poi.getOpeningHours());
 		poiValues.put("web_page", poi.getWebPage());
 		poiValues.put("image_url", poi.getImageURL());
 
 		myDataBase.update("poi", poiValues, "_id = ?", new String[]{""+poiID} );
 		//		myDataBase.replaceOrThrow("poi", null, dbValues);
 
 		poiValues.put("address_id", addressId);
 		poiValues.put("category_id", categoryId);
 
 		c.close();
 	}//editPoi
 
 	
 
 	@Override
 	public void onCreate(SQLiteDatabase db) { }
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
 
 	@Override
 	public boolean isOpen(){
 		if (myDataBase == null) {
 			return false;
 		}
 		return myDataBase.isOpen();
 	}//isOpen
 
 
 	/***
 	 * Open the Database
 	 * This is quite time-consuming, and should be done in a background process,
 	 * so the map can show immediately!
 	 */
 	@Override
 	public boolean open(){
 		try{
 			myDataBase = openDataBase();
 		}catch (SQLException e){
 			debug(0, "SQLiteConnector~500: FAILED Opening SQLite connector to "+ DB_PATH);
 			myDataBase=null;
 		}
 		long poiCount = 0;
 		try{
 			poiCount = DatabaseUtils.queryNumEntries(myDataBase, POI_TABLE);
 		}catch (SQLiteException e){ //No such table: poi (if just create blank DB)
 		}
 		debug(0, "poi-count is "+poiCount );
 		//JF: ZIP code removed
 		if ( poiCount ==0 ){ //No existing POIs, close DB, copy default DB-file, and reopen
 			debug(0, "close myDataBase, before re-open");
 			myDataBase.close();
 			try{
 				debug(0, DB_NAME+" was missing... now copying");
 				createDataBase();
 			}catch (IOException e){
 				e.printStackTrace();
 				return false;
 			}
 		}// if empty database, copy from assets
 		return (myDataBase == null) ? false : true;
 	}//open
 
 
 	/**
 	 * Opens the database.
 	 *
 	 * @throws SQLException if the database cannot be opened.
 	 * @return The SQLite database that has been opened.
 	 */
 	public SQLiteDatabase openDataBase() throws SQLException {
 		// (Re-) create DB folder in case it has been removed by the system, or for first time runs
 		File catalog = new File (DB_PATH);
 		if ( !catalog.isDirectory() ){
 			debug(0, "Making Catalog is "+catalog);
 			catalog.mkdir();
 			debug(0, "made folder for: "+myPath);
 		}//if folder missing
 
 		debug(0, "opening db: "+myPath);
 		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE+SQLiteDatabase.CREATE_IF_NECESSARY);
 
 		return myDataBase;
 	}//openDataBase
 
 	@Override
 	public void setContext(Context context) {
 		this.myContext = context;
 	} // setContext
 
 	
 }//class SQLiteConnecto
 
 
 ///////////////////////////////////////////////////////////////////////////////
 
 
 class ValueComparator2 implements Comparator<String> {
 	  Map<String,Integer> base;
 	  public ValueComparator2(Map<String,Integer> base) {
 	      this.base = base;
 	  }
 	  public int compare(String a, String b) {
 	    if(base.get(a) < base.get(b)) {
 	      return -1;
 	    } else if(base.get(a) == base.get(b)) {
 	      return 0;
 	    } else {
 	      return 1;
 	    }
 	  }//compare
 }//ValueComparator Class
 
