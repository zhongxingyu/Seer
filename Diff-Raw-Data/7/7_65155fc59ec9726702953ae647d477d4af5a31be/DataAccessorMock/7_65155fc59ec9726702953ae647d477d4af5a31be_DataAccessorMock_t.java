 package pimp.persistence;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pimp.productdefs.Car;
 import pimp.productdefs.Drink;
 import pimp.productdefs.Jacket;
 import pimp.productdefs.Product;
 
 public class DataAccessorMock {
 	
 	private static boolean initialized = false;
 	
 	public DataAccessorMock() {
 		// Do nothing.
 	}
 	
 	public static void initialize(String filename) {
 		initialized = true;
 	}
 	
 	public static List<Product> load() {
 		if (!initialized) return null;
 		
 		List<Product> productList = new ArrayList();
 		
 		/*
 		//create products
 		Car honda = new Car();
 		honda.setColour("grey");
 		honda.setMake("Honda");
 		honda.setModel("Civic");
 		honda.setYear(1992);
 		honda.setName("Ellie's Honda is boring");
 		honda.setQuantity(1);
 		//save
 		productList.add(honda);
 		
 		Car nissan = new Car();
 		nissan.setColour("red");
 		nissan.setMake("Nissan");
 		nissan.setModel("Primera");
 		nissan.setYear(1998);
 		nissan.setName("Nissan Primera");
 		nissan.setQuantity(5);
 		productList.add(nissan);
 			
 		Jacket orangeJacket = new Jacket();
 		orangeJacket.setBrand("Generic Brand");
 		orangeJacket.setColour("orange");
 		orangeJacket.setIsWaterproof(true);
 		orangeJacket.setName("Orange Jacket");
 		orangeJacket.setSize("L");
 		orangeJacket.setQuantity(7);
 		productList.add(orangeJacket);
 		*/
 			
 		Jacket purpleJacket = new Jacket();
 		purpleJacket.brand = "Generic Brand";
 		purpleJacket.colour = "purple";
 		purpleJacket.isWaterproof = false;
 		purpleJacket.name = "Purple Jacket";
 		purpleJacket.size = "M";
 		purpleJacket.quantity = 9;
 		productList.add(purpleJacket);
 		
 		//has public attributes so that field builder will work
 		Drink liftPlus = new Drink();
 		liftPlus.name = "Lift Plus";
 		liftPlus.capacity = "440ml";
 		liftPlus.flavour = "Fizzy Lemony Tang";
 		liftPlus.quantity = 4;
 		productList.add(liftPlus);
 		
 		return productList;
 	}
 }
