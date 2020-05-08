 package com.amee.domain.data;
 
 import com.amee.domain.item.BaseItemValue;
 import com.amee.platform.science.ExternalHistoryValue;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.persistence.Transient;
 import java.util.*;
 
 /**
  * A Map of {@link BaseItemValue} instances.
  * <p/>
  * The keys will be the {@link BaseItemValue} paths. The entries will be a Set of {@link BaseItemValue} instances. The Set will
  * consist of a single entry for single-valued {@link BaseItemValue} histories.
  */
 @SuppressWarnings("unchecked")
 public class NuItemValueMap extends HashMap {
 
     Log log = LogFactory.getLog(getClass());
 
     @Transient
     private transient ItemValueMap adapter;
 
     /**
      * Get the head {@link BaseItemValue} in the historical sequence.
      *
      * @param path - the {@link BaseItemValue} path.
      * @return the head {@link BaseItemValue} in the historical sequence.
      */
     public BaseItemValue get(String path) {
         BaseItemValue itemValue = null;
         TreeSet<BaseItemValue> series = (TreeSet<BaseItemValue>) super.get(path);
         if (series != null) {
             itemValue = series.first();
         }
         return itemValue;
     }
 
     /**
      * Get the list of active {@link BaseItemValue}s at the passed start Date.
      *
      * @param startDate - the active {@link BaseItemValue} will be that starting immediately prior-to or on this date.
      * @return the set of active {@link BaseItemValue}s at the passed start Date.
      */
     public List<BaseItemValue> getAll(Date startDate) {
         List<BaseItemValue> itemValues = new ArrayList();
         for (Object path : super.keySet()) {
             BaseItemValue itemValue = get((String) path, startDate);
             if (itemValue != null) {
                 itemValues.add(itemValue);
             } else {
                 log.warn("getAll() Got null BaseItemValue: path=" + path + ", startDate=" + startDate);
             }
         }
         return itemValues;
     }
 
     /**
      * Get the active {@link BaseItemValue} at the passed start Date.
      *
      * @param path      - the {@link BaseItemValue} path.
      * @param startDate - the active {@link BaseItemValue} will be that starting immediately prior-to or on this date.
      * @return the active {@link BaseItemValue} at the passed start Date.
      */
     public BaseItemValue get(String path, Date startDate) {
         BaseItemValue itemValue = null;
         Set<BaseItemValue> series = (TreeSet<BaseItemValue>) super.get(path);
         if (series != null) {
             itemValue = find(series, startDate);
         }
         return itemValue;
     }
 
     public void put(String path, BaseItemValue itemValue) {
         // Create TreeSet if it does not exist for this path.
         if (!containsKey(path)) {
             super.put(path, new TreeSet<BaseItemValue>(new Comparator<BaseItemValue>() {
                 public int compare(BaseItemValue iv1, BaseItemValue iv2) {
                     if (ExternalHistoryValue.class.isAssignableFrom(iv1.getClass()) &&
                             ExternalHistoryValue.class.isAssignableFrom(iv2.getClass())) {
                         // Both BaseItemValue are part of a history, compare their startDates.
                         return ((ExternalHistoryValue) iv1).getStartDate().compareTo(((ExternalHistoryValue) iv2).getStartDate());
                     } else if (ExternalHistoryValue.class.isAssignableFrom(iv1.getClass())) {
                         // The first BaseItemValue is historical, but the second is not, so it needs to
                         // come after the second BaseItemValue.
                        return 1;
                     } else if (ExternalHistoryValue.class.isAssignableFrom(iv2.getClass())) {
                         // The second BaseItemValue is historical, but the first is not, so it needs to
                         // come after the first BaseItemValue.
                        return -1;
                     } else {
                         // Both BaseItemValue are not historical. This should not happen but consider them equal.
                         log.warn("put() Two non-historical BaseItemValues with the same path should not exist.");
                         return 0;
                     }
                 }
             }));
         }
         // Add itemValue to the TreeSet for this path.
         Set<BaseItemValue> itemValues = (Set<BaseItemValue>) super.get(path);
         itemValues.add(itemValue);
     }
 
     /**
      * Get all instances of {@link BaseItemValue} with the passed path.
      *
      * @param path - the {@link BaseItemValue} path.
      * @return the List of {@link BaseItemValue}. Will be empty is there exists no {@link BaseItemValue}s with this path.
      */
     public List<BaseItemValue> getAll(String path) {
         Object o = super.get(path);
         return o != null ? new ArrayList((TreeSet<BaseItemValue>)o) : new ArrayList();
     }
 
     /**
      * Find the active BaseItemValue at startDate. The active BaseItemValue is the one occurring at or
      * immediately before startDate.
      *
      * @param itemValues
      * @param startDate
      * @return the discovered BaseItemValue, or null if not found
      */
     private static BaseItemValue find(Set<BaseItemValue> itemValues, Date startDate) {
         BaseItemValue selected = null;
         for (BaseItemValue itemValue : itemValues) {
             if (ExternalHistoryValue.class.isAssignableFrom(itemValue.getClass())) {
                 if (!((ExternalHistoryValue) itemValue).getStartDate().after(startDate)) {
                     selected = itemValue;
                     // TODO: Enable this.
                     // selected.setHistoryAvailable(itemValues.size() > 1);
                     // break;
                     throw new UnsupportedOperationException();
                 }
             } else {
                 // Non-historical values always come first.
                 selected = itemValue;
             }
         }
         return selected;
     }
 
     public ItemValueMap getAdapter() {
         return adapter;
     }
 
     public void setAdapter(ItemValueMap adapter) {
         this.adapter = adapter;
     }
 }
