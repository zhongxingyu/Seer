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
 
 package com.orangeleap.tangerine.controller;
 
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.lang.StringEscapeUtils;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 public class TangerineForm {
 	
 	protected Map<String, Object> fieldMap = new HashMap<String, Object>();
 	protected Object domainObject;
 	public static final String DOT = "\\.";
 	public static final String START_BRACKET = "\\[";
 	public static final String END_BRACKET = "\\]";
 	public static final String TANG_DOT = "-tangDot-";
 	public static final String TANG_START_BRACKET = "-tangStartBracket-";
 	public static final String TANG_END_BRACKET = "-tangEndBracket-";
 	public static final String TANG_DOT_VALUE = escapeFieldName(StringConstants.DOT_VALUE);
 
 	public Map<String, Object> getFieldMap() {
 		return fieldMap;
 	}
 
 	public void setFieldMap(Map<String, Object> fieldMap) {
 		this.fieldMap = fieldMap;
 	}
 	
 	public void setFieldMapFromEntity(AbstractEntity entity) {
 		for (Map.Entry<String, Object> entry : entity.getFieldValueMap().entrySet()) {
 			addField(entry.getKey(), entry.getValue());
 		}
 	}
 	
 	public static String escapeFieldName(String key) {
 		key = key.replaceAll(DOT, TANG_DOT).replaceAll(START_BRACKET, TANG_START_BRACKET).replaceAll(END_BRACKET, TANG_END_BRACKET);
 		return StringEscapeUtils.escapeHtml(key);
 	}
 
 	public static String unescapeFieldName(String key) {
 		return key.replaceAll(TANG_DOT, DOT).replaceAll(TANG_START_BRACKET, START_BRACKET).replaceAll(TANG_END_BRACKET, END_BRACKET);
 	}
 	
 	public void clearFieldMap() {
 		if (fieldMap != null) {
 			fieldMap.clear();
 		}
 	}
 	
 	public void addField(String key, Object value) {
 		if (fieldMap == null) {
 			fieldMap = new HashMap<String, Object>();
 		}
 		fieldMap.put(escapeFieldName(key), value);
 	}
 
 	/**
 	 * Finds the field value for an 'unescaped' (has '[', ']', and '.' characters) field name.
 	 * @param unescapedFieldName the field name, unescaped (or escaped)
 	 * @return field value
 	 */
 	public Object getFieldValueFromUnescapedFieldName(String unescapedFieldName) {
 		String escapedFieldName = escapeFieldName(unescapedFieldName);
		return fieldMap.get(escapedFieldName);
//		return fieldMap.get(removeCustomFieldDotValueSuffix(escapedFieldName));
 	}
 
 	/**
 	 * Finds the field value for an 'escaped' (does NOT have '[', ']', and '.' characters) field name.
 	 * @param escapedFieldName the field name, escaped
 	 * @return field value
 	 */
 	public Object getFieldValue(String escapedFieldName) {
		return fieldMap.get(escapedFieldName);
//		return fieldMap.get(removeCustomFieldDotValueSuffix(escapedFieldName));
 	}
 
 	/**
 	 * If the field name is a custom field, removes the
 	 * '.value' (escaped to -tangDot-value) from the end of the name, since field names are not stored with '.value' at the end
 	 * @param escapedFieldName the field name, escaped of '[', ']', and '.' chars
 	 * @return escapedFieldName, minus the escaped '.value'
 	 */
 	private String removeCustomFieldDotValueSuffix(String escapedFieldName) {
 		if (escapedFieldName.endsWith(TANG_DOT_VALUE)) {
 			escapedFieldName = escapedFieldName.substring(0, escapedFieldName.length() - TANG_DOT_VALUE.length());
 		}
 		return escapedFieldName;
 	}
 
 	public Object getDomainObject() {
 		return domainObject;
 	}
 
 	public void setDomainObject(Object domainObject) {
 		this.domainObject = domainObject;
 	}
 
 	public static String getGridCollectionName(String fieldName) {
 	    Matcher matcher = java.util.regex.Pattern.compile("^(.+)\\[\\d+\\].*$").matcher(fieldName);
 
 	    int start = 0;
 	    String s = null;
 	    if (matcher != null) {
 	        while (matcher.find(start)) {
 	            s = matcher.group(1);
 	            start = matcher.end();
 	        }
 	    }
 	    return s;
 	}
 }
