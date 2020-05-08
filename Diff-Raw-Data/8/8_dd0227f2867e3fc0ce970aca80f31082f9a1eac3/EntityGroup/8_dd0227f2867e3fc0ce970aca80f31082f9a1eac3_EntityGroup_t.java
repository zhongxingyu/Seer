 package spaceinvaders.entity;
 
 import org.newdawn.slick.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import spaceinvaders.script.SpaceInvaders;
 
 /**
  * Entity Data Storage<br/>
  * Class holds a global list and HashMap of entities<br/><br/>
  * 
  * It is recommended, but not at all required, that entity data be loaded from a separate data file such as JSON.<br/>
  * Each game can only have one group of entities. This data is accessible by all classes.<br/>
  * The data sets are private and there is intentionally no way to add to them. Data is loaded once in the beginning only and cannot be changed later.
  * @author Brian Yang
  */
 
 public final class EntityGroup {
     
     /** The main player with data loaded from a JSON save file */
     public static Player player;
     
     /** Lists all the Enemies */
     private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
     /** Lists all the Weapons */
     private static ArrayList<Weapon> weapons = new ArrayList<Weapon>();
     /** Lists all the Misc entities */
     private static ArrayList<Misc> misc = new ArrayList<Misc>();
     /** Lists all the Immovable entities */
     private static ArrayList<Immovable> immovable = new ArrayList<Immovable>();
     
     /** Maps all entity names to their respective entity object */
     private static final HashMap<String, Entity> entityData = new HashMap<String, Entity>();
     
     /** Lists all active entities */
     public static final ArrayList<Entity> activeList = new ArrayList<Entity>();
     /** Maps active IDs to the active entity */
     public static final HashMap<String, Entity> active = new HashMap<String, Entity>();
     /** Lists all active unscripted bullets */
     public static final ArrayList<Weapon> bullets = new ArrayList<Weapon>();
     
     /** Sprite sheet for Entities */
     public static final XMLPackedSheet sprites; 
     static {
         XMLPackedSheet tempSprites = null; // required or compiler will complain that sprites isn't initialized
         try {
             tempSprites = new XMLPackedSheet("src/spaceinvaders/entity/sprites/sprites.png", "src/spaceinvaders/entity/sprites/sprites.xml");
         } catch (SlickException e) {
             System.out.println("Sprite sheet failure.");
         }
         sprites = tempSprites;
     }
     
     /** The graphics engine provided by Slick2D */
     private static Graphics g;
     
     /** 
      * The ArrayLists are only assigned values after the constructor is run, therefore we can't create the maps here.<br/>
      * The names are mapped to IDs the first time it is needed.
      */
     private static boolean mapCreated = false;
     
     /** The player ship sprite */
     private static Image ship;
     
     /**
      * Constructs a new set of entities using a data file<br/>
      * Should be called via a separate data file such as JSON
      */
     public EntityGroup() {}
     
      /**
      * Constructs a new set of entities<br/>
      * Calling this constructor normally is not recommended. Use a data file to load the data instead.<br/>
      * Please note that the fields in EntityGroup are static and therefore this constructor will replace everything.
      * @param enemies ArrayList of enemy entities
      * @param weapons ArrayList of weapon entities
      * @param misc ArrayList of misc entities
      */
     public EntityGroup(ArrayList<Enemy> enemies, ArrayList<Weapon> weapons, ArrayList<Misc> misc, ArrayList<Immovable> immovable) {
         EntityGroup.enemies = enemies;
         EntityGroup.weapons = weapons;
         EntityGroup.misc = misc;
         EntityGroup.immovable = immovable;
     }
     
     /*
      * Start Spawning Entities
      * The below methods are used to spawn entities based on the loaded data
      */
     
     /**
      * Construct HashMaps for each type of entity<br/>
      * We don't want to have to refer to each entity by its ID/index number
      */
     private static void createMap() {
         for(Enemy e : enemies) {
             entityData.put(e.getName(), e);
         }
         
         for (Weapon w : weapons) {
             entityData.put(w.getName(), w);
         }
         
         for (Misc m : misc) {
             entityData.put(m.getName(), m);
         }
         
         for (Immovable i : immovable) {
             entityData.put(i.getName(), i);
         }
         
         //System.out.println(EntityGroup.getBaseEntity("Rainbow Laser").getName());
         entityData.put(player.getName(), player);
 
         mapCreated = true;
     }
     
     /**
      * The graphics engine<br/>
      * Passed by the render method<br/>
      * To use the graphics engine, simply call <code>EntityGroup.getGraphics()</code>
      */
     public static void receiveGraphics(Graphics g) {
         
         EntityGroup.g = g;
     }
     
     /**
      * Get the Graphics Engine
      * @return graphics engine
      */
     public static Graphics getGraphics() {
         return g;
     }
     
     /**
      * Get Player
      * @return the player
      */
     public static Player getPlayer() {
         return player;
     }
     
     /**
      * Get Entity by Name
      * @param name name of Entity
      * @return the requested Entity
      */
     public static Entity getBaseEntity(String name) {
         if(!mapCreated) createMap();
         if(entityData.containsKey(name)) {
             return entityData.get(name);
         } else {
             System.out.println("WARNING - No Entity by that name exists! Returning default entity.");
             return new Entity();
         }
     }
    
     /**
      * Get Image from Sprite
      * @return the Entity's image
      */
     public static Image getImage(String ref) {
         return sprites.getSprite(ref);
     }
     
     /**
      * Spawn the Entity at a random location with default velocity<br/>
      * EntitySpawnExceptions from <code>spawn(name, v)</code> will retry spawning with this method
      * @param name the entity to spawn
      * @param id the id reference of entity
      * @return the Entity of declared type that just got spawned
      */
     public static <EntityT extends Entity> EntityT spawn(String name, String id) {
         
         Entity e = getBaseEntity(name);
         Entity en; // the cloned entity, to be casted into EntityT type
         float[] coordinates; // random coordinates
         
         if(e instanceof Enemy) { // the entity is actaully an enemy
 
             en = cloneEnemy(e);
 
         } else if(e instanceof Misc) { // the entity is actually a misc entity
 
             en = cloneMisc(e);
 
         } else if(e instanceof Weapon) { // the entity is actually a weapon
 
             en = cloneWeapon(e);
 
         } else if(e instanceof Immovable) { // the entity is actually an immovable
 
             en = cloneImmovable(e);
 
         } else if(e instanceof Player) { // the entity is actually the player
 
             en = clonePlayer(e);
             
         } else {
             
             System.out.println("All else failed, so spawning a basic entity at a random location instead.");
             en = cloneEntity(e);
             
         }
         
         en.setId(id);
         coordinates = selectRandom();
         en.place(coordinates[0], coordinates[1]);
         activeList.add((EntityT)en);
         active.put(en.getId(), (EntityT)en);
         return (EntityT)en;
     }
     
     /**
      * Spawn the Entity at a random location with a specific velocity
      * @param name the movable entity to spawn
      * @param id the id reference of entity
      * @param vx x velocity in mph
      * @param vy y velocity in mph
      * @return the Entity of declared type that just got spawned
      */
     public static <EntityT extends MovableEntity> EntityT spawn(String name, String id, double v) {
         
         Entity e = getBaseEntity(name);
         MovableEntity en; // the cloned entity, to be casted into EntityT type
         float[] coordinates; // random coordinates
         
         try {
             
             if(!(e instanceof MovableEntity))
                 throw new EntitySpawnException("You can't give a non-movable entity velocity!");
             
             if(e instanceof Enemy) { // the entity is actaully an enemy
                 
                 en = cloneEnemy(e);
                 
             } else if(e instanceof Misc) { // the entity is actually a misc entity
                 
                 en = cloneMisc(e);
                 
             } else if(e instanceof Weapon) { // the entity is actually a weapon
                 
                 en = cloneWeapon(e);
                 
             } else if(e instanceof Player) { // the entity is actually the player
                 
                 en = clonePlayer(e);
                         
             } else {
                 
                 throw new EntitySpawnException("caught EntitySpawnException: What kind of entity are you exactly? Attempting to spawn again without velocity.");
                         
             }
             
             en.setId(id);
             en.setVelocity(v);
             coordinates = selectRandom();
             en.place(coordinates[0], coordinates[1]);
             activeList.add((EntityT)en);
             active.put(en.getId(), (EntityT)en);
             return (EntityT)en;
             
         } catch(EntitySpawnException ex) {
             
             System.out.println(ex.getMessage());
             System.out.println("Ignoring your provided velocities.");
             return spawn(name, id);
             
         }   
     }
     
     /**
      * Spawn the entity at a specific location with default velocity<br/>
      * EntitySpawnExceptions from <code>spawn(name, x, y, v)</code> will retry using this method
      * @param name the entity to spawn
      * @param id the id reference of entity
      * @param x x coordinate
      * @param y y coordinate
      * @return the Entity of declared type that just got spawned
      */
     public static <EntityT extends Entity> EntityT spawn(String name, String id, float x, float y) {
         
         Entity e = getBaseEntity(name);
         Entity en; // the cloned entity, to be casted into EntityT type
         
         if(e instanceof Enemy) { // the entity is actaully an enemy
 
             en = cloneEnemy(e);
 
         } else if(e instanceof Misc) { // the entity is actually a misc entity
 
             en = cloneMisc(e);
 
         } else if(e instanceof Weapon) { // the entity is actually a weapon
 
             en = cloneWeapon(e);
 
         } else if(e instanceof Immovable) { // the entity is actually an immovable
 
             en = cloneImmovable(e);
 
         } else if(e instanceof Player) { // the entity is actually the player
 
             en = clonePlayer(e);
             
         } else {
             
             System.out.println("All else failed, so spawning a basic entity at your specified location instead.");
             en = cloneEntity(e);
             
         }
         
         en.setId(id);
         en.place(x, y);
         activeList.add((EntityT)en);
         active.put(en.getId(), (EntityT)en);
         return (EntityT)en;
     }
     
     /**
      * Spawn the entity at a specific location with specific velocity
      * @param name the movable entity to spawn
      * @param id the id reference of entity
      * @param x x coordinate
      * @param y y coordinate
      * @param vx x velocity in mph
      * @param vy y velocity in mph
      * @return the Entity of declared type that just got spawned
      */
     public static <EntityT extends MovableEntity> EntityT spawn(String name, String id, float x, float y, double v) {
         
         Entity e = getBaseEntity(name);
         MovableEntity en; // the cloned entity, to be casted into type EntityT
         try {
             
             if(!(e instanceof MovableEntity))
                 throw new EntitySpawnException("You can't give a non-movable entity velocity!");
             
             if(e instanceof Enemy) { // the entity is actaully an enemy
                 
                 en = cloneEnemy(e);
                 
             } else if(e instanceof Misc) { // the entity is actually a misc entity
                 
                 en = cloneMisc(e);
                 
             } else if(e instanceof Weapon) { // the entity is actually a weapon
                 
                 en = cloneWeapon(e);
                 
             } else if(e instanceof Player) { // the entity is actually the player
                 
                 en = clonePlayer(e);
                         
             } else {
                 
                 throw new EntitySpawnException("caught EntitySpawnException: What kind of entity are you exactly? Attempting to spawn again without velocity.");
                         
             }
             
             en.setId(id);
             en.setVelocity(v);
             en.place(x, y);
             activeList.add((EntityT)en);
             active.put(en.getId(), (EntityT)en);
             return (EntityT)en;
             
         } catch(EntitySpawnException ex) {
             
             System.out.println(ex.getMessage());
             System.out.println("Ignoring your provided velocities.");
             return spawn(name, id, x, y);
             
         }   
     }
     
     /**
      * Clone Basic Entity
      * @param e the Entity reference
      * @return the cloned Entity
      */
     private static Entity cloneEntity(Entity e) {
         Entity ent = e;
         ent = new Entity(ent.getName(), ent.getId(), ent.getImage(), ent.getDescription());
         return ent;
     }
     
     /**
      * Clone Enemy
      * @param e the Entity reference
      * @return the cloned Enemy
      */
     private static Enemy cloneEnemy(Entity e) {
         Enemy en = (Enemy)e;
         en = new Enemy(en.getName(), en.getId(), en.getImage(), en.getMainScriptID(), en.getDescription(), en.getAttack(), en.getDefense(), en.getHp(), en.getWeaponName(), en.getVelocity());
         return en;
     }
     
     /**
      * Clone Misc Entity
      * @param e the Entity reference
      */
     private static Misc cloneMisc(Entity e) {
         Misc mi = (Misc)e;
         mi = new Misc(mi.getName(), mi.getId(), mi.getImage(), mi.getDescription(), mi.getVelocity());
         return mi;
     }
     
     /**
      * Clone Weapon
      * @param e the Entity reference
      * @return the cloned Weapon
      */
     private static Weapon cloneWeapon(Entity e) {
         Weapon we = (Weapon)e;
         we = new Weapon(we.getName(), we.getId(), we.getImage(), we.getDescription(), we.getAttack(), we.getVelocity());
         return we;
     }
     
     /**
      * Clone Immovable Entity
      * @param e the Entity reference
      * @return the cloned Immovable
      */
     private static Immovable cloneImmovable(Entity e) {
         Immovable im = (Immovable)e;
         im = new Immovable(im.getName(), im.getId(), im.getImage(), im.getDescription(), im.getAttack(), im.getDefense(), im.getHp(), im.getWeaponName());
         return im;
     }
     
     /**
      * Clone Player
      * @param e the Entity reference
      * @return the cloned Player
      */
     private static Player clonePlayer(Entity e) {
         Player pl = (Player)e;
         // There is only one player. We don't want to copy it. One instance only!
         pl = new Player(pl.getName(), pl.getId(), pl.getImage(), pl.getDescription(), pl.getAttack(), pl.getDefense(), pl.getHp(), pl.getWeaponName(), pl.getVelocity());
         return pl;
     }
     
     /**
      * Select a random coordinate on the map
      * @return an array of two floats representing the random x and y coordinates chosen
      */
     public static float[] selectRandom() {
         float[] coordinates = {(float)(Math.random() * ((SpaceInvaders.X_RESOLUTION) + 1)), (float)(Math.random() * ((SpaceInvaders.Y_RESOLUTION) + 1))};
         return coordinates;
     }
     
     /**
      * Draw all spawned entities at their provided x and y
      */
     public static void draw() {
         for (Entity e : activeList)
             e.getSprite().draw(e.getX(), e.getY());
     }
     
     /*
      * Start Controlling Active Entities
      * The below methods control entities that have spawned and are currently active
      */
     
     /**
      * Get a spawned entity
      * @param id id tag of the entity 
      * @pre the id exists and refers to a spawned entity
      * @throws EntitySpawnException if id does not exist, will spawn an asteroid, assign the provided id to it, and return it
      * @return the spawned entity of declared type
      */
     public static <EntityT extends Entity> EntityT getEntity(String id) {
         try {
             if(active.containsKey(id)) {
                 return (EntityT)active.get(id);
             } else {
                 throw new EntitySpawnException("Entity is not spawned. Spawning an asteroid then retrying.");
             }
         } catch(EntitySpawnException ex) {
             System.out.println(ex.getMessage());
             spawn("Asteroid", id);
             // Try again. Since the id has definitely spawned at this point, it should not become an infinite loop
             return getEntity(id);
         }
     }
     
     /**
      * Remove a spawned entity<br/>
      * The entity has died, moved off screen, or removed in some way<br/>
      * If id does not exist or does not refer to a spawned entity, do nothing<br/>
      * Also marks its scripting thread for deletion
      * @param id tag of entity
      * @param scripted is it scripted?
      */
     public static void remove(String id, boolean scripted) throws SlickException {
         if(scripted) {
             if(active.containsKey(id)) {
                 active.get(id).getMainThread().markForDeletion();
                 //active.get(id).getSprite().destroy();
                 activeList.remove(getEntity(id));
                 active.remove(id);
                 
             }
         } else {
             if(active.containsKey(id)) {
                 //active.get(id).getSprite().destroy();
                 activeList.remove(getEntity(id));
                 active.remove(id);
                 
             }
         }
     }
     
     /**
      * Remove a spawned entity by reference<br/>
      * The entity has died, moved off screen, or removed in some way<br/>
      * @param entity the entity to remove
      * @param scripted is it scripted?
      */
     public static void remove(Entity entity, boolean scripted) throws SlickException {
         if(scripted) {
             if(active.containsValue(entity)) {
                 String id = entity.getId();
                 active.get(id).getMainThread().markForDeletion();
                 //active.get(id).getSprite().destroy();
                 active.remove(id);
                 activeList.remove(entity);
             }
         } else {
             if(active.containsValue(entity)) {
                 String id = entity.getId();
                 //active.get(id).getSprite().destroy();
                 active.remove(id);
                 activeList.remove(entity);
                System.out.println(activeList.size());
             }
         }
     }
     
     /**
      * Spawn Count
      * @return the number of entities spawned
      */
     public static int spawnCount() {
         return active.size();
     }
     
     /**
      * Moves the player
      */
     public static void control(GameContainer gc, int delta) throws SlickException {
         Input input = gc.getInput();
         if(ship == null)
             ship = SpaceInvaders.player.getSprite();
  
         /* rotate to the left */
         if(input.isKeyDown(Input.KEY_LEFT)) {
             ship.rotate(-MovableEntity.ROTATION_SIZE * delta);
             player.rotate(-MovableEntity.ROTATION_SIZE * delta);
         }
  
         /* rotate to the right */
         if(input.isKeyDown(Input.KEY_RIGHT)) {
             ship.rotate(MovableEntity.ROTATION_SIZE * delta);
             player.rotate(MovableEntity.ROTATION_SIZE * delta);
         }
  
         /* move forward in current direction */
         if(input.isKeyDown(Input.KEY_UP)) {
             /* size for one single step */
             float step = MovableEntity.STEP_SIZE * delta;
  
             /* which direction are we facing? */
             float rotation = ship.getRotation();
             
             /* move the SpaceInvaders.player */
             if(SpaceInvaders.player.getX() + step * Math.sin(Math.toRadians(rotation)) > MovableEntity.ORIGIN - MovableEntity.EDGE_FACTOR 
                     && SpaceInvaders.player.getX() + step * Math.sin(Math.toRadians(rotation)) < SpaceInvaders.X_RESOLUTION - MovableEntity.EDGE_FACTOR
                     && SpaceInvaders.player.getY() - step * Math.cos(Math.toRadians(rotation)) > MovableEntity.ORIGIN - MovableEntity.EDGE_FACTOR 
                     && SpaceInvaders.player.getY() - step * Math.cos(Math.toRadians(rotation)) < SpaceInvaders.Y_RESOLUTION - MovableEntity.EDGE_FACTOR) {
                 SpaceInvaders.player.move(step * Math.sin(Math.toRadians(rotation)), -step * Math.cos(Math.toRadians(rotation)));
             }
         }
         
         /* back up a bit */
         if(input.isKeyDown(Input.KEY_DOWN)) {
             /* size for one single back step */
             float step = MovableEntity.BACK_SIZE * delta;
  
             /* which direction are we facing? */
             float rotation = ship.getRotation() * -1;
  
             /* move the SpaceInvaders.player */
             if(SpaceInvaders.player.getX() + step * Math.sin(Math.toRadians(rotation)) > MovableEntity.ORIGIN - MovableEntity.EDGE_FACTOR 
                     && SpaceInvaders.player.getX() + step * Math.sin(Math.toRadians(rotation)) < SpaceInvaders.X_RESOLUTION - MovableEntity.EDGE_FACTOR
                     && SpaceInvaders.player.getY() - step * Math.cos(Math.toRadians(rotation)) > MovableEntity.ORIGIN - MovableEntity.EDGE_FACTOR 
                     && SpaceInvaders.player.getY() - step * Math.cos(Math.toRadians(rotation)) < SpaceInvaders.Y_RESOLUTION - MovableEntity.EDGE_FACTOR) 
                 SpaceInvaders.player.move(step * Math.sin(Math.toRadians(rotation)), step * Math.cos(Math.toRadians(rotation)));
         }
         
         /* Temporary HP Deduction Test */
         if(input.isKeyDown(Input.KEY_H)) {
             SpaceInvaders.player.deductHp(10);
         }
         
         /* Temporary Rotation Test */
         if(input.isKeyDown(Input.KEY_SPACE)) {
             
             SpaceInvaders.player.fire();
         }
     }
     
     /*
      * Misc. Methods
      */
     
     /**
      * Enemy Count
      * @return the number of enemy entities
      */
     public static int getEnemyCount() {
         return enemies.size();
     }
     
     /**
      * Weapon Count
      * @return the number of weapon entities
      */
     public static int getWeaponCount() {
         return weapons.size();
     }
     
     /**
      * Misc Count
      * @return the number of misc entities
      */
     public static int getMiscCount() {
         return misc.size();
     }
     
     /**
      * Misc Count
      * @return the number of misc entities
      */
     public static int getImmovableCount() {
         return immovable.size();
     }
 }
