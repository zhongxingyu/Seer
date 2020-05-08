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
 
 }
