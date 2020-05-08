 package sokoban;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import sokoban.ReachableBox;
 
 /**
  *
  */
 public class Board implements Cloneable
 {
     // Input values
     /**
      * Value for a WALL on the board
      */
     public final static byte WALL = 0x01;
     /**
      * Value for a BOX on the board
      */
     public final static byte BOX = 0x02;
     /**
      * Value for a GOAL position on the board
      */
     public final static byte GOAL = 0x04;
 
     // Generated values
     /**
      * Boxes will get stuck in this square
      */
     public final static byte BOX_TRAP = 0x08;
     /**
      * The player has already passed this cell the since last move
      */
     public final static byte VISITED = 0x10;
     /**
      * The player can reach this square.
      */
     public final static byte REACHABLE = 0x20;
 
     // Bitmasks
     /**
      * A bitmask that says that a cell can't be walked into when
      * pushing, but not pulling, is allowed.
      */
     public final static byte REJECT_WALK = WALL | VISITED;
     /**
      * A bitmask that says that a cell can't be walked into when
      * pulling, but not pushing, is allowed.
      */
     public final static byte REJECT_PULL = WALL | BOX;
     /**
      * A bitmask that says that a box can't be moved into the cell, for one or
      * more of the following reasons:
      * <ul>
      * <li>The cell is a wall.</li>
      * <li>The cell contains another box.</li>
      * <li>The cell is a box trap, meaning that the box could never be moved
      * away from there.</li>
      * </ul>
      */
     public final static byte REJECT_BOX = WALL | BOX | BOX_TRAP;
 
     /**
      * Don't move boxes here at any time!
      */
     public final static byte WALL_OR_TRAP = WALL | BOX_TRAP;
 
     /**
      * All four allowed moves: { row, column }
      */
     public static final int moves[][] = { { -1, 0 }, { 1, 0 }, { 0, -1 },
             { 0, 1 } };
 
     public enum Direction {
         UP, DOWN, LEFT, RIGHT;
 
         private static final Direction[] reverse = { DOWN, UP, RIGHT, LEFT };
 
         public Direction reverse()
         {
             return reverse[this.ordinal()];
         }
     }
 
     /**
      * The width of the board
      */
     public final int width;
     /**
      * The height of the board
      */
     public final int height;
 
     /**
      * A bitmask for the input cell values.
      */
     public final static byte INPUT_CELL_MASK = WALL | GOAL | BOX;
 
     /**
      * The actual board
      */
     public byte cells[][];
 
     /**
      * Objects representing all positions on the board
      */
     public final Position positions[][];
 
     /**
      * The column at which the player resides
      */
     private int playerCol;
 
     /**
      * The row at which the player resides
      */
     private int playerRow;
 
     public int boxCount;
     private int remainingBoxes;
 
     private Collection<Position> reachableBoxes;
     private boolean boxesNeedsUpdate;
 
     /**
      * The topmost, leftmost square the player can reach. Please update with
      * updateReachability() after the board has changed and before it's
      * hashed.
      * 
      * Use getTopLeftReachable instead of accessing this variable.
      */
     private int topLeftReachable;
     private boolean topLeftNeedsUpdate;
 
     private long zobristKey;
 
     /**
      * Constructs a new board from the given string representation.
      * 
      * NOTE: Please use the Board(byte[]) constructor if the board is already
      * available as an byte array.
      * 
      * @param boardString A string representation of the board to construct.
      */
     public Board(String boardString)
     {
         this(boardString.getBytes());
     }
 
     /**
      * Constructs a new board from the given array of character bytes.
      * 
      * @param boardBytes An array of character bytes that describes the board.
      */
     public Board(byte[] boardBytes)
     {
         int boardWidth = 0;
         int boardHeight = 1; // board input string doesn't end with '\n'
         int boardPlayerCol = 0;
         int boardPlayerRow = 0;
         int rowLength = 0;
         for (int i = 0; i < boardBytes.length; ++i) {
             rowLength++;
             switch (boardBytes[i]) {
                 case '\n':
                     if (rowLength > boardWidth) {
                         boardWidth = rowLength - 1; // '\n' not part of board
                     }
                     rowLength = 0;
                     ++boardHeight;
                     break;
                 case '@':
                 case '+':
                     // Player position is 0-indexed.
                     boardPlayerCol = rowLength - 1;
                     boardPlayerRow = boardHeight - 1;
                     break;
             }
         }
 
         cells = new byte[boardHeight][boardWidth];
         positions = new Position[boardHeight][boardWidth];
 
         for (int row = 0; row < boardHeight; ++row) {
             for (int col = 0; col < boardWidth; ++col) {
                 positions[row][col] = new Position(row, col);
             }
         }
 
         int row = 0;
         int col = 0;
         for (int i = 0; i < boardBytes.length; ++i, ++col) {
             switch (boardBytes[i]) {
                 case '\n':
                     // col is incremented before first char on every row
                     col = -1;
                     ++row;
                     break;
                 case '#':
                     cells[row][col] = Board.WALL;
                     break;
                 case '$':
                     cells[row][col] = Board.BOX;
                     break;
                 case '+':
                     cells[row][col] = Board.GOAL;
                     break;
                 case '*':
                     cells[row][col] = Board.BOX | Board.GOAL;
                     break;
                 case '.':
                     cells[row][col] = Board.GOAL;
                     break;
             }
         }
 
         this.width = boardWidth;
         this.height = boardHeight;
         this.playerCol = boardPlayerCol;
         this.playerRow = boardPlayerRow;
         this.zobristKey = Zobrist.calculateHashTable(this);
 
         boxesNeedsUpdate = true;
         topLeftNeedsUpdate = true;
 
         countBoxes();
         markNonBoxSquares();
         updateReachability(true);
     }
 
     /**
      * Constructs the position that is placed one step in the given direction
      * (move) from the given position.
      * 
      * @param pos The position to start from.
      * @param move Should be a 2-length array with move[0]==row and
      *            move[1]==column.
      * @return The position corresponding to this position, after the movement
      */
     public Position getPosition(Position pos, int[] move)
     {
         if (!contains(pos.row + move[0], pos.column + move[1])) {
             return new Position(pos.row + move[0], pos.column + move[1]);
         }
         return positions[pos.row + move[0]][pos.column + move[1]];
     }
 
     /**
      * Getter for playerCol.
      * 
      * @return Column index for the player position
      */
     public int getPlayerCol()
     {
         return playerCol;
     }
 
     /**
      * Getter for playerRow.
      * 
      * @return Row index for the player position
      */
     public int getPlayerRow()
     {
         return playerRow;
     }
 
     /**
      * Gets the number of goal cells that don't have box yet.
      * 
      * @return The number of remaining boxes
      */
     public int getRemainingBoxes()
     {
         return remainingBoxes;
     }
 
     /**
      * Returns true if the square has any of the bits in the mask.
      * 
      * @param square The square at which to check for the bitmask
      * @param mask The mask to check against
      * @return true if the square and the mask matches, false otherwise
      */
     public static boolean is(byte square, byte mask)
     {
         return (square & mask) != 0;
     }
 
     /**
      * Counts the boxes that are not in a goal, and updates remainingBoxes.
      */
     private void countBoxes()
     {
         boxCount = 0;
         remainingBoxes = 0;
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 if (is(cells[row][col], BOX)) {
                     boxCount++;
                     if (!is(cells[row][col], GOAL))
                         remainingBoxes++;
                 }
             }
         }
     }
 
     /**
      * Print the current board
      * 
      * @return The string representing the board
      */
     @Override
     public String toString()
     {
         StringBuilder sb = new StringBuilder(width * height + height);
         for (int i = 0; i < height - 1; ++i) {
             for (int j = 0; j < width; ++j) {
                 sb.append(cellToChar(i, j));
             }
             if (i != height - 1) {
                 sb.append("\n");
             }
         }
         return sb.toString();
     }
 
     /**
      * Returns the official Sokoban char for the given internal value.
      * 
      * @param value
      *            The internal byte representation of a cell.
      * @return The character that is represented by the given internal value.
      */
     private char valueToChar(byte value)
     {
         switch (value & Board.INPUT_CELL_MASK) {
             case Board.WALL:
                 return '#';
             case Board.GOAL:
                 return '.';
             case Board.BOX:
                 return '$';
             case Board.GOAL | Board.BOX:
                 return '*';
             default:
                 return ((value & Board.BOX_TRAP) == 0 ? ' ' : '-');
         }
     }
 
     /**
      * Returns the Sokoban char for the given cell. Takes care of player
      * position.
      * 
      * @param row
      *            The row index of the cell to specify.
      * @param col
      *            The column index of the cel to specify.
      * @return The Sokoban char for the specified cell according to
      *         http://www.sokobano.de/wiki/index.php?title=Level_format.
      */
     public char cellToChar(int row, int col)
     {
         byte cell = cells[row][col];
 
         // Check for some errors first
         if (is(cell, BOX_TRAP) && is(cell, GOAL))
             return 'E'; // Goal on trap = error!
 
         if (is(cell, BOX_TRAP) && is(cell, BOX))
             return 'e'; // Box in trap = error!
 
         // No errors detected in this cell
         if (playerRow == row && playerCol == col) {
             return is(cell, Board.GOAL) ? '+' : '@';
         }
         else {
             return valueToChar(cell);
         }
     }
 
     private static final char moveChars[] = { 'U', 'D', 'L', 'R' };
 
     /**
      * Print the solution.
      * 
      * @param solution A list of board directions
      * @return The solution as a string
      */
     public static String solutionToString(Deque<Board.Direction> solution)
     {
         StringBuilder sb = new StringBuilder(2 * solution.size());
         for (Board.Direction move : solution) {
             sb.append(moveChars[move.ordinal()]);
             sb.append(' ');
         }
         return sb.toString();
     }
 
     /**
      * Marks squares that boxes would get stuck in (dead squares / box traps).
      */
     private void markNonBoxSquares()
     {
         // Mark corners
         for (int row = 1; row < height - 1; row++) {
             for (int col = 1; col < width - 1; col++) {
                 // Goal squares usually aren't traps
                 // (if the right block is placed there)
                 if (is(cells[row][col], GOAL))
                     continue;
 
                 boolean horizontalBlocked = is(cells[row - 1][col], WALL)
                         || is(cells[row + 1][col], WALL);
                 boolean verticalBlocked = is(cells[row][col - 1], WALL)
                         || is(cells[row][col + 1], WALL);
 
                 // This is a corner
                 if (horizontalBlocked && verticalBlocked)
                     cells[row][col] |= BOX_TRAP;
             }
         }
 
         // Find dead lines between dead squares
         boolean changed;
         do {
             changed = false;
             for (int row = 1; row < height - 1; row++) {
                 for (int col = 1; col < width - 1; col++) {
                     // Always start at a box trap
                     if (!is(cells[row][col], BOX_TRAP))
                         continue;
 
                     // Look to the right
                     for (int right = col + 1; right < width - 1; right++) {
                         // Stop at goals
                         if (is(cells[row][right], GOAL))
                             break;
 
                         // Stop and mark cells if there's either wall or a trap
                         // cell
                         if (is(cells[row][right], WALL_OR_TRAP)) {
                             // Mark cells
                             for (int i = col + 1; i < right; i++) {
                                 cells[row][i] |= BOX_TRAP;
                                 changed = true;
                             }
                             break;
                         }
 
                         // Check if there's a way to move out the block
                         if (!is(cells[row - 1][right], WALL)
                                 && !is(cells[row + 1][right], WALL))
                             break;
                     }
 
                     // Look below
                     for (int down = row + 1; down < height - 1; down++) {
                         // Stop at goals
                         if (is(cells[down][col], GOAL))
                             break;
 
                         // Stop and mark cells if there's either wall or a trap
                         // cell
                         if (is(cells[down][col], WALL_OR_TRAP)) {
                             // Mark cells
                             for (int i = row + 1; i < down; i++) {
                                 cells[i][col] |= BOX_TRAP;
                                 changed = true;
                             }
                             break;
                         }
 
                         // Check if there's a way to move out the block
                         if (!is(cells[down][col - 1], WALL)
                                 && !is(cells[down][col + 1], WALL))
                             break;
                     }
                 }
             }
         }
         while (changed);
     }
 
     /**
      * Returns a deep copy of this board.
      */
     public Object clone()
     {
         try {
             Board copy = (Board) super.clone();
 
             // Deep copy cells
             copy.cells = new byte[height][width];
             for (int row = 0; row < height; ++row) {
                 // Fastest way according to the following web page:
                 // http://www.javapractices.com/topic/TopicAction.do?Id=3
                 System.arraycopy(this.cells[row], 0, copy.cells[row], 0, width);
             }
 
             return copy;
         }
         catch (CloneNotSupportedException e) {
             throw new Error(
                     "This should not occur since we implement Cloneable");
         }
     }
 
     /**
      * Move the player in the specified direction
      * 
      * @param dir The direction to move the player in
      */
     public void move(Direction dir)
     {
         int move[] = moves[dir.ordinal()];
 
         // The cell that the player moves to
         int row = playerRow + move[0];
         int col = playerCol + move[1];
 
         // The cell that the box (if any) moves to
         int row2 = playerRow + 2 * move[0];
         int col2 = playerCol + 2 * move[1];
 
         // Mark as visited
         cells[playerRow][playerCol] |= VISITED;
 
         // Move player
         playerRow = row;
         playerCol = col;
 
         if (is(cells[row][col], BOX)) {
             // Move box
             cells[row][col] &= ~BOX;
             cells[row2][col2] |= BOX;
 
             // Keep track of remaining boxes
             remainingBoxes += (is(cells[row][col], GOAL) ? +1 : 0)
                     + (is(cells[row2][col2], GOAL) ? -1 : 0);
 
             // Clear "visited" marks
             clearFlag(VISITED);
         }
     }
 
     /**
      * Move the player on the position from to the position to.
      * 
      * TODO remove unused parameters
      * 
      * @param from
      * @param to
      */
     public void movePlayer(Position to)
     {
         playerRow = to.row;
         playerCol = to.column;
     }
 
     /**
      * Moves a box and updates remainingBoxes. This method ignores the
      * player position. Updates Zobrist hash.
      */
     public void moveBox(Position from, Position to)
     {
         // Remove box from previous position
         removeBox(from);
         
         // Move box to new position
         addBox(to);
         
         boxesNeedsUpdate = true;
         topLeftNeedsUpdate = true;
     }
     
     public void removeBox(Position box) {
         zobristKey = Zobrist.remove(zobristKey, Zobrist.BOX, box.row,
                 box.column);    
         zobristKey = Zobrist.add(zobristKey, Zobrist.EMPTY, box.row,
                 box.column);
         cells[box.row][box.column] &= ~BOX;
         if (is(cells[box.row][box.column], GOAL))
             remainingBoxes++;
         boxesNeedsUpdate = true;
         topLeftNeedsUpdate = true;
     }
     
     public void addBox(Position box) {
         zobristKey = Zobrist.remove(zobristKey, Zobrist.EMPTY, box.row,
                 box.column);
         zobristKey = Zobrist.add(zobristKey, Zobrist.BOX, box.row, box.column);
         cells[box.row][box.column] |= BOX;
         if (is(cells[box.row][box.column], GOAL))
             remainingBoxes--;
         boxesNeedsUpdate = true;
         topLeftNeedsUpdate = true;
     }
 
     /**
      * Removes a certain flag from all the squares.
      */
     public void clearFlag(byte flag)
     {
         for (int r = 0; r < height; r++) {
             for (int c = 0; c < width; c++) {
                 cells[r][c] &= ~flag;
             }
         }
     }
 
     /**
      * Compares two boards for equality.
      * 
      * @note The topmost, leftmost reachable position is compared instead of
      *       the actual player position.
      */
     @Override
     public boolean equals(Object other)
     {
         if (!(other instanceof Board))
             return false;
 
         Board o = (Board) other;
 
         if (getTopLeftReachable() != o.getTopLeftReachable())
             return false;
 
         // The outer rows/columns are always walls (or not reachable)
         for (int y = 1; y < height - 1; y++) {
             for (int x = 1; x < width - 1; x++) {
                 int cell1 = cells[y][x] & BOX;
                 int cell2 = o.cells[y][x] & BOX;
                 if (cell1 != cell2)
                     return false;
             }
         }
 
         return true;
     }
 
     public long getZobristKey()
     {
         return zobristKey ^ getTopLeftReachable();
     }
 
     // XXX: Remove later?
     // @Override
     // public int hashCode()
     // {
     // return (int) (zobristKey ^ (zobristKey >> 32));
     // }
 
     /**
      * Returns whether or not the given position is contained in this board.
      * 
      * @param pos The position.
      * @return True if this board contains the position, otherwise false.
      */
     public boolean contains(Position pos)
     {
         return contains(pos.row, pos.column);
     }
 
     /**
      * Returns whether or not the position specified by the given row and column
      * exists on this board.
      * 
      * @param row The row index.
      * @param col The column index.
      * @return True if it exists, otherwise false.
      */
     public boolean contains(int row, int col)
     {
         return row >= 0 && row < height && col >= 0 && col < width;
     }
 
     /**
      * Return the position for all boxes on the board
      * 
      * @return A collection of positions of boxes
      */
     public Collection<Position> getBoxes()
     {
         Collection<Position> tmp = new LinkedList<Position>();
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 if (is(cells[row][col], BOX)) {
                     tmp.add(positions[row][col]);
                 }
             }
         }
         return tmp;
     }
 
     /**
      * Finds a path from the player's current position on this board to the
      * specified goal position.
      * 
      * TODO: We might want to traverse the whole board (starting at players
      * position) and for each ripple (think of water) out from the player we
      * denote the direction in which we should go from that square to get back
      * to the player.
      * 
      * @param goal The position of the cell that we want to find a path to.
      * @return A collection
      */
     public Deque<Direction> findPath(Position goal)
     {
         clearFlag(VISITED);
         return findPath(positions[playerRow][playerCol], goal);
     }
 
     private class SearchNode
     {
         public final SearchNode parent;
         public final Object object;
         public final Object info;
 
         public SearchNode(Object object, SearchNode parent)
         {
             this(object, parent, null);
         }
 
         public SearchNode(Object object, SearchNode parent, Object info)
         {
             this.object = object;
             this.parent = parent;
             this.info = info;
         }
     }
 
     /**
      * Finds a path from the start position to the goal position recursively.
      * 
      * @see sokoban.Board#findPathRecursive findPathRecursive
      * @param start Starting position.
      * @param goal Goal position.
      * @return A list of directions to go from start to goal.
      */
     public Deque<Direction> findPath(Position start, Position goal)
     {
         clearFlag(VISITED);
         
         Deque<Direction> solution = new LinkedList<Direction>();
         
         Queue<SearchNode> queue = new LinkedList<SearchNode>();
         queue.add(new SearchNode(start, null));
         
         while (!queue.isEmpty()) {
             SearchNode node = queue.poll();
             
             cells[((Position)node.object).row][((Position)node.object).column] |= VISITED;
 
             for (Direction dir : Direction.values()) {
                 Position newPosition = getPosition(((Position)node.object), moves[dir.ordinal()]);
                 if (newPosition.equals(goal)) {
                     solution.add(dir);
                     while (node != null) {
                         if (node.info != null) {
                             solution.addFirst((Direction) node.info);
                         }
                         node = node.parent;
                     }
                     return solution;
                 }
 
                 // We do not move any boxes while going this path.
                 if (!is(cells[newPosition.row][newPosition.column], (byte) (WALL
                         | BOX | VISITED))) {
 
                     queue.add(new SearchNode(newPosition, node, dir));
                 }
             }
         }
         return null;
     }
 
     /**
      * Finds all boxes that can be reached by the player.
      * 
      * @return A collection of ReachableBox objects
      * 
      * @deprecated Use getReachableBoxes instead
      */
     public Collection<ReachableBox> oldFindReachableBoxSquares()
     {
         clearFlag(VISITED);
         ArrayList<ReachableBox> reachable = new ArrayList<ReachableBox>(20);
         oldFindReachableWithDFS(reachable, playerRow, playerCol,
                 new LinkedList<Direction>());
         return reachable;
     }
 
     /**
      * Finds all boxes that can be reached by the player.
      * 
      * @return A collection of ReachableBox objects
      */
     private void oldFindReachableWithDFS(ArrayList<ReachableBox> reachable,
             int startRow, int startCol, LinkedList<Direction> path)
     {
         cells[startRow][startCol] |= VISITED;
 
         boolean boxNearby = false;
         for (Direction dir : Direction.values()) {
             int row = startRow + moves[dir.ordinal()][0];
             int col = startCol + moves[dir.ordinal()][1];
 
             if (is(cells[row][col], BOX)) {
                 boxNearby = true;
                 continue;
             }
 
             if (!is(cells[row][col], (byte) (WALL | BOX | VISITED))) {
                 path.addLast(dir);
                 oldFindReachableWithDFS(reachable, row, col, path);
                 path.removeLast();
             }
         }
 
         if (boxNearby) {
             reachable.add(new ReachableBox(positions[startRow][startCol],
                     new LinkedList<Direction>(path)));
         }
     }
 
     /**
      * Returns all boxes that can be reached by the player.
      * 
      * @return A collection of ReachableBox objects
      */
     public Collection<Position> findReachableBoxSquares()
     {
         if (boxesNeedsUpdate) {
             updateReachability(true);
         }
 
         return reachableBoxes;
     }
 
     /**
      * Gets the current topLeftReachable value, and updates it if needed.
      */
     public int getTopLeftReachable()
     {
         if (topLeftNeedsUpdate) {
             updateReachability(false);
         }
 
         return topLeftReachable;
     }
 
     /**
      * Updates the reachability information. This includes two things:
      * 
      * 1) The minimum top left position that the player can move to,
      * defined as (row*width)+col. This is used for duplicate detection.
      * 
      * 2) The list of reachable boxes.
      */
     public void updateReachability(boolean updateBoxes)
     {
         updateBoxes = updateBoxes && boxesNeedsUpdate;
 
         clearFlag(REACHABLE);
         if (updateBoxes) {
             reachableBoxes = new ArrayList<Position>(boxCount);
             boxesNeedsUpdate = false;
         }
 
         // TODO: Should this be local?
         topLeftReachable = updateReachabilityDFS(playerRow, playerCol,
                 updateBoxes);
         topLeftNeedsUpdate = false;
     }
 
     /**
      * Recursive part of updateReachability
      */
     private int updateReachabilityDFS(int startRow, int startCol,
             boolean updateBoxes)
     {
         cells[startRow][startCol] |= REACHABLE;
 
         int minimum = (startRow * width) + startCol;
         boolean boxNearby = false;
         for (int dir = 0; dir < 4; ++dir) {
             int row = startRow + moves[dir][0];
             int col = startCol + moves[dir][1];
 
             if ((cells[row][col] & (BOX | REACHABLE)) == BOX) {
                 // Add to reachable list
                 boxNearby = true;
             }
 
             if (!is(cells[row][col], (byte) (WALL | REACHABLE | BOX))) {
                 int pos = updateReachabilityDFS(row, col, updateBoxes);
                 if (pos < minimum)
                     minimum = pos;
             }
         }
 
         if (boxNearby && updateBoxes) {
             reachableBoxes.add(positions[startRow][startCol]);
         }
 
         return minimum;
     }
 
     /**
      * Forces an update of the reachability.
      */
     public void forceReachabilityUpdate()
     {
         boxesNeedsUpdate = true;
         topLeftNeedsUpdate = true;
         updateReachability(true);
     }
 
 }
