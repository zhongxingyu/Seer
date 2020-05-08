 package org.hptd.io;
 
 import com.google.common.base.Function;
 import com.google.common.cache.*;
 import org.hptd.utils.HptdUtil;
 import org.joda.time.DateTime;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 
 /**
  * cache the leveldb io instance
  *
  * @author ford
  */
 public class LeveldbCache {
     private static Cache<String, LevelDbIo> cache = CacheBuilder.newBuilder().maximumSize(4).concurrencyLevel(1)
             .removalListener(new RemovalListener<String, LevelDbIo>() {
                 @Override
                 public void onRemoval(RemovalNotification<String, LevelDbIo> objectObjectRemovalNotification) {
                     objectObjectRemovalNotification.getValue().close();
                 }
             }).build();
 
     public static LevelDbIo getLeveldb(final String dbName, final Function<Long, Long> integralFunction) throws ExecutionException {
         return cache.get(dbName, new Callable<LevelDbIo>() {
             @Override
             public LevelDbIo call() throws Exception {
                 return new LevelDbIo(HptdUtil.getRootPath() + "/" + dbName, integralFunction);
             }
         });
     }
 
     public static LevelDbIo getRawLeveldb(final String dbName) throws ExecutionException {
         return getLeveldb(dbName, new Function<Long, Long>() {
             @Override
             public Long apply(Long datetime) {
                 DateTime dt = new DateTime(datetime);
                 return new DateTime(dt.getYear(), dt.getMonthOfYear(),
                         dt.getDayOfMonth(), dt.getHourOfDay(), 0, 0, 0).getMillis();
             }
         });
     }
 
     public static LevelDbIo getStatHourLeveldb(final String dbName) throws ExecutionException {
         return getLeveldb(dbName, new Function<Long, Long>() {
             @Override
             public Long apply(Long datetime) {
                 DateTime dt = new DateTime(datetime);
                 return new DateTime(dt.getYear(), dt.getMonthOfYear(),
                         1, 0, 0, 0, 0).getMillis();
             }
         });
     }
 
     public static LevelDbIo getStatDayLeveldb(final String dbName) throws ExecutionException {
         return getLeveldb(dbName, new Function<Long, Long>() {
             @Override
             public Long apply(Long datetime) {
                 DateTime dt = new DateTime(datetime);
                 return new DateTime(dt.getYear(), 1,
                         1, 0, 0, 0, 0).getMillis();
             }
         });
     }
 
     public static void destroy() {
         cache.cleanUp();
     }
 
 }
