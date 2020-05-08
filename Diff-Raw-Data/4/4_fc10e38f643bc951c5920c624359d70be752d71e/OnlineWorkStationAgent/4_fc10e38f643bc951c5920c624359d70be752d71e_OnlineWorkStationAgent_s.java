 package glassLine.agents;
 
import java.util.List;
 
 import javax.swing.JTextArea;
 
 
 import glassLine.Glass;
 import glassLine.interfaces.Machine;
 import gui.drivers.FactoryDriver;
 import gui.panels.ControlPanel;
 import gui.panels.subcontrolpanels.TracePanel;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class OnlineWorkStationAgent extends Agent implements Machine{
 
 	/**
 	 * This class represents an online machine.
 	 * Type : BREAKOUT, MANUAL_BREAKOUT, CUTTER,
 	 * WASHER, UV_LAMP, OVEN, PAINTER
 	 */
 
 	private String type;
 
 	private int guiIndex;  // for communication with GUI through transducer
 	private int capacity;
 	private List<MyGlass> glassList; 
 
 	private enum PrecedingAgentState {none, requestingToSend, sending};
 	private enum FollowingAgentState {none, requestSent, readyToReceive};
 	private enum GlassState {none, needsProcessing, doneProcessing}
 	private PrecedingAgentState precedingAgentState;
 	private FollowingAgentState followingAgentState;
 	private ConveyorAgent precedingConveyorAgent;
 	private ConveyorAgent followingConveyorAgent;
 	private enum AgentState {processing, notProcessing}
 	private AgentState state;
 	
 	
 
 
 	private class MyGlass {
 		private Glass g;
 		private GlassState state;
 
 		public MyGlass(Glass g){
 			this.g = g;
 			this.state = GlassState.none;
 		}
 	}
 
 	public OnlineWorkStationAgent(String type, int guiIndex, int capacity, Transducer transducer, TracePanel tracePanel){
 		super(type);
 		this.type = type;
 		
 		this.capacity = capacity;
 		this.transducer = transducer;
 		this.state = AgentState.notProcessing;
 		this.tracePanel = tracePanel;
 	
 		// Registering to the appropriate transducer channel
 		try{
 			if(type.equals("BREAKOUT"))
 				this.transducer.register(this, TChannel.BREAKOUT);
 			else if (type.equals("MANUAL_BREAKOUT"))
 				this.transducer.register(this, TChannel.MANUAL_BREAKOUT);
 			else if (type.equals("CUTTER"))
 				this.transducer.register(this, TChannel.CUTTER);
 			else if (type.equals("WASHER"))
 				this.transducer.register(this, TChannel.WASHER);
 			else if (type.equals("UV_LAMP"))
 				this.transducer.register(this, TChannel.UV_LAMP);
 			else if (type.equals("OVEN"))
 				this.transducer.register(this, TChannel.OVEN);
 			else if (type.equals("PAINTER"))
 				this.transducer.register(this, TChannel.PAINTER);
 			else
 				throw new Exception("Invalid Machine Type");
 		}catch(Exception e){
 			System.out.println(e.getMessage());
 		}
 		this.precedingAgentState = PrecedingAgentState.none;
 		this.followingAgentState = FollowingAgentState.none;
 
 	}
 
 	/** MESSAGES **/
 
 	/** This message is sent by the preceding ConveyorAgent or by a RobotAgent transferring a piece of glass. 
 	 * @params : Glass g (instance of glass)
 	 **/
 
 	public void msgHereIsGlass(Glass g) {
 		print("Receiving new piece of glass.");
 		glassList.add(new MyGlass(g));
 		if(this.state == AgentState.processing)
 			glassList.get(0).state = GlassState.needsProcessing;
 		else 
 			glassList.get(0).state = GlassState.doneProcessing;
 		this.precedingAgentState = PrecedingAgentState.none;
 
 		stateChanged();
 	}
 
 	/** This message is sent by the preceding ConveyorAgent or by a RobotAgent requesting to transfer a piece of glass that needs processing. 
 	 *
 	 **/
 
 	public void msgGlassIsReady(){
 		print("Received a glass transfer request.");
 		this.state = AgentState.processing;
 		this.precedingAgentState = PrecedingAgentState.requestingToSend;
 
 		stateChanged();
 	}
 	
 	/** This message is sent by the preceding ConveyorAgent or by a RobotAgent requesting to transfer a piece of glass that doesn't need processing. 
 	 * 
 	 **/
 	
 	@Override
 	public void msgGlassNeedsThrough() {
 		print("Received a glass transfer request.");
 		this.state = AgentState.notProcessing;
 		this.precedingAgentState = PrecedingAgentState.requestingToSend;
 		
 	}
 
 	/** This message is sent by the following ConveyorAgent or by a RobotAgent requesting to transfer a piece of glass. 
 	 * @params : Glass g (instance of glass)
 	 **/
 
 	public void msgReadyToTakeGlass(){
 		print("Received a confirmation that recipient is ready for glass transfer.");
 		this.followingAgentState = FollowingAgentState.readyToReceive;
 
 		stateChanged();
 	}
 	
 	/** This message is sent when the processing animation is done.
 	 *
 	 **/
 	public void msgGlassDoneProcessing(){
 		this.glassList.get(0).state = GlassState.doneProcessing;
 		stateChanged();
 	}
 	
 	
 	/** This message is sent when the transfer animation is done.
 	 *
 	 **/
 	public void msgGlassRemoved(){
 		this.glassList.remove(0);
 		stateChanged();
 	}
 	
 
 	/** SCHEDULER **/
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		/* If the preceding conveyor agent is requesting to send a piece of glass, 
 		    check if ready.  */
 		if(precedingAgentState == PrecedingAgentState.requestingToSend){
 			checkIfReadyToReceive();
 			return true;
 		}
 
 		/* If a piece of glass needs to be processed or transferred.  */
 		if(!glassList.isEmpty()){
 			if(glassList.get(0).state == GlassState.needsProcessing){
 				processGlass();
 				return true;
 			}else if (glassList.get(0).state == GlassState.doneProcessing ){
 				if(followingAgentState == FollowingAgentState.none)
 					requestToTransferGlass();
 				else if (followingAgentState == FollowingAgentState.readyToReceive)
 					transferGlass();
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 
 	/** ACTIONS **/
 
 	/**This action checks if the machine is ready to receive a piece of glass. 
 	 * 
 	 **/
 	public void checkIfReadyToReceive(){
 		if(glassList.size() >= capacity){
 			precedingConveyorAgent.msgReadyToTakeGlass();
 			this.precedingAgentState = PrecedingAgentState.sending;
 		}else 
 			this.precedingAgentState = PrecedingAgentState.requestingToSend;
 
 		stateChanged();
 	}
 
 	/**This action fires an event on the transducer to perform the animation.
 	 * 
 	 **/
 	public void processGlass(){
 
 		Object args[] = new Object[1];
 		args[0] = this.guiIndex;
 		if(type.equals("BREAKOUT"))
 			this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("MANUAL_BREAKOUT"))
 			this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("CUTTER"))
 			this.transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("WASHER"))
 			this.transducer.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("UV_LAMP"))
 			this.transducer.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("OVEN"))
 			this.transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("PAINTER"))
 			this.transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_DO_ACTION, args); 
 
 		stateChanged();
 	}
 
 	/**This action sends a message to the following CovneyorAgent requesting to transfer a piece of glass.
 	 **/
 	public void requestToTransferGlass(){
 
 		followingConveyorAgent.msgGlassIsReady();
 		this.followingAgentState = FollowingAgentState.requestSent;
 
 		stateChanged();
 	}
 
 	/**This action sends a message to the following CovneyorAgent transferring a piece of glass.
 	 **/
 	public void transferGlass(){
 
 		followingConveyorAgent.msgHereIsGlass(this.glassList.get(0).g);
 		this.glassList.remove(0);
 		this.glassList.get(0).state = GlassState.none;
 		this.followingAgentState = FollowingAgentState.none;
 
 		Object args[] = new Object[1];
 		args[0] = this.guiIndex;
 		if(type.equals("BREAKOUT"))
 			this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("MANUAL_BREAKOUT"))
 			this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("CUTTER"))
 			this.transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("WASHER"))
 			this.transducer.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("UV_LAMP"))
 			this.transducer.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("OVEN"))
 			this.transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("PAINTER"))
 			this.transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		stateChanged();
 	}
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if(type.equals("BREAKOUT")){
 			if(channel == TChannel.BREAKOUT){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 		}else if (type.equals("MANUAL_BREAKOUT")){
 			if(channel == TChannel.MANUAL_BREAKOUT){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 		}else if (type.equals("CUTTER")){
 			if(channel == TChannel.CUTTER){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 		}else if (type.equals("WASHER")){
 			if(channel == TChannel.WASHER){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 		}else if (type.equals("UV_LAMP")){
 			if(channel == TChannel.UV_LAMP){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 		}else if (type.equals("OVEN")){
 			if(channel == TChannel.OVEN){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 		}else if (type.equals("PAINTER")){
 			if(channel == TChannel.PAINTER){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}
 			}
 
 		}
 	}
 	
 	public void setConveyors(ConveyorAgent preceding, ConveyorAgent following){
 		this.precedingConveyorAgent = preceding;
 		this.followingConveyorAgent = following;
 	}
 	
 
 
 	
 	
 }
