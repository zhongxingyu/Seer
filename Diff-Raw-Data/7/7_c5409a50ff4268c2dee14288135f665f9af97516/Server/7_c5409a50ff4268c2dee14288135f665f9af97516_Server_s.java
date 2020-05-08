 package cs420.buySell.server;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.LinkedList;
 import cs420.buySell.client.Client;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kevin
  * Date: 12/11/12
  * Time: 7:44 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class Server {
 
     //private Socket socket;
     private static ServerSocket serverSocket;
 
     private static int port;
     private static final int MAX_CONNECTIONS = 100;
     private static LinkedList<Client> clients;
 
     public static void main(String[] args) {
         clients = new LinkedList<Client>();
         try{
             port = 25001;
             serverSocket = new ServerSocket(port);
 
             Socket server;
             int connections = 0;
             while(connections++ < MAX_CONNECTIONS || MAX_CONNECTIONS == 0){
                 Communication comm;
 
                 server = serverSocket.accept();
 
                 comm = new Communication(server, clients);
                 Thread t = new Thread(comm);
                 t.start();
 
             }
         } catch(IOException e){
             e.printStackTrace();
         }
     }
 
 
 
 
 }
 
 class Communication implements Runnable{
     private Socket socket;
     private String line, input;
     private LinkedList<Client> clients;
 
     Communication(Socket socket, LinkedList<Client> clients){
         this.socket = socket;
         this.clients = clients;
 
     }
 
     public void run(){
         input = "";
         try{
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintStream out = new PrintStream(socket.getOutputStream());
 
             String output = "send_port";
             out.println(output);
             out.flush();
             String input = in.readLine();
             int port = Integer.parseInt(input);
             Client client = new Client(socket.getInetAddress(), port);
             clients.add(client);
             while((line = in.readLine()) != null){
                 interpret(line);
             }
            System.out.println("Total message: " + input);
            clients.remove(clients.indexOf(socket.getInetAddress()));
 
 
         } catch(IOException e){
             e.printStackTrace();
         }
 
     }
 
     public void interpret(String input) throws IOException {
         if(input.equals("get_list")){
             sendList();
         }
 
     }
 
     public void sendList() throws IOException {
         String output = "list_incoming";
         PrintWriter out = new PrintWriter(socket.getOutputStream());
         out.println(output);
         out.flush();
         for(Client client : clients){
             out.println(client);
             out.flush();
         }
         out.println("end_of_list");
         out.flush();
 
 
 
     }
 
 
 }
