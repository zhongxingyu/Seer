 package dblockcache;
 
 import common.Constants;
 import java.util.List;
 import java.util.LinkedList;
 import virtualdisk.VirtualDisk;
 
 
 public class DBufferCache
 {
     private int myCacheSize;
     private List<DBuffer> myBlocks;
     private VirtualDisk myVirtualDisk;
 
 
 	/*
 	 * Constructor: allocates a cacheSize number of cache blocks, each
 	 * containing BLOCK-size bytes data, in memory
 	 */
 	public DBufferCache(int cacheSize, VirtualDisk virtualDisk) {
         myCacheSize = cacheSize;
         myBlocks = new LinkedList<DBuffer>();
 	}
 
 	/*
 	 * Get buffer for block specified by blockID. The buffer is "held" until the
 	 * caller releases it. A “held” buffer cannot be evicted: its block ID
 	 * cannot change.
 	 */
 	public DBuffer getBlock(int blockId)
     {
         DBuffer block = findBlock(blockId);
         block.acquire();
         if (!block.checkValid()) {
             block.startFetch();
             block.waitValid();
         }
         return block;
     }
 
 	/* Release the buffer so that others waiting on it can use it */
 	public void releaseBlock(DBuffer buf)
     {
         buf.release();
     }
 
     private synchronized DBuffer findBlock(int blockId)
     {
         for (DBuffer block : myBlocks) {
             if (block.getBlockID() == blockId) {
                 // Move block to back of list to maintain LRU ordering
                 myBlocks.remove(block);
                 myBlocks.add(block);
                 return block;
             }
         }
         return allocateBlock(blockId);
     }
 
     private DBuffer allocateBlock(int blockId)
     {
         if (myBlocks.size() > myCacheSize)
             evictBlock();
         DBuffer block = new DBuffer(blockId, myVirtualDisk);
         myBlocks.add(block);
         return block;
     }
 
     private void evictBlock()
     {
         for (DBuffer block : myBlocks) {
             if (!block.isHeld() && !block.isBusy()) {
                 myBlocks.remove(block);
                 if (!block.checkClean()) {
                     block.startPush();
                 }
                 return;
             }
         }
     }
 
 	/*
 	 * sync() writes back all dirty blocks to the volume and wait for completion.
 	 * The sync() method should maintain clean block copies in DBufferCache.
 	 */
 	public void sync() {
         for (DBuffer block : myBlocks) {
             block.acquire();
             if (block.checkValid() && block.checkClean()) {
                 block.startPush();
                waitClean();
             }
             block.release();
         }
     }
 }
