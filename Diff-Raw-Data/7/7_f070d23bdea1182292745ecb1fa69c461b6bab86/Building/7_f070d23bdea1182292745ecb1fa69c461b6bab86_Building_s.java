 package vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
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
     private static int PRODUCE_TIME = 90;
 
     public static final int MAXHEALTH = 100;
     private static UpgradeTree myUpgradeTree;
 
     private Location3D myRallyPoint;
    private List<InteractiveEntity> myInteractiveEntities;
    
     private int myBuildingID;
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
         myRallyPoint = new Location3D(getWorldLocation().getX(), getWorldLocation().getY() + 50, 0);
 
     }
 
     @Override
     public InteractiveEntity copy () {
         return new Building(getImage(), getWorldLocation(), getSize(), getSound(), getPlayerID(),
                             getHealth(), getBuildTime());
     }
 
     @Override
     public UpgradeTree getUpgradeTree () {
         return myUpgradeTree;
     }
 
     @Override
     public void paint (Graphics2D pen) {
         super.paint(pen);
        for (int i = 0; i < myInteractiveEntities.size(); i++) {
            myInteractiveEntities.get(i).paint(pen);
        }
     }
 
     /**
      * Adds the list of available upgrades into the list of available actions.
      */
     public void addUpgradeActions (UpgradeTree upgradeTree) {
         List<UpgradeNode> initialUpgrades = upgradeTree.getCurrentUpgrades();
         addUpgradeActions(initialUpgrades);
     }
 
     public void addUpgradeActions (List<UpgradeNode> nodeList) {
         /*
          * for (final UpgradeNode u: nodeList) {
          * getActions().add(new Action(u.getUpgradeName(), null, "An upgrade action"){
          * 
          * @Override
          * public void apply(int playerID) throws IllegalArgumentException, SecurityException,
          * IllegalAccessException, InvocationTargetException, InstantiationException,
          * NoSuchMethodException{
          * u.apply(playerID);
          * getActions().remove(this);
          * if (!u.getChildren().isEmpty()) {
          * addUpgradeActions(u.getChildren());
          * }
          * }
          * });
          * 
          * }
          */
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
 
     /**
      * TESTING PURPOSE.
      * Mimics player's click on action. Now invoke action after certain time.
      */
     @Override
     public void update (double elapsedTime) {
         super.update(elapsedTime);
         PRODUCE_TIME -= elapsedTime;
         if (PRODUCE_TIME <= 0) {
             try {
                 // findAction("Boost1").apply(1);
             }
             catch (Exception e) {
                 e.printStackTrace();
             }
             PRODUCE_TIME = 90;
         }
 
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
