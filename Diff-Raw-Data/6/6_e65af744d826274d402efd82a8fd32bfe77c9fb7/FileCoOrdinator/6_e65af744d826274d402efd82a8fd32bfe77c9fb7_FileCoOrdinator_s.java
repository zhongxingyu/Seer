 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aether.io;
 
 import aether.conf.ConfigMgr;
 import aether.net.ControlMessage;
 import aether.net.NetMgr;
 import aether.repl.Replication;
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 /**
  *
  * @author aniket
  */
 public class FileCoOrdinator implements Runnable {
 
     
     private int port;
     private NetMgr netMgr;
     private boolean isOne = false;
     private static final Logger log = 
             Logger.getLogger(DataMgr.class.getName());
     
     
     
     static {
         log.setLevel(Level.INFO);
         ConsoleHandler handler = new ConsoleHandler();
         handler.setFormatter(new SimpleFormatter());
         handler.setLevel(Level.ALL);
         log.addHandler(handler);
     }
     
     
     public FileCoOrdinator () 
             throws SocketException {
         
         port = ConfigMgr.getCoOrdPort();
         if (isOne) {
             log.warning("One File co-ordinator is already running");
             throw new UnsupportedOperationException();
         }
         if (ConfigMgr.getIfDebug()) {
                 log.setLevel(Level.FINE);
             }
         netMgr = new NetMgr(port);
         isOne = true;
     }
     
     
     
     
     /**
      * Take a list of chunk ids and return a string of ids separated by a :
      * @param list  Chunk id list
      * @return  String of ids separated by :
      */
     private String prepareChunkListPayload (Integer[] list) {
         
         String s = "";
         for (Integer i:list) {
             s = s + ":" + i.toString();
         }
         
         return s;
     }
     
     
     
     @Override
     public void run() {
         
         log.fine("Lauching file co-ordinator");
         while (true) {
             
             try {
                 
                 ControlMessage req = (ControlMessage) netMgr.receive();
                 String file = req.ParseEControl();
                 log.log(Level.FINE, "Received query for chunks of file {0}",
                         file);
                 Integer[] chunks = null;
                 
                 Replication repl = Replication.getInstance();
                 chunks = repl.getChunkIds(file);
                  
                 
                 if (chunks != null && chunks.length > 0) {
                     /* 
                      * Acknowledge that we have this file
                      */
                     ControlMessage ack = new ControlMessage ('k', 
                             req.getSourceIp(), prepareChunkListPayload(chunks));
                    netMgr.send(ack);
                 }
                 
                 
             } catch (SocketTimeoutException s) {
 
                 // do nothing here
             } catch (IOException ex) {
                 
                 log.severe("I/O Exception while doing file co-ordination");
                 ex.printStackTrace();
             }
         }
     }
     
 }
