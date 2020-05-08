 package test.org.valabs.odisp;
 
 import junit.framework.TestCase;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.SessionManager;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.MessageHandler;
 import org.valabs.stdmsg.StandartMessage;
 
 
 /**     .
 * @author (C) 2004 <a href="mailto:valeks@novel-il.ru">  .</a>
 * @version $Id: TestSessionManager.java,v 1.2 2004/12/02 22:08:59 valeks Exp $
  */
 public class TestSessionManager extends TestCase {
   /**    --    . */
   public void testSimpleHandler() {
     SessionManager sm = SessionManager.getSessionManager();
     MessageHandler mh = new MessageHandler() {
       public void messageReceived(final Message msg) {
         
       }
     };
     sm.addMessageListener(UUID.getNullUUID(), mh);
     Message m = new StandartMessage();
     m.setReplyTo(UUID.getNullUUID());
     assertEquals(true, sm.processMessage(m));
     assertEquals(false, sm.processMessage(m));
   }
   
   /**       . */
   public void testMultiplyCalls() {
     SessionManager sm = SessionManager.getSessionManager();
     MessageHandler mh = new MessageHandler() {
       public void messageReceived(final Message msg) {
         
       }
     };
     sm.addMessageListener(UUID.getNullUUID(), mh, true);
     Message m = new StandartMessage();
     m.setReplyTo(UUID.getNullUUID());
     assertEquals(true, sm.processMessage(m) && sm.processMessage(m));
     sm.removeMessageListener(UUID.getNullUUID(), mh);
     assertEquals(false, sm.processMessage(m));
   }
   
   /**         , 
    * SessionManager' (    ). */
   public void testModifySMFromHandler() {
     SessionManager sm = SessionManager.getSessionManager();
     MessageHandler mh = new MessageHandler() {
       public void messageReceived(final Message msg) {
         SessionManager.getSessionManager().addMessageListener(UUID.getNullUUID(), new MessageHandler() {
           public void messageReceived(final Message msg) {
             
           }
         });
       }
     };
     sm.addMessageListener(UUID.getNullUUID(), mh);
     Message m = new StandartMessage();
     m.setReplyTo(UUID.getNullUUID());
     assertEquals(true, sm.processMessage(m));
     assertEquals(true, sm.processMessage(m));
     assertEquals(false, sm.processMessage(m));
   }
   
   public static void main(String[] args) {
     junit.textui.TestRunner.run(TestSessionManager.class);
   }
 
 }
