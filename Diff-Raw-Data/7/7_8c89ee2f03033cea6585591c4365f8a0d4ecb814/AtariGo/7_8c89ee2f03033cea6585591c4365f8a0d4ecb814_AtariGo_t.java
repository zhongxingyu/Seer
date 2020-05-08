 package edu.berkeley.gamesman.game;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Value;
 
 public class AtariGo extends RectangularDartboardGame {
 	boolean[][] checked = new boolean[gameHeight][gameWidth];
 	boolean current;
 
 	public AtariGo(Configuration conf) {
 		super(conf, NO_TIE);
 	}
 
 	@Override
 	public Value primitiveValue() {
 		boolean hasWin = false;
 		char turn = getTier() % 2 == 0 ? 'X' : 'O';
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				if ((!hasWin) && checked[row][col] == current
 						&& get(row, col) == turn)
 					hasWin = !canBreathe(row, col, turn);
 				else
 					checked[row][col] = !current;
 			}
 		}
 		current = !current;
 		return hasWin ? Value.LOSE : Value.UNDECIDED;
 	}
 
 	public boolean canBreathe(int row, int col, char c) {
 		if (row < 0 || row >= gameHeight || col < 0 || col >= gameWidth) {
 			return false;
 		} else if (get(row, col) == ' ')
 			return true;
 		else if (get(row, col) != c)
 			return false;
 		else if (checked[row][col] != current)
 			return false;
 		else {
 			checked[row][col] = !current;
			boolean a = canBreathe(row + 1, col, c), b = canBreathe(row,
					col + 1, c), d = canBreathe(row - 1, col, c), e = canBreathe(
					row, col - 1, c);
			return a || b || d || e;
 		}
 	}
 
 	@Override
 	public String describe() {
 		return gameWidth + "x" + gameHeight + " Atari Go";
 	}
 }
