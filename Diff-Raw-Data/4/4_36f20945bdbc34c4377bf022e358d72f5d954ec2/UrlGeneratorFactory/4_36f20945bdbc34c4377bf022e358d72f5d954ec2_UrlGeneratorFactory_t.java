 /*
  * UrlGeneratorFactory.java
  *
  * Created on Feb 11, 2007
  *
  * Copyright (C) 2006, 2007 Carl E Harris, Jr.
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
  * License for more details.
  */
 package org.soulwing.cas.filter;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.soulwing.cas.client.ProtocolConfiguration;
 import org.soulwing.cas.client.ProtocolConfigurationImpl;
 import org.soulwing.cas.client.ProtocolConstants;
 import org.soulwing.cas.client.SimpleUrlGenerator;
 import org.soulwing.cas.client.UrlGenerator;
 
 
 /**
  * A factory for constructing UrlGenerator instances.
  *
  * @author Carl Harris
  */
 public class UrlGeneratorFactory {
 
   /**
    * Gets a UrlGenerator from this factory, configured as this factory
    * is configured. 
    * @param request request for which a UrlGenerator is needed
    * @param protocolConfiguration CAS protocol configuration
    * @return <code>UrlGenerator</code> instance
    */
   public static UrlGenerator getUrlGenerator(HttpServletRequest request, 
       ProtocolConfiguration protocolConfiguration) {
     return new SimpleUrlGenerator(
         new ContextProtocolConfiguration(request, protocolConfiguration));
   }
 
   /**
    * An extension of ProtocolConfigurationImpl that allows the 
    * value of the <code>serviceUrl</code> property to be derived from
    * an HttpServletRequest.
    *
    * @author Carl Harris
    */
   static class ContextProtocolConfiguration 
       extends ProtocolConfigurationImpl {
     private final HttpServletRequest request;
     
     public ContextProtocolConfiguration(HttpServletRequest request,
         ProtocolConfiguration config) {
       if (config.getServerUrl() == null || config.getServiceUrl() == null) {
         throw new IllegalArgumentException(
             "configuration must specify serverUrl and serviceUrl");
       }
       this.request = request;
       setServerUrl(config.getServerUrl());
       setServiceUrl(config.getServiceUrl());
       setProxyCallbackUrl(config.getProxyCallbackUrl());
       setGatewayFlag(config.getGatewayFlag());
       setRenewFlag(config.getRenewFlag());
     }
 
     public String getServiceUrl() {
       StringBuffer sb = new StringBuffer(100);
       sb.append(super.getServiceUrl());
       // SCC-20, SCC-21
       sb.append(request.getRequestURI());
       // SCC-38
      if (request.getPathInfo() != null
          // SCC-48 -- websphere returns a empty string rather than null
          && request.getPathInfo().length() > 0) {
         sb.append(request.getPathInfo());
       }
       if (request.getQueryString() != null 
           && request.getQueryString().length() > 0) {
         sb.append('?');
         sb.append(request.getQueryString());
         removeTicketFromQueryString(sb);
       }
       return sb.toString();
     }
 
     private void removeTicketFromQueryString(StringBuffer sb) {
       int i = sb.indexOf("&" + ProtocolConstants.TICKET_PARAM + "=");
       if (i == -1) {
         i = sb.indexOf("?" + ProtocolConstants.TICKET_PARAM + "=");
       }
       if (i != -1) {
         int j = sb.indexOf("&", i + 1);
         if (j == -1) {
           sb.delete(i, sb.length());
         }
         else {
           sb.delete(i + 1, j + 1);
         }
       }
     }
 
   }
   
 }
