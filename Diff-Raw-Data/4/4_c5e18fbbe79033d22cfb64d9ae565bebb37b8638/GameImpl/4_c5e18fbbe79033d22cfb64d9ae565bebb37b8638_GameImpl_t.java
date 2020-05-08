 package rps.game;
 
 import java.rmi.RemoteException;
 
 import org.hamcrest.core.IsInstanceOf;
 
 import rps.client.GameListener;
 import rps.game.data.AttackResult;
 import rps.game.data.Figure;
 import rps.game.data.FigureKind;
 import rps.game.data.Move;
 import rps.game.data.Player;
 
 /**
  * The {@code GameImpl} is an implementation for the {@code Game} interface. It
  * contains the necessary logic to play a game.
  */
 public class GameImpl implements Game {
 
 	private GameListener listener1;
 	private GameListener listener2;
 	
 	/**
 	 * not-null amount of elements in an assignment
 	 */
 	final private int ASSIGNMENT_ELEMENT_COUNT = 14;
 	
 	/**
 	 * game is surrendered?
 	 */
 	private boolean gameIsSurrendered = false;
 	
 	/**
 	 * the player moved last time
 	 */
 	private Player lastMovedPlayer;
 	
 	/**
 	 * the second player
 	 */
 	private Player player1;
 	
 	/**
 	 * the first player
 	 */
 	private Player player2;
 	
 	/**
 	 * has player1 provided an initial assignment?
 	 */
 	private boolean initialAssignmentProvidedByPlayer1 = false;
 	
 	/**
 	 * has player2 provided an initial assignment?
 	 */
 	private boolean initialAssignmentProvidedByPlayer2 = false;
 	
 	/**
 	 * initial choice of player1
 	 */
 	private FigureKind initialChoiceOfPlayer1;
 	
 	/**
 	 * initial choice of player2
 	 */
 	private FigureKind initialChoiceOfPlayer2;
 	
 	/**
 	 * choice of player1
 	 */
 	private FigureKind choiceOfPlayer1;
 	
 	/**
 	 * choice of player2
 	 */
 	private FigureKind choiceOfPlayer2;
 	
 	/**
 	 * the board
 	 */
 	private Figure[] board = new Figure[42];
 	
 	/**
 	 * the last move
 	 */
 	private Move lastMove;
 
 	public GameImpl(GameListener listener1, GameListener listener2) throws RemoteException {
 		this.listener1 = listener1;
 		this.listener2 = listener2;
 		this.player1 = listener1.getPlayer();
 		this.player2 = listener2.getPlayer();
 	}
 
 	@Override
 	public void sendMessage(Player p, String message) throws RemoteException {
 		listener1.chatMessage(p, message);
 		listener2.chatMessage(p, message);
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * set initial assignment of a player
 	 */
 	@Override
 	public void setInitialAssignment(Player p, FigureKind[] assignment) throws RemoteException {
 		if(p.equals(this.player1) && this.initialAssignmentProvidedByPlayer1
 		|| p.equals(this.player2) && this.initialAssignmentProvidedByPlayer2) {
 			throw new IllegalStateException("Initial assignment was already given!");
 		}
 		
 		// check length of assignment
 		if(assignment.length != this.board.length) {
 			throw new IllegalArgumentException("Initial assignment length is wrong!");
 		}
 		
 		// check used figure kinds
 		int countRock = 0, countPaper = 0, countScissors = 0, countTrap = 0, countFlag = 0;
 		for(FigureKind currentAssignmentItem: assignment) {
 			if(currentAssignmentItem == FigureKind.ROCK) {
 				countRock += 1;
 			} else if(currentAssignmentItem == FigureKind.PAPER) {
 				countPaper += 1;
 			} else if(currentAssignmentItem == FigureKind.SCISSORS) {
 				countScissors += 1;
 			} else if(currentAssignmentItem == FigureKind.TRAP) {
 				countTrap += 1;
 			} else if(currentAssignmentItem == FigureKind.FLAG) {
 				countFlag += 1;
 			} 
 		}
 		if(countRock != 4 || countPaper != 4 || countScissors != 4 || countTrap != 1 || countFlag != 1) {
 			throw new IllegalArgumentException("Initial assignment has wrong composition of kinds");
 		}
 		
 		// save the assignment
 		for(int i=0; i < this.board.length; i++) {
 			// skip null elements
 			if(assignment[i] instanceof FigureKind) {
 				this.board[i] = new Figure(assignment[i], p);
 			}
 		}
 		
 		if(p.equals(this.player1)) {
 			this.initialAssignmentProvidedByPlayer1 = true;
 		} else {
 			this.initialAssignmentProvidedByPlayer2 = true;
 		}
 		
 		if(this.initialAssignmentProvidedByPlayer1 && this.initialAssignmentProvidedByPlayer2) {
 			this.listener1.provideInitialChoice();
 			this.listener2.provideInitialChoice();
 		}
 	}
 
 	/**
 	 * set initial choice of a player
 	 */
 	@Override
 	public void  setInitialChoice(Player p, FigureKind kind) throws RemoteException {
 		// save initial choices
 		if(p.equals(this.player1)) {
 			this.initialChoiceOfPlayer1 = kind;
 		} else {
 			this.initialChoiceOfPlayer2 = kind;
 		}
 		
 		// compare choices if available
 		if(this.initialChoiceOfPlayer1 != null && this.initialChoiceOfPlayer2 != null) {
 			AttackResult result = this.initialChoiceOfPlayer1.attack(this.initialChoiceOfPlayer2);
 			
 			if(result == AttackResult.WIN) {
 				this.listener1.provideNextMove();
 			} else if(result == AttackResult.LOOSE) {
 				this.listener2.provideNextMove();
 			} else if(result == AttackResult.DRAW) {
 				this.listener1.provideInitialChoice();
 				this.listener2.provideInitialChoice();
 			}
 			
 			// inform listeners about the game start
 			this.listener1.startGame();
 			this.listener2.startGame();
 		}
 	}
 
 	/**
 	 * do a move
 	 */
 	@Override
 	public void move(Player movingPlayer, int fromIndex, int toIndex) throws RemoteException {
 		if(movingPlayer.equals(this.lastMovedPlayer) 
 		&& this.movableFiguresLeftByPlayer(this.getOpponent(movingPlayer))) {
 			throw new IllegalStateException("Can't move two times");
 		}
 		
 		// save this move as last move
 		this.lastMove = new Move(fromIndex, toIndex, this.board.clone());
 		
 		// save moving player
 		this.lastMovedPlayer = movingPlayer;
 		
 		if(this.board[toIndex] == null) { // normal move
 			// inform listeners
 			this.listener1.figureMoved();
 			this.listener2.figureMoved();
 						
 			// perform move
 			this.board[toIndex] = this.board[fromIndex];
 			this.board[fromIndex] = null;
 			
 			// provide next move
 			if(movingPlayer.equals(this.player1)) {
 				this.listener2.provideNextMove();
 			} else {
 				this.listener1.provideNextMove();
 			}
 		} else { // attack
 			// inform listeners
 			this.listener1.figureAttacked();
 			this.listener2.figureAttacked();
 			
 			// get result
 			AttackResult result;
 			GameListener offender, defender;
 			
 			Figure from = this.board[fromIndex];
 			Figure to = this.board[toIndex];
 			
 			result = from.getKind().attack(to.getKind());
 			
 			if(from.belongsTo(this.player1)) {
 				offender = this.listener1;
 				defender = this.listener2;
 			} else {
				offender = this.listener2;
				defender = this.listener1;
 			}
 						
 			// evaluate result
 			// update board
 			if(result == AttackResult.WIN_AGAINST_FLAG) { // game is over
 				offender.gameIsWon();
 				defender.gameIsLost();
 			} else if(result == AttackResult.DRAW) { // provide choices
 				this.listener1.provideChoiceAfterFightIsDrawn();
 				this.listener2.provideChoiceAfterFightIsDrawn();
 			} else if(result == AttackResult.WIN) { // perform move and kill target
 				this.board[toIndex] = this.board[fromIndex];
 				this.board[fromIndex] = null;
 				
 				if(this.movableFiguresLeft()) {
 					this.provideNextMove(movingPlayer);
 				} else {
 					this.informAboutGameDrawn();
 				}
 			} else if(result == AttackResult.LOOSE) { // kill source and keep target
 				this.board[fromIndex] = null;
 				
 				if(this.movableFiguresLeft()) {
 					this.provideNextMove(movingPlayer);
 				} else {
 					this.informAboutGameDrawn();
 				}
 			} else if(result == AttackResult.LOOSE_AGAINST_TRAP) { // kill source and target
 				this.board[fromIndex] = null;
 				this.board[toIndex] = null;
 				
 				if(this.movableFiguresLeft()) {
 					this.provideNextMove(movingPlayer);
 				} else {
 					this.informAboutGameDrawn();
 				}
 			}
 		}	
 	}
 
 	@Override
 	public void setUpdatedKindAfterDraw(Player p, FigureKind kind) throws RemoteException {
 		// save choices
 		if(p.equals(this.player1)) {
 			this.choiceOfPlayer1 = kind;
 		} else {
 			this.choiceOfPlayer2 = kind;
 		}
 		
 		// compare choices if available
 		if(this.choiceOfPlayer1 != null && this.choiceOfPlayer2 != null) {
 			int indexFrom = this.getLastMove().getFrom();
 			int indexTo = this.getLastMove().getTo();
 			
 			// get result
 			AttackResult result;
 			GameListener offender, defender;
 			Figure offenderFigure, defenderFigure;
 			Player offenderPlayer, defenderPlayer;
 			
 			if(this.board[indexFrom].belongsTo(this.player1)) {
 				result = this.choiceOfPlayer1.attack(this.choiceOfPlayer2);
 				offender = this.listener1;
 				defender = this.listener2;
 				offenderFigure = new Figure(this.choiceOfPlayer1, this.player1);
 				offenderFigure.setDiscovered();
 				defenderFigure = new Figure(this.choiceOfPlayer2, this.player2);
 				defenderFigure.setDiscovered();
 				offenderPlayer = this.player1;
 				defenderPlayer = this.player2;
 			} else {
 				result = this.choiceOfPlayer2.attack(this.choiceOfPlayer1);
 				offender = this.listener1;
 				defender = this.listener2;
 				offenderFigure = new Figure(this.choiceOfPlayer2, this.player2);
 				offenderFigure.setDiscovered();
 				defenderFigure = new Figure(this.choiceOfPlayer1, this.player1);
 				defenderFigure.setDiscovered();
 				offenderPlayer = this.player2;
 				defenderPlayer = this.player1;
 			}
 			
 			// evaluate result
 			if(result == AttackResult.WIN) { // do move and kill target
 				this.board[indexTo] = offenderFigure;
 				this.board[indexFrom] = null;
 				
 				this.provideNextMove(offenderPlayer);
 			} else if(result == AttackResult.LOOSE) { // kill source
 				this.board[indexTo] = defenderFigure;
 				this.board[indexFrom] = null;
 				
 				this.provideNextMove(offenderPlayer);
 			} else if(result == AttackResult.DRAW) {
 				this.listener1.provideChoiceAfterFightIsDrawn();
 				this.listener2.provideChoiceAfterFightIsDrawn();
 			}
 			
 			// inform listeners about attack
 			if(result != AttackResult.DRAW) {
 				this.listener1.figureAttacked();
 				this.listener2.figureAttacked();
 			}
 			
 			// update last move
 			Figure[] oldBoard = this.getLastMove().getOldField();
 			oldBoard[indexFrom] = offenderFigure;
 			oldBoard[indexTo] = defenderFigure;
 			this.lastMove = new Move(indexFrom, indexTo, oldBoard);
 			
 			this.choiceOfPlayer1 = null;
 			this.choiceOfPlayer2 = null;
 		}
 	}
 
 	@Override
 	public void surrender(Player p) throws RemoteException {
 		if(this.gameIsSurrendered) {
 			throw new IllegalStateException("Both players can't surrender!");
 		}
 		
 		listener1.chatMessage(p, "I surrender");
 		listener2.chatMessage(p, "I surrender");
 		
 		if(p.equals(this.player1)) {
 			this.listener1.gameIsLost();
 			this.listener2.gameIsWon();
 		} else {
 			this.listener1.gameIsWon();
 			this.listener2.gameIsLost();
 		}
 		
 		this.gameIsSurrendered = true;
 	}
 
 	/**
 	 * get board
 	 */
 	@Override
 	public Figure[] getField() throws RemoteException {
 		return this.board;
 	}
 
 	@Override
 	public Move getLastMove() throws RemoteException {
 		return this.lastMove;
 	}
 
 	/**
 	 * get opponent by player
 	 */
 	@Override
 	public Player getOpponent(Player p) throws RemoteException {
 		if(p.equals(this.player1)) {
 			return this.player2;
 		} else {
 			return this.player1;
 		}
 	}
 	
 	/**
 	 * informs all listeners that the game is drawn
 	 * @throws RemoteException 
 	 */
 	private void informAboutGameDrawn() throws RemoteException {
 		this.listener1.gameIsDrawn();
 		this.listener2.gameIsDrawn();
 	}
 	
 	private void provideNextMove(Player p) throws RemoteException {
 		if(p.equals(this.player1)) {
 			this.listener2.provideNextMove();
 		} else {
 			this.listener1.provideNextMove();
 		}
 	}
 	
 	/**
 	 * checks whether any movable figures are left
 	 * 
 	 * @return
 	 */
 	private boolean movableFiguresLeft() {
 		for(Figure figure: this.board) {
 			if(figure instanceof Figure) {
 				if(figure.getKind().isMovable()) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean movableFiguresLeftByPlayer(Player p) {
 		for(Figure figure: this.board) {
 			if(figure instanceof Figure) {
 				if(figure.getKind().isMovable() && figure.belongsTo(p)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 }
