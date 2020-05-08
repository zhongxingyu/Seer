 package com.amee.calculation.service;
 
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.profile.Profile;
 import com.amee.domain.profile.ProfileItem;
 import com.amee.platform.science.ReturnValue;
 import com.amee.platform.science.StartEndDate;
 import com.amee.service.profile.ProfileService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import javax.measure.Measure;
 import javax.measure.unit.SI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A ProfileService which prorates amounts belonging to the {@link com.amee.domain.profile.ProfileItem ProfileItem} instances
  * that are returned by the delegated ProfileService.
  * <p/>
  * <p/>
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
  * <p/>
  * Website http://www.amee.cc
  */
 @Service
 public class ProRataProfileService {
 
     private Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private ProfileService profileService;
 
     @Autowired
     private CalculationService calculationService;
 
     @SuppressWarnings(value = "unchecked")
     public List<ProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate) {
 
         if (log.isDebugEnabled()) {
             log.debug("getProfileItems() start");
         }
 
         List<ProfileItem> requestedItems = new ArrayList<ProfileItem>();
         Interval requestInterval = getInterval(startDate, endDate);
 
         for (ProfileItem pi : profileService.getProfileItems(profile, dataCategory, startDate, endDate)) {
 
             // Update ProfileItem with start and end dates.
             pi.setEffectiveStartDate(startDate);
             pi.setEffectiveEndDate(endDate);
 
             if (log.isDebugEnabled()) {
                 log.debug("getProfileItems() - ProfileItem: " + pi.getName() + " has un-prorated Amounts: " + pi.getAmounts());
             }
 
             Interval intersect = requestInterval;
 
             // Find the intersection of the profile item with the requested window.
             if (intersect.getStart().toDate().before(pi.getStartDate())) {
                 intersect = intersect.withStartMillis(pi.getStartDate().getTime());
             }
 
             if ((pi.getEndDate() != null) && pi.getEndDate().before(intersect.getEnd().toDate())) {
                 intersect = intersect.withEndMillis(pi.getEndDate().getTime());
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("getProfileItems() - request interval: " + requestInterval + ", intersect:" + intersect);
             }
 
             if (pi.hasNonZeroPerTimeValues()) {
 
                 // The ProfileItem has perTime ItemValues. In this case, the ItemValues are multiplied by
                 // the (intersect/PerTime) ratio and the CO2 value recalculated.
 
                 log.debug("getProfileItems() - ProfileItem: " + pi.getName() + " has PerTime ItemValues.");
 
                 for (ItemValue iv : pi.getItemValues()) {
                     if (iv.hasPerTimeUnit() && iv.getItemValueDefinition().isFromProfile() && iv.getValue().length() > 0) {
                        iv.setValueOverride(getProRatedItemValue(intersect, iv));
 
                         if (log.isDebugEnabled()) {
                             log.debug("getProfileItems() - ProfileItem: " + pi.getName() +
                                     ". ItemValue: " + iv.getName() + " = " + iv.getValue() + " has PerUnit: " + iv.getPerUnit() +
                                     ". Pro-rated ItemValue = " + iv.getValue());
                         }
 
                     } else {
                         log.debug("getProfileItems() - ProfileItem: " + pi.getName() + ". Unchanged ItemValue: " + iv.getName());
                     }
                 }
 
                 calculationService.calculate(pi);
 
                 if (log.isDebugEnabled()) {
                     log.debug("getProfileItems() - ProfileItem: " + pi.getName() + ". Adding prorated Amounts: " + pi.getAmounts());
                 }
 
                 requestedItems.add(pi);
 
             } else if (pi.getEndDate() != null) {
                 // The ProfileItem has no perTime ItemValues and is bounded. In this case, the CO2 value is multiplied by
                 // the (intersection/item duration) ratio.
 
                 //TODO - make Item a deep copy (and so inc. ItemValues). Will need to implement equals() in ItemValue
                 //TODO - such that overwriting in the ItemValue collection is handled correctly.
 
                 long event = getIntervalInMillis(pi.getStartDate(), pi.getEndDate());
                 double eventIntersectRatio = intersect.toDurationMillis() / (double) event;
 
                 // For each amount, store the prorated value
                 for (Map.Entry<String, ReturnValue> entry : pi.getAmounts().getReturnValues().entrySet()) {
                     ReturnValue value = entry.getValue();
                     double proRatedValue = entry.getValue().toDouble() * eventIntersectRatio;
                     pi.getAmounts().putValue(value.getType(), value.getUnit(), value.getPerUnit(), proRatedValue);
                 }
 
                 if (log.isDebugEnabled()) {
                     log.debug("getProfileItems() - ProfileItem: " + pi.getName() +
                             " is bounded (" + getInterval(pi.getStartDate(), pi.getEndDate()) +
                             ") and has no PerTime ItemValues.");
                     log.debug("getProfileItems() - Adding pro-rated Amounts: " + pi.getAmounts());
                 }
                 requestedItems.add(pi);
 
             } else {
                 // The ProfileItem has no perTime ItemValues and is unbounded. In this case, the ReturnValues are not prorated.
                 if (log.isDebugEnabled()) {
                     log.debug("getProfileItems() - ProfileItem: " + pi.getName() +
                             " is unbounded and has no PerTime ItemValues. Adding un-prorated Amounts: " + pi.getAmounts());
                 }
                 requestedItems.add(pi);
             }
 
         }
 
         if (log.isDebugEnabled()) {
             log.debug("getProfileItems() done (" + requestedItems.size() + ")");
         }
 
         return requestedItems;
     }
 
     @SuppressWarnings(value = "unchecked")
     private ItemValue getProRatedItemValue(Interval interval, ItemValue itemValue) {
         Measure measure = Measure.valueOf(1, itemValue.getPerUnit().toUnit());
         double perTime = measure.doubleValue(SI.MILLI(SI.SECOND));
         double intersectPerTimeRatio = (interval.toDurationMillis()) / perTime;
         double value = Double.parseDouble(itemValue.getValue());
         value = value * intersectPerTimeRatio;
         itemValue.setValue(Double.toString(value));
         return itemValue;
     }
 
     private Interval getInterval(Date startDate, Date endDate) {
         DateTime start = new DateTime(startDate.getTime());
         DateTime end = new DateTime(endDate.getTime());
         return new Interval(start, end);
     }
 
     private long getIntervalInMillis(Date startDate, Date endDate) {
         return getInterval(startDate, endDate).toDurationMillis();
     }
 }
