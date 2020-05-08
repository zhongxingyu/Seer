 package jbu.offheap;
 
 import jbu.serializer.Type;
 import org.junit.Test;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import static org.junit.Assert.assertTrue;
 
 public class TestAllocator {
 
     @Test
     public void test_alloc_some_byte_and_store_data_should_be_reloaded() {
         byte[] data = new byte[10];
         for (int i = 0; i < data.length; i++) {
             data[i] = (byte) i;
         }
         Allocator allocator = new Allocator(10 * 1024 * 1024);
         long firstChunk = allocator.alloc(10);
         allocator.store(firstChunk, data);
         byte[] dataRes = allocator.load(firstChunk);
         assertTrue(Arrays.equals(data, dataRes));
     }
 
     @Test
     public void test_alloc_more_byte_than_chunk_size_and_store_data_should_be_reloaded() {
        byte[] data = new byte[1500];
         for (int i = 0; i < data.length; i++) {
             data[i] = (byte) i;
         }
         Allocator allocator = new Allocator(10 * 1024 * 1024);
        long firstChunk = allocator.alloc(1500);
         allocator.store(firstChunk, data);
         byte[] dataRes = allocator.load(firstChunk);
         assertTrue(Arrays.equals(data, dataRes));
     }
 
     @Test
     public void test_alloc_more_byte_than_biggest_bin_and_store_data_should_be_reloaded() {
         byte[] data = new byte[123456];
         for (int i = 0; i < data.length; i++) {
             data[i] = (byte) i;
         }
         Allocator allocator = new Allocator(10 * 1024 * 1024);
         long firstChunk = allocator.alloc(123456);
         allocator.store(firstChunk, data);
         byte[] dataRes = allocator.load(firstChunk);
         assertTrue(Arrays.equals(data, dataRes));
     }
 
     @Test
     public void test_alloc_more_bytebuffer_than_biggest_bin_and_store_data_should_be_reloaded() {
         byte[] data = new byte[123456];
         for (int i = 0; i < data.length; i++) {
             data[i] = (byte) i;
         }
         ByteBuffer bb = ByteBuffer.allocateDirect(data.length);
         bb.put(data);
         bb.flip();
         Allocator allocator = new Allocator(10 * 1024 * 1024);
         long firstChunk = allocator.alloc(123456);
         allocator.store(firstChunk, bb);
         byte[] dataRes = allocator.load(firstChunk);
         assertTrue(Arrays.equals(data, dataRes));
     }
 
     @Test
     public void test_unsafe_store_int() {
         int a = 42;
         Allocator allocator = new Allocator(1 * 1024);
         long firstChunk = allocator.alloc(4);
         Allocator.StoreContext sc = allocator.getStoreContext(firstChunk, 4);
         sc.storeInt(a);
         Allocator.LoadContext lc = allocator.getLoadContext(firstChunk);
         int b = lc.loadInt();
         System.out.println(b);
     }
 
     @Test
     public void test_unsafe_store_object_with_int() throws NoSuchFieldException {
         OneInt oi = new OneInt(42);
         Allocator allocator = new Allocator(1 * 1024);
         long firstChunk = allocator.alloc(4);
         Allocator.StoreContext sc = allocator.getStoreContext(firstChunk, 4);
         sc.storeSomething(oi, UnsafeUtil.unsafe.objectFieldOffset(OneInt.class.getDeclaredField("a")), 4);
         Allocator.LoadContext lc = allocator.getLoadContext(firstChunk);
         OneInt oi2 = new OneInt(0);
         lc.loadSomething(oi2, UnsafeUtil.unsafe.objectFieldOffset(OneInt.class.getDeclaredField("a")), Type.INT, 4);
         System.out.println(oi2.a);
     }
 
     public static int byteArrayToInt(byte[] b) {
         int value = 0;
         for (int i = 0; i < 4; i++) {
             int shift = (4 - 1 - i) * 8;
             value += (b[i] & 0x000000FF) << shift;
         }
         return value;
     }
 
 
 }
 
 class OneInt {
     int a;
 
     OneInt(int a) {
         this.a = a;
     }
 }
