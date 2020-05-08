 package DynamicWebBrowser.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Date;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author Steffen, Mark, Shane
  */
 public class ClassServer implements Runnable {
 
     private final String PROP_FILE = "ClassServer.properties";
     private ServerSocket serverSocket;
     private Properties properties;
     private String documentRoot;
     private int port;
 
     public static void main(String[] args) {
         (new Thread(new ClassServer())).start();
     }
 
     public ClassServer() {
         properties = new Properties();
 
         try {
             //load a properties file
             properties.load(this.getClass().getResourceAsStream(PROP_FILE));
         } catch (IOException ex) {
             System.err.println("Failed to open properties file.");
         }
 
         documentRoot = properties.getProperty("root");
         port = Integer.parseInt(properties.getProperty("port"));
     }
 
     @Override
     public void run() {
         try {
             serverSocket = new ServerSocket(port);
 
             while (true) {
                 System.out.println("Waiting for connection on port: " + port);
                 Socket socket = serverSocket.accept();
                 System.out.println("Connection established.");
                 (new Thread(new ConnectionHandler(socket))).start();
             }
         } catch (IOException e) {
             System.err.println("IOException: " + e.getMessage());
             e.printStackTrace();
         }
     }
 
     /**
      * This thread processes a client (web browser) request. In the meantime the
      * web server can accept other clients.
      */
     class ConnectionHandler implements Runnable {
 
         Socket socket = null;
         BufferedReader readFromNet = null;
         PrintStream writeToNet = null;
         String inputLine;
         String httpMethod;
         StringTokenizer tokenizer;
         String fileString;
         String version;
         String contentType;
         File fileToServe;
 
         ConnectionHandler(Socket socket) {
             this.socket = socket;
         }
 
         /**
          * The method
          * <code>run()</code> is the core of the server
          */
         @Override
         public void run() {
             try {
                 writeToNet = new PrintStream(socket.getOutputStream());
                 readFromNet = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
                 inputLine = readFromNet.readLine();
                 
                 tokenizer = new StringTokenizer(inputLine);
                 httpMethod = tokenizer.nextToken();
 
                 if (httpMethod.equals("CLASS")) {
                     String protocol = tokenizer.nextToken();
                     
                     System.out.println("Client asked for: " + protocol);
                     
                     if (tokenizer.hasMoreTokens()) {
                         version = tokenizer.nextToken();
                     }
                     
                     // Skip the rest
                     while ((inputLine = readFromNet.readLine()) != null) {
                         if (inputLine.trim().equals("")) {
                             break;
                         }
                     }
                     
                     String classFile = properties.getProperty(protocol);
                     if (classFile != null) {
                         // Protocol is known
                         if (version.startsWith("HTTP/")) {
                             // Send a MIME header
                             writeToNet.print("HTTP/1.0 200 OK\r\n");
                             writeToNet.print("Date: " + new Date() + "\r\n");
                             writeToNet.print("Server: MyWebServer Version Feb 2000\r\n");
                             writeToNet.print("Content-length: " + classFile.length() + "\r\n");
                             writeToNet.print("Content-type: text/plain\r\n\r\n");
                         }
                         
                        writeToNet.println(classFile);
                         writeToNet.close();
                         System.out.println("Sent protocol class name.");
                     } else {
                         // Server doesn't know this protocol
                         if (version.startsWith("HTTP/")) {
                             // send a MIME header
                             writeToNet.print("HTTP/1.0 501 Not Implemented\r\n");
                             writeToNet.print("Date: " + new Date() + "\r\n");
                             writeToNet.print("Server: MyWebServer Version Feb 2000\r\n");
                             writeToNet.print("Content-type: text/html" + "\r\n\r\n");
                         }
 
                         writeToNet.println("<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD>");
                         writeToNet.println("<BODY><H1>HTTP Error 501: Not Implemented</H1></BODY></HTML>");
                         writeToNet.close();
                         
                         System.err.println("Class was not known, sent 501");
                     }
                 } else if (httpMethod.equals("GET")) {
                     fileString = tokenizer.nextToken();
 
                     contentType = guessContentTypeFromName(fileString);
 
                     if (tokenizer.hasMoreTokens()) {
                         version = tokenizer.nextToken();
                     }
 
                     // Skip the rest
                     while ((inputLine = readFromNet.readLine()) != null) {
                         if (inputLine.trim().equals("")) {
                             break;
                         }
                     }
 
                     try {
                         System.out.println("FileString: " + "\"" + fileString + "\"");
                         fileToServe = new File(documentRoot, fileString);
                         FileInputStream fis = new FileInputStream(fileToServe);
                         byte[] theData = new byte[(int) fileToServe.length()];

                         fis.read(theData);
                         fis.close();
 
                         if (version.startsWith("HTTP/")) {
                             // Send a MIME header
                             writeToNet.print("HTTP/1.0 200 OK\r\n");
                             writeToNet.print("Date: " + new Date() + "\r\n");
                             writeToNet.print("Server: MyWebServer Version Feb 2000\r\n");
                             writeToNet.print("Content-length: " + theData.length + "\r\n");
                             writeToNet.print("Content-type: " + contentType + "\r\n\r\n");
                         }
 
                         // Send the file
                         writeToNet.write(theData);
                         writeToNet.close();
                         System.out.println("File: " + fileToServe + " sent\n");
 
                     } catch (IOException e) {
                         // Cannot find the file
                         if (version.startsWith("HTTP/")) {
                             // send a MIME header
                             writeToNet.print("HTTP/1.0 404 File Not Found\r\n");
                             writeToNet.print("Date: " + new Date() + "\r\n");
                             writeToNet.print("Server: MyWebServer Version Feb 2000\r\n");
                             writeToNet.print("Content-type: text/html" + "\r\n\r\n");
                         }
                         writeToNet.println("<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD>");
                         writeToNet.println("<BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>");
                         writeToNet.close();
                         System.err.println("File: " + fileToServe + " not found\n");
                     }
                 } else {
                     // Method doesn't equal "GET"
                     if (version.startsWith("HTTP/")) {
                         // send a MIME header
                         writeToNet.print("HTTP/1.0 501 Not Implemented\r\n");
                         writeToNet.print("Date: " + new Date() + "\r\n");
                         writeToNet.print("Server: MyWebServer Version Feb 2000\r\n");
                         writeToNet.print("Content-type: text/html" + "\r\n\r\n");
                     }
 
                     writeToNet.println("<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD>");
                     writeToNet.println("<BODY><H1>HTTP Error 501: Not Implemented</H1></BODY></HTML>");
                     writeToNet.close();
 
                     System.err.println("Method: " + httpMethod + " is not supported\n");
 
                 }
             } catch (IOException e) {
             }
 
             try {
                 socket.close();
             } catch (IOException e) {
             }
         }
 
         /**
          * The method
          * <code>guessContentTypeFromName()</code> returns the MIME-type of a
          * file, which is guessed from the file's extention.
          */
         public String guessContentTypeFromName(String name) {
             if (name.endsWith(".html") || name.endsWith(".htm")) {
                 return "text/html";
             } else if (name.endsWith(".txt") || name.endsWith(".java")) {
                 return "text/plain";
             } else if (name.endsWith(".gif")) {
                 return "image/gif";
             } else if (name.endsWith(".class")) {
                 return "application/octet-stream";
             } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                 return "image/jpeg";
             } else {
                 return "text/plain";
             }
         }
     }
 }
