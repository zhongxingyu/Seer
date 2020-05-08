 package polly.telnet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 
 import polly.PollyConfiguration;
 
 import de.skuzzle.polly.sdk.IrcManager;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 
 
 public class TelnetConnection implements Runnable {
     
     private static Logger logger = Logger.getLogger(TelnetConnection.class.getName());
     private static AtomicInteger connectionId = new AtomicInteger();
     
     private int id;
     private Socket socket;
     private AtomicBoolean closing;
     private BufferedReader input;
     private OutputStream output;
     
     private PollyConfiguration config;
     private IrcManager ircManager;
     private MessageListener handler;
     
     
     
     public TelnetConnection(Socket socket, PollyConfiguration config, 
             IrcManager ircManager, MessageListener handler) {
         
         this.id = TelnetConnection.connectionId.incrementAndGet();
         this.socket = socket;
         this.closing = new AtomicBoolean(false);
         
         this.config = config;
         this.handler = handler;
         this.ircManager = ircManager;
         logger.debug("TelnetConnection with id " + this.id + " created.");
         
         try {
             this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             this.output = socket.getOutputStream();
             this.send("Welcome to polly telnet service.");
             this.send("Type /quit to close this connection.");
         } catch (IOException e) {
             logger.error("Failed to create input streams.", e);
             this.close();
         }
     }
     
     
     
     public void close() {
        if (this.closing.get()) {
            return;
        }
         this.closing.set(true);
         logger.info("Closing telnet connection with id " + this.id);
         try {
             this.socket.close();
         } catch (IOException e) {
             logger.error("", e);
         }
     }
     
     
     
     public synchronized void send(String message) {
        if (this.closing.get()) {
            return;
        }
         try {
             byte[] bytes = message.getBytes();
             this.output.write(System.getProperty("line.separator", "\n").getBytes());
             this.output.write(bytes);
             this.output.write(System.getProperty("line.separator", "\n").getBytes());
             this.output.flush();
         } catch (IOException e) {
             e.printStackTrace();
             logger.error("Error while sending to connection " + this.id, e);
             this.close();
         }
     }
     
     
     
     private String readInput() throws IOException {
         String line = this.input.readLine();
         if (line == null) {
             return null;
         }
         StringBuilder b = new StringBuilder(line.length());
         for (char c : line.toCharArray()) {
             if (c > 31 && c < 127) {
                 b.append(c);
             }
         }
         return b.toString();
     }
 
     
     
     public boolean isConnected() {
         return !this.closing.get() && this.socket.isConnected();
     }
     
     
 
     @Override
     public void run() {
         try {
             while (!this.closing.get()) {
                 String line = this.readInput();
                 
                 if (line == null || line.equals("/quit")) {
                     this.send("Invalid input. closing connection.");
                     this.close();
                 } else {
                     this.handler.privateMessage(this.getMessageEvent(line));
                 }
             }
         } catch (Exception e) {
             if (!this.closing.get()) {
                 this.close();
             } else {
                 logger.error(e);
             }
         }
     }
     
     
     
     private MessageEvent getMessageEvent(String input) {
         IrcUser u = new IrcUser(this.config.getAdminUserName(), "", "");
         return new MessageEvent(this.ircManager, u, 
             this.config.getAdminUserName(), input);
     }
 }
