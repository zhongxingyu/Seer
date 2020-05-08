 package com.hungry_bubbles;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 /**
  * Manages the game logic.
  * 
  * @author Timothy Heard, Shaun DeVos, John O'Brien, Mustafa Al Salihi
  */
 public class GameBoard extends View
 {
 	// TODO: Uncomment or remove
 	// The maximum number of update requests which can be pending at one time
 	//private static final int REQUEST_QUEUE_CAPACITY = 30;
 
 	// The maximum amount of time that the UI thread will wait
 	// when attempting to get exclusive access to the current
 	// update requests (in milliseconds)
 	private static final long MAX_UPDATE_WAIT_TIME = 500;
 
 	// Tag which will appear as a part of any log messages generated within 
 	// this class
 	private static final String TAG = "GameBoard";
 
 	private static final String WIN_MESSAGE = "Congratulations! You Won";
 	private static final String LOSE_MESSAGE = "You were eaten!";
 
 	private static final int SMALLER_THAN_PLAYER_COLOR = Color.BLUE;
 	private static final int LARGER_THAN_PLAYER_COLOR = Color.RED;
 	private static final int BUBBLE_STARTING_COLOR = Color.WHITE;
 	
 	private HungryBubblesActivity hostActivity;
 	private BubbleData playerData;
 	private Map<BubbleThread, BubbleData> opponentData;
 	
 	// Responsible for continuously starting new opponent BubbleThreads 
 	// throughout the game
 	private BubbleFactory bubbleFactory;
 	
 	// The AppInfo instance which represents the application and manages
 	// general game statistics and information
 	private AppInfo appInfoInstance;
 	
 	// TODO: Uncomment or remove
 	//private BlockingQueue<UpdateRequest> updateRequests;
 	
 	private NonBlockingReadQueue<UpdateRequest> updateRequests;
 	
 	private int screenWidth;
 	private int screenHeight;
 	private int boardWidth;
 	private int boardHeight;
 	
 	private boolean initialized;
 	private boolean playerAlive;
 	
 	// Keeps track of the location of the last touch event. These variables
 	// will be set to a negative value if the player is not currently
 	// moving their piece
 	private float lastTouchX, lastTouchY;
 	
 	// Set to true whenever the player presses down on top of the player
 	// bubble piece and then set back to false when the player lifts their
 	// finger
 	private boolean playerTouchActive; 
 	
 	public GameBoard(HungryBubblesActivity hostActivity)
 		throws IllegalArgumentException
 	{
 		// The host activity serves as the Context for this view
 		super(hostActivity);
 		
 		GameUtils.throwIfNull("hostActivity", "GameBoard", hostActivity);
 		
 		this.hostActivity = hostActivity;
 
 		appInfoInstance = (AppInfo) hostActivity.getApplication();
 		
 		// Player is considered to be alive until it is eaten by another bubble
 		// even though the player's game piece has not been initialized yet;
 		// initialization is deferred until the first UI update because the
 		// player's starting position is dependent on the size of the physical
 		// screen which will not be know until the UI is ready to be drawn.
 		playerAlive = true;		
 		playerData = null;
 		
 		bubbleFactory = null;
 		opponentData = new HashMap<BubbleThread, BubbleData>();
 		
 		// TODO: Remove this if NonBlockingReadQueue works
 		//
 		// Create a blocking queue which can hold up to 
 		// REQUEST_QUEUE_CAPACITY update requests and enforces fairness (i.e. 
 		// all threads which are attempting to add requests to the queue will
 		// be guaranteed the opportunity to do so at some point)
 		//updateRequests = new ArrayBlockingQueue<UpdateRequest>(
 			//REQUEST_QUEUE_CAPACITY, true);
 		
 		updateRequests = new NonBlockingReadQueue<UpdateRequest>();
 		
 		initialized = false;
 		playerTouchActive = false;
 	}
 
 	/**
 	 * Ends the current game.
 	 * 
 	 * @throws IllegalStateException	If no {@link GameView} has been 
 	 * 									registered with this {@link GameBoard}
 	 * 									yet
 	 */
 	private void endGame()
 		throws IllegalStateException
     {
 		String message;
 		
 		if(playerAlive)
 		{
 			message = WIN_MESSAGE;
 			appInfoInstance.endGame(true);
 		}
 		else
 		{
 			message = LOSE_MESSAGE;
 			appInfoInstance.endGame(false);
 		}
 		
 	    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
 	    dialogBuilder.setMessage(message);
 	    dialogBuilder.setPositiveButton("Play Again", 
 	    	new DialogInterface.OnClickListener()
 			{
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 					// Restart game
 					hostActivity.startGame();
 				}
 			});
 	    
 	    dialogBuilder.setNegativeButton("Quit", 
 	    	new DialogInterface.OnClickListener()
 			{
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 					hostActivity.quit();
 				}
 			});
 	    
 	    dialogBuilder.create().show();
     }
 
 
 	/**
 	 * Draws the current game board on the provided {@link Canvas}.
 	 */
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 
 		if(!initialized)
 		{
 			// Stores the size of the physical screen. This value only needs
 			// to be retrieved once since this activity is locked in horizontal
 			// orientation
 			screenWidth = canvas.getWidth();
 			screenHeight = canvas.getHeight();
 			
 			// The actual, effective size of the game board is MAX_RADIUS
 			// units larger than the physical screen in both directions. This
 			// additional area is used as a spawning area for new 
 			// computer-controlled bubbles so that bubbles will not spawn on 
 			// top of the player.
 			boardWidth = screenWidth + (2 * AppInfo.MAX_RADIUS); 
 			boardHeight = screenHeight + (2 * AppInfo.MAX_RADIUS);
 			
 			// Start the player centered in the screen (accounting for the
 			// virtual padding which is used as a spawning area for opponent
 			// bubbles)
 			float playerX = (screenWidth / 2) + AppInfo.MAX_RADIUS;
 			float playerY = (screenHeight / 2) + AppInfo.MAX_RADIUS; 
 			playerData = new BubbleData(Color.BLACK, playerX, playerY,
 				AppInfo.PLAYER_STARTING_RADIUS);
 			
 			this.bubbleFactory = new BubbleFactory(updateRequests, 
 				screenHeight, screenWidth, AppInfo.MAX_RADIUS);
 			
 			initialized = true;
 			playerAlive = true;
 		}
 
 		applyUpdates();
 		resolveCollisions();
 		updateBubbleColors();
 		startBubbleIfAppropriate();
 		
 		if(!playerAlive)
 		{
 			endGame();
 		}
 		else
 		{
 			drawPlayer(canvas);
 			drawOppponents(canvas);
 		}
 	}
 
 	/**
 	 * Handles a the {@link MotionEvent} generated as a result of a player 
 	 * touch interaction.
 	 * 
 	 * @return	{@code true} if the {@link MotionEvent} was handled 
 	 * 			successfully and false otherwise. 
 	 * 
 	 * @throws IllegalStateException	If no {@link GameView} has been 
 	 * 									registered with this {@link GameBoard}
 	 * 									yet.
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent e)
 	{
 		super.onTouchEvent(e);
 		
 		int eventAction = e.getAction();
 		boolean eventHandled = false;
 		
 		if(eventAction == MotionEvent.ACTION_DOWN)
 		{
 			eventHandled = handleActionDownEvent(e);
 		}
 		else if(eventAction == MotionEvent.ACTION_UP)
 		{
 			eventHandled = handleActionUpEvent(e);
 		}
 		else if(playerTouchActive)
 		{
 			eventHandled = handleContiuedTouchEvent(e);
 		}
 
 		return eventHandled;
 	}
 

		

 	public int getScreenWidth()
 	{
 		return screenWidth;
 	}
 	
 	public  int getScreenHeight()
 	{
 		return screenHeight;
 	}
 	
 	public int getBoardWidth()
 	{
 		return boardWidth;
 	}
 	
 	public int getBoardHeight()
 	{
 		return boardHeight;
 	}
 	
 	/**
 	 * Handle {@link MotionEvent}s triggered by the player pressing down on
 	 * the screen.
 	 * 
 	 * @param e		The {@link MotionEvent} triggered by the player touching
 	 * 				the device screen.
 	 * 
 	 * @return		{@code true} if the {@link MotionEvent} was handled and
 	 * 				{@code false} otherwise (i.e. if the player touched a
 	 * 				location outside of the area of the screen currently
 	 * 				occupied by the player's bubble).
 	 */
 	private boolean handleActionDownEvent(MotionEvent e)
 	{	
 		if(!isValidPlayerTouch(e))
 		{
 			return false;
 		}
 		
 		playerTouchActive = true;
 		
 		float touchX = e.getX();
 		float touchY = e.getY();
 		
 		// Adds on the virtual padding so that the player's data can be 
 		// handled in a uniform manner with the data for the opponent 
 		// bubbles which are created on a conceptual coordinate grid which 
 		// adds an AppInfo.MAX_RADIUS padding around the physical screen
 		BubbleData newPlayerData = BubbleData.updatePosition(playerData, 
 			touchX + AppInfo.MAX_RADIUS, touchY + AppInfo.MAX_RADIUS);
 		
 		if(outOfBounds(newPlayerData.getX(), newPlayerData.getY(), newPlayerData.getRadius()))
 		{
 			// The MotionEvent was handled, but since the player cannot move 
 			// off the screen the player position will not be updated, but
 			// the position of the touch point is recorded so keep player
 			// movement in sync with the motion of the player's finger.
 			lastTouchX = touchX;
 			lastTouchY = touchY;
 			
 			return true;
 		}
 
 		playerData = newPlayerData;
 		
 		// Causes the game view to be redrawn
 		invalidate();
 		
 		// It is important that these values be updated after the the view
 		// is invalidated (by calling view.invalidate()) so that these values
 		// will not be used when redrawing the view
 		lastTouchX = touchX;
 		lastTouchY = touchY;
 		
 		return true;
 	}
 	
 	/**
 	 * Handle {@link MotionEvent}s triggered by the player lifting their finger
 	 * off of the screen.
 	 * the screen.
 	 * 
 	 * @param e		The {@link MotionEvent} triggered by the player lifting 
 	 * 				their finger after touching	the device screen.
 	 * 
 	 * @return		Will always return {@code true} (i.e. this type of 
 	 * 				{@link MotionEvent} will always be handled).
 	 */
 	private boolean handleActionUpEvent(MotionEvent e)
 	{
 		playerTouchActive = false;
 		return true;
 	}
 	
 	/**
 	 * Handles {@link MotionEvent}s which result from the player holding their finger 
 	 * down on the screen and moving it across the screen (i.e. all of the
 	 * touch related {@link MotionEvent}s which are fired between ACTION_DOWN
 	 * and ACTION_UP {@link MotionEvent}s) 
 	 * 
 	 * @param e		The {@link MotionEvent} fired as a result of continued 
 	 * 				touch interaction after initial contact with the screen
 	 *  
 	 * @return		{@code true} if the {@link MotionEvent} was handled and
 	 * 				{@code false} otherwise.
 	 */
 	private boolean handleContiuedTouchEvent(MotionEvent e)
 	{
 		// If there are no valid coordinates available for the last touch 
 		// event then treat this the same as if the player had just touched
 		// the screen (an ACTION_DOWN event)
 		if(lastTouchX < 0 || lastTouchY < 0)
 		{
 			return handleActionDownEvent(e);
 		}
 
 		float touchX = e.getX();
 		float touchY = e.getY();
 
 		float changeInX = touchX - lastTouchX;
 		float changeInY = touchY - lastTouchY;
 		
 		// Move the player by the same amount that the player's touch point
 		// moved. Continuously moving the player bubble by the delta in touch
 		// point locations instead of continuously checking to see if the
 		// player touched inside the player bubble and moving the player
 		// bubble to be centered on the touch point location makes the
 		// touch interaction more responsive as the UI does not always
 		// update as fast as the player moves their finger, which results
 		// in the player's finger "slipping" off of the bubble
 		BubbleData newPlayerData = BubbleData.updatePosition(playerData, 
 			playerData.getX() + changeInX, playerData.getY() + changeInY);
 			
 		if(outOfBounds(newPlayerData.getX(), newPlayerData.getY(), newPlayerData.getRadius()))
 		{
 			// The MotionEvent was handled, but since the player cannot move 
 			// off the screen the player position will not be updated, but
 			// the position of the touch point is recorded so keep player
 			// movement in sync with the motion of the player's finger.
 			lastTouchX = touchX;
 			lastTouchY = touchY;
 			
 			return true;
 		}
 		
 		playerData = newPlayerData;
 		
 		// Causes the UI to be redrawn
 		invalidate();
 		
 		// It is important that these values be updated after the the view
 		// is invalidated (by calling view.invalidate()) so that these values
 		// will not be used when redrawing the view
 		lastTouchX = touchX;
 		lastTouchY = touchY;
 		
 		return true;
 	}
 	
 	// TODO: Remove
 	/**
 	 * Sets the {@link BubbleFactory} to be used by this {@code GameBoard} to
 	 * start new {@link BubbleThreads}, each of which represents a different
 	 * opponent bubble, and starts that {@link BubbleFactory} running in its
 	 * own {@link Thread}. 
 	 */
 	/*
 	private void setAndStartFactory(BubbleFactory bubbleFactory)
 	{
 		this.bubbleFactory = bubbleFactory;
 		this.bubbleFactoryThread = new Thread(this.bubbleFactory);
 		
 		// TODO: Uncomment once BubbleFactory is actually implemented
 		//bubbleFactoryThread.start();
 	}
 	*/
 
 	/**
 	 * Goes through all of the "bubbles" which are currently active in the game
 	 * space and handles any collisions, with the larger bubble involved in the 
 	 * collision consuming the smaller one, with consumed bubbles mass behind 
 	 * added to the mass of the consumer (up to a the maximum bubble size). The
 	 * player wins ties and ties between two computer-driven bubbles is 
 	 * non-deterministic (i.e. one of the bubbles will eat the other, but there
 	 * is no guarantee which one it will be). 
 	 */
 	private void resolveCollisions()
     {
 		List<BubbleThread> deadThreads = new ArrayList<BubbleThread>();
 		
 		for(BubbleThread bubbleThread: opponentData.keySet())
 		{
 			BubbleData opponentPosition = opponentData.get(bubbleThread);
 			
 			if(BubbleData.bubblesAreTouching(playerData, opponentPosition))
 			{
 				// Note that the player wins in the case of ties
 				if(playerData.getRadius() >= opponentPosition.getRadius())
 				{
 					playerData = BubbleData.consume(playerData, 
 						opponentPosition);
 					deadThreads.add(bubbleThread);
 				}
 				else
 				{
 					opponentData.put(bubbleThread, 
 						BubbleData.consume(opponentPosition, playerData));
 					
 					playerAlive = false;
 					break;
 				}
 			}
 		}
 		
 		for(BubbleThread bubbleThread: deadThreads)
 		{
 			// Tell the BubbleThread that it was "eaten" and then remove it 
 			// from the collection of active bubbles 
 			bubbleThread.wasEaten();
 			opponentData.remove(bubbleThread);
 		}
 		
 		// Check to see if any of the computer-controlled bubbles have
 		// collided with each other
 		Object[] bubbleThreads = opponentData.keySet().toArray(); 
 		List<Integer> deadThreadIndices = new ArrayList<Integer>();
 		for(int i = 0; i < bubbleThreads.length; i++)
 		{
 			if(deadThreadIndices.contains(i))
 			{
 				continue;
 			}
 			
 			for(int j = i + 1; j < bubbleThreads.length; j++)
 			{
 				if(deadThreadIndices.contains(j))
 				{
 					continue;
 				}
 				
 				BubbleData bubble1 = opponentData.get((BubbleThread) bubbleThreads[i]); 
 				BubbleData bubble2 = opponentData.get((BubbleThread) bubbleThreads[j]); 
 				if(BubbleData.bubblesAreTouching(bubble1, bubble2))
 				{
 					// Note that the player wins in the case of ties
 					if(bubble1.getRadius() >= bubble2.getRadius())
 					{
 						opponentData.put((BubbleThread) bubbleThreads[i], 
 							BubbleData.consume(bubble1, bubble2));
 						
 						deadThreadIndices.add(j);
 					}
 					else
 					{
 						opponentData.put((BubbleThread) bubbleThreads[j], 
 							BubbleData.consume(bubble2, bubble1));
 						
 						deadThreadIndices.add(i);
 					}
 				}
 				
 			}
 		}
 		
 		for(Integer index: deadThreadIndices)
 		{
 			// Tell the BubbleThread that it was "eaten" and then remove it 
 			// from the collection of active bubbles
 			BubbleThread bubbleThread = (BubbleThread) bubbleThreads[index];
 			bubbleThread.wasEaten();
 			opponentData.remove(bubbleThread);
 		}
     }
 
 	/**
 	 * Applies any pending updates to bubble positions, ignoring any positions 
 	 * which are currently locked. 
 	 */
 	private void applyUpdates()
     {
 		try
         {
 	        List<UpdateRequest> updatesToApply = 
 	        	updateRequests.nonblockingPop(MAX_UPDATE_WAIT_TIME);
 	        
 	        if(updatesToApply == null || updatesToApply.size() == 0)
 	        {
 	        	// TODO: Remove
 	        	Log.d(TAG, "No updates to apply.");
 	        	//
 	        	
 	        	return;
 	        }
 	        
         	// TODO: Remove
         	Log.d(TAG, "Applying updates.");
         	//
 	        
 	        for(UpdateRequest request: updatesToApply)
 	        {
 	        	if(!opponentData.containsKey(request.getRequester()))
 	        	{
 	        		// If the BubbleThread making the request for a UI update 
 	        		// is not contained in the opponentData map then the bubble
 	        		// controlled by that thread must have been eaten and this
 	        		// UI update request should be skipped
 	        		continue;
 	        	}
 	        	
 	        	opponentData.put(request.getRequester(), request.getNewPosition());
 	        }
 	        
         } 
 		catch (InterruptedException e)
         {
         	Log.d(TAG, "InterruptedException was thrown while attempting to " +
         		"apply pending UI updates. ");
         	e.printStackTrace();
         }
     }
 
 	/**
 	 * Update the colors of all of the bubbles currently on the board, with
 	 * all bubbles which are smaller than the player's bubble being made blue
 	 * and all the bubbles which are larger being made red.
 	 */
 	private void updateBubbleColors()
 	{
 		for(BubbleThread bubbleThread: opponentData.keySet())
 		{
 			BubbleData currentBubbleData = opponentData.get(bubbleThread);
 			BubbleData recoloredBubbleData;
 			if(currentBubbleData.getRadius() <= playerData.getRadius())
 			{
 				recoloredBubbleData = BubbleData.updateColor(currentBubbleData,
 					SMALLER_THAN_PLAYER_COLOR);
 			}
 			else
 			{
 				recoloredBubbleData = BubbleData.updateColor(currentBubbleData,
 					LARGER_THAN_PLAYER_COLOR);
 			}
 			
 			opponentData.put(bubbleThread, recoloredBubbleData);
 		}
 	}
 	
 	/**
 	 * Checks a number of different conditions including the number of opponent
 	 * bubbles which are currently active to determine whether or not another 
 	 * bubble should be added, and if it is determined that conditions are 
 	 * appropriate for adding another opponent bubble to the game then one will
 	 * be started using the {@code GameBoard}'s {@link BubbleFactory} instance.
 	 */
 	private void startBubbleIfAppropriate()
 	{
 		if(opponentData.size() < AppInfo.MAX_BUBBLES)
 		{
 			bubbleFactory.startNewBubble(BUBBLE_STARTING_COLOR);
 		}
 	}
 	
 	/**
 	 * Used to determine whether or not the point with the given x and y
 	 * coordinates (which include virtual padding for the bubble spawning zone)
 	 * is located on the physical screen.
 	 */
 	private boolean outOfBounds(float virtualX, float virtualY, float radius)
 	{
 		float x = virtualX - AppInfo.MAX_RADIUS;
 		float y = virtualY - AppInfo.MAX_RADIUS;
 		
 		if((x + radius < 0) || (x - radius < 0) || (x + radius > screenWidth) || (x - radius > screenWidth) ||
 		   (y + radius < 0) || (y - radius < 0) || (y + radius > screenHeight) || (y - radius > screenHeight))
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Determines whether or not the {@link MotionEvent} which resulted from a
 	 * user touch action is valid (i.e. is contained within the player's 
 	 * "bubble").
 	 * 
 	 * @param 	e	The {@link MotionEvent} caused by the player touching the 
 	 * 				screen.
 	 * 
 	 * @return	{@code true} if the player touched within their "bubble" and 
 	 * {@code false} otherwise.	
 	 */
 	private boolean isValidPlayerTouch(MotionEvent e)
 	{
 		// Account for the virtual padding
 		float eventX = e.getX() + AppInfo.MAX_RADIUS;
 		float eventY = e.getY() + AppInfo.MAX_RADIUS;
 		
 		int playerRadius = playerData.getRadius();
 		
 		if((Math.abs(playerData.getX() - eventX) <= playerRadius) && 
 		   (Math.abs(playerData.getY() - eventY) <= playerRadius) &&
 		   !outOfBounds(eventX, eventY, playerRadius))
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void drawBubble(Canvas canvas, BubbleData data)
 	{
 		Paint circlePaint = new Paint();
 		circlePaint.setColor(data.getColor());
 		
 		// Translate between the virtual coordinates which include a 
 		// AppInfo.MAX_RADIUS conceptual padding around the physical screen
 		// in order to provide the opponent bubbles with a place to spawn
 		// without being able to spawn on top of the player.
 		float bubbleX = data.getX() - AppInfo.MAX_RADIUS;
 		float bubbleY = data.getY() - AppInfo.MAX_RADIUS;
 		
 		canvas.drawCircle(bubbleX, bubbleY, data.getRadius(), circlePaint);
 	}
 	
 	private void drawPlayer(Canvas canvas)
 	{
 		drawBubble(canvas, playerData);
 	}
 
 	private void drawOppponents(Canvas canvas)
     {
 		for(BubbleData bubbleData: opponentData.values())
 		{
 			drawBubble(canvas, bubbleData);
 		}
     }
 	
 	// TODO: Remove
 	/**
 	 * Factory method for initializing a {@code GameBoard} object, including 
 	 * starting all active components (threads) which the board uses to drive
 	 * game mechanics.
 	 * 
 	 * @param hostActivity	The {@link Activity} which is hosting this 
 	 * 						{@code GameBoard}
 	 *  
 	 * @return	A fully initialized and active {@code GameBoard}.
 	 */
 	/*
 	public static GameBoard makeActiveGameBoard(HungryBubblesActivity hostActivity)
 	{
 		GameBoard board = new GameBoard(hostActivity);
 		return board;
 	}
 	*/
 }
