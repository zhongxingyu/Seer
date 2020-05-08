 ﻿package quoridor;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import quoridor.Move.MoveType;
 import util.Pair;
 import util.Two;
 
 
 /**
  * AI is called to generate a move based on game state and a smartness of a move.
  * 
  * <h2>Goals</h2>
  * <ul>
  * <li>Generates random, naive, or pro moves.</li>
  * </ul> 
  * 
  * <h2>Implementation</h2>
  * <ul>
  * <li>To be done at some point.</li>
  * </ul>
  * 
  * 
  * @author Sacha BÅ½raud <sacha.beraud@gmail.com>
  *
  */
 
 public class AI {
 
 	Game game;
 
 	public AI(Game game){
 		this.game = game;
 	}
 
 	public Move createMove(){
 		Move move = null;
 
 		if (game.myTurn().level().equals("random")) {
 			move = randomMove();
 		} else if (game.myTurn().level().equals("naive")) {
 			move = naiveMove();
 		} else if (game.myTurn().level().equals("pro")) {
 			move = proMove();
 		}
 
 		return move;
 	}
 
 	private Move randomMove() {
 		ArrayList<Move> possibleMoves = findPossibleMoves(game);
 		Random randomGenerator = new Random();
 		Move move = possibleMoves.get(randomGenerator.nextInt(possibleMoves.size()));
 
 		return move;
 	}
 
 	private Move naiveMove() {
 		ArrayList<Move> possibleMoves = findPossibleMoves(game);
 		Move move = null;
 		int myValue = game.shortestPath(game.myTurn()).size();
 		int playerShortestPath = game.shortestPath(game.players().other(game.myTurn())).size();
 
 		int highestValue = -999;
 		int index = 0;
 
 		//Find best move		
 		System.out.println("My path = " + game.shortestPath(game.myTurn()));
 		System.out.println("Their path = " + game.shortestPath(game.players.other(game.myTurn())));
 
 		if (myValue >= playerShortestPath - 1 && game.myTurn().wallsLeft() != 0) {
 			for (int i = 0; i < possibleMoves.size(); i++) {
 				int value = 0;
 				if (possibleMoves.get(i).direction() == MoveType.HORIZONTAL 
 						|| possibleMoves.get(i).direction() == MoveType.VERTICAL) {
 
 					Game tempGame = createTempGame(game.moves);
 
 					//add wall
 					tempGame.move(possibleMoves.get(i), tempGame.myTurn);
 
 					int tempShortest = tempGame.shortestPath(tempGame.myTurn).size();
 					int myTempShortest = tempGame.shortestPath(tempGame.players.other(tempGame.myTurn)).size();
 					//calculate value of each move
 					value = (tempGame.shortestPath(tempGame.myTurn).size())
 					- (myTempShortest - myValue)
 					- wallDistance(possibleMoves.get(i));
 
 					if (playerShortestPath == tempShortest) {
 						if (myValue == myTempShortest) {
 							value = 0;
 						} else {
 							value = value - 4;
 						}
 					}
 
 					//if reduces shortest path ignore distance 
 					if (isAdjacent(possibleMoves.get(i))) {
 						value++;
 					}
 
 					if (value > highestValue) {
 						highestValue = value;
 						index = i;
 					}
 				}
 				move = possibleMoves.get(index);
 			}	
 		} else {
 			move = game.shortestPath(game.myTurn()).get(1);
 			if (!game.isValid(move, game.myTurn())) {
 				move = game.shortestPath(game.myTurn()).get(2);
 				if (!game.isValid(move, game.myTurn())) {
 					//find best move comparing shortest path 
 					Game tempGame = createTempGame(game.moves);
 					Game tempGameTwo = createTempGame(game.moves);
 					
 					Point p = game.myTurn().pawn();
 					Move tempMove = new Move(p.x() + 1, p.y(), MoveType.PAWN);
 					Move tempMoveTwo = new Move(p.x() - 1, p.y(), MoveType.PAWN);
 					
 					if (tempGame.isValid(tempMove, tempGame.myTurn())) {
 						move = tempMove;
 						if (tempGame.isValid(tempMoveTwo, tempGame.myTurn())) {
 							tempGame.move(tempMove, game.myTurn());
 							tempGameTwo.move(tempMoveTwo, game.myTurn());
 							
 							if (tempGame.shortestPath(tempGame.players().other(tempGame.myTurn())).size() <
 								tempGameTwo.shortestPath(tempGameTwo.players().other(tempGameTwo.myTurn())).size())	{
 								move = tempMove;
 							} else {
 								move = tempMoveTwo;
 							}
 						}
 					} else if (tempGame.isValid(tempMoveTwo, tempGame.myTurn())) {
 						move = tempMoveTwo;
 					}
 				}
 			}
 		}
 
 		return move;
 	}	
 
 	private Move proMove() {
 		maxValue(game.moves, 2, -999999, +999999);
 		return null;
 	}
 
 	private int desiredDepth = 2;
 
 	private Pair<Integer, Move> maxValue(LinkedList<Move> moves, int currentSearchDepth, int alphaMax, int betaMin) {
 		Move bestMove = null;
 		int value;
 		if (currentSearchDepth == desiredDepth || isGoalState(moves)) {
 
 			//********** Needs implementing ***********//
 			Random random = new Random();
 			ArrayList<Move> m = findPossibleMoves(game);
 			return Pair.pair(random.nextInt(1000), m.get(random.nextInt(m.size())));
 			//return Heuristic(state), any move
 		}
 
 		Game tempGame = createTempGame(moves);
 		ArrayList<Move> moveList = findPossibleMoves(tempGame);
 
 		bestMove = moveList.get(0);
 
 		for (int i = 0; i < moveList.size(); i++) {
 			Game tempGameTwo = createTempGame(moves);
 			tempGameTwo.move(moveList.get(i), tempGame.myTurn());
 
			value = minValue(tempGameTwo.moves, currentSearchDepth + 1, alphaMax, betaMin);
 			if (value > alphaMax) {
 				alphaMax = value;
 				bestMove = moveList.get(i);
 			}
 
 			if (alphaMax >= betaMin) {				
 				return Pair.pair(alphaMax, bestMove);
 			}
 		}
 
 		return Pair.pair(alphaMax, bestMove);
 	}
 
 	private int minValue(LinkedList<Move> moves, int currentSearchDepth, int alphaMax, int betaMin) {
 		Pair<Integer, Move> value = null;
 		if (currentSearchDepth == desiredDepth || isGoalState(moves)) {
 			//********** Needs implementing ***********//
 			//return heuristic(state)
 			Random random = new Random();
 
 			return random.nextInt(1000);
 		}
 
 		Game tempGame = createTempGame(moves);
 		ArrayList<Move> moveList = findPossibleMoves(tempGame);
 		for (int i = 0; i < moveList.size(); i++) {
 			Game tempGameTwo = createTempGame(moves);
 			tempGameTwo.move(moveList.get(i), tempGame.myTurn());
 
			value = maxValue(tempGameTwo.moves, currentSearchDepth + 1, alphaMax, betaMin);
 			betaMin = Math.min(value._1, betaMin);
 			if (alphaMax >= betaMin) {
 				return betaMin;
 			}
 		}		
 
 		return betaMin;
 	}
 
 	private boolean isGoalState(LinkedList<Move> moves) {
 		for (int i = 0; i < 9; i++) {
 			if ((moves.getLast().coord().x() == i && moves.getLast().coord().y() == 1) 
 					|| (moves.getLast().coord().x() == i && moves.getLast().coord().y() == 9)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private boolean isAdjacent(Move move) {
 		Point player = game.players().other(game.myTurn()).pawn();
 		int direction;
 
 		if (game.myTurn().equals(game.players()._1())) {
 			direction = 1;
 		} else {
 			direction = -1;
 		}
 
 		if (direction == -1) {
 			if (move.coord().x() == player.x() && move.coord().y() == player.y() 
 					||	(move.coord().x() == player.x() - 1 && move.coord().y() == player.y()
 							&& move.direction() == MoveType.HORIZONTAL)
 							||	(move.coord().x() == player.x() && move.coord().y() == player.y() - 1 
 									&& move.direction() == MoveType.VERTICAL)
 									||	(move.coord().x() == player.x() + 1 && move.coord().y() == player.y() 
 											&& move.direction() == MoveType.VERTICAL)
 											||	(move.coord().x() == player.x() + 1 && move.coord().y() == player.y() - 1
 													&& move.direction() == MoveType.VERTICAL)) {
 
 				return true;
 			}
 		} else {
 			if ((move.coord().x() == player.x() && move.coord().y == player.y() - 1
 					&& move.direction() == MoveType.VERTICAL)
 					||	(move.coord().x() == player.x() && move.coord().y == player.y()
 							&& move.direction() == MoveType.VERTICAL)
 							||	(move.coord().x() == player.x() + 1 && move.coord().y == player.y() - 1
 									&& move.direction() == MoveType.VERTICAL)
 									||	(move.coord().x() == player.x() + 1 && move.coord().y == player.y()
 											&& move.direction() == MoveType.VERTICAL)
 											||	(move.coord().x() == player.x() - 1 && move.coord().y() == player.y()
 													&& move.direction() == MoveType.HORIZONTAL)
 													||	(move.coord().x() == player.x() && move.coord().y() == player.y() + 1
 															&& move.direction() == MoveType.HORIZONTAL)) {
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private int wallDistance(Move move) {
 		Point otherPlayer = game.players().other(game.myTurn()).pawn();
 
 		return (int) Math.sqrt(Math.pow((otherPlayer.x() - move.coord().x()), 2)
 				+ Math.pow((otherPlayer.y() - move.coord().y()), 2));
 	}
 
 	private Game createTempGame(LinkedList<Move> moves) {
 		Player tempPl1 = new Human("Player 1");
 		Player tempPl2 = new Human("Player 2");
 		Game tempGame = new Game(Two.two(tempPl1, tempPl2));
 
 		tempGame.initGame(null);
 		for (int j = 0; j < moves.size(); j++) {
 			tempGame.move(moves.get(j), tempGame.myTurn());
 		}
 
 		return tempGame;
 	}
 
 	private ArrayList<Move> findPossibleMoves(Game g) {
 		ArrayList<Move> possibleMoves = new ArrayList<Move>();
 		ArrayList<Move> checkList = new ArrayList<Move>();
 		Point current = g.myTurn.pawn();
 
 		checkList.add(new Move(current.x() + 1, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x() + 2, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() + 1, MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() + 2, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x() - 2, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() - 1, MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() - 2, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y() - 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() + 1, current.y() - 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y() + 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() + 1, current.y() + 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y() + 1, MoveType.PAWN));
 
 		//add walls to checklist
 		if (g.myTurn().wallsLeft() > 0) {
 			for (int i = 1; i < 9; i++) {
 				for (int j = 1; j < 9; j++) {
 					checkList.add(new Move(j, i, MoveType.HORIZONTAL));
 					checkList.add(new Move(j, i, MoveType.VERTICAL));
 				}
 			}
 		}
 
 		for (int i = 0; i < checkList.size(); i++) {
 			if (g.isValid(checkList.get(i), g.myTurn())) {
 				possibleMoves.add(checkList.get(i));
 			}
 		}		
 
 		return possibleMoves;
 	}
 
 }
