 import java.util.*; 
 import java.io.*; 
 import java.net.*;
 
 public class DirectoryServerConnection implements Runnable
 {
     private DirectoryServer _host = null; 
     private Socket _clientSocket = null;
     
     DirectoryServerConnection(DirectoryServer host, Socket clientSocket)
     {
         _host = host;
         _clientSocket = clientSocket; 
     } 
 
     public void run()
     {
         try
         {
             System.out.println("Thread for connection started.");
             
             // load in the stream data
             InputStream is = _clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
            // request data
             String line;
             String userName, addIP;
             String sendIP;
             String acceptIP;
             String hangupIP;            
             
			// read each line
             while ((line = br.readLine()) != null)
             {
                 // AddUser request
                 if (line.startsWith("<AddUser>"))
                 {
                     userName = br.readLine();
                     userName = userName.substring(10, userName.length() - 11);
 
                     addIP = br.readLine();
                     addIP = addIP.substring(11, addIP.length() - 12);
                     
                     System.out.println("\nAddUser Request");
                     System.out.println("---------------");
                     System.out.println("New user:   " + userName);
                     System.out.println("IP Address: " + addIP);
                     
                     User u = new User(userName, addIP);
                     _host._directory.add(u);
 
 					// when user adds themself, they should also receive
 					// existing directory (+ themself)
 					PrintWriter out = new PrintWriter(_clientSocket.getOutputStream(), true);
                     
                     out.println("<DirectoryListing>");
                     for (User o : _host._directory)
                     {
                         out.println("<User>");
                         out.println("<Username>" + o.getUsername() + "</Username>");
                         out.println("<IPAddress>" + o.getIPAddress() + "</IPAddress>");
                         out.println("</User>");
                     }
                     out.println("</DirectoryListing>");
                 }
                 // GetDirectory request
                 else if (line.startsWith("<GetDirectory>"))
                 {
                     System.out.println("\nGetDirectory Request");
                     
                     PrintWriter out = new PrintWriter(_clientSocket.getOutputStream(), true);
                     
                     out.println("<Directory>");
                     for (User u : _host._directory)
                     {
                         out.println("<User>");
                         out.println("<Username>" + u.getUsername() + "</Username>");
                         out.println("<IPAddress>" + u.getIPAddress() + "</IPAddress>");
                         out.println("</User>");
                     }
                     out.println("</Directory>");
                     
                     out.close();
                 }    
                 // SendCall request
                 else if (line.startsWith("<SendCall>")) {
                     sendIP = br.readLine();
                     sendIP = sendIP.substring(11, sendIP.length() - 12);
                     System.out.println("\nSendCall Request");
                     System.out.println("----------------");
                     System.out.println("IP Address: " + sendIP);
                 }
                 // AcceptCall request
                 else if (line.startsWith("<AcceptCall>")) {
                     acceptIP = br.readLine();
                     acceptIP = acceptIP.substring(11, acceptIP.length() - 12);
                     System.out.println("\nAcceptCall Request");
                     System.out.println("------------------");
                     System.out.println("IP Address: " + acceptIP);
                 }
                 // Hangup request
                 else if (line.startsWith("<Hangup>")) {
                     hangupIP = br.readLine();
                     hangupIP = hangupIP.substring(11, hangupIP.length() - 12);
                     System.out.println("\nHangup Request");
                     System.out.println("--------------");
                     System.out.println("IP Address: " + hangupIP);
                 }
                 // Something else
                 else {
                     // ignore
                 }
             }
         }
         catch (IOException ex)
         {
             // TODO: handle this exception
         }
     }
 }
 
