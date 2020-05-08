 package server;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import org.junit.Test;
 
 /**
  * Grammar: COMMAND ::= NEW | INSERT | DELETE | GET 
  * NEW ::= "NEW" NAME 
  * NAME ::= [.]+ 
  * DELETE ::= "DELETE" ID INDEX 
  * INSERT ::= "INSERT" ID INDEX LETTER
  * GET ::= "GET" ID 
  * ID ::= [0-9]+ 
  * INDEX ::= [0-9]+ 
  * LETTER ::= [.]
  * CONNECT ::= "CONNECT"
  * 
  */
 
 /**
  * This test is design to simulate the input that the clients are sending in and
  * the response of the server using both single client and multiple client. In
  * achieving this, multiple threads are used which can result in the differences
  * in responding time every run. This is not resulting from the race condition,
  * but basically not giving the server enough time to response.
  * 
  * The testing strategy is to first test the basic functions using single client
  * cases, and then get into multiple client testing possible failing conditions
  * with multiple users
  */
 
 public class ServTest {
 
 	/**
 	 * Test the basic functions of the server using the textClientDemo. NEW
 	 * INSERT DELETE GET CONNECT and the combination of them.
 	 */
 	@Test(timeout = 20000)
 	public void BasicFuntionTest() throws IOException, InterruptedException {
 
 		try {
 			File f = new File("serverDocs.cfg");
 			f.delete();
 		} catch (Exception e) {
 		}
 
 		final Server server = new Server(new ServerSocket(1337));
 		server.build();
 
 		Thread serv = new Thread(new Runnable() {
 			public void run() {
 				try {
 					server.serve();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		serv.start();
 
 		// client 1 connect
 		Socket s1 = new Socket("localhost", 1337);
 		BufferedReader br1 = new BufferedReader(new InputStreamReader(
 				s1.getInputStream()));
 		PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
 
 		// client 2 connect
 		Socket s2 = new Socket("localhost", 1337);
 		BufferedReader br2 = new BufferedReader(new InputStreamReader(
 				s2.getInputStream()));
 		PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
 
 		// client 3 connect
 		Socket s3 = new Socket("localhost", 1337);
 		BufferedReader br3 = new BufferedReader(new InputStreamReader(
 				s3.getInputStream()));
 		PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
 
 		// CONNECT command connect to the server and print all the documents.
 		// At this point, the server is still empty; therefore, should print
 		// nothing.
 		p1.println("CONNECT");
 		p2.println("CONNECT");
 		p3.println("CONNECT");
 		String o1 = null;
 		String o2 = null;
 		String o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals(""));
 
 		// client 1 makes a new Document
 		p1.println("NEW sampleDoc");
 
 		// check to see if clients 2 and 3 see this new Document
 		o1 = null;
 		o2 = null;
 		o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0%sampleDoc%"));
 
 		// clients get the documents just created.
 		p1.println("GET 0");
 		p2.println("GET 0");
 		p3.println("GET 0");
 
 		// get command will create an empty line since the file is a new file
 		// (by default empty).
 		o1 = null;
 		o2 = null;
 		o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0A"));
 
 		// check Insert command.
 		p1.println("INSERT 0 0 48");
 		o1 = null;
 		o2 = null;
 		o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0A48a"));
 
 		// check Insert command from a different client.
 		p2.println("INSERT 0 1 50");
 		o1 = null;
 		o2 = null;
 		o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0A48a50a"));
 
 		// check Delete command
		p3.println("DELETE 0 1");
 		o1 = null;
 		o2 = null;
 		o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0A50a"));
 
 		// check Delete command from another client.
		p2.println("DELETE 0 1");
 		o1 = null;
 		o2 = null;
 		o3 = null;
 		while (o1 == null)
 			o1 = br1.readLine();
 		while (o2 == null)
 			o2 = br2.readLine();
 		while (o3 == null)
 			o3 = br3.readLine();
 		assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0A"));
 	}
 
 	/**
 	 * Test weather typing very fast cause problem in the server.
 	 */
 	@Test(timeout = 20000)
 	public void RapidTypingTest() throws IOException, InterruptedException {
 		try {
 			File f = new File("serverDocs.cfg");
 			f.delete();
 		} catch (Exception e) {
 		}
 
 		final Server server = new Server(new ServerSocket(1338));
 		server.build();
 
 		Thread serv = new Thread(new Runnable() {
 			public void run() {
 				try {
 					server.serve();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		serv.start();
 
 		// client connect
 		Socket s1 = new Socket("localhost", 1338);
 		PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
 
 		// create document and wait for the create time lag.
 		p1.println("NEW sampleDoc");
 		Thread.sleep(100);
 
 		// connect to the document.
 		p1.println("CONNECT");
 		p1.println("GET 0");
 
 		// rapid typing
 		p1.println("INSERT 0 0 0");
 		p1.println("INSERT 0 0 1");
 		p1.println("INSERT 0 0 2");
 		p1.println("INSERT 0 0 3");
 		p1.println("INSERT 0 0 4");
 		p1.println("INSERT 0 0 5");
 		p1.println("INSERT 0 0 6");
 		p1.println("INSERT 0 0 7");
 		p1.println("INSERT 0 0 8");
 		p1.println("INSERT 0 0 9");
 		p1.println("INSERT 0 0 10");
 		p1.println("INSERT 0 0 11");
 		p1.println("INSERT 0 0 12");
 		p1.println("INSERT 0 0 13");
 		p1.println("INSERT 0 0 14");
 		p1.println("INSERT 0 0 15");
 		p1.println("INSERT 0 0 16");
 		p1.println("INSERT 0 0 17");
 
 		// waiting for the server to response to the typing.
 		Thread.sleep(500);
 
 		// another client use to pull out the text.
 		Socket s2 = new Socket("localhost", 1338);
 		BufferedReader br2 = new BufferedReader(new InputStreamReader(
 				s2.getInputStream()));
 		PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
 
 		p2.println("CONNECT");
 		p2.println("GET 0");
 
 		assertEquals("0%sampleDoc%", br2.readLine());
 		// check whether the text is what we expected
 		assertEquals("0A17a16a15a14a13a12a11a10a9a8a7a6a5a4a3a2a1a0a",
 				br2.readLine());
 
 		// create another document
 		p1.println("NEW sampleDoc");
 		Thread.sleep(100);
 
 		// connect to the new document.
 		p1.println("CONNECT");
 		p1.println("GET 1");
 
 		// rapid typing forward (how most people type).
 		p1.println("INSERT 1 0 0");
 		p1.println("INSERT 1 1 1");
 		p1.println("INSERT 1 2 2");
 		p1.println("INSERT 1 3 3");
 		p1.println("INSERT 1 4 4");
 		p1.println("INSERT 1 5 5");
 		p1.println("INSERT 1 6 6");
 		p1.println("INSERT 1 7 7");
 		p1.println("INSERT 1 8 8");
 		p1.println("INSERT 1 9 9");
 		p1.println("INSERT 1 10 10");
 
 		Thread.sleep(500);
 
 		p2.println("CONNECT");
 		p2.println("GET 1");
 
 		// connected, all documents are printed.
 		assertEquals("0%sampleDoc%1%sampleDoc%", br2.readLine());
 		// From the GET command, printed the document text.
 		assertEquals("1A0a1a2a3a4a5a6a7a8a9a10a", br2.readLine());
 	}
 
 	/**
 	 * Test weather two people changing the same spot will cause undesirable
 	 * behavior. Ideally, the server should handle one input at a time, and
 	 * handle all of them.
 	 */
 	@Test(timeout = 20000)
 	public void changeSameSpotTest() throws IOException, InterruptedException {
 		try {
 			File f = new File("serverDocs.cfg");
 			f.delete();
 		} catch (Exception e) {
 		}
 
 		final Server server = new Server(new ServerSocket(1339));
 		server.build();
 
 		Thread serv = new Thread(new Runnable() {
 			public void run() {
 				try {
 					server.serve();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		serv.start();
 
 		// client 1 connect
 		Socket s1 = new Socket("localhost", 1339);
 		PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
 
 		// client 2 connect
 		Socket s2 = new Socket("localhost", 1339);
 		PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
 
 		// create document and wait for the create time lag.
 		p1.println("NEW sampleDoc");
 		Thread.sleep(100);
 
 		p2.println("GET 0");
 
 		// client 1 insert
 		p1.println("INSERT 0 0 15");
 		// Ensure that "INSERT 0 0 a" get to the server first.
 		Thread.sleep(10);
 		// client 2 insert
 		p2.println("INSERT 0 0 30");
 
 		// server handle time
 		Thread.sleep(100);
 
 		// client 3 connect
 		Socket s3 = new Socket("localhost", 1339);
 		BufferedReader br3 = new BufferedReader(new InputStreamReader(
 				s3.getInputStream()));
 		PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
 
 		// get the document.
 		p3.println("GET 0");
 
 		// handle "INSERT 0 0 a" from p1 ---> "0A15a".
 		// handle "INSERT 0 0 b" from p2 ---> "0A30a15a".
 		assertEquals("0A30a15a", br3.readLine());
 	}
 
 	/**
 	 * Test weather normal concurrent typing from two people causes undesirable
 	 * problems.
 	 */
 	@Test(timeout = 20000)
 	public void concurrentTypingTest() throws IOException, InterruptedException {
 		try {
 			File f = new File("serverDocs.cfg");
 			f.delete();
 		} catch (Exception e) {
 		}
 
 		final Server server = new Server(new ServerSocket(1340));
 		server.build();
 
 		Thread serv = new Thread(new Runnable() {
 			public void run() {
 				try {
 					server.serve();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		serv.start();
 
 		// client 1 connect
 		Socket s1 = new Socket("localhost", 1340);
 		PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
 
 		// client 2 connect
 		Socket s2 = new Socket("localhost", 1340);
 		PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
 
 		// waiting for the clients to be started.
 		Thread.sleep(100);
 
 		// create document and wait for the create time lag.
 		p1.println("NEW sampleDoc");
 		Thread.sleep(100);
 
 		p2.println("GET 0");
 
 		// client 1 insert
 		p1.println("INSERT 0 0 60");
 		// Ensure that "INSERT 0 0 a" get to the server first.
 		Thread.sleep(10);
 		// client 2 insert
 		p2.println("INSERT 0 1 120");
 
 		// server handle time
 		Thread.sleep(100);
 
 		// client 3 connect
 		Socket s3 = new Socket("localhost", 1340);
 		BufferedReader br3 = new BufferedReader(new InputStreamReader(
 				s3.getInputStream()));
 		PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
 
 		// get the document.
 		p3.println("GET 0");
 
 		// handle "INSERT 0 0 a" from p1 ---> "0A60a".
 		// handle "INSERT 0 1 b" from p2 ---> "0A60a120a".
 		assertEquals("0A60a120a", br3.readLine());
 	}
 
 	/**
 	 * Test weather three people changing the same spot will cause undesirable
 	 * behavior. Ideally, the server should handle one input at a time, and
 	 * handle all of them.
 	 */
 	@Test(timeout = 20000)
 	public void ThreePersonTest() throws IOException, InterruptedException {
 		try {
 			File f = new File("serverDocs.cfg");
 			f.delete();
 		} catch (Exception e) {
 		}
 
 		final Server server = new Server(new ServerSocket(1341));
 		server.build();
 
 		Thread serv = new Thread(new Runnable() {
 			public void run() {
 				try {
 					server.serve();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		serv.start();
 
 		// client 1 connect
 		Socket s1 = new Socket("localhost", 1341);
 		PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
 
 		// client 2 connect
 		Socket s2 = new Socket("localhost", 1341);
 		PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
 
 		// client 3 connect
 		Socket s3 = new Socket("localhost", 1341);
 		PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
 
 		// waiting for the clients to be started.
 		Thread.sleep(200);
 
 		// create document and wait for the create time lag.
 		p1.println("NEW sampleDoc");
 		Thread.sleep(100);
 
 		p2.println("GET 0");
 		p3.println("GET 0");
 
 		// client 1 insert
 		p1.println("INSERT 0 0 30");
 		// Ensure that "INSERT 0 0 a" get to the server first.
 		Thread.sleep(10);
 		// client 2 insert
 		p2.println("INSERT 0 0 60");
 		Thread.sleep(10);
 		p3.println("INSERT 0 0 90");
 
 		// server handle time
 		Thread.sleep(100);
 
 		// client 3 connect
 		Socket s4 = new Socket("localhost", 1341);
 		BufferedReader br4 = new BufferedReader(new InputStreamReader(
 				s4.getInputStream()));
 		PrintWriter p4 = new PrintWriter(s4.getOutputStream(), true);
 
 		// get the document.
 		p4.println("GET 0");
 
 		// handle "INSERT 0 0 a" from p1 ---> "0A30a".
 		// handle "INSERT 0 0 b" from p2 ---> "0A60a30a".
 		// handle "INSERT 0 0 c" from p. ---> "0A90a60a30a"
 		assertEquals("0A90a60a30a", br4.readLine());
 	}
 
 	/**
 	 * Testing weather editing different document and creating document at the
 	 * same time cause the server to behave according to the spec. Ideally, it
 	 * should not affect other documents and clients editing a different file
 	 */
 	@Test(timeout = 20000)
 	public void changingMultiDocu() throws IOException, InterruptedException {
 		try {
 			File f = new File("serverDocs.cfg");
 			f.delete();
 		} catch (Exception e) {
 		}
 
 		final Server server = new Server(new ServerSocket(1342));
 		server.build();
 
 		Thread serv = new Thread(new Runnable() {
 			public void run() {
 				try {
 					server.serve();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		serv.start();
 
 		// client 1 connect
 		Socket s1 = new Socket("localhost", 1342);
 		BufferedReader br1 = new BufferedReader(new InputStreamReader(
 				s1.getInputStream()));
 		PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
 
 		// client 2 connect
 		Socket s2 = new Socket("localhost", 1342);
 		BufferedReader br2 = new BufferedReader(new InputStreamReader(
 				s2.getInputStream()));
 		PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
 
 		// client 3 connect
 		Socket s3 = new Socket("localhost", 1342);
 		BufferedReader br3 = new BufferedReader(new InputStreamReader(
 				s3.getInputStream()));
 		PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
 
 		Thread.sleep(100);
 
 		p1.println("NEW Doc1");
 		Thread.sleep(100);
 
 		p1.println("GET 0");
 		p1.println("INSERT 0 0 33");
 		p2.println("NEW Doc2");
 		Thread.sleep(100);
 
 		p2.println("GET 1");
 		p1.println("INSERT 0 1 67");
 		p2.println("INSERT 1 0 100");
 		p3.println("NEW Doc3");
 		Thread.sleep(100);
 
 		assertEquals("0%Doc1%", br1.readLine());
 		assertEquals("0A", br1.readLine());
 		assertEquals("0A33a", br1.readLine());
 		assertEquals("0A33a67a", br1.readLine());
 		assertEquals("0%Doc1%", br2.readLine());
 		assertEquals("0%Doc1%1%Doc2%", br2.readLine());
 		assertEquals("1A", br2.readLine());
 		assertEquals("1A100a", br2.readLine());
 		assertEquals("0%Doc1%", br3.readLine());
 		assertEquals("0%Doc1%1%Doc2%", br3.readLine());
 		assertEquals("0%Doc1%1%Doc2%2%Doc3%", br3.readLine());
 	}
 }
