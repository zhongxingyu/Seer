 import java.io.*;
 import java.net.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 class HttpServer implements Runnable {
 
     private ServerSocket serverSocket;
     private ExecutorService pool;
     private final int port;
 
     public static void main(String[] args) throws IOException {
         System.out.println("Starting http server...");
         Thread httpServer = new Thread(new HttpServer(8080));
         httpServer.start();
     }
 
     HttpServer(int port) {
         this.port = port;
     }
 
     public void run() {
 
         try {
             serverSocket = new ServerSocket(this.port);
             pool = Executors.newCachedThreadPool();
 
             while(true) {
                 Socket socket = serverSocket.accept();
                 pool.execute(new Handler(socket));
             }
         } catch(IOException ex) {
         } finally {
         }
     }
 }
 
 class Handler implements Runnable {
 
     private final Socket client;
 
     Handler(Socket client) {
         this.client = client;
     }
 
     public void run() {
 
 
         try {
             char[] buffer = new char[1024];
             BufferedReader bufferedReader = new BufferedReader(
                     new InputStreamReader(client.getInputStream())
                     );
             PrintWriter output = new PrintWriter(this.client.getOutputStream(), true);
 
             int count = bufferedReader.read(buffer, 0, 1024);
             String requestString = new String(buffer, 0, count);
 
             String[] lines = requestString.split("\n");
 
             if(lines.length < 1) {
                 output.println("HTTP/1.1 400 Bad Request");
                 return;
             }
 
            String[] firstLine = lines[0].split("\\s");

             if(firstLine.length != 3 ||
                     firstLine[0].compareTo("GET") != 0 ||
                     firstLine[2].compareTo("HTTP/1.1") != 0
                     ) {
                         output.println("HTTP/1.1 400 Bad Request");
                         return;
                     }
 
             output.println("HTTP/1.1 404 Not Found");
 
         } catch(IOException ex) {
         } finally {
             if(!this.client.isClosed()) {
                 try {
                     this.client.close();
                 } catch(IOException ex) {
                 }
             }
         }
     }
 
 }
