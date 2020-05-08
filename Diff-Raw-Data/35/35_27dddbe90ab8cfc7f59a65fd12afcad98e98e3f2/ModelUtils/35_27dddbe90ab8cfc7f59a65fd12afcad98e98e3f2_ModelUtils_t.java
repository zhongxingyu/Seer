 package projectrts.model.core.utils;
 
 import java.util.List;
 
 import projectrts.model.core.EntityManager;
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.IEntity;
 import projectrts.model.core.entities.NonPlayerControlledEntity;
 import projectrts.model.core.entities.PlayerControlledEntity;
 
 /**
  * Utility class
  * @author Filip Brynfors. Modified by Jakob Svensson & Bjorn Persson Mattsson
  *
  */
 public enum ModelUtils {
 	INSTANCE;
 	
 	/**
 	 * Gets the distance between two positions
 	 * @param p1 the first point
 	 * @param p2 the second point
 	 * @return the distance between the points
 	 */
 	public double getDistance(Position p1, Position p2){
 		double dx = p1.getX() - p2.getX();
 		double dy = p1.getY() - p2.getY();
 		
 		return Math.sqrt(dx*dx+dy*dy);
 	}
 	
 	
 	//TODO Anyone: Should this method be in EntityManager?
 	public PlayerControlledEntity getPlayerControlledEntityAtPosition(Position pos){
 		List<IEntity> entities = EntityManager.getInstance().getAllEntities();
 		for(IEntity entity: entities){
 			if(entity instanceof PlayerControlledEntity){
 				
 				float unitSize = entity.getSize();
 				Position unitPos = entity.getPosition();
 				
 				//If the point is within the area of the unit
 				if(isWithin(pos.getX(), unitPos.getX()-unitSize/2, unitPos.getX()+unitSize/2)
 						&& isWithin(pos.getY(), unitPos.getY()-unitSize/2, unitPos.getY() + unitSize/2)){
 					PlayerControlledEntity pcEntity  = (PlayerControlledEntity) entity; 
 					return pcEntity;
 					
 				}
 			}
 		}
 		return null;
 		
 	}
 	
 	//TODO Anyone: Extraxt common code from getPlayerControlledEntityAtPosition and this method
 	public NonPlayerControlledEntity getNonPlayerControlledEntity (Position pos){
 		List<IEntity> entities = EntityManager.getInstance().getAllEntities();
 		for(IEntity entity: entities){
 			if(entity instanceof NonPlayerControlledEntity){
 				
 				float unitSize = entity.getSize();
 				Position unitPos = entity.getPosition();
 				
 				//If the point is within the area of the unit
 				if(isWithin(pos.getX(), unitPos.getX()-unitSize/2, unitPos.getX()+unitSize/2)
 						&& isWithin(pos.getY(), unitPos.getY()-unitSize/2, unitPos.getY() + unitSize/2)){
 					NonPlayerControlledEntity npcEntity  = (NonPlayerControlledEntity) entity; 
 					return npcEntity;
 					
 				}
 			}
 		}
 		return null;
 	}
 	
 	public boolean isWithin(double p, double low, double high){
 		return (p>=low && p<=high);
 	}
	
	/**
	 * Restricts a value to be within a specified range.
	 * @param value The value to clamp.
	 * @param min The minimum value. If value is less than min, min will be returned.
	 * @param max The maximum value. If value is greater than max, max will be returned.
	 * @return If value > max, max will be returned. If value < min, min will be returned.
	 * If min <= value >= max, value will be returned.
	 */
	public double clamp(double value, double min, double max) {
		return (value > max ? max : (value < min ? min : value));
	}
 }
