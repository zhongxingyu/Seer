 package engine.JaniceCF.agent;
 
 import java.util.*;
 
 import transducer.*;
 import engine.JaniceCF.interfaces.*;
 import engine.agent.Agent;
 import engine.agent.shared.Glass;
 import engine.agent.shared.Interfaces.ConveyorFamily;
 import engine.agent.shared.Interfaces.Machine;
 import engine.ryanCF.interfaces.Bin;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 	
 	public enum ConveyorStatus {Nothing, GlassAtEnd, Requesting}
 	
 	boolean nextFree;
 	ConveyorStatus status;
 	
 	int conveyorIndex;
 	
 	Sensor begin;
 	Sensor end;
 	
 	Popup popup;
 	Machine machine;
 	
 	//ArrayList<Glass> glassList = new ArrayList<Glass>();
 	List<Glass> glassList = Collections.synchronizedList(new ArrayList<Glass>());
 	
 	public ConveyorAgent(String name, Transducer transducer, int index) {
 		super(name, transducer);
 		
 		transducer.register(this, TChannel.CONVEYOR);
 		
 		conveyorIndex = index;
 		
 		nextFree = true;
 		status = ConveyorStatus.Nothing;
 		
 		begin = new SensorAgent(name + " BeginSensor", transducer, this, conveyorIndex*2);
 		end = new SensorAgent(name + " EndSensor", transducer, this, (conveyorIndex*2) + 1);
 		
 		popup = null;
 		machine = null;
 	}
 	
 	public void msgGlassAtEnd() {
 		status = ConveyorStatus.GlassAtEnd;
 		stateChanged();
 	}
 	
 	@Override
 	public void msgSpaceAvailable() {
 		print("Received msgSpaceAvailable");
 		if (nextFree != false) {
 			System.err.println(name + ": nextFree should not be true at this point. ");
 		}
 //		if (glassList.size() != 0) {
 //			status = ConveyorStatus.GlassAtEnd;
 //		} else {
 //			status = ConveyorStatus.Nothing;
 //		}
 		nextFree = true;
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsGlass(Glass g) {
 		print("Received msgHereIsGlass");
 		synchronized (glassList) {
 			glassList.add(g);
 		} 
 		stateChanged();
 	}
 	
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		
 		if (status == ConveyorStatus.GlassAtEnd) {
 			if (popup != null) {
 				if (nextFree == true) {
 					passToPopup();
 					return true;
 				} else {
 					requestSpace();
 					return true;
 				}
 			}
 			
 			if (machine != null) {
 				if (nextFree == true) {
 					passToMachine();
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (channel == TChannel.SENSOR) {
 			if (event == TEvent.SENSOR_GUI_RELEASED) {
 				Integer[] newArgs = new Integer[1];
 				if (((Integer)args[0] % 2) == 0) {
					if (args[0].equals((conveyorIndex*2) + 1)) {	//end sensor
 						if (glassList.size() != 0 && nextFree == true) {
							//print("Released. Starting Conveyor. ");
 							newArgs[0] = (Integer) args[0] / 2;
 							//						newArgs[0] = (Integer) sensorIndex;
 							//					conveyor.msgGlassAtEnd();
							//transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
 						} else {
 							print("Released. Nothing on Conveyor.");
 							status = ConveyorStatus.Nothing;
 							stateChanged();
 						}
 					}
 				}
 			}
 		}
 
 	}
 	
 	//Actions
 	private void passToPopup() {
 		print("Passing to Popup");
 		Integer[] args = new Integer[1];
 		args[0] = (Integer)conveyorIndex;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
 		nextFree = false;
 		Glass g = glassList.get(0);
 		popup.msgHereIsGlass(g);
 		synchronized (glassList) {
 			glassList.remove(g);
 		} 
 		status = ConveyorStatus.Nothing;
 		stateChanged();
 	}
 	
 	private void passToMachine() {
 		print("Passing to Machine");
 		Integer[] args = new Integer[1];
 		args[0] = (Integer)conveyorIndex;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
 		nextFree = false;
 		Glass g = glassList.get(0);
 		machine.msgHereIsGlass(g);
 		transducer.fireEvent(machine.getChannel(), TEvent.WORKSTATION_DO_LOAD_GLASS, null);
 		synchronized (glassList) {
 			glassList.remove(g);
 		} 
 		status = ConveyorStatus.Nothing;
 		stateChanged();		
 	}
 	
 	private void requestSpace() {
 		print("Requesting Space");
 		Glass g = glassList.get(0); 
 		
 		status = ConveyorStatus.Requesting;
 		
 		if(g.ifNeedMachine(conveyorIndex)){ //if (g.recipe.machine2 == true) {
 			popup.msgIncomingNeedProcessing();
 			stateChanged();
 		} else {
 			popup.msgIncomingNoProcessing();
 			stateChanged();
 		}
 	}
 	
 	public void setPreviousMachine(Machine m) {
 		begin.setMachine(m);
 	}
 	
 	public void setMachine(Machine m) {
 		machine = m;
 	}
 	
 	public void setPopup(Popup p) {
 		popup = p;
 	}
 	
 	public void setPreviousCF(ConveyorFamily cf) {
 		begin.setPreviousCF(cf);
 	}
 	
 	public void setBin(Bin b) {
 		begin.setBin(b);
 	}
 
 }
