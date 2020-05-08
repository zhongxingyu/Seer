 package org.openmrs.module.datacomparison;
 
import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MetaDataComparisonService {
 	
 	private static final int SIMPLE_DATA_TYPE = 0;
 	
 	private static final String NULL = "null";
 
	public List<RowMeta> getRowMetaList(Object existingItem, Object incomingItem) throws IllegalAccessException {
 		
 		List<RowMeta> rowMetaList = new ArrayList<RowMeta>();
 		
 		ElementMeta existingItemMeta = null;
 		ElementMeta incomingItemMeta = null;
 		RowMeta rowMeta = null;
 		Map<String, ElementMeta> metaItems = null;
 		
 		Class c = existingItem.getClass();
 		Field[] fields = c.getDeclaredFields();
 		
 		for (int i=0; i<fields.length; i++) {
 			
 			existingItemMeta = new ElementMeta();
 			incomingItemMeta = new ElementMeta();
 			rowMeta = new RowMeta();
 			metaItems = new HashMap<String, ElementMeta>();
 			
 			fields[i].setAccessible(true);
 			
 			Object existingItemFieldValue = fields[i].get(existingItem);
 			Object incomingItemFieldValue = fields[i].get(incomingItem);
 			
 			if (existingItemFieldValue != null) {
 				
                 if (isSimpleDataType(existingItemFieldValue)) {
                 	
                 	existingItemMeta.setIsComplex(false);
                 	existingItemMeta.setPropertyType(SIMPLE_DATA_TYPE);
                 	existingItemMeta.setPropertyValue(existingItemFieldValue.toString());
                 	
                 } else {
                 	
                 	existingItemMeta.setIsComplex(true);
                 	
                 }
                 
             } else {
             	existingItemMeta.setIsComplex(false);
             	existingItemMeta.setPropertyType(SIMPLE_DATA_TYPE);
             	existingItemMeta.setPropertyValue(NULL);
             }
 			
 			if (incomingItemFieldValue != null) {
 				
                 if (isSimpleDataType(incomingItemFieldValue)) {
                 	
                 	incomingItemMeta.setIsComplex(false);
                 	incomingItemMeta.setPropertyType(SIMPLE_DATA_TYPE);
                 	incomingItemMeta.setPropertyValue(incomingItemFieldValue.toString());
                 	
                 } else {
                 	
                 	incomingItemMeta.setIsComplex(true);
                 	
                 }
                 
 			} else {
 				incomingItemMeta.setIsComplex(false);
             	incomingItemMeta.setPropertyType(SIMPLE_DATA_TYPE);
             	incomingItemMeta.setPropertyValue(NULL);
 			}
 			
 			if ((existingItemFieldValue != null) && (incomingItemFieldValue != null)) {
 				if (existingItemFieldValue.equals(incomingItemFieldValue)) {
                 	rowMeta.setIsSimilar(true);
                 } else {
                 	rowMeta.setIsSimilar(false);
                 }
 			} else if ((existingItemFieldValue == null) && (incomingItemFieldValue == null)) {
 				rowMeta.setIsSimilar(true);
 			} else {
 				rowMeta.setIsSimilar(false);
 			}
 			
 			metaItems.put("existingItem", existingItemMeta);
 			metaItems.put("incomingItem", incomingItemMeta);
 			
 			rowMeta.setPropertyName(fields[i].getName());
 			rowMeta.setMetaItems(metaItems);
 			rowMeta.setLevel(0);
 			
 			rowMetaList.add(rowMeta);
 			
 		}
 		
 		return rowMetaList;
 		
 	}
 	
 	private boolean isSimpleDataType(Object data) {
 		
 		return data.getClass().isPrimitive() ||
 			data.getClass() == java.lang.Long.class ||
 			data.getClass() == java.lang.String.class ||
 			data.getClass() == java.lang.Integer.class ||
 			data.getClass() == java.lang.Boolean.class ||
 			data.getClass() == java.lang.Float.class ||
 			data.getClass() == java.lang.Double.class ||
     		data.getClass() == java.lang.Byte.class ||
 			data.getClass() == java.lang.Character.class ||
 			data.getClass() == java.lang.Short.class ||
     		data.getClass() == java.util.Date.class;
 		
 	}
 	
 }
