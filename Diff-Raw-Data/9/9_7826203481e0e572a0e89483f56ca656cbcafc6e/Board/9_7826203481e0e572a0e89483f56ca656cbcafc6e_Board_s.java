 import java.util.HashSet;
 import java.util.Vector;
 
 public class Board implements Comparable<Board> {
 	// Dimensions of board.
 	protected int width;
 	protected int height;
 	protected Symbol[] cells;
 	private Vector<Integer> boxes = new Vector<Integer>();
 	private Vector<Integer> goals = new Vector<Integer>();
 	// Save string path while searching through the game tree.
 	protected String path;
 
 	public int f, g; // Total and accumulated cost
 
 	// Current player position
 	protected int playerPos = -1;
 
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
 			for (int k = 0; k < row.length(); k++) {
 				cells[rowMul+k] = Symbol.fromChar(row.charAt(k));
 				if (cells[rowMul+k] == Symbol.PLAYER || cells[rowMul+k] == Symbol.PLAYER_GOAL)
 					playerPos = rowMul+k;
 				if (cells[rowMul + k] == Symbol.BOX
 						|| cells[rowMul + k] == Symbol.BOX_GOAL)
 					boxes.add(rowMul + k);
 				if (cells[rowMul + k] == Symbol.GOAL
 						|| cells[rowMul + k] == Symbol.BOX_GOAL
 						|| cells[rowMul + k] == Symbol.PLAYER_GOAL)
 					goals.add(rowMul+k);
 				
 			}
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
 		this.playerPos = board.playerPos;
 		this.boxes = new Vector<Integer>(board.boxes);
 		this.goals = new Vector<Integer>(board.goals);
 		move(dir);
 	}
 
 	/**
 	 * Initialize board data for this board instance.
 	 * The cells argument should be clone()d if the board is to be copied.
 	 */
 	private void initBoard(Symbol[] cells, int width, int height, String path) {
 		// Set data
 		this.cells = new Symbol[cells.length];
 		System.arraycopy(cells, 0, this.cells, 0, cells.length);
 		this.width = width;
 		this.height = height;
 		this.path = path;
 	}
 
 	/**
 	 * Moves the player piece on this board, updating the position of it,
 	 * and any box it collides with.
 	 */
 	private void move(Direction dir) {
 		cells[playerPos] = (cells[playerPos] == Symbol.PLAYER_GOAL) ? Symbol.GOAL : Symbol.FLOOR;
 		
 		int newPos = translatePos(playerPos, dir);
 		int dst = translatePos(newPos, dir);
 		
 		if (cells[newPos] == Symbol.BOX || cells[newPos] == Symbol.BOX_GOAL) {
 			cells[dst] = (cells[dst] == Symbol.GOAL) ? Symbol.BOX_GOAL : Symbol.BOX;
 			boxes.remove((Object) newPos);
 			boxes.add(dst);
 			cells[newPos] = (cells[newPos] == Symbol.BOX) ? Symbol.PLAYER : Symbol.PLAYER_GOAL;
 		} else {
 			cells[newPos] = (cells[newPos] == Symbol.GOAL) ? Symbol.PLAYER_GOAL : Symbol.PLAYER;
 		}
 
 		playerPos = newPos;
 		path += dir.toString();
 	}
 
 	/**
 	 * Returns a Vector instance with all possible player moves
 	 * (as Direction instances) that are valid on this board.
 	 */
 	public Vector<Direction> findPossibleMoves() {
 		Vector<Direction> moves = new Vector<Direction>(4);
 
 		for (Direction dir : Direction.values()) {
 			int to = translatePos(playerPos, dir);
 			if (isEmptyCell(to) ||
 					((at(to) == Symbol.BOX  || at(to) == Symbol.BOX_GOAL) &&
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
 
 	/**
 	 * Returns true if this board represents a finished game,
 	 * where all boxes are placed on goals.
 	 */
 	public boolean isWin() {
 		for (Symbol s : cells)
 			if (s == Symbol.BOX)
 				return false;
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int h = 0, len = cells.length;
 		for (int i = 0; i < len; ++i) {
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
 	
 	public Symbol[] getCells() {
 		return this.cells;
 	}
 	
 	public String getPath() {
 		return this.path;
 	}
 	
 	/**
 	 * Returns a string representation of this board.
 	 */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < cells.length; i++) {
 			if (i > 0 && i % width == 0) sb.append('\n');
 			if (cells[i] != null)
 				sb.append(cells[i]);
 			else
 				sb.append(" ");
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Outputs the string representation of this board to the console.
 	 */
 	public void write() {
 		System.out.println(toString());
 	}
 
 	public int heuristic() {
 		int h = 0;
 
 		HashSet<Integer> foundGoals = new HashSet<Integer>();
 		// BOX DISTANCES
 		for (int i : boxes) {
 			int d = -1;
 			for (int j : goals) {
 				int t = Math.abs(i % width - j % width)
 						+ Math.abs(i / width - j / width);
 				if (d == -1 && !foundGoals.contains(j)) {
 					d = t;
 					foundGoals.add(j);
 				} else {
 					if (d > t && !foundGoals.contains(j)) {
 						d = t;
 						foundGoals.add(j);
 					}
 				}
 			}
 			// PLAYER DISTANCES
 			h += (cells[i] == Symbol.BOX_GOAL) ? d : Math.abs(i % width
 					- playerPos
 					% width)
 					+ Math.abs(i / width - playerPos / width) + d;
 		}
 
 		return h;
 	}
 
 	public int compareTo(Board other) {
 		return this.f - other.f;
 	}
 }
