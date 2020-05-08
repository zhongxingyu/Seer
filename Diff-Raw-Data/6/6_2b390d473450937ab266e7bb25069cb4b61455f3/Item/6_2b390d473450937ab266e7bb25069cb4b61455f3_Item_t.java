 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.domain.data;
 
 import com.amee.domain.AMEEEntityAdapter;
 import com.amee.domain.IAMEEEntityReference;
 import com.amee.domain.IItemService;
 import com.amee.domain.item.BaseItem;
 import com.amee.domain.path.Pathable;
 import com.amee.platform.science.InternalValue;
 import com.amee.platform.science.StartEndDate;
 import org.joda.time.Duration;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.*;
 
 public abstract class Item extends AMEEEntityAdapter implements Pathable {
 
     public final static int NAME_MAX_SIZE = LegacyItem.NAME_MAX_SIZE;
 
     public Item() {
         super();
     }
 
     public void addItemValue(ItemValue itemValue) {
         if (isLegacy()) {
             getLegacyEntity().addItemValue(itemValue.getLegacyEntity());
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public Set<ItemValueDefinition> getItemValueDefinitions() {
         if (isLegacy()) {
             return getLegacyEntity().getItemValueDefinitions();
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public JSONObject getIdentityJSONObject() throws JSONException {
         if (isLegacy()) {
             return getLegacyEntity().getIdentityJSONObject();
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public abstract JSONObject getJSONObject(boolean detailed) throws JSONException;
 
     public Element getIdentityElement(Document document) {
         if (isLegacy()) {
             return getLegacyEntity().getIdentityElement(document);
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public List<IAMEEEntityReference> getHierarchy() {
         if (isLegacy()) {
             return getLegacyEntity().getHierarchy();
         } else {
             return getNuEntity().getHierarchy();
         }
     }
 
     public ItemDefinition getItemDefinition() {
         if (isLegacy()) {
             return getLegacyEntity().getItemDefinition();
         } else {
             return getNuEntity().getItemDefinition();
         }
     }
 
     public void setItemDefinition(ItemDefinition itemDefinition) {
         if (isLegacy()) {
             getLegacyEntity().setItemDefinition(itemDefinition);
         } else {
             getNuEntity().setItemDefinition(itemDefinition);
         }
     }
 
     public DataCategory getDataCategory() {
         if (isLegacy()) {
             return getLegacyEntity().getDataCategory();
         } else {
             return getNuEntity().getDataCategory();
         }
     }
 
     public void setDataCategory(DataCategory dataCategory) {
         if (isLegacy()) {
             getLegacyEntity().setDataCategory(dataCategory);
         } else {
             getNuEntity().setDataCategory(dataCategory);
         }
     }
 
     public String getName() {
         if (isLegacy()) {
             return getLegacyEntity().getName();
         } else {
             return getNuEntity().getName();
         }
     }
 
     public String getDisplayName() {
         if (isLegacy()) {
             return getLegacyEntity().getDisplayName();
         } else {
             return getNuEntity().getDisplayName();
         }
     }
 
     public String getDisplayPath() {
         if (isLegacy()) {
             return getLegacyEntity().getDisplayPath();
         } else {
             return getNuEntity().getDisplayPath();
         }
     }
 
     public String getFullPath() {
         if (isLegacy()) {
             return getLegacyEntity().getFullPath();
         } else {
             return getNuEntity().getFullPath();
         }
     }
 
     public List<ItemValue> getItemValues() {
         if (isLegacy()) {
             List<ItemValue> itemValues = new ArrayList<ItemValue>();
             for (LegacyItemValue legacyItemValue : getLegacyEntity().getItemValues()) {
                 itemValues.add(ItemValue.getItemValue(legacyItemValue));
             }
             return itemValues;
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public List<ItemValue> getAllItemValues(String itemValuePath) {
         if (isLegacy()) {
             List<ItemValue> itemValues = new ArrayList<ItemValue>();
             for (LegacyItemValue legacyItemValue : getLegacyEntity().getAllItemValues(itemValuePath)) {
                 itemValues.add(ItemValue.getItemValue(legacyItemValue));
             }
             return itemValues;
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     @Deprecated
     public ItemValueMap getItemValuesMap() {
        if (isLegacy()) {
            return getLegacyEntity().getItemValuesMap();
        } else {
            throw new UnsupportedOperationException();
        }
     }
 
     public ItemValue getItemValue(String identifier, Date startDate) {
         if (isLegacy()) {
             return ItemValue.getItemValue(getLegacyEntity().getItemValue(identifier, startDate));
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public ItemValue getItemValue(String identifier) {
         if (isLegacy()) {
             return ItemValue.getItemValue(getLegacyEntity().getItemValue(identifier));
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public void appendInternalValues(Map<ItemValueDefinition, InternalValue> values) {
         if (isLegacy()) {
             getLegacyEntity().appendInternalValues(values);
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public void setName(String name) {
         if (isLegacy()) {
             getLegacyEntity().setName(name);
         } else {
             getNuEntity().setName(name);
         }
     }
 
     public abstract StartEndDate getStartDate();
 
     public abstract StartEndDate getEndDate();
 
     public boolean supportsCalculation() {
         return getLegacyEntity().supportsCalculation();
     }
 
     public boolean isWithinLifeTime(Date date) {
         if (isLegacy()) {
             return getLegacyEntity().isWithinLifeTime(date);
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public void setEffectiveStartDate(Date effectiveStartDate) {
         if (isLegacy()) {
             getLegacyEntity().setEffectiveStartDate(effectiveStartDate);
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public Date getEffectiveStartDate() {
         if (isLegacy()) {
             return getLegacyEntity().getEffectiveStartDate();
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public void setEffectiveEndDate(Date effectiveEndDate) {
         if (isLegacy()) {
             getLegacyEntity().setEffectiveEndDate(effectiveEndDate);
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public Date getEffectiveEndDate() {
         if (isLegacy()) {
             return getLegacyEntity().getEffectiveEndDate();
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public Duration getDuration() {
         if (isLegacy()) {
             return getLegacyEntity().getDuration();
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public boolean isUnique(ItemValueDefinition itemValueDefinition, StartEndDate startDate) {
         if (isLegacy()) {
             return getLegacyEntity().isUnique(itemValueDefinition, startDate);
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     public boolean isLegacy() {
         return getLegacyEntity() != null;
     }
 
     public abstract LegacyItem getLegacyEntity();
 
     public abstract BaseItem getNuEntity();
 
     public abstract IItemService getItemService();
 }
