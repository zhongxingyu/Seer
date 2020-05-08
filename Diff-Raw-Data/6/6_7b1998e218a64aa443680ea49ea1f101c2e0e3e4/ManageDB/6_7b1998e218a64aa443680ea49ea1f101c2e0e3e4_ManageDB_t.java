 package edu.utsa.mobbed;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 //import java.security.MessageDigest;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 
 import org.postgresql.largeobject.LargeObjectManager;
 
 /**
  * Manages a mobbed database
  * 
  * @author Jeremy Cockfield
  * 
  */
 
 public class ManageDB {
 	private HashMap<String, String[]> columnMap;
 	private Connection connection;
 	private HashMap<String, String> defaultValues;
 	private HashMap<String, String[]> keyMap;
 	private HashMap<String, String> typeMap;
 	private boolean verbose;
 	private static final String columnQuery = "SELECT column_default, column_name, data_type from information_schema.columns where table_schema = 'public' AND table_name = ?";
 	private static final String keyQuery = "SELECT pg_attribute.attname FROM pg_index, pg_class, pg_attribute"
 			+ " WHERE pg_class.oid = ?::regclass AND"
 			+ " indrelid = pg_class.oid AND"
 			+ " pg_attribute.attrelid = pg_class.oid AND"
 			+ " pg_attribute.attnum = any(pg_index.indkey)"
 			+ " AND indisprimary";
 	private static final String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name";
 	private static final String templateName = "template1";
 	public static final String noParentUuid = "591df7dd-ce3e-47f8-bea5-6a632c6fcccb";
 
 	/**
 	 * Creates a ManageDB object to interact with database
 	 * 
 	 * @param name
 	 * @param hostname
 	 * @param user
 	 * @param password
 	 * @throws Exception
 	 */
 	public ManageDB(String dbname, String hostname, String username,
 			String password, boolean verbose) throws Exception {
 		connection = establishConnection(dbname, hostname, username, password);
 		this.verbose = verbose;
 		setAutoCommit(false);
 		initializeHashMaps();
 	}
 
 	/**
 	 * Inserts rows into a database
 	 * 
 	 * @param tableName
 	 * @param columnNames
 	 * @param columnValues
 	 * @return
 	 * @throws Exception
 	 */
 	public String[] addRows(String tableName, String[] columnNames,
 			String[][] columnValues, String[] doubleColumnNames,
 			Double[][] doubleValues) throws Exception {
 		validateTableName(tableName);
 		validateColumnNames(columnNames);
 		int numRows = columnValues.length;
 		int numValues = columnValues[0].length;
 		int doubleIndex = 0;
 		Double[] currentDoubleValues;
 		String[] keyList = new String[numRows];
 		ArrayList<Integer> keyIndexes = findKeyIndexes(tableName, columnNames);
 		try {
 			String insertQry = constructInsertQuery(tableName, columnNames);
 			String updateQry = constructUpdateQuery(keyIndexes, tableName,
 					columnNames);
 			PreparedStatement insertStmt = connection
 					.prepareStatement(insertQry);
 			PreparedStatement updateStmt = connection
 					.prepareStatement(updateQry);
 			for (int i = 0; i < numRows; i++) {
 				for (int j = 0; j < numValues; j++) {
 					if (!isEmpty(columnValues[i][j]))
 						validateColumnValue(columnNames[j], columnValues[i][j]);
 					else {
 						if (!keyIndexes.contains(j)
 								&& !columnNames[j]
 										.equals("dataset_session_uuid"))
 							columnValues[i][j] = getDefaultValue(columnNames[j]);
 					}
 				}
 				if (!isEmpty(doubleValues))
 					currentDoubleValues = doubleValues[doubleIndex++];
 				else
 					currentDoubleValues = null;
 				if (keysExist(keyIndexes, tableName, columnNames,
 						columnValues[i])) {
 					setUpdateStatementValues(keyIndexes, updateStmt,
 							columnNames, columnValues[i], currentDoubleValues);
 					if (verbose)
 						System.out.println(updateStmt);
 					updateStmt.addBatch();
 				} else {
 					columnValues[i] = generateKeys(keyIndexes, tableName,
 							columnNames, columnValues[i]);
 					setInsertStatementValues(insertStmt, columnNames,
 							columnValues[i], currentDoubleValues);
 					if (verbose)
 						System.out.println(insertStmt);
 					insertStmt.addBatch();
 
 				}
 				keyList[i] = addKeyValue(keyIndexes, columnValues[i]);
 			}
 			insertStmt.executeBatch();
 			updateStmt.executeBatch();
 		} catch (SQLException me) {
 			throw new MobbedException("Could not add row(s)\n"
 					+ me.getNextException().getMessage());
 		}
 		return keyList;
 	}
 
 	/**
 	 * Checks the dataset's namespace and name to determine it's version number
 	 * 
 	 * @param IsUnique
 	 * @param datasetName
 	 * @param datasetNamespace
 	 * @return
 	 * @throws Exception
 	 */
 	public int checkDatasetVersion(boolean IsUnique, String datasetNamespace,
 			String datasetName) throws Exception {
 		String query = "SELECT MAX(DATASET_VERSION) AS LATESTVERSION"
 				+ " FROM DATASETS WHERE DATASET_NAMESPACE = ? AND DATASET_NAME = ?";
 		PreparedStatement selStmt = connection.prepareStatement(query);
 		selStmt.setString(1, datasetNamespace);
 		selStmt.setString(2, datasetName);
 		ResultSet rs = selStmt.executeQuery();
 		rs.next();
 		int version = rs.getInt(1);
 		if (IsUnique && version > 0)
 			throw new MobbedException("dataset version is not unique");
 		return version + 1;
 	}
 
 	/**
 	 * Closes a database connection
 	 * 
 	 * @throws Exception
 	 */
 	public void close() throws Exception {
 		try {
 			connection.close();
 		} catch (Exception me) {
 			throw new MobbedException("Could not close connection\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Commits all database transactions
 	 * 
 	 * @throws Exception
 	 */
 	public void commit() throws Exception {
 		try {
 			connection.commit();
 		} catch (Exception me) {
 			throw new MobbedException("Could not commit transaction(s)\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Extracts inter-related rows
 	 * 
 	 * @param inTableName
 	 * @param inColumnNames
 	 * @param inColumnValues
 	 * @param outTableName
 	 * @param outColumnNames
 	 * @param outColumnValues
 	 * @param limit
 	 * @param regExp
 	 * @param lower
 	 * @param upper
 	 * @return
 	 * @throws Exception
 	 */
 	public String[][] extractRows(String inTableName, String[] inColumnNames,
 			String[][] inColumnValues, String outTableName,
 			String[] outColumnNames, String[][] outColumnValues, double limit,
 			String regExp, double lower, double upper) throws Exception {
 		String qry = "SELECT * FROM extractRange(?,?,?,?) as (";
 		String[] columns = getColumnNames(inTableName);
 		for (int i = 0; i < columns.length; i++)
 			qry += columns[i] + " " + typeMap.get(columns[i]) + ",";
 		qry += "extracted uuid[]) ORDER BY EVENT_ENTITY_UUID, EVENT_START_TIME";
 		if (limit != Double.POSITIVE_INFINITY)
 			qry += " LIMIT " + limit;
 		String inQry = "SELECT * FROM " + inTableName;
 		if (inColumnNames != null) {
 			inQry += constructQualificationQuery(inTableName, regExp, null,
 					null, inColumnNames, inColumnValues);
 			PreparedStatement inStmt = connection.prepareStatement(inQry);
 			setStructureStatementValues(inStmt, 1, inColumnNames,
 					inColumnValues);
 			inQry = inStmt.toString();
 		}
 		String outQry = "SELECT * FROM " + inTableName;
 		if (outColumnNames != null) {
 			outQry += constructQualificationQuery(outTableName, regExp, null,
 					null, outColumnNames, outColumnValues);
 			PreparedStatement outStmt = connection.prepareStatement(outQry);
 			setStructureStatementValues(outStmt, 1, outColumnNames,
 					outColumnValues);
 			outQry = outStmt.toString();
 		}
 		PreparedStatement pstmt = connection.prepareStatement(qry,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		pstmt.setString(1, inQry);
 		pstmt.setString(2, outQry);
 		pstmt.setDouble(3, lower);
 		pstmt.setDouble(4, upper);
 		ResultSet rs = pstmt.executeQuery();
 		String[][] rows = populateArray(rs);
 		return rows;
 	}
 
 	/**
 	 * Extracts unique inter-related rows
 	 * 
 	 * @param extractedRows
 	 * @param limit
 	 * @return
 	 * @throws Exception
 	 */
 	public String[][] extractUniqueRows(String[][] extractedRows, int limit)
 			throws Exception {
 		int extractedColumn = extractedRows[0].length - 1;
 		int numRows = extractedRows.length;
 		String[] extractedUuids = null;
 		ArrayList<String> alist = new ArrayList<String>();
 		for (int i = 0; i < numRows; i++) {
 			extractedUuids = extractedRows[i][extractedColumn].replaceAll(
 					"[{}]", "").split(",");
 			for (int j = 0; j < extractedUuids.length; j++) {
 				if (!alist.contains(extractedUuids[j]))
 					alist.add("'" + extractedUuids[j] + "'");
 			}
 		}
 		String list = alist.toString().replaceAll("[\\[\\]]", "");
 		String qry = "SELECT * FROM EVENTS WHERE EVENT_UUID IN (" + list + ")";
 		if (limit > 0)
 			qry += " LIMIT " + limit;
 		Statement stmt = connection.createStatement(
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet rs = stmt.executeQuery(qry);
 		String[][] rows = populateArray(rs);
 		return rows;
 	}
 
 	/**
 	 * Gets the column names of a table
 	 * 
 	 * @param table
 	 * @return
 	 */
 	public String[] getColumnNames(String table) {
 		return columnMap.get(table);
 	}
 
 	/**
 	 * Gets the column type of a column
 	 * 
 	 * @param columnName
 	 * @return
 	 */
 	public String getColumnType(String columnName) {
 		return typeMap.get(columnName);
 	}
 
 	/**
 	 * Gets all of the columns types of a given table
 	 * 
 	 * @param table
 	 * @return
 	 */
 	public String[] getColumnTypes(String tableName) {
 		String[] columnNames = getColumnNames(tableName);
 		int numColumns = columnNames.length;
 		String[] columnTypes = new String[numColumns];
 		for (int i = 0; i < numColumns; i++)
 			columnTypes[i] = getColumnType(columnNames[i]);
 		return columnTypes;
 	}
 
 	/**
 	 * Gets a database connection
 	 * 
 	 * @return
 	 */
 	public Connection getConnection() {
 		return connection;
 	}
 
 	/**
 	 * Gets the default value of a column
 	 * 
 	 * @param columnName
 	 * @return
 	 */
 	public String getDefaultValue(String columnName) {
 		return defaultValues.get(columnName);
 	}
 
 	/**
 	 * Gets the columns that are double precision of a table
 	 * 
 	 * @param tableName
 	 * @return
 	 */
 	public String[] getDoubleColumns(String tableName) {
 		ArrayList<String> al = new ArrayList<String>();
 		String[] columns = columnMap.get(tableName);
 		int numColumns = columns.length;
 		for (int i = 0; i < numColumns; i++) {
 			if (typeMap.get(columns[i]).equalsIgnoreCase("double precision"))
 				al.add(columns[i]);
 		}
 		String[] doubleColumns = al.toArray(new String[al.size()]);
 		return doubleColumns;
 	}
 
 	/**
 	 * Gets the keys of a table
 	 * 
 	 * @param table
 	 * @return
 	 */
 	public String[] getKeys(String table) {
 		return keyMap.get(table);
 	}
 
 	/**
 	 * Gets all of the tables from a database
 	 * 
 	 * @return
 	 */
 	public String[] getTables() {
 		Set<String> keySet = columnMap.keySet();
 		String[] tables = keySet.toArray(new String[keySet.size()]);
 		return tables;
 	}
 
 	/**
 	 * Retrieves rows from a table by search criteria
 	 * 
 	 * @param tableName
 	 * @param limit
 	 * @param regExp
 	 * @param tags
 	 * @param attributes
 	 * @param columnNames
 	 * @param columnValues
 	 * @return
 	 * @throws Exception
 	 */
 	public String[][] retrieveRows(String tableName, double limit,
 			String regExp, String[][] tags, String[][] attributes,
 			String[] columnNames, String[][] columnValues) throws Exception {
 		validateTableName(tableName);
 		String qry = "SELECT * FROM " + tableName;
 		qry += constructQualificationQuery(tableName, regExp, tags, attributes,
 				columnNames, columnValues);
 		if (limit != Double.POSITIVE_INFINITY)
 			qry += " LIMIT " + limit;
 		PreparedStatement pstmt = connection.prepareStatement(qry,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		setQaulificationValues(pstmt, qry, tags, attributes, columnNames,
 				columnValues);
 		if (verbose)
 			System.out.println(pstmt);
 		ResultSet rs = pstmt.executeQuery();
 		String[][] rows = populateArray(rs);
 		return rows;
 	}
 
 	/**
 	 * Rollback all transactions
 	 * 
 	 * @throws Exception
 	 */
 	public void rollback() throws Exception {
 		try {
 			connection.rollback();
 		} catch (Exception me) {
 			throw new MobbedException("Could not rollback transactions\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Sets the auto commit mode
 	 * 
 	 * @param autoCommit
 	 * @throws Exception
 	 */
 	public void setAutoCommit(boolean autoCommit) throws Exception {
 		try {
 			connection.setAutoCommit(autoCommit);
 		} catch (Exception me) {
 			throw new MobbedException("Could not set auto commit mode\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Adds elements to String array by index
 	 * 
 	 * @param keyIndexes
 	 * @param array
 	 * @return
 	 */
 	private String[] addByIndex(ArrayList<Integer> keyIndexes, String[] array) {
 		// Used to get key column names and values
 		String[] newArray = new String[keyIndexes.size()];
 		int j = 0;
 		for (int i = 0; i < keyIndexes.size(); i++) {
 			if (keyIndexes.contains(i)) {
 				newArray[j] = array[i];
 				j++;
 			}
 		}
 		return newArray;
 	}
 
 	/**
 	 * Adds a key value to array
 	 * 
 	 * @param keyIndexes
 	 * @param columnValues
 	 * @return
 	 */
 	private String addKeyValue(ArrayList<Integer> keyIndexes,
 			String[] columnValues) {
 		String keyValue = null;
 		if (keyIndexes.size() == 1)
 			keyValue = columnValues[keyIndexes.get(0)];
 		else {
 			String concatKeys = columnValues[keyIndexes.get(0)];
 			for (int i = 1; i < keyIndexes.size(); i++)
 				concatKeys += "," + columnValues[keyIndexes.get(i)];
 			keyValue = concatKeys;
 		}
 		return keyValue;
 	}
 
 	/**
 	 * Constructs a insert query
 	 * 
 	 * @param tableName
 	 * @param columnNames
 	 * @return
 	 */
 	private String constructInsertQuery(String tableName, String[] columnNames) {
 		String qry = "INSERT INTO " + tableName;
 		qry += " (" + columnNames[0];
 		for (int i = 1; i < columnNames.length; i++)
 			qry += ", " + columnNames[i];
 		qry += ")";
 		qry += " VALUES (?";
 		for (int j = 1; j < columnNames.length; j++)
 			qry += ", ?";
 		qry += ")";
 		return qry;
 	}
 
 	/**
 	 * Constructs a query based on search criteria
 	 * 
 	 * @param tableName
 	 * @param limit
 	 * @param regExp
 	 * @param tags
 	 * @param attributes
 	 * @param columnNames
 	 * @param columnValues
 	 * @return
 	 * @throws Exception
 	 */
 	private String constructQualificationQuery(String tableName, String regExp,
 			String[][] tags, String[][] attributes, String[] columnNames,
 			String[][] columnValues) throws Exception {
 		String qry = "";
 		String closer = "";
 		String[] keys = keyMap.get(tableName);
 		if (tags != null || attributes != null) {
 			qry = " WHERE " + keys[0] + " IN (";
 			closer = ")";
 		}
 		if (tags != null)
 			qry += constructTagAttributesQuery(regExp, "Tags", tags);
 		if (attributes != null) {
 			if (tags != null)
 				qry += " INTERSECT ";
 			qry += constructTagAttributesQuery(regExp, "Attributes", attributes);
 		}
 		if (columnNames != null) {
 			if (tags != null || attributes != null)
 				qry += " INTERSECT SELECT " + keys[0] + " FROM " + tableName
 						+ " WHERE ";
 			else
 				qry += " WHERE ";
 			qry += constructStructQuery(regExp, tableName, columnNames,
 					columnValues);
 		}
 		qry += closer;
 		return qry;
 	}
 
 	/**
 	 * Constructs a select query
 	 * 
 	 * @param keyIndexes
 	 * @param tableName
 	 * @param columnNames
 	 * @return
 	 */
 	private String constructSelectQuery(ArrayList<Integer> keyIndexes,
 			String tableName, String[] columnNames) {
 		String[] keyColumns = addByIndex(keyIndexes, columnNames);
 		String qry = "SELECT * FROM " + tableName + " WHERE " + keyColumns[0]
 				+ " = ?";
 		for (int i = 1; i < keyColumns.length; i++) {
 			qry += " AND " + keyColumns[i] + " = ?";
 		}
 		return qry;
 	}
 
 	/**
 	 * Constructs a query from a structure array
 	 * 
 	 * @param regExp
 	 * @param tableName
 	 * @param columnNames
 	 * @param columnValues
 	 * @return
 	 * @throws Exception
 	 */
 	private String constructStructQuery(String regExp, String tableName,
 			String[] columnNames, String[][] columnValues) throws Exception {
		String type;
		String columnName;
 		String qry = "";
 		int numColumns = columnNames.length;
 		for (int i = 0; i < numColumns; i++) {
			type = typeMap.get(columnNames[i]);
 			// Case insensitive fix
 			if (type.equalsIgnoreCase("character varying"))
 				columnName = " UPPER(" + columnNames[i] + ")";
 			else
 				columnName = columnNames[i];
 			int numValues = columnValues[i].length;
 			// With regexp (only works for strings)
 			if (type.equalsIgnoreCase("character varying")
 					&& regExp.equalsIgnoreCase("on")) {
 				qry += columnName + " ~* ?";
 				for (int j = 1; j < numValues; j++)
 					qry += " OR " + columnName + " ~* ?";
 			}
 			// Without regexp (works for everything)
 			else {
 				qry += columnName + " IN (?";
 				for (int j = 1; j < numValues; j++)
 					qry += ",?";
 				qry += ")";
 			}
 			if (i != numColumns - 1)
 				qry += " AND ";
 		}
 		return qry;
 	}
 
 	/**
 	 * Constructs a query from tags or attributes
 	 * 
 	 * @param regExp
 	 * @param qualification
 	 * @param values
 	 * @return
 	 * @throws Exception
 	 */
 	private String constructTagAttributesQuery(String regExp,
 			String qualification, String[][] values) throws Exception {
 		String selectqry = "";
 		String columnName = "";
 		if (qualification.equalsIgnoreCase("Tags")) {
 			selectqry = " SELECT TAG_ENTITY_UUID FROM TAGS"
 					+ " WHERE UPPER(TAG_NAME)";
 			columnName = "UPPER(TAG_NAME)";
 		} else {
 			selectqry = " SELECT ATTRIBUTE_ORGANIZATIONAL_UUID"
 					+ " FROM attributes WHERE UPPER(ATTRIBUTE_VALUE)";
 			columnName = "UPPER(ATTRIBUTE_VALUE)";
 		}
 		String qry = selectqry;
 		int groups = values.length;
 		for (int i = 0; i < groups; i++) {
 			int numValues = values[i].length;
 			if (regExp.equalsIgnoreCase("on")) {
 				qry += " ~* ?";
 				for (int j = 1; j < numValues; j++)
 					qry += " OR " + columnName + " ~* ?";
 			}
 			// Without regexp (works for everything)
 			else {
 				qry += " IN (?";
 				for (int j = 1; j < numValues; j++)
 					qry += ",?";
 				qry += ")";
 			}
 			if (i != groups - 1)
 				qry += " INTERSECT " + selectqry;
 		}
 		return qry;
 	}
 
 	/**
 	 * Constructs a update query
 	 * 
 	 * @param keyIndexes
 	 * @param tableName
 	 * @param columnNames
 	 * @return
 	 */
 	private String constructUpdateQuery(ArrayList<Integer> keyIndexes,
 			String tableName, String[] columnNames) {
 		String[] nonKeyColumns = removeByIndex(keyIndexes, columnNames);
 		String[] keyColumns = addByIndex(keyIndexes, columnNames);
 		String qry = "UPDATE " + tableName + " SET " + nonKeyColumns[0]
 				+ " = ?";
 		for (int i = 1; i < nonKeyColumns.length; i++)
 			qry += " , " + nonKeyColumns[i] + " = ?";
 		qry += " WHERE " + keyColumns[0] + " = ?";
 		for (int j = 1; j < keyColumns.length; j++)
 			qry += " AND " + keyColumns[j] + " = ?";
 		return qry;
 	}
 
 	/**
 	 * Finds the indexes that contain key columns
 	 * 
 	 * @param tableName
 	 * @param columnNames
 	 * @return
 	 */
 	private ArrayList<Integer> findKeyIndexes(String tableName,
 			String[] columnNames) {
 		String[] keys = keyMap.get(tableName);
 		ArrayList<Integer> keyIndexes = new ArrayList<Integer>();
 		for (int i = 0; i < columnNames.length; i++) {
 			for (int j = 0; j < keys.length; j++)
 				if (columnNames[i].equalsIgnoreCase(keys[j]))
 					keyIndexes.add(i);
 		}
 		return keyIndexes;
 	}
 
 	/**
 	 * Generates key column values
 	 * 
 	 * @param keyIndexes
 	 * @param columnValues
 	 * @return
 	 */
 	private String[] generateKeys(ArrayList<Integer> keyIndexes,
 			String tableName, String[] columnNames, String[] columnValues)
 			throws Exception {
 		if (tableName.equalsIgnoreCase("datasets")) {
 			int sessionIndex = findIndexOfColumn(columnNames,
 					"dataset_session_uuid");
 			columnValues[sessionIndex] = UUID.randomUUID().toString();
 		}
 		int numKeys = keyIndexes.size();
 		for (int i = 0; i < numKeys; i++) {
 			if (isEmpty(columnValues[keyIndexes.get(i)]))
 				columnValues[keyIndexes.get(i)] = UUID.randomUUID().toString();
 		}
 		return columnValues;
 	}
 
 	// /**
 	// * Generates a md5 hash
 	// *
 	// * @param value
 	// * @return
 	// * @throws Exception
 	// */
 	// private String generateMd5Hash(String value) throws Exception {
 	// String md5hash = "";
 	// byte[] dataBytes = value.getBytes();
 	// MessageDigest md = MessageDigest.getInstance("MD5");
 	// md.update(dataBytes);
 	// byte[] md5bytes = md.digest();
 	// StringBuffer hexString = new StringBuffer();
 	// for (int i = 0; i < md5bytes.length; i++) {
 	// hexString.append(Integer.toHexString(0xFF & md5bytes[i]));
 	// }
 	// md5hash = hexString.toString();
 	// return md5hash;
 	// }
 
 	/**
 	 * Finds the index of a particular column
 	 * 
 	 * @param columnNames
 	 * @param columnName
 	 * @return
 	 */
 	private int findIndexOfColumn(String[] columnNames, String columnName) {
 		int index = 0;
 		int numColumns = columnNames.length;
 		for (int i = 0; i < numColumns; i++) {
 			if (columnNames[i].equalsIgnoreCase(columnName))
 				index = i;
 		}
 		return index;
 	}
 
 	/**
 	 * Initializes hash maps that involve columns
 	 * 
 	 * @param columnStatement
 	 * @param tableName
 	 * @throws Exception
 	 */
 	private void initializeColumns(PreparedStatement columnStatement,
 			String tableName) throws Exception {
 		columnStatement.setString(1, tableName);
 		ResultSet rs = columnStatement.executeQuery();
 		ArrayList<String> columnNameList = new ArrayList<String>();
 		String columnDefault = null;
 		String columnName = null;
 		String columnType = null;
 		while (rs.next()) {
 			columnDefault = rs.getString(1);
 			if (columnDefault != null)
 				columnDefault = columnDefault.split(":")[0].replaceAll("'", "");
 			columnName = rs.getString(2);
 			columnType = rs.getString(3);
 			defaultValues.put(columnName, columnDefault);
 			typeMap.put(columnName, columnType);
 			columnNameList.add(columnName);
 		}
 		String[] columnNames = columnNameList.toArray(new String[columnNameList
 				.size()]);
 		columnMap.put(tableName, columnNames);
 	}
 
 	/**
 	 * Initializes hash map fields
 	 * 
 	 * @throws Exception
 	 */
 	private void initializeHashMaps() throws Exception {
 		columnMap = new HashMap<String, String[]>();
 		typeMap = new HashMap<String, String>();
 		defaultValues = new HashMap<String, String>();
 		keyMap = new HashMap<String, String[]>();
 		Statement tableStatement = connection.createStatement();
 		PreparedStatement columnStatement = connection.prepareCall(columnQuery);
 		PreparedStatement keyStatement = connection.prepareCall(keyQuery);
 		ResultSet rs = tableStatement.executeQuery(tableQuery);
 		while (rs.next()) {
 			initializeColumns(columnStatement, rs.getString("table_name"));
 			initializeKeys(keyStatement, rs.getString("table_name"));
 		}
 	}
 
 	/**
 	 * Initialize hash map for key columns
 	 * 
 	 * @param keyStatement
 	 * @param tableName
 	 * @throws Exception
 	 */
 	private void initializeKeys(PreparedStatement keyStatement, String tableName)
 			throws Exception {
 		keyStatement.setString(1, tableName);
 		ResultSet rs = keyStatement.executeQuery();
 		ArrayList<String> keyColumnList = new ArrayList<String>();
 		while (rs.next()) {
 			keyColumnList.add(rs.getString(1));
 		}
 		String[] keyColumns = keyColumnList.toArray(new String[keyColumnList
 				.size()]);
 		keyMap.put(tableName, keyColumns);
 	}
 
 	/**
 	 * Checks if key columns are empty
 	 * 
 	 * @param keyIndexes
 	 * @param columnValues
 	 * @return
 	 * @throws Exception
 	 */
 	private boolean keysEmpty(ArrayList<Integer> keyIndexes,
 			String[] columnValues) throws Exception {
 		boolean empty = true;
 		int keyCount = 0;
 		for (int i = 0; i < keyIndexes.size(); i++) {
 			if (!isEmpty(columnValues[keyIndexes.get(i)]))
 				keyCount++;
 		}
 		if (keyCount > 0 && keyIndexes.size() > keyCount)
 			throw new MobbedException("Composite key is missing column(s)");
 		else if (keyCount == keyIndexes.size())
 			empty = false;
 		return empty;
 	}
 
 	/**
 	 * Checks if the given keys exist in the database
 	 * 
 	 * @param keyIndexes
 	 * @param tableName
 	 * @param columnNames
 	 * @param columnValues
 	 * @return
 	 * @throws Exception
 	 */
 	private boolean keysExist(ArrayList<Integer> keyIndexes, String tableName,
 			String[] columnNames, String[] columnValues) throws Exception {
 		boolean exist = false;
 		if (!keysEmpty(keyIndexes, columnValues)) {
 			String[] keyColumns = addByIndex(keyIndexes, columnNames);
 			String[] keyValues = addByIndex(keyIndexes, columnValues);
 			String selectQuery = constructSelectQuery(keyIndexes, tableName,
 					columnNames);
 			PreparedStatement pstmt = connection.prepareStatement(selectQuery);
 			setSelectStatementValues(pstmt, keyColumns, keyValues);
 			ResultSet rs = pstmt.executeQuery();
 			if (rs.next())
 				exist = true;
 		}
 		return exist;
 	}
 
 	/**
 	 * Looks up the jdbc sql types
 	 * 
 	 * @param columnName
 	 * @return
 	 */
 	private int lookupTargetType(Object columnName) {
 		// 1.6 doesn't support switch statement with string object
 		String type = typeMap.get(columnName);
 		int targetType = Types.NULL;
 		if (type.equalsIgnoreCase("uuid")) {
 			targetType = Types.OTHER;
 		} else if (type.equalsIgnoreCase("character varying")) {
 			targetType = Types.VARCHAR;
 		} else if (type.equalsIgnoreCase("ARRAY")) {
 			targetType = Types.ARRAY;
 		} else if (type.equalsIgnoreCase("integer")) {
 			targetType = Types.INTEGER;
 		} else if (type.equalsIgnoreCase("bigint")) {
 			targetType = Types.BIGINT;
 		} else if (type.equalsIgnoreCase("double precision")) {
 			targetType = Types.DOUBLE;
 		} else if (type.equalsIgnoreCase("timestamp without time zone")) {
 			targetType = Types.OTHER;
 		} else if (type.equalsIgnoreCase("oid")) {
 			targetType = Types.INTEGER;
 		}
 		return targetType;
 	}
 
 	/**
 	 * Populates an array with a result set
 	 * 
 	 * @param rs
 	 * @return
 	 * @throws Exception
 	 */
 	private String[][] populateArray(ResultSet rs) throws Exception {
 		ResultSetMetaData rsMeta = rs.getMetaData();
 		rs.last();
 		int rowCount = rs.getRow();
 		int colCount = rsMeta.getColumnCount();
 		String[][] allocatedArray = new String[rowCount][colCount];
 		rs.beforeFirst();
 		for (int i = 0; i < rowCount; i++) {
 			rs.next();
 			for (int j = 0; j < colCount; j++)
 				allocatedArray[i][j] = rs.getString(j + 1);
 		}
 		return allocatedArray;
 	}
 
 	/**
 	 * Creates an array of non-index elements
 	 * 
 	 * @param keyIndexes
 	 * @param array
 	 * @return
 	 */
 	private String[] removeByIndex(ArrayList<Integer> keyIndexes, String[] array) {
 		// Used to get non-key column names and values
 		String[] newArray = new String[array.length - keyIndexes.size()];
 		int j = 0;
 		for (int i = 0; i < array.length; i++) {
 			if (!keyIndexes.contains(i)) {
 				newArray[j] = array[i];
 				j++;
 			}
 		}
 		return newArray;
 	}
 
 	/**
 	 * Set values for insert prepared statement
 	 * 
 	 * @param pstmt
 	 * @param columnNames
 	 * @param columnValues
 	 * @throws SQLException
 	 */
 	private void setInsertStatementValues(PreparedStatement pstmt,
 			String[] columnNames, String[] columnValues, Double[] doubleValues)
 			throws SQLException {
 		int numColumns = columnNames.length;
 		int i = 0;
 		int j = 0;
 		for (int k = 0; k < numColumns; k++) {
 			int targetType = lookupTargetType(columnNames[k]);
 			if (doubleValues != null && targetType == Types.DOUBLE) {
 				if (doubleValues[i] != null)
 					pstmt.setDouble(k + 1, doubleValues[i]);
 				else
 					pstmt.setObject(k + 1, doubleValues[i]);
 				i++;
 			} else {
 				pstmt.setObject(k + 1, columnValues[j], targetType);
 				j++;
 			}
 		}
 	}
 
 	/**
 	 * Sets the values of a query constructed by search criteria
 	 * 
 	 * @param pstmt
 	 * @param qry
 	 * @param tags
 	 * @param attributes
 	 * @param columnNames
 	 * @param columnValues
 	 * @throws Exception
 	 */
 	private void setQaulificationValues(PreparedStatement pstmt, String qry,
 			String[][] tags, String[][] attributes, String[] columnNames,
 			String[][] columnValues) throws Exception {
 		int valueCount = 1;
 		if (tags != null)
 			valueCount = setTagAttributesStatementValues(pstmt, valueCount,
 					tags);
 		if (attributes != null)
 			valueCount = setTagAttributesStatementValues(pstmt, valueCount,
 					attributes);
 		if (columnNames != null)
 			valueCount = setStructureStatementValues(pstmt, valueCount,
 					columnNames, columnValues);
 	}
 
 	/**
 	 * Set values for select prepared statement
 	 * 
 	 * @param pstmt
 	 * @param columnNames
 	 * @param columnValues
 	 * @throws SQLException
 	 */
 	private void setSelectStatementValues(PreparedStatement pstmt,
 			String[] columnNames, String[] columnValues) throws SQLException {
 		for (int i = 0; i < columnValues.length; i++) {
 			int targetType = lookupTargetType(columnNames[i]);
 			pstmt.setObject(i + 1, columnValues[i], targetType);
 		}
 	}
 
 	/**
 	 * Sets structure array query values
 	 * 
 	 * @param pstmt
 	 * @param valueCount
 	 * @param columnNames
 	 * @param columnValues
 	 * @return
 	 * @throws SQLException
 	 */
 	private int setStructureStatementValues(PreparedStatement pstmt,
 			int valueCount, String[] columnNames, String[][] columnValues)
 			throws SQLException {
 		int numColumns = columnNames.length;
 		int numValues = 0;
 		int targetType = 0;
 		for (int i = 0; i < numColumns; i++) {
 			numValues = columnValues[i].length;
 			for (int j = 0; j < numValues; j++) {
 				targetType = lookupTargetType(columnNames[i]);
 				// Case insensitive fix
 				if (targetType == Types.VARCHAR)
 					columnValues[i][j] = columnValues[i][j].toUpperCase();
 				pstmt.setObject(valueCount, columnValues[i][j], targetType);
 				valueCount++;
 			}
 		}
 		return valueCount;
 	}
 
 	/**
 	 * Sets tag or attribute query values
 	 * 
 	 * @param pstmt
 	 * @param valueCount
 	 * @param values
 	 * @return
 	 * @throws SQLException
 	 */
 	private int setTagAttributesStatementValues(PreparedStatement pstmt,
 			int valueCount, String[][] values) throws SQLException {
 		int numGroups = values.length;
 		for (int i = 0; i < numGroups; i++) {
 			int numValues = values[i].length;
 			for (int j = 0; j < numValues; j++) {
 				values[i][j] = values[i][j].toUpperCase();
 				pstmt.setObject(valueCount, values[i][j], Types.VARCHAR);
 				valueCount++;
 			}
 		}
 		return valueCount;
 	}
 
 	/**
 	 * Set values for update prepared statement
 	 * 
 	 * @param keyIndexes
 	 * @param pstmt
 	 * @param columnNames
 	 * @param columnValues
 	 * @throws SQLException
 	 */
 	private void setUpdateStatementValues(ArrayList<Integer> keyIndexes,
 			PreparedStatement pstmt, String[] columnNames,
 			String[] columnValues, Double[] doubleValues) throws SQLException {
 		String[] keyColumns = addByIndex(keyIndexes, columnNames);
 		String[] nonKeyColumns = removeByIndex(keyIndexes, columnNames);
 		String[] keyValues = addByIndex(keyIndexes, columnValues);
 		String[] nonKeyValues = removeByIndex(keyIndexes, columnValues);
 		int i;
 		int k = 0;
 		int l = 0;
 		for (i = 0; i < nonKeyColumns.length; i++) {
 			int targetType = lookupTargetType(nonKeyColumns[i]);
 			if (doubleValues != null && targetType == Types.DOUBLE) {
 				if (doubleValues[k] != null)
 					pstmt.setDouble(i + 1, doubleValues[k]);
 				else
 					pstmt.setObject(i + 1, doubleValues[k]);
 				k++;
 			} else {
 				pstmt.setObject(i + 1, nonKeyValues[l], targetType);
 				l++;
 			}
 		}
 		for (int j = 0; j < keyColumns.length; j++) {
 			int targetType = lookupTargetType(keyColumns[j]);
 			pstmt.setObject(i + j + 1, keyValues[j], targetType);
 		}
 	}
 
 	/**
 	 * Validates the column names
 	 * 
 	 * @param columnNames
 	 * @return
 	 * @throws Exception
 	 */
 	private boolean validateColumnNames(String[] columnNames) throws Exception {
 		int numColumns = columnNames.length;
 		for (int i = 0; i < numColumns; i++) {
 			if (getColumnType(columnNames[i]) == null)
 				throw new MobbedException("column " + columnNames[i]
 						+ " is an invalid column type");
 		}
 		return true;
 	}
 
 	/**
 	 * Validates a given column value
 	 * 
 	 * @param columnName
 	 * @param columnValue
 	 * @return
 	 * @throws Exception
 	 */
 	private boolean validateColumnValue(String columnName, String columnValue)
 			throws Exception {
 		String type = typeMap.get(columnName);
 		try {
 			if (type.equalsIgnoreCase("uuid")) {
 				UUID.fromString(columnValue);
 			} else if (type.equalsIgnoreCase("integer")) {
 				Integer.parseInt(columnValue);
 			} else if (type.equalsIgnoreCase("bigint")) {
 				Long.parseLong(columnValue);
 			} else if (type.equalsIgnoreCase("timestamp without time zone")) {
 				Timestamp.valueOf(columnValue);
 			}
 		} catch (Exception me) {
 			throw new MobbedException("Invalid type, column: " + columnName
 					+ " value: " + columnValue);
 		}
 		return true;
 	}
 
 	/**
 	 * Validates a table name
 	 * 
 	 * @param tableName
 	 * @return
 	 * @throws Exception
 	 */
 	private boolean validateTableName(String tableName) throws Exception {
 		if (getColumnNames(tableName) == null)
 			throw new MobbedException("table " + tableName
 					+ " is an invalid table");
 		return true;
 	}
 
 	/**
 	 * Checks for active connections
 	 * 
 	 * @param dbCon
 	 * @throws Exception
 	 */
 	public static void checkForActiveConnections(Connection dbCon)
 			throws Exception {
 		Statement stmt = dbCon.createStatement();
 		String qry = "SELECT count(pid) from pg_stat_activity WHERE datname = current_database() AND pid <> pg_backend_pid()";
 		ResultSet rs = stmt.executeQuery(qry);
 		rs.next();
 		int otherConnections = rs.getInt(1);
 		if (otherConnections > 0) {
 			dbCon.close();
 			throw new MobbedException(
 					"Close all connections before dropping the database");
 		}
 	}
 
 	/**
 	 * Creates credentials that are stored in a property file
 	 * 
 	 * @param filename
 	 * @param dbname
 	 * @param hostname
 	 * @param username
 	 * @param password
 	 * @throws Exception
 	 */
 	public static void createCredentials(String filename, String dbname,
 			String hostname, String username, String password) throws Exception {
 		Properties prop = new Properties();
 		try {
 			prop.setProperty("dbname", dbname);
 			prop.setProperty("hostname", hostname);
 			prop.setProperty("username", username);
 			prop.setProperty("password", password);
 			prop.store(new FileOutputStream(filename), null);
 		} catch (Exception me) {
 			throw new MobbedException("Could not create credentials");
 		}
 
 	}
 
 	/**
 	 * Creates and sets up a database
 	 * 
 	 * @param name
 	 * @param hostname
 	 * @param user
 	 * @param password
 	 * @param tablePath
 	 * @param verbose
 	 * @throws Exception
 	 */
 	public static void createDatabase(String name, String hostname,
 			String user, String password, String filename, boolean verbose)
 			throws Exception {
 		if (isEmpty(filename))
 			throw new MobbedException("SQL script does not exist");
 		Connection templateConnection = establishConnection(templateName,
 				hostname, user, password);
 		createDatabase(templateConnection, name);
 		templateConnection.close();
 		Connection databaseConnection = establishConnection(name, hostname,
 				user, password);
 		createTables(databaseConnection, filename);
 		databaseConnection.close();
 		if (verbose)
 			System.out.println("Database " + name + " created");
 	}
 
 	/**
 	 * Deletes a database
 	 * 
 	 * @param name
 	 * @param hostname
 	 * @param user
 	 * @param password
 	 * @param verbose
 	 * @throws Exception
 	 */
 	public static void deleteDatabase(String name, String hostname,
 			String user, String password, boolean verbose) throws Exception {
 		Connection databaseConnection = establishConnection(name, hostname,
 				user, password);
 		checkForActiveConnections(databaseConnection);
 		deleteDatasetOids(databaseConnection);
 		deleteDataDefOids(databaseConnection);
 		databaseConnection.close();
 		Connection templateConnection = establishConnection(templateName,
 				hostname, user, password);
 		dropDatabase(templateConnection, name);
 		templateConnection.close();
 		if (verbose)
 			System.out.println("Database " + name + " dropped");
 	}
 
 	/**
 	 * Executes a sql statement
 	 * 
 	 * @param dbCon
 	 * @param statement
 	 * @throws Exception
 	 */
 	public static void executeSQL(Connection dbCon, String statement)
 			throws Exception {
 		Statement stmt = dbCon.createStatement();
 		try {
 			stmt.execute(statement);
 		} catch (Exception me) {
 			dbCon.close();
 			throw new MobbedException("Could not execute sql statement\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Loads credentials from a property file
 	 * 
 	 * @param filename
 	 * @return
 	 * @throws Exception
 	 */
 	public static String[] loadCredentials(String filename) throws Exception {
 		Properties prop = new Properties();
 		String[] credentials = {};
 		try {
 			prop.load(new FileInputStream(filename));
 			credentials = new String[prop.size()];
 			credentials[0] = prop.getProperty("dbname");
 			credentials[1] = prop.getProperty("hostname");
 			credentials[2] = prop.getProperty("username");
 			credentials[3] = prop.getProperty("password");
 		} catch (Exception me) {
 			throw new MobbedException("Could not load credentials");
 		}
 		return credentials;
 	}
 
 	/**
 	 * Creates a database
 	 * 
 	 * @param dbCon
 	 * @throws Exception
 	 */
 	private static void createDatabase(Connection dbCon, String name)
 			throws Exception {
 		String sql = "CREATE DATABASE " + name;
 		try {
 			PreparedStatement pStmt = dbCon.prepareStatement(sql);
 			pStmt.execute();
 		} catch (Exception me) {
 			dbCon.close();
 			throw new MobbedException("Could not create database " + name
 					+ "\n" + me.getMessage());
 		}
 	}
 
 	/**
 	 * Creates database tables from a sql file
 	 * 
 	 * @param dbCon
 	 * @param path
 	 * @throws Exception
 	 */
 	private static void createTables(Connection dbCon, String filename)
 			throws Exception {
 		DataInputStream in = null;
 		Statement stmt = dbCon.createStatement();
 		try {
 			File file = new File(filename);
 			byte[] buffer = new byte[(int) file.length()];
 			in = new DataInputStream(new FileInputStream(file));
 			in.readFully(buffer);
 			in.close();
 			String result = new String(buffer);
 			String[] tables = result.split("-- execute");
 			for (int i = 0; i < tables.length; i++)
 				stmt.execute(tables[i]);
 		} catch (Exception me) {
 			throw new MobbedException("Could not execute sql code\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Deletes oids in data_defs table
 	 * 
 	 * @param dbCon
 	 * @throws Exception
 	 */
 	private static void deleteDataDefOids(Connection dbCon) throws Exception {
 		try {
 			LargeObjectManager lobj = ((org.postgresql.PGConnection) dbCon)
 					.getLargeObjectAPI();
 			String qry = "SELECT DATADEF_OID FROM DATADEFS WHERE DATADEF_OID IS NOT NULL";
 			Statement stmt = dbCon.createStatement();
 			ResultSet rs = stmt.executeQuery(qry);
 			while (rs.next()) {
 				lobj.unlink(rs.getLong(1));
 			}
 		} catch (Exception me) {
 			dbCon.close();
 			throw new MobbedException(
 					"Could not delete oids in datadefs table\n"
 							+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Deletes oids in datasets table
 	 * 
 	 * @param dbCon
 	 * @throws Exception
 	 */
 	private static void deleteDatasetOids(Connection dbCon) throws Exception {
 		try {
 			LargeObjectManager lobj = ((org.postgresql.PGConnection) dbCon)
 					.getLargeObjectAPI();
 			String qry = "SELECT DATASET_OID FROM DATASETS WHERE DATASET_OID IS NOT NULL";
 			Statement stmt = dbCon.createStatement();
 			ResultSet rs = stmt.executeQuery(qry);
 			while (rs.next())
 				lobj.unlink(rs.getLong(1));
 		} catch (Exception me) {
 			dbCon.close();
 			throw new MobbedException(
 					"Could not delete oids in datasets table\n"
 							+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Drops a database given a name
 	 * 
 	 * @param dbCon
 	 * @param name
 	 * @throws Exception
 	 */
 	private static void dropDatabase(Connection dbCon, String name)
 			throws Exception {
 		String sql = "DROP DATABASE IF EXISTS " + name;
 		try {
 			Statement stmt = dbCon.createStatement();
 			stmt.execute(sql);
 		} catch (Exception me) {
 			dbCon.close();
 			throw new MobbedException("Could not drop database" + name + "\n"
 					+ me.getMessage());
 		}
 	}
 
 	/**
 	 * Establishes a connection to a database
 	 * 
 	 * @param name
 	 * @param hostname
 	 * @param user
 	 * @param password
 	 * @return
 	 * @throws Exception
 	 */
 	private static Connection establishConnection(String dbname,
 			String hostname, String username, String password) throws Exception {
 		Connection dbCon = null;
 		String url = "jdbc:postgresql://" + hostname + "/" + dbname;
 		try {
 			Class.forName("org.postgresql.Driver");
 			dbCon = DriverManager.getConnection(url, username, password);
 		} catch (Exception me) {
 			throw new MobbedException(
 					"Could not establish a connection to database " + dbname
 							+ "\n" + me.getMessage());
 		}
 		return dbCon;
 	}
 
 	/**
 	 * Checks if a string is empty
 	 * 
 	 * @param s
 	 * @return
 	 */
 	private static boolean isEmpty(String s) {
 		boolean empty = true;
 		if (s != null) {
 			if (s.length() > 0)
 				empty = false;
 		}
 		return empty;
 	}
 
 	/**
 	 * Checks if a array is empty
 	 * 
 	 * @param s
 	 * @return
 	 */
 	private static boolean isEmpty(Object[] o) {
 		boolean empty = true;
 		if (o != null) {
 			if (o.length > 0)
 				empty = false;
 		}
 		return empty;
 	}
 }
