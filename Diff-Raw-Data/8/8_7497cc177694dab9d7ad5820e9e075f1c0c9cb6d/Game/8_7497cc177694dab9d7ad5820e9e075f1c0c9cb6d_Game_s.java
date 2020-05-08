 package com.multimedia.slidepuzzel.gamelogic;
 
 import java.util.Random;
 
 import com.multimedia.slidepuzzel.sound.SoundManager;
 
 import android.graphics.Rect;
 
 public class Game{
 	public static final long ROTATE_DELAY = 5000;
 	
 	public enum Difficulty{
 		EASY, NORMAL, HARD;
 	}
 	
 	private int size;				// Field size (size x size)
 	private Rect[] tiles;			// Tiles containing rgb data
 	private Field curField; 		// Current field as seen by the player 
 	private Field defField;			// Default field (solved)
 	private Difficulty difficulty;	// Difficulty for this game
 	private SoundManager sound;		// Manager that loads & plays the sound
	private StopWatch gameTimer;	// stopwatch that keeps track of played time
 	private boolean solved;			// Is the puzzle solved?
 	private GameRotation rotation;	// Object that handles the automatic rotation
 	
 	public Game(Difficulty d, int s, SoundManager sound){
 		this.sound = sound;
 		size = s;
 		difficulty = d;
 		curField = new Field(size);
 		defField = new Field(size);
 		tiles = new Rect[size * size];
 		solved = false;
 		gameTimer = new StopWatch();
 		rotation = new GameRotation(this); 
 		gameTimer.start();
 	}
 	
 	public boolean useHints(){
 		return (difficulty == Difficulty.EASY);
 	}
 	
 	public boolean useRotations(){
 		return (difficulty == Difficulty.HARD);
 	}
 	
 	public Field getField(){
 		return curField;
 	}
 	
 	public Field getDefaultField(){
 		return defField;
 	}
 	
 	public Rect getTile(int i){
 		return tiles[i];
 	}
 	
 	public void setTile(Rect t, int i){
 		tiles[i] = t;
 	}
 	
 	public int getSize(){
 		return size;
 	}
 	
 	public SoundManager getSound(){
 		return sound;
 	}
 	
 	public boolean checkPuzzleSolved(){
 		if(!solved && curField.equals(defField)){
 			solved = true;
 			gameTimer.stop();
 		}
 		
 		return solved;
 	}
 	
 	public boolean isSolved(){
 		return solved;
 	}
 	
 	public StopWatch getGameTime(){
 		return gameTimer;
 	}
 	
 	public GameRotation getRotation(){
 		return rotation;
 	}
 	
 	public void shuffle(){
 		boolean loop = true;
 		boolean up, down, left, right;
 		int rand;
 		Random generator = new Random();
 		
 		while(loop){
 			for(int i = 0; i < 10000; i ++){
 				left = curField.validSwap(curField.getNullX() - 1, curField.getNullY());
 				right = curField.validSwap(curField.getNullX() + 1, curField.getNullY());
 				up = curField.validSwap(curField.getNullX(), curField.getNullY() - 1);
 				down = curField.validSwap(curField.getNullX(), curField.getNullY() + 1);
 				
 				rand = generator.nextInt();
 				rand = rand % 4;
 				if(rand == 0 && left)
 					curField.swapTile(curField.getNullX() - 1, curField.getNullY());
 				if(rand == 1 && right)
 					curField.swapTile(curField.getNullX() + 1, curField.getNullY());
 				if(rand == 2 && up)
 					curField.swapTile(curField.getNullX(), curField.getNullY() - 1);
 				if(rand == 3 && down)
 					curField.swapTile(curField.getNullX(), curField.getNullY() + 1);
 
 			}
 			if(!checkPuzzleSolved())
 				loop = false;
 		}
 	}
 }
