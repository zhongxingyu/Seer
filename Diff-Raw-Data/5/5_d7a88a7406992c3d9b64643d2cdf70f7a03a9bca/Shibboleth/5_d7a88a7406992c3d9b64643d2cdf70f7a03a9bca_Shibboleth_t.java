 /*
  * The Fascinator - Shibboleth SSO Plugin
  * Copyright (C) 2012 James Cook University
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package au.edu.jcu.fascinator.portal.sso;
 
 import com.googlecode.fascinator.api.authentication.User;
 import com.googlecode.fascinator.common.JsonSimpleConfig;
 import com.googlecode.fascinator.portal.JsonSessionState;
 import com.googlecode.fascinator.portal.sso.SSOInterface;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Fascinator Shibboleth Integration
  *
  * @author Nigel Bajema
  */
 public class Shibboleth implements SSOInterface {
     private static Logger logger = LoggerFactory.getLogger(Shibboleth.class);
     private Template shibbolethTemplate;
 
     private static final String HOME = "shib-go-home";
 
     private String SHIB_SESSION_ID;
     private String SHIB_IDP;
     private String SHIB_COMMON_NAME;
     private String SHIB_USER_NAME;
     private String SHIB_ATTRIBUTE_DELIMITER;
     private List<String> SHIB_ATTRIBUTES = new ArrayList<String>();
 
     private static final String SHIBBOLETH_PLUGIN_ID = "Shibboleth";
 
     {
         try {
             logger.debug(String.format("Resource Loader Path: %s", Velocity.getProperty(Velocity.FILE_RESOURCE_LOADER_PATH).toString()));
             shibbolethTemplate = Velocity.getTemplate("shibboleth/interface.vm");
             JsonSimpleConfig d = new JsonSimpleConfig();
             String shib_path = SHIBBOLETH_PLUGIN_ID.toLowerCase();
 
             SHIB_ATTRIBUTE_DELIMITER = d.getString(";", shib_path, "delimiter");
 
             SHIB_SESSION_ID = d.getString("Shib_Session_ID", shib_path, "session_attribute");
             SHIB_ATTRIBUTES.add(SHIB_SESSION_ID);
             SHIB_IDP = d.getString("Shib_Identity_Provide", shib_path, "idp_attribute");
             SHIB_ATTRIBUTES.add(SHIB_IDP);
             SHIB_COMMON_NAME = d.getString("cn", shib_path, "cn_attribute");
             SHIB_ATTRIBUTES.add(SHIB_COMMON_NAME);
             SHIB_USER_NAME = d.getString("eppn", shib_path, "username_attribute");
             SHIB_ATTRIBUTES.add(SHIB_USER_NAME);
 
             List attrs = d.getArray(shib_path, "attributes");
             SHIB_ATTRIBUTES.addAll(attrs);
 
             logger.debug(String.format("Session ID Attribute: %s", SHIB_SESSION_ID));
             logger.debug(String.format("Shib Identity Provider Attribute: %s", SHIB_IDP));
             logger.debug(String.format("Shib Common Name Attribute: %s", SHIB_COMMON_NAME));
             logger.debug(String.format("Shib Username Attribute: %s", SHIB_USER_NAME));
             logger.debug(String.format("Shib Attributes: %s", attrs));
             logger.debug(String.format("Shib Attribute split: %s", SHIB_ATTRIBUTE_DELIMITER));
 
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
 
 
     }
 
     @Override
     public String getId() {
         return SHIBBOLETH_PLUGIN_ID;
     }
 
     @Override
     public String getLabel() {
         return "Shibboleth";
     }
 
     @Override
     public String getInterface(String ssoUrl) {
         logger.trace(String.format("ssoGetInterface: %s", ssoUrl));
         StringWriter sw = new StringWriter();
         VelocityContext vc = new VelocityContext();
         try {
            vc.put("shibboleth_url", ssoUrl.replace("default/sso","default/sso/shibboleth"));
//            vc.put("shibboleth_url", "https://oberon.jcu.edu.au/redbox/default/sso/shibboleth?ssoId=Shibboleth");
             shibbolethTemplate.merge(vc, sw);
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
         }
         return sw.toString();
     }
 
     @Override
     public List<String> getRolesList(JsonSessionState jsonSessionState) {
         logger.trace("getRolesList");
         return new ArrayList<String>();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public User getUserObject(JsonSessionState session) {
         logger.trace("getUserObject");
         List<String> un = (List<String>) session.get(SHIB_USER_NAME);
         List<String> rn = (List<String>) session.get(SHIB_COMMON_NAME);
         if (un != null) {
             if (rn == null) {
                 rn = un;
             }
             if (un.size() > 1) {
                 logger.warn(String.format("More than one username was retrieved: %s using the first: %s", un, un.get(0)));
             }
             if (rn.size() > 1 && rn != un) {
                 logger.warn(String.format("More than one real name was retrieved: %s using the first: %s", un, un.get(0)));
             }
 
             ShibbolethUser user = new ShibbolethUser(un.get(0), rn.get(0));
             Object tmp;
             for (String s : SHIB_ATTRIBUTES) {
                 tmp = session.get(s);
                 if (!s.equals(SHIB_USER_NAME) && !s.equals(SHIB_COMMON_NAME) && tmp instanceof List) {
                     user.set(s, join(SHIB_ATTRIBUTE_DELIMITER, (List<String>) tmp));
                 }
             }
             user.setSource(SHIBBOLETH_PLUGIN_ID);
             return user;
         }
         return null;
     }
 
     private String join(String delimiter, List<String> toJoin) {
         StringBuilder toRet = new StringBuilder();
         String del = "";
         for (String s : toJoin) {
             toRet.append(s).append(del);
             del = delimiter;
         }
         return toRet.toString();
     }
 
     @Override
     public void logout(JsonSessionState session) {
         logger.trace("Logout Requested");
         for (String shibKey : SHIB_ATTRIBUTES) {
             session.remove(shibKey);
             logger.trace(String.format("Removing Shibboleth Attribute: %s from user.", shibKey));
         }
     }
 
     @Override
     public void ssoInit(JsonSessionState session, HttpServletRequest request) throws Exception {
//        request.getSession().getServletContext().
         logger.trace(String.format("ssoInit, URL: %s", session.get("ssoPortalUrl")));
         Object tmp;
         for (String key : SHIB_ATTRIBUTES) {
             tmp = request.getAttribute(key);
             if (tmp != null) {
                 addAttr(key, tmp.toString(), session);
             }
             tmp = request.getHeader(key);
             if (tmp != null) {
                 addAttr(key, tmp.toString(), session);
             }
         }
 
 
     }
 
     @SuppressWarnings("unchecked")
     private void addAttr(String key, String value, JsonSessionState session) {
         Object o = session.get(key);
         List<String> l;
         if (o == null) {
             session.set(key, l = new ArrayList<String>());
         } else {
             l = (List<String>) o;
         }
         logger.trace(String.format("Adding: %s : %s", key, value));
         l.add(value);
     }
 
     @Override
     public void ssoCheckUserDetails(JsonSessionState jsonSessionState) {
         logger.trace("ssoCheckUserDetails");
     }
 
     @Override
     public String ssoGetRemoteLogonURL(JsonSessionState session) {
         logger.trace("ssoGetRemoteLogonURL");
         return (String) session.get(HOME);
     }
 
     @Override
     public void ssoPrepareLogin(JsonSessionState session, String returnAddress, String server) throws Exception {
         logger.trace(String.format("ssoPrepareLogin, Return Address: %s Server: %s", returnAddress, server));
         session.set(HOME, returnAddress);
     }
 }
 
 
