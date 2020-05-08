 package net.fluxo.updater;
 
 import net.fluxo.updater.dbo.*;
 import net.fluxo.updater.processor.RSSTags;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ronald Kurniawan (viper)
  * Date: 27/06/13
  * Time: 9:26 AM
  */
 public class DatabaseManager {
 
 	private static String _connString = null;
 	private static Logger _logger = Logger.getLogger("net.fluxo");
 
 	public static void setConnectionString(String url) {
 		_connString = "jdbc:mysql://" + url + "?user=app_account&password=p4ssw0rd_buzz";
 	}
 
 	public DatabaseManager() throws ClassNotFoundException, IOException {
 		Class.forName("com.mysql.jdbc.Driver");
 		Properties prop = new Properties();
 		prop.load(new FileInputStream("updaterd.properties"));
 		setConnectionString(prop.getProperty("updaterd.dbPath", "localhost:3306"));
 	}
 
 	private static Connection getConnection() {
 		Connection con = null;
 		try {
 			con = DriverManager.getConnection(_connString);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			_logger.error("ERROR while connecting to db: " + e.getMessage() + "; error code: " + e.getErrorCode());
 		}
 		return con;
 	}
 
 	public static boolean isTableExists(String tableName) throws SQLException {
 		boolean exists = false;
 		Connection connection = getConnection();
 		DatabaseMetaData metaData = connection.getMetaData();
 		ResultSet rs = metaData.getTables(null, "apps", tableName, null);
 		if (rs.next()) {
 			exists = true;
 		}
 		rs.close();
 		connection.close();
 		return exists;
 	}
 
 	public static List<BrisData> readBrisbaneMasterTable() throws SQLException {
 		List<BrisData> rows = new ArrayList<BrisData>();
 		Connection connection = null;
 		Statement statement = null;
 		ResultSet rs = null;
 		try {
 			connection = getConnection();
 			statement = connection.createStatement();
 			rs = statement.executeQuery("SELECT * FROM apps.bris_data");
 			while (rs.next()) {
 				BrisData bd = new BrisData();
 				bd.setId(rs.getInt("idbris_data"));
 				bd.setFrontURL(rs.getString("front_url"));
 				bd.setDatasetURL(rs.getString("dataset_url"));
 				bd.setFile(rs.getString("file"));
 				bd.setDumpTable(rs.getString("dump_tbl"));
 				bd.setTitle(rs.getString("title"));
 				bd.setDescription(rs.getString("description"));
 				bd.setLastUpdated(rs.getTimestamp("last_updated").getTime());
 				bd.setDatePublished(rs.getTimestamp("date_published").getTime());
 				bd.setLastChecked(rs.getTimestamp("last_checked").getTime());
 				bd.setUpdateFrequency(rs.getString("update_freq").charAt(0));
 				rows.add(bd);
 			}
 		} finally {
 			if (rs != null) {
 				rs.close();
 			}
 			if (statement != null) {
 				statement.close();
 			}
 			if (connection != null) {
 				connection.close();
 			}
 		}
 		return rows;
 	}
 
 	public static void updateBrisDataObject(BrisData obj) throws SQLException {
 		// update object first
 		// check against: date updated, update frequency and last updated
 		DateTime now = DateTime.now();
 		obj.setLastUpdated(now.getMillis());
 
 		String sql = "UPDATE bris_data SET last_checked = ?, date_published = ? WHERE idbris_data = ?";
 		Connection connection = null;
 		PreparedStatement ps = null;
 		try {
 			connection = DatabaseManager.getConnection();
 			ps = connection.prepareStatement(sql);
 			ps.setTimestamp(1, new java.sql.Timestamp(obj.getLastChecked().getMillis()));
 			ps.setTimestamp(2, new java.sql.Timestamp(obj.getDatePublished().getMillis()));
 			ps.setInt(3, obj.getId());
 			ps.executeUpdate();
 		} catch (SQLException sqle) {
 			sqle.printStackTrace();
 			_logger.error(obj.getDumpTable() + ": ERROR UPDATING bris_data: " + sqle.getMessage() + "; ERROR CODE: " +
 					sqle.getErrorCode() + " caused by: " + sqle.getCause().getMessage());
 		} finally {
 			if (ps != null) {
 				ps.close();
 			}
 			connection.close();
 		}
 	}
 
 	public static void createTableForCSV(String tableName, String[] columns) throws SQLException {
 		_logger.info("Creating table " + tableName + " on qld_data...");
 		String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
 		int counter = 1;
 		for (String column : columns) {
 			sql += column + " VARCHAR(512) NOT NULL";
 			if (counter < columns.length) {
 				sql += ",";
 			}
 			counter++;
 		}
 		sql += ") ENGINE=InnoDB";
 		Connection conn = DatabaseManager.getConnection();
 		conn.setAutoCommit(false);
 		PreparedStatement ps = conn.prepareStatement(sql);
 		ps.executeUpdate();
 		// Now we need to create a unique index for this table...
 		List<String> idColumns = new ArrayList<String>();
 		for (String s : columns) {
 			if (s.indexOf("id") > -1) {
 				idColumns.add(s);
 			}
 		}
 		if (idColumns.size() > 0) {
 			for (String id : idColumns) {
 				sql = "CREATE UNIQUE INDEX " + tableName + "_" + id + "_idx ON " + tableName +
 						"(" + id + ")";
 				ps = conn.prepareStatement(sql);
 				ps.executeUpdate();
 				conn.commit();
 			}
 			conn.commit();
 		}
 		ps.close();
 		conn.close();
 	}
 
 	public static void processLinesForCSV(String tableName, String[] columns, List<String[]> data, int[] potentialKeyColumns, int[] regularColumns) throws SQLException {
 		Connection connection = null;
 		PreparedStatement ps = null;
 		try {
 			_logger.info("PROCESSING batch instructions for table " + tableName + "...");
 			int counter = 1;
 			// process column names...
 			StringBuilder cols = new StringBuilder();
 			for (String s : columns) {
 				cols.append(s);
 				if (counter < columns.length) {
 					cols.append(",");
 				}
 				counter++;
 			}
 			String sql = "INSERT INTO " + tableName + "(" + cols.toString() + ")" + "VALUES (";
 			counter = 1;
 			for (int i = 0; i < columns.length; i++) {
 				sql += "?";
 				if (counter < columns.length) {
 					sql += ",";
 				}
 				counter++;
 			}
 			sql += ") ON DUPLICATE KEY UPDATE ";
 			// output the regular columns to update in case of DUPLICATE
 			counter = 1;
 			for (int col : regularColumns) {
 				sql += columns[col] + " = ?";
 				if (counter < regularColumns.length) {
 					sql += ",";
 				}
 				counter++;
 			}
 			// ---- PROCESS THE ROWS NOW -----
 			connection = getConnection();
 			connection.setAutoCommit(false);
 			ps = connection.prepareStatement(sql);
 			for (String[] row : data) {
 				counter = 1;
 				for (int i = 0; i < row.length; i++) {
 					ps.setString(counter, row[i]);
 					counter++;
 				}
 				// now, fill the parameters for the UPDATE part...
 				for (int i = 0; i < regularColumns.length; i++) {
 					ps.setString(counter, row[regularColumns[i]]);
 					counter++;
 				}
 				ps.addBatch();
 			}
 			// executes the batch...
 			ps.executeBatch();
 			connection.commit();
 		} finally {
 			if (ps != null) {
 				ps.close();
 			}
 			if (connection != null) {
 				connection.close();
 			}
 			_logger.info("TABLE " + tableName + " was successfully updated.");
 		}
 	}
 
 	public static void createTableForRSS(String tableName, List<String> columns, List<String> extraColumns) throws SQLException {
 		_logger.info("Creating table " + tableName + " for bris_data...");
 		String sql = "DROP TABLE IF EXISTS " + tableName;
 		Connection connection = getConnection();
 		connection.setAutoCommit(false);
 		Statement dropStatement = connection.createStatement();
 		dropStatement.execute(sql);
 		dropStatement.close();
 		sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
 		int counter = 1;
 		for (String column : columns) {
 			if (column.equalsIgnoreCase(RSSTags.DESCRIPTION.getPlainTag()) || column.equalsIgnoreCase("x_description")) {
				sql += column + " TEXT NOT NULL";
				if (extraColumns.contains(column)) {
 					sql += " DEFAULT ''";
				}
 			} else if (column.equalsIgnoreCase(RSSTags.LINK.getPlainTag())) {
 				sql += column + " VARCHAR(256) NOT NULL";
 				if (extraColumns.contains(column)) {
 					sql += " DEFAULT ''";
 				}
 			} else if (column.equalsIgnoreCase(RSSTags.X_START.getPlainTag()) || column.equalsIgnoreCase(RSSTags.X_END.getPlainTag())) {
 				sql += column + " TIMESTAMP NOT NULL";
 			} else {
 				sql += column + " VARCHAR(300) NOT NULL DEFAULT ''";
 			}
 			if (counter < columns.size()) {
 				sql += ",";
 			}
 			counter++;
 		}
 		sql += ") ENGINE=InnoDB";
 		PreparedStatement ps = connection.prepareStatement(sql);
 		ps.executeUpdate();
 		ps.close();
 		connection.close();
 	}
 
 	public static void processDataForRSS(String tableName, List<RSSData> data) throws SQLException {
 		Connection connection = null;
 		PreparedStatement ps = null;
 		_logger.info("PROCESSING batch instructions for table " + tableName);
 		int dataCounter = 0;
 		// process column names...
 		for (RSSData rssData : data) {
 			if (!rssData.checkColumnsAgainstData()) {
 				continue;
 			}
 			List<String> columns = rssData.getColumns();
 			StringBuilder cols = new StringBuilder();
 			int counter = 1;
 			for (String s : columns) {
 				cols.append(s);
 				if (counter < columns.size()) {
 					cols.append(",");
 				}
 				counter++;
 			}
 			String sql = "INSERT INTO " + tableName + "(" + cols.toString() + ")" + "VALUES (";
 			counter = 1;
 			for (int i = 0; i < columns.size(); i++) {
 				sql += "?";
 				if (counter < columns.size()) {
 					sql += ",";
 				}
 				counter++;
 			}
 			sql += ")";
 			// ---- PROCESS THE ROWS NOW -----
 			connection = getConnection();
 			connection.setAutoCommit(false);
 			ps = connection.prepareStatement(sql);
 			List<String> rssValue = rssData.getData();
 			// X_START & X_END --> Timestamp
 			// X_DESCRIPTION & DESCRIPTION --> TEXT
 			// LINK --> Varchar(256)
 			for (int i = 0; i < rssValue.size(); i++) {
 				// check the COLUMN_HEADER corresponding to this value...
 				if (columns.get(i).equals(RSSTags.X_DESCRIPTION.getPlainTag()) || columns.get(i).equals(RSSTags.DESCRIPTION.getPlainTag())) {
 					ps.setString(i + 1, rssValue.get(i));
 				} else if (columns.get(i).equals(RSSTags.X_START.getPlainTag()) || columns.get(i).equals(RSSTags.X_END.getPlainTag())) {
 					ps.setTimestamp(i + 1, new java.sql.Timestamp(Long.parseLong(rssValue.get(i))));
 				} else {
 					ps.setString(i + 1, rssValue.get(i));
 				}
 			}
 			ps.executeUpdate();
 			connection.commit();
 			ps.close();
 			dataCounter++;
 		}
 		_logger.info("PROCESSED " + dataCounter + " instructions for table " + tableName + "...");
 		connection.close();
 	}
 
 	public static boolean isTableStructuresSimilar(String tableName, String[] tableColumns) throws SQLException {
 		boolean similar = true;
 		Connection connection = getConnection();
 		DatabaseMetaData metaData = connection.getMetaData();
 		ResultSet rs = metaData.getColumns(null, null, tableName, "%");
 		List<String> columns = new ArrayList<String>();
 		while (rs.next()) {
 			columns.add(rs.getString("COLUMN_NAME"));
 		}
 		rs.close();
 		connection.close();
 		String[] dbColumns = new String[columns.size()];
 		columns.toArray(dbColumns);
 		if (dbColumns.length != tableColumns.length) {
 			similar = false;
 		}
 		if (similar) {
 			// now we match the column names
 			for (int i = 0; i < dbColumns.length; i++) {
 				boolean columnSimilarCaught = false;
 				for (String s : tableColumns) {
 					if (dbColumns[i].equals(s)) {
 						columnSimilarCaught = true;
 						break;
 					}
 				}
 				if (!columnSimilarCaught) {
 					similar = false;
 					break;
 				}
 			}
 		}
 		return similar;
 	}
 
 	public static void createTableForKML(String tableName, List<KMLObjectHolder> list, KMLSchema schema) throws SQLException {
 		_logger.info("Creating table " + tableName);
 		String sql = "DROP TABLE IF EXISTS " + tableName;
 		Connection connection = getConnection();
 		connection.setAutoCommit(false);
 		Statement dropStatement = connection.createStatement();
 		dropStatement.executeUpdate(sql);
 		dropStatement.close();
 		sql = "DROP TABLE IF EXISTS " + tableName + "_coords";
 		dropStatement = connection.createStatement();
 		dropStatement.executeUpdate(sql);
 		dropStatement.close();
 		// CREATE THE TABLES
 		sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
 		for (KMLSchemaField field : schema.getSchema()) {
 			sql += field.getName();
 			sql += " " + getMysqlDatatype(field.getType());
 			sql += ",";
 		}
 		sql += "ID INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (ID)) Engine=InnoDB";
 		PreparedStatement ps = connection.prepareStatement(sql);
 		ps.executeUpdate();
 		ps.close();
 		boolean shouldCreateCoordinateTable = false;
 		for (KMLObjectHolder object : list) {
 			if (object.getCoordinates().size() > 0) {
 				shouldCreateCoordinateTable = true;
 				break;
 			}
 		}
 		if (shouldCreateCoordinateTable) {
 			sql = "CREATE TABLE IF NOT EXISTS " + tableName + "_coords (";
 			sql += "ID INT(11) NOT NULL AUTO_INCREMENT, T_ID INT(11) NOT NULL," +
 					"LONGITUDE DOUBLE(25,15), LATITUDE DOUBLE(25,15)," +
 					"ALTITUDE DOUBLE(25,15), TYPE VARCHAR(64), PRIMARY KEY(ID)) Engine=InnoDB";
 			ps = connection.prepareStatement(sql);
 			ps.executeUpdate();
 			ps.close();
 		}
 		connection.commit();
 		connection.close();
 	}
 
 	public static void processDataForKML(String tableName, List<KMLObjectHolder> list, KMLSchema schema) throws SQLException {
 		Connection connection = null;
 		PreparedStatement ps = null;
 		_logger.info("PROCESSING batch instructions for table " + tableName);
 		String sqlInsert = "INSERT INTO " + tableName + " (";
 		int counter = 1;
 		for (KMLSchemaField field : schema.getSchema()) {
 			sqlInsert += field.getName();
 			if (counter < schema.getSchema().size()) {
 				sqlInsert += ",";
 			}
 			counter++;
 		}
 		sqlInsert += ") VALUES(";
 		for (counter = 0; counter < schema.getSchema().size(); counter++) {
 			sqlInsert += "?";
 			if (counter + 1 < schema.getSchema().size()) {
 				sqlInsert += ",";
 			}
 		}
 		sqlInsert += ")";
 		int dataCounter = 0;
 		for (KMLObjectHolder object : list) {
 			connection = getConnection();
 			connection.setAutoCommit(false);
 			// We need to insert to the main table first, then get the id number and then use that number
 			// to insert into the coordinate table
 			ps = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
 			counter = 1;
 			for (KMLSchemaField field : schema.getSchema()) {
 				if (field.getType().equalsIgnoreCase("string")) {
 					ps.setString(counter, object.getValues().get(field.getName()));
 				} else if (field.getType().equalsIgnoreCase("double")) {
 					ps.setDouble(counter, Double.parseDouble(object.getValues().get(field.getName())));
 				}
 				counter++;
 			}
 			ps.executeUpdate();
 			ResultSet rsId = ps.getGeneratedKeys();
 			connection.commit();
 			if (rsId.next()) {
 				Connection coordConnection = getConnection();
 				coordConnection.setAutoCommit(false);
 				int tableId = rsId.getInt(1);
 				String csql = "INSERT INTO " + tableName + "_coords (T_ID,LONGITUDE,LATITUDE,ALTITUDE,TYPE) " +
 						"VALUES(?,?,?,?,?)";
 				PreparedStatement coordPs = coordConnection.prepareStatement(csql);
 				for (KMLObjectCoordinate obj : object.getCoordinates()) {
 					coordPs.setInt(1, tableId);
 					coordPs.setDouble(2, obj.getLongitude());
 					coordPs.setDouble(3, obj.getLatitude());
 					coordPs.setDouble(4, obj.getAltitude());
 					coordPs.setString(5, (obj.getType()));
 					coordPs.addBatch();
 				}
 				coordPs.executeBatch();
 				coordConnection.commit();
 				coordPs.close();
 				coordConnection.close();
 			}
 			rsId.close();
 			ps.close();
 			connection.close();
 			dataCounter++;
 		}
 		_logger.info("Processed " + dataCounter + " row(s) for table " + tableName + "...");
 	}
 
 	private static String getMysqlDatatype(String type) {
 		if (type.equalsIgnoreCase("double")) {
 			return "DOUBLE(25,15)";
 		} else if (type.equalsIgnoreCase("string")) {
 			return "VARCHAR(64)";
 		}
 		return "VARCHAR(32)";
 	}
 }
