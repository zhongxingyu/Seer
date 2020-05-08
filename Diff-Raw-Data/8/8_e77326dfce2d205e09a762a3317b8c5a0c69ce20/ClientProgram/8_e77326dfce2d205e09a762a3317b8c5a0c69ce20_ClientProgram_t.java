 package mimicClient;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 
 
 
 /**
  * The MM-Client software for remote host.
  * So far, a single class that handles both sides of 
  * communication with the remote server (up and down) as 
  * well as local execution of remotely (server) commanded
  * methods.  
  * 
  * @author W. Taff and P. Salevski
  *
  */
 public class ClientProgram {
 
 	/**
 	 * @param args
 	 */
 	
 	//TODO - whole class needs documentation!
 	public static void main(String[] args) {
 		
 		
 		
 		//INITIALIZATION PARAMS
 		String hostName = args[0];
 		System.out.println(args[0]);
 		
 		//String server = "127.0.0.1";
 		String server = args[1];
 		System.out.println(args[1]);
 		
 		//Integer servPort = 30000;
 		Integer servPort = Integer.parseInt(args[2]);
 		System.out.println(servPort);
 		
 		
 		Runtime r = Runtime.getRuntime();
 		
 		String textReceived = "";
 		
 		String status = "IDLE";
 		
 		String os_name = null;
 		
 		
 		//initialize socket connection
 		try {
 			
 			os_name  = System.getProperty("os.name");
 			System.out.println("OS=" + os_name);
 			
 						
 			Socket socket = new Socket(server,servPort);
 			
 			//hostname of local machine:
 			InetAddress localMachine = InetAddress.getLocalHost();
 			System.out.println("Local Hostname=" + localMachine);
 			
 			System.out.println("Connected to Server " +
 					".. waiting for #GETNAME") ;
 			
 			
 			PrintStream outPrintStream = new PrintStream(
 					socket.getOutputStream() );
 			
 			BufferedReader inBufferedReader = 
 				new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			
 			//NEED TO SLEEP TO GET TIME FOR INITIAL COMMAND TO ARRIVE
 			Thread.sleep(1000);
 			
 			
 			if (inBufferedReader.ready()) {
 				textReceived = inBufferedReader.readLine();
 				System.out.println(textReceived);
 			}
 			
 			
 			
 		//if server says getname, then tell it hostName, and status
 			
 			if (textReceived.compareTo("#GETNAME")==0 ){
 				
 				outPrintStream.println("NAME=" + hostName);
 				outPrintStream.println("STATUS=" + status);	
 				
 			}
 			
 			
 			//and then start looping and keep checking inbox
 			while ( textReceived.compareTo("HALT")!=0 ){
 				
 				outPrintStream.println("GETINBOX");
 				
 				Thread.sleep(5000);
 				
 				textReceived = inBufferedReader.readLine();
 				
 				System.out.println(textReceived);
 				
 				
 				if ( textReceived.compareTo("MOD_0")==0 ){
 					
					status = "MOD_0";
					
 					mod_0(outPrintStream, status);
 				
 					
 				}
 				
 				if ( textReceived.compareTo("MOD_1")==0 ){
 					
 					//TODO RUN MOD 1
 					mod_1(r, os_name);
 					
 					status = "MOD_1";
					outPrintStream.println("STATUS=" + status);
 					
 					
 					
 				}
 				
 				
 				if ( textReceived.compareTo("MOD_2")==0 ){
 					
 					//TODO RUN MOD 2
 					System.out.println("Running Mod 2");
 					status = "MOD_2";
					outPrintStream.println("STATUS=" + status);
 
 					
 					
 				}
 				
 				
 			
 
 				
 			}
 			
 			
 			//CLOSE CONNECTION
 			outPrintStream.println("CLOSING...");
 			
 			Thread.sleep(1000);
 			
 			socket.close();
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception f){
 			f.printStackTrace();
 		}
 		
 
 		
 		
 	}
 
 	/**
 	 * A 5 ping module.
 	 * pings server 5 times then stops.  
 	 * 
 	 * @param r
 	 * @param osName 
 	 */
 	private static void mod_1(Runtime r, String osName) {
 		// TODO Auto-generated method stub
 		
 		System.out.println("Running Mod 1");
 		
 		try {
 			
 			Process p;
 			
 			if (osName.contains("Linux")) {
 			
 				p = r.exec("/bin/ping -c5 127.0.0.1"); //works linux
 				//Process p = r.exec("/bin/ls"); // works Linux
 			
 			}
 			
 			else { //is windows
 				
 				p = r.exec("ping -n 5 127.0.0.1");
 				
 			}
 			
 			InputStream in = p.getInputStream();
 			BufferedInputStream buf = new BufferedInputStream(in);
 			InputStreamReader inread = new InputStreamReader(buf);
 			BufferedReader bufferedReader = new BufferedReader(inread);
 			
 			String line;
 			while ((line = bufferedReader.readLine()) != null) {
 				
 				System.out.println(line);
 				
 			}
 			
 			try {
 				if (p.waitFor() != 0) {
 					System.err.println("exit value = " + p.exitValue());
 				}
 			}
 			catch (InterruptedException e) {
 				System.err.println(e);
 			}
 
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println("Mod 1 Iteration Complete");
 
 		
 	}
 
 	/**
 	 * Sends a status update message to the server.  
 	 * 
 	 * @param outPrintStream
 	 * @param status
 	 */
 	private static void mod_0(PrintStream outPrintStream, 
 												String status) {
 		
 		outPrintStream.println("STATUS=" + status);	
 		
 	}
 
 }//end class
