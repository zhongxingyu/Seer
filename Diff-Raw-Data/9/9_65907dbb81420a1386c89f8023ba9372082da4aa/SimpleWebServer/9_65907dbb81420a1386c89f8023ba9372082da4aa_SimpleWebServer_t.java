 package nl.astraeus.http;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import java.net.BindException;
 import java.net.InetSocketAddress;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: rnentjes
  * Date: 4/3/12
  * Time: 7:36 PM
  */
 public class SimpleWebServer implements Runnable {
     private SimpleServletContext    context;
     private Thread                  serverThread;
     private volatile boolean        running = true;
     private int                     port = 8080;
     private int numberOfConnections = 25;
 
     private BlockingQueue<ConnectionHandler> jobQueue = new LinkedBlockingQueue<ConnectionHandler>(25);
     private List<ConnectionHandlerThread> availableThreads = new LinkedList<ConnectionHandlerThread>();
 
     private SortedMap<String, HttpServlet> servlets = new TreeMap<String, HttpServlet>(new Comparator<String>() {
         public int compare(String o1, String o2) {
             int result = 0;
 
             if (o1 != null && o2 != null) {
                 result = (o1.length() - o2.length()) > 0 ? -1 : 1;
             }
 
             return result;
         }
     });
 
     public int getNumberOfConnections() {
         return numberOfConnections;
     }
 
     public void setNumberOfConnections(int numberOfConnections) {
         this.numberOfConnections = numberOfConnections;
     }
 
     private Map<String, SimpleHttpSession> sessions = new ConcurrentHashMap<String, SimpleHttpSession>(new HashMap<String, SimpleHttpSession>());
 
     public SimpleWebServer (int port) {
         this.serverThread = new Thread(this);
         this.context = new SimpleServletContext(this);
         this.port = port;
     }
 
     public void start() {
         try {
             for (HttpServlet servlet : servlets.values()) {
                 servlet.init();
             }
 
             serverThread.start();
 
             int size = 0;
             while(size++ < numberOfConnections) {
                 ConnectionHandlerThread t = new ConnectionHandlerThread(this, "ConnectionHandlerThread "+(availableThreads.size()+1), jobQueue);
 
                 synchronized (availableThreads) {
                     availableThreads.add(t);
                 }
 
                //System.out.println("Started new connection Thread "+t);
 
                 t.start();
             }
 
 
         } catch (ServletException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public void stop() {
         running = false;
 
         serverThread.interrupt();
     }
 
     private void handleConnection(ConnectionHandler ch) {
         boolean done = false;
 
         try {
             int size = 0;
 
             synchronized (availableThreads) {
                 size = availableThreads.size();
             }
 
             while(size++ < numberOfConnections) {
                 ConnectionHandlerThread t = new ConnectionHandlerThread(this, "ConnectionHandlerThread "+(availableThreads.size()+1), jobQueue);
 
                 synchronized (availableThreads) {
                     availableThreads.add(t);
                 }
 
                 System.out.println("Started new connection Thread "+t);
 
                 t.start();
             }
 
             if (!jobQueue.offer(ch, 10000, TimeUnit.MILLISECONDS)) {
                 System.out.println("Couldn't handle job!");
             }
         } catch (InterruptedException e) {
             // ignore
         }
     }
 
     public void run() {
         try {
             ServerSocketChannel ssc = ServerSocketChannel.open();
             InetSocketAddress isa = new InetSocketAddress(port);
             ssc.socket().bind(isa);
 
             try {
                 while (running) {
                     SocketChannel sc = ssc.accept();
 
                     ConnectionHandler handler = new ConnectionHandler(this, sc);
 
                     handleConnection(handler);
                 }
             } finally {
                 ssc.close();//always close the ServerSocket
             }
         } catch (BindException B) {
             //handling exception generated if they are already running server
             System.out.println("SERVER Already Running");
         } catch (Throwable e) {
             e.printStackTrace();
         }
 
     }
 
     public void addServlet(HttpServlet servlet, String uri) {
         if (uri == null) {
             uri = "/*";
         }
 
         servlets.put(uri, servlet);
     }
 
     HttpServlet findHandlingServlet(String requestURI) {
         HttpServlet result = null;
 
         if (requestURI == null) {
             requestURI = "";
         }
 
         for (Map.Entry<String, HttpServlet> entry : servlets.entrySet()) {
             String uri = entry.getKey();
             if (uri.endsWith("*")) {
                 if (requestURI.startsWith(uri.substring(0, uri.length()-1))) {
                     result = entry.getValue();
                     break;
                 }
             } else {
                 if (requestURI.equals(uri)) {
                     result = entry.getValue();
                     break;
                 }
             }
         }
 
         return result;
     }
 
     SimpleHttpSession getSession(String id) {
         SimpleHttpSession result;
 
         if (id == null || id.length() == 0) {
             id = createSessionId();
         }
 
         result = sessions.get(id);
 
         if (result == null) {
             result = new SimpleHttpSession(this, id);
 
             sessions.put(id, result);
         }
 
         return result;
     }
 
     synchronized String createSessionId() {
         String hash = Integer.toString(((Long)System.nanoTime()).hashCode());
 
         while (sessions.containsKey(hash)) {
             hash = Integer.toString(((Long)System.nanoTime()).hashCode());
         }
 
         return hash;
     }
 
     SimpleServletContext getServletContext() {
         return context;
     }
 
     void addRequestLog(HttpServlet servlet, SimpleHttpRequest request, long l) {
     }
 
     int getPort() {
         return port;
     }
 
     public void removeThread(ConnectionHandlerThread connectionHandlerThread) {
         Throwable e = new Throwable("Thread stopped running.");
 
         removeThread(connectionHandlerThread, e);
     }
 
     public void removeThread(ConnectionHandlerThread connectionHandlerThread, Throwable e) {
         System.out.println("Removing thread "+connectionHandlerThread.getName()+"; "+e.getMessage());
 
         synchronized(availableThreads) {
             availableThreads.remove(connectionHandlerThread);
         }
     }
 }
