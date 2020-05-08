 package Learning.Towers;
 
 import Learning.Towers.Behaviours.Constructive.Construction;
 import Learning.Towers.Entities.Entity;
 import Learning.Towers.Entities.EntityFactory;
 import Learning.Towers.Entities.Meta.Group;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Piers
  * Date: 19/10/12
  * Time: 16:42
  */
 public class Faction {
     private final static Random random = new Random();
 
     private ArrayList<Entity> entities;
     private ArrayList<Group> groups;
     private Dictionary<Construction, Group> baseGroup;
 
     private float r, g, b;
 
     protected Faction(float r, float g, float b) {
         this.r = r;
         this.g = g;
         this.b = b;
 
         entities = new ArrayList<>(10);
         groups = new ArrayList<>();
         baseGroup = new Hashtable<>();
 
         Entity base = EntityFactory.getBaseEntity(this, Utilities.randomLocation(100), 26);
        Vector2D groupLocation = new Vector2D(base.getMovementBehaviour().getLocation());
         groupLocation.add(50, 50);
         Group newGroup = new Group(r, g, b, groupLocation, 5, this);
         baseGroup.put(base.getConstructionBehaviour(), newGroup);
         Main.GAME_LOOP.addEntity(base);
         Main.GAME_LOOP.addEntity(newGroup);
     }
 
     public void addEntity(Entity entity) {
         entities.add(entity);
     }
 
     public void makeEntity(double x, double y, Construction base) {
         // Create new entity
         Entity entity = EntityFactory.getGroupedEntity(this, baseGroup.get(base), new Vector2D(x, y), 5);
 
         // Add entity where necessary
         entities.add(entity);
         baseGroup.get(base).addEntity(entity);
         Main.GAME_LOOP.addEntity(entity);
 
         // House keeping on the groups
         if (baseGroup.get(base).isFull()) {
            Vector2D groupLocation = new Vector2D(base.getEntity().getMovementBehaviour().getLocation());
             groupLocation.add(50, 50);
             Group newGroup = new Group(r, g, b, groupLocation, 5, this);
             groups.add(baseGroup.get(base));
             baseGroup.get(base).switchToWander();
             baseGroup.put(base, newGroup);
             Main.GAME_LOOP.addEntity(newGroup);
         }
     }
 
     public void update() {
 
     }
 
     public float getR() {
         return r;
     }
 
     public float getG() {
         return g;
     }
 
     public float getB() {
         return b;
     }
 
     public ArrayList<Entity> getEntities() {
         return entities;
     }
 }
 
 
 
