 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.db;
 
 /**
  * @(#)Constants.java 1.16 95/12/14 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  *
  */
 import org.jbundle.model.util.Constant;
 
 /**
  * Constants.
  */
 public interface Constants extends Constant
 {
     public static final boolean DEBUG = true;
 
 // setOpenMode()
     /**
      * Lock on edit, allow deletes and changes.
      */
     public static final int OPEN_NORMAL = 0;
     /**
      * No deletes and changes.
      */
     public static final int OPEN_READ_ONLY = 1;
     /**
      * No deletes and changes, adds only.
      */
     public static final int OPEN_APPEND_ONLY = 2;
     /**
      * Cache recently used records.
      */
     public static final int OPEN_CACHE_RECORDS = 4;
     //-------------------------- Lock Strategies --------------------
     // OPEN_DONT_LOCK - Don't lock on edit (Typically, you would be using SQL transactions)
     // OPEN_LOCK_ON_EDIT - Do lock on edit
     //
     // OPEN_MERGE_ON_LOCK - Similar to transactions (If changed before write, merge then write)
     // OPEN_WAIT_FOR_LOCK - Wait for Unlock
     // OPEN_EXCEPTION_ON_LOCK - Throw an exception if locked
     /**
      * Don't physically lock the record on edit, listen for record changes.
      */
     public static final int OPEN_NO_LOCK_STRATEGY = 0;
     //v Moved down to Constants
     //vpublic static final int OPEN_NO_LOCK_TYPE = 0;    
     /**
      * When you lock, fail if already locked (ie., return false from the edit() method).
      * <br/>Note: If you want to see the user that is locking the record, then also add OPEN_EXCEPTION_ON_LOCK.
      */
     public static final int OPEN_LOCK_ON_EDIT_STRATEGY = 2048;
     /**
      * When you lock, wait until the record is unlocked (fail if hard lock).
      */
     public static final int OPEN_MERGE_ON_LOCK_STRATEGY = OPEN_LOCK_ON_EDIT_STRATEGY | 4096;
     /**
      * When you lock, wait until the record is unlocked (fail if hard lock).
      */
     public static final int OPEN_WAIT_FOR_LOCK_STRATEGY = OPEN_LOCK_ON_EDIT_STRATEGY | 8192;
     /**
      * Don't lock, just always write with lastChangedDate=current, if different, refresh, merge and re-write.
      */
     //v Moved down to Constants
     //v public static final int OPEN_LAST_MOD_LOCK_TYPE = 16384;
     /**
      * Read is normal, except the LastChanged value is not updated (so a subsequent write will error and cause a merge)
      */
     public static final int OPEN_DONT_UPDATE_LAST_READ = 262144;
     /**
      * Special read (from a thin seek). Refresh the server record and return data (This only triggers listeners for changed fields)
      */
     public static final int OPEN_REFRESH_TO_CURRENT = 524288;
     //---------------------------------------------------------------
 
     public static final int EDIT_MASK = 7;      // Only look at 3 lsb
         // Record change types
     public static final int CACHE_UPDATE_TYPE = 1;  // This is a special type only used for thin
     public static final int AFTER_UPDATE_TYPE = CACHE_UPDATE_TYPE + 1;
     public static final int SELECT_TYPE = AFTER_UPDATE_TYPE + 1;
     public static final int AFTER_DELETE_TYPE = SELECT_TYPE + 1;
     public static final int AFTER_ADD_TYPE = AFTER_DELETE_TYPE + 1;
         // Table types
     public static final int USER_DATA = 16;         // Domain User specific data
     public static final int SHARED_DATA = 0;        // Data shared between all users
     //v Moved down to Constants
     //vpublic static final int LOCAL = 0;              // Local user or shared data (typically shared)
     //vpublic static final int REMOTE = 1;             // Remote user data (always user data)
     public static final int TABLE = 2;              // Static Data table (always shared)
     public static final int SCREEN = 3;
     public static final int MEMORY = 4;             // Vector database where same records share one table
     public static final int UNSHAREABLE_MEMORY = 5; // Vector database where all tables are new
     public static final int SYSTEM_DATABASE = 6;    // System database
     public static final int INTERNAL_MAPPED = 7;    // Special internal mapped database (USE | MAPPED for database type)
     public static final int REMOTE_MEMORY = 8;      // Special Remote database is a shared memory table
     public static final int BASE_TABLE_CLASS = 32;  // Base table in a record class
     public static final int MANUAL_QUERY = 64;      // A Query where you can't use any built-in query capability (Can combine with local/remote)
     public static final int SHARED_TABLE = 128;     // This record shares a physical table with other records. If also a BASE_TABLE_CLASS can't add records.
     public static final int MAPPED = 256;           // Memory table mapped to a actual table
     public static final int LOCALIZABLE = 512 | SHARED_DATA;      // Table can have a local version (always shared! ?)
     public static final int HIERARCHICAL = 1024 | SHARED_DATA;    // Table can have a local version where data is accessed from the concrete db then from the overridden db (always shared! ?)
     public static final int SERVER_REWRITES = 2048; // Do refresh and rewrite in server (don't need to check listeners in client)
     public static final int DONT_LOG_TRX = 4096;    // Don't log transactions for this table (ie., History table)
     public static final int TABLE_TYPE_MASK = 15;   // And to get table type
     public static final int TABLE_DATA_TYPE_MASK = 16;    // And to get table data type
     public static final int TABLE_MASK = TABLE_TYPE_MASK | TABLE_DATA_TYPE_MASK;        // And to get table and data type
 
     public static String CLASS_DIR = "classes";
     public static String FILE_ROOT = "";        // "../";       // Prefix for image files, etc.
     public static final String SEEK_CURRENT_RECORD = "*"; // Package prefix
     public static final String PRIMARY_KEY = "ID";
     public static final String TIP = "Tip";     // Suffix on field tips
     public static final int NO_PARAMS_FOUND = -2;
     public static final int ACCESS_DENIED = 101;
     public static final int LOGIN_REQUIRED = 102;
     public static final int AUTHENTICATION_REQUIRED = 103;
     public static final int CREATE_USER_REQUIRED = 104;
 
     public static final int END_OF_FILE = 1;
     public static final int KEY_NOT_FOUND = 2;
     public static final int RECORD_LOCKED = 5;
     public static final int DUPLICATE_KEY = 4;
     public static final int FILE_INCONSISTENCY = 11;
     public static final int INVALID_RECORD = 12;
     public static final int INVALID_KEY = 14;
     public static final int RECORD_CHANGED = 15;
 
     public static final int UNIQUE = 1;
     public static final int NOT_UNIQUE = 0;
     public static final int SECONDARY_KEY = 2;  // Secondary "Unique" key (Not physically unique)
 
     public static final boolean ASCENDING = true;
     public static final boolean DESCENDING = false;
 
     public static final int MAIN_FIELD = 0;   // lower
     public static final int MAIN_KEY_AREA = MAIN_FIELD;
     public static final int MAIN_KEY_FIELD = MAIN_FIELD;
 
     public static final int FIRST_RECORD = Integer.MIN_VALUE;
     public static final int LAST_RECORD = Integer.MAX_VALUE;
     public static final int NEXT_RECORD = +1;
     public static final int PREVIOUS_RECORD = -1;
     /**
      * The TEMP_KEY_AREA are a temporary copy of the fields that belong to a key.
      * @see FILE_KEY_AREA.
      */
     public static final int TEMP_KEY_AREA = 1;
 
     public final static String SUBMIT = "Submit";
     public final static String RESET = "Reset";
     public final static String DELETE = "Delete";
     public final static String BACK = "Back";
     public final static String HELP = "Help";
     public static final String FORM = "Form";
     public static final String GRID = "Grid";   // Menu Record
     public static final String CLOSE = "Close";
 
     public static final String OBJECT_ID = "objectID";  // Handle in persistent store
 
     // Command options
 	public static final int USE_SAME_WINDOW = 0;
 	public static final int USE_NEW_WINDOW = 1 << 29;	// 2^ 29
     public static final int PUSH_TO_BROSWER = 0;
     public static final int DONT_PUSH_TO_BROSWER = 1 << 30;	// 2 ^ 30
     public static final int DONT_PUSH_HISTORY = 1 << 28;	// 2 ^ 28
     /**
      * Login levels.
      */
     public static final int LOGIN_USER = 1;
     public static final int LOGIN_AUTHENTICATED = 2;
 
     public static final int READ_ACCESS = 1;
     public static final int WRITE_ACCESS = 2;
     /**
      * The default servlet.
      */
     public static final String DEFAULT_SERVLET = "app";
    public static final String WEBSTART_SERVLET = "webstart";
     public static final String DATA_SERVLET = "tourdata";	// Data such as jnlp, table, etc.
     public static final String AUTO_SERVLET = "tourauto";	// Auto redirect based on browser
     public static final String DEFAULT_XHTML_SERVLET = "appxhtml";
     /**
      * User database/system prefix.
      */
     public static final String DB_USER_PREFIX = "dbPrefix";
     /**
      * For now the sub-system/demo system is the dbPrefix (I may want to change this later).
      */
     public static final String SUB_SYSTEM_LN_SUFFIX = "dbSuffix";
     // Time to wait for new services to come up (30 seconds in 3 second intervals)
     public static final int WAIT_INTERVALS = 10;
     public static final int WAIT_INTERVAL_TIME_MS = 3000;
 }
