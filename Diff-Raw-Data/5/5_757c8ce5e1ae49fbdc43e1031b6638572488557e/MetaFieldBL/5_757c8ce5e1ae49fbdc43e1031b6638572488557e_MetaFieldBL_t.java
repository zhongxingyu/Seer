 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.metafields;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.metafields.db.EntityType;
 import com.sapienter.jbilling.server.metafields.db.MetaField;
 import com.sapienter.jbilling.server.metafields.db.MetaFieldDAS;
 import com.sapienter.jbilling.server.metafields.db.MetaFieldValue;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Business Logic for meta-fields.
  *
  * @author Brian Cowdery
  * @since 03-Oct-2011
  */
 public class MetaFieldBL {
 
     public static MetaField getFieldByName(Integer entityId, EntityType entityType, String name) {
         return new MetaFieldDAS().getFieldByName(entityId, entityType, name);
     }
 
     /**
      * Returns a map of MetaField's for the given entity type keyed by the field
      * name's plain text name. Basically a list of name-value-pair names with the original
      * MetaField object to be used when building new fields.
      *
      * @param entityType entity type to query
      * @return map with available fields
      */
     public static Map<String, MetaField> getAvailableFields(Integer entityId, EntityType entityType) {
         List<MetaField> entityFields = new MetaFieldDAS().getAvailableFields(entityId, entityType);
         Map<String, MetaField> result = new LinkedHashMap<String, MetaField>();
         for (MetaField field : entityFields) {
             result.put(field.getName(), field);
         }
         return result;
     }
 
     public static List<MetaField> getAvailableFieldsList(Integer entityId, EntityType entityType) {
         return new MetaFieldDAS().getAvailableFields(entityId, entityType);
     }
 
     public static void validateMetaFields(Integer entityId, EntityType type, MetaFieldValueWS[] metaFields) {
         for (MetaField field : new MetaFieldDAS().getAvailableFields(entityId,type)) {
             MetaFieldValue value = field.createValue();
             for (MetaFieldValueWS valueWS : metaFields) {
                 if (field.getName().equals(valueWS.getFieldName())) {
                     value.setValue(valueWS.getValue());
                     break;
                 }
             }
             validateMetaField(field, value);
         }
     }
 
     /**
      * Validates all meta fields, configured for entity
      *
      * @param customizedEntity entity with meta fields for validation
      */
     public static void validateMetaFields(Integer entityId, MetaContent customizedEntity) {
         List<MetaField> availableMetaFields = getAvailableFieldsList(entityId, customizedEntity.getCustomizedEntityType());
         for (MetaField field : availableMetaFields) {
             MetaFieldValue value = customizedEntity.getMetaField(field.getName());
             MetaFieldBL.validateMetaField(field, value);
         }
     }
 
     public static void validateMetaField(MetaField field, MetaFieldValue value) {
        if (field.isDisabled())
            return;

         if (value != null) {
             value.validate();
         }

         if (field.isMandatory() && value == null) {
             throw new SessionInternalError("Validation failed.", new String[]{"MetaFieldValue,value,value.cannot.be.null"});
         }
     }
 
     public static MetaFieldValueWS[] convertMetaFieldsToWS(Integer entityId, MetaContent entity) {
         List<MetaField> availableMetaFields = new MetaFieldDAS().getAvailableFields(entityId, entity.getCustomizedEntityType());
         MetaFieldValueWS[] result = new MetaFieldValueWS[]{};
         if (availableMetaFields != null && !availableMetaFields.isEmpty()) {
             result = new MetaFieldValueWS[availableMetaFields.size()];
             int i = 0;
             for (MetaField field : availableMetaFields) {
                 MetaFieldValue value = entity.getMetaField(field.getName());
                 if (value == null) {
                     value = field.createValue();
                 }
                 result[i++] = new MetaFieldValueWS(value);
             }
         }
         return result;
     }
 
     public static void fillMetaFieldsFromWS(Integer entityId, MetaContent entity, MetaFieldValueWS[] metaFields) {
         if (metaFields != null) {
             for (MetaFieldValueWS fieldValue : metaFields) {
                 entity.setMetaField(entityId, fieldValue.getFieldName(), fieldValue.getValue());
             }
         }
     }
 
     public MetaField create(MetaField dto) {
         MetaField metaField = new MetaField();
         metaField.setEntity(dto.getEntity());
         metaField.setEntityType(dto.getEntityType());
         metaField.setDataType(dto.getDataType());
         metaField.setName(dto.getName());
         metaField.setDisplayOrder(dto.getDisplayOrder());
         metaField.setMandatory(dto.isMandatory());
         metaField.setDisabled(dto.isDisabled());
         if (dto.getDefaultValue() != null) {
             metaField.setDefaultValue(dto.getDefaultValue());
         }
         MetaFieldDAS das = new MetaFieldDAS();
         metaField = das.save(metaField);
         return metaField;
     }
 
     public void update(MetaField dto) {
         MetaFieldDAS das = new MetaFieldDAS();
         MetaField metaField = das.find(dto.getId());
         metaField.setName(dto.getName());
         metaField.setDisplayOrder(dto.getDisplayOrder());
         metaField.setMandatory(dto.isMandatory());
         metaField.setDisabled(dto.isDisabled());
         if (metaField.getDefaultValue() != null && dto.getDefaultValue() == null) {
             metaField.getDefaultValue().setValue(null);
         } else if (dto.getDefaultValue() != null && metaField.getDefaultValue() == null) {
             MetaFieldValue value = metaField.createValue();
             value.setValue(dto.getDefaultValue().getValue());
             metaField.setDefaultValue(value);
         } else if (metaField.getDefaultValue() != null) {
             metaField.getDefaultValue().setValue(dto.getDefaultValue().getValue());
         }
         das.save(metaField);
     }
 
     public void delete(int metaFieldId) {
         MetaFieldDAS das = new MetaFieldDAS();
         MetaField metaField = das.find(metaFieldId);
         if (metaField.getDefaultValue() != null) {
             metaField.setDefaultValue(null);
             das.save(metaField);
             das.flush();
         }
         das.deleteMetaFieldValuesForEntity(metaField.getEntityType(), metaFieldId);
         das.flush();
         das.clear();
 
         das.delete(metaField);
 
         das.flush();
         das.clear();
     }
 }
