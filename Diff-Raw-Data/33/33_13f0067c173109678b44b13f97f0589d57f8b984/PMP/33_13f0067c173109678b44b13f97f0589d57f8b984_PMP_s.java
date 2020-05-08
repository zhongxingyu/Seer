 package org.mypackage.hello;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 import java.net.UnknownHostException;
 import java.util.Map;
 
 /**
  * PMP = PMP Mongo Preprocessor
  * @author uko
  */
 public class PMP
 {
 	private DB connect() throws UnknownHostException
 	{
		String user = "test1";
		char[] pass = "123456".toCharArray();
 		DB db = new Mongo("maximator.uar.net").getDB("npss");
 		boolean auth = db.authenticate(user, pass);
 		if (db.authenticate(user, pass))
 		{
 			return db;
 		}
 		else
 		{
 			throw new RuntimeException();
 		}
 	}
	public Double getValue(String task, String factory, Map<String,Double> parameters) throws UnknownHostException
 	{
		return (Double) connect().getCollection(task+"."+factory).find(new BasicDBObject("parameters", new BasicDBObject(parameters))).next().get("value");
 	}
	public boolean setValue(String task, String factory, Map<String,Double> parameters, Double value) throws UnknownHostException
 	{
		DBCollection collection = connect().getCollection(task+"."+factory);
		if(collection.find(new BasicDBObject("parameters", new BasicDBObject(parameters))).length()==0)
 		{
			collection.update(new BasicDBObject("parameters", new BasicDBObject(parameters)), new BasicDBObject("$set",new BasicDBObject("value", value)), true, true);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
	
 }
