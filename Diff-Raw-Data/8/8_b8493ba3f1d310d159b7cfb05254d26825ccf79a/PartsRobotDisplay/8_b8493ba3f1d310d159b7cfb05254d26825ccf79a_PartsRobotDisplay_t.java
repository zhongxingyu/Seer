 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import Utils.PartData;
 import factory.PartType;
 
 public class PartsRobotDisplay extends DeviceGraphicsDisplay {
 
 	private static Image partsRobotImage;
 	private static ArrayList<Image> armImage;
 
 	private final ArrayList<PartGraphicsDisplay> partArrayGraphics;
 
 	private Location loc, kitloc;
 	private final Location initialLocation;
 	private final Location currentLocation;
 	private final Location armLocation;
 
 	private PartType pt;
 	private int arm;
 	private int droparm;
 	private PartType ptdrop;
 	
 	private int II;
 	private boolean move;
 	
 	private boolean rotate;
 	private boolean home;
 	private boolean pickup, givekit;
 	private boolean gotpart;
 	private final boolean sendmsg;
 	private boolean gavepart;
 
 	private final ArrayList<Location> partStartLoc;
 	private final ArrayList<Location> armLoc;
 	private int I;
 
 	public PartsRobotDisplay(Client prc) {
 		client = prc;
 
 		initialLocation = Constants.PARTS_ROBOT_LOC;
 		currentLocation = initialLocation;
 		armLocation = currentLocation;
 
 		partsRobotImage = Toolkit.getDefaultToolkit().getImage("src/images/parts_robot.png");
 		armImage = new ArrayList<Image>();
 		
 		for (int j = 0; j < 4; j++) {
 			armImage.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 		}
 
 		partArrayGraphics = new ArrayList<PartGraphicsDisplay>(4);
 		
 		
 
 		rotate = false;
 		home = true;
 		pickup = false;
 		givekit = false;
 
 		partStartLoc = new ArrayList<Location>();
 		partStartLoc.add(new Location(currentLocation.getX(), currentLocation.getY()));
 		partStartLoc.add(new Location(currentLocation.getX() + 30, currentLocation.getY()));
 		partStartLoc.add(new Location(currentLocation.getX(), currentLocation.getY() + 30));
 		partStartLoc.add(new Location(currentLocation.getX() + 30, currentLocation.getY() + 30));
 		
 		PartType pt = new PartType(null);
 		PartGraphicsDisplay pgd = new PartGraphicsDisplay(pt);
 		for (int i = 0; i<4;i++){
 		pgd.setLocation(partStartLoc.get(i));
 		partArrayGraphics.add(pgd);
 		}
 		
 		armLoc = new ArrayList<Location>();
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY()));
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY() + 30));
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY() + 60));
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY() + 90));
 		
 
 		I = 0;
 		II = 0;
 		move = false;
 		gotpart = false;
 		sendmsg = false;
 		gavepart = false;
 	}
 
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 
 		if (pickup) {
 			for (int i = 0; i < 5; i++) {
 				if (currentLocation.getX() < loc.getX() - 60) {
 					currentLocation.incrementX(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getX() > loc.getX() - 60) {
 					currentLocation.incrementX(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getY()-60+arm*30 > loc.getY()) {
 					currentLocation.incrementY(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getY()-60+arm*30 < loc.getY()) {
 					currentLocation.incrementY(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}
 			}
 
 			if (currentLocation.getX() == loc.getX() - 60 && currentLocation.getY()-60+arm*30 == loc.getY()) {
 				
 				if (armLoc.get(arm).getX() != loc.getX() && !gotpart) {
 					for (int i = 0; i < 5; i++) {
 						extendArm();
 					}
 
 					if (armLoc.get(arm).getX() == loc.getX()) {
 						pickUpPart();
 						
 						gotpart = true;
 					}
 				}
 
 				else {
 
 					if (armLoc.get(arm).getX() != loc.getX() -60) {
 					
 						for (int i = 0; i < 5; i++) {
 							retractArm();
 						}
 						if (armLoc.get(arm).getX() == loc.getX() - 60) {
 							pickup = false;
 							I++;
 							client.sendData(new Request(Constants.PARTS_ROBOT_RECEIVE_PART_COMMAND
 									+ Constants.DONE_SUFFIX, Constants.PARTS_ROBOT_TARGET, null));
 						}
 					}
 
 				}
 
 			}
 			
 			for (int k = 0; k < 4; k++) {
 				g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 			}
 
 			g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 
 		} else if (givekit) {
 			
 			for (int i = 0; i < 5; i++) {
 				if (currentLocation.getX() > kitloc.getX() - 60) {
 					currentLocation.incrementX(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}else if (currentLocation.getX() < kitloc.getX() - 60) {
 						currentLocation.incrementX(1);
 						updateArmLoc(currentLocation);
 						updatePartLoc(armLoc);
				} else if (currentLocation.getY()+arm*30-40 > kitloc.getY()) {
 					currentLocation.incrementY(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
				} else if (currentLocation.getY()+arm*30-40 < kitloc.getY()) {
 					currentLocation.incrementY(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}
 			}
 		
			if (currentLocation.getX() == kitloc.getX() - 60 && currentLocation.getY()+arm*30-40 == kitloc.getY()) {
 				
 				/*if (partArrayGraphics.isEmpty() && !gavepart) {
 					for (int i = 0; i < 5; i++) {
 						retractArmFromKit();
 					}
 					givekit = false;
 					gavepart = true;
 					I = 0;
 				} else*/ if (armLoc.get(arm).getX() != kitloc.getX()+arm*20 && !gavepart) {
 					
 					for (int i = 0; i < 5; i++) {
 						
 						extendArmToKit();
 					}
 					if (armLoc.get(arm).getX() == kitloc.getX()) {
 						
 						PartType tempPartType = partArrayGraphics.get(arm).getPartType();
 						client.sendData(new Request(
 								Constants.KIT_UPDATE_PARTS_LIST_COMMAND
 										+ Constants.DONE_SUFFIX,
 								Constants.PARTS_ROBOT_TARGET, tempPartType));
 						gavepart = true;
 					}
 				} else {
 					if (armLoc.get(arm).getX() != kitloc.getX() - 60 ) {
 						
 						PartType tempPartType = givePart();
 						PartType partType = new PartType(null);
 						PartGraphicsDisplay pgd = new PartGraphicsDisplay(partType);
 						partArrayGraphics.add(arm,pgd);
 						for (int i = 0; i < 5; i++) {
 							retractArmFromKit();
 						}
 						
 						
 						if (armLoc.get(arm).getX() == kitloc.getX() - 60) {
 						
 							I--;
 							if (arm == 3)
 								move = !move;
 							if (move)
 								II = 1;
 							if (!move)
 								II = 0;
 							client.sendData(new Request(Constants.PARTS_ROBOT_GIVE_COMMAND + Constants.DONE_SUFFIX,
 									Constants.PARTS_ROBOT_TARGET, tempPartType));
 							gavepart = false;
 							givekit = false;
 							
 						}
 					}
 				}
 
 			}
 			
 			for (int k = 0; k < 4; k++) {
 				g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 			}
 			g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 
 		} else if (!givekit && !pickup) {
 			for (int i = 0; i < 5; i++) {
 				if (currentLocation.getY() < 450) {
 					currentLocation.incrementY(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getX() > 250) {
 					currentLocation.incrementX(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getX() < 250) {
 					currentLocation.incrementX(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}
 
 				for (int k = 0; k < 4; k++) {
 					g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 				}
 
 				g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 
 			}
 		} else if (home) {
 			
 			g.drawImage(partsRobotImage, initialLocation.getX() + client.getOffset(), initialLocation.getY(), c);
 
 			for (int k = 0; k < 4; k++) {
 				g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 			}
 		}
 
 		for (int i = 0; i < 4; i++) {
 			PartGraphicsDisplay pgd = partArrayGraphics.get(i);
 			if (pgd != null)
 				pgd.drawPokeball(client.getOffset(),partStartLoc.get(i),c,g);
 		}
 
 	}
 
 	public void pickUp() {
 		home = false;
 		pickup = true;
 		givekit = false;
 		gotpart = false;
 		gavepart = false;
 	}
 
 	public void giveKit() {
 		home = false;
 		givekit = true;
 		pickup = false;
 	}
 	
 	public void updateArmLoc(Location loc){
 		
 		for (int k = 0; k < 4; k++) {
 			armLoc.get(k).setX(loc.getX());
 			armLoc.get(k).setY(currentLocation.getY() + 30 * k);
 		}
 		
 	}
 	
 	public void updatePartLoc(ArrayList<Location> loc){
 		
 		for (int j = 0; j < 4; j++) {
 			
 			if (j<4){
 			partStartLoc.get(j).setX(loc.get(j).getX() + 20);
 			partStartLoc.get(j).setY(loc.get(j).getY()-50);
 			partArrayGraphics.get(j).setLocation(partStartLoc.get(j));
 			}
 		}
 		
 	}
 
 	public void pickUpPart() {
 
 		PartType partType = pt;
 		PartGraphicsDisplay pgd = new PartGraphicsDisplay(partType);
 		
 		pgd.setLocation(partStartLoc.get(arm));
 		partArrayGraphics.add(arm,pgd);
 			
 	}
 
 	public PartType givePart() {
 			return partArrayGraphics.remove(arm).getPartType();
 			//Reminder for Adrian: Add sendData to Stand
 	}
 	
 	public void dropPart() {
 		System.out.println("Drop Arm:" +droparm);
 		partArrayGraphics.remove(droparm);
 		
 		PartGraphicsDisplay pgd = new PartGraphicsDisplay(ptdrop);
 		partArrayGraphics.add(droparm,pgd);
 		client.sendData(new Request(Constants.PARTS_ROBOT_DROP_PART_COMMAND + Constants.DONE_SUFFIX,
 				Constants.PARTS_ROBOT_TARGET, null));
 		
 		
 	}
 
 	public void extendArm() {
 		
 		armLoc.get(arm).incrementX(1);
 		partStartLoc.get(arm).setX(armLoc.get(arm).getX() + 20);
 		partStartLoc.get(arm).setY(armLoc.get(arm).getY()-50);
 		
 	}
 
 	public void extendArmToKit() {
 		
 		armLoc.get(arm).incrementX(1);
 		partStartLoc.get(arm).setX(armLoc.get(arm).getX() + 20);
 
 	}
 
 	public void retractArm() {
 		armLoc.get(arm).incrementX(-1);
 		partStartLoc.get(arm).setX(armLoc.get(arm).getX() + 20);
 		partArrayGraphics.get(arm).setLocation(partStartLoc.get(arm));
 
 	}
 
 	public void retractArmFromKit() {
 		armLoc.get(arm).incrementX(-1);
 		partStartLoc.get(arm).setX(armLoc.get(arm).getX() + 20);
 
 	}
 
 	public void rotateArm() {
 		rotate = true;
 	}
 
 	public boolean getRotate() {
 		return rotate;
 	}
 
 	@Override
 	public void receiveData(Request r) {
 		if (r.getCommand().equals(Constants.PARTS_ROBOT_GIVE_COMMAND)) {
 			kitloc = ((PartData) r.getData()).getKitLocation();
 			arm = ((PartData) r.getData()).getArm();
 			giveKit();
 		} else if (r.getCommand().equals(Constants.PARTS_ROBOT_PICKUP_COMMAND)) {
 			loc = ((PartData) r.getData()).getLocation();
 			pt = ((PartData) r.getData()).getPartType();
 			arm = ((PartData) r.getData()).getArm();
 			
 			pickUp();
 		} else if (r.getCommand().equals(Constants.PARTS_ROBOT_DROP_COMMAND)) {
 			droparm = ((PartData) r.getData()).getArm();
 			ptdrop = ((PartData) r.getData()).getPartType();
 			dropPart();
 		}
 	}
 	@Override
 	public void setLocation(Location newLocation) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
