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
 import javax.telephony.events.Ev;
 import javax.telephony.media.MediaTerminal;
 import javax.telephony.privatedata.PrivateData;
 import net.sourceforge.gjtapi.events.*;
 import net.sourceforge.gjtapi.capabilities.GenConnectionCapabilities;
 import net.sourceforge.gjtapi.media.FreeMediaTerminalConnection;
 /**
  * This type was created in VisualAge.
  */
 public class FreeConnection implements Connection, PrivateData {
 
 	private FreeCall call;
 	private String address;	// weak handle to Address object in DomainMgr
 	private int state = UNKNOWN;
 	private HashMap terminalConnections = new HashMap();	// map of Terminal name -> TerminalConnection
 
 
 
 
 
 
 
 
 
 /**
  * Create a connection between a call and an address pointer
  **/
 FreeConnection(FreeCall fca, String addrName){
 	super();
 	setState(IDLE);
 	setAddress(addrName);
 	setCall(fca);
 	hookUpAddress(addrName);
 
 	// now dispatch a new Connection event
 	((GenericProvider)fca.getProvider()).
 		dispatch(new FreeConnCreatedEv(CallEvent.CAUSE_NORMAL, Ev.META_UNKNOWN, false, this));
 }
 /**
  * Create a connection between a call and an address
  *
  * Fixed hook-up ordering bug: Loius Gibson, June 9, 2000
  **/
 FreeConnection(FreeCall fca, ConnectionData cd, DomainMgr dm){
 	this(fca, dm.getLazyAddress(cd.address));
 	setState(cd.connState);
 
 	// now cook up any TerminalConnections
 	TCData[] tcs = cd.terminalConnections;
 	if (tcs != null) {
 		int siz = tcs.length;
 		for (int i = 0; i < siz; i++) {
 			TCData data = tcs[i];
 				// Since we are creating a Connection, we must also create all TerminalConnections
 				// That is, we don't need to check for their existance.
 			FreeTerminalConnection tc = null;
 			if (data.terminal.isMedia) {
 				 tc = new FreeMediaTerminalConnection(this, data.terminal.terminal);
 			} else {
 				 tc = new FreeTerminalConnection(this, data.terminal.terminal);
 			}
 			tc.setState(data.tcState);
 		}
 	}
 }
 /**
  * Create a connection between a call and an address
  *
  * Fixed hook-up ordering bug: Loius Gibson, June 9, 2000
  **/
 FreeConnection(FreeCall fca,FreeAddress fad){
 	super();
 	setState(IDLE);
 	setAddress(fad);
 	setCall(fca);
 	hookUpAddress(fad);
 
 	// now dispatch a new Connection event
 	((GenericProvider)fca.getProvider()).
 		dispatch(new FreeConnCreatedEv(CallEvent.CAUSE_NORMAL, Ev.META_UNKNOWN, false, this));
 }
 	void addTerminalConnection(FreeTerminalConnection tc){
 		terminalConnections.put(tc.getTerminalName(), tc);
 	}
 /**
  * Remove the TerminalConnection from the Connection.
  * @param tc A TerminalConnection which associated this Connection with a Terminal.
  */
 	void delTerminalConnection(FreeTerminalConnection c){
 		terminalConnections.remove(c.getTerminalName());
 	}
 /**
  * disconnect our call.
  * This blocks until the connection is in a disconnected state.
  */
 public void disconnect() throws InvalidStateException, PrivilegeViolationException, MethodNotSupportedException, ResourceUnavailableException {
 
 		GenericProvider gp = (GenericProvider) call.getProvider();
 		// should check if we have already been disconnected
 		if (this.getState() != Connection.DISCONNECTED)
 			try {
 				// this should block until the connection is released.
 				gp.getRaw().release(this.getAddress().getName(), call.getCallID());
 				
 				// now update the state
 				this.toDisconnected(Event.CAUSE_NORMAL);
 			} catch (RawStateException re) {
 				throw re.morph((GenericProvider)((FreeAddress)this.getAddress()).getProvider());
 			}
 }
 	/**
 	 * Resurrect the Address object from the Domain manager
 	 **/
 public Address getAddress() {
 	return this.getGenProvider().getDomainMgr().getLazyAddress(this.address);
 }
 	/**
 	 * Return the pointer to the Connection's Address.
 	 **/
 String getAddressName() {
 	return this.address;
 }
 /**
  * Return any cached TerminalConnection with a given name, or null if none in the cache.
  * Creation date: (2000-06-20 16:10:30)
  * @author: Richard Deadman
  * @return A TerminalConnection, or null if none currently known.
  * @param termName The name of the Terminal identifying the TerminalConnection
  */
 FreeTerminalConnection getCachedTermConn(String termName) {
 	return (FreeTerminalConnection)this.terminalConnections.get(termName);
 }
 	public javax.telephony.Call getCall() {
 		return call;
 	}
 public ConnectionCapabilities getCapabilities() {
 	return ((GenConnectionCapabilities)this.getCall().getProvider().getConnectionCapabilities()).getDynamic(this);
 }
 /**
  * getConnectionCapabilities method comment.
  */
 public ConnectionCapabilities getConnectionCapabilities(Terminal terminal, Address address) throws PlatformException, InvalidArgumentException {
 	return this.getCapabilities();
 }
 /**
  * Accessor for the framework manager.
  * Creation date: (2000-05-05 14:07:09)
  * @author: Richard Deadman
  * @return The GenericProvider
  */
 private GenericProvider getGenProvider() {
 	return (GenericProvider)((FreeCall)this.getCall()).getProvider();
 }
 /**
  * Find or create a terminal connection associated with a terminal name
  * Creation date: (2000-02-15 13:38:51)
  * @author: Richard Deadman
  * @return A found or new TerminalConnection
  * @param call The terminal the connection should be hooked to
  */
 public FreeTerminalConnection getLazyTermConn(String termName) {
 	// look for existing one
 	FreeTerminalConnection tc = this.getCachedTermConn(termName);
 	if (tc != null)
 		return tc;
 
 	// No terminal connection found -- create a new one
 	GenericProvider prov = this.getGenProvider();
 		// look for a cached Terminal to give us the media type
 	FreeTerminal term = prov.getDomainMgr().getLazyTerminal(termName);
 	if (term != null) {	// use overloaded version to create TerminalConnection
 		tc = this.getLazyTermConn(term);
 	} else {	// must ask raw TelephonyProvider for media type
 		// we should never get here -- getLazyTerminal should create a Terminal if necessary
 		if (prov.getRaw().isMediaTerminal(termName))
 			tc = new FreeMediaTerminalConnection(this, termName);
 		else
 			tc = new FreeTerminalConnection(this, termName);
 	}
 	return tc;
 }
 /**
  * Find or create a terminal connection associated with a terminal
  * Creation date: (2000-02-15 13:38:51)
  * @author: Richard Deadman
  * @return A found or new TerminalConnection
  * @param call The terminal the connection should be hooked to
  */
 public FreeTerminalConnection getLazyTermConn(FreeTerminal t) {
 	// look for existing one
 	FreeTerminalConnection tc = (FreeTerminalConnection)this.terminalConnections.get(t.getName());
 	if (tc != null)
 		return tc;
 
 	// No terminal connection found -- create a new one
 	if (t instanceof MediaTerminal)
 		tc = new FreeMediaTerminalConnection(this, (MediaTerminal)t);
 	else
 		tc = new FreeTerminalConnection(this, t);
 	return tc;
 }
 /**
  * Get any PrivateData associated with my low-level object.
  */
 public Object getPrivateData() {
 	return this.getGenProvider().getRaw().getPrivateData(((FreeCall)this.getCall()).getCallID(), this.getAddress().getName(), null);
 }
 	public int getState() {
 		return state;
 	}
 /**
  * Return the set of Terminal Connections between this Address connection and one of the Address' Terminals.
  *
  * @return An array or TerminalConnections or null if none exist.
  **/
 public javax.telephony.TerminalConnection[] getTerminalConnections() {
 	TerminalConnection[] ret = null;
 	synchronized (this.terminalConnections) {
 		int size = this.terminalConnections.size();
 		if (size > 0) {
 			ret = new TerminalConnection[size];
 			int i = 0;
 			Iterator it = this.terminalConnections.values().iterator();
 			while (it.hasNext()) {
 				ret[i] = (TerminalConnection)it.next();
 				i++;
 			}
 		}
 	}
 	return ret;
 }
 	/**
 	 * Store the weak references between me and the Address
 	 **/
 private void hookUpAddress(String newAddress) {
 	if (newAddress != null) {
 		FreeAddress addr = this.getGenProvider().getDomainMgr().getLazyAddress(newAddress);
 		if (addr != null) {
 			addr.addConnection(this);
 		}
 	}
 }
 	/**
 	 * Hook up the weak references between me and the Address.
 	 * <P><B>Note that this should only be done from the constructor as a pair of
 	 * "setAddress(); setCall(); hookUpAddress().  This resolves the mutual dependency
 	 * between setting the Address and setting the Call.</B>
 	 **/
 private void hookUpAddress(FreeAddress newAddress) {
 	if (newAddress != null) {
 		newAddress.addConnection(this);
 	}
 }
 /**
  * Send PrivateData to my low-level object for processing.
  */
 public java.lang.Object sendPrivateData(java.lang.Object data) {
 	return this.getGenProvider().getRaw().sendPrivateData(((FreeCall)this.getCall()).getCallID(), this.getAddress().getName(), null, data);
 }
 	/**
 	 * Store the weak references between me and the Address
 	 **/
 private void setAddress(String newAddress) {
 	this.address = newAddress;
 }
 	/**
 	 * Store the weak references between me and the Address.
 	 * Delay hooking the address in until after the call has been set.
 	 **/
 private void setAddress(FreeAddress newAddress) {
 	if (newAddress != null) {
 		address = newAddress.getName();
 	} else {
 		address = null;
 	}
 }
 	void setCall(FreeCall newCall) {
 		call = newCall;
 		if (newCall != null)
 			call.addConnection(this);
 	}
 /**
  * Set PrivateData to be used in the next low-level command.
  */
 public void setPrivateData(java.lang.Object data) {
 	this.getGenProvider().getRaw().setPrivateData(((FreeCall)this.getCall()).getCallID(), this.getAddress().getName(), null, data);
 }
 /**
  * Update the state of the Connection.
  * Author: Richard Deadman
  * @param newState The new state constant for the Connection (see Connection)
  **/
 private void setState(int newState) {
 		if(state != newState){
 		  // check valid state transition
 		  if (((newState == FAILED) && (newState != DISCONNECTED)) // all valid for this
 			 || (state == IDLE) // IDLE can go anywhere
 			 || (state == UNKNOWN)
 			 || (newState == UNKNOWN)
 			 || ((state == INPROGRESS) && ( (newState == FAILED)
 										  ||(newState == ALERTING)
 										  ||(newState == CONNECTED)
 										  ||(newState == DISCONNECTED)))
 			 || ((state == ALERTING) && ( (newState == FAILED)
 										  ||(newState == CONNECTED)
 										  ||(newState == DISCONNECTED)))
 			 || ((state == CONNECTED) && ( (newState == FAILED)
 										  ||(newState == DISCONNECTED)))
 			 ) {
 
 				state = newState;
 				
 			 } else {
 				// throw (new FreeStateTransitionException(this,newState);}
 				System.err.println("Invalid FreeConnection State transition "+state +" to "+newState);
 			 }
 		}
 	}
 /**
  * Defines the actions to be taken when the Connection moves to the Alerting state.
  * <P>We don't use a normal State Pattern here since our actions are state transition actions which
  * do not change the resulting state depending on the starting state.
  * Creation date: (2000-05-04 23:58:34)
  * @author: Richard Deadman
  */
 void toAlerting(int cause) {
 	int oldState = this.getState();
 	if (oldState == Connection.UNKNOWN ||
 		oldState == Connection.INPROGRESS ||
 		oldState == Connection.IDLE) {
 		this.setState(Connection.ALERTING);
 		
 		// Unless otherwise set, this is the called address for the call
 		this.call.setCalledAddress(this.getAddress(), false);
 		
 		// notify any listeners
 		this.getGenProvider().dispatch(new FreeConnAlertingEv(cause, this));
 	}
 }
 /**
  * Defines the actions to be taken when the Connection moves to the Connected state.
  * <P>We don't use a normal State Pattern here since our actions are state transition actions which
  * do not change the resulting state depending on the starting state.
  * Creation date: (2000-05-04 23:58:34)
  * @author: Richard Deadman
  */
 void toConnected(int cause) {
 	int oldState = this.getState();
 	if (oldState != Connection.FAILED &&
 		oldState != Connection.DISCONNECTED &&
 		oldState != Connection.CONNECTED) {
 		this.setState(Connection.CONNECTED);
 		
 		// notify any listeners
 		this.getGenProvider().dispatch(new FreeConnConnectedEv(cause, this));
 	}
 }
 /**
  * Unhook this connection from a call.
  * Creation date: (2000-05-02 11:16:52)
  * @author: Richard Deadman
  * @return The removed Address from the call.
  */
 FreeAddress toDisconnected(int cause) {
 
 	int oldState = this.getState();
 	if (oldState != Connection.UNKNOWN && oldState != Connection.DISCONNECTED) {
 		// Tell all terminal connections still attached to disconnect
 		Iterator it = ((HashMap)this.terminalConnections.clone()).values().iterator();
 		while (it.hasNext()) {
 			((FreeTerminalConnection)it.next()).toDropped(cause);
 		}
 
 		// Update the connection state
 		this.setState(Connection.DISCONNECTED);
 
 		// Dispatch any events
 		this.getGenProvider().dispatch(new FreeConnDisconnectedEv(cause, this));
 
 		// unhook from the call
 		// (We sent the event first to ensure proper delivery ordering and so the listeners aren't
 		// disconnected before we report to them)
 		((FreeCall)this.getCall()).removeConnection(this);
 
 		// unregister with the address
 		FreeAddress addr = (FreeAddress)this.getAddress();
 		addr.removeConnection(this);
 
 		// return the removed address
 		return addr;
 	} else
 		return null;
 }
 /**
  * Defines the actions to be taken when the Connection moves to the Failed state.
  * <P>We don't use a normal State Pattern here since our actions are state transition actions which
  * do not change the resulting state depending on the starting state.
  * Creation date: (2000-05-04 23:58:34)
  * @author: Richard Deadman
  */
 void toFailed(int cause) {
 	int oldState = this.getState();
 	if (oldState != Connection.UNKNOWN &&
 		oldState != Connection.DISCONNECTED &&
 		oldState != Connection.FAILED) {
 	
 		this.setState(Connection.FAILED);
 		
 		// notify any listeners
 		this.getGenProvider().dispatch(new FreeConnFailedEv(cause, this));
 		}
 }
 /**
  * Defines the actions to be taken when the Connection moves to the InProgress state.
  * <P>We don't use a normal State Pattern here since our actions are state transition actions which
  * do not change the resulting state depending on the starting state.
  * Creation date: (2000-05-04 23:58:34)
  * @author: Richard Deadman
  */
 void toInProgress(int cause) {
 	int oldState = this.getState();
 	if (oldState == Connection.IDLE || oldState == Connection.UNKNOWN) {
 		this.setState(Connection.INPROGRESS);
 		
		// Unless otherwise set, this is the calling address for the call
		this.call.setCallingAddress(this.getAddress(), false);
 		
 		// notify any listeners
 		this.getGenProvider().dispatch(new FreeConnInProgressEv(cause, this));
 	}
 }
 }
