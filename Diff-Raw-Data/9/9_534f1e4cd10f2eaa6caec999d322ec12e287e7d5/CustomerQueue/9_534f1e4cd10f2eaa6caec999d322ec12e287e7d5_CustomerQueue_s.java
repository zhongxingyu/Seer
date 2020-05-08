 import org.apache.commons.collections.buffer.CircularFifoBuffer;
 
 
 /**
  * This class implements a queue of customers as a circular buffer.
  */
 public class CustomerQueue {
 	/**
 	 * Creates a new customer queue.
 	 * @param queueLength	The maximum length of the queue.
 	 * @param gui			A reference to the GUI interface.
 	 */
 	
 	private CircularFifoBuffer waitingroom = null;
 	private CircularFifoBuffer waitingroomChairNumberer = null;
 	private Gui gui = null;
 	private Integer itarator = 0;
 	
     public CustomerQueue(int queueLength, Gui gui) {
     	this.waitingroom = new CircularFifoBuffer(queueLength);
 		this.gui = gui;
 		this.gui.println("created the queue");
 	}
     public synchronized void addCustomer(Customer customer) {
     	gui.println("adding a customer. Waitingroom is full: " + waitingroom.isFull());
     	while(waitingroom.isFull()) {
     		gui.println("waitingroom full, waiting!");
     		try {
     			gui.println("addCustomer waiting");
 				wait(); 	// wait until notified
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     	boolean empty = waitingroom.isEmpty();
     	addCustomerToWaitingRoom(customer);
     	if(empty) {
     		notifyAll();
     		gui.println("addCustomer: notifying all");
     	}
 	}
     public void addCustomerToWaitingRoom(Customer customer) {
     	waitingroom.add(customer);
 		waitingroomChairNumberer.add((int)itarator);
 		gui.fillLoungeChair(itarator, customer);
 		itarator = (itarator+1)%waitingroom.maxSize();
 	}
     public synchronized Customer getCustomer() {
 		Customer customer = null;
     	while(waitingroom.isEmpty()) {
 			try {
 				gui.println("waitingroom empty: getCustomer is waiting");
 				wait();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
     	boolean full = waitingroom.isFull();
     	customer = getCustomerFromWaitingroom();
     	if(full) {
     		notifyAll();
     		gui.println("getCustomer: notifying all");
     	}
     	return customer;
 	}
     public Customer getCustomerFromWaitingroom() {
    	gui.emptyLoungeChair((int)waitingroomChairNumberer.remove());
 		return (Customer) waitingroom.remove();
 	}
 }
