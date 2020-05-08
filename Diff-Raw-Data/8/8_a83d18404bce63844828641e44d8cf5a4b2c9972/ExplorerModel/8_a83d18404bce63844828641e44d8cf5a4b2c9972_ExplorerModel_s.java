 /**
  * The explorer model is a model of the database tables in Java. This class 
  * can be used to interface with the PSQL database for all the CRUD operations.
  * 
  *  @author Andrew Hollenbach <anh7216@rit.edu>
  *  @author Andrew DeVoe <ard5852@rit.edu>
  */
 package steam.dbexplorer.model;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.github.koraktor.steamcondenser.steam.community.WebApi;
 
 import steam.dbexplorer.Credentials;
 import steam.dbexplorer.SystemCode;
 import steam.dbexplorer.dbobject.DBReference;
 
 public class ExplorerModel {
 	
 	static Connection con;
 	
 	/**
 	 * Sets up the ExplorerModel to be ready to execute JDBC operations
 	 */
     public static void setUp() {
 		try {
 			WebApi.setApiKey(Credentials.APIKEY);
 
 			String url = Credentials.DATABASEURL;
 			Class.forName("org.postgresql.Driver");
 			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     /**
      * Sets up the ExplorerModel to close the connection to the database
      */
     public static void tearDown() {
     	try {
 		    con.close();
     	} catch (Exception e) {
         	e.printStackTrace();
         }
     }
     
     
 
 	/**
 	 * Creates a new entry in the database in the supplied table with the 
 	 * supplied values.
 	 * 
 	 * @param entityName The type of object (Player, Friend, etc.)
 	 * @param values The values to insert into the table.
 	 * @return A system code describing the success or failure of the operation.
 	 */
 	public static SystemCode createEntity(String entityName, Object[] values) {
 		String createString = "insert into " + entityName + " values (";
 		for(int i = 0; i < values.length; i++) {
 			
 			if ( i != 0 ) {
 				createString += " ,";
 			}
 			createString += values[i];
 		}
 		createString += ");";
 		
 		PreparedStatement createStatement = null;
 		try {
 			createStatement = con.prepareStatement(createString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			createStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException ex) {
 			return handleInsertUpdateError(createStatement.toString(), entityName, ex);
 		}
 		
 /*		String createString = "insert into " + entityName + " values (";
 		for(int i = 0; i < values.length; i++) {
 			
 			if ( i != 0 ) {
 				createString += " ,";
 			}
 			createString += " ?";
 		}
 		createString += ");";
 				
 		try {
 			PreparedStatement createStatement = con.prepareStatement(createString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			createStatement.setString(1, entityName);
 			for(int i = 0; i < values.length; i++) {
 				createStatement.setObject(i+2, values[i]);
 			}
 			System.out.println(createStatement);
 			createStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return SystemCode.FAILURE;
 		} */
 	}
 	
 	/**
 	 * Handles errors while attempting to insert data into the database.  Takes the SQLException
 	 * and returns a user friendly error message
 	 * 
 	 * @param queryString The string query that generated the error
 	 * @param table  The table that the query was being executed on
 	 * @param ex  The SQLException throw
 	 * @return  The user friendly error message.
 	 */
 	private static SystemCode handleInsertUpdateError(String queryString, String table, SQLException ex) {
 		String message = ex.getLocalizedMessage();
 		if(message.contains("foreign key constraint")) {
 			String failTable = message.substring(message.indexOf("in table \"")+10,message.length()-2);
 			if(startsWithVowel(failTable)) {
 				failTable = "n " + failTable; //proper grammar!
 			} else {
 				failTable = " " + failTable;
 			}
 			SystemCode r = SystemCode.BAD_FK;
 			r.alterMessage(failTable);
 			return r;
 		} else if(message.contains("ERROR: syntax error") || message.contains("ERROR: column")) {
 			int pos = Integer.parseInt(message.substring(message.indexOf("Position: ")+10));
 			String createSub = queryString.substring(0,pos);
 			int colNum = StringUtils.countMatches(createSub, ",");
 			String attrName = DBReference.getDisplayName(table,colNum);				
 			SystemCode r = SystemCode.BAD_VALUE;
 			r.alterMessage(attrName);
 			return r;
 		} else if(message.contains("ERROR: duplicate key")) {
 			return SystemCode.ALREADY_EXIST;
 		} else if(message.contains("ERROR: null value")) {
 			SystemCode r = SystemCode.MISSING_PK;
 			int firstQuote = message.indexOf("\"")+1;
 			String missingPK = message.substring(firstQuote,message.indexOf("\"", firstQuote));
 			r.alterMessage(missingPK);
 			ex.printStackTrace();
 			return SystemCode.MISSING_PK;
 		}
 		ex.printStackTrace();
 		return SystemCode.FAILURE;
 	}
 
 	/**
 	 * Grabs players from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrievePlayers(Object[] options) {
 		String commandString = "select * from player ";
 		commandString += getWhereAndSort(options,"steamId");
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs friends from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to friend entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveFriends(Object[] options) {
 		String commandString = "select friend.steamId1, one.personaName, friend.steamId2, two.personaName";
 		commandString += " from friend join Player as one on one.steamId = friend.steamId1 join Player as two on two.steamId = friend.steamId2 ";
 		commandString += getWhereAndSort(options,"steamId1");
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs applications from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to application entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveApplications(Object[] options) {
 		String commandString = "select * from application ";
 		commandString += getWhereAndSort(options,"appId");
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs achievements from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to achievment entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveAchievements(Object[] options) {
 		String commandString = "select achievement.appId, application.appName, achievement.achievementName from achievement";
 		commandString += " join application on achievement.appId = application.appId ";
 		commandString += getWhereAndSort(options,"appId");
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs ownedAchievements from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param steamID The steamID of a player whose applications to retrieve
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to ownedAchievement entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveOwnedAchievements (Object[] options) {
 		String commandString = "select ownedAchievement.appId, application.appName, ownedAchievement.steamId, player.personaName, ownedAchievement.achievementName";
 		commandString += " from ownedAchievement";
 		commandString += " join application on ownedAchievement.appId = application.appId";
 		commandString += " join player on ownedAchievement.steamId = player.steamId ";
 		commandString += getWhereAndSort(options,"appId");
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs the owned applications for the specified steamID from the database 
 	 * and converts values from the resultSet into an array of objects.
 	 * Parameters subject to change in the future.
 	 *  
 	 * @param steamID The steamID of a player whose applications to retrieve
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to owned application entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveOwnedApplications (Object[] options) {
 		String commandString = "select ownedApplication.appId, application.appName, ownedApplication.steamId, player.personaName";
 		commandString += " from ownedApplication";
 		commandString += " join application on ownedApplication.appId = application.appId";
 		commandString += " join player on ownedApplication.steamId = player.steamId ";
 		commandString += getWhereAndSort(options,"appId");
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * This is a simple method that takes in a list of options created by the 
 	 * controller and returns a string ready to pass to the server
 	 * This GUARANTEES that there will be a trailing semicolon.
 	 * 
 	 * @param options A list of options for the where and sort clauses.
 	 * @param defaultSort If no sort is specified, this is used as the default attribute to sort on
 	 * @return
 	 */
 	private static String getWhereAndSort(Object[] options, String defaultSort) {
 		String commandString = "";
 		if (options.length > 0) {
 			String where = " where ";
 			String orderBy = " order by ";
 			for (int i = 0; i < options.length; i++) {
 				String command = options[i].toString();
 				if(command.split("=").length == 2 && command.split("=")[0].equals("sort")) {
 					orderBy += command.split("=")[1] + ", "; 
 				} else {
 					where += command + " and ";
 				}
 			}
 			if(!where.equals(" where ")) {
 				where = where.substring(0,where.length()-4);
 				commandString += where;
 			}
 			if(orderBy.equals(" order by ")) commandString += " order by " + 
 														defaultSort;
 			else  {
 				orderBy = orderBy.substring(0,orderBy.length()-2);
 				commandString += orderBy;
 			}
 		} else {
 			commandString += " order by " + defaultSort;
 		}
 		commandString += ";";
 		return commandString;
 	}
 	
 	/**
 	 * Updates the entry in the given table for the given entity. All primary
 	 * keys must be included in the values array in order to figure out what
 	 * entity to update.
 	 * 
 	 * @param entityName The type of object (Player, Friend, etc.)
 	 * @param values The values to update the table with. Does not contain
 	 * any primary keys.
 	 * @param keys A list of the primary keys and their values
 	 * @return A system code describing the success or failure of the operation.
 	 */
 	public static SystemCode updateEntity(String entityName, Object[] values, String[] keys) {
 		String updateString = "update " + entityName + " set ";
 		for(int i = 0; i < values.length; i++) {
 			if ( i != 0 ) {
 				updateString += " , ";
 				
 				
 			}
 			updateString += " " + values[i];
 		}
 		if (keys.length > 0) {
 			updateString += " where ";
 			for(int i = 0; i < keys.length; i++) {
 				if (keys[i] != null) {
 					if ( i != 0 ) {
 						updateString += " and ";
 					}
 					updateString += keys[i];
 				}
 			}
 		}
 		updateString += ";";
 		PreparedStatement updateStatement = null;
 		try {
 			updateStatement = con.prepareStatement(updateString);
 			updateStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException ex) {
 			return handleInsertUpdateError(updateStatement.toString(), entityName, ex);
 		}
 	}
 	
 	/**
 	 * Deletes the entry in the given table for the given entity. All primary
 	 * keys must be included in the values array in order to figure out what
 	 * entity to delete.
 	 * 
 	 * @param entityName The type of object (Player, Friend, etc.)
 	 * @param primaryKeys The primary keys used to uniquely identify an object.
 	 * @param usingTables A list of tables that the entity uses, comma separated
 	 * @return A system code describing the success or failure of the operation.
 	 */
 	public static SystemCode deleteEntity (String entityName, Object[] primaryKeys, String usingTables) {
 		String deleteString = "delete from " + entityName;
 		if(usingTables.length() > 0) {
 			deleteString += " using ";
 			deleteString += usingTables;
 			deleteString += " ";
 		}
 		deleteString +=  " where ";
 		for(int i = 0; i < primaryKeys.length; i++) {
 			if ( i != 0 ) {
 				deleteString += " and ";
 			}
 			deleteString += primaryKeys[i];
 		}
 		deleteString += ";";
 		try {
 			PreparedStatement deleteStatement = con.prepareStatement(deleteString);
 			deleteStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException e) {
			e.printStackTrace();
 			return SystemCode.FAILURE;
 		}
 /*		
 		String deleteString = "delete from ? ";
 		if(usingTables.length() > 0) {
 			deleteString += "using ? ";
 		}
 		deleteString +=  "where";
 		for(int i = 0; i < primaryKeys.length; i++) {
 			if ( i != 0 ) {
 				deleteString += " and";
 			}
 			deleteString += " ?";
 		}
 		deleteString += ";";
 		try {
 			PreparedStatement deleteStatement = con.prepareStatement(deleteString);
 			deleteStatement.setObject(1, entityName);
 			int offset = 2;
 			/*
 			if(usingTables.length() > 0) {
 				deleteStatement.setString(2, usingTables);
 				offset = 3;
 			}
 			for(int i = 0; i < primaryKeys.length; i++) {
 				System.out.println(primaryKeys[i].toString());
 				deleteStatement.setObject(i+offset, primaryKeys[i]);
 			}
 			System.out.println(deleteStatement);
 			deleteStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return SystemCode.FAILURE;
 		} */
 	}
 
 	/**
 	 * Takes in a result set and parses it, returning it as an array
 	 * of an array of objects.
 	 * 
 	 * @param rs The result set to parse
 	 * @return  The resulting collection
 	 * @throws SQLException Throws the SQLException if issues arise while parsing the ResultSet,
 	 *         assumed that issues will be handled on the caller's side.
 	 */
 	public static Object[][] getObjectArray(ResultSet rs) throws SQLException {
 		String[][] stringsToReturn = null;
 		rs.last();
 		int rsRows = rs.getRow();
 		int rsCols = rs.getMetaData().getColumnCount();
 		stringsToReturn = new String[rsRows][rsCols];
 		rs.beforeFirst();
 		for (int currentRow = 0; currentRow < rsRows; currentRow++) {
 			rs.next();
 			for (int currentColumn = 1; currentColumn <= rsCols; currentColumn++) {
 				stringsToReturn[currentRow][currentColumn-1] = rs.getString(currentColumn); 
 			}
 		}
 		return stringsToReturn;
 	}
 	
 	/**
 	 * Returns whether a string starts with a vowel.
 	 * 
 	 * @param s The string to check
 	 * @return Whether it starts with a vowel
 	 */
 	private static boolean startsWithVowel(String s) {
 	 return (s.startsWith ("a") || 
 			 s.startsWith ("e") || 
 			 s.startsWith ("i") || 
 			 s.startsWith ("o") || 
 			 s.startsWith ("u") );
 	}
 
 }
