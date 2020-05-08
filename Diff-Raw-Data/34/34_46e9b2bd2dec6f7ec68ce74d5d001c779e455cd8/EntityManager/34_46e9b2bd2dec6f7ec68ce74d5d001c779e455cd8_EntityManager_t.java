 package projectrts.model.entities;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import projectrts.model.world.Position;
 
 /**
  * The singleton entity manager.
  * 
  * @author Bjorn Persson Mattsson, Modified by Markus Ekstrm
  * 
  */
 public enum EntityManager implements IEntityManager {
 	INSTANCE;
 
 	private final List<AbstractEntity> allEntities = new ArrayList<AbstractEntity>();
 	private final List<AbstractEntity> entitiesAddQueue = new ArrayList<AbstractEntity>();
 	private final List<AbstractEntity> entitiesRemoveQueue = new ArrayList<AbstractEntity>();
 	private final List<PlayerControlledEntity> selectedEntities = new ArrayList<PlayerControlledEntity>();
 	private int idCounter = 0;
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	static {
 		try {
 			// Initialize the entity classes.
 			Class.forName(Warrior.class.getName());
 			Class.forName(Ranged.class.getName());
 			Class.forName(Worker.class.getName());
 			Class.forName(Resource.class.getName());
 			Class.forName(Headquarter.class.getName());
 			Class.forName(Barracks.class.getName());
 			Class.forName(Wall.class.getName());
 		} catch (ClassNotFoundException any) {
 			any.printStackTrace();
 		}
 	}
 
 	/**
 	 * Updates all entities.
 	 * 
 	 * @param tpf
 	 */
 	public void update(float tpf) {
 		for (AbstractEntity e : entitiesAddQueue) {
 			allEntities.add(e);
 			pcs.firePropertyChange("entityCreated", null, e);
 		}
 
 		for (AbstractEntity e : entitiesRemoveQueue) {
 			for (int i = 0; i < allEntities.size(); i++) {
 				if (e.equals(allEntities.get(i))) {
 					allEntities.remove(i);
 					selectedEntities.remove(e);
 					pcs.firePropertyChange("entityRemoved", e, null);
 				}
 			}
 		}
 		entitiesAddQueue.clear();
 		entitiesRemoveQueue.clear();
 	}
 
 	@Override
 	public List<IPlayerControlledEntity> getEntitiesOfPlayer(IPlayer player) {
 		List<IPlayerControlledEntity> output = new ArrayList<IPlayerControlledEntity>();
 		for (AbstractEntity e : allEntities) {
 			if (e instanceof IPlayerControlledEntity) {
 				IPlayerControlledEntity pce = (IPlayerControlledEntity) e;
 				if (pce.getOwner().equals(player)) {
 					output.add(pce);
 				}
 			}
 		}
 		return output;
 	}
 
 	@Override
 	public List<IEntity> getAllEntities() {
 		List<IEntity> output = new ArrayList<IEntity>();
 		for (IEntity e : allEntities) {
 			output.add(e);
 		}
 		return output;
 	}
 
 	/**
 	 * Adds a new non-player controlled entity to the EntityManager.
 	 * 
 	 * @param npce
 	 *            The class name of the npce as a string, e.g. "Rock".
 	 * @param pos
 	 *            The position of the entity.
 	 */
 	public void addNewNPCE(String npce, Position pos) {
 		NonPlayerControlledEntity newNPCE = EntityFactory.createNPCE(npce, pos);
 		entitiesAddQueue.add(newNPCE);
 	}
 
 	/**
 	 * Adds a new player controlled entity to the EntityManager.
 	 * 
 	 * @param pce
 	 *            The class name of the npce as a string, e.g. "Worker".
 	 * @param iPlayer
 	 *            The player that shall have control over the new entity.
 	 * @param pos
 	 *            The position of the entity.
 	 */
 	public void addNewPCE(String pce, Player iPlayer, Position pos) {
 		PlayerControlledEntity newPCE = EntityFactory.createPCE(pce, iPlayer,
 				pos);
 		entitiesAddQueue.add(newPCE);
 	}
 
 	/**
 	 * @return New entity ID.
 	 */
 	public int requestNewEntityID() {
 		idCounter++;
 		return idCounter;
 	}
 
 	// TODO Markus: Add javadoc
 	public void removeEntity(AbstractEntity entity) {
 		entitiesRemoveQueue.add(entity);
 	}
 
 	// TODO Markus: Possible duplicated code
 	@Override
 	public PlayerControlledEntity getPCEAtPosition(Position pos) {
 		List<IEntity> entities = EntityManager.INSTANCE.getAllEntities();
 		for (IEntity entity : entities) {
 			if (entity instanceof PlayerControlledEntity) {
 				float unitSize = entity.getSize();
 				Position unitPos = entity.getPosition();
 
 				// If the point is within the area of the unit
 				if (se.chalmers.pebjorn.javautils.Math.isWithin(pos.getX(),
 						unitPos.getX() - unitSize / 2, unitPos.getX()
 								+ unitSize / 2)
 						&& se.chalmers.pebjorn.javautils.Math.isWithin(
 								pos.getY(), unitPos.getY() - unitSize / 2,
 								unitPos.getY() + unitSize / 2)) {
 					return (PlayerControlledEntity) entity;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * If there exists a PCE that the passed player owns at the passed position
 	 * it is returned, otherwise this method returns null.
 	 * 
 	 * @param pos
 	 *            The position to check.
 	 * @param player
 	 *            The hopeful owner.
 	 * @return A PCE if there is one on the position that the player owns,
 	 *         otherwise null.
 	 */
 	public PlayerControlledEntity getPCEAtPosition(Position pos, Player player) {
 		if (getPCEAtPosition(pos) != null) {
 			// TODO Markus: PMD: These nested if statements could be combined
 			if (getPCEAtPosition(pos).getOwner().equals(player)) {
 				return getPCEAtPosition(pos);
 			}
 		}
 		return null;
 	}
 
 	// TODO Markus: Extract common code from getPlayerControlledEntityAtPosition and this method
 	@Override
 	public NonPlayerControlledEntity getNPCEAtPosition(Position pos) {
 		List<IEntity> entities = EntityManager.INSTANCE.getAllEntities();
 		for (IEntity entity : entities) {
 			if (entity instanceof NonPlayerControlledEntity) {
 				float unitSize = entity.getSize();
 				Position unitPos = entity.getPosition();
 
 				// If the point is within the area of the unit
 				if (se.chalmers.pebjorn.javautils.Math.isWithin(pos.getX(),
 						unitPos.getX() - unitSize / 2, unitPos.getX()
 								+ unitSize / 2)
 						&& se.chalmers.pebjorn.javautils.Math.isWithin(
 								pos.getY(), unitPos.getY() - unitSize / 2,
 								unitPos.getY() + unitSize / 2)) {
 					return (NonPlayerControlledEntity) entity;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void select(Position pos, IPlayer owner) {
 		selectedEntities.clear();
 		// PlayerControlledEntity entity = getPCEAtPosition(pos, owner);
 		PlayerControlledEntity entity = getPCEAtPosition(pos);
 		if (entity != null) { // No entity is at that position
 			selectedEntities.add(entity);
			pcs.firePropertyChange("entitySelected", null ,null);
 		}
 	}
 
 	@Override
 	public List<IEntity> getSelectedEntities() {
 		List<IEntity> entities = new ArrayList<IEntity>();
 		for (IEntity entity : selectedEntities) {
 			entities.add(entity);
 		}
 		return entities;
 	}
 
 	@Override
 	public List<IPlayerControlledEntity> getSelectedEntitiesOfPlayer(
 			IPlayer owner) {
 		List<IPlayerControlledEntity> output = new ArrayList<IPlayerControlledEntity>();
 		for (IEntity entity : selectedEntities) {
 			if (entity instanceof PlayerControlledEntity) {
 				PlayerControlledEntity pce = (PlayerControlledEntity) entity;
 				if (pce.getOwner().equals(owner)) {
 					output.add(pce);
 				}
 			}
 		}
 		return output;
 	}
 
 	// TODO Markus(?): Add javadoc
 	public boolean isSelected(PlayerControlledEntity entity) {
 		boolean ans = false;
 
 		for (PlayerControlledEntity sEntity : selectedEntities) {
 			if (entity.equals(sEntity)) {
 				ans = true;
 			}
 		}
 		return ans;
 	}
 
 	/**
 	 * Returns a list of all entities close to the position. Returns all
 	 * entities that can be seen from the circle with center in 'p' and the
 	 * radius 'range'.
 	 * 
 	 * @param position
 	 *            The position from which to check.
 	 * @return List of all entities close to position.
 	 */
 	public List<AbstractEntity> getNearbyEntities(Position position, float range) {
 		List<AbstractEntity> output = new ArrayList<AbstractEntity>();
 
 		for (AbstractEntity e : allEntities) {
 			Vector2d distance = new Vector2d(e.getPosition().getX()
 					- position.getX(), e.getPosition().getY() - position.getY());
 			if (distance.length() - (e.getSize() / 2) <= range) {
 				output.add(e);
 			}
 		}
 		return output;
 	}
 
 	// TODO Markus: Add javadoc
 	public PlayerControlledEntity getClosestEnemy(PlayerControlledEntity pce) {
 		List<AbstractEntity> nearbyEntities = getNearbyEntities(
 				pce.getPosition(), pce.getSightRange());
 		PlayerControlledEntity closestPCE = null;
 
 		for (AbstractEntity entity : nearbyEntities) {
 			if (entity instanceof PlayerControlledEntity) {
 				PlayerControlledEntity otherPCE = (PlayerControlledEntity) entity;
 				if (closestPCE == null && pce.getOwner() != otherPCE.getOwner()) {
 					closestPCE = (PlayerControlledEntity) entity;
 				} else if (closestPCE != null) {
 					// TODO Markus: PMD: Deeply nested if..then statements are hard to read
 					// TODO Markus: PMD: These nested if statements could be combined
 					if (Position.getDistance(pce.getPosition(),
 							entity.getPosition()) < Position.getDistance(
 							pce.getPosition(), closestPCE.getPosition())
 							&& pce.getOwner() != otherPCE.getOwner()) {
 						closestPCE = (PlayerControlledEntity) entity;
 					}
 				}
 			}
 		}
 		return closestPCE;
 	}
 
 	// TODO Markus(?): Add javadoc
 	public PlayerControlledEntity getClosestEnemyStructure(
 			PlayerControlledEntity pce) {
 		List<AbstractEntity> nearbyEntities = getNearbyEntities(
 				pce.getPosition(),
 				(float) Math.sqrt(Math.pow(100, 2) + Math.pow(100, 2))); // TODO Markus: Change this
 		PlayerControlledEntity closestEnemyStruct = null;
 
 		for (AbstractEntity entity : nearbyEntities) {
 			if (entity instanceof PlayerControlledEntity) {
 				PlayerControlledEntity otherPCE = (PlayerControlledEntity) entity;
 				if (otherPCE instanceof AbstractStructure) {
 					if (closestEnemyStruct == null
 							&& pce.getOwner() != otherPCE.getOwner()) {
 						closestEnemyStruct = (PlayerControlledEntity) entity;
 					} else if (closestEnemyStruct != null) {
 						// TODO Markus: PMD: Deeply nested if..then statements are hard to read
 						// TODO Markus: PMD: These nested if statements could be combined
 						if (Position.getDistance(pce.getPosition(),
 								entity.getPosition()) < Position.getDistance(
 								pce.getPosition(),
 								closestEnemyStruct.getPosition())
 								&& pce.getOwner() != otherPCE.getOwner()) {
 							closestEnemyStruct = (PlayerControlledEntity) entity;
 						}
 					}
 				}
 			}
 		}
 		return closestEnemyStruct;
 	}
 
 	@Override
 	public void addListener(PropertyChangeListener pcl) {
 		pcs.addPropertyChangeListener(pcl);
 	}
 
 	/**
 	 * Resets all lists with entities
 	 */
 	public void resetData() {
 		selectedEntities.clear();
 		allEntities.clear();
 		entitiesRemoveQueue.clear();
 		entitiesAddQueue.clear();
 	}
 }
