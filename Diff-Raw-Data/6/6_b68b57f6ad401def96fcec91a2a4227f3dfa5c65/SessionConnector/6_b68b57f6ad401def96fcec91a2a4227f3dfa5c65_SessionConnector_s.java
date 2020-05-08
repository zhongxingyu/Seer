 package de.dsg.forge.nc.aspectDebugger.aspects;
 
 import com.quest.forge.data.Namespace;
 import com.quest.forge.data.Type;
 import com.quest.forge.ui.web.Session;
 import com.quest.forge.ui.web.queue.SessionQueue;
 import de.dsg.forge.nc.aspectDebugger.SessionRegistry;
 import de.dsg.forge.nc.aspectDebugger.data.ActiveSession;
 import de.dsg.forge.nc.aspectDebugger.data.SessionInformation;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.annotation.Pointcut;
 
 /**
  * This Aspect will connect to the queue management and make sure to track Session creation and deletion.
  * <p/>
  * <p/>
  * User: stefan
  * Date: 20.06.13
  * Time: 23:06
  * To change this template use File | Settings | File Templates.
  */
 
 @Aspect
 public class SessionConnector {
 
     ThreadLocal<String> _mainView = new ThreadLocal<String>();
 
 
     @Pointcut("within (com.quest.forge.ui.web.Session)")
     void pcWithinSession() {
     }
 
     @Pointcut("call(* *..GlobalQueue.addSession(*..SessionQueue))")
     void callAppendSession() {
     }
 
     @Pointcut("call(* *..GlobalQueue.removeSession(*..SessionQueue))")
     void callRemoveSession() {
     }
 
     @After("set(com.quest.forge.data.Type com.quest.forge.ui.web.Session.rootDefinition)" +
             " && args(type) ")
     public void interceptRootType(Type type) {
 
         String fullName = type.getNamespace().getResolvablePath() + "." + type.getName();
 
 
         _mainView.set(fullName);
     }
 
     @Before("pcWithinSession() " +
             "&& callAppendSession() " +
             "&& args(queue) " +
             "&& this(session)")
     public void sessionCreated(SessionQueue queue, Session session) {
 
 
         try {
             String sessionID = session.getId();
             String viewID = _mainView.get();
             _mainView.set(null);
 
             String username = session.getAuthContext().getUser();
 
            SessionInformation sessionInfo = new SessionInformation(sessionID, viewID, username, System.currentTimeMillis());
             System.out.println("Info:\n" + sessionInfo);
 
             ActiveSession activeSession = new ActiveSession(sessionInfo);
             SessionRegistry sr = SessionRegistry.getSingleton();
             sr.addSession(activeSession);
             sr.registerQueue(queue,sessionID);
 
 
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
 
     @Before("pcWithinSession() " +
             "&& callRemoveSession() " +
             "&& args(queue) " +
             "&& this(session)")
     public void sessionRemoved(SessionQueue queue, Session session) {
         System.out.println("Session removed");
 
         SessionRegistry.getSingleton().removeSession(session.getId());
         SessionRegistry.getSingleton().deregisterQueue(queue);
     }
 }
