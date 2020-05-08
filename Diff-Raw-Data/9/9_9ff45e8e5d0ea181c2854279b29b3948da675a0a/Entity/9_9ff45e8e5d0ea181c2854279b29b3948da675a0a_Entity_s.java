 package com.punchline.javalib.entities;
 
 import com.badlogic.gdx.utils.Pool.Poolable;
 import com.punchline.javalib.entities.components.Component;
 import com.punchline.javalib.entities.components.ComponentManager;
import com.punchline.javalib.utils.events.EventHandler;
 
 /**
  * A game entity that contains several {@link Component Components} which define its attributes.
  * @author Natman64
  * @author MadcowD
  *
  */
 public final class Entity extends ComponentManager implements Poolable {
 	
 	//region Fields
 	
 	/**
 	 * This entity's unique tag, for identifying it individually. This must be set only once, by a template.
 	 */
 	private String tag = "";
 	
 	/**
 	 * The name of the group this entity belongs to. This must be set only once, by a template.
 	 */
 	private String group = "";
 	
 	/**
 	 * This entity's type. This must be set only once, by a template.
 	 */
 	private String type = "";
 	
 	/**
 	 * EventHandler that is invoked when this Entity is deleted.
 	 */
 	public final EventHandler onDeleted = new EventHandler();
 	
 	//endregion
 	
 	//region Flags
 	
 	/**
 	 * Deletion flag.
 	 */
 	private boolean deleted = false;
 	
 	/**
 	 * Changed flag.
 	 */
 	private boolean changed = false;
 	
 	//endregion
 	
 	//region Initialization
 	
 	/**
 	 * Creates a default entity object. In order to really initialize an 
 	 * entity, use a template, and call {@link #init}
 	 */
 	public Entity() 
 	{
 	}
 	
 	/**
 	 * Assigns the Entity's metadata.
 	 * @param tag This Entity's unique tag.
 	 * @param group This Entity's group name.
 	 * @param type This Entity's type.
 	 */
 	public void init(String tag, String group, String type) {
 		this.tag = tag;
 		this.group = group;
 		this.type = type;
 	}
 	
 	//endregion
 
 	//region Disposal
 	
 	/**
 	 * Flags this entity for deletion by the {@link EntityManager}.
 	 */
 	public void delete() {
 		deleted = true;
 	}
 
 	/**
 	 * Resets the entity for reuse in the Entity pool.
 	 */
 	@Override
 	public void reset() {
 		clearComponents();
 		tag = "";
 		group = "";
 		type = "";
 		deleted = false;
 		changed = false;
 		onDeleted.clear();
 	}	
 
 	//endregion
 	
 	//region Accessors
 	
 	/**
 	 * @return This entity's unique tag.
 	 */
 	public String getTag() {
 		return tag;
 	}
 	
 	/**
 	 * @return This entity's group name.
 	 */
 	public String getGroup() {
 		return group;
 	}
 	
 	/**
 	 * @return This entity's type.
 	 */
 	public String getType() {
 		return type;
 	}
 	
 	/**
 	 * @return Whether this Entity has been flagged for deletion.
 	 */
 	public boolean isDeleted() {
 		return deleted;
 	}
 	
 	/**
 	 * @return Whether this Entity's components were changed.
 	 */
 	public boolean wasChanged() {
 		if (changed) {
 			changed = false;
 			return true;
 		}
 
 		return false;
 	}
 	
 	//endregion
 
 	//region Component Management
 	
 	@Override
 	public Component addComponent(Component component) {
 		changed = true;
 		return super.addComponent(component);
 	}
 
 	@Override
 	public void removeComponent(Component component) {
 		changed = true;
 		super.removeComponent(component);
 	}
 
 	@Override
 	public void clearComponents(){
 		changed = true;
 		super.clearComponents();
 	}
 	
 	//endregion
 
 }
