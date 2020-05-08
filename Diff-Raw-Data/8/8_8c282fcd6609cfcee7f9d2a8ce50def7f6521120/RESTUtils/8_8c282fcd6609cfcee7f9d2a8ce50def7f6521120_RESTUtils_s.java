 package org.geoserver.rest;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.restlet.data.Request;
 
 import com.noelios.restlet.ext.servlet.ServletCall;
 import com.noelios.restlet.http.HttpRequest;
 
 /**
 * Utility class for reslets.
  * 
  * @author Justin Deoliveira, The Open Planning Project
  *
  */
 public class RESTUtils {
 
     /**
     * Returns the underlying HttpServletRequest from a reslet Request object.
      * <p>
     * Note that this only returns a value in teh case where the reslet 
      * request/call is originating from a servlet.
      * </p>
      * @return The HttpServletRequest, or null.
      */
     public static HttpServletRequest getServletRequest( Request request ) {
         if ( request instanceof HttpRequest ) {
             HttpRequest httpRequest = (HttpRequest) request;
             if ( httpRequest.getHttpCall() instanceof ServletCall ) {
                 ServletCall call = (ServletCall) httpRequest.getHttpCall();
                 return call.getRequest();
             }
         }
         
         return null;
     }
 }
