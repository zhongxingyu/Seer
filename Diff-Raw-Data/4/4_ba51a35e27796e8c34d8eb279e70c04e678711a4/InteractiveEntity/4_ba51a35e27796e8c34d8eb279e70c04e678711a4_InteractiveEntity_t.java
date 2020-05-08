 package vooga.rts.gamedesign.sprite.gamesprites.interactive;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import vooga.rts.action.Action;
 import vooga.rts.action.IActOn;
 import vooga.rts.ai.AstarFinder;
 import vooga.rts.ai.Path;
 import vooga.rts.ai.PathFinder;
import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.InformationCommand;
 import vooga.rts.gamedesign.sprite.gamesprites.GameEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.IAttackable;
 import vooga.rts.gamedesign.sprite.gamesprites.Projectile;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
 import vooga.rts.gamedesign.state.AttackingState;
 import vooga.rts.gamedesign.state.UnitState;
 import vooga.rts.gamedesign.strategy.attackstrategy.AttackStrategy;
 import vooga.rts.gamedesign.strategy.attackstrategy.CannotAttack;
 import vooga.rts.gamedesign.strategy.gatherstrategy.CannotGather;
 import vooga.rts.gamedesign.strategy.gatherstrategy.GatherStrategy;
 import vooga.rts.gamedesign.strategy.occupystrategy.CannotBeOccupied;
 import vooga.rts.gamedesign.strategy.occupystrategy.OccupyStrategy;
 import vooga.rts.gamedesign.strategy.production.CannotProduce;
 import vooga.rts.gamedesign.strategy.production.ProductionStrategy;
 import vooga.rts.gamedesign.strategy.upgradestrategy.CanUpgrade;
 import vooga.rts.gamedesign.strategy.upgradestrategy.UpgradeStrategy;
 import vooga.rts.gamedesign.upgrades.UpgradeNode;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.gamedesign.weapon.Weapon;
 import vooga.rts.state.GameState;
 import vooga.rts.util.Camera;
 import vooga.rts.util.DelayedTask;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 
 /**
  * This class is the extension of GameEntity. It represents shapes that are able
  * to upgrade (to either stat of its current properties or add new properties)
  * and attack others.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu
  * 
  */
 
 public abstract class InteractiveEntity extends GameEntity implements IAttackable, IActOn {
 
     public static final Location3D DEFAULT_LOCATION = new Location3D(0, 0, 0);
     public static final int DEFAULT_PLAYERID = 0;
     private static final int LOCATION_OFFSET = 20;
     private static int DEFAULT_INTERACTIVEENTITY_SPEED = 150;
     private boolean isSelected;
     private UpgradeTree myUpgradeTree;
     private Sound mySound;
     private AttackStrategy myAttackStrategy;
     private ProductionStrategy myProductionStrategy;
     private UpgradeStrategy myUpgradeStrategy;
     private OccupyStrategy myOccupyStrategy;
     private GatherStrategy myGatherStrategy;
     private int myArmor;
     private Map<String, Action> myActions;
     private Map<String, Information> myInfos;
     private List<DelayedTask> myTasks;
     private double myBuildTime;
     private List<InteractiveEntity> myProducables;
     private Information myInfo;
     private PathFinder myFinder;
     private Path myPath;
 
     public static final double DEFAULT_BUILD_TIME = 5;
 
     /**
      * Creates a new interactive entity.
      * 
      * @param image
      *        is the image of the interactive entity
      * @param center
      *        is the location of the interactive entity
      * @param size
      *        is the dimension of the interactive entity
      * @param sound
      *        is the sound the interactive entity makes
      * @param teamID
      *        is the ID of the team that the interactive entity is on
      * @param health
      *        is the health of the interactive entity
      */
     public InteractiveEntity (Pixmap image,
                               Location3D center,
                               Dimension size,
                               Sound sound,
                               int playerID,
                               int health,
                               double buildTime) {
         super(image, center, size, playerID, health);
         // myMakers = new HashMap<String, Factory>(); //WHERE SHOULD THIS GO?
         mySound = sound;
         myAttackStrategy = new CannotAttack();
         myProductionStrategy = new CannotProduce();
         myUpgradeStrategy = new CanUpgrade();
         myGatherStrategy = new CannotGather();
         myActions = new HashMap<String, Action>();
         myInfos = new HashMap<String, Information>();
         isSelected = false;
         myTasks = new ArrayList<DelayedTask>();
         myBuildTime = buildTime;
         myOccupyStrategy = new CannotBeOccupied();
         myProducables = new ArrayList<InteractiveEntity>();
         myPath = new Path();
         myFinder = new AstarFinder();
         setSpeed(DEFAULT_INTERACTIVEENTITY_SPEED);
     }
 
     public void addAction (String command, Action action) {
         myActions.put(command, action);
     }
 
     public void removeAction (String command) {
         myActions.remove(command);
     }
 
     public abstract void addActions ();
 
     public void addTask (DelayedTask dt) {
         myTasks.add(dt);
     }
 
     public void setInfo (Information info) {
         myInfo = info;
     }
 
     public Information getInfo () {
         return myInfo;
     }
 
     public void setUpgradeTree (UpgradeTree upgradeTree) {
         myUpgradeStrategy.setUpgradeTree(upgradeTree, this);
     }
 
     public UpgradeTree getUpgradeTree () {
         return myUpgradeStrategy.getUpgradeTree();
     }
 
     /**
      * returns the list of producables
      */
     public List<InteractiveEntity> getProducables () {
         return myProducables;
     }
 
     /**
      * adds passed in command and info into map
      * 
      * @param command
      * @param info
      */
     public void addInfo (String command, Information info) {
         myInfos.put(command, info);
     }
 
     /**
      * This method specifies that the interactive entity is attacking an
      * IAttackable. It checks to see if the IAttackable is in its range, it sets
      * the state of the interactive entity to attacking, and then it attacks the
      * IAttackable if the state of the interactive entity lets it attack.
      * 
      * @param attackable
      *        is the IAttackable that is being attacked.
      */
     public void attack (IAttackable attackable) {
         double distance =
                 Math.sqrt(Math.pow(getWorldLocation().getX() -
                                    ((InteractiveEntity) attackable).getWorldLocation().getX(), 2) +
                           Math.pow(getWorldLocation().getY() -
                                    ((InteractiveEntity) attackable).getWorldLocation().getY(), 2));
         if (!this.isDead()) {
             // getEntityState().setAttackingState(AttackingState.ATTACKING);
 
             if (getEntityState().getAttackingState() != AttackingState.WAITING &&
                 getEntityState().getAttackingState() != AttackingState.ATTACKING) {
                 getEntityState().attack();
             }
             // setVelocity(getVelocity().getAngle(), 0);
             // getGameState().setMovementState(MovementState.STATIONARY);
             if (getEntityState().canAttack()) {
                 myAttackStrategy.attack(attackable, distance);
 
                 // System.out.println("Can Attack?");
             }
         }
     }
 
     public int calculateDamage (int damage) {
         return damage * (1 - (myArmor / (myArmor + 100)));
     }
 
     public boolean containsInput (Command command) {
         return myActions.containsKey(command.getMethodName());
     }
 
     /**
      * Creates a copy of an interactive entity.
      **/
     public abstract InteractiveEntity copy ();
 
     /**
      * Returns the action that corresponds to a command.
      * 
      * @param command
      *        is a command that was entered by the player
      * @return the action the is mapped to the command
      */
     public Action getAction (Command command) {
         return myActions.get(command.getMethodName());
     }
 
     /**
      * returns all the actions this interactive entity is capable of doing
      * 
      */
     public Set<InformationCommand> getCommands () {
         Set<InformationCommand> infoCommands = new HashSet<InformationCommand>();
         if (myActions.isEmpty())
             return null; // this needs to be fixed
         for (String s : myActions.keySet()) {
             // need to check what type it is...eg it cant be a left click
             String actionType = s.split(" ")[0];
            if (!actionType.equals(ClickCommand.LEFT_CLICK ) && !actionType.equals(ClickCommand.RIGHT_CLICK)) { 
                 infoCommands.add(new InformationCommand(s, myInfos.get(s)));
             }
 
         }
         return infoCommands;
     }
 
     public void getOccupied (Unit occupier) {
         if (occupier.collidesWith(this)) {
             myOccupyStrategy.getOccupied(this, occupier);
         }
     }
 
     /**
      * This method specifies that the interactive entity is getting attacked so
      * it calls the attack method of the interactive entity on itself.
      * 
      * @param interactiveEntity
      *        is the interactive entity that is attacking this interactive
      *        entity
      */
     public void getAttacked (InteractiveEntity interactiveEntity) {
         interactiveEntity.attack(this);
     }
 
     /**
      * Returns the current attack strategy of the interactive
      * 
      * @return the current attack strategy
      */
     public AttackStrategy getAttackStrategy () {
         return myAttackStrategy;
     }
 
     /**
      * Returns the sound that the interactive entity makes.
      * 
      * @return the sound of the interactive entity
      */
 
     public Sound getSound () {
         return mySound;
     }
 
     /**
      * Returns the strategy the entity has for producing (CanProduce or
      * CannotProduce).
      * 
      * @return the production strategy of the entity
      */
     public ProductionStrategy getProductionStrategy () {
         return myProductionStrategy;
     }
 
     /**
      * Sets the production strategy of the entity to CanProduce or
      * CannotProduce.
      * 
      * @param productionStrategy
      *        is the production strategy the entity will have
      */
     public void setProductionStrategy (ProductionStrategy productionStrategy) {
         myProductionStrategy = productionStrategy;
     }
 
     /**
      * Sees whether the passed in InteractiveEntity is an enemy by checking if
      * player IDs do not match
      * 
      * @param InteractiveEntity
      *        other - the other InteractiveEntity to compare
      * @return whether the other InteractiveEntity is an enemy
      */
     public boolean isEnemy (InteractiveEntity other) {
         return getPlayerID() != other.getPlayerID();
     }
 
     @Override
     public void paint (Graphics2D pen) {
         if (!isVisible()) { return; }
         // pen.rotate(getVelocity().getAngle());
 
         // should probably use the getBottom, getHeight etc...implement them
         Point2D selectLocation = Camera.instance().worldToView(getWorldLocation());
 
         pen.drawRect((int) selectLocation.getX() - LOCATION_OFFSET,
                      (int) (selectLocation.getY() - 5 * LOCATION_OFFSET), 50, 5);
         Rectangle2D healthBar =
                 new Rectangle2D.Double((int) selectLocation.getX() - LOCATION_OFFSET,
                                        (int) (selectLocation.getY() - 5 * LOCATION_OFFSET),
                                        50 * getHealth() / getMaxHealth(), 5);
         float width = (float) (healthBar.getWidth() * (getHealth() / getMaxHealth()));
         pen.setPaint(new GradientPaint((float) healthBar.getX() - width, (float) healthBar
                 .getMaxY(), Color.RED, (float) healthBar.getMaxX(), (float) healthBar.getMaxY(),
                                        Color.GREEN));
         pen.fill(healthBar);
         pen.setColor(Color.black);
 
         if (isSelected) {
             Ellipse2D.Double selectedCircle =
                     new Ellipse2D.Double(selectLocation.getX() - LOCATION_OFFSET,
                                          selectLocation.getY() + LOCATION_OFFSET, 50, 30);
             pen.fill(selectedCircle);
         }
         super.paint(pen);
         if (myAttackStrategy.hasWeapon()) {
             for (Projectile p : myAttackStrategy.getCurrentWeapon().getProjectiles()) {
                 p.paint(pen);
             }
         }
     }
 
     public void put (String name, Action action) { // Might just use a putter
         myActions.put(name, action);
     }
 
     /**
      * If the passed in parameter is another InteractiveEntity, checks to see if
      * it is a Building and can be occupied, checks to see if it is an enemy,
      * and if so, switches to attack state. Defaults to move to the center of
      * the other InteractiveEntity
      * 
      * @param other
      *        - the other InteractiveEntity
      */
     public void recognize (InteractiveEntity other) {
         if (isEnemy(other)) {
             getEntityState().setUnitState(UnitState.ATTACK);
         }
         if (other instanceof Building) {
             getEntityState().setUnitState(UnitState.OCCUPY);
         }
 
         move(other.getWorldLocation());
     }
 
     // below are the recognize methods to handle different input parameters from
     // controller
     /**
      * If the passed in parameter is type Location3D, moves the
      * InteractiveEntity to that location
      * 
      * @param location
      *        - the location to move to
      */
     public void recognize (Location3D location) {
         move(location);
     }
 
     /***
      * Sets the isSelected boolean to the passed in bool value.
      */
     public boolean select (boolean selected) {
 
         if (selected && getState().canSelect()) {
             isSelected = selected;
         }
         if (!selected) {
             isSelected = selected;
         }
         return isSelected;
     }
 
     /**
      * Sets the attack strategy for an interactive. Can set the interactive to
      * CanAttack or to CannotAttack and then can specify how it would attack.
      * Also updates the weapons of the strategy to be at the same location of
      * this entity.
      * 
      * @param newStrategy
      *        is the new attack strategy that the interactive will have
      */
     public void setAttackStrategy (AttackStrategy newStrategy) {
         newStrategy.setWeaponLocation(getWorldLocation());
         myAttackStrategy = newStrategy;
     }
 
     /**
      * Sets the upgrade tree of the entity for a specific team based on an
      * upgrade tree and player ID that are passed in.
      * 
      * @param upgradeTree
      *        is the new upgrade tree that the entity will have
      * @param playerID
      *        is the team that the upgrade is for
      */
     public void setUpgradeTree (UpgradeTree upgradeTree, int playerID) {
         myUpgradeTree = upgradeTree;
     }
 
     @Override
     public void update (double elapsedTime) {
 
         if (myPath.size() == 0) {
             setVelocity(getVelocity().getAngle(), 0);
             getEntityState().stop();
         }
         else {
             super.move(myPath.getNext());
         }
 
         super.update(elapsedTime);
 
         Iterator<DelayedTask> it = myTasks.iterator();
         while (it.hasNext()) {
             DelayedTask dt = it.next();
             dt.update(elapsedTime);
             if (!dt.isActive()) {
                 it.remove();
             }
         }
         if (myAttackStrategy.hasWeapon()) {
             Weapon weapon = myAttackStrategy.getCurrentWeapon();
             List<InteractiveEntity> enemies =
                     GameState.getMap().<InteractiveEntity> getInArea(getWorldLocation(),
                                                                      weapon.getRange(), this,
                                                                      getPlayerID(), false);
             if (!enemies.isEmpty()) {
                 enemies.get(0).getAttacked(this);
             }
             weapon.update(elapsedTime);
         }
         getEntityState().update(elapsedTime);
 
         setChanged();
         notifyObservers();
     }
 
     /*
      * Test method to add an interactive entity to
      */
     public void addProducable (InteractiveEntity i) {
         myProducables.add(i);
     }
 
     @Override
     public void updateAction (Command command) {
         if (myActions.containsKey(command.getMethodName())) {
             Action action = myActions.get(command.getMethodName());
             action.update(command);
         }
     }
 
     /**
      * Sets the object to be in the changed state for the observer pattern.
      */
     public void setChanged () {
         super.setChanged();
     }
 
     /**
      * Gets the occupy strategy of the entity (either CanBeOccupied or
      * CannotBeOccupied).
      * 
      * @return
      */
     public OccupyStrategy getOccupyStrategy () {
         return myOccupyStrategy;
     }
 
     /**
      * Sets the occupy strategy for the entity to be CanBeOccupied or
      * CannotBeOccupied.
      * 
      * @param occupyStrategy
      *        is the occupy strategy that the entity is being set to
      */
     public void setOccupyStrategy (OccupyStrategy occupyStrategy) {
         myOccupyStrategy = occupyStrategy;
     }
 
     /**
      * Returns the time it takes to create the entity.
      * 
      * @return how long it takes to make the entity
      */
     public double getBuildTime () {
         return myBuildTime;
     }
 
     /**
      * Sets how long the build time is for the entity.
      * 
      * @param time
      *        is the amount of time it will take to create the entity
      */
     public void setBuildTime (double time) {
         myBuildTime = time;
     }
 
     @Override
     public void move (Location3D loc) {
         myPath = GameState.getMap().getPath(myFinder, getWorldLocation(), loc);
         super.move(myPath.getNext());
     }
 
     public void addWeapon (Weapon toAdd) {
         myAttackStrategy.addWeapon(toAdd);
 
     }
 
 }
