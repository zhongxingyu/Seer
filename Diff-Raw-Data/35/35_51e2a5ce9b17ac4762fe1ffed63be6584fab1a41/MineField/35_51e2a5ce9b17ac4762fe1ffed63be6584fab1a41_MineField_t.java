 package se.danielduner.minesweeper.client;
 
 import java.util.Random;
 
 public class MineField {
 	public enum GameStatus {
 		PLAYING, LOST, WON
 	}
	public static int HIDDEN=-1, EXPLODEDMINE=-2, HIDDENMINE=-3, FOUNDMINE=-4, FLAGGED=-5;
 	private boolean[][] mines;
 	private boolean[][] flagged;
 	private boolean[][] exposed;
 	private boolean[][] exploded;
 	private int[][] mineNeighbourCount;
 	private int[][] hiddenNeighbourCount;
 	private int[][] flaggedNeighbourCount;
 	
 	private int width;
 	private int height;
 	private int mineCount;
 	private int exposedCount = 0;
 	
 	private GameStatus gameStatus = GameStatus.PLAYING;
 	
 	public MineField(int width, int height, int mineCount) {
 		this.width = width;
 		this.height = height;
 		this.mineCount = mineCount;
 		mines = new boolean[width][height];
 		flagged = new boolean[width][height];
 		exposed = new boolean[width][height];
 		exploded = new boolean[width][height];
 		mineNeighbourCount = new int[width][height];
 		hiddenNeighbourCount = new int[width][height];
 		flaggedNeighbourCount = new int[width][height];
 		
 		int minesPlaced = 0;
 		Random random = new Random();
 		while (minesPlaced<mineCount) {
 			int x = random.nextInt(width);
 			int y = random.nextInt(height);
 			if (!mines[x][y]) {
 				mines[x][y] = true;
 				minesPlaced++;
 			}
 		}
 		
 		for(int y=0; y<height; y++) {
 			for(int x=0; x<width; x++) {
 				for(int yd=-1; yd<=1; yd++) {
 					for(int xd=-1; xd<=1; xd++) {
 						if (!(yd==0 && xd==0) && y+yd>=0 && y+yd<height && x+xd>=0 && x+xd<width) {
 							if(mines[x][y]){
 								mineNeighbourCount[x+xd][y+yd]++;
 							}
 							hiddenNeighbourCount[x+xd][y+yd]++;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public int getValue(int x, int y) {
 		if (exploded[x][y]) {
 			return EXPLODEDMINE;
 		} else if (gameStatus!=GameStatus.PLAYING && mines[x][y]) {
			if (flagged[x][y]) {
				return FOUNDMINE;
			} else {
				return HIDDENMINE;
			}
 		} else if(flagged[x][y]) {
 			return FLAGGED;
 		} else if(!exposed[x][y]){
 			return HIDDEN;
 		} else {
 			return mineNeighbourCount[x][y];
 		}
 	}
 	
 	public void expose(int x, int y){
 		if(!exposed[x][y]){
 			exposed[x][y] = true;
 			if (flagged[x][y]) {
 				unflag(x, y);
 			}
 			if (mines[x][y]) {
 				exploded[x][y] = true;
 				gameStatus = GameStatus.LOST;
 			}
 			if (!mines[x][y] && mineNeighbourCount[x][y]==0) {
 				for(int yd=y-1; yd<=y+1; yd++) {
 					for(int xd=x-1; xd<=x+1; xd++) {
 						if (!(yd==y && xd==x) && yd>=0 && yd<height && xd>=0 && xd<width) {
 							expose(xd, yd);
 						}
 					}
 				}
 			}
 			exposedCount++;
 			if (width*height-exposedCount == mineCount && gameStatus==GameStatus.PLAYING) {
 				gameStatus = GameStatus.WON;
 			}
 			for(int yd=y-1; yd<=y+1; yd++) {
 				for(int xd=x-1; xd<=x+1; xd++) {
 					if (!(yd==y && xd==x) && yd>=0 && yd<height && xd>=0 && xd<width) {
 						hiddenNeighbourCount[xd][yd]--;
 					}
 				}
 			}
 		}
 	}
 	
 	public void flag(int x, int y) {
 		if(!exposed[x][y]){
 			flagged[x][y] = true;
 			for(int yd=-1; yd<=1; yd++) {
 				for(int xd=-1; xd<=1; xd++) {
 					if (!(yd==0 && xd==0) && y+yd>=0 && y+yd<height && x+xd>=0 && x+xd<width) {
 						flaggedNeighbourCount[x+xd][y+yd]++;
 					}
 				}
 			}
 		}
 	}
 	
 	public void unflag(int x, int y) {
 		if(!flagged[x][y]){
 			flagged[x][y] = false;
 		}
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 	
 	public int getMineCount() {
 		return mineCount;
 	}
 	
 	public GameStatus getGameStatus() {
 		return gameStatus; 
 	}
 	
 	public int getHiddenNeighbours(int x, int y){
 		return hiddenNeighbourCount[x][y];
 	}
 	
 	public int getFlaggedNeighbours(int x, int y){
 		return flaggedNeighbourCount[x][y];
 	}
 }
