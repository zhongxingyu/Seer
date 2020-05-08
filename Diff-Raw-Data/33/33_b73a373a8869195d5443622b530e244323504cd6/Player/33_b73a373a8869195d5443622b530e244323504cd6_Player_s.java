 package org.carnegiesciencecenter.buhl;
 
 import java.util.ArrayList;
 
 public class Player extends AbstractDevice {
 	
 	static int DEFAULT_SPEED = 30;
 	
 	public PlayerStatus getStatus() {
 		return (PlayerStatus) status;
 	}
 	
 	Player(String name, String ch, ArrayList<DeviceStatus> l) {
 		this(name, ch, l, DEFAULT_SPEED);
 	}
 	
 	Player(String name, String ch, ArrayList<DeviceStatus> l, int s) {
 		super(name, ch, new PlayerStatus(), l);
 		type = DeviceTypes.PLAYER;
 		getStatus().speed = s;
 		getStatus().chapter = 0;
 		getStatus().position = 0;
 	}
 	
 	public void setSpeed(int n) {
 		getStatus().speed = n;
 	}
 	
 	public void searchCh(int n) {
 		getStatus().position = 0;
 		getStatus().chapter = n;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
 		DeviceManager.equivCmds.add(DsCmd.cmdGoto(getStatus().clockId, getStatus().atTime, objName(), 0));
 	}
 
 	public void search(int n) {
 		getStatus().position = n;
 		getStatus().state = DeviceState.STABLE;
 		this.recordStatus();
		DeviceManager.equivCmds.add(DsCmd.cmdGoto(getStatus().clockId, getStatus().atTime, objName(), n*100/getStatus().speed));
 	}
 
 	public void still() {
 		if (getStatus().isPlaying) {
 			getStatus().position += (getStatus().atTime - getStatus().prevTime)*getStatus().speed/100;
 			getStatus().isPlaying = false;
 			getStatus().state = DeviceState.STABLE;
 			this.recordStatus();
 			DeviceManager.equivCmds.add(DsCmd.cmdPause(getStatus().clockId, getStatus().atTime, objName()));
 		}
 		// ignore STILL if isPlaying==false
 	}
 
 	public void play() {
 		if (!getStatus().isPlaying) {
 			getStatus().isPlaying = true;
 			getStatus().state = DeviceState.STABLE;
 			this.recordStatus();
 			DeviceManager.equivCmds.add(DsCmd.cmdPlay(getStatus().clockId, getStatus().atTime, objName()));
 		}
 		// ignore PLAY if isPlaying==true
 	}
 
 	public void play(int n) {
 		if (!getStatus().isPlaying) {
 			getStatus().isPlaying = true;
 			getStatus().state = DeviceState.STABLE;
 			DeviceManager.equivCmds.add(DsCmd.cmdPlay(getStatus().clockId, getStatus().atTime, objName()));
 			this.recordStatus();
 			getStatus().atTime += (n - getStatus().position)/getStatus().speed*100; 
 			getStatus().position = n;
 			DeviceManager.equivCmds.add(DsCmd.cmdPause(getStatus().clockId, getStatus().atTime, objName()));
 			this.recordStatus();
 		}
 		// ignore PLAY if isPlaying==true
 	}
 	
 	public String objName() {
 		if (getStatus().chapter != 0)
 			return String.format("%s_%d", super.objName(), getStatus().chapter);
 		else
 			return super.objName();
 	}
 	
 }
