 package team035.messages;
 
 import java.io.Serializable;
 
 import team035.robots.BaseRobot;
 import battlecode.common.Clock;
 import battlecode.common.MapLocation;
 import battlecode.common.RobotType;
 
 public class MessageAddress implements Serializable {
 	private static final long serialVersionUID = -4788626538734887703L;
 
 	public enum AddressType {
 		BROADCAST,
 		ROBOT_ID,
 		ROBOT_TYPE,
 		BROADCAST_DISTANCE
 	}
 
 	public AddressType type;
 	public int id;
 	public RobotType robotType;
 	public int sentAt;
 	public int distanceSquared;
 	public MapLocation fromLocation;
 	
 	public MessageAddress(AddressType t, int id) {
 		sentAt = Clock.getRoundNum();
 		if(t == AddressType.ROBOT_ID) {
 			this.type = t;
 			this.id = id;
 		} else {
 			System.out.println("ERR: Tried to make a MessageAddress with an int and an AddressType other than ROBOT_ID");
 		}
 	}
 	
 	public MessageAddress(AddressType t, int distance, MapLocation location) {
 		sentAt = Clock.getRoundNum();
 		if(t == AddressType.BROADCAST_DISTANCE) {
 			this.type = t;
 			this.distanceSquared = distance;
 			this.fromLocation = location;
 		} else {
 			System.out.println("ERR: Tried to make a MessageAddress with an int and MaLocation and an AddressType other than BROADCAST_DISTANCE");
 		}
 	}
 	
 	public MessageAddress(AddressType t, RobotType robotType) {
 		sentAt = Clock.getRoundNum();
 
 		if(t == AddressType.ROBOT_TYPE) {
 			this.type = t;
 			this.robotType = robotType;
 		} else {
 			System.out.println("ERR: Tried to make a MessageAddress with a RobotType and an AddressType other than ROBOT_TYPE");
 		}
 	}
 	
 	public MessageAddress(AddressType t) {
 		sentAt = Clock.getRoundNum();
 
 		if(t == AddressType.BROADCAST) {
 			this.type = t;
 		} else {
 			System.out.println("ERR: Tried to make a MessageAddress with no args and an AddressType other than BROADCAST");
 		}
 	}
 	
 	public boolean isForThisRobot() {
 		if(Clock.getRoundNum()-this.sentAt > 1) return false;
 		
		switch(type) {
 			case BROADCAST:
 				return true;
 			case ROBOT_ID:
 				if(BaseRobot.robot.getRc().getRobot().getID()==this.id) return true;
 				else return false;
 			case ROBOT_TYPE:
 				if(this.robotType==BaseRobot.robot.getRc().getType()) return true;
 				else return false;
 			case BROADCAST_DISTANCE:
 				if(BaseRobot.robot.getRc().getLocation().distanceSquaredTo(this.fromLocation) < this.distanceSquared) return true;
 				else return false;
 			default:
 				return false;
 		}
 		
 	}
 	
 	public String toString() {
 		String out= "";
 		switch(type) {
 			case BROADCAST:
 				out = "@" + type;
 				break;
 			case ROBOT_ID:
 				out = "@" + type + "." + id;
 				break;
 			case ROBOT_TYPE:
 				out = "@" + type + "." + robotType;
 				break;
 			default:
 				out = "@" + type;
 				break;
 		}
 		
 		return out + " (" + this.sentAt + ")";
 	}
 }
