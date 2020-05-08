 /*
 
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
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
 package com.sapienter.jbilling.server.util;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 
import com.sapienter.jbilling.server.util.db.JbillingTable;
 import com.sapienter.jbilling.server.util.db.PreferenceDTO;
 
 public class PreferenceWS implements Serializable {
 
     private Integer id;
     private PreferenceTypeWS preferenceType;
     private Integer tableId;
     private Integer foreignId;
     private Integer intValue;
     private String strValue;
     private BigDecimal floatValue;
 
     public PreferenceWS() {
     }
 
     public PreferenceWS(PreferenceDTO dto) {
         this.id = dto.getId();
         this.preferenceType = dto.getPreferenceType() != null ? new PreferenceTypeWS(dto.getPreferenceType()) : null;
         this.tableId = dto.getJbillingTable() != null ? dto.getJbillingTable().getId() : null;
         this.foreignId = dto.getForeignId();
         this.intValue = dto.getIntValue();
         this.strValue = dto.getStrValue();
         this.floatValue = dto.getFloatValue();
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public PreferenceTypeWS getPreferenceType() {
         return this.preferenceType;
     }
 
     public void setPreferenceType(PreferenceTypeWS preferenceType) {
         this.preferenceType = preferenceType;
     }
 
     public Integer getTableId() {
         return tableId;
     }
 
     public void setTableId(Integer tableId) {
         this.tableId = tableId;
     }
     
     public Integer getForeignId() {
         return this.foreignId;
     }
 
     public void setForeignId(Integer foreignId) {
         this.foreignId = foreignId;
     }
 
     public Integer getIntValue() {
         return this.intValue;
     }
 
     public void setIntValue(Integer intValue) {
         this.intValue = intValue;
     }
 
     public String getStrValue() {
         return this.strValue;
     }
 
     public void setStrValue(String strValue) {
         this.strValue = strValue;
     }
 
     public BigDecimal getFloatValue() {
         return this.floatValue;
     }
 
     public void setFloatValue(BigDecimal floatValue) {
         this.floatValue = floatValue;
     }
 
     @Override
     public String toString() {
         return "PreferenceWS{"
                + "id=" + id
                + ", preferenceType=" + preferenceType
                + ", tableId=" + tableId
                + ", foreignId=" + foreignId
                + ", intValue=" + intValue
                + ", strValue='" + strValue + '\''
                + ", floatValue=" + floatValue
                + '}';
     }
 }
