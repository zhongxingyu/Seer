 package agent;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import agent.data.Part;
 import agent.data.PartType;
 import agent.interfaces.Lane;
 
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.PartGraphics;
 import GraphicsInterfaces.LaneGraphics;
 
 /**
  * Lane delivers parts to the nest
  * @author Arjun Bhargava
  */
 public class LaneAgent extends Agent implements Lane {
 
 	public List<PartType> requestList = new ArrayList<PartType>();
 	public List<MyPart> currentParts = new ArrayList<MyPart>();
 
 	public int currentNum = 0;
 	public int topLimit = 9;
 	public int lowerThreshold = 3;
 	
 	public LaneStatus state;
 	
 	String name;
 	
 	public Semaphore animation = new Semaphore(0, true);
 
 	public class MyPart {
 		public Part part;
 		PartStatus status;
 
 		public MyPart(Part p) {
 			part = p;
 			status = PartStatus.BEGINNING_LANE;
 		}
 	}
 
 	public enum PartStatus {
 		BEGINNING_LANE, IN_LANE, END_LANE
 	};
 	
 	public enum LaneStatus {
 		FILLING, DONE_FILLING
 	};
 
 	FeederAgent feeder;
 	NestAgent nest;
 	LaneGraphics laneGUI;
 
 	public LaneAgent(String name) {
 		super();
 
 		this.name = name;
 		state=LaneStatus.FILLING;
 	}
 
 	@Override
 	public void msgINeedPart(PartType type) {
 		print("Getting msgINeedPart");
 		requestList.add(type);
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsPart(Part p) {
 		print("I got a part");
 		currentNum++;
 		currentParts.add(new MyPart(p));
 		if(laneGUI !=null) {
 			laneGUI.receivePart(p.partGraphics);
 		}
 		/*try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/ //lane can have multiple parts moving along it at a time
 		
 		stateChanged();
 	}
 
 	@Override
 	public void msgReceivePartDone(PartGraphics part) {
 		print("received that the GUI was done with the part");
 		for(MyPart p:currentParts){
 			if(p.status==PartStatus.BEGINNING_LANE){
				p.status=PartStatus.END_LANE;
 				break;
 			}
 		}
 		//animation.release(); 
 		stateChanged();
 	}
 
 	@Override
 	public void msgGivePartToNestDone(PartGraphics part) {
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		print("In the Scheduler");
 		// TODO Auto-generated method stub
 		if(currentNum>=topLimit){
 			state=LaneStatus.DONE_FILLING;
 		}
 		else if(requestList.size()>lowerThreshold){
 			state=LaneStatus.FILLING;
 		}
 		if(state==LaneStatus.FILLING){
 			for (PartType requestedType : requestList) {
 				getParts(requestedType);
 				return true;
 			}
 		}
 		for (MyPart part : currentParts) {
 			if (part.status == PartStatus.END_LANE) {
 				giveToNest(part.part);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void getParts(PartType requestedType) {
 		print("Telling Feeder that it needs a part");
 		feeder.msgINeedPart(requestedType,this);
 		requestList.remove(requestedType);
 		stateChanged();
 	}
 
 	@Override
 	public void giveToNest(Part part) {
 		print("Giving part to Nest");
 		if(laneGUI !=null) {
 			laneGUI.givePartToNest(part.partGraphics);
 		}
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		currentNum--;
		if(nest != null) {
			nest.msgHereIsPart(part);
		}
 		for (MyPart currentPart : currentParts) {
 			if (currentPart.part == part) {
 				currentParts.remove(currentPart);
 				return;
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics lane) {
 		this.laneGUI = (LaneGraphics) lane;
 	}
 	public String getName() {
 		return name;
 	}
 
 	public void setFeeder(FeederAgent feeder) {
 		this.feeder = feeder;
 	}
 
 	public void setNest(NestAgent nest) {
 		this.nest = nest;
 	}
 
 	
 	public void thisFeederAgent(FeederAgent feeder) {
 		this.feeder = feeder;
 	}
 	
 }
