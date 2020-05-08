 package server.rooms;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import server.ConnectionHandler;
 import server.lists.ServerUserList;
 import static server.Pause.pause;
 

 /**
  * This class tests all public methods of UserList with single, and multiple users as well as before and after removing users
  */
 public class RoomListTest
 {
 	private RoomList list;
 	private ChatRoom one;
 	private ChatRoom two;
 	private ChatRoom three;
 	private ConnectionHandler client;
 	private ServerUserList users;
 	
 
     /**
      * Sets up the test fixture.
      *
      * Called before every test case method.
      * 
      * creates a list and three client stubs to test wtih.
      */
     @Before
     public void setUp()
     {
     	client = new ConnectionHandler("client");
     	users = new ServerUserList();
     	try
 		{
 			users.add(client);
 		}
 		catch (IOException e)
 		{
 		}
     	client.getQueue().clear();
     	list = new RoomList(users);
     	one = new ChatRoom("one");
     	two = new ChatRoom("two");
     	three = new ChatRoom("three");
     }
     
     //test to see if we can add a user properly
     //tests both local map as well as messages passed to the client
     @Test
     public void add1() throws IOException
     {
     	list.add(one);
     	Map<String, ChatRoom> map = list.getMap();
     	assertEquals(map.size(), 1);
     	assertEquals(map.get("one"), one);
     	LinkedBlockingQueue<String> buffer = client.getQueue();
     	String output = buffer.poll();
     	assertEquals(output, "serverRoomList one");
     	output = buffer.poll();
     	assertEquals(output, null);
     }
     
     //similar to above, tests with multiple user additions
     @Test
     public void addMany() throws IOException
     {
     	String output;
     	LinkedBlockingQueue<String> buffer;
     	list.add(one);
     	list.add(two);
     	list.add(three);
     	Map<String, ChatRoom> map = list.getMap();
     	assertEquals(map.size(), 3);
     	assertEquals(map.get("one"), one);
     	assertEquals(map.get("two"), two);
     	assertEquals(map.get("three"), three);
     	
     	buffer = client.getQueue();
     	output = buffer.poll();
     	assertEquals(output, "serverRoomList one");
     	output = buffer.poll();
     	assertEquals(output, "serverRoomList two one");
     	output = buffer.poll();
     	assertEquals(output, "serverRoomList two one three");
     	output = buffer.poll();
     	assertEquals(output, null);
     }
     
     //tests to see if multiple additions of same user fails
     @Test (expected = IOException.class)
     public void addFailure() throws IOException
     {
     	list.add(one);
     	list.add(one);
     	fail();
     }
     
     //tests removing a user from the list, also checks if all clients are notified
     @Test
     public void testRemove1() throws IOException
     {
     	String output;
     	LinkedBlockingQueue<String> buffer;
     	list.add(one);
     	list.add(two);
     	list.add(three);
     	Map<String, ChatRoom> map = list.getMap();
     	client.getQueue().clear();
     	
     	list.remove(three);
     	assertEquals(map.size(), 2);
     	
     	buffer =  client.getQueue();
     	output = buffer.poll();
     	assertEquals(output, "serverRoomList two one");
     	output = buffer.poll();
     	assertEquals(output, null);
     }
     
     //tests if we removee all clients
     @Test
     public void testRemoveAll() throws IOException
     {
     	list.add(one);
     	list.add(two);
     	list.add(three);
     	
     	list.remove(three);
     	list.remove(two);
     	list.remove(one);
     	Map<String, ChatRoom> map = list.getMap();
     	
     	assertEquals(map.size(), 0);
     }
     
     // sees if add blocks removal of client properly
     @Test
     public void testAddRemoveConcurrently() throws IOException, InterruptedException
     {
     	list = new RoomList(users, true);
     	Thread adder = new Thread()
     	{
     		public void run()
     		{
     			try
 				{
 					list.add(one);
 				}
 				catch (IOException e)
 				{
 					fail();
 				}
     		}
     	};
     	
     	Thread remover = new Thread()
     	{
     		public void run()
     		{
     			pause(500, true);
     			list.remove(one);
     		}
     	};
     
     	adder.start();
     	remover.start();
     	adder.join();
     	remover.join();
     	
     	assertEquals(list.getMap().size(), 0);
     }
     
     //sees if remove blocks add user concurrently correctly.
     @Test
     public void testTemoveAddConcurrently() throws IOException, InterruptedException
     {
     	list = new RoomList(users, true);
     	list.add(one);
     	Thread remover = new Thread()
     	{
     		public void run()
     		{
     			list.remove(one);
     		}
     	};
     	
     	Thread adder = new Thread()
     	{
     		public void run()
     		{
     			pause(500, true);
     			try
 				{
 					list.add(one);
 				}
 				catch (IOException e)
 				{
 					fail();
 				}
     		}
     	};
     	
     	remover.start();
     	adder.start();
     	remover.join();
     	adder.join();
     	
     	assertEquals(list.getMap().size(), 1);
     }
 }
