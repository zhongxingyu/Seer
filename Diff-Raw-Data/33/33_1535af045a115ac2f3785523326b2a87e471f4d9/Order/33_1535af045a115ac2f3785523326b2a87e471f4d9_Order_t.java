 import java.sql.*;
 import java.util.ArrayList;
 
 class Order {
 	private final int orderid;
 	private int kid;
 	private String status;
 	private String orderTime;
 	private String deliveryTime;
 	private String deliveryAdress;
 	private ArrayList<Dish> orderContent;
 	private double price;
	private String payment;
 	
 	public Order(int orderid,int kid, String status, String orderTime, String deliveryTime, String deliveryAdress, ArrayList<Dish> orderContent, double price) {
 		this.orderid = orderid;
 		this.kid = kid;
 		this.status = status;
 		this.orderTime = orderTime;
 		this.deliveryTime = deliveryTime;
 		this.deliveryAdress = deliveryAdress;
 		this.orderContent = new ArrayList<Dish>();
 		this. price = price;
 	}
 	public int getOrderid() {
 		return orderid;
 	}
 	public int getKid() {
 		return kid;
 	}
 	public String getStatus() {
 		return status;
 	}
 	public String getOrderTime() {
 		return orderTime;
 	}
 	public String getDeliveryTime() {
 		return deliveryTime;
 	}
 	public String getDeliveryAdress() {
 		return deliveryAdress;
 	}
 	public ArrayList<Dish> getOrderContent() {
 		return orderContent;
 	}
 	public double getPrice() {
 		return price;
 	}
 	public boolean setStatus(String newStatus, Connection connection) throws Exception {
 	    Statement state = connection.createStatement();
 		status = newStatus;
 		String sql = "update orders set status = '" + status + "' where orderid = " + orderid + "";
 		
 		int answer = state.executeUpdate(sql);
 		if(answer>0){
 			ConnectionManager.closeStatement(state);
 			return true;
 		}else{
 			ConnectionManager.closeStatement(state);
 			return false;
 		}
 	}
 	public boolean setDeliveryTime(String newTime, Connection connection) throws Exception {
 	    Statement state = connection.createStatement();
 		deliveryTime = newTime;
 		String sql = "update orders set deliverytime = '" + deliveryTime + "' where orderid = " + orderid + "";
 		int answer = state.executeUpdate(sql);
 		if(answer>0){
 			ConnectionManager.closeStatement(state);
 			return true;
 		}else{
 			ConnectionManager.closeStatement(state);
 			return false;
 		}
 	}
 	public boolean setDeliveryAdress(String newDeliveryAdress, Connection connection) throws Exception {
 	    Statement state = connection.createStatement();
 		deliveryAdress = newDeliveryAdress;
 		String sql = "update orders set deliveryadress = '" + deliveryAdress + "' where kid = " + kid + " and where orderid = " + orderid + "";
 		int answer = state.executeUpdate(sql);
 		if(answer>0){
 			ConnectionManager.closeStatement(state);
 			return true;
 		}else{
 			ConnectionManager.closeStatement(state);
 			return false;
 		}
 	}
 	public boolean setOrderContent(ArrayList<Dish> newOrderContent, Connection connection) throws Exception {
 		Statement state = connection.createStatement();
 		orderContent = newOrderContent;
 		String sql;
 		int i = 1;
 		int answer;
     	for(Dish aDish : orderContent) {
     		sql = "INSERT INTO ordercontent(orderid, orderline, dishid, antall) values(" + orderid + ", " + i + ", " + aDish.getDishID() + ", 1)";
     		answer = state.executeUpdate(sql);
     		i++;
     		if(answer<1){
     			ConnectionManager.closeStatement(state);
     			return false;
     		}
     	}
     	ConnectionManager.closeStatement(state);
     	return true;
 	}
 	public String toString() {
 		String x = "Order ID: " + orderid + ". Kid: " + kid + ". Status: " + status + 
 				". Ordertime: " + orderTime + ". Deliverytime: " + deliveryTime +
 				". Delivery Adress: " + deliveryAdress + ". OrderContent: " + orderContent;
 		return x;
 	}
 }
 
 class Subscription extends Order {
 	private int frequency;
 	public Subscription(int orderid,int kid, String status, String orderTime, String deliveryTime, String deliveryAdress, ArrayList<Dish> orderContent, double price, int frequency) {
 		super(orderid, kid, status, orderTime, deliveryTime, deliveryAdress, orderContent, price);
 		this.frequency = frequency;
 	}
 }
