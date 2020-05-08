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
 * $Id: ImportEntityViewBean.java,v 1.2 2007-08-03 22:29:02 jonnelson Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.federation;
 
 import com.iplanet.jato.RequestManager;
 import com.iplanet.jato.RequestContext;
 import com.iplanet.jato.model.ModelControlException;
 import com.iplanet.jato.view.View;
 import com.iplanet.jato.view.event.DisplayEvent;
 import com.iplanet.jato.view.event.RequestInvocationEvent;
 import com.iplanet.jato.view.html.OptionList;
 import com.sun.identity.console.base.AMPropertySheet;
 import com.sun.identity.console.base.AMPrimaryMastHeadViewBean;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMModel;
 import com.sun.identity.console.base.model.AMModelBase;
 import com.sun.identity.console.base.model.AMPropertySheetModel;
 import com.sun.identity.console.federation.model.ImportEntityModel;
 import com.sun.identity.console.federation.model.ImportEntityModelImpl;
 import com.sun.web.ui.model.CCPageTitleModel;
 import com.sun.web.ui.view.alert.CCAlert;
 import com.sun.web.ui.view.pagetitle.CCPageTitle;
 import com.sun.web.ui.view.html.CCDropDownMenu;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Collections;
 import javax.servlet.http.HttpServletRequest;
 
 public class ImportEntityViewBean
     extends AMPrimaryMastHeadViewBean
 {
     public static final String DEFAULT_DISPLAY_URL =
         "/console/federation/ImportEntity.jsp";
     
     private AMPropertySheetModel psModel;
     private CCPageTitleModel ptModel;
     private static final String PROPERTIES = "propertyAttributes";
 
     /**
      * Creates a authentication domains view bean.
      */
     public ImportEntityViewBean() {
         super("ImportEntity");
         setDefaultDisplayURL(DEFAULT_DISPLAY_URL);                   
         createPageTitleModel();
         createPropertyModel();
         registerChildren();    
     }
 
     protected void registerChildren() {       
         ptModel.registerChildren(this);
         registerChild(PROPERTIES, AMPropertySheet.class);
         psModel.registerChildren(this);
         super.registerChildren();
     }
 
     protected View createChild(String name) {
         View view = null;
 
         if (name.equals("pgtitle")) {
             view = new CCPageTitle(this, ptModel, name);
         } else if (name.equals(PROPERTIES)) {
             view = new AMPropertySheet(this, psModel, name);
         } else if ((psModel != null) && psModel.isChildSupported(name)) {
             view = psModel.createChild(this, name, getModel());
         } else if ((ptModel != null) && ptModel.isChildSupported(name)) {
             view = ptModel.createChild(this, name);
         } else {
             view = super.createChild(name);
         }
 
         return view;
     }
      
     public void beginDisplay(DisplayEvent event)
         throws ModelControlException
     {
         super.beginDisplay(event);
         populateRealmData();
         
         // hide these sections for now. We may enable them later
        psModel.setVisible("realmProperty", false);     
         psModel.setVisible("standardURLProperty", false);
         psModel.setVisible("extendedURLProperty", false);
     }    
     
     private void populateRealmData() {
          Set realmNames = Collections.EMPTY_SET;
          ImportEntityModel model = (ImportEntityModel)getModel();
          try{
              realmNames = model.getRealmNames("/", "*");         
              CCDropDownMenu menu =
                  (CCDropDownMenu)getChild(ImportEntityModel.REALM_NAME);
              OptionList sortedList = createOptionList(realmNames);
              OptionList optList =  new OptionList();
              int size = sortedList.size();
              String name;
              for (int i = 0; i < size; i++ ) {
                  name = sortedList.getValue(i);
                  optList.add(getPath(name), name);
              }
              menu.setOptions(optList);
          } catch (AMConsoleException e) {
              setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
                  e.getMessage());
          }
      }
      
     private void createPropertyModel() {
         psModel = new AMPropertySheetModel(
             getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/importEntityPropertySheet.xml"));
         psModel.clear();
     }
 
     private void createPageTitleModel() {
         ptModel = new CCPageTitleModel(
             getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/twoBtnsPageTitle.xml"));      
         ptModel.setValue("button1", "button.ok");
         ptModel.setValue("button2", "button.cancel");            
     }
 
     protected AMModel getModelInternal() {
         RequestContext rc = RequestManager.getRequestContext();
         HttpServletRequest req = rc.getRequest();
         return new ImportEntityModelImpl(req, getPageSessionAttributes());
     }
 
     /**
      * Handles upload entity button request. There are two fields on this page:
      * one for standard metadata and the other for extended. The standard is
      * required.
      *
      * @param event Request invocation event
      * @throws ModelControlException
      */
     public void handleButton1Request(RequestInvocationEvent event)
         throws ModelControlException
     {
         ImportEntityModel model = (ImportEntityModel)getModel();
         Map data = new HashMap(6);
               
         String realm = (String)getDisplayFieldValue(model.REALM_NAME);
         data.put(model.REALM_NAME, realm);
         
         String standard = (String)getDisplayFieldValue("standardFileName");   
 
         // TBD add check on URL field as well when it is enabled
         if ((standard == null) || (standard.length() < 1)) {            
             psModel.setErrorProperty("standardFileNameProperty", true);            
             setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.input.error",
                 "import.entity.missing.metadata");  
             
             forwardTo();
         } else {
             data.put(ImportEntityModel.STANDARD_META, standard);
             
             String extended = (String)getDisplayFieldValue("extendedFileName");
             if ((extended != null) || (extended.trim().length() > 0)) {
                 data.put(ImportEntityModel.EXTENDED_META, extended);
             }            
             
             try {
                 model.importEntity(data);
                
                 StringBuffer message = new StringBuffer();
                 message.append(
                     model.getLocalizedString("import.entity.metadata.success"))
                         .append("<ul>");                       
                         
                 // build the success message.
                 // don't need the realm name in the message so remove it first.
                 data.remove(ImportEntityModel.REALM_NAME);                
                 for (Iterator i = data.keySet().iterator(); i.hasNext();) {
                     String key = (String)i.next();  
                     String value = (String)data.get(key);
                     if ((value != null) && (value.length() > 0)) {
                         message.append("<li>").append((String)data.get(key));                       
                     }
                 }
                 
                 // set the message in the main view
                 setPageSessionAttribute(
                     FederationViewBean.MESSAGE_TEXT, message.toString());                
                 FederationViewBean vb = 
                     (FederationViewBean)getViewBean(FederationViewBean.class);
                 passPgSessionMap(vb);
                 vb.forwardTo(getRequestContext()); 
                     
             } catch (AMConsoleException ame) {
                 debug.warning("ImportEntityViewBean.handleButton1req ", ame);
                 setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
                     ame.getMessage());
                 
                 forwardTo();
             }
         }
     }
     
     /**
      * Handles cancel request.
      *
      * @param event Request invocation event
      */
     public void handleButton2Request(RequestInvocationEvent event) {
         FederationViewBean vb = 
             (FederationViewBean)getViewBean(FederationViewBean.class);
         backTrail();
         passPgSessionMap(vb);
         vb.forwardTo(getRequestContext());       
     }
 } 
