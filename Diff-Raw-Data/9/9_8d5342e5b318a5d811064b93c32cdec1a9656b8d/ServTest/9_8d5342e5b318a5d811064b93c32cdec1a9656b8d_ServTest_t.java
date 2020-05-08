 package server;
 
 import static org.junit.Assert.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
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
  * Currently a race condition in the testing, will figure out better way to test.
  * @author Marco Salazar
  * 
  * Fixed: Was not a race condition. Printing "Connect" create another line which offset the BufferedReader.
  * @tester Menghsuan Pan
  */
 
 public class ServTest {
 
 	@Test (timeout = 20000)
 	public void DiffCharTest() throws IOException, InterruptedException {
     	
     	try {
         	File f = new File("serverDocs.cfg");
         	f.delete();
         }catch(Exception e){}
     	
 		final Server server = new Server(new ServerSocket(1336));
 		server.build();
     	
         Thread serv = new Thread(new Runnable() {
             public void run() {
 				try {
 					server.serve();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
                 }
         });
         serv.start();
         
         // client connect
         Socket s1 = new Socket("localhost", 1336);
         BufferedReader br1 = new BufferedReader(new InputStreamReader(
                 s1.getInputStream()));
         PrintWriter p1 = new PrintWriter(s1.getOutputStream(), true);
         
         // make a new Document
         p1.println("NEW sampleDoc");
         // Waiting for document creation lag time.
         Thread.sleep(10);
         
         // test digit
         p1.println("INSERT 0 0 0");
         // test lower case letter
         p1.println("INSERT 0 1 a");
         // test capital letter
         p1.println("INSERT 0 2 A");
         // test space
         p1.println("INSERT 0 3");
         // test punctuation
         p1.println("INSERT 0 4 .");
         // test other symbols
         p1.println("INSERT 0 5 @");
         
         // server handling time
         Thread.sleep(100);
         
         // read-only purpose client connected
         Socket s2 = new Socket("localhost", 1336);
         BufferedReader br2 = new BufferedReader(new InputStreamReader(
                 s2.getInputStream()));
         PrintWriter p2 = new PrintWriter(s2.getOutputStream(), true);
         
         // get the document
         p2.println("GET 0");
         
         // check if expected.
         assertEquals("0aA .@", br2.readLine());
 	}
 	
 	
 	/**
 	 * Test the basic functions of the server using the textClientDemo.
 	 */
     @Test(timeout=20000)
     public void BasicFuntionTest() throws IOException, InterruptedException {
     	
     	try {
         	File f = new File("serverDocs.cfg");
         	f.delete();
         }catch(Exception e){}
     	
 		final Server server = new Server(new ServerSocket(1337));
 		server.build();
     	
         Thread serv = new Thread(new Runnable() {
             public void run() {
 				try {
 					server.serve();
 				} catch (IOException e1) {
 					e1.printStackTrace();
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
         // At this point, the server is still empty; therefore, should print nothing.
         p1.println("CONNECT");
         p2.println("CONNECT");
         p3.println("CONNECT");
         String o1 = null; String o2 = null; String o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals(""));
         
         //client 1 makes a new Document
         p1.println("NEW sampleDoc");
         
         //check to see if clients 2 and 3 see this new Document
         o1 = null; o2 = null; o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         System.out.println(o1);
         System.out.println(o2);
         System.out.println(o3);
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("0%sampleDoc%"));
         
         //clients get the documents just created.
         p1.println("GET 0");
         p2.println("GET 0");
         p3.println("GET 0");
         
         //get command will create an empty line since the file is a new file (by default empty).
         o1 = null; o2 = null; o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals(""));
         
         //check Insert command.
         p1.println("INSERT 0 0 a");
         o1 = null; o2 = null; o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         System.out.println(o1);
         System.out.println(o2);
         System.out.println(o3);
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("a"));
         
         //check Insert command from a different client.
         p2.println("INSERT 0 1 B");
         o1 = null; o2 = null; o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         System.out.println(o1);
         System.out.println(o2);
         System.out.println(o3);
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("aB"));                 
         
         //check Delete command
         p3.println("DELETE 0 0");
         o1 = null; o2 = null; o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         System.out.println(o1);
         System.out.println(o2);
         System.out.println(o3);
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals("B"));
         
         //check Delete command from another client.
         p2.println("DELETE 0 0");
         o1 = null; o2 = null; o3 = null;
         while (o1 == null)
         	o1 = br1.readLine();
         while (o2 == null)
         	o2 = br2.readLine();
         while (o3 == null)
         	o3 = br3.readLine();
         System.out.println(o1);
         System.out.println(o2);
         System.out.println(o3);
         assertTrue(o1.equals(o2) && o1.equals(o3) && o1.equals(""));
         
         
     }
     
     @Test
     public void RapidTypingTest() throws IOException, InterruptedException{
     	try {
         	File f = new File("serverDocs.cfg");
         	f.delete();
         }catch(Exception e){}
     	
 		final Server server = new Server(new ServerSocket(1338));
 		server.build();
     	
         Thread serv = new Thread(new Runnable() {
             public void run() {
 				try {
 					server.serve();
 				} catch (IOException e1) {
 					e1.printStackTrace();
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
         p1.println("INSERT 0 0 a");
         p1.println("INSERT 0 0 b");
         p1.println("INSERT 0 0 c");
         p1.println("INSERT 0 0 d");
         p1.println("INSERT 0 0 e");
         p1.println("INSERT 0 0 f");
         p1.println("INSERT 0 0 g");
         p1.println("INSERT 0 0 h");
         p1.println("INSERT 0 0 i");
         p1.println("INSERT 0 0 j");
         p1.println("INSERT 0 0 k");
         p1.println("INSERT 0 0 l");
         p1.println("INSERT 0 0 m");
         p1.println("INSERT 0 0 n");
         p1.println("INSERT 0 0 o");
         p1.println("INSERT 0 0 p");
         p1.println("INSERT 0 0 q");
         p1.println("INSERT 0 0 r");
         
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
         assertEquals("rqponmlkjihgfedcba", br2.readLine());
         
         // create another document
         p1.println("NEW sampleDoc");
         Thread.sleep(100);
         
         // create another document print all documents 
         assertEquals("0%sampleDoc%1%sampleDoc%", br2.readLine());
         
         // connect to the new document.
         p1.println("CONNECT");
         p1.println("GET 1");
         
         // rapid typing forward (how most people type).
         p1.println("INSERT 1 0 a");
         p1.println("INSERT 1 1 b");
         p1.println("INSERT 1 2 c");
         p1.println("INSERT 1 3 d");
         p1.println("INSERT 1 4 e");
         p1.println("INSERT 1 5 f");
         p1.println("INSERT 1 6 g");
         p1.println("INSERT 1 7 h");
         p1.println("INSERT 1 8 i");
         p1.println("INSERT 1 9 j");
         p1.println("INSERT 1 10 k");
         
         Thread.sleep(500);
         
         p2.println("CONNECT");
         p2.println("GET 1");
         
         
         // connected, all documents are printed.
         assertEquals("0%sampleDoc%1%sampleDoc%", br2.readLine());
         // From the GET command, printed the document text.
         assertEquals("abcdefghijk", br2.readLine());
     }
     
     @Test (timeout = 20000)
     public void changeSameSpotTest() throws IOException, InterruptedException{
     	try {
         	File f = new File("serverDocs.cfg");
         	f.delete();
         }catch(Exception e){}
     	
 		final Server server = new Server(new ServerSocket(1339));
 		server.build();
     	
         Thread serv = new Thread(new Runnable() {
             public void run() {
 				try {
 					server.serve();
 				} catch (IOException e1) {
 					e1.printStackTrace();
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
         p1.println("INSERT 0 0 a");
         // Ensure that "INSERT 0 0 a" get to the server first.
         Thread.sleep(10);
         // client 2 insert
         p2.println("INSERT 0 0 b");
         
         // server handle time
         Thread.sleep(100);
         
         // client 3 connect
         Socket s3 = new Socket("localhost", 1339);
         BufferedReader br3 = new BufferedReader(new InputStreamReader(
                 s3.getInputStream()));
         PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
         
         // get the document.
         p3.println("GET 0");
         
         // handle "INSERT 0 0 a" from p1 ---> "a".
         // handle "INSERT 0 0 b" from p2 ---> "ba".
         assertEquals("ba", br3.readLine());
     }
     
     @Test (timeout = 20000)
     public void concurrentTypingTest() throws IOException, InterruptedException{
     	try {
         	File f = new File("serverDocs.cfg");
         	f.delete();
         }catch(Exception e){}
     	
 		final Server server = new Server(new ServerSocket(1340));
 		server.build();
     	
         Thread serv = new Thread(new Runnable() {
             public void run() {
 				try {
 					server.serve();
 				} catch (IOException e1) {
 					e1.printStackTrace();
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
         
         // create document and wait for the create time lag.
         p1.println("NEW sampleDoc");
         Thread.sleep(100);
         
         p2.println("GET 0");
         
         // client 1 insert
         p1.println("INSERT 0 0 a");
         // Ensure that "INSERT 0 0 a" get to the server first.
         Thread.sleep(10);
         // client 2 insert
         p2.println("INSERT 0 1 b");
         
         // server handle time
         Thread.sleep(100);
         
         // client 3 connect
         Socket s3 = new Socket("localhost", 1340);
         BufferedReader br3 = new BufferedReader(new InputStreamReader(
                 s3.getInputStream()));
         PrintWriter p3 = new PrintWriter(s3.getOutputStream(), true);
         
         // get the document.
         p3.println("GET 0");
         
         // handle "INSERT 0 0 a" from p1 ---> "a".
         // handle "INSERT 0 1 b" from p2 ---> "ab".
         assertEquals("ab", br3.readLine());
     }
     
     @Test (timeout = 20000)
     public void ThreePersonTest() throws IOException, InterruptedException{
     	try {
         	File f = new File("serverDocs.cfg");
         	f.delete();
         }catch(Exception e){}
     	
 		final Server server = new Server(new ServerSocket(1341));
 		server.build();
     	
         Thread serv = new Thread(new Runnable() {
             public void run() {
 				try {
 					server.serve();
 				} catch (IOException e1) {
 					e1.printStackTrace();
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
        Thread.sleep(100);
        
         // create document and wait for the create time lag.
         p1.println("NEW sampleDoc");
         Thread.sleep(100);
         
         p2.println("GET 0");
         p3.println("GET 0");
         
         // client 1 insert
         p1.println("INSERT 0 0 a");
         // Ensure that "INSERT 0 0 a" get to the server first.
         Thread.sleep(10);
         // client 2 insert
         p2.println("INSERT 0 0 b");
         Thread.sleep(10);
         p3.println("INSERT 0 0 c");
         
         // server handle time
         Thread.sleep(100);
         
         // client 3 connect
         Socket s4 = new Socket("localhost", 1341);
         BufferedReader br4 = new BufferedReader(new InputStreamReader(
                 s4.getInputStream()));
         PrintWriter p4 = new PrintWriter(s4.getOutputStream(), true);
         
         // get the document.
         p4.println("GET 0");
         
         // handle "INSERT 0 0 a" from p1 ---> "a".
         // handle "INSERT 0 0 b" from p2 ---> "ba".
         // handle "INSERT 0 0 c" from p. ---> "cba"
         assertEquals("cba", br4.readLine());  
        
        //assertEquals("cba", br4.readLine());
     }
 }
