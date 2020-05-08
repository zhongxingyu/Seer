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
 
 import com.jellymold.kiwi.Environment;
 import com.jellymold.kiwi.environment.EnvironmentService;
 import com.jellymold.utils.Pager;
 import gc.carbon.builder.resource.ResourceBuilder;
 import gc.carbon.builder.resource.ResourceBuilderFactory;
 import gc.carbon.data.Calculator;
 import gc.carbon.data.DataService;
 import gc.carbon.domain.data.DataCategory;
 import gc.carbon.domain.data.ItemValue;
 import gc.carbon.domain.path.PathItem;
 import gc.carbon.domain.profile.Profile;
 import gc.carbon.domain.profile.ProfileItem;
 import gc.carbon.domain.profile.StartEndDate;
 import gc.carbon.path.PathItemService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Component("profileItemResource")
 @Scope("prototype")
 public class ProfileItemResource extends BaseProfileResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @PersistenceContext
     private EntityManager entityManager;
 
     @Autowired
     private ProfileService profileService;
 
     @Autowired
     private PathItemService pathItemService;
 
     @Autowired
     private ProfileSheetService profileSheetService;
 
     @Autowired
     private Calculator calculator;
 
     @Autowired
     private DataService dataService;
 
     private Environment environment;
     private ProfileBrowser profileBrowser;
     private PathItem pathItem;
     private ResourceBuilder builder;
 
     public ProfileItemResource() {
         super();
     }
 
     public ProfileItemResource(Context context, Request request, Response response) {
         super(context, request, response);
     }
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         environment = EnvironmentService.getEnvironment();
         pathItem = getPathItem();
         profileBrowser = getProfileBrowser();
         profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
         profileBrowser.setProfileItemUid(request.getAttributes().get("itemUid").toString());
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() && (profileBrowser.getProfileItem() != null);
     }
 
     @Override
     public String getTemplatePath() {
         return ProfileConstants.VIEW_PROFILE_ITEM;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         ProfileItem profileItem = profileBrowser.getProfileItem();
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", profileBrowser);
         values.put("profile", profileItem.getProfile());
         values.put("profileItem", profileItem);
         values.put("node", profileItem);
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         return builder.getJSONObject();
     }
 
     @Override
     public Element getElement(Document document) {
         return builder.getElement(document);
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet");
         if (profileBrowser.getProfileItemActions().isAllowView()) {
             builder = ResourceBuilderFactory.createProfileItemBuilder(this);
             super.handleGet();
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public boolean allowPut() {
         return true;
     }
 
     private void setBuilderStrategy() {
         builder = ResourceBuilderFactory.createProfileItemBuilder(this);
     }
 
     @Override
     public void put(Representation entity) {
         log.debug("put");
         if (profileBrowser.getProfileItemActions().isAllowModify()) {
             setBuilderStrategy();
             Form form = getForm();
             ProfileItem profileItem = profileBrowser.getProfileItem();
             // ensure updated ProfileItem does not break rules for ProfileItems
             ProfileItem profileItemCopy = profileItem.getCopy();
            if (updateProfileItem(profileItemCopy, form) &&
                    !profileService.isEquivilentProfileItemExists(profileItemCopy)) {
                 // update persistent ProfileItem
                 updateProfileItem(profileItem, form);
                 // update ItemValues if supplied
                 Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
                 for (String name : form.getNames()) {
                     ItemValue itemValue = itemValues.get(name);
                     if (itemValue != null) {
                         itemValue.setValue(form.getFirstValue(name));
                         if (itemValue.hasUnits())
                             itemValue.setUnit(form.getFirstValue(name + "Unit"));
                         if (itemValue.hasPerUnits())
                             itemValue.setPerUnit(form.getFirstValue(name + "PerUnit"));
                     }
                 }
                 log.debug("ProfileItem updated");
                 // all done, need to recalculate and clear caches
                 calculator.calculate(profileItem);
                 pathItemService.removePathItemGroup(profileBrowser.getProfile());
                 profileSheetService.removeSheets(profileBrowser.getProfile());
                 // do response
                 if (isStandardWebBrowser()) {
                     success(profileBrowser.getFullPath());
                 } else {
                     // return a response for API calls
                     super.handleGet();
                 }
             } else {
                 log.warn("ProfileItem NOT updated");
                 badRequest();
             }
         } else {
             notAuthorized();
         }
     }
 
    protected boolean updateProfileItem(ProfileItem profileItem, Form form) {

         Set<String> names = form.getNames();
 
         if (!isValidRequest()) {
             badRequest();
         }
 
         // update 'name' value
         if (names.contains("name")) {
             profileItem.setName(form.getFirstValue("name"));
         }
 
         // update 'startDate' value
         if (names.contains("startDate")) {
             profileItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
         }
 
         // update 'end' value
         if (names.contains("end")) {
             profileItem.setEnd(Boolean.valueOf(form.getFirstValue("end")));
         }
 
         // update 'endDate' value
         if (names.contains("endDate")) {
            if (StringUtils.isNotBlank(form.getFirstValue("endDate"))) {
                profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
            } else {
                profileItem.setEndDate(null);
            }
         }
 
        // update 'duration' value
         if (form.getNames().contains("duration")) {
             StartEndDate endDate = ((StartEndDate) profileItem.getStartDate()).plus(form.getFirstValue("duration"));
             profileItem.setEndDate(endDate);
         }
 
        // ensure endDate is not before startDate
         if (profileItem.getEndDate() != null &&
                 profileItem.getEndDate().before(profileItem.getStartDate())) {
            return false;
        } else {
            return true;
         }
     }
 
     @Override
     public boolean allowDelete() {
         return true;
     }
 
     @Override
     public void delete() {
         log.debug("delete");
         if (profileBrowser.getProfileItemActions().isAllowDelete()) {
             ProfileItem profileItem = profileBrowser.getProfileItem();
             profileService.remove(profileItem);
             pathItemService.removePathItemGroup(profileBrowser.getProfile());
             profileSheetService.removeSheets(profileBrowser.getProfile());
             success(pathItem.getParent().getFullPath());
         } else {
             notAuthorized();
         }
     }
 
     public List<ProfileItem> getProfileItems() {
         return null;
     }
 
     public ProfileSheetService getProfileSheetService() {
         return profileSheetService;
     }
 
     public Pager getPager() {
         return getPager(getItemsPerPage());
     }
 
     public ProfileService getProfileService() {
         return profileService;
     }
 
     public DataService getDataService() {
         return dataService;
     }
 
     public Environment getEnvironment() {
         return environment;
     }
 
     public Calculator getCalculator() {
         return calculator;
     }
 
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     public ProfileItem getProfileItem() {
         return profileBrowser.getProfileItem();
     }
 
     public Date getProfileDate() {
         return profileBrowser.getProfileDate();
     }
 
     public Date getStartDate() {
         return profileBrowser.getStartDate();
     }
 
     public Date getEndDate() {
         return profileBrowser.getEndDate();
     }
 
     public Profile getProfile() {
         return profileBrowser.getProfile();
     }
 
     public DataCategory getDataCategory() {
         return profileBrowser.getDataCategory();
     }
 }
