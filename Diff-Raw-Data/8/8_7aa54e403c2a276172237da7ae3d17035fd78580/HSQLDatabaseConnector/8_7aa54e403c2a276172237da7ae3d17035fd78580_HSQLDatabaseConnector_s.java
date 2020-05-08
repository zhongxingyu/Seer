 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw.mannheim.cloudraid.persistence;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 
 /**
  * An implementation of the {@link IDatabaseConnector} for the HSQL database
  * system.
  * 
  * @author Florian Bausch, Markus Holtermann
  * 
  */
 public class HSQLDatabaseConnector implements IDatabaseConnector {
 	private Connection con;
 	private PreparedStatement insertStatement, updateStatement, findStatement,
 			deleteStatement, findNameStatement;
 	private Statement statement;
 
	private final static String DB_PATH = Config.getCloudRAIDHome() + "filedb";

 	@Override
 	public synchronized boolean connect(String database) {
 		try {
 			con = DriverManager.getConnection("jdbc:hsqldb:file:" + database
 					+ ";shutdown=true", "SA", "");
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
	public synchronized boolean connect() {
		return this.connect(DB_PATH);
	}

	@Override
 	public synchronized boolean disconnect() {
 		try {
 			if (statement != null) {
 				statement.execute("SHUTDOWN COMPACT;");
 			}
 			if (con != null) {
 				con.commit();
 			}
 		} catch (SQLException e) {
 		}
 
 		statement = null;
 		findStatement = null;
 		insertStatement = null;
 		deleteStatement = null;
 		updateStatement = null;
 		findNameStatement = null;
 
 		try {
 			if (con != null) {
 				con.close();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public synchronized boolean initialize() {
 		try {
 			statement = con.createStatement();
 			// statement.execute("DROP TABLE IF EXISTS cloudraid_files;");
 			String createTable = "CREATE CACHED TABLE IF NOT EXISTS cloudraid_files "
 					+ "( path_name VARCHAR(512) NOT NULL, hash_name VARCHAR(256) NOT NULL, "
 					+ "last_mod TIMESTAMP NOT NULL, PRIMARY KEY ( path_name ) );";
 			statement.execute(createTable);
 
 			con.commit();
 
 			insertStatement = con
 					.prepareStatement("INSERT INTO cloudraid_files VALUES ( ?, ?, ? );");
 			updateStatement = con
 					.prepareStatement("UPDATE cloudraid_files SET last_mod = ? WHERE path_name = ? ;");
 			findStatement = con
 					.prepareStatement("SELECT * FROM cloudraid_files WHERE path_name = ? ;");
 			deleteStatement = con
 					.prepareStatement("DELETE FROM cloudraid_files WHERE path_name = ? ;");
 			findNameStatement = con
 					.prepareStatement("SELECT * FROM cloudraid_files WHERE hash_name = ?;");
 
 			return true;
 		} catch (SQLException e) {
 			try {
 				con.rollback();
 			} catch (SQLException e1) {
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public synchronized boolean insert(String path, String hash, long lastMod) {
 		try {
 			findStatement.setString(1, path);
 			ResultSet resSet = findStatement.executeQuery();
 
 			if (resSet.next()) {
 				updateStatement.setTimestamp(1, new Timestamp(lastMod));
 				updateStatement.setString(2, path);
 				updateStatement.execute();
 			} else {
 				insertStatement.setString(1, path);
 				insertStatement.setString(2, hash);
 				insertStatement.setTimestamp(3, new Timestamp(lastMod));
 				insertStatement.execute();
 			}
 			con.commit();
 			return true;
 		} catch (SQLException e) {
 			try {
 				con.rollback();
 			} catch (SQLException e1) {
 			}
 			return false;
 		} catch (NullPointerException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public synchronized String getHash(String path) {
 		try {
 			findStatement.setString(1, path);
 			findStatement.execute();
 			ResultSet rs = findStatement.getResultSet();
 			rs.next();
 			return rs.getString("hash_name");
 		} catch (SQLException e) {
 			return null;
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public synchronized long getLastMod(String path) {
 		try {
 			findStatement.setString(1, path);
 			findStatement.execute();
 			ResultSet rs = findStatement.getResultSet();
 			rs.next();
 			return rs.getTimestamp("last_mod").getTime();
 		} catch (SQLException e) {
 			return -1L;
 		} catch (NullPointerException e) {
 			return -1L;
 		}
 	}
 
 	@Override
 	public synchronized String getName(String hash) {
 		try {
 			findNameStatement.setString(1, hash);
 			findNameStatement.execute();
 			ResultSet rs = findNameStatement.getResultSet();
 			rs.next();
 			return rs.getString("path_name");
 		} catch (SQLException e) {
 			return null;
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public synchronized boolean delete(String path) {
 		try {
 			deleteStatement.setString(1, path);
 			deleteStatement.execute();
 		} catch (SQLException e) {
 		} catch (NullPointerException e) {
 			return false;
 		}
 		return true;
 	}
 
 }
