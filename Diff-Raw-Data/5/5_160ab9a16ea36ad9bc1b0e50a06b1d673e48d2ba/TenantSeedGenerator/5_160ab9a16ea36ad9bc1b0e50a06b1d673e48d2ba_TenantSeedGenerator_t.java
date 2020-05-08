 /*******************************************************************************
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  *******************************************************************************/
 package org.ofbiz.tenant.tenant;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilXml;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class TenantSeedGenerator {
     
     public final static String module = TenantSeedGenerator.class.getName();
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         if (args.length == 0) {
             System.out.println("Please enter the component name you want the system to update the TenantData.xml file from component://ofbizdemo/config");
         } else {
             String file = null;
             String tenantId = null;
             String tenantName = null;
             String jdbcUriPrefix = null;
             String jdbcUriSuffix = null;
             String jdbcUsername = null;
             String jdbcPassword = null;
             String components = null;
             String domainNames = null;
             String dbPrefix = "";
             
             for (String arg: args) {
                 String[] nameValue = arg.split("=");
                 String name = nameValue[0];
                 String value = "";
                 if (nameValue.length > 1)
                 	value = nameValue[1];
                 if ("tenantId".equals(name)) {
                     tenantId = value.trim();
                 } else if ("tenantName".equals(name)) {
                     tenantName = value.trim();
                 } else if ("jdbcUriPrefix".equals(name)) {
                     jdbcUriPrefix = value.trim();
                 } else if ("jdbcUriSuffix".equals(name)) {
                     jdbcUriSuffix = value.trim();
                 } else if ("jdbcUsername".equals(name)) {
                     jdbcUsername = value.trim();
                 } else if ("jdbcPassword".equals(name)) {
                     jdbcPassword = value.trim();
                 } else if ("components".equals(name)) {
                     components = value.trim();
                 } else if ("domainNames".equals(name)) {
                     domainNames = value.trim();
                 } else if ("file".equals(name)) {
                     file = value.trim();
                 } else if ("dbPrefix".equals(name)) {
                    dbPrefix = value.trim();
                 }
             }
 
             // create XML document
             Document document = UtilXml.makeEmptyXmlDocument();
             Element entityEngineXmlElement = document.createElement("entity-engine-xml");
             
             // Tenant
             Element tenantElement = document.createElement("Tenant");
             tenantElement.setAttribute("tenantId", tenantId);
             tenantElement.setAttribute("tenantName", tenantName);
             entityEngineXmlElement.appendChild(tenantElement);
             
             // Tenant Component
             String[] componentNames = components.split(",");
             int componentNameCount = 10;
             for (String componentName : componentNames) {
                 Element tenantComponentElement = document.createElement("TenantComponent");
                 tenantComponentElement.setAttribute("tenantId", tenantId);
                 tenantComponentElement.setAttribute("componentName", componentName);
                 tenantComponentElement.setAttribute("sequenceNum", String.valueOf(componentNameCount));
                 entityEngineXmlElement.appendChild(tenantComponentElement);
                 componentNameCount += 10;
             }
             
             // Tenant Data Source
             
             /* org.ofbiz */
             Element ofbizDataSourceElement = document.createElement("TenantDataSource");
             ofbizDataSourceElement.setAttribute("tenantId", tenantId);
             ofbizDataSourceElement.setAttribute("entityGroupName", "org.ofbiz");
             ofbizDataSourceElement.setAttribute("jdbcUri", jdbcUriPrefix + dbPrefix + tenantId + "ofbiz" + jdbcUriSuffix);
             ofbizDataSourceElement.setAttribute("jdbcUsername", jdbcUsername);
             ofbizDataSourceElement.setAttribute("jdbcPassword", jdbcPassword);
             entityEngineXmlElement.appendChild(ofbizDataSourceElement);
             
             /* org.ofbiz.olap */
             Element olapDataSourceElement = document.createElement("TenantDataSource");
             olapDataSourceElement.setAttribute("tenantId", tenantId);
             olapDataSourceElement.setAttribute("entityGroupName", "org.ofbiz.olap");
             olapDataSourceElement.setAttribute("jdbcUri", jdbcUriPrefix + dbPrefix +  tenantId + "olap" + jdbcUriSuffix);
             olapDataSourceElement.setAttribute("jdbcUsername", jdbcUsername);
             olapDataSourceElement.setAttribute("jdbcPassword", jdbcPassword);
             entityEngineXmlElement.appendChild(olapDataSourceElement);
             
             // Tenant Domain Name
             String[] domainNameTokens = domainNames.split(",");
             for (String domainNameToken : domainNameTokens) {
                 int slashIndex = domainNameToken.indexOf("/");
                 String domainName = null;
                 String initialPath = null;
                 
                 if (slashIndex > 0) {
                     domainName = domainNameToken.substring(0, slashIndex).trim();
                     initialPath = domainNameToken.substring(slashIndex).trim();
                 } else {
                     domainName = domainNameToken.trim();
                     initialPath = "/";
                 }
 
                 Element tenantDomainNameElement = document.createElement("TenantDomainName");
                 tenantDomainNameElement.setAttribute("tenantId", tenantId);
                 tenantDomainNameElement.setAttribute("domainName", domainName);
                 tenantDomainNameElement.setAttribute("initialPath", initialPath);
                 entityEngineXmlElement.appendChild(tenantDomainNameElement);
             }
             
             // write XML document
             try {
                 UtilXml.writeXmlDocument(file, entityEngineXmlElement);
             } catch (Exception e) {
                 Debug.logError(e, module);
             }
                        
         }
     }
 
 }
