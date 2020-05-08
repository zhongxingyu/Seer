 package chalmers.TDA367.B17.model;
 
 import java.awt.Dimension;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 import chalmers.TDA367.B17.console.Console.MsgLevel;
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.event.GameEvent;
 import chalmers.TDA367.B17.network.Network.Pck10_TankCreated;
 import chalmers.TDA367.B17.network.Network.Pck11_PickupCreated;
 import chalmers.TDA367.B17.network.Network.Pck8_EntityDestroyed;
 import chalmers.TDA367.B17.network.Network.Pck9_EntityCreated;
 import chalmers.TDA367.B17.powerups.Shield;
 import chalmers.TDA367.B17.powerups.powerupPickups.AbstractPowerUpPickup;
 import chalmers.TDA367.B17.spawnpoints.Spawner;
 import chalmers.TDA367.B17.spawnpoints.TankSpawnPoint;
 import chalmers.TDA367.B17.states.ServerState;
 import chalmers.TDA367.B17.terrain.BrownWall;
 import chalmers.TDA367.B17.weaponPickups.AbstractWeaponPickup;
 
 public class World {
 	//A map holding all entities with an ID
 	private Map<Integer, Entity> entities;
 	//The size of the map (world)
 	private Dimension size;
 	
 	//A spawner that spawns/respawns tanks.
 	private TankSpawner tankSpawner;
 	private boolean serverWorld = false;
 	
 	//A spawner that spawns powerup and weapons.
 	private Spawner spawner;
 	
 	/**
 	 * Create a new world.
 	 * @param size The size of the world
 	 * @param serverWorld If the world is server or client based
 	 */
 	public World(Dimension size, boolean serverWorld) {
 		this.size = size;
 		this.serverWorld = serverWorld;
 		this.entities = new ConcurrentHashMap<Integer, Entity>();
 		this.tankSpawner = new TankSpawner();
 		spawner = new Spawner();
 	}
 
 	/**
 	 * Initiate the world, giving it a MapBounds for border collision.
 	 */
 	public void init(){
 		new MapBounds(GameController.getInstance().generateID(), getSize());
 	}
 
 	/**
 	 * Adds a new entity.
 	 * @param newEntity the entity to add
 	 */
 	public void addEntity(Entity newEntity){
 		if(serverWorld){
			if(!(newEntity instanceof MapBounds) || !(newEntity instanceof BrownWall) || !(newEntity instanceof TankSpawnPoint)){
 				GameController.getInstance().getConsole().addMsg("Created (ID" + newEntity.getId() + "): "+  newEntity.getClass().getSimpleName(), MsgLevel.STANDARD);
 				
 				if(newEntity instanceof AbstractTank){
 					Pck10_TankCreated tankPacket = new Pck10_TankCreated();
 					tankPacket.entityID = ((AbstractTank) newEntity).getId();
 					tankPacket.identifier = ((AbstractTank) newEntity).getClass().getSimpleName();
 					tankPacket.direction = ((AbstractTank) newEntity).getDirection();
 					ServerState.getInstance().addToAllClientsQueue(tankPacket);
 				} else if((newEntity instanceof AbstractWeaponPickup) || (newEntity instanceof AbstractPowerUpPickup)) {
 					Pck11_PickupCreated pickPck = new Pck11_PickupCreated();
 					pickPck.entityID = newEntity.getId();
 					pickPck.identifier = newEntity.getClass().getSimpleName();
 					pickPck.position = newEntity.getPosition();
 					ServerState.getInstance().addToAllClientsQueue(pickPck);
 				} else {				
 					Pck9_EntityCreated packet = new Pck9_EntityCreated();
 					packet.entityID = newEntity.getId();
 					packet.identifier = newEntity.getClass().getSimpleName();
 					if(newEntity instanceof Shield)
 						packet.possibleOwnerID = ((Shield)newEntity).getTank().getId();
 					else if(newEntity instanceof AbstractTurret)
 						packet.possibleOwnerID = ((AbstractTurret)newEntity).getTank().getId();
 					ServerState.getInstance().addToAllClientsQueue(packet);	
 				}
 			}
 		}
 		entities.put(newEntity.getId(), newEntity);
 	}
 
 	/**
 	 * Remove an entity with a specific id from this world.
 	 * @param id The id of the entity to be removed
 	 */
 	public void removeEntity(int id){
 		removeEntity(getEntity(id));
 	}
 	
 	/**
 	 * Remove an entity from the world.
 	 * @param entity The entity to be removed
 	 */
 	public void removeEntity(Entity entity){
 		if(serverWorld){
 			GameController.getInstance().getConsole().addMsg("Destroyed (ID" + entity.getId() + "): "+  entity.getClass().getSimpleName(), MsgLevel.STANDARD);	
 			Pck8_EntityDestroyed pck = new Pck8_EntityDestroyed();
 			pck.entityID = entity.getId();
 			ServerState.getInstance().addToAllClientsQueue(pck);
 		}
 		if(entity != null){
 			entities.remove(entity.getId());
 		}
 	}	
 	
 	/**
 	 * Get an entity from the World.
 	 * @param id The id of the requested entity
 	 * @return entity
 	 */
 	public Entity getEntity(int id){
 		return entities.get(id);
 	}
 	
 	/**
 	 * Return the entities in the world.
 	 * @return entities in world.
 	 */
 	public Map<Integer, Entity> getEntities(){
 		return entities;
 	}
 
 	/**
 	 * Checks for collision between all entities that the world contains. Calls
 	 * the objects respective "didCollideWith"-methods if any entities intersects.
 	 * @param paramEntity The entity to check collision for
 	 */
 	public void checkCollisionsFor(Entity paramEntity){
 		Iterator<Entry<Integer, Entity>> iterator = entities.entrySet().iterator();
 		while(iterator.hasNext()){
 			Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 			Entity entity = entry.getValue();
 
 			if(entity!=paramEntity && (entity.getShape().intersects(paramEntity.getShape())
 					|| (entity.getShape().contains(paramEntity.getShape()) && !(entity instanceof MapBounds)))){
 				paramEntity.didCollideWith(entity);
 				// prevent double method calls
 				if(!(entity instanceof MovableEntity))
 					entity.didCollideWith(paramEntity);
 			}
 		}
 	}
 	
 	/**
 	 * Get the size of the world.
 	 * @return The size of the world
 	 */
 	public Dimension getSize() {
 		return size;
 	}
 
 	/**
 	 * Get the TankSpawner of this world.
 	 * @return The TankSpawner of this world
 	 */
 	public TankSpawner getTankSpawner() {
 		return tankSpawner;
 	}
 
 	/**
 	 * Set the TankSpawner of this world.
 	 * @param tankSpawner The new TankSpawner
 	 */
 	public void setTankSpawner(TankSpawner tankSpawner) {
 		this.tankSpawner = tankSpawner;
 	}
 	
 	/**
 	 * Get the spawner of this world.
 	 * @return The spawner
 	 */
 	public Spawner getSpawner() {
 		return spawner;
 	}
 	
 	/**
 	 * Handle a GameEvent.
 	 * @param event The event to be handled
 	 */
 	public void handleEvent(GameEvent event){
 		if(serverWorld){
 			ServerState.getInstance().sendEvent(event);
 		} else {
 			GameController.getInstance().handleEvent(event);
 		}
 	}
 }
