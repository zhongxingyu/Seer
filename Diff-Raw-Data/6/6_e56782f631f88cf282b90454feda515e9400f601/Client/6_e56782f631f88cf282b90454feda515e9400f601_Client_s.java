 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Scanner;
 
 
 public class Client extends Thread{
     private Scanner localInput;
     private PrintWriter clientToServer;
     private Scanner serverToClient;
     private Socket conn;
     private String host;
     private int portNumber;
 	public void inputToServer(String input){
 		clientToServer.println(input);
 	}
     public String outputFromServer(){
     	return serverToClient.nextLine();
 	}
     public Socket getConn(){
     	return conn;
     }
     public Client(String host, int portNumber) throws IOException{
     	conn = new Socket(host, portNumber);
 		localInput = new Scanner(System.in);
 		clientToServer = new PrintWriter(conn.getOutputStream(), true);
 		serverToClient = new Scanner(new InputStreamReader(conn.getInputStream()));
 		this.host = host;
 		this.portNumber = portNumber;
     }
     public int getPortNumber(){
     	return this.portNumber;
     }
     public String getHost(){
     	return this.host;
     }
 	
 	public boolean login(){
 		boolean loginSuccessful = false;
 		System.out.println(outputFromServer());
 		System.out.println(outputFromServer());
 		System.out.println(outputFromServer());
 		String userName = localInput.nextLine();
 		inputToServer(userName);
 		System.out.println(outputFromServer());
 		inputToServer(localInput.nextLine());
 		//Response Message from Server
 		String responseMessage = outputFromServer();
 		System.out.println(responseMessage);
 		if(responseMessage.equals("success")){
 			loginSuccessful = true;
 			System.out.println("Hi! You are now logged into SimpleServer as " + userName);
 		}
 		else if(responseMessage.equals("failure")){
 			System.out.println("Login failed.");
 		}
 		return loginSuccessful;
 	}
 	
 	public void optionMenu(){
 		System.out.println(outputFromServer());
 		System.out.println(outputFromServer());
 		System.out.println(outputFromServer());
 		System.out.println(outputFromServer());
 		System.out.println(outputFromServer());
 		String command = localInput.nextLine();
 		inputToServer(command);
		if(outputFromServer().equals("success")){
 			if(command.equals("whoelse")){
 				whoelse();
 			}
 			else if (command.equals("wholasthr")){
 				wholasthr();
 			}
 			else if (command.equals("broadcast")){
 				broadcast();
 			}
 		}
		else if (outputFromServer().equals("failure")){
 			System.out.println(outputFromServer());
 			optionMenu();
 		}
 	}
 	
     public void whoelse(){
     	int numUsers = Integer.parseInt(outputFromServer());
     	for(int i = 0; i < numUsers; i++){
     		System.out.println(outputFromServer());
     	}
     	optionMenu();
     }
     public void wholasthr(){
     	
     	
     }
     public void broadcast(){
     	System.out.println(outputFromServer());
     	inputToServer(localInput.nextLine());
     	System.out.println(outputFromServer());
     	optionMenu();
     }
 
 }
