 package com.mpower.dao.util;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class QueryUtil {
     private QueryUtil() {
     }
 
     public static StringBuilder getCustomString(Map<String, String> customParams, LinkedHashMap<String, Object> parameterMap) {
         StringBuilder customString = new StringBuilder();
         if (customParams != null && !customParams.isEmpty()) {
             int paramCount = 1;
             for (Map.Entry<String, String> pair : customParams.entrySet()) {
                 String key = pair.getKey();
                 Object value = pair.getValue();
                 customString.append(" AND EXISTS ( SELECT giftCustomField.id FROM com.mpower.domain.GiftCustomField giftCustomField WHERE giftCustomField.gift.id = gift.id AND (");
                customString.append("giftCustomField.customField.name = :name");
                 customString.append(paramCount);
                 customString.append(" AND giftCustomField.customField.value LIKE :value");
                 customString.append(paramCount);
                 parameterMap.put("name" + paramCount, key);
                 parameterMap.put("value" + paramCount, "%" + value + "%");
                customString.append("))");
                 paramCount++;
             }
         }
         return customString;
     }
 
     public static StringBuilder getAddressString(Map<String, Object> addressParams, LinkedHashMap<String, Object> parameterMap) {
         StringBuilder addressString = new StringBuilder();
         if (addressParams != null && !addressParams.isEmpty()) {
             addressString.append(" AND EXISTS ( SELECT personAddress FROM com.mpower.domain.PersonAddress personAddress WHERE personAddress.person.id = person.id ");
             for (Map.Entry<String, Object> pair : addressParams.entrySet()) {
                 String key = pair.getKey();
                 Object value = pair.getValue();
                 addressString.append("AND personAddress.address.");
                 addressString.append(key);
                 addressString.append(" LIKE :");
                 String paramName = key.replace(".", "_");
                 addressString.append(paramName);
                 if (value instanceof String) {
                     parameterMap.put(paramName, "%" + value + "%");
                 } else {
                     parameterMap.put(paramName, value);
                 }
             }
             addressString.append(")");
         }
         return addressString;
     }
 
     public static StringBuilder getPhoneString(Map<String, Object> phoneParams, LinkedHashMap<String, Object> parameterMap) {
         StringBuilder phoneString = new StringBuilder();
         if (phoneParams != null && !phoneParams.isEmpty()) {
             phoneString.append(" AND EXISTS ( SELECT personPhone FROM com.mpower.domain.PersonPhone personPhone WHERE personPhone.person.id = person.id ");
             for (Map.Entry<String, Object> pair : phoneParams.entrySet()) {
                 String key = pair.getKey();
                 Object value = pair.getValue();
                 phoneString.append("AND personPhone.phone.");
                 phoneString.append(key);
                 phoneString.append(" LIKE :");
                 String paramName = key.replace(".", "_");
                 phoneString.append(paramName);
                 if (value instanceof String) {
                     parameterMap.put(paramName, "%" + value + "%");
                 } else {
                     parameterMap.put(paramName, value);
                 }
             }
             phoneString.append(")");
         }
         return phoneString;
     }
 }
