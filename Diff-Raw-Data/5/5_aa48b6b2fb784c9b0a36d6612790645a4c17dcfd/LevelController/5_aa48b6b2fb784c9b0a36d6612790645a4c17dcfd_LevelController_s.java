 package com.me.Roguish.Controller;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import com.me.Roguish.Model.Entity;
 import com.me.Roguish.Model.HeroUnit;
 import com.me.Roguish.Model.MonsterUnit;
 import com.me.Roguish.Model.Level;
 import com.me.Roguish.View.LevelRenderer;
 
 public class LevelController {
 	private Level level;
 	private LevelRenderer renderer;
 	private HeroUnit hero;
 
 	public Random Dice = new Random();
 	public boolean gameOver = false;
 	public boolean gameWon = false;
 	private boolean ability1 = false;
 	private boolean ability2 = false;
 	private boolean ability3 = false;
 	private boolean ability4 = false;
 	private boolean ability5 = false;
 	private boolean tarLeft = false;
 	private boolean tarRight = false;
 	private boolean tarUp = false;
 	private boolean tarDown = false;
 	// Index is the index in the level entity array of the current Unit whose turn it is 
 	private int index = 0;
 	
 	enum Keys {
 		LEFT, RIGHT, UP, DOWN, ONE, TWO, THREE, FOUR, FIVE
 	}
 	
 	static Map<Keys, Boolean> keys = new HashMap<LevelController.Keys, Boolean>();
 	static {
 		keys.put(Keys.LEFT, false);
 		keys.put(Keys.RIGHT, false);
 		keys.put(Keys.UP, false);
 		keys.put(Keys.DOWN, false);
 		keys.put(Keys.ONE, false);
 		keys.put(Keys.TWO, false);
 		keys.put(Keys.THREE, false);
 		keys.put(Keys.FOUR, false);
 		keys.put(Keys.FIVE, false);
 	};
 	
 	public LevelController(Level level, LevelRenderer renderer){
 		this.level = level;
 		this.renderer = renderer;
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
 	
 	// Turn Mechanics
 	
 	private void doHeroTurn(Keys direction) {
 		checkHeroTurn();
 		if(!doHeroAbility()){
 		switch(direction){
 		case UP:{
 			if(tileOpen(hero.getX(), hero.getY() + 1)){
 				hero.movePosition(0, 1);
 				nextTurn(hero);
 				renderer.updateCam(0,1);
 				checkWinConditions();
 			}
 			break;
 		}
 		case DOWN:{
 			if(tileOpen(hero.getX(), hero.getY() - 1)){
 				hero.movePosition(0, -1);
 				nextTurn(hero);
 				renderer.updateCam(0,-1);
 				checkWinConditions();
 			}
 			break;
 		}
 		case LEFT:{
 			if(tileOpen(hero.getX() - 1, hero.getY())){
 				hero.movePosition(-1, 0);
 				nextTurn(hero);
 				renderer.updateCam(-1,0);
 				checkWinConditions();
 			}
 			break;
 		}
 		case RIGHT:{
 			if(tileOpen(hero.getX() + 1, hero.getY())){
 				hero.movePosition(1 , 0);
 				nextTurn(hero);
 				renderer.updateCam(1, 0);
 				checkWinConditions();
 			}
 			break;
 		}
 			}
 		}
 		
 	}
 	// Hope this works
 	private boolean doHeroAbility() {
 		if(!ability1 && !ability2 && !ability3 && !ability4 && !ability5) return false;
 		else{
 			if(ability1){
 				try {
 					level.ability.activate(hero, level.entities.get(findId(closestToHero(getDirection(), hero.getAbilities().get(0)))), hero.getAbilities().get(0));
 				}catch(ArrayIndexOutOfBoundsException e){ System.out.println("No target");}
 				ability1 = false;
 			}
 			else if(ability2){
 				try{
 					level.ability.activate(hero, level.entities.get(findId(closestToHero(getDirection(), hero.getAbilities().get(1)))), hero.getAbilities().get(1));
 					}catch(ArrayIndexOutOfBoundsException e){ System.out.println("No target");}
 				ability2 = false;
 			}
 			else if(ability3){
 				try{
 					level.ability.activate(hero, level.entities.get(findId(closestToHero(getDirection(), hero.getAbilities().get(2)))), hero.getAbilities().get(2));
 					}catch(ArrayIndexOutOfBoundsException e){ System.out.println("No target");}
 				ability3 = false;
 			}
 			else if(ability4){
 				try{
 					level.ability.activate(hero, level.entities.get(findId(closestToHero(getDirection(), hero.getAbilities().get(3)))), hero.getAbilities().get(3));
 					}catch(ArrayIndexOutOfBoundsException e){ System.out.println("No target");}
 				ability4 = false;
 			}
 			else if(ability5){
 				try{
 				level.ability.activate(hero, level.entities.get(findId(closestToHero(getDirection(), hero.getAbilities().get(4)))), hero.getAbilities().get(4));
 				}catch(ArrayIndexOutOfBoundsException e){ System.out.println("No target");}
 				ability5 = false;
 			}
 			nextTurn(hero);
 			return true;
 		}
 	}
 	
 	//Iterates over NPCs and performs their turns until it is the Hero's turn
 	public void checkHeroTurn(){	
 		checkLoseConditions();
 		System.out.println(level.queue.turnCount);
 		do{
 			doMonsterTurn();
 			nextTurn();
 		}while (!(level.queue.getEnt() instanceof HeroUnit));
 	}
 	
 	public void doMonsterTurn(){
 		if (level.entities.get(index) instanceof MonsterUnit && level.entities.get(index).getAlive()){
 			switch( ((MonsterUnit)level.entities.get(index)).getType()){
 				case MonsterUnit.RAT: doRatTurn(); break;
 				case MonsterUnit.BAT: doBatTurn(); break;
 				case MonsterUnit.SPIDER: doSpiderTurn(); break;
 				case MonsterUnit.SHADOW: doShadowTurn(); break;
 			}
 		}		
 	}
 	
 	private void doShadowTurn() {
 		if(adjacentHero(level.entities.get(index).getX(), level.entities.get(index).getY() )){
 			doShadowAttack();
 		}
 		else if(inRadius(level.entities.get(index), level.getHero(), 4)){
 			if(tileOpen(level.getHero().getX()-1, level.getHero().getY()))
 				level.entities.get(index).setPosition(level.getHero().getX()-1, level.getHero().getY());
 			else if(tileOpen(level.getHero().getX(), level.getHero().getY()-1))
 				level.entities.get(index).setPosition(level.getHero().getX(), level.getHero().getY()-1);
 			else if(tileOpen(level.getHero().getX()+1, level.getHero().getY()))
 				level.entities.get(index).setPosition(level.getHero().getX()+1, level.getHero().getY());
 			else if(tileOpen(level.getHero().getX(), level.getHero().getY()+1))
 				level.entities.get(index).setPosition(level.getHero().getX(), level.getHero().getY()+1);
 		}
 		else{
 			int x = Dice.nextInt(4) * (-1 * Dice.nextInt(2));
 			if(tileOpen(level.entities.get(index).getX() + x, level.entities.get(index).getY() + x))
 				level.entities.get(index).setPosition(level.getHero().getX()+x, level.getHero().getY()+x);	
 		}
 		
 	}
 
 	private void doShadowAttack() {
 		level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.SHADOWSTRIKE);
 	}
 
 	private void nextTurn(HeroUnit hero){
 		level.queue.getNext();
 		hero.resetMovement();
 		level.queue.add(hero);
 		//doMonsterTurn();   // Doesn't belong here but seems to make things work...?
 	}
 	private void nextTurn(){
 		if(level.entities.get(index).getAlive()){
 			level.entities.get(index).resetMovement();
 			level.queue.add(level.entities.get(index));
 		}
 		index = findId(level.queue.getNext());
 	}
 	
 	private int closestToHero(int direction, int ability){
 		switch(direction){
 		//Up
 		case 0:{
 			for(Entity ent : level.getEntities()){
 				if(inRange(hero, ent, ability) && ent.getY() <= hero.getY() && !ent.equals(hero) && ent.getAlive()){
 					return ent.getId();
 				}
 			}
 			break;
 		}
 		//Down
 		case 1:{
 			for(Entity ent : level.getEntities()){
 				if(inRange(hero, ent, ability) && ent.getY() >= hero.getY() && !ent.equals(hero) && ent.getAlive()) return ent.getId();
 			}
 			break;
 		}
 		//Left
 		case 2:{
 			for(Entity ent : level.getEntities()){
 				if(inRange(hero, ent, ability) && ent.getX() <= hero.getX() && !ent.equals(hero) && ent.getAlive()) return ent.getId();
 			}
 			break;
 		}
 		//Right
 		case 3:{
 			for(Entity ent : level.getEntities()){
 				if(inRange(hero, ent, ability) && ent.getX() >= hero.getX() && !ent.equals(hero) && ent.getAlive()) return ent.getId();
 			}
 			break;
 		}
 		default:{
 			System.out.println("closestToHero params error");
 			return -1;
 		}
 		}
 		System.out.println("closestToHero params error");
 	return -1;
 	}
 	
 	private int getDirection(){
 		if(tarUp){
 			tarUp = false;
 			return 0;
 		}
 		else if(tarDown){
 			tarDown = false;
 			return 1;
 		}
 		else if(tarLeft){
 			tarLeft = false;
 			return 2;
 		}
 		else if(tarRight){
 			tarRight = false;
 			return 3;
 		}
 		else{
 			System.out.println("getDirection error");
 			return -1;
 		}
 	}
 	
 	
 	
 	
 	private void doBatTurn() {
 		if(adjacentHero(level.entities.get(index).getX(), level.entities.get(index).getY()))
 			doBatAttack();
 		else doBatMovement();
 		
 	}
 
 	private void doRatTurn() {
 		if(adjacentHero(level.entities.get(index).getX(), level.entities.get(index).getY()))
 			doRatAttack();
 		else doRatMovement();
 		
 	}
 
 	private void doSpiderMovement() {
 		if(inRadius(level.entities.get(index), level.getHero(), 5)){
 			moveEntityTowardHero(level.entities.get(index));
 		}
 		else moveRandom(level.entities.get(index));
 		
 	}
 
 	
 
 	private void doSpiderTurn() {
 		
 		// Check if hero in range of WEB && has mana for spell
 		if (((MonsterUnit) level.entities.get(index) ).getMana() >= 10 && inRange(level.entities.get(index), level.getHero(), AbilityController.WEB))
 			level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.WEB);
 		//Check if in melee range
 		else if(inRadius(level.entities.get(index), level.getHero(), 1)){
 			level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.BITE);
 		}
 		else doSpiderMovement();
 		
 	}
 
 	//Location Utilities:
 	
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
 	
 	public void checkLoseConditions(){
 		if(level.getHero().getHP() <= 0) gameOver = true;
 	}
 	
 	public void checkWinConditions(){
 		for (Entity ent : level.getEntities()) {
 			if(ent.getId() == 1337 && heroOn(ent.getX(), ent.getY())) gameWon = true; 
 		}
 	}
 	
 	// Returns index of the entity
 	public int findId(int x){
 		for(int i = 0; i < level.entities.size; i++ ){
 			if(x == level.entities.get(i).getId()) return i;
 		}
 		System.out.println("findId error");
 		return -1;	
 	}
 	
 	
 	//Returns true if the tile at the x, y is open
 	public boolean tileOpen(int x, int y){
 		if(x < 0 || x > level.columns || y < 0 || y > level.rows) return false;
 		for (Entity ent : level.getEntities()) {
 			if (ent.getX() == x && ent.getY() == y && ent.getAlive()) return false;
 		}
 		return level.tilePropCheck(x,y,"wall");
 	}
 	
 	public boolean inRange(Entity source, Entity target, int ability ){
 		if( distance(source.getX(), source.getY(), target.getX(), target.getY())  <= level.ability.getRange(ability) ) return true;
 		else return false;
 	}
 	
 	private boolean inRadius(Entity source, Entity target, int radius){
 		if( distance(source.getX(), source.getY(), target.getX(), target.getY())  <= radius) return true;
 		else return false;
 	}
 	
 	//Returns Manhattan distance between two points.
 	private int distance(int x, int y, int x2, int y2) {
 		return Math.abs(x-x2) + Math.abs(y-y2);
 	}
 
 	//Unit AI
 	
 	
 	public void doRatMovement(){
 		moveRandom(level.entities.get(index));
 	}
 	
 	public void doRatAttack() {
 		level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.BITE);	
 		System.out.println(level.getHero().getHP());
 	}
 	
 	public void doBatMovement(){
 		moveEntityTowardHero(level.entities.get(index));
 	}
 	
 	public void doBatAttack(){
 		if(((MonsterUnit)level.entities.get(index)).getHP() < 4 ){
 			level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.TOUCHDRAIN);
 		}
 		else level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.STRONGBITE);
 	}
 	
 	public void moveEntityTowardHero(Entity mover){
 		int deltaX = mover.getX() - level.getHero().getX();
 		int deltaY = mover.getY() - level.getHero().getY();
 		if (deltaX > 0  && deltaY == 0){
 			if(tileOpen(mover.getX() - 1, mover.getY()))
 				mover.movePosition(-1, 0);
 			// Mover is to the right of the hero in the same y.
 		}
 		else if (deltaX < 0  && deltaY == 0){
 			if(tileOpen(mover.getX() + 1, mover.getY()))
 				mover.movePosition(1, 0);
 			// Mover is to the left of the hero in the same y.
 		}
 		else if (deltaY > 0 && deltaX == 0){
 			if(tileOpen(mover.getX(), mover.getY() - 1))
 				mover.movePosition(0, -1);
 			// Mover is below the hero.
 		}
 		else if (deltaY < 0 && deltaX == 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
 			// Mover is above the hero.
 		}
 		else if (deltaX > 0 && deltaY > 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
 			else if(tileOpen(mover.getX() - 1, mover.getY()))
 				mover.movePosition(-1, 0);
 			//mover is Right && Below
 		}
 		else if (deltaX < 0 && deltaY > 0){
 			if(tileOpen(mover.getX(), mover.getY() - 1))
 				mover.movePosition(0, -1);
 			else if(tileOpen(mover.getX() + 1, mover.getY()))
 				mover.movePosition(1, 0);
 			//mover is Left && below
 		}
 		else if (deltaX < 0 && deltaY < 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
 			else if(tileOpen(mover.getX() + 1, mover.getY()))
 				mover.movePosition(1, 0);
 			//mover is Left and Above
 		}
 		else if (deltaX > 0 && deltaY < 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
 			else if(tileOpen(mover.getX() -1, mover.getY()))
 				mover.movePosition(-1, 0);
 			//mover is Right and Above
 		}
 		
 	}
 	
 	public void moveRandom(Entity mover){
 		boolean moved = false;
 		int count = 0;
 		do{
 			switch(Dice.nextInt(4)){
 				case 0:{
 					if(tileOpen(mover.getX(), mover.getY() - 1)){
 						mover.movePosition(0, -1);
 						moved = true;
 					}
 					break;
 				}
 				case 1:{
 					if(tileOpen(mover.getX(), mover.getY() + 1)){
 						mover.movePosition(0, 1);
 						moved = true;
 					}
 					break;
 				}
 				case 2:{
 					if(tileOpen(mover.getX() - 1, mover.getY())){
 						mover.movePosition(-1, 0);
 						moved = true;
 					}
 					break;
 				}
 				case 3:{
 					if(tileOpen(mover.getX() + 1, mover.getY())){
 						mover.movePosition(1, 0);
 						moved = true;
 					}
 					break;
 				}
 				default: break;
 			}
 			count++;
 			
 		}while(!moved && count < 20);
 		
 	}
 	
 	// Keypresses
 	public void leftPressed() {
 		keys.get(keys.put(Keys.LEFT, true));
 	}
 	
 	public void leftReleased() {
 		keys.get(keys.put(Keys.LEFT, false));
 		if(!ability1 && !ability2 && !ability3 && !ability4 && !ability5)
 			doHeroTurn(Keys.LEFT);
 		else {
 			tarLeft = true;
 			System.out.println(tarLeft);
 			doHeroTurn(Keys.LEFT);
 		}
 
 	}
 
 	public void rightPressed() {
 		keys.get(keys.put(Keys.RIGHT, true));
 	}
 	
 	public void rightReleased() {
 		keys.get(keys.put(Keys.RIGHT, false));
 		if(!ability1 && !ability2 && !ability3 && !ability4 && !ability5)
 			doHeroTurn(Keys.RIGHT);
 		else {
 			tarRight = true;
 			doHeroTurn(Keys.RIGHT);
 		}
 			
 	}
 	
 	public void upPressed() {
 		keys.get(keys.put(Keys.UP, true));
 	}
 	
 	public void upReleased() {
 		keys.get(keys.put(Keys.UP, false));
 		if(!ability1 && !ability2 && !ability3 && !ability4 && !ability5)
 			doHeroTurn(Keys.UP);
 		else {
 			tarUp = true;
 			doHeroTurn(Keys.UP);
 		}
 			
 	}
 	
 	public void downPressed() {
 		keys.get(keys.put(Keys.DOWN, true));
 	}
 	
 	public void downReleased() {
 		keys.get(keys.put(Keys.DOWN, false));
 		if(!ability1 && !ability2 && !ability3 && !ability4 && !ability5)
 			doHeroTurn(Keys.DOWN);
 		else{
 			tarDown = true;
 			doHeroTurn(Keys.DOWN);
 		}
 	}
 	
 	public void onePressed(){
 		keys.get(keys.put(Keys.ONE, true));
 	}
 	public void oneReleased() {
 		keys.get(keys.put(Keys.ONE, false));
 		ability1 = true;
 	}
 	public void twoPressed(){
 		keys.get(keys.put(Keys.TWO, true));
 	}
 	public void twoReleased() {
 		keys.get(keys.put(Keys.TWO, false));
 		ability2 = true;
 	}
 	public void threePressed(){
 		keys.get(keys.put(Keys.THREE, true));
 	}
 	public void threeReleased() {
 		keys.get(keys.put(Keys.THREE, false));
 		ability3 = true;
 	}
 	public void fourPressed(){
 		keys.get(keys.put(Keys.FOUR, true));
 	}
 	public void fourReleased() {
 		keys.get(keys.put(Keys.FOUR, false));
 		ability4 = true;
 	}
 	
 	public void fivePressed(){
 		keys.get(keys.put(Keys.FIVE, true));
 	}
 	public void fiveReleased() {
 		keys.get(keys.put(Keys.FIVE, false));
 		ability5 = true;
 	}
 	
 }
