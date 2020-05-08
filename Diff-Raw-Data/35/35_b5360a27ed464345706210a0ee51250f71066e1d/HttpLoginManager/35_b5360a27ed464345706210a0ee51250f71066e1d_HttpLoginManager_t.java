 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: HttpLoginManager.java,v 1.4 2003-08-14 19:15:43 shahid.shah Exp $
  */
 
 package com.netspective.sparx.security;
 
 import java.util.BitSet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.sparx.value.HttpServletValueContext;
 import com.netspective.sparx.report.tabular.HtmlTabularReportDataSourceScrollStates;
 import com.netspective.sparx.form.listener.DialogValidateListener;
 import com.netspective.sparx.form.DialogValidationContext;
 import com.netspective.sparx.security.authenticator.SingleUserServletLoginAuthenticator;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.commons.security.AuthenticatedUsers;
 import com.netspective.commons.security.BasicAuthenticatedUser;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 
 public class HttpLoginManager
 {
     protected static final String DEFAULT_AUTHENTICATED_USER_SESS_ATTR_NAME = "authenticated-user";
     protected static final String DEFAULT_REMEMBER_USER_ID_COOKIE_NAME = "sparx-user-id-00";
     protected static final String DEFAULT_REMEMBER_PASSWORD_COOKIE_NAME = "sparx-password-00";
     protected static final ValueSource DEFAULT_INVALID_USER_MESSAGE = new StaticValueSource("Invalid user id or password.");
 
     private static final Log log = LogFactory.getLog(HttpLoginManager.class);
     private static final String MONITOR_ENTRY_FIELD_SEPARATOR = ",";
 
     protected class LoginDialogValidator implements DialogValidateListener
     {
         public void validateDialog(DialogValidationContext dvc)
         {
             LoginDialogContext ldc = (LoginDialogContext) dvc.getDialogContext();
             LoginDialog loginDialog = (LoginDialog) ldc.getDialog();
 
             if(loginAuthenticator == null)
             {
                 dvc.addError("No login authenticator provided.");
                 return;
             }
 
             if(! loginAuthenticator.isUserValid(loginDialog, ldc))
                 dvc.addError(invalidUserMessage.getTextValue(ldc));
         }
     }
 
     private boolean isDefault;
     private String name;
     private String authenticatedUserSessionAttrName = DEFAULT_AUTHENTICATED_USER_SESS_ATTR_NAME;
     private boolean allowRememberUserId;
     private String rememberUserIdCookieName = DEFAULT_REMEMBER_USER_ID_COOKIE_NAME;
     private String rememberPasswordCookieName = DEFAULT_REMEMBER_PASSWORD_COOKIE_NAME;
    private ValueSource rememberPasswordCookiePath = null;
     private Class authenticatedUserClass = BasicAuthenticatedUser.class;
     private AuthenticatedUsers activeUsers = new AuthenticatedUsers();
     private int rememberUserIdCookieMaxAge = 60 * 60 * 24 * 365; // 1 year
     private LoginDialog loginDialog;
     private LoginAuthenticator loginAuthenticator;
     private ValueSource invalidUserMessage = DEFAULT_INVALID_USER_MESSAGE;
 
     public HttpLoginManager()
     {
     }
 
     public Class getAuthenticatedUserClass()
     {
         return authenticatedUserClass;
     }
 
     public void setAuthenticatedUserClass(Class authenticatedUserClass)
     {
         this.authenticatedUserClass = authenticatedUserClass;
     }
 
     public AuthenticatedUsers getActiveUsers()
     {
         return activeUsers;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public boolean isDefault()
     {
         return isDefault;
     }
 
     public void setDefault(boolean aDefault)
     {
         isDefault = aDefault;
     }
 
     public String getAuthenticatedUserSessionAttrName()
     {
         return authenticatedUserSessionAttrName;
     }
 
     public void setAuthenticatedUserSessionAttrName(String authenticatedUserSessionAttrName)
     {
         this.authenticatedUserSessionAttrName = authenticatedUserSessionAttrName;
     }
 
     public String getRememberUserIdCookieName()
     {
         return rememberUserIdCookieName;
     }
 
     public void setRememberUserIdCookieName(String rememberUserIdCookieName)
     {
         this.rememberUserIdCookieName = rememberUserIdCookieName;
     }
 
     public String getRememberPasswordCookieName()
     {
         return rememberPasswordCookieName;
     }
 
     public void setRememberPasswordCookieName(String rememberPasswordCookieName)
     {
         this.rememberPasswordCookieName = rememberPasswordCookieName;
     }
 
     public boolean isAllowRememberUserId()
     {
         return allowRememberUserId;
     }
 
     public void setAllowRememberUserId(boolean allowRememberUserId)
     {
         this.allowRememberUserId = allowRememberUserId;
     }
 
     public int getRememberUserIdCookieMaxAge()
     {
         return rememberUserIdCookieMaxAge;
     }
 
     public void setRememberUserIdCookieMaxAge(int rememberUserIdCookieMaxAge)
     {
         this.rememberUserIdCookieMaxAge = rememberUserIdCookieMaxAge;
     }
 
    public ValueSource getRememberPasswordCookiePath()
    {
        return rememberPasswordCookiePath;
    }

    public String getRememberPasswordCookiePath(HttpServletValueContext vc)
    {
        if(rememberPasswordCookiePath == null)
            return vc.getHttpServlet().getServletContext().getServletContextName();
        else
            return rememberPasswordCookiePath.getTextValue(vc);
    }

    public void setRememberPasswordCookiePath(ValueSource rememberPasswordCookiePath)
    {
        this.rememberPasswordCookiePath = rememberPasswordCookiePath;
    }

     public ValueSource getInvalidUserMessage()
     {
         return invalidUserMessage;
     }
 
     public void setInvalidUserMessage(ValueSource invalidUserMessage)
     {
         this.invalidUserMessage = invalidUserMessage;
     }
 
     /**
      * Create the authenticated user object associated with the given userId.
      * @param ldc If a 'remembered' (by cookie) id is being used, this will be null otherwise it will be the
      *            LoginDialogContext that was constructed by the LoginDialog.
      * @param userId The userId that was either remembered or entered by a user.
      * @return
      */
     public AuthenticatedUser createAuthenticatedUser(LoginDialogContext ldc, String userId, String encryptedPassword, boolean isRemembered)
     {
         try
         {
             AuthenticatedUser authUser = (AuthenticatedUser) getAuthenticatedUserClass().newInstance();
             authUser.setUserId(userId);
             authUser.setEncryptedPassword(encryptedPassword);
             authUser.setRemembered(isRemembered);
             applyAccessControls(ldc, authUser);
             return authUser;
         }
         catch (Exception e)
         {
             log.error("Error creating authenticated user", e);
             throw new RuntimeException("Error creating authenticated user: " + e.getMessage());
         }
     }
 
     /**
      * Assign the authenticated user's access control roles and permissions.
      * @param ldc If a 'remembered' (by cookie) id is being used, this will be null otherwise it will be the
      *            LoginDialogContext that was constructed by the LoginDialog.
      * @param user The authenticated user.
      */
     public void applyAccessControls(LoginDialogContext ldc, AuthenticatedUser user)
     {
     }
 
     public AuthenticatedUser getAuthenticatedUser(HttpServletRequest request)
     {
         return (AuthenticatedUser) request.getSession(true).getAttribute(getAuthenticatedUserSessionAttrName());
     }
 
     public AuthenticatedUser getAuthenticatedUser(HttpServletValueContext vc)
     {
         return getAuthenticatedUser(vc.getHttpRequest());
     }
 
     public boolean accessAllowed(HttpServletValueContext vc)
     {
         return getAuthenticatedUser(vc) != null;
     }
 
     public String getRememberedUserId(HttpServletValueContext vc)
     {
         Cookie[] cookies = vc.getHttpRequest().getCookies();
         if(cookies != null)
         {
             for(int i =0; i < cookies.length; i++)
             {
                 Cookie cookie = cookies[i];
                 if(cookie.getName().equals(getRememberUserIdCookieName()))
                     return cookie.getValue();
             }
         }
         return null;
     }
 
     public String getRememberedEncryptedPassword(HttpServletValueContext vc)
     {
         Cookie[] cookies = vc.getHttpRequest().getCookies();
         if(cookies != null)
         {
             for(int i =0; i < cookies.length; i++)
             {
                 Cookie cookie = cookies[i];
                 if(cookie.getName().equals(getRememberPasswordCookieName()))
                     return cookie.getValue();
             }
         }
         return null;
     }
 
     public void login(HttpServletValueContext vc, AuthenticatedUser user, boolean rememberUserId)
     {
         vc.getHttpRequest().getSession(true).setAttribute(getAuthenticatedUserSessionAttrName(), user);
 
         if(isAllowRememberUserId() && rememberUserId)
         {
             Cookie cookie = new Cookie(getRememberUserIdCookieName(), user.getUserId());
            cookie.setPath(getRememberPasswordCookiePath(vc));
             cookie.setMaxAge(getRememberUserIdCookieMaxAge());
             vc.getHttpResponse().addCookie(cookie);
             cookie = new Cookie(getRememberPasswordCookieName(), user.getEncryptedPassword());
             cookie.setMaxAge(getRememberUserIdCookieMaxAge());
            cookie.setPath(getRememberPasswordCookiePath(vc));
             vc.getHttpResponse().addCookie(cookie);
         }
 
         registerLogin(vc, user);
     }
 
     public void logout(HttpServletValueContext vc)
     {
         HtmlTabularReportDataSourceScrollStates.getInstance().removeActiveState(vc);
 
         if(isAllowRememberUserId())
         {
             Cookie cookie = new Cookie(getRememberUserIdCookieName(), "");
            cookie.setPath(getRememberPasswordCookiePath(vc));
             cookie.setMaxAge(-1);
             vc.getHttpResponse().addCookie(cookie);
             cookie = new Cookie(getRememberPasswordCookieName(), "");
            cookie.setPath(getRememberPasswordCookiePath(vc));
             cookie.setMaxAge(-1);
             vc.getHttpResponse().addCookie(cookie);
         }
 
         HttpServletRequest req = vc.getHttpRequest();
         AuthenticatedUser user = getAuthenticatedUser(req);
         if(user != null)
         {
             req.getSession().removeAttribute(getAuthenticatedUserSessionAttrName());
             registerLogout(vc, user);
         }
     }
 
     protected void registerLogin(HttpServletValueContext hsvc, AuthenticatedUser user)
     {
         activeUsers.add(user);
 
         HttpServletRequest req = hsvc.getHttpRequest();
         if(log.isInfoEnabled())
         {
 
             String userId = user.getUserId();
             StringBuffer info = new StringBuffer();
             info.append("login");
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(userId);
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(req.getRemoteUser());
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(req.getRemoteHost());
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(req.getRemoteAddr());
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             BitSet perms = user.getUserPermissions();
             info.append(perms != null ? user.getUserPermissions().toString() : "{}");
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             String[] roles = user.getUserRoleNames();
             if(roles != null)
             {
                 for(int r = 0; r < roles.length; r++)
                 {
                     if(r > 0)
                         info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
                     info.append(roles[r]);
                 }
             }
             log.info(info);
         }
 
         if(log.isDebugEnabled())
         {
             String userId = user.getUserId();
             log.debug("User '" + userId + "' (" + user.getUserName() + ") is now authenticated for Session ID '" + req.getSession(true).getId() + "'");
 
             BitSet perms = user.getUserPermissions();
             if(perms != null)
                 log.debug("User '" + userId + "' has permissions " + user.getUserPermissions().toString());
             else
                 log.debug("User '" + userId + " has no permissions.");
 
             String[] roles = user.getUserRoleNames();
             if(roles != null)
             {
                 for(int r = 0; r < roles.length; r++)
                     log.debug("User '" + userId + "' has role " + roles[r]);
             }
             else
                 log.debug("User '" + userId + " has no roles.");
         }
     }
 
     protected void registerLogout(HttpServletValueContext hsvc, AuthenticatedUser user)
     {
         activeUsers.remove(user);
 
         if(user != null && log.isInfoEnabled())
         {
             HttpServletRequest req = hsvc.getHttpRequest();
             String userId = user.getUserId();
             StringBuffer info = new StringBuffer();
             info.append("logout");
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(userId);
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(req.getRemoteUser());
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(req.getRemoteHost());
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             info.append(req.getRemoteAddr());
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             BitSet perms = user.getUserPermissions();
             info.append(perms != null ? user.getUserPermissions().toString() : "{}");
             info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
             String[] roles = user.getUserRoleNames();
             if(roles != null)
             {
                 for(int r = 0; r < roles.length; r++)
                 {
                     if(r > 0)
                         info.append(MONITOR_ENTRY_FIELD_SEPARATOR);
                     info.append(roles[r]);
                 }
             }
             log.info(info);
         }
     }
 
     public String toString()
     {
         return this.getClass().getName() + " Id " + getName() + ", AuthUserSessId " + getAuthenticatedUserSessionAttrName() +
                 ", UserCookieName " + getRememberUserIdCookieName() + ", PasswordCookieName " + getRememberPasswordCookieName();
     }
 
     public LoginAuthenticator createLoginAuthenticator()
     {
         return new SingleUserServletLoginAuthenticator();
     }
 
     public void addLoginAuthenticator(LoginAuthenticator loginAuthenticator)
     {
         this.loginAuthenticator = loginAuthenticator;
     }
 
     public LoginDialog createLoginDialog()
     {
         return new LoginDialog();
     }
 
     public void addLoginDialog(LoginDialog loginDialog)
     {
         this.loginDialog = loginDialog;
         this.loginDialog.setLoginManager(this);
         this.loginDialog.addListener(new LoginDialogValidator());
     }
 
     public LoginDialog getLoginDialog()
     {
         return loginDialog;
     }
 }
