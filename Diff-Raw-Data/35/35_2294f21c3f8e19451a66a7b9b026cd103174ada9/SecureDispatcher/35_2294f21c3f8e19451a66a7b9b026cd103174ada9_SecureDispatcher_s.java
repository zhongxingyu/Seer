 package org.valabs.odisp.standart;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.ConfigurationManager;
 import org.valabs.odisp.common.Dispatcher;
 import org.valabs.odisp.common.ExceptionHandler;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.ObjectManager;
 import org.valabs.odisp.common.ResourceManager;
 import org.valabs.odisp.common.SecurityManager;
 
 
 /** £      .
  * @author <a href="mailto:valeks@valabs.spb.ru">  .</a>
 * @version $Id: SecureDispatcher.java,v 1.3 2005/02/27 12:37:31 valeks Exp $
  */
 final class SecureDispatcher implements Dispatcher {
   private final Dispatcher realDispatcher;
   private final String objectName;
   
   SecureDispatcher(final Dispatcher _realDispatcher, final String _objectName) {
     realDispatcher = _realDispatcher;
     objectName = _objectName;
   }
   
   public void send(final Message message) {
    if (message == null) {
      return;
    }
    if (message.getOrigin() == null) {
      message.setOrigin(objectName);
    }
    assert message.getOrigin().equals(objectName) : "Message origin faked.";
     realDispatcher.send(message);
   }
 
   public void send(final Message[] messageList) {
     if (messageList == null) {
       return;
     }
     for (int i = 0; i < messageList.length; i++) {
       send(messageList[i]);
     }
   }
 
   public void send(final List messageList) {
     if (messageList == null) {
       return;
     }
     final Iterator it = messageList.iterator();
     while (it.hasNext()) {
       send((Message) it.next());
     }
   }
 
   public final Message getNewMessage(final String action, final String destination, final String origin,
           final UUID inReplyTo) {
     assert origin.equals(objectName) : "Message origin faked.";
     return realDispatcher.getNewMessage(action, destination, origin, inReplyTo);
   }
 
   public Message getNewMessage() {
     return realDispatcher.getNewMessage();
   }
 
   public ObjectManager getObjectManager() {
     return realDispatcher.getObjectManager();
   }
 
   public ResourceManager getResourceManager() {
     return realDispatcher.getResourceManager();
   }
 
   public SecurityManager getSecurityManager() {
     return realDispatcher.getSecurityManager();
   }
 
   public ConfigurationManager getConfigurationManager() {
     return realDispatcher.getConfigurationManager();
   }
 
   public ExceptionHandler getExceptionHandler() {
     return realDispatcher.getExceptionHandler();
   }
 
   public void addSecurityManager(final SecurityManager additionalSecurityManager) {
     realDispatcher.addSecurityManager(additionalSecurityManager);
   }
 
   public void addExceptionHandler(final ExceptionHandler ex) {
     realDispatcher.addExceptionHandler(ex);
   }
 
   public void addConfigurationManager(final ConfigurationManager cman) {
     realDispatcher.addConfigurationManager(cman);
   }
 
 }
