 package vooga.rts.gamedesign.sprite.gamesprites.interactive;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import vooga.rts.action.Action;
 import vooga.rts.action.IActOn;
 import vooga.rts.ai.AstarFinder;
 import vooga.rts.ai.Path;
 import vooga.rts.ai.PathFinder;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.InformationCommand;
 import vooga.rts.gamedesign.sprite.gamesprites.GameEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.IAttackable;
 import vooga.rts.gamedesign.sprite.gamesprites.Projectile;
 import vooga.rts.gamedesign.sprite.gamesprites.Resource;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
 import vooga.rts.gamedesign.state.UnitState;
 import vooga.rts.gamedesign.strategy.Strategy;
 import vooga.rts.gamedesign.strategy.attackstrategy.AttackStrategy;
 import vooga.rts.gamedesign.strategy.attackstrategy.CannotAttack;
 import vooga.rts.gamedesign.strategy.gatherstrategy.CanGather;
 import vooga.rts.gamedesign.strategy.gatherstrategy.CannotGather;
 import vooga.rts.gamedesign.strategy.gatherstrategy.GatherStrategy;
 import vooga.rts.gamedesign.strategy.occupystrategy.CannotBeOccupied;
 import vooga.rts.gamedesign.strategy.occupystrategy.OccupyStrategy;
 import vooga.rts.gamedesign.strategy.production.CannotProduce;
 import vooga.rts.gamedesign.strategy.production.ProductionStrategy;
 import vooga.rts.gamedesign.strategy.upgradestrategy.CannotUpgrade;
 import vooga.rts.gamedesign.strategy.upgradestrategy.UpgradeStrategy;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.gamedesign.weapon.Weapon;
 import vooga.rts.player.Team;
 import vooga.rts.state.GameState;
 import vooga.rts.util.Camera;
 import vooga.rts.util.DelayedTask;
 import vooga.rts.util.FilterImageColor;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 
 /**
  * This class is the extension of GameEntity. It contains the strategies for
  * attacking upgrading gathering and producing.
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
     public static final double DEFAULT_BUILD_TIME = 5;
     public static final int DEFAULT_ARMOR = 10;
     private boolean isSelected;
     private Sound mySound;
     private AttackStrategy myAttackStrategy;
     private ProductionStrategy myProductionStrategy;
     private UpgradeStrategy myUpgradeStrategy;
     private OccupyStrategy myOccupyStrategy;
     private GatherStrategy myGatherStrategy;
     private int myArmor;
     private Map<String, Action> myActions;
     private Map<String, Information> myActionInfos;
     private List<DelayedTask> myTasks;
     private double myBuildTime;
     private Information myInfo;
     private PathFinder myFinder;
     private Path myPath;
     private Queue<DelayedTask> myQueueableTasks;
     private DelayedTask myCurQueueTask;
     private GameEntity myTargetEntity;
     private Location3D myRallyPoint;
 
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
         mySound = sound;
         myAttackStrategy = new CannotAttack();
         myProductionStrategy = new CannotProduce();
         myUpgradeStrategy = new CannotUpgrade();
         myGatherStrategy = new CannotGather();
         myActions = new HashMap<String, Action>();
         myActionInfos = new HashMap<String, Information>();
         isSelected = false;
         myTasks = new ArrayList<DelayedTask>();
         myBuildTime = buildTime;
         myOccupyStrategy = new CannotBeOccupied();
         myPath = new Path();
         myQueueableTasks = new LinkedList<DelayedTask>();
         myCurQueueTask = new DelayedTask(0, null);
         myFinder = new AstarFinder();
         myTargetEntity = this;
         myArmor = DEFAULT_ARMOR;
         setSpeed(DEFAULT_INTERACTIVEENTITY_SPEED);
         myRallyPoint = new Location3D(0, 0, 0);
     }
 
     public void changeImageColor () {
         Image image = getImage().getImage();
         if (image != null) {
             setImage(FilterImageColor.colorImage(image, getPlayerID()));
         }
     }
 
     /**
      * Adds an action to myActions with the passed in command and Action
      */
     public void addAction (String command, Action action) {
         myActions.put(command, action);
     }
 
     /**
      * removes the action associated with the command
      * 
      */
     public void removeAction (String command) {
         myActions.remove(command);
     }
 
     /**
      * Returns the map of all actions of this interactive entity
      * 
      * @return Map of String to Action
      */
     public Map<String, Action> getActions () {
         return myActions;
     }
 
     /**
      * Sets actions to the given map of string to action
      * 
      * @param actions
      *        , the map to set myActions to
      */
     public void setActions (Map<String, Action> actions) {
         myActions = actions;
     }
 
     /**
      * Initialize default actions. will be overriden in subclasses
      */
     public abstract void addDefaultActions ();
 
     /**
      * Adds a new Delayedtask to the list of tasks
      * 
      * @param dt
      *        the delayed task to be added
      */
     public void addTask (DelayedTask dt) {
         myTasks.add(dt);
     }
 
     /**
      * Adds a new Queuabletask like upgrades and production to the linked list
      * of queueable tasks
      * 
      * @param dt
      *        the queueable task to be added
      */
     public void addQueueableTask (DelayedTask dt) {
         myQueueableTasks.add(dt);
     }
 
     /**
      * Sets the information class of the interactive entity to the passed in
      * information
      * 
      * @param info
      *        the Information to be set as the interactive entity's
      *        information
      */
     public void setInfo (Information info) {
         myInfo = info;
     }
 
     /**
      * Returns the information class of this interactive entity
      * 
      * @return the information class
      */
     public Information getInfo () {
         return myInfo;
     }
 
     @Override
     public void setPlayerID (int playerID) {
         this.changeImageColor();
         super.setPlayerID(playerID);
     }
 
     /**
      * Sets the upgradeTree to the passed in upgrade tree
      * 
      * @param upgradeTree
      *        the upgrade tree to be used
      */
     public void setUpgradeTree (UpgradeTree upgradeTree) {
         myUpgradeStrategy.setUpgradeTree(upgradeTree, this);
     }
 
     /**
      * Returns the upgrade tree associated with the interactive entity
      * 
      * @return the upgradeTree
      */
     public UpgradeTree getUpgradeTree () {
         return myUpgradeStrategy.getUpgradeTree();
     }
 
     /**
      * Returns all the strategies of this interactive entity
      * 
      * @return an array of all Strategies
      */
     public Strategy[] getStrategies () {
         Strategy[] all = new Strategy[5];
         all[0] = myAttackStrategy;
         all[1] = myOccupyStrategy;
         all[2] = myGatherStrategy;
         all[3] = myProductionStrategy;
         all[4] = myUpgradeStrategy;
         return all;
     }
 
     /**
      * adds passed in command and info into map
      * 
      * @param command
      * @param info
      */
     public void addActionInfo (String command, Information info) {
         myActionInfos.put(command, info);
     }
 
     public void removeActionInfo (String command) {
         myActionInfos.remove(command);
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
 
         double distance = distance(attackable);
         if (!this.isDead()) {
             if (!getEntityState().isAttacking()) {
                 if (attackInRange(attackable, distance)) {
                     getEntityState().stop();
                     this.stopMoving();
                 }
                 getEntityState().attack();
             }
             if (getEntityState().canAttack()) {
                if (getPlayerID() == 1) {
                    System.out.println(distance);
                    if (distance < 200) {
                        System.out.println("Attacking");
                    }
                }
                 myAttackStrategy.attack(attackable, distance);
             }
         }
     }
 
     /**
      * Checks to see if an entity is in attack state and comes in range of
      * another entity.
      * 
      * @param attackable
      *        is an enemy entity
      * @param distance
      *        is the distance an enemy entity is away
      * @return true if the entity should stop and attack the enemy and false if
      *         it should not
      */
     private boolean attackInRange (IAttackable attackable, double distance) {
         return getEntityState().inAttackMode() &&
                this.getAttackStrategy().getCurrentWeapon()
                        .inRange((InteractiveEntity) attackable, distance);
     }
 
     /**
      * Calculates the distance that an enemy is away from this entity.
      * 
      * @param attackable
      *        is an enemy entity
      * @return the distance that the enemy is away
      */
     private double distance (IAttackable attackable) {
         return Math.sqrt(Math.pow(getWorldLocation().getX() -
                                   ((InteractiveEntity) attackable).getWorldLocation().getX(), 2) +
                          Math.pow(getWorldLocation().getY() -
                                   ((InteractiveEntity) attackable).getWorldLocation().getY(), 2));
     }
 
     public void calculateDamage (int damage) {
         int healthLost = damage * (1 - (myArmor / (myArmor + 100)));
         changeHealth(healthLost);
     }
 
     /**
      * checks to see if the passed in command is a valid input for an action in
      * this interactive entity
      * 
      * @param command
      */
     public boolean containsInput (Command command) {
         return myActions.containsKey(command.getMethodName());
     }
 
     /**
      * Creates a copy of an interactive entity.
      **/
     public abstract InteractiveEntity copy ();
 
     /**
      * Gives "toOther" all the information and strategies attributed to this
      * class.
      * 
      * @param toOther
      */
     public void transmitProperties (InteractiveEntity toOther) {
         toOther.setInfo(getInfo());
         for (Strategy s : getStrategies()) {
             s.copyStrategy(toOther);
         }
     }
 
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
             String isMake = s.split(" ")[0];
             if (isMake.equals("make")) { // very buggy
                 infoCommands.add(new InformationCommand(s, myActionInfos.get(s)));
             }
             else
                 if (isMake.equals("upgrade")) {
                     infoCommands.add(new InformationCommand(s, myActionInfos.get(s)));
                 }
                 else
                     if (isMake.equals("deoccupy")) {
                         infoCommands.add(new InformationCommand(s, myActionInfos.get(s)));
                     }
 
         }
         if (infoCommands.isEmpty()) {
             return null;
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
      * Returns the upgrade strategy of this interactive entity
      * 
      * @return the upgrade strategy of the entity
      */
     public UpgradeStrategy getUpgradeStrategy () {
         return myUpgradeStrategy;
     }
 
     /**
      * Sets the amount that the worker can gather at a time.
      * 
      * @param gatherAmount
      *        is the amount that the worker can gather
      */
     public void setGatherAmount (int gatherAmount) {
         myGatherStrategy.setGatherAmount(gatherAmount);
         myGatherStrategy = new CanGather(CanGather.DEFAULTCOOL, myGatherStrategy.getGatherAmount());
     }
 
     /**
      * The worker gathers the resource if it can and then resets its gather
      * cooldown.
      * 
      * @param gatherable
      *        is the resource being gathered.
      */
     public void gather (IGatherable gatherable) {
         if (this.collidesWith((GameEntity) gatherable)) {
             myGatherStrategy.gatherResource(getPlayerID(), gatherable);
         }
     }
 
     /**
      * Sets the gather strategy of this entity to the passed in gatherStrategy
      * 
      * @param gatherStrategy
      */
     public void setGatherStrategy (GatherStrategy gatherStrategy) {
         myGatherStrategy = gatherStrategy;
     }
 
     /**
      * Returns the gather strategy of this entity
      */
     public GatherStrategy getGatherStrategy () {
         return myGatherStrategy;
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
         if (!isVisible()) {
             return;
         }
         Point2D selectLocation = Camera.instance().worldToView(getWorldLocation());
         paintHealthBar(pen, selectLocation);
         if (isSelected) {
             Ellipse2D.Double selectedCircle =
                     new Ellipse2D.Double(selectLocation.getX() - LOCATION_OFFSET,
                                          selectLocation.getY() + LOCATION_OFFSET, 50, 30);
             pen.fill(selectedCircle);
         }
         super.paint(pen);
         paintProjectiles(pen);
     }
 
     private void paintProjectiles (Graphics2D pen) {
         if (myAttackStrategy.hasWeapon()) {
             for (Projectile p : myAttackStrategy.getCurrentWeapon().getProjectiles()) {
                 p.paint(pen);
             }
         }
     }
 
     private void paintHealthBar (Graphics2D pen, Point2D selectLocation) {
         pen.drawRect((int) selectLocation.getX() - LOCATION_OFFSET,
                      (int) (selectLocation.getY() - 5 * LOCATION_OFFSET), 50, 5);
         Rectangle2D healthBar =
                 new Rectangle2D.Double((int) selectLocation.getX() - LOCATION_OFFSET,
                                        (int) (selectLocation.getY() - 5 * LOCATION_OFFSET),
                                        50 * getHealth() / getMaxHealth(), 5);
         float width = calculateHealthBarWidth(healthBar);
         pen.setPaint(new GradientPaint((float) healthBar.getX() - width, (float) healthBar
                 .getMaxY(), Color.RED, (float) healthBar.getMaxX(), (float) healthBar.getMaxY(),
                                        Color.GREEN));
         pen.fill(healthBar);
         pen.setColor(Color.black);
     }
 
     private float calculateHealthBarGradient (Rectangle2D healthBar) {
         float width = (float) (healthBar.getWidth() * (getHealth() / getMaxHealth()));
         return width;
     }
 
     private float calculateHealthBarWidth (Rectangle2D healthBar) {
         float width = (float) (healthBar.getWidth() * (getHealth() / getMaxHealth()));
         return width;
     }
 
     /**
      * If the passed in parameter is another GameEntity, checks to see if it is
      * a Building and can be occupied, checks to see if it is an enemy, and if
      * so, switches to attack state. Defaults to move to the center of the other
      * GameEntity
      * 
      * @param other
      *        - the other GameEntity
      */
     public void recognize (GameEntity other) {
         myTargetEntity = other;
         if (other instanceof Resource) {
             getEntityState().setUnitState(UnitState.GATHER);
         }
         if (isEnemy((InteractiveEntity) other)) {
             getEntityState().setUnitState(UnitState.ATTACK);
         }
         else
             if (other instanceof Building) {
                 getEntityState().setUnitState(UnitState.OCCUPY);
             }
             else {
                 getEntityState().setUnitState(UnitState.NO_STATE);
             }
         move(other.getWorldLocation());
     }
 
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
 
         if (selected && getEntityState().canSelect()) {
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
 
     @Override
     public void update (double elapsedTime) {
         updatePath();
 
         super.update(elapsedTime);
 
         updateTasks(elapsedTime);
         updateQueueableTasks(elapsedTime);
         updateAttack(elapsedTime);
         if (this instanceof Unit) {
             updateOccupy(elapsedTime);
         }
         getEntityState().update(elapsedTime);
         getGatherStrategy().update(elapsedTime);
         setChanged();
         notifyObservers();
     }
 
     private void updateOccupy (double elapsedTime) {
         List<InteractiveEntity> shelters = findShelters();
         if (!shelters.isEmpty()) {
             shelters.get(0).getOccupied((Unit) this);
         }
     }
 
     private List<InteractiveEntity> findShelters () {
         List<InteractiveEntity> possibleShelters =
                 GameState.getMap().<InteractiveEntity> getInArea(getWorldLocation(),
                                                                  10,
                                                                  InteractiveEntity.class,
                                                                  GameState.getPlayers()
                                                                          .getTeamID(getPlayerID()),
                                                                  true);
         return possibleShelters;
     }
 
     private void updateAttack (double elapsedTime) {
         if (myAttackStrategy.hasWeapon()) {
             Weapon weapon = myAttackStrategy.getCurrentWeapon();
             if (getEntityState().inAttackMode()) {
                 ((InteractiveEntity) myTargetEntity).getAttacked(this);
                 removeState();
             }
             else {
                 List<InteractiveEntity> enemies = findEnemies(weapon);
                 if (!enemies.isEmpty()) {
                     enemies.get(0).getAttacked(this);
                 }
 
                 weapon.update(elapsedTime);
             }
         }
     }
 
     private void updateQueueableTasks (double elapsedTime) {
         if (myCurQueueTask != null) {
 
             if (!myCurQueueTask.isActive() && myQueueableTasks.peek() != null) {
                 myCurQueueTask = myQueueableTasks.poll();
             }
 
             myCurQueueTask.update(elapsedTime);
 
         }
     }
 
     private void updateTasks (double elapsedTime) {
         Iterator<DelayedTask> it = myTasks.iterator();
         while (it.hasNext()) {
             DelayedTask dt = it.next();
             dt.update(elapsedTime);
             if (!dt.isActive()) {
                 it.remove();
             }
         }
     }
 
     private void updatePath () {
         if (myPath != null) {
             if (myPath.size() == 0) {
                 setVelocity(getVelocity().getAngle(), 0);
                 getEntityState().stop();
             }
             else {
                 super.move(myPath.getNext());
             }
         }
     }
 
     /**
      * If the target entity is dead then the unit is set to have no state. For
      * example if the entity that is being attacked dies, then this entity
      * switches out of targeting attack mode so that it can find and attack
      * other units.
      */
     public void removeState () {
         if (myTargetEntity.isDead()) {
             getEntityState().setUnitState(UnitState.NO_STATE);
         }
     }
 
     /**
      * Finds a list of enemies that are in range of the entity.
      * 
      * @param weapon
      *        is the weapon that the entity has
      * @return a list of enemies
      */
     private List<InteractiveEntity> findEnemies (Weapon weapon) {
         List<InteractiveEntity> enemies =
                 GameState.getMap().<InteractiveEntity> getInArea(getWorldLocation(),
                                                                  weapon.getRange(),
                                                                  InteractiveEntity.class,
                                                                  GameState.getPlayers()
                                                                          .getTeamID(getPlayerID()),
                                                                  false);
         return enemies;
     }
 
     /*
      * Test method to add an interactive entity to
      */
     public void addProducable (InteractiveEntity producable) {
         myProductionStrategy.addProducable(producable);
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
         final Location3D temp = new Location3D(loc);
         myPath = null;
         findpath(temp);
     }
 
     private void findpath (Location3D destination) {
         myPath = GameState.getMap().getPath(myFinder, getWorldLocation(), destination);
         if (myPath != null) {
             setRallyPoint();
         }
     }
 
     /**
      * Adds a weapon to the list of weapons in this entity
      * 
      * @param toAdd
      *        the weapon to be added
      */
     public void addWeapon (Weapon toAdd) {
         toAdd.setCenter(getWorldLocation());
         myAttackStrategy.addWeapon(toAdd);
     }
 
     /**
      * Sets the upgrade strategy to the passed in strategy
      */
     public void setUpgradeStrategy (UpgradeStrategy upgradeStrategy) {
         myUpgradeStrategy = upgradeStrategy;
     }
 
     /**
      * Returns the target entity of this entity. In other words, if an entity is
      * right clicked on, that entity becomes the target entity which is returned
      * from this method.
      * 
      * @return the target interactive entity
      */
     public GameEntity getTargetEntity () {
         return myTargetEntity;
     }
 
     /**
      * Sets the target entity that this entity should act on.
      * 
      * @param entity
      *        is the target entity
      */
     public void setTargetEntity (GameEntity entity) {
         myTargetEntity = entity;
     }
 
     /**
      * Sets the armor of the entity.
      * 
      * @param armor
      *        is the amount of armor the entity will have
      */
     public void setArmor (int armor) {
         myArmor = armor;
     }
 
     /**
      * Returns the rally point of the production building. Will be used to move
      * the newly created units to
      * 
      * @return myRallyPoint, the rally point of the production building
      */
     public Location3D getRallyPoint () {
         return myRallyPoint;
     }
 
     /**
      * Sets the rally point of the production building
      * 
      * @param rallyPoint
      *        the location of the new rally point
      */
     public void setRallyPoint (Location3D rallyPoint) {
         myRallyPoint = rallyPoint;
     }
 
     /**
      * Sets the rally point of the entity that can produce so that units will
      * move to that point after they are created by the entity.
      * 
      * @param rallyPoint
      *        is the location where the units will go when they are created
      */
 
     public void setRallyPoint () {
         myRallyPoint =
                 new Location3D(getWorldLocation().getX(), getWorldLocation().getY() + getHeight(),
                                0);
     }
 
 }
