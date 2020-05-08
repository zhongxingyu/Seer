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
 package com.amee.restlet.data;
 
 import com.amee.core.APIUtils;
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.data.*;
 import com.amee.domain.path.PathItem;
 import com.amee.restlet.data.builder.DataCategoryResourceBuilder;
 import com.amee.restlet.utils.APIFault;
 import com.amee.service.data.DataBrowser;
 import com.amee.service.data.DataConstants;
 import com.amee.service.data.DataService;
 import com.amee.service.definition.DefinitionService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.DocumentException;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.*;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 @Component("dataCategoryResource")
 @Scope("prototype")
 public class DataCategoryResource extends BaseDataResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private static final Date EPOCH = new DateTime(0, DateTimeZone.UTC).toDate();
     
     @Autowired
     private DataService dataService;
 
     @Autowired
     private DefinitionService definitionService;
 
     private List<DataCategory> dataCategories;
     private DataItem dataItem;
     private List<DataItem> dataItems;
     private String type = "";
 
     @Autowired
     private DataCategoryResourceBuilder builder;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         setDataCategory(request.getAttributes().get("categoryUid").toString());
         setPage(request);
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 getDataCategory() != null &&
                 !getDataCategory().isTrash() &&
                 getDataCategory().getEnvironment().equals(environment);
     }
 
     @Override
     public String getTemplatePath() {
         return getAPIVersion() + "/" + DataConstants.VIEW_DATA_CATEGORY;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.putAll(builder.getTemplateValues(this));
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         return builder.getJSONObject(this);
     }
 
     @Override
     public Element getElement(Document document) {
         return builder.getElement(this, document);
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet()");
         // l user must have appropriate actions to view a data category. For deprecated data categories, the user
         // must have the appropriate actions.
         if (!dataBrowser.getDataCategoryActions().isAllowView()) {
             notAuthorized();
             return;
         } else if (getDataCategory().isDeprecated() && !dataBrowser.getDataCategoryActions().isAllowViewDeprecated()) {
             deprecated();
             return;
         }
 
         if (getAPIVersion().isNotVersionOne()) {
             Form form = getRequest().getResourceRef().getQueryAsForm();
             dataBrowser.setQueryStartDate(form.getFirstValue("startDate"));
             dataBrowser.setQueryEndDate(form.getFirstValue("endDate"));
         }
         super.handleGet();
     }
 
     @Override
     public void acceptRepresentation(Representation entity) {
         log.debug("acceptRepresentation()");
         acceptOrStore(entity);
     }
 
     @Override
     public void storeRepresentation(Representation entity) {
         log.debug("storeRepresentation()");
         acceptOrStore(entity);
     }
 
     public void acceptOrStore(Representation entity) {
         log.debug("acceptOrStore()");
         DataCategory thisDataCategory = getDataCategory();
 
         dataItems = new ArrayList<DataItem>();
         dataCategories = new ArrayList<DataCategory>();
 
         MediaType mediaType = entity.getMediaType();
         if (MediaType.APPLICATION_XML.includes(mediaType)) {
             acceptXML(entity);
         } else if (MediaType.APPLICATION_JSON.includes(mediaType)) {
             acceptJSON(entity);
         } else {
             if (getRequest().getMethod().equals(Method.POST)) {
                 acceptFormPost(getForm());
             } else if (getRequest().getMethod().equals(Method.PUT)) {
                 acceptFormPut(getForm());
             }
         }
 
         if ((dataCategory != null) || (dataItem != null) || !dataCategories.isEmpty() || !dataItems.isEmpty()) {
             // clear caches
             dataService.clearCaches(thisDataCategory);
             if (isPost()) {
                  if (isBatchPost()) {
                     successfulBatchPost();
                  } else if (type.equalsIgnoreCase("DC")) {
                     successfulPost(getFullPath(), dataCategory.getPath());
                  } else if (type.equalsIgnoreCase("DI")) {
                     successfulPost(getFullPath(), dataItem.getUid());
                  } else {
                     badRequest();
                  }
             } else {
                 successfulPut(getFullPath());
             }
         } else {
             badRequest();
         }
     }
 
     protected void acceptJSON(Representation entity) {
         log.debug("acceptJSON()");
         DataCategory dataCategory;
         DataItem dataItem;
         Form form;
         String key;
         JSONObject rootJSON;
         JSONArray dataCategoriesJSON;
         JSONArray dataItemsJSON;
         JSONObject itemJSON;
         try {
             rootJSON = new JSONObject(entity.getText());
             if (rootJSON.has("dataCategories")) {
                 dataCategoriesJSON = rootJSON.getJSONArray("dataCategories");
                 for (int i = 0; i < dataCategoriesJSON.length(); i++) {
                     itemJSON = dataCategoriesJSON.getJSONObject(i);
                     form = new Form();
                     for (Iterator iterator = itemJSON.keys(); iterator.hasNext();) {
                         key = (String) iterator.next();
                         form.add(key, itemJSON.getString(key));
                     }
                     dataCategory = acceptFormForDataCategory(form);
                     if (dataCategory != null) {
                         dataCategories.add(dataCategory);
                     } else {
                         log.warn("acceptJSON() - Data Category not added/modified");
                         return;
                     }
                 }
             }
             if (rootJSON.has("dataItems")) {
                 dataItemsJSON = rootJSON.getJSONArray("dataItems");
                 for (int i = 0; i < dataItemsJSON.length(); i++) {
                     itemJSON = dataItemsJSON.getJSONObject(i);
                     form = new Form();
                     for (Iterator iterator = itemJSON.keys(); iterator.hasNext();) {
                         key = (String) iterator.next();
                         form.add(key, itemJSON.getString(key));
                     }
                     dataItem = acceptFormForDataItem(form);
                     if (dataItem != null) {
                         dataItems.add(dataItem);
                     } else {
                         log.warn("acceptJSON() - Data Item not added/modified");
                         return;
                     }
                 }
             }
         } catch (JSONException e) {
             log.warn("acceptJSON() - Caught JSONException: " + e.getMessage(), e);
         } catch (IOException e) {
             log.warn("acceptJSON() - Caught JSONException: " + e.getMessage(), e);
         }
     }
 
     protected void acceptXML(Representation entity) {
         log.debug("acceptXML()");
         DataCategory dataCategory;
         DataItem dataItem;
         Form form;
         org.dom4j.Element rootElem;
         org.dom4j.Element dataCategoriesElem;
         org.dom4j.Element dataItemsElem;
         org.dom4j.Element itemElem;
         org.dom4j.Element valueElem;
         try {
             rootElem = APIUtils.getRootElement(entity.getStream());
             if (rootElem.getName().equalsIgnoreCase("DataCategory")) {
                 // handle Data Categories
                 dataCategoriesElem = rootElem.element("DataCategories");
                 if (dataCategoriesElem != null) {
                     for (Object o1 : dataCategoriesElem.elements("DataCategory")) {
                         itemElem = (org.dom4j.Element) o1;
                         form = new Form();
                         for (Object o2 : itemElem.elements()) {
                             valueElem = (org.dom4j.Element) o2;
                             form.add(valueElem.getName(), valueElem.getText());
                         }
                         dataCategory = acceptFormForDataCategory(form);
                         if (dataCategory != null) {
                             dataCategories.add(dataCategory);
                         } else {
                             log.warn("acceptXML() - Data Category not added");
                             return;
                         }
                     }
                 }
                 // handle Data Items
                 dataItemsElem = rootElem.element("DataItems");
                 if (dataItemsElem != null) {
                     for (Object o1 : dataItemsElem.elements("DataItem")) {
                         itemElem = (org.dom4j.Element) o1;
                         form = new Form();
                         for (Object o2 : itemElem.elements()) {
                             valueElem = (org.dom4j.Element) o2;
                             form.add(valueElem.getName(), valueElem.getText());
                         }
                         dataItem = acceptFormForDataItem(form);
                         if (dataItem != null) {
                             dataItems.add(dataItem);
                         } else {
                             log.warn("acceptXML() - Data Item not added");
                             return;
                         }
                     }
                 }
             } else {
                 log.warn("acceptXML() - DataCategory not found");
             }
         } catch (DocumentException e) {
             log.warn("acceptXML() - Caught DocumentException: " + e.getMessage(), e);
         } catch (IOException e) {
             log.warn("acceptXML() - Caught IOException: " + e.getMessage(), e);
         }
     }
 
     protected void acceptFormPost(Form form) {
         log.debug("acceptFormPost()");
         type = form.getFirstValue("newObjectType");
         if (type != null) {
             if (type.equalsIgnoreCase("DC")) {
                 if (dataBrowser.getDataCategoryActions().isAllowCreate()) {
                     dataCategory = acceptFormForDataCategory(form);
                 } else {
                     notAuthorized();
                 }
             } else if (type.equalsIgnoreCase("DI")) {
                 if (dataBrowser.getDataItemActions().isAllowCreate()) {
                     dataItem = acceptFormForDataItem(form);
                 } else {
                     notAuthorized();
                 }
             } else {
                 badRequest();
             }
         } else {
             badRequest();
         }
     }
 
     protected DataCategory acceptFormForDataCategory(Form form) {
 
         log.debug("acceptFormForDataCategory()");
 
         String uid;
         DataCategory dataCategory = null;
         DataCategory thisDataCategory;
 
         thisDataCategory = getDataCategory();
 
         if (getRequest().getMethod().equals(Method.POST)) {
                 if (dataBrowser.getDataCategoryActions().isAllowCreate() && thisDataCategory.getAliasedCategory() == null) {
                 // new DataCategory
                 dataCategory = new DataCategory(thisDataCategory);
 
                 // Is this a sym-link to another DataCategory?
                 if (form.getNames().contains("aliasedTo")) {
                     String aliasedToUid = form.getFirstValue("aliasedTo");
                     DataCategory aliasedTo = dataService.getDataCategoryByUid(aliasedToUid);
                     if (aliasedTo != null) {
                         dataCategory.setAliasedTo(aliasedTo);
                     } else {
                         badRequest(APIFault.INVALID_PARAMETERS);
                         return null;
                     }
                 } else if (form.getNames().contains("itemDefinitionUid")) {
                     ItemDefinition itemDefinition =
                             definitionService.getItemDefinition(
                                     thisDataCategory.getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                     if (itemDefinition != null) {
                         dataCategory.setItemDefinition(itemDefinition);
                     }
                 }
 
                 dataCategory = populateDataCategory(form, dataCategory);
 
                 if (!validDataCategory(dataCategory)) {
                     badRequest();
                 } else if (!isUnique(dataCategory)) {
                     badRequest(APIFault.DUPLICATE_ITEM);
                     return null;
                 } else {
                     dataCategory = acceptDataCategory(form, dataCategory);
                 }
             } else {
                 notAuthorized();
             }
         } else if (getRequest().getMethod().equals(Method.PUT)) {
             if (dataBrowser.getDataCategoryActions().isAllowModify()) {
                 // update DataCategory
                 uid = form.getFirstValue("dataCategoryUid");
                 if (uid != null) {
                     dataCategory = dataService.getDataCategoryByUid(uid);
                     if (dataCategory != null) {
                         dataCategory = populateDataCategory(form, dataCategory);
                         if (!validDataCategory(dataCategory)) {
                             badRequest();
                         } else {
                             dataCategory = acceptDataCategory(form, dataCategory);
                         }
                     }
                 }
             } else {
                 notAuthorized();
             }
         }
         return dataCategory;
     }
 
 
     private boolean validDataCategory(DataCategory dataCategory) {
         if (StringUtils.isBlank(dataCategory.getName()) || StringUtils.isBlank(dataCategory.getPath())) {
             return false;
         }
         return true;
     }
 
     // A unique DataCategory is one with a unique path within it's set of un-trashed sibling DataCategories.
     private boolean isUnique(DataCategory dataCategory) {
         boolean unique = true;
         for (PathItem sibling : getPathItem().getChildrenByType("DC")) {
             if (sibling.getPath().equals(dataCategory.getPath())) {
                 if (!dataService.getDataCategoryByUid(sibling.getUid()).isTrash()) {
                     unique = false;
                     break;
                 }
             }
         }
         return unique;
     }
 
     private DataCategory populateDataCategory(Form form, DataCategory dataCategory) {
         dataCategory.setName(form.getFirstValue("name"));
         dataCategory.setPath(form.getFirstValue("path"));
         return dataCategory;
     }
 
     private DataCategory acceptDataCategory(Form form, DataCategory dataCategory) {
         dataService.persist(dataCategory);
         return dataCategory;
     }
 
     protected DataItem acceptFormForDataItem(Form form) {
 
         log.debug("acceptFormForDataItem()");
 
         String uid;
         DataItem dataItem = null;
         ItemDefinition itemDefinition;
         DataCategory thisDataCategory;
 
         thisDataCategory = getDataCategory();
         if (getRequest().getMethod().equals(Method.POST)) {
             if (dataBrowser.getDataItemActions().isAllowCreate()) {
                 // new DataItem
                 itemDefinition = thisDataCategory.getItemDefinition();
                 if (itemDefinition != null) {
                     dataItem = new DataItem(thisDataCategory, itemDefinition);
                     dataItem = acceptDataItem(form, dataItem);
                 } else {
                     badRequest();
                 }
             } else {
                 notAuthorized();
             }
         } else if (getRequest().getMethod().equals(Method.PUT)) {
             if (dataBrowser.getDataItemActions().isAllowCreate()) {
                 // update DataItem
                 uid = form.getFirstValue("dataItemUid");
                 if (uid != null) {
                     dataItem = dataService.getDataItem(
                             environment,
                             uid);
                     if (dataItem != null) {
                         dataItem = acceptDataItem(form, dataItem);
                     }
                 }
             } else {
                 notAuthorized();
             }
         }
         return dataItem;
     }
 
     protected DataItem acceptDataItem(Form form, DataItem dataItem) {
         dataItem.setName(form.getFirstValue("name"));
         dataItem.setPath(form.getFirstValue("path"));
 
 
         // determine startdate for new DataItem
         /*
 
         Out-commenting DI startDate/endDate functionality. Pending final approval from AC, this will be 
         permanently deleted in due course - SM (15/07/09).
 
         StartEndDate startDate = new StartEndDate(form.getFirstValue("startDate"));
         dataItem.setStartDate(startDate);
 
 
         if (form.getNames().contains("endDate")) {
             dataItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
         } else {
             if (form.getNames().contains("duration")) {
                 StartEndDate endDate = startDate.plus(form.getFirstValue("duration"));
                 dataItem.setEndDate(endDate);
             }
         }
 
         if (dataItem.getEndDate() != null && dataItem.getEndDate().before(dataItem.getStartDate())) {
             badRequest(APIFault.INVALID_DATE_RANGE);
             return null;
         }
         */
 
         // Default the DataItem startDate to the EPOCH
         dataItem.setStartDate(EPOCH);
 
         dataService.persist(dataItem);
 
         // update item values if supplied
         for (String name : form.getNames()) {
             ItemValue itemValue = dataItem.getItemValue(name);
             if (itemValue != null) {
                 itemValue.setValue(form.getFirstValue(name));
             }
         }
         return dataItem;
     }
 
     public void acceptFormPut(Form form) {
         log.debug("acceptFormPut()");
         DataCategory thisDataCategory;
         if (dataBrowser.getDataCategoryActions().isAllowModify()) {
             thisDataCategory = getDataCategory();
             if (form.getNames().contains("name")) {
                 thisDataCategory.setName(form.getFirstValue("name"));
             }
             if (form.getNames().contains("path")) {
                 thisDataCategory.setPath(form.getFirstValue("path"));
             }
             if (form.getNames().contains("itemDefinitionUid")) {
 
                 // A sym-link category cannot have an itemDefinition
                 if (dataCategory.getAliasedCategory() != null) {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return;
                 } else {
                     ItemDefinition itemDefinition =
                             definitionService.getItemDefinition(thisDataCategory.getEnvironment(),
                                     form.getFirstValue("itemDefinitionUid"));
                     if (itemDefinition != null) {
                         thisDataCategory.setItemDefinition(itemDefinition);
                     } else {
                         thisDataCategory.setItemDefinition(null);
                     }
                 }
             }
 
             String deprecated = form.getFirstValue("deprecated");
             if (StringUtils.isNotBlank(deprecated)) {
                 if (deprecated.equals("true")) {
                     thisDataCategory.setStatus(AMEEStatus.DEPRECATED);
                 } else if (deprecated.equals("false")) {
                     thisDataCategory.setStatus(AMEEStatus.ACTIVE);
                 }
             }
 
             // Parse any submitted locale names
             for (String name : form.getNames()) {
                 if (name.startsWith("name_")) {
                     String localeNameStr = form.getFirstValue(name);
                     String locale = name.substring(name.indexOf("_") + 1);
                     if (LocaleName.AVAILABLE_LOCALES.containsKey(locale)) {
                         LocaleName localeName =
                                 new DataCategoryLocaleName(thisDataCategory, LocaleName.AVAILABLE_LOCALES.get(locale), localeNameStr);
                         thisDataCategory.putLocaleName(localeName);
                         form.removeFirst(name);
                     } else {
                         badRequest(APIFault.INVALID_PARAMETERS);
                         return;
                     }
                 }
             }
 
             dataService.clearCaches(thisDataCategory);
             successfulPut(getFullPath());
             dataCategory = thisDataCategory;
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public void removeRepresentations() {
         log.debug("removeRepresentations()");
         if (dataBrowser.getDataCategoryActions().isAllowDelete()) {
             dataService.clearCaches(getDataCategory());
             dataService.remove(getDataCategory());
             successfulDelete(pathItem.getParent().getFullPath());
         } else {
             notAuthorized();
         }
     }
 
     public DataService getDataService() {
         return dataService;
     }
 
     public DataBrowser getDataBrowser() {
         return dataBrowser;
     }
 
     public DataCategory getDataCategory() {
         return dataCategory;
     }
 
     public List<DataCategory> getDataCategories() {
         return dataCategories;
     }
 
     public DataItem getDataItem() {
         return dataItem;
     }
 
     public List<DataItem> getDataItems() {
         return dataItems;
     }
 }
