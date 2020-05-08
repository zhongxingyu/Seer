 public class SimpleReflexAgent extends Agent {
 	private Direction dir;
 	private boolean forward;
 
 	public SimpleReflexAgent(Environment env) {
 		super(env);
 		forward = true;
 	}
 
 	public void move() {
 		super.move();
 		if (forward) {
 			if (row % 2 == 0) {
 				dir = Direction.RIGHT;
 			} else {
 				dir = Direction.LEFT;
 			}
 			switch (dir) {
 			case LEFT: {
 				if (col > 0) {
 					col--;
 				} else if (row < Environment.HEIGHT - 1) {
 					row++;
 				} else {
 					forward = false;
 				}
 				break;
 			}
 			case RIGHT: {
 				if (col < Environment.WIDTH - 1) {
 					col++;
 				} else if (row < Environment.HEIGHT - 1) {
 					row++;
 				} else {
 					forward = false;
 				}
 				break;
 			}
 			}
 		} else {
 			if (row % 2 == 0) {
 				dir = Direction.LEFT;
 			} else {
 				dir = Direction.RIGHT;
 			}
 			switch (dir) {
 			case LEFT: {
 				if (col > 0) {
 					col--;
 				} else if (row > 0) {
 					row--;
 				} else {
 					forward = true;
 				}
 				break;
 			}
 			case RIGHT: {
 				if (col < Environment.WIDTH - 1) {
 					col++;
 				} else if (row > 0) {
 					row--;
 				} else {
 					forward = true;
 				}
 				break;
 			}
 			}
 		}
 	}
 }
