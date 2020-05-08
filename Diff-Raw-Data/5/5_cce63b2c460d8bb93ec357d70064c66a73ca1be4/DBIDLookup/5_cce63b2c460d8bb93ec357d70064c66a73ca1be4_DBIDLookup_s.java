 package fedora.server.storage.replication;
 
 /**
  * Title: DBIDLookup.java
  * Description: Database DBID lookup code.
  * Copyright: Copyright (c) 2002
  * Company:
  * @author Paul Charlton
  * @version 1.0
  */
 
 import java.util.*;
 import java.sql.*;
 import java.io.*;
 
 /**
 *
 * Description: Looks up and returns the DBID for a row that matches the column
 * values passed in for that particular row.
 *
 * @version 1.0
 *
 */
 public class DBIDLookup {
 
         /**
         *
         * Looks up a BehaviorDefinition DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bDefPID Behavior definition PID
         *
         * @return The DBID of the specified Behavior Definition row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupBehaviorDefinitionDBID(Connection connection, String bDefPID) throws SQLException {
 		return lookupDBID1(connection, "BDEF_DBID", "BehaviorDefinition", "BDEF_PID", bDefPID);
 	}
 
         /**
         *
         * Looks up a BehaviorMechanism DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bMechPID Behavior mechanism PID
         *
         * @return The DBID of the specified Behavior Mechanism row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupBehaviorMechanismDBID(Connection connection, String bMechPID) throws SQLException {
 		return lookupDBID1(connection, "BMECH_DBID", "BehaviorMechanism", "BMECH_PID", bMechPID);
 	}
 
         /**
         *
         * Looks up a DataStreamBindingMap DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDBID Behavior mechanism DBID
         * @param dsBindingMapID Data stream binding map ID
         *
         * @return The DBID of the specified DataStreamBindingMap row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupDataStreamBindingMapDBID(Connection connection, String bMechDBID, String dsBindingMapID) throws SQLException {
 		return lookupDBID2FirstNum(connection, "BindingMap_DBID", "DataStreamBindingMap", "BMECH_DBID", bMechDBID, "DSBindingMap_ID", dsBindingMapID);
 	}
 
         /**
         *
         * Looks up a DataStreamBindingSpec DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bMechDBID Behavior mechanism DBID
         * @param dsBindingSpecName Data stream binding spec name
         *
         * @return The DBID of the specified DataStreamBindingSpec row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupDataStreamBindingSpecDBID(Connection connection, String bMechDBID, String dsBindingSpecName) throws SQLException {
 		return lookupDBID2FirstNum(connection, "DSBindingKey_DBID", "DataStreamBindingSpec", "BMECH_DBID", bMechDBID, "DSBindingSpec_Name", dsBindingSpecName);
 	}
 
         /**
         *
         * Looks up a DigitalObject DBID.
         *
         * @param connection JDBC DBMS connection
         * @param doPID Data object PID
         *
         * @return The DBID of the specified DigitalObject row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupDigitalObjectDBID(Connection connection, String doPID) throws SQLException {
 		return lookupDBID1(connection, "DO_DBID", "DigitalObject", "DO_PID", doPID);
 	}
 
         /**
         *
         * Looks up a Disseminator DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDBID Behavior definition DBID
         * @param bMechDBID Behavior mechanism DBID
         * @param dissID Disseminator ID
         *
         * @return The DBID of the specified Disseminator row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupDisseminatorDBID(Connection connection, String bDefDBID, String bMechDBID, String dissID) throws SQLException {
 		String query;
 		String ID = null;
 		Statement statement;
 		ResultSet rs;
 
 		query = "SELECT DISS_DBID FROM Disseminator WHERE ";
 		query += "BDEF_DBID = " + bDefDBID + " AND ";
 		query += "BMECH_DBID = " + bMechDBID + " AND ";
 		query += "DISS_ID = '" + dissID + "';";
 
 		// Debug statement
 		// System.out.println("lookupDisseminator, query = " + query);
 
 		statement = connection.createStatement();
 		rs = statement.executeQuery(query);
 
 		while (rs.next())
 			ID = rs.getString(1);
 
 		statement.close();
 		rs.close();
 
 		return ID;
 	}
 
         /**
         *
         * Looks up a Method DBID.
         *
         * @param connection JDBC DBMS connection
         * @param bDefDBID Behavior definition DBID
         * @param methName Method name
         *
         * @return The DBID of the specified Method row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupMethodDBID(Connection connection, String bDefDBID, String methName) throws SQLException {
 		return lookupDBID2FirstNum(connection, "METH_DBID", "Method", "BDEF_DBID", bDefDBID, "METH_Name", methName);
 	}
 
         /**
         *
         * General JDBC lookup method with 1 lookup column value.
         *
         * @param connection JDBC DBMS connection
         * @param DBIDName DBID column name
         * @param tableName Table name
         * @param lookupColumnName Lookup column name
         * @param lookupColumnValue Lookup column value
         *
         * @return The DBID of the specified row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupDBID1(Connection connection, String DBIDName, String tableName, String lookupColumnName, String lookupColumnValue) throws SQLException {
 		String query;
 		String ID = null;
 		Statement statement;
 		ResultSet rs;
 
 		query = "SELECT " + DBIDName + " FROM " + tableName + " WHERE ";
 		query += lookupColumnName + " = '" + lookupColumnValue + "';";
 
 		// Debug statement
 		// System.out.println("lookupDBID1, query = " + query);
 
 		statement = connection.createStatement();
 		rs = statement.executeQuery(query);
 
 		while (rs.next())
 			ID = rs.getString(1);
 		statement.close();
 		rs.close();
 
 		return ID;
 	}
 
         /**
         *
         * General JDBC lookup method with 2 lookup column values.
         *
         * @param connection JDBC DBMS connection
         * @param DBIDName DBID Column name
         * @param tableName Table name
         * @param lookupColumnName1 First lookup column name
         * @param lookupColumnValue1 First lookup column value
         * @param lookupColumnName2 Second lookup column name
         * @param lookupColumnValue2 Second lookup column value
         *
         * @return The DBID of the specified row.
         *
         * @exception SQLException JDBC, SQL error
         */
 	public String lookupDBID2(Connection connection, String DBIDName, String tableName, String lookupColumnName1, String lookupColumnValue1, String lookupColumnName2, String lookupColumnValue2) throws SQLException {
 		String query;
 		String ID = null;
 		Statement statement;
 		ResultSet rs;
 
 		query = "SELECT " + DBIDName + " FROM " + tableName + " WHERE ";
 		query += lookupColumnName1 + " = '" + lookupColumnValue1 + "' AND ";
 		query += lookupColumnName2 + " = '" + lookupColumnValue2 + "';";
 
 		// Debug statement
 		// System.out.println("lookupDBID2, query = " + query);
 
 		statement = connection.createStatement();
 		rs = statement.executeQuery(query);
 
 		while (rs.next())
 			ID = rs.getString(1);
 
 		statement.close();
 		rs.close();
 
 		return ID;
 	}
 
 	public String lookupDBID2FirstNum(Connection connection, String DBIDName, String tableName, String lookupColumnName1, String lookupColumnValue1, String lookupColumnName2, String lookupColumnValue2) throws SQLException {
 		String query;
 		String ID = null;
 		Statement statement;
 		ResultSet rs;
 
 		query = "SELECT " + DBIDName + " FROM " + tableName + " WHERE ";
		query += lookupColumnName1 + " = '" + lookupColumnValue1 + "' AND ";
 		query += lookupColumnName2 + " = '" + lookupColumnValue2 + "';";
 
 		// Debug statement
		// System.out.println("lookupDBID2FirstNum, query = " + query);
 
 		statement = connection.createStatement();
 		rs = statement.executeQuery(query);
 
 		while (rs.next())
 			ID = rs.getString(1);
 		statement.close();
 		rs.close();
 
 		return ID;
 	}
 
 }
