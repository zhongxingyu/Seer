 package org.vsegda.service;
 
 import net.sf.jsr107cache.Cache;
 import net.sf.jsr107cache.CacheException;
 import net.sf.jsr107cache.CacheFactory;
 import net.sf.jsr107cache.CacheManager;
 import org.vsegda.data.DataItem;
 import org.vsegda.data.DataStream;
 import org.vsegda.shared.DataStreamMode;
 import org.vsegda.storage.DataArchiveStorage;
 import org.vsegda.storage.DataItemStorage;
 import org.vsegda.util.TimeInstant;
 import org.vsegda.util.TimePeriod;
 import org.vsegda.util.TimePeriodUnit;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Roman Elizarov
  */
 public class DataItemService {
     public static final TimePeriod DEFAULT_SPAN = TimePeriod.valueOf(1, TimePeriodUnit.WEEK);
     public static final int DEFAULT_N = 2500;
 
     private static final int MAX_CACHED_LIST_SIZE = (int)(1.5 * DEFAULT_N);
     private static final Cache LIST_CACHE;
 
     static {
         try {
             CacheFactory cf = CacheManager.getInstance().getCacheFactory();
             LIST_CACHE = cf.createCache(Collections.emptyMap());
         } catch (CacheException e) {
             throw new ExceptionInInitializerError(e);
         }
     }
 
     private DataItemService() {} // do not create
 
     public static void addDataItem(DataItem dataItem) {
         DataItemStorage.storeDataItem(dataItem);
         ListEntry entry = (ListEntry) LIST_CACHE.get(dataItem.getStreamId());
         if (entry != null) {
             entry.items.add(dataItem);
             int size = entry.items.size();
             if (size >= 2 && DataItem.ORDER_BY_TIME.compare(entry.items.get(size - 1), entry.items.get(size - 2)) < 0)
                 Collections.sort(entry.items, DataItem.ORDER_BY_TIME);
             if (size > MAX_CACHED_LIST_SIZE)
                 entry.items.subList(0, size - DEFAULT_N).clear();
             LIST_CACHE.put(dataItem.getStreamId(), entry);
         }
     }
 
     public static void addDataItems(List<DataItem> items) {
         for (DataItem item : items)
             addDataItem(item);
     }
 
     public static void removeDataItems(DataStream stream, List<DataItem> items) {
         long lastTime = 0;
         for (DataItem item : items) {
             if (item.getStreamId() != stream.getStreamId())
                 throw new IllegalArgumentException();
             DataItemStorage.deleteDataItem(item);
             lastTime = Math.max(lastTime, item.getTimeMillis());
         }
         // check if cache is still correct
         ListEntry entry = (ListEntry) LIST_CACHE.get(stream.getStreamId());
         if (entry != null && !entry.items.isEmpty() && entry.items.get(0).getTimeMillis() <= lastTime)
             // just kill cache completely for the stream in this case
             LIST_CACHE.remove(stream.getStreamId());
     }
 
     public static void refreshCache(long streamId) {
         performItemsQuery(streamId, null, null, DEFAULT_N, true);
     }
 
     public static DataItem getFirstDataItem(DataStream stream) {
         DataItem item = DataItemStorage.loadFirstDataItem(stream.getStreamId());
         if (item != null)
             item.setStream(stream);
         return item;
     }
 
     public static DataItem getLastDataItem(DataStream stream) {
         List<DataItem> items = getDataItems(stream, null, null, 1);
         return items.isEmpty() ? new DataItem(stream, Double.NaN, 0) : items.get(0);
     }
 
     public static List<DataItem> getDataItems(DataStream stream, TimeInstant from, TimeInstant to, int n) {
         if (stream.getMode() == DataStreamMode.LAST) {
             if (to != null)
                 return Collections.emptyList();
             n = 1;
         }
         // try cache
         ListEntry entry = (ListEntry) LIST_CACHE.get(stream.getStreamId());
         List<DataItem> items = null;
         if (entry != null) {
             int size = entry.items.size();
             int fromIndex = 0;
             int toIndex = size;
             if (from != null) {
                 while (fromIndex < size && entry.items.get(fromIndex).getTimeMillis() < from.time())
                     fromIndex++;
             }
             if (to != null) {
                 while (toIndex > fromIndex && entry.items.get(toIndex - 1).getTimeMillis() >= to.time())
                     toIndex--;
             }
             long fromTime = from == null ? 0 : from.time();
             if (entry.fromTime <= fromTime || fromIndex > 0 || toIndex - fromIndex >= n) {
                 // can safely return from cache
                 items = entry.items.subList(Math.max(fromIndex, toIndex - n), toIndex);
             }
         }
         // perform query if not found in cache
         if (items == null)
             items = performItemsQuery(stream.getStreamId(), from, to, n, false);
         return fillStream(stream, items);
     }
 
     private static List<DataItem> performItemsQuery(long streamId, TimeInstant from, TimeInstant to, int n, boolean forceCacheUpdate) {
         // query both recent items and archive
         List<DataItem> items = new ArrayList<DataItem>();
         items.addAll(DataItemStorage.queryDataItems(streamId, from, to, n));
         if (items.size() < n)
            items.addAll(DataArchiveStorage.queryItemsFromDataArchives(streamId, from, to, items.size() - n));
         Collections.sort(items, DataItem.ORDER_BY_TIME);
        if (items.size()  > n)
             items.subList(0, items.size() - n).clear(); // remove extra items
         // update cache if needed (only when querying up to now)
         if (to == null) {
             long fromTime = from == null ? (items.size() < n ? 0 : items.get(0).getTimeMillis()) : from.time();
             ListEntry oldCacheEntry = (ListEntry) LIST_CACHE.get(streamId);
             if (forceCacheUpdate || oldCacheEntry == null ||
                     oldCacheEntry.items.size() <= items.size() ||
                     oldCacheEntry.fromTime >= fromTime)
             {
                 List<DataItem> cacheItems = items.size() <= MAX_CACHED_LIST_SIZE ? items :
                         new ArrayList<DataItem>(items.subList(items.size() - MAX_CACHED_LIST_SIZE, items.size()));
                 LIST_CACHE.put(streamId, new ListEntry(fromTime, cacheItems));
             }
         }
         return items;
     }
 
     private static List<DataItem> fillStream(DataStream stream, List<DataItem> items) {
         for (DataItem item : items)
             item.setStream(stream);
         return items;
     }
 
     private static class ListEntry implements Serializable {
         private static final long serialVersionUID = 4488592054732566662L;
 
         long fromTime;
         List<DataItem> items;
 
         ListEntry(long fromTime, List<DataItem> items) {
             this.fromTime = fromTime;
             this.items = items;
         }
     }
 }
