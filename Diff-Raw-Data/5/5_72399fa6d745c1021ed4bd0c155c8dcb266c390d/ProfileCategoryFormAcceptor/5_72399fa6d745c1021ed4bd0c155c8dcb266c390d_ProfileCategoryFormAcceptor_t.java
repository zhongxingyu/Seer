 package gc.carbon.profile.acceptor;
 
 import com.jellymold.utils.APIFault;
 import gc.carbon.data.DataService;
 import gc.carbon.domain.data.DataCategory;
 import gc.carbon.domain.data.DataItem;
 import gc.carbon.domain.data.ItemValue;
 import gc.carbon.domain.profile.ProfileItem;
 import gc.carbon.domain.profile.StartEndDate;
 import gc.carbon.domain.profile.ValidFromDate;
 import gc.carbon.profile.ProfileCategoryResource;
 import gc.carbon.profile.ProfileService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.restlet.data.Form;
 import org.restlet.data.Method;
 import org.restlet.resource.Representation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class ProfileCategoryFormAcceptor implements Acceptor {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private ProfileCategoryResource resource;
     private ProfileService profileService;
     private DataService dataService;
 
     public ProfileCategoryFormAcceptor(ProfileCategoryResource resource) {
         this.resource = resource;
         this.profileService = resource.getProfileService();
         this.dataService = resource.getDataService();
     }
 
     public List<ProfileItem> accept(Representation entity) {
         return accept(resource.getForm());
     }
 
     public List<ProfileItem> accept(Form form) {
 
         List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
         DataCategory dataCategory;
         DataItem dataItem;
         ProfileItem profileItem = null;
         String uid;
         dataCategory = resource.getDataCategory();
         if (resource.getRequest().getMethod().equals(Method.POST)) {
             // new ProfileItem
             uid = form.getFirstValue("dataItemUid");
             if (uid != null) {
                 // the root DataCategory has an empty path
                 if (dataCategory.getPath().length() == 0) {
                     // allow any DataItem for any DataCategory
                     dataItem = dataService.getDataItem(resource.getEnvironment(), uid);
                 } else {
                     // only allow DataItems for specific DataCategory (not root)
                     dataItem = dataService.getDataItem(dataCategory, uid);
                 }
                 if (dataItem != null) {
                     // create new ProfileItem
                     profileItem = new ProfileItem(resource.getProfile(), dataItem);
                     profileItem = acceptProfileItem(form, profileItem);
                 } else {
                     log.warn("accept() - Data Item not found");
                     resource.notFound();
                     profileItem = null;
                 }
             } else {
                 log.warn("accept() - dataItemUid not supplied");
                 resource.badRequest(APIFault.MISSING_PARAMETERS);
             }
         } else if (resource.getRequest().getMethod().equals(Method.PUT)) {
             // update ProfileItem
             uid = form.getFirstValue("profileItemUid");
             if (uid != null) {
                 // find existing Profile Item
                 // the root DataCategory has an empty path
                 if (dataCategory.getPath().length() == 0) {
                     // allow any ProfileItem for any DataCategory
                     profileItem = profileService.getProfileItem(resource.getProfileBrowser().getProfile().getUid(), uid);
                 } else {
                     // only allow ProfileItems for specific DataCategory (not root)
                     profileItem = profileService.getProfileItem(resource.getProfileBrowser().getProfile().getUid(), dataCategory.getUid(), uid);
                 }
                 if (profileItem != null) {
                     // update existing Profile Item
                     profileItem = acceptProfileItem(form, profileItem);
                 } else {
                     log.warn("accept() - Profile Item not found");
                     resource.notFound();
                     profileItem = null;
                 }
             } else {
                 log.warn("accept() - profileItemUid not supplied");
                 resource.badRequest(APIFault.MISSING_PARAMETERS);
                 profileItem = null;
             }
         }
 
         if (profileItem != null)
             profileItems.add(profileItem);
 
         return profileItems;
     }
 
     private ProfileItem acceptProfileItem(Form form, ProfileItem profileItem) {
 
         if (!resource.validateParameters()) {
             return null;
         }
 
         // TODO - Refactor this, each version should have it's own acceptor
         if (resource.getVersion().isVersionOne()) {
 
             profileItem.setStartDate(new ValidFromDate(form.getFirstValue("validFrom")));
             profileItem.setEnd(Boolean.valueOf(form.getFirstValue("end")));
 
         } else {
             profileItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
            if (form.getNames().contains("endDate") && form.getFirstValue("endDate") != null) {
                 profileItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
             } else {
                if (form.getNames().contains("duration") && form.getFirstValue("duration") != null) {
                     profileItem.setDuration(form.getFirstValue("duration"));
                     StartEndDate endDate = profileItem.getStartDate().plus(form.getFirstValue("duration"));
                     profileItem.setEndDate(endDate);
                 }
             }
 
             if (profileItem.getEndDate() != null && profileItem.getEndDate().before(profileItem.getStartDate())) {
                 resource.badRequest(APIFault.INVALID_DATE_RANGE);
                 return null;
             }
         }
 
         // determine name for new ProfileItem
         profileItem.setName(form.getFirstValue("name"));
 
         // see if ProfileItem already exists
         if (profileService.isUnique(profileItem)) {
 
             // save newProfileItem and do calculations
             profileService.persist(profileItem);
 
             // clear caches
             profileService.clearCaches(resource.getProfileBrowser());
 
             try {
                 // update item values if supplied
                 Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
                 for (String name : form.getNames()) {
                     ItemValue itemValue = itemValues.get(name);
                     if (itemValue != null) {
                         itemValue.setValue(form.getFirstValue(name));
                         if (itemValue.hasUnits() && form.getNames().contains(name+"Unit")) {
                             itemValue.setUnit(form.getFirstValue(name + "Unit"));
                         }
                         if (itemValue.hasPerUnits() && form.getNames().contains(name+"PerUnit")) {
                             itemValue.setPerUnit(form.getFirstValue(name + "PerUnit"));
                         }
                     }
                 }
                 profileService.calculate(profileItem);
             } catch (IllegalArgumentException ex) {
                 log.warn("accept() - Bad parameter received");
                 profileService.remove(profileItem);
                 resource.badRequest(APIFault.INVALID_PARAMETERS);
                 profileItem = null;
             }
         } else {
             log.warn("accept() - Profile Item already exists");
             resource.badRequest(APIFault.DUPLICATE_ITEM);
             return null;
         }
         return profileItem;
     }
 }
