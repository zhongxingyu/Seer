 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as
  * published by the Free Software Foundation and distributed hereunder
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this
  * code.
  */
 
 package com.sun.mpk20.voicelib.impl.service.voice;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Enumeration;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.text.ParseException;
 
 import com.sun.voip.CallParticipant;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 public class BridgeManager extends Thread 
 	implements BridgeOfflineListener, CallStatusListener {
 
     private static final Logger logger = Logger.getLogger(
 	    BridgeManager.class.getName());
 
    private static final int BRIDGE_ONLINE_WATCHER_PORT = 6668;
 
    private int bridgeOnlineWatcherPort = BRIDGE_ONLINE_WATCHER_PORT;
 
     private ArrayList<BridgeConnection> bridgeConnections =
         new ArrayList<BridgeConnection>();
 
     /*
      * Map of callId's to callInfo
      */
     private ConcurrentHashMap<String, CallInfo> callConnectionMap =
         new ConcurrentHashMap<String, CallInfo>();
 
     private VoiceServiceImpl voiceService;
 
     private PrivateMixManager privateMixManager;
 
     private Reconnector reconnector;
   
     public BridgeManager(VoiceServiceImpl voiceService) {
 	this.voiceService = voiceService;
 
 	reconnector = new Reconnector(this);
 
 	String s = System.getProperty(
	    "com.sun.voip.server.BRIDGE_ONLINE_WATCHER_PORT");
 
 	if (s != null) {
 	    try {
		bridgeOnlineWatcherPort = Integer.parseInt(s);
 	    } catch (NumberFormatException e) {
 		logger.info("Invalid bridge online watcher port: " + s
		    + ".  Defaulting to " + bridgeOnlineWatcherPort);
 	    }
 	}
 
 	privateMixManager = new PrivateMixManager(this);
 
 	start();
     }
 
     public void configure(Properties properties) {
     }
 
     /*
      * The bridge address is composed of Strings separated
      * by ":" as follows:
      *
      * <bridge private address>:
      * <bridge private control port>:
      * <bridge private sip port>:
      * <bridge public address>:
      * <bridge public control port>:
      * <bridge public sip port>
      */
     public void connect(String bridgeServer) throws IOException {
 	BridgeConnection bc = findBridge(bridgeServer);
 
 	if (bc != null) {
 	    /*
 	     * This is a voice bridge we already knew about.
 	     */
 	    if (bc.isConnected()) {
 		/*
 		 * It's already connected.  Tell the bridge connector
 		 * we got a ping from the bridge.
 		 */
 		logger.finest("got ping from " + bc);
 		bc.gotBridgePing();
 		return;
 	    }
 
 	    /*
 	     * This happens if the watchdog isn't running.
 	     */
 	    bridgeOffline(bc, bc.getCallParticipantArray());
 
 	    logger.info("Disconnected bridge is now back online: " 
 		+ bridgeServer);
 	} else {
 	    logger.info("Bridge " + (bridgeConnections.size() + 1) 
 	        + " came online:  '" + bridgeServer + "'");
 	}
 
 	String[] tokens = bridgeServer.split(":");
 	
 	if (tokens.length != 6) {
 	    throw new IOException("Invalid bridge server:  " + bridgeServer);
 	}
 
 	String privateHost = tokens[0];
 
 	int privateControlPort = 6666;
 
 	try {
 	    privateControlPort = Integer.parseInt(tokens[1]);
 	} catch (NumberFormatException e) {
 	    logger.info("Invalid private control port " + tokens[1]
 		+ ".  Defaulting to " + privateControlPort);
 	}
 
 	int privateSipPort = 5060;
 
 	try {
 	    privateSipPort = Integer.parseInt(tokens[2]);
 	} catch (NumberFormatException e) {
 	    logger.info("Invalid private sip port " + tokens[2]
 		+ ".  Defaulting to " + privateSipPort);
 	}
 
 	String publicHost = tokens[3];
 
 	int publicControlPort = privateControlPort;
 
 	try {
 	    publicControlPort = Integer.parseInt(tokens[4]);
 	} catch (NumberFormatException e) {
 	    logger.info("Invalid public control port " + tokens[4]
 		+ ".  Defaulting to " + publicControlPort);
 	}
 
 	int publicSipPort = privateSipPort;
 
 	try {
 	    publicSipPort = Integer.parseInt(tokens[5]);
 	} catch (NumberFormatException e) {
 	    logger.info("Invalid public SIP port " + tokens[5]
 		+ ".  Defaulting to " + publicSipPort);
 	}
 
 	try {
 	    /*
 	     * This connection is used to send commands to the bridge
 	     * and get status
 	     */
 	    bc = new BridgeConnection(privateHost, privateControlPort,
 		privateSipPort, publicHost, publicControlPort, publicSipPort, 
 		true);
 
 	    bc.addBridgeOfflineListener(this);
 	    bc.addCallStatusListener(this);
 	    bc.monitorIncomingCalls(true);
 
 	    synchronized (bridgeConnections) {
 		for (BridgeConnection bridgeConnection : bridgeConnections) {
 		    bc.monitorConferences(bridgeConnection.getConferences());
 		}
 	    }
 	} catch (IOException e) {
 	    throw new IOException("Unable to connect to bridge "
 		+ privateHost + ":" +  privateControlPort + ":"
 		+ publicHost + ":" + publicSipPort + " " + e.getMessage());
 	}
 
 	synchronized (bridgeConnections) {
 	    if (bc.isConnected() == false) {
 		logger.info("New bridge " + bc + " is no longer connected!");
 		return; 
 	    }
 
 	    bridgeConnections.add(bc);
 
 	    reconnector.bridgeOnline();
 
 	    bridgeConnections.notifyAll();
 	}
     }
 
     public void bridgeOffline(BridgeConnection bc, 
 	    ArrayList<CallParticipant> calls) {
 
         synchronized (bridgeConnections) {
             logger.info("Removing bridge connection for offline bridge " + bc);
 
             if (bridgeConnections.contains(bc) == false) {
 		/*
 		 * It has already been handled.
 		 */
 		return;
 	    }
 
 	    bridgeConnections.remove(bc);
 	}
 
 	bc.disconnect();
 
 	/*
 	 * Notify other bridges that a bridge went down
 	 */
 	synchronized (bridgeConnections) {
             for (BridgeConnection bridgeConnection: bridgeConnections) {
 		bridgeConnection.bridgeOffline(bc);
 	    }
 	}
 
 	for (int i = 0; i < calls.size(); i++) {
 	    CallParticipant cp = calls.get(i);
 
 	    String callId = cp.getCallId();
 
 	    try {
 	        endCall(bc, callId);
 	    } catch (IOException e) {
 		logger.info("Unable to end call " + callId);
 	    }
 	}
 
 	reconnector.bridgeOffline(bc, calls);
     }
 
     public void transferCall(String callId, String conferenceId) 
 	    throws IOException {
 
 	CallInfo callInfo = callConnectionMap.get(callId);
 
 	if (callInfo == null) {
 	    logger.warning("No CallInfo for " + callId);
 	    return;
 	}
 
 	callInfo.bridgeConnection.transferCall(callId, conferenceId);
     }
 
     public CallInfo getCallConnection(String callId) {
 	return callConnectionMap.get(callId);
     }
 
     public void putCallConnection(CallStatus status) {
 	CallParticipant cp = new CallParticipant();
 
 	String callId = status.getCallId();
 
 	String info = status.getCallInfo();
 
 	if (info == null) {
 	    info = "Anonymous";
 	}
 
 	String[] tokens = info.split("@");
 
 	if (info.startsWith("sip:")) {
 	    cp.setName("Anonymous");
 	    cp.setPhoneNumber(tokens[2]);
 	} else {
 	    cp.setName(tokens[0]);
 	    cp.setPhoneNumber(tokens[1]);
 	}
 
 	cp.setCallId(callId);
 	cp.setConferenceId(status.getOption("ConferenceId"));
 
 	String bridgeInfo = status.getOption("BridgeInfo");
 
 	BridgeConnection bc = findBridge(bridgeInfo);
 	
 	putCallConnection(callId, new CallInfo(cp, bc));
     }
 
     public void putCallConnection(String callId, CallInfo callInfo) {
 	callConnectionMap.put(callId, callInfo);
     }
 
     public void removeCallConnection(String callId) {
 	callConnectionMap.remove(callId);
     }
 
     public void initiateCall(CallParticipant cp) throws IOException,
 	    ParseException {
 
 	initiateCall(cp, null);
     }
 
     /*
      * Initiate a call.  bridgeInfo must be what was returned
      * from getVoiceBridge().  This is <bridge public address>:<public sip port>
      */
     public void initiateCall(CallParticipant cp, String bridgeInfo) 
 	    throws IOException, ParseException {
 
 	String callId = cp.getCallId();
 
 	BridgeConnection bc = null;
 
 	if (bridgeInfo == null) {
 	    try {
 	        bc = getBridgeConnection();
 	    } catch (IOException e) {
 	        throw new IOException(
 		    "No voice bridge available " + cp + " " + e.getMessage());
 	    }
 	} else {
 	    String tokens[] = bridgeInfo.split(":");
 
 	    if (tokens.length != 2) {
 		throw new IOException("Invalid bridge info: " + bridgeInfo);
 	    }
 
 	    bc = findBridge(tokens[0], tokens[1]);
 
 	    if (bc == null) {
 		throw new IOException("Unable to find bridge for '"
 		    + bridgeInfo + "'");
 	    }
 	}
 
 	/*
 	 * Make sure a call with the same id is ended before starting a new call.
 	 */
 	try {
     	    endCall(bc, callId);
 	} catch (IOException e) {
 	    logger.fine("Unable to end call " + callId);
 	}
 
 	logger.info("Setting up call " + cp + " on " + bc);
 
 	if (bc.isConnected() == false) {
 	    /*
 	     * mark bridge offline, so we stop using it
 	     */
 	    bridgeOffline(bc, bc.getCallParticipantArray());	
 
 	    throw new IOException("Unable to setup call on disconnect bridge" 
 		+ bc);
 	}
 
 	/*
 	 * We have to put this in the map now because we
 	 * could get call status before setupCall() returns.
 	 */
 	putCallConnection(callId, new CallInfo(cp, bc));
 
 	try {
 	    bc.setupCall(cp);
 	} catch (IOException e) {
 	    removeCallConnection(callId);
 
 	    /*
 	     * There needs to be a better way to do this!
 	     * XXX We can't do this because when a bridge
 	     * goes down we get an IOException because the
 	     * socket got closed.  In that case we want to restart
 	     * the call.
 	     */
 	    if (false && e.getMessage().indexOf("CallId " + callId
 		    + " is already in use") < 0) {
 
 		throw new ParseException(
 		    "Invalid call setup Parameters", 0);
 	    }
 
 	    if (e.getMessage().indexOf("CallId " + callId
 		    + " is already in use") < 0) {
 
 		/*
 		 * It's not a duplicate callId so the bridge is most likely dead.
 		 * XXX Not necesesarily!  For example, a bad treatment...
 		 */
 		//logger.info("Marking bridge offline " + bc 
 		//    + " isConnected=" + bc.isConnected());
 
 		//bridgeOffline(bc); // mark bridge offline so we stop using it
 	    }
 
 	    throw new IOException("Failed to setup call " + cp + " " 
 		+ e.getMessage());
 	}
     }
 
     public void playTreatmentToCall(String callId, String treatment) 
 	    throws IOException {
 
 	BridgeConnection bc = getBridgeConnection(callId);
 
 	bc.playTreatmentToCall(callId, treatment);
     }
 
     public void pauseTreatmentToCall(String callId, String treatment)
 	     throws IOException {
 
 	BridgeConnection bc = getBridgeConnection(callId);
 
 	bc.pauseTreatmentToCall(callId, treatment);
     }
 
     public void stopTreatmentToCall(String callId, String treatment)
 	     throws IOException {
 
 	BridgeConnection bc = getBridgeConnection(callId);
 
 	bc.stopTreatmentToCall(callId, treatment);
     }
 
     public void migrateCall(CallParticipant cp) throws IOException {
 	BridgeConnection bc = getBridgeConnection(cp.getCallId(), true);
 
 	putCallConnection(cp.getCallId(), new CallInfo(cp, bc));
 
 	bc.migrateCall(cp);
     }
 
     public void endCall(String callId) throws IOException {
 	BridgeConnection bc = getBridgeConnection(callId);
 
 	endCall(bc, callId);
     }
 
     public void endCall(BridgeConnection bc, String callId) throws IOException {
 	if (bc != null) {
 	    try {
                 bc.endCall(callId);
             } catch (IOException e) {
                 logger.info("Unable to end call:  " + e.getMessage());
             }
 	}
 
 	removeCallConnection(callId);
 
 	privateMixManager.endCall(callId);
     }
 
     public void muteCall(String callId, boolean isMuted) throws IOException {
         BridgeConnection bc = getBridgeConnection(callId);
  	bc.muteCall(callId, isMuted);
     }
 
     public void startRecording(String callId, String recordingFile) throws IOException {
         BridgeConnection bc = getBridgeConnection(callId);
  	bc.startRecordingToCall(callId, recordingFile);
     }
 
     public void stopRecording(String callId) throws IOException {
         BridgeConnection bc = getBridgeConnection(callId);
  	bc.stopRecordingToCall(callId);
     }
 
     public void newInputTreatment(String callId, String treatment) 
 	    throws IOException {
 
 	BridgeConnection bc = getBridgeConnection(callId, true);
 
 	bc.newInputTreatment(callId, treatment);
     }
 
     public void stopInputTreatment(String callId) throws IOException {
 	BridgeConnection bc = getBridgeConnection(
             callId);
 
 	bc.stopInputTreatment(callId);
     }
 
     public void restartInputTreatment(String callId) throws IOException {
 	BridgeConnection bc = getBridgeConnection(
             callId);
 
 	bc.restartInputTreatment(callId);
     }
 
     public void setPrivateMix(Work work) {
 	privateMixManager.setPrivateMix(work);
     }
 
     public void commit() {
 	privateMixManager.commit();
     }
 
     public void addToRecoveryList(String callId, CallParticipant cp) {
 	reconnector.addToRecoveryList(callId, cp);
     }
 
     public void dump() {
 	dumpBridgeConnections();
 	dumpCallConnections();
 	reconnector.dump();
 	privateMixManager.dump();
     }
 
     public void dumpBridgeConnections() {
 	logger.info("Bridge connections " + bridgeConnections.size());
 
 	for (BridgeConnection bc : bridgeConnections) {
             dumpBridgeConnection(bc);
         }
     }
 
     private void dumpCallConnections() {
 	Enumeration<String> keys = callConnectionMap.keys();
 
 	logger.info("Call Connection Map " + callConnectionMap.size());
 
 	while (keys.hasMoreElements()) {
             String callId = keys.nextElement();
 
 	    CallInfo callInfo = callConnectionMap.get(callId);
 
 	    if (callInfo == null) {
 		logger.info("callInfo is null for " + callId + "!");
 	    } else {
 	        logger.info("  " + callInfo);
 	    }
 	}
     }
 
     private void dumpBridgeConnection(BridgeConnection bc) {
         ArrayList<CallParticipant> cpArray = bc.getCallParticipantArray();
 
         logger.info("  " + bc.toString()
             + (bc.isConnected() ? " Connected " : " NOT Connected ")
             + cpArray.size());
 
         for (int i = 0; i < cpArray.size(); i++) {
             logger.info("    " + cpArray.get(i));
         }
     }
 
     /*
      * Find the bridge for a new call.
      */
     public BridgeConnection findBridge(String publicHost, String publicSipAddress) {
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 		if (bc.equals(publicHost, publicSipAddress)) {
 		    return bc;
 		}
 	    }
 	}
 
 	return null;
     }
 
     public BridgeConnection findBridge(String bridgeInfo) {
 	String[] tokens = bridgeInfo.split(":");
 
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 		if (bc.toString().equals(bridgeInfo)) {
 		    return bc;
 		}
 	    }
 	}
 
 	return null;
     }
 
     public BridgeConnection getBridgeConnection() throws IOException {
 	BridgeConnection bridgeConnection = null;
 
 	synchronized (bridgeConnections) {
 	    if (bridgeConnections.size() == 0) {
 	        throw new IOException("There are no voice bridges available!");
 	    }
 
 	    for (BridgeConnection bc : bridgeConnections) {
 	        if (bc.isConnected() == false) {
 	            logger.warning("Skipping Voice bridge " + bc 
 		        + " which is DOWN!");
 
 		    continue;
 	        }
 
 	        if (bridgeConnection == null) {
 		    bridgeConnection = bc;
 	        } else {
 		    /*
 		     * Choose bridge with least number of calls
 		     */
 	            if (bc.getNumberOfCalls() < bridgeConnection.getNumberOfCalls()) {
 		        bridgeConnection = bc;
 	            }
 	    	}
 	    } 
 	}
 
 	if (bridgeConnection == null) {
 	    throw new IOException("There are no voice bridges available!");
 	}
 
 	return bridgeConnection;
     }
 
     public BridgeConnection getBridgeConnection(String callId) 
 	    throws IOException {
 
 	return getBridgeConnection(callId, false);
     }
 
     public BridgeConnection getBridgeConnection(String callId, boolean allocate) 
 	    throws IOException {
 
 	CallInfo callInfo = callConnectionMap.get(callId);
 
 	if (callInfo != null) {
 	    return callInfo.bridgeConnection;
 	}
 
 	if (allocate == false) {
 	    throw new IOException("Cannot find bridgeConnection for " + callId);
 	}
 
 	return getBridgeConnection();
     }
 
     public BridgeConnection waitForBridge() {
 	synchronized (bridgeConnections) {
 	    while (isBridgeAvailable() == false) {
 		logger.info("Waiting for a bridge to come online "
 		    + " to finish processing calls");
 
 		try {
 		    bridgeConnections.wait();
 		} catch (InterruptedException ex) {
 		}
 	    }
 
 	    return bridgeConnections.get(0);
 	}
     }
 
     private boolean isBridgeAvailable() {
 	if (bridgeConnections.size() == 0) {
 	    return false;
 	}
 
 	for (BridgeConnection bc : bridgeConnections) {
 	    if (bc.isConnected() == true) {
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     public void run() {
 	ServerSocket serverSocket;
 
 	try {
	    serverSocket = new ServerSocket(bridgeOnlineWatcherPort);
 	} catch (IOException e) {
 	    logger.info("Unable to create server socket:  " 
 		+ e.getMessage());
 	    return;
 	}
 
 	while (true) {
 	    Socket socket;
 
 	    try {
 		socket = serverSocket.accept(); // wait for a connection
 	    } catch (IOException e) {
 		logger.info("Unable to accept connection: " 
 		    + e.getMessage());
 		continue;
 	    }
 
 	    logger.fine("New connection accepted from " 
 	        + socket.getRemoteSocketAddress());
 
 	    try {
 		new BridgeOnlineReader(socket);
 	    } catch (IOException e) {
 		logger.info("Unable to start BridgeOnlineReader for "
 	            + socket.getRemoteSocketAddress() + e.getMessage());
 	    } 
 	}
     }
 
     private String conferenceId;
 
     public void  monitorConference(String conferenceId) throws IOException {
 	this.conferenceId = conferenceId;
 
         synchronized (bridgeConnections) {
             for (BridgeConnection bc : bridgeConnections) {
                 bc.monitorConference(conferenceId);
             }
         }
     }
 
     public String getVoiceBridge() {
 	BridgeConnection bc;
 
 	try {
 	    bc = getBridgeConnection();
 	} catch (IOException e) {
 	    return "";
 	}
 
 	return bc.getPublicAddress();
     }
 
     public void setSpatialAudio(boolean enabled) throws IOException {
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 	        bc.setSpatialAudio(enabled);
 	    }
 	}
     }
 
     public void setSpatialMinVolume(double spatialMinVolume) 
 	    throws IOException {
 
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 	        bc.setSpatialMinVolume(spatialMinVolume);
 	    }
 	}
     }
 
     public void setSpatialFalloff(double spatialFalloff) throws IOException {
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 	        bc.setSpatialFalloff(spatialFalloff);
 	    }
 	}
     }
 
     public void setSpatialEchoDelay(double spatialEchoDelay) throws IOException {
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 	        bc.setSpatialEchoDelay(spatialEchoDelay);
 	    }
 	}
     }
 
     public void setSpatialEchoVolume(double spatialEchoVolume) throws IOException {
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 	        bc.setSpatialEchoVolume(spatialEchoVolume);
 	    }
 	}
     }
 
     public void setSpatialBehindVolume(double spatialBehindVolume) 
 	    throws IOException {
 
 	synchronized (bridgeConnections) {
 	    for (BridgeConnection bc : bridgeConnections) {
 	        bc.setSpatialBehindVolume(spatialBehindVolume);
 	    }
 	}
     }
 
     public void callStatusChanged(CallStatus status) {
         logger.finest("Call status changed:  " + status + " " + this);
 
 	String callId = status.getCallId();
 
         if (status == null || callId == null) {
             return;
         }
 
         if (status.getCode() == CallStatus.ESTABLISHED) {
             privateMixManager.callEstablished(callId);
 	} else if (status.getCode() == CallStatus.ENDED) {
 	    privateMixManager.endCall(callId);
 	} else if (status.getCode() == CallStatus.BRIDGE_OFFLINE) {
 	    if (callId.length() == 0) {
 		/*
 		 * Let's see how things look now that we've
 		 * recovered from bridge failure.
 		 */
 		if (logger.isLoggable(Level.FINE)) {
 		    dump();
 		}
 	    }
 	}
 
 	voiceService.callStatusChanged(status);
     }
 
     class BridgeOnlineReader extends Thread {
 
         private Socket socket;
         private BufferedReader bufferedReader;
 
         public BridgeOnlineReader(Socket socket) throws IOException {
 	    this.socket = socket;
 
        	    bufferedReader = new BufferedReader(
 	        new InputStreamReader(socket.getInputStream()));
 
 	    start();
 	}
 
 	public void run() {
 	    InetSocketAddress isa = (InetSocketAddress) 
 	        socket.getRemoteSocketAddress();
 
 	    String bridgeAddress = null;
 
 	    try {
 	        while (true) {
 	            bridgeAddress = bufferedReader.readLine();
 
 		    if (bridgeAddress == null) {
 			logger.info("Bridge " + isa + " has disconnected");
 			break;
 		    }
 
 		    /*
 		     * The bridge address is composed of Strings separated
 		     * by ":" as follows:
 		     * <bridge private address>:
 		     * <bridge private control port>:
 		     * <bridge private sip port>:
 		     * <bridge public address>:
 		     * <bridge public control port>:
 		     * <bridge public sip port>
 		     */
 	            String s = "BridgeUP:";
 
 	            int ix = bridgeAddress.indexOf(s);
 
 	            if (ix >= 0) {
 		        bridgeAddress = bridgeAddress.substring(s.length());
 
 			/*
 			 * Don't we have to notify all the voice services?
 			 * XXX
 			 */
 			try {
 	                    connect(bridgeAddress);
 			} catch (IOException e) {
 			    /*
 			     * We weren't able to connect.  Got back and wait for
 			     * next online message.  The bridge might not yet
 			     * be ready for connections, even though it said it was up.
 			     */
 			    logger.info(e.getMessage());
 			}
 	            } else {
 		        logger.info("Unexpected data:  " + bridgeAddress);
 	            }
 	        }
 	    } catch (IOException e) {
 	        logger.info("Unable to read data from " + isa
 	    	    + " " + e.getMessage());		    
 	    }
 	}
     }
 
 }
