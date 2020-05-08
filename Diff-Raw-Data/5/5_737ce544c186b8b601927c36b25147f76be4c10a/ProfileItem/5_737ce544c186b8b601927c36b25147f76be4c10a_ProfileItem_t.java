 package com.amee.domain.profile;
 
 import com.amee.platform.science.CO2Amount;
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.Builder;
 import com.amee.domain.ObjectType;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.DataItem;
 import com.amee.domain.data.Item;
 import com.amee.domain.data.ItemValue;
 import com.amee.platform.science.StartEndDate;
 import org.hibernate.annotations.Index;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.annotation.Resource;
 import javax.persistence.*;
 import java.util.Date;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 
 @Configurable(autowire = Autowire.BY_TYPE)
 @Entity
 @DiscriminatorValue("PI")
 public class ProfileItem extends Item {
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "PROFILE_ID")
     private Profile profile;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "DATA_ITEM_ID")
     private DataItem dataItem;
 
     @Column(name = "START_DATE")
     @Index(name = "START_DATE_IND")
     protected Date startDate = new Date();
 
     @Column(name = "END_DATE")
     @Index(name = "END_DATE_IND")
     protected Date endDate;
 
     @Transient
     private Double amount;
 
     @Transient
     private Builder builder;
 
     @Transient
     @Resource
     private CO2CalculationService calculationService;
 
     public ProfileItem() {
         super();
     }
 
     public ProfileItem(Profile profile, DataItem dataItem) {
         super(dataItem.getDataCategory(), dataItem.getItemDefinition());
         setProfile(profile);
         setDataItem(dataItem);
     }
 
     public ProfileItem(Profile profile, DataCategory dataCategory, DataItem dataItem) {
         super(dataCategory, dataItem.getItemDefinition());
         setProfile(profile);
         setDataItem(dataItem);
     }
 
     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
     @Override
     public String toString() {
         return "ProfileItem_" + getUid();
     }
 
     public ProfileItem getCopy() {
         log.debug("getCopy()");
         ProfileItem profileItem = new ProfileItem();
         copyTo(profileItem);
         return profileItem;
     }
 
     /**
      * Copy values from this instance to the supplied instance.
      *
      * @param o Object to copy values to
      */
     protected void copyTo(ProfileItem o) {
         super.copyTo(o);
         o.profile = profile;
         o.dataItem = dataItem;
         o.startDate = (startDate != null) ? (Date) startDate.clone() : null;
         o.endDate = (endDate != null) ? (Date) endDate.clone() : null;
         o.amount = amount;
         o.builder = builder;
         o.calculationService = calculationService;
     }
 
     public String getPath() {
         return getUid();
     }
 
     public Profile getProfile() {
         return profile;
     }
 
     public void setProfile(Profile profile) {
         this.profile = profile;
     }
 
     public DataItem getDataItem() {
         return dataItem;
     }
 
     public void setDataItem(DataItem dataItem) {
         if (dataItem != null) {
             this.dataItem = dataItem;
         }
     }
 
     public StartEndDate getStartDate() {
         return new StartEndDate(startDate);
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public StartEndDate getEndDate() {
         if (endDate != null) {
             return new StartEndDate(endDate);
         } else {
             return null;
         }
     }
 
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public boolean isEnd() {
         return (endDate != null) && (startDate.compareTo(endDate) == 0);
     }
 
     /**
     * Get the {@link com.amee.platform.science.CO2Amount CO2Amount} for this ProfileItem.
      * <p/>
      * If the ProfileItem does not support CO2 calculations (i.e. metadata) CO2Amount.ZERO is returned.
      *
     * @return - the {@link com.amee.platform.science.CO2Amount CO2Amount} for this ProfileItem
      */
     public CO2Amount getAmount() {
         if (amount == null) {
             log.debug("getAmount() - calculating amount");
             calculationService.calculate(this);
         }
         return new CO2Amount(amount);
     }
 
     public void setAmount(CO2Amount amount) {
         this.amount = amount.getValue();
     }
 
     @Override
     public JSONObject getJSONObject(boolean b) throws JSONException {
         return builder.getJSONObject(b);
     }
 
     public Element getElement(Document document, boolean b) {
         return builder.getElement(document, b);
     }
 
     public ObjectType getObjectType() {
         return ObjectType.PI;
     }
 
     public boolean hasNonZeroPerTimeValues() {
         for (ItemValue iv : getItemValues()) {
             if (iv.hasPerTimeUnit() && iv.isNonZero()) {
                 return true;
             }
         }
         return false;
     }
 
     //TODO - TEMP HACK - will remove as soon we decide how to handle return units in V1 correctly.
 
     public boolean isSingleFlight() {
         for (ItemValue iv : getItemValues()) {
             if ((iv.getName().startsWith("IATA") && iv.getValue().length() > 0) ||
                     (iv.getName().startsWith("Lat") && !iv.getValue().equals("-999")) ||
                     (iv.getName().startsWith("Lon") && !iv.getValue().equals("-999"))) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean isTrash() {
         return status.equals(AMEEStatus.TRASH) || getDataItem().isTrash();
     }
 }
