 package customAgents;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import planning.AStar;
 import planning.Cell;
 import planning.Map;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.TickerBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 public class RobotAgent extends Agent {
 	final int IDLE = 0;
 	final static int ITEMPICKUP = 1;
 	final static int STORAGEAGENT = 2;
 	final static int ITEMDROPDOWN = 3;
 
 	final static char MAPMOVEPLACE = '.';
 	final static char MAPPRODUCTPLACE = 'P';
 	final static char MAPOUTPUTQUEUE = 'o';
 	final static char MAPINPUTQUEUE = 'I';
 	final static char MAPWALL = 'X';
 
 	public Point location;// current location of robot
 	boolean claimDropdownID;
 	private AID[] storageAgents;// list of active storage agents
 	private AID[] guiAgents;// list of active guiAgents
 	public boolean movementVerified;
 	public List<Point> travelPoints = new ArrayList<Point>();// in this list is
 																// an ordered
 																// array of
 																// points where
 																// the robot is
 																// going
 	public List<Point> moveMentQueue = new ArrayList<Point>();
 	public List<Point> occupiedPoints = new ArrayList<Point>();
 	public Point holdingItem;// the warehouse coords of the current holding
 								// item, 0,0 is no item
 	public List<Integer> nextDestination = new ArrayList<Integer>();// 0=nothing,
 																	// 1=itemPickup,
 																	// 2=storageAgent,
 																	// 3=itemDropdown
 	public boolean awaitingRelease;// will be true if robot agent enters a
 									// storage agent, after receiving a GO
 									// message it will be false again
 	private Map uiMap = null;
 
 	/**
 	 * refreshes the list of current active GuiAgents
 	 */
 	public void updateGuiAgents() {
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-GuiAgent");
 		template.addServices(sd);
 		try {
 			DFAgentDescription[] result = DFService.search(this, template);
 			guiAgents = new AID[result.length];
 			System.out.println("Search performed, result amount: "
 					+ result.length);
 			for (int i = 0; i < result.length; ++i) {
 				guiAgents[i] = result[i].getName();
 				System.out.println(guiAgents[i].getName());
 			}
 		} catch (FIPAException e) {
 			e.printStackTrace();
 			System.out.println("in the catch");
 		}
 	}
 
 	/**
 	 * refreshes the list of current active StorageAgents
 	 */
 	public void updateStorageAgents() {
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-StorageAgent");
 		template.addServices(sd);
 		try {
 			DFAgentDescription[] result = DFService.search(this, template);
 			storageAgents = new AID[result.length];
 			System.out.println("Search performed, result amount: "
 					+ result.length);
 			for (int i = 0; i < result.length; ++i) {
 				storageAgents[i] = result[i].getName();
 				System.out.println(storageAgents[i].getName());
 			}
 		} catch (FIPAException e) {
 			e.printStackTrace();
 			System.out.println("in the catch");
 		}
 	}
 
 	/**
 	 * entry point of agent
 	 */
 	protected void setup() {
 		claimDropdownID = false;
 		holdingItem = new Point();
 		nextDestination.add(IDLE);
 		movementVerified = false;
 		awaitingRelease = false;
 		// uiMap = new Map();
 		updateGuiAgents();// Robot agent wont start without an active GuiAgent
 		if (guiAgents.length == 0) {
 			System.out
 					.println("No gui agent found, robot agent is terminating");
 			doDelete();
 		}
 
 		// RobotAgent wont start without coordination arguments (looks like: 1,1
 		// ) on startup
 		Object[] args = getArguments();
 		if (args != null && args.length > 0) {
 			location = new Point(Integer.parseInt((String) args[0]),
 					Integer.parseInt((String) args[1]));
 			System.out.println(getAID().getName() + " is positioned at "
 					+ location.x + "," + location.y);
 		} else {
 			// Make the agent terminate
 			System.out.println("No position defined, closing down");
 			doDelete();
 		}
 
 		System.out.println("Hallo! Robot-agent " + getAID().getName()
 				+ " is ready.");
 
 		// register at yellow pages serivce
 		DFAgentDescription dfd = new DFAgentDescription();
 		dfd.setName(getAID());
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-RobotAgent");
 		sd.setName("Warehouse-RobotAutomation");
 		dfd.addServices(sd);
 		try {
 			DFService.register(this, dfd);
 
 		} catch (FIPAException e) {
 			System.out.println("catch");
 			e.printStackTrace();
 		}
 
 		addBehaviour(new OfferRequestsServer());// bid on current requests
 		addBehaviour(new AcceptRequestServer());// accept an incomming request
 		addBehaviour(new MapReceiver());// awaits a map message from guiAgent
 		addBehaviour(new MapRequest());// one shot behaviour to load the map
 		addBehaviour(new MovementBehaviour(this, 1000));// every second the
 														// robot is allowed to
 														// move a spot
 		addBehaviour(new HopReply());// Behaviour which awaits incomming
 										// accept/decline after a hop claim
 										// request
 		addBehaviour(new ArrivalReply());// Behaviour which awaits incomming go
 											// after arriving at a storage agent
 
 	}
 
 	protected void takeDown() {
 		// Deregister from the yellow pages
 		try {
 			DFService.deregister(this);
 		} catch (FIPAException fe) {
 			fe.printStackTrace();
 		}
 		// Printout a dismissal message
 		System.out.println("Robot-agent " + getAID().getName()
 				+ " terminating.");
 	}
 
 	public Integer calculatePathCost(Point l) {
 		// the agent has a current position and a list of points (travelPoints)
 		// orderd to visit. return the current amount of spaces it has to move
 		// before being idle again
 		// temporarily add the given point to the end of travelPoints, a request
 		// has been made how far the robot has to move to THAT point
 
 		if (uiMap == null)
 			return 1000000000;
 
 		AStar finder = new AStar();
 		Cell[] path = finder.findPath(uiMap, location, l);
 		if (path != null) {
 			return path.length;
 		}
 
 		return (5);// Debugging purpose in version without a loaded map
 	}
 
 	private class OfferRequestsServer extends CyclicBehaviour {
 		public void action() {// listen to incomming CFP from storageAgents
 			MessageTemplate mt = MessageTemplate
 					.MatchPerformative(ACLMessage.CFP);
 			ACLMessage msg = myAgent.receive(mt);
 			if (msg != null) {// only respond to CFP messages
 				// CFP Message received. Process it
 				String requestedLocation = msg.getContent();
 				ACLMessage reply = msg.createReply();// add the sender as
 														// recipient
 				Integer x = Integer.parseInt(requestedLocation.substring(0,
 						requestedLocation.lastIndexOf(',')));// parse the x of
 																// product
 				Integer y = Integer.parseInt(requestedLocation.substring(
 						requestedLocation.lastIndexOf(',') + 1,
 						requestedLocation.length()));// parse the y of product
 				Integer price = calculatePathCost(new Point(x, y));
 				if (holdingItem.x == x && holdingItem.y == y) {// robot is
 																// carrying the
 																// requested
 																// item
 					price = 1;
 				}
 				if (price != null) {
 					reply.setPerformative(ACLMessage.PROPOSE);
 					reply.setContent(String.valueOf(price.intValue()));
 					// System.out.println(getAID().getName() + ": My cost is " +
 					// price + ". To get to location: " +x + ","+y);//debugging
 					// purpose
 				} else {
 					reply.setPerformative(ACLMessage.REFUSE);
 					reply.setContent("Pathplanning error");
 				}
 				myAgent.send(reply);
 			} else {
 				block();
 			}
 		}
 	} // End of inner class OfferRequestsServer
 
 	private class AcceptRequestServer extends CyclicBehaviour {
 		public void action() {// confirm that this agent will pick up the item
 			MessageTemplate mt = MessageTemplate
 					.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
 			ACLMessage msg = myAgent.receive(mt);
 			if (msg != null) {// this agent is chosen to pick up the item
 				// ACCEPT_PROPOSAL Message received. Process it
 				String requestedLocation = msg.getContent();
 				Integer itemX = Integer.parseInt(requestedLocation.substring(0,
 						requestedLocation.indexOf(',', 1)));
 				Integer itemY = Integer.parseInt(requestedLocation.substring(
 						requestedLocation.indexOf(',', 1) + 1,
 						requestedLocation.indexOf(':', 1)));
 				Integer storageAgentX = Integer.parseInt(requestedLocation
 						.substring(requestedLocation.indexOf(':', 1) + 1,
 								requestedLocation.indexOf(',', 2)));
 				Integer storageAgentY = Integer.parseInt(requestedLocation
 						.substring(requestedLocation.indexOf(',', 2) + 1,
 								requestedLocation.length()));
 				ACLMessage reply = msg.createReply();
 
 				reply.setPerformative(ACLMessage.INFORM);
 				System.out.println(getAID().getName()
 						+ " will pick up the item at " + itemX + "," + itemY
 						+ " and bring it to storage agent: " + (storageAgentX)
 						+ "," + storageAgentY);
 				myAgent.send(reply);
 
 				if (holdingItem.x == itemX && holdingItem.y == itemY) {// robot
 																		// is
 																		// carrying
 																		// the
 																		// requested
 																		// item
 					if (nextDestination.get(0) == ITEMDROPDOWN)// stop dropdown
 																// go deliver
 																// the item
 					{
 						travelPoints.remove(0);
 						nextDestination.remove(0);
 						travelPoints.add(0, new Point(storageAgentX - 11,
 								storageAgentY));
 						travelPoints.add(1, new Point(storageAgentX - 1,
 								storageAgentY));
 						travelPoints.add(2, new Point(storageAgentX - 1,
 								storageAgentY - 1));
 						travelPoints.add(3, new Point(storageAgentX - 11,
 								storageAgentY - 1));
 						travelPoints.add(4, new Point(itemX, itemY));
 						nextDestination.add(0, STORAGEAGENT);
 						nextDestination.add(1, STORAGEAGENT);
 						nextDestination.add(2, ITEMDROPDOWN);
 						nextDestination.add(3, ITEMDROPDOWN);
 						nextDestination.add(1, ITEMDROPDOWN);
 
 					} else if (nextDestination.get(0) == STORAGEAGENT) {
 						travelPoints.remove(4);
 						nextDestination.remove(4);
 						travelPoints.add(4, new Point(storageAgentX - 11,
 								storageAgentY));
 						travelPoints.add(5, new Point(storageAgentX - 1,
 								storageAgentY));
 						travelPoints.add(6, new Point(storageAgentX - 1,
 								storageAgentY - 1));
 						travelPoints.add(7, new Point(storageAgentX - 11,
 								storageAgentY - 1));
 						travelPoints.add(8, new Point(itemX, itemY));
 						nextDestination.add(4, STORAGEAGENT);
 						nextDestination.add(5, STORAGEAGENT);
 						nextDestination.add(6, ITEMDROPDOWN);
 						nextDestination.add(7, ITEMDROPDOWN);
 						nextDestination.add(8, ITEMDROPDOWN);
 					}
 				} else {
 					travelPoints.add(new Point(itemX, itemY));
 					travelPoints.add(new Point(storageAgentX - 11,
 							storageAgentY));
 					travelPoints
 							.add(new Point(storageAgentX - 1, storageAgentY));
 					travelPoints.add(new Point(storageAgentX - 1,
 							storageAgentY - 1));
 					travelPoints.add(new Point(storageAgentX - 11,
 							storageAgentY - 1));
 					travelPoints.add(new Point(itemX, itemY));
 					nextDestination.add(ITEMPICKUP);
 					nextDestination.add(STORAGEAGENT);
 					nextDestination.add(STORAGEAGENT);
 					nextDestination.add(ITEMDROPDOWN);
 					nextDestination.add(ITEMDROPDOWN);
 					nextDestination.add(ITEMDROPDOWN);
 					nextDestination.add(ITEMDROPDOWN);
 				}
 			} else {
 				block();
 			}
 		}
 	} // End of inner class OfferRequestsServer
 
 	public void calculateNextHop() {
 		// look at list of travel points, and the list of occupiedPoints.
 		// calculate next 3 areas and put them in the list movementQueue
 
 		if (this.travelPoints.size() == 0)
 			return;
 
 		Point nextDest = this.travelPoints.get(0);
 		AStar finder = new AStar();
 		Cell[] path = finder.findPath(uiMap, location, nextDest);
 		if (path != null) {
 			this.moveMentQueue.add(path[0].getPosition());
 			this.moveMentQueue.add(path[1].getPosition());
 			this.moveMentQueue.add(path[2].getPosition());
 		}
 
 		// fill moveMentQueue with the 3 found locations
 		addBehaviour(new HopRequest());// one shot behaviour to claim the hop
 	}
 
 	public void checkArrival() {
 
 		switch (nextDestination.get(0)) {
 		case ITEMPICKUP:// the real fysical robot would have to pick the item
 						// here
 			holdingItem.x = location.x;
 			holdingItem.y = location.y;
 			break;
 
 		case STORAGEAGENT:// the real fysical robot would have to deliver the
 							// item here
 			awaitingRelease = true;
 			addBehaviour(new ArrivedAtStorage());// one shot behaviour to notice
 													// all storage agents this
 													// agent is at one of them
 			break;
 
 		case ITEMDROPDOWN:// the real fysical robot would have to drop the item
 							// here
 			holdingItem.x = 0;
 			holdingItem.y = 0;
 			break;
 		}
 	}
 
 	public void createUIGraph(String uiStr) {
 		String[] sub = uiStr.split(";");
 		int width = Integer.parseInt(sub[0]);
 		int height = Integer.parseInt(sub[1]);
 		uiMap = new Map(width, height);
 		String map = sub[2];
 
 		char[][] strMap = new char[height][width];
 
 		for (int i = 0; i < height; i++) {
 			String line = map.substring((i * width), ((i * width) + width));
 			int linePos = 0;
 			for (char p : line.toCharArray()) {
 				/*switch (p) {
 				case MAPMOVEPLACE:
 					strMap[linePos][i] = p;// String.valueOf(p);//create movable
 											// position in graph
 					break;
 				case MAPOUTPUTQUEUE:
 					strMap[linePos][i] = p;// String.valueOf(p);//create output
 											// queue position in graph
 					break;
 				case MAPINPUTQUEUE:
 					strMap[linePos][i] = p;// String.valueOf(p);//create input
 											// queue position in graph
 					break;
 				case MAPPRODUCTPLACE:
 					strMap[linePos][i] = p;// String.valueOf(p);//create product
 											// position in graph
 					break;
 				}*/
 				strMap[i][linePos] = p;
 				linePos++;
 			}
 		}
 		this.createMap(strMap, width, height);
 	}
 
 	private void createMap(char[][] stringMap, int width, int height) {
 		for (int i = 0; i < height-1; i++) {
 			for (int j = 0; j < width-1; j++) {
 				char str = stringMap[i][j];
 				// Node n = createGraphNode(str, i, j);
 				Cell cl = new Cell();
 				cl.setPosition(new Point(j, i));
 				/*
 				 * MAPMOVEPLACE = '.'; MAPPRODUCTPLACE = 'P'; MAPOUTPUTQUEUE =
 				 * 'o'; MAPINPUTQUEUE = 'I'; MAPWALL = 'X';
 				 */
 				if (str == MAPMOVEPLACE || str == MAPOUTPUTQUEUE
 						|| str == MAPINPUTQUEUE) {
 					cl.setBlocked(false);
 				} else // is not movable create block
 				{
 					cl.setBlocked(true);
 				}
 				uiMap.addCell(cl);
 			}
 		}
 	}
 
 	private class MovementBehaviour extends TickerBehaviour {
 		public MovementBehaviour(Agent a, long period) {
 			super(a, period);
 		}
 
 		@Override
 		protected void onTick() {// every 1000 ms
 			if (awaitingRelease == false) {
 				if (moveMentQueue.size() == 0)// agent has no pending movement
 				{
 					if (travelPoints.size() != 0)// if there are actually points
 													// to visit
 					{
 						calculateNextHop();
 						try {
 							Thread.sleep(2);
 						} catch (InterruptedException e) {
 							e.printStackTrace();// wait a small while to receive
 												// a reply to the hop claim
 						}
 					}
 				}
 
 				if (moveMentQueue.size() > 0 && movementVerified) {
 					String conversationString = "";
 					String statusID = "V";// holding item
 					if (holdingItem.x == 0)// not holding item
 					{
 						statusID = "R";
 					}
 					if (claimDropdownID == true) {
 						statusID = "D";
 						claimDropdownID = false;
 					}
 					if (nextDestination.get(0) == ITEMDROPDOWN
 							&& moveMentQueue.get(0) == travelPoints.get(0)) {
 						claimDropdownID = true;
 					}
 					conversationString+=(statusID + ",");
 					conversationString+=(location.x + ",");
 					conversationString+=(location.y + ",");
					conversationString+=moveMentQueue.get(0).x;
 					conversationString+=moveMentQueue.get(0).y;
 					location = moveMentQueue.get(0);
 					moveMentQueue.remove(0);
 					if (location == travelPoints.get(0)) {
 						checkArrival();
 					}
 					ACLMessage mapUpd = new ACLMessage(ACLMessage.INFORM);
 					mapUpd.addReceiver(guiAgents[0]);// The gui agent needs to
 														// know that the robot
 														// actually did a move
 
 					mapUpd.setContent(conversationString);// x,y is for the
 																// GUI agent, it
 																// has to
 																// visualize
 																// movement
 					mapUpd.setConversationId("map-update");
 					myAgent.send(mapUpd);
 				}
 			}
 		}
 	}
 
 	private class ArrivedAtStorage extends Behaviour {// one shot
 		public void action() {// send a message with current coords, the storage
 								// agent knows it s own coords so it can be
 								// verified where the agent is
 			updateStorageAgents();
 			ACLMessage arrMsg = new ACLMessage(ACLMessage.INFORM);
 			updateStorageAgents();
 
 			for (int i = 0; i < storageAgents.length; ++i) {
 				arrMsg.addReceiver(storageAgents[i]);
 			}
 
 			arrMsg.setContent(location.x + "," + location.y + ";"
 					+ holdingItem.x + "," + holdingItem.y);
 			arrMsg.setConversationId("arrival-inform");
 			myAgent.send(arrMsg);
 			done();
 		}
 
 		@Override
 		public boolean done() {
 			return true;
 		}
 	}
 
 	private class ArrivalReply extends CyclicBehaviour {
 		public void action() {
 			MessageTemplate mt = MessageTemplate.and(
 					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
 					MessageTemplate.MatchConversationId("arrival-inform"));
 			ACLMessage msg = myAgent.receive(mt);
 			if (msg != null) {// this agent received a YES or NO after his
 								// movement request
 				String response = msg.getContent();
 				if (response.contains("go")) {
 					awaitingRelease = false;
 				}
 			} else {
 				block();
 			}
 		}
 	}
 
 	private class HopRequest extends Behaviour {// one shot
 		public void action() {// request the hop
 			updateStorageAgents();
 			ACLMessage movReq = new ACLMessage(ACLMessage.QUERY_IF);
 			movReq.addReceiver(storageAgents[0]);// address only the first
 													// storage agent(could be
 													// randomnized to decrease
 													// workload), all storage
 													// agents are aware of the
 													// same map they share among
 													// them
 			movReq.setContent(moveMentQueue.get(0).x+","+moveMentQueue.get(0).y+","+ //;x,y;x,y;x,y;");// last x,y is the agent its
 					moveMentQueue.get(1).x+","+moveMentQueue.get(1).y+","+
 					moveMentQueue.get(2).x+","+moveMentQueue.get(2).y+","+
 					location.x+","+location.y+",");//yes, the last comma is needed
 													// current location, this
 													// needs to be claimed too
 			movReq.setConversationId("hop-request");
 			myAgent.send(movReq);
 			done();
 		}
 
 		@Override
 		public boolean done() {
 			return true;
 		}
 	}
 
 	private class HopReply extends CyclicBehaviour {
 		public void action() {
 			MessageTemplate mt = MessageTemplate.and(
 					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
 					MessageTemplate.MatchConversationId("hop-request"));
 			ACLMessage msg = myAgent.receive(mt);
 			if (msg != null) {// this agent received a YES or NO after his
 								// movement request
 				String response = msg.getContent();
 				if (response.contains("yes")) {
 					movementVerified = true;
 					for (int j = 0; j < occupiedPoints.size(); j++) {// after a
 																		// hop
 																		// request
 																		// is
 																		// accepted
 																		// clear
 																		// the
 																		// list
 																		// with
 																		// occupied
 																		// points
 						occupiedPoints.remove(0);
 					}
 				} else {
 					// request new map
 					ACLMessage mapReq = new ACLMessage(ACLMessage.QUERY_IF);
 					mapReq.addReceiver(guiAgents[0]);
 					mapReq.setContent("Give me map");
 					mapReq.setConversationId("map-request");
 					myAgent.send(mapReq);
 
 					/*
 					 * for(int i = 0;i<moveMentQueue.size();i++) {
 					 * occupiedPoints.add(moveMentQueue.get(i)); }
 					 */
 					calculateNextHop();
 				}
 			} else {
 				block();
 			}
 		}
 	}
 
 	private class MapRequest extends Behaviour {// one shot
 		public void action() {// request the map from the GuiAgent
 			updateGuiAgents();
 			// System.out.println("I did a one shot map request");//debugging
 			// purpose
 			ACLMessage mapReq = new ACLMessage(ACLMessage.QUERY_IF);
 
 			mapReq.addReceiver(guiAgents[0]);
 			mapReq.setContent("Give me map");
 			mapReq.setConversationId("map-request");
 			myAgent.send(mapReq);
 
 			ACLMessage mapUpd = new ACLMessage(ACLMessage.INFORM);
 			mapUpd.addReceiver(guiAgents[0]);// The gui agent needs to know
 												// where the agent is
 												// initialized
 			mapUpd.setContent("R,26,1" + "," + location.x + "," + location.y);// put the agent on
 																// the map
 			mapUpd.setConversationId("map-update");
 			myAgent.send(mapUpd);
 			done();
 		}
 
 		@Override
 		public boolean done() {
 			return true;
 		}
 	}
 
 	private class MapReceiver extends CyclicBehaviour {
 		public void action() {
 			MessageTemplate mt = MessageTemplate.and(
 					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
 					MessageTemplate.MatchConversationId("map-request"));
 			ACLMessage msg = myAgent.receive(mt);
 			if (msg != null) {// this agent received a map after his map request
 				String response = msg.getContent();
 				// TODO : process the map string to a local stored map
 				createUIGraph(response);
 			} else {
 				block();
 			}
 		}
 
 		// private void
 	}
 }
