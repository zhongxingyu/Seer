 package engine.agent.Dongyoung;
 
 import engine.interfaces.ConveyorFamily;
 import shared.Glass;
 import transducer.*;
 
 public class Conveyor extends Component implements TReceiver{
 
 	// DATA
 	private ConveyorFamily previousFamily = null;
 	private ConveyorFamily nextFamily = null;
 	private boolean glassLeaveFront = false;
 	private Integer[] conveyorNum = new Integer[1];
 	private int frontSensorNum, backSensorNum, sensorNum;
 	private Glass glassToNext;
 	
 	// Constructor
 	public Conveyor(String name, int num, int frontSensorNum, int backSensorNum) {
 		super(name);
 		conveyorNum[0] = num;
 		this.frontSensorNum = frontSensorNum;
 		this.backSensorNum = backSensorNum;
 	}
 	
 	// MESSAGE - Directly from Transducer. Refer to function 'eventFired'
 	
 	// SCHEDULER
 	@Override
 	protected boolean pickAndExecuteAnAction(){		
 		// Non-norm. Fix
 		if( fix ){
 			fixNonNorm();
 			return true;
 		}
 		
 		// Conveyor is broken
 		if( broken ){
 			return false;
 		}
 		
 		// New Glass on Front Sensor
 		if( newGlass ){
 			newGlassAction();
 			return true;
 		}
 		
 		// Glass leaves Front Sensor
 		if( glassLeaveFront ){
 			glassLeaveFrontAction();
 			return true;
 		}
 		
 		// New Glass on Back Sensor
 		if( checkPass ){
 			checkPassAction();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	// ACTION
 	private void fixNonNorm(){
 		fix = false;
 		broken = false;
 		conveyorCheck();
 	}
 	
 	/*
 	 * Check if next component is ready to accept glasses.
 	 * If not, the conveyor keeps the glass waiting.
 	 */
 	private void checkPassAction(){
 		transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum );
 		if( nextCompFree ){
 			nextCompFree = false;
 			passGlassAction();
 			transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNum );
 			checkDone = true;
 		}
 		else{
 			checkDone = false;
 		}
 		checkPass = false;
 	}
 	
 	/* New glass on Front Sensor */
 	private void newGlassAction(){
 		transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum );
 		newGlass = false;
 		conveyorCheck();
 	}
 	
 	/* Glass leaves Front Sensor */
 	private void glassLeaveFrontAction(){
 		glassLeaveFront = false;
 		notifyIAmFreeAction();
 	}
 
 	/* Notification */
 	private void notifyIAmFreeAction(){
 		if( previousFamily == null ){
 			previousComp.msgIAmFree();
 		}
 		else{
 			previousFamily.msgIAmFree();
 		}
 	}
 	
 	/* Glass Pass */
 	private void passGlassAction(){
 		glassToNext = glasses.remove(0);
 		
 		if( nextFamily == null ){
 			nextComp.msgHereIsGlass( glassToNext );
 		}
 		else{
 			nextFamily.msgHereIsGlass( glassToNext );
 		}
 	}
 
 	// EXTRA
 	/* From Transducer */
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		
 		sensorNum = (Integer)args[0];
 		if( event == TEvent.SENSOR_GUI_PRESSED ){
 			if( sensorNum == frontSensorNum ){
 				newGlass = true;
 			}
 			else if( sensorNum == backSensorNum ){
 				checkPass = true;
 			}
 			stateChanged();
 		}
 		else if( event == TEvent.SENSOR_GUI_RELEASED ){
 			if( sensorNum == frontSensorNum ){
 				glassLeaveFront = true;
 				stateChanged();
 			}
 		}
		else if(args[0] == conveyorNum[0] && event == TEvent.CONVEYOR_BROKEN ){
 			broken = true;
 		}
		else if(args[0] == conveyorNum[0] && event == TEvent.CONVEYOR_FIXED ){
 			fix = true;
 			stateChanged();
 		}
 	}
 	
 	/* Everytime the conveyor status is changed, it should check the conveyor should run or stops. */
 	private void conveyorCheck(){
 		// Glass on Front Sensor or on Conveyor, but no Glass on Back Sensor
 		if( ( newGlass || !glasses.isEmpty() ) && !checkPass && checkDone ){
 			transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNum );
 		}
 		else{
 			transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum );
 		}
 	}
 
 	public void setter(Object previous, Object next, Transducer transducer){
 		this.transducer = transducer;
 		transducer.register(this, TChannel.SENSOR);
 		transducer.register(this, TChannel.CONVEYOR);
 		
 		if( previous instanceof Component ){
 			previousComp = (Component)previous;
 		}
 		else if( previous instanceof ConveyorFamily ){
 			previousFamily = (ConveyorFamily)previous;
 		}
 		if( next instanceof Component ){
 			nextComp = (Component)next;
 		}
 		else if( next instanceof ConveyorFamily ){
 			nextFamily = (ConveyorFamily)next;
 		}
 	}
 }
