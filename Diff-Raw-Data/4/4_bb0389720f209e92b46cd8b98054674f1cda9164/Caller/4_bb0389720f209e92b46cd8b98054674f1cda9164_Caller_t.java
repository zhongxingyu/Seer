 package org.agilewiki.pamailbox;
 
import java.util.Collection;
 import java.util.concurrent.Semaphore;
 
 import org.agilewiki.pactor.Mailbox;
 
 final class Caller implements MessageSource {
     private final Semaphore done = new Semaphore(0);
     private transient Object result;
 
     public Object call() throws Exception {
         done.acquire();
         if (result instanceof Exception)
             throw (Exception) result;
         if (result instanceof Error)
             throw (Error) result;
         return result;
     }
 
     @Override
     public void incomingResponse(final Message message,
             final Mailbox responseSource) {
         this.result = message.getResponse();
         done.release();
     }
 
     @Override
     public boolean buffer(final Message message, final MessageSource target) {
         return false;
     }
 
     /* (non-Javadoc)
      * @see org.agilewiki.pamailbox.MessageSource#addUnbufferedMessages(java.lang.Iterable)
      */
     @Override
    public void addUnbufferedMessages(final Collection<Message> messages)
             throws Exception {
         throw new UnsupportedOperationException();
     }
 }
