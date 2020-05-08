 package bot;
 
 import util.Scoreable;
 
 public class Action implements Scoreable {
 
 	private double score;
 	private Direction dir;	
 	private ActionType type;
 	
 	public Action(ActionType type, Direction dir, double score){
 		this.type = type;
 		this.dir = dir;
 		this.score = score;
 	}
 	
 	public Direction getDirection(){
 		return dir;
 	}
 	
 	public ActionType getType(){
 		return type;
 	}
 	
 	public double getScore() {
 		return score;
 	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Action : [");
		sb.append(type);
		sb.append(',');
		sb.append(dir);
		sb.append(',');
		sb.append(score);
		sb.append(']');
		return sb.toString();
	}
 
 }
