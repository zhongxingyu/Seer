 package com.amm.nosql.dao.couchbase;
 
 import java.util.*;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import net.spy.memcached.internal.OperationFuture;
 
 import org.apache.log4j.Logger;
 import com.amm.nosql.data.NoSqlEntity;
 import com.amm.nosql.dao.NoSqlDao;
 import com.amm.mapper.ObjectMapper;
 import com.amm.nosql.NoSqlException;
 import com.couchbase.client.CouchbaseClient;
 import com.couchbase.client.CouchbaseConnectionFactory;
 import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
 
 /**
  * Couchbase implementation.
  * @author rhariharan
  */
 public class CouchbaseDao<T extends NoSqlEntity> implements NoSqlDao<T> {
 	private static final Logger logger = Logger.getLogger(CouchbaseDao.class);
 	private CouchbaseClient client ;
 	private String hostnames ;
 	private int port = 8091 ;
 	private int expiration ;
 	private long timeout ;
 	private String bucketname;
 	private String password = "" ;
 	private ObjectMapper<T> entityMapper;
 	private CouchbaseConnectionFactoryBuilder connectionFactoryBuilder = new CouchbaseConnectionFactoryBuilder();
 	
 	public CouchbaseDao(String hostname, int expiration, long timeout, String bucketname, ObjectMapper<T> entityMapper) throws IOException {
 		this.hostnames = hostname ;
 		this.expiration = expiration ;
 		this.timeout = timeout ;
 		this.bucketname = bucketname;
 		this.entityMapper = entityMapper ;
 		createClient();
 	}
 
 	public CouchbaseDao(String hostname, int port, int expiration, long timeout, String bucketname, ObjectMapper<T> entityMapper) throws IOException { 
 		this.hostnames = hostname ;
 		this.port = port ;
 		this.expiration = expiration ;
 		this.timeout = timeout ;
 		this.bucketname = bucketname;
 		this.entityMapper = entityMapper ;
 		createClient();
 	}
 
 	private void createClient() throws IOException {
 		connectionFactoryBuilder.setOpTimeout(timeout);
 		String [] serverIPs = hostnames.split(",");
 		List<URI> serverList = new ArrayList<URI>();  
         for (String serverName : serverIPs) {  
             URI base = URI.create(String.format("http://%s:%d/pools", serverName, port));  
             serverList.add(base);  
         }
 		
 		CouchbaseConnectionFactory cf = connectionFactoryBuilder.buildCouchbaseConnection(serverList, bucketname, password); 
 		client = new CouchbaseClient(cf);
 	}
 
 	@SuppressWarnings("unchecked") 
 	public T get(String key) throws Exception {
 		byte [] value = (byte[])client.get(key);
 		logger.debug("get: key="+key+" value="+value);
 		if (value == null)
 			return null ;
 		T entity = entityMapper.toObject(value);
         entity.setKey(key);
 		return entity;
 	}
 
 // If never call future.get, then value is never persisted!
 
 	public void put(T entity) throws Exception {
 		String key = getKey(entity);
 		final byte [] value = entityMapper.toBytes(entity);
 		logger.debug("put: key="+key+" value.length="+value.length);
 		OperationFuture<Boolean> op = client.set(key, expiration, value);
 		
 		if (!op.get().booleanValue()) {
 			logger.debug("Set failed: " + op.getStatus().getMessage());
 			logger.debug("put: key="+key+" value.length="+value.length +" result= failure " + "timeout="+timeout);
			String emsg = "Failed to set: key=" +key+" value.length="+value.length + " timeout="+timeout+" Op.status="+op.getStatus().getMessage();
 			throw new NoSqlException(emsg);
 	    }
 		
 		//Boolean result = timeout<0?null: future.get(timeout,TimeUnit.MILLISECONDS);
 		logger.debug("put: key="+key+" value.length="+value.length +" result=success " + " timeout="+ timeout);
 	}
 
 	public void delete(String key) throws Exception {
 		logger.debug("delete: key="+key);
 		OperationFuture<Boolean> op = client.delete(key); 
 		
 		if (!op.get().booleanValue()) {
 			logger.debug("delete failed: " + op.getStatus().getMessage());
 			logger.debug("delete: key="+key+" result= failure");
			String emsg = "Failed to delete: key=" +key+ " timeout="+timeout+" Op.status="+op.getStatus().getMessage();
 	    } 
 		
 		logger.debug("delete: key="+key+" result= success");
 	}
 
 	private String getKey(T entity) {
 		return entity.getKey().toString();
 	}
 
     public void setExpiration(int seconds) {
 		this.expiration = seconds ;
 	}
 
     public void setTimeout(long timeout) {
 		this.timeout = timeout ;
 	}
 
 
 	@Override 
 	public String toString() {
 		return 
 			"hostname="+hostnames
 			+" port="+port
 			+" expiration="+expiration
 			+" timeout="+timeout
 		;
 	}
 }
 
