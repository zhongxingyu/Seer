 package org.aimas.craftingquest.state;
 
 import java.io.Serializable;
 
 /**
  * The general type of a strategic resource (either a merchant camp or a defense tower).
  * Data about the type and position of the strategic resource is given.
  */
 @SuppressWarnings("serial")
 public class StrategicResource implements Serializable {
 	public enum StrategicResourceType {
 		Tower, Merchant
 	};
 	
 	/**
 	 * the position of this strategic resource
 	 */
 	Point2i position;
 	
 	/**
 	 * the type of this strategic resource
 	 */
 	StrategicResourceType type;
 	
 	public StrategicResource(StrategicResourceType type, Point2i position) {
 		this.type = type;
 		this.position = position;
 	}
 
 	public Point2i getPosition() {
 		return position;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof StrategicResource)) {
 			return false;
 		}
 		
 		final StrategicResource other = (StrategicResource)obj;
 		
 		if (type != other.type) {
 			return false;
 		}
 		
 		if (position.x != other.position.x || position.y != other.position.y) {
 			return false;
 		}
 		
 		return true;
 	}
 
	@Override
	public int hashCode() {
		return type.ordinal();
	}
	
 	public StrategicResourceType getType() {
 		return type;
 	}
 }
