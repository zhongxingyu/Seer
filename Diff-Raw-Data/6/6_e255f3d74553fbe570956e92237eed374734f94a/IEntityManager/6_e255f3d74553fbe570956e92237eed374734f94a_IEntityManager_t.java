 package projectrts.model.entities;
 
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import projectrts.model.world.Position;
 
/**
 * The interface for the EntityManager.
 * 
 * @author Markus Ekstrom
 */
 public interface IEntityManager {
 
 	/**
 	 * @return All entities.
 	 */
 	List<IEntity> getAllEntities();
 
 	/**
 	 * Returns all entities that the provided player owns.
 	 * 
 	 * @param player
 	 *            Player.
 	 * @return A list of all entities of player.
 	 */
 	List<IPlayerControlledEntity> getEntitiesOfPlayer(IPlayer player);
 
 	/**
 	 * Selects entities at the specified position
 	 * 
 	 * @param pos
 	 *            the position where entities should be selected
 	 * @param owner
 	 *            the player that selected the entities
 	 */
 	void select(Position pos, IPlayer owner);
 
 	/**
 	 * Returns the selected entities
 	 * 
 	 * @return A list with the selected entities
 	 */
 	List<IEntity> getSelectedEntities();
 
 	/**
 	 * Returns the selected entities of the player.
 	 * 
 	 * @param player
 	 *            The player.
 	 * @return All selected entities of the player.
 	 */
 	List<IPlayerControlledEntity> getSelectedEntitiesOfPlayer(IPlayer player);
 	void addListener(PropertyChangeListener pcl);
 
 	/**
 	 * If there exists a PCE at the passed position it is returned, otherwise
 	 * this method returns null.
 	 * 
 	 * @param pos
 	 *            The position to check.
 	 * @return A PCE if there is one on the position, otherwise null.
 	 */
 	IPlayerControlledEntity getPCEAtPosition(Position pos);
 
 	IEntity getNPCEAtPosition(Position pos);
 }
