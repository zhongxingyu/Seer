 /**
  * This file is part of Waarp Project.
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
  * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
  * 
  * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  * 
  * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Waarp . If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.waarp.openr66.database.data;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.waarp.common.database.DbPreparedStatement;
 import org.waarp.common.database.DbSession;
 import org.waarp.common.database.data.AbstractDbData;
 import org.waarp.common.database.data.DbValue;
 import org.waarp.common.database.exception.WaarpDatabaseException;
 import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
 import org.waarp.common.database.exception.WaarpDatabaseNoDataException;
 import org.waarp.common.database.exception.WaarpDatabaseSqlException;
 import org.waarp.common.digest.FilesystemBasedDigest;
 import org.waarp.common.utility.WaarpStringUtils;
 import org.waarp.openr66.context.R66Session;
 import org.waarp.openr66.protocol.configuration.Configuration;
 import org.waarp.openr66.protocol.networkhandler.NetworkTransaction;
 
 /**
  * Host Authentication Table object
  * 
  * @author Frederic Bregier
  * 
  */
 public class DbHostAuth extends AbstractDbData {
 	public static enum Columns {
 		ADDRESS, PORT, ISSSL, HOSTKEY, ADMINROLE, ISCLIENT, UPDATEDINFO, HOSTID
 	}
 
 	public static final int[] dbTypes = {
 			Types.VARCHAR, Types.INTEGER, Types.BIT,
 			Types.VARBINARY, Types.BIT, Types.BIT, Types.INTEGER, Types.VARCHAR };
 
 	public static final String table = " HOSTS ";
 
 	/**
 	 * HashTable in case of lack of database
 	 */
 	private static final ConcurrentHashMap<String, DbHostAuth> dbR66HostAuthHashMap =
 			new ConcurrentHashMap<String, DbHostAuth>();
 
 	private String hostid;
 
 	private String address;
 
 	private int port;
 
 	private boolean isSsl;
 
 	private byte[] hostkey;
 
 	private boolean adminrole;
 
 	private boolean isClient;
 
 	private int updatedInfo = UpdatedInfo.UNKNOWN
 			.ordinal();
 
 	// ALL TABLE SHOULD IMPLEMENT THIS
 	public static final int NBPRKEY = 1;
 
 	protected static final String selectAllFields = Columns.ADDRESS
 			.name()
 			+ ","
 			+
 			Columns.PORT
 					.name()
 			+ ","
 			+ Columns.ISSSL
 					.name()
 			+ ","
 			+
 			Columns.HOSTKEY
 					.name()
 			+ ","
 			+
 			Columns.ADMINROLE
 					.name()
 			+ ","
 			+ Columns.ISCLIENT
 					.name()
 			+ ","
 			+
 			Columns.UPDATEDINFO
 					.name()
 			+ ","
 			+
 			Columns.HOSTID
 					.name();
 
 	protected static final String updateAllFields =
 			Columns.ADDRESS
 					.name()
 					+ "=?,"
 					+ Columns.PORT
 							.name()
 					+
 					"=?,"
 					+ Columns.ISSSL
 							.name()
 					+ "=?,"
 					+ Columns.HOSTKEY
 							.name()
 					+
 					"=?,"
 					+ Columns.ADMINROLE
 							.name()
 					+ "=?,"
 					+
 					Columns.ISCLIENT
 							.name()
 					+ "=?,"
 					+
 					Columns.UPDATEDINFO
 							.name()
 					+ "=?";
 
 	protected static final String insertAllValues = " (?,?,?,?,?,?,?,?) ";
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#initObject()
 	 */
 	@Override
 	protected void initObject() {
 		primaryKey = new DbValue[] { new DbValue(hostid, Columns.HOSTID
 				.name()) };
 		otherFields = new DbValue[] {
 				new DbValue(address, Columns.ADDRESS.name()),
 				new DbValue(port, Columns.PORT.name()),
 				new DbValue(isSsl, Columns.ISSSL.name()),
 				new DbValue(hostkey, Columns.HOSTKEY.name()),
 				new DbValue(adminrole, Columns.ADMINROLE.name()),
 				new DbValue(isClient, Columns.ISCLIENT.name()),
 				new DbValue(updatedInfo, Columns.UPDATEDINFO.name()) };
 		allFields = new DbValue[] {
 				otherFields[0], otherFields[1], otherFields[2],
 				otherFields[3], otherFields[4], otherFields[5], otherFields[6], primaryKey[0] };
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#getSelectAllFields()
 	 */
 	@Override
 	protected String getSelectAllFields() {
 		return selectAllFields;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#getTable()
 	 */
 	@Override
 	protected String getTable() {
 		return table;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#getInsertAllValues()
 	 */
 	@Override
 	protected String getInsertAllValues() {
 		return insertAllValues;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#getUpdateAllFields()
 	 */
 	@Override
 	protected String getUpdateAllFields() {
 		return updateAllFields;
 	}
 
 	@Override
 	protected void setToArray() {
 		allFields[Columns.ADDRESS.ordinal()].setValue(address);
 		allFields[Columns.PORT.ordinal()].setValue(port);
 		allFields[Columns.ISSSL.ordinal()].setValue(isSsl);
 		allFields[Columns.HOSTKEY.ordinal()].setValue(hostkey);
 		allFields[Columns.ADMINROLE.ordinal()].setValue(adminrole);
 		allFields[Columns.ISCLIENT.ordinal()].setValue(isClient);
 		allFields[Columns.UPDATEDINFO.ordinal()].setValue(updatedInfo);
 		allFields[Columns.HOSTID.ordinal()].setValue(hostid);
 	}
 
 	@Override
 	protected void setFromArray() throws WaarpDatabaseSqlException {
 		address = (String) allFields[Columns.ADDRESS.ordinal()].getValue();
 		port = (Integer) allFields[Columns.PORT.ordinal()].getValue();
 		isSsl = (Boolean) allFields[Columns.ISSSL.ordinal()].getValue();
 		hostkey = (byte[]) allFields[Columns.HOSTKEY.ordinal()].getValue();
 		adminrole = (Boolean) allFields[Columns.ADMINROLE.ordinal()].getValue();
 		isClient = (Boolean) allFields[Columns.ISCLIENT.ordinal()].getValue();
 		updatedInfo = (Integer) allFields[Columns.UPDATEDINFO.ordinal()]
 				.getValue();
 		hostid = (String) allFields[Columns.HOSTID.ordinal()].getValue();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#getWherePrimaryKey()
 	 */
 	@Override
 	protected String getWherePrimaryKey() {
 		return primaryKey[0].column + " = ? ";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.common.database.data.AbstractDbData#setPrimaryKey()
 	 */
 	@Override
 	protected void setPrimaryKey() {
 		primaryKey[0].setValue(hostid);
 	}
 
 	/**
 	 * @param dbSession
 	 * @param hostid
 	 * @param address
 	 * @param port
 	 * @param isSSL
 	 * @param hostkey
 	 * @param adminrole
 	 * @param isClient
 	 */
 	public DbHostAuth(DbSession dbSession, String hostid, String address, int port,
 			boolean isSSL, byte[] hostkey, boolean adminrole, boolean isClient) {
 		super(dbSession);
 		this.hostid = hostid;
 		this.address = address;
 		this.port = port;
 		this.isSsl = isSSL;
 		if (hostkey == null) {
 			this.hostkey = null;
 		} else {
 			try {
 				// Save as crypted with the local Key and Base64
 				this.hostkey = Configuration.configuration.cryptoKey.cryptToHex(hostkey).getBytes();
 			} catch (Exception e) {
 				this.hostkey = new byte[0];
 			}
 		}
 		this.adminrole = adminrole;
 		this.isClient = isClient;
 		setToArray();
 		isSaved = false;
 	}
 
 	/**
 	 * @param dbSession
 	 * @param hostid
 	 * @throws WaarpDatabaseException
 	 */
 	public DbHostAuth(DbSession dbSession, String hostid) throws WaarpDatabaseException {
 		super(dbSession);
 		this.hostid = hostid;
 		// load from DB
 		select();
 	}
 
 	/**
 	 * Delete all entries (used when purge and reload)
 	 * 
 	 * @param dbSession
 	 * @return the previous existing array of DbRule
 	 * @throws WaarpDatabaseException
 	 */
 	public static DbHostAuth[] deleteAll(DbSession dbSession) throws WaarpDatabaseException {
 		DbHostAuth[] result = getAllHosts(dbSession);
 		if (dbSession == null) {
 			dbR66HostAuthHashMap.clear();
 			return result;
 		}
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(
 				dbSession);
 		try {
 			preparedStatement.createPrepareStatement("DELETE FROM " + table);
 			preparedStatement.executeUpdate();
 			return result;
 		} finally {
 			preparedStatement.realClose();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.openr66.databaseold.data.AbstractDbData#delete()
 	 */
 	@Override
 	public void delete() throws WaarpDatabaseException {
 		if (dbSession == null) {
 			dbR66HostAuthHashMap.remove(this.hostid);
 			isSaved = false;
 			return;
 		}
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(
 				dbSession);
 		try {
 			preparedStatement.createPrepareStatement("DELETE FROM " + table +
 					" WHERE " + getWherePrimaryKey());
 			setPrimaryKey();
 			setValues(preparedStatement, primaryKey);
 			int count = preparedStatement.executeUpdate();
 			if (count <= 0) {
 				throw new WaarpDatabaseNoDataException("No row found");
 			}
 			isSaved = false;
 		} finally {
 			preparedStatement.realClose();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.openr66.databaseold.data.AbstractDbData#insert()
 	 */
 	@Override
 	public void insert() throws WaarpDatabaseException {
 		if (isSaved) {
 			return;
 		}
 		if (dbSession == null) {
 			dbR66HostAuthHashMap.put(this.hostid, this);
 			isSaved = true;
 			return;
 		}
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(
 				dbSession);
 		try {
 			preparedStatement.createPrepareStatement("INSERT INTO " + table +
 					" (" + selectAllFields + ") VALUES " + insertAllValues);
 			setValues(preparedStatement, allFields);
 			int count = preparedStatement.executeUpdate();
 			if (count <= 0) {
 				throw new WaarpDatabaseNoDataException("No row found");
 			}
 			isSaved = true;
 		} finally {
 			preparedStatement.realClose();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.openr66.databaseold.data.AbstractDbData#exist()
 	 */
 	@Override
 	public boolean exist() throws WaarpDatabaseException {
 		if (dbSession == null) {
 			return dbR66HostAuthHashMap.containsKey(hostid);
 		}
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(
 				dbSession);
 		try {
 			preparedStatement.createPrepareStatement("SELECT " +
 					primaryKey[0].column + " FROM " + table + " WHERE " +
 					getWherePrimaryKey());
 			setPrimaryKey();
 			setValues(preparedStatement, primaryKey);
 			preparedStatement.executeQuery();
 			return preparedStatement.getNext();
 		} finally {
 			preparedStatement.realClose();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.openr66.databaseold.data.AbstractDbData#select()
 	 */
 	@Override
 	public void select() throws WaarpDatabaseException {
 		if (dbSession == null) {
 			DbHostAuth host = dbR66HostAuthHashMap.get(this.hostid);
 			if (host == null) {
 				throw new WaarpDatabaseNoDataException("No row found");
 			} else {
 				// copy info
 				for (int i = 0; i < allFields.length; i++) {
 					allFields[i].value = host.allFields[i].value;
 				}
 				setFromArray();
 				isSaved = true;
 				return;
 			}
 		}
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(
 				dbSession);
 		try {
 			preparedStatement.createPrepareStatement("SELECT " +
 					selectAllFields + " FROM " + table + " WHERE " +
 					getWherePrimaryKey());
 			setPrimaryKey();
 			setValues(preparedStatement, primaryKey);
 			preparedStatement.executeQuery();
 			if (preparedStatement.getNext()) {
 				getValues(preparedStatement, allFields);
 				setFromArray();
 				isSaved = true;
 			} else {
 				throw new WaarpDatabaseNoDataException("No row found");
 			}
 		} finally {
 			preparedStatement.realClose();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.openr66.databaseold.data.AbstractDbData#update()
 	 */
 	@Override
 	public void update() throws WaarpDatabaseException {
 		if (isSaved) {
 			return;
 		}
 		if (dbSession == null) {
 			dbR66HostAuthHashMap.put(this.hostid, this);
 			isSaved = true;
 			return;
 		}
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(
 				dbSession);
 		try {
 			preparedStatement.createPrepareStatement("UPDATE " + table +
 					" SET " + updateAllFields + " WHERE " +
 					getWherePrimaryKey());
 			setValues(preparedStatement, allFields);
 			int count = preparedStatement.executeUpdate();
 			if (count <= 0) {
 				throw new WaarpDatabaseNoDataException("No row found");
 			}
 			isSaved = true;
 		} finally {
 			preparedStatement.realClose();
 		}
 	}
 
 	/**
 	 * Private constructor for Commander only
 	 */
 	private DbHostAuth(DbSession session) {
 		super(session);
 	}
 
 	/**
 	 * Get All DbHostAuth from database or from internal hashMap in case of no database support
 	 * 
 	 * @param dbSession
 	 *            may be null
 	 * @return the array of DbHostAuth
 	 * @throws WaarpDatabaseNoConnectionException
 	 * @throws WaarpDatabaseSqlException
 	 */
 	public static DbHostAuth[] getAllHosts(DbSession dbSession)
 			throws WaarpDatabaseNoConnectionException, WaarpDatabaseSqlException {
 		if (dbSession == null) {
 			DbHostAuth[] result = new DbHostAuth[0];
 			return dbR66HostAuthHashMap.values().toArray(result);
 		}
 		String request = "SELECT " + selectAllFields;
 		request += " FROM " + table;
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(dbSession, request);
 		ArrayList<DbHostAuth> dbArrayList = new ArrayList<DbHostAuth>();
 		preparedStatement.executeQuery();
 		while (preparedStatement.getNext()) {
 			DbHostAuth hostAuth = getFromStatement(preparedStatement);
 			dbArrayList.add(hostAuth);
 		}
 		preparedStatement.realClose();
 		DbHostAuth[] result = new DbHostAuth[0];
 		return dbArrayList.toArray(result);
 	}
 
 	/**
 	 * For instance from Commander when getting updated information
 	 * 
 	 * @param preparedStatement
 	 * @return the next updated DbHostAuth
 	 * @throws WaarpDatabaseNoConnectionException
 	 * @throws WaarpDatabaseSqlException
 	 */
 	public static DbHostAuth getFromStatement(DbPreparedStatement preparedStatement)
 			throws WaarpDatabaseNoConnectionException, WaarpDatabaseSqlException {
 		DbHostAuth dbHostAuth = new DbHostAuth(preparedStatement.getDbSession());
 		dbHostAuth.getValues(preparedStatement, dbHostAuth.allFields);
 		dbHostAuth.setFromArray();
 		dbHostAuth.isSaved = true;
 		return dbHostAuth;
 	}
 
 	/**
 	 * 
 	 * @return the DbPreparedStatement for getting Updated Object
 	 * @throws WaarpDatabaseNoConnectionException
 	 * @throws WaarpDatabaseSqlException
 	 */
 	public static DbPreparedStatement getUpdatedPrepareStament(DbSession session)
 			throws WaarpDatabaseNoConnectionException, WaarpDatabaseSqlException {
 		String request = "SELECT " + selectAllFields;
 		request += " FROM " + table +
 				" WHERE " + Columns.UPDATEDINFO.name() + " = " +
 				AbstractDbData.UpdatedInfo.TOSUBMIT.ordinal();
 		DbPreparedStatement prep = new DbPreparedStatement(session, request);
 		return prep;
 	}
 
 	/**
 	 * 
 	 * @param session
 	 * @param host
 	 * @param addr
 	 * @param ssl
 	 * @return the DbPreparedStatement according to the filter
 	 * @throws WaarpDatabaseNoConnectionException
 	 * @throws WaarpDatabaseSqlException
 	 */
 	public static DbPreparedStatement getFilterPrepareStament(DbSession session,
 			String host, String addr, boolean ssl)
 			throws WaarpDatabaseNoConnectionException, WaarpDatabaseSqlException {
 		DbPreparedStatement preparedStatement = new DbPreparedStatement(session);
 		String request = "SELECT " + selectAllFields + " FROM " + table + " WHERE ";
 		String condition = null;
 		if (host != null) {
 			condition = Columns.HOSTID.name() + " LIKE '%" + host + "%' ";
 		}
 		if (addr != null) {
 			if (condition != null) {
 				condition += " AND ";
 			} else {
 				condition = "";
 			}
 			condition += Columns.ADDRESS.name() + " LIKE '%" + addr + "%' ";
 		}
 		if (condition != null) {
 			condition += " AND ";
 		} else {
 			condition = "";
 		}
 		condition += Columns.ISSSL.name() + " = ?";
 		preparedStatement.createPrepareStatement(request + condition +
 				" ORDER BY " + Columns.HOSTID.name());
 		try {
 			preparedStatement.getPreparedStatement().setBoolean(1, ssl);
 		} catch (SQLException e) {
 			preparedStatement.realClose();
 			throw new WaarpDatabaseSqlException(e);
 		}
 		return preparedStatement;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.waarp.openr66.databaseold.data.AbstractDbData#changeUpdatedInfo(UpdatedInfo)
 	 */
 	@Override
 	public void changeUpdatedInfo(UpdatedInfo info) {
 		if (updatedInfo != info.ordinal()) {
 			updatedInfo = info.ordinal();
 			allFields[Columns.UPDATEDINFO.ordinal()].setValue(updatedInfo);
 			isSaved = false;
 		}
 	}
 
 	/**
 	 * Is the given key a valid one
 	 * 
 	 * @param newkey
 	 * @return True if the key is valid (or any key is valid)
 	 */
 	public boolean isKeyValid(byte[] newkey) {
 		// It is valid to not have a key
 		if (this.hostkey == null) {
 			return true;
 		}
 		if (newkey == null) {
 			return false;
 		}
 		try {
 			return FilesystemBasedDigest.equalPasswd(
 					Configuration.configuration.cryptoKey.decryptHexInBytes(this.hostkey),
 					newkey);
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * @return the hostkey
 	 */
 	public byte[] getHostkey() {
 		if (hostkey == null) {
 			return null;
 		}
 		try {
 			return Configuration.configuration.cryptoKey.decryptHexInBytes(hostkey);
 		} catch (Exception e) {
 			return new byte[0];
 		}
 	}
 
 	/**
 	 * @return the adminrole
 	 */
 	public boolean isAdminrole() {
 		return adminrole;
 	}
 
 	/**
 	 * Test if the address is 0.0.0.0 for a client or isClient
 	 * 
 	 * @return True if the address is a client address (0.0.0.0) or isClient
 	 */
 	public boolean isClient() {
 		return isClient || (this.address.equals("0.0.0.0"));
 	}
 
 	/**
 	 * True if the address is a client address (0.0.0.0)
 	 * 
 	 * @return True if the address is a client address (0.0.0.0)
 	 */
 	public boolean isNoAddress() {
 		return (this.address.equals("0.0.0.0"));
 	}
 
 	/**
 	 * 
 	 * @return the SocketAddress from the address and port
 	 */
 	public SocketAddress getSocketAddress() {
 		return new InetSocketAddress(this.address, this.port);
 	}
 
 	/**
 	 * 
 	 * @return True if this Host ref is with SSL support
 	 */
 	public boolean isSsl() {
 		return this.isSsl;
 	}
 
 	/**
 	 * @return the hostid
 	 */
 	public String getHostid() {
 		return hostid;
 	}
 
 	/**
 	 * @return the address
 	 */
 	public String getAddress() {
 		return address;
 	}
 
 	/**
 	 * @return the port
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	@Override
 	public String toString() {
		//System.err.println(hostid+" Version: "+Configuration.configuration.versions.get(hostid)+":"+Configuration.configuration.versions.containsKey(hostid));
 		return "HostAuth: " + hostid + " address: " + address + ":" + port + " isSSL: " + isSsl +
 				" admin: " + adminrole + " isClient: " + isClient + " ("
				+ (hostkey != null ? hostkey.length : 0) + ") Version: "
				+ (Configuration.configuration.versions.containsKey(hostid) ? 
						Configuration.configuration.versions.get(hostid) :
							"Unknown");
 	}
 
 	/**
 	 * @param session
 	 * @param body
 	 * @param crypted
 	 *            True if the Key is kept crypted, False it will be in clear form
 	 * @return the runner in Html format specified by body by replacing all instance of fields
 	 */
 	public String toSpecializedHtml(R66Session session, String body, boolean crypted) {
 		StringBuilder builder = new StringBuilder(body);
 		WaarpStringUtils.replace(builder, "XXXHOSTXXX", hostid);
 		WaarpStringUtils.replace(builder, "XXXADDRXXX", address);
 		WaarpStringUtils.replace(builder, "XXXPORTXXX", Integer.toString(port));
 		if (crypted) {
 			WaarpStringUtils.replace(builder, "XXXKEYXXX", new String(hostkey));
 		} else {
 			try {
 				WaarpStringUtils.replace(builder, "XXXKEYXXX",
 						Configuration.configuration.cryptoKey.decryptHexInString(new String(
 								this.hostkey)));
 			} catch (Exception e) {
 				WaarpStringUtils.replace(builder, "XXXKEYXXX", "BAD DECRYPT");
 			}
 		}
 		WaarpStringUtils.replace(builder, "XXXSSLXXX", isSsl ? "checked" : "");
 		WaarpStringUtils.replace(builder, "XXXADMXXX", adminrole ? "checked" : "");
 		WaarpStringUtils.replace(builder, "XXXISCXXX", isClient ? "checked" : "");
 		int nb = NetworkTransaction.existConnection(getSocketAddress(), getHostid());
 		WaarpStringUtils.replace(builder, "XXXCONNXXX", (nb > 0)
 				? "(" + nb + " Connected) " : "");
 		return builder.toString();
 	}
 }
