 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.el.ELException;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.component.*;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.model.SelectItem;
 import javax.faces.model.SelectItemGroup;
 import java.util.*;
 import java.lang.reflect.Array;
 
 import com.flexive.faces.components.input.FxSelectRenderer;
 
 /**
  * Utility functions for JSF/Facelets components.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxJsfComponentUtils {
 
     private static final Log LOG = LogFactory.getLog(FxJsfComponentUtils.class);
     /**
      * Attributed to be passed through
      */
     public final static List<String> SELECTLIST_ATTRIBUTES = Collections.unmodifiableList(Arrays.asList(
             "disabled",
             "readonly",
             "accesskey",
             "dir",
             "lang",
             "onblur",
             "onchange",
             "onclick",
             "ondblclick",
             "onfocus",
             "onkeydown",
             "onkeypress",
             "onkeyup",
             "onmousedown",
             "onmousemove",
             "onmouseout",
             "onmouseover",
             "onmouseup",
             "onselect",
             "style",
             "tabindex",
             "title"
     ));
 
     /**
      * Private constructor to avoid instantiation.
      */
     private FxJsfComponentUtils() {
     }
 
     /**
      * Evaluate the string attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @return the bound value, or null if no value is bound
      */
     public static String getStringValue(UIComponent component, String attributeName) {
         return (String) getValue(component, attributeName);
     }
 
     /**
      * Evaluate the string attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @param defaultValue  the default value to be used if no value is bound
      * @return the bound value, or null if no value is bound
      */
     public static String getStringValue(UIComponent component, String attributeName, String defaultValue) {
         return StringUtils.defaultIfEmpty((String) getValue(component, attributeName), defaultValue);
     }
 
     /**
      * Evaluate the integer attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @return the bound value, or null if no value is bound
      */
     public static Integer getIntegerValue(UIComponent component, String attributeName) {
         return (Integer) getValue(component, attributeName);
     }
 
     /**
      * Evaluate the long attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @param defaultValue  the default value to be used if no value is bound
      * @return the bound value, or <code>defaultValue</code> if no value is bound
      */
     public static int getIntegerValue(UIComponent component, String attributeName, int defaultValue) {
         final Integer intValue = (Integer) getValue(component, attributeName);
         return intValue != null ? intValue : defaultValue;
     }
 
     /**
      * Evaluate the long attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @return the bound value, or null if no value is bound
      */
     public static Long getLongValue(UIComponent component, String attributeName) {
         return (Long) getValue(component, attributeName);
     }
 
     /**
      * Evaluate the long attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @param defaultValue  the default value to be used if no value is bound
      * @return the bound value, or <code>defaultValue</code> if no value is bound
      */
     public static long getLongValue(UIComponent component, String attributeName, long defaultValue) {
         final Long longValue = (Long) getValue(component, attributeName);
         return longValue != null ? longValue : defaultValue;
     }
 
     /**
      * Evaluate the boolean attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @return the bound value, or null if no value is bound
      */
     public static Boolean getBooleanValue(UIComponent component, String attributeName) {
         return (Boolean) getValue(component, attributeName);
     }
 
     /**
      * Evaluate the boolean attribute of a component.
      *
      * @param component     a JSF component
      * @param attributeName the attribute name to be evaluated, e.g. "title"
      * @param defaultValue  the default value to be used when the attribute is null
      * @return the bound value, or null if no value is bound
      */
     public static boolean getBooleanValue(UIComponent component, String attributeName, boolean defaultValue) {
         final Boolean value = (Boolean) getValue(component, attributeName);
         return value != null ? value : defaultValue;
     }
 
     public static Object getValue(UIComponent component, String attributeName) {
         ValueExpression ve = component.getValueExpression(attributeName);
         if (ve != null) {
             return ve.getValue(FacesContext.getCurrentInstance().getELContext());
         }
         return null;
     }
 
     /**
      * Throws a FacesException if the given value is not set.
      *
      * @param attributeName the attribute name, e.g. "var"
      * @param componentName the component name, e.g. "fx:value"
      * @param value         the value set on the component
      * @throws FacesException if value is null or a blank string
      */
     public static void requireAttribute(String componentName, String attributeName, Object value) throws FacesException {
         if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
             throw new FacesException("Attribute '" + attributeName + "' of " + componentName + " not set.");
         }
     }
 
     /**
      * <p>Return a List of {@link javax.faces.model.SelectItem}
      * instances representing the available options for this component,
      * assembled from the set of {@link javax.faces.component.UISelectItem}
      * and/or {@link javax.faces.component.UISelectItems} components that are
      * direct children of this component.  If there are no such children, an
      * empty <code>Iterator</code> is returned.</p>
      * <p/>
      * Portions of the code are taken and adapted from the JSF RI 1.2
      *
      * @param context   The {@link javax.faces.context.FacesContext} for the current request.
      *                  If null, the UISelectItems behavior will not work.
      * @param component the component
      * @return a List of the select items for the specified component
      * @throws IllegalArgumentException if <code>context</code> is <code>null</code>
      */
     public static List<SelectItem> getSelectItems(FacesContext context, UIComponent component) {
         if (context == null)
             throw new IllegalArgumentException("context was null");
 
         ArrayList<SelectItem> list = new ArrayList<SelectItem>();
         for (UIComponent child : component.getChildren()) {
             if (child instanceof UISelectItem) {
                 UISelectItem item = (UISelectItem) child;
                 Object value = item.getValue();
                 if (value == null) {
                     list.add(new SelectItem(item.getItemValue(),
                             item.getItemLabel(),
                             item.getItemDescription(),
                             item.isItemDisabled(),
                             item.isItemEscaped()));
                 } else if (value instanceof SelectItem)
                     list.add((SelectItem) value);
                 else
                     throw new IllegalArgumentException("value class can not be converted to SelectItem: " + value.getClass().getName());
             } else if (child instanceof UISelectItems) {
                 Object value = ((UISelectItems) child).getValue();
                 if (value instanceof SelectItem) {
                     list.add((SelectItem) value);
                 } else if (value instanceof SelectItem[]) {
                     SelectItem[] items = (SelectItem[]) value;
                     // we manually copy the elements so that the list is modifiable.
                     // Arrays.asList() returns a non-mutable list.
                     //noinspection ManualArrayToCollectionCopy
                     for (SelectItem item : items)
                         list.add(item);
                 } else if (value instanceof Collection) {
                     for (Object element : ((Collection) value))
                         if (SelectItem.class.isInstance(element))
                             list.add((SelectItem) element);
                         else
                             throw new IllegalArgumentException("value class can not be converted to SelectItem: " + value.getClass().getName());
                 } else if (value instanceof Map) {
                     Map optionMap = (Map) value;
                     for (Object o : optionMap.entrySet()) {
                         Map.Entry entry = (Map.Entry) o;
                         Object key = entry.getKey();
                         Object val = entry.getValue();
                         if (key == null || val == null)
                             continue;
                         list.add(new SelectItem(val, key.toString()));
                     }
                 } else
                     throw new IllegalArgumentException("Child is not of expected type UISelectItem/UISelectItems. Family:" +
                             component.getFamily() + ", id:" + component.getId() + ", value class:" +
                            value != null ? value.getClass().getName() : "null");
             }
         }
         return (list);
     }
 
     /**
      * Get the converted value of a component making sure that validators are processed.
      * Portions of the code are taken and adapted from HtmlBasicInputRenderer of the JSF RI 1.2
      *
      * @param context        faces context
      * @param component      the component affected
      * @param submittedValue the submitted value
      * @return the converted value
      * @throws ConverterException on errors
      */
     public static Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
             throws ConverterException {
         String newValue = (String) submittedValue;
         // if we have no local value, try to get the valueExpression.
         ValueExpression valueExpression = component.getValueExpression("value");
         Converter converter = null;
 
         // If there is a converter attribute, use it to to ask application
         // instance for a converter with this identifer.
         if (component instanceof ValueHolder) {
             converter = ((ValueHolder) component).getConverter();
         }
 
         if (null == converter && null != valueExpression) {
             Class converterType = valueExpression.getType(context.getELContext());
             // if converterType is null, assume the modelType is "String".
             if (converterType == null || converterType == Object.class) {
                 //no conversion necessary
                 return newValue;
             }
 
             // If the converterType is a String, and we don't have a
             // converter-for-class for java.lang.String, assume the type is "String".
             if (converterType == String.class && !(null != context.getApplication().createConverter(String.class))) {
                 //no conversion necessary
                 return newValue;
             }
             // if getType returns a type for which we support a default
 
             try {
                 Application application = context.getApplication();
                 converter = application.createConverter(converterType);
             } catch (Exception e) {
                 LOG.error("Could not instantiate converter for type " + converterType + ": " + e.toString());
                 return (null);
             }
         } else if (converter == null) {
             // if there is no valueExpression and converter attribute set,
             // assume the modelType as "String" since we have no way of
             // figuring out the type. So for the selectOne and
             // selectMany, converter has to be set if there is no
             // valueExpression attribute set on the component.
 
             return newValue;
         }
         if (converter != null) {
             // If the conversion eventually falls to needing to use EL type coercion,
             // make sure our special ConverterPropertyEditor knows about this value.
             return converter.getAsObject(context, component, newValue);
         } else {
             // throw converter exception.
             throw new ConverterException("No converter for " + newValue + " available!");
         }
     }
 
     /**
      * Get a converter for the given class
      *
      * @param converterClass class to get a converter for
      * @param context        faces context
      * @return converter
      */
     public static Converter getConverterForClass(Class converterClass, FacesContext context) {
         if (converterClass == null)
             return null;
         try {
             Application application = context.getApplication();
             return (application.createConverter(converterClass));
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Coerce a value to its type
      *
      * @param context       faces context
      * @param value         value to coerce
      * @param itemValueType value class
      * @return coerced value
      */
     public static Object coerceToModelType(FacesContext context, Object value, Class itemValueType) {
         Object newValue;
         try {
             ExpressionFactory ef = context.getApplication().getExpressionFactory();
             newValue = ef.coerceToType(value, itemValueType);
         } catch (ELException ele) {
             newValue = value;
         } catch (IllegalArgumentException iae) {
             newValue = value;
         }
         return newValue;
     }
 
     /**
      * Get the values currently selected in a Select[Many|One]Listbox
      *
      * @param component the component
      * @return selected values as Object array
      */
     public static Object[] getCurrentSelectedValues(UIComponent component) {
         if (component instanceof UISelectMany) {
             UISelectMany select = (UISelectMany) component;
             Object value = select.getValue();
             if (value instanceof Collection)
                 return ((Collection) value).toArray();
             else if (value != null && !value.getClass().isArray())
                 LOG.warn("The UISelectMany value should be an array or a collection type, the actual type is " + value.getClass().getName());
             return (Object[]) value;
         } else if (component instanceof UISelectOne) {
             //select one
             UISelectOne select = (UISelectOne) component;
             Object val = select.getValue();
             if (val != null)
                 return new Object[]{val};
         }
         return new Object[0];
     }
 
     /**
      * Get the submitted values that are selected
      *
      * @param component the component
      * @return submitted values that are selected
      */
     public static Object[] getSubmittedSelectedValues(UIComponent component) {
         if (component instanceof UISelectMany) {
             UISelectMany select = (UISelectMany) component;
             return (Object[]) select.getSubmittedValue();
         }
 
         UISelectOne select = (UISelectOne) component;
         Object val = select.getSubmittedValue();
         if (val != null)
             return new Object[]{val};
         return null;
     }
 
     /**
      * Get the number of options to render depending on the type of the items (eg item groups require more items to be rendered)
      *
      * @param selectItems the select items to inspect
      * @return number of options to be rendered
      */
     public static int getSelectlistOptionCount(List<SelectItem> selectItems) {
         int itemCount = 0;
         if (!selectItems.isEmpty()) {
             for (Object selectItem : selectItems) {
                 itemCount++;
                 if (selectItem instanceof SelectItemGroup)
                     itemCount += ((SelectItemGroup) selectItem).getSelectItems().length;
             }
         }
         return itemCount;
 
     }
 
     /**
      * Check if an item is selected
      *
      * @param context    faces context
      * @param component  our component
      * @param itemValue  the value to check for selection
      * @param valueArray all values to compare against
      * @param converter  the converter
      * @return selected
      */
     public static boolean isSelectItemSelected(FacesContext context, UIComponent component, Object itemValue, Object valueArray, Converter converter) {
         if (itemValue == null && valueArray == null)
             return true;
         if (null != valueArray) {
             if (!valueArray.getClass().isArray()) {
                 LOG.warn("valueArray is not an array, the actual type is " + valueArray.getClass());
                 return valueArray.equals(itemValue);
             }
             int len = Array.getLength(valueArray);
             for (int i = 0; i < len; i++) {
                 Object value = Array.get(valueArray, i);
                 if (value == null && itemValue == null) {
                     return true;
                 } else {
                     if ((value == null) ^ (itemValue == null))
                         continue;
 
                     Object compareValue;
                     if (converter == null) {
                         compareValue = coerceToModelType(context, itemValue, value.getClass());
                     } else {
                         compareValue = itemValue;
                         if (compareValue instanceof String && !(value instanceof String)) {
                             // type mismatch between the time and the value we're
                             // comparing.  Invoke the Converter.
                             compareValue = converter.getAsObject(context, component, (String) compareValue);
                         }
                     }
 
                     if (value.equals(compareValue))
                         return true; //selected
                 }
             }
         }
         return false;
     }
 
     /**
      * Check if the given array contains an actual value
      *
      * @param valueArray value array
      * @return if an actual value is set in the array
      */
     public static boolean containsaValue(Object valueArray) {
         if (null != valueArray) {
             int len = Array.getLength(valueArray);
             for (int i = 0; i < len; i++) {
                 Object value = Array.get(valueArray, i);
                 if (value != null && !(value.equals(FxSelectRenderer.NO_VALUE))) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
