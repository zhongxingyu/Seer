 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.servlets;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.vfny.geoserver.config.TypeInfo;
 import org.vfny.geoserver.requests.FeatureRequest;
 import org.vfny.geoserver.requests.FeatureKvpReader;
 import org.vfny.geoserver.requests.XmlRequestReader;
 import org.vfny.geoserver.responses.FeatureResponse;
 import org.vfny.geoserver.responses.WfsException;
 
 /**
  * Implements the WFS GetFeature interface, which responds to requests for GML.
  * This servlet accepts a getFeatures request and returns GML2.1 structured
  * XML docs.
  *
  *@author Rob Hranac, TOPP
  *@version $Version$
  */
 public class Feature
     extends HttpServlet {
 
     /** Class logger */
     private static Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.servlets");
     
     /** Specifies MIME type */
     private static final String MIME_TYPE = "text/xml";
 
     /**
      * Reads the XML request from the client, turns it into a generic request 
      * object, generates a generic response object, and writes to client.
      * @param request The servlet request object.
      * @param response The servlet response object.
      */ 
     public void doPost(HttpServletRequest request,HttpServletResponse response)
         throws ServletException, IOException {
         
         /** create temporary response string */
         String tempResponse = "";
         
         // implements the main request/response logic
         try {
             FeatureRequest wfsRequest = 
                 XmlRequestReader.readGetFeature(request.getReader());
             tempResponse = FeatureResponse.getXmlResponse(wfsRequest);
         }
         
         // catches all errors; client should never see a stack trace 
         catch (WfsException wfs) {
             tempResponse = wfs.getXmlResponse();
             LOGGER.info("Threw a wfs exception: " + wfs.getMessage());
            if(response != null) wfs.printStackTrace(response.getWriter());
             wfs.printStackTrace();
         }
         catch (Exception e) {
             tempResponse = e.getMessage();
             LOGGER.info("Had an undefined error: " + e.getMessage());
            if(response != null) e.printStackTrace(response.getWriter());
             e.printStackTrace();
         }
         
                 // set content type and return response, whatever it is 
         response.setContentType(MIME_TYPE);
         response.getWriter().write( tempResponse );
         
     }
     
     
     /**
      * Handles all Get requests.
      * This method implements the main return logic for the class.
      * @param request The servlet request object.
      * @param response The servlet response object.
      */ 
     public void doGet(HttpServletRequest request,HttpServletResponse response) 
         throws ServletException, IOException {
         
         /** create temporary response string */
         String tempResponse;
         
         // implements the main request/response logic
         try {
             FeatureKvpReader currentKvpRequest = 
                 new FeatureKvpReader(request.getQueryString());
             FeatureRequest wfsRequest = 
                 currentKvpRequest.getRequest();
             tempResponse = FeatureResponse.getXmlResponse(wfsRequest);
         }
         
         // catches all errors; client should never see a stack trace 
         catch (WfsException wfs) {
             tempResponse = wfs.getXmlResponse();
         }
         
         // set content type and return response, whatever it is 
         response.setContentType(MIME_TYPE);
         response.getWriter().write( tempResponse );        
     }    
 }
