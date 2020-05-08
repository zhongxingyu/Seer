 package factory;
 
 import java.util.concurrent.Semaphore;
 
 import factory.interfaces.*;
 import agent.Agent;
 
 import factory.graphics.FrameKitAssemblyManager;
 
 enum KitRobotAgentState { DOING_NOTHING, NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_ONE, 
 	NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_TWO, NEEDS_TO_PROCESS_KIT_AT_INSPECTION_SLOT, GRABING_EMPTY_KIT_AND_PLACING_IN_SLOT_ONE,
 	GRABING_EMPTY_KIT_AND_PLACING_IN_SLOT_TWO }
 
 public class KitRobotAgent extends Agent implements KitRobot {
 
 	/** DATA **/
 	
 	StandAgent stand;
 	FrameKitAssemblyManager server;
 	KitRobotAgentState state;
 	
 	Semaphore animation = new Semaphore(0);
 	
 	public KitRobotAgent(StandAgent stand, FrameKitAssemblyManager server){
 		state = KitRobotAgentState.DOING_NOTHING;
 		this.stand = stand;
 		this.server = server;
 	}
 	
 	/** MESSAGES **/
 	
 	/**
 	 * Message sent from the StandAgent when we need to grab an empty kit and place it in a slot
 	 */
 	public void msgGrabAndBringEmptyKitFromConveyorToSlot(String slot) {
 		debug(" received msgGrabAndBringEmptyKitFromConveyorToSlot("+slot+") from server");
 		if(slot == "topSlot"){
 			state = KitRobotAgentState.NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_ONE;
 		}
 		else if(slot == "bottomSlot"){
 			state = KitRobotAgentState.NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_TWO;
 		}
 		stateChanged();
 	}
 
 	/**
 	 * Message from the server when the animation is done
 	 */
 	public void msgAnimationDone(){
 		debug(" received msgAnimationDone() from server");
 		animation.release();
 	}
 	
 	@Override
 	public void msgComeMoveKitToInspectionSlot(String slot) {
 		if(slot == "topSlot"){
 			state = KitRobotAgentState.NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_ONE;
 		}
 		else if(slot == "bottomSlot"){
 			state = KitRobotAgentState.NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_TWO;
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgComeProcessAnalyzedKitAtInspectionSlot() {
 		state = KitRobotAgentState.NEEDS_TO_PROCESS_KIT_AT_INSPECTION_SLOT;
 	}
 	
 
 	/** SCHEDULER **/
 	protected boolean pickAndExecuteAnAction() {
 		
 		synchronized(state){
 
 			/**
 			 * If there is an empty kit at conveyor that needs to be placed at slot one
 			 */
 			if(state == KitRobotAgentState.NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_ONE){
 				DoGrabEmptyKitAndPlaceInSlotOne();
 				return true;
 			}
 
 			/**
 			 * If there is an empty kit at conveyor that needs to be placed at slot two
 			 */
 			if(state == KitRobotAgentState.NEEDS_TO_GRAB_EMPTY_KIT_AND_PLACE_IN_SLOT_TWO){
 				DoGrabEmptyKitAndPlaceInSlotTwo();
 				return true;
 			}
 			
 			
 		}
 		return false;
 	}
 	
 	/** ACTIONS **/
 
 	public void DoGrabEmptyKitAndPlaceInSlotOne(){
 		debug(" executing DoGrabEmptyKitAndPlaceInSlotOne()");
 		
 		// Tell server to do animation of moving empty kit from conveyor to the topSlot of the stand
		server.moveEmptyKitToSlot(1);
 
 		// Update the state of the Kit Robot
 		this.state = KitRobotAgentState.GRABING_EMPTY_KIT_AND_PLACING_IN_SLOT_ONE;
 		
 		// Wait until the animation is done
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		// Put an empty kit in the topSlot of the stand
 		stand.topSlot.kit = new Kit();
 		
 		// Update the state of the topSlot of the stand
 		stand.topSlot.state = MySlotState.EMPTY_KIT_JUST_PLACED;
 	}
 	
 	public void DoGrabEmptyKitAndPlaceInSlotTwo(){
 		server.moveEmptyKitToSlot(2);
 		state = KitRobotAgentState.GRABING_EMPTY_KIT_AND_PLACING_IN_SLOT_TWO;
 	}
 
 
 	/** ANIMATIONS **/
 
 	
 }
 
