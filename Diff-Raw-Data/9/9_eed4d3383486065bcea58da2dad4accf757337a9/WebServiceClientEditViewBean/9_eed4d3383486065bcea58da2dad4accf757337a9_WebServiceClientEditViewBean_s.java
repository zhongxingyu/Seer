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
 * $Id: WebServiceClientEditViewBean.java,v 1.3 2007-08-28 22:20:58 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.idm;
 
 import com.iplanet.jato.RequestContext;
 import com.iplanet.jato.model.ModelControlException;
 import com.iplanet.jato.view.event.RequestInvocationEvent;
 import com.sun.identity.console.base.AMPropertySheet;
 import com.sun.identity.console.base.model.AMAdminConstants;
 import com.sun.identity.console.base.model.AMAdminUtils;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMPropertySheetModel;
 import com.sun.identity.console.idm.model.EntitiesModel;
 import com.sun.identity.wss.security.SecurityMechanism;
 import com.sun.web.ui.view.alert.CCAlert;
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import javax.servlet.http.HttpServletRequest;
 
 public class WebServiceClientEditViewBean 
     extends WebServiceEditViewBean {
     private static final String PAGE_NAME = "WebServiceClientEdit";
     static final String CHILD_NAME_USERTOKEN_NAME = "usernametokenname";
     static final String CHILD_NAME_USERTOKEN_PASSWORD = "usernametokenpassword";
     private static final String CHILD_NAME_STS_ENDPOINT =
         "securitytokenendpoint";
     private static final String CHILD_NAME_STS_METADATA_ENDPOINT =
         "securitytokenmetadataendpoint";
     private static final String ATTR_NAME_STS_ENDPOINT = "STSEndpoint";
     private static final String ATTR_NAME_STS_MEX_ENDPOINT = "STSMexEndpoint";
         
     public static final String DEFAULT_DISPLAY_URL =
         "/console/idm/WebServiceClientEdit.jsp";
     
     private Set clientUIProperties = parseExternalizeUIProperties(
         "webServiceClientUI");
     public WebServiceClientEditViewBean() {
         super(PAGE_NAME, DEFAULT_DISPLAY_URL, true,
             "com/sun/identity/console/propertyWebServiceClientEdit.xml");
     }
 
     protected void setExtendedDefaultValues(Map attrValues)
         throws AMConsoleException {
         Set values = (Set)attrValues.get(
             EntitiesModel.ATTR_NAME_DEVICE_KEY_VALUE);
         setExternalizeUIValues(clientUIProperties, values);
         setUserCredential(values);
 
         String secMech = getAttributeFromSet(values, ATTR_NAME_SECURITY_MECH);
         if (secMech.equals(SecurityMechanism.STS_SECURITY_URI)) {
             propertySheetModel.setValue(CHILD_NAME_STS_ENDPOINT,
                 getAttributeFromSet(values, ATTR_NAME_STS_ENDPOINT));
             propertySheetModel.setValue(CHILD_NAME_STS_METADATA_ENDPOINT,
                 getAttributeFromSet(values, ATTR_NAME_STS_MEX_ENDPOINT));
         }
     }
     
     private void setUserCredential(Set values) {
         String userCredential = getAttributeFromSet(values, 
             ATTR_NAME_USERCREDENTIAL);
         if ((userCredential != null) && (userCredential.trim().length() > 0)) {
             String[] result = splitUserCredToken(userCredential);
             if (result != null) {
                 propertySheetModel.setValue(CHILD_NAME_USERTOKEN_NAME, 
                     result[0]);
                 propertySheetModel.setValue(CHILD_NAME_USERTOKEN_PASSWORD, 
                     result[1]);
             }
         }
     }
 
     protected void getExtendedFormsValues(Set deviceKeyValue)
         throws AMConsoleException {
         String userCredName = (String)propertySheetModel.getValue(
             CHILD_NAME_USERTOKEN_NAME);
         String userCredPwd = (String)propertySheetModel.getValue(
             CHILD_NAME_USERTOKEN_PASSWORD);
 
         if ((userCredName != null) && (userCredName.trim().length() > 0) &&
             (userCredPwd  != null) && (userCredPwd.trim().length() > 0)
         ) {
             deviceKeyValue.add(ATTR_NAME_USERCREDENTIAL + "=" +
                 ATTR_NAME_USERCREDENTIAL_NAME + userCredName + "|" + 
                 ATTR_NAME_USERCREDENTIAL_PWD + userCredPwd);
         }
 
         String secMech = (String)propertySheetModel.getValue(
             ATTR_NAME_SECURITY_MECH);
         if (secMech.equals(SecurityMechanism.STS_SECURITY_URI)) {
             String stsURL = (String)propertySheetModel.getValue(
                 CHILD_NAME_STS_ENDPOINT);
             String stsMetaURL = (String)propertySheetModel.getValue(
                 CHILD_NAME_STS_METADATA_ENDPOINT);
 
             if ((stsURL == null) || (stsURL.trim().length() == 0) ||
                 (stsMetaURL == null) || (stsMetaURL.trim().length() == 0)
             ) {
                 throw new AMConsoleException(getModel().getLocalizedString(
                     "web.services.exception.securitytoken.info.missing"));
            }
             deviceKeyValue.add(ATTR_NAME_STS_ENDPOINT + "=" + stsURL);
             deviceKeyValue.add(ATTR_NAME_STS_MEX_ENDPOINT + "=" + stsMetaURL);
         }
 
         getExternalizeUIValues(clientUIProperties, deviceKeyValue);
         deviceKeyValue.add(EntitiesViewBean.ATTR_NAME_AGENT_TYPE + "WSC");
     }
 
     protected List getMessageLevelSecurityMech() {
         return SecurityMechanism.getAllWSCSecurityMechanisms();
     }
 } 
