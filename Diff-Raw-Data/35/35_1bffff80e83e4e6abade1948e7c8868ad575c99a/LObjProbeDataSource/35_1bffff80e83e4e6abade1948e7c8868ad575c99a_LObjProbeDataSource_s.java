 package org.concord.CCProbe;
 
 import waba.util.*;
 import waba.ui.*;
 import extra.io.*;
 import org.concord.waba.extra.ui.*;
 import extra.ui.*;
 import extra.util.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.waba.extra.probware.ProbManager;
 import org.concord.waba.extra.probware.CCInterfaceManager;
 import org.concord.LabBook.*;
 
 
 public class LObjProbeDataSource extends LObjSubDict
 	implements DataSource, DataListener, ProbListener
 {
 CCProb 			probe = null;
 DataListener 	calibrationListener = null;
 CCUnit		currentUnit = null;
 public 		waba.util.Vector 	dataListeners = null;
 	public waba.util.Vector probListeners = null;
 
     public LObjProbeDataSource()
     {
 		super(DataObjFactory.PROBE_DATA_SOURCE);
     }
 
     public LObjProbeDataSource(CCProb probe)
     {
 		this();
 		setProbe(probe);
     }
 
     public LabObjectView getView(ViewContainer vc, boolean edit,LObjDictionary curDict){
     	return null;
     }
     
     public LabObjectView getPropertyView(ViewContainer vc, boolean edit,LObjDictionary curDict){
     	return null;
     }
 
 	public void addDataListener(DataListener l){
 		if(dataListeners == null) dataListeners = new waba.util.Vector();
 		if(dataListeners.find(l) < 0) dataListeners.add(l);
 	}
 	public void removeDataListener(DataListener l){
 		int index = dataListeners.find(l);
 		if(index >= 0) dataListeners.del(index);
 	}
 
 	public String getLabel()
 	{
 		if(probe != null) return probe.getLabel();
 		else return null;		
 	}
 
 	public String getSummary()
 	{
 		String summary;
 
 		CCProb p = getProbe();
 
 		summary = p.getName() + "(";
 		PropObject [] props = p.getProperties();
 		int i;
 		for(i=0; i < props.length-1; i++){
 			summary += props[i].getName() + "- " + props[i].getValue() + "; ";
 		}
 		summary += props[i].getName() + "- " + props[i].getValue() + ")";
 
 		return summary;
 	}
 
 
 	public CCUnit 	getUnit()
 	{
 		if(probe != null) return CCUnit.getUnit(probe.getUnit());
 		else return null;
 	}
 	public boolean 	setUnit(CCUnit unit){
 		boolean retValue = false;
 		if(probe == null || unit == null) return retValue;
 		retValue = probe.setUnit(unit.code);
 		if(retValue){
 			currentUnit = CCUnit.getUnit(probe.getUnit());
 		}
 		return retValue;
 	}
 
 	public void startDataDelivery(){
 		if(probe == null) return;
 		ProbManager pb = ProbManager.getProbManager(probe.getInterfaceType());

 		if(pb != null){
 			pb.start();
 		}
 	}
 	
 	public void stopDataDelivery(){
 		if(probe == null) return;
 		ProbManager pb = ProbManager.getProbManager(probe.getInterfaceType());
 		if(pb != null){
 			pb.stop();
 		}
 	}
 
 	
 	public void setCalibrationListener(DataListener calibrationListener){
 		if(probe != null){
 			this.calibrationListener = calibrationListener;
 			if(calibrationListener != null)
 				probe.setCalibrationListener(this);
 			else
 				probe.setCalibrationListener(null);
 		}else{
 			calibrationListener = null;
 		}
 	}
 	
 	public void clearCalibrationListener(){
 		setCalibrationListener(null);
 	}
 
 	public CCProb 	getProbe(){return probe;}
 	public void		setProbe(CCProb probe){
 		unRegisterProbeWithPM();
 		if(this.probe != null){
 			this.probe.removeDataListener(this);
 			this.probe.removeProbListener(this);
 		}
 		this.probe = probe;
 		setUnit();
 		name = (probe == null)?null:probe.getName();
 		if(this.probe != null){
 			this.probe.addProbListener(this);
 		}
 		registerProbeWithPM();
 	}
 	
 	public void unRegisterProbeWithPM(){
 		if(probe == null) return;
 		ProbManager pb = ProbManager.getProbManager(probe.getInterfaceType());
 		pb.unRegisterProb(probe);
 	}
 	public void registerProbeWithPM(){
 		if(probe == null) return;
 		ProbManager pb = ProbManager.getProbManager(probe.getInterfaceType());
 		if(pb == null) return;
 		pb.registerProb(probe);
 		pb.addDataListenerToProb(probe.getName(),this);
 	}
 	
 	public void addProbManagerListener(ProbManagerListener l){
 		ProbManager pb = ProbManager.getProbManager(probe.getInterfaceType());
 		if(pb != null) pb.addProbManagerListener(l);
 	}
 	
 	public void closeEverything(){
 		ProbManager pb = ProbManager.getProbManager(probe.getInterfaceType());
 		if(pb != null){
 			pb.dispose();
 			pb = null;
 		}
 	}
 
 	void setUnit(){
 		if(probe == null){
 			currentUnit = null;
 			return;
 		}
 		currentUnit = CCUnit.getUnit(probe.getUnit());
 	}
 
 	public void notifyDataListenersEvent(DataEvent e){
 		if(calibrationListener != null){
 			calibrationListener.dataStreamEvent(e);
 		}else{
 			if(dataListeners == null) return;
 			for(int i = 0; i < dataListeners.getCount(); i++){
 				DataListener l = (DataListener)dataListeners.get(i);
 				l.dataStreamEvent(e);
 			}
 		}
 	}
 
 	public void notifyDataListenersReceived(DataEvent e){
 		if(calibrationListener != null){
 			calibrationListener.dataReceived(e);
 		}else{
 			if(dataListeners == null) return;
 			for(int i = 0; i < dataListeners.getCount(); i++){
 				DataListener l = (DataListener)dataListeners.get(i);
 				l.dataReceived(e);
 			}
 		}
 	}
 
 	public void addProbListener(ProbListener l){
 		if(probListeners == null) probListeners = new waba.util.Vector();
 		if(probListeners.find(l) < 0) probListeners.add(l);
 	}
 	public void removeProbListener(ProbListener l){
 		int index = probListeners.find(l);
 		if(index >= 0) probListeners.del(index);
 	}
 	public void notifyProbListeners(ProbEvent e){
 		if(probListeners == null) return;
 		for(int i = 0; i < probListeners.getCount(); i++){
 			ProbListener l = (ProbListener)probListeners.get(i);
 			l.probChanged(e);
 		}
 	}
 	
 	public void dataReceived(DataEvent dataEvent){
 		notifyDataListenersReceived(dataEvent);
 	}
 
 	public void dataStreamEvent(DataEvent dataEvent){
 		notifyDataListenersEvent(dataEvent);
 	}
 
     public void probChanged(ProbEvent e){
     	notifyProbListeners(e);
     }
 
     public void writeExternal(extra.io.DataStream out)
     {
 		super.writeExternal(out);
     	ProbFactory.storeProbeToStream(probe,out);
     }
 
     public void readExternal(extra.io.DataStream in)
     {
 		super.readExternal(in);
     	CCProb probe = ProbFactory.createProbeFromStream(in);
 		setProbe(probe);
     }
 
 	public void calibrateMe(ExtraMainWindow owner,DialogListener l){
 		if(probe == null) return;
 		probe.calibrateMe(owner,l,probe.getInterfaceType());
 	}
 	
	
 	public static LObjProbeDataSource getProbeDataSource(int probeID,int interfaceType, int interfacePort){
 		CCProb p = ProbFactory.createProb(probeID,interfacePort);
 		if(p == null) return null;
 		p.setInterfaceType(interfaceType);
 		LObjProbeDataSource me = DataObjFactory.createProbeDataSource();
 		me.setProbe(p);
 		return me;
 	}
 
 }
 
