 package eu.leads.crawler.utils;
 
 import com.googlecode.flaxcrawler.concurrent.Queue;
 import org.infinispan.Cache;
import org.infinispan.atomic.AtomicObjectFactory;
 import org.infinispan.manager.DefaultCacheManager;
 import org.infinispan.manager.EmbeddedCacheManager;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentMap;
 
 import static java.lang.System.getProperties;
 
 /**
  *
  * This class implements a concurrent map and a concurrent queue on top of Infinispan.
  *
  * @author otrack
  * @since 4.0
  */
 public class Infinispan {
 
     private static EmbeddedCacheManager manager;
     private static ConcurrentMap map;
     private static InfinispanQueue queue;
 
     static{
         String infinispanConfig = getProperties().getProperty("infinispanConfigFile");
         if(infinispanConfig != null){
             try {
                 manager = new DefaultCacheManager(infinispanConfig);
             } catch (IOException e) {
                 e.printStackTrace();
                 throw new RuntimeException("Incorrect Infinispan configuration file");
             }
         }else{
             manager = new DefaultCacheManager();
         }
         queue = new InfinispanQueue(manager.getCache("queue"));
         map = manager.getCache();
     }
 
     public static ConcurrentMap getOrCreatePersistentMap() {
         return map;
     }
 
     public static InfinispanQueue getOrCreateQueue(){
         return queue;
 
     }
 
 
     /**
      * This class implements a Flaxcrawler queue (com.googlecode.flaxcrawler.concurrent.Queue)
      * on top of inifinispan using the AtomicTypeFactory facility.
      */
     private static class InfinispanQueue implements Queue {
 
         private LinkedList queue;
 
         public InfinispanQueue(Cache cache){
             try {
                AtomicObjectFactory factory = new AtomicObjectFactory(cache);
                queue = (LinkedList)factory.getOrCreateInstanceOf(LinkedList.class, "0");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         public void add(Object obj) {
             queue.add(obj);
         }
 
         public void defer(Object obj) {
             queue.add(obj);
         }
 
         public Object poll() {
             return queue.poll();
         }
 
         public void dispose() {
         }
 
         public int size() {
             return queue.size();
         }
     }
 
 
 }
