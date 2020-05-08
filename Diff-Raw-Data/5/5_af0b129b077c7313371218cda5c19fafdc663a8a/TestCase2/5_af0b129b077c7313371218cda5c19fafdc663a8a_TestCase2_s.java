 package app.test;
 
 import app.client.RetailStoreClientImplDriver1;
 
 public class TestCase2 {
 	
 	public static void main(String[] args) {
 		try {
 			(new Thread(new RetailStoreClientImplDriver1("M00001"))).start();
 			(new Thread(new RetailStoreClientImplDriver1("T00001"))).start();
 			(new Thread(new RetailStoreClientImplDriver1("V00001"))).start();
 		} catch (Exception e) {
 		    System.err.println("Client exception: " + e.toString());
 		    e.printStackTrace();
 		}
 	}
 	
 }
