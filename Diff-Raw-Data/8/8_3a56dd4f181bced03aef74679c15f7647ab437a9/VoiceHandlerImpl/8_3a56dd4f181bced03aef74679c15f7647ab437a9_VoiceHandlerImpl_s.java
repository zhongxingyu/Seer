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
 
 package com.sun.mpk20.voicelib.impl.app;
 
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 import com.sun.mpk20.voicelib.app.ManagedCallBeginEndListener;
 import com.sun.mpk20.voicelib.app.VoiceHandler;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.mpk20.voicelib.app.VoiceManagerParameters;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.NameNotBoundException;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import java.math.BigInteger;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 import com.sun.voip.client.connector.impl.VoiceBridgeConnection;
 import com.sun.voip.CallParticipant;
 
 import com.sun.mpk20.voicelib.app.Spatializer;
 
 public class VoiceHandlerImpl implements VoiceHandler, 
 	ManagedCallStatusListener, Serializable {
 
     /** The serialVersionUID for this class. */
     private final static long serialVersionUID = 1L;
 
     public static final String NAME = VoiceManager.class.getName();
     public static final String DS_PREFIX = NAME + ".";
 
     /* The name of the list for call status listeners */
     private static final String DS_CALL_STATUS_LISTENERS =
         DS_PREFIX + "CallStatusListeners";
     
     /* The name of the list for call begin/end listeners */
     private static final String DS_CALL_BEGIN_END_LISTENERS =
         DS_PREFIX + "CallBeginEndListeners";
     
     private final static String DEFAULT_CONFERENCE = "Test:PCM/16000/2";
 
     private final static String AUDIO_DIR =
             "com.sun.mpk20.gdcdemo.server.AUDIO_DIR";
 
     private final static String DEFAULT_AUDIO_DIR = ".";
 
     private static final Logger logger =
         Logger.getLogger(VoiceHandlerImpl.class.getName());
 
     // the audio directory
     private static String audioDir = 
             System.getProperty(AUDIO_DIR, DEFAULT_AUDIO_DIR);
     
     private static final String SCALE = 
 	"org.jdesktop.lg3d.wonderland.darkstar.server.VoiceHandler.SCALE";
 
     private static double scale = 100.;
 
     private String conferenceId;
 
     static {
 	String s = System.getProperty(SCALE);
 
 	if (s != null) {
 	    try {
 		scale = Double.parseDouble(s);
 	    } catch (NumberFormatException e) {
 		logger.info("Invalid scale factor:  " + s);
 	    }
 	}
     }
 
     public static VoiceHandler getInstance() {
         DataManager dm = AppContext.getDataManager();
         
         VoiceHandlerImpl vh;
         
         try {
             vh = dm.getBinding(NAME, VoiceHandlerImpl.class);
         } catch (NameNotBoundException nnbe) {
             vh = new VoiceHandlerImpl();
             dm.setBinding(NAME, vh);
         }
         
         return vh;
     }
 
     private VoiceHandlerImpl() {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	voiceManager.addCallStatusListener(this);
 
         DataManager dm = AppContext.getDataManager();
 
         try {
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
         } catch (NameNotBoundException e) {
             try {
                 dm.setBinding(DS_CALL_STATUS_LISTENERS, new CallStatusListeners());
             }  catch (RuntimeException re) {
                 logger.warning("failed to bind pending map " + re.getMessage());
                 throw re;
             }
         }
 
         try {
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
         } catch (NameNotBoundException e) {
             try {
                 dm.setBinding(DS_CALL_BEGIN_END_LISTENERS, new CallBeginEndListeners());
             }  catch (RuntimeException re) {
                 logger.warning("failed to bind pending map " + re.getMessage());
                 throw re;
             }
         }
 
 	try {
 	    /*
 	     * XXX We just do this so the voiceService can have a 
 	     * default task owner to use.
 	     * Also so we start monitoring the conference status in case someone
 	     * places a call from outside the client.
 	     */
 	    conferenceId = System.getProperty(
 	        "com.sun.sgs.impl.app.voice", DEFAULT_CONFERENCE);
 
 	    voiceManager.monitorConference(conferenceId);
 	} catch (IOException e) {
 	    logger.severe("Unable to communicate with voice bridge:  " 
 		+ e.getMessage());
 	    return;
 	} 
     }
 
     public static final class CallStatusListeners extends HashMap<BigInteger, 
 	    ListenerInfo> implements ManagedObject, Serializable {
 
          private static final long serialVersionUID = 1;
     }
 
     private static final class CallBeginEndListeners extends HashMap<BigInteger, 
 	    ListenerInfo> implements ManagedObject, Serializable {
 
          private static final long serialVersionUID = 1;
     }
 
     public String getVoiceBridge() {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	String vb = voiceManager.getVoiceBridge();
 	logger.finer("voice bridge is " + vb);
 	return vb;
     }
 
     /*
      * The callId must be unique.  For wonderland, the userId is guaranteed to
      * be unique and is used as the callId for the softphone call.
      */
     public String setupCall(String callId, String sipUrl, String bridge) {
 	logger.info("setupCall:  callId " + callId + " Url: " + sipUrl
 	    + " Bridge: " + bridge);
 
 	CallParticipant cp = getCallParticipant(callId);
 
 	cp.setPhoneNumber(sipUrl);
 
 	String name = "Anonymous";
 
         int end;
 
         if ((end = sipUrl.indexOf("@")) >= 0) {
             name = sipUrl.substring(0, end);
 
             int start;
 
             if ((start = sipUrl.indexOf(":")) >= 0) {
                 name = sipUrl.substring(start + 1, end);
             }
         }
 
 	cp.setName(name);
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
 	    //DefaultSpatializer spatializer = new DefaultSpatializer();
 
	    //spatializer.setFallOff(.95);
 
 	    //voiceManager.setupCall(cp, 0, 0, 0, 0, spatializer, bridge);
 
 	    voiceManager.setupCall(cp, 0, 0, 0, 0, null, bridge);
 	    return callId;
 	} catch (IOException e) {
 	    logger.info("Unable to place call to " + cp.getPhoneNumber()
 		+ " " + e.getMessage());
 	} 
 
 	return null;
     }
     
     private CallParticipant getCallParticipant(String callId) {
 	CallParticipant cp = new CallParticipant();
 
         String conference = System.getProperty(
 	   "com.sun.sgs.impl.app.voice", DEFAULT_CONFERENCE);
 
 	cp.setConferenceId(conference);
 
 	cp.setCallId(callId);
 
         cp.setVoiceDetection(true);
 	cp.setDtmfDetection(true);
 	cp.setVoiceDetectionWhileMuted(true);
 	cp.setHandleSessionProgress(true);
 
 	return cp;
     }
 
     public void createPlayer(String callId, double x, double y, double z,
 	    double orientation, boolean isOutworlder) {
 
 	logger.info("creating player for " + callId 
 	    + " at (" + x + ", " + y + ", " + z + ")" 
 	    + " isOutworlder " + isOutworlder);
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.createPlayer(callId, x, y, z, orientation, isOutworlder);
     }
 
     public void removePlayer(String callId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.removePlayer(callId);
     }
 
     public void transferCall(String callId) throws IOException {
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	String[] tokens = conferenceId.split(":");
 
 	voiceManager.transferCall(callId, tokens[0]);
     }
 	
     public void setPublicSpatializer(String callId, Spatializer spatializer) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setPublicSpatializer(callId, spatializer);
     }
     
     public void removePublicSpatializer(String callId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setPublicSpatializer(callId, null);
     }
 
     public void setPrivateSpatializer(String targetCallId, String sourceCallId,
 	    Spatializer spatializer) {
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setPrivateSpatializer(targetCallId, sourceCallId,
 	     spatializer);
     }
 
     public void removePrivateSpatializer(String targetCallId, String sourceCallId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setPrivateSpatializer(targetCallId, sourceCallId, null);
     }
 
     public void setIncomingSpatializer(String targetCallId, 
 	    Spatializer spatializer) {
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setIncomingSpatializer(targetCallId, spatializer);
     }
 
     public void removeIncomingSpatializer(String targetCallId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setIncomingSpatializer(targetCallId, null);
     }
 
     public void setTalkAttenuator(String callId, double talkAttenuator) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	
 	voiceManager.setTalkAttenuator(callId, talkAttenuator);
     }
 
     public double getTalkAttenuator(String callId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	
 	return voiceManager.getTalkAttenuator(callId);
     }
 
     public void setListenAttenuator(String callId, double listenAttenuator) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	
 	voiceManager.setListenAttenuator(callId, listenAttenuator);
     }
 
     public double getListenAttenuator(String callId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	
 	return voiceManager.getListenAttenuator(callId);
     }
 
     private static Object treatmentLock = new Object();
 
     /* Maps callId's to treatmentGroupId */
     private static Hashtable<String, String> treatmentCallIds;
 
     /* Maps treatmentGroupId's to treatmentGroups */
     private static Hashtable<String, Hashtable> treatmentGroups;
 
     /* Maps callId to treatment information */
     private static ConcurrentHashMap<String, TreatmentInfo> treatmentInfo =
 	new ConcurrentHashMap<String, TreatmentInfo>();;
 
     class TreatmentInfo {
 	public String treatment;
 	public String group;
 	public ManagedReference listener;
 	public double lowerLeftX;
 	public double lowerLeftY; 
 	public double lowerLeftZ;
 	public double upperRightX;
 	public double upperRightY;
 	public double upperRightZ;
 
  	public boolean isCallEstablished;
 
 	public TreatmentInfo(String treatment, String group,
                 ManagedReference listener,
                 double lowerLeftX, double lowerLeftY, double lowerLeftZ,
                 double upperRightX, double upperRightY, double upperRightZ) {
 
 	    this.treatment = treatment;
 	    this.group = group;
 	    this.listener = listener;
 	    this.lowerLeftX = lowerLeftX;
 	    this.lowerLeftY = lowerLeftY;
 	    this.lowerLeftZ = lowerLeftZ;
 	    this.upperRightX = upperRightX;
 	    this.upperRightY = upperRightY;
 	    this.upperRightZ = upperRightZ;
 	}
     }
 
     public String setupTreatment(String id, String treatment, String group, 
 	    ManagedCallStatusListener listener,
 	    double lowerLeftX, double lowerLeftY, double lowerLeftZ, 
 	    double upperRightX, double upperRightY, double upperRightZ) {
 
 	logger.info("setupTreatment:  id " + id + " treatment " + treatment
             + " group: " + group);
         
         if (treatment == null) {
             return null;
         }
         
 	String callId = id;
 
 	CallParticipant cp = new CallParticipant();
 
         String conference = System.getProperty(
 	   "com.sun.sgs.impl.app.voice", DEFAULT_CONFERENCE);
 
 	cp.setConferenceId(conference);
 
 	cp.setInputTreatment(getTreatmentFile(treatment));
 	cp.setName(id);
 	//cp.setVoiceDetection(true);
         
 	String treatmentGroupId = group;
 
 	cp.setCallId(callId);
 
         ManagedReference mr = null;
 
 	if (listener != null) {
             DataManager dm = AppContext.getDataManager();
         
 	    mr = dm.createReference(listener);
 
 	    addCallStatusListener(mr, callId);
 	}
 
         // get a spatializer
         Spatializer spatializer = null;
         
 	if (lowerLeftX != upperRightX) {
 	     spatializer = new AmbientSpatializer(
 		lowerLeftX, lowerLeftY, lowerLeftZ,
 		upperRightX, upperRightY, upperRightZ);
 	}
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
 	    voiceManager.setupCall(cp, 0, 0, 0, 0, spatializer, null);
             voiceManager.setAttenuationVolume(callId, 1);
 	} catch (IOException e) {
 	    logger.info("Unable to setup treatment " + treatment
 		+ " " + e.getMessage());
 
 	    return null;
 	} 
 
 	logger.finest("back from starting treatment...");
 
         DataManager dm = AppContext.getDataManager();
         
 	treatmentInfo.put(callId, new TreatmentInfo(treatment, group,
             mr, lowerLeftX, lowerLeftY, lowerLeftZ,
             upperRightX, upperRightY, upperRightZ));
 
 	synchronized (treatmentLock) {
 	    if (treatmentCallIds == null) {
 	        treatmentCallIds = new Hashtable<String, String>();
 	    }
 
 	    if (treatmentGroups == null) {
 	        treatmentGroups = new Hashtable<String, Hashtable>();
 	    }
 
             if (treatmentGroupId == null) {
                 treatmentGroupId = callId;
             }
             
 	    Hashtable treatmentGroup = treatmentGroups.get(treatmentGroupId);
 	
 	    if (treatmentGroup == null) {
 	        /*
 	         * This is a new treatment group
 	         */
 		logger.finer("creating new treatment group for " 
 		    + treatmentGroupId);
 
 	        treatmentGroup = new Hashtable<String, String>();
 	        treatmentGroups.put(treatmentGroupId, treatmentGroup);
 	    }
 
 	    logger.finest("Adding " + callId + " to treatment group " 
 		+ treatmentGroupId);
 
 	    treatmentCallIds.put(callId, treatmentGroupId);
 	    treatmentGroup.put(callId, new Boolean(false));
 	}
 
 	restartInputTreatments(treatmentGroupId);
 	return callId;
     }
 
     private void restartTreatment(String callId) {
 	logger.fine("restarting treatment " + callId);
 
 	if (treatmentCallIds == null) {
 	    return;   // there haven't been any treatments
 	}
 
 	TreatmentInfo t = treatmentInfo.get(callId);
 
 	if (t == null) {
 	    logger.fine("callId " + callId + " treatment info is null");
 	    return;
 	}
 
 	/*
 	 * Check if all treatments in the group have finished.  
 	 * If so, restart all of them.
 	 */
 	String treatmentGroupId;
 
 	synchronized (treatmentLock) {
 	    treatmentGroupId = treatmentCallIds.get(callId);
 
 	    if (treatmentGroupId == null) {
 		logger.finest("call id not found for treatment done... " 
 		    + callId);
 		return; // call wasn't started here
 	    }
 	    
 	    Hashtable<String, Boolean> treatmentGroup = 
 		treatmentGroups.get(treatmentGroupId);
 
 	    treatmentGroup.put(callId, new Boolean(true));
 
 	    Enumeration<String> keys = treatmentGroup.keys();
 
 	    while (keys.hasMoreElements()) {
 		String id = keys.nextElement();
 
 		Boolean done = treatmentGroup.get(id);
 
 		if (done == false) {
 		    logger.finest("Waiting for other treatments to finish");
 		    return;	// still some treatments which haven't finished
 		}
 	    }	
 	}
 
 	restartInputTreatments(treatmentGroupId);
     }
 
     public void newInputTreatment(String callId, String treatment, 
 	    String group) {
 
 	logger.fine("new treatment " + callId);
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.newInputTreatment(callId, 
                     getTreatmentFile(treatment));
 	} catch (IOException e) {
 	    logger.info("Unable to start new treatment " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     public void stopInputTreatment(String callId) {
 	logger.fine("stop treatment for callId " + callId);
 
 	removeTreatment(callId);
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.endCall(callId);
 	} catch (IOException e) {
 	    logger.info("Unable to end call " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     private void removeTreatment(String callId) {
 	logger.fine("remove treatment " + callId);
 
 	TreatmentInfo t = treatmentInfo.get(callId);
 
 	if (t == null) {
 	    logger.fine("No treatment info for " + callId);
 	    return;
 	}
 
 	if (t.listener != null) {
 	    removeCallStatusListener(t.listener);
 	}
 
         Hashtable<String, Boolean> treatmentGroup = 
 	    treatmentGroups.get(t.group);
 
 	treatmentGroup.remove(callId);
 
 	treatmentCallIds.remove(callId);
         treatmentInfo.remove(callId);
 
 	Enumeration<String> keys = treatmentGroup.keys();
 
 	if (keys.hasMoreElements() == false) {
 	    logger.fine("Removing last treatment in group " + t.group);
 
 	    treatmentGroups.remove(t.group);
 	}
     }
 
     public void playTreatmentToCall(String callId, String treatment) {
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.playTreatmentToCall(callId, treatment);
 	} catch (IOException e) {
 	    logger.info("Unable to play treatment to call " + callId
 		+ " " + treatment + " " + e.getMessage());
 	}
     }
 
     public void pauseTreatmentToCall(String callId, String treatment) {
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.pauseTreatmentToCall(callId, treatment);
 	} catch (IOException e) {
 	    logger.info("Unable to pause treatment to call " + callId
 		+ " " + treatment + " " + e.getMessage());
 	}
     }
 
     public void stopTreatmentToCall(String callId, String treatment) {
 	logger.fine("Stopping treatment " + treatment + " to call "
 	    + callId);
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.stopTreatmentToCall(callId, treatment);
 	} catch (IOException e) {
 	    logger.info("Unable to end treatment to call " + callId
 		+ " " + treatment + " " + e.getMessage());
 	}
     }
 
     public void migrateCall(String callId, String phoneNumber) {
 	CallParticipant cp = getCallParticipant(callId);
 
 	cp.setPhoneNumber(phoneNumber);
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.migrateCall(cp);
 	} catch (IOException e) {
 	    sendStatus(CallStatus.NOANSWER, callId, e.getMessage());
 	}
     }
 
     public void endCall(String callId) {
 	logger.fine("ending call " + callId);
 
         removeTreatment(callId);
 
         DataManager dm = AppContext.getDataManager();
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.endCall(callId);
 	} catch (IOException e) {
 	    logger.info("Unable to end call " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     public void disconnectCall(String callId) {
 	logger.info("Disconnecting call " + callId);
 
         DataManager dm = AppContext.getDataManager();
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.disconnectCall(callId);
 	} catch (IOException e) {
 	    logger.info("Unable to end call " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     public void muteCall(String callId, boolean isMuted) {
 	if (isMuted) {
             logger.fine("muting call " + callId);
 	} else {
             logger.fine("unmuting call " + callId);
 	}
 
         try {
             VoiceManager voiceManager =
                 AppContext.getManager(VoiceManager.class);
             voiceManager.muteCall(callId, isMuted);
         } catch (IOException e) {
             logger.info("Unable to mute/unmute " + callId
                 + " " + e.getMessage());
         }
     }
 
     /*
      * Restart treatments in the group if there's more than one call
      */
     private void restartInputTreatments(String treatmentGroupId) {
 	logger.fine("Restarting input treatments for " + treatmentGroupId);
 
 	synchronized (treatmentLock) {
 	    Hashtable<String, Hashtable<String, Boolean>> treatmentGroup = 
 		treatmentGroups.get(treatmentGroupId);
 
 	    Enumeration<String> keys = treatmentGroup.keys();
 
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
 	    while (keys.hasMoreElements()) {
 	        String id = keys.nextElement();
 
 		TreatmentInfo t = treatmentInfo.get(id);
 
 		if (t == null) {
 		    logger.info("No treatment info for " + id);
 		    continue;
 		}
 
 		if (t.isCallEstablished == false) {
 		    continue;
 		}
 
 	        try {
 		    logger.fine("Restarting input treatment for call " + id);
 	            voiceManager.restartInputTreatment(id);
 	        } catch (IOException e) {
 	            logger.warning("Unable to restart treatment for " + id
 		        + " " + e.getMessage());
 	        }
 	    }
 	}
     }
 
     /**
      * Resolve a treatment name into an absolute file name.  If the treatment
      * name does not start with the path separator, prepend the value of
      * the audioDir property to it to get the file name
      * @param treatment the unresolved treatment name
      * @return the resolved treatment name
      */
     private String getTreatmentFile(String treatment) {
         String ps = System.getProperty("file.separator");
         if (!treatment.startsWith(ps)) {
             treatment = audioDir + ps + treatment;
         }
         
         return treatment;
     }
     
     public Spatializer getLivePlayerSpatializer() {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	return voiceManager.getLivePlayerSpatializer();
     }
 
     public Spatializer getStationarySpatializer() {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	return voiceManager.getStationarySpatializer();
     }
 
     public Spatializer getOutworlderSpatializer() {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	return voiceManager.getOutworlderSpatializer();
     }
 
     public void setSpatialAudio(boolean enabled) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setSpatialAudio(enabled);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio: " + e.getMessage());
 	}
     }
 
     public void setSpatialMinVolume(double spatialMinVolume) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setSpatialMinVolume(spatialMinVolume);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio min volume: " 
 		+ e.getMessage());
 	}
     }
 
    public void setSpatialFallOff(double spatialFallOff) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
	    voiceManager.setSpatialFallOff(spatialFallOff);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio fall off: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialEchoDelay(double spatialEchoDelay) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setSpatialEchoDelay(spatialEchoDelay);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo delay: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialEchoVolume(double spatialEchoVolume) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setSpatialEchoVolume(spatialEchoVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo volume: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialBehindVolume(double spatialBehindVolume) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setSpatialBehindVolume(spatialBehindVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio behind volume: "
                 + e.getMessage());
         }
     }
 
     public void setMasterVolume(String callId, double masterVolume) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         voiceManager.setMasterVolume(callId, masterVolume);
     }
 
     public double getMasterVolume(String callId) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         return voiceManager.getMasterVolume(callId);
     }
 
     static class ListenerInfo implements Serializable {
 	private static final long serialVersionUID = 1;
 
 	public ManagedReference mr;
 	public String callId;
 
 	public ListenerInfo(ManagedReference mr) {
 	    this(mr, null);
 	}
 
 	public ListenerInfo(ManagedReference mr, String callId) {
 	    this.mr = mr;
 	    this.callId = callId;
 	}
     }
 
     /*
      * Add a listener for all calls
      */
     public void addCallStatusListener(ManagedCallStatusListener listener) {
 	addCallStatusListener(listener, null);
     }
 
     /*
      * Add a listener for a specific callId
      */
     public void addCallStatusListener(ManagedCallStatusListener listener, 
 	    String callId) {
 
         DataManager dm = AppContext.getDataManager();
 
 	ManagedReference mr = dm.createReference(listener);
 	
 	addCallStatusListener(mr, callId);
     }
 
     /*
      * FIXME:  There is a problem here.  A given listener
      * can only register to listen for one phone call!
      */
     public void addCallStatusListener(ManagedReference mr, String callId) {
 
 	logger.finer("Adding listener " + mr + " for callId " + callId);
 
         DataManager dm = AppContext.getDataManager();
 
         CallStatusListeners listeners =
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
 
         listeners.put(mr.getId(), new ListenerInfo(mr, callId));
 
         logger.fine("listeners size " + listeners.size() + " added mr " + mr.getId());
     }
 
     public void removeCallStatusListener(ManagedCallStatusListener listener) {
         DataManager dm = AppContext.getDataManager();
 
 	ManagedReference mr = dm.createReference(listener);
 
 	removeCallStatusListener(mr);
     }
 
     public void removeCallStatusListener(ManagedReference mr) {
 	logger.finer("removing listener " + mr); 
 
         DataManager dm = AppContext.getDataManager();
 
         CallStatusListeners listeners =
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
 
 	logger.fine("removing listener mr " + mr.getId());
 
 	if (listeners.remove(mr.getId()) == null) {
 	    logger.info("mr " + mr + " id " + mr.getId()
 		+ " is not in map of call status listeners!");
 	}
     }
 
     public void addCallBeginEndListener(ManagedCallBeginEndListener listener) {
         DataManager dm = AppContext.getDataManager();
 
         CallBeginEndListeners listeners =
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
 
         /*
          * Create a reference to listener and keep that.
          */
         ManagedReference mr = dm.createReference(listener);
 
 	logger.finer("adding listener " + mr);
 
         listeners.put(mr.getId(), new ListenerInfo(mr));
 
         logger.finest("listeners size " + listeners.size());
     }
 
     public void removeCallBeginEndListener(ManagedCallBeginEndListener listener) {
         DataManager dm = AppContext.getDataManager();
 
         CallBeginEndListeners listeners =
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
 
         ManagedReference mr = dm.createReference(listener);
 
 	logger.finer("removing listener " + mr);
 
 	if (listeners.remove(mr.getId()) == null) {
 	    logger.info("mr " + mr + " id " + mr.getId()
 		+ " is not in map of call status listeners!");
 	}
     }
 
     private void sendStatus(int statusCode, String callId, String info) {
         String s = "SIPDialer/1.0 " + statusCode + " "
             + CallStatus.getCodeString(statusCode)
             + " CallId='" + callId + "'"
             + " CallInfo='" + info + "'";
 
         CallStatus callStatus = null;
 
         try {
             callStatus = VoiceBridgeConnection.parseCallStatus(s);
 
             if (callStatus == null) {
                 logger.info("Unable to parse call status:  " + s);
                 return;
             }
 
 	    callStatusChanged(callStatus);
         } catch (IOException e) {
             logger.info("Unable to parse call status:  " + s
 		+ " " + e.getMessage());
         }
     }
 
     public void callStatusChanged(CallStatus callStatus) {
 	int code = callStatus.getCode();
 
 	String callId = callStatus.getCallId();
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
         
 	notifyCallStatusListeners(callStatus);
 
 	TreatmentInfo t;
 
 	switch (code) {
         case CallStatus.ESTABLISHED:
             logger.fine("callEstablished: " + callId);
 
             try {
 	        voiceManager.callEstablished(callId);
 	    } catch (IOException e) {
 		logger.info(e.getMessage());
 	    }
 
 	    t = treatmentInfo.get(callId);
 
 	    if (t != null) {
 	        t.isCallEstablished = true;
 		restartTreatment(callId);
 	    } else {
 		logger.fine("No treatment info for " + callId);
 	    }
 
 	    String s = callStatus.getOption("IncomingCall");
 
 	    if (s != null && s.equals("true")) {
 		handleIncomingCall(callStatus);
 	    }
 
 	    notifyCallBeginEndListeners(callStatus);
             break;
 
         case CallStatus.STARTEDSPEAKING:
             break;
 
         case CallStatus.STOPPEDSPEAKING:
             break;
 
 	case CallStatus.TREATMENTDONE:
 	    logger.finer("Treatment done: " + callStatus);
 	    restartTreatment(callId);
 	    break;
 
         case CallStatus.ENDED:
 	    logger.info(callStatus.toString());
 
 	    notifyCallBeginEndListeners(callStatus);
 
 	    try {
                 voiceManager.endCall(callId, false);
             } catch (IOException e) {
                 logger.warning("Unable to tell voice manager that call " 
 		    + callId + " ended");
             }
 
 	    break;
 
 	case CallStatus.BRIDGE_OFFLINE:
 	    logger.info("Bridge offline: " + callStatus);
 	    
 	    /*
 	     * Treatments are automatically restarted here.
 	     * Clients are notified that the bridge went down
 	     * and they notify the softphone which then reconnects.
 	     */
 	    t = treatmentInfo.get(callId);
 
 	    if (t != null) {
 		logger.fine("Restarting treatment " + t.treatment);
 		restartInputTreatments(t.group);
 	    } 
 
 	    /*
 	     * After the last bridge_offline call (callID=''),
 	     * we have to tell the voice manager to restore
 	     * all the pm's for live players.
 	     */
 	    if (callId.length() == 0) {
 		logger.fine("Restoring private mixes...");
 
 		try {
 	    	    voiceManager.restorePrivateMixes();
 		} catch (IOException e) {
 	    	    logger.info("restorePrivateMixes failed:  "
 		        + e.getMessage());
 		}
 	    }
 	    break;
         }
     }
 
     private void handleIncomingCall(CallStatus callStatus) {
     }
 
     private void notifyCallStatusListeners(CallStatus status) {
         DataManager dm = AppContext.getDataManager();
 
         CallStatusListeners callStatusListeners =
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
 
         ArrayList<ListenerInfo> listenerList = new ArrayList<ListenerInfo>();
 
         synchronized (callStatusListeners) {
 	    Collection<ListenerInfo> c = callStatusListeners.values();
 
             Iterator<ListenerInfo> iterator = c.iterator();
 
 	    while (iterator.hasNext()) {
 		ListenerInfo info = iterator.next();
 
 		listenerList.add(info);
 	    }
 	}
 	
         for (ListenerInfo info : listenerList) {
             ManagedCallStatusListener listener =
                 info.mr.get(ManagedCallStatusListener.class);
 
 	    if (info.callId == null || info.callId.equals(status.getCallId())) {
                 listener.callStatusChanged(status);
 	    } 
         }
     }
 
     private void notifyCallBeginEndListeners(CallStatus status) {
         DataManager dm = AppContext.getDataManager();
 
         CallBeginEndListeners callBeginEndListeners =
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
 
         ArrayList<ListenerInfo> listenerList = new ArrayList<ListenerInfo>();
 
         synchronized (callBeginEndListeners) {
 	    Collection<ListenerInfo> c = callBeginEndListeners.values();
 
             Iterator<ListenerInfo> iterator = c.iterator();
 
 	    while (iterator.hasNext()) {
 		ListenerInfo info = iterator.next();
 		listenerList.add(info);
 	    }
 	}
 	
         for (ListenerInfo info : listenerList) {
             ManagedCallBeginEndListener listener =
                 info.mr.get(ManagedCallBeginEndListener.class);
 
             listener.callBeginEndNotification(status);
         }
     }
 
     public void setPositionAndOrientation(String callId, double x, double y, 
 	    double z, double orientation) {
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setPositionAndOrientation(callId, x / scale, y / scale, z / scale,
 		orientation);
 	} catch (IOException e) {
 	    logger.info("callId " + callId + " setPositionAndOrientation failed:  "
 		+ e.getMessage());
 	}
     }
 
     public void setPosition(String callId, double x, double y, double z) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setPosition(callId, x / scale, y / scale, z / scale);
 	} catch (IOException e) {
 	    logger.info("callId " + callId + " setPosition failed:  "
 		+ e.getMessage());
 	}
     }
 
     public void setOrientation(String callId, double orientation) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setOrientation(callId, orientation);
         } catch (IOException e) {
             logger.info("callId " + callId + " setOrientation failed:  "
                 + e.getMessage());
         }
     }
 
     public void setAttenuationRadius(String callId,
 	    double attenuationRadius) {
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setAttenuationRadius(callId, attenuationRadius);
         } catch (IOException e) {
             logger.info("callId " + callId + " setAttenuationRadius failed:  "
                 + e.getMessage());
         }
     }
 
     public void setAttenuationVolume(String callId,
 	    double attenuationVolume) {
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setAttenuationVolume(callId, attenuationVolume);
         } catch (IOException e) {
             logger.info("callId " + callId + " setAttenuationVolume failed:  "
                 + e.getMessage());
         }
     }
 
     public void addWall(double startX, double startY, double endX,
 	    double endY, double characteristic) {
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.addWall(startX, startY, endX, endY, characteristic);
         } catch (IOException e) {
             logger.info(" addWall failed:  " + e.getMessage());
         }
     }
 
     public void setVoiceManagerParameters(VoiceManagerParameters parameters) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	setLogLevel(parameters.logLevel);
 	voiceManager.setParameters(parameters);
     }
 
     public VoiceManagerParameters getVoiceManagerParameters() {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	VoiceManagerParameters parameters = voiceManager.getParameters();
 	
 	parameters.logLevel = getLogLevel();
 	return parameters;
     }
 
     private int getLogLevel() {
 	Level logLevel;
 
 	Logger l = logger;
 
 	while ((logLevel = l.getLevel()) == null) {
 	    l = l.getParent();
 	}
 
 	if (logLevel.equals(Level.SEVERE)) {
 	    return 0;
 	}
 	
 	if (logLevel.equals(Level.WARNING)) {
 	    return 1;
 	}
 	
 	if (logLevel.equals(Level.INFO)) {
 	    return 2;
 	}
 	
 	if (logLevel.equals(Level.FINE)) {
 	    return 3;
 	}
 	
 	if (logLevel.equals(Level.FINER)) {
 	    return 4;
 	}
 	
 	if (logLevel.equals(Level.ALL)) {
 	    return 5;
 	}
 
 	logger.info("Unknown log level " + logLevel + " using FINEST");
 
 	return 5;
     }
 
     private void setLogLevel(int logLevel) {
 	Level level;
 
 	switch (logLevel) {
 	case 0:
 	    level = Level.SEVERE;
 	    break;
 
 	case 1:
 	    level = Level.WARNING;
 	    break;
 
 	case 2:
 	    level = Level.INFO;
 	    break;
 
 	case 3:
 	    level = Level.FINE;
 	    break;
 
 	case 4:
 	    level = Level.FINER;
 	    break;
 
 	case 5:
 	    level = Level.ALL;
 	    break;
 
 	default:
 	    logger.info("Invalid log level " + logLevel);
 	    return;
 	}
 
 	logger.fine("Setting log level to " + level);
 	logger.setLevel(level);
 	
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	voiceManager.setLogLevel(level);
     }
 
     public int getNumberOfPlayersInRange(double x, double y, double z) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	return voiceManager.getNumberOfPlayersInRange(x / scale, y / scale, 
 	    z / scale);
     }
 
     public int getNumberOfPlayersInRange(String callId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	return voiceManager.getNumberOfPlayersInRange(callId);
     }
 
     public void setupRecorder(String callId, double x, double y, double z, 
 	    String recordDirectory) throws IOException {
 
 	CallParticipant cp = new CallParticipant();
 
         String conference = System.getProperty(
 	   "com.sun.sgs.impl.app.voice", DEFAULT_CONFERENCE);
 
 	cp.setConferenceId(conference);
 
 	cp.setInputTreatment("null");
 	cp.setRecorder(true);
 	cp.setName(callId);
 
 	cp.setCallId(callId);
 
 	cp.setRecordDirectory(recordDirectory);
 
 	logger.info("New recorder at (" + x + ":" + z + ":" + y + ")");
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
 	    voiceManager.setupCall(cp, x / scale, z / scale, y / scale, 
 		0, null, null);
 	} catch (IOException e) {
 	    logger.info("Unable to setup recorder " + callId 
 		+ e.getMessage());
 	    return;
 	} 
 
 	logger.finest("back from starting recorder...");
     }
 
     public void startRecording(String callId, String recordingFile) 
 	    throws IOException {
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.startRecording(callId, recordingFile);
     }
 
     public void stopRecording(String callId) throws IOException {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.stopRecording(callId);
     }
 
     public void playRecording(String callId, String recordingFile) throws IOException {
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.newInputTreatment(callId, recordingFile);
 	} catch (IOException e) {
 	    logger.info("Unable to start recording " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     static class AmbientSpatializer implements Spatializer {
         double minX;
         double maxX;
         double minY;
         double maxY;
         double minZ;
         double maxZ;
 
 	double attenuator;
 
         public AmbientSpatializer(double lowerLeftX, double lowerLeftY,
 	 	double lowerLeftZ, double upperRightX,
 		double upperRightY, double upperRightZ) {
 
 	    setBounds(lowerLeftX, lowerLeftY, lowerLeftZ,
 		upperRightX, upperRightY, upperRightZ);
         }
         
 	public void setBounds(double lowerLeftX, double lowerLeftY,
 	 	double lowerLeftZ, double upperRightX,
 		double upperRightY, double upperRightZ) {
 
 	    //logger.finest("lX " + lowerLeftX + " lY " + lowerLeftY
 	    //	+ " lZ " + lowerLeftZ + " uX " + upperRightX
 	    //	+ " uY " + upperRightY + " uZ " + upperRightZ);
 
             minX = Math.min(lowerLeftX / scale, upperRightX / scale);
             maxX = Math.max(lowerLeftX / scale, upperRightX / scale);
             minY = Math.min(lowerLeftY / scale, upperRightY / scale);
             maxY = Math.max(lowerLeftY / scale, upperRightY / scale);
             minZ = Math.min(lowerLeftZ / scale, upperRightZ / scale);
             maxZ = Math.max(lowerLeftZ / scale, upperRightZ / scale);
 	}
 
         public double[] spatialize(double sourceX, double sourceY, 
                                    double sourceZ, double sourceOrientation, 
                                    double destX, double destY, 
                                    double destZ, double destOrientation)
         {
             // see if the destination is inside the ambient audio range
             if (isInside(destX, destY, destZ)) {
 		//logger.finest("inside min (" + round(minX) + ", " + round(minY) 
 		//  + ", " + round(minZ) + ") "
 		//  + " max (" + round(maxX) + ", " + round(maxY) + ", " 
 		//  + round(maxZ) + ") " + " dest (" + round(destX) + ", " 
 		//  + round(destY) + ", " + round(destZ) + ")");
                 return new double[] { 0, 0, 0, 1 };
             } else {
 		//logger.info("outside min (" + round(minX) + ", " + round(minY) 
 		//  + ", " + round(minZ) + ") "
 		//  + " max (" + round(maxX) + ", " + round(maxY) + ", " 
 		//  + round(maxZ) + ") " + " dest (" + round(destX) + ", " 
 		//  + round(destY) + ", " + round(destZ) + ")");
                 return new double[] { 0, 0, 0, 0 };
             }
         }
         
 	private boolean isInside(double x, double y, double z) {
 	    //logger.info("isInside: x " + round(x) + " y " + round(y) 
 	    //	+ " z " + z
 	    //	+ " minX " + round(minX) + " maxX " + round(maxX)
 	    //	+ " minY " + round(minY) + " maxY " + round(maxY)
 	    //	+ " minZ " + minZ + " maxZ " + maxZ); 
 
 	    /*
 	     * Don't check z because we always expect 0, but it's always
 	     * non-zero.
 	     */
             return x >= minX && x <= maxX &&
                y >= minY && y <= maxY;
 	}
 
 	public void setAttenuator(double attenuator) {
 	    this.attenuator = attenuator;
 	}
 
 	public double getAttenuator() {
 	    return attenuator;
 	}
 
     }
 
     private static double round(double d) {
 	return Math.round(d * 100) / 100.;
     }
 
 }
