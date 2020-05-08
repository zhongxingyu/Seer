 /**
 * Copyright 2008 Unicon (R) Licensed under the
  * Educational Community License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS"
  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 package net.unicon.kaltura;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 
 import net.unicon.kaltura.service.KalturaService;
 
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.sling.SlingServlet;
 import org.apache.sling.api.SlingHttpServletRequest;
 import org.apache.sling.api.SlingHttpServletResponse;
 import org.apache.sling.api.servlets.SlingAllMethodsServlet;
 import org.apache.sling.commons.json.JSONException;
 import org.sakaiproject.nakamura.api.doc.ServiceDocumentation;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.user.UserConstants;
 import org.sakaiproject.nakamura.util.ExtendedJSONWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This is a servlet for interfacing with the kaltura service via REST (as needed)
  * Test it with: http://localhost:8080/kaltura
  * 
  * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
  */
 @ServiceDocumentation(
         name = "Kaltura Test Servlet",
         description = "Allows firing requests directly to the kaltura service for testing"
 )
 @SlingServlet(paths = { "/kaltura" }, generateComponent = true, generateService = true, methods = { "POST","GET" })
 public class KalturaServlet extends SlingAllMethodsServlet {
     private static final long serialVersionUID = 1L;
 
     private static final Logger LOG = LoggerFactory.getLogger(KalturaServlet.class);
 
     @Reference
     transient KalturaService kalturaService;
 
     @Override
     protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
     throws ServletException, IOException {
         // TODO
         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "NOT IMPLEMENTED YET");
     }
 
     @Override
     protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
     throws ServletException, IOException {
         response.setCharacterEncoding("UTF-8");
         response.setContentType("application/json");
 
         boolean full = false;
         if ( request.getParameter("full") != null) {
             full = true;
         }
         LOG.info("GET: full="+full);
 
         PrintWriter pw = response.getWriter();
         ExtendedJSONWriter jsonWriter = new ExtendedJSONWriter(pw);
         if (full) {
             User u = getUser(request, null);
             if (u == null) {
                 // Die since we cannot get the current user
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot get the current user!");
                 return;
             }
             LOG.info("Current user="+u.getId());
             Map<String, Object> m = new HashMap<String, Object>();
             m.put("id", u.getId());
             m.put("admin", u.isAdmin());
             try {
                 jsonWriter.valueMap(m);
             } catch (JSONException e) {
                 LOG.error(e.getMessage(), e);
             }
         } else {
             String userId = getCurrentUserId(request);
             if (userId == null || "".equals(userId)) {
                 // Die since we cannot get the current user
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot get the current user!");
                 return;
             }
             LOG.info("Current userId="+userId);
             //String requestedUserId = request.getParameter("uid");
             try {
                 jsonWriter.object();
                 jsonWriter.key("id");
                 jsonWriter.value(userId);
                 jsonWriter.endObject();
             } catch (JSONException e) {
                 LOG.error(e.getMessage(), e);
             }
         }
     }
 
 
     /**
      * Get the current userId for this request
      * @param request the current request
      * @return the userId of the current user
      */
     private String getCurrentUserId(SlingHttpServletRequest request) {
         return request.getRemoteUser();
     }
 
     /**
      * 
      * @param request the current request
      * @param userId the userId to get the user for
      * @return the user for this userId if one can be found OR null if none found or user is anonymous
      */
     private User getUser(SlingHttpServletRequest request, String userId) {
         User u = null;
         if (userId == null || "".equals(userId)) {
             userId = getCurrentUserId(request);
         }
         if (userId != null && !"".equals(userId)) {
             try {
                 javax.jcr.Session jcrSession = request.getResourceResolver().adaptTo(javax.jcr.Session.class);
                 Session session = StorageClientUtils.adaptToSession(jcrSession);
                 AuthorizableManager am = session.getAuthorizableManager();
                 Authorizable authorizable = am.findAuthorizable(userId);
                 if ( authorizable != null ) {
                     if (!UserConstants.ANON_USERID.equals(authorizable.getId())) {
                         // only include real users, no anonymous ones
                         u = (User) authorizable;
                     }
                 }
             } catch (Exception e) {
                 // No user for you!
                 e.printStackTrace();
             }
         }
         return u;
     }
 
 }
