 package btwmods.world;
 
 import java.util.EventObject;
 
 import btwmods.events.IEventInterrupter;
 import net.minecraft.src.Entity;
 
 public class EntityEvent extends EventObject implements IEventInterrupter {
 	
 	public enum TYPE { EXPLODE_ATTEMPT };
 
 	private TYPE type;
 	private Entity entity;
 	
 	private boolean isHandled = false;
 	private boolean isAllowed = true;
 	
 	public TYPE getType() {
 		return type;
 	}
 	
 	public Entity getEntity() {
 		return entity;
 	}
 	
 	public boolean isHandled() {
 		return (type == TYPE.EXPLODE_ATTEMPT) && isHandled;
 	}
 	
 	public void markHandled() {
 		isHandled = true;
 	}
 	
 	public boolean isAllowed() {
 		return isAllowed;
 	}
 	
 	public void markNotAllowed() {
 		isAllowed = false;
 	}
 
 	public static EntityEvent ExplodeAttempt(Entity entity) {
		EntityEvent event = new EntityEvent(entity);
 		return event;
 	}
 
	private EntityEvent(Entity entity) {
 		super(entity);
 		this.entity = entity;
 	}
 
 	@Override
 	public boolean isInterrupted() {
 		return isHandled();
 	}
 }
