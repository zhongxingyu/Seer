 package net.sparktank.morrigan.library;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sparktank.morrigan.model.media.MediaTrack;
 
 public class SqliteLayer {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private final String dbFilePath;
 
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Public.
 	
 //	TODO: nice, exception-wrapped public methods go here.
 	
 	public SqliteLayer (String dbFilePath) throws DbException {
 		this.dbFilePath = dbFilePath;
 		
 		try {
 			initDatabaseTables();
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 	public void dispose () throws DbException {
 		try {
 			disposeDbCon();
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 	public List<MediaTrack> getAllMedia () throws DbException {
 		try {
 			return local_getAllMedia();
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 	public List<String> getSources () throws DbException {
 		try {
 			return local_getSources();
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 	public void addSource (String source) throws DbException {
 		try {
 			local_addSource(source);
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 	public void removeSource (String source) throws DbException {
 		try {
 			local_removeSource(source);
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 	public void addFile (File file) throws DbException {
 		try {
 			local_addTrack(file);
 		} catch (Exception e) {
 			throw new DbException(e);
 		}
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Schema.
 	
 	private static final String SQL_TBL_MEDIAFILES_EXISTS = 
 		"SELECT name FROM sqlite_master WHERE name='tbl_mediafiles';";
 	
 	private static final String SQL_TBL_MEDIAFILES_CREATE = 
 		"create table tbl_mediafiles(" +
 	    "sfile VARCHAR(10000) not null collate nocase primary key," +
 	    "dadded DATETIME," +
 	    "lstartcnt INT(6)," +
 	    "lendcnt INT(6)," +
 	    "dlastplay DATETIME," +
 	    "lmd5 BIGINT," +
 	    "lduration INT(6)," +
 	    "benabled INT(1)," +
 	    "bmissing INT(1));";
 	
 	private static final String SQL_TBL_SOURCES_EXISTS =
 		"SELECT name FROM sqlite_master WHERE name='tbl_sources';";
 	
 	private static final String SQL_TBL_SOURCES_CREATE = 
 		"CREATE TABLE tbl_sources (" +
 		"path VARCHAR(1000) NOT NULL  collate nocase primary key" +
 		");";
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Sources.
 	
 	private static final String SQL_TBL_SOURCES_Q_ALL =
 		"SELECT path FROM tbl_sources;";
 	
 	private static final String SQL_TBL_SOURCES_ADD =
 		"INSERT INTO tbl_sources (path) VALUES (?)";
 	
 	private static final String SQL_TBL_SOURCES_REMOVE =
 		"DELETE FROM tbl_sources WHERE path=?";
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Library queries.
 	
 	private static final String SQL_TBL_MEDIAFILES_Q_ALL = 
 		"SELECT sfile, dadded, lstartcnt, lendcnt, dlastplay, " +
 	    "lmd5, lduration, benabled, bmissing FROM tbl_mediafiles " +
 	    "ORDER BY sfile COLLATE NOCASE ASC;";
 	
 	private static final String SQL_TBL_MEDIAFILES_Q_EXISTS =
		"SELECT count(*) FROM tbl_mediafiles WHERE sfile=? COLLATE NOCASE;";
 	
 	private static final String SQL_TBL_MEDIAFILES_ADD =
 		"INSERT INTO tbl_mediafiles (sfile,dadded,lstartcnt,lendcnt,lduration,benabled) VALUES " +
 		"(?,date('now'),0,0,0,1);";
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	DB connection.
 	
 	private Connection dbConnection = null;
 	
 	private Connection getDbCon () throws ClassNotFoundException, SQLException {
 		if (dbConnection==null) {
 			Class.forName("org.sqlite.JDBC");
 			String url = "jdbc:sqlite:/" + dbFilePath;
 			dbConnection = DriverManager.getConnection(url); // FIXME is this always safe?
 		}
 		
 		return dbConnection;
 	}
 	
 	private void disposeDbCon () throws SQLException {
 		if (dbConnection!=null) {
 			dbConnection.close();
 		}
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Init.
 	
 	private void initDatabaseTables () throws SQLException, ClassNotFoundException {
 		Statement stat = getDbCon().createStatement();
 		
 		ResultSet rs;
 		
 		rs = stat.executeQuery(SQL_TBL_MEDIAFILES_EXISTS);
 		if (!rs.next()) { // True if there are rows in the result.
 			stat.executeUpdate(SQL_TBL_MEDIAFILES_CREATE);
 		}
 		rs.close();
 		
 		rs = stat.executeQuery(SQL_TBL_SOURCES_EXISTS);
 		if (!rs.next()) { // True if there are rows in the result.
 			stat.executeUpdate(SQL_TBL_SOURCES_CREATE);
 		}
 		rs.close();
 		
 		stat.close();
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Sources.
 	
 	private List<String> local_getSources () throws SQLException, ClassNotFoundException {
 		Statement stat = getDbCon().createStatement();
 		ResultSet rs = stat.executeQuery(SQL_TBL_SOURCES_Q_ALL);
 		
 		List<String> ret = new ArrayList<String>();
 		
 		while (rs.next()) {
 			ret.add(rs.getString("path"));
 		}
 		
 		rs.close();
 		stat.close();
 		
 		return ret;
 	}
 	
 	private void local_addSource (String source) throws SQLException, ClassNotFoundException, DbException {
 		PreparedStatement ps = getDbCon().prepareStatement(SQL_TBL_SOURCES_ADD);
 		ps.setString(1, source);
 		int n = ps.executeUpdate();
 		ps.close();
 		if (n<1) throw new DbException("No update occured.");
 	}
 	
 	private void local_removeSource (String source) throws SQLException, ClassNotFoundException, DbException {
 		PreparedStatement ps = getDbCon().prepareStatement(SQL_TBL_SOURCES_REMOVE);
 		ps.setString(1, source);
 		int n = ps.executeUpdate();
 		ps.close();
 		if (n<1) throw new DbException("No update occured.");
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Media.
 	
 	private List<MediaTrack> local_getAllMedia () throws SQLException, ClassNotFoundException {
 		Statement stat = getDbCon().createStatement();
 		ResultSet rs = stat.executeQuery(SQL_TBL_MEDIAFILES_Q_ALL);
 		
 		List<MediaTrack> ret = new ArrayList<MediaTrack>();
 		
 		while (rs.next()) {
 			MediaTrack mt = new MediaTrack();
 			
 			mt.setfilepath(rs.getString("sfile"));
 			
 			ret.add(mt);
 		}
 		
 		rs.close();
 		stat.close();
 		
 		return ret;
 	}
 	
 	private void local_addTrack (File file) throws SQLException, ClassNotFoundException, DbException {
 		PreparedStatement ps;
 		ResultSet rs;
 		
 		String filePath = file.getAbsolutePath();
 		
 		ps = getDbCon().prepareStatement(SQL_TBL_MEDIAFILES_Q_EXISTS);
 		ps.setString(1, filePath);
 		rs = ps.executeQuery();
 		int n = 0;
 		if (rs.next()) {
			n = rs.getInt(1);
 		}
 		rs.close();
 		ps.close();
 		
 		if (n == 0) {
 			ps = getDbCon().prepareStatement(SQL_TBL_MEDIAFILES_ADD);
 			ps.setString(1, filePath);
 			n = ps.executeUpdate();
 			ps.close();
 			if (n<1) throw new DbException("No update occured.");
 		}
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
