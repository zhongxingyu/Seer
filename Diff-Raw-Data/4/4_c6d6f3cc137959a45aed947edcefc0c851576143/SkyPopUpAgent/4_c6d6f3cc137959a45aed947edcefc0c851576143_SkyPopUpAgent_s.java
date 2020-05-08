 package engine.sky.agent;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.Semaphore;
 
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 import engine.interfaces.SkyMachine;
 import engine.util.GlassType;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class SkyPopUpAgent extends Agent implements ConveyorFamily {
 
 	/** Data **/
 
 	private MyConveyor postConveyor;
 	private MyConveyor preConveyor;
 	private MyMachine firstMachine;
 	private MyMachine secondMachine;
 
 	private int myGuiIndex;
 	private GlassType currentGlass;
 	private Target target;
 	private State myState, savedState;
 	private boolean glassLoaded;
 	private boolean informed = false;
 
 
 	public enum MachineState {Idle, Processing, Done, Off};
 	public enum ConveyorState {Available, UnAvailable};
 	public enum Target {None, PreConveyor, PostConveyor, Machine1, Machine2, Animating};
 	public enum State { Down, Up, Animating, Broken};
 
 	private class MyConveyor {
 		ConveyorFamily conveyor;
 		ConveyorState state;
 
 		public MyConveyor(ConveyorFamily c, ConveyorState s) {
 			conveyor = c;
 			state = s;
 		}
 	}
 
 	private class MyMachine {
 		SkyMachine machine;
 		MachineState state, savedState;
 
 		public MyMachine(SkyMachine m, MachineState s) {
 			machine = m;
 			state = s;
 		}
 	}
 
 	/** Constructor **/
 	public SkyPopUpAgent(ConveyorFamily pre, ConveyorFamily post, SkyMachine first, SkyMachine second, int guiIndex, String n, Transducer tr) {
 
 		super(n,tr);
 		preConveyor = new MyConveyor(pre, ConveyorState.UnAvailable);
 		postConveyor = new MyConveyor(post, ConveyorState.UnAvailable);
 		firstMachine = new MyMachine (first, MachineState.Idle);
 		secondMachine = new MyMachine (second, MachineState.Idle);
 		myGuiIndex = guiIndex;
 
 		transducer.register(this, TChannel.POPUP);
 	}
 
 	public SkyPopUpAgent(int guiIndex, String n, Transducer tr) {
 		super(n,tr);
 		myGuiIndex = guiIndex;
 
 		transducer.register(this, TChannel.POPUP);
 		target = Target.None;
 		myState = State.Down;
 		glassLoaded = false;
 
 	}
 
 	/** Messages **/
 	public void msgGlassRemoved(SkyMachine machine){
 		if (machine == firstMachine.machine) {
 			if (firstMachine.state != MachineState.Off) {
 				firstMachine.state = MachineState.Idle;
 			}
 			else {
 				firstMachine.savedState = MachineState.Idle;
 			}
 		}
 		else if (machine == secondMachine.machine) {
 			if (secondMachine.state != MachineState.Off) {
 				secondMachine.state =MachineState.Idle;
 			}
 			else {
 				secondMachine.savedState = MachineState.Idle;
 			}
 		}
 		stateChanged();
 	}
 
 
 	@Override
 	public void msgPassingGlass(GlassType gt) {
 		currentGlass = gt;
 		glassLoaded = false;
 		target = Target.PreConveyor;
 		stateChanged();
 	}
 
 	@Override
 	public void msgIAmAvailable() {
 		System.out.println(this + " received msgIAmAvailable");
 		postConveyor.state = ConveyorState.Available;
 		stateChanged();
 	}
 
 	@Override
 	public void msgIAmNotAvailable() {
 		System.out.println(this + " msgIAmNotAvailable: Target = " + target + " State = " + myState);
 		postConveyor.state = ConveyorState.UnAvailable;
 
 		if (currentGlass == null) {
 			System.out.println("CurrentGlass = null");
 		}
 		else {
 			System.out.println("CurrentGlass != null");
 		}
 
 		if (glassLoaded == true) {
 			System.out.println("Load finished");
 		}
 		else {
 			System.out.println("Load Not Finished");
 		}
 		stateChanged();
 	}
 
 	//Called by machine when machine finished loading
 	public void msgLoadFinished() {
 		target = Target.None;
 		if (myState != State.Broken) {
 			myState = State.Up;
 		}
 		else {
 			savedState = State.Up;
 		}
 		System.out.println("msgLoadFinished: Target = " + target + " State = " + myState);
 		stateChanged();
 	}
 
 
 
 
 	public void msgGlassDone(SkyMachine machine, GlassType gt) {
 		if (machine == firstMachine.machine) {
 			if (firstMachine.state != MachineState.Off) {
 				firstMachine.state = MachineState.Done;
 			}
 			else {
 				firstMachine.savedState = MachineState.Done;
 			}
 
 		} else if (machine == secondMachine.machine) {
 			if (secondMachine.state != MachineState.Off) {
 				secondMachine.state =MachineState.Done;
 			}
 			else {
 				secondMachine.savedState = MachineState.Done;
 			}
 
 		}
 		stateChanged();
 	}
 
 	public void msgReturningGlass(SkyMachine machine, GlassType gt) {
 		currentGlass = gt;
 		glassLoaded = false;
 
 		if (machine == firstMachine.machine) {
 			firstMachine.state = MachineState.Idle;
 			target = Target.Machine1;
 		} else if (machine == secondMachine.machine) {
 			secondMachine.state =MachineState.Idle;
 			target = Target.Machine2;
 		}
 		stateChanged();
 
 	}
 
 	public void msgPopUpBreak() {
 		if (myState == State.Up) {
 			savedState = State.Up;
 		}
 		else if (myState == State.Down) {
 			savedState = State.Down;
 		}
 		else if (myState == State.Animating) {
 			savedState = State.Animating;
 		}
 
 		preConveyor.conveyor.msgIAmNotAvailable();
 		informed = false;
 		myState = State.Broken;
 		System.out.println(this+" breaking the pop up");
 	}
 
 	public void msgPopUpUnbreak() {
 		myState = savedState;
 		System.out.println(this+" fixing the pop up with state = ");
 		stateChanged();
 	}
 
 	public void msgOfflineMachineOn(int machineIndex) {
 		if (machineIndex%2 ==0) {
 
 			firstMachine.state = firstMachine.savedState;
 		}
 		else {
 
 			secondMachine.state = secondMachine.savedState;
 		}
 
 	}
 
 	public void msgOfflineMachineOff(int machineIndex) {
 		if (machineIndex%2 ==0) {
 			firstMachine.savedState = firstMachine.state;
 			firstMachine.state = MachineState.Off;
 		}
 		else {
 			secondMachine.savedState = secondMachine.state;
 			secondMachine.state = MachineState.Off;
 		}
 	}
 
 	// Animation messages
 	private void msgGlassLoaded() {
 		glassLoaded = true;
 		if (target == Target.None || target == Target.PostConveyor || target == Target.PreConveyor) {
 			if (myState!=State.Broken)
 				myState = State.Down;
 			else {
 				savedState = State.Down;
 			}
 			target = Target.None;
 		}
 		else{
 			if (myState!=State.Broken)
 				myState = State.Up;
 			else {
 				savedState = State.Up;
 			}
 			target = Target.PostConveyor;
 		}
 		stateChanged();
 	}
 
 	private void msgMovedUp() {
 		if (myState!=State.Broken)
 			myState = State.Up;
 		else {
 			savedState = State.Up;
 		}
 		stateChanged();
 	}
 
 	private void msgMovedDown() {
 		if (myState!=State.Broken)
 			myState = State.Down;
 		else {
 			savedState = State.Down;
 		}
 		stateChanged();
 	}
 
 	private void msgGlassReleased() {
 		currentGlass = null;
 		if (myState!=State.Broken)
 			myState = State.Down;
 		else {
 			savedState = State.Down;
 		}
 		target = Target.None;
 		stateChanged();
 	}
 
 
 
 	/** Scheduler **/
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		System.out.println(this + " Scheduler with state = " +myState + " target = " + target);
 
 		if (currentGlass != null && target == Target.None && myState == State.Up) {
 			if (firstMachine.state == MachineState.Idle) {
 				target = Target.Machine1;
 				passToMachine(firstMachine);
 				return true;
 
 			}
 			else if (secondMachine.state == MachineState.Idle){
 				target = Target.Machine2;
 				passToMachine(secondMachine);
 				return true;
 
 			}
 		}
 
 		if (currentGlass == null && target == Target.None && myState == State.Down) {
 			if (firstMachine.state == MachineState.Done) {
 				target = Target.Machine1;
 				popUp(); // pop up to say ready
 				return true;
 
 			}
 			else if (secondMachine.state == MachineState.Done) {
 				target = Target.Machine2;
 				popUp(); // pop up to say ready
 				return true;
 			}
 			else if (!informed && (firstMachine.state == MachineState.Idle || secondMachine.state == MachineState.Idle)){
 				informIAmAvailable();
 				return true;
 			}
 			else if (!informed) {
 				informOkToSkip();
 				return true;
 			}
 		}
 
 		if (currentGlass != null && target == Target.None && myState == State.Down && glassLoaded) {
 			if (currentGlass.getConfig(myGuiIndex)) {
 				target = Target.None;
 				popUp(); // pop up to give to machine
 				return true;
 			}
 		}
 
 
 
 		if (currentGlass == null && myState == State.Up) {
 			if (target == Target.None) {
 				popDown();
 			}
 			else if (target == Target.Machine1) {
 				sayReady(firstMachine);
 			}
 			else if (target == Target.Machine2) {
 				sayReady(secondMachine);
 			}
 			return true;
 		}
 
 		if (currentGlass !=null && myState == State.Up && target == Target.PostConveyor) {
 			popDown();
 			return true;
 		}
 
 		if (currentGlass !=null && myState == State.Down && glassLoaded) {
 			if (postConveyor.state == ConveyorState.Available) {
 				passToConveyor();
 				return true;
 			}
			else {
 				preConveyor.conveyor.msgIAmNotAvailable();
 				return true;
 			}
 
 		}
 
 
 
 
 		return false;
 	}
 
 	/**Actions **/
 
 	private void informIAmAvailable() {
 		System.out.println(this +" Action: informIAmAvailable");
 		preConveyor.conveyor.msgIAmAvailable();
 		//		myState = State.Animating;
 		informed = true;
 	}
 
 	private void informOkToSkip() {
 		System.out.println(this +" Action: informOkToPass");
 		((SkyConveyorAgent) preConveyor.conveyor).msgOkToSkip();
 		informed = true;
 	}
 
 	private void popUp() {
 		System.out.println(this +" Action: popUp");
 
 		preConveyor.conveyor.msgIAmNotAvailable();
 
 		Object[] args = new Object[1];
 		args[0] = myGuiIndex;
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
 
 		myState = State.Animating;
 	}
 
 	private void popDown() {
 		System.out.println(this +" Action: popDown");
 		Object[] args = new Object[1];
 		args[0] = myGuiIndex;
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
 
 		myState = State.Animating;
 		informed = false;
 	}
 
 	private void sayReady(MyMachine mm) {
 		System.out.println(this +" Action: sayReady");
 		mm.machine.msgIAmReady();
 		myState = State.Animating;
 	}
 
 	private void passToMachine(MyMachine mm) {
 		System.out.println(this +" Action: passToMachine");
 		mm.machine.msgPassingGlass(currentGlass);
 		mm.state = MachineState.Processing;
 		myState = State.Animating;
 		currentGlass = null;
 	}
 
 	private void passToConveyor() {
 		System.out.println(this + " Action: passToConveyor");
 		preConveyor.conveyor.msgIAmNotAvailable();
 
 		Object[] args = new Object[1];
 		args[0] = myGuiIndex;
 
 		postConveyor.conveyor.msgPassingGlass(currentGlass);
 
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
 
 		myState = State.Animating;
 		informed = false;
 
 	}
 
 
 
 	/** Utilities **/
 
 
 	public GlassType getGlass() {
 		return currentGlass;
 	}
 
 	public MachineState getFirstMachineState() {
 		return firstMachine.state;
 	}
 
 	public MachineState getSecondMachineState() {
 		return secondMachine.state;
 	}
 
 	public ConveyorState getPostConveyorState() {
 		return postConveyor.state;
 	}
 
 	public void connectAgents(ConveyorFamily pre, ConveyorFamily post, SkyMachine first, SkyMachine second) {
 		preConveyor = new MyConveyor(pre, ConveyorState.UnAvailable);
 		postConveyor = new MyConveyor(post, ConveyorState.UnAvailable);
 		firstMachine = new MyMachine (first, MachineState.Idle);
 		secondMachine = new MyMachine (second, MachineState.Idle);
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED &&((Integer)args[0]).equals(myGuiIndex)) {
 			this.msgGlassLoaded();
 		}
 
 		if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP && ((Integer)args[0]).equals(myGuiIndex)) {
 			this.msgMovedUp();
 		}
 
 		if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN && ((Integer)args[0]).equals(myGuiIndex)) {
 			this.msgMovedDown();
 		}
 
 		if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_RELEASE_FINISHED && ((Integer)args[0]).equals(myGuiIndex)) {
 			this.msgGlassReleased();
 		}
 	}
 
 
 
 }
