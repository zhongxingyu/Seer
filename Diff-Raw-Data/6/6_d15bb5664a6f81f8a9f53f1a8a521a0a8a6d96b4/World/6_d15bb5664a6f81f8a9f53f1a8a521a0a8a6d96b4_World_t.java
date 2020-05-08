 package chalmers.TDA367.B17.model;
 
 import java.awt.*;
 import java.util.*;
 import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
 
 public class World {
 	
	private Map<Integer, Entity> entities;
 
 	private Dimension size;
 	
 	private int latestID;
 	
 	public World(Dimension size) {
 		this.size = size;
		this.entities = new HashMap<Integer, Entity>();
 	}
 
 	public void init(){
 		new MapBounds(getSize());
 	}
 
 	/**
 	 * Adds a new entity.
 	 * @param newEntity the entity to add
 	 */
 	public void addEntity(Entity newEntity){
 		entities.put(newEntity.getId(), newEntity);
 	}
 	
 	/**
 	 * Get an entity from the World.
 	 * @param id The id of the requested entity
 	 * @return
 	 */
 	public Entity getEntity(int id){
 		return entities.get(id);
 	}
 	
 	/**
 	 * Generate a new ID.
 	 * @return The ID
 	 */
 	public int generateID(){
 		latestID += 1;
 		return latestID;
 	}
 	
 	/**
 	 * Updates all entities existing in this world.
 	 * @param delta the time in milliseconds since last update
 	 */
 	public void update(int delta){
 		Iterator<Entry<Integer, Entity>> iterator = entities.entrySet().iterator();
 		while(iterator.hasNext()){
 			Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 			Entity entity = entry.getValue();
 			entity.update(delta);
 
 			if(entity instanceof MovableEntity)
 				checkCollisionsFor((MovableEntity)entity);
 		}
 	}
 
 	private void checkCollisionsFor(MovableEntity movableEntity){
 		Iterator<Entry<Integer, Entity>> iterator = entities.entrySet().iterator();
 		while(iterator.hasNext()){
 			Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 			Entity entity = entry.getValue();
 
 			if(entity!=movableEntity && entity.getShape().intersects(movableEntity.getShape())){
 				movableEntity.didCollideWith(entity);
 				// prevent double method calls
 				if(!(entity instanceof MovableEntity))
 					entity.didCollideWith(movableEntity);
 			}
 		}
 	}
 
 	public void removeEntity(Entity entity){
 		entities.remove(entity.getId());
 	}
 
 	public Dimension getSize() {
 		return size;
 	}
 }
