 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.util;
 
 import com.sapienter.jbilling.server.util.db.JbillingTableDAS;
 import com.sapienter.jbilling.server.util.db.PreferenceDAS;
 import com.sapienter.jbilling.server.util.db.PreferenceDTO;
 import com.sapienter.jbilling.server.util.db.PreferenceTypeDAS;
 import com.sapienter.jbilling.server.util.db.PreferenceTypeDTO;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.dao.EmptyResultDataAccessException;
import org.hibernate.ObjectNotFoundException;
 import java.math.BigDecimal;
 
 public class PreferenceBL {
     private static Logger LOG = Logger.getLogger(PreferenceBL.class);
 
     private PreferenceDAS preferenceDas = null;
     private PreferenceTypeDAS typeDas = null;
     private PreferenceDTO preference = null;
     private PreferenceTypeDTO type = null;
     private JbillingTableDAS jbDAS = null;
 
     public PreferenceBL() {
         init();
     }
 
     public PreferenceBL(Integer preferenceId) {
         init();
         preference = preferenceDas.find(preferenceId);
     }
 
     public PreferenceBL(Integer entityId, Integer preferenceTypeId) {
         init();
         set(entityId, preferenceTypeId);
     }
     
     private void init() {
         preferenceDas = new PreferenceDAS();
         typeDas = new PreferenceTypeDAS();
         jbDAS = (JbillingTableDAS) Context.getBean(Context.Name.JBILLING_TABLE_DAS);
     }
 
     public void set(Integer entityId, Integer typeId) throws EmptyResultDataAccessException {
        
         LOG.debug("Looking for preference " + typeId + ", for entity " + entityId
                   + " and table '" + Constants.TABLE_ENTITY + "'");
 
        try {
            preference = preferenceDas.findByType_Row( typeId, entityId, Constants.TABLE_ENTITY);
            type = typeDas.find(typeId);
 
            // throw exception if there is no preference, or if the type does not have a
            // default value that can be returned.
            if (preference == null) {
                if (type == null || type.getDefaultValue() == null) {
                    throw new EmptyResultDataAccessException("Could not find preference " + typeId, 1);
                }
             }
        } catch (ObjectNotFoundException e) {
            // do nothing
            throw new EmptyResultDataAccessException("Could not find preference " + typeId, 1);
         }
     }
 
     public void createUpdateForEntity(Integer entityId, Integer preferenceId, Integer value) {
         createUpdateForEntity(entityId, preferenceId, (value != null ? value.toString() : ""));
     }
 
     public void createUpdateForEntity(Integer entityId, Integer preferenceId, BigDecimal value) {
         createUpdateForEntity(entityId, preferenceId, (value != null ? value.toString() : ""));
     }
 
     public void createUpdateForEntity(Integer entityId, Integer preferenceId, String value) {
         preference = preferenceDas.findByType_Row(preferenceId, entityId, Constants.TABLE_ENTITY);
         type = typeDas.find(preferenceId);
 
         if (preference != null) {
             // update preference
             preference.setValue(value);
 
         } else {
             // create a new preference
             preference = new PreferenceDTO();
             preference.setValue(value);
             preference.setForeignId(entityId);
             preference.setJbillingTable(jbDAS.findByName(Constants.TABLE_ENTITY));
             preference.setPreferenceType(new PreferenceTypeDAS().find(preferenceId));
             preference = preferenceDas.save(preference);
         }
     }
 
 
     /**
      * Returns the preference value if set. If the preference is null or has no
      * set value (is blank), the preference type default value will be returned.
      *
      * @return preference value as a string
      */
     public String getString() {
         if (preference != null && StringUtils.isNotBlank(preference.getValue())) {
             return preference.getValue();
         } else {
             return type != null ? type.getDefaultValue() : null;
         }
 
     }
 
     public Integer getInt() {
         String value = getString();
         return value != null ? Integer.valueOf(value) : null;
     }
 
     public Float getFloat() {
         String value = getString();
         return value != null ? Float.valueOf(value) : null;
     }
 
     public BigDecimal getDecimal() {
         String value = getString();
         return value != null ? new BigDecimal(value) : null;
     }
 
     /**
      * Returns the preference value as a string.
      *
      * @see #getString()
      * @return string value of preference
      */
     public String getValueAsString() {
         return getString();
     }
 
     /**
      * Returns the default value for the given preference type.
      *
      * @param preferenceTypeId preference type id
      * @return default preference value
      */
     public String getDefaultValue(Integer preferenceTypeId) {
         type = typeDas.find(preferenceTypeId);
         return type != null ? type.getDefaultValue() : null;
     }
 
     /**
      * Returns true if the preference value is null, false if value is set.
      *
      * This method ignores the preference type default value, unlike {@link #getString()}
      * and {@link #getValueAsString()} which will return the type default value if the
      * preference itself is unset.
      *
      * @return true if preference value is null, false if value is set.
      */
     public boolean isNull() {
         return preference == null || preference.getValue() == null;
     }
     
     public PreferenceDTO getEntity() {
         return preference;
     }
     
 }
