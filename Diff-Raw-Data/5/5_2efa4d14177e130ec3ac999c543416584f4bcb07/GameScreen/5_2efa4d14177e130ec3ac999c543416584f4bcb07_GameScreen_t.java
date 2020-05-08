 package com.teamBasics.CollegeTD;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.MotionEvent;
 
 import com.teamBasics.framework.Game;
 import com.teamBasics.framework.Graphics;
 import com.teamBasics.framework.Image;
 import com.teamBasics.framework.Graphics.ImageFormat;
 import com.teamBasics.framework.Input.TouchEvent;
 import com.teamBasics.framework.Screen;
 
 public class GameScreen extends Screen {
 	enum GameState {
 		Ready, Running, Paused, GameOver
 	}
 
 	GameState state = GameState.Ready;
 	
 	enum TowerType {
 		reddit, starbucks, pencil, none
 	}
 	
 	
 	// Variable Setup
 	private String descriptionText = "";
 	private String scoreText = "0";
 	private String livesText;
 	private String cashText = "$0";
 	TowerType towerType = TowerType.none;
 	private int Xtower = 0;
 	private int Ytower = 0;
 	private int Xbox = -50;
 	private int Ybox = -50;
 
 	Paint paintInit, paintMenu, paintDescriptionText, paintHUBText;
 
 	//Levels
 	private static Level CurrentLevel;
 	private ArrayList<Level> levelArray = new ArrayList<Level>(20);
 	//private Level level1;
 	
 	public GameScreen(Game game) {
 		super(game);
 
 		for(int i = 0; i < 20; i++){
 			levelArray.add(i, ( new Level() ));
 			levelArray.get(i).setLevel_number(i+1);
 		}
 		//level1 = new Level();
 		// Must update CurrentLevel to next level when user has defeated all enemies.
 		CurrentLevel = levelArray.get(0);	
 		
 		// Defining a paint object
 		// Start Text
 		paintInit = new Paint();
 		paintInit.setTextSize(30);
 		paintInit.setTextAlign(Paint.Align.CENTER);
 		paintInit.setAntiAlias(true);
 		paintInit.setColor(Color.GRAY);
 
 		// Back button (options menu)
 		paintMenu = new Paint();
 		paintMenu.setTextSize(100);
 		paintMenu.setTextAlign(Paint.Align.CENTER);
 		paintMenu.setAntiAlias(true);
 		paintMenu.setColor(Color.LTGRAY);
 		
 		// Description Text
 		paintDescriptionText = new Paint();
 		paintDescriptionText.setTextSize(20);
 		paintDescriptionText.setTextAlign(Paint.Align.LEFT);
 		paintDescriptionText.setAntiAlias(true);
 		paintDescriptionText.setColor(Color.CYAN);
 		
 		// HUB Text
 		paintHUBText = new Paint();
 		paintHUBText.setTextSize(20);
 		paintHUBText.setTextAlign(Paint.Align.RIGHT);
 		paintHUBText.setAntiAlias(true);
 		paintHUBText.setColor(Color.CYAN);
 
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 
 		// We have four separate update methods in this example.
 		// Depending on the state of the game, we call different update methods.
 		// Refer to Unit 3's code. We did a similar thing without separating the
 		// update methods.
 
 		if (state == GameState.Ready)
 			updateReady(touchEvents);
 		if (state == GameState.Running)
 			updateRunning(touchEvents, deltaTime);
 		if (state == GameState.Paused)
 			updatePaused(touchEvents);
 		if (state == GameState.GameOver)
 			updateGameOver(touchEvents);
 	}
 
 	private void updateReady(List<TouchEvent> touchEvents) {
 
 		// This example starts with a "Ready" screen.
 		// When the user touches the screen, the game begins.
 		// state now becomes GameState.Running.
 		// Now the updateRunning() method will be called!
 
 		if (touchEvents.size() > 0)
 			state = GameState.Running;
 	}
 
 	private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
 		Graphics sel = game.getGraphics();
 		// This is identical to the update() method from our Unit 2/3 game.
 
 		// 1. All touch input is handled here:
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			int X = event.x;
 			int Y = event.y;
 			// Handles Touch PRESS
 			if (event.type == TouchEvent.TOUCH_DOWN) {
 						
 				// Display Selected Item on touch_down(press)
 				
 				// Reddit Tower
 				if(inBounds(event, 735, 49, 40, 40)){
 					Assets.selectItem = Assets.redditTower;
 					descriptionText = "This is the reddit tower!";
 					towerType = TowerType.reddit;
 					Xtower = -50;
 					Ytower = -50;
 				}
 				// Pencil Tower
 				else if(inBounds(event, 735, 115, 40, 40)){
 					Assets.selectItem = Assets.pencilTower;
 					descriptionText = "This is the pencil tower!";
 					towerType = TowerType.pencil;
 					Xtower = -50;
 					Ytower = -50;
 				}
 				// Starbucks Tower
 				else if(inBounds(event, 735, 181, 40, 40)){
 					Assets.selectItem = Assets.starbucksTower;
 					descriptionText = "This is the starbucks tower!";
 					towerType = TowerType.starbucks;
 					Xtower = -50;
 					Ytower = -50;
 				}
 				
 				// Sleep Upgrade
 				else if(inBounds(event, 735, 245, 40, 40)){
 					Assets.selectItem = Assets.sleepUp;
 					descriptionText = "This is the sleep upgrade!";
 				}
 				// Social Upgrade
 				else if(inBounds(event, 735, 309, 40, 40)){
 					Assets.selectItem = Assets.socialUp;
 					descriptionText = "This is the social upgrade!";
 				}
 				// Academic Upgrade
 				else if(inBounds(event, 735, 373, 40, 40)){
 					Assets.selectItem = Assets.academicUp;
 					descriptionText = "This is the academic upgrade!";
 				}
 				else {
 					towerType = TowerType.none;
 					
 				}
 
 			}
 
 			if (event.type == MotionEvent.ACTION_MOVE) {
 				// Sets the X and Y coords of the tower to the touch even ones
 				Xtower = X;
 				Ytower = Y;
 				// This section uses the same subroutine as the final drop does to display the box where the tile will be located
 				// The tower is drawn when all of the other game screen images are updated.
 				Xbox = cordFix(Xtower);
 				Ybox = cordFix(Ytower);
 				
 			}
 			
 			// Handles touch RELEASE
 			if (event.type == TouchEvent.TOUCH_UP) {
 				
 				if(inBounds(event, 5, 1, 34, 33)){
 					pause();
 				}
 				Xbox = -50;
 				Ybox = -50;
 				
 				
 				if(Xtower > 21 && Xtower < 619 && Ytower > 61 && Ytower < 299) {  // CHANGE TO ACTUAL VALUES
 					Xtower = cordFix(Xtower);
 					Ytower = cordFix(Ytower);
 					
 					
 					if(validPlacement(Xtower, Ytower)) {
 					
 					
 						if (towerType == TowerType.reddit) {
 							RedditTower temp = new RedditTower(Xtower, Ytower);
 							if(CurrentLevel.getCash()>=temp.cost) {
 								CurrentLevel.addTower(temp);
 								CurrentLevel.setCash(CurrentLevel.getCash()-temp.cost);
 							} else {
 								descriptionText = "Insufficient funds!";
 							}
 						}
 						else if (towerType == TowerType.starbucks) {
 					
 							StarbucksTower temp = new StarbucksTower(Xtower, Ytower);
 							if(CurrentLevel.getCash()>=temp.cost) {
 								CurrentLevel.addTower(temp);
 								CurrentLevel.setCash(CurrentLevel.getCash()-temp.cost);	
 							} else {
 								descriptionText = "Insufficient funds!";
 							}
 						}
 						else if (towerType == TowerType.pencil) {
 					
 							PencilTower temp = new PencilTower(Xtower, Ytower);
 							if(CurrentLevel.getCash()>=temp.cost) {
 								CurrentLevel.addTower(temp);
 								CurrentLevel.setCash(CurrentLevel.getCash()-temp.cost);
 							} else {
 								descriptionText = "Insufficient funds!";
 							}
 						}
 						}
 				} else {
 					// invalid tower placement
 					towerType = TowerType.none;
 				}
 				towerType = TowerType.none;
 				}
 		}
 
 		// 2. Check miscellaneous events like death:
 
 		if (CurrentLevel.getLivesLeft() == 0) {
 			state = GameState.GameOver;
 		}
 
 		// 3. Call individual update() methods here.
 		// This is where all the game updates happens
 
 		CurrentLevel.updateBorderTiles();
 		CurrentLevel.updatePathTiles();
 		CurrentLevel.update();
 	}
 
 	private boolean inBounds(TouchEvent event, int x, int y, int width,
 			int height) {
 		if (event.x > x && event.x < x + width - 1 && event.y > y
 				&& event.y < y + height - 1)
 			return true;
 		else
 			return false;
 	}
 
 	private void updatePaused(List<TouchEvent> touchEvents) {
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_UP) {
 				if (inBounds(event, 0, 0, 800, 240)) {
 
 					if (!inBounds(event, 0, 0, 35, 35)) {
 						resume();
 					}
 				}
 
 				if (inBounds(event, 0, 240, 800, 240)) {
 					nullify();
 					goToMenu();
 				}
 			}
 		}
 	}
 
 	private void updateGameOver(List<TouchEvent> touchEvents) {
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_DOWN) {
 				if (inBounds(event, 0, 0, 800, 480)) {
 					nullify();
 					game.setScreen(new MainMenuScreen(game));
 					return;
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
 		
 		// Draw Main Game Screen
 		g.drawImage(Assets.gamescreen, 0, 0);
 		
 		// Draw custom background
 		g.drawImage(Assets.space, 0, 41);
 		g.drawImage(Assets.space, 0, 200);
 		g.drawImage(Assets.space, 340, 41);
 		g.drawImage(Assets.space, 340, 200);
 		
 		// Paint Tiles
 		CurrentLevel.paintBorderTiles(g);
 		CurrentLevel.paintPathTiles(g);
 
 		// Secondly, draw the UI above the game elements.
 		if (state == GameState.Ready)
 			drawReadyUI();
 		if (state == GameState.Running)
 			drawRunningUI();
 		if (state == GameState.Paused)
 			drawPausedUI();
 		if (state == GameState.GameOver) {
 			drawGameOverUI();
 		}
 		
 	}
 
 	private void nullify() {
 
 		// Set all variables to null. You will be recreating them in the
 		// constructor.
 		paintInit = null;
 		//level1 = null;
 		CurrentLevel=null;
 
 		// Call garbage collector to clean up memory.
 		System.gc();
 	}
 
 	private void drawReadyUI() {
 		Graphics g = game.getGraphics();
 
 		g.drawARGB(80, 0, 0, 0);
 		g.drawString("Tap to Start!", 400, 240, paintInit);
 
 	}
 
 	private void drawRunningUI() {
 		Graphics g = game.getGraphics();
 		
 		// Tower sprites
 		g.drawImage(Assets.redditTower, 735, 49);
 		g.drawImage(Assets.pencilTower, 735, 115);
 		g.drawImage(Assets.starbucksTower, 735, 181);
 		
 		// Draw towers that are being dragged
 		if (towerType == TowerType.reddit) {
 			g.drawImage(Assets.redditTower, Xtower, Ytower);
 		}
 		else if (towerType == TowerType.starbucks) {
 			g.drawImage(Assets.starbucksTower, Xtower, Ytower);
 		}
 		else if (towerType == TowerType.pencil) {
 			g.drawImage(Assets.pencilTower, Xtower, Ytower);
 		}
 		// Draw the Highlighted box where the tower will be placed
 		if(towerType != TowerType.none && (Xtower > 21 && Xtower < 619 && Ytower > 61 && Ytower < 299)){
 			g.drawImage(Assets.towerBox, Xbox, Ybox);
 		}
 		
 		// Upgrade sprites
 		g.drawImage(Assets.sleepUp, 735, 245);
 		g.drawImage(Assets.socialUp, 735, 309);
 		g.drawImage(Assets.academicUp, 735, 373);
 		
 		g.drawImage(Assets.selectItem, 49, 423);
 		g.drawString(descriptionText, 160, 439, paintDescriptionText);
 		scoreText = "" + CurrentLevel.getScore();
 		g.drawString(scoreText, 290, 26, paintHUBText);
 		livesText = "" + CurrentLevel.getLivesLeft();
 		g.drawString(livesText, 516, 26, paintHUBText);
 		cashText = "$" + CurrentLevel.getCash(); 
 		g.drawString(cashText, 695, 26, paintHUBText);
 		
 		CurrentLevel.draw(g);
 	}
 	
 	private void drawPausedUI() {
 		Graphics g = game.getGraphics();
 		// Darken the entire screen so you can display the Paused screen.
 		g.drawARGB(155, 0, 0, 0);
 		g.drawString("Resume", 400, 165, paintMenu);
 		g.drawString("Menu", 400, 360, paintMenu);
 
 	}
 	private int cordFix(int cord) {
 		int tempCord = cord/40;
 		int modCord = cord%40;
 		
 		if(modCord > 20) {
 			tempCord += 1;
 		}
 		
 		return tempCord*40;
 	}
 	private void drawGameOverUI() {
 		Graphics g = game.getGraphics();
 		g.drawRect(0, 0, 1281, 801, Color.BLACK);
 		g.drawString("GAME OVER.", 400, 240, paintMenu);
 		g.drawString("Try Again!", 400, 290, paintInit);
 
 	}
 
 	private boolean validPlacement(int Xloc, int Yloc) {
 		boolean ret = true;
 		ArrayList<PathTile> tilePath = CurrentLevel.getTilearrayPath();
 		for(int i = 0; i < tilePath.size(); i++) { // check the map tiles to make sure that it is not a stone or dirt tile
			if(tilePath.get(i).getTileX() == Xloc && tilePath.get(i).getTileY() == Yloc && (tilePath.get(i).type2 == 's' || tilePath.get(i).type2 == 'w' || tilePath.get(i).type2 == 'a' || tilePath.get(i).type2 == 'd' || tilePath.get(i).type2 == 'x' || tilePath.get(i).type2 == 'z' || tilePath.get(i).type2 == 'q' || tilePath.get(i).type2 == 'c' || tilePath.get(i).type2 == 'e' || tilePath.get(i).type2 == 'r' && towerType != TowerType.none)){ // check if the stone tile X and Y coords are the same
 				descriptionText = "Towers cannot be on path";
 				return false;
 			}
 		}
 		
 		ArrayList<Tower> towerList = CurrentLevel.getTowers(); // get list of already placed towers
 		for(int i = 0; i < towerList.size(); i++) { // check to make sure that the tower is not already in that location
			if( towerList.get(i).getPosX() == Xloc && towerList.get(i).getPosY() == Yloc && towerType != TowerType.none) {
 				descriptionText = "Towers cannot overlap.";
 				return false;				
 			}
 		}		
 		
 		
 		return ret;
 	}
 	
 	@Override
 	public void pause() {
 		if (state == GameState.Running)
 			state = GameState.Paused;
 
 	}
 
 	@Override
 	public void resume() {
 		if (state == GameState.Paused)
 			state = GameState.Running;
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void backButton() {
 		pause();
 	}
 
 	private void goToMenu() {
 		// TODO Auto-generated method stub
 		game.setScreen(new MainMenuScreen(game));
 	}
 	
 	public static Level getCurrentLevel(){
 		return CurrentLevel;
 	}
 }
