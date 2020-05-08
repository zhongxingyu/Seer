 /*  Copyright (C) 2012  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package io;
 import filter.FilterItem;
 import filter.FilterState;
 import io.dao.BlacklistDAO;
 import io.dao.CacheDAO;
 import io.dao.DuplicateDAO;
 import io.dao.FilterDAO;
 import io.dao.IndexDAO;
 import io.dao.LocationDAO;
 import io.tables.BlacklistRecord;
 import io.tables.Cache;
 import io.tables.DirectoryPathRecord;
 import io.tables.DnwRecord;
 import io.tables.DuplicateRecord;
 import io.tables.FilePathRecord;
 import io.tables.FileRecord;
 import io.tables.IndexRecord;
 import io.tables.LocationRecord;
 import io.tables.Settings;
 import io.tables.Thumbnail;
 
 import java.awt.Image;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import com.github.dozedoff.commonj.file.FileInfo;
 import com.github.dozedoff.commonj.file.FileUtil;
 import com.github.dozedoff.commonj.io.ConnectionPool;
 import com.github.dozedoff.commonj.io.DBsettings;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.SelectArg;
 import com.j256.ormlite.support.ConnectionSource;
 /**
  * Class for database communication.
  */
 public class AidDAO{
 	private static final HashMap<String, String> prepStmts = new HashMap<String, String>();
 	protected static Logger logger = Logger.getLogger(AidDAO.class.getName());
 	protected final String RS_CLOSE_ERR = "Could not close ResultSet: ";
 	protected final String SQL_OP_ERR = "MySQL operation failed: ";
 	private final String DEFAULT_LOCATION = "UNKNOWN";
 	protected final ConnectionPool connPool;
 	
 	private CacheDAO cacheDAO;
 	private Dao<Thumbnail, Integer> ThumbnailDAO;
 	private LocationDAO locationDao;
 	private IndexDAO indexDao;
 	private Dao<DirectoryPathRecord, Integer> directoryDAO;
 	private Dao<FilePathRecord, Integer> fileDAO;
 	private DuplicateDAO duplicateDAO;
 	private Dao<DnwRecord, String> dnwDAO;
 	private BlacklistDAO blackListDAO;
 	private FilterDAO filterDAO;
 	private Dao<Settings, String> settingDao;
 	
 
 	public AidDAO(ConnectionPool connPool){
 		this.connPool = connPool;
 		createDaos();
 	}
 	
 	private void createDaos() {
 		try{
 			ConnectionSource cSource = connPool.getConnectionSource();
 			
 			cacheDAO = new CacheDAO(cSource);
 			DaoManager.registerDao(cSource, cacheDAO);
 			ThumbnailDAO = DaoManager.createDao(cSource, Thumbnail.class);
 			indexDao = new IndexDAO(cSource);
 			DaoManager.registerDao(cSource, indexDao);
 			locationDao = new LocationDAO(cSource);
 			DaoManager.registerDao(cSource, locationDao);
 			directoryDAO = DaoManager.createDao(cSource, DirectoryPathRecord.class);
 			fileDAO = DaoManager.createDao(cSource, FilePathRecord.class);
 			duplicateDAO = new DuplicateDAO(cSource);
 			DaoManager.registerDao(cSource, duplicateDAO);
 			dnwDAO = DaoManager.createDao(cSource, DnwRecord.class);
 			blackListDAO = new BlacklistDAO(cSource);
 			DaoManager.registerDao(cSource, blackListDAO);
 			filterDAO = new FilterDAO(cSource);
 			DaoManager.registerDao(cSource, filterDAO);
 			settingDao = DaoManager.createDao(cSource, Settings.class);
 		}catch(SQLException e){
 			logger.severe("Unable to create DAO: " + e.getMessage());
 		}
 	}
 
 	protected Connection getConnection(){
 		try {
 			return connPool.getConnection();
 		} catch (SQLException e) {
 			logger.warning("Failed to get database connection");
 		}
 		
 		return null;
 	}
 
 	protected static void addPrepStmt(String id, String stmt){
 		try {
 			if(prepStmts.containsKey(id))
 				throw new IllegalArgumentException("Key "+"'"+id+"'"+" is already present");
 			prepStmts.put(id, stmt);
 		} catch (NullPointerException npe){
 			logger.severe("Prepared Statement could not be created, invalid connection");
 		} catch (IllegalArgumentException iae){
 			logger.severe("Prepared Statement could not be created, "+iae.getMessage());
 		}
 	}
 
 	/**
 	 * Use DAO instead.
 	 */
 	@Deprecated
 	public boolean batchExecute(String[] statements){
 		Connection cn = getConnection();
 		Statement req = null;
 		
 		try {
 			for(String sql : statements){
 				req = cn.createStatement();
 				req.execute(sql);
 				req.close();
 			}
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR+e.getMessage());
 			return false;
 		} finally {
 			silentClose(cn, null, null);
 		}
 		
 		return true;
 	}
 	
 	public LinkedList<String> getBlacklistedFiles(){
 		LinkedList<String> images = new LinkedList<>();
 		
 		try {
 			images = blackListDAO.getBlacklisted();
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return images;
 	}
 	/**
 	 * Use {@link AidDAO#getDuplicatesAndOriginal()} instead.
 	 */
 	@Deprecated
 	public LinkedList<String[]> getDuplicates(){
 		final int NUM_OF_COLS = 3;
 		LinkedList<String[]> duplicates = new LinkedList<>();
 		LinkedList<FileRecord> records = duplicateDAO.getDuplicatesAndOriginals();
 
 		for(FileRecord record : records){
 			String[] dupe = new String[NUM_OF_COLS];
 			dupe[0] = record.getId();
 			dupe[1] = record.getLocation();
 			dupe[2] = record.getRelativePath().toString();
 			duplicates.add(dupe);
 		}
 		return duplicates;
 	}
 	
 	public LinkedList<FileRecord> getDuplicatesAndOriginal() {
 		return duplicateDAO.getDuplicatesAndOriginals();
 	}
 
 	protected PreparedStatement getPrepStmt(String command){
 		if(prepStmts.containsKey(command)){
 			Connection cn = getConnection();
 			
 			PreparedStatement prepStmt = null;
 			try {
 				prepStmt = cn.prepareStatement(prepStmts.get(command));
 			} catch (SQLException e) {
 				logger.warning("Failed to create prepared statement for command \""+command+"\"");
 			}
 			return prepStmt;
 		}else{
 			logger.warning("Prepared statment command \""+command+"\" not found.\nHas this object been initialized?");
 			return null;
 		}
 	}
 	
 	protected void silentClose(Connection cn, PreparedStatement ps, ResultSet rs){
 		if(rs != null)
 			try{rs.close();}catch(SQLException e){}
 		if(ps != null)
 			try{ps.close();}catch(SQLException e){}
 		if(cn != null)
 			try{cn.close();}catch(SQLException e){}
 	}
 	
 	protected void closeAll(PreparedStatement ps){
 		Connection cn = null;
 		ResultSet rs = null;
 		
 		if(ps == null)
 			return;
 		
 		try{cn = ps.getConnection();}catch(SQLException e){}
 		try{rs = ps.getResultSet();}catch(SQLException e){}
 		
 		if(rs != null)
 			try{rs.close();}catch(SQLException e){}
 		if(ps != null)
 			try{ps.close();}catch(SQLException e){}
 		if(cn != null)
 			try{cn.close();}catch(SQLException e){}
 	}
 
 	/**
 	 * Add the current URL to the cache, or update it's Timestamp if it
 	 * already exists. Method will return true if the URL is already present,
 	 * otherwise false.
 	 * @param url URL to be added
 	 * @return true if URL is already present else false.
 	 * Retruns true on error.
 	 */
 	public boolean addCache(URL url) {
 		try {
 
 			String id = url.toString();
 			Cache cacheRecord = new Cache(id);
 
 			if (cacheDAO.idExists(id)) {
 				return false;
 			} else {
 				cacheDAO.create(cacheRecord);
 				return true;
 			}
 		} catch (SQLException e) {
 			logger.warning(SQL_OP_ERR + e.getMessage());
 		}
 
 		return false;
 	}
 
 	public void addThumb(String url,String filename, byte[] data){
 		Thumbnail thumb = new Thumbnail(url, filename, data);
 		
 		try {
 			ThumbnailDAO.create(thumb);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 	}
 	
 	public boolean addIndex(FileInfo fileInfo, String location){
 		try {
 			LocationRecord locationRec = locationDao.queryForLocation(location);
 			
 			if(locationRec == null) {
 				locationRec = locationDao.createIfNotExists(new LocationRecord(location));
 			}
 			
 			IndexRecord index = new IndexRecord(fileInfo, locationRec);
 			
 			if(indexDao.idExists(fileInfo.getHash())) {
 				return false;
 			}
 			
 			resolvePathIDs(index);
 			
 			int rowsChanged = indexDao.create(index);
 			
 			if(rowsChanged == 1){
 				return true;
 			}
 			
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return false;
 	}
 	
 	private void resolvePathIDs(FileRecord record) throws SQLException {
 		DirectoryPathRecord directory = record.getDirectory();
 		FilePathRecord file = record.getFile();
 		
 		List<DirectoryPathRecord> directories = directoryDAO.queryForMatchingArgs(directory);
 		List<FilePathRecord> files = fileDAO.queryForMatchingArgs(file);
 		
 		if(directories.isEmpty()){
 			directoryDAO.createOrUpdate(directory);
 		}else{
 			directory = directories.get(0);
 		}
 		
 		if(files.isEmpty()) {
 			fileDAO.createOrUpdate(file);
 		}else{
 			file = files.get(0);
 		}
 		
 		record.setDirectory(directory);
 		record.setFile(file);
 	}
 	
 	public boolean addIndex(String hash, String path, long size, String location){
 		FileInfo info = new FileInfo(Paths.get(path), hash);
 		info.setSize(size);
 		
 		return addIndex(info, location);
 	}
 	
 	public boolean addDuplicate(String hash, String path, long size, String location){
 		FileInfo info = new FileInfo(Paths.get(path), hash);
 		info.setSize(size);
 		try {
 			LocationRecord locationRec = locationDao.queryForLocation(location);
 			DuplicateRecord index = new DuplicateRecord(info, locationRec);
 		
 			resolvePathIDs(index);
 			
 			int rowsChanged = duplicateDAO.create(index);
 			
 			if(rowsChanged == 1){
 				return true;
 			}
 			
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Get the number of pending filter items.
 	 * @return Number of pending items.
 	 */
 	public int getPending(){
 		try {
 			return filterDAO.getPendingFilterCount();
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return -1;
 	}
 
 	public ArrayList<Image> getThumb(String url){
 		LinkedList<Thumbnail> thumbs;
 		ArrayList<Image> images = new ArrayList<>(1);
 		
 		try {
 			thumbs = new LinkedList<>(ThumbnailDAO.queryForEq("url", url));
 			images = new ArrayList<>(thumbs.size());
 			
 			for(Thumbnail thumb : thumbs){
 				InputStream	is = new ByteArrayInputStream(thumb.getThumb());
 				images.add(ImageIO.read(is));
 				is.close();
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		} catch (IOException e) {
 			logger.severe(e.getMessage());
 		}
 		
 		return images;
 	}
 
 	/**
 	 * Use DAO countOf() instead.
 	 */
 	@Deprecated
 	public int size(AidTables table) {
 		long value = -1L;
 
 		try {
 			switch (table) {
 			case Filter:
 				value = filterDAO.countOf();
 				break;
 
 			case Cache:
 				value = cacheDAO.countOf();
 				break;
 
 			case Fileduplicate:
 				value = duplicateDAO.countOf();
 				break;
 
 			default:
 				break;
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return (int) value;
 	}
 
 	/**
 	 * Check the ID against the cache.
 	 * @param uniqueID ID to check
 	 * @return true if the ID is present otherwise false.
 	 * Returns true on errors.
 	 */
 	public boolean isCached(URL url){
 		return isCached(url.toString());
 	}
 
 	/**
 	 * Check the ID against the cache.
 	 * @param uniqueID ID to check
 	 * @return true if the ID is present otherwise false.
 	 * Returns true on errors.
 	 */
 	public boolean isCached(String uniqueID){
 		try {
 			return cacheDAO.idExists(uniqueID);
 		} catch (SQLException e) {
 			logSQLerror(e);
 			return true;
 		}
 	}
 	
 	private void logSQLerror(SQLException e) {
 		logSQLerror(e, "");
 		e.printStackTrace();
 	}
 	
 	private void logSQLerror(SQLException e, String command) {
 		logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
 	}
 
 	public boolean isDnw(String hash){
 		if(hash == null){
 			return false;
 		}
 		
 		boolean result = true;
 		
 		try {
 			result = dnwDAO.idExists(hash);
 			return result;
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return true;
 	}
 
 	public boolean isHashed(String hash){
 		try {
 			return indexDao.idExists(hash);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return true;
 	}
 
 	public boolean isBlacklisted(String hash){
 		try {
 			return blackListDAO.idExists(hash);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return true;
 	}
 	
 	public boolean isValidTag(String tag) {
 		if (getTagId(tag) == -1) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public int getTagId(String tag) {
 		try {
 			LocationRecord locRec = locationDao.queryForLocation(tag);
 
 			if (locRec != null) {
 				return locRec.getTag_id();
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return -1;
 	}
 	/**
 	 * Use the relevant DAO instead.
 	 */
 	@Deprecated
 	public void update(String id, AidTables table) {
 		try {
 			switch (table) {
 			case Block:
 				BlacklistRecord blacklist = new BlacklistRecord(id);
 				blackListDAO.createOrUpdate(blacklist);
 				break;
 				
 			case Dnw:
 				DnwRecord dnw = new DnwRecord(id);
 				dnwDAO.createOrUpdate(dnw);
 				break;
 
 			default:
 				logger.severe("Unhandled enum Table: " + table.toString());
 				break;
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 	}
 	
 	
 	//TODO add isIndexedPath(Path fullPath)
 	
 	public boolean isIndexedPath(Path fullpath, String locationTag) {
 		Path relPath = FileUtil.removeDriveLetter(fullpath);
 		LocationRecord locRec;
 		try {
 			locRec = locationDao.queryForLocation(locationTag);
 			FileInfo info = new FileInfo(relPath);
 			IndexRecord index = new IndexRecord(info, locRec);
 			resolvePathIDs(index);
 			index = indexDao.queryForFirst(index);
 			if (index != null) {
 				return true;
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 
 		return false;
 	}
 	
 	public int deleteIndexByPath(String fullpath){
 		return deleteIndexByPath(Paths.get(fullpath.toLowerCase()));
 	}
 	
 	public int deleteIndexByPath(Path path) {
 		FileInfo info = new FileInfo(path);
 		IndexRecord index = new IndexRecord(info, null);
 		int affectedRows = 0;
 		
 		try {
 			resolvePathIDs(index);
 			List<IndexRecord> results = indexDao.queryForMatching(index);
 			if(! results.isEmpty()){
 				affectedRows = indexDao.delete(results.get(0));
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return affectedRows;
 	}
 	
 	public int deleteDuplicateByPath(String fullpath){
 		return deleteDuplicateByPath(FileUtil.removeDriveLetter(Paths.get(fullpath.toLowerCase())));
 	}
 	
 	public int deleteDuplicateByPath(Path path){
 		FileInfo info = new FileInfo(path);
 		DuplicateRecord index = new DuplicateRecord(info, null);
 		int affectedRows = 0;
 		
 		try {
 			resolvePathIDs(index);
 			List<DuplicateRecord> results = duplicateDAO.queryForMatching(index);
 			if(! results.isEmpty()){
 				affectedRows = duplicateDAO.delete(results.get(0));
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return affectedRows;
 	}
 	
 	public void pruneCache(long maxAge){
 		try {
 			cacheDAO.pruneCache(maxAge);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 	}
 	
 	/**
 	 * Use DAO instead.
 	 */
 	@Deprecated
 	public int delete(AidTables table, String id) {
 		int affected = -1;
 
 		try {
 			switch (table) {
 			case Filter:
 				affected = filterDAO.deleteById(id);
 				break;
 
 			case Fileindex:
 				affected = indexDao.deleteById(id);
 				break;
 
 			case Dnw:
 				affected = dnwDAO.deleteById(id);
 				break;
 
 			case Block:
 				affected = blackListDAO.deleteById(id);
 				break;
 
 			default:
 				logger.severe("Unable to perform delete for table "
 						+ table.toString());
 				break;
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 
 		return affected;
 	}
 
 	public void sendStatement(String sqlStatment){
 		Connection cn = getConnection();
 		Statement req = null;
 		try {
 			req = cn.createStatement();
 			req.execute(sqlStatment);
 			req.close();
 		} catch (SQLException e) {
 			logger.warning("Failed to execute statement id: "+sqlStatment+"\n"+e.getMessage());
 		} finally {
 			if(req != null)
 				try{req.close();} catch (SQLException e){}
 
 			silentClose(cn, null, null);
 		}
 	}
 
 	public String getSetting(DBsettings settingName) {
 		try {
 			Settings setting = settingDao.queryForId(settingName.toString());
 			return setting.getParam();
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Get path associated with the given hash value.
 	 * 
 	 * @param hash hash value to lookup
 	 * @return path as a String or null if not found
 	 */
 	public String getPath(String hash){
 		try {
 			IndexRecord index = indexDao.queryForId(hash);
 			return index.getRelativePath().toString();
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return null;
 	}
 	
 	public ArrayList<Path> getLocationPathList(String locationTag) {
 		ArrayList<Path> pathList = new ArrayList<>();
 		
 		try {
 			List<LocationRecord> locRec = locationDao.queryForEq("location", locationTag);
 			
 			if(! locRec.isEmpty()){
 				IndexRecord index = new IndexRecord();
 				index.setLocation(locRec.get(0));
 				List<IndexRecord> results = indexDao.queryForMatchingArgs(index);
 				
 				for(IndexRecord result : results){
 					pathList.add(result.getRelativePath());
 				}
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return pathList;
 	}
 	
 	public ArrayList<String> getLocationFilelist(String locationTag) {
 		ArrayList<Path> pathList = getLocationPathList(locationTag);
 		ArrayList<String> pathStrings = new ArrayList<>(pathList.size());
 
 		for (Path path : pathList) {
 			pathStrings.add(path.toString());
 		}
 
 		return pathStrings;
 	}
 	
 	public int getLocationIndexSize(String locationTag) {
 		try {
 			LocationRecord locRec = locationDao.queryForLocation(locationTag);
 
 			if (locRec == null) {
 				return -1;
 			}
 			SelectArg selectLoc = new SelectArg();
 			selectLoc.setValue(locRec.getTag_id());
 			PreparedQuery<IndexRecord> indexPrep = indexDao.queryBuilder()
 					.setCountOf(true).where().eq("location", selectLoc)
 					.prepare();
 			return (int) indexDao.countOf(indexPrep);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 
 		return -1;
 	}
 	
 	public String getLocationById(String id) {
 		try {
 			IndexRecord index = indexDao.queryForId(id);
 			if (index == null) {
 				return DEFAULT_LOCATION;
 			} else {
 				return index.getLocation();
 			}
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return DEFAULT_LOCATION;
 	}
 
 	public boolean moveIndexToDuplicate(String id){
 		try {
 			return indexDao.moveIndexToDuplicate(id);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return false;
 	}
 
 	public boolean moveDuplicateToIndex(String id){
 		try {
 			return duplicateDAO.moveDuplicateToIndex(id);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return false;
 	}
 	
 	public boolean addFilter(FilterItem fi) {
 		try {
 			String id = filterDAO.extractId(fi);
 			if(filterDAO.idExists(id)){
 				return false;
 			}
 			
 			filterDAO.create(fi);
 			return true;
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Adds a filter item to the database.
 	 * @param id id of the item
 	 * @param board board alias
 	 * @param reason reason for adding the filter
 	 * @param state initial state of the filter
 	 * @return true if the filter was added, else false
 	 */
 	public boolean addFilter(String id, String board, String reason, FilterState state) {
 		try {
 			FilterItem filter = new FilterItem(new URL(id), board, reason, state);
 			return addFilter(filter);
 		} catch (MalformedURLException e) {}
 	
 		return false;
 	}
 
 	public void updateState(String id, FilterState state) {
 		try {
 			FilterItem filter = filterDAO.queryForId(id);
 			filter.setState(state);
 			filterDAO.update(filter);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 	}
 
 	public FilterState getFilterState(String id) {
 		try {
 			FilterItem filter = filterDAO.queryForId(id);
 			return filter.getState();
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		
 		return FilterState.UNKNOWN;
 	}
 
 	/**
 	 * Returns all items in the filter with state set to pending (1).
 	 * @return a list of all pending filter items
 	 */
 	public LinkedList<FilterItem> getPendingFilters() {
 		try {
 			List<FilterItem> results = filterDAO.queryForEq("status", FilterState.PENDING);
 			return new LinkedList<>(results);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return new LinkedList<FilterItem>();
 	}
 
 	public void updateFilterTimestamp(String id) {
 		try {
 			filterDAO.updateFilterTimestamp(id);
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 	}
 
 	public String getOldestFilter() {
 		try {
			URL oldestUrl = filterDAO.getOldestFilter().getUrl();
			return oldestUrl.toString();
 		} catch (SQLException e) {
 			logSQLerror(e);
 		}
 		return null;
 	}
 }
