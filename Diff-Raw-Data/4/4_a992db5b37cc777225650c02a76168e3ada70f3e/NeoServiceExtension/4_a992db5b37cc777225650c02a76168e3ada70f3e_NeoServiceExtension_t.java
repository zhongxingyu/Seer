 package org.amanzi.awe.catalog.neo;
 
 import java.io.File;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.refractions.udig.catalog.IService;
 import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.URLUtils;
 
 public class NeoServiceExtension implements ServiceExtension {
     /* Neo4J service key, URL to the Neo4J database and gis node */
     public static final String URL_KEY = "org.amanzi.awe.catalog.neo.url";
     public static final String CLASS_KEY = "org.amanzi.awe.catalog.neo.class";
 
     @Override
     public Map<String, Serializable> createParams(URL url) {
         try {
             if (url.getProtocol().equals("file")) {
                 // the URL represent a normal file or directory on disk
                File path = URLUtils.urlToFile(url);
                 if (path.exists() && path.isDirectory()) {
                     // check the directory, does it contain a neo4j database
                     File neostore = new File(path,"neostore");
                     if (neostore.exists()) {
                         Map<String, Serializable> params = new HashMap<String, Serializable>();
                         params.put(URL_KEY, url);
                         params.put(CLASS_KEY, URL.class);
                         return params;
                     }
                 }
             }
         } catch (Throwable t) {
             // something went wrong, URL must be for another service
         }
 
         // unable to create the parameters, URL must be for another service
         return null;
     }
 
     @Override
     public IService createService(URL id, Map<String, Serializable> params) {
         // good defensive programming
         if (params == null) {
             return null;
         }
 
         // check for the property service key
         if (params.containsKey(URL_KEY)) {
             // found it, create the service handle
             return new NeoService(params);
         }
 
         // key not found
         return null;
     }
 
 }
