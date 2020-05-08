 package com.amee.domain.data;
 
 import com.amee.domain.item.BaseItemValue;
 import com.amee.platform.science.ExternalHistoryValue;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 
 /**
  * A Map of {@link BaseItemValue} instances.
  * <p/>
  * The keys will be the {@link BaseItemValue} paths. The entries will be a SortedSet of {@link BaseItemValue} instances. The Set will
  * consist of a single entry for single-valued {@link BaseItemValue} histories and a set of {@link BaseItemValue}s sorted in
  * descending order (most recent first) for multi-valued histories. The non-historical value is the last item in the set.
  * <p/>
  * Note: This class has a natural ordering that is inconsistent with equals.
  */
 public class ItemValueMap {
 
     Log log = LogFactory.getLog(getClass());
 
     private Map<String, SortedSet<BaseItemValue>> map = new HashMap<String, SortedSet<BaseItemValue>>();
 
     /**
      * Get the head {@link BaseItemValue} in the historical sequence (the earliest value).
      *
      * @param path - the {@link BaseItemValue} path.
      * @return the head (earliest) {@link BaseItemValue} in the historical sequence.
      */
     public BaseItemValue get(String path) {
         BaseItemValue itemValue = null;
         SortedSet<BaseItemValue> series = map.get(path);
         if (series != null) {
             itemValue = series.last();
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
         List<BaseItemValue> itemValues = new ArrayList<BaseItemValue>();
         for (String path : map.keySet()) {
             BaseItemValue itemValue = get(path, startDate);
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
         SortedSet<BaseItemValue> series = map.get(path);
         if (series != null) {
             itemValue = find(series, startDate);
         }
         return itemValue;
     }
 
     public void put(String path, BaseItemValue itemValue) {
         // Create TreeSet if it does not exist for this path.
         if (!map.containsKey(path)) {
             map.put(path, new TreeSet<BaseItemValue>(
                     Collections.reverseOrder(
                             new Comparator<BaseItemValue>() {
                                 public int compare(BaseItemValue iv1, BaseItemValue iv2) {
                                     if (isHistoricValue(iv1) && isHistoricValue(iv2)) {
                                         // Both BaseItemValue are part of a history, compare their startDates.
                                         return ((ExternalHistoryValue) iv1).getStartDate().compareTo(((ExternalHistoryValue) iv2).getStartDate());
                                     } else if (isHistoricValue(iv1)) {
                                         // The first BaseItemValue is historical, but the second is not, so it needs to
                                         // come after the second BaseItemValue.
                                         return 1;
                                     } else if (isHistoricValue(iv2)) {
                                         // The second BaseItemValue is historical, but the first is not, so it needs to
                                         // come after the first BaseItemValue.
                                         return -1;
                                     } else {
                                         // Both BaseItemValues are not historical. This should not happen but consider them equal.
                                         // The new BaseItemValue will not be added to the TreeSet (see class note about inconsistency with equals).
                                         log.warn("put() Two non-historical BaseItemValues with the same path should not exist.");
                                         return 0;
                                     }
                                 }
                             })));
         }
         // Add itemValue to the TreeSet for this path.
         SortedSet<BaseItemValue> itemValues = map.get(path);
         itemValues.add(itemValue);
     }
 
     /**
      * Get all instances of {@link BaseItemValue} with the passed path.
      *
      * @param path - the {@link BaseItemValue} path.
      * @return the List of {@link BaseItemValue}. Will be empty is there exists no {@link BaseItemValue}s with this path.
      */
     public List<BaseItemValue> getAll(String path) {
         SortedSet<BaseItemValue> series = map.get(path);
         return series != null ? new ArrayList<BaseItemValue>(series) : new ArrayList<BaseItemValue>();
     }
 
     /**
      * Find the active BaseItemValue at startDate. The active BaseItemValue is the one occurring at or
      * immediately before startDate.
      *
      * @param itemValues the item values sorted by startDate, most recent first (descending).
     * @param startDate - the active {@link BaseItemValue} will be that starting immediately prior-to or on this date.
      * @return the discovered BaseItemValue, or null if not found
      */
     private static BaseItemValue find(SortedSet<BaseItemValue> itemValues, Date startDate) {
         // Default to the current date.
         if (startDate == null) {
             startDate = new Date();
         }
         // Find active BaseItemValue.
         BaseItemValue selected = null;
         for (BaseItemValue itemValue : itemValues) {
             if (isHistoricValue(itemValue)) {
                 if (!((ExternalHistoryValue) itemValue).getStartDate().after(startDate)) {
                     selected = itemValue;
                     break;
                 }
             } else {
                 // No historical values match so use the non-historical value.
                 selected = itemValue;
             }
         }
        if (selected != null) {
            selected.setHistoryAvailable(itemValues.size() > 1);
        }
         return selected;
     }
 
     private static boolean isHistoricValue(BaseItemValue itemValue) {
         return ExternalHistoryValue.class.isAssignableFrom(itemValue.getClass());
     }
 
     public Set<String> keySet() {
         return map.keySet();
     }
 }
