 package org.kaffeezusatz.commodity.collections;
 
import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 public class OrderedRunnableQueue {
 	
 	private final int runEveryEntries;
 	
 	private Integer lastNumber;
 	
 	private TreeMap<Integer, Runnable> queue;
 
 	private List<OrderedRunnableQueueListener> listener;
 
 	/**
 	 * Ausfuehren wenn kein Leak oder Ausfuehren nach X neuen Elementen.
 	 */
 	public OrderedRunnableQueue(final int runEveryEntries) {
 		this.runEveryEntries = runEveryEntries;
 		
 		this.lastNumber = -1;
 		
 		this.queue = new TreeMap<Integer, Runnable>();
		
		this.listener = new LinkedList<OrderedRunnableQueue.OrderedRunnableQueueListener>();
 	}
 
 	public void addListener(final OrderedRunnableQueueListener listener) {
 		this.listener.add(listener);
 	}
 
 	public OrderedRunnableQueueListener removeListener(final OrderedRunnableQueueListener listener) {
 		this.listener.remove(listener);
 		return listener;
 	}
 
 	public synchronized void add(final Integer number, final Runnable runnable) {
 		if (number == null) {
 			throw new IllegalArgumentException("Integer number can't be null!");
 		}
 		
 		if (runnable == null) {
 			throw new IllegalArgumentException("Runnable runnable can't be null!");
 		}
 		
 		fireAddEvent(number);
 		
 		this.queue.put(number, runnable);
 		
 		runOrdered();
 	}
 	
 	public synchronized void add(final OrderedRunnable or) {
 		if (or == null) {
 			throw new IllegalArgumentException("OrderedRunnable or can't be null!");
 		}
 		
 		add(or.getNumber(), or);
 	}
 	
 	/**
 	 * Iterates over queue and run only queued runnables without leak!
 	 * After X leaks queue will be handled as it is, disregard its leaks!
 	 */
 	protected void runOrdered() {
 		int last = lastNumber.intValue() + 1;
 		
 		TreeMap<Integer, Runnable> mapCopy = new TreeMap<Integer, Runnable>(queue);
 		
 		for (Entry<Integer, Runnable> runnable : mapCopy.entrySet()) {
 			if (last == runnable.getKey().intValue()) {
 				last += 1;
 				lastNumber = runnable.getKey();
 				
 				fireRunEvent(runnable.getKey());
 				queue.remove(runnable.getKey()).run();
 			} else if (runEveryEntries == queue.size()) {
 				runForced();
 				last = lastNumber.intValue() + 1;
 			}
 		}
 	}
 	
 	/**
 	 * Iterates ordered over queue and runs every single runnable! After that queue will be cleared!
 	 */
 	protected void runForced() {
 		fireRunForcedEvent();
 		
 		synchronized (queue) {
 			for (Entry<Integer, Runnable> entry : queue.entrySet()) {
 				if (entry.getValue() != null) {
 					fireRunEvent(entry.getKey());
 					entry.getValue().run();
 				}
 				lastNumber = entry.getKey();
 			}
 		}
 		
 		queue.clear();
 	}
 	
 	private void fireAddEvent(final Integer number) {
 		for (OrderedRunnableQueueListener listener : this.listener) {
 			listener.addEvent(number);
 		}
 	}
 	
 	private void fireRunEvent(final Integer number) {
 		for (OrderedRunnableQueueListener listener : this.listener) {
 			listener.runEvent(number);
 		}
 	}
 	
 	private void fireRunForcedEvent() {
 		for (OrderedRunnableQueueListener listener : this.listener) {
 			listener.runForcedEvent();
 		}
 	}
 	
 	public static interface OrderedRunnable extends Runnable {
 		public int getNumber();
 	}
 	
 	public static interface OrderedRunnableQueueListener {
 		public void addEvent(final Integer number);
 		
 		public void runEvent(final Integer number);
 		
 		public void runForcedEvent();
 	}
 }
