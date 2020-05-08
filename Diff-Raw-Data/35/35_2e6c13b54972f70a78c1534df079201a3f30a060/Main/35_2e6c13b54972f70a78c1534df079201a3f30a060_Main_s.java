 
 public class Main {
 	public static void main(String[] args){
 		
 		OrderList kitchenYo = new OrderList();
 		
		Server server = new Server(kitchenYo);	
 		Thread cook1 = new Thread(new Cook("Fred",kitchenYo), "cook1");
 		Thread cashier1 = new Thread(new Cashier("Bob",kitchenYo), "cashier1");
 		
 		cashier1.start();
 		cook1.start();
 		
 		
 		
 	}
 }
