 package agents;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import state.FactoryState;
 
 import agents.interfaces.*;
 import gui.GUI_Lane;
 import gui.GUI_Part;
 
 /**	LaneAgent class
 *	Responsible for:
 *		passing part requests from Nests to Feeders
 *		passing parts from Feeders to Nests
 *	
 **/
 
 
 /*** DO METHODS ***
  *  doPurge()
  *  doMakeRequest(int count)
  * 
  **/
 
 
 public class LaneAgent extends PartsMoverAgent implements Lane {
 	
 	//Constant for the number of spaces on the lane, 10
 	public final static int LANE_SIZE = 10;
 	
 	
 	
     /*** Data Structures **/
     
     GUI_Lane guiLane;					// Use public interface to update view
     public boolean jammed = false;		// True when the lane is jammed
     public boolean spaceOpened = true;	// Like ReadyToMove but for receiving
     
 	//Define events which the scheduler would need to manage
 	enum Event {stop, start, jammed, checkForPart, spaceOpened};
 	
 	private List<Event> events = Collections.synchronizedList(new ArrayList<Event>());
 	
     /*** Constructor ***/
     
     public LaneAgent(String name) {
         super (name, LANE_SIZE);
     }
 
     /*** Messages ***/
 
     /*  Message to Lane w/ a Part!
      *  Source: Feeder
      */
     public void msgHereIsPart(String p) {
     	super.msgHereIsPart(p);
     } 
     
     //Called by nest to stop and purge the lane
     public void msgStopLane()
     {
     	events.add(Event.stop);
     	stateChanged();
     }
     
     //Called by nest to start lane again after it has been purged
     public void msgStartLaneAgain()
     {
     	events.add(Event.start);
     	stateChanged();
     }
     
     //Called by nest to check if the lane has jammed
     public void msgWhereIsPart()
     {
     	events.add(Event.checkForPart);
     	stateChanged();
     }
     
     //Called to make the lane jammed
     public void msgLaneJammed()
     {
     	events.add(Event.jammed);
     	stateChanged();
     }
     
     public void msgSpaceOpened()
     {
     	events.add(Event.spaceOpened);
     	stateChanged();
     }
 
     /*** Scheduler ***/
     
     //Override scheduler to manage events
     @Override
     public boolean pickAndExecuteAnAction() {
     	
     	
     	if (super.pickAndExecuteAnAction())
     		return true;
     	
     	
     	//Process events
     	if (!events.isEmpty())
     	{
     		Event e = events.remove(0);
     		if (e == Event.start)
 			{
 				print("Starting lane");
 				FactoryState.out.println("Starting lane");
 				if (!jammed && !readyToMove)
     				guiLane.turnOnLane();
 			}
     		else if (e == Event.stop)
 			{
 				print("Stopping lane");
 				FactoryState.out.println("Stopping lane");
 				guiLane.turnOffLane();
 				//Tell the nest that it was stopped
 				((Nest)receiver).msgDoStoppedLane();
 				
 			}
     		else if (e == Event.jammed)
 			{
     			print("Jamming");
 				FactoryState.out.println("Lane jammed");
 				jammed = true;
 				guiLane.turnOffLane();
 			}
     		else if (e == Event.checkForPart)
 			{
 				if (jammed)
 				{
 					print("Lane was jammed, fixing");
 					FactoryState.out.println("Fixing lane jam with extra water pressure");
 					guiLane.DoMakeLaneFaster();
 					//Fix the jam
 					jammed = false;
 					if (!jammed && !readyToMove)
 						guiLane.turnOnLane();
 				}
				else if (!guiLane.checkLane() && holding > 0)
				{
					print ("I was off, turning back on");
					//Lane was turned off lol
					if (!readyToMove)
						guiLane.turnOnLane();
				}
 				else
 				{
 					print("Lane is fine, asking feeder what's up");
 					//Ask the feeder where the part is
 					((Feeder)supplier).msgWhereIsPart();
 				}
 			}
     		else if (e == Event.spaceOpened)
     		{
     			spaceOpened = true;
         		supplier.msgRequestParts(currentPart, 0, 1, this);
     		}
     		
     		return true;
     	}
     	
 
 //        }// synch events
 //        }// synch pending reqs
     	
     	return false;
     }
     
 
     /*** Actions ***/
     
     @Override
     protected void doFill(Request r) {
     	super.doFill(r);
     	if (holding == 0) spaceOpened = true;
     }
     
     //Eliminate all parts on the lane so we can get a new type of part 
     protected void doPurge()
     {
     	super.doPurge();
     	jammed = false;		//Jams are automatically fixed by purging
     }
 
 	@Override
 	public void partRemoved() {
 		guiLane.removePart(0);
 		if (holding > 0 && !jammed)
 			guiLane.turnOnLane();
 	}
 
 	@Override
 	public void partAdded() {
 		guiLane.notifyLane(new GUI_Part(new Part(currentPart)));
 		if (!jammed) guiLane.turnOnLane();
 		
 		spaceOpened = false;
 		
 		//Cannot get a new part until a space is open
 		supplier.msgRequestParts(currentPart, 0, 0, this);
 		
 	}
 
 	@Override
 	public void partsCleared() {
 		guiLane.clear();
 	}
     
 	public void setguiLane(GUI_Lane guiLane)
 	{
 		this.guiLane = guiLane;
 		guiLane.turnOffLane();	//Start off
 	}
 	
 	protected int getOpenSpaces()
 	{
 		if (spaceOpened)
 			return 1;
 		else return 0;
 			
 	}
 	
 }
