 package com.htb.cnk.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.htb.constant.Table;
 
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
 			if (count > 0) {
 				TableSetting.setLocalTableStatusById(pendedOrders.get(0).tid, Table.OPEN_TABLE_STATUS);;
 				int ret = MyOrder.submitPendedOrder(pendedOrders.get(0)
 						.getOrder(), pendedOrders.get(0).getStatus());
 				if (ret >= 0) {
 					pendedOrders.remove(0);
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
