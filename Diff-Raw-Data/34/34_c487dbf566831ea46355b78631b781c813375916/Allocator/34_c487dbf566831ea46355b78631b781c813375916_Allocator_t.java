 package jbu.offheap;
 
 import jbu.serializer.Type;
 import sun.misc.Unsafe;
 
 import javax.management.*;
 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Frontend of off-heap store
  */
 public class Allocator implements AllocatorMBean {
 
     private static Unsafe unsafe = UnsafeUtil.unsafe;
 
     //Thread safe until cannot be modified at runtime
     private final Map<Integer, Bins> binsByAddr = new HashMap<Integer, Bins>();
     //Thread safe until cannot be modified at runtime
     private final TreeMap<Integer, Bins> binsBySize = new TreeMap<Integer, Bins>();
 
     private final AtomicInteger allocatedMemory = new AtomicInteger(0);
     private final AtomicInteger usedMemory = new AtomicInteger(0);
     private final AtomicLong nbAllocation = new AtomicLong(0);
     private final AtomicLong nbFree = new AtomicLong(0);
 
 
     public Allocator(int maxMemory) {
         constructWithLogScale(maxMemory, 1, true);
     }
 
     private void constructWithLogScale(int initialMemory, int maxBins, boolean unsafe) {
         // Construct scale
 
         int binsSize = initialMemory / maxBins;
         int currentChunkSize = 64;
 
         for (int i = 0; i < maxBins; i++) {
             Bins bbb;
             if (unsafe) {
                 bbb = new UnsafeBins((int) Math.ceil((double) binsSize / (double) currentChunkSize), currentChunkSize, i);
             } else {
                 bbb = new ByteBufferBins(binsSize / currentChunkSize, currentChunkSize, i);
             }
             binsBySize.put(currentChunkSize, bbb);
             binsByAddr.put(i, bbb);
             currentChunkSize = currentChunkSize * 2;
         }
     }
 
     // Allocate with less waste but much more chunks
     public long alloc(int memorySize) {
         int usedMemoryByAllocate = 0;
         int memoryToAllocate = memorySize;
         long previousChunkAddr = -1;
         long firstChunk = -1;
         int nbAllocateChunk = 0;
         // Search for the bin with just size lesser
         while (memoryToAllocate > 0) {
             Map.Entry<Integer, Bins> usedBin;
             // Search upper and lesser
             Map.Entry<Integer, Bins> lesserBin = binsBySize.floorEntry(memoryToAllocate);
             Map.Entry<Integer, Bins> upperBin = binsBySize.ceilingEntry(memoryToAllocate);
             // Fill factor determine when stop cutting in two memory to allocate
             if (lesserBin == null || (((double) memorySize / (double) lesserBin.getKey()) > 0.75d && upperBin != null)) {
                 // take upper
                 usedBin = upperBin;
             } else {
                 usedBin = lesserBin;
             }
             // Allocate one chunk
             long chunkAddr = usedBin.getValue().allocateOneChunk();
 
             // If no chunk available
             // Try to take a inferior chunk if not available superior chunk and two inf chunk two sup chunk etc...
             if (chunkAddr < 0) {
                 Map.Entry<Integer, Bins> currentBin = usedBin;
                 // take inf
                 currentBin = binsBySize.lowerEntry(currentBin.getKey());
                 if (currentBin != null) {
                     chunkAddr = currentBin.getValue().allocateOneChunk();
                 }
             }
 
            // Here try superior chunk
             if (chunkAddr < 0) {
                 Map.Entry<Integer, Bins> currentBin = usedBin;
                 // take inf
                 currentBin = binsBySize.higherEntry(currentBin.getKey());
                 if (currentBin != null) {
                     chunkAddr = currentBin.getValue().allocateOneChunk();
                 }
             }
 
 
             // update next chunk of previous element
            if (previousChunkAddr != -1) {
                 setNextChunk(previousChunkAddr, chunkAddr);
             } else {
                 // First chunk
                 firstChunk = chunkAddr;
             }
             previousChunkAddr = chunkAddr;
             // update memory to allocate
             memoryToAllocate -= usedBin.getKey();
             // update used memory
             usedMemoryByAllocate += usedBin.getValue().finalChunkSize;
 
             nbAllocateChunk++;
         }
         // Set no next chunk to last chunk
         setNextChunk(previousChunkAddr, -1);
         this.usedMemory.getAndAdd(usedMemoryByAllocate);
         this.nbAllocation.getAndAdd(nbAllocateChunk);
         return firstChunk;
     }
 
     public void free(long firstChunkAdr) {
         // get all chunk ref
         // FIXME find bin
         // Get next adr
         long currentAdr = firstChunkAdr;
         long nextAdr;
         do {
             Bins bin = getBinFromAddr(currentAdr);
             nextAdr = bin.getNextChunkId(AddrAlign.getChunkId(currentAdr));
             // and free current
             bin.freeChunk(currentAdr);
             currentAdr = nextAdr;
             // update counter
             usedMemory.getAndAdd(-bin.finalChunkSize);
             nbFree.incrementAndGet();
         } while (nextAdr != -1);
     }
 
 
     public void store(long firstChunkAdr, byte[] data) {
         // store in first chunk all data that's I can store
         long currentChunkAdr = firstChunkAdr;
         int currentOffset = 0;
         while (currentOffset < data.length) {
             int currentChunkId = AddrAlign.getChunkId(currentChunkAdr);
             // Check if currentChunkId = -1 Error
             if (currentChunkAdr == -1) {
                 throw new BufferOverflowException("Data is too large");
             }
             Bins bin = getBinFromAddr(currentChunkAdr);
             int chunkSize = bin.chunkSize;
             bin.storeInChunk(currentChunkId, data, currentOffset,
                     (data.length - currentOffset > chunkSize) ? chunkSize : data.length - currentOffset);
             currentOffset += chunkSize;
             currentChunkAdr = bin.getNextChunkId(currentChunkId);
         }
     }
 
     public void store(long firstChunkAdr, ByteBuffer data) {
         // store in first chunk all data that's I can store
         // The byte buffer must be ready for reading...
         // Store only from position to limit
         long currentChunkAdr = firstChunkAdr;
         while (data.remaining() > 0) {
             int currentChunkId = AddrAlign.getChunkId(currentChunkAdr);
             // Check if currentChunkId = -1 Error
             if (currentChunkAdr == -1) {
                 throw new BufferOverflowException("Data is too large");
             }
             Bins bin = getBinFromAddr(currentChunkAdr);
             int chunkSize = bin.chunkSize;
             bin.storeInChunk(currentChunkId, data);
             currentChunkAdr = bin.getNextChunkId(currentChunkId);
         }
     }
 
     public StoreContext getStoreContext(long firstChunkAdr, int size) {
         return new StoreContext(firstChunkAdr, size);
     }
 
     public LoadContext getLoadContext(long firstChunkAdr) {
         return new LoadContext(firstChunkAdr);
     }
 
     public byte[] load(long firstChunkAdr) {
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         byte[] tmpB;
         long currentChunkAdr = firstChunkAdr;
         // Get data of current chunk
         while (currentChunkAdr != -1) {
             int currentChunkId = AddrAlign.getChunkId(currentChunkAdr);
             Bins bin = getBinFromAddr(currentChunkAdr);
             tmpB = bin.loadFromChunk(currentChunkId);
             bout.write(tmpB, 0, tmpB.length);
             // get next chunk
             currentChunkAdr = bin.getNextChunkId(currentChunkId);
         }
         return bout.toByteArray();
     }
 
     private void setNextChunk(long currentChunkAdr, long nextChunkAddr) {
         // FIXME find bin
         getBinFromAddr(currentChunkAdr).setNextChunkId(AddrAlign.getChunkId(currentChunkAdr), nextChunkAddr);
     }
 
     private Bins getBinFromAddr(long chunkAddr) {
         return binsByAddr.get(AddrAlign.getBinId(chunkAddr));
     }
 
     public void registerInMBeanServer(MBeanServer mbs) {
         try {
             mbs.registerMBean(this, new ObjectName("Allocator:name=allocator"));
             for (Bins bbb : binsBySize.values()) {
                 if (bbb instanceof ByteBufferBins) {
                     mbs.registerMBean(bbb, new ObjectName("Allocator.ByteBufferBins:maxChunk=" + bbb.chunkSize));
                 } else if (bbb instanceof UnsafeBins) {
                     mbs.registerMBean(bbb, new ObjectName("Allocator.UnsafeBins:maxChunk=" + bbb.chunkSize));
                 }
             }
         } catch (InstanceAlreadyExistsException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (MBeanRegistrationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NotCompliantMBeanException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (MalformedObjectNameException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     public void unRegisterInMBeanServer(MBeanServer mbs) {
         try {
             mbs.unregisterMBean(new ObjectName("Allocator:name=allocator"));
         } catch (InstanceNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (MBeanRegistrationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (MalformedObjectNameException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Override
     public int getAllocatedMemory() {
         return allocatedMemory.intValue();
     }
 
     @Override
     public int getUsedMemory() {
         return usedMemory.intValue();
     }
 
     @Override
     public long getNbAllocation() {
         return nbAllocation.longValue();
     }
 
     @Override
     public long getNbFree() {
         return nbFree.longValue();
     }
 
     public class StoreContext {
 
         private long currentChunkAdr;
         private long currentBaseAdr;
         private int currentOffset;
         private int remaining;
 
         public StoreContext(long firstChunkAdr, int size) {
             beginNewChunk(firstChunkAdr);
             // Store size
             storeInt(size);
         }
 
         public void storeInt(int value) {
             unsafe.putInt(this.currentBaseAdr + this.currentOffset, value);
             // FIXME 4 (int length) must be constant
             this.currentOffset += 4;
             this.remaining -= 4;
         }
 
         public void storeSomething(Object object, long offset, int byteRemaining) {
             do {
                 int byteToCopy = (remaining > byteRemaining) ? byteRemaining : remaining;
                 byteRemaining -= byteToCopy;
                 unsafe.copyMemory(object, offset, null, this.currentBaseAdr + this.currentOffset, byteToCopy);
                 this.currentOffset += byteToCopy;
                 this.remaining -= byteToCopy;
                 // If remaining in currentChunk == 0 load next chunk
                 if (this.remaining == 0) {
                     // Get next chunk address in last 4 byte
                     beginNewChunk(unsafe.getInt(this.currentBaseAdr + this.currentOffset));
                 }
             } while (byteRemaining > 0);
         }
 
         private void beginNewChunk(long chunkAdr) {
             this.currentChunkAdr = chunkAdr;
             // get bins
             // FIXME Suport only unsafebin
             // Get baseAdr of allocated memory
             // Get baseOffset of chunk
             // And store this in currentBaseAdr
             UnsafeBins b = (UnsafeBins) getBinFromAddr(chunkAdr);
             this.currentBaseAdr = b.binAddr + b.findOffsetForChunkId(AddrAlign.getBinId(chunkAdr));
             this.currentOffset = 0;
             this.remaining = b.chunkSize;
         }
     }
 
     public class LoadContext {
 
         private long currentChunkAdr;
         private long currentBaseAdr;
         private int currentOffset;
         private int remaining;
 
         public LoadContext(long firstChunkAdr) {
             beginNewChunk(firstChunkAdr);
             // Store size
             this.remaining = loadInt();
         }
 
         public int loadInt() {
             int res = unsafe.getInt(this.currentBaseAdr + this.currentOffset);
             // FIXME 4 (int length) must be constant
             this.currentOffset += 4;
             this.remaining -= 4;
             return res;
         }
 
         // FIXME NOT USE IT .... NYI (Not yet implemented)
         public void loadSomething2(Object object, long offset, int byteRemaining) {
             do {
                 int byteToCopy = (byteRemaining > remaining) ? remaining : byteRemaining;
                 //int d = unsafe.getInt(this.currentBaseAdr + this.currentOffset);
                 // FIXME WHY DIRECT MEMORY DON'T WORK !!!!!!!!!! (throw illegalArgument)
                 // FIXME Marked as NotYetImplemented in code work only if destination are primitive array
                 // See http://mail.openjdk.java.net/pipermail/hotspot-runtime-dev/2012-March/003322.html
                 unsafe.copyMemory(null, this.currentBaseAdr + this.currentOffset, object, offset, byteToCopy);
                 //unsafe.putInt(object, offset, d);
                 byteRemaining -= byteToCopy;
                 this.currentOffset += byteToCopy;
                 this.remaining -= byteToCopy;
                 if (this.remaining == 0) {
                     // Get next chunk address in last 4 byte
                    beginNewChunk(unsafe.getLong(this.currentBaseAdr + this.currentOffset));
                 }
             } while (byteRemaining > 0);
         }
 
         // loadSomething2 has a better impl, but working only when jdk7 are really fully implemented...
         // Workaround for copy memory
         // Allocate tmpbuffer in offheap .. and read with typed method of unsafe
         public void loadSomething(Object dest, long destOffset, Type type, int byteRemaining) {
             // allocate buffer
             long tmpAddr = unsafe.allocateMemory(byteRemaining);
             try {
                 int offset = 0;
                 do {
                     int byteToCopy = (byteRemaining > remaining) ? remaining : byteRemaining;
                     unsafe.copyMemory(this.currentBaseAdr + this.currentOffset, tmpAddr + offset, byteToCopy);
                     byteRemaining -= byteToCopy;
                     this.currentOffset += byteToCopy;
                     this.remaining -= byteToCopy;
                     if (this.remaining == 0) {
                         // Get next chunk address in last 4 byte
                        beginNewChunk(unsafe.getLong(this.currentBaseAdr + this.currentOffset));
                     }
                     offset += byteToCopy;
                 } while (byteRemaining > 0);
                 // Ok now write to heap
                 if (!type.isArray) {
                     if (type.equals(Type.BOOLEAN)) {
                         boolean b = unsafe.getBoolean(null, tmpAddr);
                         unsafe.putBoolean(dest, destOffset, b);
                     }
                     if (type.equals(Type.CHAR)) {
                         char c = unsafe.getChar(tmpAddr);
                         unsafe.putChar(dest, destOffset, c);
                     }
                     if (type.equals(Type.BYTE)) {
                         byte b = unsafe.getByte(tmpAddr);
                         unsafe.putByte(dest, destOffset, b);
                     }
                     if (type.equals(Type.SHORT)) {
                         short s = unsafe.getShort(tmpAddr);
                         unsafe.putShort(dest, destOffset, s);
                     }
                     if (type.equals(Type.INT)) {
                         int i = unsafe.getInt(tmpAddr);
                         unsafe.putInt(dest, destOffset, i);
                     }
                     if (type.equals(Type.LONG)) {
                         long l = unsafe.getLong(tmpAddr);
                         unsafe.putLong(dest, destOffset, l);
                     }
                     if (type.equals(Type.FLOAT)) {
                         float f = unsafe.getFloat(tmpAddr);
                         unsafe.putFloat(dest, destOffset, f);
                     }
                     if (type.equals(Type.DOUBLE)) {
                         double d = unsafe.getDouble(tmpAddr);
                         unsafe.putDouble(dest, destOffset, d);
                     }
                 }
             } finally {
                 // In all case unallocate buffer
                 unsafe.freeMemory(tmpAddr);
             }
 
         }
 
         private void beginNewChunk(long chunkAdr) {
             this.currentChunkAdr = chunkAdr;
             // get bins
             // FIXME Suport only unsafebin
             // Get baseAdr of allocated memory
             // Get baseOffset of chunk
             // And store this in currentBaseAdr
             UnsafeBins b = (UnsafeBins) getBinFromAddr(chunkAdr);
             this.currentBaseAdr = b.binAddr + b.findOffsetForChunkId(AddrAlign.getBinId(chunkAdr));
             this.currentOffset = 0;
             this.remaining = b.chunkSize;
         }
     }
 }
