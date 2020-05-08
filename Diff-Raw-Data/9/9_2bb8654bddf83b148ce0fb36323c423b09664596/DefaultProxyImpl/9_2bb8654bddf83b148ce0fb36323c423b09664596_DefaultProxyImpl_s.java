 /*
  * DefaultProxyImpl.java
  *
  * Created on Feb 9, 2007
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
 package org.soulwing.cas.client;
 
 import org.soulwing.cas.client.jdom.ProxySuccessResponse;
 
 
 /**
  * The default implementation of the Proxy interface as a POJO with
  * configurable properties.
  *
  * @author Carl Harris
  */
 public class DefaultProxyImpl implements Proxy {
 
   private ProtocolHandler protocolHandler;
   private ProtocolMappingStrategy proxyMappingStrategy;
   private UrlGenerator urlGenerator;
   private ProtocolSource protocolSource = new UrlProtocolSource();
 
   /* 
    * @see org.soulwing.cas.client.Proxy#proxy(ProxyRequest)
    */
   public ProxyResponse proxy(ProxyRequest request) {
     return (ProxySuccessResponse)
         this.protocolHandler.processResult(
             getProtocolSource().getSource(
                 getUrlGenerator().getProxyUrl(
                     request.getProxyGrantingTicket(), 
                     request.getTargetService())), proxyMappingStrategy);
   }
 
   /**
    * Gets the <code>urlGenerator</code> property.
    * @return <code>UrlGenerator}</code> property value
    */
   public UrlGenerator getUrlGenerator() {
     return urlGenerator;
   }
 
   /**
    * Sets the <code>urlGenerator</code> property.
    * @param urlGenerator <code>UrlGenerator</code> property value
    */
  public void setUrlGenerator(UrlGenerator generator) {
    this.urlGenerator = generator;
   }
 
   /**
    * Gets the <code>protocolHandler</code> property.
    * @return <code>ProtocolHandler}</code> property value
    */
   public ProtocolHandler getProtocolHandler() {
     return protocolHandler;
   }
 
   /**
    * Sets the <code>protocolHandler</code> property.
    * @param protocolHandler <code>ProtocolHandler</code> property value
    */
   public void setProtocolHandler(ProtocolHandler protocolHandler) {
     this.protocolHandler = protocolHandler;
   }
 
   /**
    * Gets the <code>proxyMappingStrategy</code> property.
    * @return <code>ProtocolMappingStrategy}</code> property value
    */
   public ProtocolMappingStrategy getProxyMappingStrategy() {
     return proxyMappingStrategy;
   }
 
   /**
    * Sets the <code>proxyMappingStrategy</code> property.
    * @param proxyMappingStrategy <code>ProtocolMappingStrategy</code> property value
    */
   public void setProxyMappingStrategy(
       ProtocolMappingStrategy proxyMappingStrategy) {
     this.proxyMappingStrategy = proxyMappingStrategy;
   }
 
   /**
    * Gets the <code>protocolSource</code> property.
    * @return <code>ProtocolSource}</code> property value
    */
   public ProtocolSource getProtocolSource() {
     return protocolSource;
   }
 
   /**
    * Sets the <code>protocolSource</code> property.
    * @param protocolSource <code>ProtocolSource</code> property value
    */
  public void setProtocolSource(ProtocolSource source) {
    this.protocolSource = source;
   }
 
 }
