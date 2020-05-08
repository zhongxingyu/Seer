 package org.valabs.odisp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.MessageHandler;
import org.valabs.stdmsg.ReplytimeoutMessage;
 import org.valabs.stdmsg.StandartMessage;
 
 /**
  * <h5> ODsessionManager':</h5>
  * <p>
  *       ,     ODISP (  
  * ).    :
  * 
  * <pre>
  *       #   : 
  *       Message newMsg = dispatcher.getNewMessage(...);
  *       ...
  *       ODSessionManager sm = ODSessionManager.getSessionManager();
  *       sm.add(newMsg.getId(), new MessageHandler() {
  *          public void messageReceived(Message msg) {
  *            System.out.println(&quot;Some reply-message received!&quot;);
  *          }
  *       });
  *       
  *       ...
  *       
  *       handleMessage(Message msg) {
  *         ODSessionManager sm = ODSessionManager.getSessionManager();
  *         if (!sm.processMessage(msg)) {
  *           System.out.println(&quot;Message processed by SessionManager&quot;);
  *         } else if (ODObjectLoaded.equals(msg)) {
  *           ...
  *         }
  *         ...
  *       }
  * </pre>
  * 
  * @author (C) 2004-2005 <a href="dron@novel-il.ru"> .  </a>
 * @version $Id: SessionManager.java,v 1.18 2005/09/28 09:56:11 dron Exp $
  */
 public class SessionManager {
   /** ,      . */
   public static final int DELAY_PERIOD = 2000;
   
   /**   ODSessionManager */
   private static SessionManager sessionManager = new SessionManager();
   /**  . */
   private final List handlers = Collections.synchronizedList(new ArrayList());
   /** */
   private Timer timer = new Timer(true);
 
   /**
    * .
    */
   protected SessionManager() {
     super();
     
     TimerTask timerTask = new TimerTask() {
       public void run() {
         synchronized (handlers) {
           Iterator it = handlers.iterator();
           while (it.hasNext()) {
             SessionRecord el = (SessionRecord) it.next();
             if (el.cycle != -1) el.cycle--;
             if (el.cycle == 0) {
               /*
                *    dispatcher  SessionManager,     
                * dispatcher'  .
                */
               Message timeout = new StandartMessage();
              ReplytimeoutMessage.setup(timeout, "", "sessionManager", el.getMsgId());
               el.messageHandler.messageReceived(timeout);
               it.remove();
             }
           }
         }
       }
     };
     timer.schedule(timerTask, 1000, DELAY_PERIOD);
   }
 
   /**
    *   .
    * 
    * @return  .
    */
   public static SessionManager getSessionManager() {
     return sessionManager;
   }
 
   /**
    *    . - --      .
    * 
    * @param messageId  ,   .
    * @see org.valabs.odisp.common.Message#getId()
    * @param messageHandler   .
    */
   public void addMessageListener(final UUID messageId, final MessageHandler messageHandler) {
     addMessageListener(messageId, messageHandler, false);
   }
 
   /**
    *     .       
    * (multiply   true)         SessionManager 
    * .  ,    ţ     removeMessageListener.
    * 
    * @param messageId   .
    * @param messageHandler  .
    * @param multiply  .
    * @see SessionManager#removeMessageListener(UUID, MessageHandler)
    */
   public final void addMessageListener(final UUID messageId, final MessageHandler messageHandler, final boolean multiply) {
     handlers.add(new SessionRecord(messageId, messageHandler, multiply, -1));
   }
   
   /**
    * 
    * @param messageId   .
    * @param messageHandler  .
    * @param timeoutCycle     ,  ̣ 
    */
   public final void addMessageListenerWithTimeout(final UUID messageId, final MessageHandler messageHandler, final int timeoutCycle) {
     handlers.add(new SessionRecord(messageId, messageHandler, false, timeoutCycle));
   }
 
   /**
    *    .
    * 
    * @param messageId   .
    * @param messageHandler  .
    * @see SessionManager#addMessageListener(UUID, MessageHandler) 
    * @see SessionManager#addMessageListener(UUID, MessageHandler, boolean)
    */
   public final void removeMessageListener(final UUID messageId, final MessageHandler messageHandler) {
     synchronized (handlers) {
       final Iterator handlerIt = handlers.iterator();
       while (handlerIt.hasNext()) {
         final SessionRecord element = (SessionRecord) handlerIt.next();
         if (element.getMsgId().equals(messageId) && element.getMessageHandler().equals(messageHandler)) {
           handlerIt.remove();
           break;
         }
       }
     }
   }
 
   /**
    *    Odisp.    messageRecevied  handleMessage.
    * 
    * @param msg  Odisp
    * @return          ,   true, 
    *            ,        (
    *          ).
    */
   public final boolean processMessage(final Message msg) {
     boolean matched = false;
     final List toPerform = new ArrayList();
     final List toRemove = new ArrayList();
     Iterator commonIt;
     synchronized (handlers) {
 		commonIt = handlers.iterator();
 		while (commonIt.hasNext()) {
 			final SessionRecord element = (SessionRecord) commonIt.next();
 			if (element.getMsgId().equals(msg.getReplyTo())) {
 				toPerform.add(element);
 				if (!element.isMultiply()) {
 					toRemove.add(element);
 				}
 				matched = true;
 			}
 		}
 
 		commonIt = toRemove.iterator();
 		while (commonIt.hasNext()) {
 			final Object element = commonIt.next();
 			handlers.remove(element);
 		}
 	}
 
     commonIt = toPerform.iterator();
     while (commonIt.hasNext()) {
       final SessionRecord element = (SessionRecord) commonIt.next();
       element.getMessageHandler().messageReceived(msg);
     }
     return matched;
   }
 
   static class SessionRecord {
 
     private final UUID msgId;
 
     private final MessageHandler messageHandler;
 
     private final boolean multiply;
     
     private int cycle;
 
     SessionRecord(UUID _msgId, MessageHandler _messageHandler, boolean _multiply, int _cycle) {
       msgId = _msgId;
       messageHandler = _messageHandler;
       multiply = _multiply;
       cycle = _cycle;
     }
 
     MessageHandler getMessageHandler() {
       return messageHandler;
     }
 
     UUID getMsgId() {
       return msgId;
     }
 
     boolean isMultiply() {
       return multiply;
     }
     
     int getCycle() {
       return cycle;
     }
   }
 }
