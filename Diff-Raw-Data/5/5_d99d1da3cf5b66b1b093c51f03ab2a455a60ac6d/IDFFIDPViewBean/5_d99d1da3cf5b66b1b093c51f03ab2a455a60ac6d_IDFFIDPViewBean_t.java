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
 * $Id: IDFFIDPViewBean.java,v 1.4 2007-08-29 21:59:03 asyhuang Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.federation;
 
 import com.iplanet.jato.model.ModelControlException;
 import com.iplanet.jato.view.event.DisplayEvent;
 import com.iplanet.jato.view.event.RequestInvocationEvent;
 import com.sun.identity.console.base.AMPropertySheet;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMModel;
 import com.sun.identity.console.base.model.AMPropertySheetModel;
 import com.sun.identity.console.federation.model.IDFFEntityProviderModel;
 import com.sun.identity.console.federation.model.IDFFEntityProviderModelImpl;
 import com.sun.identity.federation.common.IFSConstants;
 import com.sun.web.ui.view.alert.CCAlert;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBException;
 
 public class IDFFIDPViewBean
     extends IDFFViewBeanBase 
 {
     public static final String DEFAULT_DISPLAY_URL =
         "/console/federation/IDFFIDP.jsp";
     
     public IDFFIDPViewBean() {
         super("IDFFIDP");
         setDefaultDisplayURL(DEFAULT_DISPLAY_URL);
     }
     
     public void beginDisplay(DisplayEvent event)
         throws ModelControlException 
     {
         super.beginDisplay(event);
         IDFFEntityProviderModel model =
             (IDFFEntityProviderModel)getModelInternal();
         
         psModel.setValue(IDFFEntityProviderModel.ATTR_PROVIDER_TYPE,
             (String)getPageSessionAttribute(ENTITY_LOCATION));
         
         populateValue(entityName);              
     }
     
     private void populateValue(String name) {
         IDFFEntityProviderModel model =
             (IDFFEntityProviderModel)getModelInternal();              
        Map values = model.getEntityIDPDescriptor(name);                 
         values.putAll(model.getEntityConfig(name,
             IFSConstants.IDP, location));
         AMPropertySheet ps = (AMPropertySheet)getChild(PROPERTY_ATTRIBUTES);
         ps.setAttributeValues(values, model);      
     }
     
     protected AMModel getModelInternal() {
         HttpServletRequest req = getRequestContext().getRequest();
         return new IDFFEntityProviderModelImpl(req, getPageSessionAttributes());
     }
     
     protected void createPropertyModel() {
         retrieveCommonProperties();
         if (isHosted()) {           
             psModel = new AMPropertySheetModel(
                 getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/propertyIDFFIDPHosted.xml"));
         } else {           
             psModel = new AMPropertySheetModel(
                 getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/propertyIDFFIDPRemote.xml"));
         }
         psModel.clear();
     }
     
     /**
      * Handles save
      *
      * @param event Request invocation event
      */
     public void handleButton1Request(RequestInvocationEvent event)
         throws ModelControlException 
     {
         retrieveCommonProperties();
                
         try {
             IDFFEntityProviderModel model = 
                 (IDFFEntityProviderModel)getModel();
             AMPropertySheet ps = 
                 (AMPropertySheet)getChild(PROPERTY_ATTRIBUTES);
             
             // update standard metadata
             Map origStdMeta =  model.getEntityIDPDescriptor(entityName);
             Map stdValues = ps.getAttributeValues(origStdMeta, false, model);
             model.updateEntityDescriptor(entityName, 
                 IFSConstants.IDP, stdValues);
             
             //update extended metadata
             Map origExtMeta = model.getEntityConfig(entityName,
                 IFSConstants.IDP, location);
             Map extValues = ps.getAttributeValues(origExtMeta, false, model);
             model.updateEntityConfig(entityName, IFSConstants.IDP, extValues);
             
             setInlineAlertMessage(CCAlert.TYPE_INFO,
                 "message.information",
                 "idff.entityDescriptor.provider.idp.updated");
         } catch (AMConsoleException e) {
             setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
                 e.getMessage());
         } catch (JAXBException e){
             setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
                 e.getMessage());
         }
         forwardTo();
     }
 }
