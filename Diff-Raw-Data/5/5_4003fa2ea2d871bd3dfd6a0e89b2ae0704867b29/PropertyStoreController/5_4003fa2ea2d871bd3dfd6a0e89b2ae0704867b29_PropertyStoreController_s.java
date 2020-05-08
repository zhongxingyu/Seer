 package com.epms.rest;
 
 import java.net.UnknownHostException;
 
 import javax.annotation.PostConstruct;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 @Controller
 @RequestMapping("/applicationNames")
 public class PropertyStoreController {
 	private static final Logger logger = LoggerFactory.getLogger(PropertyStoreController.class);
 	private DB epmsDB;
 	
 	@PostConstruct
 	public void initPropertyStoreController() {
 		System.out.println("initPropertyStoreController() called...");
 		try {
 			MongoClient mongoClient = new MongoClient();
 	        epmsDB = mongoClient.getDB("epmsDB");
 		} 
 		catch (UnknownHostException e) {
 			logger.error("initPropertyStoreController() problem...");
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public @ResponseBody String getAllApplicationNames() {
 		logger.info("getAllApplicationNames() called...");
 		return findAllApplicationNames();
 	}
 
 	@RequestMapping(value="/{applicationName}", method = RequestMethod.GET)
 	public @ResponseBody String getPropertiesForApplicationName(@PathVariable("applicationName") String applicationName) {
 		logger.info("getPropertiesForApplicationName() called...");
 		return findPropertiesForApplicationName(applicationName);
 	}
 	
 	private String findAllApplicationNames() {
         StringBuilder sb = new StringBuilder();
		sb.append("[");
 
 		// get a collection object to work with
         DBCollection epmsCollection = epmsDB.getCollection("epms");
 
         BasicDBObject keys = new BasicDBObject();
         keys.put("applicationName", 1);
         
         DBCursor cursor = epmsCollection.find(new BasicDBObject(), keys);
         try {
             while (cursor.hasNext()) {
             	sb.append(cursor.next());
             	sb.append(",");
             }
         } 
         finally {
             cursor.close();
         }
         sb.setLength(sb.length()-1);	//remove the last comma
		sb.append("]");
 		return sb.toString();
 	}
 
 	private String findPropertiesForApplicationName(String applicationName) {
 		// get a collection object to work with
         DBCollection epmsCollection = epmsDB.getCollection("epms");
 
         BasicDBObject query = new BasicDBObject();
         query.put("applicationName", applicationName);
         
         DBObject result = epmsCollection.findOne(query);
         return result.toString();
 	}
 }
