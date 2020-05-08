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
 import org.vfny.geoserver.requests.TransactionRequest;
 import org.vfny.geoserver.requests.DeleteRequest;
 import org.vfny.geoserver.requests.DeleteKvpReader;
 import org.vfny.geoserver.requests.XmlRequestReader;
 import org.vfny.geoserver.responses.TransactionResponse;
 import org.vfny.geoserver.responses.WfsException;
 import org.vfny.geoserver.responses.WfsTransResponse;
 import org.vfny.geoserver.responses.WfsTransactionException;
 
 /**
  * Implements the WFS Transaction interface, which performs insert, update and
  * delete functions on the dataset.
  * This servlet accepts a Transaction request and returns a TransactionResponse
  * xml element.
  *
  *@author Chris Holmes, TOPP
  *@version $Version$
  */
 public class Transaction
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
 	TransactionRequest wfsRequest = null;
 	try {
             wfsRequest = XmlRequestReader.readTransaction(request.getReader());
 	    LOGGER.finer("sending request to Transaction Response: " + wfsRequest);
             tempResponse = TransactionResponse.getXmlResponse(wfsRequest);
         
         
         // catches all errors; client should never see a stack trace 
 	    } catch (WfsTransactionException wte) {
		LOGGER.info("Threw a WfsTransactionException");
	    wte.setHandle(wfsRequest.getHandle());
	    tempResponse =  wte.getXmlResponse();
 	    }
 	 catch (Exception e) {
             tempResponse = e.getMessage();
             LOGGER.info("Had an undefined error: " + e.getMessage());
             e.printStackTrace(response.getWriter());
             e.printStackTrace();
 	    }
         
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
 
 	LOGGER.info("we got a request!");
 	LOGGER.finer("send it to the kvp reader dude.");
         
         // implements the main request/response logic
         try {
             DeleteKvpReader currentKvpRequest = 
                 new DeleteKvpReader(request.getQueryString());
             TransactionRequest wfsRequest = 
                 currentKvpRequest.getRequest();
             tempResponse = TransactionResponse.getXmlResponse(wfsRequest);
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
