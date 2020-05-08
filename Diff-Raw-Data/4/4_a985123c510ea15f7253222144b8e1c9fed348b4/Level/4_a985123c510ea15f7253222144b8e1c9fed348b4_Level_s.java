 package minesweeper.client;
 
 public class Level {
 	public static final int EASY = 1;
 	public static final int MEDIUM = 2;
 	public static final int HARD = 3;
 	public static final int CUSTOM = -1;
 	private int level = EASY;
 	private Minefield parent;
 
 	public Level(Minefield parent) {
 		this.parent = parent;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 
 		switch (level) {
 		case EASY:
 			parent.setCols(8);
 			parent.setRows(8);
 			parent.setMinesNum(10);
 			break;
 
 		case MEDIUM:
 			parent.setCols(16);
 			parent.setRows(16);
 			parent.setMinesNum(40);
 			break;
 
 		case HARD:
 			parent.setCols(31);
 			parent.setRows(16);
 			parent.setMinesNum(99);
 			break;
 
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public void setParams(int cols, int rows, int minesNum) {
		int _cols = Math.min(300, Math.max(3, cols));
		int _rows = Math.min(300, Math.max(3, rows));
 		int _maxMines = cols * rows / 4;
 		int _minesNum = Math.min(_maxMines, Math.max(1, minesNum));
 
 		parent.setCols(_cols);
 		parent.setRows(_rows);
 		parent.setMinesNum(_minesNum);
 
 		if (_cols == 8 && rows == 8 && minesNum == 10) {
 			level = EASY;
 		} else if (_cols == 16 && rows == 16 && minesNum == 40) {
 			level = MEDIUM;
 		} else if (_cols == 31 && rows == 16 && minesNum == 99) {
 			level = HARD;
 		} else {
 			level = CUSTOM;
 		}
 	}
 
 	public void setParams(String cols, String rows, String minesNum) {
 		setParams(Integer.parseInt(cols), Integer.parseInt(rows), Integer
 				.parseInt(minesNum));
 	}
 }
