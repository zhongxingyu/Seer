 package jbu.offheap;
 
 import sun.misc.Unsafe;
 
 import java.lang.reflect.Field;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 
 public class UnsafeBins extends Bins implements UnsafeBinsMBean {
 
     static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
 
     private long arrayBaseOffset = (long) unsafe.arrayBaseOffset(byte[].class);
 
     // Unsafe instanciation
     private static final Unsafe unsafe = UnsafeUtil.unsafe;
 
     // Method for get address of a direct byte buffer
     private final static Field bufferAddr;
 
     static {
         try {
             bufferAddr = Buffer.class.getDeclaredField("address");
             bufferAddr.setAccessible(true);
         } catch (NoSuchFieldException e) {
             // Never append ??
             throw new RuntimeException("Cannot use UnsafeBins in this JVM", e);
         }
     }
 
     final long binAddr;
 
     UnsafeBins(int initialChunkNumber, int chunkSize, int baseAddr) {
         super(initialChunkNumber, chunkSize, baseAddr);
         // FIXME Cannot allocate more than Integer.MAX_VALUE. Check this
         binAddr = unsafe.allocateMemory(initialChunkNumber * finalChunkSize);
     }
 
     @Override
     boolean storeInChunk(int chunkId, byte[] data, int currentOffset, int length) {
         // check size for avoid overflow on other chunk
         if (length > this.chunkSize) {
             throw new BufferOverflowException("Try to store too many data. Store "
                     + data.length + " in " + this.chunkSize);
         }
         long baseAddr = findOffsetForChunkId(chunkId) + binAddr;
 
         // put the length
         unsafe.putInt(baseAddr, length);
         // FIXME Who is this 4 ? Use constant
         long dstAddr = baseAddr + 4;
         long offset = arrayBaseOffset;
         while (length > 0) {
             long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(data, offset, null, dstAddr, size);
             length -= size;
             offset += size;
             dstAddr += size;
         }
 
         return true;
     }
 
     @Override
     public boolean storeInChunk(int chunkId, ByteBuffer data) {
         // Read only data between position and chunksize or buffer limit
         if (data.isDirect()) {
             // get base adress of the buffer
             try {
                 int length = (data.remaining() > this.chunkSize) ? this.chunkSize : data.remaining();
                 long dataAddr = (Long) UnsafeBins.bufferAddr.get(data);
                 long baseAddr = findOffsetForChunkId(chunkId) + binAddr;
 
                 // put the length
                 unsafe.putInt(baseAddr, length);
                 // FIXME Who is this 4 ?
                 long dstAddr = baseAddr + 4;
                 // put data from source position
                 unsafe.copyMemory(dataAddr + (data.position() << 0), dstAddr, length);
                 // update source position
                 data.position(data.position()+length);
             } catch (IllegalAccessException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 return false;
             }
             return true;
         } else {
             // Easy way put part to save in a byte array and call standard storeInChunk
             byte[] tmp = new byte[this.chunkSize];
             data.get(tmp);
             return storeInChunk(chunkId, tmp, 0, this.chunkSize);
         }
     }
 
     @Override
     byte[] loadFromChunk(int chunkId) {
         long baseAddr = binAddr + findOffsetForChunkId(chunkId);
         int size = unsafe.getInt(baseAddr);
         byte[] data = new byte[size];
         for (int i = 0; i < size; i++) {
             data[i] = unsafe.getByte(baseAddr + 4 + i);
         }
         return data;
     }
 
     @Override
     void setNextChunkId(int currentChunkId, long nextChunkId) {
         // Set nextChunkId to last 8 bytes of currentChunkId
         long nextChunkOffset = binAddr + findOffsetForChunkId(currentChunkId) + chunkSize + 4;
         unsafe.putLong(nextChunkOffset, nextChunkId);
     }
 
     @Override
     long getNextChunkId(int currentChunkId) {
         // Set nextChunkId to last 8 bytes of currentChunkId
         long nextChunkOffset = binAddr + findOffsetForChunkId(currentChunkId) + chunkSize + 4;
         return unsafe.getLong(nextChunkOffset);
     }
 
 }
