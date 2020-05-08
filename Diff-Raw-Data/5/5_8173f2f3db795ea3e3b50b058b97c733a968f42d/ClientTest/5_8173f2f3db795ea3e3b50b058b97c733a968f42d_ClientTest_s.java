 package com.tal.clienttext.junit;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 
 import com.tal.socketclient.SocketClient;
 
 public class ClientTest {
 
 	private SocketClient client;
 
 	@Before
 	public void runBeforeEveryTest() {
 		try {
 			client = new SocketClient("localhost", 8080);
 		} catch (IOException e) {
 			fail("Exception on creating client: " + e.getMessage());
 		}
 	}
 
 	@After
 	public void runAfterEveryTest() {
 		client = null;
 	} 
 
 	@Test(timeout = 10000)  //10 second timeout as this is a socket operation
 	public void testConnection() {
 		for(int i=0; i<10; i++){
 			String reply = "";
 
 			try {
 				reply = client.getData("GET_DATA");
 			} catch (IOException e) {
 				fail("Exception when getting data from server: " + e.getMessage());
 			}
 
 			//Check that the server returned what we expect
			assert(reply == "DATA="+i);
 		}
 	}
 }
