 package server.storage;
 
 import common.EventI;
 
 
 import server.system.StorageServer;
 import client.system.DummyStorageServerConnection;
 import client.system.StorageServerConnection;
 import junit.extensions.jfcunit.JFCTestCase;
 
 import org.joda.time.DateTime;
 
 public class StorageTest  extends JFCTestCase {	
 	public void testSimpleWithDummyServer() throws Exception {
 		
 		// Set up dummy client/server (without Java RMI)
 		DummyStorageServerConnection client = new DummyStorageServerConnection();
 		
 		
 		// Create a new Event
 		EventI event = client.eventStorage.create();	
 		
 		// Update fields (this is instantly sent to database)
 		event.setEventName("KTN forelesning");
 		//event.setCreatedByUser(MainClass.getCurrentUser());
 		event.setMeeting(true);
		event.setStart(new DateTime("2013-03-15 12:15:00"));
		event.setEnd(new DateTime("2013-03-15 15:00:00"));
 		event.setLocation("R1");
 		
 		// Delete
 		//TODO: This don't work of some reason
 		//client.eventStorage.delete(event);
 	}
 	
 	
 	public void testMultipleClients() throws Exception { // THIS IS JUST FOR DEBUGGING
 		
 		// Set up different client
 		StorageServer server = new StorageServer();
 		StorageServerConnection client1 = new StorageServerConnection();
 		StorageServerConnection client2 = new StorageServerConnection();
 
 		// NB: BØR DET VÆRE EventI(nterface) her?
 		EventI eventClient1 = client1.eventStorage.create();	
 		int eventID = eventClient1.getEventID();
 		
 		// Test om oppdatering av navn fungerer
 		eventClient1.setEventName("Test123");
 		assertEquals("Test123", eventClient1.getEventName());
 		
 		// Test om også oppdatert på serveren
 		EventI eventServer = server.eventStorage.get(eventID);
 		assertEquals("Test123", eventServer.getEventName());
 		
 		// Test om også oppdatert på klient2
 		EventI eventClient2 = client2.eventStorage.get(eventID);
 		assertEquals("Test123", eventClient2.getEventName());
 		
 	}
 }
