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
 
 import com.amee.base.utils.ThreadBeanHolder;
 import com.amee.base.utils.XMLUtils;
 import com.amee.domain.IAMEEEntityReference;
 import com.amee.domain.LocaleConstants;
 import com.amee.domain.LocaleService;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.builder.DataItemBuilder;
 import com.amee.domain.data.builder.v2.ItemValueBuilder;
 import com.amee.domain.data.builder.v2.ItemValueInListBuilder;
 import com.amee.domain.item.BaseItemValue;
 import com.amee.domain.item.HistoryValue;
 import com.amee.domain.item.data.DataItem;
 import com.amee.platform.science.StartEndDate;
 import com.amee.restlet.AMEEResource;
 import com.amee.restlet.RequestContext;
 import com.amee.restlet.utils.APIFault;
 import com.amee.service.data.DataBrowser;
 import com.amee.service.data.DataConstants;
 import com.amee.service.invalidation.InvalidationService;
 import com.amee.service.item.DataItemService;
 import com.amee.service.profile.ProfileService;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
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
 
 import java.io.Serializable;
 import java.util.*;
 
 //TODO - Move to builder model
 
 @Component
 @Scope("prototype")
 public class DataItemValueResource extends AMEEResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private DataItemService dataItemService;
 
     @Autowired
     private LocaleService localeService;
 
     @Autowired
     private DataBrowser dataBrowser;
 
     private DataCategory dataCategory;
     private DataItem dataItem;
 
     // Will be null is a sequence of ItemValues is being requested.
     private BaseItemValue itemValue;
 
     // Will be null is a single ItemValue is being requested.
     private List<BaseItemValue> itemValues;
 
     // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
     private int valuesPerPage = 1;
 
     @Autowired
     protected ProfileService profileService;
 
     @Autowired
     private InvalidationService invalidationService;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
 
         // Obtain DataCategory.
         dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
         dataBrowser.setDataCategory(dataCategory);
         (ThreadBeanHolder.get(RequestContext.class)).setDataCategory(dataCategory);
 
         // Obtain DataItem.
         dataItem = dataItemService.getDataItemByIdentifier(dataCategory, request.getAttributes().get("itemPath").toString());
         (ThreadBeanHolder.get(RequestContext.class)).setDataItem(dataItem);
 
         // Obtain ItemValue.
         setDataItemValue(request);
         (ThreadBeanHolder.get(RequestContext.class)).setItemValue(itemValue);
     }
 
     /**
      * Returns true if fetched objects for this request are valid.
      *
      * @return true if valid, otherwise false
      */
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 (dataCategory != null) &&
                 (dataItem != null) &&
                 dataItem.getDataCategory().equals(dataCategory) &&
                 !dataItem.isTrash() &&
                 (isItemValueValid() || isItemValuesValid());
     }
 
     /**
      * Returns true if itemValue is valid. Internally calls isItemValueValid(ItemValue itemValue).
      * <p/>
      * An ItemValue is valid if; it is not trashed and it belongs to the current DataItem.
      *
      * @return true if the itemValue is valid, otherwise false
      */
     private boolean isItemValueValid() {
         return isItemValueValid(itemValue);
     }
 
     /**
      * Returns true if itemValue is valid.
      * <p/>
      * An ItemValue is valid if; it is not trashed & it belongs to the current DataItem.
      *
      * @param itemValue to validate
      * @return true if the itemValue is valid, otherwise false
      */
     private boolean isItemValueValid(BaseItemValue itemValue) {
         return (itemValue != null) &&
                 !itemValue.isTrash() &&
                 itemValue.getItem().equals(dataItem);
     }
 
     /**
      * Returns true if the itemValues list is valid.
      * <p/>
      * Each ItemValue is checked with isItemValueValid(ItemValue itemValue).
      * <p/>
      * The itemValues list may be modified during a call (invalid items will be removed).
      *
      * @return true if the itemValues is valid, otherwise false
      */
     @SuppressWarnings(value = "unchecked")
     private boolean isItemValuesValid() {
 
         // Must have a list if ItemValues.
         if (itemValues == null) {
             return false;
         }
 
         // Validate all ItemValues in the itemValues list and remove any invalid items.
         itemValues = (List<BaseItemValue>) CollectionUtils.select(itemValues, new Predicate() {
             public boolean evaluate(Object o) {
                 return isItemValueValid((BaseItemValue) o);
             }
         });
 
         // The itemValues list is invalid if it is empty.
         return !itemValues.isEmpty();
     }
 
     @Override
     public List<IAMEEEntityReference> getEntities() {
         List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
         entities.add(dataItem);
         DataCategory dc = dataItem.getDataCategory();
         while (dc != null) {
             entities.add(dc);
             dc = dc.getDataCategory();
         }
         Collections.reverse(entities);
         return entities;
     }
 
     @Override
     public String getTemplatePath() {
         return getAPIVersion() + "/" + DataConstants.VIEW_ITEM_VALUE;
     }
 
     @Override
     // Note, itemValues (historical sequences) are not supported in V1 API and templates are only used in v1 API.
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", dataBrowser);
         values.put("dataItem", dataItem);
         values.put("itemValue", itemValue);
         values.put("node", itemValue);
         values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
         return values;
     }
 
     private void setDataItemValue(Request request) {
 
         Form query = request.getResourceRef().getQueryAsForm();
 
         // Must have a DataItem.
         if (dataItem == null) {
             return;
         }
 
         // Get the ItemValue identifier, which could be a path or a uid.
         String itemValueIdentifier = request.getAttributes().get("valuePath").toString();
 
         // Identifier must not be empty.
         if (itemValueIdentifier.isEmpty()) {
             return;
         }
 
         // The resource may receive a startDate parameter that sets the current date in an historical sequence of
         // ItemValues.
         Date startDate = new Date();
         if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
             startDate = new StartEndDate(query.getFirstValue("startDate"));
         }
 
         // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
         if (StringUtils.isNumeric(query.getFirstValue("valuesPerPage"))) {
             valuesPerPage = Integer.parseInt(query.getFirstValue("valuesPerPage"));
         }
 
         // Retrieve all itemValues in a historical sequence if mandated in the request (get=all), otherwise retrieve
         // the closest match.
         if (valuesPerPage > 1) {
             // TODO: Implement paging.
             itemValues = dataItemService.getAllItemValues(dataItem, itemValueIdentifier);
         } else {
             itemValue = dataItemService.getItemValue(dataItem, itemValueIdentifier, startDate);
         }
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         DataItemBuilder dataItemBuilder = new DataItemBuilder(dataItem, dataItemService);
         JSONObject obj = new JSONObject();
         if (itemValue != null) {
             obj.put("itemValue", new ItemValueBuilder(itemValue, dataItemBuilder, dataItemService).getJSONObject());
         } else {
             JSONArray values = new JSONArray();
             for (BaseItemValue iv : itemValues) {
                 values.put(new ItemValueInListBuilder(iv, dataItemService).getJSONObject(false));
             }
             obj.put("itemValues", values);
         }
         obj.put("dataItem", dataItemBuilder.getIdentityJSONObject());
         obj.put("path", dataItem.getFullPath() + "/" + getRequest().getAttributes().get("itemPath").toString());
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         DataItemBuilder dataItemBuilder = new DataItemBuilder(dataItem, dataItemService);
         Element element = document.createElement("DataItemValueResource");
         if (itemValue != null) {
             element.appendChild(new ItemValueBuilder(itemValue, dataItemBuilder, dataItemService).getElement(document));
         } else {
             Element values = document.createElement("ItemValues");
             for (BaseItemValue iv : itemValues) {
                 values.appendChild(new ItemValueInListBuilder(iv, dataItemService).getElement(document, false));
             }
             element.appendChild(values);
         }
         element.appendChild(dataItemBuilder.getIdentityElement(document));
         element.appendChild(XMLUtils.getElement(document, "Path", dataItem.getFullPath() + "/" + getRequest().getAttributes().get("itemPath").toString()));
         return element;
     }
 
     @Override
     public boolean allowPost() {
         // POSTs to Data ItemValues are never allowed.
         return false;
     }
 
     /**
      * Update an ItemValue based on PUT parameters.
      *
      * @param entity representation
      */
     @Override
     public void doStore(Representation entity) {
 
         log.debug("doStore()");
 
         // Must have a DataItem.
         if (dataItem == null) {
             badRequest();
             return;
         }
 
         boolean modified = false;
         Form form = getForm();
 
         // Update the ItemValue value field if parameter is present.
         // NOTE: This code makes it impossible to set the value to empty or null.
         if (StringUtils.isNotBlank(form.getFirstValue("value"))) {
             itemValue.setValue(form.getFirstValue("value"));
         }
 
         // Parse any submitted locale values
         for (String name : form.getNames()) {
             if (name.startsWith("value_")) {
                 // Get locale and locale name to handle.
                 String locale = name.substring(name.indexOf("_") + 1);
                 // Validate - Must have an available locale.
                 if (!LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return;
                 }
                 // Remove or Update/Create?
                 if (form.getNames().contains("remove_value_" + locale)) {
                     // Remove.
                     localeService.clearLocaleName(itemValue, locale);
                     // We modified.
                     modified = true;
                 } else {
                     // Update or create.
                     String localeNameStr = form.getFirstValue(name);
                     // Validate - Must have a locale name value.
                     if (StringUtils.isBlank(localeNameStr)) {
                         badRequest(APIFault.INVALID_PARAMETERS);
                         return;
                     }
                     // Do the update or create.
                     localeService.setLocaleName(itemValue, locale, localeNameStr);
                     // We modified.
                     modified = true;
                 }
             }
         }
 
         // Has a startDate parameter been submitted?
         if (StringUtils.isNotBlank(form.getFirstValue("startDate"))) {
 
             // Parse the startDate parameter into a Date object.
             Date startDate = new StartEndDate(form.getFirstValue("startDate"));
 
            // TODO: PL-6577 - Just update the date here? Transaction will rollback.

            // TODO: PL-6577 - Just compare to the EPOCH.
             // Can't amend the startDate of the first ItemValue in a history (startDate == DI.startDate)
            if (!HistoryValue.class.isAssignableFrom(dataItem.getClass())) {
                 log.warn("doStore() badRequest - Trying to update the startDate of the first DIV in a history.");
                 badRequest(APIFault.INVALID_RESOURCE_MODIFICATION);
                 return;
             }
 
             // TODO: Check that the IV is still unique?
             // !dataItem.isUnique(itemValueDefinition, startDate)
 
             // Update the startDate field, the parameter was valid.
             ((HistoryValue) itemValue).setStartDate(startDate);
 
             // We modified.
             modified = true;
         }
 
         // Mark the ItemValue and DataItem as modified.
         if (modified) {
             itemValue.onModify();
             dataItem.onModify();
         }
 
         // Always invalidate the DataCategory caches.
         dataItemService.clearItemValues();
         invalidationService.invalidate(dataItem.getDataCategory());
 
         // Update was a success.
         successfulPut(getFullPath());
     }
 
     /**
      * DELETEs an ItemValue from the DataItem. An ItemValue can only be removed if there is at least one
      * equivalent remaining ItemValue. Within a DataItem at least one ItemValue must
      * exist per ItemValueDefinition for the ItemDefinition.
      */
     @Override
     public void doRemove() {
         log.debug("doRemove()");
 
         // Must have a DataItem.
         if (dataItem == null) {
             badRequest();
             return;
         }
 
         // Attempt to remove a single ItemValue.
         int remaining = dataItemService.getAllItemValues(dataItem, itemValue.getItemValueDefinition().getPath()).size();
         if (remaining > 1) {
             dataItemService.remove(itemValue);
             dataItem.onModify();
             dataItemService.clearItemValues();
             invalidationService.invalidate(dataItem.getDataCategory());
             successfulDelete("/data/ " + dataItem.getFullPath());
         } else {
             badRequest(APIFault.DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE);
         }
     }
 
     public String getFullPath() {
         return "/data" + itemValue.getFullPath();
     }
 }
