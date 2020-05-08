 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 /**
  * A Buffer Pool that implements the Least Recently Used algorithm to
  * determine which blocks should be maintained in main memory.
  * 
  * @author Reese Moore
  * @author Tyler Kahn
  * @version 2011.10.12
  */
 public class LRUBufferPool implements BufferPool {
 	private RandomAccessFile disk;
 	private FiniteLinkedPriorityQueue<Buffer> lru;
 	private int block_size;
 	private Buffer[] pool;
 	private int max_buffers;
 	
 	/**
 	 * Create a new Buffer Pool that is backed by a file on disk.
 	 * @param file A File that we are going to be reading from / writing to.
 	 * @param num_buffers The maximum number of buffers that we are allowed to 
 	 * keep loaded into main memory 
 	 * @param block_size The size of an individual block.
 	 * @throws IOException 
 	 */
 	public LRUBufferPool(File file, int num_buffers, int block_size) 
         throws IOException
 	{
 		// Store the arguments in private memory
 		this.block_size = block_size;
 		
		// Allocate a RandomAccessFile from the file
		if (!file.exists()) { throw new FileNotFoundException(); }
 		disk = new RandomAccessFile(file, "rw");
 		
 		// Allocate the pool of buffers
 		max_buffers = ((int) disk.length() / block_size);
 		pool = new Buffer[max_buffers];
 		
 		// Allocate the FLPQ that we're going to use to implement LRU.
 		lru = new FiniteLinkedPriorityQueue<Buffer>(num_buffers);
 		
 	}
 	
 	/**
 	 * Get a handle to a Buffer that represents the block-th block of the file
 	 * that is backing this BufferPool.
 	 * @param block The index of the block we want to acquire.
 	 * @return A buffer handle to that block.
 	 */
 	@Override
 	public Buffer acquireBuffer(int block) {
 		if (pool[block] == null) {
 			pool[block] = allocateNewBuffer(block);
 		}
 		return pool[block];
 	}
 
 	/**
 	 * Alert the LRUBufferPool that a particular buffer was just used. If you
 	 * using LRUBuffers then this should 'just happen' and require no 
 	 * intervention on your part. This is where the LRU magic happens.
 	 * @param buffer The buffer to mark as just having been used.
 	 */
 	public void markUsed(Buffer buffer)
 	{
 		Buffer removed = lru.insertOrPromote(buffer);
 		if (removed != null) {
 			removed.flush();
 		}
 	}
 	
 	/**
 	 * Alert the LRUBufferPool that a particular buffer was just flushed. This
 	 * should then remove the buffer from the LRU.
 	 * @param buffer The buffer to mark as having been flushed.
 	 */
 	public void markFlushed(Buffer buffer)
 	{
 		lru.remove(buffer);
 	}
 	
 	/**
 	 * Allocate a new LRUBuffer that is linked to this Buffer pool, knows its
 	 * size and offset, and has access to the RandomAccessFile.
 	 * @param block The block that this new buffer is backed by.
 	 * @return A handle to the newly created buffer.
 	 */
 	private Buffer allocateNewBuffer(int block)
 	{
 		assert(block <= max_buffers);
 		return new LRUBuffer(this, disk, block * block_size, block_size);
 	}
 	
 	/**
 	 * Get the maximum number of blocks that are addressable by this buffer 
 	 * pool.
 	 * @return the number of blocks.
 	 */
 	public int size()
 	{
 		return max_buffers;
 	}
 	
 }
