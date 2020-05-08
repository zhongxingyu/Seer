 package com.amee.restlet.profile.builder.v2;
 
 import com.amee.base.utils.XMLUtils;
 import com.amee.calculation.service.ProRataProfileService;
 import com.amee.domain.Pager;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.environment.Environment;
 import com.amee.domain.profile.Profile;
 import com.amee.domain.profile.ProfileItem;
 import com.amee.domain.profile.builder.v2.ProfileItemBuilder;
 import com.amee.platform.science.CO2AmountUnit;
 import com.amee.restlet.profile.ProfileCategoryResource;
 import com.amee.restlet.profile.builder.IProfileCategoryResourceBuilder;
 import com.amee.service.data.DataService;
 import com.amee.service.profile.ProfileBrowser;
 import com.amee.service.profile.ProfileService;
 import com.amee.service.profile.SelectByProfileService;
 import org.apache.abdera.ext.history.FeedPagingHelper;
 import org.apache.abdera.ext.opensearch.OpenSearchExtensionFactory;
 import org.apache.abdera.model.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.*;
 
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
 @Service("v2ProfileCategoryResourceBuilder")
 public class ProfileCategoryResourceBuilder implements IProfileCategoryResourceBuilder {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private DataService dataService;
 
     @Autowired
     private ProRataProfileService proRataProfileService;
 
     @Autowired
     private SelectByProfileService selectByProfileService;
 
     @Autowired
     private ProfileService profileService;
 
     public JSONObject getJSONObject(ProfileCategoryResource resource) throws JSONException {
 
         JSONObject obj = new JSONObject();
 
         if (resource.isBatchPost() && !resource.isFullRepresentationRequested()) {
 
             // For batch modifications return a list of newly created URIs
             JSONArray jsonProfileItems = new JSONArray();
             for (ProfileItem item : getProfileItems(resource, resource.getPager())) {
                 JSONObject itemJSON = new JSONObject();
                 itemJSON.put("uri", getFullPath(item));
                 itemJSON.put("uid", item.getUid());
                 jsonProfileItems.put(itemJSON);
             }
             obj.put("profileItems", jsonProfileItems);
 
         } else {
 
             // add objects
             obj.put("path", resource.getDataCategory().getFullPath());
 
             // add relevant Profile info depending on whether we are at root
            if (!resource.getDataCategory().getPath().isEmpty()) {
                 obj.put("profile", resource.getProfile().getIdentityJSONObject());
             } else {
                 obj.put("profile", resource.getProfile().getJSONObject());
             }
 
             // add environment
             obj.put("environment", Environment.ENVIRONMENT.getJSONObject(true));
 
             // add Data Category
             obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));
 
             // add Data Categories to children
             JSONArray dataCategories = new JSONArray();
             for (DataCategory dc : dataService.getDataCategories(resource.getDataCategory())) {
                 JSONObject dcObj = new JSONObject();
                 dcObj.put("uid", dc.getUid());
                 dcObj.put("name", dc.getName());
                 dcObj.put("path", dc.getPath());
                 dataCategories.put(dcObj);
             }
             obj.put("profileCategories", dataCategories);
 
             // profile items
             Pager pager = resource.getPager();
             List<ProfileItem> profileItems = getProfileItems(resource, pager);
 
             if (!profileItems.isEmpty()) {
                 JSONArray jsonProfileItems = new JSONArray();
                 obj.put("profileItems", jsonProfileItems);
                 for (ProfileItem pi : profileItems) {
                     setProfileItemBuilder(resource.getProfileBrowser(), pi);
                     jsonProfileItems.put(pi.getJSONObject(false));
                 }
 
                 // pager
                 if (pager != null) {
                     obj.put("pager", pager.getJSONObject());
                 }
 
                 // add CO2 amount
                 JSONObject totalAmount = new JSONObject();
                 totalAmount.put("value", getTotalAmount(profileItems, resource.getProfileBrowser().getCo2AmountUnit()) + "");
                 totalAmount.put("unit", resource.getProfileBrowser().getCo2AmountUnit());
                 obj.put("totalAmount", totalAmount);
 
             } else {
                 obj.put("profileItems", new JSONObject());
                 obj.put("pager", new JSONObject());
                 obj.put("totalAmount", "0");
             }
 
         }
 
         return obj;
     }
 
     public Element getElement(ProfileCategoryResource resource, Document document) {
 
         Element element;
 
         if (resource.isBatchPost() && !resource.isFullRepresentationRequested()) {
 
             // Generate only a basic representation of the ProfileItems
             element = document.createElement("ProfileItems");
             for (ProfileItem item : getProfileItems(resource, resource.getPager())) {
                 Element itemElement = document.createElement("ProfileItem");
                 itemElement.setAttribute("uri", getFullPath(item));
                 itemElement.setAttribute("uid", item.getUid());
                 element.appendChild(itemElement);
             }
 
         } else {
             // create element
             element = document.createElement("ProfileCategoryResource");
 
             element.appendChild(XMLUtils.getElement(document, "Path", resource.getDataCategory().getFullPath()));
 
             // add relevant Profile info depending on whether we are at root
            if (!resource.getDataCategory().getPath().isEmpty()) {
                 element.appendChild(resource.getProfile().getIdentityElement(document));
             } else {
                 element.appendChild(resource.getProfile().getElement(document));
             }
 
             // add environment
             element.appendChild(Environment.ENVIRONMENT.getElement(document, true));
 
             // add DataCategory
             element.appendChild(resource.getDataCategory().getIdentityElement(document));
 
             // add Data Categories
             Element dataCategoriesElement = document.createElement("ProfileCategories");
             for (DataCategory dc : dataService.getDataCategories(resource.getDataCategory())) {
                 Element dcElement = document.createElement("DataCategory");
                 dcElement.setAttribute("uid", dc.getUid());
                 dcElement.appendChild(XMLUtils.getElement(document, "Name", dc.getName()));
                 dcElement.appendChild(XMLUtils.getElement(document, "Path", dc.getPath()));
                 dataCategoriesElement.appendChild(dcElement);
             }
             element.appendChild(dataCategoriesElement);
 
             // profile items
             Pager pager = resource.getPager();
             List<ProfileItem> profileItems = getProfileItems(resource, pager);
 
             if (!profileItems.isEmpty()) {
 
                 Element profileItemsElement = document.createElement("ProfileItems");
                 element.appendChild(profileItemsElement);
                 for (ProfileItem pi : profileItems) {
                     setProfileItemBuilder(resource.getProfileBrowser(), pi);
                     profileItemsElement.appendChild(pi.getElement(document, false));
                 }
 
                 // pager
                 if (pager != null) {
                     element.appendChild(pager.getElement(document));
                 }
 
                 // add CO2 amount
                 Element totalAmount = XMLUtils.getElement(document,
                         "TotalAmount",
                         Double.toString(getTotalAmount(profileItems, resource.getProfileBrowser().getCo2AmountUnit())));
                 totalAmount.setAttribute("unit", resource.getProfileBrowser().getCo2AmountUnit().toString());
                 element.appendChild(totalAmount);
 
             }
             // Shouldn't we still display the amount here. See: JSON. (PL-1653)
 
         }
         return element;
     }
 
     private List<ProfileItem> pageResults(List<ProfileItem> profileItems, Pager pager, int currentPage) {
         // set-up pager
         if (!(profileItems == null || profileItems.isEmpty())) {
             pager.setCurrentPage(currentPage);
             pager.setItems(profileItems.size());
             pager.goRequestedPage();
 
             // limit results
             profileItems = profileItems.subList((int) pager.getStart(), (int) pager.getTo());
 
             pager.setItemsFound(profileItems.size());
         }
         return profileItems;
     }
 
     private List<ProfileItem> getProfileItems(ProfileCategoryResource resource, Pager pager) {
         List<ProfileItem> profileItems;
         if (resource.isGet()) {
             // get profile items
             profileItems = getProfileItems(resource);
             profileItems = pageResults(profileItems, pager, resource.getPage());
         } else {
             profileItems = resource.getProfileItems();
         }
         return profileItems;
     }
 
     private List<ProfileItem> getProfileItems(ProfileCategoryResource resource) {
 
         if (resource.getDataCategory().getItemDefinition() == null) {
             return new ArrayList<ProfileItem>();
         }
 
         ProfileBrowser browser = resource.getProfileBrowser();
 
         if (browser.isProRataRequest()) {
             return proRataProfileService.getProfileItems(
                     resource.getProfile(),
                     resource.getDataCategory(),
                     browser.getQueryStartDate(),
                     browser.getQueryEndDate());
 
         } else if (browser.isSelectByRequest()) {
             return selectByProfileService.getProfileItems(
                     resource.getProfile(),
                     resource.getDataCategory(),
                     browser.getQueryStartDate(),
                     browser.getQueryEndDate(),
                     browser.getSelectBy());
 
         } else {
             return profileService.getProfileItems(
                     resource.getProfile(),
                     resource.getDataCategory(),
                     browser.getQueryStartDate(),
                     browser.getQueryEndDate());
         }
     }
 
     // TODO: What is the total? Total default? Total CO2e?
 
     private double getTotalAmount(List<ProfileItem> profileItems, CO2AmountUnit returnUnit) {
         double totalAmount = 0.0;
         double amount;
         for (ProfileItem profileItem : profileItems) {
             amount = profileItem.getAmounts().defaultValueAsAmount().convert(returnUnit).getValue();
             totalAmount = totalAmount + amount;
         }
         return totalAmount;
     }
 
     private void setProfileItemBuilder(ProfileBrowser browser, ProfileItem pi) {
         if (browser.requestedCO2InExternalUnit()) {
             pi.setBuilder(new ProfileItemBuilder(pi, browser.getCo2AmountUnit()));
         } else {
             pi.setBuilder(new ProfileItemBuilder(pi));
         }
     }
 
     public Map<String, Object> getTemplateValues(ProfileCategoryResource resource) {
         Profile profile = resource.getProfile();
         DataCategory dataCategory = resource.getDataCategory();
         Map<String, Object> values = new HashMap<String, Object>();
         values.put("browser", resource.getProfileBrowser());
         values.put("profile", profile);
         values.put("dataCategory", dataCategory);
         values.put("node", dataCategory);
         return values;
     }
 
     public org.apache.abdera.model.Element getAtomElement(ProfileCategoryResource resource) {
         if (resource.isGet()) {
             return getAtomElementForGet(resource);
         } else {
             return getAtomElementForPost(resource);
         }
     }
 
     // Generate the Atom feed in response to a GET request to a ProfileCategory.
     // The request may contains query (search) parameters which may constrain and modify the returned ProfileItems.
 
     private org.apache.abdera.model.Element getAtomElementForGet(ProfileCategoryResource resource) {
 
         log.debug("getAtomElementForGet()");
 
         AtomFeed atomFeed = AtomFeed.getInstance();
 
         final Feed feed = atomFeed.newFeed();
         feed.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());
         feed.setTitle("Profile " + resource.getProfile().getDisplayName() + ", Category " + resource.getDataCategory().getName());
 
         atomFeed.newID(feed).setText("urn:dataCategory:" + resource.getDataCategory().getUid());
 
         atomFeed.addGenerator(feed, resource.getAPIVersion());
 
         atomFeed.addLinks(feed, "");
 
         Person author = atomFeed.newAuthor(feed);
         author.setName(resource.getProfile().getDisplayPath());
 
         Date epoch = new Date(0);
         Date lastModified = epoch;
 
         List<ProfileItem> profileItems = getProfileItems(resource);
 
         atomFeed.addName(feed, resource.getDataCategory().getName());
         double totalAmount = getTotalAmount(profileItems, resource.getProfileBrowser().getCo2AmountUnit());
         atomFeed.addTotalAmount(feed, Double.toString(totalAmount), resource.getProfileBrowser().getCo2AmountUnit().toString());
 
         Pager pager = resource.getPager();
         int numOfProfileItems = profileItems.size();
         if (numOfProfileItems > pager.getItemsPerPage()) {
             profileItems = pageResults(profileItems, pager, resource.getPage());
             FeedPagingHelper.setNext(feed, feed.getBaseUri() + "?page=" + pager.getNextPage());
             if (pager.getCurrentPage() != 1) {
                 FeedPagingHelper.setPrevious(feed, feed.getBaseUri() + "?page=" + pager.getPreviousPage());
             }
             FeedPagingHelper.setFirst(feed, feed.getBaseUri().toString());
             FeedPagingHelper.setLast(feed, feed.getBaseUri() + "?page=" + pager.getLastPage());
         }
 
         // If the GET contained query (search) parameters, add OpenSearch elements describing the query and the results.
         if (resource.getProfileBrowser().isQuery()) {
 
             if (numOfProfileItems > pager.getItemsPerPage()) {
                 feed.addExtension(OpenSearchExtensionFactory.ITEMS_PER_PAGE).setText("" + pager.getItemsPerPage());
                 feed.addExtension(OpenSearchExtensionFactory.START_INDEX).setText("1");
                 feed.addExtension(OpenSearchExtensionFactory.TOTAL_RESULTS).setText("" + pager.getItems());
             }
             org.apache.abdera.model.Element query = feed.addExtension(OpenSearchExtensionFactory.QUERY);
             query.setAttributeValue("role", "request");
             query.setAttributeValue(AtomFeed.Q_NAME_START_DATE, resource.getProfileBrowser().getQueryStartDate().toString());
             if (resource.getProfileBrowser().getQueryEndDate() != null) {
                 query.setAttributeValue(AtomFeed.Q_NAME_END_DATE, resource.getProfileBrowser().getQueryEndDate().toString());
             }
         }
 
 
         atomFeed.addChildCategories(feed, resource);
 
         CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();
 
         // Add all ProfileItems as Entries in the Atom feed.
         for (ProfileItem profileItem : profileItems) {
 
             String amount = profileItem.getAmounts().defaultValueAsAmount().convert(returnUnit).toString();
 
             Entry entry = feed.addEntry();
 
             Text title = atomFeed.newTitle(entry);
             title.setText(profileItem.getDisplayName());
             Text subtitle = atomFeed.newSubtitle(entry);
             subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));
 
             atomFeed.addLinks(entry, profileItem.getUid());
 
             IRIElement eid = atomFeed.newID(entry);
             eid.setText("urn:item:" + profileItem.getUid());
 
             entry.setPublished(profileItem.getStartDate());
             entry.setUpdated(profileItem.getStartDate());
 
             atomFeed.addDataItem(entry, profileItem.getDataItem());
 
             atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
 
             if (profileItem.getEndDate() != null) {
                 atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
             }
 
             atomFeed.addAmount(entry, amount, returnUnit.toString());
 
             atomFeed.addItemValuesWithLinks(entry, profileItem.getItemValues(), profileItem.getUid());
 
             HCalendar content = new HCalendar();
 
             content.addSummary(amount + " " + returnUnit.toString());
             content.addStartDate(profileItem.getStartDate());
 
             if (profileItem.getEndDate() != null) {
                 content.addEndDate(profileItem.getEndDate());
             }
 
             if (profileItem.getName() != null) {
                 atomFeed.addName(entry, profileItem.getName());
             }
 
             entry.setContentAsHtml(content.toString());
 
             Category cat = atomFeed.newItemCategory(entry);
             cat.setTerm(profileItem.getDataItem().getUid());
 
             cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());
             if (profileItem.getModified().after(lastModified))
                 lastModified = profileItem.getModified();
         }
 
         // If there are no ProfileItems in this feed, the lastModified date will be Date(0). In this case,
         // displaying the current Date is probably most sensible.
         if (lastModified.equals(epoch)) {
             feed.setUpdated(new Date());
         } else {
             feed.setUpdated(lastModified);
         }
 
         return feed;
     }
 
     // Generate the Atom feed in reponse to a POST to a ProfileCategory.
     // The feed will contain a single Atom Entry representing the new ProfileItem.
 
     private org.apache.abdera.model.Element getAtomElementForPost(ProfileCategoryResource resource) {
 
         AtomFeed atomFeed = AtomFeed.getInstance();
 
         //TODO - Add batch support
         ProfileItem profileItem = resource.getProfileItems().get(0);
 
         CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();
         String amount = profileItem.getAmounts().defaultValueAsAmount().convert(returnUnit).toString();
 
         Entry entry = atomFeed.newEntry();
         entry.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());
 
         Text title = atomFeed.newTitle(entry);
         title.setText(profileItem.getDisplayName());
         Text subtitle = atomFeed.newSubtitle(entry);
         subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));
 
         atomFeed.addLinks(entry, profileItem.getUid());
 
         IRIElement eid = atomFeed.newID(entry);
         eid.setText("urn:item:" + profileItem.getUid());
 
         entry.setPublished(profileItem.getStartDate());
         entry.setUpdated(profileItem.getStartDate());
 
         HCalendar content = new HCalendar();
 
         atomFeed.addAmount(entry, amount, returnUnit.toString());
 
         content.addSummary(profileItem.getAmounts().defaultValueAsAmount().convert(returnUnit) + " " + returnUnit.toString());
         content.addStartDate(profileItem.getStartDate());
         if (profileItem.getEndDate() != null) {
             content.addEndDate(profileItem.getEndDate());
         }
         entry.setContentAsHtml(content.toString());
 
         atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
 
         if (profileItem.getEndDate() != null) {
             atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
         }
 
         if (profileItem.getName() != null) {
             atomFeed.addName(entry, profileItem.getName());
         }
 
         atomFeed.addAmount(entry, amount, returnUnit.toString());
 
         atomFeed.addItemValuesWithLinks(entry, profileItem.getItemValues(), profileItem.getUid());
 
         Category cat = atomFeed.newItemCategory(entry);
         cat.setTerm(profileItem.getDataItem().getUid());
         cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());
 
         return entry;
     }
 
     private String getFullPath(ProfileItem profileItem) {
         return "/profiles/" + profileItem.getProfile().getDisplayPath() + profileItem.getFullPath();
     }
 }
 
