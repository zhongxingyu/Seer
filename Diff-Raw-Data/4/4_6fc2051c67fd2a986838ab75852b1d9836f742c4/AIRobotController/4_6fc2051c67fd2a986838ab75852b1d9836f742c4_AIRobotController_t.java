 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.BoardElement;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Move;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Turn;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.TurnType;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Hole;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 public class AIRobotController {
 	private GameBoard gb;
 	private List<Card> chosenCards;
 
 	public AIRobotController(GameBoard gb) {
 		this.gb = gb;
 	}
 
 	public void makeMove(Robot robot) {
 		List<Card> cards = new ArrayList<Card>();
 		chosenCards = new ArrayList<Card>();
 		cards.addAll(robot.getCards());
 		placeCards(robot, cards);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void placeCards(Robot robot, List<Card> cards) {
 		if (chosenCards.size() == 5) {
 			robot.setChosenCards(chosenCards);
 			robot.setSentCards(true);
 			return;
 		}
 		List<Move> moveForwardCards = new ArrayList<Move>();
 		List<Move> moveBackwardCards = new ArrayList<Move>();
 		List<Turn> turnCards = new ArrayList<Turn>();
 		List<Turn> leftTurnCards = new ArrayList<Turn>();
 		List<Turn> rightTurnCards = new ArrayList<Turn>();
 		List<Turn> uTurnCards = new ArrayList<Turn>();
 
 		boolean isRightDirection = false;
 		for (Integer direction : getDirections(robot)) { // Check if the robot stand in a correct direction
 			if (robot.getDirection() == direction) {
 				isRightDirection = true;
 			}
 		}
 		if (isRightDirection) {
 			for (Card card : cards) {
 				if (card instanceof Move) {
 					if (((Move)card).getDistance() > 0) {
 						moveForwardCards.add((Move)card);
 					}
 				}
 			}
 			if (moveForwardCards.size() != 0) { // Move forward as long as possible
 				Collections.sort(moveForwardCards);
 				chosenCards.add(moveForwardCards.get(0));
 				cards.remove(moveForwardCards.get(0));
 			} else { // if there are no move forwards cards -> random card 
 				randomizeCard(robot, cards);
 			}
 			placeCards(robot, cards);
 			return;
 		}
 		else { // If the robot is turned towards a wrong direction.
 			for (Card card : cards) {
 				if (card instanceof Turn) {
 					if(((Turn)card).getTurn() == TurnType.LEFT){
 						leftTurnCards.add((Turn)card);
 					}else if(((Turn)card).getTurn() == TurnType.RIGHT){
 						rightTurnCards.add((Turn)card);
 					}else {
 						uTurnCards.add((Turn)card);
 					}
 				}
 			}
 			if (turnCards.size() != 0) { // Try turn towards a correct direction
 				for(Integer i : getDirections(robot)){
 					boolean cardAdded = false;
 					int turnDifference = Math.abs(i.intValue() - 
 							robot.getDirection());
 					if(turnDifference == 1){
 						if(leftTurnCards.size() != 0){
 							chosenCards.add(leftTurnCards.get(0));
 							cards.remove(leftTurnCards.get(0));
 							cardAdded = true;
 						}
 					}else if(turnDifference == 2){
 						if(uTurnCards.size() != 0){
 							chosenCards.add(uTurnCards.get(0));
 							cards.remove(uTurnCards.get(0));
 							cardAdded = true;
 						}
 					}else if(turnDifference == 3){
 						if(rightTurnCards.size() != 0){
 							chosenCards.add(rightTurnCards.get(0));
 							cards.remove(rightTurnCards.get(0));
 							cardAdded = true;
 						}
 					}
 					if(!cardAdded){
 						for(Card card : cards){
 							if(!(card instanceof Move)){
 								chosenCards.add(card);
 								cards.remove(card);
 							}
 						}
 					}
 					placeCards(robot, cards);
 					return;
 				}
 				
 			} else { // No turn cards -> random card
 				randomizeCard(robot, cards);
 				return;
 			}
 			placeCards(robot, cards);
 			return;
 		}
 	}
 
 	private int[] nextCheckPoint(Robot robot) {
		int[] xy = new int[2];
		for(int i = 0; i <= 1; i++) {
 			xy[i] = gb.getCheckPoints().get(robot.getLastCheckPoint()+1)[i]; //TODO will crash game if game is over and continues
 		}
 		return xy;
 	}
 
 	private int getDistanceToNextCheckPoint(Robot robot){
 		int distance = 0;
 		int[] nextCheckPoint = nextCheckPoint(robot);
 		distance = Math.abs(robot.getX() - nextCheckPoint[0]);
 		distance += Math.abs(robot.getY() - nextCheckPoint[1]);
 		return distance;
 	}
 
 	private Card nextCard(Robot robot){
 
 		return null;
 	}
 
 	private List<Integer> getDirections(Robot robot){
 		List<Integer> directions = new ArrayList<Integer>();
 		int dx = getDX(robot);
 		int dy = getDY(robot);
 		if(dx < 0){
 			directions.add(new Integer(GameBoard.EAST));
 		}else if(dx > 0){
 			directions.add(new Integer(GameBoard.WEST));
 		}
 		if(dy < 0){
 			directions.add(new Integer(GameBoard.SOUTH));
 		}else if(dy > 0){
 			directions.add(new Integer(GameBoard.NORTH));
 		}
 		removeBadDirections(robot, directions);
 
 
 		if(directions.size() == 2){
 			if(Math.abs(dx) < Math.abs(dy)){
 				directions.add(directions.remove(0));
 			}
 		}
 		return directions;
 	}
 
 	private void removeDirection(List<Integer> directions, Integer indexToRemove){
 		if(directions.size() == 1){
 			directions.add(new Integer((directions.get(indexToRemove) + 1) % 4));
 			directions.add(new Integer((directions.get(indexToRemove) + 3) % 4));
 		}
 		directions.remove(indexToRemove);
 	}
 
 	private List<Integer> removeBadDirections(Robot robot, List<Integer> directions){
 		int x = robot.getX();
 		int y = robot.getY();
 		for(int i = 0; i < directions.size(); i++){
 			if(directions.get(i).intValue() == GameBoard.NORTH){
 				for(int j = 0; j < 3; j++){
 					if((y-j) >= 0){
 						for(BoardElement boardElement : gb.getTile(x, (y-j)).getBoardElements()){
 							if(boardElement instanceof Hole){
 								removeDirection(directions, i);
 								break;
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}else if(directions.get(i).intValue() == GameBoard.EAST){
 				for(int j = 0; j < 3; j++){
 					if((x+j) <= gb.getWidth()){
 						for(BoardElement boardElement : gb.getTile((x+j), y).getBoardElements()){
 							if(boardElement instanceof Hole){
 								removeDirection(directions, i);
 								break;
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}else if(directions.get(i).intValue() == GameBoard.SOUTH){
 				for(int j = 0; j < 3; j++){
 					if((y+j) <= gb.getHeight()){
 						for(BoardElement boardElement : gb.getTile(x, (y+j)).getBoardElements()){
 							if(boardElement instanceof Hole){
 								removeDirection(directions, i);
 								break;
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}else if(directions.get(i).intValue() == GameBoard.WEST){
 				for(int j = 0; j < 3; j++){
 					if((x-j) >= 0){
 						for(BoardElement boardElement : gb.getTile((x-j), y).getBoardElements()){
 							if(boardElement instanceof Hole){
 								removeDirection(directions, i);
 								break;
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}
 		}
 		return directions;
 	}
 
 	private int getDX(Robot robot){
 		return (robot.getX() - nextCheckPoint(robot)[0]);
 	}
 
 	private int getDY(Robot robot){
 		return (robot.getY() - nextCheckPoint(robot)[1]);
 	}
 
 
 	private void randomizeCard(Robot robot, List<Card> cards) {
 		Random rand = new Random();
 		int index = rand.nextInt(cards.size());
 		Card randChosenCard = cards.get(index);
 		chosenCards.add(randChosenCard);
 		cards.remove(index);
 	}
 
 }
