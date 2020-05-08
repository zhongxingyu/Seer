 package controller;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Queue;
 
 /**
  * Controll
  * @author Mattias Knutsson and Andreas Gustafsson
  *
  */
 public class ElevatorController extends Thread {
 	private final Queue<Message> inbox;			//The inbox with messages from MasterController
 	private final Queue<Message> outbox;		//The outbox with messages to MasterController
 	private final ArrayList<Message> taskQueue;	//The queue with tasks the controller will execute
 	private double floor;						//The current position
 	private double targetFloor;					//The destination floor
 	private final int elevator;					//The ID
 	private int direction;						//The direction (1 = UP, -1 = DOWN)
 	private int intendedDirection;				//The intended direction (the direction after first pickup)
 
 	/**
 	 * 	Constructs a new elevator with standard settings
 	 * @param elevator - The number ID of the elevator
 	 */
 	public ElevatorController(int elevator) {
 		setDirection(0);
 		setIntendedDirection(0);
 		floor = setTargetFloor(0);
 		this.elevator = elevator;
 		taskQueue = new ArrayList<Message>();
 		outbox = new ArrayDeque<Message>();
 		inbox = new ArrayDeque<Message>();
 	}
 
 	/**
 	 * The running class in a elevator controller.
 	 * Parsed message from MasterController and run tasks from a queue
 	 */
 	@Override
 	public void run() {
 		try {
 			while (true) {
 				while (inbox.isEmpty() && taskQueue.isEmpty()){ 	//While elevator can stay idle
 					Thread.yield();
 				}
 				Message m = peekQueue();							//Looks at a the top task in the elevators quere
 				if (m != null) {									//If was a task in queue
 					setTargetFloor(m.getTargetFloor());				//Set target to top task floor
					if (getFloor() >= getTargetFloor() - 0.05 && getFloor() <= getTargetFloor() + 0.05) { //If you already are on the correct floor
 						doorAction();								//Open the doors
 						pollQueue();								//Remove the task from the queue 
 						setDirection(0);							//Set the elevator to not move
 						continue;									
 					}
 					decideMove();									//Get the elevator moving in the correct direction
 				}
 				Message msg = pollMessage();						//Get message from mastercontroller
 				if (msg != null) {
 					switch (msg.getType()) {						//Get the type of the message
 					case 'p':										//P-Message is a message with a move to this floor order
 						if (msg.getTargetFloor() == 32000) {		//Emergency stop
 							addMessage(new Message('m', getElevator(), 0, 0)); //Stop elevator
 							synchronized (this) {
 								inbox.clear();						//Clear both queues
 								taskQueue.clear();
 							}
 							setDirection(0);						//Set direction to not move
							continue;
 						}	
 						addQueue(msg);								//Add the message to the queue					
 						break;
 					case 'f':										//F-Message information to the controller about the elevators current position 
 						if (getDirection() == 0)					//If the elevator have stopped moving
 							continue;
 						setFloor(msg.getcurPos());					//Set the elevators position					
 						if (Math.abs(getFloor() - getTargetFloor()) < 0.05) { 	//If the correct floor is reached
 							addMessage(new Message('m', getElevator(), 0, 0));	//Stop the elevator
 							doorAction();										//Open and Close the door
 							pollQueue();										//Remove the task from the taskqueue
 							setDirection(0);									//Set the direction to not moving
 						}
 						break;
 					default:
 						System.err.println("Unhandled message.");
 					}
 				}
 				decideMove();										//Get the elevator moving in the correct direction					
 				Thread.yield();										//Yield the remaining timeslice
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Adding a task to taskqueue sorted by prior order (earlies approach first)
 	 * @param msg
 	 */
 	public synchronized void addQueue(Message msg) {
 		System.out.println("Adding task: " + msg);
 
 		if (!(getDirection() * msg.getcurPos() < getDirection() 		//Assume that the task is in the elevators direction
 				* getFloor())) {
 			for (int i = 0; i < taskQueue.size(); i++) {				//Sort in the task in correct position
 				if (getDirection() * msg.getcurPos() < getDirection()
 						* taskQueue.get(i).getcurPos()) {
 					taskQueue.add(i, msg);								//Add task to position i in the queue
 					return;
 				}
 			}
 			taskQueue.add(msg);											//Add last in queue
 		}else{
 			System.err.println("The Elevator is not heading that way, douche.");	//If a button push is in the wrong direction
 		}
 	}
 
 	/**
 	 * Let the controller check the top prior task
 	 * @return The Message in top of the taskqueue 
 	 */
 	public synchronized Message peekQueue() {
 		return taskQueue.isEmpty() ? null : taskQueue.get(0);
 	}
 
 	/**
 	 * Remove and returns the first element in the taskqueue
 	 * @return the message
 	 */
 	public synchronized Message pollQueue() {
 		if(taskQueue.size() == 1) setIntendedDirection(0);
 		return taskQueue.isEmpty() ? null : taskQueue.remove(0);
 	}
 
 	/**
 	 * Sets direction and sends a message to start move the elevator in the correct direction
 	 */
 	private void decideMove() {
 		if (getDirection() == 0
 				&& Math.abs(getFloor() - getTargetFloor()) > 0.05) {
 			System.err.println("DECIDE MOVE");
 			int modifier;
 			if (getFloor() < getTargetFloor() - 0.05) { 		//If the elevator should move up
 				System.err.println("Going up.");
 				setDirection(1);
 				modifier = 1;
 			} else if (getFloor() > getTargetFloor() + 0.05) {	//If the elevator should move down
 				System.err.println("Going down.");
 				setDirection(-1);
 				modifier = -1;
 			} else {											//If the elevator is in the correct floor
 				return;		
 			}
 			Message msg1 = new Message('m', getElevator(), modifier, 0);	
 			addMessage(msg1);									//Add the message to outbox
 		}
 	}
 
 	/**
 	 * Send message to open the door, wait a sec, then close the doors again
 	 * @throws InterruptedException If the sleep interrupts
 	 */
 	private void doorAction() throws InterruptedException {
 		addMessage(new Message('d', getElevator(), 1, 0));
 		Thread.sleep(1000);
 		addMessage(new Message('d', getElevator(), -1, 0));
 		Thread.sleep(1000);
 	}
 
 
 
 	/**
 	 * Below here is only getters and setters.
 	 */
 
 	public synchronized Message retrieveMessage() {
 		return outbox.poll();
 	}
 
 	public synchronized void addMessage(Message msg) {
 		System.err.println("Adding message: " + msg);
 		outbox.add(msg);
 	}
 
 	public synchronized int getInboxSize(){
 		return inbox.size();
 	}
 
 	public synchronized int getTaskQueueSize(){
 		return taskQueue.size();
 	}
 
 	public synchronized void postMessage(Message msg) {
 		inbox.add(msg);
 	}
 
 	public synchronized Message pollMessage() {
 		return inbox.poll();
 	}
 
 	public synchronized double getFloor() {
 		return floor;
 	}
 
 	public synchronized void setFloor(double floor) {
 		this.floor = floor;
 	}
 
 	public synchronized int getDirection() {
 		return direction;
 	}
 
 	public synchronized void setDirection(int direction) {
 		// System.err.println("Setting direction to " + direction);
 		this.direction = direction;
 	}
 
 	public synchronized double getTargetFloor() {
 		return targetFloor;
 	}
 
 	public synchronized double setTargetFloor(double targetFloor) {
 		this.targetFloor = targetFloor;
 		return targetFloor;
 	}
 
 	public int getElevator() {
 		return elevator;
 	}
 
 	public synchronized int getIntendedDirection() {
 		return intendedDirection;
 	}
 
 	public synchronized void setIntendedDirection(int intendedDirection) {
 		this.intendedDirection = intendedDirection;
 	}
 
 }
