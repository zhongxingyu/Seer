 /* Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root application directory.
  */
 
 package org.vfny.geoserver.servlets;
 
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.util.Properties;
 import javax.servlet.http.*;
 //import javax.servlet.*;
 import org.geotools.resources.Geotools;
 import org.vfny.geoserver.config.ConfigInfo;
 import org.vfny.geoserver.zserver.GeoZServer;
 
 //Logging system
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 
 /**
  * Initializes all logging functions.
  * 
  * @author Rob Hranac, Vision for New York
  * @author Chris Holmes, TOPP
  * @version 0.9 beta, 11/01/01
  *
  */
 public class FreefsLog extends HttpServlet {
 
         /** Standard logging instance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.servlet");
    
     /** Default name for feature type schemas */
     private static final String CONFIG_FILE =  "configuration.xml";
     /** Default name for feature type schemas */
     private static final String CONFIG_DIR =  "data/";
 
     /**
      * The logger for the filter module.
      */
     private static final Logger LOG = Logger.getLogger("org.vfny.geoserver.servlets");
    
 
     private GeoZServer server;
 
     /**
      * Initializes logging and config.
      *
      */ 
     public void init() {
	Geotools.init("Log4JFormatter", Level.INFO);
 	String root = this.getServletContext().getRealPath("/");
 	String path = root + CONFIG_DIR;
 	LOG.finest("init with path" + path);
 	ConfigInfo cfgInfo = ConfigInfo.getInstance(path);
 	Properties zserverProps = new Properties();
 	zserverProps.put("port", "5210"); //HACK -allow user to configure this!
 	zserverProps.put("datafolder", cfgInfo.getTypeDir());
 	zserverProps.put("fieldmap", path + cfgInfo.GEO_MAP_FILE);
 	zserverProps.put("database", root + CONFIG_DIR + "zserver-index");
 	try {
 	    server = new GeoZServer(zserverProps);
 	    server.start();
 	} catch (java.io.IOException e) {
 	    LOGGER.info("zserver module could not start: " + e.getMessage());
 	}
 
     }
     
     
     /**
      * Initializes logging.
      *
      * @param req The servlet request object.
      * @param resp The servlet response object.
      */ 
     public void doGet(HttpServletRequest req, HttpServletResponse res) {
         //BasicConfigurator.configure();
     }
     
     public void destroy() {
 	LOGGER.finer("shutting down zserver");
	server.shutdown(1);
 	
     }
 
 }
