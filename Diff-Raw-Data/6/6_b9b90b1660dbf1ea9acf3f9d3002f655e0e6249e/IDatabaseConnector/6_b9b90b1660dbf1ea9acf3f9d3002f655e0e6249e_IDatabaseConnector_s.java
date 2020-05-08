 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details.
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
 
 /**
  * @author Florian Bausch, Markus Holtermann
  *
  */
 public interface IDatabaseConnector {
 
 	/**
	 * Creates a connection to a specific database. To use the default, use
	 * {@link #connect()}
 	 * 
 	 * @param database
 	 *            The absolute path to the database.
 	 * 
 	 * @return true, if the connection could be opened; false, if not.
 	 */
 	public boolean connect(String database);
 
 	/**
	 * Closes the connection to the database opened by {@link #connect()}.
 	 * 
 	 * @return true, if the connection could be closed.
 	 */
 	public boolean disconnect();
 
 	/**
 	 * Creates the database schemas.
 	 * 
 	 * @return true, if the initialization could be executed.
 	 */
 	public boolean initialize();
 
 	/**
 	 * Inserts a data set into the database.
 	 * 
 	 * @param path
 	 *            The path of a file.
 	 * @param hash
 	 *            The hash of the file name.
 	 * @param lastMod
 	 *            The last modification date.
 	 * @return true, if the data set could be inserted into the database.
 	 */
 	public boolean insert(String path, String hash, long lastMod);
 
 	/**
 	 * Looks up the hash value of an entry in the database.
 	 * 
 	 * @param path
 	 *            The path of the file (identifies the data set).
 	 * @return The hash value. Or <code>null</code>, if the path does not exist
 	 *         in the database.
 	 */
 	public String getHash(String path);
 
 	/**
 	 * Looks up the last modification date of a file.
 	 * 
 	 * @param path
 	 *            The path of the file.
 	 * @return The last modification date. Or <code>-1L</code>, if the path does
 	 *         not exist in the database.
 	 */
 	public long getLastMod(String path);
 
 	/**
 	 * Looks up a file name for a given hash value.
 	 * 
 	 * @param hash
 	 *            The hash value.
 	 * @return The path of the file. Or <code>null</code>, if the hash does not
 	 *         exist in the database.
 	 */
 	public String getName(String hash);
 
 	/**
 	 * Deletes a data set in the database defined by the path.
 	 * 
 	 * @param path
 	 *            The path of the file.
 	 * @return true, if the data set could be deleted.
 	 */
 	public boolean delete(String path);
 }
