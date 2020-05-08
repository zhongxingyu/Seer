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
 * $Id: SAML2COTUtils.java,v 1.2 2007-09-11 19:39:51 bina Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.saml2.meta;
 
 import javax.xml.bind.JAXBException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.logging.Level;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.saml2.logging.LogUtil;
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.saml2.jaxb.entityconfig.AttributeType;
 import com.sun.identity.saml2.jaxb.entityconfig.BaseConfigType;
 import com.sun.identity.saml2.jaxb.entityconfig.ObjectFactory;
 import com.sun.identity.saml2.jaxb.entityconfig.EntityConfigElement;
 import com.sun.identity.saml2.jaxb.metadata.EntityDescriptorElement;
 
 /**
  * The <code>SAML2COTUtils</code> provides utility methods to update
  * the SAML2 Entity Configuration <code>cotlist</code> attributes
  * in the Service and Identity Provider configurations.
  */
 public class SAML2COTUtils {
     
     private static Debug debug = SAML2MetaUtils.debug;
     
     /**
      * Default Constructor.
      */
     public SAML2COTUtils()  {
         
     }
     
     /**
      * Updates the entity config to add the circle of turst name to the
      * <code>cotlist</code> attribute. The Service Provider and Identity
      * Provider Configuration are updated.
      *
      * @param realm the realm name where the entity configuration is.
      * @param name the circle of trust name.
      * @entityId the name of the Entity identifier.
      * @throws SAML2MetaException if there is a configuration error when
      *         updating the configuration.
      * @throws JAXBException is there is an error updating the entity
      *          configuration.
      */
     
     public void updateEntityConfig(String realm, String name, String entityId)
     throws SAML2MetaException, JAXBException {
         String classMethod = "SAML2COTUtils.updateEntityConfig: ";
         SAML2MetaManager metaManager = new SAML2MetaManager();
         ObjectFactory objFactory = new ObjectFactory();
         // Check whether the entity id existed in the DS
         EntityDescriptorElement edes = metaManager.getEntityDescriptor(
                 realm, entityId);
         if (edes == null) {
             debug.error(classMethod +"No such entity: " + entityId);
             String[] data = {realm, entityId};
             throw new SAML2MetaException("entityid_invalid", data);
         }
         EntityConfigElement eConfig = metaManager.getEntityConfig(
                 realm, entityId);
         if (eConfig == null) {
             BaseConfigType bctype = null;
             AttributeType atype = objFactory.createAttributeType();
             atype.setName(SAML2Constants.COT_LIST);
             atype.getValue().add(name);
             // add to eConfig
             EntityConfigElement ele = objFactory.createEntityConfigElement();
             ele.setEntityID(entityId);
             ele.setHosted(false);
             List ll =
                     ele.getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig();
             // Decide which role EntityDescriptorElement includes
             // It could have one sp and one idp.
             if (SAML2MetaUtils.getSPSSODescriptor(edes) != null) {
                 bctype = objFactory.createSPSSOConfigElement();
                 bctype.getAttribute().add(atype);
                 ll.add(bctype);
             }
             if (SAML2MetaUtils.getIDPSSODescriptor(edes) != null) {
                 bctype = objFactory.createIDPSSOConfigElement();
                 bctype.getAttribute().add(atype);
                 ll.add(bctype);
             }
             if (SAML2MetaUtils.getPolicyEnforcementPointDescriptor(edes)
                     != null) {
                 bctype =
                     objFactory.createXACMLAuthzDecisionQueryConfigElement();
                 bctype.getAttribute().add(atype);
                 ll.add(bctype);
             }
             if (SAML2MetaUtils.getPolicyDecisionPointDescriptor(edes) != null) {
                 bctype = objFactory.createXACMLPDPConfigElement();
                 bctype.getAttribute().add(atype);
                 ll.add(bctype);
             }
             metaManager.setEntityConfig(realm,ele);
         } else {
             List elist = eConfig.
                     getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig();
            boolean foundCOT = false;
             for (Iterator iter = elist.iterator(); iter.hasNext();) {
                 BaseConfigType bConfig = (BaseConfigType)iter.next();
                 List list = bConfig.getAttribute();
                 for (Iterator iter2 = list.iterator(); iter2.hasNext();) {
                     AttributeType avp = (AttributeType)iter2.next();
                     if (avp.getName().trim().equalsIgnoreCase(
                             SAML2Constants.COT_LIST)) {
                         foundCOT = true;
                         List avpl = avp.getValue();
                         if (avpl.isEmpty() ||!containsValue(avpl,name)) {
                             avpl.add(name);
                             metaManager.setEntityConfig(realm,eConfig);
                             break;
                         }
                     }
                 }
                 // no cot_list in the original entity config
                 if (!foundCOT) {
                     AttributeType atype = objFactory.createAttributeType();
                     atype.setName(SAML2Constants.COT_LIST);
                     atype.getValue().add(name);
                     list.add(atype);
                     metaManager.setEntityConfig(realm, eConfig);
                 }
             }
         }
     }
     
     private boolean containsValue(List list, String name) {
         for (Iterator iter = list.iterator(); iter.hasNext();) {
             if (((String) iter.next()).trim().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Removes the circle trust name passed from the <code>cotlist</code>
      * attribute in the Entity Config. The Service Provider and Identity
      * Provider Entity Configuration are updated.
      *
      * @param name the circle of trust name to be removed.
      * @param entityId the entity identifier of the provider.
      * @throws SAML2MetaException if there is an error updating the entity
      *          config.
      * @throws JAXBException if there is an error updating the entity config.
      */
     
     public void removeFromEntityConfig(String realm,String name,String entityId)
     throws SAML2MetaException, JAXBException {
         String classMethod = "SAML2COTUtils.removeFromEntityConfig: ";
         SAML2MetaManager metaManager = new SAML2MetaManager();
         // Check whether the entity id existed in the DS
         EntityDescriptorElement edes = metaManager.getEntityDescriptor(
                 realm, entityId);
         if (edes == null) {
             debug.error(classMethod +"No such entity: " + entityId);
             String[] data = {realm, entityId};
             throw new SAML2MetaException("entityid_invalid", data);
         }
         EntityConfigElement eConfig = metaManager.getEntityConfig(
                 realm, entityId);
         if (eConfig != null) {
             List elist = eConfig.
                     getIDPSSOConfigOrSPSSOConfigOrAuthnAuthorityConfig();
             for (Iterator iter = elist.iterator(); iter.hasNext();) {
                 BaseConfigType bConfig = (BaseConfigType)iter.next();
                 List list = bConfig.getAttribute();
                 for (Iterator iter2 = list.iterator(); iter2.hasNext();) {
                     AttributeType avp = (AttributeType)iter2.next();
                     if (avp.getName().trim().equalsIgnoreCase(
                             SAML2Constants.COT_LIST)) {
                         List avpl = avp.getValue();
                         if (avpl != null && !avpl.isEmpty() &&
                                 containsValue(avpl,name)) {
                             avpl.remove(name);
                             metaManager.setEntityConfig(realm,eConfig);
                             break;
                         }
                     }
                 }
             }
         }
     }
 }
