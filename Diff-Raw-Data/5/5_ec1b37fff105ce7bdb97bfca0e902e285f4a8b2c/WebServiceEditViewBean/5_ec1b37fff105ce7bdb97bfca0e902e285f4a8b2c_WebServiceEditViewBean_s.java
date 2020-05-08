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
 * $Id: WebServiceEditViewBean.java,v 1.2 2007-06-14 21:02:50 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.idm;
 
 import com.iplanet.jato.model.ModelControlException;
 import com.iplanet.jato.view.event.RequestInvocationEvent;
 import com.sun.identity.console.base.AMPropertySheet;
 import com.sun.identity.console.base.model.AMAdminConstants;
 import com.sun.identity.console.base.model.AMAdminUtils;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMModel;
 import com.sun.identity.console.base.model.AMPropertySheetModel;
 import com.sun.identity.console.idm.model.EntitiesModel;
 import com.sun.identity.wss.provider.ProviderConfig;
 import com.sun.identity.wss.security.SecurityMechanism;
 import com.sun.web.ui.view.alert.CCAlert;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import javax.servlet.http.HttpServletRequest;
 
 public abstract class WebServiceEditViewBean 
     extends EntityEditViewBean {
     static final String TRACKER_ATTR = "pgWebServiceTracker";
     static final String SECURITY_MECH_PREFIX = "securitymech-";
     static final String ATTR_NAME_SECURITY_MECH = "SecurityMech";
     static final String ATTR_NAME_USE_DEFAULT_KEYSTORE = "useDefaultStore";
     static final String CHILD_NAME_KEYSTORE_USAGE = "keystoreusage";
     static final String ATTR_NAME_KEY_STORE_LOCATION = "KeyStoreFile";
     static final String CHILD_NAME_KEY_STORE_LOCATION = "keystorelocation";
     static final String ATTR_NAME_KEY_STORE_PASSWORD = "KeyStorePassword";
     static final String CHILD_NAME_KEY_STORE_PASSWORD = "keystorepassword";
     static final String ATTR_NAME_KEY_PASSWORD = "KeyPassword";
     static final String CHILD_NAME_KEY_PASSWORD = "keypassword";
    static final String ATTR_NAME_CERT_ALIAS = "CertAlias";
     static final String CHILD_NAME_CERT_ALIAS = "certalias";
 
     static final String ATTR_NAME_USERCREDENTIAL = "UserCredential";
     static final String ATTR_NAME_USERCREDENTIAL_NAME = "UserName:";
     static final String ATTR_NAME_USERCREDENTIAL_PWD = "UserPassword:";
     static final String CHILD_NAME_USERTOKEN_NAME = "usernametokenname";
     static final String CHILD_NAME_USERTOKEN_PASSWORD = "usernametokenpassword";
     
     private Set externalizeUIProperties = parseExternalizeUIProperties(
         "webServiceUI");
     private String xmlFileName;
     private String pageName;
     private boolean isWebClient;
 
     WebServiceEditViewBean(
         String pageName, 
         String defaultURL,
         boolean isWebClient,
         String xml) {
         super(pageName, defaultURL);
         this.pageName = pageName;
         this.isWebClient = isWebClient;
         xmlFileName = xml;
     }
 
     protected AMPropertySheetModel createPropertySheetModel(String type) {
         AMPropertySheetModel psModel = null;
         String xml = AMAdminUtils.getStringFromInputStream(
             getClass().getClassLoader().getResourceAsStream(xmlFileName));
         StringBuffer buff = new StringBuffer();
         List securityMechs = getMessageLevelSecurityMech();
         for (Iterator i = securityMechs.iterator(); i.hasNext(); ) {
             SecurityMechanism mech = (SecurityMechanism)i.next();
             String displayName = mech.getName();
             String uri = mech.getURI();
             String[] params = new String[2];
 
             if (!isWebClient) {
                 params[0] = SECURITY_MECH_PREFIX + uri;
                 params[1] = displayName;
                 buff.append(MessageFormat.format(PROPERTY_CHECKBOX_TEMPLATE,
                     params));
             } else {
                 params[0] = displayName;
                 params[1] = uri;
                 buff.append(MessageFormat.format(PROPERTY_OPTION_TEMPLATE,
                     params));
             }
         }
         xml = xml.replaceAll("@securitymechanism@", buff.toString());
         return new AMPropertySheetModel(xml);
     }
     
     protected List getMessageLevelSecurityMech() {
         return ProviderConfig.getAllMessageLevelSecurityMech();
     }
 
     protected void setDefaultValues(String type)
         throws AMConsoleException {
         if (propertySheetModel != null) {
             EntitiesModel model = (EntitiesModel)getModel();
             String universalId = (String)getPageSessionAttribute(UNIVERSAL_ID);
 
             if (!submitCycle) {
                 propertySheetModel.clear();
 
                 try {
                     Map attrValues = (Map)removePageSessionAttribute(
                         TRACKER_ATTR);
                     if (attrValues == null) {
                         attrValues = model.getAttributeValues(
                             universalId, false);
                     }
                     
                     AMPropertySheet prop = (AMPropertySheet)getChild(
                         PROPERTY_ATTRIBUTE);
                     prop.setAttributeValues(attrValues, model);
                     Set values = (Set)attrValues.get(
                         EntitiesModel.ATTR_NAME_DEVICE_KEY_VALUE);
                     setSecurityMech(getAttributeFromSet(
                         values, ATTR_NAME_SECURITY_MECH));
                     setExternalizeUIValues(externalizeUIProperties, values);
                     setKeyStoreInfo(values);
                     setExtendedDefaultValues(attrValues);
                 } catch (AMConsoleException e) {
                     setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
                         e.getMessage());
                 }
             }
 
             String[] uuid = {universalId};
             propertySheetModel.setValues(PROPERTY_UUID, uuid, model);
         }
     }
     
     void setExternalizeUIValues(Set extUIs, Set values) {
         for (Iterator i = extUIs.iterator(); i.hasNext(); 
         ) {
             WebServiceUIElement elm = (WebServiceUIElement)i.next();
 
             if (elm.attrType.equals(WebServiceUIElement.TYPE_BOOL)){
                 setBooleanValue(getAttributeFromSet(values,
                     elm.attrName), elm.childName);
             } else if (elm.attrType.equals(
                 WebServiceUIElement.TYPE_TEXT)
             ) {
                 setBooleanValue(getAttributeFromSet(values,
                     elm.attrName), elm.childName);
             }
         }
     }
     
     void getExternalizeUIValues(Set extUIs, Set deviceKeyValue) {
         for (Iterator i = extUIs.iterator(); i.hasNext(); ) {
             WebServiceUIElement elm = (WebServiceUIElement)i.next();
 
             if (elm.attrType.equals(WebServiceUIElement.TYPE_BOOL)) {
                 String val = (String)propertySheetModel.getValue(elm.childName);
                 
                 if (val.equals("true")) {
                     deviceKeyValue.add(elm.attrName + "=true");
                 } else {
                     deviceKeyValue.add(elm.attrName + "=false");
                 }
             } else if (elm.attrType.equals(WebServiceUIElement.TYPE_TEXT)) {
                 String val = (String)propertySheetModel.getValue(elm.childName);
                 if ((val != null) && val.length() > 0) {
                     deviceKeyValue.add(elm.attrName + "=" + val);
                 } else {
                     deviceKeyValue.add(elm.attrName + "=");
                 }
             }
         }   
     }
     
     private void setKeyStoreInfo(Set values) {
         String useDefaultKeyStore = getAttributeFromSet(values, 
             ATTR_NAME_USE_DEFAULT_KEYSTORE);
         if ((useDefaultKeyStore != null) && 
             useDefaultKeyStore.equals("true")
         ) {
             propertySheetModel.setValue(CHILD_NAME_KEYSTORE_USAGE, "default"); 
         } else {
             propertySheetModel.setValue(CHILD_NAME_KEYSTORE_USAGE, "custom"); 
             propertySheetModel.setValue(CHILD_NAME_KEY_STORE_LOCATION, 
                 getAttributeFromSet(values, ATTR_NAME_KEY_STORE_LOCATION));
             propertySheetModel.setValue(CHILD_NAME_KEY_STORE_PASSWORD, 
                 getAttributeFromSet(values, ATTR_NAME_KEY_STORE_PASSWORD));
             propertySheetModel.setValue(CHILD_NAME_KEY_PASSWORD, 
                 getAttributeFromSet(values, ATTR_NAME_KEY_PASSWORD));
             propertySheetModel.setValue(CHILD_NAME_CERT_ALIAS, 
                 getAttributeFromSet(values, ATTR_NAME_CERT_ALIAS));
         }
     }
 
     protected static String getAttributeFromSet(Set values, String prefix) {
         String value = null;
         if ((values != null) && !values.isEmpty()) {
             prefix = prefix + "=";
             for (Iterator i = values.iterator(); i.hasNext() && (value == null);
             ) {
                 String val = (String)i.next();
                 if (val.startsWith(prefix)) {
                     value = val.substring(prefix.length());
                 }
             }
         }
         return value;
     }
     
     protected static boolean removeAttributeFromSet(Set values, String prefix) {
         boolean done = false;
         if (values != null) {
             prefix = prefix + "=";
             for (Iterator i = values.iterator(); i.hasNext() && !done; ) {
                 String val = (String)i.next();
                 if (val.startsWith(prefix)) {
                     i.remove();
                     done = true;
                 }
             }
         }
         return done;
     }
     
     private void setSecurityMech(String value) {
         if (isWebClient) {
             propertySheetModel.setValue(ATTR_NAME_SECURITY_MECH, value);
         } else {
             if ((value != null) && (value.length() > 0)) {
                 StringTokenizer st = new StringTokenizer(value, ",");
                 while (st.hasMoreTokens()) {
                     String uri = st.nextToken().trim();
                     if (uri.length() > 0) {
                         propertySheetModel.setValue(SECURITY_MECH_PREFIX + uri, 
                             "true");
                     }
                 }
             }
         }
     }
     
     protected void setBooleanValue(String value, String childName) {
         if ((value != null) && (value.length() > 0)) {
             propertySheetModel.setValue(childName, value); 
         }
     }
 
     public void handleButton1Request(RequestInvocationEvent event)
         throws ModelControlException {
         submitCycle = true;
         EntitiesModel model = (EntitiesModel)getModel();
         String universalId = (String)getPageSessionAttribute(UNIVERSAL_ID);
         try {
             Map values = getFormValues();
             String curRealm = (String)getPageSessionAttribute(
                 AMAdminConstants.CURRENT_REALM);
             model.modifyEntity(curRealm, universalId, values);
             setInlineAlertMessage(CCAlert.TYPE_INFO, "message.information",
                 "message.updated");
         } catch (AMConsoleException e) {
             setInlineAlertMessage(CCAlert.TYPE_ERROR, "message.error",
                 e.getMessage());
         }
         forwardTo();
     }
     
     protected Map getFormValues()
         throws AMConsoleException, ModelControlException {
         EntitiesModel model = (EntitiesModel)getModel();
         AMPropertySheet prop = (AMPropertySheet)getChild(PROPERTY_ATTRIBUTE);
         String universalId = (String)getPageSessionAttribute(UNIVERSAL_ID);
         Map oldValues = model.getAttributeValues(universalId, false);
         
         Map values = prop.getAttributeValues(oldValues.keySet());
         Set deviceKeyValue = new HashSet();
 
         if (!isWebClient) {
             getSecurityMech(deviceKeyValue);
         } else {
             String secMech = (String)propertySheetModel.getValue(
                 ATTR_NAME_SECURITY_MECH);
             if ((secMech != null) && (secMech.length() > 0)) {
                 deviceKeyValue.add(ATTR_NAME_SECURITY_MECH + "=" + secMech);
             }
         }
         
         getExternalizeUIValues(externalizeUIProperties, deviceKeyValue);
         
         String useDefaultKeyStore = (String)propertySheetModel.getValue(
             CHILD_NAME_KEYSTORE_USAGE);
         if (useDefaultKeyStore.equals("default")) {
             deviceKeyValue.add(ATTR_NAME_USE_DEFAULT_KEYSTORE + "=true");
         } else {
             deviceKeyValue.add(ATTR_NAME_USE_DEFAULT_KEYSTORE + "=false");
             getkeyStoreInfo(deviceKeyValue);
         }
 
         getExtendedFormsValues(deviceKeyValue);
 
         values.put(EntitiesModel.ATTR_NAME_DEVICE_KEY_VALUE, deviceKeyValue);
         return values;
     }
     
     
     static String formUserCredToken(String username, String password) {
         return ATTR_NAME_USERCREDENTIAL_NAME + username.trim() + "|" +
             ATTR_NAME_USERCREDENTIAL_PWD + password.trim();
     }
 
     static String[] splitUserCredToken(String token) {
         String username = null;
         String password = null;
         int idx = token.indexOf('|');
 
         if (idx > 0) {
             String part1 = token.substring(0, idx);
             String part2 = token.substring(idx+1);
             if (part1.startsWith(ATTR_NAME_USERCREDENTIAL_NAME)) {
                 username = part1.substring(
                     ATTR_NAME_USERCREDENTIAL_NAME.length());
             } else if (part1.startsWith(ATTR_NAME_USERCREDENTIAL_PWD)) {
                 password = part1.substring(
                     ATTR_NAME_USERCREDENTIAL_PWD.length());
             }
             if (part2.startsWith(ATTR_NAME_USERCREDENTIAL_NAME)) {
                 username = part2.substring(
                     ATTR_NAME_USERCREDENTIAL_NAME.length());
             } else if (part2.startsWith(ATTR_NAME_USERCREDENTIAL_PWD)) {
                 password = part2.substring(
                     ATTR_NAME_USERCREDENTIAL_PWD.length());
             }
         }
 
         if ((username != null) && (password != null)) {
             String[] temp = {username, password};
             return temp;
         }
         return null;
     }
     
     static void addToUserCredTokenAttr(
         String username, 
         String password,
         Set attrValues,
         AMModel model
     ) throws AMConsoleException {
         Map map = getUserCredentials(attrValues);
         if (map.keySet().contains(username)) {
             throw new AMConsoleException(model.getLocalizedString(
                 "web.services.profile.error-user-cred-exists"));
         }
         map.put(username, password);
         removeAttributeFromSet(attrValues, ATTR_NAME_USERCREDENTIAL);
         attrValues.add(formatUserCredential(map));
     }
 
     static void replaceUserCredTokenAttr(
         String username,
         String password,
         Set attrValues
     ) throws AMConsoleException {
         Map map = getUserCredentials(attrValues);
         map.put(username, password);
         removeAttributeFromSet(attrValues, ATTR_NAME_USERCREDENTIAL);
         attrValues.add(formatUserCredential(map));
     }
 
     static void removeUserCredTokenAttr(String username, Set attrValues) {
         Map map = getUserCredentials(attrValues);
         map.remove(username);
         removeAttributeFromSet(attrValues, ATTR_NAME_USERCREDENTIAL);
         attrValues.add(formatUserCredential(map));
     }
 
     static void removeUserCredTokenAttr(Set todelete, Set attrValues) {
         Map map = getUserCredentials(attrValues);
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             if (todelete.contains(key)) {
                 i.remove();
             }
         }
         removeAttributeFromSet(attrValues, ATTR_NAME_USERCREDENTIAL);
         attrValues.add(formatUserCredential(map));
     }
 
     static Map getUserCredentials(Set values) {
         Map mapNameToPassword = new HashMap();
         String userCredentials = getAttributeFromSet(values,
             ATTR_NAME_USERCREDENTIAL);
 
         if ((userCredentials != null) && (userCredentials.trim().length() > 0)){
             StringTokenizer st = new StringTokenizer(userCredentials, ",");
             while (st.hasMoreTokens()) {
                 String uc = st.nextToken();
                 String[] userpwd = splitUserCredToken(uc);
                 if (userpwd != null) {
                     mapNameToPassword.put(userpwd[0], userpwd[1]);
                 }
             }
         }
         return mapNameToPassword;
     }
 
     static String formatUserCredential(Map map) {
         StringBuffer buff = new StringBuffer();
         buff.append(ATTR_NAME_USERCREDENTIAL).append("=");
         boolean first = true;
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             String val = (String)map.get(key);
             if (first) {
                 first = false;
             } else {
                 buff.append(",");
             }
             buff.append(formUserCredToken(key, val));
         }
         return buff.toString();
     }
     
     private void getkeyStoreInfo(Set deviceKeyValue)
         throws AMConsoleException {
         String keyStoreLoc = (String)propertySheetModel.getValue(
             CHILD_NAME_KEY_STORE_LOCATION);
         String keyStorePwd = (String)propertySheetModel.getValue(
             CHILD_NAME_KEY_STORE_PASSWORD);
         String keyPwd = (String)propertySheetModel.getValue(
             CHILD_NAME_KEY_PASSWORD);
         String certAlias = (String)propertySheetModel.getValue(
             CHILD_NAME_CERT_ALIAS);
 
         if ((keyStoreLoc == null) || (keyStoreLoc.trim().length() == 0) ||
             (keyStorePwd == null) || (keyStorePwd.trim().length() == 0) ||
             (keyPwd == null) || (keyPwd.trim().length() == 0) ||
             (certAlias == null) || (certAlias.trim().length() == 0)
         ){
             throw new AMConsoleException(getModel().getLocalizedString(
                 "web.services.profile.missing-keystore-info"));
         }
         
         deviceKeyValue.add(ATTR_NAME_KEY_STORE_LOCATION + "=" + keyStoreLoc);
         deviceKeyValue.add(ATTR_NAME_KEY_STORE_PASSWORD + "=" + keyStorePwd);
         deviceKeyValue.add(ATTR_NAME_KEY_PASSWORD + "=" + keyPwd);
         deviceKeyValue.add(ATTR_NAME_CERT_ALIAS + "=" + certAlias);
     }
     
     private void getSecurityMech(Set deviceKeyValue) {
         HttpServletRequest req = getRequestContext().getRequest();
         StringBuffer buff = new StringBuffer();
         String prefix = pageName + "." + SECURITY_MECH_PREFIX;
         boolean hasValue = false;
         
         Map map = req.getParameterMap();
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             if (key.startsWith(prefix) && !key.endsWith(".jato_boolean")) {
                 key = key.substring(prefix.length());
                 if (hasValue) {
                     buff.append(",");
                 }
                 buff.append(key);
                 hasValue = true;
             }
         }
         
         if (hasValue) {
             deviceKeyValue.add(ATTR_NAME_SECURITY_MECH + "=" + buff.toString());
         }
     }
     
     static Set parseExternalizeUIProperties(String propName) {
         Set set = new HashSet();
         ResourceBundle bundle = ResourceBundle.getBundle(propName);
         for (Enumeration e = bundle.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             if (key.endsWith(".attributeName")) {
                 String childName = key.substring(0, key.length() - 14);
                 String attrName = bundle.getString(key);
                 String attrType = bundle.getString(
                     childName + ".attributeType");
                 set.add(new WebServiceUIElement(childName, attrName, attrType));
             }
         }
         return set;
     }
     
     protected abstract void getExtendedFormsValues(Set deviceKeyValue)
         throws AMConsoleException;
     protected abstract void setExtendedDefaultValues(Map attrValues)
         throws AMConsoleException;
     
     private static final String PROPERTY_CHECKBOX_TEMPLATE =
 "<property><cc name=\"{0}\" tagclass=\"com.sun.web.ui.taglib.html.CCCheckBoxTag\"><attribute name=\"label\" value=\"{1}\" /></cc></property>";
     private static final String PROPERTY_OPTION_TEMPLATE =
         "<option label=\"{0}\" value=\"{1}\" />";
 }
