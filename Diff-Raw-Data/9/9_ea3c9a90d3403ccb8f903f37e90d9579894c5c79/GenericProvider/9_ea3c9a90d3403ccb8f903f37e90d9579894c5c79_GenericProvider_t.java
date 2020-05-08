 package net.sourceforge.gjtapi;
 
 /*
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
 import java.lang.ref.WeakReference;
 import net.sourceforge.gjtapi.capabilities.Capabilities;
 /* Copyright UForce Inc. 2000 */
 /**
  * This is the provider that is responsibile for setting up the
  * high-level JTAPI services and linking them into the associated
  * low-level RawProvider.
  *
  * <P>This implements the marker method MediaProvider to indicate that it can support its own
  * MediaServices.
  * <P>It implements RawListener so that it can hookup and forward on low-level events
  **/
 import java.util.*;
 import javax.telephony.*;
 import javax.telephony.events.*;
 import javax.telephony.capabilities.*;
 import javax.telephony.media.*;
 import net.sourceforge.gjtapi.media.FreeMediaTerminal;
 import net.sourceforge.gjtapi.util.*;
 import javax.telephony.privatedata.PrivateData;
 
 import net.sourceforge.gjtapi.events.*;
 
 public class GenericProvider implements MediaProvider, javax.telephony.privatedata.PrivateData, Provider {
 	int state = Provider.OUT_OF_SERVICE;
 	private String serv;					// name of raw telephony provider
 	private TelephonyProvider rawProvider;
 	private CallMgr callMgr = null;
 	private DomainMgr domainMgr = null;
 	private transient Vector providerListeners;
 	private net.sourceforge.gjtapi.media.MediaMgr mediaMgr = new net.sourceforge.gjtapi.media.MediaMgr();
 	private net.sourceforge.gjtapi.capabilities.Capabilities capabilities = null;
 	private RawEventHandler eventHandler = null;
 	// callback handle on a Jain Provider, if Jain decoration is used.
 	private net.sourceforge.gjtapi.jcc.Provider jainProv = null;
 	private ObservableHelper observers = new ObservableHelper() {
 		// make an array of ProviderObservers
 		Object[] mkObserverArray(int i) {
 			return new ProviderObserver[i];
 		}
 		void notifyObserver(Object o, Ev[] e) {
 			((ProviderObserver) o).providerChangedEvent((ProvEv[]) e);
 		}
 	};
 /**
  * Create a Generic constructor and hook it up to the raw provider
  **/
 GenericProvider(String name, TelephonyProvider raw, Map props) {
 	super();
 
 	// set things up and hook up the raw provider
 	this.serv = name;
 	this.rawProvider = raw;
 
 	// load my capabilities
 	this.capabilities = new Capabilities();
 	this.capabilities.setCapabilities(raw.getCapabilities());
 
 	// initialize addresses and terminals
 	this.initialize(props);
 
 	// Add myself as a listener
 	raw.addListener(this.setEventHandler(new RawEventHandler(this)));
 }
 /**
  * addObserver method comment.
  */
 public void addObserver(ProviderObserver observer) throws MethodNotSupportedException, ResourceUnavailableException {
   observers.addObserver(observer);
 }
 /**
  * Add a ProviderListener to my set
  */
  // This uses some synchonization tricks from WestHawk
 public synchronized void addProviderListener(ProviderListener listener) throws MethodNotSupportedException, ResourceUnavailableException {
 	Vector v = providerListeners == null ? new Vector(2) : (Vector) providerListeners.clone();
 	if (!v.contains(listener)) {
 	  v.addElement(listener);
 	  providerListeners = v;
 	}
 }
 /**
  * Create a new Call Object
  */
 public Call createCall() throws InvalidStateException, PrivilegeViolationException, MethodNotSupportedException, ResourceUnavailableException {
 	FreeCall ret = null;
 	if (this.getState() == Provider.IN_SERVICE) {
 		ret = new FreeCall();
 		ret.setProvider(this);
 		this.getCallMgr().preRegister(ret);
 	} else {
 		throw new InvalidStateException(this, InvalidStateException.PROVIDER_OBJECT, this.getState());
 	}
 	return ret;
 }
 /**
  * Dispatch an event off to its registered listeners.
  * Creation date: (2000-05-02 12:36:08)
  * @author: Richard Deadman
  * @param ev The event to dispatch.
  */
 void dispatch(FreeCallEvent ev) {
 	this.getEventHandler().dispatch(ev);
 }
 /**
  * Get the Address object associated with a certain number.
  * <P>If this is not already known, we may query the Raw telephony provider and create the dynamic
  * Address on the fly.  This occurs if the raw telephony provider did not return a static Address set or
  * if the (non-strict-JTAPI) case where dynamic Addresses are allowed on top of the static Address set.
  */
 public Address getAddress(String number) throws InvalidArgumentException {
 	return this.getDomainMgr().getFaultedAddress(number);
 }
 /**
  * getAddressCapabilities method comment.
  */
 public AddressCapabilities getAddressCapabilities() {
 	return this.capabilities.getAddressCapabilities();
 }
 /**
  * getAddressCapabilities method comment.
  */
 public AddressCapabilities getAddressCapabilities(Terminal terminal) throws PlatformException, InvalidArgumentException {
 	return this.getAddressCapabilities();
 }
 /**
  * Return an array of addresses, unless it is too big to have been preloaded.
  */
 public Address[] getAddresses() throws ResourceUnavailableException {
 	return this.getDomainMgr().getAddresses();
 }
 /**
  * Return the first known MediaTerminal with no connections currently on it.
  * Only known MediaTerminals are checked.
  * This may be used to find a MediaTerminal to bind a media service to.
  * Creation date: (2000-06-22 13:05:04)
  * @author: Richard Deadman
  * @return An idle MediaTerminal or null.
  */
 public MediaTerminal getAvailableMediaTerminal() {
 	MediaTerminal mt = null;
 	DomainMgr mgr = this.getDomainMgr();
 	Iterator it = mgr.mediaTerminals();
 	while (it.hasNext()) {
 		String termName = (String)it.next();
 		FreeMediaTerminal maybe = (FreeMediaTerminal)mgr.getLazyTerminal(termName, true);
 		TerminalConnection[] tcs = maybe.getTerminalConnections();
 		if (tcs == null || tcs.length == 0) {
 			mt = maybe;
 			break;
 		}
 	}
 
 	return mt;
 }
 /**
  * getCallCapabilities method comment.
  */
 public CallCapabilities getCallCapabilities() {
 	return this.capabilities.getCallCapabilities();
 }
 /**
  * getCallCapabilities method comment.
  */
 public CallCapabilities getCallCapabilities(Terminal terminal, Address address) throws PlatformException, InvalidArgumentException {
 	return this.getCallCapabilities();
 }
 /**
  * Package Accessor for the CallManager aggregate helper object.
  */
 CallMgr getCallMgr() {
 	return this.callMgr;
 }
 /**
  * Return an array (snapshot) of currently active calls.
  *
  * @author Richard Deadman
  * *
  */
 public Call[] getCalls() throws ResourceUnavailableException {
 	CallMgr cm = this.getCallMgr();
 	if (cm.isDynamic())
 		throw new ResourceUnavailableException(ResourceUnavailableException.UNSPECIFIED_LIMIT_EXCEEDED,
 			"The TelephonyProvider does not support querying the full call set");
 
 		// otherwise turn the known call set into an array of Call objects
 	Call[] calls = cm.toArray();
 	
 		// check if the array is empty
 	if (calls.length == 0)
 		return null;
     return calls;
 }
 /**
  * getCapabilities method comment.
  */
 public ProviderCapabilities getCapabilities() {
 	return this.capabilities.getProviderCapabilities();
 }
 /**
  * getConnectionCapabilities method comment.
  */
 public ConnectionCapabilities getConnectionCapabilities() {
 	return this.capabilities.getConnectionCapabilities();
 }
 /**
  * getConnectionCapabilities method comment.
  * @deprecated
  */
 public ConnectionCapabilities getConnectionCapabilities(Terminal terminal, Address address) throws PlatformException, InvalidArgumentException {
 	return this.getConnectionCapabilities();
 }
 /**
  * Package-scope client event dispatch pool.
  * Creation date: (2000-04-25 14:14:22)
  * @author: Richard Deadman
  * @return A BlockManager that takes EventHandlers and runs them with a null visitor.
  */
 BlockManager getDispatchPool() {
 	return this.getEventHandler().getDispatchPool();
 }
 /**
  * Package-level accessor to the Domain (Address and Terminal) manager.
  * Creation date: (2000-06-19 11:27:23)
  * @author: Richard Deadman
  * @return The object that manages the address and terminal object space.
  */
 DomainMgr getDomainMgr() {
 	return domainMgr;
 }
 /**
  * Return the event handler that listens for RawProvider events.
  * Creation date: (2000-04-18 23:43:11)
  * @author: Richard Deadman
  * @return A RawEventHandler responsible for listening for and handling RawProvider events.
  */
 private RawEventHandler getEventHandler() {
 	return eventHandler;
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-11-14 16:01:26)
  * @return com.uforce.jain.generic.Provider
  */
 net.sourceforge.gjtapi.jcc.Provider getJainProvider() {
 	return this.jainProv;
 }
 /**
  * MediaMgr accessor
  * Creation date: (2000-03-29 12:16:04)
  * @author: Richard Deadman
  * @return The holder for assigned and waiting MediaServices
  */
 public net.sourceforge.gjtapi.media.MediaMgr getMediaMgr() {
 	return this.mediaMgr;
 }
 /**
  * getName method comment.
  */
 public String getName() {
 	return serv;
 }
 /**
  * delegate getObservers .
  */
 public ProviderObserver[] getObservers() {
 	return (ProviderObserver[]) observers.getObjects();
 }
 /**
  * Get any PrivateData associated with my low-level object.
  */
 public Object getPrivateData() {
 	return this.getRaw().getPrivateData(null, null, null);
 }
 /**
  * getProviderCapabilities method comment.
  */
 public ProviderCapabilities getProviderCapabilities() {
 	return this.capabilities.getProviderCapabilities();
 }
 /**
  * getProviderCapabilities method comment.
  * @deprecated
  */
 public ProviderCapabilities getProviderCapabilities(Terminal terminal) throws PlatformException, InvalidArgumentException {
 	return this.getProviderCapabilities();
 }
 /**
  * Return an array of my listeners
  * @return An array of my listeners
  */
 public ProviderListener[] getProviderListeners() {
 	return (ProviderListener[])providerListeners.toArray(new ProviderListener[0]);
 }
 /**
  * Return the plugged-in raw provider
  * Creation date: (2000-02-04 15:48:41)
  * @author: Richard Deadman
  * @return A low-level telephony provider
  */
 public TelephonyProvider getRaw() {
 	return this.rawProvider;
 }
 /**
  * Get the API requirements for the raw provider hooked into me.
  */
 public net.sourceforge.gjtapi.capabilities.RawCapabilities getRawCapabilities() {
 	return this.capabilities.getRawCapabilities();
 }
 /**
  * getState method comment.
  */
 public int getState() {
 	return state;
 }
 /**
  * Terminal lookup
  */
 public Terminal getTerminal(String name) throws InvalidArgumentException {
 	return this.getDomainMgr().getFaultedTerminal(name);
 }
 /**
  * getTerminalCapabilities method comment.
  */
 public TerminalCapabilities getTerminalCapabilities() {
 	return this.capabilities.getTerminalCapabilities();
 }
 /**
  * getTerminalCapabilities method comment.
  * @deprecated
  */
 public TerminalCapabilities getTerminalCapabilities(Terminal terminal) throws PlatformException, InvalidArgumentException {
 	return this.getTerminalCapabilities();
 }
 /**
  * getTerminalConnectionCapabilities method comment.
  */
 public TerminalConnectionCapabilities getTerminalConnectionCapabilities() {
 	return this.capabilities.getTerminalConnectionCapabilities();
 }
 /**
  * getTerminalConnectionCapabilities method comment.
  * @deprecated
  */
 public TerminalConnectionCapabilities getTerminalConnectionCapabilities(Terminal terminal) throws PlatformException, InvalidArgumentException {
 	return this.getTerminalConnectionCapabilities();
 }
 /**
  * Return a my terminal collection.
  */
 public Terminal[] getTerminals() throws ResourceUnavailableException {
 	return this.getDomainMgr().getTerminals();
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-11-14 15:43:10)
  * @param prov com.uforce.jain.generic.Provider
  */
 public void hookupJainCallback(net.sourceforge.gjtapi.jcc.Provider prov) {
 	this.jainProv = prov;
 }
 /**
  * Initialize the provider
  * Creation date: (2000-02-11 11:04:54)
  * @author: Richard Deadman
  */
 private void initialize(Map props) {
 
 	// handle the properties I care about
 
 	// load the addresses and associated terminals
	boolean dynamic = this.getRawCapabilities().dynamicAddresses;
	DomainMgr dm = this.setDomainMgr(new DomainMgr(this, dynamic));
	if (!dynamic) {
		dm.loadAddresses();
		dm.loadTerminals();
	}
 
 	// see if dynamic call querying is required
 	this.setCallMgr(new CallMgr(this, this.getRawCapabilities().throttle));
 
 	// turn me on
 	state = Provider.IN_SERVICE;
 }
 /**
  * removeObserver method comment.
  */
 public void removeObserver(ProviderObserver observer) {
   observers.removeObserver(observer) ;
 }
 /**
  * Remove one of my listeners.
  * @param l The ProviderListener to remove
  */
 public synchronized void removeProviderListener(ProviderListener l) {
 	if (providerListeners != null && providerListeners.contains(l)) {
 	  Vector v = (Vector) providerListeners.clone();
 	  v.removeElement(l);
 	  providerListeners = v;
 	}
   }          
 /**
  * Send PrivateData to my low-level object for processing.
  */
 public java.lang.Object sendPrivateData(java.lang.Object data) {
 	return this.getRaw().sendPrivateData(null, null, null, data);
 }
 /**
  * Forward the event off to all observers.
  * Creation date: (2000-08-09 15:06:59)
  * @author: Richard Deadman
  * @param ev The event to forward
  */
 void sendToObservers(FreeEv ev) {
 	// send to observers
 	FreeEv[] evs = {ev};
 	this.observers.sendEvents(evs);
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 11:27:23)
  * @author: Richard Deadman
  * @param newCallMgr net.sourceforge.gjtapi.CallMgr
  */
 private void setCallMgr(CallMgr newCallMgr) {
 	callMgr = newCallMgr;
 }
 /**
  * Set the current DomainMgr.
  * Creation date: (2000-06-19 11:27:23)
  * @author: Richard Deadman
  * @param newDomainMgr The new manager or Address and Terminal sets.
  */
 private DomainMgr setDomainMgr(DomainMgr newDomainMgr) {
 	return this.domainMgr = newDomainMgr;
 }
 /**
  * Internal settor for the RawListener event handler.
  * Creation date: (2000-04-18 23:43:11)
  * @author: Richard Deadman
  * @param newEventHandler A RawListener event handler.
  */
 private TelephonyListener setEventHandler(RawEventHandler newEventHandler) {
 	return eventHandler = newEventHandler;
 }
 /**
  * Set PrivateData to be used in the next low-level command.
  */
 public void setPrivateData(java.lang.Object data) {
 	this.getRaw().setPrivateData(null, null, null, data);
 }
 /**
  * Instructs the Provider to shut itself down and perform all necessary cleanup.
  */
 public void shutdown() {
   state = Provider.SHUTDOWN;
   this.getRaw().removeListener(this.getEventHandler());
   this.getRaw().shutdown();
   rawProvider = null;
 }
 }
