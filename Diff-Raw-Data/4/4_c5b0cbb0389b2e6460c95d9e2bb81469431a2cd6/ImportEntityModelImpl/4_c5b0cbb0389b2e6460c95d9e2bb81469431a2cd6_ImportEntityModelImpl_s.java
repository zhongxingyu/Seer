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
 * $Id: ImportEntityModelImpl.java,v 1.2 2007-08-03 23:12:25 jonnelson Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.federation.model;
 
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.common.DisplayUtils;
 import com.sun.identity.console.base.model.AMModelBase;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMFormatUtils;
 import com.sun.identity.federation.jaxb.entityconfig.IDPDescriptorConfigElement;
 import com.sun.identity.federation.jaxb.entityconfig.SPDescriptorConfigElement;
 import com.sun.identity.federation.meta.IDFFMetaException;
 import com.sun.identity.federation.meta.IDFFMetaManager;
 import com.sun.identity.federation.meta.IDFFMetaUtils;
import com.sun.identity.wsfederation.
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.saml2.jaxb.entityconfig.BaseConfigType;
 import com.sun.identity.saml2.jaxb.entityconfig.EntityConfigElement;
 import com.sun.identity.saml2.jaxb.metadata.EntityDescriptorElement;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaSecurityUtils;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.SMSException;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.HashSet;
 import javax.xml.bind.JAXBException;
 import javax.servlet.http.HttpServletRequest;
 import org.w3c.dom.Document;
 
 /**
  * This class provides import entity provider related functionality. Currently
  * the supported types are SAMLv2, IDFF, and WSFederation.
  */
 public class ImportEntityModelImpl extends AMModelBase
     implements ImportEntityModel 
 {
     private static final String IDFF = "urn:liberty:metadata";
     private static final String WSFED = "WS-Fed";
     private static final String DEFAULT_ROOT = "/";
     
     private String standardMetaData;
     private String extendedMetaData;
     private String realm;
    
     public ImportEntityModelImpl(HttpServletRequest req, Map map) {
 	super(req, map);
     }
     /**
      * Import one of the following entity types: SAMLv2, IDFF, or WSFed. The
      * parameters are the file names containing the standard and
      * extended metadata. The standard is required, while the extended is  
      * optional.
      *
      * @param requestData is a Map containing the name of the standard meta 
      *  data file name, and the name of the extended meta data file name.
      *
      * @throws AMConsoleException if unable to process this request.
      */
     public void importEntity(Map requestData) 
         throws AMConsoleException 
     {   
         // standardMetaData is the name of the file containing the metada. This
         // is a required parameter. If we don't find it in the request throw
         // an exception.
         String standardFile = (String)requestData.get(STANDARD_META);
         if (standardFile == null){            
             throw new AMConsoleException("missing.metadata");
         }
         standardMetaData = loadMetaDataFile(standardFile);
         String protocol = getProtocol(standardMetaData);
         
         // try loading the extended metadata, which is optional
         String extendedFile = (String)requestData.get(EXTENDED_META);
         if ((extendedFile != null) && (extendedFile.length() > 0)) {
             extendedMetaData = loadMetaDataFile(extendedFile);
             String tmp = getProtocol(standardMetaData);
             
             // the protocols defined in the standard and extended metadata
             // must be the same.
             if (!protocol.equals(tmp)) {
                 throw new AMConsoleException("protocol.mismatch");
             }
         }
 
         // the realm is used by the createXXX commands for storing the entity
         realm = (String)requestData.get(REALM_NAME);
         if (realm == null) {
             realm = DEFAULT_ROOT;
         }
         if (protocol.equals(SAML2Constants.PROTOCOL_NAMESPACE)) {
             createSAMLv2Entity();
         } else if (protocol.equals(IDFF)) {
             createIDFFEntity();
         } else {
             createWSFedEntity();
         }
     }
     
     private void createSAMLv2Entity() throws AMConsoleException {
         try {
             EntityConfigElement configElt = null;
             
             if (extendedMetaData != null) {
                 configElt = getEntityConfigElement();
 
                 if (configElt != null && configElt.isHosted()) {
                     List config = 
                        configElt.getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig();
                     if (!config.isEmpty()) {
                         BaseConfigType bConfig = (BaseConfigType)
                             config.iterator().next();
                         
                         // get the realm from the extended meta and use 
                         // for import
                         realm = SAML2MetaUtils.getRealmByMetaAlias(
                             bConfig.getMetaAlias());                     
                         if (debug.messageEnabled()) {
                             debug.message(
                                 "ImportEntityModel.createSAMLv2Entity " +
                                 "getting realm from ext metadata - " +
                                 realm);
                         }
                     }
                 }
             }
                         
             SAML2MetaManager metaManager = new SAML2MetaManager();
             if (standardMetaData != null) {
                 importSAML2MetaData(metaManager, realm);
             }
             
             if (configElt != null) {
                 metaManager.createEntityConfig(realm, configElt);
             }        
         } catch (SAML2MetaException e) {
             throw new AMConsoleException(e.getMessage());
         }
     }
     
     private EntityConfigElement getEntityConfigElement()
         throws SAML2MetaException, AMConsoleException 
     {
         try {
             Object obj = SAML2MetaUtils.convertStringToJAXB(extendedMetaData);
             return (obj instanceof EntityConfigElement) ?
                 (EntityConfigElement)obj : null;
         } catch (JAXBException e) {
             debug.error("ImportEntityModel.getEntityConfigElement", e);
             throw new AMConsoleException(e.getMessage());
         } catch (IllegalArgumentException e) {
             debug.error("ImportEntityModel.getEntityConfigElement", e);
             throw new AMConsoleException(e.getMessage());        
         }
     }
 
     private void importSAML2MetaData(SAML2MetaManager metaManager, String realm)
         throws SAML2MetaException, AMConsoleException
     {        
         try {
             Object obj = SAML2MetaUtils.convertStringToJAXB(standardMetaData);
             Document doc = XMLUtils.toDOMDocument(standardMetaData, debug);
 
             if (obj instanceof EntityDescriptorElement) {
                 EntityDescriptorElement descriptor =
                     (EntityDescriptorElement)obj;
              
                 SAML2MetaSecurityUtils.verifySignature(doc);
                 metaManager.createEntityDescriptor(realm, descriptor);             
             }
         } catch (JAXBException e) {
             debug.warning("ImportEntityModel.importSAML2MetaData", e);
             throw new AMConsoleException(e.getMessage());
         } catch (IllegalArgumentException e) {
             debug.warning("ImportEntityModel.importSAML2MetaData", e);
             throw new AMConsoleException(e.getMessage());
         } 
     }    
     
     private void createIDFFEntity() throws AMConsoleException {
         try {
             IDFFMetaManager metaManager = new IDFFMetaManager(
                 getUserSSOToken());
 
             com.sun.identity.federation.jaxb.entityconfig.EntityConfigElement
                 configElt = null;
             
             if (extendedMetaData != null) {
                 configElt = getIDFFEntityConfigElement();
                 
                 if ((configElt != null) && configElt.isHosted()) {
                     IDPDescriptorConfigElement idpConfig = 
                         IDFFMetaUtils.getIDPDescriptorConfig(configElt);
                     if (idpConfig != null) {
                         SAML2MetaUtils.getRealmByMetaAlias(
                             idpConfig.getMetaAlias());
                     } else {
                         SPDescriptorConfigElement spConfig =
                             IDFFMetaUtils.getSPDescriptorConfig(configElt);
                         if (spConfig != null) {
                             SAML2MetaUtils.getRealmByMetaAlias(
                                 spConfig.getMetaAlias());
                         }
                     }
                 }
             }
                        
             importIDFFMetaData(metaManager);            
             if (configElt != null) {
                 metaManager.createEntityConfig(configElt);                
             }
 
         } catch (IDFFMetaException e) {
             throw new AMConsoleException(e.getMessage());
         } 
     }
 
     private void importIDFFMetaData(IDFFMetaManager metaManager)
         throws IDFFMetaException, AMConsoleException
     {        
         if (standardMetaData == null) {
             if (debug.warningEnabled()) {
                 debug.warning("ImportEntityModel.importIDFFMetaData - " +
                     "metaData value was null, skipping import");
             }
             return;
         }
         
         try {
             Object obj = IDFFMetaUtils.convertStringToJAXB(standardMetaData);
 
             if (obj instanceof
                 com.sun.identity.liberty.ws.meta.jaxb.EntityDescriptorElement) {
                 com.sun.identity.liberty.ws.meta.jaxb.EntityDescriptorElement
                     descriptor =
                  (com.sun.identity.liberty.ws.meta.jaxb.EntityDescriptorElement)
                     obj;
 
                 //TODO: signature
                 //SAML2MetaSecurityUtils.verifySignature(doc);
                 //
                 metaManager.createEntityDescriptor(descriptor);
             }
     
         } catch (JAXBException e) {
             debug.warning("ImportEntityModel.importIDFFMetaData", e);
             throw new AMConsoleException(e.getMessage());
         } catch (IllegalArgumentException e) {
             debug.warning("ImportEntityModel.importIDFFMetaData", e);
             throw new AMConsoleException(e.getMessage());
         } 
     }
            
     private void createWSFedEntity() {
         // TBD not implemented yet
     }    
     
     private com.sun.identity.federation.jaxb.entityconfig.EntityConfigElement
         getIDFFEntityConfigElement() throws IDFFMetaException, AMConsoleException {
 
         try {
             Object obj = IDFFMetaUtils.convertStringToJAXB(extendedMetaData);
             return (obj instanceof 
             com.sun.identity.federation.jaxb.entityconfig.EntityConfigElement) ?
              (com.sun.identity.federation.jaxb.entityconfig.EntityConfigElement)
                 obj : null;
         } catch (JAXBException e) {            
             throw new AMConsoleException(e.getMessage());
         } catch (IllegalArgumentException e) {
             throw new AMConsoleException(e.getMessage());
         }
     }
 
     private static String loadMetaDataFile(String fileName)
         throws AMConsoleException 
     {
         BufferedReader br = null;
         StringBuffer buff = new StringBuffer();
         try {
             br = new BufferedReader(new FileReader(fileName));
             String line = br.readLine();
             while (line != null) {
                 buff.append(line).append("\n");
                 line = br.readLine();
             }
         } catch(IOException e){
             debug.error("ImportEnityModel.loadMetaDataFile", e);
             throw new AMConsoleException(e.getMessage());
         } finally {
             if (br != null) {
                 try {
                     br.close();
                 } catch(IOException e){
                     debug.error("ImportEnityModel.loadMetaDataFile", e);
                     throw new AMConsoleException(e.getMessage());
                 }                
             }
         }
         return buff.toString();
     }
     
     private String getProtocol(String metaData) {
         String protocol = SAML2Constants.PROTOCOL_NAMESPACE;
         if (metaData.contains(WSFED)) {
             protocol = WSFED;
         } else if (metaData.contains(IDFF)) {
             protocol = IDFF;            
         }                
         
         return protocol;           
     }
 }
