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
 import com.amee.core.ThreadBeanHolder;
 import com.amee.domain.AMEEEntity;
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.StartEndDate;
 import com.amee.domain.data.*;
 import com.amee.domain.data.builder.v2.ItemValueBuilder;
 import com.amee.restlet.RequestContext;
 import com.amee.restlet.utils.APIFault;
 import com.amee.service.data.DataConstants;
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
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 import java.util.*;
 
 //TODO - Move to builder model
 @Component
 @Scope("prototype")
 public class DataItemValueResource extends BaseDataResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     // Will be null is a sequence of ItemValues is being requested.
     private ItemValue itemValue;
 
     // Will be null is a single ItemValue is being requested.
     private List<ItemValue> itemValues;
 
     // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
     private int valuesPerPage = 1;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         setDataItemByPathOrUid(request.getAttributes().get("itemPath").toString());
         setDataItemValue(request);
         if (getDataItemValue() != null) {
             ((RequestContext) ThreadBeanHolder.get("ctx")).setItemValue(getDataItemValue());
         }
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 getDataItem() != null &&
                 (isItemValueValid() || isItemValuesValid());
     }
 
     @Override
     public List<AMEEEntity> getEntities() {
         List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
         entities.add(getDataItemValue());
         entities.add(getDataItem());
         DataCategory dc = getDataItem().getDataCategory();
         while (dc != null) {
             entities.add(dc);
             dc = dc.getDataCategory();
         }
         entities.add(getActiveEnvironment());
         Collections.reverse(entities);
         return entities;
     }
 
     @Override
     public String getTemplatePath() {
         return getAPIVersion() + "/" + DataConstants.VIEW_CARBON_VALUE;
     }
 
     @Override
     // Note, itemValues (historical sequences) are not supported in V1 API and templates are only used in v1 API.
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", dataBrowser);
         values.put("dataItem", getDataItem());
         values.put("itemValue", this.itemValue);
         values.put("node", this.itemValue);
         values.put("availableLocales", LocaleName.AVAILABLE_LOCALES.keySet());
         return values;
     }
 
     private void setDataItemValue(Request request) {
 
         // This could be a path or a uid
         String itemValueIdentifier = request.getAttributes().get("valuePath").toString();
 
         if (itemValueIdentifier.isEmpty() || getDataItem() == null) {
             return;
         }
 
         Form query = request.getResourceRef().getQueryAsForm();
 
         // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
         if (StringUtils.isNumeric(query.getFirstValue("valuesPerPage"))) {
             this.valuesPerPage = Integer.parseInt(query.getFirstValue("valuesPerPage"));
         }
 
         // The resource may receive a startDate parameter that sets the current date in an historical sequence of
         // ItemValues.
         Date startDate = new Date();
         if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
             startDate = new StartEndDate(query.getFirstValue("startDate"));
         }
 
         //TODO - Implement paging
         // Retrieve all itemValues in a historical sequence if mandated in the request (get=all), otherwise retrieve
         // the closest match
         if (valuesPerPage > 1) {
             this.itemValues = getDataItem().getAllItemValues(itemValueIdentifier);
         } else {
             this.itemValue = getDataItem().getItemValue(itemValueIdentifier, startDate);
         }
     }
 
     private boolean isItemValueValid() {
         return this.itemValue != null &&
                 !this.itemValue.isTrash() &&
                 this.itemValue.getItem().equals(getDataItem()) &&
                 this.itemValue.getEnvironment().equals(getActiveEnvironment());
     }
 
     @SuppressWarnings(value = "unchecked")
     private boolean isItemValuesValid() {
         if (itemValues == null)
             return false;
 
         itemValues = (List<ItemValue>) CollectionUtils.select(itemValues, new Predicate() {
 
             @Override
             public boolean evaluate(Object o) {
                 ItemValue iv = (ItemValue) o;
                 return !iv.isTrash() &&
                         iv.getItem().equals(getDataItem()) &&
                         iv.getEnvironment().equals(getActiveEnvironment());
             }
         });
 
         return !itemValues.isEmpty();
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         if (itemValue != null) {
             itemValue.setBuilder(new ItemValueBuilder(this.itemValue));
             obj.put("itemValue", this.itemValue.getJSONObject());
         } else {
             JSONArray values = new JSONArray();
             for (ItemValue iv : itemValues) {
                 iv.setBuilder(new ItemValueBuilder(iv));
                 values.put(iv.getJSONObject(false));
             }
             obj.put("itemValues", values);
         }
         obj.put("dataItem", getDataItem().getIdentityJSONObject());
         obj.put("path", pathItem.getFullPath());
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         Element element = document.createElement("DataItemValueResource");
         if (itemValue != null) {
             itemValue.setBuilder(new ItemValueBuilder(this.itemValue));
             element.appendChild(this.itemValue.getElement(document));
         } else {
             Element values = document.createElement("ItemValues");
             for (ItemValue iv : itemValues) {
                 iv.setBuilder(new ItemValueBuilder(iv));
                 values.appendChild(iv.getElement(document, false));
             }
             element.appendChild(values);
         }
         element.appendChild(getDataItem().getIdentityElement(document));
         element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
         return element;
     }
 
     @Override
     public boolean allowPost() {
         // POSTs to Data ItemValues are never allowed.
         return false;
     }
 
     @Override
     public void doStore(Representation entity) {
 
         log.debug("doStore()");
 
         Form form = getForm();
         if (StringUtils.isNotBlank(form.getFirstValue("value"))) {
             this.itemValue.setValue(form.getFirstValue("value"));
         }
 
         // Parse any submitted locale values
         for (String name : form.getNames()) {
             if (name.startsWith("value_")) {
 
                 String locale = name.substring(name.indexOf("_") + 1);
                 String localeValueStr = form.getFirstValue(name);
 
                 if (StringUtils.isBlank(localeValueStr) || !LocaleName.AVAILABLE_LOCALES.containsKey(locale)) {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return;
                 }
 
                 if (this.itemValue.getLocaleValues().containsKey(locale)) {
                     LocaleName localeName = this.itemValue.getLocaleValues().get(locale);
                     localeName.setName(localeValueStr);
                     if (form.getNames().contains("remove_value_" + locale)) {
                         localeName.setStatus(AMEEStatus.TRASH);
                     }
                 } else {
                     LocaleName localeName =
                             new ItemValueLocaleName(this.itemValue, LocaleName.AVAILABLE_LOCALES.get(locale), localeValueStr);
                     this.itemValue.addLocaleName(localeName);
                 }
             }
         }
 
         if (StringUtils.isNotBlank(form.getFirstValue("startDate"))) {
             Date startDate = new StartEndDate(form.getFirstValue("startDate"));
 
             // Can't amend the startDate of the first ItemValue in a history (startDate == DI.startDate)
             if (itemValue.getStartDate().equals(getDataItem().getStartDate())) {
                log.warn("acceptRepresentation() - badRequest: trying to update the startDate of the first DIV in a history.");
                 badRequest(APIFault.INVALID_RESOURCE_MODIFICATION);
                 return;
             }
 
             // The submitted startDate must be (i) after or equal to the startDate and (ii) before the endDate of the owning DataItem.
             if (!getDataItem().isWithinLifeTime(startDate)) {
                log.warn("acceptRepresentation() - badRequest: trying to update a DIV starting after the endDate of the owning DI.");
                 badRequest(APIFault.INVALID_DATE_RANGE);
                 return;
             }
 
             this.itemValue.setStartDate(startDate);
         }
 
         dataService.clearCaches(getDataItem().getDataCategory());
         successfulPut(getFullPath());
 
     }
 
     @Override
     public void doRemove() {
 
         log.debug("doRemove()");
 
         // Only allow delete if there would be at least one DataItemValue for this ItemValueDefinition remaining.
         ItemValue itemValue = this.itemValue;
         final ItemValueDefinition itemValueDefinition = itemValue.getItemValueDefinition();
 
         int remaining = getDataItem().getAllItemValues(itemValueDefinition.getPath()).size();
         if (remaining > 1) {
             dataService.clearCaches(getDataItem().getDataCategory());
             dataService.remove(itemValue);
             successfulDelete(pathItem.getParent().getFullPath());
         } else {
             badRequest(APIFault.DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE);
         }
     }
 
     public ItemValue getDataItemValue() {
         return itemValue;
     }
 }
