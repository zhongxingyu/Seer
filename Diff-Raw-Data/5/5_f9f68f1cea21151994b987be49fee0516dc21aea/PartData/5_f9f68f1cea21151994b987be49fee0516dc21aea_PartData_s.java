 package Utils;
 
 import java.io.Serializable;
 
 import factory.PartType;
 
 public class PartData implements Serializable {
 	Location loc;
 	PartType pt;
 	Location kitloc;
 	boolean quality;
 	int Arm;
 
 	public PartData(PartType pt, boolean q) {
 		this.pt = pt;
 		quality = q;
 	}
 
 	public PartData(Location l, PartType p, int arm) {
 		loc = l;
 		pt = p;
 		Arm = arm;
 	}
 
 	public PartData(Location l, int arm) {
 		kitloc = l;
 		Arm = arm;
 	}
 
 	public Location getKitLocation() {
 		return kitloc;
 	}
 
 	public Location getLocation() {
 		return loc;
 	}
 
 	public PartType getPartType() {
 		return pt;
 	}
 
 	public boolean getQuality() {
 		return quality;
 	}
 
 	public int getArm() {
 		return Arm;
 	}
 
 }
