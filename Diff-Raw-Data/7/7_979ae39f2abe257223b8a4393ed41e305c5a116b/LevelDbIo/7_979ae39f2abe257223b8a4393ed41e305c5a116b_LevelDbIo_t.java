 package org.hptd.io;
 
 import com.google.common.base.Function;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.primitives.Longs;
 
 import org.hptd.cache.NameCache;
 import org.hptd.format.ChunkData;
 import org.hptd.utils.ByteBufferUtil;
 
 import org.iq80.leveldb.DB;
 import org.iq80.leveldb.DBIterator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 /**
  * use the leveldb api to operate the hptd data.
  *
  * @author ford
  */
 public class LevelDbIo {
     private static final Logger logger = LoggerFactory.getLogger(LevelDbIo.class);
     private final DB levelDb;
     //key is innerId,value is map with minimal integral time and minimal datetime
     private LoadingCache<Long, SortedMap<Long, Long>> timeRangeCache = null;
     private final Function<Long, Long> integralFunction;
 
 
     protected LevelDbIo(String dbName, final Function<Long, Long> integralFunction) throws IOException {
         this.levelDb = LeveldbFactoryAdapter.getDb(dbName);
         this.integralFunction = integralFunction;
         timeRangeCache = CacheBuilder.newBuilder().maximumSize(NameCache.getNameCacheCount()).build(
                 new CacheLoader<Long, SortedMap<Long, Long>>() {
                     @Override
                     public SortedMap<Long, Long> load(Long innerId) throws Exception {
                         byte[] bytes = levelDb.get(Longs.toByteArray(innerId));
                         SortedMap<Long, Long> timeMap = new TreeMap<Long, Long>();
 
                         if (bytes == null) return timeMap;
                         ByteBuffer buffer = ByteBufferUtil.bigEndianWrap(bytes);
                         while (buffer.hasRemaining()) {
                             checkTimeSet(timeMap, buffer.getLong());
                         }
                         return timeMap;
                     }
                 });
     }
 
     private boolean checkTimeSet(SortedMap<Long, Long> timeMap, Long datetime) {
         Long integralTime = integralFunction.apply(datetime);
         Long minTime = timeMap.get(integralTime);
         if (minTime == null || minTime > datetime) {
             timeMap.put(integralTime, datetime);
             return true;
         } else {
             return false;
         }
     }
 
     public void put(HptdKey key, ChunkData data) {
         try {
             Long innerId = key.getInnerId();
             SortedMap<Long, Long> timeMap = timeRangeCache.get(innerId);
             if (checkTimeSet(timeMap, key.getDatetime())) {
                 ByteBuffer buffer = ByteBufferUtil.bigEndianAllocate(timeMap.size() * 8);
                 for (Long datetime : timeMap.values()) {
                     buffer.putLong(datetime);
                 }
                 levelDb.put(Longs.toByteArray(innerId), buffer.array());
             }
         } catch (ExecutionException e) {
             logger.error("get range error,with innerId:" + key.getInnerId(), e);
         }
         levelDb.put(key.toBytes(), data.toBuffer().array());
     }
 
     public ChunkData get(long innerId, long datetime) {
         byte[] bytes = levelDb.get(HptdKey.getBytes(innerId, datetime));
         return getChunkData(datetime, bytes);
     }
 
     private ChunkData getChunkData(long datetime, byte[] bytes) {
         if (bytes == null) return null;
         ChunkData data = ChunkData.valueOf(ByteBufferUtil.bigEndianWrap(bytes));
         data.setDatetime(datetime);
         return data;
     }
 
     public ChunkData get(HptdKey key) {
         byte[] bytes = levelDb.get(key.toBytes());
         return getChunkData(key.getDatetime(), bytes);
     }
 
     public List<ChunkData> getRange(long hptdId, long startTime, long endTime) {
         long datetime = startTime;
         if (startTime > endTime) {
             startTime = endTime;
             endTime = datetime;
         }
         List<ChunkData> datas = new ArrayList<ChunkData>();
         Long newStart = null;
         try {
             SortedMap<Long, Long> timeMap = timeRangeCache.get(hptdId);
             newStart = timeMap.get(integralFunction.apply(startTime));
             if (newStart == null && timeMap.size() > 0) {
                 newStart = timeMap.get(timeMap.firstKey());
             }
         } catch (ExecutionException e) {
             logger.error("get datetime error with innerId:" + hptdId);
         }
         if (newStart == null) return datas;
         HptdKey start = new HptdKey(hptdId, newStart);
         DBIterator it = levelDb.iterator();
         try {
             it.seek(start.toBytes());
             while (it.hasNext()) {
                 Map.Entry<byte[], byte[]> entry = it.next();
                 ByteBuffer buffer = ByteBufferUtil.bigEndianWrap(entry.getKey());
                 if (buffer.capacity() < 16) continue;
                 long innerId = buffer.getLong();
                 datetime = buffer.getLong();
                if (datetime < startTime) continue;
                 if (innerId == hptdId && datetime <= endTime) {
                     ChunkData data = ChunkData.valueOf(ByteBufferUtil.bigEndianWrap(entry.getValue()));
                     data.setDatetime(datetime);
                     datas.add(data);
                 } else if (datetime > endTime) {
                     break;
                 }
             }
         } finally {
             if (it != null) {
                 try {
                     it.close();
                 } catch (IOException e) {
                     logger.error("close iterator error for hptdId:" + hptdId, e);
                 }
             }
         }
         return datas;
     }
 
     protected void close() {
         if (levelDb != null) {
             try {
                 levelDb.close();
             } catch (IOException e) {
                 logger.error("close leveldb error,with leveldb:" + levelDb, e);
             }
         }
     }
 }
