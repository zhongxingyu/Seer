 package jbu.cache;
 
 import jbu.offheap.Allocator;
 import jbu.serializer.unsafe.UnsafePrimitiveBeanSerializer;
 import jbu.testobject.LotOfPrimitiveAndArrayAndString;
 import jbu.testobject.LotOfString;
 import jbu.testobject.LotOfWrapper;
 import jbu.testobject.ObjectWithArrayList;
 import org.junit.Test;
 
 import java.util.Arrays;
 
 public class BenchCache {
 
     @Test
     public void bench_put_get() {
 
         // Put n object in map
         // Get them all
         // Remove them
         // etc...
        int NB_OBJ = 100000;
 
         long putTime = 0;
         long put = 0;
         long getTime = 0;
         long get = 0;
 
        Allocator allocator = new Allocator(1024 * 1024 * 1024);
         Cache<Integer, LotOfPrimitiveAndArrayAndString> cache = new Cache<Integer, LotOfPrimitiveAndArrayAndString>("testCache", allocator, new UnsafePrimitiveBeanSerializer());
         LotOfPrimitiveAndArrayAndString cachedObject = new LotOfPrimitiveAndArrayAndString();
         int estimSize = new UnsafePrimitiveBeanSerializer().calculateSerializedSize(cachedObject);
        long objectSizeInMemory = estimSize * get / 1024 / 1024;
        System.out.println("Object size  : " + estimSize);
         System.out.println("Store : " + NB_OBJ);
         for (int j = 0; j < 100; j++) {
             long start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.put(i, cachedObject);
             }
             putTime += System.nanoTime() - start;
             put += NB_OBJ;
 
             start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.get(i);
             }
             getTime += System.nanoTime() - start;
             get += NB_OBJ;
 
 
             System.out.println("Iteration : " + j);
             double getTimeSecond = getTime / (double) (1000 * 1000 * 1000);
             double putTimeSecond = putTime / (double) (1000 * 1000 * 1000);
 
 
             System.out.println("Real object size : " + objectSizeInMemory + " MB");
             System.out.println("Memory allocated : " + allocator.getAllocatedMemory() / 1024 / 1024 + " MB");
             System.out.println("Memory used : " + allocator.getUsedMemory() / 1024 / 1024 + " MB");
             System.out.println("Puts : " + put / putTimeSecond + " object/s");
             System.out.println("Gets : " + get / getTimeSecond + " object/s");
             System.out.println("Puts : " + (put * estimSize / 1024 / 1024) / putTimeSecond + " MB/s");
             System.out.println("Gets : " + (get * estimSize / 1024 / 1024) / getTimeSecond + " MB/s");
             System.out.println("");
             System.out.println("");
             cache.clean();
         }
 
 
     }
 
     @Test
     public void bench_put_get_wrapper() {
 
         // Put n object in map
         // Get them all
         // Remove them
         // etc...
         int NB_OBJ = 10000;
 
         long putTime = 0;
         long put = 0;
         long getTime = 0;
         long get = 0;
 
         Allocator allocator = new Allocator(1024 * 1024 * 1024);
         Cache<Integer, LotOfWrapper> cache = new Cache<>("testCache", allocator, new UnsafePrimitiveBeanSerializer());
         LotOfWrapper cachedObject = new LotOfWrapper();
         int estimSize = new UnsafePrimitiveBeanSerializer().calculateSerializedSize(cachedObject);
         long objectSizeInMemory = estimSize * get / 1024 / 1024;
         System.out.println("Object size  : " + estimSize);
         System.out.println("Store : " + NB_OBJ);
         for (int j = 0; j < 10000; j++) {
             long start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.put(i, cachedObject);
             }
             putTime += System.nanoTime() - start;
             put += NB_OBJ;
 
             start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.get(i);
             }
             getTime += System.nanoTime() - start;
             get += NB_OBJ;
 
 
             System.out.println("Iteration : " + j);
             double getTimeSecond = getTime / (double) (1000 * 1000 * 1000);
             double putTimeSecond = putTime / (double) (1000 * 1000 * 1000);
 
 
             System.out.println("Real object size : " + objectSizeInMemory + " MB");
             System.out.println("Memory allocated : " + allocator.getAllocatedMemory() / 1024 / 1024 + " MB");
             System.out.println("Memory used : " + allocator.getUsedMemory() / 1024 / 1024 + " MB");
             System.out.println("Puts : " + put / putTimeSecond + " object/s");
             System.out.println("Gets : " + get / getTimeSecond + " object/s");
             System.out.println("Puts : " + (put * estimSize / 1024 / 1024) / putTimeSecond + " MB/s");
             System.out.println("Gets : " + (get * estimSize / 1024 / 1024) / getTimeSecond + " MB/s");
             System.out.println("");
             System.out.println("");
             cache.clean();
         }
 
 
     }
 
 
     @Test
     public void bench_put_get_string() {
         // Put n object in map
         // Get them all
         // Remove them
         // etc...
         int NB_OBJ = 100000;
 
         long putTime = 0;
         long put = 0;
         long getTime = 0;
         long get = 0;
 
         Allocator allocator = new Allocator(1024 * 1024 * 1024);
         Cache<Integer, LotOfString> cache = new Cache<Integer, LotOfString>("testCache", allocator, new UnsafePrimitiveBeanSerializer());
         LotOfString cachedObject = new LotOfString();
         int estimSize = new UnsafePrimitiveBeanSerializer().calculateSerializedSize(cachedObject);
         long objectSizeInMemory = estimSize * get / 1024 / 1024;
         System.out.println("Object size  : " + estimSize);
         System.out.println("Store : " + NB_OBJ);
         for (int j = 0; j < 100; j++) {
             long start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.put(i, cachedObject);
             }
             putTime += System.nanoTime() - start;
             put += NB_OBJ;
 
             start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.get(i);
             }
             getTime += System.nanoTime() - start;
             get += NB_OBJ;
 
 
             System.out.println("Iteration : " + j);
             double getTimeSecond = getTime / (double) (1000 * 1000 * 1000);
             double putTimeSecond = putTime / (double) (1000 * 1000 * 1000);
 
 
             System.out.println("Real object size : " + objectSizeInMemory + " MB");
             System.out.println("Memory allocated : " + allocator.getAllocatedMemory() / 1024 / 1024 + " MB");
             System.out.println("Memory used : " + allocator.getUsedMemory() / 1024 / 1024 + " MB");
             System.out.println("Puts : " + put / putTimeSecond + " object/s");
             System.out.println("Gets : " + get / getTimeSecond + " object/s");
             System.out.println("Puts : " + (put * estimSize / 1024 / 1024) / putTimeSecond + " MB/s");
             System.out.println("Gets : " + (get * estimSize / 1024 / 1024) / getTimeSecond + " MB/s");
             System.out.println("");
             System.out.println("");
             cache.clean();
         }
 
 
     }
 
 
     @Test
     public void bench_put_get_collection() {
         // Put n object in map
         // Get them all
         // Remove them
         // etc...
         int NB_OBJ = 100000;
 
         long putTime = 0;
         long put = 0;
         long getTime = 0;
         long get = 0;
 
         Allocator allocator = new Allocator(1024 * 1024 * 1024);
         Cache<Integer, ObjectWithArrayList> cache = new Cache<Integer, ObjectWithArrayList>("testCache", allocator, new UnsafePrimitiveBeanSerializer());
         ObjectWithArrayList cachedObject = new ObjectWithArrayList();
         cachedObject.collection = Arrays.asList(new String[]{"azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop",
                 "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop",
                 "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop",
                 "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop",
                 "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop", "azertyuiop"});
         int estimSize = new UnsafePrimitiveBeanSerializer().calculateSerializedSize(cachedObject);
         long objectSizeInMemory = estimSize * get / 1024 / 1024;
         System.out.println("Object size  : " + estimSize);
         System.out.println("Store : " + NB_OBJ);
         for (int j = 0; j < 1000; j++) {
             long start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.put(i, cachedObject);
             }
             putTime += System.nanoTime() - start;
             put += NB_OBJ;
 
             start = System.nanoTime();
             for (int i = 0; i < NB_OBJ; i++) {
                 cache.get(i);
             }
             getTime += System.nanoTime() - start;
             get += NB_OBJ;
 
 
             System.out.println("Iteration : " + j);
             double getTimeSecond = getTime / (double) (1000 * 1000 * 1000);
             double putTimeSecond = putTime / (double) (1000 * 1000 * 1000);
 
 
             System.out.println("Real object size : " + objectSizeInMemory + " MB");
             System.out.println("Memory allocated : " + allocator.getAllocatedMemory() / 1024 / 1024 + " MB");
             System.out.println("Memory used : " + allocator.getUsedMemory() / 1024 / 1024 + " MB");
             System.out.println("Puts : " + put / putTimeSecond + " object/s");
             System.out.println("Gets : " + get / getTimeSecond + " object/s");
             System.out.println("Puts : " + (put * estimSize / 1024 / 1024) / putTimeSecond + " MB/s");
             System.out.println("Gets : " + (get * estimSize / 1024 / 1024) / getTimeSecond + " MB/s");
             System.out.println("");
             System.out.println("");
             cache.clean();
         }
 
 
     }
 
 }
