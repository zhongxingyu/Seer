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
 
 package com.orangeleap.tangerine.controller.gift;
 
 import com.orangeleap.tangerine.controller.TangerineConstituentAttributesFormController;
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.MutableGrid;
 import org.apache.commons.lang.math.NumberUtils;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.web.bind.ServletRequestDataBinder;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: alexlo
  * Date: Jul 20, 2009
  * Time: 10:57:28 AM
  */
 public abstract class AbstractMutableGridFormController extends TangerineConstituentAttributesFormController {
 
 	private String gridCollectionName;
 	private String amountKey;
 
 	public void setGridCollectionName(String gridCollectionName) {
 		this.gridCollectionName = gridCollectionName;
 	}
 
 	public String getGridCollectionName() {
 		return gridCollectionName;
 	}
 
 	public String getAmountKey() {
 		return amountKey;
 	}
 
 	public void setAmountKey(String amountKey) {
 		this.amountKey = amountKey;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	protected void convertFormToDomain(HttpServletRequest request, TangerineForm form, Map<String, Object> paramMap) throws Exception {
 		Map<String, Object> mutableGridMap = new TreeMap<String, Object>();
 		Map<String, Object> hiddenFieldsMutableGridMap = new TreeMap<String, Object>();
 
 		Iterator<Map.Entry<String, Object>> paramEntry = paramMap.entrySet().iterator();
 		while (paramEntry.hasNext()) {
 			Map.Entry<String, Object> entry = paramEntry.next();
 
 			// Filter for <gridCollectionName> or _<gridCollectionName> (hidden fields) 
 			if (entry.getKey().startsWith(getGridCollectionName())) {
 				mutableGridMap.put(entry.getKey(), request.getParameter(entry.getKey()));
 				paramEntry.remove();
 			}
 			else if (entry.getKey().startsWith("_" + getGridCollectionName())) {
 				hiddenFieldsMutableGridMap.put(entry.getKey(), request.getParameter(entry.getKey()));
 				paramEntry.remove();
 			}
 		}
 		/* Handle everything else that is not a gridRow */
 		super.convertFormToDomain(request, form, paramMap);
 
 		if (form.getDomainObject() instanceof MutableGrid) {
 			int newIndex = -1;
 			int oldIndex = -1;
 			MutablePropertyValues propertyValues = new MutablePropertyValues();
 
 			ServletRequestDataBinder binder = new ServletRequestDataBinder(form.getDomainObject());
 			initBinder(request, binder);
             BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(form.getDomainObject());
 
 			/*
 			 * Grid row indexes will be in ascending order but could skip numbers (i.e, 1, 3, 9, 15).
 			 * We need to 'normalize' the indexes to 0, 1, 2, 3 in the example above
 			 */
 			for (Map.Entry<String, Object> mutableGridEntry : mutableGridMap.entrySet()) {
 				int mutableGridIndex = findIndex(mutableGridEntry.getKey());
 
 				/* Create the row only if an amount key is specified */
 				boolean hasAmount = false;
 
 				String rowAmtKey = new StringBuilder(getGridCollectionName()).append(TangerineForm.TANG_START_BRACKET).
 						append(mutableGridIndex).append(TangerineForm.TANG_END_BRACKET).append(TangerineForm.TANG_DOT).append(getAmountKey()).toString();
 				String amountString = request.getParameter(rowAmtKey);
 
 				if (NumberUtils.isNumber(amountString)) {
 					hasAmount = true;
 				}
 				
 				if (mutableGridIndex != oldIndex) {
 					oldIndex = mutableGridIndex;
 					if (hasAmount) {
 						newIndex++;
 
 						/* Hidden fields need to have their indexes replaced also */
 						Iterator<Map.Entry<String, Object>> hiddenIter = hiddenFieldsMutableGridMap.entrySet().iterator();
 						while (hiddenIter.hasNext()) {
 							Map.Entry<String, Object> hiddenEntry = hiddenIter.next();
 
 							if (hiddenEntry.getKey().indexOf(TangerineForm.TANG_START_BRACKET + oldIndex + TangerineForm.TANG_END_BRACKET) > -1) {
 								// re-index
 								String newHiddenKey = hiddenEntry.getKey().replaceFirst(TangerineForm.TANG_START_BRACKET + oldIndex + TangerineForm.TANG_END_BRACKET,
 										TangerineForm.TANG_START_BRACKET + newIndex + TangerineForm.TANG_END_BRACKET);
 
 								form.addField(newHiddenKey, hiddenEntry.getValue());
 								propertyValues.addPropertyValue(TangerineForm.unescapeFieldName(newHiddenKey), hiddenEntry.getValue());
 
 								hiddenIter.remove();
 							}
 						}
 					}
 				}
 
 				if (hasAmount) {
 					String newRowKey = mutableGridEntry.getKey().replaceFirst(TangerineForm.TANG_START_BRACKET + "(\\d+)" + TangerineForm.TANG_END_BRACKET,
 							TangerineForm.TANG_START_BRACKET + newIndex + TangerineForm.TANG_END_BRACKET);
 
                     Object value = mutableGridEntry.getValue();
                    if (isEncryptedField(form, TangerineForm.unescapeFieldName(mutableGridEntry.getKey()), value)) {
                         value = handleEncryptedValue(value, beanWrapper, TangerineForm.unescapeFieldName(newRowKey));
                     }
 
 					/* Add the re-indexed property back to the form in case an error occurs and we need to re-display */
 					form.addField(newRowKey, value);
 
 					propertyValues.addPropertyValue(TangerineForm.unescapeFieldName(newRowKey), value);
 				}
 			}
 
 			/* Now create the row objects to bind to */
 			int arraySize = ++newIndex;
 			if (arraySize >= 0) {
 				if (form.getDomainObject() instanceof MutableGrid) {
 					MutableGrid mutableGrid = (MutableGrid) form.getDomainObject();
 					mutableGrid.resetAddGridRows(arraySize, getConstituent(request));
 				}
 			}
 
 			/* Now, bind to the rows */
 			binder.bind(propertyValues);
 		}
 	}
 
 	protected int findIndex(String fieldName) {
 		Matcher matcher = Pattern.compile("^" + getGridCollectionName() + TangerineForm.TANG_START_BRACKET + "(\\d+)" + TangerineForm.TANG_END_BRACKET + ".+$").matcher(fieldName);
 		int start = 0;
 		String s = null;
 		if (matcher != null) {
 		    while (matcher.find(start)) {
 		        s = matcher.group(1);
 		        start = matcher.end();
 		    }
 		}
 		return Integer.parseInt(s);
 	}
 }
