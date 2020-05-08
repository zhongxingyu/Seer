 package com.swoop.data.mongo;
 
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 import com.mongodb.MongoURI;
 import com.mongodb.ReadPreference;
 
 import com.swoop.data.util.Connection;
 
 import java.io.IOException;
 
 /**
  * Basic MongoDB connection utility, for internal package use.
  */
 class MongoConnection
 	implements Connection<DB>
 {
 	private MongoURI uri;
 	private Mongo mongo;
 	private DB db;
 
 	/**
 	 * Default constructor.  Connects to the default local MongoDB instance.
 	 */
	@SuppressWarnings("deprecation")
 	MongoConnection()
 	{
 		this.uri = new MongoURI("mongodb://localhost/test");
 	}
 
 	/**
 	 * Constructor.
 	 * @param uriString
 	 *    the connection information, encoded as a MongoDB URI  (see MongoDB driver documentation)
 	 */
	@SuppressWarnings("deprecation")
 	MongoConnection(String uriString)
 	{
 		this.uri = new MongoURI(uriString);
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public boolean isOpen()
 	{
 		return db != null;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
	@SuppressWarnings("deprecation")
 	@Override
 	public DB open()
 		throws IOException
 	{
 		try {
 			if (db == null) {
 				this.mongo = uri.connect();
 				this.mongo.setReadPreference(ReadPreference.SECONDARY);
 				String database = uri.getDatabase();
 				if (database == null) {
 					throw new IOException("no DB specified");
 				}
 				this.db = this.mongo.getDB(database);
 				authenticate(uri);
 			}
 			return db;
 		}
 		catch (java.net.UnknownHostException e) {  // why this type doesn't extend IOException beats me.
 			throw new IOException(e);
 		}
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void close()
 	{
 		if (mongo != null) {
 			mongo.close();
 			mongo = null;
 		}
 		db = null;
 	}
 
 	@Override
 	public String toString()
 	{
 		// DO NOT LEAK PASSWORD HERE!
 		return "[MongoDB:" + uri.getHosts() + "," + uri.getDatabase() + "]";
 	}
 
 	//
 	// Note a minor flaw in this system: the Java MongoDB driver internally caches Mongo DB connections
 	// per network address.  So here we may receive a DB object that has been previously authenticated
 	// by a different MongoConnection.  Configuring multiple MongoConnections to the same server but with
 	// different authentication info may lead to mysterious authentication failures - mysterious because
 	// of the order dependency.
 	//
 	private void authenticate(MongoURI uri)
 	{
 		String userName = uri.getUsername();
 		if (userName != null && userName.length() > 0 && !db.isAuthenticated()) {
 			db.authenticate(userName, uri.getPassword());
 		}
 	}
 }
