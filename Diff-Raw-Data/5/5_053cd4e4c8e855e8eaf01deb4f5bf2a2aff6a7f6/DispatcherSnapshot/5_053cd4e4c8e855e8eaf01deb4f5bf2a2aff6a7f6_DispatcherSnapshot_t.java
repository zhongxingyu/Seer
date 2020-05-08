 package org.valabs.odisp.standart;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 /**
  *        .
  * 
  * @author <a href="mailto:valeks@valabs.spb.ru">  .</a>
 * @version $Id: DispatcherSnapshot.java,v 1.5 2005/11/14 22:04:36 valeks Exp $
  */
 class DispatcherSnapshot {
   private static final String SNAP_NAME = "restart.snap";
   private Map objectSnapshots = new HashMap();
   private List messageQueue = new ArrayList();
   private final static Logger log = Logger.getLogger(DispatcherSnapshot.class.getName());
   /**
    * 
    */
   public DispatcherSnapshot() {
     try {
       final ObjectInputStream snapFile = new ObjectInputStream(new FileInputStream(SNAP_NAME));
       objectSnapshots = (Map) snapFile.readObject();
       messageQueue = (List) snapFile.readObject();
       new File(SNAP_NAME).delete();
     } catch (FileNotFoundException e) {
      log.info("No snapshot found. Starting from scratch.");
     } catch (IOException e) {
       log.warning("System restart snapshot exists but it could not be loaded. Starting from scratch.");
     } catch (ClassNotFoundException e) {
       log.warning("System restart snapshot exists but it could not be loaded. Starting from scratch.");
     }
     Runtime.getRuntime().addShutdownHook(new Thread() {
       public final void run() {
         if (objectSnapshots.size() > 0) {
           try {
             final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SNAP_NAME));
             out.writeObject(objectSnapshots);
             out.writeObject(messageQueue);
             out.close();
             log.info("System snapshot written.");
           } catch (IOException e) {
             log.warning("Unable to write system snapshot!");
           }
         }
       }
     });
   }
   
   public void clearSnapshot() {
     objectSnapshots.clear();
     messageQueue.clear();
   }
 
   public final void addObjectSnapshot(final String objectName, final Map options) {
     if (options != null) {
       objectSnapshots.put(objectName, options);
       log.info("Saving object " + objectName + " into snapshot database ["+ options.size() +" states].");
     }
   }
   
   public void setMessageQueue(final List queueMessages) {
     messageQueue.clear();
     messageQueue.addAll(queueMessages);
   }
   
   public List getMessageQueue() {
     return messageQueue;
   }
   
   public Map getObjectSnapshot(final String objectName) {
     return (Map) objectSnapshots.get(objectName);
   }
 
   /**
    * @return true -  , false - .
    */
   public boolean hasSnapshot() {
     return objectSnapshots.size() > 0 || messageQueue.size() > 0;
   }
 }
