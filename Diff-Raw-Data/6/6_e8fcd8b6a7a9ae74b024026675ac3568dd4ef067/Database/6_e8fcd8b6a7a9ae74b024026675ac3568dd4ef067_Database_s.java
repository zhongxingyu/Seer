 package org.sdu.database;
 
 import java.net.InetAddress;
 import java.sql.*;
 
 import org.sdu.server.DatabaseInterface;
 
 /**
  * Access database.
  * 
  * @version 0.1 rev 8008 Jan. 13, 2013
  * Copyright (c) HyperCube Dev Team
  */
 public class Database implements DatabaseInterface {
 	private final String mainDatabase = "hypercube";
 	private final String infoTable = "info";
 	private Statement statement;
 	private Connection conn;
 
 	public Database() throws Exception {
 		// Read configuration file
 		Configure.read();
 
 		// Connect to database
 		String url = "jdbc:mysql://" + Configure.databaseAddress + "/"
 				+ mainDatabase + "?characterEncoding=utf8";
 		Class.forName("com.mysql.jdbc.Driver");
 		conn = DriverManager.getConnection(url, Configure.user,
 				Configure.password);
 		statement = conn.createStatement();
 	}
 
 	public void close() {
 		try {
 			statement.close();
 			conn.close();
 		} catch (Exception e) {
 		}
 	}
 
 	public int getCount() throws Exception {
 		int num = 0;
 		ResultSet count = statement.executeQuery("select count(*) from "
 				+ infoTable);
 		count.next();
 		num = count.getInt(1);
 		count.close();
 		return num;
 	}
 
 	public void deleteUser(String id) throws Exception {
 		statement
 				.execute("delete from " + infoTable + " where id='" + id + "'");
 	}
 
 	public boolean checkExist(String id) throws Exception {
 		boolean flag = false;
 		ResultSet rs = statement.executeQuery("select * from " + infoTable
 				+ " where id='" + id + "'");
 		if (rs.next())
 			flag = true;
 		return flag;
 	}
 
 	public boolean checkPassword(String id, String password) throws Exception {
 		boolean flag = false;
 		ResultSet rs = statement.executeQuery("select * from " + infoTable
 				+ " where id='" + id + "'");
 		if (rs.next() && (rs.getString("password").equals(password)))
 			flag = true;
 		return flag;
 	}
 
 	public void setOnline(String id, boolean visible) throws Exception {
 		int flag;
 		if (visible)
 			flag = 1;
 		else
 			flag = 0;
 		statement.executeUpdate("update " + infoTable
 				+ " set online=1, visible=" + flag + " where id='" + id + "'");
 	}
 
 	public void setOffline(String id) throws Exception {
 		statement.executeUpdate("update " + infoTable
 				+ " set online=0 where id='" + id + "'");
 	}
 
 	public void setVisible(String id, boolean visible) throws Exception {
 		int flag;
 		if (visible)
 			flag = 1;
 		else
 			flag = 0;
 		statement.executeUpdate("update " + infoTable + " visible=" + flag
 				+ " where id='" + id + "'");
 	}
 
 	public void setNickname(String id, String nickname) throws Exception {
 		statement.executeUpdate("update " + infoTable + " nickname='"
 				+ nickname + "' where id='" + id + "'");
 	}
 
 	public boolean getOnline(String id) throws Exception {
 		boolean flag = false;
 		ResultSet rs = statement.executeQuery("select * from " + infoTable
 				+ " where id='" + id + "'");
 		if (rs.next() && (rs.getInt("online") == 1))
 			flag = true;
 		return flag;
 	}
 
 	public boolean getVisible(String id) throws Exception {
 		boolean flag = false;
 		ResultSet rs = statement.executeQuery("select * from " + infoTable
 				+ " where id='" + id + "'");
 		if (rs.next() && (rs.getInt("visible") == 1))
 			flag = true;
 		return flag;
 	}
 
 	public String getNickname(String id) throws Exception {
 		ResultSet rs = statement.executeQuery("select * from " + infoTable
 				+ " where id='" + id + "'");
 		rs.next();
 		return rs.getString("nickname");
 	}
 
 	@Override
 	public void setOnline(String id, boolean visible, InetAddress IP)
 			throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setNotification(String Notification, String id)
 			throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String[] getUserGroup(String SQL_Query) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setMessage(String Message, String id) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setVisible(String id) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setInvisible(String id) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void chatdatabackup(String id, String Data) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String[] getNotification(String id, long timestamp) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setFreeze(String id) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean Freeze(String id) throws Exception {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
