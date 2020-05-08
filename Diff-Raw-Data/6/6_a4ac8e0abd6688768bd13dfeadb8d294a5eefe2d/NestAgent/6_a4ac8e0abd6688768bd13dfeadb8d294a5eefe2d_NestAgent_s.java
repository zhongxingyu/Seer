 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import DeviceGraphics.DeviceGraphics;
 import GraphicsInterfaces.NestGraphics;
 import Utils.Constants;
 import agent.data.Part;
 import factory.PartType;
 import agent.interfaces.Nest;
 
 /**
  * Nests hold parts
  * @author Arjun Bhargava
  */
 public class NestAgent extends Agent implements Nest {
 
 	public List<PartType> requestList = Collections.synchronizedList(new ArrayList<PartType>());
 	PartType currentPartType;
 	public List<MyPart> currentParts = Collections.synchronizedList(new ArrayList<MyPart>());
 	public int count = 0;
 	public int countRequest = 0;
 	int full = 9;
 	public boolean takingParts = false;
 	
 	public NestGraphics nestGraphics;
 
 	public Semaphore animation = new Semaphore(0, true);
 
 	String name;
 
 	LaneAgent lane;
 	CameraAgent camera;
 
 	public class MyPart {
 		Part part;
 		NestStatus status;
 
 		public MyPart(Part p) {
 			part = p;
 			status = NestStatus.IN_NEST;
 		}
 	}
 
 	public enum NestStatus {
 		IN_NEST, IN_NEST_POSITION
 	};
 
 	public NestAgent(String name) {
 		super();
 
 		this.name = name;
 	}
 
 	// MESSAGES
 	@Override
 	public void msgHereIsPartType(PartType type) {
 		print("Received msgHereIsPartType");
 		if(currentPartType != type) {
 			if(nestGraphics != null) { nestGraphics.purge(); }
 			 
 			currentPartType = type;
 			countRequest = 0;
 			count = 0;
 			requestList.clear();
 			requestList.add(type);
 			stateChanged();
 		}
 	}
 
 	@Override
 	public void msgHereIsPart(Part p) {
 		print("Received msgHereIsPart");
 		count++;
 		currentParts.add(new MyPart(p));
 		stateChanged();
 	}
 
 	@Override
 	public void msgTakingPart(Part p) {
 		print("Received msgTakingPart");
 		if (nestGraphics != null) {
 			nestGraphics.givePartToPartsRobot(p.partGraphics);
 		}
 		/*
 		 * try { animation.acquire(); } catch (InterruptedException e) { // TODO
 		 * Auto-generated catch block e.printStackTrace(); }
 		 */
 		synchronized(currentParts) {
 		for (MyPart part : currentParts) {
 			if (part.part.equals(p)) {
 				print("found part");
 				currentParts.remove(part);
 				return;
 			}
 		}
 		}
 		count--;
 		countRequest--;
 		stateChanged();
 	}
 
 	@Override
 	public void msgDoneTakingParts() {
 		print("Received msgDoneTakingParts");
 		takingParts = false;
 		stateChanged();
 	}
 
 	@Override
 	public void msgReceivePartDone() {
 		print("Received msgReceivePartDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgGivePartToPartsRobotDone() {
 		print("Received msgGivePartToPartsRobotDone from graphics");
 		// animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgPurgingDone() {
 		print("Received msgPurgingDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		synchronized(requestList) {
 		for (PartType requestedPart : requestList) {
 			if (countRequest < full) {
 				getParts(requestedPart);
 				return true;
 			}
 		}
 		}
 		synchronized(currentParts) {
 		for (MyPart currentPart : currentParts) {
 			if (currentPart.status == NestStatus.IN_NEST) {
 				moveToPosition(currentPart.part);
 				return true;
 			}
 		}
 		}
		if (count == full && takingParts == false) {
 			nestFull();
 			return true;
 		}
 		if (takingParts == true) {
 			updateParts();
 			return true;
 		}
 		return false;
 	}
 
 	// ACTIONS
 	@Override
 	public void getParts(PartType requestedType) {
 		print("Telling lane it need a part and incrementing count");
 		countRequest++;
 		lane.msgINeedPart(requestedType);
 		stateChanged();
 	}
 
 	@Override
 	public void moveToPosition(Part part) {
 		print("Moving part to proper nest location");
 		
 		/*if (nestGraphics != null) {
 			// TODO
 			nestGraphics.receivePart(part.partGraphics);
 		}
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		*/
 
 		for (MyPart currentPart : currentParts) {
 			if (currentPart.part == part) {
 				currentPart.status = NestStatus.IN_NEST_POSITION;
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void nestFull() {
 		print("Telling camera that this nest is full");
 		camera.msgIAmFull(this);
 		takingParts = true;
 	}
 
 	@Override
 	public void updateParts() {
 		// nestGraphics.updatePartsList();
 		/*
 		 * try { animation.acquire(); } catch (InterruptedException e) { // TODO
 		 * Auto-generated catch block e.printStackTrace(); } stateChanged();
 		 */
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setLane(LaneAgent lane) {
 		this.lane = lane;
 	}
 
 	@Override
 	public void setCamera(CameraAgent camera) {
 		this.camera = camera;
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics nest) {
 		this.nestGraphics = (NestGraphics) nest;
 	}
 
 	public List<Part> getParts() {
 		List<Part> parts = new ArrayList<Part>();
 		synchronized(currentParts) {
 		for (MyPart p : currentParts) {
 			parts.add(p.part);
 		}
 		}
 		return parts;
 	}
 
 	public ArrayList<PartType> getTypesOfParts() {
 		ArrayList<PartType> types = new ArrayList<PartType>();
 		synchronized(currentParts) {
 		for (MyPart p : currentParts) {
 			types.add(p.part.type);
 		}
 		}
 		return types;
 	}
 
 	// HACK for v0 only
 	public void FillWithParts() {
 		for (int i = 1; i < full; i++) {
 			currentParts.add(new MyPart(new Part(Constants.DEFAULT_PARTTYPES.get(0))));
 		}
 	}
 
 }
