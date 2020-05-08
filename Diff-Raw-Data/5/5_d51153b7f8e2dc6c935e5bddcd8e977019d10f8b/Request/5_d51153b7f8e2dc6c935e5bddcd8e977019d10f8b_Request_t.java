 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 /**
  * Defines a general Request type and provides accessor methods for unversal
  * request information.
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
 * @version $Id: Request.java,v 1.4 2003/12/10 15:56:35 cholmesny Exp $
  */
 abstract public class Request {
     /** Request service */
     protected String service = "WFS";
 
     /** Request type */
     protected String request = new String();
 
     /** Request version */
     protected String version = new String();
 
     /**
      * Empty constructor.
      */
     public Request() {
     }
 
     /**
      * Gets requested service.
      *
      * @return The requested service.
      */
     public String getService() {
         return this.service;
     }
 
     /**
      * Gets requested service.
      *
      * @param service The requested service.
      */
     public void setService(String service) {
         this.service = service;
     }
 
     /**
      * Gets requested request type.
      *
      * @return The type of request.
      */
     public String getRequest() {
         return this.request;
     }
 
     /**
      * Sets requested request type.
      *
      * @param reqeust The type of request.
      */
    public void setRequest(String request) {
         this.request = request;
     }
 
     /**
      * Return version type.
      *
      * @return The request type version.
      */
     public String getVersion() {
         return this.version;
     }
 
     /**
      * Sets version type.
      *
      * @param version The request type version.
      */
     public void setVersion(String version) {
         this.version = version;
     }
 
    
     public boolean equals(Object o) {
         if (!(o instanceof Request)) {
             return false;
         }
 
         Request req = (Request) o;
         boolean equals = true;
         equals = ((request == null) ? (req.getRequest() == null)
                                     : request.equals(req.getRequest()))
             && equals;
         equals = ((version == null) ? (req.getVersion() == null)
                                     : version.equals(req.getVersion()))
             && equals;
         equals = ((service == null) ? (req.getService() == null)
                                     : service.equals(req.getService()))
             && equals;
 
         return equals;
     }
 
     public int hashCode() {
         int result = 17;
         result = (23 * result) + ((request == null) ? 0 : request.hashCode());
         result = (23 * result) + ((request == null) ? 0 : version.hashCode());
         result = (23 * result) + ((request == null) ? 0 : service.hashCode());
 
         return result;
     }
 }
