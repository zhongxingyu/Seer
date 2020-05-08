 /**
  * @author sekhri
  */
 
 
 package dbs.data;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 
 import db.DBManagement;
 import dbs.DBSConstants;
 import dbs.util.DBSConfig;
 import dbs.util.DBSUtil;
 import dbs.sql.DBSSql;
 
 /**
 * A class that maintains a cache of hastables in memory. This cache is loaded in the memory at the servlet startup time. The following tables are cached
 * Person (DN)<br>
 * Algorithm Config (family,psethash, version, exe) <br>
 * ProcessedDataset (Path in format /primary/processed) <br>
 * DataTier (TierName) <br>
 * FileStatus (Status) <br>
 * FileValidStatus (Status) <br>
 * FileType (Type) <br>
 *
 * More can be easily added . <br>
 *
 *
 * There is a constant defined in DBSConstant.java file called USECACHE. If set to true it uses the cache before looking in the database. If set to false it bypass the cache and goes to the database directly. <br>
 *
 * The cache is synchronized using read write locks. Many readers can read at the same time but only one write can write at a time. When a new writer comes, it wait for all the readers to finish. Any new readers requesting access will be given low priority than the waiting writer. After all the readers finishes, the writes locks the cache and at that time readers wait to get access. So this way writer does not wait forever and readers can read simultaneously. Notice that for each table described above a separate read write locks are used. This way if any thread is writing to algoconfig, it has no affect on other tables (Person, FileTyep etc) . They can still be accessed. <br>
 *
 * When an entry is accessed , it is first looked into the local cache of the thread. This cache has the lifetime of the thread itself that is when the API finishes. Very few entries, on the need basis, goes into this local cache. If this local cache have the entry , it is returned <br>
 * If the local cache does not have the entry, then the global cache is queried. <br>
 * If the global cache has the entry then that entry is written into the local cache and returned <br>
 * If the global cache does not have the entry, then it is reloaded from the database <br>
 *
 * This way the most frequently used entries within the API are accessed via the local cache. <br>
 */
 public class DBSDataCache {
 	private ReadWriteLock globalPersonLock = new ReentrantReadWriteLock();
 	private Lock readPersonLock = globalPersonLock.readLock();
 	private Lock writePersonLock = globalPersonLock.writeLock();
 	private static Hashtable persons = new Hashtable();
 
 	private ReadWriteLock globalProcDSLock = new ReentrantReadWriteLock();
 	private Lock readProcDSLock = globalProcDSLock.readLock();
 	private Lock writeProcDSLock = globalProcDSLock.writeLock();
 	private static Hashtable procDSs = new Hashtable();
 
 	private ReadWriteLock globalAlgoLock = new ReentrantReadWriteLock();
 	private Lock readAlgoLock = globalAlgoLock.readLock();
 	private Lock writeAlgoLock = globalAlgoLock.writeLock();
 	private static Hashtable algos = new Hashtable();
 
 	private ReadWriteLock globalTierLock = new ReentrantReadWriteLock();
 	private Lock readTierLock = globalTierLock.readLock();
 	private Lock writeTierLock = globalTierLock.writeLock();
 	private static Hashtable tiers = new Hashtable();
 
 	private ReadWriteLock globalFileStatusLock = new ReentrantReadWriteLock();
 	private Lock readFileStatusLock = globalFileStatusLock.readLock();
 	private Lock writeFileStatusLock = globalFileStatusLock.writeLock();
 	private static Hashtable fileStatus = new Hashtable();
 
 	private ReadWriteLock globalFileTypeLock = new ReentrantReadWriteLock();
 	private Lock readFileTypeLock = globalFileTypeLock.readLock();
 	private Lock writeFileTypeLock = globalFileTypeLock.writeLock();
 	private static Hashtable fileTypes = new Hashtable();
 
 	private ReadWriteLock globalFileValStatusLock = new ReentrantReadWriteLock();
 	private Lock readFileValStatusLock = globalFileValStatusLock.readLock();
 	private Lock writeFileValStatusLock = globalFileValStatusLock.writeLock();
 	private static Hashtable fileValStatus = new Hashtable();
 
 	
 	//private static ReadWriteLock globalInstanceLock = new ReentrantReadWriteLock();
 	//private static Lock readInstanceLock = globalInstanceLock.readLock();
 	//private static Lock writeInstanceLock = globalInstanceLock.writeLock();
 
 	
 	private static DBSDataCache instance = null;
 	private static Boolean mutex = new Boolean(true);
 	/*private static Boolean mutexPerson = new Boolean(true);
 	private static Boolean mutexProcDS = new Boolean(true);
 	private static Boolean mutexAlgo = new Boolean(true);
 	private static Boolean mutexTier = new Boolean(true);*/
 	
 	/*public static DBSDataCache getDBSDataCacheInstance(Connection conn) throws Exception {
 		readInstanceLock.lock(); 
 		try {
 			if( instance != null) return instance;
 		} finally { 
 			readInstanceLock.unlock();
 		}
 
 		writeInstanceLock.lock();
 		try {
 			instance = new DBSDataCache(conn);
 		} finally { 
 			writeInstanceLock.unlock();
 		}
 
 		return instance;
 	}*/
 
 	/**
 	 * Since this cache is a static singleton there is no public constructor and the only way one can make a abject of this cache is via this method
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable .
 	 */
 	public static DBSDataCache getDBSDataCacheInstance(Connection conn) throws Exception {
 		if( instance != null) return instance;
 		synchronized(mutex) {
 			if( instance != null ) return instance;
 			instance = new DBSDataCache(conn);
 		}
 		return instance;
 	}
 	
 
 	private DBSDataCache(Connection conn) throws Exception {
 		resetCache(conn);
 	}
 
 	public void resetCache(Connection conn) throws Exception {
 		if (DBSConstants.USECACHE) {
 			System.out.print("\nLoading Person information in cache ...... ");
 			refreshPersons(conn);
 			System.out.println("Done");
 			System.out.print("\nLoading Processed DS information in cache ...... ");
 			refreshProcDSs(conn);
 			System.out.println("Done");
			//System.out.print("\nLoading Algorithm information in cache ...... ");
			//refreshAlgos(conn);
			//System.out.println("Done");
 			System.out.print("\nLoading Tier information in cache ...... ");
 			refreshTiers(conn);
 			System.out.println("Done");
 			System.out.print("\nLoading File Status information in cache ...... ");
 			refreshFileStatus(conn);
 			System.out.println("Done");
 			System.out.print("\nLoading File Type information in cache ...... ");
 			refreshFileTypes(conn);
 			System.out.println("Done");
 			System.out.print("\nLoading File ValStatus information in cache ...... ");
 			refreshFileValStatus(conn);
 			System.out.println("Done\n");
 		}
 
 	}
 
 
 	//----------------------------------------------------------------------------------------------------------------------------
 	// Person cache
 	private String readUserIDSycnronized(Connection conn, String userDN) throws Exception {
 		String id = "";
 		readPersonLock.lock(); 
 		try { 
 			id = get(persons, userDN); 
 		} finally { 
 			readPersonLock.unlock();
 		}
 		return id;
 	}
 	
 	/**
 	 * Gets a id of a user from the Cache in a sycnronized way. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param userDN The user DN in  <code>java.lang.String</code> format.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the supplied parameters are invalid or  the database connection is unavailable.
 	 */
 	public String getUserID(Connection conn, String userDN) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String id = readUserIDSycnronized(conn, userDN);
 		if (isNull(id)) { 
 			refreshPersons(conn);
 			writeCacheLog("CACHE-MIS getUserID " + userDN + " .. Reloading ..");
 			return readUserIDSycnronized(conn, userDN);
 		} else 	writeCacheLog("CACHE-HIT getUserID " + userDN);
 		return id;
 	}
 
 	private void refreshPersons(Connection conn) throws Exception {
 		writePersonLock.lock();
 		try {
 			persons = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listPersons(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) persons.put(get(rs, "DN") ,  get(rs, "ID"));
 				//writeCacheLog("DN " + get(rs, "DN") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writePersonLock.unlock();
 		}
 	}
 	
 
 	//----------------------------------------------------------------------------------------------------------------------------
 	// Processed dataset cache
 
 	private String readProcessedDSIDSycnronized(Connection conn, String pathToQuery) throws Exception {
 		String id = "";
 		readProcDSLock.lock();
 		try { 
 			id =  get(procDSs, pathToQuery); 
 		} finally { 
 			readProcDSLock.unlock();
 		}
 		return id;
 	}
 
 	/**
 	 * Gets a processed data set ID from the Cache in a sycnronized way. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param path a dataset path in the format of /primary/tier/processed. 
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable.
 	 */
 	public String getProcessedDSID(Connection conn, String path) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String[] data = path.split("/");
 		String pathToQuery = "/" + data[1] + "/" + data[2];
 		String id = readProcessedDSIDSycnronized(conn, pathToQuery);
 		if (isNull(id)) { 
 			writeCacheLog("CACHE-MIS getProcessedDSID " + pathToQuery + " .. Reloading ..");
 			refreshProcDSs(conn);
 			return readProcessedDSIDSycnronized(conn, pathToQuery);
 		} else 	writeCacheLog("CACHE-HIT getProcessedDSID " + pathToQuery);
 		return id;
 
 	}
 
 	private void refreshProcDSs(Connection conn) throws Exception {
 		writeProcDSLock.lock();
 		try {
 			procDSs = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listProcessedDatasets(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) procDSs.put("/" + get(rs, "PRIMARY_DATATSET_NAME") + "/" +  get(rs, "PROCESSED_DATATSET_NAME") ,  get(rs, "ID"));
 				//writeCacheLog("Path /" + get(rs, "PRIMARY_DATATSET_NAME") + "/" +  get(rs, "PROCESSED_DATATSET_NAME") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writeProcDSLock.unlock();
 		}
 	}
 
 	
 	//----------------------------------------------------------------------------------------------------------------------------
 	// Algorithm cache
 	
 	private String readAlgorithmIDSycnronized(Connection conn, String algoToQuery) throws Exception {
 		String id = "";
 		readAlgoLock.lock(); 
 		try { 
 			id = get(algos, algoToQuery); 
 		} finally { 
 			readAlgoLock.unlock();
 		}
 		return id;
 	}
 	
 
 	/**
 	 * Gets a algorithm id from the from the Cache in a sycnronized way using the application version, application family, application executable and parameter set name. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param ver the name of the application version whose algorithm configuration id needs to be fetched.
 	 * @param fam the name of the application family whose algorithm configuration id needs to be fetched.
 	 * @param exe the name of the application executable whose algorithm configuration id needs to be fetched.
 	 * @param psHash the hash of the parameter set whose algorithm configuration id needs to be fetched.
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable.
 	 */
 	public String getAlgorithmID(Connection conn, String ver, String fam, String exe, String psHash) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String algoToQuery = "/" + ver + "/" + fam + "/" + exe + "/" + psHash;
 		String id = readAlgorithmIDSycnronized(conn, algoToQuery);
 		if (isNull(id)) { 
 			writeCacheLog("CACHE-MIS getAlgorithmID " + algoToQuery + " .. Reloading ..");
 			refreshAlgos(conn);
 			return readAlgorithmIDSycnronized(conn, algoToQuery);
 		} else 	writeCacheLog("CACHE-HIT getAlgorithmID " + algoToQuery);
 		return id;
 
 	}
 
 
 	private void refreshAlgos(Connection conn) throws Exception {
 		writeAlgoLock.lock();
 		try {
 			algos = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listAlgorithms(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) algos.put("/" + get(rs, "APP_VERSION") + "/" +  get(rs, "APP_FAMILY_NAME") + "/" +  get(rs, "APP_EXECUTABLE_NAME") + "/" + get(rs, "PS_HASH") ,  get(rs, "ID"));
 				//writeCacheLog("Algorithm /" +  get(rs, "APP_VERSION") + "/" +  get(rs, "APP_FAMILY_NAME") + "/" +  get(rs, "APP_EXECUTABLE_NAME") + "/" + get(rs, "PS_HASH") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writeAlgoLock.unlock();
 		}
 	}
 
 	//----------------------------------------------------------------------------------------------------------------------------
 	// Tier cache
 	
 	
 	private String readTierIDSycnronized(Connection conn, String name) throws Exception {
 		String id = "";
 		readTierLock.lock();
 		try { 
 			id = get(tiers, name); 
 		} finally { 
 			readTierLock.unlock();
 		}
 		return id;
 
 	}
 	
 	/**
 	 * Gets a data tier set ID from the Cache in a sycnronized way. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param name a data tier name in <code>java.lang.String</code> format. 
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable.
 	 */
 	public String getTierID(Connection conn, String name) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String id = readTierIDSycnronized(conn, name);
 		if (isNull(id)) { 
 			writeCacheLog("CACHE-MIS getTierID " + name + " .. Reloading ..");
 			refreshTiers(conn);
 			return readTierIDSycnronized(conn, name);
 		} else 	writeCacheLog("CACHE-HIT getTierID " + name);
 		return id;
 	}
 
 	private void refreshTiers(Connection conn) throws Exception {
 		writeTierLock.lock();
 		try {
 			tiers = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listTiers(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) tiers.put(get(rs, "NAME") ,  get(rs, "ID"));
 				//writeCacheLog("DN " + get(rs, "DN") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writeTierLock.unlock();
 		}
 	}
 	
 
 	//----------------------------------------------------------------------------------------------------------------------------
 	// File Status cache
 	
 	
 	private String readFileStatusIDSycnronized(Connection conn, String status) throws Exception {
 		String id = "";
 		readFileStatusLock.lock();
 		try { 
 			id = get(fileStatus, status); 
 		} finally { 
 			readFileStatusLock.unlock();
 		}
 		return id;
 
 	}
 	
 
 	/**
 	 * Gets a file status ID from the Cache in a sycnronized way. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param status a file status name in <code>java.lang.String</code> format. 
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable.
 	 */
 	public String getFileStatusID(Connection conn, String status) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String id = readFileStatusIDSycnronized(conn, status);
 		if (isNull(id)) { 
 			writeCacheLog("CACHE-MIS getFileStatusID " + status + " .. Reloading ..");
 			refreshFileStatus(conn);
 			return readFileStatusIDSycnronized(conn, status);
 		} else 	writeCacheLog("CACHE-HIT getFileStatusID " + status);
 		return id;
 	}
 
 	private void refreshFileStatus(Connection conn) throws Exception {
 		writeFileStatusLock.lock();
 		try {
 			fileStatus = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listFileStatus(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) fileStatus.put(get(rs, "STATUS") ,  get(rs, "ID"));
 				//writeCacheLog("DN " + get(rs, "STATUS") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writeFileStatusLock.unlock();
 		}
 	}
 	
 	//----------------------------------------------------------------------------------------------------------------------------
 	// File Type cache
 	
 	
 	private String readFileTypeIDSycnronized(Connection conn, String type) throws Exception {
 		String id = "";
 		readFileTypeLock.lock();
 		try { 
 			id = get(fileTypes, type); 
 		} finally { 
 			readFileTypeLock.unlock();
 		}
 		return id;
 
 	}
 	
 
 	/**
 	 * Gets a file type ID from the Cache in a sycnronized way. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param type a file type name in <code>java.lang.String</code> format. 
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable.
 	 */
 	public String getFileTypeID(Connection conn, String type) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String id = readFileTypeIDSycnronized(conn, type);
 		if (isNull(id)) { 
 			writeCacheLog("CACHE-MIS getFileTypeID " + type + " .. Reloading ..");
 			refreshFileTypes(conn);
 			return readFileTypeIDSycnronized(conn, type);
 		} else 	writeCacheLog("CACHE-HIT getFileTypeID " + type);
 		return id;
 	}
 
 	private void refreshFileTypes(Connection conn) throws Exception {
 		writeFileTypeLock.lock();
 		try {
 			fileTypes = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listFileTypes(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) fileTypes.put(get(rs, "TYPE") ,  get(rs, "ID"));
 				//writeCacheLog("DN " + get(rs, "TYPE") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writeFileTypeLock.unlock();
 		}
 	}
 	
 
 	//----------------------------------------------------------------------------------------------------------------------------
 	// File ValStatus cache
 	
 	
 	private String readFileValStatusIDSycnronized(Connection conn, String status) throws Exception {
 		String id = "";
 		readFileValStatusLock.lock();
 		try { 
 			id = get(fileValStatus, status); 
 		} finally { 
 			readFileValStatusLock.unlock();
 		}
 		return id;
 
 	}
 	
 
 	/**
 	 * Gets a file validation status ID from the Cache in a sycnronized way. If the ID is not found in the cache, then the cache is reloaded from the database.
 	 * @param conn a database connection <code>java.sql.Connection</code> object created externally.
 	 * @param status a file validation status name in <code>java.lang.String</code> format. 
 	 * @throws Exception Various types of exceptions can be thrown. Commonly they are thrown if the database connection is unavailable.
 	 */
 	public String getFileValStatusID(Connection conn, String status) throws Exception {
 		if (!DBSConstants.USECACHE) {writeCacheLog("CACHE-DISABLED"); return "";}
 		String id = readFileValStatusIDSycnronized(conn, status);
 		if (isNull(id)) { 
 			writeCacheLog("CACHE-MIS getFileValStatusID " + status + " .. Reloading ..");
 			refreshFileValStatus(conn);
 			return readFileValStatusIDSycnronized(conn, status);
 		} else 	writeCacheLog("CACHE-HIT getFileValStatusID " + status);
 		return id;
 	}
 
 	private void refreshFileValStatus(Connection conn) throws Exception {
 		writeFileValStatusLock.lock();
 		try {
 			fileValStatus = new Hashtable();
 			PreparedStatement ps = null;
 			ResultSet rs =  null;
 			try {
 				ps = DBSSql.listFileValStatus(conn);
 				rs =  ps.executeQuery();
 				while(rs.next()) fileValStatus.put(get(rs, "STATUS") ,  get(rs, "ID"));
 				//writeCacheLog("DN " + get(rs, "STATUS") + "  ID " + get(rs, "ID"));
 			} finally { 
 				if (rs != null) rs.close();
 				if (ps != null) ps.close();
 			}
 		} finally { 
 			writeFileValStatusLock.unlock();
 		}
 	}
 
 	//----------------------------------------------------------------------------------------------------------------------------
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	
 
 	private String get(ResultSet rs, String key) throws Exception {
 		return DBSUtil.get(rs, key);
 	}
 
 	private String get(Hashtable table, String key) throws Exception {
 		return DBSUtil.get(table, key);
 	}
 		
 	private boolean isNull(String pattern) {
 		return DBSUtil.isNull(pattern);
 	}
 	
 	private void writeCacheLog(String log) {
 		DBSUtil.writeCacheLog(log);
 	}
 		
 
 	public static void main (String args[]) {
 		try {
 			DBSConfig config = DBSConfig.getInstance();
 			Connection conn = DBManagement.getConnection( config.getDbDriver(),
 					config.getDbURL(), 
 					config.getDbUserName(),
 					config.getDbUserPasswd());
 					
 			DBSDataCache data = DBSDataCache.getDBSDataCacheInstance(conn);
 			System.out.println( "id for ANZARDN " + data.getUserID(conn, "ANZARDN"));
 			System.out.println( "again id for ANZARDN " + data.getUserID(conn, "ANZARDN"));
 			System.out.println( "again id for ANZARDN " + data.getUserID(conn, "ANZARDN"));
 			System.out.println( "new id for ANZARDNaaa " + data.getUserID(conn, "ANZARDNaaa"));
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 	}
 }
