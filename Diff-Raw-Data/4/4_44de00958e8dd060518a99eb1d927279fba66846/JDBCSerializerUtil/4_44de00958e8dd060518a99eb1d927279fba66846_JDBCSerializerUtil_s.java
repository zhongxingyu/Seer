 /**
  * 
  */
 package de.xwic.cube.util;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.xwic.cube.IDataPool;
 import de.xwic.cube.IDimension;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.IMeasure;
 
 /**
  * @author Developer
  *
  */
 public class JDBCSerializerUtil {
 
 	private static Log log = LogFactory.getLog(JDBCSerializerUtil.class);
 	
 	/**
 	 * Update the specified table with the measures in the DataPool.
 	 * @param connection
 	 * @param tableName
 	 * @param pool
 	 * @throws SQLException
 	 */
 	public static void storeMeasures(Connection connection, IDataPool pool, String tableName) throws SQLException {
 		
 		Statement stmt = connection.createStatement();
 		PreparedStatement psUpdate = connection.prepareStatement("UPDATE [" + tableName + "] SET [Title] = ?, [FunctionClass] = ?, [ValueFormatProvider] = ? WHERE [Key] = ?");
 		PreparedStatement psInsert = connection.prepareStatement("INSERT INTO [" + tableName + "] ([Key], [Title], [FunctionClass], [ValueFormatProvider]) VALUES (?, ?, ?, ?)");
 		PreparedStatement psDelete = connection.prepareStatement("DELETE FROM [" + tableName + "] WHERE [Key] = ?");
 		ResultSet rs = stmt.executeQuery("SELECT [Key] FROM [" + tableName + "]");
 		Set<String> keys = new HashSet<String>();
 		while (rs.next()) {
 			keys.add(rs.getString(1));
 		}
 		rs.close();
 		stmt.close();
 		
 		for (IMeasure measure : pool.getMeasures()) {
 			if (keys.contains(measure.getKey())) {
 				psUpdate.clearParameters();
 				psUpdate.setString(1, measure.getTitle());
 				if (measure.isFunction()) {
 					psUpdate.setString(2, measure.getFunction().getClass().getName());
 				} else {
 					psUpdate.setNull(2, Types.VARCHAR);
 				}
 				psUpdate.setString(3, measure.getValueFormatProvider().getClass().getName());
 				psUpdate.setString(4, measure.getKey());
 				int updates = psUpdate.executeUpdate();
 				if (updates != 1) {
 					System.out.println("Measure update failed for " + measure.getKey()); 
 				}
 				keys.remove(measure.getKey());
 			} else {
 				psInsert.clearParameters();
 				psInsert.setString(1, measure.getKey());
 				psInsert.setString(2, measure.getTitle());
 				if (measure.isFunction()) {
 					psInsert.setString(3, measure.getFunction().getClass().getName());
 				} else {
 					psInsert.setNull(3, Types.VARCHAR);
 				}
 				psInsert.setString(4, measure.getValueFormatProvider().getClass().getName());
 				psInsert.executeUpdate();
 			}
 		}
 
 		// delete old keys.
 		for (String key : keys) {
 			psDelete.clearParameters();
 			psDelete.setString(1, key);
 			psDelete.executeUpdate();
 		}
 		
 		psUpdate.close();
 		psInsert.close();
 		psDelete.close();
 	}
 	
 	/**
 	 * Update the specified table with the measures in the DataPool.
 	 * @param connection
 	 * @param tableName
 	 * @param pool
 	 * @throws SQLException
 	 */
 	public static void storeDimensions(Connection connection, IDataPool pool, String dimTableName, String dimElmTableName) throws SQLException {
 		
 		checkDimensionTable(connection, dimTableName, dimElmTableName);
 		
 		PreparedStatement psUpdateDim = connection.prepareStatement("UPDATE [" + dimTableName + "] SET [Title] = ?, [Sealed] = ? WHERE [Key] = ?");
 		PreparedStatement psInsertDim = connection.prepareStatement("INSERT INTO [" + dimTableName + "] ([Key], [Title], [Sealed]) VALUES (?, ?)");
 		PreparedStatement psDeleteDim = connection.prepareStatement("DELETE FROM [" + dimTableName + "] WHERE [Key] = ?");
 
 		PreparedStatement psSelectDimElm = connection.prepareStatement("SELECT [ID] FROM [" + dimElmTableName + "] WHERE [DimensionKey] = ? AND [ParentID] = ?");
 		PreparedStatement psUpdateDimElm = connection.prepareStatement("UPDATE [" + dimElmTableName + "] SET [Title] = ?, [weight] = ?, [order_index] = ? WHERE [ID] = ?");
 		PreparedStatement psInsertDimElm = connection.prepareStatement("INSERT INTO [" + dimElmTableName + "] ([ID], [ParentID], [DimensionKey], [Key], [Title], [weight], [order_index]) VALUES (?, ?, ?, ?, ?, ?, ?)");
 		PreparedStatement psDeleteDimElm = connection.prepareStatement("DELETE FROM [" + dimElmTableName + "] WHERE [ID] = ?");
 		
 		Statement stmt = connection.createStatement();
 		ResultSet rs = stmt.executeQuery("SELECT [Key] FROM [" + dimTableName + "]");
 		Set<String> keys = new HashSet<String>();
 		while (rs.next()) {
 			keys.add(rs.getString(1));
 		}
 		rs.close();
 		stmt.close();
 		
 		for (IDimension dimension : pool.getDimensions()) {
 			if (keys.contains(dimension.getKey())) {
 				psUpdateDim.clearParameters();
 				psUpdateDim.setString(1, dimension.getTitle());
 				psUpdateDim.setBoolean(2, dimension.isSealed());
 				psUpdateDim.setString(3, dimension.getKey());
 				int updates = psUpdateDim.executeUpdate();
 				if (updates != 1) {
 					System.out.println("Dimension update failed for " + dimension.getKey()); 
 				}
 				keys.remove(dimension.getKey());
 			} else {
 				psInsertDim.clearParameters();
 				psInsertDim.setString(1, dimension.getKey());
 				psInsertDim.setString(2, dimension.getTitle());
 				psUpdateDim.setBoolean(3, dimension.isSealed());
 				psInsertDim.executeUpdate();
 			}
 			updateDimensionElements(dimension, psSelectDimElm, psInsertDimElm, psUpdateDimElm, psDeleteDimElm);
 		}
 
 		// delete old keys.
 		for (String key : keys) {
 			psDeleteDim.clearParameters();
 			psDeleteDim.setString(1, key);
 			psDeleteDim.executeUpdate();
 		}
 		
 		psUpdateDim.close();
 		psInsertDim.close();
 		psDeleteDim.close();
 		psSelectDimElm.close();
 		psUpdateDimElm.close();
 		psDeleteDimElm.close();
 		psInsertDimElm.close();
 	}
 
 	/**
 	 * @param connection
 	 * @param dimElmTableName 
 	 * @param dimTableName 
 	 * @throws SQLException 
 	 */
 	private static void checkDimensionTable(Connection connection, String dimTableName, String dimElmTableName) throws SQLException {
 		
 		if (!columnExists(connection, dimTableName, "Sealed")) {
 			// create column
 			log.warn("Column 'Sealed' does not exist - will be created now..");
 			Statement stmt = connection.createStatement();
 			stmt.execute("ALTER TABLE [" + dimTableName + "] ADD [Sealed] Bit NOT NULL Default 0");
 			SQLWarning sw = stmt.getWarnings();
 			if (sw != null) {
 				log.warn("SQL Result: " + sw);
 			}
 		}
 
 		
 	}
 	
 	/**
 	 * Check if a column exists already.
 	 * @param con
 	 * @param tableName
 	 * @param columnName
 	 * @return
 	 * @throws SQLException
 	 */
 	private static boolean columnExists(Connection con, String tableName, String columnName) throws SQLException {
 		
 		DatabaseMetaData metaData = con.getMetaData();
 		ResultSet columns = metaData.getColumns(con.getCatalog(), null, tableName, columnName);
 		try {
 			if (columns.next()) {
 				return true;
 			}
 			return false;
 		} finally {
 			columns.close();
 		}
 		
 	}
 
 	/**
 	 * @param dimension
 	 * @param psInsertDimElm
 	 * @param psUpdateDimElm
 	 * @param psDeleteDimElm
 	 */
 	private static void updateDimensionElements(IDimensionElement elm, PreparedStatement psSelectDimElm, PreparedStatement psInsertDimElm, PreparedStatement psUpdateDimElm, PreparedStatement psDeleteDimElm) throws SQLException {
 		
 		psSelectDimElm.clearParameters();
 		psSelectDimElm.setString(1, elm.getDimension().getKey());
 		psSelectDimElm.setString(2, elm.getID());
 		ResultSet rs = psSelectDimElm.executeQuery();
 		Set<String> keys = new HashSet<String>();
 		while (rs.next()) {
 			keys.add(rs.getString(1));
 		}
 		rs.close();
 		
 		int index = 0;
 		for (IDimensionElement dimElm : elm.getDimensionElements()) {
 			if (keys.contains(dimElm.getID())) {
 				psUpdateDimElm.clearParameters();
 				psUpdateDimElm.setString(1, dimElm.getTitle());
 				psUpdateDimElm.setDouble(2, dimElm.getWeight());
 				psUpdateDimElm.setInt(3, index++);
 				psUpdateDimElm.setString(4, dimElm.getID());
 				int updates = psUpdateDimElm.executeUpdate();
 				if (updates != 1) {
 					System.out.println("DimensionElement update failed for " + dimElm.getKey()); 
 				}
 				keys.remove(dimElm.getID());
 			} else {
 				// ([ID], [ParentID], [DimensionKey], [Key], [Title], [weight], [order_index])
 				psInsertDimElm.clearParameters();
 				psInsertDimElm.setString(1, dimElm.getID());
 				psInsertDimElm.setString(2, elm.getID());
 				psInsertDimElm.setString(3, dimElm.getDimension().getKey());
 				psInsertDimElm.setString(4, dimElm.getKey());
 				psInsertDimElm.setString(5, dimElm.getTitle());
 				psInsertDimElm.setDouble(6, dimElm.getWeight());
 				psInsertDimElm.setInt(7, index++);
 				psInsertDimElm.executeUpdate();
 			}
 			updateDimensionElements(dimElm, psSelectDimElm, psInsertDimElm, psUpdateDimElm, psDeleteDimElm);
 		}
 
 		// delete old keys.
 		for (String key : keys) {
 			psDeleteDimElm.clearParameters();
 			psDeleteDimElm.setString(1, key);
 			psDeleteDimElm.executeUpdate();
 		}
 
 
 		
 	}
 
 	/**
 	 * @param connection
 	 * @param pool
 	 * @throws SQLException 
 	 */
 	public static void restoreDimensions(Connection connection, IDataPool pool, String dimTableName, String dimElmTableName) throws SQLException {
 		
 		checkDimensionTable(connection, dimTableName, dimElmTableName);
 		
 		// restores dimensions.
 		PreparedStatement psSelectDimElm = connection.prepareStatement("SELECT [Key], [Title], [weight] FROM [" + dimElmTableName + "] WHERE [DimensionKey] = ? AND [ParentID] = ? ORDER BY order_index ASC");
 		
 		Statement stmt = connection.createStatement();
 		ResultSet rs = stmt.executeQuery("SELECT [Key], [Title], [Sealed] FROM [" + dimTableName + "]");
 		while (rs.next()) {
 			String key = rs.getString(1);
 			String title = rs.getString(2);
 			boolean sealed = rs.getBoolean(3);
 			System.out.println("Validating Dimension " + key);
 			IDimension dim;
 			if (!pool.containsDimension(key)) {
 				dim = pool.createDimension(key);
 			} else {
 				dim = pool.getDimension(key);
 			}
 			dim.setTitle(title);
			dim.setSealed(sealed);
 			
 			// load child elements
 			restoreChilds(dim, psSelectDimElm);
 		}
 		rs.close();
 		stmt.close();
 		
 	}
 
 	/**
 	 * @param dim
 	 * @param psSelectDimElm
 	 * @throws SQLException 
 	 */
 	private static void restoreChilds(IDimensionElement elm, PreparedStatement psSelectDimElm) throws SQLException {
 		
 		psSelectDimElm.clearParameters();
 		psSelectDimElm.setString(1, elm.getDimension().getKey());
 		psSelectDimElm.setString(2, elm.getID());
 		psSelectDimElm.setFetchSize(1000);
 		
 		ResultSet rs = psSelectDimElm.executeQuery();
 		while (rs.next()) {
 			String key = rs.getString("Key");
 			String title = rs.getString("Title");
 			double weight = rs.getDouble("weight");
 			IDimensionElement child;
 			if (elm.containsDimensionElement(key)) {
 				child = elm.getDimensionElement(key);
 			} else {
 				child = elm.createDimensionElement(key);
 			}
 			child.setTitle(title);
 			child.setWeight(weight);
 		}
 		rs.close();
 		
 		for (IDimensionElement child : elm.getDimensionElements()) {
 			restoreChilds(child, psSelectDimElm);
 		}
 		
 	}
 }
