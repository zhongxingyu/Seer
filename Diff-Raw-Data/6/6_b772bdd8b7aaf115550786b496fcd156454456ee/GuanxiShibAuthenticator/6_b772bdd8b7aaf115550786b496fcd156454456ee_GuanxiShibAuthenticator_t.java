 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.xwiki.authentication.guanxi;
 
 import java.security.Principal;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 //import org.apache.ecs.xhtml.map;
 import org.securityfilter.realm.SimplePrincipal;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.objects.classes.BaseClass;
 import com.xpn.xwiki.user.api.XWikiUser;
 import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
 import com.xpn.xwiki.web.XWikiRequest;
 
 public class GuanxiShibAuthenticator extends XWikiAuthServiceImpl {
 
     /**
      * Logging tool
      */
     private static final Log log = LogFactory.getLog(GuanxiShibAuthenticator.class);
 
     /** 
      * Establish our config vars
      */
     private static GuanxiShibConfig gxconfig;
 
     static {
         gxconfig = GuanxiShibConfigurator.getGuanxiShibConfig( );
     }
 
     /**
      * Authenticate user. 
      *
      * @param String,String,XWikiContext
      * @return Principal
      */ 
     @Override
     public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException {
         log.debug("GuanxiShibAuthenticator.authenticate");
         //only work with SSO, does not take into account login/password
         XWikiUser user = checkAuth(context);
         return user == null ? null : new SimplePrincipal(checkAuth(context).getUser());
     }
 
     /**
      * Authenticates the user
      * @param context
      * @return authenticated XWiki user object
      * @throws com.xpn.xwiki.XWikiException
      */
     @Override
     public XWikiUser checkAuth(XWikiContext context) throws XWikiException {
 
         log.debug("GuanxiShibAuthenticator.checkAuth");
 
         // Set default to Guest
         String xwikifullname = "XWiki.XWikiGuest";
         log.debug("GuanxiShibAuthenticator: set guest user");
 
         String eid = context.getRequest().getRemoteUser();
 
         if ((eid == null) || eid.equals("")) {
             log.debug("GuanxiShibAuthenticator: (REMOTE_USER) is null/not present");
             log.debug("GuanxiShibAuthenticator: using header search instead");
             
             eid = context.getRequest().getHttpServletRequest().
                   getHeader(gxconfig.getHeaderUserid());
 
             if ((eid == null) || eid.equals("")) { 
                 log.debug("GuanxiShibAuthenticator: noting found in " + 
                     gxconfig.getHeaderUserid() + " not logged in");
             } else { 
                 String safeEID = getSafeUserid(eid);
             	this.createUser(safeEID, context);
 	        xwikifullname = getXwikiFullName(safeEID);
             } 
         } else {
             String safeEID = getSafeUserid(eid);
             this.createUser(safeEID, context);
 	    xwikifullname = getXwikiFullName(safeEID);
         }
 
         context.setUser(xwikifullname);
         log.info("GuanxiShibAuthenticator: authentication successful for user " 
            + xwikifullname);
         return new XWikiUser(xwikifullname);
     }
 
     /**
      * Creates the user if he doesnt exist in the XWiki repository. 
      * User is assigned to the default XWikiAllGroup
      *
      * @param String  
      * @param context
      * @throws com.xpn.xwiki.XWikiException
      */
     @Override
     protected void createUser(String user, XWikiContext context) throws XWikiException {
         String xwikiUser = super.findUser(user, context);
         if (xwikiUser == null) {
            String xwikifullname = getXwikiFullName(user);
            log.debug("GuanxiShibAuthenticator: User " + xwikifullname + " does not exist");
             String wikiname = context.getWiki().clearName(user, true, true, context);
             context.getWiki().createEmptyUser(wikiname, "edit", context);
            log.debug("GuanxiShibAuthenticator: User " + xwikifullname + " has been created");
         } else {
             log.debug("GuanxiShibAuthenticator: User " + xwikiUser + " exists continuing");
         }
     }
 
     /**
      *   Creates the user (Prinicpal) from header attributes delivered by shibboleth
      *
      */
     private Principal createUserFromAttributes(XWikiContext context) throws XWikiException {
 
         try {
             if (log.isDebugEnabled()) {
               log.debug("attempting to create a user for you");
             }
             BaseClass baseclass = context.getWiki().getUserClass(context);
             XWikiRequest req = context.getRequest();
 
             String eid = getSafeUserid(req.getRemoteUser());
             if (eid == null) {
                 eid = getSafeUserid(context.getRequest().getHttpServletRequest().
                   getHeader(gxconfig.getHeaderUserid()));
                 if (log.isDebugEnabled()) {
                     log.debug("getting EID value from header value");
                 }
             }
        
             String email = context.getRequest().getHttpServletRequest().getHeader(
                gxconfig.getHeaderMail());
 
             String displayname = context.getRequest().getHttpServletRequest().
                getHeader(gxconfig.getHeaderFullname());
 
             String fullwikiname = gxconfig.getDefaultUserSpace() + "." + eid;
 
             XWikiDocument doc = context.getWiki().getDocument(fullwikiname, context);
             //if (!doc.isNew()) {
             //return getUserPrincipal(fullwikiname, context);
             //}
 
             Map map = new HashMap();
             map.put("active", "1");
             BaseObject newobject = (BaseObject)baseclass.fromMap(map, context);
             newobject.setName(fullwikiname);
             doc.addObject(baseclass.getName(), newobject);
             doc.setParent("");
             doc.setContent("#includeForm(\"XWiki.XWikiUserTemplate\")");
 
             context.getWiki().ProtectUserPage(context, fullwikiname, "edit", doc);
 
             context.getWiki().saveDocument(doc, null, context);
 
             context.getWiki().SetUserDefaultGroup(context, fullwikiname);
 
             return getUserPrincipal(fullwikiname, context);
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Update user info based on incoming header values.
      *
      * @param context XWiki context
      * @throws XWikiException error 
      */
     public void updateUserInfo() throws XWikiException {
         return;
     }
 
     /**
      *  Get a safe string to use as the suffix portion of xwikifullname 
      *
      *  @param String string to check
      *  @param String returns a valid string
      */
     private String getSafeUserid(String userid) {
         userid.toLowerCase();
         String u = StringUtils.makeValid(userid,
            gxconfig.getReplacementChar());
 
         if (log.isDebugEnabled()) {
             log.debug("GuanxiAuthenticator: created XWiki safe userid " + u + 
               " from " + userid);
         }
         return u;
     }
 
     /**
      * See if user exists in the system already.
      * 
      * @param String eid Enterprise id of user
      * @param XWikiContext context The context
      * @return Boolean t,f
      */
     private Boolean userExists(String eid, XWikiContext context) {
         Boolean b = new Boolean(true);
         String fullwikiname = gxconfig.getDefaultUserSpace() + "." + eid;
         Principal p = getUserPrincipal(fullwikiname,context);
 
         if (p == null) {
             b = false;
         }
 
         if (log.isDebugEnabled()) {
             log.debug("looking for user " + fullwikiname + "[" + eid + 
               "] = " + b); 
         }
         return b;
     }
 
     /**
      * 
      * 
      */
     private Principal getUserPrincipal(String susername, XWikiContext context) {
         Principal principal = null;
 
         // First we check in the local database
         try {
             String user = findUser(susername, context);
             if (user!=null) {
                 principal = new SimplePrincipal(user);
             }
         } catch (Exception e) {}
 
         if (context.isVirtual()) {
          if (principal==null) {
          // Then we check in the main database
             String db = context.getDatabase();
             try {
                 context.setDatabase(context.getWiki().getDatabase());
                 try {
                     String user = findUser(susername, context);
                     if (user!=null)
                         principal = new SimplePrincipal(context.getDatabase() + ":" + user);
                 } catch (Exception e) {}
             } finally {
                 context.setDatabase(db);
             }
          }
         }
         return principal;
      }
 
     /**
      * Get the full String representation of the Xwiki username in the form of SPACE.USER
      * 
      * @param String incomming enterprize id of user
      * @return String XWiki username 
      */
      private String getXwikiFullName(String user) {
          return (String) gxconfig.getDefaultUserSpace() + "." + user;
      }
 }
