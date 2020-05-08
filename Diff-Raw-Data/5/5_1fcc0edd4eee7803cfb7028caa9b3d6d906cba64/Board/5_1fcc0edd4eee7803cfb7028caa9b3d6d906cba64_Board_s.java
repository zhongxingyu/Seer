 import java.util.Vector;
 
 public class Board {
 	// Dimensions of board.
 	protected int width, height;
 	protected Symbol[] cells;
 	
 	// Save string path while searching through the game tree.
 	protected String path;
 
 	// Current player position
 	protected int player;
 
 	/**
 	 * Create new board with a predefined layout.
 	 */
 	Board(Symbol[] cells, int width, int height) {
 		initBoard(cells, width, height, "");
 	}
 
 	/**
 	 * Create new board with a predefined layout defined by a string.
 	 */
 	Board(String boardRep, int width, int height) {
 		cells = new Symbol[width*height];
 
 		int rowMul = 0;
 		for (String row : boardRep.split("\n")) {
 			for (int k = 0; k < row.length(); k++)
 				cells[rowMul+k] = Symbol.fromChar(row.charAt(k));
 			rowMul += width;
 		}
 
 		initBoard(cells, width, height, "");
 	}
 
 	/**
 	 * Create new board with a predefined layout and immidiately move the
 	 * player in the specified direction.
 	 */
 	Board(Board board, Direction dir) {
 		initBoard(board.getCells(), board.getWidth(), board.getHeight(), board.getPath());
 		move(dir);
 	}
 
 	/**
 	 * Initialize board data for this board instance.
 	 * The cells argument should be clone()d if the board is to be copied.
 	 */
 	private void initBoard(Symbol[] cells, int width, int height, String path) {
 		// Set data
		this.cells = cells;
 		this.width = width;
 		this.height = height;
 		this.path = path;
 
 		// Set player position
 		player = -1;
 		for (int i = 0; i < cells.length; i++)
 			if (cells[i] == Symbol.PLAYER || cells[i] == Symbol.PLAYER_GOAL)
 				player = i;
 	}
 
 	/**
 	 * Moves the player piece on this board, updating the position of it,
 	 * and any box it collides with.
 	 */
 	public Board move(Direction dir) {
 		// Restore cell at old player position
 		cells[player] = (at(player) == Symbol.PLAYER_GOAL) ? Symbol.GOAL : Symbol.FLOOR;
 		player = translatePos(player, dir);
 
 		Symbol to = at(player);
 
 		// TODO: try/catch array index out of bounds
 		// Move box?
 		if (to == Symbol.BOX || to == Symbol.BOX_GOAL) {
 			int boxDest = translatePos(player, dir);
 			cells[boxDest] = (at(boxDest) == Symbol.GOAL) ? Symbol.BOX_GOAL : Symbol.BOX;
 		}
 
 		// Update cell at new player position
 		cells[player] = (to == Symbol.GOAL) ? Symbol.PLAYER_GOAL : Symbol.PLAYER;
 
 		// Append move to path
 		path += dir.toString();
 
 		return this;
 	}
 
 	/**
 	 * Returns a Vector instance with all possible player moves
 	 * (as Direction instances) that are valid on this board.
 	 */
 	public Vector<Direction> findPossibleMoves() {
 		Vector<Direction> moves = new Vector<Direction>(4);
 		for (Direction dir : Direction.values()) {
 			int to = translatePos(player, dir);
 			if (isEmptyCell(to) ||
 					(at(to) == Symbol.BOX &&
 					isEmptyCell(translatePos(to, dir))))
 				moves.add(dir);
 		}
 		return moves;
 	}
 
 	/**
 	 * Returns true if the cell at the specified position is empty,
 	 * and a valid target for movement.
 	 */
 	public boolean isEmptyCell(int pos) {
 		Symbol c = at(pos);
 		if (c == Symbol.FLOOR || c == Symbol.GOAL)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Returns a position translated in a specified direction.
 	 */
 	public int translatePos(int pos, Direction dir) {
 		switch (dir) {
 		case UP:
 			return (pos - this.width);
 		case DOWN:
 			return (pos + this.width);
 		case LEFT:
 			return (pos - 1);
 		case RIGHT:
 			return (pos + 1);
 		}
 		return pos;
 	}
 
 	/**
 	 * Returns the symbol located at the specified position.
 	 */
 	public Symbol at(int pos) {
 		if (pos < 0 || pos >= cells.length)
 			return Symbol.WALL;
 		return cells[pos];
 	}
 
 	/**
 	 * Returns the symbol located in the specified direction from a position.
 	 */
 	public Symbol at(int pos, Direction dir) {
 		return at(translatePos(pos, dir));
 	}
 
 
 	public boolean isWin(){
 		for (Symbol s : cells)
 			if (s == Symbol.BOX) return false;
 				return true;
 	}
 
 	/**
 	 * Returns true if this board represents a finished game,
 	 * where all boxes are placed on goals.
 	 */
 	public boolean isEOG() {
 		for (Symbol s : cells)
 			if (s == Symbol.BOX) return false;
 				return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int h = 0;
 		for(int i = 0; i < cells.length; i++) {
 			h ^= ( h << 5 ) + ( h >> 2 ) + cells[i].toChar();
 		}
 		return h;
 	}
 
 	public int getWidth() {
 		return this.width;
 	}
 
 	public int getHeight() {
 		return this.height;
 	}
 
 	public String getPath() {
 		return this.path;
 	}
 
 	public Symbol[] getCells() {
 		return this.cells;
 	}
 
 	/**
 	 * Returns a string representation of this board.
 	 */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < cells.length; i++) {
 			if (i > 0 && i % width == 0) sb.append('\n');
 			sb.append(cells[i]);
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Outputs the string representation of this board to the console.
 	 */
 	public void write() {
 		System.out.println(toString());
 	}
 }
