 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import DeviceGraphics.BinGraphics;
 import Networking.Client;
 import Networking.Request;
 import Utils.BinData;
 import Utils.Constants;
 import Utils.Location;
 import agent.data.Bin;
 
 public class GantryGraphicsDisplay extends DeviceGraphicsDisplay {
 	
 	Location currentLocation;
 	Location destinationLocation;
 	
 	ArrayList<BinGraphicsDisplay> binList;
 	
 	boolean isBinHeld = false; // Determines whether or not the gantry is holding a bin
 	boolean isMoving = false;
 	
 	BinGraphicsDisplay heldBin;
 	
 	BinData tempBin;
 	
 	Client client;
 	
 	double rotationAxisX;
 	double rotationAxisY;
 	
 	int currentDegree;
 	int finalDegree;
 
 	public GantryGraphicsDisplay (Client c) {
 		currentLocation = new Location (Constants.GANTRY_ROBOT_LOC);
 		destinationLocation = new Location (Constants.GANTRY_ROBOT_LOC);
 		binList = new ArrayList<BinGraphicsDisplay>();
 		client = c;
 		
 		tempBin = null;
 		rotationAxisX = 0;
 		rotationAxisY = 0;
 		currentDegree = 0;
 		finalDegree = 0;
 	}
 	
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 		// If robot is at incorrect Y location, first move bot to inital X location
 		if (currentLocation.getY() != destinationLocation.getY() && currentLocation.getX() != Constants.GANTRY_ROBOT_LOC.getX()) {
 			if(currentLocation.getX() < Constants.GANTRY_ROBOT_LOC.getX()) {
 				currentLocation.incrementX(5);
 			}
 			else if(currentLocation.getX() > Constants.GANTRY_ROBOT_LOC.getX()) {
 				currentLocation.incrementX(-5);
 			}
 		}
 		
 		//If robot is in initial X, move to correct Y
 		if(currentLocation.getX() == Constants.GANTRY_ROBOT_LOC.getX() && currentLocation.getY() != destinationLocation.getY()) {
 			if(currentLocation.getY() < destinationLocation.getY()) {
 				currentLocation.incrementY(5);
 			}
 			if(currentLocation.getY() > destinationLocation.getY()) {
 				currentLocation.incrementY(-5);
 			}
 		}
 		
 	/*	//If robot is at correct Y, rotate
 		if (currentLocation.getY() == destinationLocation.getY()) {
 			if (finalDegree == 0 || finalDegree == 180) {
 				if (currentLocation.getX() < destinationLocation.getX()){
 					finalDegree = 90;
 				}
 				else
 					finalDegree = -90;
 			}
 			
 			if (currentDegree != finalDegree){
 				rotationAxisX = 
 			}
 		}*/
 		
 		
 		
 		//If robot is at correct Y and correct rotation, move to correct X
 		if (currentLocation.getY() == destinationLocation.getY() && currentLocation.getX() != destinationLocation.getX()) { //&& currentDegree == finalDegree) {
 			if(currentLocation.getX() < destinationLocation.getX()) {
 				currentLocation.incrementX(5);
 			}
 			else if(currentLocation.getX() > destinationLocation.getX()) {
 				currentLocation.incrementX(-5);
 			}
 		
 			if(currentLocation.getX() == destinationLocation.getX() && isMoving == true) {
 				client.sendData(new Request(Constants.GANTRY_ROBOT_DONE_MOVE, Constants.GANTRY_ROBOT_TARGET, null));
 				isMoving = false;
 			}
 		}
 		
 		for (int i = 0; i < binList.size(); i ++) {
 			binList.get(i).drawWithOffset(c, g, client.getOffset());
 			binList.get(i).draw(c, g);
 		}
 			
 		if (isBinHeld) {
 			heldBin.setLocation(currentLocation);
 		}
 		g.drawImage(Constants.GANTRY_ROBOT_IMAGE, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		
 		if (req.getCommand().equals(Constants.GANTRY_ROBOT_GET_BIN_COMMAND)) {
 			tempBin = (BinData) req.getData();
 			for (int i = 0; i < binList.size(); i ++) 
 				if (binList.get(i).getPartType().equals(tempBin.getBinPartType())){
 					heldBin = binList.get(i);
 					isBinHeld = true;
 			}
			//heldBin = new BinGraphicsDisplay(currentLocation, tempBin.getBinPartType());
 			tempBin = null;
 		}
 		else if (req.getCommand().equals(Constants.GANTRY_ROBOT_MOVE_TO_LOC_COMMAND)) {
 			destinationLocation = (Location) req.getData();
 			isMoving = true;
 		}
		else if (req.getCommand().equals(Constants.GANTRY_ROBOT_DROP_BIN_COMMAND)) {			
 			heldBin = null;
 			isBinHeld = false;
 		}
 		else if (req.getCommand().equals(Constants.GANTRY_ROBOT_ADD_NEW_BIN)) {
 			tempBin = (BinData) req.getData();
 			binList.add(new BinGraphicsDisplay(tempBin.getBinLocation(), tempBin.getBinPartType()));
 			tempBin = null;
 		}
 	}
 
 	@Override
 	public void setLocation(Location newLocation) {
 		destinationLocation = newLocation;	
 	}
 
 }
