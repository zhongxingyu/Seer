 package org.timadorus.webapp.server.rpc.service;
 
 import javax.servlet.http.HttpSession;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import org.timadorus.webapp.client.SessionId;
 import org.timadorus.webapp.client.rpc.service.SessionService;
 
 public class SessionServiceImpl extends RemoteServiceServlet implements SessionService {
 
   private static final long serialVersionUID = -6274876845484737659L;
 
   public SessionId session(SessionId sessionId) {
     HttpSession httpSession = getThreadLocalRequest().getSession(false);
     if (httpSession != null) {
       try {
         sessionId.setSessionId(httpSession.getId());
       } catch (IllegalStateException e) {
         sessionId.setSessionId("");
       }
       return sessionId;
    } // end if(result == null)
     return null;
  } // end session
 }
