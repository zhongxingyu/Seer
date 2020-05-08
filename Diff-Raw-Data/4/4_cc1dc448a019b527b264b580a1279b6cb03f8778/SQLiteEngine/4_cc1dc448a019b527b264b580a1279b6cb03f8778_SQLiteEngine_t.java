 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.heliocentric.sugarsync.LocalStorage;
 
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteException;
 import com.almworks.sqlite4java.SQLiteStatement;
 import java.io.File;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Helio
  */
 public class SQLiteEngine extends StorageEngine {
 	public SQLiteEngine() {
 		this.filename = System.getProperty("java.io.tmpdir") + File.separator + "synchro.db";
 	}
 	public SQLiteEngine(String string) {
 		this.filename = string;
 	}
 	private String filename;
 	private SQLiteConnection DB;
 	private int TransactionLock;
 	@Override
 	public boolean Open() throws StorageEngineException {
 		boolean retval = false;
 		boolean newfile = false;
 		this.TransactionLock = 0;
 		File DBFile = new File(this.filename);
 		if (!DBFile.exists()) {
 			newfile = true;
 		}
 		
 		this.DB = new SQLiteConnection(DBFile);
 		try {
 			this.DB.open(true);
 			if (newfile) {
 				this.BeginTransaction();
 				this.DB.exec("CREATE TABLE config (name VARCHAR(255) PRIMARY KEY ASC, value VARCHAR(255))");
 				this.DB.exec("INSERT INTO config (name, value) VALUES ('schema','1.1.1')");
 				this.DB.exec("INSERT INTO config (name, value) VALUES ('application','synchro-0.1')");
 				this.CommitTransaction();
 			}
 			retval = true;
 		} catch (SQLiteException ex) {
 			Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		this.Upgrade();
 		return retval;
 	}
 	
 	@Override
 	public boolean Upgrade() throws StorageEngineException {
 		boolean retval = false;
 		try {
 			this.BeginTransaction();
 			if (this.GetSchema().equals("1.1.1")) {
 					this.DB.exec("CREATE TABLE fileid (uuid VARCHAR(36) PRIMARY KEY ASC)");
 					this.DB.exec("CREATE TABLE hashlist (uuid VARCHAR(36) PRIMARY KEY ASC, sha256 VARCHAR(64), md5 VARCHAR(32))");
 					this.DB.exec("UPDATE config SET value='1.1.2' WHERE name='schema'");
 			}
 			
 			if (this.GetSchema().equals("1.1.2")) {
 			
 					this.DB.exec("ALTER TABLE hashlist ADD COLUMN hashtype VARCHAR(255)");
 					this.DB.exec("UPDATE config SET value='1.1.3' WHERE name='schema'");
 			}
 			
 			if (this.GetSchema().equals("1.1.3")) {
 				
 					this.DB.exec("CREATE TABLE file_revision (uuid VARCHAR(36) PRIMARY KEY ASC, fileid VARCHAR(36), current_hash VARCHAR(36), previous_hash VARCHAR(36), date TIMESTAMP)");
 					this.DB.exec("UPDATE config SET value='1.1.4' WHERE name='schema'");
 			}
 			
 			if (this.GetSchema().equals("1.1.4")) {
 					this.DB.exec("CREATE TABLE filelist (uuid VARCHAR(36) PRIMARY KEY ASC, domain VARCHAR(36), filename VARCHAR(255), fileid VARCHAR(36))");
 					this.DB.exec("UPDATE config SET value='1.1.5' WHERE name='schema'");
 			
 			}
 			if (this.GetSchema().equals("1.1.5")) {
 					this.DB.exec("CREATE TABLE domain (uuid VARCHAR(36) PRIMARY KEY ASC, localpath VARCHAR(255))");
 					this.DB.exec("UPDATE config SET value='1.1.6' WHERE name='schema'");
 			
 			}
 			if (this.GetSchema().equals("1.1.6")) {
 				this.DB.exec("ALTER TABLE fileid ADD COLUMN domain VARCHAR(36)");
 				this.DB.exec("ALTER TABLE fileid ADD COLUMN path VARCHAR(255)");
 				this.DB.exec("UPDATE config SET value='1.1.7' WHERE name='schema'");
 			
 			}
 			this.CommitTransaction();
 		} catch (SQLiteException ex) {
 			Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		catch (StorageEngineException e) {
 			
 		}
 		return retval;
 	}	
 	@Override
 	public boolean BeginTransaction() {
 		this.TransactionLock += 1;
 		if (this.TransactionLock == 1) {
 			try {
 				this.DB.exec("BEGIN TRANSACTION");
 				return true;
 			} catch (SQLiteException ex) {
 				return false;
 			}
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public boolean RollbackTransaction() {
 		this.TransactionLock -= 1;
 		if (this.TransactionLock <= 0) {
 			try {
 				this.DB.exec("ROLLBACK");
 				return true;
 			} catch (SQLiteException ex) {
 				Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 				return false;
 			}
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public boolean CommitTransaction() {
 		this.TransactionLock -= 1;
 		if (this.TransactionLock <= 0) {
 			try {
 				this.DB.exec("COMMIT");
 				return true;
 			} catch (SQLiteException ex) {
 				Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 				return false;
 			} 
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public String GetSchema() throws StorageEngineException {
 		try {
 		SQLiteStatement st = this.DB.prepare("SELECT value FROM config WHERE name = 'schema'");
 		while (st.step()) {
 			return st.columnString(0);
 		}
 		}
 		catch (SQLiteException e) {
 			throw new StorageEngineException();
 		}
 		return "0";
 	}
 
 
 	@Override
 	protected String GetDomainObject(String Folder) throws StorageEngineException {
 		String retval = "";
 		return retval;
 	}
 
 	@Override
 	public String getAttributeString(StorageObject Object, String Name) {
 		return (String) this.getAttribute(Object, Name, "String");
 	}
 	@Override
 	public boolean setAttributeString(StorageObject Object, String Name, String Value) {
 		boolean retval;
 		try {
 			String query = "UPDATE " + Object.getTable() + " SET " + Name + "='" + Value + "' WHERE uuid='" + Object.getUUID() + "'";
 			this.DB.exec(query);
 			retval = true;
 		} catch (SQLiteException ex) {
 			Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 			retval = false;
 		}
 		return retval;
 	}
 	
 	@Override
 	public Integer getAttributeInt(StorageObject Object, String Name) {
 		return (Integer) this.getAttribute(Object, Name, "Integer");
 	}
 	
 	@Override
 	public boolean setAttributeInt(StorageObject Object, String Name, Integer Value) {
 		return this.setAttributeString(Object, Name, Value.toString());
 	}
 	
 	private Object getAttribute(StorageObject Object, String Name, String Type) {
 		SQLiteStatement st;
 		try {
 			st = this.DB.prepare("SELECT " + Name + " FROM " + Object.getTable() + " WHERE uuid = '" + Object.getUUID() + "'");
 			while (st.step()) {
 				if (Type.equals("String")) {
 					return st.columnString(0);
 				} else if (Type.equals("Integer")) {
 					return st.columnInt(0);
 				}
 			}
 		} catch (SQLiteException ex) {
 			Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		return null;
 	}
 
 	@Override
 	protected String AddFolderRec() throws StorageEngineException {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public FileID getFileID(Domain domain, String file) throws StorageEngineException {
 		FileID fileid = new FileID();
		String uuid = "";
 		SQLiteStatement st;
 		try {
 			st = this.DB.prepare("SELECT uuid FROM fileid WHERE domain = '" + domain.object.getUUID() + "' AND path = '" + file + "'");
 			while (st.step()) {
 				uuid = st.columnString(0);
 			}
 			if (uuid.equals("")) {
				uuid = UUID.randomUUID().toString();
 				this.DB.exec("INSERT INTO fileid (uuid,domain,path) VALUES('" + uuid + "','" + domain.object.getUUID() + "','" + file + "')");
 			}
 		} catch (SQLiteException ex) {
 			Logger.getLogger(SQLiteEngine.class.getName()).log(Level.SEVERE, null, ex);
 			st = null;
 			throw new StorageEngineException();
 		}
 		fileid.object.setEngine(this);
 		fileid.object.setTable("fileid");
 		fileid.object.setUUID(uuid);
 		return fileid;
 	}
 	
 }
