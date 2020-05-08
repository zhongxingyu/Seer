 package edu.ntnu.ttk4145.recs;
 
 import static edu.ntnu.ttk4145.recs.Order.NO_ORDER;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import edu.ntnu.ttk4145.recs.driver.Driver;
 import edu.ntnu.ttk4145.recs.driver.Driver.Call;
 import edu.ntnu.ttk4145.recs.manager.Manager;
 
 public class Elevator {
 
 	protected static final int DOOR_WAIT_TIME = 5; // number of seconds the door is open, in seconds
 
 	protected static final long ONE_SECOND = 1000; // millis in one second
 
 	private static Elevator localInstance = null;
 	
 	public static Elevator getLocalElevator(){
 		if(localInstance == null){
 			localInstance = new Elevator(Util.makeLocalId());
 		}
 		return localInstance;
 	}
 
 	private long id;
 
 	private State state = new State();
 	
 	public Elevator(long id){
 		this.id = id;
 	}
 	
 	public long getId() {
 		return id;
 	}
 	
 	public void setObstructed(boolean obstructed) {
 		state.obstructed = obstructed;
 		updateElevatorState();
 	}
 
 	public void setFloor(int floor, boolean arriving) {
 		if(arriving){
 			state.floor = floor;
 		}
 		state.atFloor = arriving;
 		updateElevatorState();
 	}
 
 	public void setStopped(boolean stopped) {
 		state.stopped = stopped;
 		updateElevatorState();
 	}
 	
 	public synchronized void addOrder(Order order){
 		state.orders[order.call.ordinal()][order.floor] = order.id;
 		updateElevatorState();
 	}
 
 	public synchronized void addOrder(Call button, int floor) {
 		state.orders[button.ordinal()][floor] = 1;
 		updateElevatorState();
 	}
 	
 	private synchronized void updateElevatorState(){
 		
 		if(state.stopped || state.obstructed || state.doorsOpen){
 			;
 		} else {
 			if(state.atFloor) {
 				// Stopped at a floor
 				long orderId = state.orders[state.dir.ordinal()][state.floor];
 				if(orderId != NO_ORDER || state.orders[Call.COMMAND.ordinal()][state.floor] != NO_ORDER){
 					// Stop at this floor;
 					letPeopleOnOff();
 					
 					if(orderId != NO_ORDER){
 						Manager.getInstance().orderDone(orderId);
 					}
 					
 					state.orders[state.dir.ordinal()][state.floor] = NO_ORDER;
 					state.orders[Call.COMMAND.ordinal()][state.floor] = NO_ORDER;
 				}
 			}
 			
 			int callsOver = 0;
 			int callsUnder = 0;
 			for(Call call : Call.values()){
 				for(int floor = 0; floor < Driver.NUMBER_OF_FLOORS; floor++){
 					if(state.orders[call.ordinal()][floor] != NO_ORDER){
 						if(floor > state.floor){
 							callsOver++;
 						} else if (floor < state.floor){
 							callsUnder++;
 						}
 					}
 				}
 			}
 			if(callsOver == 0 && callsUnder == 0){
 				state.dir = Direction.NONE;
 			} else if (callsOver  > 0 && callsUnder == 0){
 				state.dir = Direction.UP;
 				state.atFloor = false;
 			} else if (callsUnder > 0 && callsOver  == 0){
 				state.dir = Direction.DOWN;
 				state.atFloor = false;
 			} else {
 				state.dir = state.dir; // noop, continue the way you were going.
 				state.atFloor = false;
 			}
 		}
 		
 		System.out.println(state);
 		
 		updatePhysicalElevator();
 		Manager.getInstance().updateState(state);
 	}
 	
 	private void letPeopleOnOff() {
 		state.doorsOpen = true;
 		new Thread(new Runnable(){
 			@Override
 			public void run() {
 				int wait = DOOR_WAIT_TIME;
 				while(wait-- > 0 || state.obstructed){
 					try {
 						Thread.sleep(ONE_SECOND);
 					} catch (InterruptedException e) {}
 				}
 				state.doorsOpen = false;
 				updateElevatorState();
 			}
 		}).start();
 	}
 
 	private void updatePhysicalElevator() {
 		Driver driver = Driver.getInstance();
 		for(Call button : Call.values()){
 			for(int floor = 0; floor < Driver.NUMBER_OF_FLOORS; floor++){
 				driver.setButtonLamp(button, floor, state.orders[button.ordinal()][floor] != NO_ORDER);
 			}
 		}
 		driver.setStopLamp(state.stopped);
 		driver.setDoorOpenLamp(state.doorsOpen);
 		driver.setFloorIndicator(state.floor);
 		if(state.atFloor || state.doorsOpen || state.stopped || state.obstructed){
 			driver.setSpeed(0);
 		} else {
 			driver.setSpeed(state.dir.speed);
 		}
 	}
 
 	public static enum Direction {
 		UP(100), DOWN(-100), NONE(0);
 		
 		private int speed;
 		
 		private Direction(int speed){
 			this.speed = speed;
 		}
 	}
 	
 	public static class State{
 		
 		private Direction dir = Direction.NONE;
 		
 		private int floor;
 		
 		private long[][] orders = new long[Call.values().length][Driver.NUMBER_OF_FLOORS];
 
 		private boolean atFloor;
 		private boolean stopped;
 		private boolean obstructed;
 		private boolean doorsOpen;
 		
 		public Direction getDirection(){
 			return dir;
 		}
 		
 		public int getFloor(){
 			return floor;
 		}
 		
 		public long[][] getOrders(){
 			return orders;
 		}
 		
 		public boolean isAtFloor(){
 			return atFloor;
 		}
 		
 		public boolean isStopped(){
 			return stopped;
 		}
 		
 		public boolean isObstructed(){
 			return obstructed;
 		}
 		
 		public boolean isDoorsOpen(){
 			return doorsOpen;
 		}
 		
 		public String toString(){
 			StringWriter sw = new StringWriter();
 			PrintWriter  pw = new PrintWriter(sw);
 			
 			pw.printf("dir: %s, floor: %d, [at:%c,s:%c,o:%c,d:%c]%n",dir,floor,
 					atFloor ? '*':' ',stopped ? '*':' ',obstructed ? '*':' ',doorsOpen ? '*':' ');
 			
			for(int floor = 0; floor < Driver.NUMBER_OF_FLOORS; floor++){
				pw.printf("%d: ",floor);
 				for(Call call : Call.values()){
 					pw.printf("%s: %d, ", call,orders[call.ordinal()][floor]);
 				}
 				pw.println();
 			}
 			return sw.toString();
 		}
 	}
 }
