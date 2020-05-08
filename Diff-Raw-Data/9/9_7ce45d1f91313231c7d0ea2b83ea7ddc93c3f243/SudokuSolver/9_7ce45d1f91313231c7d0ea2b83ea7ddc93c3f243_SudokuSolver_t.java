 package sudoku;
 
 import java.util.BitSet;
 import java.util.Stack;
 
 public class SudokuSolver {
 	// place holder for unfilled cell from original board
 	public static final int NULL = 0;
 
 	/**
 	 * @return names of the authors and their student IDs (1 per line).
 	 */
 	public String authors() {
 		String ret = "NAMES OF THE AUTHORS AND THEIR STUDENT IDs (1 PER LINE)\n";
 		ret += "Yi (Armand) Li 61420048\n";
		ret += "Jeffrey Cheung 66994062\n";
 		return ret;
 	}
 
 	/**
 	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
 	 * 
 	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
 	 * @return the solved Sudoku board
 	 */
 	public int[][] solve(int[][] board) {
 		BitSet[][] domains = makeDomain(board); // produce the initial domain for all variables 
 		Stack<Constraint> stack = initConstraints(board); // produce all the constraints for the board
 		domainSplit(domains, board, stack); // do AC with domain split
 		return board;
 	}
 
 	/**
 	 * finds out the square size of the sudoku board. The program
 	 * is implemented such that the board size can vary, so long as
 	 * each square on board is a power of 2 of some integer bigger than 3
 	 * e.g. you can have a sudoku board of 1024 by 1024 and this will
 	 * still do the job
 	 * @param board
 	 * @return the side size of each square inside the sudoku, minimum sudoku size is set to 3 by 3
 	 */
 	int squareFactor(int[][] board){
 		int k = 3;
 		while (k * k < board.length) ++k;
 		assert(k * k == board.length);
 		return k;
 	}
 	
 	/**
 	 * find out the size of the domain, total number of possible 
 	 * values for any cell that is not being filled
 	 * @param board
 	 * @return the total number of possible values for a given unfilled cell
 	 */
 	int domainSize(int[][] board){
 		int k = squareFactor(board);
 		return k * k;
 	}
 	
 	/**
 	 * takes the board and produces the domain values for all variables
 	 * @param board
 	 * @return 2 dimensional array of all possible domain values for all variables
 	 */
 	BitSet[][] makeDomain(int[][] board){
 		int domSize = domainSize(board);
 		BitSet[][] ret = new BitSet[board.length][board.length];
 		for (int i = 0; i < board.length; ++i)
 			for (int j = 0; j < board.length; ++j)
 				if (board[i][j] == NULL){ // if no value filled, we can have all possible values for this variable
 					ret[i][j] = new BitSet(domSize);
 					ret[i][j].set(0, domSize);
 				}else{ // if a value is already filled we only have one value for the domain
 					ret[i][j] = new BitSet(domSize);
 					ret[i][j].set(board[i][j] - 1);
 				}
 		return ret;
 	}
 	
 	/**
 	 * remove all values v in domain of variable a such that there is no value in variable domain of b
 	 * that doesn't equal to v for all v
 	 * @param domains
 	 * @param ax row index of variable a
 	 * @param ay column index of variable a
 	 * @param bx row index of variable b
 	 * @param by column index of variable b
 	 * @return if domain of variable a has been modified, return true, else return false
 	 */
 	boolean checkConstraint(BitSet[][] domains, int ax, int ay, int bx, int by){
 		if (domains[ax][ay].cardinality() == 0 || domains[bx][by].cardinality() == 0)
 			return false;
 		boolean needRecheck = false;
 		for (int i = 0; i < domains.length; ++i)
 			if (domains[ax][ay].get(i)){
 				boolean found = false;
 				for (int j = 0; j < domains.length; ++j)
 					if (domains[bx][by].get(j) && j != i){
 						found = true;
 						break;
 					}
 				if (!found){
 					domains[ax][ay].set(i, false);
 					needRecheck = true;
 				}
 			}
 		return needRecheck;
 	}
 	
 	/**
 	 * AC function. takes a stack of constraints that need to be checked, check each one of them
 	 * until no more constraints need to be checked
 	 * @param domains the domains for all variables
 	 * @param board
 	 * @param stack   the stack of outstanding constraints
 	 */
 	void gac(BitSet[][] domains, int[][] board, Stack<Constraint> stack){
 		while (!stack.isEmpty()){
 			Constraint c = stack.pop();
 			if (checkConstraint(domains, c.ax, c.ay, c.bx, c.by))
 				addConstraint(stack, c.ax, c.ay, board);
 		}
 	}
 	
 	/**
 	 * takes a variable's domain and split it in two, save them separately in b1 and b2
 	 * @param domains
 	 * @param x
 	 * @param y
 	 * @param b1      bitset containing the first part of the splitted domain
 	 * @param b2      bitset containing the second part of the splitted domain
 	 */
 	void splitDomain(BitSet[][] domains, int x, int y, BitSet b1, BitSet b2){
 		int total = domains[x][y].cardinality();
 		int k = 0, i = 0;
 		while(k < total / 2){
 			i = domains[x][y].nextSetBit(i);
 			b1.set(i++);
 			k++;
 		}
 		while (k < total){
 			i = domains[x][y].nextSetBit(i);
 			b2.set(i++);
 			k++;
 		}
 	}
 	
 	/**
 	 * makes a deep copy of the parameter domain 
 	 * @param domains
 	 * @return a deep copy of the paramter domain
 	 */
 	BitSet[][] cloneDomain(BitSet[][] domains){
 		BitSet[][] ret = new BitSet[domains.length][domains.length];
 		for (int i = 0; i < domains.length; ++i)
 			for (int j = 0; j < domains.length; ++j)
 				ret[i][j] = (BitSet)domains[i][j].clone();
 		return ret;
 	}
 	
 	/**
 	 * do AC, then if a variable has multiple values in its domain, split it in half
 	 * and recursively do domainSplit on them
 	 * @param domains
 	 * @param board
 	 * @param stack
 	 */
   void domainSplit(BitSet[][] domains, int[][] board, Stack<Constraint> stack){
 		gac(domains, board, stack);
 		boolean fin = true;
 		for (int i = 0; i < board.length; ++i)
 			for (int j = 0; j < board.length && fin; ++j){
 				int temp = domains[i][j].cardinality();
 				// if a variable domain has no value, this is invalid board, terminate recursion
 				if (temp == 0)
 					return;
 				// if more than 1 value detected in domain of a variable, we do split
 				else if (temp > 1){
 					fin = false;
 					// make first half, recurse
 					BitSet b1 = new BitSet(domains.length), b2 = new BitSet(domains.length);
 					splitDomain(domains, i, j, b1, b2);
 					BitSet[][] splitted = cloneDomain(domains);
 					splitted[i][j] = b1;
 					Stack<Constraint> s = new Stack<Constraint>();
 					addConstraint(s, i, j, board);
 					domainSplit(splitted, board, s);
 					// make second half, recurse
 					splitted = cloneDomain(domains);
 					splitted[i][j] = b2;
 					s = new Stack<Constraint>();
 					addConstraint(s, i, j, board);
 					domainSplit(splitted, board, s);
 				}
 			}
 		// if domains of all variables only have one value, we fill the board
 		if (fin){
 			for (int i = 0; i < domains.length; ++i)
 				for (int j = 0; j < domains.length; ++j)
 					if (board[i][j] == NULL)
 						board[i][j] = domains[i][j].nextSetBit(0) + 1;
 		}
 	}
   
   /**
    * add to the stack all constraints that is related to variable v
    * @param stack
    * @param vx     row value of variable v
    * @param vy     column value of variable v
    * @param board
    */
 	void addConstraint(Stack<Constraint> stack, int vx, int vy, int[][] board){
 		for (int k = 0; k < board.length; ++k){
 			// add all variables having the same row
 			if (k != vy)
 				stack.add(new Constraint(vx, k, vx, vy));
 			// add all variables having the same column
 			if (k != vx)
 				stack.add(new Constraint(k, vy, vx, vy));
 			// add all variables having the same square
 			int sx = vx / 3, sy = vy / 3, offx = k / 3, offy = k % 3;
 			sx = sx * 3 + offx;
 			sy = sy * 3 + offy;
 			if (sx != vx && sy != vy)
 				stack.add(new Constraint(sx, sy, vx, vy));
 		}
 	}
 	
 	/**
 	 * get all constraints of the board
 	 * @param board
 	 * @return
 	 */
 	Stack<Constraint> initConstraints(int[][] board){
 		Stack<Constraint> stack = new Stack<Constraint>();
 		for (int i = 0; i < board.length; ++i)
 			for (int j = 0; j < board.length; ++j)
 				for (int k = 0; k < board.length; ++k){
 					// add all variables having the same row
 					if (k != j)
 						stack.add(new Constraint(i, j, i, k));
 					// add all variables having the same column
 					if (k != i)
 						stack.add(new Constraint(i, j, k ,j));
 					// add all variables having the same square
 					int sx = i / 3, sy = j / 3, offx = k / 3, offy = k % 3;
 					sx = sx * 3 + offx;
 					sy = sy * 3 + offy;
 					if (sx != i && sy != j)
 						stack.add(new Constraint(i, j, sx, sy));
 				}
 		return stack;
 	}
 	
 	/**
 	 * data structure that captures the information needed for a constraint.
 	 * because all constraints of sudoku is just one, we don't have to specify
 	 * what type of constraint it is.
 	 *
 	 */
 	private class Constraint{
 		public int ax, ay, bx, by; // variable index of a and b, x are the row values and y are the column values
 		Constraint(int ax, int ay, int bx, int by){
 			this.ax = ax; this.ay = ay;
 			this.bx = bx; this.by = by;
 		}
 	}
 }
