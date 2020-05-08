 package controller;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import elevator.Elevators;
 
 /**
  * 
  * @author Mattias Knutsson and Andreas Gustafsson
  *
  */
 public class MasterController extends Thread {
 	private Socket s;
 	private BufferedReader in;									// The reader from the socket 
 	private PrintWriter out;									// The writer to the socket
 	private final ArrayList<ElevatorController> controllers; 	// The list of controllers
	private final int N;
 
	public MasterController(int N) {
		this.N = N;
 		controllers = new ArrayList<ElevatorController>();
 	}
 
 
 
	public static void main(String[] args) {
		MasterController mc;
		if(args.length>0)
			mc=new MasterController(Integer.valueOf(args[0]));
		else
			mc=new MasterController(1);
		mc.start();
	}
 	/**
 	 * Initialize the socket and read/write, then run controllElevators
 	 */
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				s = new Socket("localhost", 4711);
 				in = new BufferedReader(new InputStreamReader(
 						s.getInputStream()));
 				out = new PrintWriter(s.getOutputStream(), true);
 				controllElevators();
 			} catch (UnknownHostException e) {
 				continue;
 			} catch (IOException e) {
 				continue;
 			}
 			break;
 		}
 	}
 
 	/**
 	 * 
 	 * @throws SocketException
 	 */
 	private void controllElevators() throws SocketException {
 		System.err.println("Controller starts.");
		for (int i = 0; i < N; i++) {			//Start all controllers
 			controllers.add(new ElevatorController(i + 1));
 			controllers.get(i).start();
 		}
 		
 		//A thread that read the controllers outbox and send to Elevators
 		new Thread(new Runnable() {
 			public void run() {
 				while (true) {
 					for (ElevatorController c : controllers) {
 						Message msg = null;
 						if ((msg = c.retrieveMessage()) != null) {
 							out.println(msg.toString());
 						}
 					}
 					Thread.yield();
 				}
 			}
 		}).start();
 		
 		
 		//Read input from Elevators and convert to a message
 		//If the message is a b-message send it to assigner
 		//Else send it to the correct controller
 		while (true) {
 			while (!s.isInputShutdown()) {
 				String[] message = null;
 				try {
 					message = in.readLine().split(" "); //Parse the message
 					char type = message[0].charAt(0);
 					int elevator = Integer.valueOf(message[1]);
 					double modifier = Double.valueOf(message[2]);
 					if (type == 'b') {
 						new Assigner((int) elevator, (int) modifier).start(); //If b-message send to assigner
 						continue;
 					}
 					
 					//Create and send message
 					Message msg = new Message(type, elevator, (int) modifier,
 							modifier);
 					controllers.get(elevator - 1).postMessage(msg);
 				} catch (Exception e) {
 				}
 
 			}
 		}
 	}
 	/**
 	 * Assigner is threads trying to assign a task to a elevator 
 	 * @author Mattias Knutsson and Andreas Gustafsson
 	 */
 	private class Assigner extends Thread {
 
 		private final int floor;
 		private final int direction;
 
 		/**
 		 * Constructs a Assigner.
 		 * @param floor - The floor a person want to leave.
 		 * @param direction - The direction the person want to go.
 		 */
 		public Assigner(int floor, int direction) {
 			this.floor = floor;
 			this.direction = direction;
 		}
 
 		/**
 		 * Try to assign the task to a elevatorController
 		 * The assigner prefers to assign the task to a elevator passing the floor
 		 * If that isn't possible take the closest unassigned elevator
 		 * Or what until one of this gets possible
 		 */
 		@Override
 		public void run() {
 			ElevatorController closestEmpty = null, closestJoin = null;
 			double costEmpty, costJoin;
 			costEmpty = costJoin = Double.POSITIVE_INFINITY;
 			for (boolean first = true; closestEmpty == null
 					&& closestJoin == null; first = false) { //While the task haven't a possible assignable elevatorController
 				if (!first)
 					Thread.yield();							//Yield at the start everytime except the first 
 
 				for (ElevatorController c : controllers) { 	
 					
 					// Join to a current tour
 					if (c.getIntendedDirection() == direction				//If the elevators direction is same as the persons direction and the elevator havn't yet passed the floor
 							&& c.getDirection() * c.getFloor() < c			
 									.getDirection() * floor) {
 						double thisCost = Math.abs(floor - c.getFloor()) + 2	//Calculate the cost
 								* c.getTaskQueueSize();
 						if (thisCost < costJoin) {							
 							costJoin = thisCost;
 							closestJoin = c;									//Set the controller as closest if it has the least cost
 						}
 					}else if (c.getIntendedDirection()!=c.getDirection() && c.getIntendedDirection()==direction){ //If the elevator have the same intented direction but not started to move in that direction yet
 						double thisCost = Math.abs(floor - c.getFloor()) + 2	
 								* c.getTaskQueueSize();
 						if (thisCost < costJoin) {
 							costJoin = thisCost;
 							closestJoin = c;									//Set the controller as closest if it has the least cost
 						}
 					}
 
 					// Assign Empty ELevator
 					if (c.getTaskQueueSize() == 0 && c.getInboxSize() == 0) {	//If the elevator is unassigned
 						double thisCost = Math.abs(floor - c.getFloor()) + 2
 								* c.getTaskQueueSize();
 						if (thisCost < costEmpty) {
 							costEmpty = thisCost;
 							closestEmpty = c;									//Set the controller as closest if it has the least cost
 						}
 					}
 				}
 			}
 			ElevatorController cf = closestEmpty;		 
 			if (closestJoin != null)				//If the is a possible join 
 				cf = closestJoin;
 			else
 				cf.setIntendedDirection(direction);
 
 			Message m = new Message('p', cf.getElevator(), floor, floor); 	//Create the message translated to a p-message
 			System.out.println("ASSIGNER MESSAGE: " + m);
 			cf.postMessage(m);												//Send the message
 		}
 	}
 }
