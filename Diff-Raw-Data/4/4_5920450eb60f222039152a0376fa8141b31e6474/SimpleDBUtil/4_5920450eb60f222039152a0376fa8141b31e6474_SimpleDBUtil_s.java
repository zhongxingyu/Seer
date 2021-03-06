 /* Copyright (C) 2011 SpringSource
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.grails.datastore.mapping.simpledb.util;
 
 import com.amazonaws.services.simpledb.model.Attribute;
 import com.amazonaws.services.simpledb.model.Item;
 import com.amazonaws.services.simpledb.util.SimpleDBUtils;
 import org.grails.datastore.mapping.model.ClassMapping;
 import org.grails.datastore.mapping.model.PersistentEntity;
 import org.grails.datastore.mapping.simpledb.config.SimpleDBDomainClassMappedForm;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Simple util class for SimpleDB.
  *
  * @author Roman Stepanenko
  * @since 0.1
  */
 public class SimpleDBUtil {
     public static final String AWS_ERR_CODE_CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailed";
 
     /**
      * Quotes and escapes an attribute name or domain name by wrapping it with backticks and escaping any backticks inside the name.
      * @param name
      * @return
      */
     public static String quoteName(String name) {
         return SimpleDBUtils.quoteName(name);
     }
 
     /**
      * Quotes and escapes an attribute value by wrapping it with single quotes and escaping any single quotes inside the value.
      * @param value
      * @return
      */
     public static String quoteValue(String value){
         return SimpleDBUtils.quoteValue(value);
     }
 
     /**
      * Quotes and escapes a list of values so that they can be used in a SimpleDB query.
      * @param values
      * @return
      */
     public static String quoteValues(Collection<String> values){
         return SimpleDBUtils.quoteValues(values);
     }
 
     /**
      * If domainNamePrefix is not null returns prefexed domain name.
      * @param domainName
      * @param domainNamePrefix
      * @return
      */
     public static String getPrefixedDomainName(String domainNamePrefix, String domainName){
         if (domainNamePrefix != null) {
             return domainNamePrefix + domainName;
         } else {
             return domainName;
         }
     }
 
     /**
      * Returns mapped domain name (*unprefixed*) for the specified @{link PersistentEntity}.
      * @param entity
      * @return
      */
     public static String getMappedDomainName(PersistentEntity entity){
         @SuppressWarnings("unchecked")
         ClassMapping<SimpleDBDomainClassMappedForm> classMapping = entity.getMapping();
         SimpleDBDomainClassMappedForm mappedForm = classMapping.getMappedForm();
         String entityFamily = getFamily(entity, mappedForm);
         return entityFamily;
     }
 
     private static String getFamily(PersistentEntity persistentEntity, SimpleDBDomainClassMappedForm mappedForm) {
         String table = null;
         if (mappedForm != null) {
             table = mappedForm.getFamily();
         }
        if (table == null) table = persistentEntity.getJavaClass().getName();
         return table;
     }
 
     public static List collectAttributeValues(Item item, String attributeName) {
         List ids = new LinkedList();
         for (Attribute attribute : item.getAttributes()) {
             if (attributeName.equals(attribute.getName())) {
                 ids.add(attribute.getValue());
             }
         }
         return ids;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     public static List<String> collectItemNames(List<Item> items) {
         if (items.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
 
         List ids = new LinkedList();
         for (Item item : items) {
             ids.add(item.getName());
         }
         return ids;
     }
 }
