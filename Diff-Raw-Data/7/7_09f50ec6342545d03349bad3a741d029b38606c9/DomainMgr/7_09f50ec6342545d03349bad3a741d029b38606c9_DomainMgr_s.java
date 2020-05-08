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
 import net.sourceforge.gjtapi.media.FreeMediaTerminal;
 import javax.telephony.*;
 import java.util.*;
 import net.sourceforge.gjtapi.util.*;
 /**
  * This is the Address and Terminal Lookup manager that brokers Address and Terminal lookup requests
  * for the Provider as well as Calls.
  * <P>There are three type of smart accessors:
  * <OL>
  *  <LI><B>getCachedXXX</B> - this means that the item will be looked for in the cache only.
  *  <LI><B>getFaultedXXX</B> - this means that the item will be looked for in the cache and,
  * if it isn't found, requested (flushed in) from the raw TelephonyProvider.
  *  <LI><B>getLazyXXX</B> - this means that the item will be looked for in the cache and, if
  * if it isn't found, a new empty one will be created.
  * </ol>
  * Creation date: (2000-06-15 14:03:03)
  * @author: Richard Deadman
  */
 class DomainMgr {
 	private boolean dynamicAddr = false;
 	private boolean dynamicTerm = false;
  	private GenericProvider provider = null;
 	private TelephonyProvider raw = null;	// shortcut tp provider->getRaw()
 			// map address names to Address for quick lookup
 	private Map localAddresses = null;
 		// String -> WeakReference(Address) map
 	private Map remoteAddresses = null;
 		// note if our static Address and Terminal arrays are null because they are too large
 		// If these are non-null, we are collecting dynamic Addresses and Terminals
 		// and mapping them here to their names as WeakReferences
 	private Map terminals = null;
 		// String -> WeakReference(Terminal) map
 	private Map remoteTerminals = null;
 		// The set of known terminals that can handle media
 	private HashSet mediaTerminals = new HashSet();
 		// The set of Terminals or Addresses with listeners attached -- these are strongly held to avoid
 		// garbage collection if we are using SoftMaps
 	private HashSet observed = null;
  /**
  * Constructor that determines which kind of maps to keep and how to resolve Address and Terminal requests.
  * Creation date: (2000-06-15 14:33:03)
  * @author: Richard Deadman
  * @param tp The raw TelephonyProvider handle.
  * @param isDynamic true if not all addresses and terminals are reported from the TelephonyProvider.
  */
 DomainMgr(GenericProvider gp, boolean isDynamic) {
 	super();
  	this.setRaw(gp.getRaw());
 	this.setProvider(gp);
 	this.setDynamic(isDynamic);
 	if (isDynamic) {
 		this.setLocalAddresses(Collections.synchronizedMap(new SoftMap()));
 		this.setTerminalSet(Collections.synchronizedMap(new SoftMap()));
 		this.setObserved();	// allow protection of Addresses and Terminals
 	} else {
 		this.setLocalAddresses(Collections.synchronizedMap(new HashMap()));
 		this.setTerminalSet(Collections.synchronizedMap(new HashMap()));
 	}
 	this.setRemoteAddresses(Collections.synchronizedMap(new SoftMap()));
 	this.setRemoteTerminals(Collections.synchronizedMap(new SoftMap()));
 }
 /**
  * Factory to create a (local) Address
  * @param number The name of the Address to create.
  * @return The created FreeAddress.
  */
 private FreeAddress createLocalAddress(String number) {
 	FreeAddress addr = new FreeAddress(number, this.getProvider(), true);
 		// Now store an explicit or implicit handle
 	this.putLocalAddress(addr);
 	
 	return addr;
 }
 /**
  * Factory to create a (remote) Address
  * @param number The name of the Address to create.
  * @return The created FreeAddress.
  */
 private FreeAddress createRemoteAddress(String number) {
 	FreeAddress addr = new FreeAddress(number, this.getProvider(), false);
 		// Now store an explicit or implicit handle
 	this.putRemoteAddress(addr);
 	
 	return addr;
 }
 /**
  * Factory to create a Terminal.
  * <P>Create a MediaTerminal if the raw provider supports media
  * @param name The terminal name to load
  * @return The created FreeTerminal.
  */
 private FreeTerminal createTerminal(String name) {
 	FreeTerminal term = null;
 	GenericProvider prov = this.getProvider();
 	
 		// test if it's a media terminal
 	net.sourceforge.gjtapi.capabilities.RawCapabilities cap = prov.getRawCapabilities();
 	if (cap.media && (cap.allMediaTerminals || this.getRaw().isMediaTerminal(name))) {
 		term = new FreeMediaTerminal(name, prov);
 		this.getMediaTerminals().add(name);
 	} else {
 		term = new FreeTerminal(name, prov);
 	}
 		// Now store an explicit or implicit handle
 	this.putLocalTerminal(term);
  	return term;
 }
 /**
  * Factory to create a Terminal.
  * <P>Create a MediaTerminal if the raw provider supports media
  * @param name The terminal name to load
  * @param isMedia true if the new Terminal should be marked to support media.
  * @return The created FreeTerminal.
  */
 private FreeTerminal createTerminal(String name, boolean isMedia) {
 	FreeTerminal term = null;
 	GenericProvider prov = this.getProvider();
 	
 		// test if it's a media terminal
 	if (isMedia) {
 		term = new FreeMediaTerminal(name, prov);
 		this.getMediaTerminals().add(name);
 	} else {
 		term = new FreeTerminal(name, prov);
 	}
 		// Now store an explicit or implicit handle
 	this.putLocalTerminal(term);
  	return term;
 }
 /**
  * Factory to create a (remote) Terminal
  * @param name The name of the Terminal to create.
  * @return The created FreeTerminal.
  */
 private FreeTerminal createRemoteTerminal(String name) {
 	FreeTerminal term = new FreeTerminal(name, this.getProvider());
 		// Now store an explicit or implicit handle
 	this.putRemoteTerminal(term);
 	
 	return term;
 }
 /**
  * Return an array of addresses, unless it is too big to have been preloaded.
  */
 Address[] getAddresses() throws ResourceUnavailableException {
   if (this.isDynamicAddr()) {
 	throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN);
   }
    Map addr = this.getLocalAddresses();
   // test if it's null
   if (addr == null)
   	return null;
    return (Address[])addr.values().toArray(new Address[0]);
 }
 /**
  * Find a FreeAddress that is associated with a number, or null if none exists in the current cache.
  * This returns an address from either the local or remote cache.
  * Creation date: (2000-06-05 11:02:58)
  * @author: Richard Deadman
  * @return The found FreeAddress or null.
  * @param name The Address's logical number.
  */
 FreeAddress getCachedAddress(String number) {
 	FreeAddress addr = this.getCachedLocalAddress(number);
  	if (addr == null)
 		addr = this.getCachedRemoteAddress(number);
  	return addr;
 }
 /**
  * Find a FreeAddress that is associated with a number, or null if none exists in the current cache.
  * Creation date: (2000-06-05 11:02:58)
  * @author: Richard Deadman
  * @return The found FreeAddress or null.
  * @param number The Address's logical number.
  */
 private FreeAddress getCachedLocalAddress(String number) {
 	return (FreeAddress)this.getLocalAddresses().get(number);
 }
 /**
  * Find a FreeAddress that is associated with a remote number, or null if none exists in the current cache.
  * Creation date: (2000-06-05 11:02:58)
  * @author: Richard Deadman
  * @return The found FreeAddress or null.
  * @param number The Address's remote number.
  */
 private FreeAddress getCachedRemoteAddress(String number) {
 	return (FreeAddress)this.getRemoteAddresses().get(number);
 }
 /**
  * Return any cached Terminal given by a name.
  * Creation date: (2000-06-20 16:06:15)
  * @author: Richard Deadman
  * @return A Terminal, or null if none in the cache.
  * @param termName The name of the Terminal to find.
  */
 FreeTerminal getCachedTerminal(String termName) {
 	FreeTerminal term = this.getCachedLocalTerminal(termName);
  	if (term == null)
 		term = this.getCachedRemoteTerminal(termName);
  	return term;
 }
 /**
  * Find a local FreeTerminal that is associated with a name, or null if none exists in the current cache.
  * Creation date: (2000-06-05 11:02:58)
  * @author: Richard Deadman
  * @return The found FreeTerminal or null.
  * @param name The Terminal's logical name.
  */
 private FreeTerminal getCachedLocalTerminal(String name) {
 	return (FreeTerminal)this.getLocalTerminals().get(name);
 }
 /**
  * Find a remote FreeTerminal that is associated with a remote name, or null if none exists in the current cache.
  * Creation date: (2000-06-05 11:02:58)
  * @author: Richard Deadman
  * @return The found FreeTerminal or null.
  * @param name The Terminal's remote name.
  */
 private FreeTerminal getCachedRemoteTerminal(String name) {
 	return (FreeTerminal)this.getRemoteTerminals().get(name);
 }
 /**
  * Get the local Address object associated with a certain number.
  * <P>If this is not already known, we may query the Raw telephony provider and create the dynamic
  * Address on the fly.  This occurs if the raw telephony provider did not return a static Address set or
  * if the (non-strict-JTAPI) case where dynamic Addresses are allowed on top of the static Address set.
  */
 FreeAddress getFaultedAddress(String number) throws InvalidArgumentException {
 	FreeAddress addr = this.getCachedLocalAddress(number);
  	// test is we still haven't found the Address
 	if (addr == null) {
 			// do we need to check dynamically?
 		if (this.isDynamicAddr() || this.getProvider().getRawCapabilities().dynamicAddresses) {
 				// may throw InvalidArgumentException if invalid
 			TermData[] terms = this.getRaw().getTerminals(number);
 				//create the valid address
 			addr = this.createLocalAddress(number);
 				// tell it about its terminals
 			addr.setTerminalData(terms);
 		} else
 			throw new InvalidArgumentException("Address " + number + " not known to Provider.");
  	}
 		
 	return addr;
 }
 /**
  * Get the Terminal object associated with a certain number.
  * <P>If this is not already known, we may query the Raw telephony provider and create the dynamic
  * Terminal on the fly.  This occurs if the raw telephony provider did not return a static Terminal set or
  * if the (non-strict-JTAPI) case where dynamic Terminals are allowed on top of the static Terminal set.
  * @param name The name of the terminal to find.
  * @return A cached or faulted from the provider Terminal
  * @throws InvalidArgumentException If the terminal is not valid.
  */
 FreeTerminal getFaultedTerminal(String name) throws InvalidArgumentException {
 	FreeTerminal term = this.getCachedTerminal(name);
  	// test is we still haven't found the Terminal
 	if (term == null) {
 			// do we need to check dynamically?
 		if (this.isDynamicTerm() || this.getProvider().getRawCapabilities().dynamicAddresses) {
 				// may throw InvalidArgumentException if invalid
 			TelephonyProvider tp = this.getRaw();
 			String[] addrNames = tp.getAddresses(name);
 				//create the valid address
 			term = this.createTerminal(name);
 				// tell it about its terminals
 			term.setAddressNames(addrNames);
 		} else
 			throw new InvalidArgumentException("Terminal " + name + " not known to Provider.");
  	}
 		
 	return term;
 }
 /**
  * Get the Terminal object associated with a certain number.
  * <P>If this is not already known, we may query the Raw telephony provider and create the dynamic
  * Terminal on the fly.  This occurs if the raw telephony provider did not return a static Terminal set or
  * if the (non-strict-JTAPI) case where dynamic Terminals are allowed on top of the static Terminal set.
  * @param name The name of the terminal to find.
  * @param media Does the terminal support media?
  * @return A cached or faulted from the provider Terminal
  * @throws InvalidArgumentException If the terminal is not valid.
  */
 FreeTerminal getFaultedTerminal(String name, boolean media) throws InvalidArgumentException {
 	FreeTerminal term = this.getCachedTerminal(name);
 
 
 	// test is we still haven't found the Terminal
 	if (term == null) {
 			// do we need to check dynamically?
 		if (this.isDynamicTerm() || this.getProvider().getRawCapabilities().dynamicAddresses) {
 				// may throw InvalidArgumentException if invalid
 			TelephonyProvider tp = this.getRaw();
 			String[] addrNames = tp.getAddresses(name);
 				//create the valid address
 			term = this.createTerminal(name, media);
 				// tell it about its terminals
 			term.setAddressNames(addrNames);
 		} else
 			throw new InvalidArgumentException("Terminal " + name + " not known to Provider.");
 
 
 	}
 		
 	return term;
 }
 /**
  * This returns either a local address or a remote address for a given string.
  * If dynamic address faulting is supported, the system will try to first ask the telephonyProvider
  * for the address.
  * For remote addresses, it will check a remote address cache, and lazily create one if necessary.
  * Note that this is public so that our events can access this method.  The most harm this can cause is
  * the erroneous addition or remote addresses in the weak cache.
  * Creation date: (2000-02-11 16:25:48)
  * @author: Richard Deadman
  * @return A new Address, either local or remote
  * @param name The name for the Address to find or create.
  */
 FreeAddress getLazyAddress(String name) {
 	FreeAddress addr = this.getCachedAddress(name);	// local and remote caches
  	if (addr == null) {		// see if we need to fault it in or we can just create a remote one
  		if (this.isDynamicAddr()) {
 			try {
 				addr = this.getFaultedAddress(name);
 			} catch (InvalidArgumentException iae) {
 				// must be a remote address
 			}
 		}
  		if (addr == null) {		// we either are not dynamic or the local faulting failed
 			addr = this.createRemoteAddress(name);
 		}
 	}
  	return addr;
 }
 /**
  * Get the Terminal object associated with a certain number.
  * <P>If this is not already known, we may query the Raw telephony provider and create the dynamic
  * Terminal on the fly.  This occurs if the raw telephony provider did not return a static Terminal set or
  * if the (non-strict-JTAPI) case where dynamic Terminals are allowed on top of the static Terminal set.
  * <P>This will create the Terminal if it is not found, so should only be called when the Terminal
  * is known to exist.
  * @param name The name of the terminal to find.
  * @param isMedia Does the terminal support media?
  * @return A cached or lazily created Terminal
  */
 FreeTerminal getLazyTerminal(String name, boolean isMedia) {
 	FreeTerminal term = this.getCachedTerminal(name);
  	// test is we still haven't found the Terminal
 	if (term == null) {
 			// do we need to check dynamically?
 		if (this.isDynamicTerm() || this.getProvider().getRawCapabilities().dynamicAddresses) {
 			try {
 				term = this.getFaultedTerminal(name, isMedia);
 			} catch (InvalidArgumentException iae) {
 				// must be a remote address
 			}
 		}
 			
 		if (term == null)
 			term = this.createRemoteTerminal(name);
 
 	}
 		
 	return term;
 }
 /**
  * Get the Terminal object associated with a certain number.
  * <P>If this is not already known, we may query the Raw telephony provider and create the dynamic
  * Terminal on the fly.  This occurs if the raw telephony provider did not return a static Terminal set or
  * if the (non-strict-JTAPI) case where dynamic Terminals are allowed on top of the static Terminal set.
  * <P>This will create the Terminal if it is not found, so should only be called when the Terminal
  * is known to exist.
  * @param name The name of the terminal to find.
  * @return A cached or lazily created Terminal
  */
 FreeTerminal getLazyTerminal(String name) {
 	FreeTerminal term = this.getCachedTerminal(name);
 
 
 	// test is we still haven't found the Terminal
 	if (term == null) {
 			// do we need to check dynamically?
 		if (this.isDynamicTerm() || this.getProvider().getRawCapabilities().dynamicAddresses) {
 			try {
 				term = this.getFaultedTerminal(name);
 			} catch (InvalidArgumentException iae) {
 				// must be a remote address
 			}
 		}
 			
 		if (term == null)
 			term = this.createRemoteTerminal(name);
 
 	}
 		
 	return term;
 }
 /**
  * Factory to get (local) a Address
  * If Address already exists, then return it.
  * Otherwise create a new one, and return it.
  * This is to be called by an object that has already been told the Address exists, like
  * a Terminal or Connection.
  * @param number The name of the Address to find or create.
  * @return The found or created FreeAddress.
  */
 FreeAddress getLocalAddress(String number) {
 	FreeAddress addr = this.getCachedLocalAddress(number);
 		// test if the Address has not yet been created.
 	if (addr == null) {
 		addr = this.createLocalAddress(number);
 	}
 	return addr;
 }
 /**
  * Internal accessor for the map of local Address names to Addresses
  * Creation date: (2000-06-19 11:08:05)
  * @author: Richard Deadman
  * @return A map of Address names to Addresses
  */
 private java.util.Map getLocalAddresses() {
 	return localAddresses;
 }
 /**
  * Internal accessor for the map of local Terminal names to Terminals
  * If a SoftMap is used, Terminals may be garbage collected if not used and the VM is short of memory.
  * Creation date: (2000-06-19 11:01:26)
  * @author: Richard Deadman
  * @return A strong or soft map of terminal names to Terminals.
  */
 private java.util.Map getLocalTerminals() {
 	return terminals;
 }
 /**
  * Internal accessor for the MediaTerminal name set.
  * Creation date: (2000-06-19 11:08:08)
  * @author: Richard Deadman
  * @return A set of MediaTerminal names.
  */
 private Set getMediaTerminals() {
 	return mediaTerminals;
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 12:24:57)
  * @author: Richard Deadman
  * @return net.sourceforge.gjtapi.GenericProvider
  */
 private GenericProvider getProvider() {
 	return provider;
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 11:01:26)
  * @author: Richard Deadman
  * @return net.sourceforge.gjtapi.TelephonyProvider
  */
 private TelephonyProvider getRaw() {
 	return raw;
 }
 /**
  * This returns a map of remote string->Address pairs.
  * The Address is wrapped in a weak reference so that the cache may
  * automatically clear.
  * Creation date: (2000-02-11 15:07:25)
  * @author: Richard Deadman
  * @return The Map that allows remote Addresses to be searched by name.
  */
 private Map getRemoteAddresses() {
 	return this.remoteAddresses;
 }
 /**
  * This returns a map of remote string->Terminal pairs.
  * The Terminal is wrapped in a weak reference so that the cache may
  * automatically clear.
  * Creation date: (2000-02-11 15:07:25)
  * @author: Richard Deadman
  * @return The Map that allows remote Terminals to be searched by name.
  */
 private Map getRemoteTerminals() {
 	return this.remoteTerminals;
 }
 /**
  * Return a my terminal collection.
  */
 Terminal[] getTerminals() throws ResourceUnavailableException {
   if (this.isDynamicTerm()) {
 	throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN);
   }
    Map terms = this.getLocalTerminals();
   // test if it's null
   if (terms == null)
   	return null;
    return (Terminal[])terms.values().toArray(new Terminal[0]);
 }
 /**
  * Note if our Address set is dynamic.  This is either indicated by a property or during
  * Address loading.
  * Creation date: (2000-06-22 13:22:16)
  * @author: Richard Deadman
  * @return true if I don't have a full set of Addresses in memory.
  */
 private boolean isDynamicAddr() {
 	return dynamicAddr;
 }
 /**
  * Note if our Terminal set is dynamic.  This is either indicated by a property or during
  * Address loading.
  * Creation date: (2000-06-22 13:22:16)
  * @author: Richard Deadman
  * @return true if I don't have a full set of Terminals in memory.
  */
 private boolean isDynamicTerm() {
 	return dynamicTerm;
 }
 /**
  * <p>Query the raw telephony provider
  * and create the initial set of Addresses.  Note that the associations between Addresses
  * and Terminals are lazily created.</p>
  * <P>An Address is a phone number.
  * </P>
  */
 void loadAddresses() {
 	TelephonyProvider raw = this.getRaw();
 	GenericProvider prov = this.getProvider();
  	// Get the Address names and map them to our localAddress set.
 	try {
 		String[] addresses = raw.getAddresses();
 		for (int i = 0; i < addresses.length; i++) {
 			FreeAddress a = new FreeAddress(addresses[i], prov, true);
  				// add to set of addresses;
 			this.putLocalAddress(a);
 		}
  	} catch (ResourceUnavailableException rue) {
 		// our address set is too large
 		if (!this.isDynamicAddr()) {
 			this.setDynamicAddr(true);
 			this.setLocalAddresses(Collections.synchronizedMap(new SoftMap()));
 			this.setObserved();		// allow protection from gc
 		}
 	}
 }
 /**
  * <p>Query the raw telephony provider
  * and create the Terminal set.  Associations between Addresses and Terminals are lazily queried.</p>
  * <P>A terminal is a device (ideally physical) like a phone
  * </P>
  * Note that terminal names may be associated with multiple Addresses
  */
 void loadTerminals() {
 	TelephonyProvider raw = this.getRaw();
 	GenericProvider prov = this.getProvider();
  	// Get the Terminal names and map them to our localAddress set.
 	try {
 		TermData[] terminals = raw.getTerminals();
 		for (int i = 0; i < terminals.length; i++) {
 			FreeTerminal t = this.createTerminal(terminals[i].terminal, terminals[i].isMedia);
 		}
  	} catch (ResourceUnavailableException rue) {
 		// our Terminal set is too large
 		if (!this.isDynamicTerm()) {
 			this.setDynamicTerm(true);
 			this.setTerminalSet(Collections.synchronizedMap(new SoftMap()));
 			this.setObserved();	// lazily create the observation gc protector
 		}
 	}
 }
 /**
  * Return an iterator over a set of MediaTerminal names.
  * At some point, the Terminals these point to were in the cache.
  * Note that "remove()" on the iterator will have no effect on the underlieing set.
  * Creation date: (2000-06-19 11:08:08)
  * @author: Richard Deadman
  * @return A set of MediaTerminal names.
  */
 Iterator mediaTerminals() {
 	return ((Set)mediaTerminals.clone()).iterator();
 }
 /**
  * This is called by a Terminal or Address that has been observed or Listened to so that it will be protected from
  * any garbage collection if dynamic tracking is used.
  * Creation date: (2000-06-23 10:59:02)
  * @author: Richard Deadman
  * @param call The object to protect from potential cache clearing.
  */
 void protect(Object termOrAddr) {
 	// check that we are using a WeakMap
 	HashSet obs = this.observed;
 	if (obs != null) {
 		obs.add(termOrAddr);
 	}
 }
 /**
  * Store the address indexed against its name.
  * Creation date: (2000-06-05 11:08:51)
  * @author: Richard Deadman
  * @param name The logical name of the Address.
  * @param term The Address to store.
  */
 private void putLocalAddress(FreeAddress addr) {
 	this.getLocalAddresses().put(addr.getName(), addr);
 }
 /**
  * Story the terminal indexed against its name.
  * Creation date: (2000-06-05 11:08:51)
  * @author: Richard Deadman
  * @param name The logical name of the Terminal.
  * @param term The terminal to store.
  */
 private void putLocalTerminal(FreeTerminal term) {
 	this.getLocalTerminals().put(term.getName(), term);
 }
 /**
  * Store the remote address indexed against its name.
  * Creation date: (2000-06-05 11:08:51)
  * @author: Richard Deadman
  * @param term The Address to store.
  */
 private void putRemoteAddress(FreeAddress addr) {
 	this.getRemoteAddresses().put(addr.getName(), addr);
 }
 /**
  * Store the remote terminal indexed against its name.
  * Creation date: (2000-06-05 11:08:51)
  * @author: Richard Deadman
  * @param term The Terminal to store.
  */
 private void putRemoteTerminal(FreeTerminal term) {
 	this.getRemoteTerminals().put(term.getName(), term);
 }
 /**
  * Note if the Addresses must be dynamically queried from the raw provider.
  * Creation date: (2000-06-02 16:10:23)
  * @author: Richard Deadman
  * @return true if the static getAddresses() set is null due to the raw providers inability to build a big enough set.
  * @param value true if the static getAddresses() set is null due to the raw providers inability to build a big enough set, false otherwise.
  */
 private void setDynamic(boolean newDynamic) {
 	this.setDynamicAddr(newDynamic);
 	this.setDynamicTerm(newDynamic);
 }
 /**
  * Note that the set of Addresses is not complete.
  * Creation date: (2000-06-22 13:22:16)
  * @author: Richard Deadman
  * @param newDynamicTerm true if I only hold a partial set of the domain Addresses.
  */
 private void setDynamicAddr(boolean newDynamicAddr) {
 	dynamicAddr = newDynamicAddr;
 }
 /**
  * Note that the set of terminals is not complete.
  * Creation date: (2000-06-22 13:22:16)
  * @author: Richard Deadman
  * @param newDynamicTerm true if I only hold a partial set of the domain Terminals.
  */
 private void setDynamicTerm(boolean newDynamicTerm) {
 	dynamicTerm = newDynamicTerm;
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 11:08:05)
  * @author: Richard Deadman
  * @param newLocalAddresses java.util.Map
  */
 private void setLocalAddresses(java.util.Map newLocalAddresses) {
 	localAddresses = newLocalAddresses;
 }
 /**
  * Lazy creator that creates the holder for domain objects to protect them from garbage collection.
  * The existance of this set turns on "holding".
  * Creation date: (2000-06-23 11:44:43)
  * @author: Richard Deadman
  */
 private synchronized void setObserved() {
 	if (this.observed == null)
 		this.observed = new HashSet();
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 12:24:57)
  * @author: Richard Deadman
  * @param newProvider net.sourceforge.gjtapi.GenericProvider
  */
 private void setProvider(GenericProvider newProvider) {
 	provider = newProvider;
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 11:01:26)
  * @author: Richard Deadman
  * @param newRaw net.sourceforge.gjtapi.TelephonyProvider
  */
 private void setRaw(TelephonyProvider newRaw) {
 	raw = newRaw;
 }
 /**
  * Set the map of Address numbers to weakly held remote addresses.
  * Creation date: (2000-06-19 11:08:08)
  * @author: Richard Deadman
  * @param newRemoteAddresses A map to store names to Addresses.
  */
 private void setRemoteAddresses(java.util.Map newRemoteAddresses) {
 	remoteAddresses = newRemoteAddresses;
 }
 /**
  * Set the map of Terminal names to weakly held remote terminals.
  * Creation date: (2000-06-19 11:08:08)
  * @author: Richard Deadman
  * @param newRemoteTerminals A map to store names to Terminals.
  */
 private void setRemoteTerminals(java.util.Map newRemoteTerminals) {
 	remoteTerminals = newRemoteTerminals;
 }
 /**
  * Set the map container for terminals.
  * his may be a normal strong map or a SoftMap.
  * Creation date: (2000-06-19 11:01:26)
  * @author: Richard Deadman
  * @param newTerminalSet A map to hold terminal names to Terminals.
  */
 private void setTerminalSet(Map newTerminalSet) {
 	this.terminals = newTerminalSet;
 }
 /**
  * This is called by a Terminal or Address that is no longer observed or Listened to so that it will
  * no longer be protected from any garbage collection if dynamic tracking is used.
  * Creation date: (2000-06-23 10:59:02)
  * @author: Richard Deadman
  * @param call The object to allow for potential cache clearing.
  */
 void unProtect(Object termOrAddr) {
 	// check that we are using a WeakMap
 	HashSet obs = this.observed;
 	if (obs != null) {
 		obs.remove(termOrAddr);
 	}
 }
 }
