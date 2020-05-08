 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import factory.PartType;
 
 /**
  * @author Shalynn Ho, Harry Trieu
  * 
  */
 public class NestGraphicsDisplay extends DeviceGraphicsDisplay {
 	// max number of parts this Nest holds
 	private static final int MAX_PARTS = 8;
 	private static final int BOTTOM_ROW_OFFSET = 30;
 	// y-coordinates of the nest0
 	private static final int NEST_Y = 45, NEST_Y_INCR = 75;
 
 	// the id of this nest
 	private final int nestID;
 	// array of part locations in nest
 	private ArrayList<Location> partLocs;
 	// controls animation
 	private boolean receivingPart = false, purging = false;
 	private boolean receivePartDoneSent = false, purgeDoneSent = false;
 	// dynamically stores the parts currently in the Nest
 	private final List<PartGraphicsDisplay> partsInNest;
 	// start location of a part entering the nest
 	private final Location partStartLoc;
 	// purge location
 	private final Location purgeLoc;
 
 	/**
 	 * Default constructor
 	 */
 	public NestGraphicsDisplay(Client c, int id) {
 		client = c;
 		nestID = id;
 
 		location = new Location(Constants.LANE_END_X - Constants.NEST_WIDTH, NEST_Y + nestID * NEST_Y_INCR);
 		partsInNest = Collections.synchronizedList(new ArrayList<PartGraphicsDisplay>());
 		partStartLoc = new Location(Constants.LANE_END_X - Constants.PART_PADDING, location.getY() + Constants.PART_WIDTH / 2
 				- Constants.PART_OFFSET);
 		purgeLoc = new Location(location.getX() - Constants.PART_WIDTH, partStartLoc.getY());
 		generatePartLocations();
 	}
 
 	/**
 	 * Handles drawing of NestGraphicsDisplay objects
 	 */
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 		g.drawImage(Constants.NEST_IMAGE, location.getX() + client.getOffset(), location.getY(), c);
 
 		if (receivingPart) { // part in motion
 			// get last part added to nest
 			int index = partsInNest.size() - 1;
 			PartGraphicsDisplay pgd = partsInNest.get(index);
 			Location partLoc = pgd.getLocation();
 			Location endLoc = partLocs.get(index); 
 			
 			// check x-coord
 			updateXLoc(partLoc, endLoc, 3);
 			// check y-coord
 			updateYLoc(partLoc, endLoc, 1);
 
 			// check if part in place
 			if (partLoc.equals(endLoc)) {
 				receivingPart = false;
 				msgAgentReceivePartDone();
 			}
 		}
 
 		if (purging) {
 			animatePurge();
 		}
 			
 		synchronized (partsInNest) {
 			for (PartGraphicsDisplay part : partsInNest) {
 				part.drawTransition(client.getOffset(), part.getLocation(), c, g);
 				if (part.trans.animate == false) {
 					part.drawPokeball(client.getOffset(), part.getLocation(), c, g);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Processes requests targeted at the NestGraphicsDisplay
 	 */
 	@Override
 	public void receiveData(Request req) {
 
 		if (req.getCommand().equals(Constants.NEST_RECEIVE_PART_COMMAND)) {
 			if (partsInNest.size() >= MAX_PARTS) {
 				// TODO should this be a message back to the server?
 				// NOTE: according to the agents, this should never happen
 				// anyway
 				System.out.println("Nest is full");
 			} else {
 				PartType type = (PartType) req.getData();
 				receivePart(type);
 			}
 
 		} else if (req.getCommand().equals(Constants.NEST_GIVE_TO_PART_ROBOT_COMMAND)) {
 			givePartToPartsRobot();
 
 		} else if (req.getCommand().equals(Constants.NEST_PURGE_COMMAND)) {
 			purge();
 		}
 	}
 
 	/**
 	 * Sets part locations to animate horizontal purge
 	 */
 	private void animatePurge() {
 		if (partsInNest.size() > 1 && partsInNest.get(1) != null) {
 			movePartToPurgeLoc(partsInNest.get(1));
 			if (partsInNest.get(1).getLocation().equals(purgeLoc)) {
 				partsInNest.remove(1);
 			}
 		}
 
 		if (partsInNest.size() > 0 && partsInNest.get(0) != null) {
 			movePartToPurgeLoc(partsInNest.get(0));
 			if (partsInNest.get(0).getLocation().equals(purgeLoc)) {
 				partsInNest.remove(0);
 			}
 		} else { // nest is empty
 			purging = false;
 			msgAgentPurgingDone();
 		}
 
 		// move remaining parts left horizontally toward purge loc
 		int numRemaining = partsInNest.size() - 2;
 		if (numRemaining > 0) {
 			for (int i = 2; i < numRemaining + 2; i++) {
 				updateXLoc(partsInNest.get(i).getLocation(), partLocs.get(i - 2), 3);
 				updateYLoc(partsInNest.get(i).getLocation(), partLocs.get(i - 2), 1);
 			}
 		}
 	}
 
 	private void movePartToPurgeLoc(PartGraphicsDisplay pgd) {
 		updateXLoc(pgd.getLocation(), purgeLoc, 3);
 		updateYLoc(pgd.getLocation(), purgeLoc, 1);
 	}
 
 	/**
 	 * Increments the X-coordinate
 	 * 
 	 * @param loc
 	 *            - the location being incremented
 	 * @param end
 	 *            - the end location toward which loc is being incremented
 	 * @param increment
 	 *            - a POSITIVE value representing number of pixels moved each call to draw
 	 */
 	private void updateXLoc(Location loc, Location end, int increment) {
 		if (Math.abs(end.getX() - loc.getX()) < increment) {
 			loc.setX(end.getX());
 		}
 		if (loc.getX() > end.getX()) { // moving left
 			loc.incrementX(-increment);
 		} else if (loc.getX() < end.getX()) { // moving right
 			loc.incrementX(increment);
 		}
 	}
 
 	/**
 	 * Increments the Y-coordinate
 	 * 
 	 * @param loc
 	 *            - the location being incremented
 	 * @param end
 	 *            - the end location toward which loc is being incremented
 	 * @param increment
 	 *            - a POSITIVE value representing number of pixels moved each call to draw
 	 */
 	private void updateYLoc(Location loc, Location end, int increment) {
 		if (Math.abs(end.getY() - loc.getY()) < increment) {
 			loc.setY(end.getY());
 		}
 		if (loc.getY() > end.getY()) { // moving up
 			loc.incrementY(-increment);
 		} else if (loc.getY() < end.getY()) { // moving down
 			loc.incrementY(increment);
 		}
 	}
 
 	/**
 	 * Generates an array of Locations for the parts in the nest.
 	 */
 	private void generatePartLocations() {
 		partLocs = new ArrayList<Location>(MAX_PARTS);
 		for (int i = 0; i < MAX_PARTS; i++) {
 			if (i % 2 == 0) { // top row
				partLocs.add(new Location(location.getX() + i / 2 * Constants.PART_WIDTH - 2 * Constants.PART_PADDING, location.getY()
 						- Constants.PART_OFFSET));
 			} else { // bottom row
				partLocs.add(new Location(location.getX() + i / 2 * Constants.PART_WIDTH - 2 * Constants.PART_PADDING, location.getY()
 						+ BOTTOM_ROW_OFFSET - Constants.PART_OFFSET));
 			}
 		}
 	}
 
 	private void givePartToPartsRobot() {
 		// TODO: V2: are we supporting PR removal of parts from any loc in the nest?
 		partsInNest.remove(0);
 		setPartLocations();
 		client.sendData(new Request(Constants.NEST_GIVE_TO_PART_ROBOT_COMMAND + Constants.DONE_SUFFIX,
 				Constants.NEST_TARGET + nestID, null));
 	}
 
 	private void purge() {
 		purging = true;
 		purgeDoneSent = false;
 	}
 
 	private void receivePart(PartType type) {
 		PartGraphicsDisplay pgd = new PartGraphicsDisplay(type);
 		pgd.setLocation(partStartLoc);
 		partsInNest.add(pgd);
 		receivingPart = true;
 		receivePartDoneSent = false;
 
 		System.out.println("NESTGD" + nestID + " RECEIVING PART " + partsInNest.size());
 	}
 
 	private boolean isFull() {
 		return partsInNest.size() >= MAX_PARTS;
 	}
 
 	/**
 	 * Sets/updates the locations of the parts in the nest.
 	 */
 	private void setPartLocations() {
 		// whichever is less
 		int min = MAX_PARTS < partsInNest.size() ? MAX_PARTS : partsInNest.size();
 		for (int i = 0; i < min; i++) {
 			partsInNest.get(i).setLocation(partLocs.get(i));
 		}
 	}
 
 	/**
 	 * Tells the agent that the part has reached its place in the nest. Make sure only sends message once for each part,
 	 * not on every call to draw.
 	 */
 	private void msgAgentReceivePartDone() {
 		if (!receivePartDoneSent) {
 			client.sendData(new Request(Constants.NEST_RECEIVE_PART_COMMAND + Constants.DONE_SUFFIX,
 					Constants.NEST_TARGET + nestID, null));
 			System.out.println("	NESTGD" + nestID+": receive part done sent.");
 			receivePartDoneSent = true;
 		}
 	}
 
 	/**
 	 * Tells the agent that purging is done. Make sure only sends message once for each part, not on every call to draw.
 	 */
 	private void msgAgentPurgingDone() {
 		if (partsInNest.size() == 0 && !purgeDoneSent) {
 			client.sendData(new Request(Constants.NEST_PURGE_COMMAND + Constants.DONE_SUFFIX, Constants.NEST_TARGET
 					+ nestID, null));
 			System.out.println("	NESTGD" + nestID+": purge done sent.");
 			purgeDoneSent = true;
 		}
 	}
 }
