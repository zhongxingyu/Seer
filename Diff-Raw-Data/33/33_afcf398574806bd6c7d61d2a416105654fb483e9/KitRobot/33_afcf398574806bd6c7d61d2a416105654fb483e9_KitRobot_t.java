 package agents.interfaces;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.GUI_KitStand;
 
 import java.util.*;
 import java.util.concurrent.Semaphore; 
 
 public interface KitRobot {
     public abstract void msgRequestMoveKit();
     public abstract void msgHeresKits(List<Kit> kits);
     public abstract void msgKitIsDone();
 	public abstract Semaphore visionLock();
 	public abstract GUI_KitStand getGUIKitStand();
 	//public abstract void msgHeresKits(Kit k, boolean sendingTwo);
	public abstract void reduceNumRequests();
 
 }
