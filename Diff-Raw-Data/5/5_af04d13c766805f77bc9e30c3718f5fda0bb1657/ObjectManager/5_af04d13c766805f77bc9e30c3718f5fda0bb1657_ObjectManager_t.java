 package com.novel.odisp;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import com.novel.odisp.common.Dispatcher;
 import com.novel.odisp.common.Message;
 import com.novel.odisp.common.ODObject;
 import com.novel.odisp.common.ObjectManager;
 import com.novel.stdmsg.ODCleanupMessage;
 import com.novel.stdmsg.ODObjectLoadedMessage;
 
 /**   ODISP.
  * @author (C) 2004 <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
 * @version $Id: ObjectManager.java,v 1.30 2004/05/31 15:38:43 valeks Exp $
  */
 
 public class StandartObjectManager implements ObjectManager {
   /**  . */
   private Dispatcher dispatcher;
   /**   . */
   private DefferedMessages messages = new DefferedMessages();
   /**  . */
   private Map objects = new HashMap();
   /** . */
   private Logger log = Logger.getLogger("com.novel.odisp.StandartObjectManager");
   /**   . */
   private Map provided = new HashMap();
   /**   . */
   private List senderPool = new ArrayList();
   /**       . */
   public static final int SENDER_POOL_SIZE = 5;
   /**   . */
   private int objCount = 0;
   /**   . */
   private List messageStorage = new ArrayList();
 
   /**      .
    * @param service  
    * @param objectName  
    */
   public void addProvider(final String service, final String objectName) {
     if (!provided.containsKey(service)) {
       provided.put(service, new ArrayList());
     }
     ((List) provided.get(service)).add(objectName);
   }
 
   /**    .
    *           --    .
    * @param service  
    * @param objectName  
    */
   public void removeProvider(final String service, final String objectName) {
     if (provided.containsKey(service)) {
       ((List) provided.get(service)).remove(objectName);
       if (((List) provided.get(service)).size() == 0) {
 	provided.remove(service);
       }
     }
   }
 
   /**     .
    * @param service  
    * @return   
    */
   private boolean hasProviders(final String service) {
     return provided.containsKey(service);
   }
 
   /**   - .
    * @param service  
    * @return  thread-safe  
    */
   private List getProviders(final String service) {
     if (provided.containsKey(service)) {
       return Collections.unmodifiableList(Collections.synchronizedList((List) provided.get(service)));
     } else {
       return null;
     }
   }
 
   /**    .
    * @return   
    */
   public List getProviding() {
     return new ArrayList(Collections.unmodifiableSet(provided.keySet()));
   }
 
   /**         . */
   public final void loadPending() {
     // resources
     Map resourceList = new HashMap(dispatcher.getResourceManager().getResources());
     Iterator it = resourceList.keySet().iterator();
     while (it.hasNext()) {
       String objectName = (String) it.next();
       log.fine("added resource provider " + objectName);
       if (!hasProviders(objectName)) {
 	//       
 	addProvider(objectName, objectName);
       }
     }
     int loaded = 0;
     Map localObjects = null;
     synchronized (objects) {
       localObjects = new HashMap(objects);
     }
     it = localObjects.keySet().iterator();
     while (it.hasNext()) {
       String objectName = (String) it.next();
       ObjectEntry oe = (ObjectEntry) objects.get(objectName);
       if (oe.isLoaded()) {
 	continue;
       }
       log.config("trying to load object " + objectName);
       int numRequested = oe.getDepends().length;
       for (int i = 0; i < oe.getDepends().length; i++) {
 	if (hasProviders(oe.getDepends()[i])) {
 	  numRequested--;
 	} else {
 	  log.finer("dependency not met: " + oe.getDepends()[i]);
 	}
       }
       if (numRequested == 0) {
 	for (int i = 0; i < oe.getProvides().length; i++) {
 	  log.fine("added as provider of " + oe.getProvides()[i]);
 	  addProvider(oe.getProvides()[i], oe.getObject().getObjectName());
 	}
         oe.setLoaded(true);
         flushDefferedMessages(oe.getObject().getObjectName());
 	log.config(" ok. loaded = " + objectName);
 	Message m = new ODObjectLoadedMessage(objectName);
 	oe.getObject().handleMessage(m);
 	loaded++;
       }
     }
     if (loaded > 0) {
       loadPending();
     }
     /*  
     synchronized (objects) {
       log.fine("Penging objects: " + pendingObjects);
       Iterator lit = pendingObjects.iterator();
       while (lit.hasNext()) {
 	String objectName = (String) lit.next();
 	ObjectEntry oe = (ObjectEntry) objects.get(objectName);
 	for (int i = 0; i < oe.getProvides().length; i++) {
 	  log.fine(oe.getObject().getObjectName() + " added as provider of " + oe.getProvides()[i]);
 	  addProvider(oe.getProvides()[i], oe.getObject().getObjectName());
 	}
         oe.setLoaded(true);
         flushDefferedMessages(oe.getObject().getObjectName());
 	log.config(" ok. loaded = " + objectName);
 	Message m = new ODObjectLoadedMessage(objectName);
 	oe.getObject().handleMessage(m);
       }
       pendingObjects.clear();
     }
      */
   }
 
   /**    ( ޣ ).
    * @param cName   
    * @param configuration   
    */
   public final void loadObject(final String cName, final Map configuration) {
     log.config("loading object " + cName);
     try {
       Object[] params = new Object[1];
       params[0] = new Integer(objCount++);
       Class[] dParams = new Class[1];
       dParams[0] = params[0].getClass();
       ODObject load =
 	(ODObject) Class.forName(cName).getConstructor(dParams).newInstance(params);
       load.setDispatcher(dispatcher);
       load.setConfiguration(configuration);
       synchronized (objects) {
 	ObjectEntry oe =
 	  new ObjectEntry(cName, load.getDepends(), load.getProviding());
 	oe.setObject(load);
 	oe.setLoaded(false);
 	objects.put(load.getObjectName(), oe);
 	/**   :
 	 *      -     
 	 *               -   
 	 * -------------''------------     -    
 	 *             (  depends  )
 	 *  watermark    
 	 */
 	/*  
 	if (oe.getDepends().length == 0) {
 	  pendingObjects.add(0, load.getObjectName());
 	  log.fine("free object " + load.getObjectName() + " added at the top of pending list");
 	} else {
 	  boolean found = false;
 	  ListIterator lit = pendingObjects.listIterator(pendingObjects.size());
 	  while (lit.hasPrevious()) {
 	    String objectName = (String) lit.previous();
 	    ObjectEntry tmpoe = (ObjectEntry) objects.get(objectName);
 	    List waiting = new ArrayList(Arrays.asList(oe.getDepends()));
 	    waiting.retainAll(Arrays.asList(tmpoe.getProvides()));
 	    if (waiting.size() > 0) {
 	      log.fine("object " + load.getObjectName() + " added after it's last provider: " + tmpoe.getObject().getObjectName());
 	      lit.add(load.getObjectName());
 	      found = true;
 	      break;
 	    }
 	  }
 	  if (!found) {
 	    log.fine("non-free object " + load.getObjectName() + " added at the bottom of pending list");
 	    pendingObjects.add(load.getObjectName());
 	  }
 	 */
       }
     } catch (InvocationTargetException e) {
       log.warning(" failed: " + e + " cause: " + e.getTargetException());
       e.getTargetException().printStackTrace();
     } catch (NoSuchMethodException e) {
       log.warning(" failed: " + e);
     } catch (ClassNotFoundException e) {
       log.warning(" failed: " + e);
     } catch (InstantiationException e) {
       log.warning(" failed: " + e);
     } catch (IllegalAccessException e) {
       log.warning(" failed: " + e);
     } catch (IllegalArgumentException e) {
       log.warning(" failed: " + e);
     }
   }
 
   /**      .
    * ,    :
    * <ul>
    * <li>    
    * <li>   
    * <li>   
    * </ul>
    * @param objectName     .
    * @param code   ( code != 0  
    *  ).
    */
   public synchronized final void unloadObject(final String objectName, final int code) {
     if (objects.containsKey(objectName)) {
       ObjectEntry oe = (ObjectEntry) objects.get(objectName);
       String[] provides = oe.getProvides();
       Iterator it = objects.keySet().iterator();
       List dependingObjs = new ArrayList();
 
       while (it.hasNext()) {
 	String className = (String) it.next();
 	String[] depends = ((ObjectEntry) objects.get(className)).getDepends();
 	for (int i = 0; i < provides.length; i++) {
 	  for (int j = 0; j < depends.length; j++) {
 	    if (provides[i].equals(depends[j])
 		&& !dependingObjs.contains(className)) {
 	      dependingObjs.add(className);
 	    }
 	  }
 	  //       
 	  removeProvider(provides[i], objectName);
 	}
       }
       if (code == 0) {
 	it = dependingObjs.iterator();
 	while (it.hasNext()) {
 	  String className = (String) it.next();
 	  if (objects.containsKey(className)) {
 	    log.fine("removing " + objectName + "'s dependency " + className);
 	    unloadObject(className, code);
 	  }
 	}
       }
       ODObject obj = oe.getObject();
       ODCleanupMessage m = new ODCleanupMessage(objectName, 0);
       m.setReason(code);
       dispatcher.send(m);
       objects.remove(objectName);
       log.config("\tobject " + objectName + " unloaded");
     }
   }
 
   /**    . 
    * @return  
    */
   public final Map getObjects() {
     return objects;
   }
 
   /**  .
    * @param newDispatcher      
    */
   public StandartObjectManager(final Dispatcher newDispatcher) {
     dispatcher = newDispatcher;
     for (int i = 0; i < SENDER_POOL_SIZE; i++) {
       senderPool.add(new Sender(this));
     }
   }
 
   /**    .
    * @param objectName  
    * @param message 
    */
   private void sendToObject(final String objectName, final Message message) {
     ObjectEntry oe = null;
     //     
     synchronized (objects) {
       oe = (ObjectEntry) objects.get(objectName);
     }
     if(oe == null) {
       return;
     }
     ODObject objToSendTo = null;
     //     
     synchronized (oe) {
       if (!oe.isLoaded()) {
 	log.finest("deffered message " + message.getAction() + " for " + objectName);
 	messages.addMessage(objectName, message);
 	return;
       }
       objToSendTo = oe.getObject();
     }
     synchronized (messageStorage) {
 		messageStorage.add(new SendRecord(message, objToSendTo));
     }
    }
 
   /**     .
    * @param message 
    */
   public final void send(Message message) {
     if (message == null
 	|| message.getAction().length() == 0
 	|| !message.isCorrect()) {
       return;
     }
     // ,      
     //      -  ;-))) 
     List recipients = null;
     //   .
     boolean serviceMatch = true;
     //        
     // --   ,    
     if (hasProviders(message.getDestination())) {
       List providers = new ArrayList(getProviders(message.getDestination()));
       if (providers != null) {
 	recipients = providers;
       }
     }
     if (recipients == null){
       serviceMatch = false;
       recipients = new ArrayList();
       synchronized (objects) {
         Iterator it = objects.keySet().iterator();
         while (it.hasNext()) {
 	  String key = (String) it.next();
 	  ObjectEntry oe = (ObjectEntry) objects.get(key);
 	  if (Pattern.matches(oe.getObject().getMatch(), message.getDestination())
 	      || Pattern.matches(message.getDestination(), oe.getObject().getObjectName())) {
 	    recipients.add(key);
 	  }
         }
       }
     }
     Iterator it = recipients.iterator();
     while (it.hasNext()) {
       String objectName = (String) it.next();
       if (serviceMatch) {
         message.setDestination(objectName);
       }
       sendToObject(objectName, message);
     }
   }
 
   /**        .
    * @param objectName  
    */
   private void flushDefferedMessages(final String objectName) {
     if (!objects.containsKey(objectName)) {
 		return;
     }
     List toFlush = messages.flush(objectName);
     Iterator it = toFlush.iterator();
     while (it.hasNext()) {
      sendToObject(objectName, (Message) it.next());
     }
     loadPending();
   }
   
   /**     . */
   public final SendRecord getNextPendingMessage() {
   	SendRecord toSend = null;
   	synchronized (messageStorage) {
   		if (messageStorage.size() > 0) {
   			toSend = (SendRecord) messageStorage.get(0);
   			messageStorage.remove(0);
   		}
   	}
   	return toSend;
   }
 } // StandartObjectManager
