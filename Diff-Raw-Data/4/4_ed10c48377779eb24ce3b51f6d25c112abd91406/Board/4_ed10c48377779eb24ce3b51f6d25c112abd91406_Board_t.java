 package game;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * Board as specified in Section 2.1
  * EXCEPT: we start counting at (0,0).
  * 
  * @author seba
  */
 public class Board {
 
   /**
    * FIRST coordinate is COLUMN;
    * SECOND coordinate is ROW.
    * 
    * (col, row) --> (col*height + row)
    */
   // public final Cell[] grid;
   
   public static final int N = 7;
   private static final Cell[] cells = Cell.values();
 
   public final int width;
   public final int height;
 
   public final BitSet[] bitsets = new BitSet[N];
   
   public Map<Integer, String> trampolinePos;
   public Map<String, Integer> targetPos;
   public Map<String, String> trampolineTargets;
   
   public final int length;
   
   public int robot;
   public int lift;
 
   /**
    * Packs a two-dimensional coordinate in a single integer.
    */
   public final int position(int col, int row) {
     return col * height + row;
   }
 
   /**
    * Unpacks the column (= x) dimension from a single integer.
    */
   public final int col(int position) {
     return position / height;
   }
 
   /**
    * Unpacks the row (= y) dimension from a single integer.
    */
   public final int row(int position) {
     return position % height;
   }
 
   /**
    * Returns the left neighbor of a position.
    */
   public final int left(int position) {
     return position - height;
   }
   
   /**
    * Returns the right neighbor of a position.
    */
   public final int right(int position) {
     return position + height;
   }
 
   /**
    * Returns the upper neighbor of a position.
    */
   public final int up(int position) {
     return position + 1;
   }
 
   /**
    * Returns the lower neighbor of a position.
    */
   public final int down(int position) {
     return position - 1;
   }
     
   /**
    * Create empty board.
    */
   public Board(int width, int height) {
     this.width = width;
     this.height = height;
     this.length = width * height;
     for (int i = 0; i < N; i++) {
       this.bitsets[i] = new BitSet(length);
     }
     trampolinePos = new HashMap<Integer, String>();
     targetPos = new HashMap<String, Integer>();
     trampolineTargets = new HashMap<String, String>();
   }
 
   /**
    * Create a board as a clone of another board.
    */
   public Board(Board that) {
     this.width = that.width;
     this.height = that.height;
     this.length = that.length;
     System.arraycopy(that.bitsets, 0, this.bitsets, 0, N);
     this.robot = that.robot;
     this.lift = that.lift;
     this.trampolinePos = that.trampolinePos;
     this.targetPos = that.targetPos;
     this.trampolineTargets = that.trampolineTargets;
   }
   
   /**
    * Be careful. no range checking!
    */
   public Cell get(int position) {
     for (int  i = 0; i < N; i++) {
       if (bitsets[i].get(position)) {
     	if(i == Cell.Trampoline.ordinal()) {
     		String trampoline = trampolinePos.get(position);
     		String target = trampolineTargets.get(trampoline);
     		Integer jumppos  = targetPos.get(target);
     		if(get(jumppos) != Cell.Target) {
     		  return Cell.Empty;
     		}
     	}
         return cells[i];
       }
     }
 
     if (position == lift && position == robot)
       return Cell.RobotAndLift;
 
     if (position == robot)
       return Cell.Robot;
     
     if (position == lift)
       return Cell.Lift;
     
     return Cell.Empty;
 }
   
   /**
    * FIRST coordinate is COLUMN;
    * SECOND coordinate is ROW.
    */
   public Cell get(int col, int row) {
     if (col < 0 || col >= width || row < 0 || row >= height)
       return Cell.Wall;
     return get(position(col, row));
   }
   
   /**
    * Be careful. No range checking.
    */
   public void set(int position, Cell cell) {
     int newI = cell.ordinal();
     int oldI = get(position).ordinal();
 
     if (oldI != newI) {
       // set new info
       if (newI < N) {
         bitsets[newI] = (BitSet) bitsets[newI].clone();
         bitsets[newI].set(position);
       } else {
         switch (cell) {
         case Robot:
           robot = position;
           break;
         case Lift:
           lift = position;
           break;
         case RobotAndLift:
           robot = position;
           lift = position;
           break;
         case FallingRock:
           assert (false);
           break;
         case Empty:
           break;
         }
       }
       
       // clear old info
       if (oldI < N) {
         bitsets[oldI] = (BitSet) bitsets[oldI].clone();
         bitsets[oldI].clear(position);
       }
     }
   }
   
   /**
    * FIRST coordinate is COLUMN;
    * SECOND coordinate is ROW.
    */
   public void set(int col, int row, Cell c) {
     if (col < 0 || col >= width || row < 0 || row >= height)
       return;
     set(position(col, row), c);
   }
   
   public boolean isWall(int position) {
     return bitsets[Cell.Wall.ordinal()].get(position);
   }
 
   public boolean isRock(int position) {
     return bitsets[Cell.Rock.ordinal()].get(position);
   }
 
   public boolean isFallingRock(int position) {
     return bitsets[Cell.FallingRock.ordinal()].get(position);
   }
 
   public boolean isEarth(int position) {
     return bitsets[Cell.Earth.ordinal()].get(position);
   }
 
   public boolean isLambda(int position) {
     return bitsets[Cell.Lambda.ordinal()].get(position);
   }
 
   public boolean isRobot(int position) {
     return position == robot;
   }
 
   public boolean isLift(int position) {
     return position == lift;
   }
 
   public boolean isRobotAndLift(int position) {
     return isRobot(position) && isLift(position);
   }
   
   public boolean isEmpty(int position) {
     return get(position) == Cell.Empty;
   }
   
   public boolean isTrampoline(int position) {
 	return get(position) == Cell.Trampoline;
   }
   
   public boolean isTarget(int position) {
 	return get(position) == Cell.Target;
   }
 
   
   public boolean isPosition(int position) {
     int col = position / height;
     int row = position % height;
     return !(col < 0 || col >= width || row < 0 || row >= height);
   }
 
   public String toString() {
     StringBuilder sb = new StringBuilder();
     for (int rrow = height - 1; rrow >= 0; --rrow) {
       for (int col = 0; col < width; ++col)
         sb.append(get(col * height + rrow).shortName());
       if (rrow > 0)
         sb.append('\n');
     }
     return sb.toString();
   }
   
   public static Board parse(String s) {
     List<List<Cell>> flippedBoard = new ArrayList<List<Cell>>();
     int colCount = -1;
     
     
     StringTokenizer tokenizer = new StringTokenizer(s, "\n");
     while (tokenizer.hasMoreTokens()) {
       String line = tokenizer.nextToken();
       List<Cell> row = new ArrayList<Cell>();
       flippedBoard.add(row);
       
       for (int i = 0; i < line.length(); ++i) {
     	if((int)line.charAt(i) == 13) continue;
     	Cell cell = Cell.parse(line.charAt(i));
         row.add(cell);
       }
       colCount = Math.max(colCount, line.length());
     }
     
     int rowCount = flippedBoard.size();
     
     Board board = new Board(colCount, rowCount);
     for (int row = 0; row < rowCount; ++row)
       for (int col = 0; col < colCount; ++col) {
         int rrow = rowCount - row - 1;
         List<Cell> rowList = flippedBoard.get(rrow);
         if (col < rowList.size()) {
           // board.grid[col * rowCount + row] = rowList.get(col);
           int position = board.position(col, row);
           Cell cell = rowList.get(col);
           board.unsafeSet(position, cell);
                 
         } else { 
           // Section 2.5: "Shorter lines are assumed to be padded out with spaces"
           // we don't store Empty cells explicitly
         }
       }
     
     tokenizer = new StringTokenizer(s, "\n");
     int row = rowCount;
     while (tokenizer.hasMoreTokens()) {
       --row;
       String line = tokenizer.nextToken();
       for (int i = 0; i < line.length(); ++i) {
     	Cell cell = Cell.parse(line.charAt(i));
     	if(cell == Cell.Trampoline) {
     		int pos = board.position(i, row);
     		board.trampolinePos.put(pos, String.valueOf(line.charAt(i)));
     	}
     	if(cell == Cell.Target) {
     		int pos = board.position(i, row);
     		board.targetPos.put(String.valueOf(line.charAt(i)), pos);
     	}    	
       }
     }
     return board;
   }
   
   /**
    * Don't call this one.
    */
   public void unsafeSet(int position, Cell cell) {
     int i = cell.ordinal();
     if (i < N) {
       bitsets[i].set(position);
     } else {
       switch (cell) {
       case Robot:
         robot = position;
         break;
       case Lift:
         lift = position;
         break;
       case Empty:
         // we don't store Empty cells explicitly
         break;
       }
     }    
   }
 
   public Board clone() {
     return new Board(this);
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + Arrays.deepHashCode(bitsets);
     result = prime * result + height;
     result = prime * result + lift;
     result = prime * result + robot;
     result = prime * result + width;
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
     Board other = (Board) obj;
     if (!Arrays.deepEquals(bitsets, other.bitsets))
       return false;
     if (height != other.height)
       return false;
     if (lift != other.lift)
       return false;
     if (robot != other.robot)
       return false;
     if (width != other.width)
       return false;
     return true;
   }
 
   /**
    * Returns true if the rock at (col, row) cannot ever be pushed.
    */
   public boolean unpushable(int col, int row) {
     Cell left = get(col - 1, row);
     Cell right = get(col + 1, row);
     
     if (left == Cell.Wall || right == Cell.Wall)
       return true;
     if (left == Cell.Rock && get(col - 1, row - 1) == Cell.Wall)
       return true;
     if (right == Cell.Rock && get(col + 1, row - 1) == Cell.Wall)
       return true;
     
     return false;
   }
 
   /**
    * Returns true if the rock at (col, row) cannot ever move.
    */
   public boolean immovable(int col, int row) {
    if (get(col, row - 1) == Cell.Wall)
       return unpushable(col, row);
     return false;
       
   }
 }
