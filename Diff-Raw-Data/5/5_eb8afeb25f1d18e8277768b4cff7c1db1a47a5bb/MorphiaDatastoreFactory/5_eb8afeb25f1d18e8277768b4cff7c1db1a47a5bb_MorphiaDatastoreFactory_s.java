 package org.mdissjava.mdisscore.model.dao.factory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mdissjava.commonutils.mongo.morphia.MorphiaDatastoreConnection;
 import org.mdissjava.mdisscore.model.pojo.Camera;
 import org.mdissjava.mdisscore.model.pojo.Photo;
import org.mdissjava.mdisscore.model.pojo.User;
 
 import com.google.code.morphia.Datastore;
 
 /**
  * This Factory automates the Morphia Datastore process 
  * 
  * @author MDISS Java team 2011-2012 University of Deusto
  *
  */
 public class MorphiaDatastoreFactory {
 	
 	/**
 	 * Gets the dataStore from a given database
 	 * 
 	 * @param database is the database where the database will be set
 	 * @return the datastore from Morphia
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	@SuppressWarnings("rawtypes")
 	public static Datastore getDatastore(String database){
 		
 		ArrayList<Class> classes = (ArrayList<Class>)getClasses();
 		
 		MorphiaDatastoreConnection mdc = MorphiaDatastoreConnection.getInstance();
 		mdc.connect("127.0.0.1", 27017, database, classes);
 		
 		return mdc.getDatastore();
 		
 	}
 	
 	/**
 	 * Sets the classes to map in the Morphia instance
 	 * 
 	 * @return the list of classes to map in Morphia
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	@SuppressWarnings("rawtypes")
 	private static List<Class> getClasses(){
 	
 
 		ArrayList<Class> classes = new ArrayList<Class>();
 		
 		//Declare the classes to register
 		final Class CLAZZEZ[] = {
 				Camera.class,
				Photo.class
				
 		};
 		
 		//add to the array
 		for(Class i: CLAZZEZ){			
 			classes.add(i);
 		}
 		
         return classes;
 		
 	}
 
 }
