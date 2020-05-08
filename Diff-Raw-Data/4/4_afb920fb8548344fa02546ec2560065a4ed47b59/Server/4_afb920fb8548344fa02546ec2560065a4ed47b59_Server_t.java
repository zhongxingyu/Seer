 package server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.*;
 
 public class Server implements Runnable{
 	private Socket client;
 	
 	public Server(Socket client){
 		this.client = client;
 	}
 	public static void main(String[] args) {
 		ServerSocket serverSocket = null;
 		try {
 			serverSocket = new ServerSocket(4359);
 			
 		} catch (IOException e) {			
 			e.printStackTrace();
 		}
 		while(true){
 			Server server;
 			try{
 				Socket socket = serverSocket.accept();
 				server = new Server(socket);
 				Thread request = new Thread(server);
 				request.start();				
 				
 			}catch(IOException e){
 				System.out.println("Something went wrong");
 			}
 		}
 
 	}
 	
 	@Override
 	public void run() {
 		
 		System.out.println("SERVER: listening to new Client");
 		BufferedReader input = null;
 		PrintWriter output = null;
 		try {
 			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
 			output = new PrintWriter(client.getOutputStream(),true);
 			MathLogic logic = new MathLogic();
 			String received = input.readLine();
 			if(received != null){
 				String receivedInput[] = received.split(",");
 				int a = Integer.parseInt(receivedInput[1]);
 				int b = Integer.parseInt(receivedInput[2]);
 				String operation = receivedInput[0];
 				if(operation.equals("a")){
 					output.write(logic.add(a, b));
 					}
 				else if(operation.equals("s")){
 					output.write(logic.subtract(a, b));
 					}
 				output.flush();
				client.close();
 			}
 		}
 	catch (IOException e) {			
 			e.printStackTrace();
 		}
 		
 		
 		
 	}
 
 }
