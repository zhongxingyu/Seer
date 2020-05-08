 package suncertify.socket.server;
 
 import static suncertify.constants.Variables.TERMINATOR;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import suncertify.db.Data;
 import suncertify.db.DuplicateKeyException;
 import suncertify.db.RecordNotFoundException;
 import suncertify.parser.PropertiesLoader;
 import suncertify.socket.MessageHelper;
 import suncertify.socket.MessageType;
 
 public class Server {
   private static final Logger log = LoggerFactory.getLogger(Server.class);
   private ServerSocket serverSocket;
   private Data data = Data.getInstance();
 
   // TODO: replace for throw new MyIOException
   public Server(int port) throws IOException {
     serverSocket = new ServerSocket(port);
     log.info("Listening for clients on " + port + "....");
     int count = 0;
     while (true) {
       Socket clientSocket = serverSocket.accept();
       ServerThread thread = new ServerThread(clientSocket, count++);
       thread.start();
     }
   }
 
   // TODO: delete after
   public static void main(String[] args) throws IOException {
     new Server(Integer.parseInt(PropertiesLoader.getInstance().getDbPort()));
   }
 
   private class ServerThread extends Thread {
     private Socket clientSocket;
     private int count = -1;
     private ObjectOutputStream out;
     private ObjectInputStream in;
     private String message;
 
     private ServerThread(Socket clientSocket, int count) {
       this.clientSocket = clientSocket;
       this.count = count;
     }
 
     @Override
     public void run() {
       try {
         log.info("Connection received from " + clientSocket.getInetAddress().getHostName() + "; count: " + count);
         out = new ObjectOutputStream(clientSocket.getOutputStream());
         out.flush();
         in = new ObjectInputStream(clientSocket.getInputStream());
         do {
           try {
             // receive
             message = (String) in.readObject();
             log.info("server received:>" + message);
             if (message.startsWith(MessageType.READ.getName())) {
               long recNo = Long.parseLong(message.split(TERMINATOR)[1]);
               try {
                 sendMessage(MessageHelper.getReadResponseMessage(data.readRecord(recNo)));
               } catch (RecordNotFoundException e) {
                 sendMessage(MessageHelper.getNoRecordError());
               }
             } else if (message.startsWith(MessageType.FIND.getName())) {
               log.info("find message: " + message);
               String[] temp = message.split(TERMINATOR, -1);
               String[] criteria = { temp[1], temp[2], temp[3], temp[4], temp[5], temp[6] };
               sendMessage(MessageHelper.getFindByCriteriaResponseMessage(data.findByCriteria(criteria)));
             } else if (message.startsWith(MessageType.UPDATE.getName())) {
               String[] temp = message.split(TERMINATOR, -1);
               long recNo = Long.parseLong(temp[1]);
               String[] u_data = { temp[2], temp[3], temp[4], temp[5], temp[6], temp[7] };
               long lockCookie = Long.parseLong(temp[8]);
               try {
                 data.updateRecord(recNo, u_data, lockCookie);
                 sendMessage(MessageHelper.getUpdateResponseMessage());
               } catch (SecurityException e) {
                 sendMessage(MessageHelper.getSecurityError());
               } catch (RecordNotFoundException e) {
                 sendMessage(MessageHelper.getNoRecordError());
               }
             } else if (message.startsWith(MessageType.DELETE.getName())) {
               String[] temp = message.split(TERMINATOR, -1);
               long recNo = Long.parseLong(temp[1]);
               long lockCookie = Long.parseLong(temp[2]);
               try {
                 data.deleteRecord(recNo, lockCookie);
                 sendMessage(MessageHelper.getDeleteResponseMessage());
               } catch (SecurityException e) {
                 sendMessage(MessageHelper.getSecurityError());
               } catch (RecordNotFoundException e) {
                 sendMessage(MessageHelper.getNoRecordError());
               }
             } else if (message.startsWith(MessageType.CREATE.getName())) {
               String[] temp = message.split(TERMINATOR, -1);
               String[] c_data = { temp[1], temp[2], temp[3], temp[4], temp[5], temp[6] };
               try {
                 long recNo = data.createRecord(c_data);
                 sendMessage(MessageHelper.getCreateResponseMessage(recNo));
               } catch (DuplicateKeyException e) {
                 sendMessage(MessageHelper.getDuplicateError());
               }
             } else if (message.startsWith(MessageType.LOCK.getName())) {
               String[] temp = message.split(TERMINATOR, -1);
               long recNo = Long.parseLong(temp[1]);
               try {
                 long cookie = data.lockRecord(recNo);
                 log.info("Locked recNo: " + recNo + " by cookie: " + cookie);
                 sendMessage(MessageHelper.getLockResponseMessage(cookie));
               } catch (RecordNotFoundException e) {
                 sendMessage(MessageHelper.getNoRecordError());
               }
             } else if (message.startsWith(MessageType.UNLOCK.getName())) {
               String[] temp = message.split(TERMINATOR, -1);
               long recNo = Long.parseLong(temp[1]);
               long cookie = Long.parseLong(temp[2]);
               log.info("unlock recNo: " + recNo + ", cookie: " + cookie);
               try {
                 data.unlock(recNo, cookie);
               } catch (SecurityException e) {
                 sendMessage(MessageHelper.getSecurityError());
               }
             }
           } catch (ClassNotFoundException e) {
             log.error("Data received in unknown format", e);
           }
         } while (true);
       } catch (EOFException e) {
       } catch (IOException e) {
         log.error(e.getMessage(), e);
       } finally {
         try {
           in.close();
         } catch (IOException e) {
           log.error(e.getMessage(), e);
         }
         try {
           out.close();
         } catch (IOException e) {
           log.error(e.getMessage(), e);
         }
       }
     }
 
     private void sendMessage(String msg) {
       try {
         out.writeObject(msg);
         out.flush();
         log.info("server sends:> " + msg);
       } catch (IOException e) {
         log.error(e.getMessage(), e);
       }
     }
   }
 }
