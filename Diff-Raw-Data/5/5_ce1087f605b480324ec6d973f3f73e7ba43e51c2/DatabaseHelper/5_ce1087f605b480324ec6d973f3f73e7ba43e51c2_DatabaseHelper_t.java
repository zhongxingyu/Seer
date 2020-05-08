 package safeguard;
 
 import safeguard.Logging.*;
 
 import java.util.*;
 import java.util.regex.*;
 import java.io.File;
 import java.math.BigInteger;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.*;
 import java.sql.Date;
 
 import safeguard.AccessController.ClassLevel;
         
 public class DatabaseHelper {
     private Connection connection;	
     private String query;
     private Statement stmt;
     private static final Path path = Paths.get(System.getProperty("user.dir"), "SafeGuard.db");
 	
     private static DatabaseHelper databaseHelperInstance = null;
 
     private void handleException(Exception e) {
     	System.err.println( e.getClass().getName() + ": " + e.getMessage() );
     	try {
     		stmt.close();
     		connection.close();
     	} catch (Exception ee) {
     		
     	} finally {
     		System.exit(0);
     	}
     }
     
     private String escape(String str) {
     	return "\"" + str + "\"";
     }
     
     private DatabaseHelper() {
     	try {
     	      Class.forName("org.sqlite.JDBC"); // dynamically load the class
     	      boolean existed = Files.exists(path, LinkOption.NOFOLLOW_LINKS);
     	      connection = DriverManager.getConnection("jdbc:sqlite:" + path);  // Will be created if not found.
     	      stmt = connection.createStatement();
     	      connection.setAutoCommit(true); // is the default. Do this to be double-sure. Needed for the following pragma statement. 
     	      stmt.execute("PRAGMA foreign_keys = ON;"); // must be invoked each time new connection is created. We share an only connection though.
     	      if (!existed) {
     	    	  createTables();
     	    	  test();
     	      }
     	} catch (Exception e) {
     		handleException(e);
     	}
 
 //    	System.err.println("Connection established");
     }
     
     public static DatabaseHelper getInstance() {
     	if (databaseHelperInstance != null)
     		return databaseHelperInstance;
     	else 
     		return databaseHelperInstance = new DatabaseHelper();
     }
     
     public void createTables() { 
     	try {
 //	    	stmt = connection.createStatement();
     		connection.setAutoCommit(false); // larger, user-defined transactions
 	    	query = "CREATE TABLE IF NOT EXISTS `Resource` (" +
 	    			"`idFile` INTEGER PRIMARY KEY NOT NULL," + /*The table stores only protected resources (secret files, integrity-checked files, protected executables, directories and volumes)*/
 	    			"`name` TEXT UNIQUE NOT NULL," +
 	    			"`classLevel` INTEGER NULL)";  /*NULL if the file is not classified (still, we may want to check its integrity) or is not a secret file (i.e. an executable, a volume or a folder).*/
 	    	stmt.execute(query);
 	    	
 	    	query = "CREATE TABLE IF NOT EXISTS `Integrity` (" + 
 	    			"`idFile` INTEGER NOT NULL," + 
 	    			"`checkSum` TEXT NOT NULL," + 
 	    			"PRIMARY KEY (`idFile`)," + 
 	    			"CONSTRAINT `idFile`" + 
 	    				"FOREIGN KEY (`idFile`)"+
 	    				"REFERENCES `Resource` (`idFile`)" +
 	    				"ON DELETE CASCADE ON UPDATE NO ACTION)";
 	    	stmt.execute(query);
 
 	    	
 	    	query = "CREATE TABLE IF NOT EXISTS User (" + 
 	    			"uid INTEGER PRIMARY KEY ASC AUTOINCREMENT NOT NULL," +
 	    			"[group] INTEGER NULL, " + // NULL group number is -1 in terms of the specification 
 	    			"classLevel INTEGER NOT NULL," +
 	    			"newFileMark INTEGER NOT NULL, " + 
 	    			"newDirMark INTEGER NOT NULL," +
 	    			"passHash VARCHAR NOT NULL, " +
 	    			"name VARCHAR UNIQUE NOT NULL," + 
 	    			"isAdmin TINYINT NOT NULL)";
 	    	stmt.execute(query);
 	    	
 	    	query = "CREATE TABLE IF NOT EXISTS `Register` (" +
 	    			"`eventType` INTEGER NOT NULL, " +
 	    			"`userName` TEXT NOT NULL, "+
 	    			"`date` TEXT NOT NULL, "+ // YYYY-MM-DD HH:MM:SS.SSS if we use sqlite built-in datetime functions
 	    			"`fileName` TEXT NULL, " +  /*Program/Secret file/Directory/Null*/ // NULL for event types not associated with files
 //	    			"PRIMARY KEY (`date`),"+ // FIXME: what is precision of datetime? what if we accidentally make it not unique? 
 	    			"CONSTRAINT `uid`"+
 	    			  	"FOREIGN KEY (`userName`)"+
 	    			  	"REFERENCES `User` (`name`)"+
 	    			  	"ON DELETE NO ACTION "+ // Register logs persist even for non-existent users 
 	    			  	"ON UPDATE NO ACTION,"+ // Not supposed to be user-modifiable
 	    			"CONSTRAINT `idFile`"+
 	    			  	"FOREIGN KEY (`fileName`)"+
 	    			  	"REFERENCES `Resource` (`name`)"+
 	    			  	"ON DELETE NO ACTION "+ // Logs persist even for deleted files 
 	    			  	"ON UPDATE NO ACTION)"; // Not supposed to be user-modifiable
 	    	stmt.execute(query);
 	    	
 	    	query= " CREATE TABLE IF NOT EXISTS `Access` (" +
 	    			" `idFile` INTEGER NOT NULL,"+ 
 	    			" `uid` INTEGER NOT NULL," +
 	    			" `full` TINYINT(1) NULL, " +  /* comment truncated */ /*Full access flag is used for secret files only and means RW access (false then means partial access or R)*/
 	    			" PRIMARY KEY (`idFile`, `uid`),"+ 
 	    			" CONSTRAINT `uid`" +
 	    				" FOREIGN KEY (`uid`)"+ 
 	    				" REFERENCES `User` (`uid`)"+ 
 	    				" ON DELETE CASCADE" +  // No user -> no access table for that user
 	    				" ON UPDATE NO ACTION," + // Not supposed to be user-modifiable
 	    			" CONSTRAINT `idFIle`" +
 	    				" FOREIGN KEY (`idFile`)" +
 	    				" REFERENCES `Resource` (`idFile`)" +
 	    				" ON DELETE CASCADE" + // No resource -> no access to it
 	    				" ON UPDATE NO ACTION)"; // Not supposed to be user-modifiable
 	    	stmt.execute(query);
 	    	connection.commit();
 	    	connection.setAutoCommit(true); 
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     //TODO
     
     public void test () {
     	try {
     		query = "insert into resource (name) values (\"X:\")"; // volume
     		stmt.execute(query);
     		query = "insert into resource (name) values (\"C:\\folder\\\")"; // folder
     		stmt.execute(query);
     		query = "insert into resource (name) values (\"C:\")"; // volume
     		stmt.execute(query);
     		query = "insert into resource (name) values (\"C:\\folder\\folder2\\\")"; // folder
     		stmt.execute(query);
     		query = "insert into resource (name) values (\"C:\\folder\\file1\")"; // file
     		stmt.execute(query);
     		
     		query = "insert into resource (name) values (\"C:\\file0\")"; // file
     		stmt.execute(query);
     		
     		query = "insert into integrity (idFile, checkSum) values (5, " + escape("324234") + ")";
     		stmt.execute(query);
     		query = "insert into integrity (idFile, checkSum) values (6, " + escape("324") + ")";
     		stmt.execute(query);
     		
     		System.err.println(addUser("vasya", -1, AccessController.ClassLevel.CONFIDENTIAL, Engine.AccessMode.FULL_ACCESS, Engine.AccessMode.FULL_ACCESS, "abacaba", true));
     		System.err.println("hewe");
     		Event event = new OpenSecretFileEvent("vasya", Paths.get("C:\\folder\\file1"));
     		System.err.println("hewe1");
 //    		logEvent(event);
     		System.err.println("hewe2");
     	} catch (Exception e) {
     		System.out.printf("failo\n");
     		handleException(e);
     	}
     }
     
     public boolean isResource(Path fileName) {
     	try {
     		query = "SELECT [name] FROM Resource WHERE [name] = " + escape(fileName.toString());
     		boolean result = false;
     		ResultSet rs = stmt.executeQuery(query);
     		if (rs.next())
     			result = true;
     		rs.close();
     		return result;
     	} catch (Exception e) {
     		handleException(e);
     		return false;
     	}
     }
     
     public List<Path> getControlledDirsAndFilesIn(Path directory) {
     	String directoryStr = directory.toString();
     	if (directoryStr.charAt(directoryStr.length() - 1) != '\\')
     		directoryStr += "\\";
     	StringBuilder strbld = new StringBuilder(directoryStr);
     	int fromIndex = 0;
     	while ((fromIndex = strbld.indexOf("\\", fromIndex)) != -1) { // double-backslash for regex
     		strbld.insert(fromIndex, "\\");
     		fromIndex += 2;
     	}
     	query = "SELECT name FROM Resource WHERE name LIKE \"" + directoryStr + "%\"";
     	Pattern regex = Pattern.compile(strbld.toString() + "[^\\\\]+\\\\?"); // mmm, java regex... love 'em
     	try {
     		ResultSet rs = stmt.executeQuery(query);
     		List<Path> list = new ArrayList<>();
     		while (rs.next()) {
     			if (regex.matcher(rs.getString("name")).matches())
     				list.add(Paths.get(rs.getString("name")));
     		}
     		rs.close();
     		return list;
     	} catch (Exception e) {
     		handleException(e);
     		return null; // make compiler happy
     	}
     }
     
     public List<Path> getIntegrityCheckedFilenames() {
     	try {
     		List<Path> list = new ArrayList<>();
     		query = "SELECT name FROM Integrity JOIN Resource ON Integrity.idFile = Resource.idFile";
     		ResultSet rs = stmt.executeQuery(query);
     		while ( rs.next() ) 
     			list.add(Paths.get(rs.getString("name")));
     		rs.close();
     		return list;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public String getCheckSum(Path fileName) {
     	try {
     		assert isResource(fileName);
     		query = "SELECT checkSum FROM Integrity AS I JOIN Resource AS R ON I.idFile = R.idFile WHERE name = " + escape(fileName.toString());
     		ResultSet rs = stmt.executeQuery(query);
     		String result = null;
     		if (rs.next())
     			result = rs.getString(1);
     		rs.close();
     		return result;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public void updateCheckSum(Path fileName, String checkSum) {
     	try {
     		assert isResource(fileName);
     		query = "UPDATE Integrity SET checkSum = " + escape(checkSum) +
     				 	" WHERE idFile IN " +  
     					"(SELECT idFile FROM Resource WHERE name = " + escape(fileName.toString()) + ")";
     		stmt.executeUpdate(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     // before calling this method make sure fileName is a resource
     public void startIntegrityControl(Path fileName, String checkSum) {
     	try {
     		assert isResource(fileName);
     		int idFile = getFileId(fileName);
     		query = "INSERT INTO Integrity (idFile, checkSum) VALUES (" + idFile + ", " + escape(checkSum) + ")";
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public  void stopIntegrityControl(Path fileName) {
     	try {
     		query = "DELETE FROM Integrity WHERE idFile IN " +
     					"(SELECT idFile FROM Resource WHERE name = " + escape(fileName.toString()) + ")";
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     // use for classified files only!
     public void addResourceSecretFile(Path fileName, AccessController.ClassLevel classLevel) {
     	try {
     		query = "INSERT INTO Resource (name, classLevel) VALUES (" + escape(fileName.toString()) + ", " + classLevel.value() + ")"; 
     		stmt.executeUpdate(query);
     		assert isResource(fileName);
     	} catch (Exception e) {
     		handleException(e);
     	}    	
     }
 
     // for non-secret files (e.g. we only want no check integrity), executables, dirs and volumes
     public void addResourceOther(Path resName) {
     	try {
     		query = "INSERT INTO Resource (name) VALUES (" + escape(resName.toString()) + ")";
     		stmt.executeUpdate(query);
     		assert isResource(resName);
     	} catch (Exception e) {
     		handleException(e);
     	}    	    	
     }
     
     // get all uids 
     public List<Integer> getUsers() {
     	try {
     		query = "SELECT uid FROM User";
     		ResultSet rs = stmt.executeQuery(query);
     		List<Integer> list = new ArrayList<>();
     		while (rs.next()) 
     			list.add(rs.getInt(1));
     		rs.close();
     		return list;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}    	
     }
     
     public boolean addUser(String name, Integer group, AccessController.ClassLevel classLevel, Engine.AccessMode newFileMark, 
     		Engine.AccessMode newDirMark, String passHash, Boolean isAdmin) {
     	assert newDirMark == Engine.AccessMode.CANNOT_OPEN || newDirMark == Engine.AccessMode.FULL_ACCESS;
     	try {  		
     		query = "INSERT INTO user (name, [group], classLevel, newFileMark, newDirMark, passHash, isAdmin) " +
 	    			"VALUES (" + escape(name) + "," + (group == null || group < 0 ? "NULL" : group.toString()) +
 	    			"," + classLevel.value() + "," + newFileMark.value() + "," +
 	    			newDirMark.value() + "," + escape(passHash) + "," + (isAdmin ? "1)" : "0)");
     		System.err.println(query);
     		stmt.execute(query);
     		return true;
     	} catch (Exception e) {
     		return false;
     	}
     }
 
     // be careful! It only adds access when there was no access (CANNOT_OPEN)
     public void addAccessToSecretFile(int uid, Path fileName, Engine.AccessMode mode) {
     	try {
     		int idFile = getFileId(fileName);
     		int full = mode == Engine.AccessMode.FULL_ACCESS ? 1 : 0;
     		query = "INSERT INTO Access (uid, idFile, full) VALUES (" + uid + ", " + idFile + ", " + full + ")";
     		stmt.execute(query);
     	} catch(Exception e) {
     		handleException(e);
     	}
     }
     
     // Dirs, volumes, executables
     public void addAccessToOther(int uid, Path resName) {
     	// omit full flag so it will be null
     	try {
     		int idFile = getFileId(resName);
     		query = "INSERT INTO Access (uid, idFile) VALUES (" + uid + ", " + idFile + ")";
     		stmt.execute(query);
     	} catch(Exception e) {
     		handleException(e);
     	}
     }
     
     public void updateAccessModeToSecretFile(int uid, Path fileName, Engine.AccessMode mode) {
     	try {
     		if (mode == Engine.AccessMode.CANNOT_OPEN) {
     			removeAccessToResource(uid, fileName);
     			return;
     		}
     		int idFile = getFileId(fileName);
     		int full = mode == Engine.AccessMode.FULL_ACCESS ? 1 : 0;
     		query = "UPDATE Access SET full = " + full + " WHERE uid = " + uid + " AND idFile = " + idFile;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public void removeAccessToResource(int uid, Path name) {
     	try {
     		query = "DELETE FROM Access WHERE uid = " + uid + " AND idFile = " + getFileId(name);
     		stmt.execute(query);
     	} catch(Exception e) {
     		handleException(e);
     	}    	
     }
     
     public AccessController.ClassLevel getUserClass(int uid) {
     	try {
     		query = "SELECT classLevel FROM User WHERE uid = " + uid;
     		ResultSet rs = stmt.executeQuery(query);
     		AccessController.ClassLevel result = AccessController.ClassLevel.map(rs.getInt("classLevel"));
 	    	rs.close();
 	    	return result;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public void setUserClass(int uid, AccessController.ClassLevel level) {
     	try {
     		query = "UPDATE User SET classLevel = " + level.value() + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
 
     // -1 for no group
     public Integer getUserGroup(int uid) { 
     	try {
     		query = "SELECT [group] FROM User WHERE uid = " + uid;
     		ResultSet rs = stmt.executeQuery(query);
     		int result = rs.getInt(1);
     		if (rs.wasNull())
     			result = -1;
     	    rs.close();
     	    return result;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public void setUserGroup(int uid, int gid) {
     	try {
     		query = "UPDATE User SET [group] = " + gid + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
 
     public Engine.AccessMode getUserNewFileMark(int uid) {
     	try {
     		query = "SELECT [newFileMark] FROM User WHERE uid = " + uid;
     		ResultSet rs = stmt.executeQuery(query);
     		int val = rs.getInt(1);
     		rs.close();
     		return Engine.AccessMode.map(val);
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}	
     }
     
     public void setUserNewFileMark(int uid, Engine.AccessMode mode) {
     	try {
     		query = "UPDATE User SET [newFileMark] = " + mode.value() + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public Engine.AccessMode getUserNewDirMark(int uid) {
     	try {
     		query = "SELECT [newDirMark] FROM User WHERE uid = " + uid;
     		ResultSet rs = stmt.executeQuery(query);
     		int val = rs.getInt(1);
     		rs.close();
     		return Engine.AccessMode.map(val);
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}	
     }
     
     public int getUserId(String name) {
     	try {
     		query = "SELECT [uid] FROM User WHERE name = " + escape(name);
     		ResultSet rs = stmt.executeQuery(query);
     		int val = rs.getInt(1);
     		rs.close();
     		return val;
     	} catch (Exception e) {
     		handleException(e);
     		return -1;
     	}
     }
     
     public void setUserNewDirMark(int uid, Engine.AccessMode mode) {
     	assert mode == Engine.AccessMode.CANNOT_OPEN || mode == Engine.AccessMode.FULL_ACCESS;
     	try {
     		query = "UPDATE User SET [newDirMark] = " + mode.value() + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}   	
     }
     
     public String getUserName(int uid) {
     	try {
     		query = "SELECT [name] FROM User WHERE uid = " + uid;
     		ResultSet rs = stmt.executeQuery(query);
     		String val = rs.getString(1);
     		rs.close();
     		return val;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}	
     }
     
     public void setUserName(int uid, String name) {
     	try {
     		query = "UPDATE User SET [name] = " + escape(name) + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public Boolean getUserIsAdmin (int uid) {
     	try {
     		query = "SELECT [isAdmin] FROM User WHERE uid = " + uid;
     		ResultSet rs = stmt.executeQuery(query);
     		int val = rs.getInt(1);
     		rs.close();
     		return val == 1;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}	
     }
     
     public void setUserIsAdmin(int uid, boolean adminness) { // who makes sure there is at least one admin? Logic says it's not me. 
     	try {
     		query = "UPDATE User SET [isAdmin] = " + (adminness ? "1" : "0") + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public Engine.AccessMode accessTypeDirVolExe(int uid, Path fileName) {
     	try {
     		if (!isResource(fileName)) 
     			return Engine.AccessMode.CANNOT_OPEN;	// No access for folders not listed in our system.
     		query = "SELECT [full] FROM Access JOIN Resource ON Access.idFile = Resource.idFile WHERE uid = " + uid + " AND [name] = " + escape(fileName.toString());
     		ResultSet rs = stmt.executeQuery(query);
     		if (rs.next()) 
     			return Engine.AccessMode.FULL_ACCESS;
     		return Engine.AccessMode.CANNOT_OPEN;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public Engine.AccessMode accessTypeSecretFile(int uid, Path fileName) {
     	try {
     		assert isResource(fileName);
     		query = "SELECT [full] FROM Access JOIN Resource ON Access.idFile = Resource.idFile WHERE uid = " + uid + " AND [name] = " + escape(fileName.toString());
     		ResultSet rs = stmt.executeQuery(query);
     		if (rs.next()) 
     			return rs.getInt("full") == 0 ? Engine.AccessMode.READ_ONLY : Engine.AccessMode.FULL_ACCESS;
     		return Engine.AccessMode.CANNOT_OPEN;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public String getUserPassHash(int uid) { // String it is!
 		try {
 			query = "SELECT [passHash] FROM User WHERE uid = " + uid;
 			ResultSet rs = stmt.executeQuery(query);
 			String val = rs.getString(1);
 			rs.close();
 			return val;
 		} catch (Exception e) {
 			handleException(e);
 			return null;
 		}
     }
 
     public void setUserPassHash(int uid, String hash) {
     	try {
     		query = "UPDATE USER SET passHash = " + escape(hash) + " WHERE uid = " + uid;
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public AccessController.ClassLevel getFileClass(Path fileName) {
     	try {
     		assert isResource(fileName);
     		query = "SELECT classLevel FROM Resource WHERE name = " + escape(fileName.toString());
     		ResultSet rs = stmt.executeQuery(query);
     		AccessController.ClassLevel result = AccessController.ClassLevel.map(rs.getInt("classLevel"));
     	    if (rs.wasNull()) {
     	    	rs.close();
     	    	return null;
     	    } else {
     	    	rs.close();
     	    	return result;
     	    }
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     public void setFileClass(Path fileName, AccessController.ClassLevel level) {
     	try {
     		assert isResource(fileName);
     		query = "UPDATE Resource SET classLevel = " + level.value() + " WHERE name = " + escape(fileName.toString());
     		stmt.execute(query);
     	} catch (Exception e) {
     	
     	}
     }
     
     public void logEvent(Event event) {
     	try {
     		query = "INSERT INTO Register (eventType, userName, fileName, [date]) VALUES (" +
     				event.getIdEvent().value() + "," +
     				escape(event.getUserName()) + "," +
     				escape(event.getAdvancedField()) + "," +
     				"datetime(\"now\")" + ")";
 //    		System.err.println(query);
     		stmt.execute(query);
     	} catch (Exception e) {
     		handleException(e);
     	}
     }
     
     public List<Event> getRegister() {
     	try {
     		query = "SELECT * FROM Register";
     		ResultSet rs = stmt.executeQuery(query);
     		List<Event> list = new ArrayList<>();
     		Event event = null;
     		while (rs.next()) {
     			Date dateEvent = rs.getDate("date");
     			int type = rs.getInt("eventType");
     			String userName = rs.getString("userName");
    			Path fileName = Paths.get(rs.getString("fileName"));
     			switch (type) { // FIXME: add assertions somewhere. Probably move this switch to another class.
     			case 0: 
     				event = new LoginEvent(userName, dateEvent);
     				break;
     			case 1:
     				event = new LoginAttemptEvent(userName, dateEvent);
     				break;
     			case 2:
     				event = new OpenSecretFileEvent(userName, fileName, dateEvent);
     				break;
     			case 3:
     				event = new RunProgEvent(userName, fileName, dateEvent);
     				break;
     			case 4:
     				event = new UnauthorizedAccessToCatalogEvent(userName, fileName, dateEvent);
     				break;
     			case 5:
     				event = new UnauthorizedAccessToDiskEvent(userName, fileName, dateEvent); 
     				break;
     			case 6:
     				event = new UnauthorizedCatalogDeletionEvent(userName, fileName, dateEvent);
     				break;
     			case 7:
    				event = new UnauthorizedFileDeletionEvent(userName, fileName, dateEvent); 
     				break;
     			case 8:
     				event = new UnauthorizedMakeCatalogEvent(userName, fileName, dateEvent);
     				break;
     			case 9:
     				event = new UnauthorizedRunProgEvent(userName, fileName, dateEvent);
     				break;
     			}
     			list.add(event);
     		}
     		rs.close();
     		return list;
     	} catch (Exception e) {
     		handleException(e);
     		return null;
     	}
     }
     
     private int getFileId(Path fileName) {
     	try {
     		assert isResource(fileName);
     		query = "SELECT idFile FROM Resource WHERE name = " + escape(fileName.toString());
     		ResultSet rs = stmt.executeQuery(query);
     		int idFile = rs.getInt(1);
     		rs.close();    	
     		return idFile;
     	} catch (Exception e) {
     		handleException(e);
     		return 0;
     	}
     }
 }
