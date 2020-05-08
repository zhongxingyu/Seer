 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: FAMSTSImpl.java,v 1.3 2007-09-01 00:21:43 mrudul_uchil Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.wss.sts;
 
 import javax.xml.ws.Provider;
 import javax.xml.ws.Service;
 import javax.xml.ws.Service.Mode;
 import javax.xml.ws.ServiceMode;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.WebServiceProvider;
 
 import com.sun.xml.ws.security.trust.sts.BaseSTSImpl;
 
 import javax.annotation.Resource;
 
 import javax.xml.transform.Source;
 import javax.xml.ws.handler.MessageContext;
 
 @ServiceMode(value=Service.Mode.PAYLOAD)
 @javax.xml.ws.WebServiceProvider(targetNamespace="http://localhost:8080/openfm/SecurityTokenService/",
                                 portName="ISecurityTokenService_Port",
                                  serviceName="SecurityTokenService",
                                  wsdlLocation="WEB-INF/wsdl/famsts.wsdl")
 @javax.xml.ws.BindingType(value=javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
 public class FAMSTSImpl extends BaseSTSImpl implements Provider<Source>{
     @Resource
     protected WebServiceContext context;
     
     protected MessageContext getMessageContext() {        
         MessageContext msgCtx = context.getMessageContext(); 
         return msgCtx;
     }  
 }
 
 
