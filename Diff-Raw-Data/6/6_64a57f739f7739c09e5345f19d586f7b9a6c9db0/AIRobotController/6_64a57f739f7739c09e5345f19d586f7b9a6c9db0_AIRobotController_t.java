 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 public class AIRobotController {
 	private GameBoard gb;
 			
 	public AIRobotController(GameBoard gb) {
 		this.gb = gb;
 	}
 	
 	public void makeMove(Robot robot) {
 		List<Card> cards = robot.getCards();
 		
		for (int i = 0; i < cards.size(); i++) {
			
		}
 		
 		
 	}
 	
 	private int[] nextCheckPoint(Robot robot) {
 		int[] xy = new int[]{};
 		for(int i = 0; i < 1; i++) {
 			xy[i] = gb.getCheckPoints().get(robot.getLastCheckPoint()+1)[i]; //TODO will crash game if game is over and continues
 		}
 		return xy;
 	}
 	
 	private int[][] findPath() {
 		//Linus fixar TODO
 		return new int[][]{};
 	}
 	
 	
 	
 	
 	
 }
