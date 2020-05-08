 package vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.upgrades.UpgradeNode;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 
 /**
  * This is an class that represents a building. It will be extended
  * by specific types of buildings such as AttackTower.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu
  * 
  */
 
 public class Building extends InteractiveEntity {
     public static final int PRODUCE_TIME = 90;
     public static final Dimension DEFAULT_SIZE = new Dimension(100,100);
     public static final int MAXHEALTH = 100;
 
     private Location3D myRallyPoint;
     /**
      * Creates a new building with a rally point, a list of what can be 
      * produced, a list of what can observe the building, and an upgrade tree.
      * @param image is the picture of the building
      * @param center is the location of the building
      * @param size is the dimensions of the building
      * @param sound is the sound that the building makes
      * @param playerID is the team that the building is on
      * @param health is how much health the building has
      * @param buildTime is the time it takes to create a building
      * @param upgradeTree is the upgrade tree for the building
      */
     public Building (Pixmap image,
                      Location3D center,
                      Dimension size,
                      Sound sound,
                      int playerID,
                      int health,
                      double buildTime) {
         super(image, center, size, sound, playerID, MAXHEALTH, buildTime);
         myRallyPoint = new Location3D(getWorldLocation().getX(), getWorldLocation().getY() + 150, 0);
 
     }
     
     public Building(Pixmap image, Sound sound, int health, double buildTime) {
     	this(image, InteractiveEntity.DEFAULT_LOCATION, DEFAULT_SIZE, sound, InteractiveEntity.DEFAULT_PLAYERID, health, buildTime);
     }
 
     @Override
     public InteractiveEntity copy () {
        return new Building(getImage(), getWorldLocation(), getSize(), getSound(), getPlayerID(),
                            getHealth(), getBuildTime());
     }
 
 
     /**
      * Returns the rally point of the production building.
      * Will be used to move the newly created units to
      * 
      * @return myRallyPoint, the rally point of the
      *         production building
      */
     public Location3D getRallyPoint () {
         return myRallyPoint;
     }
 
     /**
      * Sets the rally point of the production building
      * 
      * @param rallyPoint the location of the new rally point
      */
     public void setRallyPoint (Location3D rallyPoint) {
         myRallyPoint = rallyPoint;
     }
 
     @Override
     public int getSpeed() {
     	return 0;
     }
 
 	@Override
 	public void addActions() {
 		// TODO Auto-generated method stub
 		
 	}
 }
