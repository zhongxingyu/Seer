 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package gc.carbon.profile;
 
 import com.jellymold.kiwi.Group;
 import com.jellymold.kiwi.Permission;
 import com.jellymold.kiwi.User;
 import com.jellymold.kiwi.auth.AuthService;
 import com.jellymold.utils.Pager;
 import gc.carbon.AMEEResource;
 import gc.carbon.domain.profile.Profile;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.Method;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 @Component("profilesResource")
 @Scope("prototype")
 public class ProfilesResource extends AMEEResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private ProfileService profileService;
 
     private ProfileBrowser profileBrowser;
     private User user;
     private Group group;
     private Profile newProfile = null;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         user = AuthService.getUser();
         group = AuthService.getGroup();
         profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
         setPage(request);
     }
 
     @Override
     public String getTemplatePath() {
         return ProfileConstants.VIEW_PROFILES;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(getItemsPerPage());
         List<Profile> profiles = profileService.getProfiles(pager);
         pager.setCurrentPage(getPage());
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", profileBrowser);
         values.put("profiles", profiles);
         values.put("pager", pager);
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         if (isGet()) {
            Pager pager = getPager(getItemsPerPage());
             List<Profile> profiles = profileService.getProfiles(pager);
             pager.setCurrentPage(getPage());
             JSONArray profilesJSONArray = new JSONArray();
             for (Profile profile : profiles) {
                 profilesJSONArray.put(profile.getJSONObject());
             }
             obj.put("profiles", profilesJSONArray);
             obj.put("pager", pager.getJSONObject());
         } else if (getRequest().getMethod().equals(Method.POST)) {
             obj.put("profile", newProfile.getJSONObject());
         }
         return obj;
     }
 
     @Override
     public Element getElement(Document document, boolean detailed) {
         Element element = document.createElement("ProfilesResource");
         if (isGet()) {
            Pager pager = getPager(getItemsPerPage());
             List<Profile> profiles = profileService.getProfiles(pager);
             pager.setCurrentPage(getPage());
             Element profilesElement = document.createElement("Profiles");
             for (Profile profile : profiles) {
                 profilesElement.appendChild(profile.getElement(document));
             }
             element.appendChild(profilesElement);
             element.appendChild(pager.getElement(document));
         } else if (getRequest().getMethod().equals(Method.POST)) {
             element.appendChild(newProfile.getElement(document));
         }
         return element;
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet()");
         if (profileBrowser.getProfileActions().isAllowList()) {
             super.handleGet();
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public boolean allowPost() {
         return true;
     }
 
     @Override
     public void acceptRepresentation(Representation entity) {
         log.debug("acceptRepresentation()");
         if (profileBrowser.getProfileActions().isAllowCreate()) {
             Form form = getForm();
             // are we creating a new Profile?
             if ((form.getFirstValue("profile") != null) && (group != null)) {
                 // create Permission for Profile
                 Permission permission = new Permission(group, user);
                 // create new Profile
                 newProfile = new Profile(environment, permission);
                 profileService.persist(newProfile);
             }
             if (newProfile != null) {
                 if (isStandardWebBrowser()) {
                     success();
                 } else {
                     // return a response for API calls
                     super.handleGet();
                 }
             } else {
                 badRequest();
             }
         } else {
             notAuthorized();
         }
     }
 }
