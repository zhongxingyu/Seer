 package sk.estesadohodneme.sturik.game;
 
 import java.util.Queue;
 import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
 
 public class GameSnake extends Game {
 
 	public static final int BOARD_WIDTH = 15;
 	public static final int BOARD_HEIGHT= 13;
 	public static final int BOARD_EMPTY = 0;
 	public static final int BOARD_SNAKE = 1;
 	public static final int BOARD_HEAD  = 2;
 	public static final int BOARD_FOOD  = 3;
 	public static final int BOARD_SCORE = 4;
 	public static final int SNAKE_LIFES = 10;
 	public static final int SCORE_DURATION = 10;
 	
 	protected Queue<Integer> mSnake;
 	protected short[][] mBoard = new short[BOARD_HEIGHT][BOARD_WIDTH];
 	protected Random mRandom = new Random();
 	
 	protected int mShowScore;
 	protected int mSnakeStock;
 	protected int mSnakeVX,mSnakeVY;
 	protected int mKilledSnakes;
 	protected boolean mIsFinished;
 	
 	public boolean isFinished() {
 		return mIsFinished;
 	}
 	
 	protected short[][] digitBitmap() {
 		short k = BOARD_SCORE;
 		if (mKilledSnakes == 1) {
 			short bitmap[][] = {
 				      { 0, k, 0 },
 				      { 0, k, 0 },
 				      { 0, k, 0 },
 				      { 0, k, 0 },
 				      { 0, k, 0 }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 2) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { 0, 0, k },
 				      { k, k, k },
 				      { k, 0, 0 },
 				      { k, k, k }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 3) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { 0, 0, k },
 				      { k, k, k },
 				      { 0, 0, k },
 				      { k, k, k }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 4) {
 			short bitmap[][] = {
 				      { k, 0, 0 },
 				      { k, 0, 0 },
 				      { k, k, k },
 				      { 0, k, 0 },
 				      { 0, k, 0 }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 5) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { k, 0, 0 },
 				      { k, k, k },
 				      { 0, 0, k },
 				      { k, k, k }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 6) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { k, 0, 0 },
 				      { k, k, k },
 				      { k, 0, k },
 				      { k, k, k }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 7) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { 0, 0, k },
 				      { 0, 0, k },
 				      { 0, 0, k },
 				      { 0, 0, k }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 8) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { k, 0, k },
 				      { k, k, k },
 				      { k, 0, k },
 				      { k, k, k }
 				    };
 			return bitmap;
 		}
 		if (mKilledSnakes == 9) {
 			short bitmap[][] = {
 				      { k, k, k },
 				      { k, 0, k },
 				      { k, k, k },
 				      { 0, 0, k },
 				      { k, k, k }
 				    };
 			return bitmap;
 		}
 		return null;
 	}
 	
 	protected void showScore() {
 		int sx = BOARD_WIDTH/2 - 1;
 		int sy = BOARD_HEIGHT/2 - 3;
 		
 		short[][] bitmap = digitBitmap();
 		for(int i=0;i<5;i++)
 		for(int j=0;j<3;j++)
 			mBoard[i+sy][j+sx] = bitmap[i][j];
 	}
 	
 	protected boolean isEmpty(int i) {
 		if (mBoard[i/BOARD_WIDTH][i%BOARD_WIDTH] == BOARD_EMPTY) return true;
 		return false;
 	}
 	
 	protected int findEmptyPosition() {
 		int rnd = mRandom.nextInt(BOARD_HEIGHT*BOARD_WIDTH);
 		if (!isEmpty(rnd)) rnd = findEmptyPosition();
 		return(rnd);
 	}
 	
 	protected void boardAddFood() {
 		int pos = findEmptyPosition();
 		mBoard[pos/BOARD_WIDTH][pos%BOARD_WIDTH] = BOARD_FOOD;
 	}
 	
 	protected void killSnake() {
 		/*
 		while(!mSnake.isEmpty()) {
 			int p = mSnake.remove();
 			mBoard[p/BOARD_WIDTH][p%BOARD_WIDTH] = BOARD_EMPTY;
 		}*/
 		clearBoard();
 		
 		mShowScore = SCORE_DURATION;
 		
 		mKilledSnakes++;
 		if (mKilledSnakes == SNAKE_LIFES) mIsFinished = true;
 		//newSnake();
 	}
 	
 	protected void newSnake() {
 		mSnake.clear();
 		int startingPos = findEmptyPosition();
 		mSnake.add(startingPos);
 		mBoard[startingPos/BOARD_WIDTH][startingPos%BOARD_WIDTH] = BOARD_HEAD;
 		mSnakeVX = 0;
 		mSnakeVY = 0;
 		
 		mSnakeStock = 2;
 	}
 
 	protected void clearBoard() {
 		for(int i=0;i<BOARD_HEIGHT;i++) 
 		for(int j=0;j<BOARD_WIDTH;j++)
 			mBoard[i][j] = BOARD_EMPTY;
 	}
 	
 	public GameSnake() {
 		mIsFinished = false;
 		mKilledSnakes = 0;
 		mShowScore = 0;
		mSnake = new LinkedBlockingQueue<Integer>();
 		clearBoard();
 		boardAddFood();
 		newSnake();
 	}
 	@Override
 	public short[][] doStep(UserAction userAction) {
 		if (mShowScore > 0) { 
 			mShowScore--;
 			if (mShowScore == 0) {
 				clearBoard();
 				newSnake();
 				boardAddFood();
 			}
 			else
 				showScore();
 			
 			return mBoard;
 		}
 		
 		if ((mSnakeVX == 0)&&(mSnakeVY == 0))
 			return mBoard;
 		
 		int newX = mSnake.peek()%BOARD_WIDTH + mSnakeVX;
 		int newY = mSnake.peek()/BOARD_WIDTH + mSnakeVY;
 		
 		if ((newX<0)||(newX>=BOARD_WIDTH)||(newY<0)||(newY>=BOARD_HEIGHT)) {
 			killSnake();
 			return mBoard;
 		}
 		
 		if (mBoard[newY][newX] == BOARD_FOOD) {
 			mSnakeStock++;
 			boardAddFood();
 		}
 		else if (mBoard[newY][newX] != BOARD_EMPTY) {
 			killSnake();
 			return mBoard;
 		}
 		
 		
 		if (mSnakeStock>0) 
 			mSnakeStock--;
 		else {
 			int old = mSnake.remove();
 			mBoard[old/BOARD_WIDTH][old%BOARD_WIDTH] = BOARD_EMPTY;
 		}
 			
 		mSnake.add(newY*BOARD_WIDTH + newX);
 		mBoard[newX][newY] = BOARD_HEAD;
 		
 		return mBoard;
 	}
 
 }
