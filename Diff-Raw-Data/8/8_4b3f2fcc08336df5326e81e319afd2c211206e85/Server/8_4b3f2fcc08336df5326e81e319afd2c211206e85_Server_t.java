 package com.thoughtWorks.Server;
 
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class Server extends Thread{
     private int port;
     private Socket connectedSocket;
    ServerSocket serverSocket;
 
     public Server(int port) {
         this.port = port;
     }
 
     public void run() {
         Response response=new Response();
        try {
            serverSocket= new ServerSocket(port);
        } catch (IOException e) {
        }
         while (true) {
             try {
                 connectedSocket = createSocket();
                 Client client= getClient(connectedSocket);
                 sleep(472);
                 DataOutputStream output = new DataOutputStream(connectedSocket.getOutputStream());
                 response.sendResponse(output, client,this);
                 output.close();
             }
 
             catch (Exception e) {
             }
         }
     }
     public Socket createSocket() throws IOException {
         return serverSocket.accept();
     }
 
     public Client getClient(Socket connectedSocket) throws IOException {
         return new Client((new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()))).readLine());
     }
 
     public Socket getClientSocket(){
         return connectedSocket;
     }
 }
