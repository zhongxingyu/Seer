 package aether.repl;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import aether.io.Chunk;
 
 /**
  * This is the main class that performs the tasks as follows:
  * 1. get the next chunk to be replicated from the queue
  * 2. send a message over the network to get the available
  * 		space at each node
  * 3. select a node on which the chunk is to be replicated
  * 4. create a task for replicating the chunk and execute it
  * 5. send a confirmation message once the chunk has been 
  * 		replicated 
  * */
 class ChunkManager implements Runnable {
 	private static ChunkManager chunkMgr;
 	BlockingQueue chunkQueue;
 	//Chunk[] chunkList;
 	ChunkSpaceMap csm;
 	public static Executor exec;
 	public static final int THREADS_IN_REPL_POOL = 10;		// number of threads in the thread pool used by replicator
 	
 	public static synchronized ChunkManager getInstance () {
 		if (chunkMgr == null) {
 			chunkMgr = new ChunkManager (new LinkedBlockingQueue(), ChunkSpaceMap.getInstance() );			
 		}
 		return chunkMgr;
 	}
 	
 	public ChunkManager (BlockingQueue b, ChunkSpaceMap c) {
 		csm = c;
 		chunkQueue = b;
 		exec = Executors.newFixedThreadPool(3);		
 	}
 	
 	public void run () {
 		System.out.println("Chunk Manager thread started");
 		while (!Thread.currentThread().isInterrupted()) {
 			
 			
 			Chunk c;
 			try {
 				//calculatefreeMemory();
 				//remove from the queue
				
 				csm.calculatefreeMemory(); 					//call for free memory check
 				c = (Chunk)chunkQueue.take();
				Thread.currentThread().sleep(5000);
 				
 				/*csm.put(InetAddress.getLocalHost(), 7653, 4556);
 				csm.put(InetAddress.getLocalHost(), 7653, 4588);
 				csm.put(InetAddress.getLocalHost(), 7653, 4555);
 				csm.put(InetAddress.getLocalHost(), 7653, 4552);
 				csm.put(InetAddress.getLocalHost(), 7653, 4545);
 				csm.put(InetAddress.getLocalHost(), 7653, 4598);
 				System.out.println(InetAddress.getLocalHost());*/
 				NodeSpace node = csm.getStorageNode(c.getDataLength());
 				ChunkReplicator cr = new ChunkReplicator (c, node.getIPAddress(), node.getPort());
 				
 				
 				//replicate the chunk using a thread from the executor pool
 				exec.execute(cr);
 				
 				System.out.println("Sending chunk "+c.getChunkName());
 				
 			} catch (InterruptedException e) {
 				System.out.println("Interrupted Exception at Chunk Manager");
 				e.printStackTrace();
 			} catch (NoSpaceAvailableException e) {
 				System.out.println("No Space Available exception at Chunk Manager");
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.out.println("IOException at Chunk Manager");
 				e.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * gets the next chunk from the queue of file chunks
 	 * */
 	public Object getNextChunk () {
 		return chunkQueue.remove();
 	}
 	/**
 	 * adds a chunk to the queue
 	 * */
 	public void addChunk (Object o) {
 		chunkQueue.add(o);
 	}
 	/**
 	 * gets the chunk array from 
 	 * the client component process
 	 * */
 	/*public void getChunkArray (Chunk[] chunkArr) {
 		chunkList = chunkArr;
 	}*/
 	/**
 	 * sets up the blocking queue of chunks 
 	 * by importing chunks from the array into the queue 
 	 * */
 	public synchronized void addToQueue (Chunk[] chunkList) throws InterruptedException {
 		try {
 			for (int i = 0; i < chunkList.length; i++) {
 				chunkQueue.put(chunkList[i]);
 			}
 		} catch (InterruptedException e) {
 			throw e;
 		}
 		System.out.println("Added the chunks to the queue");
 	}
 }
