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
 
 import com.sun.mpk20.voicelib.impl.service.voice.work.treatmentgroup.*;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.NameNotBoundException;
 
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.TreatmentGroup;
 import com.sun.mpk20.voicelib.app.Treatment;
 import com.sun.mpk20.voicelib.app.Util;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import java.util.logging.Logger;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 public class TreatmentGroupImpl implements TreatmentGroup, CallStatusListener, Serializable {
 
     private static final Logger logger =
         Logger.getLogger(PlayerImpl.class.getName());
 
     private String id;
 
     private ConcurrentHashMap<String, Treatment> treatments = new ConcurrentHashMap();
 
     private int numberTreatmentsDone;
 
     public TreatmentGroupImpl(String id) {
 	this.id = Util.generateUniqueId(id);
 
 	if (VoiceImpl.getInstance().addWork(new CreateTreatmentGroupWork(this)) == false) {
 	    treatmentGroupImplCommit();
 	}
     }
 
     private void treatmentGroupImplCommit() {
 	VoiceImpl.getInstance().putTreatmentGroup(this);
     }
 	
     public void removeTreatmentGroupCommit() {
 	VoiceImpl.getInstance().removeTreatmentGroup(this);
     }
 
     public String getId() {
 	return id;
     }
 
     public void addTreatment(Treatment treatment) {
 	if (VoiceImpl.getInstance().addWork(new AddTreatmentWork(this, treatment)) == false) {
 	    addTreatmentCommit(treatment);
 	} else {
 	    DataManager dm = AppContext.getDataManager();
 
             WarmStartTreatments warmStartTreatments;
 
             warmStartTreatments = (WarmStartTreatments) dm.getBinding(
                 WarmStartInfo.DS_WARM_START_TREATMENTS);
 
 	    warmStartTreatments.put(treatment.getId(), 
 	        new WarmStartTreatmentInfo(id, treatment.getSetup()));
 	}
     }
 
     private void addTreatmentCommit(Treatment treatment) {
	if (treatment.getCall() == null) {
	    logger.warning("Treatment call ended:  " + treatment);
	    return;
	}

 	String callId = treatment.getCall().getId();
 
 	VoiceImpl.getInstance().addCallStatusListener(this, callId);
 	treatments.put(callId, treatment);
 	restartTreatments(true);
     }
 
     public void removeTreatment(Treatment treatment) {
 	removeTreatment(treatment, true);
     }
 
     public void removeTreatment(Treatment treatment, boolean restartTreatments) {
 	if (VoiceImpl.getInstance().addWork(new RemoveTreatmentWork(this, treatment, restartTreatments)) == false) {
 	    removeTreatmentCommit(treatment, restartTreatments);
 	    return;
 	}
 
 	DataManager dm = AppContext.getDataManager();
 
         WarmStartTreatments warmStartTreatments;
 
         warmStartTreatments = (WarmStartTreatments) dm.getBinding(
             WarmStartInfo.DS_WARM_START_TREATMENTS);
 
 	WarmStartTreatmentInfo info = warmStartTreatments.get(treatment.getId());
 
 	if (info != null) {
 	    info.groupId = null;
 	}
     }
 
     private void removeTreatmentCommit(Treatment treatment, boolean restartTreatments) {
 	Call call = treatment.getCall();
 
 	if (call != null) {
 	    String callId = treatment.getCall().getId();
 
 	    VoiceImpl.getInstance().removeCallStatusListener(this, callId);
 	    treatments.remove(callId);
 	}
 
 	if (restartTreatments) {
 	    restartTreatments(true);
 	}
     }
 
     public ConcurrentHashMap<String, Treatment> getTreatments() {
 	return treatments;
     }
 
     /*
      * Restart treatments in the group if there's more than one call
      */
     private void restartTreatments(boolean alwaysRestart) {
 	logger.fine("Restarting input treatments for " + id);
 
 	Collection<Treatment> c = treatments.values();
 
 	Iterator<Treatment> it = c.iterator();
 
 	while (it.hasNext()) {
 	    Treatment treatment = it.next();
 
 	    CallImpl call = (CallImpl) treatment.getCall();
 
 	    if (alwaysRestart == false && call.getPlayer() == null) {
 		continue;
 	    }
 
 	    String callId = call.getId();
 
 	    logger.fine("Restarting input treatment " + treatment);
 
 	    call.restartInputTreatment();
 	}
     }
 
     public void callStatusChanged(CallStatus status) {
 	int code = status.getCode();
 
 	String callId = status.getCallId();
 
 	if (callId == null) {
 	    return;
 	}
 
 	Treatment treatment = treatments.get(callId);
 
 	switch (code) {
         case CallStatus.ESTABLISHED:
         case CallStatus.MIGRATED:
             logger.fine("callEstablished: " + callId);
 	    Player p = VoiceImpl.getInstance().getPlayer(callId);
 
 	    if (p == null) {
 		logger.warning("No player for " + callId);
 		break;
 	    }
 
 	    p.setPrivateMixes(true);
             break;
 
 	case CallStatus.TREATMENTDONE:
 	    logger.finer("Treatment done: " + status);
 
 	    numberTreatmentsDone++;
 
 	    if (numberTreatmentsDone == treatments.size()) {
 		numberTreatmentsDone = 0;
 	        restartTreatments(false);
 	    }
 
 	    break;
 
         case CallStatus.ENDED:
 	    logger.info(status.toString());
 	    removeTreatment(treatment);
 	    break;
 
 	case CallStatus.BRIDGE_OFFLINE:
 	    logger.info("Bridge offline: " + status);
 	    
 	    if (callId == null || callId.length() == 0 || treatment == null) {
 		return;
 	    }
 
 	    treatment.stop();
 
 	    removeTreatment(treatment, false);
 
 	    try {
 	  	TreatmentImpl treatmentImpl = new TreatmentImpl(treatment.getId(), 
 		    treatment.getSetup());
 	        addTreatment(treatment);
 	    } catch (IOException e) {
 	        logger.warning("Unable to create treatment " + treatment.getId());
 	    }
 
 	    break;
         }
     }
 
     public void commit(TreatmentGroupWork work) {
 	VoiceImpl voiceImpl = VoiceImpl.getInstance();
 
 	if (work instanceof CreateTreatmentGroupWork) {
 	    treatmentGroupImplCommit();
 	    return;
 	}
 
 	if (work instanceof RemoveTreatmentGroupWork) {
 	    removeTreatmentGroupCommit();
 	    return;
 	}
 
 	if (work instanceof AddTreatmentWork) {
 	    addTreatmentCommit(((AddTreatmentWork) work).treatment);
 	    return;
 	}
 
 	if (work instanceof RemoveTreatmentWork) {
 	    RemoveTreatmentWork w = (RemoveTreatmentWork) work;
 	    removeTreatmentCommit(w.treatment, w.restartTreatments);
 	    return;
 	}
     }
 
     public String dump() {
         Collection<Treatment> c = treatments.values();
 
         Iterator<Treatment> it = c.iterator();
 
 	String s = id + "\n";
 
         while (it.hasNext()) {
             s += "  " + it.next().getId();
 	}
 
 	return s;
     }
 
     public String toString() {
 	return id;
     }
     
 }
