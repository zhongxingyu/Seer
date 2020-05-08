 import java.awt.event.*;
 import java.awt.geom.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.swing.*;
 
 /** class implementing a server application to coordinate factory clients over a network
     (the server-side factory control window is implemented in the FactoryControlManager class) */
 public class Server implements ActionListener, Networked {
 	/** networking port that server listens on */
 	public static final int PORT = 44247;
 	/** interval between timer ticks in milliseconds */
 	public static final int UPDATE_RATE = 200;
 	/** file path of factory settings file */
 	public static final String SETTINGS_PATH = "save/factory.dat";
 
 	/** types of information a client could want to be kept up-to-date on */
 	public enum WantsEnum {
 		PART_TYPES, KIT_TYPES, STATUS, STATE
 	}
 
 	/** whether a client wants to be kept up-to-date with various information */
 	private class ClientWants {
 		/** whether each client wants to be updated with the part types */
 		public boolean partTypes;
 		/** whether each client wants to be updated with the kit types */
 		public boolean kitTypes;
 		/** whether each client wants to be updated with the production status */
 		public boolean status;
 		/** whether each client wants to be updated with the factory state */
 		public boolean state;
 
 		/** initialize things a client could want to false */
 		public ClientWants() {
 			partTypes = false;
 			kitTypes = false;
 			status = false;
 			state = false;
 		}
 	}
 
 	/** server socket used to set up connections with clients */
 	private ServerSocket serverSocket;
 	/** ArrayList of client connections */
 	private ArrayList<NetComm> netComms;
 	/** whether each client wants to be updated with various things */
 	private ArrayList<ClientWants> wants;
 	/** Part types that are available to produce */
 	private ArrayList<Part> partTypes;
 	/** Kit types that are available to produce */
 	private ArrayList<Kit> kitTypes;
 	/** current production status */
 	private ProduceStatusMsg status;
 	/** current factory state */
 	private FactoryStateMsg state;
 	/** GUI window for user to control the factory */
 	private FactoryControlManager controller;
 
 	/** indices of nests in factory state */
 	public ArrayList<Integer> nestIDs;
 	/** indices of lanes in factory state */
 	public ArrayList<Integer> laneIDs;
 	/** indices of diverter arms in factory state */
 	public ArrayList<Integer> diverterArmIDs;
 	/** indices of feeders in factory state */
 	public ArrayList<Integer> feederIDs;
 	/** indices of part bins in factory state */
 	public TreeMap<Integer, Integer> partBinIDs;
 	/** indices of purge bins in factory state */
 	public TreeMap<Integer, Integer> purgeBinIDs;
 	/** indices of spare part bins in factory state */
 	public TreeMap<Integer, Integer> sparePartBinIDs;
 	/** index of kit stand in factory state */
 	public int kitStandID;
 	/** index of kit delivery station in factory state */
 	public int kitDelivID;
 	/** index of kit robot in factory state */
 	public int kitRobotID;
 	/** index of part robot in factory state */
 	public int partRobotID;
 	/** index of gantry robot in factory state */
 	public int gantryID;
 
 	/** constructor for server class */
 	public Server() throws IOException {
 		// initialize server socket
 		try {
 			serverSocket = new ServerSocket(PORT);
 		}
 		catch (IOException ex) {
 			throw ex;
 		}
 		// load factory settings from file (or set up new factory if can't load from file)
 		loadSettings();
 		// open controller window
 		controller = new FactoryControlManager(this);
 		// start update timer
 		new javax.swing.Timer(UPDATE_RATE, this).start();
 		// wait for clients to connect
 		new Thread(new AcceptClientsThread()).start();
 	}
 
 	public static void main(String[] args) {
 		Server server;
 		try {
 			server = new Server();
 		}
 		catch (Exception ex) {
 			System.out.println("Error initializing server:");
 			ex.printStackTrace();
 			return;
 		}
 	}
 
 	/** called during timer tick; updates simulation and broadcasts factoryUpdate to clients */
 	public void actionPerformed(ActionEvent ae) {
 		int i, j;
 		if (ae.getSource() instanceof javax.swing.Timer) {
 			FactoryUpdateMsg update = new FactoryUpdateMsg(state);
 			boolean updatedPartBins = false;
 			for (i = 0; i < laneIDs.size(); i++) {
 				// drop part off at nest if it reaches end of lane
 				GUILane lane = getLane(i);
 				for (j = 0; j < 2; j++) {
 					int endItem = lane.endItem((j == 0) ? -1 : 1);
 					GUINest nest = getNest(i * 2 + j);
 					if (lane.itemAtEnd(endItem, update.timeElapsed) && !nest.nest.isNestFull()) {
 						Object laneObj = lane.removeItem(endItem, update.timeElapsed);
 						if (laneObj instanceof GUIPart) {
 							if (nest.nest.addPart(((GUIPart)laneObj).part)) {
 								update.putItems.put(nestIDs.get(i * 2 + j), nest);
 							}
 							else {
 								System.out.println("Warning: nest refused to add part despite not being full");
 							}
 						}
 						else {
 							System.out.println("Warning: lane end item is not a part");
 						}
 						update.putItems.put(laneIDs.get(i), lane);
 					}
 				}
 			}
 			for (i = 0; i < feederIDs.size(); i++) {
 				// feed part if it is time to
 				GUIFeeder feeder = getFeeder(i);
 				GUILane lane = getLane(i);
 				if (lane.isLaneOn() && feeder.feeder.shouldFeed(update.timeElapsed)) {
 					int ins = lane.addItem(new GUIPart(feeder.feeder.feedPart(update.timeElapsed), 0, 0), new Point2D.Double(lane.getPos().x + lane.getLength(), feeder.feeder.getDiverter()), update.timeElapsed);
 					if (lane.getItemLocation(ins, update.timeElapsed).x > lane.getPos().x + lane.getLength() + 1) {
 						// lane is full, remove fed item
 						lane.removeItem(ins, update.timeElapsed);
 						System.out.println("Lane is full, drop item in feeder");
 					}
 					update.putItems.put(feederIDs.get(i), feeder);
 					update.putItems.put(laneIDs.get(i), lane);
 				}
 			}
 			for (Map.Entry<Integer, GUIItem> e : state.items.entrySet()) {
 				int key = e.getKey();
 				boolean updated = false;
 				if (e.getValue() instanceof GUIKitCamera) {
 					// remove expired kit cameras
 					GUIKitCamera kitCamera = (GUIKitCamera)e.getValue();
 					if (kitCamera.isExpired(update.timeElapsed)) update.removeItems.add(key);
 				}
 				else if (e.getValue() instanceof GUILane) {
 					GUILane lane = (GUILane)e.getValue();
 					// reset lane if has moved 1 segment length
 					if (lane.shouldReset(update.timeElapsed)) {
 						lane.reset(update.timeElapsed);
 						updated = true;
 					}
 				}
 				else if (e.getValue() instanceof GUIKitDeliveryStation) {
 					// kit delivery station
 					GUIKitDeliveryStation kitDeliv = (GUIKitDeliveryStation)e.getValue();
 					updated = kitDeliv.checkStatus(update.timeElapsed); // let kit delivery station update itself
 					// ensure there is always a pallet on the in conveyor
 					if (!kitDeliv.inConveyor.containsItems()) {
 						kitDeliv.inConveyor.addEmptyPallet(update.timeElapsed);
 						updated = true;
 					}
 					// remove any pallet that reaches end of out conveyor
 					if (kitDeliv.outConveyor.hasFullPalletAtEnd(update.timeElapsed)) {
 						kitDeliv.outConveyor.removeItem(0, update.timeElapsed);
 						updated = true;
 					}
 				}
 				else if (e.getValue() instanceof GUIKitRobot) {
 					// kit robot
 					GUIKitRobot kitRobot = (GUIKitRobot)e.getValue();
 					GUIKitDeliveryStation kitDeliv = getKitDeliv();
 					GUIKitStand kitStand = getKitStand();
 					if (kitRobot.movement.arrived(update.timeElapsed)) {
 						if (kitRobot.kitRobot.state == KitRobot.KRState.PICK_UP && kitDeliv.inConveyor.hasFullPalletAtEnd(update.timeElapsed) && kitRobot.kitRobot.getKit() == null) {
 							// pick up kit from delivery station
 							kitRobot.kitRobot.setKit(kitDeliv.inConveyor.removeEndPalletKit());
 							update.putItems.put(kitDelivID, kitDeliv);
 							updated = true;
 						}
 						else if (kitRobot.kitRobot.state == KitRobot.KRState.DROP_OFF && kitRobot.kitRobot.getKit() != null) {
 							// drop off kit at delivery station
 							kitDeliv.outConveyor.addItem(new GUIPallet(new Pallet(kitRobot.kitRobot.removeKit()), 0, 0), new Point2D.Double(kitDeliv.getOutConveyorLocation().x, 0), update.timeElapsed);
 							update.putItems.put(kitDelivID, kitDeliv);
 							controller.kitRobotPanel.resetMoveButtons();
 							updated = true;
 						}
 						else if (kitRobot.kitRobot.state == KitRobot.KRState.KIT_STAND) {
 							if (kitStand.getKit(kitRobot.kitRobot.targetID) != null && kitRobot.kitRobot.getKit() == null) {
 								// pick up kit in kit stand
 								kitRobot.kitRobot.setKit(kitStand.removeKit(kitRobot.kitRobot.targetID).kit);
 								update.putItems.put(kitStandID, kitStand);
 								updated = true;
 							}
 							else if (kitStand.getKit(kitRobot.kitRobot.targetID) == null && kitRobot.kitRobot.getKit() != null) {
 								// drop off kit in kit stand
 								kitStand.addKit(new GUIKit(kitRobot.kitRobot.removeKit(), 0, 0), kitRobot.kitRobot.targetID);
 								update.putItems.put(kitStandID, kitStand);
 								controller.kitRobotPanel.resetMoveButtons();
 								updated = true;
 							}
 						}
 						if (updated) kitRobot.kitRobot.state = KitRobot.KRState.IDLE;
 					}
 				}
 				else if (e.getValue() instanceof GUIPartRobot) {
 					// part robot
 					GUIPartRobot partRobot = (GUIPartRobot)e.getValue();
 					if (partRobot.movement.arrived(update.timeElapsed)) {
 						if (partRobot.partRobot.state == PartRobot.PRState.NEST) {
 							// pick up part from nest
 							GUINest nest = getNest(partRobot.partRobot.targetID);
 							Part part = nest.nest.removePart();
 							if (part != null) partRobot.addPartToGripper(partRobot.partRobot.gripperID, new GUIPart(part, 0, 0));
 							update.putItems.put(nestIDs.get(partRobot.partRobot.targetID), nest);
 							updated = true;
 						}
 						else if (partRobot.partRobot.state == PartRobot.PRState.KIT_STAND && getKitStand().getKit(partRobot.partRobot.targetID) != null && partRobot.partRobot.partsInGripper.containsKey(partRobot.partRobot.gripperID)) {
 							// drop off part in kit stand
 							GUIKitStand kitStand = getKitStand();
 							kitStand.getKit(partRobot.partRobot.targetID).kit.addPart(partRobot.partRobot.kitPosID, partRobot.removePartFromGripper(partRobot.partRobot.gripperID).part);
 							update.putItems.put(kitStandID, kitStand);
 							controller.partRobotPanel.resetMoveButtons();
 							updated = true;
 						}
 						if (updated) partRobot.partRobot.state = PartRobot.PRState.IDLE;
 					}
 				}
 				else if (e.getValue() instanceof GUIGantry) {
 					// gantry robot
 					GUIGantry gantry = (GUIGantry)e.getValue();
 					if (gantry.movement.arrived(update.timeElapsed)) {
 						if (gantry.state == GUIGantry.GRState.PART_BIN && partBinIDs.containsKey(gantry.targetID) && gantry.guiBin == null) {
 							// pick up part bin
 							gantry.addBin(getPartBin(gantry.targetID));
 							update.removeItems.add(partBinIDs.get(gantry.targetID));
 							updated = true;
 						}
 						else if (gantry.state == GUIGantry.GRState.FEEDER && gantry.guiBin != null) {
 							// drop off bin in feeder
 							GUIFeeder feeder = getFeeder(gantry.targetID);
 							feeder.loadBin(gantry.removeBin().bin);
 							update.putItems.put(feederIDs.get(gantry.targetID), feeder);
 							controller.gantryRobotPanel.resetMoveButtons();
 							updatedPartBins = true;
 							updated = true;
 						}
 						if (updated) gantry.state = GUIGantry.GRState.IDLE;
 					}
 				}
 				if (updated) {
 					// item was updated, add it to factory update
 					update.putItems.put(key, e.getValue());
 				}
 			}
 			if (update.putItems.size() > 0 || update.removeItems.size() > 0 || update.itemMoves.size() > 0) {
 				applyUpdate(update);
 			}
 			if (updatedPartBins) updatePartBins();
 		}
 	}
 
 	/** handle message received from clients */
 	public void msgReceived(Object msgObj, NetComm sender) {
 		int senderIndex;
 		// find who sent the message
 		for (senderIndex = 0; senderIndex < netComms.size(); senderIndex++) {
 			if (sender == netComms.get(senderIndex)) break;
 		}
 		if (senderIndex == netComms.size()) {
 			System.out.println("Warning: received message from unknown client: " + msgObj);
 			return;
 		}
 		// handle message
 		if (msgObj instanceof CloseConnectionMsg) {
 			// close connection with client
 			// (but don't call clients.get(i).close() because client might still receive the message and get confused)
 			System.out.println("Client " + senderIndex + " has left");
 			netComms.remove(senderIndex);
 			wants.remove(senderIndex);
 		}
 		else if (msgObj instanceof String) {
 			// broadcast message to all clients (for TestClient only, not used by factory managers)
 			for (int i = 0; i < netComms.size(); i++) {
 				netComms.get(i).write("Message from " + senderIndex + " to " + i + ": " + (String)msgObj);
 			}
 		}
 		else if (msgObj instanceof NewPartMsg) {
 			// add a new part type
 			if (addPart(senderIndex, (NewPartMsg)msgObj, true)) {
 				System.out.println("Client " + senderIndex + " added a part");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to add a part");
 			}
 		}
 		else if (msgObj instanceof ChangePartMsg) {
 			// change an existing part type
 			if (changePart(senderIndex, (ChangePartMsg)msgObj)) {
 				System.out.println("Client " + senderIndex + " changed a part");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to change a part");
 			}
 		}
 		else if (msgObj instanceof DeletePartMsg) {
 			// delete an existing part type
 			if (deletePart(senderIndex, (DeletePartMsg)msgObj, true) != null) {
 				System.out.println("Client " + senderIndex + " deleted a part");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to delete a part");
 			}
 		}
 		else if (msgObj instanceof PartListMsg) {
 			// send available part types to client
 			netComms.get(senderIndex).write(new PartListMsg(partTypes));
 			wants.get(senderIndex).partTypes = true;
 			System.out.println("Sent part list to client " + senderIndex);
 		}
 		else if (msgObj instanceof NewKitMsg) {
 			// add a new kit type
 			if (addKit(senderIndex, (NewKitMsg)msgObj, true)) {
 				System.out.println("Client " + senderIndex + " added a kit");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to add a kit");
 			}
 		}
 		else if (msgObj instanceof ChangeKitMsg) {
 			// change an existing kit type
 			if (changeKit(senderIndex, (ChangeKitMsg)msgObj)) {
 				System.out.println("Client " + senderIndex + " changed a kit");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to change a kit");
 			}
 		}
 		else if (msgObj instanceof DeleteKitMsg) {
 			// delete an existing kit type
 			if (deleteKit(senderIndex, (DeleteKitMsg)msgObj, true) != null) {
 				System.out.println("Client " + senderIndex + " deleted a kit");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to delete a kit");
 			}
 		}
 		else if (msgObj instanceof KitListMsg) {
 			// send available kit types to client
 			netComms.get(senderIndex).write(new KitListMsg(kitTypes));
 			wants.get(senderIndex).kitTypes = true;
 			System.out.println("Sent kit list to client " + senderIndex);
 		}
 		else if (msgObj instanceof ProduceKitsMsg) {
 			// add kit production command to queue
 			if (produceKits(senderIndex, (ProduceKitsMsg)msgObj)) {
 				System.out.println("Client " + senderIndex + " added a production request");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to add a production request");
 			}
 		}
 		else if (msgObj instanceof ProduceStatusMsg) {
 			// send production status to client
 			netComms.get(senderIndex).write(status);
 			wants.get(senderIndex).status = true;
 			System.out.println("Sent production status to client " + senderIndex);
 		}
 		else if (msgObj instanceof FactoryStateMsg) {
 			// this client wants to be updated with factory state
 			netComms.get(senderIndex).write(state);
 			wants.get(senderIndex).state = true;
 			System.out.println("Sent factory state to client " + senderIndex);
 		}
 		else if (msgObj instanceof NonNormativeMsg) {
 			// TODO: create a non-normative scenario
 			NonNormativeMsg msg = (NonNormativeMsg)msgObj;
 			System.out.println("Client " + senderIndex + " sent a NonNormativeMsg with type=" + msg.type + " index=" + msg.index + " cmd=" + msg.cmd + " (currently unhandled)");
 		}
 		else {
 			System.out.println("Warning: received unknown message from client " + senderIndex + ": " + msgObj);
 		}
 	}
 
 	/** adds part to partTypes (if valid), if notify is true sends StringMsg to client indicating success or failure */
 	private boolean addPart(int clientIndex, NewPartMsg msg, boolean notify) {
 		String valid = newPartIsValid(msg.part);
 		if (notify) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.NEW_PART, valid));
 		}
 		if (!valid.isEmpty()) return false;
 		partTypes.add(msg.part);
 		if (notify) broadcast(WantsEnum.PART_TYPES);
 		return true;
 	}
 
 	/** changes specified part (if valid and not in production), sends StringMsg to client indicating success or failure */
 	private boolean changePart(int clientIndex, ChangePartMsg msg) {
 		// delete old part
 		Part oldPart = deletePart(clientIndex, new DeletePartMsg(msg.oldNumber), false);
 		if (oldPart == null) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_PART, "Requested part either in production or does not exist"));
 		}
 		// add replacement part
 		else if (!addPart(clientIndex, new NewPartMsg(msg.part), false)) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_PART, newPartIsValid(msg.part)));
 			partTypes.add(oldPart);
 		}
 		else {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_PART, ""));
 			broadcast(WantsEnum.PART_TYPES);
 		}
 		return false;
 	}
 
 	/** deletes part with specified name (if exists), if notify is true sends StringMsg to client indicating success or failure,
 	    returns deleted part if succeeded or null if failed */
 	private Part deletePart(int clientIndex, DeletePartMsg msg, boolean notify) {
 		int i, j;
 		// TODO: don't delete part types in production
 		/*for (i = 0; i < status.cmds.size(); i++) {
 			if (status.status.get(i) == ProduceStatusMsg.KitStatus.QUEUED
 			    || status.status.get(i) == ProduceStatusMsg.KitStatus.PRODUCTION) {
 				Kit kit = getKitByNumber(status.cmds.get(i).kitNumber);
 				for (j = 0; j < kit.partsNeeded.size(); j++) {
 					if (msg.number == kit.partsNeeded.get(j).getNumber()) {
 						if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, "May not delete part that is in production"));	
 						return null;
 					}
 				}
 			}
 		}*/
 		// delete part with specified number
 		for (i = 0; i < partTypes.size(); i++) {
 			if (msg.number == partTypes.get(i).getNumber()) {
 				Part ret = partTypes.remove(i);
 				if (notify) {
 					netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, ""));
 					broadcast(WantsEnum.PART_TYPES);
 				}
 				return ret;
 			}
 		}
 		if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, "Part never existed or has already been deleted"));
 		return null;
 	}
 
 	/** adds kit to kitTypes (if valid), if notify is true sends StringMsg to client indicating success or failure */
 	private boolean addKit(int clientIndex, NewKitMsg msg, boolean notify) {
 		String valid = newKitIsValid(msg.kit);
 		if (notify) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.NEW_KIT, valid));
 		}
 		if (!valid.isEmpty()) return false;
 		kitTypes.add(msg.kit);
 		if (notify) broadcast(WantsEnum.KIT_TYPES);
 		return true;
 	}
 
 	/** changes specified kit (if valid and not in production), sends StringMsg to client indicating success or failure */
 	private boolean changeKit(int clientIndex, ChangeKitMsg msg) {
 		// delete old kit
 		Kit oldKit = deleteKit(clientIndex, new DeleteKitMsg(msg.oldNumber), false);
 		if (oldKit == null) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_KIT, "Requested kit either in production or does not exist"));
 		}
 		// add replacement kit
 		else if (!addKit(clientIndex, new NewKitMsg(msg.kit), false)) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_KIT, newKitIsValid(msg.kit)));
 			kitTypes.add(oldKit);
 		}
 		else {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_KIT, ""));
 			broadcast(WantsEnum.KIT_TYPES);
 		}
 		return false;
 	}
 
 	/** deletes kit with specified name (if exists), if notify is true sends StringMsg to client indicating success or failure,
 	    returns deleted kit if succeeded or null if failed */
 	private Kit deleteKit(int clientIndex, DeleteKitMsg msg, boolean notify) {
 		int i, j;
 		// TODO: don't delete kit types in production
 		/*for (i = 0; i < status.cmds.size(); i++) {
 			if (status.status.get(i) == ProduceStatusMsg.KitStatus.QUEUED
 			    || status.status.get(i) == ProduceStatusMsg.KitStatus.PRODUCTION) {
 				Kit kit = getKitByNumber(status.cmds.get(i).kitNumber);
 				for (j = 0; j < kit.partsNeeded.size(); j++) {
 					if (msg.number == kit.partsNeeded.get(j).getNumber()) {
 						if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, "May not delete part that is in production"));	
 						return null;
 					}
 				}
 			}
 		}*/
 		// delete kit with specified number
 		for (i = 0; i < kitTypes.size(); i++) {
 			if (msg.number == kitTypes.get(i).getNumber()) {
 				Kit ret = kitTypes.remove(i);
 				if (notify) {
 					netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_KIT, ""));
 					broadcast(WantsEnum.KIT_TYPES);
 				}
 				return ret;
 			}
 		}
 		if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_KIT, "Kit never existed or has already been deleted"));
 		return null;
 	}
 
 	/** returns empty string if given part is valid, or error message if it is not */
 	private String newPartIsValid(Part part) {
 		if (part.getNumber() <= 0) return "Part number must be a positive integer";
 		if (!isValidName(part.getName())) {
 			if (part.getName().isEmpty()) return "Please enter a part name";
 			return "Part name may only contain letters, numbers, or spaces";
 		}
 		for (int i = 0; i < partTypes.size(); i++) {
 			if (part.getNumber() == partTypes.get(i).getNumber()) {
 				return "Another part has the same number";
 			}
 			if (part.getName().equals(partTypes.get(i).getName())) {
 				return "Another part has the same name";
 			}
 		}
 		return "";
 	}
 
 	/** returns empty string if given kit is valid, or error message if it is not */
 	private String newKitIsValid(Kit kit) {
 		if (kit.getNumber() <= 0) return "Kit number must be a positive integer";
 		if (!isValidName(kit.getName())) {
 			if (kit.getName().isEmpty()) return "Please enter a kit name";
 			return "Kit name may only contain letters, numbers, or spaces";
 		}
 		for (int i = 0; i < kitTypes.size(); i++) {
 			if (kit.getNumber() == kitTypes.get(i).getNumber()) {
 				return "Another kit has the same number";
 			}
 			if (kit.getName().equals(kitTypes.get(i).getName())) {
 				return "Another kit has the same name";
 			}
 		}
 		// number of parts in kit validated on client side, so don't need to check it here
 		return "";
 	}
 
 	/** queue specified production command in production status (if valid), sends StringMsg to client indicating success or failure */
 	private boolean produceKits(int clientIndex, ProduceKitsMsg msg) {
 		if (msg.howMany <= 0) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.PRODUCE_KITS, "Must produce at least 1 new kit"));
 			return false;
 		}
 		if (getKitByNumber(msg.kitNumber) == null) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.PRODUCE_KITS, "Kit number must refer to an existing kit"));
 			return false;
 		}
 		status.cmds.add(msg);
 		status.status.add(ProduceStatusMsg.KitStatus.QUEUED);
 		netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.PRODUCE_KITS, ""));
 		broadcast(WantsEnum.STATUS);
 		return true;
 	}
 
 	/** update part bins so that there is 1 per part type */
 	private void updatePartBins() {
 		FactoryUpdateMsg update = new FactoryUpdateMsg(state);
 		// reset control panel labels
		for (int i = 0; i < 8; i++) {
			controller.gantryRobotPanel.setPartsBoxStorageContents("Empty", i);
 		}
 		// delete previous part bins
 		for (Integer i : partBinIDs.values()) {
 			update.removeItems.add(i);
 		}
 		applyUpdate(update);
 		partBinIDs.clear();
 		// add replacement part bins
 		update.removeItems.clear();
 		for (int i = 0; i < partTypes.size(); i++) {
 			int key = state.items.lastKey() + 1 + i;
 			update.putItems.put(key, new GUIBin(new Bin(partTypes.get(i), 10), 1200 - i * 120, 650));
 			partBinIDs.put(i, key);
			if (i < 4) controller.gantryRobotPanel.setPartsBoxStorageContents(partTypes.get(i).getName(), i);
 		}
 		applyUpdate(update);
 	}
 
 	/** set nest labels in part robot control panel */
 	private void updateNestLabels() {
 		for (int i = 0; i < nestIDs.size(); i++) {
 			Nest nest = getNest(i).nest;
 			if (nest.nestedItems.isEmpty()) {
 				controller.partRobotPanel.setPartContent("Empty", i);
 			}
 			else {
 				controller.partRobotPanel.setPartContent(nest.nestedItems.get(0).getName(), i);
 			}
 		}
 	}
 
 	/** set feeder labels in gantry robot control panel */
 	private void updateFeederLabels() {
 		for (int i = 0; i < feederIDs.size(); i++) {
 			ArrayList<Part> feederParts = getFeeder(i).feeder.getParts();
 			if (feederParts.isEmpty()) {
 				controller.gantryRobotPanel.setFeederContents("Empty", i);
 			}
 			else {
 				controller.gantryRobotPanel.setFeederContents(feederParts.get(0).getName(), i);
 			}
 		}
 	}
 
 	/** apply update to factory state on server and all clients that requested it */
 	public void applyUpdate(FactoryUpdateMsg update) {
 		// update control panel labels
 		boolean updatedNests = false;
 		boolean updatedFeeders = false;
 		for (GUIItem item : update.putItems.values()) {
 			if (item instanceof GUINest) updatedNests = true;
 			if (item instanceof GUIFeeder) updatedFeeders = true;
 		}
 		if (updatedNests) updateNestLabels();
 		if (updatedFeeders) updateFeederLabels();
 		// broadcast update to clients
 		for (int i = 0; i < wants.size(); i++) {
 			if (wants.get(i).state) {
 				netComms.get(i).write(update);
 			}
 		}
 		// update factory state on server
 		state.update(update);
 	}
 
 	/** broadcast specified information to clients that want it */
 	public void broadcast(WantsEnum wantsEnum) {
 		for (int i = 0; i < wants.size(); i++) {
 			if (wantsEnum == WantsEnum.PART_TYPES && wants.get(i).partTypes) {
 				netComms.get(i).write(new PartListMsg(partTypes));
 			}
 			else if (wantsEnum == WantsEnum.KIT_TYPES && wants.get(i).kitTypes) {
 				netComms.get(i).write(new KitListMsg(kitTypes));
 			}
 			else if (wantsEnum == WantsEnum.STATUS && wants.get(i).status) {
 				netComms.get(i).write(status);
 			}
 		}
 		if (wantsEnum == WantsEnum.PART_TYPES) updatePartBins(); // if part types changed, then should update factory state too
 		controller.updateSchedule(kitTypes, status);
 	}
 
 	/** returns part type with specified part number, or null if there is no such part */
 	private Part getPartByNumber(int number) {
 		for (int i = 0; i < partTypes.size(); i++) {
 			if (partTypes.get(i).getNumber() == number) return partTypes.get(i);
 		}
 		return null;
 	}
 
 	/** returns kit type with specified kit number, or null if there is no such kit */
 	private Kit getKitByNumber(int number) {
 		for (int i = 0; i < kitTypes.size(); i++) {
 			if (kitTypes.get(i).getNumber() == number) return kitTypes.get(i);
 		}
 		return null;
 	}
 
 	/** returns whether specified part/kit name is valid
 	    (i.e. is not empty and is composed only of letters, numbers, or spaces);
 	    copied from Andrew's HW3 submission */
 	public static boolean isValidName(String name) {
 		if (name.isEmpty()) {
 			return false;
 		}
 		for (char ch : name.toCharArray()) {
 			if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/** initialize new/default factory */
 	private void initFactory() {
 		// instantiate lists
 		netComms = new ArrayList<NetComm>();
 		wants = new ArrayList<ClientWants>();
 		partTypes = new ArrayList<Part>();
 		kitTypes = new ArrayList<Kit>();
 		status = new ProduceStatusMsg();
 		state = new FactoryStateMsg();
 		nestIDs = new ArrayList<Integer>();
 		laneIDs = new ArrayList<Integer>();
 		diverterArmIDs = new ArrayList<Integer>();
 		feederIDs = new ArrayList<Integer>();
 		partBinIDs = new TreeMap<Integer, Integer>();
 		purgeBinIDs = new TreeMap<Integer, Integer>();
 		sparePartBinIDs = new TreeMap<Integer, Integer>();
 		// initialize factory state
 		int laneSeparation = 120;
 		for (int i = 0; i < 4; i++)
 		{
 			state.add(new GUINest(new Nest(), 550, 120 + laneSeparation*i));
 			nestIDs.add(state.items.lastKey());
 			state.add(new GUINest(new Nest(), 550, 120 + laneSeparation*i + 50));
 			nestIDs.add(state.items.lastKey());
 			
 			GUILane guiLane = new GUILane(new Lane(), true, 6, 630, 124 + laneSeparation*i);
 			guiLane.turnOff(0);
 			guiLane.setAmplitude(Lane.AMP_LOW);
 			
 			state.add(guiLane);
 			laneIDs.add(state.items.lastKey());
 			state.add(new GUIDiverterArm(990, 170 + laneSeparation*i));
 			diverterArmIDs.add(state.items.lastKey());
 			state.add(new GUIFeeder(new Feeder(), 1165, 170 + laneSeparation*i));
 			feederIDs.add(state.items.lastKey());
 		}
 
 		state.add(new GUIKitStand());
 		kitStandID = state.items.lastKey();
 
 		GUIKitDeliveryStation guiKitDeliv = new GUIKitDeliveryStation(new KitDeliveryStation(), 
 		 		   new GUILane(new Lane(), false, 8, 350,-10), 
 		 		   new GUILane(new Lane(), false, 3, 350-180, -10), 10, 10);
 		guiKitDeliv.inConveyor.turnOn(0);
 		guiKitDeliv.outConveyor.turnOn(0);
 
 		state.add(guiKitDeliv);
 		kitDelivID = state.items.lastKey();
 								 
 		state.add(new GUIKitRobot(new KitRobot(), new Point2D.Double(350, 250)));
 		kitRobotID = state.items.lastKey();
 		state.add(new GUIPartRobot(new PartRobot(), new Point2D.Double(350, 340)));
 		partRobotID = state.items.lastKey();
 		state.add(new GUIGantry(500, 500));
 		gantryID = state.items.lastKey();
 	}
 
 	/** load factory settings from file */
 	private void loadSettings() {
 		initFactory();
 		try {
 			ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(SETTINGS_PATH));
 			Object inObj;
 			while (inStream.readBoolean()) {
 				inObj = inStream.readObject();
 				if (inObj instanceof Part) {
 					partTypes.add((Part)inObj);
 				}
 				else if (inObj instanceof Kit) {
 					kitTypes.add((Kit)inObj);
 				}
 				else if (inObj instanceof ProduceStatusMsg) {
 					status = (ProduceStatusMsg)inObj;
 				}
 				else if (inObj instanceof FactoryStateMsg) {
 					state = (FactoryStateMsg)inObj;
 					state.timeStart = System.currentTimeMillis() - state.timeElapsed;
 				}
 			}
 			inStream.close();
 			// remove parts bins from factory state then regenerate them to put them in part bins treemap
 			// see http://www.coderanch.com/t/386106/java/java/remove-key-Map for how to delete items while iterating over TreeMap
 			for (Iterator<Map.Entry<Integer, GUIItem>> iter = state.items.entrySet().iterator(); iter.hasNext(); ) {
 				Map.Entry<Integer, GUIItem> e = iter.next();
 				if (e.getValue() instanceof GUIBin) iter.remove();
 			}
 			updatePartBins();
 		}
 		catch (FileNotFoundException ex) {
 			System.out.println("Settings file not found; a new factory has been set up.");
 		}
 		catch (Exception ex) {
 			initFactory();
 			System.out.println("Error loading settings from file; a new factory has been set up.");
 		}
 	}
 
 	/** save factory settings to file */
 	public void saveSettings() {
 		int i;
 		try {
 			ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(SETTINGS_PATH));
 			for (i = 0; i < partTypes.size(); i++) {
 				outStream.writeBoolean(true);
 				outStream.writeObject(partTypes.get(i));
 			}
 			for (i = 0; i < kitTypes.size(); i++) {
 				outStream.writeBoolean(true);
 				outStream.writeObject(kitTypes.get(i));
 			}
 			outStream.writeBoolean(true);
 			outStream.writeObject(status);
 			outStream.writeBoolean(true);
 			outStream.writeObject(state);
 			outStream.writeBoolean(false);
 			outStream.close();
 			System.out.println("Saved factory settings to file.");
 		}
 		catch (Exception ex) {
 			System.out.println("Error saving factory settings to file.");
 			System.out.println("Make sure the \"save\" folder exists.");
 		}
 	}
 
 	/** getter for part types */
 	public ArrayList<Part> getParts() {
 		return partTypes;
 	}
 
 	/** getter for kit types */
 	public ArrayList<Kit> getKits() {
 		return kitTypes;
 	}
 
 	/** getter for production status */
 	public ProduceStatusMsg getStatus() {
 		return status;
 	}
 
 	/** getter for factory state */
 	public FactoryStateMsg getState() {
 		return state;
 	}
 
 	/** getter for a nest */
 	public GUINest getNest(int index) {
 		int key = nestIDs.get(index);
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUINest)) throw new IllegalArgumentException("nest key does not point to a nest");
 		return (GUINest)stateObj;
 	}
 
 	/** getter for a lane */
 	public GUILane getLane(int index) {
 		int key = laneIDs.get(index);
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUILane)) throw new IllegalArgumentException("lane key does not point to a lane");
 		return (GUILane)stateObj;
 	}
 
 	/** getter for a diverter arm */
 	public GUIDiverterArm getDiverterArm(int index) {
 		int key = diverterArmIDs.get(index);
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIDiverterArm)) throw new IllegalArgumentException("diverter arm key does not point to a diverter arm");
 		return (GUIDiverterArm)stateObj;
 	}
 
 	/** getter for a feeder */
 	public GUIFeeder getFeeder(int index) {
 		int key = feederIDs.get(index);
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIFeeder)) throw new IllegalArgumentException("feeder key does not point to a feeder");
 		return (GUIFeeder)stateObj;
 	}
 
 	/** getter for a part bin */
 	public GUIBin getPartBin(int index) {
 		int key = partBinIDs.get(index);
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIBin)) throw new IllegalArgumentException("part bin key does not point to a bin");
 		return (GUIBin)stateObj;
 	}
 
 	// TODO: add getters for purge bin and spare part bin when they are added
 
 	/** getter for kit stand */
 	public GUIKitStand getKitStand() {
 		int key = kitStandID;
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIKitStand)) throw new IllegalArgumentException("kit stand key does not point to a kit stand");
 		return (GUIKitStand)stateObj;
 	}
 
 	/** getter for kit delivery station */
 	public GUIKitDeliveryStation getKitDeliv() {
 		int key = kitDelivID;
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIKitDeliveryStation)) throw new IllegalArgumentException("kit delivery station key does not point to a kit delivery station");
 		return (GUIKitDeliveryStation)stateObj;
 	}
 
 	/** getter for kit robot */
 	public GUIKitRobot getKitRobot() {
 		int key = kitRobotID;
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIKitRobot)) throw new IllegalArgumentException("kit robot key does not point to a kit robot");
 		return (GUIKitRobot)stateObj;
 	}
 
 	/** getter for part robot */
 	public GUIPartRobot getPartRobot() {
 		int key = partRobotID;
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIPartRobot)) throw new IllegalArgumentException("part robot key does not point to a part robot");
 		return (GUIPartRobot)stateObj;
 	}
 
 	/** getter for gantry robot */
 	public GUIGantry getGantry() {
 		int key = gantryID;
 		Object stateObj = state.items.get(key);
 		if (!(stateObj instanceof GUIGantry)) throw new IllegalArgumentException("gantry robot key does not point to a gantry robot");
 		return (GUIGantry)stateObj;
 	}
 
 	/** thread to accept new clients */
 	private class AcceptClientsThread implements Runnable {
 		/** wait for clients to connect */
 		public void run() {
 			while (true) { // loop exits when user presses ctrl+C
 				try {
 					Socket socket = serverSocket.accept();
 					netComms.add(new NetComm(socket, Server.this));
 					wants.add(new ClientWants());
 					System.out.println("Client " + (netComms.size() - 1) + " has joined");
 				}
 				catch (Exception ex) {
 					System.out.println("Error accepting new client connection");
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 }
