 package org.valabs.odisp;
 
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.valabs.odisp.common.Dispatcher;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.MessageHandler;
 
 
 /** 
  *        .   
  *  UpdaterGUI.java.       javadoc  .
  * 
  * @author <a href="dron@novel-il.ru"> . </a>
  * @author (C) 2005  "-"
 * @version $Id: SessionQueue.java,v 1.3 2005/10/13 12:11:17 dron Exp $
  */
 public class SessionQueue implements Runnable {
   private List messages = new ArrayList();
   private int counter = 0;
   private SessionPacketReply spr;
   
   Logger logger = Logger.getLogger(SessionPacketReply.class.getName());
   SessionManager sm = SessionManager.getSessionManager();
   Dispatcher dispatcher;
   
   /**
    * . 
    *
    * @param _dispatcher
    */
   public SessionQueue(Dispatcher _dispatcher) {
     dispatcher = _dispatcher;
     spr = new SessionPacketReply(this, sm);
   }
   
   /**
    *    .
    * 
    * @param msg
    * @param mh_reply
    */
   public void addMessage(Message msg, MessageHandlerEx mh_reply) {
     if (mh_reply == null) {
       throw new InvalidParameterException("Null mh_reply passed.");
     }
     MessageData newPacket = new MessageData();
     newPacket.msg = msg;
     newPacket.mh_reply = mh_reply;
     messages.add(newPacket);
   }
   
   public void addMessageAfterCurrent(Message msg, MessageHandlerEx mh_reply) {
     if (mh_reply == null) {
       throw new InvalidParameterException("Null mh_reply passed.");
     }
     MessageData newPacket = new MessageData();
     newPacket.msg = msg;
     newPacket.mh_reply = mh_reply;
    messages.add(0, newPacket);
   }
   
   public int getCurrentState() {
     return counter;
   }
   
   public MessageData getNextMessage() {
     return (messages.size() > 0) ? (MessageData) messages.get(0) : null;
   }
   
   public void run() {
     synchronized (messages) {
       spr.currentData = getNextMessage();
       if (spr.currentData != null) {
         messages.remove(0);
         if (spr.currentData.msg != null) {
           sm.addMessageListener(spr.currentData.msg.getId(), spr);
           dispatcher.send(spr.currentData.msg);
         } else {
           spr.messageReceived(null);
         }
         counter++;
       }
     }
   }
   
   /**
    *   .      
    *  {@link SessionQueue}.
    */
   public interface MessageHandlerEx {
     public int messageReceived(Message msg, SessionQueue sq);
   }
   
   /**
    *   .
    */
   public class MessageData {
     public Message msg;
     public MessageHandlerEx mh_reply;
   }
   
   /**
    *    SessionManager'.
    */
   class SessionPacketReply implements MessageHandler {
     public SessionQueue sp;
     public SessionManager sm;
     public MessageData currentData;
     
     public SessionPacketReply(SessionQueue _sp, SessionManager _sm) {
       sp = _sp;
       sm = _sm;
     }
     
     public void messageReceived(Message msg) {
       logger.finest("[DBG]: SPR, SM_reply, currentState = " + sp.getCurrentState());
       int errCode = -1;
       if ((errCode = currentData.mh_reply.messageReceived(msg, sp)) == 0) {
         sp.run();
       } else {
         logger.warning("SPR, SM_reply: handler return " + errCode + "\n" +
                 " + Session aborted at currentState = " + getCurrentState() + "\n" +
                 " + NextMessage = " + getNextMessage());
       }
     }
   }
 }
 
