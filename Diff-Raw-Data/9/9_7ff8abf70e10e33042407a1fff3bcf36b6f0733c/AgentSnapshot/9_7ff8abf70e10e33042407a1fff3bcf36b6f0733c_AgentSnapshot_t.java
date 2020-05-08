 package edu.wheaton.simulator.statistics;
 
 import com.google.common.collect.ImmutableMap;
 import edu.wheaton.simulator.entity.EntityID;
 import edu.wheaton.simulator.entity.PrototypeID;
 
 
 /**
  * A class representing all the information to track from agents in the game.
  * 
  * @author Akon, Daniel Gill
  * 
  */
 public class AgentSnapshot extends EntitySnapshot {
 
 	/**
 	 * The present prototype for the category of this Entity.
 	 */
 	public final PrototypeID prototype;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param id
 	 *            The ID of the Agent associated with this snapshot.
 	 * @param fields
 	 *            The current values of the fields of the Entity
 	 * @param step
 	 *            The step in the simulation associated with this snapshot.
 	 * @param prototype
 	 *            The prototype for this category of Agent.
 	 */
 	public AgentSnapshot(EntityID entityID, ImmutableMap<String, FieldSnapshot> fields,
 			Integer step, PrototypeID prototype) {
 		super(entityID, fields, step);
 		this.prototype = prototype;
 	}
 
 	/**
 	 * Produce a string serializing this object
 	 * @return a String containing all of the data in this snapshot
 	 * 
 	 * Format: (Stuff in parentheses is just notes - not actually there)
 	 * -----------------------------------------------------------------
 	 * AgentSnapshot
 	 * 145 (EntityID - just an int)
 	 * Fields: MapString FieldSnapshot Name Value true numberValue
 	 * Fields: MapString FieldSnapshot Name Value true numberValue
 	 * 3 (step - an int)
 	 * 12 (PrototypeID - an int)
 	 */
 	@Override
 	public String serialize(){
 		String s = super.serialize();
		s = s.replace("EntitySnapshot", "AgentSnapshot"); 
 		return s + "\n" + prototype.getInt(); 
 	}
 }
