 package test;
 
import static org.junit.Assert.*;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import dk.diku.pcsd.assignment1.impl.FileNotFoundException_Exception;
 import dk.diku.pcsd.assignment1.impl.IOException_Exception;
 import dk.diku.pcsd.assignment1.impl.KeyAlreadyPresentException_Exception;
 import dk.diku.pcsd.assignment1.impl.KeyImpl;
 import dk.diku.pcsd.assignment1.impl.KeyNotFoundException_Exception;
 import dk.diku.pcsd.assignment1.impl.KeyValueBaseImplService;
 import dk.diku.pcsd.assignment1.impl.KeyValueBaseImplServiceService;
 import dk.diku.pcsd.assignment1.impl.ServiceAlreadyInitializedException_Exception;
 import dk.diku.pcsd.assignment1.impl.ServiceInitializingException_Exception;
 import dk.diku.pcsd.assignment1.impl.ServiceNotInitializedException_Exception;
 import dk.diku.pcsd.assignment1.impl.ValueImpl;
 import dk.diku.pcsd.assignment1.impl.ValueListImpl;
 
 public class AtomicUpdateTest {
 	static KeyValueBaseImplServiceService kvbiss;
 	static KeyValueBaseImplService kvbis;
 	
 	static HashMap<String, String> testMap = new HashMap<String, String>();
 	static ArrayList<String> keys;
 	
	static String updatedValue = new String();
	static String readValue = new String();
	
 	static boolean testSuccessfull = true;
 	
 	
 	@BeforeClass 
 	public static void testSetup(){
 		kvbiss = new KeyValueBaseImplServiceService();
 		kvbis = kvbiss.getKeyValueBaseImplServicePort();
 		try {
 			kvbis.init(null);
 		} catch (FileNotFoundException_Exception e) {
 			e.printStackTrace();
 		} catch (ServiceAlreadyInitializedException_Exception e) {
 			e.printStackTrace();
 		} catch (ServiceInitializingException_Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void parallelRead() {
 		Random rnd = new Random();	
 		int N = 10;
 		
 		//write N key values in hashmap and store
 		for (int i=0; i<N; i++) {
 				String keyValue = String.valueOf(rnd.nextInt(999999));
 				String resultValue = String.valueOf(rnd.nextInt(99999));
 				
 				KeyImpl key = new KeyImpl();
 				key.setKey(keyValue);
 				
 				ValueImpl value = new ValueImpl();
 				value.setValue(resultValue);
 				
 				ValueListImpl valueList = new ValueListImpl();
 				valueList.getValueList().add(value);
 				try{
 				kvbis.insert(key, valueList);
 				testMap.put(keyValue, resultValue);
 				} catch (KeyAlreadyPresentException_Exception e) {
 					//do nothing
 				} catch (IOException_Exception e) {
 					e.printStackTrace();
 				} catch (ServiceNotInitializedException_Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 			keys = new ArrayList<String>(testMap.keySet());
 			String randomUpdateKey = keys.get( rnd.nextInt(keys.size()) );
 		
 			Runnable updater = new UpdateThread(randomUpdateKey);
 			Thread updateThread = new Thread(updater);
 			updateThread.start();
 			
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			Runnable reader = new ReadThread(randomUpdateKey);
 			Thread readThread = new Thread(reader);
 			readThread.start();
			
			try {
				readThread.join();
				updateThread.join();
			} catch(InterruptedException e) {
				
			}
 		    		
 		    //if one read failed fail test case
		    assertEquals("Result", readValue, updatedValue);
 		
 	}
 	
 	public static class UpdateThread implements Runnable {
 		String key;
 		
 		public UpdateThread(String key){
 			this.key = key;
 		}
 
 		@Override
 		public void run() {
 			Random rnd = new Random();
 			//a random update
 			
 			
 			String randomUpdateValue  = String.valueOf(rnd.nextInt(99999));
 			
 			testMap.put(this.key, randomUpdateValue);
 			
 			KeyImpl keyUpdate = new KeyImpl();
 			keyUpdate.setKey(key);
 			
 			ValueImpl value = new ValueImpl();
 			value.setValue(randomUpdateValue);
 		
 			ValueListImpl valueUpdateList = new ValueListImpl();
 			valueUpdateList.getValueList().add(value);
 			
 			try {
				kvbis.update(keyUpdate, valueUpdateList);		
				updatedValue = randomUpdateValue;			
 			} catch (IOException_Exception e) {
 				e.printStackTrace();
 				System.out.println("1");
 			} catch (KeyNotFoundException_Exception e) {
 				e.printStackTrace();
 				System.out.println("2");
 			} catch (ServiceNotInitializedException_Exception e) {
 				e.printStackTrace();
 				System.out.println("3");
 			}
 			}	
 	}
 	 
 	public static class ReadThread implements Runnable {
 		String key;
 		
 		public ReadThread(String key){
 			this.key = key;
 		}
 
 			@Override
 			public void run() {
 				Random rnd = new Random();
 				String actualValue = "";
 			
 				KeyImpl keyRead = new KeyImpl();
 				keyRead.setKey(this.key);
 				
 				try {
 					actualValue = kvbis.read(keyRead).getValueList().get(0).getValue().toString();
					readValue = actualValue;
 				} catch (IOException_Exception e) {
 					e.printStackTrace();
 				} catch (KeyNotFoundException_Exception e) {
 					e.printStackTrace();
 				} catch (ServiceNotInitializedException_Exception e) {
 					e.printStackTrace();
 				}
 									
 			}	
 		}
 			
 }
 	
 	
 	
 
