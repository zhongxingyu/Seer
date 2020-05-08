 package net.sourceforge.gjtapi;
 
 /*
 	Copyright (c) 1999,2002 Westhawk Ltd (www.westhawk.co.uk) 
 	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 
 
 	All rights reserved. 
 
 	Permission is hereby granted, free of charge, to any person obtaining a 
 	copy of this software and associated documentation files (the 
 	"Software"), to deal in the Software without restriction, including 
 	without limitation the rights to use, copy, modify, merge, publish, 
 	distribute, and/or sell copies of the Software, and to permit persons 
 	to whom the Software is furnished to do so, provided that the above 
 	copyright notice(s) and this permission notice appear in all copies of 
 	the Software and that both the above copyright notice(s) and this 
 	permission notice appear in supporting documentation. 
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
 	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
 	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
 	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
 	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
 	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
 
 	Except as contained in this notice, the name of a copyright holder 
 	shall not be used in advertising or otherwise to promote the sale, use 
 	or other dealings in this Software without prior written authorization 
 	of the copyright holder.
 */
 import java.util.*;
 import javax.telephony.*;
 import javax.telephony.capabilities.*;
 import javax.telephony.callcontrol.CallControlTerminalConnection;
 import javax.telephony.media.*;
 import javax.telephony.privatedata.PrivateData;
 import net.sourceforge.gjtapi.media.*;
 import net.sourceforge.gjtapi.events.*;
 /**
  * This is a TerminalConnection that implements both Core and CallControl TerminalConnection
  * capabilities.
  *
  * @author: Richard Deadman
  **/
 
 public class FreeTerminalConnection implements javax.telephony.callcontrol.CallControlTerminalConnection, javax.telephony.privatedata.PrivateData {
 	private int ccstate = UNKNOWN;
 	private FreeConnection connection;
 	private String terminal;		// weak handle through the DomainMgr to the terminal
 public FreeTerminalConnection(Connection con, String term) {
 	super();
 	
 	setState(CallControlTerminalConnection.IDLE);
 	setTerminal(term);
 	setConnection(con);
 	hookUpTerminal(term);
 	
 	// dispatch off created events
 	this.getGenProvider().dispatch(new FreeTermConnCreatedEv(Event.CAUSE_NORMAL, this));
 }
 public FreeTerminalConnection(Connection con, Terminal term) {
 	super();
 	
 	setState(CallControlTerminalConnection.IDLE);
 	setTerminal(term);
 	setConnection(con);
 	hookUpTerminal(term);
 
 	// dispatch off created events
 	this.getGenProvider().dispatch(new FreeTermConnCreatedEv(Event.CAUSE_NORMAL, this));
 }
 	public void answer() throws PrivilegeViolationException, ResourceUnavailableException, MethodNotSupportedException, InvalidStateException {
 		FreeCall call = (FreeCall) connection.getCall();
 		TelephonyProvider p = ((GenericProvider) call.getProvider()).getRaw();
 		try {
 			p.answerCall(call.getCallID(),
 				this.getConnection().getAddress().getName(),
 				this.getTerminal().getName());
 		} catch (RawStateException re) {
 			throw re.morph((FreeTerminal)this.getTerminal());
 		}
 	}
 /**
  * Returns the call control state of the TerminalConnection.
  * The return values will be one of the integer state constants defined in the CallControlTerminalConnection interface.
  * @Author: Richard Deadman
  * @return The current call control state of the TerminalConnection.
  */
 public int getCallControlState() {
 	return ccstate;
 }
 	public TerminalConnectionCapabilities getCapabilities() {
 		return ((net.sourceforge.gjtapi.capabilities.GenTermConnCapabilities)this.getTerminal().getProvider().getTerminalConnectionCapabilities()).getDynamic(this);
 	}
 	public javax.telephony.Connection getConnection() {
 		return connection;
 	}
 /**
  * Internal accessor for the Framework manager.
  * Creation date: (2000-05-05 14:18:52)
  * @author: Richard Deadman
  * @return The framework manager
  */
 private GenericProvider getGenProvider() {
 	return (GenericProvider)((FreeCall)((FreeConnection)this.getConnection()).getCall()).getProvider();
 }
 /**
  * Get any PrivateData associated with my low-level object.
  */
 public Object getPrivateData() {
 	FreeConnection conn = (FreeConnection)this.getConnection();
 	return this.getGenProvider().getRaw().getPrivateData(((FreeCall)conn.getCall()).getCallID(), conn.getAddress().getName(), this.getTerminal().getName());
 }
   /**
    * Returns the call control state of the TerminalConnection. The return
    * values will be one of the integer state constants defined int javax.telephony.TerminalConnection.
    * <p>
    * @return The current call control state of the TerminalConnection.
    */
 	public int getState() {
 		switch (this.ccstate) {
 			case CallControlTerminalConnection.IDLE: {
 				return TerminalConnection.IDLE;
 			}
 			case CallControlTerminalConnection.RINGING: {
 				return TerminalConnection.RINGING;
 			}
 			case CallControlTerminalConnection.TALKING: {
 				return TerminalConnection.ACTIVE;
 			}
 			case CallControlTerminalConnection.HELD: {
 				return TerminalConnection.ACTIVE;
 			}
 			case CallControlTerminalConnection.INUSE: {
 				return TerminalConnection.PASSIVE;
 			}
 			case CallControlTerminalConnection.BRIDGED: {
 				return TerminalConnection.PASSIVE;
 			}
 			case CallControlTerminalConnection.DROPPED: {
 				return TerminalConnection.DROPPED;
 			}
 			case CallControlTerminalConnection.UNKNOWN: {
 				return TerminalConnection.UNKNOWN;
 			}
 		}
 		return TerminalConnection.UNKNOWN;
 	}
 	/**
 	 * Get the Terminal mapped to this TerminalConnection, looking up through the DomainMgr.
 	 **/
 public javax.telephony.Terminal getTerminal() {
 	try {
 		return this.getGenProvider().getDomainMgr().getFaultedTerminal(this.terminal);
 	} catch (InvalidArgumentException iae) {
 		throw new PlatformException("Domain error: lost Terminal named: " + this.terminal);
 	}
 }
 /**
  * @deprecated
  **/
 public TerminalConnectionCapabilities getTerminalConnectionCapabilities(Terminal terminal, Address address) throws InvalidArgumentException, PlatformException {
 	return this.getCapabilities();
 }
 /**
  * Return the name weak pointer to the TerminalConnection in the DomainMgr.
  * Creation date: (2000-06-20 15:48:33)
  * @author: Richard Deadman
  * @return The name of my Terminal
  */
 String getTerminalName() {
 	return this.terminal;
 }
 /**
  * Places a TerminalConnection on hold with respect to the Call of which it is a part.
  * Many Terminals may be on the same Call and associated with the same Connection. Any one of
  * them may go "on hold" at any time, provided they are active in the Call. The
  * TerminalConnection must be in the CallControlTerminalConnection.TALKING state. This method
  * returns when the TerminalConnection has moved to the CallControlTerminalConnection.HELD state,
  * or until an error occurs and an exception is thrown. 
  */
 public void hold() throws InvalidStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
 	// check the state first
 	
 	// initiate action
 	Terminal t = this.getTerminal();
 	try {
 		Connection conn = this.getConnection();
 		((GenericProvider)t.getProvider()).getRaw().hold(((FreeCall)conn.getCall()).getCallID(),
 			t.getName(),
 			conn.getAddress().getName());
 	} catch (RawStateException re) {
 		throw re.morph((FreeTerminal)this.getTerminal());
 	}
 }
 	/**
 	 * Hook up the referred to Terminal
 	 **/
 private void hookUpTerminal(String newTerminal) {
 		// See if there is a cached Terminal that should be updated
 	if (newTerminal != null) {
 		FreeTerminal t = this.getGenProvider().getDomainMgr().getCachedTerminal(newTerminal);
 		if (t != null) {
 			t.addTerminalConnection(this);
 		}
 	}
 }
 	/**
 	 * Hook up the referred to Terminal
 	 **/
 private void hookUpTerminal(Terminal newTerminal) {
 	if (newTerminal != null) {
 		((FreeTerminal)newTerminal).addTerminalConnection(this);
 	}
 }
 /**
  * Makes a currently bridged TerminalConnection active on a Call.
  * Author: Richard Deadman
  */
 public void join() throws InvalidStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
 	throw new MethodNotSupportedException();
 }
 /**
  * Places a currently active TerminalConnection in a bridged state on a Call.
  * @Author: Richard Deadman
  */
 public void leave() throws javax.telephony.InvalidStateException, javax.telephony.MethodNotSupportedException, javax.telephony.PrivilegeViolationException, javax.telephony.ResourceUnavailableException {
 	throw new MethodNotSupportedException();
 }
 /**
  * Send PrivateData to my low-level object for processing.
  */
 public java.lang.Object sendPrivateData(java.lang.Object data) {
 	FreeConnection conn = (FreeConnection)this.getConnection();
 	return this.getGenProvider().getRaw().sendPrivateData(((FreeCall)conn.getCall()).getCallID(), conn.getAddress().getName(), this.getTerminal().getName(), data);
 }
 	public void setConnection(javax.telephony.Connection newConnection) {
 		connection = (FreeConnection) newConnection;
 		if (newConnection != null)
 			connection.addTerminalConnection(this);
 	}
 /**
  * Set PrivateData to be used in the next low-level command.
  */
 public void setPrivateData(java.lang.Object data) {
 	FreeConnection conn = (FreeConnection)this.getConnection();
 	this.getGenProvider().getRaw().setPrivateData(((FreeCall)conn.getCall()).getCallID(), conn.getAddress().getName(), this.getTerminal().getName(), data);
 }
 /**
  * Package access to setting the Terminal Connection state.
  * The passed in state is the CallControlTerminalConnection state.  TerminalConnection states are
  * derived from this.
  * <P>Accessed by the RawEventHandler.
  * <P>This is not synchronized, since the RawHandler processes event sequentially.
  * Author: Richard Deadman
  * @param newState The new TerminalConnection state for the call
  **/
 void setState(int newState) {
 	// check for valid states -- assumes state ordering.
 	if (newState < CallControlTerminalConnection.IDLE || newState > CallControlTerminalConnection.UNKNOWN) {
 		System.err.println("Invalid FreeTerminalConnection CC State:"+newState);
 	}
 	// check valid transitions
 	int state = this.getCallControlState();
 	if (state != newState){
 		if ((state == CallControlTerminalConnection.IDLE)   // any new state valid
 			|| (newState == CallControlTerminalConnection.DROPPED) // any one can transition to Dropped
 			|| (state == CallControlTerminalConnection.UNKNOWN)
 			|| (newState == CallControlTerminalConnection.UNKNOWN)
 			|| (state == CallControlTerminalConnection.RINGING)		// ringing can go anywhere
 			|| ((state == CallControlTerminalConnection.BRIDGED) &&	// bridged can go to talking
 				(newState == CallControlTerminalConnection.TALKING))
 			|| ((state == CallControlTerminalConnection.TALKING) &&	// talking can go to bridged, inuse or held
 				((newState == CallControlTerminalConnection.BRIDGED) ||
 					(newState == CallControlTerminalConnection.INUSE) ||
 					(newState == CallControlTerminalConnection.HELD)))
 			|| ((state == CallControlTerminalConnection.HELD) &&	// held can go to bridged, inuse or talking
 				((newState == CallControlTerminalConnection.BRIDGED) ||
 					(newState == CallControlTerminalConnection.INUSE) ||
 					(newState == CallControlTerminalConnection.TALKING)))
 			) {
 				this.ccstate = newState;
 			} else {
 				// throw (new FreeStateTransitionException(this,newState);}
 				System.err.println("Invalid FreeTerminalConnection State transition "+state +" to "+newState);
 			}
 		} // else states are equal
 }
 	/**
 	 * Store the name of the referred to Terminal
 	 **/
 private void setTerminal(String newTerminal) {
 	this.terminal = newTerminal;
 }
 	/**
 	 * Store the name of the referred to Terminal
 	 **/
 private void setTerminal(Terminal newTerminal) {
 	if (newTerminal != null) {
 		this.terminal = newTerminal.getName();
 	} else {
 		this.terminal = null;
 	}
 }
 /**
  * This TerminalConnection has been disconnected and must unhook from the Connection and Terminal.
  * Creation date: (2000-05-02 10:57:25)
  * @author: Richard Deadman
  * @param cause The Event reason for the terminal connection "drop".
  * @return The removed terminal from the Call.
  */
 protected Terminal toDropped(int cause) {
 	if (this.getCallControlState() != CallControlTerminalConnection.DROPPED) {
 		// cache for later after I unhook the connection.
 		GenericProvider prov = this.getGenProvider();
 
 		// tell my Connection and Terminal to release me	
 		((FreeConnection)this.getConnection()).delTerminalConnection(this);
 
 		FreeTerminal term = (FreeTerminal)this.getTerminal();
 		term.removeTerminalConnection(this);
 
 		// Update the connection state
 		this.setState(CallControlTerminalConnection.DROPPED);
 
 		// Determine if we should notify media services that the Terminal is no longer in the call
 		if (term instanceof FreeMediaTerminal) {
 			final MediaServiceHolder lms = prov.getMediaMgr().findForTerminal(term.getName());
 			if (lms != null) {
 				prov.getDispatchPool().put(new net.sourceforge.gjtapi.util.EventHandler() {
 					public void process(Object o) {	// ignore o -- will be null
 						Iterator it = lms.getListeners();
 						GenericMediaEvent me = new GenericMediaEvent(MediaEvent.ev_Disconnected, lms.getMediaService());
 						while (it.hasNext()) {
 							Object l = it.next();
 							if (l instanceof MediaServiceListener)
 								((MediaServiceListener)l).onDisconnected(me);
 						}
 					}
 				});
 			}
 		}
 
 		// dispatch any events
 		prov.dispatch(new FreeTermConnDroppedEv(cause, this));
 
 		// return the dropped terminal
 		return term;
 	} else
 		return null;
 }
 /**
  * Process a talking state request
  * Creation date: (2000-05-05 14:15:44)
  * @author: Richard Deadman
  * @param cause The cause of the creation
  */
 protected void toHeld(int cause) {
 	int oldState = this.getCallControlState();
	if (oldState != CallControlTerminalConnection.HELD &&
            oldState != CallControlTerminalConnection.INUSE &&
            oldState != CallControlTerminalConnection.BRIDGED &&
            oldState != CallControlTerminalConnection.DROPPED) {
 		// Update the connection state
 		this.setState(CallControlTerminalConnection.HELD);
 
 		// Create and dispatch the common event
 		GenericProvider prov = this.getGenProvider();
 		prov.dispatch(new FreeTermConnHeldEv(cause, this));
 		if (oldState != CallControlTerminalConnection.TALKING)
 			prov.dispatch(new FreeTermConnActiveEv(cause, this, false));
 	}
 }
 /**
  * Process a ringing state request
  * Creation date: (2000-05-05 14:15:44)
  * @author: Richard Deadman
  * @param cause The cause of the creation
  */
 void toRinging(int cause) {
 	if (this.getCallControlState() == CallControlTerminalConnection.IDLE) {
 		// Update the connection state
 		this.setState(CallControlTerminalConnection.RINGING);
 
 		// Create and dispatch the common event
 		this.getGenProvider().dispatch(new FreeTermConnRingingEv(cause, this));
 	}
 }
 /**
  * Process a talking state request
  * Creation date: (2000-05-05 14:15:44)
  * @author: Richard Deadman
  * @param cause The cause of the creation
  */
 protected void toTalking(int cause) {
 	int oldState = this.getCallControlState();
 	if (oldState != CallControlTerminalConnection.TALKING &&
 		oldState != CallControlTerminalConnection.INUSE &&
 		oldState != CallControlTerminalConnection.DROPPED) {
 		// Update the connection state
 		this.setState(CallControlTerminalConnection.TALKING);
 
 		// Create and dispatch the common event
 		GenericProvider prov = this.getGenProvider();
 		prov.dispatch(new FreeTermConnTalkingEv(cause, this));
 		if (oldState != CallControlTerminalConnection.HELD)
 			prov.dispatch(new FreeTermConnActiveEv(cause, this, true));
 	}
 }
 /**
  * Takes a TerminalConnection off hold with respect to the Call of which it is a part.
  * Many Terminals may be on the same Call and associated with the same Connection. Any
  * one of them may go "on hold" at any time, provided they are active in the Call. The
  * TerminalConnection must be in the CallControlTerminalConnection.HELD state. This method
  * returns successfully when the TerminalConnection moves into the
  * CallControlTerminalConnection.TALKING state or until an error occurs and an exception is thrown.
  *
  * @author: Richard Deadman
  */
 public void unhold() throws InvalidStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
 	// check the state first
 	
 	// initiate action
 	Terminal t = this.getTerminal();
 	try {
 		((GenericProvider)t.getProvider()).getRaw().unHold(((FreeCall)this.getConnection().getCall()).getCallID(),
 			this.getConnection().getAddress().getName(),
 			t.getName());
 	} catch (RawStateException re) {
 		throw re.morph((FreeTerminal)this.getTerminal());
 	}
 }
 
 /** Describes myself */
 public String toString() {
     return "FreeTerminalConnection from Terminal '" + this.getTerminal().getName() + "' to Call '" + ((FreeCall)this.getConnection().getCall()).getCallID() + "'";
 }
 }
