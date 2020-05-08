 package agent.data;
 
 import java.util.ArrayList;
 
import Utils.Constants;

 import factory.KitConfig;
 import factory.PartType;
 
 import DeviceGraphics.KitGraphics;
 
 public class Kit {
 
 	public KitGraphics kitGraphics;
 
 	public String kitID;
 
 	public KitConfig partsExpected;
 
 	public ArrayList<Part> parts = new ArrayList<Part>();
 
 	public Kit() {
 		kitGraphics = new KitGraphics(null);
		partsExpected = new KitConfig("default", Constants.DEFAULT_PARTTYPES.get(0));
 	}
 
 	public Kit(KitConfig expected) {
 		kitGraphics = new KitGraphics(null);
 		partsExpected = expected;
 	}
 
 	public Kit(String kitID) {
 		kitGraphics = new KitGraphics(null);
 		this.kitID = kitID;
 	}
 
 	public boolean needPart(Part part) {
 		int count = 0;
 		for (PartType type : partsExpected.getConfig().keySet()) {
 			if (type == part.type) {
 				count++;
 			}
 		}
 		for (Part tempPart : parts) {
 			if (tempPart.type == part.type) {
 				count--;
 			}
 		}
 		if (count > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
