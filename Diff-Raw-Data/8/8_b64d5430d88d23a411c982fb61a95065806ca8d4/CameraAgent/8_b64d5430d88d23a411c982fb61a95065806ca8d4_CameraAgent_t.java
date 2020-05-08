 package factory;
 
 import java.util.*;
 
 import factory.data.Kit;
 import factory.data.PartType;
 import factory.interfaces.Camera;
 import factory.interfaces.Nest;
 import DeviceGraphics.PartGraphics;
 import GraphicsInterfaces.CameraGraphics;
 import agent.Agent;
 
 public class CameraAgent extends Agent implements Camera {
 	private Kit kit;
 	private enum KitStatus {NOT_READY,DONE,MESSAGED};
 	private KitStatus kitState = KitStatus.NOT_READY;
 	private boolean kitDone;
 	private List<MyNest> nests = Collections.synchronizedList(new ArrayList<MyNest>());
 
 	public CameraGraphics guiCamera;
 	
 	private enum NestStatus {NOT_READY,READY,PHOTOGRAPHED};
 	
 	private class MyNest{
		NestAgent nest;
 		PartType type;
 		List<PartGraphics> guiParts;
 		NestStatus state;
		MyNest(NestAgent nest, PartType type){
 			this.nest = nest;
 			this.type = type;
 			this.state = NestStatus.NOT_READY;
 		}
 	}
 	
 	/**********MESSAGES************/
 	/**
 	 * 
 	 */
 	public void msgInspectKit(Kit kit) {
 		this.kit = kit;
 		kitState = KitStatus.NOT_READY;
 		stateChanged();		
 	}
 
 	public void msgIAmFull(NestAgent n) {
 		MyNest nest = new MyNest(n, n.currentPartType);
 		nests.add(nest);
 		stateChanged();		
 	}
 	
	public void msgTakePictureNestDone(List<GUIPart> parts, NestAgent nest) {
 		for(MyNest n: nests)
 		{
 			if(n.nest == nest)
 			{
 				n.guiParts = parts;
 				n.state = NestStatus.PHOTOGRAPHED;
 				break;
 			}
 		}
 		stateChanged();
 	}
 
 	public void msgTakePictureKitDone(boolean done) {
 		kitDone = done;
 		kitState = KitStatus.DONE;
 		stateChanged();
 	}
 	
 	/***********SCHEDULER**************/
 	public boolean pickAndExecuteAnAction() {
 		for(MyNest n: nests){
 			if(n.state == NestStatus.NOT_READY)
 			{
 				takePictureOfNest(n);
 			}
 		}
 		for(MyNest n: nests)
 		{
 			if(n.state == NestStatus.PHOTOGRAPHED)
 			{
 				tellPartsRobot(n);
 			}
 		}
 		if(kit != null && kitState == KitStatus.NOT_READY){
 			takePictureOfKit(kit);
 		}
 		if(kit != null && kitState == KitStatus.DONE){
 			tellKitRobot();
 		}
 		return false;
 	}
 
 	/***********ACTIONS*******************/
 	private void tellKitRobot() {
 		KitRobotAgent.msgKitPassedInspection(done);
 		kitState = KitStatus.MESSAGED;
 		stateChanged();
 	}
 
 	private void takePictureOfKit(Kit kit2) {
 		guiCamera.takePicture(kit);
 		stateChanged();		
 	}
 
 	private void tellPartsRobot(MyNest n) {
 		List<GUIPart> goodParts = new ArrayList<GUIPart>();
 		for(GUIPart part: n.guiParts)
 		{
 			if(part.isGood())
 			{
 				goodParts.add(part);
 			}
 		}
 		PartsRobotAgent.msgHereAreGoodParts(new Map<n.nest,goodParts>);
 		nests.remove(n);
 		stateChanged();
 		
 	}
 
 	private void takePictureOfNest(MyNest n) {
 		guiCamera.takePicture(n);
 		n.state = NestStatus.READY;
 		stateChanged();		
 	}
 
 	
 
 }
