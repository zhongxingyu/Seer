 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 
 package com.aetrion.flickr.util;
 
 import com.aetrion.flickr.Parameter;
 import com.aetrion.flickr.RequestContext;
 import com.aetrion.flickr.auth.Auth;
 import com.aetrion.flickr.auth.AuthUtilities;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 /** @author Anthony Eden */
 public class UrlUtilities {
 
     /**
      * Build a request URL.
      * @param host The host
      * @param port The port
      * @param path The path
      * @param parameters The parameters
      * @return The URL
      * @throws MalformedURLException
      */
     public static URL buildUrl(String host, int port, String path, List parameters) throws MalformedURLException {
         AuthUtilities.addAuthToken(parameters);
 
         StringBuffer buffer = new StringBuffer();
         buffer.append("http://");
         buffer.append(host);
         if (port > 0) {
             buffer.append(":");
             buffer.append(port);
         }
         if (path == null) {
             path = "/";
         }
         buffer.append(path);
 
         Iterator iter = parameters.iterator();
         if (iter.hasNext()) {
             buffer.append("?");
         }
         while (iter.hasNext()) {
             Parameter p = (Parameter) iter.next();
             buffer.append(p.getName());
             buffer.append("=");
            buffer.append(p.getValue());
             if (iter.hasNext()) buffer.append("&");
         }
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Auth auth = requestContext.getAuth();
         if (auth != null && !ignoreMethod(getMethod(parameters))) {
             buffer.append("&api_sig=");
             buffer.append(AuthUtilities.getSignature(parameters));
         }
 
         return new URL(buffer.toString());
     }
 
     public static URL buildPostUrl(String host, int port, String path) throws MalformedURLException {
         StringBuffer buffer = new StringBuffer();
         buffer.append("http://");
         buffer.append(host);
         if (port > 0) {
             buffer.append(":");
             buffer.append(port);
         }
         if (path == null) {
             path = "/";
         }
         buffer.append(path);
         return new URL(buffer.toString());
     }
 
     private static String getMethod(List parameters) {
         Iterator iter = parameters.iterator();
         while (iter.hasNext()) {
             Parameter parameter = (Parameter) iter.next();
             if ("method".equals(parameter.getName())) {
                 return String.valueOf(parameter.getValue());
             }
         }
         return null;
     }
 
     private static boolean ignoreMethod(String method) {
         if (method != null) {
             if ("flickr.auth.checkToken".equals(method)) {
                 return true;
             }
         }
         return false;
     }
 
 }
