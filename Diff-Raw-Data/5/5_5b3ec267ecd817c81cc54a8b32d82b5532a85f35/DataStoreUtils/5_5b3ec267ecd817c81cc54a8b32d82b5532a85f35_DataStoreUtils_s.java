 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.action.data;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFactorySpi;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DataStoreFactorySpi.Param;
 import org.geotools.data.DataStoreFinder;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 
 /**
  * A collecitno of utilties for dealing with GeotTools DataStore.
  *
  * @author Richard Gould, Refractions Research, Inc.
  * @author $Author: cholmesny $ (last modification)
 * @version $Id: DataStoreUtils.java,v 1.11 2004/09/20 20:46:19 cholmesny Exp $
  */
 public abstract class DataStoreUtils {
     public static DataStore aquireDataStore(Map params, ServletContext sc)
         throws IOException {
     	String baseDir = sc.getRealPath("/");
        	DataStore store = DataStoreFinder.getDataStore(getParams(params,baseDir));
         if (store == null) {
             //TODO: this should throw an exception, but the classes using
             //this class aren't ready to actually get it...
            //LOGGER.fine("could not acquire datastore with params: " + params);
            //throw new DataSourceException("could not find a dataStore with " +
             //			  "params: " + params);
             return null;
 	} else {
 	    return store;
 	}
     }
 
     /**
      * Get Connect params.
      * <p>
      * This is used to smooth any relative path kind of issues for any
      * file URLS. This code should be expanded to deal with any other context
      * sensitve isses dataStores tend to have.
      * </p>
      */
     protected static Map getParams(Map m, String baseDir){
     	Map params = new HashMap( m );
     	for( Iterator i=params.entrySet().iterator(); i.hasNext();){
     		Map.Entry entry = (Map.Entry) i.next();
     		String key = (String) entry.getKey();
     		Object value = entry.getValue();
     		try {
 	    		if( "url".equals( key ) && value instanceof String ){
 	    			String path = (String) value;
 	    			if( path.startsWith("file:") ){
 	    				path = path.substring( 5 ); // remove 'file:' prefix
 	    				File file = new File( baseDir, path );
 	    				entry.setValue( file.toURL().toExternalForm() );
 	    			}	    			
 	    		}
 	    		else if (value instanceof URL && ((URL)value).getProtocol().equals("file")){
 	    			URL url = (URL) value;
 	    			String path = url.getPath();
 	    			File file = new File( baseDir, path );
     				entry.setValue( file.toURL() );
 	    		}	    	
     		}
     		catch( MalformedURLException ignore ){
     			// ignore attempt to fix relative paths
     		}
     	}
     	return params;
     }
 
     /**
      * When loading from DTO use the params to locate factory.
      * 
      * <p>
      * bleck
      * </p>
      *
      * @param params
      *
      * @return
      */
     public static DataStoreFactorySpi aquireFactory(Map params) {
         for (Iterator i = DataStoreFinder.getAvailableDataStores();
                 i.hasNext();) {
             DataStoreFactorySpi factory = (DataStoreFactorySpi) i.next();
 
             if (factory.canProcess(params)) {
                 return factory;
             }
         }
 
         return null;
     }
 
     /**
      * After user has selected Description can aquire Factory based on
      * description.
      * 
      * <p>
      * Use factory for:
      * </p>
      * 
      * <ul>
      * <li>
      * List of Params (attrb name, help text)
      * </li>
      * <li>
      * Checking user's input with factory.canProcess( params )
      * </li>
      * </ul>
      * 
      *
      * @param description
      *
      * @return
      */
     public static DataStoreFactorySpi aquireFactory(String description) {
         for (Iterator i = DataStoreFinder.getAvailableDataStores();
                 i.hasNext();) {
             DataStoreFactorySpi factory = (DataStoreFactorySpi) i.next();
 
             if (factory.getDescription().equals(description)) {
                 return factory;
             }
 
             if (factory.getClass().toString().equals(description)) {
                 return factory;
             }
         }
 
         return null;
     }
 
     /**
      * Utility method for finding Params
      *
      * @param factory DOCUMENT ME!
      * @param key DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public static Param find(DataStoreFactorySpi factory, String key) {
         return find(factory.getParametersInfo(), key);
     }
 
     /**
      * Utility methods for find param by key
      *
      * @param params DOCUMENT ME!
      * @param key DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public static Param find(Param[] params, String key) {
         for (int i = 0; i < params.length; i++) {
             if (key.equalsIgnoreCase(params[i].key)) {
                 return params[i];
             }
         }
 
         return null;
     }
 
     /**
      * Returns the descriptions for the available DataStores.
      * 
      * <p>
      * Arrrg! Put these in the select box.
      * </p>
      *
      * @return Descriptions for user to choose from
      */
     public static List listDataStoresDescriptions() {
         List list = new ArrayList();
 
         for (Iterator i = DataStoreFinder.getAvailableDataStores();
                 i.hasNext();) {
             DataStoreFactorySpi factory = (DataStoreFactorySpi) i.next();
             list.add(factory.getDescription());
         }
 
         return list;
     }
 
     public static Map defaultParams(String description) {
         return defaultParams(aquireFactory(description));
     }
 
     public static Map defaultParams(DataStoreFactorySpi factory) {
         Map defaults = new HashMap();
         Param[] params = factory.getParametersInfo();
 
         for (int i = 0; i < params.length; i++) {
             Param param = params[i];
             String key = param.key;
             String value = null;
 
             //if (param.required ) {
                 if( param.sample != null){
                     // Required params may have nice sample values
                     //
                     value = param.text( param.sample );
                 }
                 if (value == null ) {
                     // or not
                     value = "";
                 }
 		//}
             if (value != null) {
                 defaults.put(key, value);
             }
         }
 
         return defaults;
     }
 
     /**
      * Convert map to real values based on factory Params.
      * 
      * <p>
      * The resulting map should still be checked with factory.acceptsMap( map )
      * </p>
      *
      * @param factory
      * @param params
      *
      * @return Map with real values that may be acceptable to Factory
      *
      * @throws IOException DOCUMENT ME!
      */
     public static Map toConnectionParams(DataStoreFactorySpi factory, Map params)
         throws IOException {
         Map map = new HashMap(params.size());
 
         Param[] info = factory.getParametersInfo();
 
         // Convert Params into the kind of Map we actually need
         for (Iterator i = params.keySet().iterator(); i.hasNext();) {
             String key = (String) i.next();
 
             Object value = find(info, key).lookUp(params);
 
             if (value != null) {
                 map.put(key, value);
             }
         }
 
         return map;
     }
  
     public static Envelope getBoundingBoxEnvelope(FeatureSource fs) throws IOException {
         Envelope ev = fs.getBounds();
         if(ev == null || ev.isNull()){
             try{
                 ev = fs.getFeatures().getBounds();
             }catch(Throwable t){
                ev = null;
             }
         }
         return ev;
     }
 }
