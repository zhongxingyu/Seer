 package edu.ntnu.ttk4145.recs.driver;
 
 public abstract class Driver {
 	
 	public static final int NUMBER_OF_FLOORS = 4;
 
 	private static final int MOTOR          = 0x100 +  0;
 
 	private static final int FLOOR_DOWN2    = 0x200 +  0;
 	private static final int FLOOR_UP3      = 0x200 +  1;
 	private static final int FLOOR_DOWN3    = 0x200 +  2;
 	private static final int FLOOR_DOWN4    = 0x200 +  3;
 	private static final int FLOOR_SENSOR1  = 0x200 +  4;
 	private static final int FLOOR_SENSOR2  = 0x200 +  5;
 	private static final int FLOOR_SENSOR3  = 0x200 +  6;
 	private static final int FLOOR_SENSOR4  = 0x200 +  7;
 	
 	private static final int FLOOR_IND1     = 0x300 +  0;
 	private static final int FLOOR_IND2     = 0x300 +  1;
 	private static final int DOOR_OPEN      = 0x300 +  3;
 	private static final int LIGHT_DOWN4    = 0x300 +  4;
 	private static final int LIGHT_DOWN3    = 0x300 +  5;
 	private static final int LIGHT_UP3      = 0x300 +  6;
 	private static final int LIGHT_DOWN2    = 0x300 +  7;
 	private static final int LIGHT_UP2      = 0x300 +  8;
 	private static final int LIGHT_UP1      = 0x300 +  9;
 	private static final int LIGHT_COMMAND4 = 0x300 + 10;
 	private static final int LIGHT_COMMAND3 = 0x300 + 11;
 	private static final int LIGHT_COMMAND2 = 0x300 + 12;
 	private static final int LIGHT_COMMAND1 = 0x300 + 13;
 	private static final int LIGHT_STOP     = 0x300 + 14;
 	private static final int MOTORDIR       = 0x300 + 15;
 	private static final int FLOOR_UP2      = 0x300 + 16;
 	private static final int FLOOR_UP1      = 0x300 + 17;
 	private static final int FLOOR_COMMAND4 = 0x300 + 18;
 	private static final int FLOOR_COMMAND3 = 0x300 + 19;
 	private static final int FLOOR_COMMAND2 = 0x300 + 20;
 	private static final int FLOOR_COMMAND1 = 0x300 + 21;
 	private static final int STOP           = 0x300 + 22;
 	private static final int OBSTRUCTION    = 0x300 + 23;
 
 	private static final int LIGHT_DOWN1    = -1;
 	private static final int FLOOR_DOWN1    = -1;
 	private static final int LIGHT_UP4      = -1;
 	private static final int FLOOR_UP4      = -1;
 	
 	
 	private static final int[][] LAMP_CHANNELS = {
 		{LIGHT_UP1, LIGHT_UP2, LIGHT_UP3,LIGHT_UP4},
 		{LIGHT_DOWN1, LIGHT_DOWN2, LIGHT_DOWN3,LIGHT_DOWN4},
 		{LIGHT_COMMAND1, LIGHT_COMMAND2, LIGHT_COMMAND3,LIGHT_COMMAND4}
 	};
 	
 	private static final int[][] SIGNAL_CHANNELS = {
 		{FLOOR_UP1,FLOOR_UP2,FLOOR_UP3,FLOOR_UP4},
 		{FLOOR_DOWN1,FLOOR_DOWN2,FLOOR_DOWN3,FLOOR_DOWN4},
 		{FLOOR_COMMAND1,FLOOR_COMMAND2,FLOOR_COMMAND3,FLOOR_COMMAND4},
 		{FLOOR_SENSOR1,FLOOR_SENSOR2,FLOOR_SENSOR3,FLOOR_SENSOR4},
 		{STOP},
 		{OBSTRUCTION}
 	};
 	
 	static{
 		System.loadLibrary("io");
 	}
 	
 	private static Driver instance = null;
 	
 	public static Driver makeInstance(Class<? extends Driver> driverClass){
 		try {
 			return instance = driverClass.newInstance();
 		} catch (InstantiationException e) {
 			throw new IllegalArgumentException(e);
 		} catch (IllegalAccessException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 	
 	public static Driver getInstance(){
 		if(instance == null){
 			throw new IllegalStateException("Instance is null, call Driver.makeInstance first.");
 		}
 		return instance;
 	}
 	
 	protected Driver(){
 		if(io_init() == 0){
 			System.out.println("io failed to initialize, exiting.");
 			System.exit(1);
 		}
 		clearElevatorState();
 	}
 
 	private native int io_init();
 	
 	private native int io_read_bit(int channel);
 
 	private native void io_set_bit(int channel);
 	
 	private native void io_clear_bit(int channel);
 	
 	private native int io_read_analog(int channel);
 	
 	private native void io_write_analog(int channel, int value);
 	
 	private int lastSpeed = 0;
 	
 	public void setSpeed(int speed){
 		
 		// If to start (speed > 0)
 		if (speed > 0) 
 			io_clear_bit(MOTORDIR);
 		else if (speed < 0) 
 			io_set_bit(MOTORDIR);
 
 		// If to stop (speed == 0)
 		else if (lastSpeed < 0)
 			io_clear_bit(MOTORDIR);
 		else if (lastSpeed > 0)
 			io_set_bit(MOTORDIR);
 
 		lastSpeed = speed;
 
 		// Write new setting to motor.
 		io_write_analog(MOTOR, 2048 + 2*Math.abs(speed));
 	}
 	
 	public void setDoorOpenLamp(boolean on) {
 		if(on){
 			io_set_bit(DOOR_OPEN);
 		} else {
 			io_clear_bit(DOOR_OPEN);
 		}
 	}
 	
 	public void setStopLamp(boolean on) {
 		if(on){
 			io_set_bit(LIGHT_STOP);
 		} else {
 			io_clear_bit(LIGHT_STOP);
 		}
 	}
 	
 	public void setFloorIndicator(int floor) {
 		legalFloor(floor);
 		
 		
 		if((floor & 0x01) != 0){
 			io_set_bit(FLOOR_IND2); 
 		} else {
 			io_clear_bit(FLOOR_IND2);
 		}		
 		
 		if((floor & 0x02) != 0){
 			io_set_bit(FLOOR_IND1);
 		} else {
 			io_clear_bit(FLOOR_IND1);
 		}
 	}
 
 	private void legalFloor(int floor) {
 		if(floor < 0 || floor >= NUMBER_OF_FLOORS){
 			throw new IllegalArgumentException("Not a legal floor: " + floor);
 		}
 	}
 	
 	public void setButtonLamp(Call call, int floor, boolean on) {
 		legalFloor(floor);
 		
 		if(on){
 			io_set_bit(LAMP_CHANNELS[call.ordinal()][floor]);
 		} else {
 			io_clear_bit(LAMP_CHANNELS[call.ordinal()][floor]);
 		}
 	}
 	
 	public int getFloorSensorState(){
 		for(int floor = 0; floor < NUMBER_OF_FLOORS; floor++){
 			if(io_read_bit(SIGNAL_CHANNELS[SignalType.SENSOR.ordinal()][floor]) == 1){
 				return floor;
 			}
 		}
 		return -1;
 	}
 
 	public void resetAllLamps() {
 		setStopLamp(false);
 		setDoorOpenLamp(false);
 		for(int floor = 0; floor < NUMBER_OF_FLOORS; floor++){
 			setButtonLamp(Call.DOWN,floor,false);
 			setButtonLamp(Call.UP,floor,false);
 			setButtonLamp(Call.COMMAND,floor,false);
 		}
 	}
 	
 	public void clearElevatorState(){
 		resetAllLamps();
 		setSpeed(0);
 	}
 	
 	private Thread callback;
 	private boolean running;
 	
 	private boolean[][] previousValue = new boolean[SignalType.values().length][NUMBER_OF_FLOORS];
	private long stopPressed;
 	private long stopButtonDelay = 2000; // default stop button delay is 2000 ms
 
 	public void setStopButtonDelay(int delay){
 		stopButtonDelay = Math.max(0,delay);
 	}
 	
 	public void startCallbacks(){
 		callback = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				running = true;
 				while(running){
 					for(SignalType type : SignalType.values()){
 						for(int floor = 0; floor < SIGNAL_CHANNELS[type.ordinal()].length; floor++){
 							boolean value = io_read_bit(SIGNAL_CHANNELS[type.ordinal()][floor]) == 1;
 							
 							// If value has not changed, ignore.
							if(value != previousValue[type.ordinal()][floor] || type == SignalType.STOP){
 								switch(type){
 								case CALL_UP:
 									if(value)
 										buttonPressed(Call.UP, floor);
 									break;
 								case CALL_DOWN:
 									if(value)
 										buttonPressed(Call.DOWN,floor);
 									break;
 								case CALL_COMMAND:
 									if(value)
 										buttonPressed(Call.COMMAND,floor);
 									break;
 								case SENSOR:
 									floorSensorTriggered(floor, value);
 									break;
 								case STOP:
 									if(value){
 										stopPressed = System.currentTimeMillis();
 									} else {
 										long now = System.currentTimeMillis();
 										if(now - stopPressed > stopButtonDelay){
 											stopButtonPressed();
 										}
 									}
 									break;
 								case OBSTRUCTION:
 									obstructionSensorTriggered(value);
 									break;
 								default:
 									throw new RuntimeException("Unknown SignalType:" + type);
 								}
 								previousValue[type.ordinal()][floor] = value;
 							}
 						}
 					}
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {}
 				}
 			}
 		});
 		callback.start();
 	}
 	
 	public void stopCallbacks() {
 		running = false;
 		try {
 			callback.join();
 		} catch (InterruptedException e) {}
 	}
 	
 	protected abstract void buttonPressed(Call call, int floor);
 	
 	protected abstract void floorSensorTriggered(int floor, boolean arriving);
 	
 	protected abstract void stopButtonPressed();
 	
 	protected abstract void obstructionSensorTriggered(boolean enabled);
 	
 	public static enum Call {
 		UP, DOWN, COMMAND;
 	}
 	
 	public static enum SignalType {
 		CALL_UP,CALL_DOWN,CALL_COMMAND,SENSOR,STOP,OBSTRUCTION;
 	}
 }
