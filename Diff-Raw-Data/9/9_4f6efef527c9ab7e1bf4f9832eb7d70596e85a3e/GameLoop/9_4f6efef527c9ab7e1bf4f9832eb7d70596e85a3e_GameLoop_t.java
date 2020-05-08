 package Project.Game;
 
 import Project.Game.Entities.Entity;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * User: Piers
  * Date: 23/10/12
  * Time: 11:17
  */
 public class GameLoop implements Runnable {
 
     private ArrayList<Entity> entities;
     public final Object _entities = new Object();
 
     private ArrayList<Faction> factions;
     private final Object _factions = new Object();
 
     private ArrayList<Entity> addEntities;
 
     private int tickDelay;
     private boolean paused;
 
     public GameLoop() {
         entities = new ArrayList<>();
         addEntities = new ArrayList<>();
         factions = new ArrayList<>();
         tickDelay = 50;
         paused = true;
     }
 
     public GameLoop(int tickDelay) {
         this();
         this.tickDelay = tickDelay;
     }
 
     @Override
     public void run() {
         while (true) {
             try {
                 Thread.sleep(tickDelay);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             if (!paused) {
                 // Run the removal run
                 synchronized (_entities) {
                     Iterator<Entity> iterator = entities.iterator();
                     while (iterator.hasNext()) {
                         Entity entity = iterator.next();
                         if (!entity.isAlive()) {
                             iterator.remove();
                         }
                     }
                 }
                 // Addition run
                 synchronized (_entities) {
                     entities.addAll(addEntities);
                     addEntities.clear();
                 }
 
                 // Update run
                 synchronized (_entities) {
                     for (Entity entity : entities) entity.update();
                 }
 
                 // Faction update
                 synchronized (_factions) {
                     for (Faction faction : factions) faction.update();
                 }
             }
         }
     }
 
     public void addEntity(Entity entity) {
         addEntities.add(entity);
     }
 
 
     // Not thread safe - only add factions when thread paused
     public void addFaction(Faction faction) {
         factions.add(faction);
     }
 
     public void setPaused(boolean paused) {
         this.paused = paused;
     }
 
     public ArrayList<Entity> getEntities() {
         return entities;
     }
 
     /**
      * Gets entities that are within a certain radius to the given location
      *
      * @param location location of the search
      * @param radius   radius to search in
      * @return List of entities
      */
     public ArrayList<Entity> getEntities(Vector2D location, int radius) {
         ArrayList<Entity> closeEntities = new ArrayList<>();
         synchronized (_entities) {
             for (Entity entity : entities) {
                 if (entity.isAlive()) {
                     if (entity.getMovementBehaviour().getLocation().dist(location) < radius) {
                         closeEntities.add(entity);
                     }
                 }
             }
         }
 
         return closeEntities;
     }
 
     public ArrayList<Entity> getEntities(Vector2D location, int radius, Faction faction) {
         ArrayList<Entity> closeEntities = new ArrayList<>();
         synchronized (_entities) {
             for (Entity entity : entities) {
                if (entity.isAlive() && entity.getFaction() != faction) {
                     if (entity.getMovementBehaviour().getLocation().dist(location) < radius) {
                         closeEntities.add(entity);
                     }
                 }
             }
         }
 
         return closeEntities;
     }
 
     public void draw() {
         synchronized (_entities) {
             for (Entity entity : entities) {
                 entity.draw();
             }
         }
     }
 }
