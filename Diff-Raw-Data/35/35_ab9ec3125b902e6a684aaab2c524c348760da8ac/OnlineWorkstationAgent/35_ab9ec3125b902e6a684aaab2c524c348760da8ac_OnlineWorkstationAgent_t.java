 package engine.agent;
 
 import java.util.concurrent.Semaphore;
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.LineComponent;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class OnlineWorkstationAgent extends Agent implements LineComponent {
 	private MachineType type;
 	private TChannel channel;
 	private Glass glass;
 
 	enum GlassState {pending, arrived, processing, processed, releasing, released};
 
 	private GlassState state;
 	private LineComponent prev, next;
 	private Semaphore aniSem;
 	private boolean recPosFree;
 
 	public OnlineWorkstationAgent(String name, MachineType mt, Transducer t) {
 		super(name, t);
 		type = mt;
 		recPosFree = true;
 		channel = type.getChannel();
 		aniSem = new Semaphore(0);
 		transducer.register(this, channel);
 	}
 
 	// *** MESSAGES ***
 	
 	public void msgHereIsGlass(Glass g) {
 		glass = g;
 		state = GlassState.pending;
 		stateChanged();
 	}
 
 	public void msgPositionFree() {
 		recPosFree = true;
 		stateChanged();
 	}
 
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
 			state = GlassState.arrived;
 			stateChanged();
 		} else if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
 			aniSem.release();
 		} else if (event == TEvent.WORKSTATION_RELEASE_FINISHED) {
 			state = GlassState.released;
 			stateChanged();
 		}
 	}
 
 	// *** SCHEDULER ***
 	public boolean pickAndExecuteAnAction() {
 		if (state == GlassState.arrived) {
 			processGlass();
 			return true;
 		}
 		if (state == GlassState.processed) {
 			releaseGlass();
 			return true;
 		}
 		if (state == GlassState.released && recPosFree) {
 			reset();
 			return true;
 		}
 		return false;
 	}
 
 	// *** ACTIONS ***
 	
	/* Process the glass if it needs to be processed. */
 	private void processGlass() {
		if (glass.getNeedsProcessing(type)) {
			doStartProcessing();
			doWaitProcessing();
 		}
 		state = GlassState.processed;
 	}
 	
 	private void releaseGlass() {
 		transducer.fireEvent(channel, TEvent.WORKSTATION_RELEASE_GLASS, null);
 		state = GlassState.releasing;
 		
 		if (next != null)
 			next.msgHereIsGlass(glass);
 		else {
 			print("There is no 'after' LineComponent set for this workstation.");
 		}
 	}
 	
 	private void reset() {
 		state = null;
 		glass = null;
 		recPosFree = false;
 		prev.msgPositionFree();
 	}
 	
	// *** ANIMATION ACTIONS ***
	
	private void doStartProcessing() {
		transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, null);
		state = GlassState.processing;
	}
	
	private void doWaitProcessing() {
		try {
			aniSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
 	// *** ACCESSORS & MUTATORS ***
 	
 	public MachineType getType() {
 		return type;
 	}
 	
 	public TChannel getChannel() {
 		return channel;
 	}
 	
 	/* Setters */
 	public void setPrev(LineComponent lc) { prev = lc; }
 	public void setNext(LineComponent lc) { next = lc; }
 }
