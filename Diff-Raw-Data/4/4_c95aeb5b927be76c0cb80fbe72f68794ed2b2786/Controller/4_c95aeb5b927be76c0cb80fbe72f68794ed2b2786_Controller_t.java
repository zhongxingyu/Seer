 package pylos.controller;
 
 import pylos.Pylos;
import pylos.exception.PylosError;
 import pylos.model.Ball;
 import pylos.model.Model;
 import pylos.model.Position;
 import pylos.view.View;
 
 /**
  * The Pylos Controller.
  * Implements the singleton pattern by having everything static and no instance (the object is the class).
  */
 public abstract class Controller {
 	static View view;
 
 	public static void initialize(View view) {
 		Controller.view = view;
 		initTurn();
 	}
 
 	public static void updateView() {
 		view.board.drawBalls();
 		view.updatePositionBalls();
 	}
 
 	public static void initTurn() {
 		Model.currentPlayer.resetAction();
 	}
 
 	public static void finishTurn() {
 		if (Model.isWinner()) {
 			updateView();
 			Pylos.logger.info(Model.currentPlayer + " won");
 		} else {
 			nextTurn();
 		}
 	}
 
 	public static void placePlayerBall(Position position) {
 		Model.currentPlayer.putBallOnBoard(position);
 		finishTurn();
 	}
 
 	private static void nextTurn() {
 		Model.currentPlayer = (Model.currentPlayer == Model.player1) ? Model.player2 : Model.player1;
 		initTurn();
 		updateView();
 	}
 
 	public static void risePlayerBall(Ball ball) {
 		if (Model.getPositionsToRise(ball).isEmpty()) {
			throw new PylosError("Can not rise ball, there is no place to rise it.");
 		} else {
 			Model.currentPlayer.riseBall(ball);
 			view.updatePositionsToRise(ball);
 			updateView();
 		}
 	}
 }
