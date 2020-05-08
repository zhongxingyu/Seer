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
 
 package cbccore.sensors.buttons;
 
 import cbccore.events.Event;
 import cbccore.events.EventType;
 import cbccore.events.EventManager;
 
 /**
  * An unintuitive but necessary class to interface buttons with the event system
  * Call ButtonEmitter.get().start(); to begin listening for button events.
  * ButtonEmitter.get().exit(); will stop listening for button events. Connect
  * Listeners that you make to the EventTypes available in this class, and when
  * a button is pressed/released, the handler will be called.
  * 
  * @author Braden McDorman, Benjamin Woodruff
  */
 
 @SuppressWarnings("unchecked")
 public class ButtonEmitter extends Thread {
 	
 	@SuppressWarnings("unused")
 	private EventManager manager;
 	
 	// Pressed EventTypes
 	public static EventType AButtonPressed = EventManager.get().getUniqueEventType();
 	public static EventType BButtonPressed = EventManager.get().getUniqueEventType();
 	public static EventType BlackButtonPressed = EventManager.get().getUniqueEventType();
 	public static EventType DownButtonPressed = EventManager.get().getUniqueEventType();
 	public static EventType UpButtonPressed = EventManager.get().getUniqueEventType();
 	public static EventType LeftButtonPressed = EventManager.get().getUniqueEventType();
 	public static EventType RightButtonPressed = EventManager.get().getUniqueEventType();
 	
 	// Released EventTypes
 	public static EventType AButtonReleased = EventManager.get().getUniqueEventType();
 	public static EventType BButtonReleased = EventManager.get().getUniqueEventType();
 	public static EventType BlackButtonReleased = EventManager.get().getUniqueEventType();
 	public static EventType DownButtonReleased = EventManager.get().getUniqueEventType();
 	public static EventType UpButtonReleased = EventManager.get().getUniqueEventType();
 	public static EventType LeftButtonReleased = EventManager.get().getUniqueEventType();
 	public static EventType RightButtonReleased = EventManager.get().getUniqueEventType();
 	
 	// Pressed Events
 	private static Event AButtonPressedEvent = new Event(AButtonPressed, get());
 	private static Event BButtonPressedEvent = new Event(BButtonPressed, get());
 	private static Event BlackButtonPressedEvent = new Event(BlackButtonPressed, get());
 	private static Event DownButtonPressedEvent = new Event(DownButtonPressed, get());
 	private static Event UpButtonPressedEvent = new Event(UpButtonPressed, get());
 	private static Event LeftButtonPressedEvent = new Event(LeftButtonPressed, get());
 	private static Event RightButtonPressedEvent = new Event(RightButtonPressed, get());
 	
 	// Released Events
 	private static Event AButtonReleasedEvent = new Event(AButtonReleased, get());
 	private static Event BButtonReleasedEvent = new Event(BButtonReleased, get());
 	private static Event BlackButtonReleasedEvent = new Event(BlackButtonReleased, get());
 	private static Event DownButtonReleasedEvent = new Event(DownButtonReleased, get());
 	private static Event UpButtonReleasedEvent = new Event(UpButtonReleased, get());
 	private static Event LeftButtonReleasedEvent = new Event(LeftButtonReleased, get());
 	private static Event RightButtonReleasedEvent = new Event(RightButtonReleased, get());
 	
 	
 	private AButton aButton = new AButton();
 	private BButton bButton = new BButton();
 	private BlackButton blackButton = new BlackButton();
 	private DownButton downButton = new DownButton();
 	private UpButton upButton = new UpButton();
 	private LeftButton leftButton = new LeftButton();
 	private RightButton rightButton = new RightButton();
 	
 	private boolean aButtonState = false;
 	private boolean bButtonState = false;
 	private boolean blackButtonState = false;
 	private boolean downButtonState = false;
 	private boolean upButtonState = false;
 	private boolean leftButtonState = false;
 	private boolean rightButtonState = false;
 	
 	private boolean exit = false;
 	
 	private static ButtonEmitter instance = null;
 
 	public ButtonEmitter(EventManager manager) {
		setDaemon(true);
 		this.manager = manager;
 	}
 
 	public static ButtonEmitter get() {
 		if (instance == null)
 			instance = new ButtonEmitter(EventManager.get());
 		return instance;
 	}
 	
 	/**
 	 * Don't call this function. Call start() instead.
 	 */
 	@Override
 	public void run() {
 		while (!exit) {
 			boolean newState = aButton.getValue();
 			if (newState != aButtonState) {
 				if (newState) {
 					AButtonPressedEvent.emit();
 				} else {
 					AButtonReleasedEvent.emit();
 				}
 				aButtonState = newState;
 			}
 			newState = bButton.getValue();
 			if (newState != bButtonState) {
 				if (newState) {
 					BButtonPressedEvent.emit();
 				} else {
 					BButtonReleasedEvent.emit();
 				}
 				bButtonState = newState;
 			}
 			newState = blackButton.getValue();
 			if (newState != blackButtonState) {
 				if (newState) {
 					BlackButtonPressedEvent.emit();
 				} else {
 					BlackButtonReleasedEvent.emit();
 				}
 				blackButtonState = newState;
 			}
 			newState = downButton.getValue();
 			if (newState != downButtonState) {
 				if (newState) {
 					DownButtonPressedEvent.emit();
 				} else {
 					DownButtonReleasedEvent.emit();
 				}
 				downButtonState = newState;
 			}
 			newState = upButton.getValue();
 			if (newState != upButtonState) {
 				if (newState) {
 					UpButtonPressedEvent.emit();
 				} else {
 					UpButtonReleasedEvent.emit();
 				}
 				upButtonState = newState;
 			}
 			newState = leftButton.getValue();
 			if (newState != leftButtonState) {
 				if (newState) {
 					LeftButtonPressedEvent.emit();
 				} else {
 					LeftButtonReleasedEvent.emit();
 				}
 				leftButtonState = newState;
 			}
 			newState = rightButton.getValue();
 			if (newState != rightButtonState) {
 				if (newState) {
 					RightButtonPressedEvent.emit();
 				} else {
 					RightButtonReleasedEvent.emit();
 				}
 				rightButtonState = newState;
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
