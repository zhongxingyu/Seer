 /*
  * This file is part of CBCJVM.
  * CBCJVM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CBCJVM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CBCJVM.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cbccore.sensors.digital;
 
 import cbccore.sensors.IBooleanSensor;
 import cbccore.events.Event;
 import cbccore.events.EventType;
 import cbccore.events.EventManager;
 
 /**
  * An unintuitive but necessary class to interface buttons with the event system
  * Call DigitalSensorEmitter.get().start(); to begin listening for button
  * events. DigitalSensorEmitter.get().exit(); will stop listening for button
  * events. Connect Listeners that you make to the EventTypes available in this
  * class, and when a button is pressed/released, the handler will be called.
  * 
  * @author Braden McDorman, Benjamin Woodruff
  */
 @SuppressWarnings("unchecked")
 public class DigitalSensorEmitter extends Thread {
 	
 	private EventManager manager;
 	
 	private static final int minPort = 0;
 	private static final int maxPort = 7;
 	
 	private IBooleanSensor[] sensors = new IBooleanSensor[maxPort-minPort];
 	private Event[] sensorOnEvents = new Event[maxPort-minPort];
 	private Event[] sensorOffEvents = new Event[maxPort-minPort];
 	private EventType[] sensorOnEventTypes = new EventType[maxPort-minPort];
 	private EventType[] sensorOffEventTypes = new EventType[maxPort-minPort];
 	private boolean[] sensorStates = new boolean[maxPort-minPort];
 	
 	private boolean exit = false;
 	
 	private static DigitalSensorEmitter instance = null;
 
 	public DigitalSensorEmitter(EventManager manager) {
		setDaemon(true);
 		this.manager = manager;
 	}
 
 	public static DigitalSensorEmitter get() {
 		if (instance == null)
 			instance = new DigitalSensorEmitter(EventManager.get());
 		return instance;
 	}
 	
 	
 	public EventType getTouchPressedType(int index) {
 		index -= minPort;
 		EventType type = sensorOnEventTypes[index];
 		if(type != null) {
 			if(!(sensors[index] instanceof Touch)) {
 				throw new RuntimeException("EventType was already allocated to a different type of sensor.");
 			}
 			return sensorOnEventTypes[index];
 		}
 		sensorOnEventTypes[index] = manager.getUniqueEventType();
 		sensorOnEvents[index] = new Event(sensorOnEventTypes[index], this);
 		return sensorOnEventTypes[index];
 	}
 	
 	public EventType getTouchReleasedType(int index) {
 		index -= minPort;
 		EventType type = sensorOffEventTypes[index];
 		if(type != null) {
 			if(!(sensors[index] instanceof Touch)) {
 				throw new RuntimeException("EventType was already allocated to a different type of sensor.");
 			}
 			return sensorOffEventTypes[index];
 		}
 		sensorOffEventTypes[index] = manager.getUniqueEventType();
 		sensorOffEvents[index] = new Event(sensorOffEventTypes[index], this);
 		return sensorOffEventTypes[index];
 	}
 	
 	
 	
 	public EventType getBeamPressedType(int index) {
 		index -= minPort;
 		EventType type = sensorOnEventTypes[index];
 		if(type != null) {
 			if(!(sensors[index] instanceof BreakBeam)) {
 				throw new RuntimeException("EventType was already allocated to a different type of sensor.");
 			}
 			return sensorOnEventTypes[index];
 		}
 		sensorOnEventTypes[index] = manager.getUniqueEventType();
 		sensorOnEvents[index] = new Event(sensorOnEventTypes[index], this);
 		return sensorOnEventTypes[index];
 	}
 	
 	public EventType getBeamReleasedType(int index) {
 		index -= minPort;
 		EventType type = sensorOffEventTypes[index];
 		if(type != null) {
 			if(!(sensors[index] instanceof BreakBeam)) {
 				throw new RuntimeException("EventType was already allocated to a different type of sensor.");
 			}
 			return sensorOffEventTypes[index];
 		}
 		sensorOffEventTypes[index] = manager.getUniqueEventType();
 		sensorOffEvents[index] = new Event(sensorOffEventTypes[index], this);
 		return sensorOffEventTypes[index];
 	}
 	
 	
 	/**
 	 * Don't call this function. Call start() instead.
 	 */
 	@Override
 	public void run() {
 		while (!exit) {
 			for(int i = 0; i < sensors.length; ++i) {
 				if(sensors[i] != null) {
 					
 					
 					
 					boolean newState = sensors[i].getValue();
 					if (newState != sensorStates[i]) {
 						if (newState) {
 							sensorOnEvents[i].emit();
 						} else {
 							sensorOffEvents[i].emit();
 						}
 						sensorStates[i] = newState;
 					}
 					
 					
 					
 					
 				}
 			}
 			Thread.yield();
 			try { Thread.sleep(0l, 1); } catch (Exception e) {}
 		}
 		exit = false;
 	}
 	
 	//dangerous
 	public void exit() {
 		exit = true;
 	}
 }
