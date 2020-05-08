 package eighteen;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import eighteen.Piece.adjLoc;
 
 public class Board {
 
 	public static class BadMoveException extends Exception {
 		private static final long serialVersionUID = -4508322448788975541L;
 
 		public BadMoveException(String message){
 			super(message);
 		}
 	}
 	
 	public static class BadBoardException extends Exception {
 		private static final long serialVersionUID = -1512647492285542624L;
 
 		public BadBoardException() {
 			super("Board doesn't have allowable number of rows or columns");
 		}
 	}
 	
 	public static class GameOverException extends Exception {
 		private static final long serialVersionUID = 8578520222249569915L;
 
 		public GameOverException(String winner) {
 			super("Game over with winner " + winner);
 		}
 	}
 	
 	Piece[][] theBoard;
 	
 	//The number of whites and blacks
 	int blacks;
 	int whites;
 	
 	//The number of moves made to reach this point
 	int moves;
 	//Whether or not a chain is active
 	boolean chain;
 	//What Color is being chained
 	Color chainColor;
 	Piece.adjLoc previousSpot;
 	Direction previousDirection;
 	//The previous spots moved to in this chain
 	ArrayList<Piece.adjLoc> previousLocations;
 	//The moves in this chain
 	ArrayList<Move> chainMoves;
 	
 	Color turn;
 	
 	//Static as only one game is played at a time
 	public static int ROWS;
 	public static int COLUMNS;
 	public static int MAXMOVES;
 	
 	public Board() throws BadBoardException {
 		this(5,13);
 	}
 	
 	public Board(int rows, int columns) throws BadBoardException {
 		resetBoard(rows, columns);
 	}
 	
 	//Copy constructor
 	public Board(Board b) {
 		theBoard = new Piece[ROWS][COLUMNS];
 		for(int i = 0; i < ROWS; i++) {
 			for(int j = 0; j < COLUMNS; j++) {
 				theBoard[i][j] = new Piece(b.theBoard[i][j]);
 			}
 		}
 		blacks = b.blacks;
 		whites = b.whites;
 		moves = b.moves;
 		chain = b.chain;
 		chainColor = b.chainColor;
 		previousSpot = b.previousSpot;
 		previousDirection = b.previousDirection;
 		previousLocations = new ArrayList<Piece.adjLoc>(b.previousLocations);
 		chainMoves = new ArrayList<Move>(b.chainMoves);
 		turn = b.turn;
 	}
 	
 	public Piece getPiece(int x, int y) {
 		return theBoard[x][y];
 	}
 	
 	//Utility value of the board
 	public double Utility() {
 		double ret = ((double)whites) / ((double)(blacks + whites));
 		ret *= 200;
 		ret -= 100;
 		return ret;
 	}
 	
 	private void resetBoard(int rows, int columns) throws BadBoardException {
 		if((rows < 1 || rows > 13) || (columns < 1 || columns > 13) || (rows % 2 == 0) || (columns % 2 == 0)) {
 			throw new BadBoardException();
 		}
 		ROWS = rows;
 		COLUMNS = columns;
 		MAXMOVES = ROWS * 10;
 		theBoard = new Piece[ROWS][COLUMNS];
 		for(int i = 0; i < ROWS; i++) {
 			for(int j = 0; j < COLUMNS; j++) {
 				if(i < ROWS/2) {
 					theBoard[i][j] = new Piece(i, j, Color.BLACK);
 				}
 				else {
 					theBoard[i][j] = new Piece(i, j, Color.WHITE);
 				}
 			}
 		}
 		//The middle row
 		for(int j = 0; j < COLUMNS; j++) {
 			if(j == COLUMNS/2) {
 				theBoard[ROWS/2][j] = new Piece(ROWS/2, j, Color.GRAY);
 			}
 			else if(j < COLUMNS/2) {
 				if(j % 2 == 0) {
 					theBoard[ROWS/2][j] = new Piece(ROWS/2, j, Color.WHITE);
 				}
 				else {
 					theBoard[ROWS/2][j] = new Piece(ROWS/2, j, Color.BLACK);
 				}
 			}
 			else {
 				if(j % 2 == 0) {
 					theBoard[ROWS/2][j] = new Piece(ROWS/2, j, Color.BLACK);
 				}
 				else {
 					theBoard[ROWS/2][j] = new Piece(ROWS/2, j, Color.WHITE);
 				}
 			}
 		}
 		moves = 0;
 		chain = false;
 		//The first mover is white, so we want it to start a new chain, thus it is BLACK
 		chainColor = Color.BLACK;
 		//0,0 will never be a problem for a first move in the middle, which will reset it
 		previousSpot = new Piece.adjLoc(0, 0);
 		//As white moves first, this will be reset and not be a problem
 		previousDirection = Direction.LEFT;
 		previousLocations = new ArrayList<Piece.adjLoc>();
 		whites = (rows * columns) / 2;
 		blacks = whites;
 		//Thus the first move call will increment moves to 1
 		chainColor = Color.BLACK;
 		chainMoves = new ArrayList<Move>();
 		//White is the first to move
 		turn = Color.WHITE;
 	}
 	
 	public Color switchTurn() {
 		chain = false;
 		turn = oppositeColor(turn);
 		//System.out.println("Changing turns...");
 		previousLocations.clear();
 		previousDirection = null;
 		//chainMoves = new ArrayList<Move>();
 		for(int i = 0; i < ROWS; i++) {
 			for(int j = 0; j < COLUMNS; j++) {
 				if(theBoard[i][j].getColor().equals(Color.GREEN)) {
 					theBoard[i][j].setColor(Color.RED);
 				}
 				if(theBoard[i][j].getColor().equals(Color.RED)) {
 					theBoard[i][j].setColor(Color.GRAY);
 				}
 			}
 		}
 		return turn;
 	}
 	
 	//Returns whether the game is over
 	public ArrayList<Piece> move(Move mov) throws BadMoveException, GameOverException {
 		if(!isValidMove(mov)) {
 			throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] to [" + mov.getEnd().row + ", " + mov.getEnd().column + "]");
 		}
 		
 		boolean isChain = true;
 		
 		//Checks for errors in chaining
 		if(mov.getStart().getColor() != chainColor/* || mov.getState() == AttackState.NEITHER || mov.getState() == AttackState.SACRIFICE*/) {
 			//In this case the turn changed, so change the chain to false
 			isChain = false;
 			previousLocations = new ArrayList<Piece.adjLoc>();
 			chainColor = mov.getStart().getColor();
 			chainMoves = new ArrayList<Move>();
 			moves++;
 		}
 		chain = isChain;
 		if(chain) {
 			for(Piece.adjLoc prev: previousLocations) {
 				if(prev.equals(mov.end)) {
 					System.out.println("BAD");
 					throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] -> That space has already been moved to in this chain");
 				}
 			}
 			if(!(new Piece.adjLoc(mov.getStart())).equals(previousSpot)) {
 				throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] -> Wrong starting spot");
 			}
 			else if(previousDirection == mov.getDirection()) {
 				throw new BadMoveException("Bad move at [" + mov.getStart().row + ", " + mov.getStart().column + "] -> Same direction as previous move in chain");
 			}
 		}
 		
 		int iterateVertical = 0;
 		int iterateHorizontal = 0;
 		switch(mov.getDirection()) {
 		case UP:
 			iterateVertical = -1;
 			break;
 		case UPRIGHT:
 			iterateVertical = -1;
 			iterateHorizontal = 1;
 			break;
 		case UPLEFT:
 			iterateVertical = -1;
 			iterateHorizontal = -1;
 			break;
 		case DOWN:
 			iterateVertical = 1;
 			break;
 		case DOWNRIGHT:
 			iterateVertical = 1;
 			iterateHorizontal = 1;
 			break;
 		case DOWNLEFT:
 			iterateVertical = 1;
 			iterateHorizontal = -1;
 			break;
 		case LEFT:
 			iterateHorizontal = -1;
 			break;
 		case RIGHT:
 			iterateHorizontal = 1;
 		}
 		
 		if(mov.getState() == AttackState.WITHDRAWING) {
 			iterateVertical *= -1;
 			iterateHorizontal *= -1;
 		}
 		
 		
 		// Finds the pieces being removed, and adds the updated pieces so the GUI can change them
 		ArrayList<Piece> ret = new ArrayList<Piece>();
 		if(mov.getState() == AttackState.ADVANCING || mov.getState() == AttackState.WITHDRAWING) {
 			//Keeps track of which piece we are looking at
 			int nextRow;
 			int nextColumn;
 			if(mov.getState() == AttackState.ADVANCING) {
 				nextRow = mov.getEnd().row;
 				nextColumn = mov.getEnd().column;
 			}
 			else {
 				nextRow = mov.getStart().row;
 				nextColumn = mov.getStart().column;
 			}
 			try {
 				while(true) {
 					nextRow += iterateVertical;
 					nextColumn += iterateHorizontal;
 					if(!Piece.isValidSpace(nextRow, nextColumn)) {
 						break;
 					}
 					if(theBoard[nextRow][nextColumn].getColor() != oppositeColor(mov.getColor())) {
 						break;
 					}
 					//System.out.println("Something should die!");
 					theBoard[nextRow][nextColumn].setColor(Color.GRAY);
 					if(chainColor == Color.WHITE) {
 						blacks--;
 					}
 					else {
 						whites--;
 					}
 					ret.add(theBoard[nextRow][nextColumn]);
 				}
 			}
 			finally {}
 		}
 		else if(mov.getState() == AttackState.SACRIFICE) {
 			mov.getStart().setColor(Color.GREEN);
 		}
 		// Update all the variables
 		chainColor = mov.getColor();
 		previousSpot = mov.getEnd();
 		previousDirection = mov.getDirection();
 		previousLocations.add(new Piece.adjLoc(mov.getStart()));
 		chainMoves.add(mov);
 		if(mov.state == AttackState.NEITHER)
 			chain = false;
 		else
 			chain = true;
 		
 		//Done later to not mess with mov.getColor
 		//Move the actual piece
 		if(mov.getState() != AttackState.SACRIFICE) {
 			theBoard[mov.getEnd().row][mov.getEnd().column].setColor(mov.getStart().getColor());
 		}
 		theBoard[mov.getStart().row][mov.getStart().column].setColor(Color.GRAY);
 		
 		if(getValidChainMoves(mov.getEnd()).size() == 0 || mov.getState() == AttackState.NEITHER || mov.getState() == AttackState.SACRIFICE) {
 			switchTurn();
 		}
 		
 		if(whites == 0) {
 			throw new GameOverException("black");
 		}
 		if(blacks == 0) {
 			throw new GameOverException("white");
 		}
 		if(moves > MAXMOVES) {
 			String winner = "";
 			if(whites == 0) {
 				if(blacks == 0) {
 					winner = "tie";
 				}
 				else {
 					winner = "black";
 				}
 			}
 			else {
 				winner = "white";
 			}
 			throw new GameOverException(winner);
 		}
 		
 		return ret;
 	}
 	
 	public boolean isValidMove(Move mov) {
 		// Space is taken
 		if(getPiece(mov.getEnd().row, mov.getEnd().column).getColor() != Color.GRAY) {
 			return false;
 		}
 		// Can't travel the same direction twice
 		if(previousDirection == mov.getDirection())
 			return false;
 		if(Piece.isValidSpace(mov.getStart().row, mov.getStart().column) && !mov.getStart().equals(mov.getEnd())) {
 			for(Piece.adjLoc prev: previousLocations) {
 				// Can't move to it's own spot
 				if(prev.equals(mov.getEnd())) {
 					return false;
 				}
 			}
 			// The ending point must be adjacent to the start point
 			for(Piece.adjLoc p : mov.getStart().adjacentLocations)
 			{
 				if(p.column == mov.getEnd().column && p.row == mov.getEnd().row) 
 				{
 					return true;
 				}
 			}
 		}
 		System.out.println("Not valid space or start equaled end.");
 		return false;
 	}
 	
 	// Finds an attack state of a possible move
 	public AttackState isAdvancing(Move mov) {
 		int iterateVertical = mov.getEnd().row - mov.getStart().row;
 		int iterateHorizontal = mov.getEnd().column - mov.getStart().column;
 		Piece.adjLoc nextSpace = new Piece.adjLoc(mov.getEnd().row + iterateVertical, mov.getEnd().column + iterateHorizontal);
 		Piece.adjLoc previousSpace = new Piece.adjLoc(mov.getStart().row - iterateVertical, mov.getStart().column - iterateHorizontal);
 		if(Piece.isValidSpace(nextSpace)) {
 			if(theBoard[nextSpace.row][nextSpace.column].getColor() == oppositeColor(mov.getColor())) {
 				if(Piece.isValidSpace(previousSpace)) {
 					if(theBoard[previousSpace.row][previousSpace.column].getColor() == oppositeColor(mov.getColor())) {
 						return AttackState.BOTH;
 					}
 					else {
 						return AttackState.ADVANCING;
 					}
 				}
 				else {
 					return AttackState.ADVANCING;
 				}
 			}
 			else if(Piece.isValidSpace(previousSpace)) {
 				if(theBoard[previousSpace.row][previousSpace.column].getColor() == oppositeColor(mov.getColor())) {
 					return AttackState.WITHDRAWING;
 				}
 			}
 		}
 		else if(Piece.isValidSpace(previousSpace)) {
 			if(theBoard[previousSpace.row][previousSpace.column].getColor() == oppositeColor(mov.getColor())) {
 				return AttackState.WITHDRAWING;
 			}
 		}
 		return AttackState.NEITHER;
 	}
 	
 	// Gets all valid chain moves for a specific point
 	public ArrayList<Move> getValidChainMoves(adjLoc place) throws BadMoveException {
 		return getValidChainMoves(place.row, place.column);
 	}
 	
 	public ArrayList<Move> getValidChainMoves(int x, int y) throws BadMoveException {
 		ArrayList<Move> capture = new ArrayList<Move>();
 		Piece start = getPiece(x,y);
 		
 		// In case the point doesn't have a piece there
 		if(start.isEmpty()) {
 			return capture;
 		}
 		
 		// Local move to get the direction of each potential move
 		Move move = new Move();
 		move.setStart(start);
 		for(Piece.adjLoc end: start.adjacentLocations) {
 			// Updates the direction
 			for(Piece.adjLoc previous: previousLocations) {
 				if(previous.equals(end)) {
 					continue;
 				}
 			}
 			move.setEnd(end);
 			move.updateDirection();
 			if(move.getDirection() == previousDirection) {
 				continue;
 			}
 			int rowAdv = 0;
 			int rowWd = 0;
 			int colAdv = 0;
 			int colWd = 0;
 			if(isValidMove(move)) {
 				Piece advance;
 				Piece withdraw;
 				// Sets the locations of the pieces for an advance and a withdraw
 				switch(move.getDirection()) {
 				case UP:
 					rowAdv = -2;
 					colAdv = 0;
 					rowWd = 1;
 					colWd = 0;
 					break;
 				case UPRIGHT:
 					rowAdv = -2;
 					colAdv = 2;
 					rowWd = 1;
 					colWd = -1;
 					break;
 				case UPLEFT:
 					rowAdv = -2;
 					colAdv = -2;
 					rowWd = 1;
 					colWd = 1;
 					break;
 				case DOWN:
 					rowAdv = 2;
 					colAdv = 0;
 					rowWd = -1;
 					colWd = 0;
 					break;
 				case DOWNRIGHT:
 					rowAdv = 2;
 					colAdv = 2;
 					rowWd = -1;
 					colWd = -1;
 					break;
 				case DOWNLEFT:
 					rowAdv = 2;
 					colAdv = -2;
 					rowWd = -1;
 					colWd = 1;
 					break;
 				case LEFT:
 					rowAdv = 0;
 					colAdv = -2;
 					rowWd = 0;
 					colWd = 1;
 					break;
 				case RIGHT:
 					rowAdv = 0;
 					colAdv = 2;
 					rowWd = 0;
 					colWd = -1;
 					break;
 				}
 				// Accounts for if an advance AND a withdraw are possible
 				if(Piece.isValidSpace(start.row + rowAdv, start.column + colAdv) && Piece.isValidSpace(start.row + rowWd, start.column + colWd)) {
 					advance = theBoard[start.row + rowAdv][start.column + colAdv];
 					withdraw = theBoard[start.row + rowWd][start.column + colWd];
 					if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 						Move newMove = new Move(start, end, AttackState.ADVANCING);
 						if(isValidMove(newMove))
 							capture.add(newMove);
 					}
 					if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 						Move newMove = new Move(start, end, AttackState.WITHDRAWING);
 						if(isValidMove(newMove))
 							capture.add(newMove);
 					}
 				}
 				// Just an advance is possible
 				else if(Piece.isValidSpace(start.row + rowAdv, start.column + colAdv)) {
 					advance = theBoard[start.row + rowAdv][start.column + colAdv];
 					if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 						Move newMove = new Move(start, end, AttackState.ADVANCING);
 						if(isValidMove(newMove))
 							capture.add(newMove);
 					}
 				}
 				// Just a withdraw is possible
 				else if(Piece.isValidSpace(start.row + rowWd, start.column + colWd)) {
 					withdraw = theBoard[start.row + rowWd][start.column + colWd];
 					if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 						Move newMove = new Move(start, end, AttackState.WITHDRAWING);
 						if(isValidMove(newMove))
 							capture.add(newMove);
 					}
 				}
 			}
 		}
 		return capture;
 	}
 	
 	// Gets valid moves for a specific color on the entire board
 	public ArrayList<Move> getValidMoves(Color color) throws BadMoveException {
 		ArrayList<Move> capture = new ArrayList<Move>();
 		ArrayList<Move> paika = new ArrayList<Move>();
 		
 		for(int x=0; x < Board.ROWS; x++) {
 			for(int y=0; y < Board.COLUMNS; y++) {
 				Piece piece = getPiece(x, y);
 				Move move = new Move();
 				// It is the color we are checking, find valid moves
 				if(piece.getColor() == color) {
 					move.setStart(piece);
 					for(Piece.adjLoc end: piece.adjacentLocations) {
 						// Can't move to a spot you've already been to
 						if(previousLocations.contains(end)) {
 							continue;
 						}
 						move.setEnd(end);
 						move.updateDirection();
 						// Can't move the same direction twice in a row
 						if(move.getDirection() == previousDirection) {
 							continue;
 						}
 						int rowAdv = 0;
 						int rowWd = 0;
 						int colAdv = 0;
 						int colWd = 0;
 						// Move checking is valid
 						if(isValidMove(move)) {
 							Piece advance;
 							Piece withdraw;
 							switch(move.getDirection()) {
 							case UP:
 								rowAdv = -2;
 								colAdv = 0;
 								rowWd = 1;
 								colWd = 0;
 								break;
 							case UPRIGHT:
 								rowAdv = -2;
 								colAdv = 2;
 								rowWd = 1;
 								colWd = -1;
 								break;
 							case UPLEFT:
 								rowAdv = -2;
 								colAdv = -2;
 								rowWd = 1;
 								colWd = 1;
 								break;
 							case DOWN:
 								rowAdv = 2;
 								colAdv = 0;
 								rowWd = -1;
 								colWd = 0;
 								break;
 							case DOWNRIGHT:
 								rowAdv = 2;
 								colAdv = 2;
 								rowWd = -1;
 								colWd = -1;
 								break;
 							case DOWNLEFT:
 								rowAdv = 2;
 								colAdv = -2;
 								rowWd = -1;
 								colWd = 1;
 								break;
 							case LEFT:
 								rowAdv = 0;
 								colAdv = -2;
 								rowWd = 0;
 								colWd = 1;
 								break;
 							case RIGHT:
 								rowAdv = 0;
 								colAdv = 2;
 								rowWd = 0;
 								colWd = -1;
 								break;
 							}
 							// Advance and withdraw are possible
 							if(Piece.isValidSpace(piece.row + rowAdv, piece.column + colAdv) && Piece.isValidSpace(piece.row + rowWd, piece.column + colWd)) {
 								advance = theBoard[piece.row + rowAdv][piece.column + colAdv];
 								withdraw = theBoard[piece.row + rowWd][piece.column + colWd];
 								if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 									Move newMove = new Move(piece, end, AttackState.ADVANCING);
 									capture.add(newMove);
 								}
 								if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 									Move newMove = new Move(piece, end, AttackState.WITHDRAWING);
 									capture.add(newMove);
 								}
 							}
 							// Just an advance
 							else if(Piece.isValidSpace(piece.row + rowAdv, piece.column + colAdv)) {
 								advance = theBoard[piece.row + rowAdv][piece.column + colAdv];
 								if(advance.getColor() != move.getColor() && !advance.isEmpty()) {
 									Move newMove = new Move(piece, end, AttackState.ADVANCING);
 									capture.add(newMove);
 								}
 							}
 							// Just a withdraw
 							else if(Piece.isValidSpace(piece.row + rowWd, piece.column + colWd)) {
 								withdraw = theBoard[piece.row + rowWd][piece.column + colWd];
 								if(withdraw.getColor() != move.getColor() && !withdraw.isEmpty()) {
 									Move newMove = new Move(piece, end, AttackState.WITHDRAWING);
 									capture.add(newMove);
 								}
 							}
 							// No capture move are possible
 							if(capture.isEmpty()) {
 								paika.add(new Move(piece, end, AttackState.NEITHER));
 								//paika.add(new Move(piece, end, AttackState.SACRIFICE));
 							}
 							//else 
 								//capture.add(new Move(piece, end, AttackState.SACRIFICE));
 						}
 					}
 				}
 			}
 		}
 		if(!capture.isEmpty())
 			return capture;
 		else
 			return paika;
 	}
 	
 	// Finds the opposite color
 	public static Color oppositeColor(Color color) {
 		if(color == Color.GRAY) {
 			return Color.GRAY;
 		}
 		else if(color == Color.WHITE) {
 			return Color.BLACK;
 		}
 		else {
 			return Color.WHITE;
 		}
 	}
 	
 	
 	/*public boolean equals(Board b) {
 		if(blacks == b.blacks && whites == b.whites && moves == b.moves && chain == b.chain) {
 			if(chainColor == b.chainColor && previousSpot.equals(b.previousSpot)) {
 				if(previousDirection == b.previousDirection && previousLocations.equals(b.previousLocations)) {
 					System.out.println("Same previousDirection and locations");
 					if(chainMoves.equals(b.chainMoves) && turn.equals(b.turn)) {
 						for(int i = 0; i < ROWS; i++) {
 							if(!Arrays.equals(theBoard[i], b.theBoard[i])) {
 								return false;
 							}
 						}
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}*/
 	
 	public boolean equals(Board b) {
 		for(int x=0; x < ROWS; x++) 
 			for(int y=0; y < COLUMNS; y++)
 				if(!theBoard[x][y].equals(b.theBoard[x][y]))
 					return false;
 		return true;
 	}
 	
 	// Displays a board for debugging
 	public void print() {
 		for(int x=0; x < Board.ROWS; x++) {
 			for(int y=0; y < Board.COLUMNS; y++) {
 				Color color = theBoard[x][y].getColor();
 				if(color == Color.BLACK)
 					System.out.print("B ");
 				if(color == Color.WHITE)
 					System.out.print("W ");
 				if(color == Color.GRAY)
 					System.out.print("G ");
 			}
 			System.out.println();
 		}
 	}
 }
