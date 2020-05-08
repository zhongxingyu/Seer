 package com.richitec.donkey.conference.actor;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipFactory;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import akka.actor.ActorRef;
 
 import com.richitec.donkey.ContextLoader;
 import com.richitec.donkey.conference.GlobalConfig;
 import com.richitec.donkey.conference.message.ActorMessage;
 import com.richitec.donkey.sip.servlet.B2BUASIPServlet;
 
 public class AttendeeActor extends BaseActor {
 	
 	private static Log log = LogFactory.getLog(AttendeeActor.class);
 	
 	public enum AttendeeState {
 		INITIAL, 
 		INVITE_MS, 
 		INVITE_USER, 
 		ACK_MS,
 		CALL_FAILED, 
 		CONFIRMED, 
 		TERM_WAIT, 
 		DESTROY 
 	};
 	
 	private AttendeeState state;
 	
 	public static final String Actor = "actor";
 	public static final String INVITE = "INVITE";
 	public static final String BYE = "BYE";
 	
 	private String caller;
 	private String sipUri;
 	private SipFactory sipFactory;
 	private SipApplicationSession sipAppSession;
 	
 	private SipSession mediaServerSession;
 	private boolean isMediaServerSessionValid = false;
 	
 	private SipSession userSession;
 	private boolean isUserSessionValid = false;
 	
 	private String conn;
 	private SipServletResponse mediaServerResponse;
 	private SipServletRequest inviteUser;
 	private SipServletRequest inviteMediaServer;
 	
 	private GlobalConfig config;
 	
 	private ActorRef controlChannelActor;
 	
 	public AttendeeActor(String sipUri, ActorRef controlChannelActor, String caller){
 		super();
 		this.sipUri = sipUri;
 		this.caller = caller;
 		this.sipFactory = ContextLoader.getSipFactory();
 		this.config = ContextLoader.getGlobalConfig();
 		this.state = AttendeeState.INITIAL;
 		this.controlChannelActor = controlChannelActor;
 	}
 	
 	/**
 	 * Call in user.
 	 * 
 	 * @param sipAppSession
 	 * @param userSession
 	 * @param mediaServerSession
 	 * @param sipUri
 	 */
 	public AttendeeActor(SipApplicationSession sipAppSession, SipSession userSession,
 			SipSession mediaServerSession, String sipUri, String conn, String caller){
 		super();
 		this.sipFactory = ContextLoader.getSipFactory();
 		this.config = ContextLoader.getGlobalConfig();
 		this.init(sipAppSession, userSession, mediaServerSession, sipUri, conn, caller);
 	}
 	
 	public void init(SipApplicationSession sipAppSession, SipSession userSession,
 			SipSession mediaServerSession, String sipUri, String conn, String caller){
 		this.sipAppSession = sipAppSession;
 		this.isUserSessionValid = true;
 		this.userSession = userSession;
 		this.isMediaServerSessionValid = true;
 		this.mediaServerSession = mediaServerSession;
 		this.sipUri = sipUri;
 		this.conn = conn;
 		this.caller = caller;
 		this.state = AttendeeState.CONFIRMED;
 	}
 	
 	private String getMySipUri(){
 		String result = this.sipUri;
 		if (!this.sipUri.startsWith("sip:")){
 			result = "sip:" + this.sipUri;
 		} 
 		if (!this.sipUri.contains("@")) {
 			result = result + "@" + config.getSoftSwitchIP();
 		}
 		return result;
 	}
 	
 	private String getCallerSipUri(){
 		if (null == caller || caller.isEmpty()){
 			return config.getSipUri();
 		}
 		return "sip:" + caller + "@" + config.getSoftSwitchIP();
 	}
 
 	@Override
 	public void onReceive(Object msg) throws Exception {
 		if (msg instanceof ActorMessage.CmdJoinConference){
 			onCmdJoinConference((ActorMessage.CmdJoinConference) msg);
 		} else 
 		if (msg instanceof ActorMessage.SipSuccessResponse){
 			onSipSuccessResponse((ActorMessage.SipSuccessResponse) msg);
 		} else 
 		if (msg instanceof ActorMessage.SipProvisionalResponse) {
 			onSipProvisionalResponse((ActorMessage.SipProvisionalResponse) msg);
 		} else 
 		if (msg instanceof ActorMessage.SipErrorResponse) {
 			onSipErrorResponse((ActorMessage.SipErrorResponse) msg);
 		} else
 		if (msg instanceof ActorMessage.SipByeRequest) {
 			onSipByeRequest((ActorMessage.SipByeRequest) msg);
 		} else 
 		if (msg instanceof ActorMessage.CmdUnjoinConference){
 			onCmdUnjoinConference((ActorMessage.CmdUnjoinConference) msg);
 		} else 
 		if (msg instanceof ActorMessage.CmdMuteAttendee) {
 			onCmdMuteAttendee((ActorMessage.CmdMuteAttendee) msg);
 		} else 
 		if (msg instanceof ActorMessage.CmdUnmuteAttendee) {
 			onCmdUnmuteAttendee((ActorMessage.CmdUnmuteAttendee) msg);
 		} else
 		if (msg instanceof ActorMessage.CmdDestroyConference) {
 			onCmdDestroyConference((ActorMessage.CmdDestroyConference) msg);
 		} else 
 		if (msg instanceof ActorMessage.ErrConferenceStatusConflict) {
 			onErrConferenceState((ActorMessage.ErrConferenceStatusConflict) msg);
 		} else
 		if (msg instanceof ActorMessage.SipSessionReadyToInvalidate) {
 			onSipSessionReadyToInvalidate((ActorMessage.SipSessionReadyToInvalidate) msg);
 		} else 
 		if (msg instanceof ActorMessage.EvtAttendeeCallInConference) {
 			onEvtAttendeeCallInConference((ActorMessage.EvtAttendeeCallInConference) msg);
 		} else {
 			unhandled(msg);
 		}
 	}
 	
 	private void onEvtAttendeeCallInConference(ActorMessage.EvtAttendeeCallInConference msg){
 		SipApplicationSession sipAppSession = msg.getSipAppSession();
 		SipSession userSession = msg.getUserSession();
 		SipSession mediaServerSession = msg.getMediaServerSession();
 		String sipUri = msg.getSipUri();
 		String conn = msg.getConn();
 		
 		this.init(sipAppSession, userSession, mediaServerSession, sipUri, conn, caller);
 	}
 	
 	private void onCmdJoinConference(ActorMessage.CmdJoinConference msg) throws UnsupportedEncodingException, ServletException{
 		if (this.state != AttendeeState.INITIAL){
 			getSender().tell(new ActorMessage.ErrAttendeeStatusConflict(msg.getMethod(), sipUri, state));
 			return;
 		}
 		
 		if (null == sipAppSession || !sipAppSession.isValid() || sipAppSession.isReadyToInvalidate()){
 			sipAppSession = sipFactory.createApplicationSession();
 		}
 		
 		inviteMediaServer = sipFactory.createRequest(sipAppSession, 
 				INVITE, config.getSipUri(), config.getMediaServerSipUri());
 		
 		mediaServerSession = inviteMediaServer.getSession();
 		mediaServerSession.setHandler(B2BUASIPServlet.class.getSimpleName());
 		mediaServerSession.setAttribute(Actor, getSelf());
 		
 		String outboundIPAddr = config.getOutboundIP();
 		Integer port = config.getOutboundPort();
 		InetSocketAddress address = new InetSocketAddress(outboundIPAddr, port);
 		mediaServerSession.setOutboundInterface(address);
 		
 		this.state = AttendeeState.INVITE_MS;
 		this.isMediaServerSessionValid = true;
 		
 		send(inviteMediaServer);
 	}
 	
 	private void onSipSuccessResponse(ActorMessage.SipSuccessResponse msg) throws UnsupportedEncodingException, IOException, ServletException{
 		SipServletResponse response = msg.getResponse();
 		SipSession sipSession = response.getSession(false);
 		if (sipSession.equals(mediaServerSession)){
 			mediaServerResponse = response;
 			SipApplicationSession sipAppSession = sipSession.getApplicationSession();
 			inviteUser = sipFactory.createRequest(sipAppSession, 
 					INVITE, getCallerSipUri(), getMySipUri());
 			
 			inviteUser.setContent(response.getContent(), response.getContentType());
 			
 			// set route address
 			Address routeAddr = sipFactory.createAddress(config.getSoftSwitchSipUri());
 			// set lr parameter, it is important
 			routeAddr.setParameter("lr", "");
 			inviteUser.pushRoute(routeAddr);
 			
 			userSession = inviteUser.getSession();
 			userSession.setAttribute(Actor, getSelf());
 			
 			String outboundIPAddr = config.getOutboundIP();
 			Integer port = config.getOutboundPort();
 			InetSocketAddress address = new InetSocketAddress(outboundIPAddr, port);
 			userSession.setOutboundInterface(address);
 			
 			userSession.setHandler(B2BUASIPServlet.class.getSimpleName());
 			
 			this.state = AttendeeState.INVITE_USER;
 			this.isUserSessionValid = true;
 			
 			send(inviteUser);
 		} else if (sipSession.equals(userSession)){
 	    	SipServletRequest ack = response.createAck();
 	    	send(ack);
 	    	
 			this.state = AttendeeState.CONFIRMED;
 			this.conn = mediaServerResponse.getTo().getParameter("tag");
 			getContext().parent().tell(new ActorMessage.EvtAttendeeCallEstablished(this.sipUri, this.conn), getSelf());
 		} else {
 			log.error("Unknown SipSession");
 		}
 	}
 	
 	private void onSipProvisionalResponse(ActorMessage.SipProvisionalResponse msg) throws UnsupportedEncodingException, IOException{
 		SipServletResponse response = msg.getResponse();
 		SipSession sipSession = response.getSession(false);
 		if (!sipSession.equals(userSession)){
 			return;
 		}
 		
 		if (this.state != AttendeeState.INVITE_USER){
 		    return;
 		}
 		
 		if (response.getStatus() ==  SipServletResponse.SC_SESSION_PROGRESS ||
 		    response.getStatus() == SipServletResponse.SC_RINGING) {
 			if (response.getContent() != null){
     		    this.state = AttendeeState.ACK_MS;
     			SipServletRequest ack = mediaServerResponse.createAck();
     			ack.setContent(response.getContent(), response.getContentType());
     			send(ack);
 			}
 		}
 	}
 
 	private void onSipErrorResponse(ActorMessage.SipErrorResponse msg){
 		SipServletResponse response = msg.getResponse();
 		int status = response.getStatus();
 		SipSession sipSession = response.getSession(false);
 		if (sipSession.equals(mediaServerSession)){
 			this.state = AttendeeState.CALL_FAILED;
 			log.error("INVITE meida server failed with status: " + status);
 			getContext().parent().tell(new ActorMessage.EvtMediaServerCallFailed(this.sipUri, status), getSelf());
 		} else if (sipSession.equals(userSession)){
 			//send BYE to media server session
 			this.state = AttendeeState.CALL_FAILED;
 			SipServletRequest bye = mediaServerSession.createRequest(BYE);
 			send(bye);
 			
 			//Notify ConferenActor that user calling failed.
 			log.warn("INVITE " + response.getTo()+ " Failed " + status);
 			getContext().parent().tell(new ActorMessage.EvtAttendeeCallFailed(this.sipUri, status), getSelf());
 		} else {
 			log.error("Unknown SipSession");
 		}
 	}
 	
 	private void onSipByeRequest(ActorMessage.SipByeRequest msg){
 		this.state = AttendeeState.TERM_WAIT;
 		SipSession session = msg.getSipSession();
 		SipServletRequest bye = null;
 		if (session.equals(userSession)){
 			bye = mediaServerSession.createRequest(BYE);
 		} else if (session.equals(mediaServerSession)){
 			bye = userSession.createRequest(BYE);
 		}
 		
 		if (null != bye){
 			send(bye);
 		}
 	}
 	
 	private void onErrConferenceState(ActorMessage.ErrConferenceStatusConflict msg){
 		if (this.state == AttendeeState.CONFIRMED){
 			this.state = AttendeeState.TERM_WAIT;
 			bye(mediaServerSession);
 			bye(userSession);
 		} else {
 			log.warn("The expected AttendeeState onErrConferenceState " +
 					"is CONFIRMED, but it is " + this.state.name());
 		}
 	}
 	
 	private void onCmdMuteAttendee(ActorMessage.CmdMuteAttendee msg) {
 		log.info("Current AttendeeActor state is " + this.state.name() + 
 				"  " + (this.state != AttendeeState.CONFIRMED));
 		if (this.state != AttendeeState.CONFIRMED){
 			getSender().tell(new ActorMessage.ErrAttendeeStatusConflict(msg.getMethod(), sipUri, state));
 		} else {
 			msg.setConn(this.conn);
 			controlChannelActor.tell(msg);
 		}
 	}
 	
 	private void onCmdUnmuteAttendee(ActorMessage.CmdUnmuteAttendee msg){
 		if (this.state != AttendeeState.CONFIRMED){
 			getSender().tell(new ActorMessage.ErrAttendeeStatusConflict(msg.getMethod(), sipUri, state));
 		} else {
 			msg.setConn(this.conn);
 			controlChannelActor.tell(msg);
 		}
 	}
 	
 	private void onCmdUnjoinConference(ActorMessage.CmdUnjoinConference msg){
 		if (this.state == AttendeeState.CONFIRMED){
 			this.state = AttendeeState.TERM_WAIT;
 			bye(mediaServerSession);
 			bye(userSession);
 		} else 
 		if (this.state == AttendeeState.INVITE_USER){
 			this.state = AttendeeState.TERM_WAIT;
 			//cancel(inviteUser);
 			bye(mediaServerSession);
 			bye(userSession);
 		} else
 		if (this.state == AttendeeState.INVITE_MS){
 			this.state = AttendeeState.TERM_WAIT;
 			//cancel(inviteMediaServer);
 			bye(mediaServerSession);
		} else 
		if (this.state == AttendeeState.ACK_MS) {
		    this.state = AttendeeState.TERM_WAIT;
		    bye(userSession);
		    bye(mediaServerSession);
 		} else {
 			getSender().tell(new ActorMessage.ErrAttendeeStatusConflict(msg.getMethod(), sipUri, state));			
 		}
 	}
 	
 	private void bye(SipSession session){
 		SipServletRequest bye = session.createRequest(BYE);
 		send(bye);
 	}
 	
 	private void cancel(SipServletRequest request){
 		SipServletRequest cancel = request.createCancel();
 		send(cancel);
 	}
 	
 	private void onCmdDestroyConference(ActorMessage.CmdDestroyConference msg){
 		if (this.state == AttendeeState.INITIAL){
 			getContext().stop(getSelf());
 		} else if (this.state == AttendeeState.INVITE_MS) {
 			this.state = AttendeeState.DESTROY;
 			bye(mediaServerSession);
 		} else if (this.state == AttendeeState.INVITE_USER) {
 			this.state = AttendeeState.DESTROY;
 			bye(mediaServerSession);
 			bye(userSession);
 		} else if (this.state == AttendeeState.CONFIRMED) {
 			this.state = AttendeeState.DESTROY;
 			bye(mediaServerSession);
 			bye(userSession);
 		} else if (this.state == AttendeeState.CALL_FAILED) {
 			this.state = AttendeeState.DESTROY;
 		} else if (this.state == AttendeeState.TERM_WAIT) {
 			this.state = AttendeeState.DESTROY;
 		} 
 	}
 
 	private void onSipSessionReadyToInvalidate(ActorMessage.SipSessionReadyToInvalidate msg){
 		SipSession session = msg.getSipSession();
 		if (session.equals(mediaServerSession)){
 			isMediaServerSessionValid = false;
 			log.debug("\nAttendeeActor <" + sipUri + "> Media Server Session ReadyToInvalidate.");
 		} else if (session.equals(userSession)){
 			isUserSessionValid = false;
 			log.debug("\nAttendeeActor <" + sipUri + "> Attendee Session ReadyToInvalidate.");
 		}
 		
 		if (!isMediaServerSessionValid && !isUserSessionValid){
 			if (this.state == AttendeeState.TERM_WAIT || this.state == AttendeeState.CONFIRMED) {
 				this.state = AttendeeState.INITIAL;
 				getContext().parent().tell(new ActorMessage.EvtAttendeeCallTerminated(sipUri), getSelf());
 			} else if (this.state == AttendeeState.CALL_FAILED ||
 			        this.state == AttendeeState.ACK_MS) {
 				this.state = AttendeeState.INITIAL;
 			} else if (this.state == AttendeeState.DESTROY){
 				getContext().stop(getSelf());
 			} else {
 				log.error("Invalid AttendeeState <" + this.state.name() + "> onSipSessionReadyToInvalidate");
 			}
 		}
 	}
 	
 	public static void main(String [] args){
 		AttendeeState state = AttendeeState.CONFIRMED;
 		System.out.println("state == CONFIRMED : " + (state == AttendeeState.CONFIRMED));
 		System.out.println("state != CONFIRMED : " + (state != AttendeeState.CONFIRMED));
 		System.out.println("state equals CONFIEMED : " + (state.equals(AttendeeState.CONFIRMED)));
 	}
 	
 }
