 /*
  * OpenRemote, the Home of the Digital Home.
  * Copyright 2008-2012, OpenRemote Inc.
  *
  * See the contributors.txt file in the distribution for a
  * full listing of individual contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.openremote.controller.rest;
 
 import java.security.cert.X509Certificate;
 
 import java.lang.ProcessBuilder;
 import java.lang.InterruptedException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 
 import org.apache.log4j.Logger;
 import org.openremote.controller.Constants;
 import org.openremote.controller.exception.ControlCommandException;
 import org.openremote.controller.service.ProfileService;
 import org.openremote.controller.spring.SpringContext;
 
 /**
  * This servlet implements the REST API '/rest/clients' functionality which creates
  * a certificate when a call has been done.  <p>
  *
  * See <a href = "http://www.openremote.org/display/docs/Controller+2.0+HTTP-REST-XML">
  * Controller 2.0 REST XML API<a> and
  * <a href = "http://openremote.org/display/docs/Controller+2.0+HTTP-REST-JSONP">Controller 2.0
  * REST JSONP API</a> for more details.
  *
  * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a>
  */
 public class ListClientInfo extends RESTAPI
 {
 
   /*
    *  IMPLEMENTATION NOTES:
    *
    *    - This adheres to the current 2.0 version of the HTTP/REST/XML and HTTP/REST/JSON APIs.
    *      There's currently no packaging or REST URL distinction for supported API versions.
    *      Later versions of the Controller may support multiple revisions of the API depending
    *      on client request. Appropriate implementation changes should be made then.
    *                                                                                      [JPL]
    */
 
 
   // Class Members --------------------------------------------------------------------------------
 
   /**
    * Common log category for HTTP REST API.
    */
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
 
 
   // TODO :
   //  reduce API dependency and lookup service implementation through either an service container
   //  or short term servlet application context
 
   private final static ProfileService profileService = (ProfileService) SpringContext.getInstance().getBean(
        "profileService");
 
 
   protected String getClients(String path) throws NullPointerException
   {
 	String files;
 	String output = "";
 	File folder = new File(path);
 	File[] listOfFiles = folder.listFiles(); 
  
 	for (int i = 0; i < listOfFiles.length; i++) 
 	{
 		if (listOfFiles[i].isFile()) 
 		{
 			files = listOfFiles[i].getName();
	   		if (files.endsWith(".crt") && !files.equals("myca.crt"))
 	   		{
				//openssl x509 -subject -enddate -serial -noout -in ./certs/vincent.crt
 		  		output += "File #: " + i + " - " + files;
 			}
 	 	}
 	}
 	return output;
   }
 
   // Implement REST API ---------------------------------------------------------------------------
 
 
   @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
   {
     try
     {
         sendResponse(response, this.getClients("/usr/share/tomcat6/cert/ca/certs"));
     }
 
     catch (NullPointerException e)
     {
       logger.error("NullPointer client", e);
     }
   }
 
 }
