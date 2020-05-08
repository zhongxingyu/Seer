import java.io.IOException;

 
 public class Main {
 	public static void main(String[] args){
 		
 		OrderList kitchenYo = new OrderList();
 		
		Thread server1 = new Thread(new Server(kitchenYo), "server1");	
 		Thread cook1 = new Thread(new Cook("Fred",kitchenYo), "cook1");
 		Thread cashier1 = new Thread(new Cashier("Bob",kitchenYo), "cashier1");
	
 		
		server1.start();
		System.out.println("wahh");
 		cashier1.start();
 		cook1.start();
 		
 		
 		
 	}
 }
