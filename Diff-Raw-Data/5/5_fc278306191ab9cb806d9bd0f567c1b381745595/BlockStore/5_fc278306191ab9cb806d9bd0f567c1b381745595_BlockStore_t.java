 /*
  * Hyperchron, a timeseries data management solution.
  * Copyright (C) 2011 Tobias Wegner
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package org.hyperchron.impl.blocks;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.nio.ByteBuffer;
 import java.nio.LongBuffer;
 import java.nio.channels.FileChannel;
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.hyperchron.impl.TreeLeaf;
 
 public class BlockStore implements Runnable {
 	public static int BLOCK_SIZE = 4096;	
 	
 	public static int BLOCK_SIZE_BYTES = BLOCK_SIZE * 8;
 	
 	public static int SUPERBLOCK_ENTRIES = 4;
 	//entity id, start time, length 
 	
 	public int blocksPerSuperblock = BLOCK_SIZE_BYTES / (SUPERBLOCK_ENTRIES * 8);
 	
 	public static int BLOCK_FLUSH_INTERVAL = 1000;
 	
 	public static BlockStore instance = new BlockStore();
 	
 	protected Thread cacheFlushThread;
 	protected boolean cacheFlushThreadActive = true;
 	
 	public String blockDB = null;
 	
 	public BlockStore () {
 		blockDB = System.getProperty("timeseries.blockfile");
 		if (blockDB == null)
 			blockDB = "D:\\Temp\\ts\\block.db";
 		
 		ReadHeader();
 		
 		cacheFlushThread = new Thread(this);
 		cacheFlushThread.setName("Blockstore cache flush thread");
 	}
 	
 	public TreeLeaf LRULeaf = null;
 	public TreeLeaf OldLeaf = null;
 	
 	public int LeafCount = 0;
 	public Object LRUListMutex = new Object();
 	
 	public final int MAX_LEAF_COUNT = 2;
 	
 	java.util.concurrent.atomic.AtomicLong blockID = new AtomicLong();	
 	
 	public long getNextBlockID () {
 		return blockID.getAndIncrement();
 	}	
 	
 	public boolean LoadDataIntoLeaf(String uuid, TreeLeaf leaf, boolean init) {
 		long chunks = leaf.blockID / blocksPerSuperblock;
 		int chunkOffset = (int)(leaf.blockID % blocksPerSuperblock);
 		
 		long blockID = (chunks * (1 + blocksPerSuperblock) + chunkOffset + 1);			
 			
 		LongBuffer longBuffer = ReadBlock(blockID);
 
 		if ((longBuffer != null) && (longBuffer.capacity() == BLOCK_SIZE_BYTES / 8)) {
 			synchronized (leaf) {
 				if (leaf.timeStamps != null)
 					return false;
 				
 				leaf.length = (int)ReadFromSuperblock(chunks, chunkOffset, 2);
 				leaf.timeStamps = new long[BLOCK_SIZE];
 				longBuffer.get(leaf.timeStamps);	
 				
 				//this block is still clean
 				leaf.lastWrite = -1;
 				leaf.lastFlush = new Date().getTime();
 			}
 		} 
 		else
 		{	
 			if (!init)
 				return false;
 
 			InitLeaf(leaf);
 			
 			return false;
 		}
 		
 		InsertLeafIntoLRUList (leaf);
 		
 		return true;
 	}
 	
 	public void SaveDataToDisk(String uuid, TreeLeaf leaf) {
 		ByteBuffer buffer = ByteBuffer.allocateDirect(BLOCK_SIZE_BYTES);
 
 		LongBuffer longBuffer = buffer.asLongBuffer();
 		
 		synchronized (leaf) {
 			longBuffer.put(leaf.timeStamps);
 
 			//clean again
 			leaf.lastWrite = -1;
 			leaf.lastFlush = new Date().getTime();
 		}
 		
 		long chunks = leaf.blockID / blocksPerSuperblock;
 		int chunkOffset = (int)(leaf.blockID % blocksPerSuperblock);
 
 		WriteToSuperblock(chunks, chunkOffset, 0, leaf.entityID);
 		WriteToSuperblock(chunks, chunkOffset, 1, leaf.startingTimestamp);
 		WriteToSuperblock(chunks, chunkOffset, 2, leaf.length);
 			
 		long blockID = (chunks * (1 + blocksPerSuperblock) + chunkOffset + 1);		
 		
 		WriteBlock(blockID, buffer);
 	}	
 	
 	public void AccessLeaf (TreeLeaf leaf) {
 		//remove from current position
 		if ((leaf.LRUprev != null) || (leaf.LRUnext != null)) {
 			if (leaf.LRUprev != null)
 				leaf.LRUprev.LRUnext = leaf.LRUnext;
 			
 			if (leaf.LRUnext != null)
 				leaf.LRUnext.LRUprev = leaf.LRUprev;
 			else
 				synchronized (OldLeaf) {
 					OldLeaf = leaf.LRUprev;				
 				}
 		}
 		
 		synchronized (LRULeaf) {
 			leaf.LRUnext = LRULeaf;
 			LRULeaf = leaf;
 			leaf.LRUnext.LRUprev = leaf;
 			leaf.LRUprev = null;
 		}
 	}
 	
 	public void WriteHeader () {
 		File file = new File(blockDB);
 		
 		long nextBlockID = getNextBlockID();
 
 		long chunks = nextBlockID / blocksPerSuperblock;
 		int chunkOffset = (int)(nextBlockID % blocksPerSuperblock);
 		long blockID = (chunks * (1 + blocksPerSuperblock) + chunkOffset + 1);		
 		
 		if (chunkOffset == 0) {
 			//prevent superblock filled with '0's
 			LoadSuperblock(chunks);
 			WriteBackSuperblock(SuperblockCacheID);	
 		}
 		
 		try {
 			FileOutputStream fos = new FileOutputStream(file, true);
 
 			FileChannel fc = fos.getChannel();
 			
 			//LongBuffer longBuffer = fc.map(MapMode.READ_WRITE, leaf.blockID * BLOCK_SIZE * 2, BLOCK_SIZE * 2).asLongBuffer();
 			ByteBuffer buf = ByteBuffer.allocateDirect(8);
 
 			LongBuffer longBuffer = buf.asLongBuffer();
 			
 			longBuffer.put(nextBlockID);
 
 			fc.write(buf, blockID * BLOCK_SIZE_BYTES);
 			
 			fc.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public boolean ReadHeader () {
 		boolean LoadedDataFromDisk = false;
 		
 		File file = new File(blockDB);
 		
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			
 			FileChannel fc = fis.getChannel();
 			
 			//LongBuffer longBuffer = fc.map(MapMode.READ_ONLY, leaf.blockID * BLOCK_SIZE * 2, BLOCK_SIZE * 2).asLongBuffer();
 			ByteBuffer buf = ByteBuffer.allocateDirect(8);
 			
 			fc.read(buf, file.length() - 8);
 			
 			buf.flip();
 			
 			LongBuffer longBuffer = buf.asLongBuffer();
 
 			blockID.set(longBuffer.get());
 			
 			LoadedDataFromDisk = true;
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}		
 		
 		return LoadedDataFromDisk;
 	}
 	
 	public void InitLeaf (TreeLeaf leaf) {
 		synchronized (leaf) {
 			leaf.length = 0;
 			leaf.timeStamps = new long[BLOCK_SIZE];
 			
 			//block is still clean
 			leaf.lastWrite = -1;
 			leaf.lastFlush = new Date().getTime();
 		}
 
 		InsertLeafIntoLRUList (leaf);
 	}
 	
 	public void InsertLeafIntoLRUList (TreeLeaf leaf) {
 		synchronized (LRUListMutex) {
 			LeafCount++;
 
 			if (LeafCount == 1) {
 				LRULeaf = leaf;
 				OldLeaf = leaf;
 			} else {
 				AccessLeaf(leaf);
 			}								
 		}
 	}
 	
 	public TreeLeaf RemoveLeafFromLRUList () {
 		if (LeafCount == 0)
 			return null;
 		
 		synchronized (LRUListMutex) {
 			TreeLeaf swapOutLeaf = OldLeaf;
 			
 			OldLeaf = OldLeaf.LRUprev;
 			OldLeaf.LRUnext = null;
 			
 			LeafCount--;
 
 			return swapOutLeaf;			
 		}
 	}
 	
 	public void RunGarbageCollector() {
 		while (LeafCount > MAX_LEAF_COUNT) {
 			TreeLeaf swapOutLeaf = RemoveLeafFromLRUList();
 				
 			synchronized (swapOutLeaf) {
 				SaveDataToDisk(swapOutLeaf.entityDescriptor.uuid, swapOutLeaf);
 				
 				swapOutLeaf.timeStamps = null;
 				swapOutLeaf.LRUprev = null;
 				swapOutLeaf.LRUnext = null;	
 			}
 		}		
 	}
 	
 	public void FlushCache() {
 		if (LeafCount == 0)
 			return;	//nothing to do
 		
 		TreeLeaf leaf = OldLeaf;
 		
 		while (leaf != null) {
 			if (leaf.lastWrite != -1) {
 				SaveDataToDisk(leaf.entityDescriptor.uuid, leaf);
 			}
 			
 			leaf = leaf.LRUprev;
 		}
 	}
 	
 	public void Shutdown () {
 		cacheFlushThreadActive = false;
 		cacheFlushThread.interrupt();
 		
 		synchronized (LRUListMutex) {
 			while (LRULeaf != null) {
 				SaveDataToDisk(LRULeaf.entityDescriptor.uuid, LRULeaf);
 
 				LRULeaf = LRULeaf.LRUnext;
 			}
 		}
 		
 		if (SuperblockCacheID != -1)
 			WriteBackSuperblock(SuperblockCacheID);
 		
 		WriteHeader();
 	}
 	
 	public long ReadFromSuperblock(long chunks, int chunkOffset, int entry) {
 		synchronized (SuperblockCache) {
 			if (chunks != SuperblockCacheID) {
 				if (SuperblockCacheID != -1)
 					WriteBackSuperblock(SuperblockCacheID);
 
 				LoadSuperblock(chunks);
 			}
 			
 			return SuperblockCache[chunkOffset * SUPERBLOCK_ENTRIES + entry];			
 		}
 	}
 	
 	public void WriteToSuperblock(long chunks, int chunkOffset, int entry, long value) {
 		synchronized (SuperblockCache) {
 			if (chunks != SuperblockCacheID) {
 				if (SuperblockCacheID != -1)
 					WriteBackSuperblock(SuperblockCacheID);			
 				
 				LoadSuperblock(chunks);
 			}
 			
 			SuperblockCache[chunkOffset * SUPERBLOCK_ENTRIES + entry] = value;
 		}
 	}
 	
 	protected void WriteBackSuperblock(long chunk) {
 		ByteBuffer buf = ByteBuffer.allocateDirect(BLOCK_SIZE_BYTES);
 		
 		LongBuffer longBuffer = buf.asLongBuffer();
 		
 		longBuffer.put(SuperblockCache);
 		
 		WriteBlock (chunk * (1 + blocksPerSuperblock), buf);
 	}
 	
 	protected boolean LoadSuperblock(long chunk) {
 		Boolean LoadedFromDisk = true;
 		
 		LongBuffer longBuffer = ReadBlock (chunk * (1 + blocksPerSuperblock));
 		
 		if (longBuffer != null)
 			longBuffer.get(SuperblockCache);
 		else {
			for (int i = 0; i < BLOCK_SIZE; i++)
 				SuperblockCache[i] = -1;
 			
 			LoadedFromDisk = false;
 		}
 		
 		SuperblockCacheID = chunk;
 		
 		return LoadedFromDisk;
 	}
 
 	protected void WriteBlock(long blockID, ByteBuffer buffer) {
 		File file = new File(blockDB);
 		
 		try {
 			FileOutputStream fos = new FileOutputStream(file, true);
 			
 			FileChannel fc = fos.getChannel();
 			
 			long position = blockID * BLOCK_SIZE_BYTES;			
 			
 			fc.write(buffer, position);
 			
 			fc.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	protected LongBuffer ReadBlock(long blockID) {
 		File file = new File(blockDB);
 		
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			
 			FileChannel fc = fis.getChannel();
 			
 			ByteBuffer buf = ByteBuffer.allocateDirect(BLOCK_SIZE_BYTES);
 						
 			long position = blockID * BLOCK_SIZE_BYTES;			
 			
 			int bytesRead = fc.read(buf, position);
 			
 			if (bytesRead != buf.capacity())
 				return null;
 			
 			buf.flip();
 			
 			return buf.asLongBuffer();
 		} catch (Exception e) {
 			//e.printStackTrace();
 			
 			return null;
 		}		
 	}
 
	long SuperblockCache[] = new long[BLOCK_SIZE];
 	long SuperblockCacheID = -1;
 
 	@Override
 	public void run() {
 		while (cacheFlushThreadActive) {
 			FlushCache();
 			
 			RunGarbageCollector();
 		
 			try {
 				Thread.sleep(BLOCK_FLUSH_INTERVAL);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
