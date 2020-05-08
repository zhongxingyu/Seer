 package com.lastcrusade.fanclub.net;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.lastcrusade.fanclub.net.message.IMessage;
 
 /**
 * An Android Handler used as a fanClub network message dispatch.  This will receive Android Message objects that represent
 * network messages, and dispatch them to the appropriate handler.
  * 
  * @author Jesse Rosalia
  *
  */
 public class MessageThreadMessageDispatch extends Handler {
 
     public interface IMessageHandler<T extends IMessage> {
         /**
          * Handle an incoming message from a remote connection.
          * 
          * @param messageNo A monotonically increasing message counter
         * @param message A message object.  The type of this object is specified through generic type parameters 
         * @param fromAddr The MAC address of the remote device (e.g. the Bluetooth address).  It will be in the form
         * xx:yy:zz:aa:bb:cc, made up of hexadecimal digits.
          */
         public void handleMessage(int messageNo, T message, String fromAddr);
     }
 
     private static final String TAG = "MessageThreadMessageDispatch";
 
     private Map<Class<? extends IMessage>, IMessageHandler<? extends IMessage>> dispatchMap =
             new HashMap<Class<? extends IMessage>, IMessageHandler<? extends IMessage>>();
 
     /**
      * Register a message handler with the dispatch.  This handler will get called
      * when a message is received.
      * 
      * NOTE: only one handler per message, per dispatch.
      * 
      * @param messageClass
      * @param handler
      */
     public <T extends IMessage> void registerHandler(Class<T> messageClass, IMessageHandler<T> handler) {
         dispatchMap.put(messageClass, handler);
     }
     
     @Override
     public void handleMessage(Message msg) {
         IMessageHandler handler   = null;
         IMessage        message   = null;
         int             messageNo = 0;
         String          fromAddr  = null;
         
         if (msg.what == MessageThread.MESSAGE_READ) {
             messageNo = msg.arg1;
             message   = (IMessage)msg.obj;
             handler   = dispatchMap.get(message.getClass());
             fromAddr  = msg.getData().getString(MessageThread.EXTRA_ADDRESS);
         }
         
         if (message != null && handler != null) {
             Log.w(TAG, "Message received: " + messageNo);
             handler.handleMessage(messageNo, message, fromAddr);
         } else {
             // default...call the base class
             super.handleMessage(msg);
         }
     }
     
 }
