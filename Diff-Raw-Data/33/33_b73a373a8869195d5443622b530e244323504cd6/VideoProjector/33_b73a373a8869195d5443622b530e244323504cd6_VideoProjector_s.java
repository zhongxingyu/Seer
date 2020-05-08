 /**
  * 
  */
 package org.carnegiesciencecenter.buhl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author Anh Le
  *
  */
 public class VideoProjector extends Projector {
 
 	static private HashMap<Integer,AbstractDevice> sources = null;
 
 	public VideoProjectorStatus getStatus() {
 		return (VideoProjectorStatus) status;
 	}
 
 	VideoProjector(String name, String ch, ArrayList<DeviceStatus> l) {
 		super(name, ch, new VideoProjectorStatus(), l);
 		type = DeviceTypes.VIDEO_PROJECTOR;
 		if (sources == null)
 			sources = new HashMap<Integer,AbstractDevice>();
 	}
 	
 	static void addSource(int id, AbstractDevice d) {
 		sources.put(id, d);
 	}
 
 	void selectSource(int id) {
 		getStatus().sourceId = id;
 		getStatus().currentSource = sources.get(id);
 		sources.get(id).getStatus().linkedDevice = this;
 	}
 
 	public void fade(int T, int n) {
 		getStatus().state = DeviceState.BEGIN_TRANSITION;
 		this.recordStatus();
 		getStatus().brightness = n;
 
 		if (n != 0) {
 			if (getStatus().currentSource.type == DeviceTypes.INTERACTIVE) {
 				if (((InterStatus) getStatus().currentSource.getStatus()).usingSVID) {
 					DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
 						"AVStream.LIVE:"+objName()));
 				}
 				else {
 					DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
 							DEFAULT_IMAGE_PATH+objName()+DEFAULT_IMAGE_EXT));
 				}
 			}
 			else if (getStatus().currentSource.type == DeviceTypes.PLAYER) { 
 				DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
 						DEFAULT_VIDEO_PATH+objName()+DEFAULT_VIDEO_EXT));
 				DeviceManager.equivCmds.add(DsCmd.cmdGoto(getStatus().clockId, getStatus().atTime, objName(), 
						((Player)getStatus().currentSource).getStatus().position));
 			}
 			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
 					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
 		}
 		DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
 
 		getStatus().atTime += T;
 		
 		if (n == 0)
 			DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));
 
 		getStatus().state = DeviceState.END_TRANSITION;
 		this.recordStatus();
 }
 	
 	public void alt(int T, int n) {
 		getStatus().state = DeviceState.BEGIN_TRANSITION;
 		this.recordStatus();
 		if (getStatus().brightness == 0) {
 			getStatus().brightness = n;
 
 			if (getStatus().currentSource.type == DeviceTypes.INTERACTIVE) {
 				if (((InterStatus) getStatus().currentSource.getStatus()).usingSVID) {
 					DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
 						"AVStream.LIVE:"+objName()));
 				}
 				else {
 					DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
 							DEFAULT_IMAGE_PATH+objName()+DEFAULT_IMAGE_EXT));
 				}
 			}
 			else if (getStatus().currentSource.type == DeviceTypes.PLAYER) { 
 				DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
 						DEFAULT_VIDEO_PATH+objName()+DEFAULT_VIDEO_EXT));
 				DeviceManager.equivCmds.add(DsCmd.cmdGoto(getStatus().clockId, getStatus().atTime, objName(), 
						((Player)getStatus().currentSource).getStatus().position));
 			}
 			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
 					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
 			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
 		}
 		else {
 			getStatus().brightness = 0;
 
 			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, 0));
 			DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime + T, objName()));
 		}
 		getStatus().atTime += T;
 		getStatus().state = DeviceState.END_TRANSITION;
 		this.recordStatus();
 	}
 	
 	public void fanOn() {
 		getStatus().isFanRunning = true;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
 	}
 	
 	public void fanOff() {
 		getStatus().isFanRunning = false;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
 	}
 	
 	public void contrast(int n) {
 		getStatus().contrast = n;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
 	}
 	
 	public String objName() {
 		if (getStatus().currentSource != null)
 			return getStatus().currentSource.objName();
 		else {
 			System.out.println("Unknown source for " + getStatus().deviceName + "-" + getStatus().channelName);
 			return "ERROR";
 		}
 	}
 
 }
