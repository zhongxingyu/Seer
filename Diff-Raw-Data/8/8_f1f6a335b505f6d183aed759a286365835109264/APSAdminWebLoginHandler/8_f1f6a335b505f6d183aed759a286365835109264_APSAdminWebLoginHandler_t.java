 /* 
  * 
  * PROJECT
  *     Name
  *         APS Web Tools
  *     
  *     Code Version
  *         0.9.0
  *     
  *     Description
  *         This provides some utility classes for web applications.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2013-02-03: Created!
  *         
  */
 package se.natusoft.osgi.aps.tools.web;
 
 import org.osgi.framework.BundleContext;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * This is a login handler to use by any admin web registering with the APSAdminWeb to validate
  * that there is a valid login available.
  */
 public class APSAdminWebLoginHandler extends APSLoginHandler implements APSLoginHandler.HandlerInfo {
     //
     // Private Members
     //
 
     /** The current APSSession id. */
     private String sessionId = null;
 
     //
     // Constructors
     //
 
     /**
      * Creates a new APSAdminWebLoginHandler.
      *
      * @param context The bundle context.
      */
     public APSAdminWebLoginHandler(BundleContext context) {
         super(context, null);
         setHandlerInfo(this);
     }
 
     //
     // Methods
     //
 
     /**
      * Sets the session id from a cookie in the specified request.
      *
      * @param request The request to get the session id cookie from.
      */
     public void setSessionIdFromRequestCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String sessId = CookieTool.getCookie(request.getCookies(), "aps-adminweb-session-id");
            if (sessId != null) {
                this.sessionId = sessId;
            }
         }
     }
 
     /**
      * Saves the current session id on the specified response.
      *
      * @param response The response to save the session id cookie on.
      */
     public void saveSessionIdOnResponse(HttpServletResponse response) {
         if (this.sessionId != null) {
 
             CookieTool.setCookie(response, "aps-adminweb-session-id", this.sessionId, 3600 * 24, "/");
         }
     }
 
     /**
      * @return An id to an APSSessionService session.
      */
     @Override
     public String getSessionId() {
         return this.sessionId;
     }
 
     /**
      * Sets a new session id.
      *
      * @param sessionId The session id to set.
      */
     @Override
     public void setSessionId(String sessionId) {
         this.sessionId = sessionId;
     }
 
     /**
      * @return The name of the session data containing the logged in user if any.
      */
     @Override
     public String getUserSessionName() {
         return "aps-admin-user";
     }
 
     /**
      * @return The required role of the user for it to be considered logged in.
      */
     @Override
     public String getRequiredRole() {
         return "apsadmin";
     }
 }
