 package com.htb.cnk.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PendedOrder {
 
 	class PendedOrderDetail {
 		int tid;
 		int status;
 		String name;
 		String order;
 		
 		public PendedOrderDetail(int id, String name, int status, String order) {
 			tid = id;
 			this.name = name;
 			this.order = order;
			this.status = status;
 		}
 		
 		public int getTableId() {
 			return tid;
 		}
 		
 		public String getTableName() {
 			return name;
 		}
 		
 		public String getOrder() {
 			return order;
 		}
 		
 		public int getStatus() {
 			return status;
 		}
 	}
 	
 	private List<PendedOrderDetail> pendedOrders = new ArrayList<PendedOrder.PendedOrderDetail>();
 	public static final Object lock = new Object();
 
 	public void add(int id, String name, int status, String order) {
 		PendedOrderDetail porder = new PendedOrderDetail(id, name, status, order);
 		
 		pendedOrders.add(porder);
 	}
 	
 	public void remove(int id)  {
 		for (int i=count()-1; i>=0; i--) {
 			if (pendedOrders.get(i).getTableId() == id) {
 				pendedOrders.remove(i);
 				break;
 			}
 		}
 	}
 	
 	public int submit() {
 		synchronized (lock) {
 			int count = count();
 			for (int i = 0; i < count; i++) {
 				int ret = MyOrder.submitPendedOrder(pendedOrders.get(i)
 						.getOrder(), pendedOrders.get(i).getStatus());
 				if (ret >= 0) {
 					pendedOrders.remove(i);
 					break;
 				} else {
 					return -1;
 				}
 			}
 			return 0;
 		}
 	}
 	
 	public int count() {
 		return pendedOrders.size();
 	}
 }
