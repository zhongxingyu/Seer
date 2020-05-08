 package com.barview.constants;
 
 public class BarviewConstants {
 	public static final String LOCALHOST			= "10.0.2.2";
 	
 	public static final String FACEBOOK_APP_ID_DEV	= "177771455596726";
 	public static final String FACEBOOK_APP_ID		= "177771455596726";
 	
 	public static final String DEV_MODE				= "DEV";
 	public static final String PROD_MODE			= "PROD";
	public static final String RUN_MODE				= DEV_MODE;
 	
 	public static final String FAVORITES_URL_DEV = "http://" + LOCALHOST + ":8888/barview/index.php?/rest/favorites";
 	public static final String FAVORITE_URL_DEV = "http://" + LOCALHOST + ":8888/barview/index.php?/rest/favorite";		// For POST and DELETE
 	public static final String BARIMAGES_URL_DEV = "http://" + LOCALHOST + ":8888/barview/index.php?/rest/barimage";
 	public static final String NEARBYBARS_URL_DEV = "http://" + LOCALHOST + ":8888/barview/index.php?/rest/nearbybars";
 	
 	public static final String FAVORITES_URL = "http://bar-view.com/index.php?/rest/favorites";
 	public static final String FAVORITE_URL = "http://bar-view.com/index.php?/rest/favorite";		// For POST and DELETE
	public static final String BARIMAGES_URL = "http://bar-view.com/barview/index.php?/rest/barimage";
	public static final String NEARBYBARS_URL = "http://bar-view.com/barview/index.php?/rest/nearbybars";
 	
 	// Favorite constants for the XML
 	public static final String FAVORITE_AGGREGATE	= "favorite";
 	public static final String FAVORITE_BAR_ID		= "bar_id";
 	public static final String FAVORITE_ADDRESS		= "address";
 	public static final String FAVORITE_NAME		= "name";
 	
 	// BarImage constants for the XML
 	public static final String BARIMAGE_AGGREGATE	= "barimage";
 	public static final String BARIMAGE_BAR_ID		= "bar_id";
 	public static final String BARIMAGE_IMAGE		= "image";
 	
 	// Nearby bar constants for the XML
 	public static final String NEARBY_BAR_AGGREGATE	= "bar";
 	public static final String NEARBY_BAR_ID		= "bar_id";
 	public static final String NEARBY_BAR_NAME		= "name";
 	public static final String NEARBY_BAR_ADDRESS	= "address";
 	public static final String NEARBY_BAR_LAT		= "lat";
 	public static final String NEARBY_BAR_LNG		= "lng";
 	
 	// REST stuff
 	public static final String REST_USER_ID			= "User_id";
 	
 	// Extras parameters
 	public static final String BAR_ID				= "bar_id";
 	public static final String BAR_NAME				= "bar_name";
 	
 	public static final int GEOPOINT_MULT			= 1000000;
 	
 	public static final int BARIMAGE_REFRESH_TIME	= 5000;		// Time in milliseconds
 }
