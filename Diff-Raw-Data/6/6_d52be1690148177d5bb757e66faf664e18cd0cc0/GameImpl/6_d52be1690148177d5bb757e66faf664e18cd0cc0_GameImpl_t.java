 package rps.game;
 
 import java.rmi.RemoteException;
 
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
 		if (message.equals("iwanttowinthisgamenow")){  //Added a little cheatcode
 			for(int i=0; i<this.board.length; i++) {
 				if(this.board[i] != null) {
 					this.board[i].setDiscovered();
 				}
 			}
 			if(p == this.player1){
 				listener1.gameIsWon();
 				listener2.gameIsLost();
 			}
 			else{
 				listener2.gameIsWon();
 				listener1.gameIsLost();
 			}
 		}
 		else{
 		listener1.chatMessage(p, message);
 		listener2.chatMessage(p, message);
 		}
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
 			
 			this.initialChoiceOfPlayer1 = null;
 			this.initialChoiceOfPlayer2 = null;
 			
 			if(result != AttackResult.DRAW) {
 				// inform listeners about the game start
 				this.listener1.startGame();
 				this.listener2.startGame();
 			}
 		}
 	}
 
 	/**
 	 * do a move
 	 */
 	@Override
 	public void move(Player movingPlayer, int fromIndex, int toIndex) throws RemoteException {
 		if(movingPlayer.equals(this.lastMovedPlayer) 
 		&& movableFiguresLeftByPlayer(this.board, getOpponent(movingPlayer))) {
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
 			if(movableFiguresLeftByPlayer(this.board, getOpponent(movingPlayer))) {
 				provideNextMove(getOpponent(movingPlayer));
 			} else {
 				provideNextMove(movingPlayer);
 			}
 		} else { // attack
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
 			
 			// discover lastMove figures
 			Figure[] oldBoard = this.lastMove.getOldField();
 			int indexFrom = this.lastMove.getFrom();
 			int indexTo = this.lastMove.getTo();
 			
 			if(oldBoard[indexFrom] != null) {
 				oldBoard[indexFrom].setDiscovered();
 			}
 			
 			if(oldBoard[indexTo] != null) {
 				oldBoard[indexTo].setDiscovered();
 			}
 						
 			// evaluate result
 			// update board
 			if(result == AttackResult.WIN_AGAINST_FLAG) { // game is over
 				offender.gameIsWon();
 				defender.gameIsLost();
 				
 				for(int i=0; i<this.board.length; i++) {
 					if(this.board[i] != null) {
 						this.board[i].setDiscovered();
 					}
 				}
 			} else if(result == AttackResult.DRAW) { // provide choices
 				this.listener1.provideChoiceAfterFightIsDrawn();
 				this.listener2.provideChoiceAfterFightIsDrawn();
 			} else if(result == AttackResult.WIN) { // perform move and kill target
 				this.board[toIndex] = this.board[fromIndex];
 				this.board[fromIndex] = null;
 				
 				if(this.movableFiguresLeftByPlayer(this.board, getOpponent(movingPlayer))) {
 					this.provideNextMove(getOpponent(movingPlayer));
 				} else {
 					this.provideNextMove(movingPlayer);
 				}
 			} else if(result == AttackResult.LOOSE) { // kill source and keep target
 				this.board[fromIndex] = null;
 				
 				this.provideNextMove(getOpponent(movingPlayer));
 			} else if(result == AttackResult.LOOSE_AGAINST_TRAP) { // kill source and target
 				
 				this.board[fromIndex] = null;
 				this.board[toIndex] = null;
 				
 				if(this.movableFiguresLeftByPlayer(this.board, getOpponent(movingPlayer))) {
 					this.provideNextMove(getOpponent(movingPlayer));
 				} else if(movableFiguresLeft(this.board)) {
 					this.provideNextMove(movingPlayer);
 				} else {
 					this.informAboutGameDrawn();
 				}
 			}
 			
 			// inform listeners
 			this.listener1.figureAttacked();
 			this.listener2.figureAttacked();
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
 			Figure offenderFigure, defenderFigure;
 			Player defenderPlayer;
 			
 			if(this.board[indexFrom].belongsTo(this.player1)) {
 				result = this.choiceOfPlayer1.attack(this.choiceOfPlayer2);
 				offenderFigure = new Figure(this.choiceOfPlayer1, this.player1);
 				offenderFigure.setDiscovered();
 				defenderFigure = new Figure(this.choiceOfPlayer2, this.player2);
 				defenderFigure.setDiscovered();
 				defenderPlayer = this.player2;
 			} else {
 				result = this.choiceOfPlayer2.attack(this.choiceOfPlayer1);
 				offenderFigure = new Figure(this.choiceOfPlayer2, this.player2);
 				offenderFigure.setDiscovered();
 				defenderFigure = new Figure(this.choiceOfPlayer1, this.player1);
 				defenderFigure.setDiscovered();
 				defenderPlayer = this.player1;
 			}
 			
 			// evaluate result
 			if(result == AttackResult.WIN) { // do move and kill target
 				this.board[indexTo] = offenderFigure;
 				this.board[indexFrom] = null;
 				
 				this.provideNextMove(defenderPlayer);
 			} else if(result == AttackResult.LOOSE) { // kill source
 				this.board[indexTo] = defenderFigure;
 				this.board[indexFrom] = null;
 				
 				this.provideNextMove(defenderPlayer);
 			} else if(result == AttackResult.DRAW) {
 				this.listener1.provideChoiceAfterFightIsDrawn();
 				this.listener2.provideChoiceAfterFightIsDrawn();
				
				this.board[indexFrom] = offenderFigure;
				this.board[indexTo] = defenderFigure;
 			}
 			
 			// update last move
			Figure[] oldBoard = this.board.clone();
 			oldBoard[indexFrom] = offenderFigure;
 			oldBoard[indexTo] = defenderFigure;
 			this.lastMove = new Move(indexFrom, indexTo, oldBoard);
 			
 			this.choiceOfPlayer1 = null;
 			this.choiceOfPlayer2 = null;
 			
 			this.listener1.figureAttacked();
 			this.listener2.figureAttacked();
 		}
 	}
 
 	@Override
 	public void surrender(Player p) throws RemoteException {
 		if(this.gameIsSurrendered) {
 			throw new IllegalStateException("Both players can't surrender!");
 		}
 		
 		listener1.chatMessage(p, "Ich gebe auf");
 		listener2.chatMessage(p, "Ich gebe auf");
 		
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
 			this.listener1.provideNextMove();
 		} else {
 			this.listener2.provideNextMove();
 		}
 	}
 	
 	/**
 	 * checks whether any movable figures are left
 	 * 
 	 * @return
 	 * @throws RemoteException 
 	 */
 	private boolean movableFiguresLeft(Figure[] board) throws RemoteException {
 		return movableFiguresLeftByPlayer(board, this.player1) || movableFiguresLeftByPlayer(board, this.player2);
 	}
 	
 	private boolean movableFiguresLeftByPlayer(Figure[] board, Player p) throws RemoteException {
 		for(int i=0; i<board.length; i++) {
 			if(board[i] != null) {
 				if(board[i].belongsTo(p)) {
 					// blocked in the left-down corner
 					if(i == 0 
 					&& rightFieldIsOwnUnmovableFigure(i, p) 
 					&& underFieldIsOwnUnmovableFigure(i, p)) {
 						continue;
 					}
 				
 					// blocked in the right-down corner
 					if(i == 6 
 					&& leftFieldIsOwnUnmovableFigure(i, p) 
 					&& underFieldIsOwnUnmovableFigure(i, p)) {
 						continue;
 					}
 					
 					// blocked in the left-up corner
 					if(i == 35 
 					&& rightFieldIsOwnUnmovableFigure(i, p) 
 					&& aboveFieldIsOwnUnmovableFigure(i, p)) {
 						continue;
 					}
 					
 					// blocked in the right-up corner
 					if(i == 41 
 					&& leftFieldIsOwnUnmovableFigure(i, p) 
 					&& aboveFieldIsOwnUnmovableFigure(i, p)) {
 						continue;
 					}
 					
 					if(board[i].getKind().isMovable()) {
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Returns whether the field left of i blocks i to move there.
 	 * 
 	 * @param i
 	 * @param p
 	 * @return
 	 * @throws RemoteException
 	 */
 	private boolean leftFieldIsOwnUnmovableFigure(int i, Player p) throws RemoteException {
 		Figure figure = null;
 		try {
 			figure = board[i-1];
 		} catch(IndexOutOfBoundsException e) {
 			return false;
 		}
 		
 		return (figure != null && figure.belongsTo(p) && !figure.getKind().isMovable());
 	}
 	
 	/**
 	 * Returns whether the field right of i blocks i to move there.
 	 * 
 	 * @param i
 	 * @param p
 	 * @return
 	 * @throws RemoteException
 	 */
 	private boolean rightFieldIsOwnUnmovableFigure(int i, Player p) throws RemoteException {
 		Figure figure = null;
 		try {
 			figure = board[i+1];
 		} catch(IndexOutOfBoundsException e) {
 			return false;
 		}
 		
 		return (figure != null && figure.belongsTo(p) && !figure.getKind().isMovable());
 	}
 	
 	/**
 	 * Returns whether the field above i blocks i to move there.
 	 * 
 	 * @param i
 	 * @param p
 	 * @return
 	 * @throws RemoteException
 	 */
 	private boolean aboveFieldIsOwnUnmovableFigure(int i, Player p) throws RemoteException {
 		Figure figure = null;
 		try {
 			figure = board[i-7];
 		} catch(IndexOutOfBoundsException e) {
 			return false;
 		}
 		
 		return (figure != null && figure.belongsTo(p) && !figure.getKind().isMovable());
 	}
 	
 	/**
 	 * Returns whether the field under i blocks i to move there.
 	 * 
 	 * @param i
 	 * @param p
 	 * @return
 	 * @throws RemoteException
 	 */
 	private boolean underFieldIsOwnUnmovableFigure(int i, Player p) throws RemoteException {
 		Figure figure = null;
 		try {
 			figure = board[i+7];
 		} catch(IndexOutOfBoundsException e) {
 			return false;
 		}
 		
 		return (figure != null && figure.belongsTo(p) && !figure.getKind().isMovable());
 	}
 }
