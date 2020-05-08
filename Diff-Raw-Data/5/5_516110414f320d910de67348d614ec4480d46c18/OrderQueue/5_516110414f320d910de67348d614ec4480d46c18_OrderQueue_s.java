 package server_battle;
 
 import java.util.concurrent.PriorityBlockingQueue;
 
 public class OrderQueue {
 
 	private class OEWrapper implements Comparable<OEWrapper> {
 
 		public final Order order;
 		public final long executionTime;
 
 		public OEWrapper(Order order, long executionTime) {
 			this.order = order;
 			this.executionTime = executionTime;
 		}
 
 		@Override
 		public int compareTo(OEWrapper other) {
 			return Long.compare(this.executionTime, other.executionTime);
 		}
 
 		@Override
 		public boolean equals(Object other) {
			if(other instanceof OEWrapper || other instanceof Order) {
 				return this.order.equals(other);
 			}
 			return false;
 		}
 	}
 
 	private final PriorityBlockingQueue<OEWrapper> inner;
 
 	public OrderQueue() {
 		inner = new PriorityBlockingQueue<OEWrapper>();
 	}
 
 	public void add(Order order, long executionWait) {
 		inner.put(new OEWrapper(order, System.currentTimeMillis() + executionWait));
 	}
 
 	public boolean ready() {
 		OEWrapper order = inner.peek();
 		return order != null && order.executionTime <= System.currentTimeMillis();
 	}
 
 	public Order poll() {
 		OEWrapper order = inner.poll();
 		if(order == null) {
 			return null;
 		}
 		return order.order;
 	}
 	
 }
 
