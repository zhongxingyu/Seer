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
 package com.amee.domain.profile.builder.v1;
 
 import com.amee.base.utils.XMLUtils;
 import com.amee.domain.Builder;
 import com.amee.domain.TimeZoneHolder;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.data.builder.v1.ItemValueBuilder;
 import com.amee.domain.profile.ProfileItem;
 import com.amee.platform.science.AmountPerUnit;
 import com.amee.platform.science.StartEndDate;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class ProfileItemBuilder implements Builder {
 
     private static final String DAY_DATE = "yyyyMMdd";
     private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);
 
     private ProfileItem item;
 
     public ProfileItemBuilder(ProfileItem item) {
         this.item = item;
     }
     
     public void buildElement(JSONObject obj, boolean detailed) throws JSONException {
         obj.put("uid", item.getUid());
         obj.put("name", item.getDisplayName());
         JSONArray itemValues = new JSONArray();
         for (ItemValue itemValue : item.getItemValues()) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             itemValues.put(itemValue.getJSONObject(false));
         }
         obj.put("itemValues", itemValues);
         if (detailed) {
             obj.put("created", item.getCreated());
             obj.put("modified", item.getModified());
             obj.put("environment", item.getEnvironment().getIdentityJSONObject());
             obj.put("itemDefinition", item.getItemDefinition().getIdentityJSONObject());
             obj.put("dataCategory", item.getDataCategory().getIdentityJSONObject());
         }
     }
 
     public void buildElement(Document document, Element element, boolean detailed) {
         element.setAttribute("uid", item.getUid());
         element.appendChild(XMLUtils.getElement(document, "Name", item.getDisplayName()));
         Element itemValuesElem = document.createElement("ItemValues");
         for (ItemValue itemValue : item.getItemValues()) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             itemValuesElem.appendChild(itemValue.getElement(document, false));
         }
         element.appendChild(itemValuesElem);
         if (detailed) {
             element.setAttribute("created", item.getCreated().toString());
             element.setAttribute("modified", item.getModified().toString());
             element.appendChild(item.getEnvironment().getIdentityElement(document));
             element.appendChild(item.getItemDefinition().getIdentityElement(document));
             element.appendChild(item.getDataCategory().getIdentityElement(document));
         }
     }
 
     public JSONObject getJSONObject(boolean detailed) throws JSONException {
         JSONObject obj = new JSONObject();
         buildElement(obj, detailed);
         if (!item.isSingleFlight()) {
             obj.put("amountPerMonth", item.getAmounts().defaultValueAsAmount().convert(AmountPerUnit.MONTH).getValue());
         } else {
            obj.put("amountPerMonth", item.getAmounts().defaultValueAsDouble());
         }
         obj.put("validFrom", DAY_DATE_FMT.format(StartEndDate.getLocalStartEndDate(item.getStartDate(), TimeZoneHolder.getTimeZone())));
         obj.put("end", Boolean.toString(item.isEnd()));
         obj.put("dataItem", item.getDataItem().getIdentityJSONObject());
         if (detailed) {
             obj.put("profile", item.getProfile().getIdentityJSONObject());
         }
         return obj;
     }
 
     public Element getElement(Document document, boolean detailed) {
         Element element = document.createElement("ProfileItem");
         buildElement(document, element, detailed);
 
         if (!item.isSingleFlight()) {
             element.appendChild(XMLUtils.getElement(document, "AmountPerMonth",
                item.getAmounts().defaultValueAsAmount().convert(AmountPerUnit.MONTH).getValue() + ""));
         } else {
            element.appendChild(XMLUtils.getElement(document, "AmountPerMonth", item.getAmounts().defaultValueAsDouble() + ""));
         }
         element.appendChild(XMLUtils.getElement(document, "ValidFrom",
                 DAY_DATE_FMT.format(StartEndDate.getLocalStartEndDate(item.getStartDate(), TimeZoneHolder.getTimeZone()))));
         element.appendChild(XMLUtils.getElement(document, "End", Boolean.toString(item.isEnd())));
         element.appendChild(item.getDataItem().getIdentityElement(document));
         if (detailed) {
             element.appendChild(item.getProfile().getIdentityElement(document));
         }
         return element;
     }
 }
