 package server_battle;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 
 public abstract class Order_Lists {
 public static AtomicLong Unique_Order_ID = new AtomicLong();
 private List<Order> Outstanding_Orders = new ArrayList<Order>();
 private final ToDoQueue toDoQueue;
 
 public Order_Lists(ToDoQueue toDoQueue){
 	this.toDoQueue = toDoQueue;
 	toDoQueue.RegisterOrderList(this);
 }
 
 public long add(Order order){
 	order.setOrder_ID(GetandIncrement());
 	Outstanding_Orders.add(order);
 	Activity activity = new Activity(200,order.getOrder_ID(),this);
 	toDoQueue.add(activity);
 	return order.getOrder_ID();
 }
 
 public void update(long order_ID){
 	for(Order x:Outstanding_Orders){
 		if(x.getOrder_ID() == order_ID){
 			x.update();
			break;
		}
 	}
 }
 
 public Order remove(long order_ID){
 	int index = 0;
 	for(Order order : Outstanding_Orders){
 		if(order.getOrder_ID() == order_ID)
 			return Outstanding_Orders.remove(index);
 		else
 			index++;
 	}
 	return null;
 }
 
 private synchronized long GetandIncrement(){
 	return Unique_Order_ID.getAndIncrement();
 }
 }
