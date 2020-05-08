 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package org.ow2.play.service.registry.mongo;
 
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jws.WebMethod;
 
 import org.ow2.play.service.registry.api.Registry;
 import org.ow2.play.service.registry.api.RegistryException;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.ServerAddress;
 
 /**
  * Stores and read data from the serviceregistry Mongo collection.
  * 
  * @author chamerling
  * 
  */
 public class RegistryImpl implements Registry {
 
 	private final static String DEFAULT_MONGO_DB_HOSTNAME = "localhost";
 	private final static String DEFAULT_MONGO_DB_PORT = "27017";
 	private final static String DEFAULT_MONGO_DB_DATABASE_NAME = "play";
 	private final static String DEFAULT_MONGO_DB_COLLECTION_NAME = "serviceregistry";
 
 	private final static String URL_KEY = "url";
 	private final static String NAME_KEY = "name";
 
 	private String hostname = DEFAULT_MONGO_DB_HOSTNAME;
 	private String port = DEFAULT_MONGO_DB_PORT;
 	private String databaseName = DEFAULT_MONGO_DB_DATABASE_NAME;
 	private String collectionName = DEFAULT_MONGO_DB_COLLECTION_NAME;
 	private String userName;
 	private String password;
 	private Mongo mongo;
 	private DBCollection collection;
 
 	private Properties properties;
 
 	private boolean initialized = false;
 
 	private static Logger logger = Logger.getLogger(RegistryImpl.class
 			.getName());
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.play.service.registry.api.Registry#init()
 	 */
 	@Override
 	@WebMethod
 	public void init() throws RegistryException {
 		logger.info("Initializing registry service");
 
 		if (mongo != null) {
 			close();
 		}
 
 		if (properties != null) {
 			logger.fine("Getting properties from " + properties);
 			hostname = properties.getProperty("mongo.hostname",
 					DEFAULT_MONGO_DB_HOSTNAME);
 			port = properties.getProperty("mongo.port", DEFAULT_MONGO_DB_PORT);
 			userName = properties.getProperty("mongo.username", userName);
 			password = properties.getProperty("mongo.password", password);
 			collectionName = properties.getProperty("mongo.collection",
 					DEFAULT_MONGO_DB_COLLECTION_NAME);
 		}
 
 		if (logger.isLoggable(Level.INFO)) {
 			logger.info(String.format(
 					"Connection to %s %s with credentials %s %s", hostname,
 					port, userName, "******"));
 		}
 
 		List<ServerAddress> addresses = getServerAddresses(hostname, port);
 		logger.fine("Got server addresses " + addresses);
 		mongo = getMongo(addresses);
 
 		DB database = getDatabase(mongo, databaseName);
 
 		if (userName != null && userName.trim().length() > 0) {
 			if (!database.authenticate(userName, password.toCharArray())) {
 				throw new RuntimeException(
 						"Unable to authenticate with MongoDB server.");
 			}
 
 			// Allow password to be GCed
 			password = null;
 		}
 
 		setCollection(database.getCollection(collectionName));
 		initialized = true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.play.service.registry.api.Registry#get(java.lang.String)
 	 */
 	@Override
 	@WebMethod
 	public String get(String name) throws RegistryException {
 		String url = null;
 
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine(String.format("Get url for name %s", name));
 		}
 		checkInitialized();
 
 		DBObject filter = new BasicDBObject();
 		filter.put(NAME_KEY, name);
 
 		DBObject result = collection.findOne(filter);
 		if (result != null && result.get(URL_KEY) != null) {
 			url = result.get(URL_KEY).toString();
 		}
 		return url;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.play.service.registry.api.Registry#put(java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	@WebMethod
 	public void put(String name, String url) throws RegistryException {
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine(String.format("Put url %s for name %s", url, name));
 		}
 		checkInitialized();
 
 		if (name == null || url == null) {
 			throw new RegistryException(
 					"Can not put null values name = %s, url = %s", name, url);
 		}
 
 		// update the entry if it already exists
 		DBObject filter = new BasicDBObject();
 		filter.put(NAME_KEY, name);
 
 		DBObject filtered = collection.findOne(filter);
 		if (filtered != null) {
 			filtered.put(URL_KEY, url);
 			collection.save(filtered);
 		} else {
 			DBObject o = new BasicDBObject();
 			o.put(NAME_KEY, name);
 			o.put(URL_KEY, url);
 			collection.insert(o);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.play.service.registry.api.Registry#keys()
 	 */
 	@Override
 	@WebMethod
 	public List<String> keys() throws RegistryException {
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine("Get keys");
 		}
 
 		checkInitialized();
 
 		List<String> result = new ArrayList<String>();
 
 		DBCursor cursor = collection.find();
 		Iterator<DBObject> iter = cursor.iterator();
 		while (iter.hasNext()) {
 			DBObject dbObject = iter.next();
 			if (dbObject != null && dbObject.get(NAME_KEY) != null) {
 				result.add(dbObject.get(NAME_KEY).toString());
 			}
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.play.service.registry.api.Registry#clear()
 	 */
 	@Override
 	@WebMethod
 	public void clear() throws RegistryException {
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine("Clear");
 		}
 		checkInitialized();
 
 		throw new RegistryException("Not implemented");
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ow2.play.service.registry.api.Registry#load(java.lang.String)
 	 */
 	@Override
 	@WebMethod
 	public void load(String url) throws RegistryException {
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine(String.format("Load from url %s", url));
 		}
 
 		if (url == null) {
 			throw new RegistryException(
 					"Can not load properties from null value");
 		}
 
 		// This is the only time we initialize if not already done...
 		if (!initialized) {
 			init();
 		}
 
 		Properties props = new Properties();
 		try {
 			URL u = new URL(url);
 			props.load(u.openStream());
 
 			for (Object key : props.keySet()) {
 				String k = key.toString();
 				Object v = props.getProperty(k);
 				if (v != null) {
 					this.put(k, v.toString());
 				}
 			}
 
 		} catch (Exception e) {
 			logger.warning(e.getMessage());
 			throw new RegistryException(e);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	protected void checkInitialized() throws RegistryException {
 		if (!initialized) {
 			throw new RegistryException("Registry has not been initialized");
 		}
 	}
 
 	/**
 	 * Set the connection properties
 	 * 
 	 * @param props
 	 */
 	public void setProperties(Properties props) {
 		this.properties = props;
 	}
 
 	/*
 	 * This method could be overridden to provide the DB instance from an
 	 * existing connection.
 	 */
 	protected DB getDatabase(Mongo mongo, String databaseName) {
 		return mongo.getDB(databaseName);
 	}
 
 	protected DBCollection getDbCollection() {
 		return this.collection;
 	}
 
 	/*
 	 * This method could be overridden to provide the Mongo instance from an
 	 * existing connection or for unit test with some mock libs
 	 */
 	protected Mongo getMongo(List<ServerAddress> addresses) {
 		if (addresses.size() == 1) {
 			return new Mongo(addresses.get(0));
 		} else {
 			// Replica set
 			return new Mongo(addresses);
 		}
 	}
 
 	protected void close() {
 		if (mongo != null) {
 			collection = null;
 			mongo.close();
 		}
 	}
 
 	/**
 	 * Note: this method is primarily intended for use by the unit tests.
 	 * 
 	 * @param collection
 	 *            The MongoDB collection to use when logging events.
 	 */
 	public void setCollection(final DBCollection collection) {
 		assert collection != null : "collection must not be null";
 
 		this.collection = collection;
 	}
 
 	/**
 	 * Returns a List of ServerAddress objects for each host specified in the
 	 * hostname property. Returns an empty list if configuration is detected to
 	 * be invalid, e.g.:
 	 * <ul>
 	 * <li>Port property doesn't contain either one port or one port per host</li>
 	 * <li>After parsing port property to integers, there isn't either one port
 	 * or one port per host</li>
 	 * </ul>
 	 * 
 	 * @param hostname
 	 *            Blank space delimited hostnames
 	 * @param port
 	 *            Blank space delimited ports. Must specify one port for all
 	 *            hosts or a port per host.
 	 * @return List of ServerAddresses to connect to
 	 */
 	private List<ServerAddress> getServerAddresses(String hostname, String port) {
 		List<ServerAddress> addresses = new ArrayList<ServerAddress>();
 
 		String[] hosts = hostname.split(" ");
 		String[] ports = port.split(" ");
 
 		if (ports.length != 1 && ports.length != hosts.length) {
 			// errorHandler
 			// .error("MongoDB appender port property must contain one port or a port per host",
 			// null, ErrorCode.ADDRESS_PARSE_FAILURE);
 		} else {
 			List<Integer> portNums = getPortNums(ports);
 			// Validate number of ports again after parsing
 			if (portNums.size() != 1 && portNums.size() != hosts.length) {
 				// error("MongoDB appender port property must contain one port or a valid port per host",
 				// null, ErrorCode.ADDRESS_PARSE_FAILURE);
 			} else {
 				boolean onePort = (portNums.size() == 1);
 
 				int i = 0;
 				for (String host : hosts) {
 					int portNum = (onePort) ? portNums.get(0) : portNums.get(i);
 					try {
 						addresses.add(new ServerAddress(host.trim(), portNum));
 					} catch (UnknownHostException e) {
 						// errorHandler
 						// .error("MongoDB appender hostname property contains unknown host",
 						// e, ErrorCode.ADDRESS_PARSE_FAILURE);
 					}
 					i++;
 				}
 			}
 		}
 		return addresses;
 	}
 
 	private List<Integer> getPortNums(String[] ports) {
 		List<Integer> portNums = new ArrayList<Integer>();
 
 		for (String port : ports) {
 			try {
 				Integer portNum = Integer.valueOf(port.trim());
 				if (portNum < 0) {
 					// errorHandler
 					// .error("MongoDB appender port property can't contain a negative integer",
 					// null, ErrorCode.ADDRESS_PARSE_FAILURE);
 				} else {
 					portNums.add(portNum);
 				}
 			} catch (NumberFormatException e) {
 				// errorHandler
 				// .error("MongoDB appender can't parse a port property value into an integer",
 				// e, ErrorCode.ADDRESS_PARSE_FAILURE);
 			}
 
 		}
 
 		return portNums;
 	}
 
 }
