 // Copyright (C) 2008 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.google.enterprise.connector.security.identity;
 
 import com.google.enterprise.connector.common.cookie.CookieSet;
 import com.google.enterprise.connector.common.cookie.CookieUtil;
 import com.google.enterprise.connector.spi.AbstractAuthnIdentity;
 
 import java.util.Collection;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpSession;
 
 /**
  * This mis-named class models the per-session info associated with verifying a particular
  * identity using a particular mechanism.  A better name might be "verification info", but
  * we have yet to settle on an appropriate name.  This object directly holds the
  * per-session data, while the configuration info, which is shared by all sessions, is
  * stored in a separate object.
  */
 public class DomainCredentials extends AbstractAuthnIdentity {
 
   private final AuthnDomain configInfo;
   private final CredentialsGroup cg;
   private final CookieSet cookies;
 
   DomainCredentials(AuthnDomain configInfo, CredentialsGroup cg) {
     super();
     this.configInfo = configInfo;
     this.cg = cg;
     cookies = new CookieSet();
     cg.addElement(this);
   }
 
   // Used for testing only:
   public static DomainCredentials dummy(HttpSession session) {
     return new DomainCredentials(null, CredentialsGroup.dummy(session));
   }
 
   /* @Override */
   public String getDomain() {
     return configInfo.getName();
   }
 
   /* @Override */
   public AuthnMechanism getMechanism() {
     return configInfo.getMechanism();
   }
 
   /* @Override */
   public String getSampleUrl() {
     return configInfo.getSampleUrl();
   }
 
   /* @Override */
   public String getUsername() {
     return cg.getUsername();
   }
 
   /* @Override */
   public void setUsername(String username) {
     cg.setUsername(username);
   }
 
   /* @Override */
   public String getPassword() {
     return cg.getPassword();
   }
 
   /* @Override */
   public HttpSession getSession() {
     return cg.getSession();
   }
 
   /* @Override */
   public Collection<Cookie> getCookies() {
     return cookies;
   }
 
   /**
    * Generate a string giving an outline of the contents.
    *
    * @return The outline string.
    */
   public String dumpInfo() {
     StringBuilder sb = new StringBuilder();
     sb.append(getUsername());
     if (getPassword() != null) {
       sb.append(":#");
       sb.append(getPassword().hashCode());
     }
     String cookieString = CookieUtil.cookieHeaderValue(getCookies(), false);
     if (cookieString != null) {
       sb.append("; ");
       sb.append(cookieString);
     }
     return sb.toString();
   }
 }
