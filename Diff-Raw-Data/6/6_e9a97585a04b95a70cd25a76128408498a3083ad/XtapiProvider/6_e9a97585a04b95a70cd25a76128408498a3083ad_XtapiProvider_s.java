 package net.sourceforge.gjtapi.raw.xtapi;
 
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.telephony.Address;
 import javax.telephony.Call;
 import javax.telephony.Connection;
 import javax.telephony.Event;
 import javax.telephony.InvalidArgumentException;
 import javax.telephony.InvalidPartyException;
 import javax.telephony.MethodNotSupportedException;
 import javax.telephony.PrivilegeViolationException;
 import javax.telephony.ProviderUnavailableException;
 import javax.telephony.ResourceUnavailableException;
 import javax.telephony.Terminal;
 import javax.telephony.TerminalConnection;
 import javax.telephony.events.ConnAlertingEv;
 import javax.telephony.events.ConnConnectedEv;
 import javax.telephony.events.ConnDisconnectedEv;
 import javax.telephony.events.ConnInProgressEv;
 import javax.telephony.events.TermConnActiveEv;
 import javax.telephony.events.TermConnDroppedEv;
 import javax.telephony.events.TermConnRingingEv;
 import javax.telephony.media.MediaResourceException;
 import javax.telephony.media.PlayerConstants;
 import javax.telephony.media.RTC;
 import javax.telephony.media.RecorderConstants;
 import javax.telephony.media.SignalDetectorConstants;
 import javax.telephony.media.Symbol;
 import net.sourceforge.gjtapi.CallId;
 import net.sourceforge.gjtapi.FreeAddress;
 import net.sourceforge.gjtapi.FreeCall;
 import net.sourceforge.gjtapi.FreeConnection;
 import net.sourceforge.gjtapi.FreeTerminal;
 import net.sourceforge.gjtapi.FreeTerminalConnection;
 import net.sourceforge.gjtapi.RawSigDetectEvent;
 import net.sourceforge.gjtapi.RawStateException;
 import net.sourceforge.gjtapi.TelephonyListener;
 import net.sourceforge.gjtapi.TermData;
 import net.sourceforge.gjtapi.media.FreeMediaTerminalConnection;
 import net.sourceforge.gjtapi.media.SymbolConvertor;
 import net.sourceforge.gjtapi.raw.MediaTpi;
 
 import net.xtapi.serviceProvider.*;
 
 /*
 *  GJTAPI - XTAPI Bridge
 *  Copyright (C) 2002 Richard Deadman
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
  * -------------
  * This code is released under the GLP instead of the BSX/X11 licence used by
  * the rest of GJTAPI since it calls XTAPI objects and implements XTAPI interfaces
  * that are licenced under the GPL. For that reason, the GJTAPI Service Provider
  * should not be bundled in the same jar as the reast of GJTAPI but should have its
  * own jar. While GPL libraries and jars can only be directly linked from other GPL
  * code, this does not change the GJTAPI licence, since it is the JTAPI application
  * that connects GJTAPI to this service provider. Base GJTAPI has no references to
  * it.
  *
  * @author  Richard Deadman
  * @version .01
  */
 
 /**
  * This is a bridge GJTAPI service provider that allows XTAPI service providers
  * to be plugged in underneath.
  */
 public class XtapiProvider implements MediaTpi, IXTapiCallBack {
 	
 	/**
 	 * This is identifier for calls known by GJTAPI. As such, it has existance
 	 * before an XTAPI id is created for it.
 	 * <P>Note that this uses identity for equality, since otherwise we have
 	 * to keep track of the disposal of a pool of identity tokens.
 	 */
 	public class XtapiCallId implements CallId {
 		// initially not set.
 		private int callNum = -1;
 		
 		public XtapiCallId() {
 			super();
 		}
 		
 		/**
 		 * Set the XTAPI id for the call.
 		 */
 		public void setCallNumber(int num) {
 			this.callNum = num;
 		}
 		
 		int getCallNum() { return callNum; }
 		
 		/**
 		 * Ensure equality works
 		 */
 		public boolean equals(Object other) {
 			if (other instanceof XtapiCallId) {
 				if (this.getCallNum() == ((XtapiCallId)other).getCallNum())
 					return true;
 			}
 			return false;
 		}
 		
 		/**
 		 * Ensure hashing works
 		 */
 		public int hashCode() {
 			return this.getCallNum();
 		}
 		
 	}
 	
 	/**
 	 * Simple holder for Address information.
 	 * XTAPI associates one terminal with each address and also holds
 	 * a raw handle for each address.
 	 */
 	class AddressInfo {
 		int line = -1;
 		String terminal = null;
 		int rawHandle = -1;
 		
 		AddressInfo(int lineNo, String termName, int handle) {
 			super();
 			
 			this.line = lineNo;
 			this.terminal = termName;
 			this.rawHandle = handle;
 		}
 		
 		String getName() { return ADDR_PREFIX + line; }
 
 		/**
 		 * Ensure equality works
 		 */
 		public boolean equals(Object other) {
 			if (other instanceof AddressInfo) {
 				AddressInfo ai = (AddressInfo)other;
 				if ((this.line == ai.line) &&
 					(this.terminal.equals(ai.terminal)) &&
 					(this.rawHandle == ai.rawHandle))
 						return true;
 			}
 			return false;
 		}
 		
 		/**
 		 * Ensure hashing works
 		 */
 		public int hashCode() {
 			return this.line + this.terminal.hashCode() + this.rawHandle;
 		}
 		
 	}
 
 	// The initialization map key	
 	private final static String IXTAPI_KEY = "XtapiSp";
 	
 	// The TAPI service provider
 	private final static String TAPI_SP = "net.xtapi.serviceProvider.MSTAPI";
 	
 	// The Comm service provider
 	private final static String COMM_SP = "net.xtapi.serviceProvider.Serial";
 	
 	// The address prefix
 	private final static String ADDR_PREFIX = "addr_";
 	
 	// The XTAPI spervice provider
 	private IXTapi realProvider = null;
 	
 	// The GJTAPI Listener I delegate callbacks to
 	private TelephonyListener gjListener = null;
 	
 	// The number of lines my real provider manages
 	private int numLines = 0;
 	
 	// Map of address name to AddressInfo for XTAPI
 	private Map addInfoMap = new HashMap();
 	
 	// Map of Terminal name to AddressInfo
 	private Map termToAddr = new HashMap();
 	
 	// Map of line name (new Integer(rawHandle)) to AddressInfo
 	private Map lineToAddr = new HashMap();
 	
 	/**
 	 * Map of terminals to CallHandles
 	 * This is used by the media methods to map media terminal to calls
 	 */
 	private Map termToCalls = new HashMap();
 	
 	/**
 	 * Map of line ids to CallHandles
 	 */
 	private Map lineToCalls = new HashMap();
 	
 	/**
 	 * Raw constructor used by the GenericJtapiPeer factory
 	 * Creation date: (2002-04-12 10:28:55)
 	 * @author: Richard Deadman
 	 */
 	public XtapiProvider() {
 		super();
 	}
 
 	/**
 	 * @see BasicJtapiTpi#getAddresses()
 	 */
 	public String[] getAddresses() throws ResourceUnavailableException {
 		if (this.numLines > 0) {
 			String addresses[] = new String[numLines];
 			for (int i = 0; i < numLines; i++) {
 				addresses[numLines] = ADDR_PREFIX + i;
 			}
 			return addresses;
 		} else
 			return null;
 	}
 
 	/**
 	 * @see BasicJtapiTpi#getAddresses(String)
 	 */
 	public String[] getAddresses(String terminal) throws InvalidArgumentException {
 		if (terminal != null) {
 			AddressInfo addInfo = (AddressInfo)this.termToAddr.get(terminal);
 			if (addInfo == null)
 				return null;
 			String result[] = { addInfo.getName() };
 			return result;
 		} else
 			return null;
 	}
 
 	/**
 	 * @see BasicJtapiTpi#getTerminals()
 	 */
 	public TermData[] getTerminals() throws ResourceUnavailableException {
 		int size = this.termToAddr.size();
 		if (size > 0) {
 			TermData terminals[] = new TermData[size];
 			Iterator it = this.termToAddr.keySet().iterator();
 			int i = 0;
 			while (it.hasNext()) {
 				String termName = (String)it.next();
 					// all terminals support media
 				terminals[i] = new TermData(termName, true);
 				i++;
 			}
 			return terminals;
 		} else
 			return null;
 	}
 
 	/**
 	 * @see BasicJtapiTpi#getTerminals(String)
 	 */
 	public TermData[] getTerminals(String address)
 		throws InvalidArgumentException {
 		if (address != null) {
 			AddressInfo addInfo = (AddressInfo)addInfoMap.get(address);
 			if (addInfo == null)
 				return null;
 			TermData result[] = { new TermData(addInfo.terminal, true) };
 			return result;
 		} else
 			return null;
 	}
 
 	/**
 	 * @see CoreTpi#addListener(TelephonyListener)
 	 */
 	public void addListener(TelephonyListener ro) {
 		this.gjListener = ro;
 	}
 
 	/**
 	 * @see CoreTpi#answerCall(CallId, String, String)
 	 */
 	public void answerCall(CallId call, String address, String terminal)
 		throws
 			PrivilegeViolationException,
 			ResourceUnavailableException,
 			MethodNotSupportedException,
 			RawStateException {
 		this.realProvider.XTAnswerCall(((XtapiCallId)call).getCallNum());
 	}
 
 	/**
 	 * @see CoreTpi#createCall(CallId, String, String, String)
 	 */
 	public CallId createCall(CallId id, String address, String term, String dest)
 		throws
 			ResourceUnavailableException,
 			PrivilegeViolationException,
 			InvalidPartyException,
 			InvalidArgumentException,
 			RawStateException,
 			MethodNotSupportedException {
 		// translate the address into a line
 		AddressInfo addInfo = (AddressInfo)this.addInfoMap.get(address);
 		if (addInfo == null)
 			throw new InvalidArgumentException("Address not known: " + address);
 			
 		// Get the call handle
 		int callNum = this.realProvider.XTConnectCall(addInfo.line,
 											dest,
 											addInfo.rawHandle);
 		// update the call structure
 		((XtapiCallId)id).setCallNumber(callNum);
 		
 		// register the call with the terminal
 		this.termToCalls.put(term, id);
 		
 		// register the call with the line
 		this.lineToCalls.put(new Integer(addInfo.line), id);
 		
 		return id;
 	}
 
 	/**
 	 * I support the default capabilities associated with the MediaTpi
 	 * @see CoreTpi#getCapabilities()
 	 */
 	public Properties getCapabilities() {
 		return null;
 	}
 
 	/**
 	 * Initialize the Xtapi Bricdge Provider with a Map that defines which xTapi service provider to load beneath me.
 	 * This consists of one known property:
 	 * <ul>
 	 *  <li><B>XtapiSp</B> The name of the IXTapi implementation class, or a known alias.
 	 * </ul>
 	 * @see CoreTpi#initialize(Map)
 	 */
 	public void initialize(Map props) throws ProviderUnavailableException {
 		
 		// see if the propery is set
 		String xtapiProvName = TAPI_SP;
 		Object value = props.get(IXTAPI_KEY);
 		if (value instanceof String && value != null) {
 			xtapiProvName = (String)value;
 		}
 		
 		// now check if we should use an alias
 		if (xtapiProvName.toLowerCase().equals("tapi"))
 			xtapiProvName = TAPI_SP;
 		else if (xtapiProvName.toLowerCase().equals("serial"))
 			xtapiProvName = COMM_SP;
 		
 		// try to instantiate xtapi provider
 		try {
 			realProvider = (IXTapi)Class.forName(xtapiProvName).newInstance();
 		} catch (Exception ex) {
 			throw new ProviderUnavailableException(ex.getMessage());
 		}
 		
 		// Now hook it up and get the number of lines
 		numLines = realProvider.XTinit(this);
 		
 		// populate the Address and Terminal information
 		for (int i = 0; i < numLines; i++) {
 			// Get Address information for each line
 			StringBuffer termName = new StringBuffer();
 			int handle = realProvider.XTOpenLine(i, termName);
 			
 			// populate the Address and Terminal information
 			String term = termName.toString();
 			AddressInfo addInfo = new AddressInfo(i, term, handle);
 			this.addInfoMap.put(ADDR_PREFIX + i, addInfo);
 			
 			this.termToAddr.put(term, addInfo);
 			
 			this.lineToAddr.put(new Integer(handle), addInfo);
 		}
 	}
 
 	/**
 	 * @see CoreTpi#releaseCallId(CallId)
 	 */
 	public void releaseCallId(CallId id) {
 		this.realProvider.XTDropCall(((XtapiCallId)id).getCallNum());
 	}
 
 	/**
 	 * I don't really need to do this, but it is here for completeness
 	 * @see CoreTpi#removeListener(TelephonyListener)
 	 */
 	public void removeListener(TelephonyListener ro) {
 		if (this.gjListener.equals(ro))
 			this.gjListener = null;
 	}
 
 	/**
 	 * Get a CallId object for later connection.
 	 * The address parameter is ignoted.
 	 * @see CoreTpi#reserveCallId(String)
 	 */
 	public CallId reserveCallId(String address) throws InvalidArgumentException {
 		return new XtapiCallId();
 	}
 
 	/**
 	 * @see CoreTpi#shutdown()
 	 */
 	public void shutdown() {
 		this.realProvider.XTShutdown();
 	}
 
 	// Media calls
 	/**
 	 * Allocate media resources.
 	 * This is a NO-OP, since XTAPI doesn't need this.
 	 * @see MediaTpi#allocateMedia(String, int, Dictionary)
 	 */
 	public boolean allocateMedia(
 		String terminal,
 		int type,
 		Dictionary resourceArgs) {
 		return true;
 	}
  	/**
 	 * Free media resources.
 	 * This is a NO-OP, since XTAPI doesn't need this.
 	 * @see MediaTpi#freeMedia(String, int)
 	 */
 	public boolean freeMedia(String terminal, int type) {
 		return false;
 	}
  	/**
 	 * All known terminals support media
 	 * @see MediaTpi#isMediaTerminal(String)
 	 */
 	public boolean isMediaTerminal(String terminal) {
 		if (this.termToAddr.containsKey(terminal))
 			return true;
 		return false;
 	}
  	/**
 	 * @see MediaTpi#play(String, String[], int, RTC[], Dictionary)
 	 */
 	public void play(
 		String terminal,
 		String[] streamIds,
 		int offset,
 		RTC[] rtcs,
 		Dictionary optArgs)
 		throws MediaResourceException {
 			// look up the id for the terminal
 			int id = this.getLineId(terminal);
 			int size = streamIds.length;
 			
 			// play each id on the found line
 			for (int i = 0; i < size; i++)
 				this.realProvider.XTPlaySound(streamIds[i], id);
 	}
  	/**
 	 * @see MediaTpi#record(String, String, RTC[], Dictionary)
 	 */
 	public void record(
 		String terminal,
 		String streamId,
 		RTC[] rtcs,
 		Dictionary optArgs)
 		throws MediaResourceException {
 			// look up the id for the terminal
 			int id = this.getLineId(terminal);
 			this.realProvider.XTRecordSound(streamId, id);
 	}
  	/**
 	 * @see MediaTpi#retrieveSignals(String, int, Symbol[], RTC[], Dictionary)
 	 */
 	public RawSigDetectEvent retrieveSignals(
 		String terminal,
 		int num,
 		Symbol[] patterns,
 		RTC[] rtcs,
 		Dictionary optArgs)
 		throws MediaResourceException {
 			// look up the id for the terminal
 			int id = this.getCallId(terminal);
 			this.realProvider.XTMonitorDigits(id, true);
 			// wait for signals to come inXXXX
 			return new RawSigDetectEvent();
 	}
  	/**
 	 * This should wait until the signals have all been sent.
 	 * @see MediaTpi#sendSignals(String, Symbol[], RTC[], Dictionary)
 	 */
 	public void sendSignals(
 		String terminal,
 		Symbol[] syms,
 		RTC[] rtcs,
 		Dictionary optArgs)
 		throws MediaResourceException {
 			// look up the id for the terminal
 			int id = this.getCallId(terminal);
 			this.realProvider.XTSendDigits(id, SymbolConvertor.convert(syms));			
 	}
  	/**
 	 * @see MediaTpi#stop(String)
 	 */
 	public void stop(String terminal) {
 		// look up the id for the terminal
 		int id = this.getLineId(terminal);
 		this.realProvider.XTStopPlaying(id);
 		this.realProvider.XTStopRecording(id);
 		
 		int callId = this.getCallId(terminal);
 		this.realProvider.XTMonitorDigits(callId, false);
 	}
  	/**
 	 * RTCs are not supported by XTAPI, but we do support stopping
 	 * @see MediaTpi#triggerRTC(String, Symbol)
 	 */
 	public void triggerRTC(String terminal, Symbol action) {
 		// look up the id for the terminal
 		int id = this.getCallId(terminal);
 		
 		if (action.equals(PlayerConstants.rtca_Stop))
 			this.realProvider.XTStopPlaying(id);
 			
 		if (action.equals(RecorderConstants.rtca_Stop))
 			this.realProvider.XTStopRecording(id);
 			
 		if (action.equals(SignalDetectorConstants.rtca_Stop)) {
 			int callId = this.getCallId(terminal);
 			this.realProvider.XTMonitorDigits(callId, false);
 		}
 	}
 
 	// Callback interface
 	/**
 	 * Receive the XTAPI service provider callback events and transform them
 	 * into JTAPI TelephonyListener events.
 	 * The delegation method also stores transformation information that is needed
 	 * for other delegation methods, such as media action termination.
 	 * @see IXTapiCallBack#callback(int, int, int, int, int, int)
 	 */
 	public void callback(
 		int dwDevice,
 		int dwMessage,
 		int dwInstance,
 		int dwParam1,
 		int dwParam2,
 		int dwParam3) {
         try {
             
         switch (dwMessage) {
             case LINE_ADDRESSSTATE:
                 break;
                 
             case LINE_CALLINFO:
                //this.realProvider.XTGetCallInfo(dwDevice);
                 break;
                 
             case LINE_CALLSTATE:
                 lineCallState(dwDevice,dwInstance,dwParam1,dwParam2,dwParam3);
                 break;
                 
             case LINE_CLOSE:
                 break;
                 
             case LINE_DEVSPECIFIC:
                 break;
                 
             case LINE_DEVSPECIFICFEATURE:
                 break;
                 
             case LINE_GATHERDIGITS:
                 break;
                 
             case LINE_MONITORDIGITS:
 /*                
                 debugString(5,"dwDevice -> " + dwDevice + " dwInstance -> " + dwInstance +
                 " dwParam1 -> "  + dwParam1 + " dwParam2 -> " + dwParam2 + 
                 " dwParam3 -> " + dwParam3);
 */                
 				AddressInfo ai = (AddressInfo)this.lineToAddr.get(new Integer(dwInstance));
 				if (ai != null) {
 					String terminal = ai.terminal;
 	                char[] detectedChars = { (char)dwParam1 };
                 	Symbol[] syms = SymbolConvertor.convert(new String(detectedChars));
                 	this.gjListener.mediaSignalDetectorDetected(terminal, syms);
 				}
                 break;                
                 
             case LINE_LINEDEVSTATE:
                 lineDevState(dwDevice,dwInstance,dwParam1,dwParam2,dwParam3);
                 break;
                 
             case LINE_MONITORMEDIA:
                 break;
                 
             case LINE_MONITORTONE:
                 break;
                 
             case LINE_REPLY:
                 //m_reply = dwParam2;
                 //m_device = dwInstance;
                 /*
                 TODO:   Check LINE_REPLY Data for success
                         Switch on current state, maybe connecting or
                         disconnecting.
                  */
 /*
                 evt = new XCallActiveEv(m_Call,CallActiveEv.ID,
                     Ev.CAUSE_NEW_CALL,0,false);
                 evlist[0] = evt;
                 publishEvent(evlist);
  */
                 break;
                 
             case LINE_REQUEST:
                 break;
                 
             case PHONE_BUTTON:
                 break;
                 
             case PHONE_CLOSE:
                 break;
                 
             case PHONE_DEVSPECIFIC:
                 break;
                 
             case PHONE_REPLY:
                 break;
                 
             case PHONE_STATE:
                 break;
                 
             case LINE_CREATE:
                 break;
                 
             case PHONE_CREATE:
                 break;
                 
             default:
                 System.out.println("UNKNOWN TAPI EVENT: " + dwMessage);
                 break;
         }
         }catch(Exception e){
             System.out.println("Exception " + e.toString() + " in callback()");
         }
 
 	}
 
     //callback recieved a LINE_CALLSTATE event:
     private void lineCallState(int dwDevice,int dwInstance,
            int dwParam1,int dwParam2,int dwParam3)
     {
     	Integer lineKey = new Integer(dwInstance);
         switch (dwParam1)
         {
             case LINECALLSTATE_IDLE:
                 try{
                 	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
 	                if (callId != null) {
 	                	AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
 	                	if (ai != null) {
 	                		this.gjListener.connectionDisconnected(callId, ai.getName(), Event.CAUSE_NORMAL);
 	                	}
 	                }
                 }catch(Exception e){
                     System.out.println("LINECALLSTATE_IDLE exception: " + e.toString());
                 }
                 
                 //Connection.DISCONNECTED
                 break;
 
             case LINECALLSTATE_OFFERING:
                 
 				try{
 					// create a call
 	                AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
 	                if (ai != null) {
 						XtapiCallId callId = new XtapiCallId();
 	                	callId.setCallNumber(dwInstance);
 	                	// get the calling connection and notify of new connection
 	                	this.gjListener.connectionConnected(callId, "UNKNOWN", Event.CAUSE_NORMAL);
 	                	this.gjListener.terminalConnectionRinging(callId, ai.getName(), ai.terminal, Event.CAUSE_NORMAL);
 	                }
 				}catch(Exception e){
 					System.out.println("Exception in LINECALLSTATE_OFFERING: " +
 						e.toString());
 				}
                 break;
 
             case LINECALLSTATE_ACCEPTED:
                 break;
 
             case LINECALLSTATE_DIALTONE:
                 break;
 
             case LINECALLSTATE_DIALING:
                 break;
 
             case LINECALLSTATE_RINGBACK:
                 //event.add(new XEv(ConnAlertingEv.ID,Ev.CAUSE_NEW_CALL,                            0,false));                
                 break;
 
             case LINECALLSTATE_BUSY:
                 break;
 
             case LINECALLSTATE_SPECIALINFO:
                 break;
 
             case LINECALLSTATE_CONNECTED:
                 try{
                 	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
 	                if (callId != null) {
 	                	AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
 	                	if (ai != null) {
 	                		this.gjListener.connectionConnected(callId, ai.getName(), Event.CAUSE_NORMAL);
 	                	}
 	                }
                 }catch(Exception e){
                     System.out.println("LINECALLSTATE_IDLE exception: " + e.toString());
                 }
                 break;
 
             case LINECALLSTATE_PROCEEDING:
                 // Outgoing call invokes two JTAPI Connection events.
                 
                 try{
                 	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
 	                if (callId != null) {
 	                	AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
 	                	if (ai != null) {
 	                		this.gjListener.connectionInProgress(callId, ai.getName(), Event.CAUSE_NORMAL);
 	                	}
 	                }
                 }catch(Exception e){
                     System.out.println("LINECALLSTATE_IDLE exception: " + e.toString());
                 }
                 break;
 
             case LINECALLSTATE_ONHOLD:
                 break;
 
             case LINECALLSTATE_CONFERENCED:
                 break;
 
             case LINECALLSTATE_ONHOLDPENDCONF:
                 break;
 
             case LINECALLSTATE_ONHOLDPENDTRANSFER:
                 break;
 
             case LINECALLSTATE_DISCONNECTED:
                 try{
                 	XtapiCallId callId = (XtapiCallId)this.lineToCalls.get(lineKey);
 	                if (callId != null) {
 	                	AddressInfo ai = (AddressInfo)this.lineToAddr.get(lineKey);
 	                	if (ai != null) {
 	                		this.gjListener.connectionDisconnected(callId, ai.getName(), Event.CAUSE_NORMAL);
 	                	}
 	                }
                 }catch(Exception e){
                     System.out.println("LINECALLSTATE_IDLE exception: " + e.toString());
                 }
                 break;
 
             case LINECALLSTATE_UNKNOWN:
                 break;
                 
             default:
                 break; 
         }
     }
 
     //callback recieved a LINE_DEVSTATE event:
     private void lineDevState(int dwDevice,int dwInstance,
            int dwParam1,int dwParam2,int dwParam3)
     {
         switch(dwParam1)
         {
             case LINEDEVSTATE_OTHER:
                 break;
                 
             case LINEDEVSTATE_RINGING:
                //dwParam3 contains the ring count.
                 XtapiCallId xCallId = (XtapiCallId)this.lineToCalls.get(new Integer(dwInstance));
                 if (xCallId != null) {
                 	AddressInfo ai = (AddressInfo)this.lineToAddr.get(new Integer(dwInstance));
                 	if (ai != null) {
                 		this.gjListener.connectionAlerting(xCallId, ai.getName(), Event.CAUSE_NEW_CALL);
                 	}
                 }
                 break;
                 
             case LINEDEVSTATE_CONNECTED:
                 break;
                 
             case LINEDEVSTATE_DISCONNECTED:
                 break;
                 
             case LINEDEVSTATE_MSGWAITON:
                 break;
                 
             case LINEDEVSTATE_MSGWAITOFF:
                 break;
                 
             case LINEDEVSTATE_INSERVICE:
                 break;
                 
             case LINEDEVSTATE_OUTOFSERVICE:
                 break;
                 
             case LINEDEVSTATE_MAINTENANCE:
                 break;
                 
             case LINEDEVSTATE_OPEN:
                 break;
                 
             case LINEDEVSTATE_CLOSE:
                 break;
                 
             case LINEDEVSTATE_NUMCALLS:
                 break;
                 
             case LINEDEVSTATE_NUMCOMPLETIONS:
                 break;
                 
             case LINEDEVSTATE_TERMINALS:
                 break;
 
             case LINEDEVSTATE_ROAMMODE:
                 break;
 
             case LINEDEVSTATE_BATTERY:
                 break;
 
             case LINEDEVSTATE_SIGNAL:
                 break;
 
             case LINEDEVSTATE_DEVSPECIFIC:
                 break;
 
             case LINEDEVSTATE_REINIT:
                 break;
 
             case LINEDEVSTATE_LOCK:
                 break;
 
             case LINEDEVSTATE_CAPSCHANGE:
                 break;
 
             case LINEDEVSTATE_CONFIGCHANGE:
                 break;
 
             case LINEDEVSTATE_TRANSLATECHANGE:
                 break;
 
             case LINEDEVSTATE_COMPLCANCEL:
                 break;
 
             case LINEDEVSTATE_REMOVED:
                 break;
 
             default:
                 break; 
         }
     }
 	// end of interface implementation
 	
 	/**
 	 * Helper function for getting a call id from a terminal name
 	 */
 	private int getCallId(String terminal) {
 		return ((XtapiCallId)this.termToCalls.get(terminal)).getCallNum();
 	}
 	
 	/**
 	 * Helper function for getting a line id from a terminal name
 	 */
 	private int getLineId(String terminal) {
 		return ((AddressInfo)this.termToAddr.get(terminal)).rawHandle;
 	}
 	
 	/**
 	 * Describe myself
 	 */
 	public String toString() {
 		return "GJTAPI bridge to XTAPI service provider: " + realProvider.toString();
 	}
 }
 
