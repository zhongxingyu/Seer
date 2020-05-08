 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package streamfish;
 
 /**
  *
  * @author Kristian
  */
 public class Order {
 
 	private int orderId;
 	private int menuId;
 	private int customerId;
 	private int emplId;
 	private int nrPersons;
 	private String deliveryDate;
 	private String address;
 
 	public Order(int orderId, int menuId, int customerID, int emplId, int nrPersons, String deliveryDate, String address) {
 		this.orderId = orderId;
 		this.menuId = menuId;
 		this.customerId = customerID;
 		this.emplId = emplId;
 		this.nrPersons = nrPersons;
 		this.deliveryDate = deliveryDate;
 		this.address = address;
 	}
 
 	public Order(int menuId, int customerID, int emplId, int nrPersons, String deliveryDate, String address) {
 		this.menuId = menuId;
 		this.customerId = customerID;
 		this.emplId = emplId;
 		this.nrPersons = nrPersons;
 		this.deliveryDate = deliveryDate;
 		this.address = address;
 	}
 
 	public int getOrderId() {
 		return orderId;
 	}
 
 	public int getMenuId() {
 		return menuId;
 	}
 
 	public int getCustomerId() {
 		return customerId;
 	}
 
 	public int getEmplId() {
 		return emplId;
 	}
 
 	public int getNrPersons() {
 		return nrPersons;
 	}
 
 	public String getDeliveryDate() {
 		return deliveryDate;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 	
 	@Override
 	public String toString(){
 		String res = "";
		res += "ID: " + orderId + " | delivery date: " + deliveryDate + ", empl-id:" + emplId;
 		return res;
 	}
 }
