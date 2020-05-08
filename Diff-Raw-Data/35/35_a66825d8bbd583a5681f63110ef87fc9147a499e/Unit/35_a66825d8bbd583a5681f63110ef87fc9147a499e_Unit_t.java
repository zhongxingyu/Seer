 package vooga.rts.gamedesign.sprite.gamesprites.interactive.units;
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import vooga.rts.action.InteractiveAction;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.DragCommand;
 import vooga.rts.commands.PositionCommand;
 import vooga.rts.action.Action;
 import vooga.rts.action.InteractiveAction;
 import vooga.rts.gamedesign.sprite.gamesprites.GameEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.GameSprite;
 import vooga.rts.gamedesign.sprite.gamesprites.Resource;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.IGatherable;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.strategy.gatherstrategy.CanGather;
 import vooga.rts.gamedesign.strategy.gatherstrategy.CannotGather;
 import vooga.rts.gamedesign.strategy.gatherstrategy.GatherStrategy;
 import vooga.rts.gamedesign.strategy.occupystrategy.OccupyStrategy;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 
 /**
  * This class is an extension of InteractiveEntity, and represents shapes that
  * have the potential to gather resources (contains GatherStrategy) and the
  * potential to be occupied (contains OccupyStrategy). The movement of Units are
  * defined by the AI Engine.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu
  * 
  */
 public class Unit extends InteractiveEntity {
 
 	//default values
     public static Pixmap DEFAULT_IMAGE = new Pixmap("images/sprites/soldier.png");
     public static Location3D DEFAULT_LOCATION = new Location3D();
     public static Dimension DEFAULT_SIZE = new Dimension(90,90);
     public static Sound DEFAULT_SOUND = null;
     public static int DEFAULT_PLAYERID = 1;
     public static int DEFAULT_HEALTH = 100;
     
 
     private GatherStrategy myGatherStrategy;
     
     public Unit () {
         this(DEFAULT_IMAGE, DEFAULT_LOCATION, DEFAULT_SIZE, DEFAULT_SOUND, DEFAULT_PLAYERID, DEFAULT_HEALTH, InteractiveEntity.DEFAULT_BUILD_TIME, InteractiveEntity.DEFAULT_SPEED);
         Information i = new Information("Marine", "I fear no darkness. I was born in it", null, "buttons/marine.png");
         setInfo(i);
 
     }
     
     /**
      * Creates a new unit with an image, location, size, sound, teamID,
      * health, and upgrade tree
      * 
      * @param image
      *        is the image of the unit
      * @param center
      *        is the position of the unit on the map
      * @param size
      *        is the size of the unit
      * @param sound
      *        is the sound the unit makes
      * @param playerID
      *        is the ID for the team that the unit is on
      * @param health
      *        is the max health of the unit
      */
     public Unit (Pixmap image,
                  Location3D center,
                  Dimension size,
                  Sound sound,
                  int playerID,
                  int health,
                  double buildTime,
                  int speed) {
         super(image, center, size, sound, playerID, health, buildTime);
         myGatherStrategy = new CannotGather();
         setSpeed(speed);
         addActions();
     }
 
     public Unit (Pixmap image, Sound sound, int health, double buildTime, int speed) {
         this(image, InteractiveEntity.DEFAULT_LOCATION, DEFAULT_SIZE, sound,
              InteractiveEntity.DEFAULT_PLAYERID, health, buildTime, speed);
 
     }
 
     @Override
     public void addActions () {
         put(ClickCommand.LEFT_CLICK, new InteractiveAction(this) {
             private Location3D myLocation;
 
             @Override
             public void apply () {
                 getEntity().move(myLocation);
             }
 
             @Override
             public void update (Command command) {
                 ClickCommand click = (ClickCommand) command;
                 myLocation = Camera.instance().viewtoWorld(click.getPosition());
             }
         });
     }
 
     public void occupy (InteractiveEntity i) {
         i.getOccupied(this);
     }
 
     @Override
     public InteractiveEntity copy () {
    	Unit copyUnit = new Unit(getImage(), getWorldLocation(), getSize(), getSound(), getPlayerID(),
                getHealth(), getBuildTime(), getSpeed());
    	copyUnit.setInfo(this.getInfo());
    	copyUnit.setActions(this.getActions());
        return copyUnit;
    	
     }
 
 	/**
 	 * Sets the amount that the worker can gather at a time.
 	 * 
 	 * @param gatherAmount
 	 *            is the amount that the worker can gather
 	 */
 	public void setGatherAmount(int gatherAmount) {
 		myGatherStrategy.setGatherAmount(gatherAmount);
 		myGatherStrategy = new CanGather(CanGather.DEFAULTCOOL,
 				myGatherStrategy.getGatherAmount());
 	}
 	
 	public void setGatherStrategy(GatherStrategy gatherStrategy) {
 		myGatherStrategy = gatherStrategy;
 	}
 	
 	//OLD WORKER METHODS. Put here just in case
 	
 	/**
 	 * Another recognize method specific for workers as they can gather
 	 * resources which are not InteractiveEntities
 	 */
 	public void recognize(Resource resource) {
 		gather(resource);
 	}
 
 	/**
 	 * The worker gathers the resource if it can and then resets its gather
 	 * cooldown.
 	 * 
 	 * @param gatherable
 	 *            is the resource being gathered.
 	 */
 	public void gather(IGatherable gatherable) {
 		// shouldnt the cast be to a type Resource?
 		if (this.collidesWith((GameEntity) gatherable)) {
 			myGatherStrategy.gatherResource(getPlayerID(), gatherable);
 		}
 	}
 }
