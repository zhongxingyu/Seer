 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import factory.data.PartType;
 
 /**
  * This class handles drawing of the feeder and diverter.
  * @author Harry Trieu
  *
  */
 
 public class FeederGraphicsDisplay extends DeviceGraphicsDisplay {
 	// this will store a reference to the client
 	private Client client;
 	
 	private static final int FEEDER_HEIGHT = 120;
 	private static final int FEEDER_WIDTH = 120;
 	private static final int DIVERTER_HEIGHT = 25;
 	private static final int DIVERTER_WIDTH = 98;
 	
 	private static final double DIVERTER_POINTING_TOP_ANGLE = 0.18;
 	private static final double DIVERTER_POINTING_BOTTOM_ANGLE = -0.18;
 	private static final double DIVERTER_STEP = Math.abs((DIVERTER_POINTING_TOP_ANGLE-DIVERTER_POINTING_BOTTOM_ANGLE)/20);
 	private static final int STEPS_TO_ROTATE_DIVERTER = (1000/Constants.TIMER_DELAY);
 	
 	// v0 stuff
 	private static final int STEPS_TO_MOVE_PART = 50;
 	
 	private static Image diverterImage = Toolkit.getDefaultToolkit().getImage("src/images/Diverter.png");
 	private static Image feederImage = Toolkit.getDefaultToolkit().getImage("src/images/Feeder.png");
 	
 	// true if the diverter is pointing to the top lane
 	private boolean diverterTop;
 	// number of steps remaining for the diverter to finish rotating
 	private int animationCounter;
 	
 	// true if a bin has been received
 	private boolean haveBin;
 	
 	// TODO what if a bin is purged?
 	
 	// location of the feeder
 	private Location feederLocation;
 	// location of the diverter
 	private Location diverterLocation;
 	
 	// the final location of the part before it leaves the diverter
 	private Location finalPartLocation;
 	
 	
 	private Location startingPartLocation;
 	
 	// v0 stuff
 	private BinGraphicsDisplay bgd; 
 	private ArrayList<PartGraphicsDisplay> partGDList = new ArrayList<PartGraphicsDisplay>();
 	
 	private double xIncrements;
 	private double yIncrements;
 	
 	/**
 	 * constructor
 	 */
 	public FeederGraphicsDisplay(Client cli, Location loc) {
 		// store a reference to the client
 		client = cli;
 		
 		// set the feeder's default location
 		feederLocation = loc;
 		
 		// set the diverter's default location
 		diverterLocation = new Location(feederLocation.getX()-90, feederLocation.getY()+(FEEDER_HEIGHT/2)-(DIVERTER_HEIGHT/2));
 		
 		// diverter initially points to the top lane
 		diverterTop = true;
 		
 		// TODO change this later - end of the diverter when it's pointing to the top lane
		finalPartLocation = new Location(diverterLocation.getX(), diverterLocation.getY());
 		
 		// TODO change this later - starting location for parts
 		startingPartLocation = new Location(feederLocation.getX(), feederLocation.getY()+(FEEDER_HEIGHT/2)-(DIVERTER_HEIGHT/2));
 		
 		// do not animate the diverter rotating
 		animationCounter = -1;
 		
 		haveBin = false;
 		
 		
 		// temp V0
 		xIncrements = (finalPartLocation.getX() - startingPartLocation.getX())/STEPS_TO_MOVE_PART;
 		yIncrements = (finalPartLocation.getY() - startingPartLocation.getY())/STEPS_TO_MOVE_PART;
 		
 		
 		// force an initial repaint to display feeder and diverter
 		client.repaint();
 	}
 	
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 		AffineTransform originalTransform = g.getTransform();
 		
 		if (animationCounter < 0) {
 			if (diverterTop) {
 				g.rotate(DIVERTER_POINTING_TOP_ANGLE, feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
 			} else {
 				g.rotate(DIVERTER_POINTING_BOTTOM_ANGLE, feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
 			}
 		} else {
 			if (diverterTop) {
 				g.rotate(DIVERTER_POINTING_BOTTOM_ANGLE + ((STEPS_TO_ROTATE_DIVERTER-animationCounter)*DIVERTER_STEP), feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
 			} else {
 				g.rotate(DIVERTER_POINTING_TOP_ANGLE - ((STEPS_TO_ROTATE_DIVERTER-animationCounter)*DIVERTER_STEP), feederLocation.getX(), diverterLocation.getY() + DIVERTER_HEIGHT/2);
 			}
 			animationCounter--;
 		}		
 		
 		g.drawImage(diverterImage, diverterLocation.getX(), diverterLocation.getY(), c);
 		
 		if (partGDList.size() > 0) {
 			for(PartGraphicsDisplay part : partGDList) {
				if(part.getLocation().compareToX(diverterLocation) > 0) {			
 				
 					part.setLocation(new Location(part.getLocation().getX() + xIncrements, part.getLocation().getY() + yIncrements));
 					part.draw(c, g);
 				}
 			}
 		}
 		
 		g.setTransform(originalTransform);
 		g.drawImage(feederImage, feederLocation.getX(), feederLocation.getY(), c);
 		
 		if (haveBin) {
 			bgd.draw(c, g);
 		}
 	}
 
 	public void flipDiverter() {
 		animationCounter = STEPS_TO_ROTATE_DIVERTER;
 		diverterTop = !diverterTop;
 		
 		if (diverterTop) {
 			finalPartLocation = new Location(feederLocation.getX() - 100, feederLocation.getY());
 		} else {
 			finalPartLocation = new Location(feederLocation.getX() - 100, feederLocation.getY() + 50);
 		}
 	}
 	
 	@Override
 	public void setLocation(Location newLocation) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.FEEDER_FLIP_DIVERTER_COMMAND)) {
 			flipDiverter();
 		} else if (req.getCommand().equals(Constants.FEEDER_RECEIVED_BIN_COMMAND)) {
 			// TODO fix bin coordinates
 			
 			bgd = new BinGraphicsDisplay(new Location(feederLocation.getX() + FEEDER_WIDTH - 50, feederLocation.getY() + FEEDER_HEIGHT/2), PartType.B);
 			bgd.setFull(true);
 			
 			haveBin = true;
 		} else if (req.getCommand().equals(Constants.FEEDER_PURGE_BIN_COMMAND)) {
 			// TODO future: move bin to purge area
 			// cannot purge bin unless there is a bin
 			
 			bgd.setFull(false); // could be problematic if called when bin has not been received
 			haveBin = false;
 		} else if (req.getCommand().equals(Constants.FEEDER_MOVE_TO_DIVERTER_COMMAND)) {
 			
 			PartGraphicsDisplay part = new PartGraphicsDisplay(bgd.getPartType());
 			
 			// where the part starts
 			part.setLocation(startingPartLocation);
 			partGDList.add(part);
 						
 		} else if (req.getCommand().equals(Constants.FEEDER_MOVE_TO_LANE_COMMAND)) {
 			
 		}
 	}
 
 }
