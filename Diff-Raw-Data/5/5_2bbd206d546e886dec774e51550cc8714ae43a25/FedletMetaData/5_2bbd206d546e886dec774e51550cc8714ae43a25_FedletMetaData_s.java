 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: FedletMetaData.java,v 1.5 2008-09-24 22:27:51 veiming Exp $
  *
  */
 
 package com.sun.identity.workflow;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class FedletMetaData {
     private FedletMetaData() {
     }
 
     public static String createStandardMetaData(
         String entityId,
         String AssertConsumerURL
     ) {
         String xml = STANDARD_METADATA.replaceAll(TAG_ENTITY_ID, entityId);
         return xml.replaceAll(TAG_ASSSERT_CONSUMER_URL, AssertConsumerURL);
     }
 
     public static String createExtendedMetaData(
         String realm,
         String entityId,
         List attrMapping
     ) throws WorkflowException {
         Map map = new HashMap();
         map.put(MetaTemplateParameters.P_SP,Task.generateMetaAliasForSP(realm));
         String extendedData =
             CreateSAML2HostedProviderTemplate.createExtendedDataTemplate(
             entityId, map, null, false);
         
         int idx = extendedData.indexOf("<Attribute name=\"spAccountMapper\">");
         if (idx != -1) {
             idx = extendedData.indexOf("<Value>", idx);
             int idx1 = extendedData.indexOf("</Value>", idx);
             extendedData = extendedData.substring(0, idx+7) +
                 "com.sun.identity.saml2.plugins.DefaultLibrarySPAccountMapper" +
                 extendedData.substring(idx1);
         }
         idx = extendedData.indexOf("<Attribute name=\"transientUser\">");
         if (idx != -1) {
             idx = extendedData.indexOf("<Value>", idx);
             int idx1 = extendedData.indexOf("</Value>", idx);
             extendedData = extendedData.substring(0, idx+7) +
                 "anonymous" +
                 extendedData.substring(idx1);
         }
         
         idx = extendedData.indexOf("<Attribute name=\"saeSPUrl\">");
         if (idx != -1) {
             idx = extendedData.indexOf("<Value>", idx);
             int idx1 = extendedData.indexOf("</Value>", idx);
             extendedData = extendedData.substring(0, idx+7) +
                 extendedData.substring(idx1);
         }
         
         if ((attrMapping != null) && !attrMapping.isEmpty()) {
             idx = extendedData.indexOf("<Attribute name=\"attributeMap\">");
             if (idx != -1) {
                 StringBuffer buff = new StringBuffer();
                 for (Iterator i = attrMapping.iterator(); i.hasNext(); ) {
                     buff.append("<Value>")
                         .append((String)i.next())
                         .append("</Value>");
                 }
                 idx = extendedData.indexOf(">", idx);
                 extendedData = extendedData.substring(0, idx+1) + 
                     buff.toString() +
                     extendedData.substring(idx+1);
             }
         }
         return extendedData;
     }
 
     private static final String TAG_ENTITY_ID = "@ENTITY_ID@";
     private static final String TAG_ASSSERT_CONSUMER_URL = "@ASSSERT_CONSUMER_URL@";
 
     private static final String STANDARD_METADATA = 
        "<EntityDescriptor entityID=\"@ENTITY_ID@\" xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\"><SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"false\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\"><NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</NameIDFormat><AssertionConsumerService index=\"1\" Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"@ASSSERT_CONSUMER_URL@\"/></SPSSODescriptor></EntityDescriptor>";
 }
