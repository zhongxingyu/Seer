 package com.example.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.CountDownTimer;
 import android.util.Log;
 import com.example.phonepet.SleepActivity;
 import com.example.utils.Point;
 import com.example.utils.RunawayCountdownTimer;
 import com.example.phonepet.AccessorizeActivity;
 import com.example.phonepet.CleanActivity;
 import com.example.phonepet.R;
 import com.example.phonepet.RunawayActivity;
 import com.example.views.HomeView;
 import com.example.vos.Food;
 import com.example.vos.PetVo;
 
 /*
  * Remember, the model is nothing more than a glorified name-value object
  * and a good view is dumb and just updates when the model changes and 
  * does whatever the controller says.
  * 
  * The controller does 3 things:
  * 1. Updates the model
  * 2. Handles messages from the view
  * 3. Sends messages to the view.
  * 
  */
 
 public class PetController extends Controller {
 	public static final int MESSAGE_LOAD = 0;
 	public static final int MESSAGE_MODEL_UPDATED = 1;
 	public static final int MESSAGE_ACCESSORIZE = 2;
 	public static final int MESSAGE_SCOOP_POOP = 3;
 	public static final int MESSAGE_FEED = 4;
 	public static final int MESSAGE_CLEAN = 5;
 	public static final int MESSAGE_PLAY = 6;
 	public static final int MESSAGE_KEY_EVENT = 7;
 	public static final int MESSAGE_TAPPED = 8;
 	public static final int MESSAGE_STOP_LIFE = 9;
 	public static final int MESSAGE_RESUME_LIFE = 10;
 	public static final int MESSAGE_PET_RETURNING = 11;
 	public static final int MESSAGE_PET_RUNAWAY = 12;
 	public static final int MESSAGE_TEST_BUTTON_CLICKED = 13;
 	public static final int MESSAGE_TEST_BUTTON_HELD = 14;
 	public static final int MESSAGE_SET_RUNAWAY_TIMER = 15;
 	public static final int MESSAGE_SET_SLEEP_TIMER = 16;
 	
 	/*
 	 * Phone screen sizes are different, these constants are used to handle this.
 	 */
 	private int BACKGROUND_WIDTH;
 	private int BACKGROUND_HEIGHT;
 	
 	// AREA values represent the rectangle contained in the fence.
 	// Pet can NEVER move outside of AREA.
 	private int AREA_MIN_Y; // 2/3 of background height
 	
 	private int WIDTH_OF_HOUSE; // Width of pet house is 6/13 of BACKGROUND_WIDTH; 
 	private int CENTER_HOUSE_X;	// The center of the opening of the pet house width is 7/10 of background width
 	private int BOTTOM_HOUSE_Y; // The bottom of pet house height is 7/10 background height.
 	private int LEFT_HOUSE_X; // Left x coord of house is CENTER_HOUSE_X - (WIDTH_OF_HOUSE/2)
 	private int RIGHT_HOUSE_X; // Right x coord of house is CENTER_HOUSE_X + (WIDTH_OF_HOUSE/2)
 	private int LEFT_HOUSE_DOOR_X; // x coord of leftmost part of house door is LEFT_OF_HOUSE + 1/3 WIDTH_OF_HOUSE
 	private int RIGHT_HOUSE_DOOR_X; // x coord of rightmost part of house door is LEFT_OF_HOUSE + 2/3 WIDTH_OF_HOUSE
 	private int TOP_HOUSE_DOOR_Y; // y coord of topmost part of house door is BOTTOM_HOUSE_Y - WIDTH_OF_HOUSE/2
 	
 	String fileName = "preferences";
 	
 	private PetVo model;
 	private PetLife life;
 	private Context homeContext;
 	private SharedPreferences sharedPref;
 	
 	private boolean isMovingToFood = false;
 	
 	private RunawayCountdownTimer runawayTimer, sleepTimer;
 	
 	public PetController(PetVo model, Context hActivityContext)
 	{
 		this.model = model;
 		homeContext = hActivityContext;
 		sharedPref = homeContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
 	}
 	
 	public PetVo getModel()
 	{
 		return model;
 	}
 	
 	public Context getHomeContext()
 	{
 		return this.homeContext;
 	}
 	
 	
 	/**
 	 * This is the message system used to communicate between view and controller. The view needs
 	 * a synchronous response to whether or not the event was handled.
 	 * 
 	 * @param what: Represents which action should be taken
 	 * @param data: Generic data object that can be used to send extra information
 	 * 				along with the message.
 	 */
 	@Override
 	public boolean handleMessage(int what, Object data) {
 		switch (what) {
 		case MESSAGE_LOAD:
 			loadPet();
 			return true;
 		case MESSAGE_ACCESSORIZE:
 			accessorize();
 			return true;
 		case MESSAGE_SCOOP_POOP:
			scoopPoop();
 			return true;
 		case MESSAGE_FEED:
 			Log.v("message feed", " call move pet to food");
 			movePetToFood((Food)data);
 			return true;
 		case MESSAGE_CLEAN:
			clean();
 			return true;
 		case MESSAGE_PLAY:
			play();
 			return true;
 		case MESSAGE_TAPPED:
 			handleTap(data);
 			return true;
 		case MESSAGE_PET_RETURNING:
 			model.setPetIsHome(true);
 			return true;
 		case MESSAGE_SET_RUNAWAY_TIMER:
 			setRunawayTimer((Long)data);
 			return true;
 		case MESSAGE_SET_SLEEP_TIMER:
 			setSleepTimer();
 			return true;
 		}
 		return false;
 	}
 
 	private void handleTap(Object data)
 	{
 		// Determine what the user tapped.
 		Point point = (Point)data;
 
 		// Check if user tapped pet.
 		if (point.x >= model.getXCoord() && point.x <= model.getXCoord() + model.getWidth()
 				&& (point.y >= model.getXCoord() && point.y <= model.getYCoord() + model.getHeight())) {
 			
 				int direction = 1 + (int)(Math.random() * ((4 - 1) + 1));
 				
 				life.petMove = direction;
 				synchronized(life) {
 					life.notify();
 				}
 			}
 	} // End method handleTap
 
 	// Get pet's information
 	private void loadPet()
 	{
 		// Set constant values
 		BACKGROUND_WIDTH = sharedPref.getInt("backgroundWidth", 10);
 		BACKGROUND_HEIGHT = sharedPref.getInt("backgroundHeight", 10);
 		
 		// Bottom of fence is 2/3 of the background height.
 		AREA_MIN_Y =  (int)(BACKGROUND_HEIGHT * (2f/3f));
 		
 		// Find house dimensions
 		WIDTH_OF_HOUSE = (int)(BACKGROUND_WIDTH * (6f/13f)); // 6/13 of background width
 		CENTER_HOUSE_X = (int)(BACKGROUND_WIDTH * (7f/10f)); // 7/10 of background width
 		BOTTOM_HOUSE_Y = (int)(BACKGROUND_HEIGHT * (7f/10f)); // 7/10 of background height
 		LEFT_HOUSE_X = (int)(CENTER_HOUSE_X - WIDTH_OF_HOUSE/2);
 		RIGHT_HOUSE_X = (int)(CENTER_HOUSE_X + WIDTH_OF_HOUSE/2);
 		LEFT_HOUSE_DOOR_X = LEFT_HOUSE_X + WIDTH_OF_HOUSE / 3; // x coord of leftmost part of house door is 1/3 house width + left of house 
 		RIGHT_HOUSE_DOOR_X = LEFT_HOUSE_X + (int)(WIDTH_OF_HOUSE * (2f/3f)); // x coord of rightmost part of house door is 2/3 house width + left of house
 		TOP_HOUSE_DOOR_Y = BOTTOM_HOUSE_Y - WIDTH_OF_HOUSE/2;
 
 		// Load pet coordinates from previous app state.
 		model.loadPet(
 					sharedPref.getInt("petWidth", 0), sharedPref.getInt("petHeight", 0),
 					sharedPref.getInt("petX", 0), sharedPref.getInt("petY", 0),
 					sharedPref.getInt("petType", 0), sharedPref.getInt("petDrawable", R.drawable.orange_fox)
 					);
 		 
 		// Create and start a thread to control pet's actions and feelings.
 		life = new PetLife();
 		life.start();
 		
 		// Place the pet in the middle of the play area.
 		model.setXYCoord(BACKGROUND_WIDTH/2-(model.getWidth()/2), 		
 						(AREA_MIN_Y + (BACKGROUND_HEIGHT-AREA_MIN_Y)/2) - (model.getHeight()/2));
 	
 	} // End method loadPet
 
 
 	public boolean isWithinPlayground(int x, int y)
 	{
 		if((x > LEFT_HOUSE_X) || (x < RIGHT_HOUSE_X))
 		{
 			// Within x coordinates of house, check height.
 			if(y < BOTTOM_HOUSE_Y)
 			{
 				// Is on house, return false.
 				return false;
 			}
 		}
 		
 		// In playground, return true.
 		return true;
 	} // End method isWithinPlayground
 	
 	
 	private void movePetToFood(Food food)
 	{
 		Log.v("move pet to food function", "top");
 		//sleepThread(2000); // 10 seconds
 		
 		int currentPetX, currentPetY, currentFoodX, currentFoodY;
 		int horiz_dx, vert_dy;
 		int jumpDistance = BACKGROUND_WIDTH/10;
 		int numHorizJumps, numVertJumps;
 		
 		// Get current pet location
 		currentPetX = model.getXCoord();
 		currentPetY = model.getYCoord();
 		
 		// Get food location
 		currentFoodX = food.getX();
 		currentFoodY = food.getY();
 		
 		// If locations are not the same, alternate moving vertical / horizontal until pet is next to food.
 		horiz_dx = currentPetX - currentFoodX;
 		vert_dy = currentPetY - currentFoodY;
 		
 		// If vert_dy is positive, pet needs to move up. If negative, pet needs to move down.
 		if(vert_dy != 0)
 		{
 			numVertJumps = vert_dy / jumpDistance;
 		}
 		else
 		{
 			// Pet is already on the same horizontal axis as the food.
 			numVertJumps = 0;
 		}
 		
 		// If horix_dx is positive, pet needs to move left. If negative, pet needs to move right.
 		if(horiz_dx != 0)
 		{
 			numHorizJumps = horiz_dx / jumpDistance;
 		}
 		else
 		{
 			// Pet is already on the same vertical axis as the food.
 			numHorizJumps = 0;
 		}
 		
 		// Now move
 		if(numVertJumps > 0)
 		{
 			numVertJumps++;
 			for(int k=0; k<numVertJumps; k++)
 			{
 				//life.petMove = 3; // Move up
 				life.moveSequence.add(3);
 				Log.v("add#3", "move up");
 			}
 		}
 		else if(numVertJumps <= 0)
 		{
 			numVertJumps *= -1; // Make number positive
 			numVertJumps++;
 			for(int k=0; k<numVertJumps; k++)
 			{
 				//life.petMove = 4; // Move down
 				life.moveSequence.add(4);
 				Log.v("add#4", "move down");
 			}
 		}
 		
 		if(numHorizJumps > 0)
 		{
 			numHorizJumps++;
 			for(int k=0; k<numHorizJumps; k++)
 			{
 				//life.petMove = 1; // Move left
 				life.moveSequence.add(1);
 				Log.v("add#1", "move left");
 			}
 		}
 		else if(numHorizJumps <= 0)
 		{
 			numHorizJumps *= -1; // Make number positive
 			numHorizJumps++;
 			for(int k=0; k<numHorizJumps; k++)
 			{
 				//life.petMove = 2; // Move right
 				life.moveSequence.add(2);
 				Log.v("add#2", "move right");
 			}
 		}
 		
 		life.isMovingToFood = true;
 		life.movementEnabled = false;
 		synchronized(life)
 		{
 			life.notify();
 		}
 		//isMovingToFood = true;
 		// Delete food.
 		
 	}
 	
 	
 	/**
 	 * Make the pet move in a specified direction. A complete movement
 	 * takes 20 frames.
 	 * 
 	 *	@param direction: Integer from 1-8 used to specify movement direction
 	 *
 		  	Values:
 		  	5 3 6
 		  	1 * 2
 		  	7 4 8	
 	 */
 	private boolean move(int direction) {
 		int destX, destY;
 		// No matter what the movement, the distance will be 1/10th the 
 		// width of screen in x or y or both.
 		int distance = BACKGROUND_WIDTH/10;
 		
 		// Move left 1/10 background width	
 		if (direction == 1) {
 			destX = model.getXCoord() - distance;
 
 			if (destX <= 0)
 				// Cannot move off screen
 				return false;
 			if ((destX < RIGHT_HOUSE_X) && (destX > LEFT_HOUSE_X) && (model.getYCoord() > BOTTOM_HOUSE_Y + model.getHeight()))
 				// Cannot jump into side of house
 				return false;
 		
 
 			// A complete movement takes 20 frames.
 			int newY;
 			int dx = Math.round(distance/20);
 
 			for (int i=0; i<20; i++) {
 				if (i<10) 
 					// Jumping up
 					newY = model.getYCoord() - dx;
 				else 
 					// Jumping down
 					newY = model.getYCoord() + dx;
 				
 				model.setXYCoord(model.getXCoord() - dx, newY);
 				sleepThread(10); // Sleep for 10 milliseconds to implement animation.
 			}
 		}
 		// Move right
 		else if (direction == 2) {
 			destX = model.getXCoord() + distance;
 
 			if (destX+model.getWidth() >= BACKGROUND_WIDTH)
 				// Cannot move off screen
 				return false;
 			if ((destX < RIGHT_HOUSE_X) && (destX > LEFT_HOUSE_X) && (model.getYCoord() > BOTTOM_HOUSE_Y + model.getHeight()))
 				// Cannot jump into house
 				return false;
 			
 			
 			// Move occurs in 20 frames
 			int newY;
 			int dx = Math.round(distance/20);
 
 			for (int i=0; i<20; i++) {
 				if (i<10) newY = model.getYCoord() - dx;
 				else newY = model.getYCoord() + dx;
 				
 				model.setXYCoord(model.getXCoord() + dx, newY);
 				sleepThread(10);
 			}
 			
 		}
 		
 		// Move up
 		else if (direction == 3) {
 			destY = model.getYCoord() - distance;
 
 			// Check if pet is trying to move into house.
 			if (!(model.getXCoord() > LEFT_HOUSE_DOOR_X && model.getXCoord() + model.getWidth() < RIGHT_HOUSE_DOOR_X)) {
 				// Pet's x coord is not within the range of door opening.
 				// So check if it's trying to jump through fence.
 				if (destY+model.getHeight() < AREA_MIN_Y) {
 					// Cannot move through fence
 					return false;
 				}
 			}
 			else {
 				// Pet is within range of house door.  Move up only if not going through top of house
 				Log.v("gothere", "5");
 				if (destY < TOP_HOUSE_DOOR_Y) {
 					return false;
 				}
 			}
 			
 			// Move occurs in 20 frames
 			int dx = Math.round(distance/20);
 
 			for (int i=0; i<20; i++) {	
 				model.setYCoord(model.getYCoord() - dx);
 				sleepThread(10);
 			}
 		}
 		// Move down
 		else if (direction == 4) {
 			destY = model.getYCoord() + distance;
 
 			if (destY+model.getHeight() > BACKGROUND_HEIGHT)
 				// Cannot move down
 				return false;
 			
 			// Move occurs in 20 frames
 			int dx = Math.round(distance/20);
 
 			for (int i=0; i<20; i++) {
 				model.setYCoord(model.getYCoord() + dx);
 				sleepThread(10);
 			}
 			
 		}
 
 		return true;
 		
 	}
 
 	/**
 	 * Puts the main thread to sleep
 	 * 
 	 * @param millis: The number of milliseconds the thread will sleep for
 	 */
 	private void sleepThread(int millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	private void accessorize() {
 		// Pet is leaving Home to go to new activity.
 		model.setPetIsHome(false);
 		
 		// Launch AccessorizeActivity.
 		Intent myIntent = new Intent(getHomeContext(), AccessorizeActivity.class);
 		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		PetController.this.homeContext.startActivity(myIntent);
 	}
 	
 	// Create and start the sleep timer.
 	public void setSleepTimer()
 	{
 		sleepTimer = new RunawayCountdownTimer(model.TIME_UNITL_NEXT_SLEEP, 1000)
 		{
 			public void onFinish()
 			{
 				handleSleeping();
 			}
 		};
 		sleepTimer.start();
 		
 	} // End method setSleepTimer
 	
 	
 	// Launch the sleep activity. Pet will sleep for four hours.
 	private void handleSleeping()
 	{
 		// Pet is leaving Home to go to new activity.
 		model.setPetIsHome(false);
 		model.setPetIsSleeping(true);
 		
 		// Launch SleepActivity.
 		Intent myIntent = new Intent(getHomeContext(), SleepActivity.class);
 		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		PetController.this.homeContext.startActivity(myIntent);
 		
 	} // End method handleSleeping
 	
 	// Create and start the runaway timer.
 	public void setRunawayTimer(long time)
 	{
 		runawayTimer = new RunawayCountdownTimer(time, 1000)
 		{
 			public void onFinish()
 			{
 				runaway();
 			}
 		};
 		runawayTimer.start();
 		
 	} // End method setRunawayTimer
 	
 	// The pet is unhappy and is going to run away now :(
 	private void runaway()
 	{
 		// Pet is leaving Home to go to new activity.
 		model.setPetIsHome(false);
 		
 		// Launch RunawayActivity.
 		Intent myIntent = new Intent(getHomeContext(), RunawayActivity.class);
 		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		PetController.this.homeContext.startActivity(myIntent);
 	}
 	
 	
 	// Need to be able to get time remaining in countdown
 	public long getCountdownTimeLeft(int s)
 	{
 		switch(s)
 		{
 		case 1:
 			// Runaway Timer
 			return runawayTimer.getTimeLeft();
 		case 2:
 			// Sleep Timer
 			return sleepTimer.getTimeLeft();
 		default:
 			return 0;
 		}
 	}
 
 	/**
 	 *  This thread is used to control the pet's actions and feelings. 
 	 */
 	private class PetLife extends Thread {
 		int petMove = 0;
 		boolean movementEnabled = true, isMovingToFood = false;
 		ArrayList<Integer> moveSequence = new ArrayList<Integer>();
 		
 		public void run()
 		{
 			while(true)
 			{
 				// Wait. Wake up after 2 seconds or when main thread commands it.
 				try {
 					synchronized(this) {
 						wait(2000);
 					}
 					
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				// Stay here if user working with pet in another activity.
 				while (!model.getPetIsHome())
 				{
 					yield();
 				}
 				
 				if(isMovingToFood)
 				{
 				
 					if(!moveSequence.isEmpty())
 					{
 							int temp = moveSequence.remove(0);
 							Log.v("moving pet direction =", Integer.toString(temp));
 							petMove = temp;
 					}
 					else
 					{
 						Log.v("movetofood", "should be at food nao");
 						isMovingToFood = false;
 						movementEnabled = true;
 						model.setPetIsEating(false);
 					}
 				}
 				
 				if (petMove > 0)
 				{
 					// Perform move
 					move(petMove);
 					petMove = 0;		
 				}
 				if (movementEnabled)
 				{
 					// Move pet
 					petMove++;
 					if (petMove == 3) {
 						petMove = 0;
 						// Move the pet randomly.
 						int direction = 1 + (int)(Math.random() * ((4 - 1) + 1));
 									//  min + ................... ((max - min) + 1));
 						move(direction);
 					}
 				}
 				
 				// Redraw Home every thread loop.
 				model.justDraw();
 			}
 			
 		}
 		
 	}
 	
 }
