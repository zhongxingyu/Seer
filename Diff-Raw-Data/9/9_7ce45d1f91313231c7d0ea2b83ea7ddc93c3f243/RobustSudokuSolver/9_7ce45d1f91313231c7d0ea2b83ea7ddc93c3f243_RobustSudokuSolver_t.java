 package sudoku;
 
 import java.util.BitSet;
 import java.util.Stack;
 import java.io.IOException;
 
 public class RobustSudokuSolver {
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
 	public int[][] solve(int[][] board) throws IOException {
 		BitSet[][] domains = makeDomain(board);
 		Stack<Constraint> stack = initConstraints(board);
 		domainSplit(domains, board, stack, 0);
 		return board;
 	}
 	
 	int squareFactor(int[][] board){
 		int k = 3;
 		while (k * k < board.length) ++k;
 		assert(k * k == board.length);
 		return k;
 	}
 	
 	int domainSize(int[][] board){
 		int k = squareFactor(board);
 		return k * k;
 	}
 	
 	BitSet[][] makeDomain(int[][] board){
 		int domSize = domainSize(board);
 		BitSet[][] ret = new BitSet[board.length][board.length];
 		for (int i = 0; i < board.length; ++i)
 			for (int j = 0; j < board.length; ++j)
 				if (board[i][j] == NULL){
 					ret[i][j] = new BitSet(domSize);
 					ret[i][j].set(0, domSize);
 				}else{
 					ret[i][j] = new BitSet(domSize);
 					ret[i][j].set(board[i][j] - 1);
 				}
 		return ret;
 	}
 	
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
 	
 	void gac(BitSet[][] domains, int[][] board, Stack<Constraint> stack){
 		while (!stack.isEmpty()){
 			Constraint c = stack.pop();
 			if (checkConstraint(domains, c.ax, c.ay, c.bx, c.by))
 				addConstraint(stack, c.ax, c.ay, board);
 		}
 	}
 	
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
 	
 	BitSet[][] cloneDomain(BitSet[][] domains){
 		BitSet[][] ret = new BitSet[domains.length][domains.length];
 		for (int i = 0; i < domains.length; ++i)
 			for (int j = 0; j < domains.length; ++j)
 				ret[i][j] = (BitSet)domains[i][j].clone();
 		return ret;
 	}
 	
 	/**
 	 * This is the only modified section in Robust Sudoku Solver compared to Sudoku Solver.
 	 * domain split now returns a value indicating the number of solutions it has found.
 	 * if it is the root of the domain split search tree, we check how many solution found
 	 * and throws IOException if there is no solution or there is more than one solution
 	 * @param domains
 	 * @param board
 	 * @param stack
 	 * @param level    the level of the domain split search tree, the root is level 0
 	 * @return
 	 * @throws IOException
 	 */
   int domainSplit(BitSet[][] domains, int[][] board, Stack<Constraint> stack, int level) throws IOException {
 		gac(domains, board, stack);
 		boolean fin = true;
 		int count = 0;
 		for (int i = 0; i < board.length; ++i)
 			for (int j = 0; j < board.length && fin; ++j){
 				int temp = domains[i][j].cardinality();
 				if (temp == 0){
 					if (level == 0)
 						throw new IOException("No Solution Found!");
 					return 0;
 				}
 				else if (temp > 1){
 					fin = false;
 					BitSet b1 = new BitSet(domains.length), b2 = new BitSet(domains.length);
 					splitDomain(domains, i, j, b1, b2);
 					BitSet[][] splitted = cloneDomain(domains);
 					splitted[i][j] = b1;
 					Stack<Constraint> s = new Stack<Constraint>();
 					addConstraint(s, i, j, board);
 					count += domainSplit(splitted, board, s, level + 1);
 					splitted = cloneDomain(domains);
 					splitted[i][j] = b2;
 					s = new Stack<Constraint>();
 					addConstraint(s, i, j, board);
 					count += domainSplit(splitted, board, s, level + 1);
 					if (count == 0 && level == 0)
 						throw new IOException("No Solution Found!");
 					else if (count > 1 && level == 0)
 						throw new IOException("Multiple Solutions Found!");
 				}
 			}
 		if (fin){
 			for (int i = 0; i < domains.length; ++i)
 				for (int j = 0; j < domains.length; ++j)
 					if (board[i][j] == NULL)
 						board[i][j] = domains[i][j].nextSetBit(0) + 1;
 			return 1;
 		}
 		return count;
 	}
   
 	void addConstraint(Stack<Constraint> stack, int vx, int vy, int[][] board){
 		for (int k = 0; k < board.length; ++k){
 			if (k != vy)
 				stack.add(new Constraint(vx, k, vx, vy));
 			if (k != vx)
 				stack.add(new Constraint(k, vy, vx, vy));
 			int sx = vx / 3, sy = vy / 3, offx = k / 3, offy = k % 3;
 			sx = sx * 3 + offx;
 			sy = sy * 3 + offy;
 			if (sx != vx && sy != vy)
 				stack.add(new Constraint(sx, sy, vx, vy));
 		}
 	}
 	
 	Stack<Constraint> initConstraints(int[][] board){
 		Stack<Constraint> stack = new Stack<Constraint>();
 		for (int i = 0; i < board.length; ++i)
 			for (int j = 0; j < board.length; ++j)
 				for (int k = 0; k < board.length; ++k){
 					if (k != j)
 						stack.add(new Constraint(i, j, i, k));
 					if (k != i)
 						stack.add(new Constraint(i, j, k ,j));
 					int sx = i / 3, sy = j / 3, offx = k / 3, offy = k % 3;
 					sx = sx * 3 + offx;
 					sy = sy * 3 + offy;
 					if (sx != i && sy != j)
 						stack.add(new Constraint(i, j, sx, sy));
 				}
 		return stack;
 	}
 	
 	private class Constraint{
 		public int ax, ay, bx, by;
 		Constraint(int ax, int ay, int bx, int by){
 			this.ax = ax; this.ay = ay;
 			this.bx = bx; this.by = by;
 		}
 	}
 }
