 package edu.wheaton.simulator.statistics;
 
 import java.util.Map.Entry;
 
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.entity.EntityID;
 
 
 /**
  * A class representing all the information to track from each Entity in the
  * game.
  * 
  * @author Daniel Gill, Akon Ngoh
  */
 public class EntitySnapshot {
 
 	public final EntityID entityID;
 
 	/**
 	 * The saved fields of this entity.
 	 */
 	public final ImmutableMap<String, FieldSnapshot> fields;
 
 	/**
 	 * The point in the simulation at which this snapshot was taken.
 	 */
 	public final Integer step;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param id
 	 *            The ID of the Entity associated with this snapshot.
 	 * @param fields
 	 *            The current values of the fields of the Entity.
 	 * @param step
 	 *            The step in the simulation associated with this snapshot.
 	 */
 	public EntitySnapshot(EntityID entityID, ImmutableMap<String, FieldSnapshot> fields,
 			Integer step) {
 		this.entityID = entityID;
 		this.step = step;
 		this.fields = fields;
 	}
 	
 	/**
 	 * Produce a string serializing this object
 	 * @return a String containing all of the data in this snapshot
 	 * 
 	 * Format: (Stuff in parentheses is just notes - not actually there)
 	 * -----------------------------------------------------------------
 	 * EntitySnapshot
 	 * 145 (EntityID - just an int)
 	 * Fields: MapString FieldSnapshot Name Value true numberValue
 	 * Fields: MapString FieldSnapshot Name Value true numberValue
 	 * 3 (step - an int)
 	 */
 	public String serialize(){
 		String s = "EntitySnapshot";
 		s += "\n" + entityID.getInt();
 		
 		for (Entry<String, FieldSnapshot> entry : fields.entrySet()) {
			s += "\nFields: " + entry.getKey() + entry.getValue().serialize();
 		}
 		
 		s += "\n" + step; 
 		
 		return s; 
 	}
 	
 }
