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
 
 package com.sun.voip.client.connector.impl;
 
 import com.sun.voip.CallParticipant;
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 import java.net.Socket;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Mediate connections between the client and the voice
  * bridge.
  */
public class VoiceBridgeConnection implements Runnable {
     /** bridge properties */
     private final String bridgeHost;
     private final int bridgePort;
     
     /** connection to bridge */
     private Socket socket;
     private OutputStream output;
     private BufferedReader bufferedReader;
 
     /** the listeners */
     private Set listeners;
     
     /** the logger */
     private static final Logger logger =
         Logger.getLogger(VoiceBridgeConnection.class.getName());
     
     
     public VoiceBridgeConnection(String bridgeHost, int bridgePort) {
         this.bridgeHost = bridgeHost;
         this.bridgePort = bridgePort;
     
         listeners = new HashSet();
     }
     
     /**
      * Connect to the voice bridge
      *
      * @return true if the connection succeeded
      */
     public synchronized void connect() throws IOException {
         if (socket != null && socket.isConnected()) {
             // already connected
             return;
         }
 
         logger.finest("Voice Bridge Connector connecting to " + 
                       bridgeHost + ":" + bridgePort);
 
         //
 	// Open a tcp connection to the remote host at the well-known port.
 	//
 
 	try {
 	    socket = new Socket(bridgeHost, bridgePort);
             output = socket.getOutputStream();
 
             bufferedReader = new BufferedReader(
 		new InputStreamReader(socket.getInputStream()));
 	} catch (IOException e) {
             throw new IOException("Can't connect to bridge " 
 		+ bridgeHost + ":" + bridgePort + " " + e.getMessage());
 	}
 
	new Thread(this).start();
     }
 
     /**
      * Return whether or not this bridge is connected
      *
      * @return true if the bridge is connected
      */
     public boolean isConnected() {
 	return isConnected(socket);
     }
 
     public boolean isConnected(Socket socket) {
 	if (socket == null) {
 	    return false;
 	}
 
 	if (socket.isClosed()) {
 	    return false;
 	}
 
         return socket.isConnected();
     }
     
     /**
      * Disconnect from the voice bridge
      */
     public void disconnect() {
 	logger.finest("Voice Bridge Connector disconnecting from " + 
                       bridgeHost + ":" + bridgePort);
         
 	if (socket == null) {
 	    return;
 	}
 
 	try {
 	    socket.close();
 	} catch (IOException e) {
 	}
 
 	socket = null;
     }
     
     /**
      * Add a listener that will be notified whenever the call status
      * changes
      *
      * @param listener the listener
      */
     public void addCallStatusListener(CallStatusListener listener) {
         synchronized (listeners) {
             listeners.add(listener);
         }
     }
     
     /**
      * Remove a status listener
      *
      * @param listener the listener
      */
     public void removeCallStatusListener(CallStatusListener listener) {
         synchronized (listeners) {
             listeners.remove(listener);
         }
     }
     
     /**
      * Monitor incoming calls
      */
     public void monitorIncomingCalls(boolean monitor) throws IOException {
         sendMessage("mic=" + monitor + "\n");
     }
 
     /**
      * Create a conference
      * @param conferenceId the conference id to create
      * @param name the display name of the conference to create
      * @param quality the audio quality to use
      */
     public void createConference(String conferenceId, String name, 
                                  String quality)
         throws IOException
     {
         sendMessage("cc=" + conferenceId + ":" + quality + 
                     ":" + name + "\n");
     }
    
     /**
      * Remove a conference
      * @param conferenceId the conferenceId to remove
      */
     public void removeConference(String conferenceId)
         throws IOException
     {
         sendMessage("rconf=" + conferenceId + "\n");
     }
     
     /**
      * Place a call to the specified call participant
      *
      * @param participant the participant to place the call to
      */
     public void placeCall(CallParticipant cp) throws IOException { 
         sendMessage(cp.getCallSetupRequest());
     }
 
     /**
      * Transfer a call to the specified conference
      *
      * @param participant the participant to transfer
      */
     public void transferCall(String callId, String conferenceId) 
         throws IOException 
     {
         // monitor status on the call
 	String s = "monitorCallStatus=true:" + callId + "\n";
 	sendMessage(s);
 
         // do the actual transfer
 	s = "transferCall=" + callId + ":" + conferenceId + "\n";
 	sendMessage(s);
     }
 
     /**
      * Migrate a call from one endpoint (phone) to another.
      *
      * @param participant the participant to transfer
      */
     public void migrateCall(CallParticipant cp) throws IOException {
 	sendMessage(cp.getCallSetupRequest());
 
 	// XXX What if the number is bad or no one answers or it goes to voicemail?
     }
 
     /**
      * Cancel the migration
      *
      * @param participant the participant to cancel the migration for
      */
     public void cancelMigration(String callId) throws IOException {
         String s = "cancelMigration=" + callId + "\n";
         sendMessage(s);
     }
     
     
     /**
      * Set a particular call to be muted
      *
      * @param callId the id of the call to mute
      * @param isMuted true to mute the call, or false to unmute
      */
     public void setMute(String callId, boolean isMuted) throws IOException {    
 	String s = "mute=" + isMuted + ":" + callId +"\n";
         sendMessage(s);
     }
 
     public void setDetectWhileMuted(String callId, boolean value) 
 	    throws IOException {
 
 	String show = "vm=" + true + ":" + callId + "\n";
 	sendMessage(show);
 	System.out.println("sent to bridge: "+ show);
     }
 	
     public void setMuteConference(String callId, boolean value) 
 	    throws IOException {
 
 	String muteConf = "mc=" + value + ":" + callId + "\n";
 	sendMessage(muteConf);
 	System.out.println("sent to bridge: " + muteConf);
     }
 	
     public void setConferenceSilenced(String callId, boolean value)
             throws IOException {
 
         String silenceConf = "smc=" + value + ":" + callId + "\n";
         sendMessage(silenceConf);
         System.out.println("sent to bridge: " + silenceConf);
     }
 
     /**
      * Set the volume factor for a call.  The volumeFactor is normally 1.0.
      * Setting the factor to less than 1.0 lowers the volume, greater than 1.0
      * increases the volume.
      *
      * @param callId the id of the call to set the volume factor for
      * @param volumeFactor float used to multiply the voice data thereby
      * increasing or decreasing the volume level.
      */
     public void setVolumeFactor(String callId, float volumeFactor) 
         throws IOException 
     {
 	String s = "volumeLevel=" + volumeFactor + ":" + callId +"\n";
         sendMessage(s);
     }
 
     /**
      * Set the stereo volumes for a call.All values are from 0.0 (mute) to 
      * 1.0 (normal) and higher to increase volume.  Normally volumes are 
      * [1.0, 0.0, 0.0, 1.0].  These volumes are heard by everyone in the
      * conference.
      *
      * @param callId the call to change the volume of
      * @param leftLeft the volume from the left input channel to the left 
      * output channel
      * @param leftRight the volume from the left input channel to the right
      * output channel
      * @param rightLeft the volume from the right input channel to the left
      * output channel
      * @param rightRight the volume from the right input channel to the right 
      * output channel
      */
     public void setStereoVolumes(String callId, float leftLeft, 
                                  float leftRight, float rightLeft, 
                                  float rightRight)
         throws IOException
     {
         String s = "iv=" + leftLeft + ":" + leftRight + ":" + 
                    rightLeft + ":" + rightRight + ":" + callId + "\n";
         sendMessage(s);
     }
     
     /**
      * Set the stereo volumes for a call.All values are from 0.0 (mute) to 
      * 1.0 (normal) and higher to increase volume.  Normally volumes are 
      * [1.0, 0.0, 0.0, 1.0].  These volumes are private to the source
      * call.
      *
      * @param sourceCallId the call id that hears the volume changes
      * @param targetCallId the call id that has its volumes changed
      * @param leftLeft the volume from the left input channel to the left 
      * output channel
      * @param leftRight the volume from the left input channel to the right
      * output channel
      * @param rightLeft the volume from the right input channel to the left
      * output channel
      * @param rightRight the volume from the right input channel to the right 
      * output channel
      */
     public void setPrivateVolumes(String sourceCallId, String targetCallId,
                                   float leftLeft, float leftRight, 
                                   float rightLeft, float rightRight)
         throws IOException
     {
         String s = "pm=" + leftLeft + ":" + leftRight + ":" + 
                    rightLeft + ":" + rightRight + ":" + targetCallId + ":" +
                    sourceCallId + "\n";
         sendMessage(s);
     }
     
     /**
      * Play a treatment to the specified call
      *
      * @param callId the call to play the treatment to
      * @param treatment the actual treatment to play
      */
     public void playTreatmentToCall(String callId, String treatment) 
         throws IOException 
     {
 	String s = "playTreatmentToCall=" + treatment + ":" + callId +"\n";
         sendMessage(s);
     }
     
     /**
      * Stop all treatments to a call.
      */
     public void stopAllTreatmentsToCall(String callId) throws IOException {
 	String s = "stopTreatmentToCall=" + callId + "\n";
 
 	sendMessage(s);
     }
 
     /**
      * End the specified call
      *
      * @param callId the id of the call to end
      */
     public void endCall(String callId) throws IOException {
 	String s = "cancel=" + callId +"\n";
         sendMessage(s);
     }
     
     /**
      * Set the answer treatment for a conference
      * @param conferenceId the conference id to set the treatment for
      * @param treatment the treatment to play on answer
      */
     public void setAnswerTreatment(String conferenceId, String treatment)
         throws IOException
     {
         sendMessage("at=" + treatment + ":" + conferenceId + "\n");
     }
     
     /**
      * Play a treatment to an entire conference
      *
      * @param conferenceId the conferenceId to play the treatment to
      * @param treatment the treatment to play
      */
     public void playTreatmentToConference(String conferenceId, String treatment)
 	    throws IOException 
     {
 	String s = "playTreatmentToConference=" + treatment + ":" 
 	    + conferenceId +"\n";
         sendMessage(s);
     }
 
     /**
      * Record a conference to the specified file
      *
      * @param conferenceId the conferenceId to record from
      * @param recordingFile the file to record to
      */
     public void recordConference(String conferenceId, String recordingFile) 
         throws IOException 
     {
         StringBuffer sb = new StringBuffer();
         sb.append("recordConference=true:" + conferenceId + ":" + recordingFile + "\n");
         sendMessage(sb.toString());
     }
 
     /**
      * Stop recording a conference
      *
      * @param conferenceId the id to stop recording
      */
     public void stopRecordingConference(String conferenceId) 
         throws IOException 
     {
         String s = "recordConference=false:" + conferenceId + "\n";
         sendMessage(s);
     }
 
     /**
      * Record the audio of a call
      */
     public void recordCall(String recordingFile, String callId) throws IOException {
 		String s = "recordMemberSpeech=true:" + callId +":"+ recordingFile + "\n";
 		System.out.println("****\n"+s+"\n****");
 	    sendMessage(s);
     }
     
     /**
      * Stop recording the audio of a call
      */
     public void stopRecordingCall(String callId) throws IOException {
         String s = "recordMemberSpeech=false:" + callId;
         sendMessage(s);
     }
     
     /**
      * Create a whisper group
      * @param conferenceId the conference to create the whisper group in
      * @param name the name of the whisper group to create
      */
     public void createWhisperGroup(String conferenceId, String name) 
         throws IOException 
     {
 	sendMessage("cwg=" + conferenceId + ":" + name + "\n");
     }
 
     /**
      * Create a whisper group with options
      * @param conferenceId the conference to create the whisper group in
      * @param name the name of the whisper group to create
      * @param trans whether or not the group is transient
      * @param locked whether or not the group is locked
      * @param volume the attenutation factor
      */
     public void createWhisperGroup(String conferenceId, String name, 
             boolean trans, boolean locked, float volume) 
 	throws IOException 
     {
         // create the group
 	sendMessage("cwg=" + conferenceId + ":" + name + "\n");
         
         // set the options
         StringBuffer options = new StringBuffer();
         options.append("transient=" + trans + ":");
         options.append("locked=" + locked + ":");
         options.append("attenuation=" + volume);
         sendMessage("wgo=" + conferenceId + ":" + name + ":" + 
                     options.toString() + "\n");
     }
     
     /**
      * Destroy an existing whisper group
      * @param conferenceId the conference to remove the whisper group from
      * @param name the name of the whisper group to destroy
      */
     public void destroyWhisperGroup(String conferenceId, String name)
         throws IOException 
     {
         String s = "dwg=" + conferenceId + ":" + name + "\n";
         sendMessage(s);
     }
     
     /**
      * Add a call to an existing whisper group
      * @param conferenceId the id of the conference to change whisper groups in
      * @param whisperGroup the name of the group to add a call to
      * @param callId the call id of the call to add
      */
     public void addCallToWhisperGroup(String whisperGroup, String callId)
         throws IOException 
     {
         String s = "acwg=" + whisperGroup + ":" + callId + "\n";
         sendMessage(s);
     }
     
     /**
      * Remove a call from a whisper group
      *
      * @param whisperGroup the name of the whisper group to remove the call from
      * @param callId the id of the call to remove
      */
     public void removeCallFromWhisperGroup(String whisperGroup, String callId)
         throws IOException 
     {
         String s = "rcwg=" + whisperGroup + ":" + callId + "\n";
         sendMessage(s);
     }
      
     /**
      * Enable or disable whispering on a call
      *
      * @param callId the id of the call to remove
      * @param enable true to enable whispering, or false to disable it
      */
     public void setWhisperEnabled(String callId, boolean enabled)
         throws IOException 
     {
         String s = "wok=" + enabled + ":" + callId + "\n";
         sendMessage(s);
     }
     
     /**
      * Mute or unmute whisper groups on a call
      *
      * @param callId the id of the call to mute
      * @param isMuted true to mute whispering, or false to unmute
      */
     public void setWhisperMute(String callId, boolean isMuted)
         throws IOException 
     {
         String s = "mwg=" + isMuted + ":" + callId + "\n";
         sendMessage(s);
     }
     
     /**
      * Start or stop whispering for a given call in a given whisper group
      *
      * @param whisper group the name of the whisper group
      * @param callId the id of the call 
      * @param whispering true to start whispering, or false to stop
      */
     public void setWhispering(String whisperGroup, String callId)
         throws IOException 
     {
         String s = "w=" + whisperGroup + ":" + callId + "\n";
         sendMessage(s);
     }
     
     public void run() {
 	logger.fine("Bridge Connector connecting to " + bridgeHost + 
                     ":" + bridgePort);
 
 	while (isConnected(socket)) {
 	    String s;
 
 	    try {
                 s = bufferedReader.readLine();
 	    } catch (IOException e) {
 		if (isConnected(socket)) {
 	 	    logger.warning("Can't read socket! " 
 			+ socket + " " + e.getMessage());
 
 		    disconnect();
 		}
 		break;
 	    }
 
 	    if (s == null) {
 		if (isConnected(socket)) {
 	 	    logger.warning("Can't read socket, no data! " 
 			+ socket);
 
 		    disconnect();
 		}
 		break;
 	    }
 
             handleCallStatus(s);
         }
 
 	logger.info("No longer connected, socket " + socket);
     }
     
     /**
      * Notify listeners of a change to call status
      *
      * @param status the status
      */
     protected void notifyListeners(CallStatus status) {
         CallStatusListener[] listenerArray;
         
         synchronized (listeners) {
             listenerArray = new CallStatusListener[listeners.size()];
             listeners.toArray(listenerArray);
         }
         
         for (int i = 0; i < listenerArray.length; i++) {
             listenerArray[i].callStatusChanged(status);
         }
     }
     
     /**
      * Send a message to the bridge
      *
      * @param message the message to send
      */
     protected void sendMessage(String message) throws IOException {
         logger.finest("Send message: " + message);
         
         if (!isConnected(socket) || output == null) {
             throw new IOException("Not connected");
         }
         
         synchronized (output) {
             output.write(message.getBytes());
         }
     }
     
     /**
      * Parse a string in the form:
      * 
      * SIPDialer/1.0 100 INVITED CallId='8' ConferenceId='JonCon' CallInfo='22500'
      * 
      * into its component pieces.
      *
      * @param status the status string
      * @return a parsed call status, or null if the string does not represent
      * a valid status
      */
     public static CallStatus parseCallStatus(String status) 
         throws IOException
     {
         logger.finest("Parse call status: " + status);
         
         Pattern p = Pattern.compile("SIPDialer\\/(\\d+\\.\\d+) (\\d+) (\\w+) (.*)");
         Matcher m = p.matcher(status);
         
         if (!m.matches()) {
             return null;
         }
         
         String version = m.group(1);
         int code = Integer.parseInt(m.group(2));
         
         // XXX hack to avoid issues with setup string XXX
         if (code == 0) {
             return null;
         }
         
         // other fields we care about are in name='value' pairs
         Pattern p1 = Pattern.compile("\\b(\\S*)='(.*?)'");
         Matcher m1 = p1.matcher(m.group(4));
         Map options = new HashMap();
         
         while (m1.find()) {
             String name = m1.group(1);
             String value = m1.group(2);
             options.put(name, value);
         }
         
         // finally, create the call status
         return CallStatus.getInstance(version, code, options);
     }
     
     /**
      * Handle a given status string
      * 
      * @param status the status string to handle
      */
     private void handleCallStatus(String status) {
         try {
             CallStatus cs = parseCallStatus(status);
 	    
             if (cs != null) {
                 notifyListeners(cs);
             } else {
 		logger.fine("parse failed! " + status);
 	    }
         } catch (IOException ioe) {
             logger.log(Level.FINE, "Ignore bad status string: " + 
                        status, ioe);
         }
     }
 }
