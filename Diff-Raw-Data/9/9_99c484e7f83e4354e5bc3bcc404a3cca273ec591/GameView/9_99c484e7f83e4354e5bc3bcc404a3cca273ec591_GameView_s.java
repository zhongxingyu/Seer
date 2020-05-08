 package edu.mines.csci498.snake;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Point;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 
 public class GameView extends BoardView implements View.OnTouchListener {
 
 	private TextView mStatus;
 
 	/* Hold the state of the game
 	 * MENU - Menu screen (show options)
 	 * GAME - Game screen (show game)
 	 * LOSE - Lost game (show lost game screen)
 	 */
 	private int mState = MENU;
 	public static final int MENU = 0;
 	public static final int GAME = 1;
 	public static final int LOSE = 2;
 	
 	//Score & move delay
 	private int mNumberOfApples = 0;
	private long mSpeed = 1000;
 	private long mLastTick;
 	
 	 /* 
 	  * Hold the direction, next direction (for collision detection)
 	  */
 	private int mDirection;
 	private int mNextDirection; //Initialized to be same as mDirection
 	public static final int NORTH = 0;
 	public static final int EAST = 1;
 	public static final int SOUTH = 2;
 	public static final int WEST = 3;
 
 	//The snake, apples & obstacles
 	private List<Point> mSnake;
 	private List<Point> mApples;
 	private List<Point> mObstacles;
 	
 	//Resources
 	public static final int DEFAULT_TILE = 0;
 	public static final int SNAKE_BODY = 1;
 	public static final int SNAKE_HEAD = 2;
 	public static final int GAME_APPLE = 3;
 	public static final int GAME_WALL = 4;
 	public static final int GAME_OBSTACLE = 5;
 	
 	private RefreshHandler mRefreshHandler = new RefreshHandler(this);
 	private final Random random = new Random();
 	/*
 	 * This inner class is used to mimic animation by passing delayed empty messages to mimic a redraw cycle.
 	 * I found this on the official android documentation in one of their demos
 	 */
     static class RefreshHandler extends Handler {
     	private GameView game;
     	
     	public RefreshHandler(GameView handle) {
     		this.game = handle;
     	}
     	
         @Override
         public void handleMessage(Message msg) {
             game.update();
             game.invalidate();
             Log.i("snake", "refreshing game");
         }
 
         public void sleep(long delayMillis) {
         	this.removeMessages(0);
             sendMessageDelayed(obtainMessage(0), delayMillis);
         }
     };
     
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs, 25);
 		init();
 	}
 	
 	//Init a new game
 	public void init() {
 		mSnake = new ArrayList<Point>();
 		mApples = new ArrayList<Point>();
 		mObstacles = new ArrayList<Point>();
 
 		newGame();
 	}
 	
 	public void newGame() {
 		mSnake.clear();
 		mApples.clear();
 		mObstacles.clear();
 		
 		loadResources();
 		setMode(MENU);
 	}
 	
 	public void initSnake() {
 		//add beginning snake body & direction
 		mSnake.add(new Point(4, 10));
 		mSnake.add(new Point(5, 10));
 		mSnake.add(new Point(6, 10));
 		mSnake.add(new Point(7, 10));
 		mSnake.add(new Point(8, 10));
 		
 		mDirection = NORTH;
 		mNextDirection = NORTH;
 		
 		addRandomApple();
 		addRandomObstacle();
 	}
 	
 	public void addRandomApple() {
 		Point newPos = null;
 		boolean collision = false;
 		while (!collision) {
 			//Add 1 to not add an apple in the wall. Subtract 2 to not look at the wall.
 			int newX = 1 + random.nextInt(mNumberTilesX -2);
 			int newY = 1 + random.nextInt(mNumberTilesY -2);
 			newPos = new Point(newX, newY);
 			
 			collision = !( getCollidePoint(newPos, mSnake) != null || getCollidePoint(newPos, mObstacles) != null);
 		}
 		mApples.add(newPos);
 	}
 	
 	public void addRandomObstacle() {
 		Point newPos = null;
 		boolean collision = false;
 		while (!collision) {
 			int newX = 1 + random.nextInt(mNumberTilesX -2);
 			int newY = 1 + random.nextInt(mNumberTilesY -2);
 			newPos = new Point( newX, newY );
 			
 			collision = !( getCollidePoint(newPos, mSnake) != null || getCollidePoint(newPos, mApples) != null);
 		}
 		mObstacles.add(newPos);
 	}
 	
 	private Point getCollidePoint(Point pos, List<Point> compareList) {
 		boolean collide = false;
 		for (Point p : compareList)
 			if (p.equals(pos))
 				return p;
 		return null;
 	}
 	
 	
 	public void loadResources() {
 		
 		createNewBitmapArray(6);
 		//loadResource(key, resource)
 		loadResource(DEFAULT_TILE, getContext().getResources().getDrawable(R.drawable.default_tile));
 		loadResource(SNAKE_BODY, getContext().getResources().getDrawable(R.drawable.snake_body));
 		loadResource(SNAKE_HEAD, getContext().getResources().getDrawable(R.drawable.snake_head));
 		loadResource(GAME_APPLE, getContext().getResources().getDrawable(R.drawable.apple));
 		loadResource(GAME_WALL, getContext().getResources().getDrawable(R.drawable.wall));
 		loadResource(GAME_OBSTACLE, getContext().getResources().getDrawable(R.drawable.obstacle));
 		Log.i("snake", "Done loading resources");
 	}
 	
 	public void setMode(int mode) {
 		this.mState = mode;
 		if (mState == GAME) {
 			mLastTick = System.currentTimeMillis();
 			mStatus.setText("");
 			update();
 			return;
 		}
 	}
 	
 	public void setStatusTextView(TextView statusText) {
 		mStatus = statusText;
 	}
 	
 	/*
 	 * This update mechanic was also found in an example from the documentation
 	 */
 	public void update() {
 		if (mState == GAME) {
 			long timer = System.currentTimeMillis();
 			Log.i("snake", "checking if we need to update");
 			if (timer - mLastTick > mSpeed) {
 				clearTiles();
 				addWalls();
 				addSnake();
 				addApples();
 				addObstacles();
 				mLastTick = timer;
 			}
 			mRefreshHandler.sleep(mSpeed);
 		}
 	}
 	
 	public void addWalls() {
 		//Horizontal walls
 		for (int i = 0; i < mNumberTilesX; ++i) {
 			setTile(i, 0, GAME_WALL);
 			setTile(i, mNumberTilesY -1, GAME_WALL);
 		}
 		
 		//Vertical walls
 		for (int i = 0; i < mNumberTilesY; ++i) {
 			setTile(0, i, GAME_WALL);
 			setTile(mNumberTilesX -1, i, GAME_WALL);
 		}
 		
 	}
 	
 	public void addObstacles() {
 		for (Point p : mObstacles)
 			setTile(p.x, p.y, GAME_OBSTACLE);
 	}
 	
 	public void addApples() {
 		for (Point p : mApples)
 			setTile(p.x, p.y, GAME_APPLE);
 	}
 	
 	public void addSnake() {
 		Point head = mSnake.get(0);
 		Point nextPosition = new Point(head);		
 		
 		mDirection = mNextDirection;
 		//Calculate next position based off mNextDirection
 		if (mDirection == NORTH) {
 			nextPosition.y = head.y - 1;
 		} else if (mDirection == EAST) {
 			nextPosition.x = head.x + 1;
 		} else if (mDirection == SOUTH) {
 			nextPosition.y = head.y + 1;
 		} else if (mDirection == WEST) {
 			nextPosition.x = head.x - 1;
 		}
 		
 		//Check boundary collisions
 		if (nextPosition.x < 1 || nextPosition.x > (mNumberTilesX - 2)) {
 			//Collision against vertical walls
 			setMode(LOSE);
 			return;
 		}
 		if (nextPosition.y < 1 || nextPosition.y > (mNumberTilesY -2)) {
 			//Collision against horizontal walls
 			setMode(LOSE);
 			return;
 		}
 		
 		//Check collisions against obstacles
 		if (getCollidePoint(nextPosition, mObstacles) != null) {
 			setMode(LOSE);
 			return;
 		}
 		
 		//Check collisions against itself
 		if (getCollidePoint(nextPosition, mSnake) != null) {
 			setMode(LOSE);
 			return;
 		}
 		
 		boolean grow = false;
 		//Check if we just ate an apple
 		Point whichApple;
 		if ((whichApple = getCollidePoint(nextPosition, mApples)) != null) {
 			grow = true;
 			mApples.remove(whichApple);
			mSpeed *= .5;
 			mNumberOfApples++;
 		}
 		
 		//if no collision, add nextPosition as head. If we are *not* growing, remove the tail too
 		mSnake.add(0, nextPosition);
 		if (!grow) {
 			mSnake.remove(mSnake.size() -1);
 			addRandomApple();
 		}
 		
 		setTile(nextPosition.x, nextPosition.y, SNAKE_HEAD);
 		for (int i = 1; i < mSnake.size(); ++i) {
 			Point pos = mSnake.get(i);
 			setTile(pos.x, pos.y, SNAKE_BODY);
 		}
 		
 	}
 	
 	/**
 	 * Detects which 'zone' the user tapped with the help of a site and some math. We split the screen
 	 * into two main diagonals like so:
 	 * 
 	 * -------------------
 	 * |\                /|
 	 * | \              / |
 	 * |  \     1      /  |
 	 * |   \          /   |
 	 * |    \        /    |
 	 * |     \      /     |
 	 * |      \    /      |
 	 * |       \  /       |
 	 * |  2     \/    3   |
 	 * |        /\        |
 	 * |       /  \       |
 	 * |      /    \      | 	
 	 * |     /      \     | 	
 	 * |    /   4    \    | 	
 	 * |   /          \   | 	
 	 * |  /            \  | 
 	 * -------------------
 	 */
 	public boolean onTouch(View v, MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 		float width = getWidth();
 		float height = getHeight();
 		float slope = height/width;
 		float mainDiagonal = slope*x;
 		float negativeDiagonal = -slope*x + height;
 		Log.i("snake", "getting touch input y: " + String.valueOf(y)
 				+ "slope: " + String.valueOf(slope)
 				+ "diag: " + String.valueOf(mainDiagonal)
 				+ "negDia: " + String.valueOf(negativeDiagonal));
 		//See if user has released their screen tap
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			if ((y < mainDiagonal) && (y < negativeDiagonal)) {
 				//Quadrant 1 - UP
 				Log.i("snake", "Touch event: UP");
 				if (mState == MENU || mState == LOSE) {
 					//start new Game
 					initSnake();
 					setMode(GAME);
 					update();
 					return true;
 				}
 				if (mDirection != SOUTH)
 					mNextDirection = NORTH;
 				return true;
 			} else if ((y > mainDiagonal) && (y > negativeDiagonal)) {
 				//Quadrant 4 - DOWN
 				Log.i("snake", "Touch event: DOWN");
 				if (mDirection != NORTH)
 					mNextDirection = SOUTH;
 				return true;
 			} else if ((y > mainDiagonal) && (y < negativeDiagonal)) {
 				//Quadrant 2 - LEFT
 				Log.i("snake", "Touch event: LEFT");
 				if (mDirection != EAST)
 					mNextDirection = WEST;
 				return true;
 			} else if ((y < mainDiagonal) && (y > negativeDiagonal)) {
 				//Quadrant 3 - RIGHT
 				Log.i("snake", "Touch event: RIGHT");
 				if (mDirection != WEST)
 					mNextDirection = EAST;
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 }
