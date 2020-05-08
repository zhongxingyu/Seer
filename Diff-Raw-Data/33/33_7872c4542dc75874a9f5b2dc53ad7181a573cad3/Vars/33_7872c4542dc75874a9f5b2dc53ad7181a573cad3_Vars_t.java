 package com.shoutbreak;
 
 public class Vars {
 
 	public static final String SERVER_ADDRESS = "http://app.shoutbreak.co";
	public static final long IDLE_THREAD_LOOP_INTERVAL = 60000; //20000; // milliseconds
 	
 	public static final int GPS_MIN_UPDATE_MILLISECS = 10000;
 	public static final int GPS_MIN_UPDATE_METERS = 100;
 	
 	public static final int DENSITY_GRID_X_GRANULARITY = 129600; // 10 second cells
 	public static final int DENSITY_GRID_Y_GRANULARITY = 64800; // 10 second cells
 	public static final long DENSITY_EXPIRATION = (long) 4.32E8; // 5 days
 	
 	public static final int MIN_TARGETS_FOR_HIT_COUNT = 3;
 	public static final double SHOUT_SCORING_Z_SCORE = 1.644853;
 		
 	public static final int DEFAULT_ZOOM_LEVEL = 15;
 	public static final int RESIZE_ICON_TOUCH_TOLERANCE = 100; // +/- 50 px from center
 	public static final int TOUCH_TOLERANCE = 4;
 	public static final int MIN_RADIUS_PX = 30; // user can't resize below this
 	public static final int MIN_RADIUS_METERS = 100; // initializes to this size when no density
 	public static final int DEGREE_LAT_IN_METERS = 111133; // 60 nautical miles - avg'd from http://en.wikipedia.org/wiki/Latitude#Degree_length
 
 	public static final int SHOUT_STATE_READ = 0;
 	public static final int SHOUT_STATE_NEW = 1;
 	
 	public static final int SHOUT_VOTE_UP = 1;
 	public static final int SHOUT_VOTE_DOWN = -1;	
 	
 	public static final int APP_NOTIFICATION_ID = 0;
 	
 	// JSON FALLBACKS 
 	// what we assume if value not returned by server	
 	public static final int NULL_APPROVAL = -1;
 	public static final int NULL_DOWNS = 0;
 	public static final int NULL_HIT = 0;
 	public static final boolean NULL_OPEN = false;
 	public static final int NULL_PTS = 0;
 	public static final int NULL_SCORE = 0;
 	public static final int NULL_UPS = 0;
 	public static final int NULL_VOTE = 0;
 	
 	// PREFERENCES ////////////////////////////////////////////////////////////
 	
 	public static final String PREFS_NAMESPACE = "shoutbreak";
 	public static final String PREF_APP_ON_OFF_STATUS = "pref_app_on_off_status";
 	
 	// DATABASE  //////////////////////////////////////////////////////////////
 	
 	public static final String DB_NAME = "sbdb";
 	public static final String DB_TABLE_USER_SETTINGS = "USER_SETTINGS";
 	public static final String DB_TABLE_DENSITY = "DENSITY";
 	public static final String DB_TABLE_SHOUTS = "SHOUTS";
 	public static final int DB_VERSION = 5;
 	
 	public static final String KEY_USER_PW = "user_pw";
 	public static final String KEY_USER_ID = "user_id";
 	
 	// MESSAGES ///////////////////////////////////////////////////////////////
 	
 	// from HttpConnectionThreads
 	public static final int MESSAGE_HTTP_DID_START = 0;
 	public static final int MESSAGE_HTTP_DID_ERROR = 1;
 	public static final int MESSAGE_HTTP_DID_SUCCEED = 2;
 	
 	// from UIThread & ServiceThread
 	public static final int MESSAGE_IDLE_EXIT = 3;
 	public static final int MESSAGE_REPOST_IDLE_DELAYED = 14;
 	public static final int MESSAGE_STATE_DELETE_SHOUT = 15;
 	public static final int MESSAGE_STATE_INIT = 4;
 	public static final int MESSAGE_STATE_IDLE = 5;
 	public static final int MESSAGE_STATE_NEW_USER = 6;
 	public static final int MESSAGE_STATE_NEW_USER_2 = 7;
 	public static final int MESSAGE_STATE_EXPIRED_AUTH = 8;
 	public static final int MESSAGE_STATE_SHOUT = 9;
 	public static final int MESSAGE_STATE_RECEIVE_SHOUTS = 10;
 	public static final int MESSAGE_STATE_INVALID_UID = 11;
 	public static final int MESSAGE_STATE_LEVEL_CHANGE = 12;
 	public static final int MESSAGE_STATE_UI_RECONNECT = 13;
 	public static final int MESSAGE_STATE_VOTE = 14;
 	
 	// CALLBACK MESSAGES
 	public static final int CALLBACK_SERVICE_EVENT_COMPLETE = 50;
 	
 	// SERVICE EVENT CODES ////////////////////////////////////////////////////
 	
 	public static final int SEC_SHOUT_SENT = 0;
 	public static final int SEC_RECEIVE_SHOUTS = 1;
 	public static final int SEC_VOTE_COMPLETED = 2;
 	
 	// JSON KEYS  /////////////////////////////////////////////////////////////
 	
 	public static final String JSON_CODE = "code";
 	public static final String JSON_CODE_CREATE_ACCOUNT_0 = "create_account_0";
 	public static final String JSON_CODE_CREATE_ACCOUNT_1 = "create_account_1";
 	public static final String JSON_CODE_EXPIRED_AUTH = "expired_auth";
 	public static final String JSON_CODE_INVALID_UID = "invalid_uid";
 	public static final String JSON_CODE_LEVEL_CHANGE = "level_change";
 	public static final String JSON_CODE_PING_OK = "ping_ok";
 	public static final String JSON_CODE_SHOUTS = "shouts";
 	
 	public static final String JSON_ACTION = "a";
 	public static final String JSON_ACTION_CREATE_ACCOUNT = "create_account";
 	public static final String JSON_ACTION_SHOUT = "shout";
 	public static final String JSON_ACTION_USER_PING = "user_ping";
 	public static final String JSON_ACTION_VOTE = "vote";
 
 	public static final String JSON_ANDROID_ID = "android_id";
 	public static final String JSON_AUTH = "auth";
 	public static final String JSON_CARRIER_NAME = "carrier";
 	public static final String JSON_DENSITY = "rho";
 	public static final String JSON_DEVICE_ID = "device_id";
 	public static final String JSON_LAT = "lat";
 	public static final String JSON_LEVEL = "lvl";
 	public static final String JSON_LONG = "long";
 	public static final String JSON_NEXT_LEVEL_AT = "next_lvl_at";
 	public static final String JSON_NONCE = "nonce";
 	public static final String JSON_PHONE_NUM = "phone_num";
 	public static final String JSON_POINTS = "pts";
 	public static final String JSON_PW = "pw";
 	public static final String JSON_SCORES = "scores";
 	public static final String JSON_SHOUTS = "shouts";
 	public static final String JSON_SHOUT_APPROVAL = "approval";
 	public static final String JSON_SHOUT_DOWNS = "downs";
 	public static final String JSON_SHOUT_HIT = "hit";
 	public static final String JSON_SHOUT_ID = "shout_id";
 	public static final String JSON_SHOUT_OPEN = "open";
 	public static final String JSON_SHOUT_POWER = "power";
 	public static final String JSON_SHOUT_RE = "re";
 	public static final String JSON_SHOUT_TEXT = "txt";
 	public static final String JSON_SHOUT_TIMESTAMP = "ts";
 	public static final String JSON_SHOUT_UPS = "ups";
 	public static final String JSON_UID = "uid";
 	public static final String JSON_VOTE = "vote";
 	
 }
