 package org.publicmain.sql;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.publicmain.common.Config;
 import org.publicmain.common.LogEngine;
 
 
 public class BackupDBConnection {
 
 	private static BackupDBConnection me;
 	
 	private Statement 	stmt;
 	private int 		backUpDBStatus;
 	private long 		warteZeitInSec;
 	private boolean		successfulConnected;
 	private int 		maxVersuche;
 	
 	private BackupDBConnection() {
 		
 		this.backUpDBStatus		= 0;									// Status: 1 verbunden und bereit
 		this.warteZeitInSec 	= 10;
 		this.maxVersuche 		= 5;
 		this.successfulConnected = false;
 		
 		//connectToBackupDBServer();
 
 		//Config.getConfig().setBackupDBChoosenUsername("rene");
 		//Config.getConfig().setBackupDBChoosenUserPassWord("rene");
 		//Config.write();
 		//createUser(Config.getConfig().getBackupDBChoosenUsername(), Config.getConfig().getBackupDBChoosenUserPassWord());
 		
 	}
 	// Verbindungsdaten
 //	private Connection 	con;
 	
 	public static BackupDBConnection getBackupDBConnection() {
 		if (me == null) {
 			me = new BackupDBConnection();
 		}
 		return me;
 	}
 	
 	private long getMyID() {
 		String username = Config.getConfig().getBackupDBChoosenUsername();
 		String password = Config.getConfig().getBackupDBChoosenUserPassWord();
 		long tmpID=-1;
 		synchronized (stmt) {
 			try {
 				PreparedStatement prp = getCon().prepareStatement("Select backupUserID from t_backupUser where username like ? and password like ?");
 				prp.setString(1, username);
 				prp.setString(2, password);
 				ResultSet myid = prp.executeQuery();
 				if (myid.first()) tmpID=myid.getLong(1);
 			} catch (SQLException e) {
 				LogEngine.log(this, e);
 				return -1;
 			}
 			return tmpID;
 		}
 	
 	}
 
 	
 	
 	
 	
 	// push & pull fr ResultSets
 	
 	private synchronized boolean connectToBackupDBServer(){
 		new Thread(new Runnable() {
 			int versuche = 0;
 			public void run() {
 				while ((versuche < maxVersuche) && (backUpDBStatus == 0)) {
 					try {
 						
 						stmt = getCon().createStatement();
 						stmt.execute("use db_publicMain_backup");
 						LogEngine.log("BackupDBConnection", "DB-BackupServerConnection as " + Config.getConfig().getBackupDBUser() + " successful", LogEngine.INFO);
 						backUpDBStatus = 3;
 						successfulConnected = true;
 					} catch (SQLException e) {
 						try {
 							Thread.sleep(warteZeitInSec * 1000);
 						} catch (InterruptedException e1) {
 							LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
 						}
 						versuche ++;
 						backUpDBStatus = 0;
 						LogEngine.log(this, "Error while connecting to BackupDB. Try again in:" + warteZeitInSec + " " + versuche+1 + "/"+ maxVersuche + e.getMessage(), LogEngine.ERROR);
 						successfulConnected = false;
 					}
 				}
 			}
 		
 		}).start();	
 		if(!successfulConnected){
 			LogEngine.log(this, "Connecting to BackupDB failt - will not try again", LogEngine.ERROR);
 		}
 		return successfulConnected;
 	}
 	
 	public ResultSet pull_settings(){
 		
 		if(backUpDBStatus >= 3 && Config.getConfig().getBackupDBChoosenUsername()!= null) {
 			try {
 				return stmt.executeQuery("SELECT * FROM v_searchInHistory WHERE fk_t_backupUser_backupUserID_2 LIKE '" + getMyID() + "'");
 			} catch (SQLException e) {
 				LogEngine.log(this, "Error while pulling settings from backupDB " + e.getMessage(), LogEngine.ERROR );
 			}
 		}
 		return null;
 	}
 	
 	
 	
 	public synchronized boolean push_msgs(ResultSet tmp_messages) {
 		long myID = getMyID();
 		if(myID !=-1){
 			try {
 				PreparedStatement prp = getCon().prepareStatement("Insert ignore into t_messages(fk_t_backupUser_backupUserID,msgID , timestmp, fk_t_users_userID_sender, displayName, groupName, fk_t_users_userID_empfaenger, txt,  fk_t_msgType_ID) values(?,?,?,?,?,?,?,?,?)");
 				while (tmp_messages.next()){
 					prp.setLong(1, myID);
 					prp.setInt(2,tmp_messages.getInt(1));
 					prp.setLong(3, tmp_messages.getLong(2));
 					prp.setLong(4,tmp_messages.getLong(3));
 					prp.setString(5, tmp_messages.getString(4));
 					prp.setString(6, tmp_messages.getString(7));
 					long tmp = tmp_messages.getLong(6);
 					if(tmp_messages.wasNull()){
 						 prp.setNull(7, java.sql.Types.BIGINT);
 					}else{
 						prp.setLong(7, tmp);
 					}
 					prp.setString(8,tmp_messages.getString(5));
 					prp.setInt(9,tmp_messages.getInt(8));
 					prp.addBatch();
 				}
 				prp.executeBatch();
 				prp.close();
 				return true;
 
 			} catch (SQLException e) {
 				LogEngine.log(this, e);
 				return false;
 
 			}
 			
 		}
 		else {
 			LogEngine.log(this, "BackUpUser nicht vorhanden, ANLEGEN!", LogEngine.INFO);
 			return false;
 		}
 	}
 	
 	
 	
 	
 	public synchronized boolean push_users(ResultSet tmp_users) {
 		long myID = getMyID();
 		if(myID !=-1){
 			try {
 				PreparedStatement prp = getCon().prepareStatement("Insert ignore into t_users(userID, displayName, userName) values(?,?,?)");
 				while (tmp_users.next()){
 					prp.setLong(1, tmp_users.getLong(1));
 					prp.setString(2, tmp_users.getString(2));
 					prp.setString(3, tmp_users.getString(3));
 					prp.addBatch();
 				}
 				prp.executeBatch();
 				prp.close();
 
 			} catch (SQLException e) {
 				LogEngine.log(this, e);
 
 			}
 
 		}
 		else return false;
 		return true;	
 	}
 
 	public synchronized boolean push_settings(ResultSet tmp_settings) {
 		long myID = getMyID();
 		if(myID !=-1){
 			try {
 				PreparedStatement prp = getCon().prepareStatement("Insert ignore into t_settings(settingsKey, settingsValue, fk_t_backupUser_backupUserID_2) values(?,?,?)");
 				while (tmp_settings.next()){
 					prp.setString(1, tmp_settings.getString(1));
 					prp.setString(2, tmp_settings.getString(3));
					prp.setLong(3, myID);
 					prp.addBatch();
 				}
 				prp.executeBatch();
 				prp.close();
 
 			} catch (SQLException e) {
 				LogEngine.log(this, e);
 
 			}
 
 		}
 		else return false;
 		return true;
 	}
 
 	
 	
 	
 	
 	public boolean getStatus() {
 		//returns true if Backupserver is connected and User in config is present
 		//Displays a Dialog for Userdata if Connection is present but Userdata not
 		// TODO Auto-generated method stub
 		if (backUpDBStatus >= 3){
 			return true;
 		}
 		connectToBackupDBServer();
 		return false;
 	}
 	
 	private synchronized boolean userexists(String userName) throws SQLException{
 				PreparedStatement prp = getCon().prepareStatement("Select backupUserID from t_backupUser where username like ?");
 				prp.setString(1, userName);
 				ResultSet tmp_rs= prp.executeQuery();
 				boolean tmp = (tmp_rs.next());
 				prp.close();
 				return tmp;
 	}
 	
 	public synchronized boolean createUser (String usrName, String passwd){
 		try {
 			if(!userexists(usrName)){
 			PreparedStatement prp = getCon().prepareStatement("Insert into t_backupUser(username,password) values(?,?)");
 			prp.setString(1, usrName);
 			prp.setString(2, passwd);
 			prp.execute();
 			prp.close();
 			return true;
 			}
 			else{
 				LogEngine.log(this, "BackupUser already exists",LogEngine.ERROR);
 			}
 		} catch (SQLException e) {
 			LogEngine.log(this, e);
 		}
 		return false;
 		
 	}
 
 	public Connection getCon() throws SQLException {
 		return DriverManager.getConnection("jdbc:mysql://"+Config.getConfig().getBackupDBIP()+":"+ Config.getConfig().getBackupDBPort()+"/"+Config.getConfig().getBackupDBDatabasename(), Config.getConfig().getBackupDBUser(), Config.getConfig().getBackupDBPw());
 	}
 
 }
