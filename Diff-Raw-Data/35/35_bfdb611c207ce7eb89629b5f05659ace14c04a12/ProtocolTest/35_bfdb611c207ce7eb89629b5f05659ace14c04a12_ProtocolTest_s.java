 package no.ntnu.osnap.com.testing;
 
 import java.util.Random;
 import java.util.concurrent.TimeoutException;
 import no.ntnu.osnap.com.deprecated.ComLayer;
 
 public class ProtocolTest {
     public static void main(String[] args) throws TimeoutException, InterruptedException {
         ComLayer arduino = new ComLayer();
		//arduino.setDaemon(true);
		arduino.start();
 		
 		boolean toggle = false;
 		
 		Random rand = new Random();
         
         for (long i = 120; i < 1000; ++i){
             arduino.data(new byte[]{
 				(byte)rand.nextInt(255),
 				(byte)rand.nextInt(255),
 				(byte)rand.nextInt(255),
 				(byte)rand.nextInt(255),
 				(byte)rand.nextInt(255),
 				(byte)rand.nextInt(255)
 			}, true);
 			//Thread.sleep(10);
         }
 		
 		Thread.sleep(10000);
		arduino.stopThread();
		
		while (arduino.isAlive())
			Thread.sleep(100);
 		
 		arduino.close();
         
         //arduino.print("ZooPark");
         //arduino.print("D");
 		System.out.println("Done");
     }
     
 }
