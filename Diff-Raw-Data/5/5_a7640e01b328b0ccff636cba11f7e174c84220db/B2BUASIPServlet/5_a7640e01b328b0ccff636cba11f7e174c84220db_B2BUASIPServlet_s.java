 
 package com.richitec.donkey.sip.servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.sip.SipServlet;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import akka.actor.ActorRef;
 
 import com.richitec.donkey.conference.actor.AttendeeActor;
 import com.richitec.donkey.conference.message.ActorMessage;
 
 
 /**
  * SipServlet implementation class CallUserSipServlet
  */
 @javax.servlet.sip.annotation.SipServlet
 public class B2BUASIPServlet extends SipServlet {
 
     private static final long serialVersionUID = 3978425801979081269L;
     private static final Log log = LogFactory.getLog(B2BUASIPServlet.class);
 
     //Reference to context - The ctx Map is used as a central storage for this app
     ServletContext ctx = null;
     
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         ctx = config.getServletContext();
     }
     
 
     /*
      * Demonstrates extension with a new "REPUBLISH" method
      */
     @Override
     protected void doRequest(SipServletRequest req) throws ServletException, IOException {
         if( req.getMethod().equals("REPUBLISH") ) {
             doRepublish(req);
         } else {
             super.doRequest(req);
         }
     }
     
     @Override
     protected void doInvite(SipServletRequest req) throws ServletException, IOException {
     	super.doInvite(req);
     	log.debug("INVITE from : " + req.getFrom().getValue());
     }
     
     /*
      * Implement the REPUBLISH extension here
      */    
     protected void doRepublish(SipServletRequest req) throws ServletException, IOException {
 		// TODO Auto-generated method stub
     }
     
     @Override
     protected void doBye(SipServletRequest req) throws IOException {
     	SipSession session = req.getSession(false);
     	ActorRef actor = (ActorRef) session.getAttribute(AttendeeActor.Actor);
     	actor.tell(new ActorMessage.SipByeRequest(session));
     	
     	SipServletResponse response = req.createResponse(SipServletResponse.SC_OK);
     	response.send();
     }
     
     @Override
     protected void doResponse(SipServletResponse resp) 
     				throws ServletException, IOException {
 		String method = resp.getMethod();
 		if (AttendeeActor.INVITE.equals(method)){
 			super.doResponse(resp);
 		} else if (AttendeeActor.BYE.equals(method)){
 			//do nothing
 		} else {
 			log.error("Unexpected SIP response method: " + method);
 		}
 	}
     
     @Override
     protected void doProvisionalResponse(SipServletResponse resp) {
 		SipSession session = resp.getSession(false);
 		ActorRef actor = (ActorRef) session.getAttribute(AttendeeActor.Actor);
 		actor.tell(new ActorMessage.SipProvisionalResponse(resp));
 	}    
     
     @Override
     protected void doSuccessResponse(SipServletResponse resp) throws IOException {
     	SipSession session = resp.getSession(false);
     	ActorRef actor = (ActorRef) session.getAttribute(AttendeeActor.Actor);
     	actor.tell(new ActorMessage.SipSuccessResponse(resp));
 	}
     
     @Override
     protected void doBranchResponse(SipServletResponse resp) {
 		log.warn("Branch SIP Response: " + resp.getStatus() + "\n" +
 				"Method: " + resp.getMethod() + "\n" +
 				"From: " + resp.getFrom().getValue() + "\n" +
 				"To: " + resp.getTo().getValue() + "\n" +
 				"CallId: " + resp.getCallId() + "\n");
 	}
     
     @Override
     protected void doRedirectResponse(SipServletResponse resp) {
 		log.warn("Redirect SIP Response: " + resp.getStatus() + "\n" +
  				"Method: " + resp.getMethod() + "\n" +
 				"From: " + resp.getFrom().getValue() + "\n" +
 				"To: " + resp.getTo().getValue() + "\n" +
 				"CallId: " + resp.getCallId() + "\n" );
 	}    
     
     @Override
     protected void doErrorResponse(SipServletResponse resp) throws IOException {
     	log.debug("Error SIP Response: " + resp.getStatus() + "\n");
     	SipSession session = resp.getSession(false);
     	ActorRef actor = (ActorRef) session.getAttribute(AttendeeActor.Actor);
     	actor.tell(new ActorMessage.SipErrorResponse(resp));
    	
     	if (resp.getStatus() != SipServletResponse.SC_REQUEST_TIMEOUT){
     		SipServletRequest ack = resp.createAck();
     		ack.send();
     	}
 	}    
 }
