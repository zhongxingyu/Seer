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
 import java.lang.ref.WeakReference;
 import javax.telephony.events.*;
 import javax.telephony.capabilities.*;
 import javax.telephony.*;
 import javax.telephony.privatedata.PrivateData;
 import net.sourceforge.gjtapi.capabilities.GenTerminalCapabilities;
 import net.sourceforge.gjtapi.events.*;
 
 public class FreeTerminal implements Terminal, PrivateData {
 
   private String name;
   private Provider provider;
 
   private ObservableHelper observers = new ObservableHelper(){
 	  Object [] mkObserverArray(int i) { return new TerminalObserver[i];}
 	  void notifyObserver(Object o, Ev [] e) { ((TerminalObserver)o).terminalChangedEvent((TermEv[])e);}
   };
   private Vector callObservers = new Vector();
   private transient Vector terminalListeners;
   private transient Vector callListeners = new Vector();
 
   private HashSet addresses = null;		// lazily fetched address names -- null means not fetched
   private transient HashSet terminalConnections = new HashSet(2);	// TCHolder set
 
   private boolean reporting;	// Have I asked the raw provider to send events?
 
 	/**
    * A weak reference to a Connection that can re-instantiate itself as necessary
    **/
   class TCHolder {
 	  private CallId callId;
 	  private String address;
 	  private String terminal;
 	  WeakReference tcRef = null;
 
 	  TCHolder(CallId id, String addr, String term) {
 		  callId = id;
 		  address = addr;
 		  terminal = term;
 	  }
 
 	  TCHolder(FreeTerminalConnection termConn) {
 		  Connection conn = termConn.getConnection();
 		  callId = ((FreeCall)conn.getCall()).getCallID();
 		  address = conn.getAddress().getName();
 		  terminal = termConn.getTerminal().getName();
 		  tcRef = new WeakReference(termConn);
 	  }
 
 	  CallId getCallId() {
 		  return callId;
 	  }
 
 	  private String getAddress() {
 		  return address;
 	  }
 
 	  private String getTerminal() {
 		  return terminal;
 	  }
 
 	  /**
 	   * Return the current referrent or null
 	   **/
 	  private FreeTerminalConnection getReferent() {
 		  WeakReference wr = this.tcRef;
 		  FreeTerminalConnection tc = null;
 		  if (wr != null)
 			  tc = (FreeTerminalConnection)wr.get();
 		  return tc;
 	  }
 
 	  public int hashCode() {
 		  return this.getCallId().hashCode() +
 		  	this.getAddress().hashCode() +
 		  	this.getTerminal().hashCode();
 	  }
 
 	  public boolean equals(Object o) {
 		  if (o instanceof TCHolder) {
 			  TCHolder ch = (TCHolder)o;
 			  if (ch.getCallId().equals(this.getCallId()) &&
 				  ch.getAddress().equals(this.getAddress()) &&
 				  ch.getTerminal().equals(this.getTerminal()))
 			  	  return true;
 		  }
 		  return false;
 	  }
 	  
 	  FreeTerminalConnection getTerminalConnection() {
 		  FreeTerminalConnection tc = getReferent();
 		  if (tc == null) {
 			  synchronized (this) {
 				  // double check
 				  tc = getReferent();
 				  if (tc == null) {
 					  tc = ((GenericProvider)getProvider()).getCallMgr().getFaultedTermConn(this.getCallId(),
 						  		this.getAddress(),
 						  		this.getTerminal());
 				  }
 			  }
 		  }
 		  return tc;
 	  }
   }
 /**
  * Create a Terminal Object without any Addresses mapped to it yet.
  **/
 protected FreeTerminal(String n,Provider p) {
 	  super();
 	setName(n);
 	setProvider(p);
 }
  public synchronized void addCallListener(CallListener l) throws MethodNotSupportedException, ResourceUnavailableException {
 	Vector v = (Vector) callListeners.clone();
 	if (!v.contains(l)) {
 	  v.addElement(l);
 	  callListeners = v;
          this.startEvents();
 	}
 	TerminalConnection [] tc = this.getTerminalConnections();
 	if (tc != null) {
 		for (int i=0; i<tc.length;i++){
 			Connection con = tc[i].getConnection();
 			if (null != con) {
 				FreeCall c = (FreeCall) con.getCall();
 				if (c != null) {
 					c.addCallListener(l, this);
 				}
 			}
 		}
 	}
   }            
 /**
  * addCallObserver method comment.
  */
 public void addCallObserver(CallObserver observer) throws MethodNotSupportedException, ResourceUnavailableException {
    this.callObservers.add(observer);
    this.startEvents();
 
   // add to calls currently attached to the terminal
   	TerminalConnection [] tc = this.getTerminalConnections();
 	if (tc != null) {
 		for (int i=tc.length-1; i>-1 ;i--){
 			Connection con = tc[i].getConnection();
 			if (null != con) {
 				FreeCall c = (FreeCall) con.getCall();
 				if (c != null) {
 					c.addCallObserver(observer, this);
 				}
 			}
 		}
 	}
 
 }
 /**
  * Add an old-style Observer to the Terminal.  This doesn't report much.
  * If this is the first observer or listener on the Terminal and soft Terminal caching is supported,
  * then protect this from garbage collection.
  */
 public void addObserver(TerminalObserver observer) throws MethodNotSupportedException, ResourceUnavailableException {
 	// test if we should protect this Terminal from garbage collection
 	if (observer != null) {
 		this.protect();
 	}
 	observers.addObserver(observer);
 }
 void addTerminalConnection(FreeTerminalConnection ftc) {
 	// add the terminal connection
 	terminalConnections.add(new TCHolder(ftc));
 
 	// find the call and assign listeners to it
 	FreeCall c = (FreeCall) ftc.getConnection().getCall();
 	CallListener[] lists = this.getCallListeners();
 	if (lists != null)
 		for (int i = 0; i < lists.length; i++) {
 			CallListener l = lists[i];
 			c.addCallListener(l, this);
 		}
 		// assign any observers
 	CallObserver[] obs = this.getCallObservers();
 	if (obs != null)
 		for (int i = obs.length-1; i>-1; i--) {
 			c.addCallObserver(obs[i], this);
 		}
 }
 /**
  * Add a Listener to the Terminal.  This doesn't report much.
  * If this is the first observer or listener on the Terminal and soft Terminal caching is supported,
  * then protect this from garbage collection.
  */
 public synchronized void addTerminalListener(TerminalListener l) {
 	Vector v = terminalListeners == null ? new Vector(2) : (Vector) terminalListeners.clone();
 	// check if we will need protection
 	if (l != null)
 		this.protect();
 	if (!v.contains(l)) {
 		v.addElement(l);
 		terminalListeners = v;
 	}
 }
   protected void fireTerminalListenerEnded(TerminalEvent e) {
 	if (terminalListeners != null) {
 	  Vector listeners = terminalListeners;
 	  int count = listeners.size();
 	  for (int i = 0; i < count; i++)
 		((TerminalListener) listeners.elementAt(i)).terminalListenerEnded(e);
 	}
   }        
   /**
    * this is an atypical fire method - in that it only fires on a single
    * listener, not all of them.
    */
   protected void fireTermListenerEnded(TerminalListener l ) {
 	TerminalEvent e = new FreeTermObservationEndedEv(Event.CAUSE_NORMAL,	// cause
 			Ev.META_UNKNOWN,	// what is the meta-event
 			false,				// is metaevent
 			this);				// FreeTerminal
 	l.terminalListenerEnded(e);
   }      
 /**
  * Return the set of Addresses associated with the Terminal.  If these haven't been fetched yet,
  * ask the raw provider for them.
  **/
 public Address[] getAddresses() {
 	// check if we need to retrieve the Address list
 	if (this.addresses == null) {
 		synchronized (this) {
 			// now double check
 			if (this.addresses == null) {
 				this.addresses = new HashSet(1);
 				try {
 				String[] addrNames = ((GenericProvider) this.getProvider()).getRaw().getAddresses(this.getName());
 				if (addrNames != null) {
 					int size = addrNames.length;
 					for (int i = 0; i < size; i++) {
 						this.addresses.add(addrNames[i]);
 					}
 				}
 				} catch (InvalidArgumentException iae) {
 					// no addresses -- we shouldn't have this in the domain then
 					// we'll catch this below
 				}
 			}
 		}
 	}
 		// transforn collection of Terminal names to array of terminals
 	synchronized (addresses) {
 		Address[] ret = null;
 		if (this.addresses.isEmpty())
 			throw new PlatformException("Terminal " + this.getName() + " has no Addresses.");
 
 		ret = new Address[addresses.size()];
 		Iterator it = addresses.iterator();
 		int i = 0;
 		while (it.hasNext()) {
 			String addrName = (String)it.next();
 			ret[i] = ((GenericProvider) this.getProvider()).getDomainMgr().getLocalAddress(addrName);
 			i++;
 		}
 
 		return ret;
 	}
 }
 /**
  * Return a set of CallListeners for this Terminal, or null if none exist.
  * @return javax.telephony.CallListener[]
  */
 public CallListener[] getCallListeners() {
 	CallListener[] ret = null;
 	if (callListeners != null) {
 		synchronized (callListeners) {
 			ret = new CallListener[callListeners.size()];
 			callListeners.copyInto(ret);
 		}
 		if (ret.length == 0) {
 			ret = null;
 		}
 	}
 	return ret;
 }
 /**
  * getCallObservers method comment.
  */
 public CallObserver[] getCallObservers() {
 	return (CallObserver[]) callObservers.toArray(new CallObserver[0]);
 }
 /**
  * getCapabilities method comment.
  */
 public TerminalCapabilities getCapabilities() {
 	return ((GenTerminalCapabilities)this.getProvider().getTerminalCapabilities()).getDynamic(this);
 }
   public String getName() {
 	return name;
   }        
 /**
  * getObservers method comment.
  */
 public javax.telephony.TerminalObserver[] getObservers() {
 	return (TerminalObserver[]) observers.getObjects();
 }
 /**
  * Get any PrivateData associated with my low-level object.
  */
 public Object getPrivateData() {
 	return ((GenericProvider)this.getProvider()).getRaw().getPrivateData(null, null, this.getName());
 }
   public Provider getProvider() {
 	return provider;
   }        
 /**
  * getTerminalCapabilities method comment.
  * @deprecated
  */
 public TerminalCapabilities getTerminalCapabilities(Terminal terminal, Address address) throws PlatformException, InvalidArgumentException {
 	return this.getCapabilities();
 }
   /**
    * Transform the collection of weak TCHolders into an array of TerminalConnection objects.
    **/
 public TerminalConnection[] getTerminalConnections() {
 	TerminalConnection[] ret = null;
 
 	// check if we need to ask the raw TelephonyProvider to flush in my calls
 	CallMgr cm = ((GenericProvider)this.getProvider()).getCallMgr();
 	if (cm.isDynamic()) {
 		cm.loadCalls(this);
 	}
 
 	// now dereference all my TerminalConnections
 	synchronized (terminalConnections) {
             ret = new TerminalConnection[terminalConnections.size()];
             // check if the array is empty
             if (ret.length == 0) {
                 return null;
             }
             Iterator it = terminalConnections.iterator();
             int i = 0;
             while (it.hasNext()) {
                 ret[i] = ((TCHolder) it.next()).getTerminalConnection();
                 i++;
             }
 	}
 	return ret;
 }
 /**
  * This method was created in VisualAge.
  * @return javax.telephony.TerminalListener[]
  */
 public TerminalListener[] getTerminalListeners() {
 	return null;
 }
   /**
    * Determine if I have any current Observers or Listeners.
    **/
 private boolean isObserved() {
 	// test if we should protect the Address
 	if (((this.terminalListeners == null) || (this.terminalListeners.size() == 0)) &&
 		((this.observers == null) || (this.observers.size() == 0))) {
 		return false;
 	} else {
 		return true;
 	}
 }      
   /**
    * protect this address from garbage collection if dynamic Address memory is supported
    **/
 private void protect() {
 	// test if we are about to add the first observer
 	if (!isObserved()) {
 		((GenericProvider)this.getProvider()).getDomainMgr().protect(this);
 	}
 }      
 public synchronized void removeCallListener(CallListener l) {
 	if (callListeners.contains(l)) {
 		Vector v = (Vector) callListeners.clone();
 		v.removeElement(l);
 		callListeners = v;
 	}
 }
 /**
  * Remove an observer that is registered to be added to calls visiting this terminal.
  * @param observer The observer to add to calls coming to this terminal.
  */
 public void removeCallObserver(CallObserver observer) {
 	this.callObservers.remove(observer);
 }
 /**
  * Remove an old-style observer from the Terminal.
  * If this is the last observer or listener on the Terminal and soft Terminal caching is supported,
  * then unprotect this from garbage collection.
  */
 public void removeObserver(TerminalObserver observer) {
 	if (observers.removeObserver(observer)) {
 		sendTermObservationEndedEv(observer);
 		this.unProtect();
 	}
 }
   void removeTerminalConnection(FreeTerminalConnection ftc){
 	terminalConnections.remove(new TCHolder(ftc));
   }          
 /**
  * Remove a listener from the Terminal.
  * If this is the last observer or listener on the Terminal and soft Terminal caching is supported,
  * then unprotect this from garbage collection.
  */
 public synchronized void removeTerminalListener(TerminalListener l) {
 	if (terminalListeners != null && terminalListeners.contains(l)) {
 		Vector v = (Vector) terminalListeners.clone();
 		v.removeElement(l);
 		terminalListeners = v;
 		fireTermListenerEnded(l);
 		// test if we can free up for garbage collection.
 		this.unProtect();
 	}
 }
 /**
  * Forward the event off to all observers and listeners
  * Creation date: (2000-02-14 15:06:59)
  * @author: Richard Deadman
  * @param ev The event to forward
  */
 public void send(FreeTerminalEvent ev) {
 	// send to observers
 	this.sendToObservers(ev);
 
 	// send to listeners
 	if (this.terminalListeners != null) {
 		Iterator it = this.terminalListeners.iterator();
 		while (it.hasNext()) {
 			TerminalListener tl = (TerminalListener) it.next();
 			switch (ev.getID()) {
 				case TerminalEvent.TERMINAL_EVENT_TRANSMISSION_ENDED :
 					{
 						tl.terminalListenerEnded(ev);
 					}
 			}
 		}
 	}
 }
 /**
  * Send PrivateData to my low-level object for processing.
  */
 public java.lang.Object sendPrivateData(java.lang.Object data) {
 	return ((GenericProvider)this.getProvider()).getRaw().sendPrivateData(null, null, this.getName(), data);
 }
   /**
    * this is an atypical send method - in that it only fires on a single
    * observer, not all of them.
    */
   protected void sendTermObservationEndedEv(TerminalObserver to){
 	TermEv e[] = new TermObservationEndedEv[1];
 	e[0] = new FreeTermObservationEndedEv(Ev.CAUSE_NORMAL,	// cause
 			Ev.META_UNKNOWN,	// what is the meta-event
 			false,				// is metaevent
 			this);				// FreeTerminal
 	to.terminalChangedEvent(e);
   }        
 /**
  * Forward the event off to all observers.
  * Creation date: (2000-02-14 15:06:59)
  * @author: Richard Deadman
  * @param ev The event to forward
  */
 void sendToObservers(FreeTerminalEvent ev) {
 	// send to observers
 	FreeTerminalEvent[] evs = {ev};
 	this.observers.sendEvents(evs);
 }
 /**
  * Set the weak Address reference names for this Terminal.
  * Creation date: (2000-06-22 12:09:31)
  * @author: Richard Deadman
  * @param names A set of names for the associated Addresses.
  */
 void setAddressNames(String[] names) {
 	HashSet addr = this.addresses = new HashSet();
 
 	if (names != null) {
 		int size = names.length;
 		for (int i = 0; i < size; i++) {
 			addr.add(names[i]);
 		}
 	}
 }
   public void setName(String newName) {
 	name = newName;
   }        
 /**
  * Set PrivateData to be used in the next low-level command.
  */
 public void setPrivateData(java.lang.Object data) {
 	((GenericProvider)this.getProvider()).getRaw().setPrivateData(null, null, this.getName(), data);
 }
   public void setProvider(Provider newProvider) {
 	provider = newProvider;
   }        
 /**
  * Called after Call Observers or Listeners are attached to the Terminal.
  * This will determine the current throttle state of the raw provider and update it
  * if necessary.
  * Creation date: (2000-05-04 15:23:56)
  * @author: Richard Deadman
  */
 private synchronized void startEvents() throws ResourceUnavailableException {
 	GenericProvider prov = null;
 	if (!this.reporting) {
 		if ((prov = (GenericProvider)this.getProvider()).getRawCapabilities().throttle) {
 			try {
 				prov.getRaw().reportCallsOnTerminal(this.getName(), true);
 			} catch (InvalidArgumentException iae) {
 				// logic error!
 				throw new RuntimeException("Error setting terminal reporting");
 			}
 		}
 		this.reporting = true;
 	}
 }
 /**
  * Called after Call Observers or Listeners are detatched from the Terminal.
  * This will determine the current throttle state of the raw provider and update it
  * if necessary.
  * Creation date: (2000-05-04 15:23:56)
  * @author: Richard Deadman
  */
 private synchronized void stopEvents() {
 	GenericProvider prov = null;
 	if (this.reporting &&
 			this.callListeners.size() == 0 &&
 			this.callObservers.size() == 0) {
 			if ((prov = (GenericProvider)this.getProvider()).getRawCapabilities().throttle) {
 				try {
 					prov.getRaw().reportCallsOnTerminal(this.getName(), false);
 				} catch (InvalidArgumentException iae) {
 					// logic error!
 					throw new RuntimeException("Error clearing terminal reporting");
 				} catch (ResourceUnavailableException rue) {
 					// eat it
 				}
 			}
 			this.reporting = false;
 	}
 }
   /**
    * Unprotect this address from garbage collection if dynamic Address memory is supported
    **/
 private void unProtect() {
 	// test if we just removed the last observer
 	if (!isObserved()) {
 		((GenericProvider)this.getProvider()).getDomainMgr().unProtect(this);
 	}
 }      
 
 	/**
 	 * Describe myself
 	 */
 	public String toString() {
 		return "Terminal: " + this.getName();
 	}
 }
