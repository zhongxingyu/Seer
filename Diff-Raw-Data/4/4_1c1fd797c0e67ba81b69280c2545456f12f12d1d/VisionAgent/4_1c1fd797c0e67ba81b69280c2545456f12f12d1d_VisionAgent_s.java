 package factory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import agent.Agent;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 
 import factory.Kit.KitState;
 import factory.interfaces.*;
 import factory.masterControl.MasterControl;
 
 public class VisionAgent extends Agent implements Vision {
 
 	public enum KitPicRequestState { NEED_TO_INSPECT, INSPECTED }
 	public enum PictureRequestState { NESTS_READY, ASKED_PARTS_ROBOT, PARTS_ROBOT_CLEAR, PICTURE_TAKEN }
 
 	public ArrayList<PictureRequest> picRequests = new ArrayList<PictureRequest>(); 
 	public ArrayList<KitPicRequest> kitPicRequests = new ArrayList<KitPicRequest>();     
 	public PartsRobot partsRobot;
 	public Stand stand;
 	public Random r = new Random();
 	public Semaphore pictureAllowed = new Semaphore(1);
 	public ArrayList<Nest> nests;
 	public boolean isUnitTesting = false;
 	
 	public Feeder feeder0;
 	public Feeder feeder1;
 	public Feeder feeder2;
 	public Feeder feeder3;
 	
 	public VisionAgent(PartsRobot partsRobot, Stand stand, MasterControl mc){
 		super(mc);
 		this.partsRobot = partsRobot;
 		this.stand = stand;
		this.feeder0 = f0;
		this.feeder1 = f1;
		this.feeder2 = f2;
		this.feeder3 = f3;
 	}
 
 	public class KitPicRequest {
 
 		public KitPicRequestState state;
 		public boolean forceFail = false;
 
 		public KitPicRequest(KitPicRequestState kprs, boolean forceFail) { 
 			state = kprs;
 			this.forceFail = forceFail;
 		}
 	}
 
 	public class PictureRequest {
 
 		public Nest nestOne;
 		public Part nestOnePart;
 		public Part nestTwoPart;
 		public Nest nestTwo;
 		public PictureRequestState state;
 		public Feeder feeder;
 
 		public PictureRequest(Nest nestOne, Part nestOnePart, Nest nestTwo, Part nestTwoPart, Feeder feeder){
 			this.state = PictureRequestState.NESTS_READY;
 			this.nestOne = nestOne;
 			this.nestOnePart = nestOnePart;
 			this.nestOnePart = nestOnePart;
 			this.nestTwo = nestTwo;
 			this.feeder = feeder;
 		}
 	} 
 
 
 
 
 	// *** MESSAGES ***
 
 	public void msgNewNestConfig( ArrayList<Nest> nests){
 		this.nests = nests;
 	}
 
 	public void msgMyNestsReadyForPicture(Nest nestOne, Part nestOnePart, Nest nestTwo, Part nestTwoPart, Feeder feeder) {
 		if(!isUnitTesting)
 		debug("msgMyNestsReadyForPicture("+nestOne.getPart().name+","+nestTwo.getPart().name+")");
 		picRequests.add(new PictureRequest(nestOne, nestOnePart, nestTwo,  nestOnePart, feeder));
 		this.stateChanged();
 	}
 
 	public void msgVisionClearForPictureInNests(Nest nestOne, Nest nestTwo) {
 		for( PictureRequest pr: picRequests) {
 			if(pr.nestOne == nestOne && pr.nestTwo == nestTwo){
 				pr.state = PictureRequestState.PARTS_ROBOT_CLEAR;
 			}
 		}
 		this.stateChanged();
 	}
 	
 	/**
 	 * This is a message from the swing panel to force the nest to have no good parts
 	 * @param nestNum The number of the nest that has no good parts
 	 */
 	public void msgNoGoodPartsFound(int nestNum){
 		if (nestNum == 0 || nestNum == 1){
 			feeder0.msgBadNest(nestNum % 2);
 		}
 		if (nestNum == 2 || nestNum == 3){
 			feeder1.msgBadNest(nestNum % 2);
 		}
 		if (nestNum == 4 || nestNum == 5){
 			feeder2.msgBadNest(nestNum % 2);
 		}
 		if (nestNum == 6 || nestNum == 7){
 			feeder3.msgBadNest(nestNum % 2);
 		}
 	}
 
 
 	//the following message existed in the wiki, but the parameter is different.  It takes a timer rather than feeder
 	//Yeah, that was my fault.  It is no longer needed.
 	//	@Override
 	//	public void msgMyNestsReadyForPicture(Nest nest, Nest nest2, TimerTask timerTask) {
 	//		// TODO Auto-generated method stub
 	//		
 	//	}
 
 
 	public void msgAnalyzeKitAtInspection(Kit kit) {
 		debug("Received msgAnalyzeKitAtInspection() from the kit robot.");
 		kitPicRequests.add(new KitPicRequest(KitPicRequestState.NEED_TO_INSPECT, kit.forceFail));
 		stateChanged();
 	}
 
 
 	// *** SCHEDULER ***
 	public boolean pickAndExecuteAnAction() {
 
 		for(PictureRequest pr: picRequests){
 			if(pr.state == PictureRequestState.PARTS_ROBOT_CLEAR){
 				Nest one = this.nests.get(this.nests.indexOf(pr.nestOne));
 				Nest two = this.nests.get(this.nests.indexOf(pr.nestTwo));
 
 				if(one.getPart().name == pr.nestOne.getPart().name && two.getPart().name == pr.nestTwo.getPart().name){
 					takePicture(pr);
 				}
 				else {
 					debug("#################");
 					debug("#################");
 					debug("#################");
 					debug("WRONG PICTURE!!!");
 					debug("#################");
 					debug("#################");
 					debug("#################");
 				}
 				return true;
 			}
 		}
 
 		for(PictureRequest pr: picRequests){
 			if(pr.state == PictureRequestState.NESTS_READY){
 				checkLineOfSight(pr);
 				return true;
 			}
 		}
 
 		for(KitPicRequest k: kitPicRequests){
 			if(k.state == KitPicRequestState.NEED_TO_INSPECT){
 				inspectKit(k);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	// *** ACTIONS ***
 	private void inspectKit(KitPicRequest k) {
 
 		try{
 			pictureAllowed.acquire();
 		}
 		catch(Exception ex){
 
 		}
 
 		if (!isUnitTesting){
 			DoTakePicture();
 		}
 		if (k.forceFail == true)
 			stand.msgResultsOfKitAtInspection(KitState.FAILED_INSPECTION);
 		else
 			stand.msgResultsOfKitAtInspection(KitState.PASSED_INSPECTION);
 		/*
 		int randomNum = r.nextInt(11);
 		if(randomNum < 4)
 			stand.msgResultsOfKitAtInspection(KitState.PASSED_INSPECTION);
 		else
 			stand.msgResultsOfKitAtInspection(KitState.PASSED_INSPECTION);
 			*/
 
 		pictureAllowed.release();
 
 		k.state = KitPicRequestState.INSPECTED;
 	}
 
 
 	private void takePicture(PictureRequest pr){
 		try{
 			pictureAllowed.acquire();
 			DoAnimationTakePictureOfNest(pr.nestOne);
 
 			partsRobot.msgPictureTaken(pr.nestOne, pr.nestTwo);
 			if(true) {
 				partsRobot.msgHereArePartCoordinatesForNest(pr.nestOne, pr.nestOne.getPart(), r.nextInt(9));
 			}
 
 			if(true) {
 
 				partsRobot.msgHereArePartCoordinatesForNest(pr.nestTwo, pr.nestTwo.getPart(), r.nextInt(9));
 			}
 
 			picRequests.remove(pr);
 			pictureAllowed.release();
 			stateChanged();
 		}
 		catch(Exception ex){}
 	}
 
 	private void DoTakePicture() {
 		debug("Executing DoTakePicture()");
 		server.command("va kam cmd takepictureofinspection");
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 
 	}
 	private void DoAnimationTakePictureOfNest(Nest nest) {
 
 		debug("Executing DoAnimationTakePicture()");
 		server.command("va lm cmd takepictureofnest " + nest.getPosition()/2);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 	private void checkLineOfSight(PictureRequest pr){
 		debug("Executing checkLineOfSight()");
 		partsRobot.msgClearLineOfSight(pr.nestOne, pr.nestTwo);
 		pr.state = PictureRequestState.ASKED_PARTS_ROBOT;
 	}
 
 
 	public void setFeeder(Feeder feeder, int feederNum){
 		if (feederNum == 0){
 			this.feeder0 = feeder;
 		}
 		if (feederNum == 1){
 			this.feeder1 = feeder;
 		}
 		if (feederNum == 2){
 			this.feeder2 = feeder;
 		}
 		if (feederNum == 3){
 			this.feeder3 = feeder;
 		}
 	}
 
 	public void setPartsRobot(PartsRobot pr) {
 		this.partsRobot = pr;
 	}
 }
