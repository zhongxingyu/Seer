 import java.awt.Image;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 
 public class Tower {
 
 	private Image base, arm,arrow;
 	Util.TowerType type;
 	String dir;
 	private int x, y, armX, armY, width, height;
 	private boolean isFiring;
 	private int rate, range, cost,upgradeCost,windmillBonus;
 	private int fireCounter = 0;
 	private int killCount = 0;
 	private boolean turnedAround = false;
 	private boolean trashRemoves = false;
 	private boolean highlighted = false;
 	private boolean valid = false;
 	private Trash theTrash;
 	int curPath;
 
 	Tower(int initX, int initY, int fireRate, int towerRange,Util.TowerType type, boolean isValid, String dir) {
 		ImageIcon ii;
 		// Get image for the base
 		ii = getBaseImageIcon(type, dir, isValid);
 		base = ii.getImage();
 		this.dir = dir;
 		width = ii.getIconWidth();
 		height = ii.getIconHeight();
 		valid=isValid;
 		x = initX;
 		y = initY;
 		armX = initX;
 		armY = initY;
 		range = towerRange;
 		rate = fireRate;
 		isFiring = false;
 		this.type = type;
 		
 		// Get image for the arm
 		if (type != Util.TowerType.windmill && isValid) {
 			ii = getArmImageIcon(type, dir);
 			arm = ii.getImage();
 			windmillBonus=100;
 		}
 		windmillBonus=100;
 		
 		switch(type){
 			case compost: cost=250; break;
 			case incenerator: cost=100; break;
 			case metal: cost=250; break;
 			case nuclear: cost=400; break;
 			case recycle: cost=200; break;
 			case windmill: cost=300; break;
 		}
 		upgradeCost=(int)(cost*1.5);
 	}
 	
 	public ImageIcon getArrowImageIcon(String dir, boolean valid){
 		ImageIcon ii;
 		
 		
 		if(valid){
 			ii = new ImageIcon(this.getClass().getResource(
 				"pics/arrow" + dir + ".png"));
 			System.out.println(this.getClass().getResource(
 					"pics/arrow" + dir + ".png"));
 		}else{
 			ii = new ImageIcon(this.getClass().getResource(
 					"pics/arrow" + dir + "Invalid.png"));
 		}
 		return ii;
 	}
 
 	public ImageIcon getArmImageIcon(Util.TowerType type, String dir) {
 		ImageIcon ii;
 		String typeString = "";
 		if (type == Util.TowerType.incenerator) {
 			typeString = "Incenerator";
 
 		} else if (type == Util.TowerType.compost) {
 
 			typeString = "Compost";
 
 		} else if (type == Util.TowerType.recycle) {
 
 			typeString = "Recycle";
 
 		}else if (type == Util.TowerType.nuclear) {
 
 			typeString = "Nuclear";
 
 		} else if (type == Util.TowerType.metal) {
 
 			typeString = "Metal";
 
 		} else {
 			typeString = "Recycle";
 		}
 		ii = new ImageIcon(this.getClass().getResource(
 				"pics/Towers/" + typeString + "/arm" + dir + ".png"));
 		return ii;
 	}
 
 	public ImageIcon getBaseImageIcon(Util.TowerType type, String dir,
 			boolean valid) {
 		ImageIcon ii;
 		if (type == Util.TowerType.incenerator) {
 
 			if (valid) {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Incenerator/base" + dir + ".png"));
 			} else {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Incenerator/base" + dir + "Invalid.png"));
 			}
 
 		} else if (type == Util.TowerType.compost) {
 
 			if (valid) {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Compost/base" + dir + ".png"));
 			} else {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Compost/base" + dir + "Invalid.png"));
 			}
 
 		} else if (type == Util.TowerType.recycle) {
 
 			if (valid) {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Recycle/base" + dir + ".png"));
 			} else {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Recycle/base" + dir + "Invalid.png"));
 			}
 
 		} 
 		else if (type == Util.TowerType.nuclear) {
 
 			if (valid) {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Nuclear/base" + dir + ".png"));
 			} else {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Nuclear/base" + dir + "Invalid.png"));
 			}
 
 		} else if (type == Util.TowerType.metal) {
 
 			if (valid) {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Metal/base" + dir + ".png"));
 			} else {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Metal/base" + dir + "Invalid.png"));
 			}
 
 			// Windmills don't rotate
 		} else if (type == Util.TowerType.windmill) {
 			if (valid) {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Windmill/base.png"));
 			} else {
 				ii = new ImageIcon(this.getClass().getResource(
 						"pics/Towers/Windmill/baseInvalid.png"));
 			}
 		} else {
 			ii = new ImageIcon(this.getClass().getResource(
 					"pics/Trash/paper.png"));
 		}
 		return ii;
 	}
 
 	public String rotateDir() {
 		// Find the direction that we need to rotate to
 		if (dir.equalsIgnoreCase("north")) {
 			dir = "East";
 		} else if (dir.equalsIgnoreCase("South")) {
 			dir = "West";
 
 		} else if (dir.equalsIgnoreCase("East")) {
 			dir = "South";
 
 		} else if (dir.equalsIgnoreCase("West")) {
 			dir = "North";
 		}
 		ImageIcon ii = null;
 		// Get updated image for base
 		ii = getBaseImageIcon(type, dir, true);
 		base = ii.getImage();
 		// Get updated image for arm
 		ii = getArmImageIcon(type, dir);
 		arm = ii.getImage();
 		return dir;
 	}
 
 	public int getArmX() {
 		return armX;
 	}
 
 	public int getArmY() {
 		return armY;
 	}
 
 	public void extendArm(int dist) {
 		
 		if (dir.equalsIgnoreCase("North")) {
 			armY -= dist;
 		} else if (dir.equalsIgnoreCase("South")) {
 			armY += dist;
 		} else if (dir.equalsIgnoreCase("East")) {
 			armX += dist;
 		} else if (dir.equalsIgnoreCase("West")) {
 			armX -= dist;
 		}
 	}
 
 	public void fire() {
 		
 		if (fireCounter < Util.pathWidth) {
 			extendArm(rate);
 			fireCounter += rate;
 		} else if (fireCounter <= Util.pathWidth * 2) {
 			if (!trashRemoves){
 				theTrash.removeImage();
 				killCount++;
 				if(!Board.effectMute){
 
	                  
	                   String path = "audio/trashkill.wav";
 
 	                   try {
	                	   InputStream in = getClass().getResourceAsStream(path);
 	                       AudioInputStream as = AudioSystem.getAudioInputStream(in);
 	                       Clip clip = AudioSystem.getClip();
 	                       clip.open(as);
 	                       clip.start();
 	                   } catch (FileNotFoundException e) {
 	                       // TODO Auto-generated catch block
 	                       e.printStackTrace();
 	                   } catch (UnsupportedAudioFileException e) {
 	                       // TODO Auto-generated catch block
 	                       e.printStackTrace();
 	                   } catch (IOException e) {
 	                       // TODO Auto-generated catch block
 	                       e.printStackTrace();
 	                   } catch (LineUnavailableException e) {
 	                       // TODO Auto-generated catch block
 	                       e.printStackTrace();
 	                   }
 	               }
 				trashRemoves = true;
 				}
 			
 			extendArm(-rate);
 			fireCounter += rate;
 		} else {
 			fireCounter = 0;
 			armX = x;
 			armY = y;
 			isFiring = false;
 			trashRemoves = false;
 			theTrash.removeImage();
 			theTrash = null;
 		}
 	}
 
 	public boolean getFiring() {
 		return isFiring;
 	}
 
 	public void setFiring(boolean newFire, Trash aTrash) {
 		theTrash = aTrash;
 		isFiring = newFire;
 	}
 
 	public Image getBaseImage() {
 		return base;
 	}
 	
 	public Image getArrowImage(){
 		arrow=getArrowImageIcon(this.dir,this.valid).getImage();
 		return arrow;
 	}
 
 	public int getRange() {
 		return range;
 	}
 
 	public void setX(int newX) {
 		x = newX;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setY(int newY) {
 		y = newY;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setType(Util.TowerType newType) {
 		this.type = newType;
 	}
 
 	public Util.TowerType getType() {
 		return type;
 	}
 
 	public int getRate() {
 		return rate;
 	}
 
 	public int getFireCounter() {
 		return fireCounter;
 	}
 
 	public Image getArmImage() {
 		return arm;
 	}
 
 	public String getDirection() {
 		return dir;
 	}
 
 	public void resetTower() {
 		armX=x;
 		armY=y;
 		isFiring=false;
 		fireCounter=0;
 	}
 	
 	public void setHighLight(boolean yes){
 		highlighted=yes;
 	}
 	public boolean isHighLight(){
 		return highlighted;
 	}
 	
 	public int getKillCount(){
 		return killCount;
 	}
 	
 	public void upgrade(){
 		
 		upgradeCost*=2;
 		cost*=1.5;
 		rate++;
 		if(type==Util.TowerType.windmill){
 			windmillBonus*=2.5;
 		}
 	}
 		
 	
 	public int getCost(){
 		return cost;
 	}
 	
 	public int getUpgradeCost(){
 		return upgradeCost;
 	}
 	
 	public int getWindmillBonus(){
 		return windmillBonus;
 	}
 	
 }
