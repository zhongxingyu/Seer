import model;
 
 public class Block {
 	protected Map map;
 	protected Position position;
 	protected boolean onGoal;
 
 	Block(Map m, Position pos) {
 		map = m;
 		position = pos;
		if (m.getGoals().contains(pos) {
 			onGoal = true;
 		}
 	}
 
 	public boolean isOnGoal() {
 		return onGoal;
 	}
 
 	public Position getPosition() {
 		return position;
 	}
 }
