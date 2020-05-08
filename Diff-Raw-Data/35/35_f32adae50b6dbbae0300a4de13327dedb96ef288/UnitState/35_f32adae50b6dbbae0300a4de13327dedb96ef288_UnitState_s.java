 package org.aimas.craftingquest.state;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 
 import org.aimas.craftingquest.core.GamePolicy;
 import org.aimas.craftingquest.state.CraftedObject.BasicResourceType;
 
 /**
  * Describes the state of a unit at any moment in the game. The class defines:
  * <ul>
  * <li>the possible unit types</li>
  * <li>the unit id</li>
  * <li>the id of the player the unit belongs to</li>
  * <li>the unit's position on the map</li>
  * <li>the unit's sight from its current position</li>
  * <li>the unit's energy levels</li>
  * <li>the opponent view for this unit</li>
  * <li>the list of carried basic resources and their quantities</li>
  * <li>the list of carried artifacts and their quantities</li>
  * </ul>
  * 
  * <p>Additionally, the state of unit contains two fields which get cleared before each action attempt.
  * These are the last scanned resource attributes and the last dug up soil resources.
  * </p> 
  */
 @SuppressWarnings("serial")
 public class UnitState implements Serializable {
 
 	public enum UnitType {
 		Crocodile, Tazmanian, Fox
 	}
 
 	/**
 	 * the opponent view for this unit
 	 */
 	private BasicUnit opponentPerspective = null;
 	
 	/* what */
 	/**
 	 * the unit id
 	 */
 	public int id;
 	
 	/**
 	 * the id of the player that owns the unit
 	 */
 	public int playerID;
 	
 	/**
 	 * the unit type
 	 */
 	public UnitType type;
 	
 	/**
 	 * the unit energy points
 	 */
 	public int energy;
 
 	// must be cleared before each transition
 	/**
 	 * the last scanned resource attributes. the field will be reset to null before each action attempt
 	 */
 	public List<int[]>[][] scannedResourceAttributes = null;
 	
 	/**
 	 * the last dug up soil resources. the field will be reset to null before each action attempt
 	 */
 	public HashMap<BasicResourceType, Integer> currentCellResources = new HashMap<BasicResourceType, Integer>();
 
 	// pertain from round to round
 	/**
 	 * the list of carried basic resources and their associated quantities
 	 */
 	public HashMap<BasicResourceType, Integer> carriedResources = new HashMap<BasicResourceType, Integer>();
 	
 	/**
 	 * the list of carried objects and their associated quantities
 	 */
 	public HashMap<CraftedObject, Integer> carriedObjects = new HashMap<CraftedObject, Integer>();
 
 	/* where */
 	/**
 	 * the unit position
 	 */
 	public Point2i pos;
 	
 	/**
 	 * <p>The unit sight. It is a fixed 5x5 array of {@link CellState} objects.</p>
 	 * <p>The unit is positioned in the middle of the visibility array.</p>
 	 * <p>If the unit is near the margins of the map, cells falling outside the boundaries will be null.</p>  
 	 */
 	public CellState[][] sight;
 
 	public UnitState() {
 		int sightDim = 2 * GamePolicy.sightRadius + 1;
 		sight = new CellState[sightDim][sightDim];
 	}
 
 	public UnitState(int id, int playerID, UnitType type, Point2i pos, int energy) {
 		this.id = id;
 		this.playerID = playerID;
 		this.type = type;
 		this.pos = pos;
 		this.energy = energy;
 
 		int sightDim = 2 * GamePolicy.sightRadius + 1;
 		sight = new CellState[sightDim][sightDim];
 	}
 
 	@Override
 	public String toString() {
 		String info = "";
 		info += "playerID=" + playerID + " type=" + type + " pos=" + pos + " energy=" + energy + "\n";
 		info += "    carried resources: \n";
 		info += "        ";
 		for (BasicResourceType br : carriedResources.keySet()) {
 			info += br.name() + ":" + carriedResources.get(br) + " ";
 		}
 		info += "\n";
 		
 		info += "    carried objects: \n";
 		info += "        ";
 		for (CraftedObject o : carriedObjects.keySet()) {
 			info += o.getType().name() + ":" + carriedObjects.get(o) + " ";
 		}
 		info += "\n";
 		
 
 		return info;
 	}
 
 	/**
 	 * Gives the opponent's view of this unit.
 	 * @return the opponent perspective of this unit
 	 */
 	public BasicUnit getOpponentPerspective() {
 		if (opponentPerspective == null) {
 			opponentPerspective = new BasicUnit();
 			opponentPerspective.set(type, playerID, energy);
 		}
 		else {
 			opponentPerspective.set(type, playerID, energy);
 		}
 		
 		return opponentPerspective;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof UnitState)) {
 			return false;
 		}
 		
 		final UnitState other = (UnitState)obj;
 		if (type != other.type) {
 			return false;
 		}
 		
 		if (playerID != other.playerID) {
 			return false;
 		}
 		
 		return true;
 	}
 }
