 package com.me.Roguish.Controller;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.me.Roguish.Model.Entity;
 import com.me.Roguish.Model.HeroUnit;
 import com.me.Roguish.Model.MonsterUnit;
 import com.me.Roguish.Model.Level;
 import com.me.Roguish.Model.TurnQueue;
 
 public class LevelController {
 	private Level level;
 	private Entity hero;
 	public Random Dice = new Random();
 	public boolean gameOver = false;
 	public boolean gameWon = false;
 	// Index is the index in the level entity array of the current Unit whose turn it is 
 	private int index = 0;
 	
 	enum Keys {
 		LEFT, RIGHT, UP, DOWN
 	}
 	
 	static Map<Keys, Boolean> keys = new HashMap<LevelController.Keys, Boolean>();
 	static {
 		keys.put(Keys.LEFT, false);
 		keys.put(Keys.RIGHT, false);
 		keys.put(Keys.UP, false);
 		keys.put(Keys.DOWN, false);
 	};
 	
 	public LevelController(Level level){
 		this.level = level;
 		this.hero = level.getHero();
 		
 	}
 	
 	// Main update method
 	public void update(float delta){
 		// process
 		processInput();
 		// do something
 	}
 	
 	
 	private void processInput(){
 		if (keys.get(Keys.LEFT)){
 			
 		}
 		if (keys.get(Keys.RIGHT)){
 		}
 		if (keys.get(Keys.DOWN)){
 			
 		}
 		if (keys.get(Keys.UP)){
 		}
 	}
 	
 	
 	// Keypresses
 	public void leftPressed() {
 		keys.get(keys.put(Keys.LEFT, true));
 	}
 	
 	public void leftReleased() {
 		keys.get(keys.put(Keys.LEFT, false));
 		checkHeroTurn();
 		if(hero.getX() > 0 && tileOpen(hero.getX() - 1, hero.getY())){
 			level.queue.getNext();
 			hero.movePosition(-1, 0);
 			level.queue.add(hero);
 		}
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	public void rightPressed() {
 		keys.get(keys.put(Keys.RIGHT, true));
 	}
 	
 	public void rightReleased() {
 		keys.get(keys.put(Keys.RIGHT, false));
 		checkHeroTurn();
 		if(hero.getX() < 9 && tileOpen(hero.getX() + 1, hero.getY())){
 			level.queue.getNext();
 			hero.movePosition(1 , 0);
 			level.queue.add(hero);
 		}
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	public void upPressed() {
 		keys.get(keys.put(Keys.UP, true));
 	}
 	
 	public void upReleased() {
 		keys.get(keys.put(Keys.UP, false));
 		checkHeroTurn();
 		if(hero.getY() > 0 && tileOpen(hero.getX(), hero.getY() - 1)){
 			level.queue.getNext();
 			hero.movePosition(0, -1);
 			level.queue.add(hero);
 		}
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	public void downPressed() {
 		keys.get(keys.put(Keys.DOWN, true));
 	}
 	
 	public void downReleased() {
 		keys.get(keys.put(Keys.DOWN, false));
 		checkHeroTurn();
 		if(hero.getY() < 14 && tileOpen(hero.getX(), hero.getY() + 1)){
 			level.queue.getNext();
 			hero.movePosition(0, 1);
 			level.queue.add(hero);
 		}
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	//Returns true if the tile at the x, y is open
 	public boolean tileOpen(int x, int y){
 		for (Entity ent : level.getEntities()) {
 			if (ent.getX() == x && ent.getY() == y && ent.getAlive()) return false;
 		}
 		if(level.tilePropCheck(x,y,"wall")) return false;
 		else return true;
 	}
 	
 	public void doMonsterTurns(){
 		if (level.entities.get(index) instanceof MonsterUnit){
 			if(adjacentHero(level.entities.get(index).getX(), level.entities.get(index).getX())){
 				switch( ((MonsterUnit)level.entities.get(index)).getType()){
 					case MonsterUnit.RAT: doRatAttack();
 				}
 				
 			}
 			else{
 				switch( ((MonsterUnit)level.entities.get(index)).getType()){
 					case MonsterUnit.RAT: doRatMovement();
 				}	
 			}
 			if(level.entities.get(index).getAlive()) level.queue.add(level.entities.get(index));
 		}
 		
 			
 	}
 	
 	private void doRatAttack() {
 		level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.BITE);	
 		System.out.println(level.getHero().getHP());
 	}
 
 	//Iterates over NPCs and performs their turns until it is the Hero's turn
 	public void checkHeroTurn(){
 		checkLoseConditions();
 		checkWinConditions();
 		
 		System.out.println(level.queue.turnCount);
 		while (!(level.queue.getEnt() instanceof HeroUnit)){
 			doMonsterTurns();
 			index = findId(level.queue.getNext());
 		}
 	}
 	
 	public void checkLoseConditions(){
		if(level.getHero().getHP() == 0) gameOver = true;
 	}
 	
 	public void checkWinConditions(){
 		for (Entity ent : level.getEntities()) {
 			if(ent.getId() == 1337 && heroOn(ent.getX(), ent.getY())) gameWon = true; 
 		}
 	}
 	
 	public boolean heroOn(int x, int y){
 		return(hero.getX() == x && hero.getY() == y );
 	}
 	
 	// Returns true if the hero is adjacent to the inputted x,y coordinates
 	public boolean adjacentHero(int x, int y){
 		if ( (hero.getX() == x + 1 || hero.getX() == x -1)  && (hero.getY() == y))
 			return true;
 		if ( (hero.getY() == y + 1 || hero.getY() == y - 1) && (hero.getX() == x))
 			return true;
 		else return false;
 	}
 	
 	public int findId(int x){
 		for(int i = 0; i < level.entities.size; i++ ){
 			if(x == level.entities.get(i).getId()) return i;
 		}
 		return -1;	
 	}
 	
 	//Returns an integer array of current open tiles adjacent to the inputted x,y
 	//  index   tile         open value = 1  taken value = 0
 	//    0      up
 	//    1      down
 	//    2      left 
 	//    3      right
 	public int[] findOpen(int x, int y){
 		int count[] = new int[4];
 		for(int i = 0; i < count.length; i++)
 			count[i] = 0;
 		if (tileOpen(x+1, y)) count[3] = 1;
 		if (tileOpen(x-1, y)) count[2] = 1;
 		if (tileOpen(x, y-1)) count[1] = 1;
 		if (tileOpen(x, y+1)) count[0] = 1;
 		return count;
 	}
 	
 	public void doRatMovement(){
 		int[] tiles = findOpen(level.entities.get(index).getX(), level.entities.get(index).getY());
 		int[] x = new int[4];
 		int[] y = new int[4];
 		x[0] = 0;
 		x[1] = 0;
 		x[2] = -1;
 		x[3] = 1;
 		y[0] = 1;
 		y[1] = -1;
 		y[2] = 0;
 		y[3] = 0;
 		int count = 0;
 		boolean done = false;
 		do{
 			int rand = Dice.nextInt(4);
 			if(tiles[rand] == 1){
 				level.entities.get(index).movePosition(x[rand], y[rand]);
 				done = true;
 			}
 			count++;
 			if(count > 5) done = true;
 		}while(!done);
 	}
 	
 }
