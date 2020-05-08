 package engine.agent.Dongyoung;
 
import shared.Glass;
 import transducer.*;
 
 public class InlineMachine extends Component implements TReceiver{
 
 	// DATA
 	private TChannel channel;
 	private boolean loadFinished = false, actionFinished = false, releaseFinished = false;
	private Glass glassOnSpot;
 	
 	// Constructor
 	public InlineMachine(TChannel channel) {
 		super(channel.toString());
 		this.channel = channel;
 	}
 	
 	// MESSAGE - Directly from Transducer. Refer to function 'eventFired'
 	
 	// SCHEDULER
 	@Override
 	protected boolean pickAndExecuteAnAction(){
 		if( broken ){
 			return false;
 		}
 		
 		if( loadFinished ){
 			doWorkAction();
 			return true;
 		}
 		
 		if( actionFinished ){
 			actionFinishedAction();
 			return true;
 		}
 		
 		if( releaseFinished ){
 			releaseFinishedAction();
 			return true;
 		}
 		
 		return false;
 	}
 	
	@Override
	public void msgHereIsGlass(Glass g) {
		glasses.add(g);
		glassOnSpot = g;
		
	}
	
 	// ACTION
 	/*
 	 * Need work - machine should work
 	 * No need - machine should not work
 	 */
 	private void doWorkAction(){
 		loadFinished = false;
		if( glassOnSpot.getRecipe( channel ) ){
 			transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, null);
 		}
 		else{
 			actionFinished = true;
 		}
 	}
 	
 	private void actionFinishedAction(){
 		if( nextCompFree ){
 			transducer.fireEvent(channel, TEvent.WORKSTATION_RELEASE_GLASS, null);
 			passGlassAction();
 			actionFinished = false;
 			nextCompFree = false;
 		}
 	}
 	
 	private void releaseFinishedAction(){
 		notifyIAmFreeAction();
 		releaseFinished = false;
 	}
 	
 	/* Notification */
 	private void notifyIAmFreeAction(){
 		previousComp.msgIAmFree();
 	}
 	
 	/* Glass Pass */
 	private void passGlassAction(){
		synchronized(glassOnSpot) {
			nextComp.msgHereIsGlass( glassOnSpot );
			glasses.remove(glassOnSpot);
			glassOnSpot = null;
		}
 	}
 
 	// EXTRA
 	/* From Transducer */
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if( event == TEvent.WORKSTATION_LOAD_FINISHED ){
 			loadFinished = true;
 		}
 		else if( event == TEvent.WORKSTATION_GUI_ACTION_FINISHED ){
 			actionFinished = true;
 		}
 		else if( event == TEvent.WORKSTATION_RELEASE_FINISHED ){
 			releaseFinished = true;
 		}
 		else if( event == TEvent.WORKSTATION_BROKEN ){
 			broken = true;
 		}
 		else if( event == TEvent.WORKSTATION_FIXED ){
 			broken = false;
 		}
 		stateChanged();
 	}
 	
 	public void setter(Conveyor previousConveyor, Conveyor nextConveyor, Transducer transducer){
 		previousComp = previousConveyor;
 		nextComp = nextConveyor;
 		this.transducer = transducer;
 		transducer.register(this, channel);
 	}
 }
