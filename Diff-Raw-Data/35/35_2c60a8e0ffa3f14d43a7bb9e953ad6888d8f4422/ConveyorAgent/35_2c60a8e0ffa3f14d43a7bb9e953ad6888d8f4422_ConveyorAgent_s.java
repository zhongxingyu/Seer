 package engine.agent.tim.agents;
 
 import java.util.*;
 
 import javax.swing.Popup;
 
 import shared.Glass;
 import shared.interfaces.OfflineConveyorFamily;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.tim.interfaces.Conveyor;
 import engine.agent.tim.misc.ConveyorEvent;
 import engine.agent.tim.misc.ConveyorFamilyImp;
 import engine.agent.tim.misc.MyGlassConveyor;
 import engine.agent.tim.misc.MyGlassConveyor.conveyorState;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 	//Name: ConveyorAgent
 
 	//Description:  Will hold the glass until it needs to go into the next conveyor for a different set of processes, or to leave the factory entirely.
 
 	//Data:
 	private List<MyGlassConveyor> glassSheets; // List to hold all of the glass sheets
 	private boolean conveyorOn; // Is the Gui conveyor on?
 	private ConveyorFamilyImp cf; // Reference to the current conveyor family
 	
 	private List<ConveyorEvent> events; // Used to hold all of the sensor events
 
 	int guiIndex; // Needed to communicate with the transducer conveyor
 	
 	// Constructors:
 	public ConveyorAgent(String name, Transducer transducer, int guiIndex) {
 		// Set the passed in values first
 		super(name, transducer);
 		
 		// Then set the values that need to be initialized within this class, specifically
 		this.glassSheets = Collections.synchronizedList(new ArrayList<MyGlassConveyor>());
 		this.conveyorOn = false; // The conveyor is off when this simulation starts
 		
 		this.events = Collections.synchronizedList(new ArrayList<ConveyorEvent>());
 		
 		this.guiIndex = guiIndex;
 		
 		initializeTransducerChannels();
 	}
 	
 	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
 		// Register any appropriate channels
 		transducer.register(this, TChannel.CONVEYOR); // Set this agent to listen to the CONVEYOR channel of the transducer
 	}
 
 	//Messages:
 	public void msgGiveGlassToConveyor(Glass g) { // Add glass to the conveyor
 		glassSheets.add(new MyGlassConveyor(g, conveyorState.beforeEntrySensor)); // conveyorState will always initializes to onConveyor
 		print("Glass with ID (" + g.getID() + ") added to conveyor");
 		stateChanged();
 	}
 	
 	public void msgPositionFree() { // Allow this conveyor to pass a piece of glass to the next conveyor family
 		events.add(ConveyorEvent.popUpFree);
 		print("Event added: PopUpFree.");
 		stateChanged();
 	}
 
 	public void msgUpdateGlass(ConveyorEvent e) { // This message is akin to a stub, but I wanted to match up to my current interaction diagram  I could just call msgGiveGlassToConveyor directly, but the semantics do not look as good that way
 		events.add(e);
 		print("Event added: " + e.toString());
 		stateChanged();
 	}
 
 	//Scheduler:
 	public boolean pickAndExecuteAnAction() {
 		if (events.isEmpty()) { return false; }
 		
 		ConveyorEvent e = events.remove(0);
 		
 		MyGlassConveyor glass = null; // Use null variable for determining is value is found from synchronized loop
 		
 		if (e == ConveyorEvent.onEntrySensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.beforeEntrySensor) {
 						glass = g;
 						break;
 					}
 				}
 			}
 		}
 		if (glass != null) {
 			actSetGlassOnEntrySensor(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.offEntrySensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.onEntrySensor) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		if (glass != null) {
 			actSetGlassOffEntrySensor(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.onPopUpSensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.beforePopUpSensor) {
 						glass = g;
 						break;
 					}
 				}
 			}			
 		}
 		if (glass != null) {
 			actSetGlassOnPopUpSensor(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.popUpFree) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.onPopUpSensor) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		if (glass != null) {
 			actTurnOnConveyorAndSendGlass(); return true;
 		}
 		
 		if (e == ConveyorEvent.offPopUpSensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.onPopUpSensor) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		if (glass != null) {
 			actSetGlassOffPopUpSensor(glass); return true;
 		}
 		
 		print("An event did not match a piece of glass's state, that's a problem");
 		
 		return false;		
 	}
 	
 	//Actions:
 	
 	private void actSetGlassOnEntrySensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.onEntrySensor;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
		turnOnConveyorGUI();
 	}
 
 	private void actSetGlassOffEntrySensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.beforePopUpSensor;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
 		if (cf.getPrevCF() != null) {
 			cf.getPrevCF().msgPositionFree();
 			print("msgPositionFree() sent to previous LineComponent");
 		}
 	}
 
 	private void actSetGlassOnPopUpSensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.onPopUpSensor;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
 		turnOffConveyorGUI(); 
 		cf.getPopUp().msgGiveGlassToPopUp(g.glass);
 	}
 
 	private void actTurnOnConveyorAndSendGlass() {
 		turnOnConveyorGUI(); 
 	}
 
 	private void actSetGlassOffPopUpSensor(MyGlassConveyor g) {
 		glassSheets.remove(g);
 		print("MyGlass " + g.glass.getID() + " removed from conveyor");
 		//if (glassSheets.size() == 0)
 			//turnOffConveyorGUI();
 	}
 	
 	private void turnOnConveyorGUI() {
 		if (!conveyorOn) {
 			Integer[] args = {guiIndex};
 			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
 			print("Turned on conveyor " + guiIndex);
 			conveyorOn = true;
 		}
 	}
 	
 	private void turnOffConveyorGUI() {
 		if (conveyorOn) {
 			Integer[] args = {guiIndex};
 			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
 			print("Turned off conveyor " + guiIndex);
 			conveyorOn = false;
 		}
 	}
 
 	//Other Methods:
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (channel == TChannel.CONVEYOR && ((Integer) args[0] == guiIndex)) {
 			if (event == TEvent.CONVEYOR_DO_START && !conveyorOn) {
 				turnOnConveyorGUI();
 			}
 			else if (event == TEvent.CONVEYOR_DO_STOP && conveyorOn) {
 				turnOffConveyorGUI();
 			}
 		}
 	}
 
 	// Getters and setters
 	
 	/**
 	 * @return the conveyorOn
 	 */
 	public boolean isConveyorOn() {
 		return conveyorOn;
 	}
 
 	/**
 	 * @return the glassSheets
 	 */
 	public List<MyGlassConveyor> getGlassSheets() {
 		return glassSheets;
 	}
 
 	@Override
 	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
 		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
 	}
 
 	/**
 	 * @return the events
 	 */
 	public List<ConveyorEvent> getEvents() {
 		return events;
 	}
 
 	@Override
 	public Integer getGUIIndex() {
 		return guiIndex;
 	}
 }
