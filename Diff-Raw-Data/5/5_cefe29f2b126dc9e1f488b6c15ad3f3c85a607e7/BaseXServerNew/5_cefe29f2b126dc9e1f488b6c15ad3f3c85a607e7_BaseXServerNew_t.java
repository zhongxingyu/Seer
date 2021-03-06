 package org.basex.server;
 
 import static org.basex.Text.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.BindException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 
 import org.basex.BaseX;
 import org.basex.core.Prop;
 import org.basex.core.proc.Exit;
 import org.basex.util.Token;
 
 /**
  * This is the starter class for the database server. It handles incoming
  * requests and offers some simple threading to allow simultaneous database
  * requests. Add the '-h' option to get a list on all available command-line
  * arguments.
  * 
  * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
  * @author Andreas Weiler
  */
 public class BaseXServerNew {
 
   /** Flag for server activity. */
   boolean running = true;
   /** Verbose mode. */
   boolean verbose = false;
   /** Last Id from a client. */
   int lastid = 0;
   /** Current client connections. */
   final ArrayList<Session> sessions = new ArrayList<Session>();
   /** ServerSocket. */
   ServerSocket serverSocket;
   /** InputListener. */
   InputListener inputListener;
   /** SessionListenre. */
   SessionListener sessionListener;
 
   /**
    * Main method, launching the server process. Command-line arguments can be
    * listed with the <code>-h</code> argument.
    * @param args command line arguments
    */
   public static void main(final String[] args) {
     new BaseXServerNew(args);
   }
 
   /**
    * The server calls this constructor to listen on the given port for incoming
    * connections. Protocol version handshake is performed when the connection is
    * established. This constructor blocks until a client connects.
    * @param args arguments
    */
   public BaseXServerNew(final String... args) {
     Prop.server = true;
 
     if(!parseArguments(args)) return;
 
     try {
       serverSocket = new ServerSocket(Prop.port);
       BaseX.outln(SERVERSTART);
       inputListener = new InputListener();
       inputListener.start();
       sessionListener = new SessionListener(this);
       sessionListener.start();
     } catch(final Exception ex) {
       BaseX.debug(ex);
       if(ex instanceof BindException) {
         BaseX.errln(SERVERBIND);
       } else if(ex instanceof IOException) {
         BaseX.errln(SERVERERR);
       } else {
         BaseX.errln(ex.getMessage());
       }
     }
   }
   
   /**
    * Stops.
    */
   public void stop() {
     for(int i = 0; i < sessions.size(); i++) {
       try {
         sessions.get(i).dis.close();
       } catch(IOException e) {
         e.printStackTrace();
       }
     }
    try {
         sessionListener.thread = null;
         inputListener.thread = null;
         serverSocket.close();
         new Exit().execute(null);
       } catch(IOException e) {
         e.printStackTrace();
       }
   }
 
   /**
    * Parses the command line arguments.
    * @param args the command line arguments
    * @return true if all arguments have been correctly parsed
    */
   private boolean parseArguments(final String[] args) {
     boolean ok = true;
 
     // loop through all arguments
     for(int a = 0; a < args.length; a++) {
       ok = false;
       if(args[a].startsWith("-")) {
         for(int i = 1; i < args[a].length(); i++) {
           final char c = args[a].charAt(i);
           if(c == 'p') {
             // parse server port
             if(++i == args[a].length()) {
               a++;
               i = 0;
             }
             if(a == args.length) break;
             final int p = Token.toInt(args[a].substring(i));
             if(p <= 0) {
               BaseX.errln(SERVERPORT + args[a].substring(i));
               break;
             }
             Prop.port = p;
             i = args[a].length();
             ok = true;
           } else if(c == 'd') {
             Prop.debug = true;
             ok = true;
           } else if(c == 'v') {
             verbose = true;
             ok = true;
           } else {
             break;
           }
         }
       }
       if(!ok) break;
     }
     if(!ok) BaseX.errln(SERVERINFO);
     return ok;
   }
   
   /**
    * InputListener listens to the Console Input.
    * 
    * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
    * @author Andreas Weiler
    *
    */
   class InputListener implements Runnable {
     
     /** Thread. */
     Thread thread = null;
     
     /**
      * Starts the Thread.
      */
     public synchronized void start() {
       if (thread == null) {
         thread = new Thread(this);
         thread.start();
       }
     }
 
     public void run() {
       while(thread != null) {
      // get user input
         try {
           final InputStreamReader isr = new InputStreamReader(System.in);
           String com = new BufferedReader(isr).readLine().trim();
           if(com.equals("stop")) {
             stop();
           } else if(com.equals("list")) {
             int size = sessions.size();
             BaseX.outln("Number of Clients: " + size);
             BaseX.outln("List of Clients:");
             for(int i = 0; i < size; i++) {
               Session session = sessions.get(i);
               String dbname = "No Database opened.";
               if(session.context.db()) {
               dbname = session.context.data().meta.dbname;
               }
               BaseX.outln("Client " + session.clientId + ": " + dbname);
             }
           } else {
             BaseX.outln("No such command");
           }
         } catch(final Exception ex) {
           // also catches interruptions such as ctrl+c, etc.
           BaseX.outln();
         }
       }
     } 
   }
   
   /**
    * SessionListener listens to new Client-Server Sessions.
    * 
    * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
    * @author Andreas Weiler
    *
    */
   class SessionListener implements Runnable {
     
     /** Thread. */
     Thread thread = null;
     /** BaseXServerNew. */
     BaseXServerNew bx;
    
     /**
      * Constructor.
      * @param b BaseXServerNew
      */
     public SessionListener(final BaseXServerNew b) {
       this.bx = b;
     }
     
     /**
      * Starts the Thread.
      */
     public synchronized void start() {
       if (thread == null) {
         thread = new Thread(this);
         thread.start();
       }
     }
 
     public void run() {
       while(thread != null) {
         Socket s;
         try {
           s = serverSocket.accept();
           lastid++;
           Session session = new Session(s, lastid, verbose, bx);
           session.start();
           sessions.add(session);
         } catch(IOException e) {
           if(e instanceof SocketException) {
             return;
           } else e.printStackTrace();
         }
       }
     }
   }
 }
