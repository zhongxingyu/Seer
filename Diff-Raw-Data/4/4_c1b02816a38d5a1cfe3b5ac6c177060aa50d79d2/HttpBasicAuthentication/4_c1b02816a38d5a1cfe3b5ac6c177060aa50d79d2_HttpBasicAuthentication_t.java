 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.api.client.authentication;
 
 import org.mule.ibeans.api.client.State;
 import org.mule.ibeans.api.client.params.HeaderParam;
import org.mule.ibeans.api.client.params.Optional;
 import org.mule.ibeans.api.client.params.PropertyParam;
 
 /**
  * Client iBeans that extend this interface will have HttpBasic support. Simply call the setCredentials() method
  * before making other calls on the client iBean.
  */
 public interface HttpBasicAuthentication extends ClientAuthentication
 {
     @HeaderParam("Authorization")
     HttpBasicHeaderParamFactory HTTP_BASIC_HEADER_FACTORY = new HttpBasicHeaderParamFactory();
 
     @State
    public void setCredentials(@Optional @PropertyParam("username") String username, @Optional @PropertyParam("password") String password);
 }
