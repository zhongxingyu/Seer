 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import factory.PartType;
 
 /**
  * This class contains the graphics display components for a lane.
  * 
  * @author Shalynn Ho
  * 
  */
 public class LaneGraphicsDisplay extends DeviceGraphicsDisplay {
     // max number of parts that can be on a Lane
     private static final int MAX_PARTS = Constants.LANE_LENGTH / (Constants.PART_WIDTH + Constants.PART_PADDING / 2);
     // number of lines on the lane
     private static final int NUMLINES = 1 + Constants.LANE_LENGTH / (4 * Constants.PART_WIDTH);
     // space between each line
     private static final int LINESPACE = Constants.LANE_LENGTH / NUMLINES;
     // width of lane lines
     private static final int LINE_WIDTH = 3;
 
     // stores the parts on the lane
     private final ArrayList<PartGraphicsDisplay> partsOnLane;
     // start location of parts on this lane
     private final Location partStartLoc;
     // location of a lane jam
     private final Location jamLoc;
     // array list of locations of the lane lines
     private ArrayList<Location> laneLines;
 
     // the LaneManager (client) which talks to the Server
     private final Client client;
     // the ID of this Lane
     private final int laneID;
     // the amplitude of this lane
     private int speed = 1;
     private int amplitude = 2;
     // true if Lane is on
     private boolean laneOn = true;
     // use to make sure only 1 message is sent to agent for each part that reaches end of lane
     private boolean receivePartDoneSent = false;
     private boolean purgeDoneSent = false;
     // state of the parts on lane
     private boolean partAtLaneEnd = false;
     private boolean purging = false;
     private boolean jammed = false;
 
     /**
      * LGD constructor
      * 
      * @param lm
      *            - the lane manager (client)
      * @param lid
      *            - lane ID
      */
     public LaneGraphicsDisplay(Client c, int lid) {
 	client = c;
 	laneID = lid;
 
 	partsOnLane = new ArrayList<PartGraphicsDisplay>();
 	// generate lane start location
 	location = new Location(Constants.LANE_END_X, 53 + laneID * 75);
 
 	jamLoc = new Location(Constants.LANE_END_X + (Constants.LANE_LENGTH / 2), location.getY());
 
 	// for reference only
 	partStartLoc = new Location(Constants.LANE_BEG_X, location.getY() + Constants.PART_WIDTH / 2
 		- Constants.PART_OFFSET);
 
 	resetLaneLineLocs();
     }
 
     /**
      * Animates lane movement and sets location of parts moving down lane
      * 
      * @param c
      *            - component on which this is drawn
      * @param g
      *            - the graphics component on which this draws
      */
     @Override
     public void draw(JComponent c, Graphics2D g) {
 	if (laneOn) {
 	    if (laneID % 2 == 0) {
 		g.drawImage(Constants.LANE_IMAGE1, location.getX() + client.getOffset(), location.getY(), c);
 	    } else {
 		g.drawImage(Constants.LANE_IMAGE2, location.getX() + client.getOffset(), location.getY(), c);
 	    }
 
 	    // animate lane movements using lines
 	    for (int i = 0; i < laneLines.size(); i++) {
 		g.drawImage(Constants.LANE_LINE, laneLines.get(i).getX() + client.getOffset(), laneLines.get(i).getY(),
 			c);
 	    }
 	    moveLane();
 
 	    // animate parts moving down lane
 	    if (partsOnLane.size() > 0) {
 
		int min = Math.min(MAX_PARTS, partsOnLane.size());
 		for (int i = 0; i < min; i++) {
 
		    if (i >= partsOnLane.size()) {
			continue;
		    }
 		    PartGraphicsDisplay pgd = partsOnLane.get(i);
 		    Location loc = pgd.getLocation();
 
 		    if (i == 0) { // first part on the lane
 
 			if (loc.getX() > Constants.LANE_END_X - Constants.PART_PADDING) { // hasn't reached end of lane
 			    updateXLoc(loc, Constants.LANE_END_X - Constants.PART_PADDING, speed);
 			    partAtLaneEnd = false;
 			} else { // at end of lane
 			    if (!purging) {
 				partAtLaneEnd = true;
 				msgAgentReceivePartDone();
 			    } else { // purging, continue till off lane
 
 				if (loc.getX() > Constants.LANE_END_X - Constants.PART_WIDTH - Constants.PART_PADDING) {
 				    updateXLoc(loc, Constants.LANE_END_X - Constants.PART_WIDTH
 					    - Constants.PART_PADDING, speed);
 				    // loc.incrementX(-amplitude);
 				} else { // once off lane and not visible, remove
 				    if (partsOnLane.size() > 0) {
 					partsOnLane.remove(0);
 					break;
 				    } else { // all parts removed, done purging
 					purging = false;
 					msgAgentPurgingDone();
 				    }
 				}
 			    }
 			}
 
 		    } else { // all other parts on lane (not first)
 
 			// part in front of i
 			PartGraphicsDisplay pgdInFront = partsOnLane.get(i - 1);
 			Location locInFront = pgdInFront.getLocation();
 
 			// makes sure parts are spaced out as they appear on
 			// lane, but don't overlap part in front
 			if (locInFront.getX() <= Constants.LANE_BEG_X - 2 * Constants.PART_WIDTH
 				- Constants.PART_PADDING
 				&& loc.getX() > locInFront.getX() + Constants.PART_WIDTH + Constants.PART_PADDING / 2) {
 			    updateXLoc(loc, Constants.LANE_END_X - Constants.PART_PADDING, speed);
 			}
 		    }
 		    vibrateParts(loc);
 		    pgd.setLocation(loc);
 		    pgd.drawWithOffset(c, g, client.getOffset());
 
 		} // end for loop
 
 	    } else if (purging) {
 		purging = false;
 		msgAgentPurgingDone();
 	    }
 	} else { // lane is off
 	    if (laneID % 2 == 0) {
 		g.drawImage(Constants.LANE_IMAGE1, location.getX() + client.getOffset(), location.getY(), c);
 	    } else {
 		g.drawImage(Constants.LANE_IMAGE2, location.getX() + client.getOffset(), location.getY(), c);
 	    }
 
 	    // draw lane lines
 	    for (int i = 0; i < laneLines.size(); i++) {
 		g.drawImage(Constants.LANE_LINE, laneLines.get(i).getX() + client.getOffset(), laneLines.get(i).getY(),
 			c);
 	    }
 	}
     }
 
     /**
      * Receives and sorts messages/data from the server
      * 
      * @param r
      *            - the request to be parsed
      */
     @Override
     public void receiveData(Request r) {
 	String cmd = r.getCommand();
 	// parse data request here
 
 	if (cmd.equals(Constants.LANE_PURGE_COMMAND)) {
 	    purge();
 
 	} else if (cmd.equals(Constants.LANE_SET_AMPLITUDE_COMMAND)) {
 	    amplitude = (Integer) r.getData();
 	    speed = (int) 1.5 * amplitude; // TODO: ADJUST THIS
 
 	} else if (cmd.equals(Constants.LANE_TOGGLE_COMMAND)) {
 	    laneOn = (Boolean) r.getData();
 
 	} else if (cmd.equals(Constants.LANE_SET_STARTLOC_COMMAND)) {
 	    location = (Location) r.getData();
 
 	} else if (cmd.equals(Constants.LANE_RECEIVE_PART_COMMAND)) {
 	    PartType type = (PartType) r.getData();
 	    receivePart(type);
 
 	} else if (cmd.equals(Constants.LANE_GIVE_PART_TO_NEST)) {
 	    givePartToNest();
 
 	} else {
 	    System.out.println("LANE_GD: command not recognized.");
 	}
     }
 
     /**
      * Set location of this lane
      */
     @Override
     public void setLocation(Location newLocation) {
 	location = newLocation;
     }
 
     /**
      * Give part to nest, removes from this lane
      */
     private void givePartToNest() {
 	partsOnLane.remove(0);
 	receivePartDoneSent = false; // reset
 	client.sendData(new Request(Constants.LANE_GIVE_PART_TO_NEST + Constants.DONE_SUFFIX, Constants.LANE_TARGET
 		+ laneID, null));
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
     private void updateXLoc(Location loc, int end, int increment) {
 	// System.out.println("loc.getX(): "+loc.getX()+", end: "+end+", abs(dif): "+(Math.abs(end - loc.getX()) <
 	// increment));
 
 	if (Math.abs(end - loc.getX()) < increment) {
 	    loc.setX(end);
 	}
 
 	if (loc.getX() > end) { // moving left
 	    loc.incrementX(-increment);
 	}
     }
 
     /**
      * Animates the lane lines
      */
     private void moveLane() {
 	for (int i = 0; i < laneLines.size(); i++) {
 	    int xCurrent = laneLines.get(i).getX();
 	    if (xCurrent <= Constants.LANE_END_X - LINE_WIDTH) {
 		if (i == 0) {
 		    int xPrev = laneLines.get(laneLines.size() - 1).getX();
 		    laneLines.get(i).setX(xPrev + LINESPACE);
 		} else {
 		    int xPrev = laneLines.get(i - 1).getX();
 		    laneLines.get(i).setX(xPrev + LINESPACE);
 		}
 	    } else {
 		laneLines.get(i).incrementX(-speed);
 	    }
 	}
     }
 
     /**
      * Purges lane of all parts
      */
     private void purge() {
 	// TODO: lane should continue as is, parts fall off the lane
 	purgeDoneSent = false;
 	purging = true;
     }
 
     private void receivePart(PartType type) {
 	PartGraphicsDisplay pg = new PartGraphicsDisplay(type);
 	Location newLoc = new Location(location.getX() + Constants.LANE_LENGTH, location.getY() + Constants.PART_WIDTH
 		/ 2 - Constants.PART_OFFSET);
 	pg.setLocation(newLoc);
 	partsOnLane.add(pg);
 	System.out.println("LANEGD" + laneID + " RECEIVING PART " + partsOnLane.size());
     }
 
     /**
      * Creates an array list of Locations for the lane lines
      */
     private void resetLaneLineLocs() {
 	// create array list of location for lane lines
 	laneLines = new ArrayList<Location>(NUMLINES);
 	int startLineX = Constants.LANE_END_X + LINE_WIDTH;
 	for (int i = 0; i < NUMLINES; i++) {
 	    laneLines.add(new Location(startLineX, location.getY()));
 	    startLineX += LINESPACE;
 	}
     }
 
     /**
      * Changes y-coords to show vibration down lane (may have to adjust values)
      * 
      * @param i
      *            - counter, increments every call to draw
      * @param loc
      *            - location of the current part
      */
     private void vibrateParts(Location loc) {
 	// to show vibration down lane (may have to adjust values)
 	if (loc.getY() <= partStartLoc.getY() + amplitude) {
 	    loc.incrementY(2);
 	} else if (loc.getY() > partStartLoc.getY() - amplitude) {
 	    loc.incrementY(-2);
 	}
     }
 
     private void animateJam() {
 	// TODO
     }
 
     /**
      * Tells the agent that the part has reached the end of the lane. Make sure only sends message once for each part,
      * not on every call to draw.
      */
     private void msgAgentReceivePartDone() {
 	if (partAtLaneEnd && !receivePartDoneSent) {
 	    client.sendData(new Request(Constants.LANE_RECEIVE_PART_COMMAND + Constants.DONE_SUFFIX,
 		    Constants.LANE_TARGET + laneID, null));
 	    System.out.println("	LANEGD" + laneID + ": receive part done sent.");
 	    receivePartDoneSent = true;
 	}
     }
 
     /**
      * Tells the agent that purging is done. Make sure only sends message once for each part, not on every call to draw.
      */
     private void msgAgentPurgingDone() {
 	if (partsOnLane.size() == 0 && !purgeDoneSent) {
 	    client.sendData(new Request(Constants.LANE_PURGE_COMMAND + Constants.DONE_SUFFIX, Constants.LANE_TARGET
 		    + laneID, null));
 	    System.out.println("	LANEGD" + laneID + ": purge done sent.");
 	    purgeDoneSent = true;
 	}
     }
 
 }
