 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.json.controller.admin.picklists;
 
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.type.AccessType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONSerializer;
 import org.apache.commons.beanutils.DynaBean;
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.util.WebUtils;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 @Controller
 @RequestMapping("/customizePicklist.json")
 public class PicklistCustomizeController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
 	public static final String BLANK = "<blank>";
 	public static final String ITEM_TEMPLATE = "item-template-";
 	public static final String GL_ACCOUNT_CODE = "GLAccountCode";
 	public static final String PARENT_LIST = "parentList";
 	public static final String PARENT_VALUE = "parentValue";
     public static final String KEY = "key";
     public static final String VALUE = "value";
     public final static String ACCOUNT_STRING_1 = "AccountString1";
     public final static String ACCOUNT_STRING_2 = "AccountString2";
 
     @Resource(name="picklistItemService")
     protected PicklistItemService picklistItemService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping(method = RequestMethod.GET)
     public ModelMap getCustomFields(HttpServletRequest request, String picklistNameId, Long picklistItemId) {
         if (!picklistEditAllowed(request)) {
             throw new RuntimeException("You are not authorized to access this page");
         }
         ModelMap map = new ModelMap();
         List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
         if (picklistItemId == null || picklistItemId <= 0) {
             getPicklistCustomFields(picklistNameId, map, returnList);
         }
         else {
              getPicklistItemCustomFields(picklistNameId, picklistItemId, map, returnList);
         }
         return map;
     }
 
     private void getPicklistCustomFields(String picklistNameId, ModelMap map, List<Map<String, String>> returnList) {
         Picklist picklist = picklistItemService.getPicklist(picklistNameId);
         Map<String, String> customFieldNameValueMap = getCustomFieldNameValues(picklist.getCustomFieldMap());
         if (customFieldNameValueMap.isEmpty()) {
             if (isOutOfBoxDependentPicklist(picklist)) {
                 customFieldNameValueMap.put(PARENT_LIST, StringConstants.EMPTY);
                 customFieldNameValueMap.put(ITEM_TEMPLATE + PARENT_VALUE, StringConstants.EMPTY);
             }
             if (isGLCoded(picklist)) {
                 customFieldNameValueMap.put(ITEM_TEMPLATE + GL_ACCOUNT_CODE, StringConstants.EMPTY);
                 customFieldNameValueMap.put(ITEM_TEMPLATE + "01-GLPART1", StringConstants.EMPTY);
                 customFieldNameValueMap.put(ITEM_TEMPLATE + "02-GLPART2", StringConstants.EMPTY);
                 customFieldNameValueMap.put(ITEM_TEMPLATE + "03-GLPART3", StringConstants.EMPTY);
                 customFieldNameValueMap.put(ITEM_TEMPLATE + "04-GLPART4", StringConstants.EMPTY);
             }
         }
         appendToModelMap(map, returnList, customFieldNameValueMap, picklist);
     }
 
     @SuppressWarnings("unchecked")
     private void getPicklistItemCustomFields(String picklistNameId, Long picklistItemId,
                                              ModelMap map, List<Map<String, String>> returnList) {
         Picklist picklist = picklistItemService.getPicklist(picklistNameId);
         PicklistItem item = getPicklistItem(picklist, picklistItemId);
         Map<String, String> customFieldNameValueMap = getCustomFieldNameValues(item.getCustomFieldMap());
         addDefaultFields(picklist, customFieldNameValueMap);
         appendToModelMap(map, returnList, customFieldNameValueMap, picklist);
     }
 
     @SuppressWarnings("unchecked")
     private void appendToModelMap(ModelMap map, List<Map<String, String>> returnList,
                                   Map<String, String> customFieldNameValueMap, Picklist picklist) {
         for (Map.Entry<String, String> entry : customFieldNameValueMap.entrySet()) {
             Map<String, String> fldMap = new HashMap<String, String>();
             fldMap.put(StringConstants.ID, entry.getKey()); 
             fldMap.put(KEY, entry.getKey());
             fldMap.put(VALUE, entry.getValue());
             returnList.add(fldMap);
         }
         map.put("rows", returnList);
         map.put("totalRows", returnList.size());
         map.put("picklistName", picklist.getPicklistDesc());
     }
 
     private PicklistItem getPicklistItem(Picklist picklist, Long picklistItemId) {
         PicklistItem returnItem = null;
         for (PicklistItem item : picklist.getPicklistItems()) {
             if (picklistItemId.equals(item.getId())) {
                 returnItem = picklistItemService.getPicklistItem(item.getId());
             }
         }
         return returnItem;
     }
 
     private void addDefaultFields(Picklist picklist, Map<String, String> customFieldNameValueMap) {
         for (Map.Entry<String, CustomField> e : picklist.getCustomFieldMap().entrySet()) {
             String name = e.getValue().getName();
             if (name.startsWith(ITEM_TEMPLATE)) {
                 name = name.substring(ITEM_TEMPLATE.length());
                 String value = e.getValue().getValue();
                 if ( ! customFieldNameValueMap.containsKey(name)) {
                     customFieldNameValueMap.put(name, replaceBlankWithEmpty(value));
                 }
             }
         }
     }
 
 	@SuppressWarnings("unchecked")
 	public static boolean picklistEditAllowed(HttpServletRequest request) {
 		Map<String, AccessType> pageAccess = (Map<String, AccessType>)WebUtils.getSessionAttribute(request, "pageAccess");
 		return pageAccess.get("/picklistItems.htm") == AccessType.ALLOWED;
 	}
 
     private boolean isOutOfBoxDependentPicklist(Picklist picklist) {
         return picklist.getPicklistName().equals("stateProvince");
     }
 
     private boolean isGLCoded(Picklist picklist) {
     	return picklist.getPicklistNameId().endsWith("projectCode")
             || picklist.getPicklistNameId().endsWith("customFieldMap[bank]");
     }
 
 	protected Map<String, String> getCustomFieldNameValues(Map<String, CustomField> map) {
 		Map<String, String> result = new TreeMap<String, String>();
 		for (Map.Entry<String, CustomField> entry : map.entrySet()) {
 			result.put(entry.getValue().getName(), replaceBlankWithEmpty(entry.getValue().getValue()));
 		}
 		return result;
 	}
 
     private String replaceBlankWithEmpty(String value) {
         if (value.equalsIgnoreCase(BLANK)) {
             value = StringConstants.EMPTY;
         }
         return value;
     }
 
     @RequestMapping(method = RequestMethod.POST)
     @SuppressWarnings("unchecked")
     public ModelMap saveCustomFields(String rows, String picklistNameId, Long picklistItemId) {
         if (logger.isTraceEnabled()) {
             logger.trace("saveCustomFields: rows = " + rows + " picklistNameId = " + picklistNameId +
                     " picklistItemId = " + picklistItemId);
         }
         Picklist picklist = picklistItemService.getPicklist(picklistNameId);
         if (picklist == null) {
             throw new IllegalArgumentException("The picklist was not found for " + picklistNameId);
         }
         ModelMap map = new ModelMap();
         List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
         Map<String, String> customFieldMap = resolveCustomFieldMap(rows);
 
         if (picklistItemId == null || picklistItemId < 1) {
             updateCustomFieldMap(customFieldMap, picklist);
             picklist = picklistItemService.maintainPicklist(picklist);
             customFieldMap = getCustomFieldNameValues(picklist.getCustomFieldMap());
         }
         else {
             PicklistItem item = getPicklistItem(picklist, picklistItemId);
             if (isGLCoded(picklist)) {
                 customFieldMap.put(ACCOUNT_STRING_1, getAccountString(customFieldMap, false));
                 customFieldMap.put(ACCOUNT_STRING_2, getAccountString(customFieldMap, true));
             }
             updateCustomFieldMap(customFieldMap, item);
             item = picklistItemService.maintainPicklistItem(item);
             customFieldMap = getCustomFieldNameValues(item.getCustomFieldMap());
         }
         appendToModelMap(map, returnList, customFieldMap, picklist);
         map.put("success", Boolean.TRUE.toString().toLowerCase());
         return map;
     }
 
     @SuppressWarnings("unchecked")
     private Map<String, String> resolveCustomFieldMap(String rows) {
         JSONArray jsonArray = JSONArray.fromObject(rows);
         List<DynaBean> beans = (List<DynaBean>) JSONSerializer.toJava(jsonArray);
 
         Map<String, String> customFieldMap = new TreeMap<String, String>();
         for (DynaBean bean : beans) {
             String fieldName = null;
             if (bean.getDynaClass().getDynaProperty(KEY) != null && bean.get(KEY) != null &&
                     StringUtils.hasText(bean.get(KEY).toString())) {
                 fieldName = bean.get(KEY).toString();
             }
             else if (bean.getDynaClass().getDynaProperty(StringConstants.ID) != null && bean.get(StringConstants.ID) != null) {
                 fieldName = bean.get(StringConstants.ID).toString();
             }
             String fieldValue = null;
             if (bean.getDynaClass().getDynaProperty(VALUE) != null && bean.get(VALUE) != null &&
                     StringUtils.hasText(bean.get(VALUE).toString())) {
                 fieldValue = bean.get(VALUE).toString();
             }
             if ( ! StringUtils.hasText(fieldValue)) {
                 fieldValue = BLANK;
             }
             if (StringUtils.hasText(fieldName)) {
                 customFieldMap.put(fieldName, fieldValue);
             }
         }
         return customFieldMap;
     }
 
 	private void updateCustomFieldMap(Map<String, String> map, AbstractCustomizableEntity entity) {
 		entity.getCustomFieldMap().clear();
 		for (Map.Entry<String, String> e : map.entrySet()) {
 			entity.setCustomFieldValue(e.getKey(), e.getValue());
 		}
 	}
     
     private String getAccountString(Map<String, String> map, boolean extraDash) {
         StringBuilder sb = new StringBuilder();
         for (Map.Entry<String, String> e : map.entrySet()) {
             if (e.getKey().matches("^[0-9]+-.*")) {
                String value = e.getValue().trim();
                boolean hasValue = value.length() > 0;
                 if (sb.length() > 0) {
                     if (hasValue || extraDash) {
                         sb.append("-");
                     }
                 }
                sb.append(e.getValue());
             }
         }
         return sb.toString();
     }
 }
