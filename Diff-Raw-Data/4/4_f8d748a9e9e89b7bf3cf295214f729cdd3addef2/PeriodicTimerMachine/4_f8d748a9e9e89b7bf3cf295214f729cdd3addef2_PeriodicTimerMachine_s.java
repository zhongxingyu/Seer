 package exercises;
 
 import runtime.EventWindow;
 import runtime.IStateMachine;
 import runtime.Scheduler;
 import runtime.Timer;
 
 public class PeriodicTimerMachine implements IStateMachine {
 	
 	private static final String START = "Start", STOP = "Stop", TIMER_1 = "t1";	
 	public static final String[] EVENTS = {START, STOP};
 	private enum STATES {IDLE, ACTIVE, FINAL}
 	private Timer t1 = new Timer("t1");
 	protected STATES state = STATES.IDLE;
 
 	public int fire(String event, Scheduler scheduler) {
 		if(state==STATES.IDLE) {
 			if(event.equals(START)) {
 				t1.start(scheduler, 1000);
 				state = STATES.ACTIVE;
 				return EXECUTE_TRANSITION;
 			}  
 		} else if(state==STATES.ACTIVE) {
 			if(event.equals(STOP)) {
				t1.start(scheduler, 1000);
				state = STATES.ACTIVE;
 				return EXECUTE_TRANSITION;
 			} else if (event.equals(TIMER_1)) {
 				System.out.println("tick");
 				t1.start(scheduler, 1000);
 				state = STATES.ACTIVE;
 				return EXECUTE_TRANSITION;
 			}
 		}
 		return DISCARD_EVENT;
 	}
 	
 	public static void main(String[] args) {
 		IStateMachine stm = new PeriodicTimerMachine();
 		Scheduler s = new Scheduler(stm);
 		
 		EventWindow w = new EventWindow(EVENTS, s);
 		w.show();
 
 		s.run();
 	}
 }
