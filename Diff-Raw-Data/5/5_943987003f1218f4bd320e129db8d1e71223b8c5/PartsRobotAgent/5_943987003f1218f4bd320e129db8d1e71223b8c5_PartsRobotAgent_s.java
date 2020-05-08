 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import agent.data.Kit;
 import agent.data.Part;
 import agent.data.PartType;
 import agent.interfaces.Nest;
 import agent.interfaces.PartsRobot;
 import agent.interfaces.Stand;
 
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.PartsRobotGraphics;
 
 /**
  * Parts robot picks parts from nests and places them in kits
  * @author Ross Newman, Michael Gendotti, Daniel Paje
  */
 public class PartsRobotAgent extends Agent implements PartsRobot {
 
 	public PartsRobotAgent(String name) {
 		super();
 
 		this.name = name;
 
 		// Add arms
 		for (int i = 0; i < 4; i++) {
 			this.Arms.add(new Arm());
 		}
 	}
 
 	String name;
 
 	public class MyKit {
 		public Kit kit;
 		public MyKitStatus MKS;
 
 		public MyKit(Kit k) {
 			kit = k;
 			MKS = MyKitStatus.NotDone;
 		}
 	}
 
 	public enum MyKitStatus {
 		NotDone, Done
 	};
 
 	public class Arm {
 		Part part;
 		ArmStatus AS;
 
 		public Arm() {
 			part = null;
 			AS = ArmStatus.Empty;
 		}
 	}
 
 	private enum ArmStatus {
 		Empty, Full
 	};
 
 	private List<PartType> KitConfig = Collections
 			.synchronizedList(new ArrayList<PartType>());
 	private final List<MyKit> MyKits = Collections
 			.synchronizedList(new ArrayList<MyKit>());
 	public Map<Nest, List<Part>> GoodParts = new HashMap<Nest, List<Part>>();
 	public List<Arm> Arms = Collections.synchronizedList(new ArrayList<Arm>());
 
 	List<Kit> KitsOnStand;
 	List<Nest> nests;
 
 	Stand stand;
 	PartsRobotGraphics partsRobotGraphics;
 
 	public Semaphore animation = new Semaphore(1, true);
 
 	// public Semaphore accessKit = new Semaphore(0, true);
 
 	/***** MESSAGES ***************************************/
 	/**
 	 * Changes the configuration for the kits From FCS
 	 */
 	@Override
 	public void msgHereIsKitConfiguration(List<PartType> config) {
 		KitConfig = config;
 		stateChanged();
 	}
 
 	/**
 	 * From Camera
 	 */
 	@Override
 	public void msgHereAreGoodParts(Nest n, List<Part> goodParts) {
 		GoodParts.put(n, goodParts);
 		stateChanged();
 	}
 
 	/**
 	 * From Stand
 	 */
 	@Override
 	public void msgUseThisKit(Kit k) {
 		MyKit mk = new MyKit(k);
 		MyKits.add(mk);
 		stateChanged();
 	}
 
 	/**
 	 * Releases animation semaphore after a part is picked up, so that a new
 	 * animation may be run by GUI From GUI
 	 */
 	@Override
 	public void msgPickUpPartDone() {
 		animation.release();
 		stateChanged();
 	}
 
 	/**
 	 * Releases animation semaphore after a part is given to kit, so that a new
 	 * animation may be run by GUI From GUI
 	 */
 	@Override
 	public void msgGivePartToKitDone() {
 		animation.release();
 		stateChanged();
 	}
 
 	/**************** SCHEDULER ***********************/
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		// Checks if a kit is done and inspects it if it is
 		synchronized (MyKits) {
 			if (MyKits.size() > 0) {
 				for (MyKit mk : MyKits) {
 					if (mk.MKS == MyKitStatus.Done) {
 						RequestInspection(mk);
 						return true;
 					}
 				}
 			}
 
 			// Checks if there is an empty arm, if there is it fills it with a
 			// good part that the kit needs
 			if (IsAnyArmEmpty()) {
 				synchronized (GoodParts) {
 					for (Nest nest : GoodParts.keySet()) {
 						// Going through all the good parts
 						for (Part part : GoodParts.get(nest)) {
 							for (MyKit mk : MyKits) {
 								// Checking if the good part is needed by
 								// either kit
 								if (mk.kit.needPart(part)) {
 									print("Found a part I need");
 									for (Arm arm : Arms) {
 										if (arm.AS == ArmStatus.Empty) {
 											// Find the empty arm
 											PickUpPart(arm, part, nest);
 											return true;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Checks if any arm is holding a part and places it if there is one
 		for (Arm arm : Arms) {
 			if (arm.AS == ArmStatus.Full) {
 				PlacePart(arm);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/********** ACTIONS **************/
 
 	private void PickUpPart(Arm arm, Part part, Nest nest) {
 
 		print("Picking up part");
 		GoodParts.remove(nest);
 		arm.AS = ArmStatus.Full;
 		arm.part = part;
 
 		// Tells the graphics to pickup the part
 		// partsRobotGraphics.pickUpPart(part.part);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Only takes 1 part from a nest at a time
 		nest.msgTakingPart(part);
 		nest.msgDoneTakingParts();
 
 		stateChanged();
 	}
 
 	private void PlacePart(Arm arm) {
 		print("Placing part");
 		for (MyKit mk : MyKits) {
 			if (mk.kit.needPart(arm.part)) {
 
 				// partsRobotGraphics.givePartToKit(mk.kit.kit);
 				try {
 					animation.acquire();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// Tells the kit it has the part now
 				mk.kit.parts.add(arm.part);
 				mk.kit.kitGraphics.receivePart(arm.part.partGraphics);
 				// mk.kit.partsExpected.remove(arm.part);
 				arm.part = null;
 				arm.AS = ArmStatus.Empty;
 
 				// Checks if the kit is done
 				CheckMyKit(mk);
 
 				break;
 			}
 		}
 		stateChanged();
 	}
 
 	private void CheckMyKit(MyKit mk) {
 		print("Need " + (mk.kit.partsExpected.size() - mk.kit.parts.size())
 				+ " more part(s) to finish kit.");
 		if (mk.kit.parts.size() == mk.kit.partsExpected.size()) {
 			mk.MKS = MyKitStatus.Done;
 		}
 		// stateChanged();
 	}
 
 	private void RequestInspection(MyKit mk) {
 		print("Requesting inspection.");
 		stand.msgKitAssembled(mk.kit);
 		MyKits.remove(mk);
 		stateChanged();
 	}
 
 	// Helper methods
 
 	// Checks if any of the arms are empty
 	private boolean IsAnyArmEmpty() {
 		for (Arm a : Arms) {
 			if (a.AS == ArmStatus.Empty) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<PartType> getKitConfig() {
 		return KitConfig;
 	}
 
 	public void setKitConfig(List<PartType> kitConfig) {
 		KitConfig = kitConfig;
 	}
 
 	public Map<Nest, List<Part>> getGoodParts() {
 		return GoodParts;
 	}
 
 	public void setGoodParts(Map<Nest, List<Part>> goodParts) {
 		GoodParts = goodParts;
 	}
 
 	public List<Arm> getArms() {
 		return Arms;
 	}
 
 	public void setArms(List<Arm> arms) {
 		Arms = arms;
 	}
 
 	public List<Kit> getKitsOnStand() {
 		return KitsOnStand;
 	}
 
 	public void setKitsOnStand(List<Kit> kitsOnStand) {
 		KitsOnStand = kitsOnStand;
 	}
 
 	public List<Nest> getNests() {
 		return nests;
 	}
 
 	public void setNests(List<Nest> nests) {
 		this.nests = nests;
 	}
 
 	public Stand getStand() {
 		return stand;
 	}
 
 	public void setStand(Stand stand) {
 		this.stand = stand;
 	}
 
 	public PartsRobotGraphics getPartsrobotGraphics() {
		return partsrobotGraphics;
 	}
 
 	public void setGraphicalRepresentation(DeviceGraphics partsrobotGraphics) {
		this.partsrobotGraphics = (PartsRobotGraphics) partsrobotGraphics;
 	}
 
 	public Semaphore getAnimation() {
 		return animation;
 	}
 
 	public void setAnimation(Semaphore animation) {
 		this.animation = animation;
 	}
 
 	public List<MyKit> getMyKits() {
 		return MyKits;
 	}
 }
