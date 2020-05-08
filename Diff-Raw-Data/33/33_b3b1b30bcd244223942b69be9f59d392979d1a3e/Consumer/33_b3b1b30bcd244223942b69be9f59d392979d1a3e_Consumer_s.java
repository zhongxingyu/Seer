 package eu.socialsensor.sfc.streams.store;
 
 import java.io.IOException;
 import java.util.Queue;
 
 import eu.socialsensor.framework.common.domain.Item;
 import eu.socialsensor.framework.common.domain.Item.Operation;
 
 /**
  * Class for storing items to databases
  * 
  * 
  * @author manosetro
  * @email  manosetro@iti.gr
  * @author ailiakop
  * @email  ailiakop@iti.gr
  *
  */
 public class Consumer extends Thread {
 	private boolean isAlive = true;
 	private StreamUpdateStorage store = null;
 	private Queue<Item> queue;
 	
 	public Consumer(Queue<Item> queue,StreamUpdateStorage store){
 		this.store = store;
 		this.queue = queue;
 	}
 	
 	/**
 	 * Stores an item if the latter is found waiting in the queue
 	 */
 	public void run() {			
 		Item item = null;
 		while (isAlive) {
 			try {
 				item = poll();
				if (item == null){
 					continue;
 				}else {
 					dump(item);
 				}
 
 			} catch(IOException e) {
 				e.printStackTrace();
 				
 			}
 		}
 		
 		//empty queue
 		while ((item = poll()) != null) {
 			try {
 				dump(item);
 			} catch (IOException e) {
 				e.printStackTrace();
 				
 			}
 		}
 	}
 	
 	/**
 	 * Stores an item to all available databases
 	 * @param item
 	 * @throws IOException
 	 */
 	private void dump(Item item) throws IOException {
 		//dump update to store
 		if (store != null) {
 			if (item.getOperation() == Operation.NEW) {
 				
 				store.store(item);
 				
 			} 
 			else if (item.getOperation() == Operation.UPDATE) {
 				store.update(item);
 			}
 			else if (item.getOperation() == Operation.DELETED) {
 				store.delete(item.getId());
 			}
 			else {
 				System.out.println(item.getOperation() + ": Not supported operation");		
 			}
 		}
 	}
 	
 	/**
 	 * Polls an item from the queue
 	 * @return
 	 */
 	private Item poll() {
 		synchronized (queue) {					
			if (!queue.isEmpty()) {
				Item item = queue.remove();
				return item;
			}
			try {
				queue.wait(1000);
			} catch (InterruptedException e) {
				
			}
			return null;
 		}
 	}
 	
 	/**
 	 * Adds an item to the queue in order to be stored
 	 * @param item
 	 */
 	public synchronized void update(Item item) {
 		synchronized(queue) {
 			queue.add(item);
 		}
 	}
 	
 	/**
 	 * Stops the consumer thread
 	 * and closes all storages in case they have
 	 * not yet been closed
 	 */
 	public synchronized void die() {
 		isAlive = false;
 		
 		if(store != null){
 			store.close();
 		}
 	}
 }
