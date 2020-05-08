 
 import java.net.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.io.*;
 import java.util.*;
 
 
 public class BlueTestListener {
 	public static void main(String argv[]) throws IOException{
 		int portNum = Integer.parseInt(argv[0]);
 		ServerSocket ss = new ServerSocket(portNum);
 		
 		//while(true)
 		BlueTestConnection testClient =	new BlueTestConnection(ss.accept(), portNum);
 		testClient.start();
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		Thread.yield();
 		
 		if(testClient.isAllPassed){
 			System.out.println("Awesome, nothing breaks");
 			System.exit(0);
 		}
 		else{
 			System.out.println("Oh crap, you just broke it, fix it now");
 			System.exit(1);
 		}
 		
 	}
 }
 
 class BlueTestConnection extends Thread {
 	Socket client;
 	int portNum;
 	public String result;
 	public boolean isAllPassed;
 	BlueTestConnection(Socket client, int portNum) throws SocketException{
 		this.setPriority(Thread.MAX_PRIORITY);
 		this.client = client;
 		this.portNum = portNum;
 		isAllPassed = true;
 		result = "";
 	}
 	
 	public void run(){
 		boolean connection = true;
 		try{
 			while(connection){
 				BufferedReader in = new BufferedReader(
 						new InputStreamReader(client.getInputStream()));
 				DataOutputStream output =
 			            new DataOutputStream(client.getOutputStream());
 				String request = "";
 				int i = 1;
 				while((request = in.readLine())!=null){
 					
 					if(request.equalsIgnoreCase("END_TEST_RESULT")){
 						break;
 					}
 					if(i>11)
 						result = result+request;
						//System.out.println(request);
 					i++;
 				}
 				//System.out.println("END");
 				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 				Date date = new Date();
 				
 				output.writeBytes(construct_http_header(200, 5));
 				output.writeChars("Team Blue Jasmine Server: "+dateFormat.format(date));
 				output.close();
 				client.close();
 				connection = false;
 				
 				BlueJUnitToTeamCity report = new BlueJUnitToTeamCity();
 				report.printTeamCityTestReport(result);
 				if(report.numFailuresOcc>0) {isAllPassed = false;}
 				//System.out.println("Close");
 			}
 			
 			client.close();
 
 		}
 		catch(IOException e){}
 	}
 	 
 	  private String construct_http_header(int return_code, int file_type) {
 	    String s = "HTTP/1.0 ";
 	    switch (return_code) {
 	      case 200:
 	        s = s + "200 OK";
 	        break;
 	      case 400:
 	        s = s + "400 Bad Request";
 	        break;
 	      case 403:
 	        s = s + "403 Forbidden";
 	        break;
 	      case 404:
 	        s = s + "404 Not Found";
 	        break;
 	      case 500:
 	        s = s + "500 Internal Server Error";
 	        break;
 	      case 501:
 	        s = s + "501 Not Implemented";
 	        break;
 	    }
 
 	    s = s + "\r\n"; //other header fields,
 	    s = s + "Connection: close\r\n"; //we can't handle persistent connections
 	    s = s + "Server: TeamBlue Jasmine Server\r\n"; //server name
 
 	    switch (file_type) {
 	      //plenty of types for you to fill in
 	      case 0:
 	        break;
 	      case 1:
 	        s = s + "Content-Type: image/jpeg\r\n";
 	        break;
 	      case 2:
 	        s = s + "Content-Type: image/gif\r\n";
 	      case 3:
 	        s = s + "Content-Type: application/x-zip-compressed\r\n";
 	      default:
 	        s = s + "Content-Type: text/html\r\n";
 	        break;
 	    }
 
 	    ////so on and so on......
 	    s = s + "\r\n"; //this marks the end of the httpheader
 	    //and the start of the body
 	    //ok return our newly created header!
 	    return s;
 	  }
 }
 
 
