 package engine.agent.Luis;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 
public class Truck_Agent_LV extends Agent {
 	
 	String name;
 	ConveyorFamily previousFamily;
 	Transducer t;
 	List<Glass> truckGlass;
 	enum TruckState{PARKED,COMMUTING,ARRIVED,LOADING};
 	Semaphore drivingSemaphore = new Semaphore(0,true);
 	TruckState state;
 	
	Truck_Agent_LV(String n)
 	{
 		name = n;
 		truckGlass = new ArrayList<Glass>();
 		state = TruckState.ARRIVED;
 	}
 	
 	public void setInteractions(ConveyorFamily cf, Transducer trans)
 	{
 		previousFamily = cf;		
 		t = trans;
 		t.register(this, TChannel.TRUCK);
 	}
 	
 	//Messages
 	
 	public void msgTakeAwayGlass(Glass glass)
 	{
 		truckGlass.add(glass);
 		stateChanged();
 	}
 	
 	//Scheduler
 	public boolean pickAndExecuteAnAction() {
 
 		if(state == TruckState.ARRIVED)
 		{
 			tellConveyorIAmFree();
 			return true;
 		}
 		
 		if(state == TruckState.LOADING)
 		{
 			for(Glass g : truckGlass)
 			{
 				moveGlass(g);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 
 		if(channel == TChannel.TRUCK)
 		{
 			if(event == TEvent.TRUCK_GUI_LOAD_FINISHED)
 			{
 				state = TruckState.LOADING;
 			}
 			else if(event == TEvent.TRUCK_GUI_EMPTY_FINISHED)
 			{
 				state = TruckState.ARRIVED;
 			}
 		}
 		
 		stateChanged();
 		
 	}
 	
 	//Actions
 	public void tellConveyorIAmFree()
 	{
 		print("Letting conveyor know I can load glass");
 		truckGlass.remove(0);
 		previousFamily.msgIAmFree();
 		state = TruckState.PARKED;
 	}
 	
 	public void moveGlass(Glass g)
 	{
 		print("Delivering Glass");
 		state = TruckState.COMMUTING;
 		
 	}
 
 }
