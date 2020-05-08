 package org.joushou.FiveProxy;
  
  
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Vector;
 import java.util.Arrays;
 import java.lang.Long;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 
 public class webServer {
  
 	static Vector threads = new Vector();
 	static Vector allThreads = new Vector();
   static int workingThreads = 0;
  
 	public static void startServer() {
 		for (int i = 0; i < Settings.threads; i++) {
 			Worker w = new Worker();
 			w.start();
 			threads.addElement(w);
 			allThreads.addElement(w);
 		}
 		ServerSocket serverSocket = null;
 		try {
 			serverSocket = new ServerSocket(Settings.listenPort);
 		} catch (IOException e) {
 			System.out.println("Couldn't listen on port "+ Settings.listenPort);
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	  Caching.init();
 		System.out.println("Waiting for requests...");
 		while (true) {
 			try {
 				Socket clientSocket = serverSocket.accept();
  
 				Worker w = null;
 				synchronized(threads) {
 					if(threads.isEmpty()) {
             BufferedOutputStream o = new BufferedOutputStream(clientSocket.getOutputStream());
             BufferedReader i = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             while (!i.readLine().equals("")){}
            o.write(("HTTP/1.1 503 Service Unavailable\r\nContent-Type: text/html; charset=UTF-8\r\nConnection: close\r\n\r\n<h1>Temporarily Unavailable</h1>\nToo many request are flowing in at the moment.<br />\nPlease try again in a few moments, or contact the <a href=\"mailto:"+Settings.webmaster+"\">webmaster</a>.\n<br /><hr /><i style=\"font-size: 10px\">five-proxy at <a href=\"http://"+Settings.hostname+":"+Settings.listenPort+"\">http://"+Settings.hostname+":"+Settings.listenPort+"</a></font></i>").getBytes());
             o.close();
             i.close();
             clientSocket.close();
 					} else {
 					  int i;
 					  int connections = 0;
 					  for (i = 0; i < allThreads.size(); i++) {
 					    Worker worker = (Worker) allThreads.elementAt(i);
 					    if (worker.clientIp != null && worker.clientIp.equals(clientSocket.getInetAddress().getHostAddress()))
 					      connections++;
 					  }
 					  System.out.println(connections);
             if (connections >= Settings.connLimit) {
               BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
               BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
               while (!br.readLine().equals("")){}
              bos.write(("HTTP/1.1 403 Forbidden\r\nContent-Type: text/html; charset=UTF-8\r\nConnection: close\r\n\r\n<h1>Too many connections from your ip</h1>\nToo many requests from your ip.<br />\nPlease try again in a few moments, or contact the <a href=\"mailto:"+Settings.webmaster+"\">webmaster</a>.\n<br /><hr /><i style=\"font-size: 10px\">five-proxy at <a href=\"http://"+Settings.hostname+":"+Settings.listenPort+"\">http://"+Settings.hostname+":"+Settings.listenPort+"</a></font></i>").getBytes());
               bos.close();
               br.close();
               clientSocket.close();
             } else {
   						w = (Worker) threads.elementAt(0);
   						threads.removeElementAt(0);
   				    w.socketOpenTime = System.currentTimeMillis();
   						w.setSocket(clientSocket);
 					  }
 					}
 				}
  
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		}
 	}
 }
