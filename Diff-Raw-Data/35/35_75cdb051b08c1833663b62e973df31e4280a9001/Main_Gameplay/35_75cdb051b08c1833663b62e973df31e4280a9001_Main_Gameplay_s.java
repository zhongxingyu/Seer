 package com.tiny.tank;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.tiny.terrain.TerrainMap;
 import com.tiny.weapons.Shot;
 import com.tiny.weapons.shots.Shots;
 
 public class Main_Gameplay extends BasicGameState{
	Image pause;
 	
 	private int id;
 	public static TerrainMap map;
 	private Input input;
 	
 	//update counter
 	private final int timeStep = 10;
 	private int timeCounter;
 	
 	
 	private final int numOfPlayers = 2;
 	//index of player whose turn it is.
 	private int playersTurnIndex;
 	public static ArrayList<Tank> players;
 	
 	public Main_Gameplay(int id){
 		this.id = id;
 	}
 
 	
 	/**
 	 * Called only once. Initializes things in state.
 	 */
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		map = new TerrainMap(container.getWidth(),container.getHeight());
 		input = container.getInput();
 		timeCounter = 0;
		pause=new Image("res/menu.png");
 		
 		players = new ArrayList<Tank>();
 		
 	}
 	
 	/**
 	 * Called when entering the state
 	 * @see org.newdawn.slick.state.BasicGameState#enter(org.newdawn.slick.GameContainer, org.newdawn.slick.state.StateBasedGame)
 	 */
 	@Override
 	public void enter(GameContainer container, StateBasedGame game){
 		//gets where we are coming from
 		int previousState = TinyTank.getPreviousState();
 		//clears the input so we dont get unintential input
 		input.clearMousePressedRecord();
 		input.clearKeyPressedRecord();
 		
 		//no reason this should happen
 		if(previousState == STATES.MAIN_GAMEPLAY.getId()){
 			System.out.println("what");
 			return;
 		}
 		
 		//this is just a placeholder till we get the weapon select up and running
 		if(previousState == STATES.SELECT_WEAPONS_MENU.getId()){
 			players = ((Select_Weapons_Menu) STATES.SELECT_WEAPONS_MENU.getState()).getTanks();
 		}
 		
 		//sets player one to his turn
 		playersTurnIndex =0;
 		timeCounter = 0;
 	} 
 
 
 	/**
 	 * Renders our objects to the screen. Renders in layers in order put.
 	 */
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		//draw order: background,map,tanks,shots
 		g.setBackground(Color.gray);
//		pause.draw(100,0);
 		map.getImage().draw();
 		for(int i = 0; i < numOfPlayers; i++){
 			players.get(i).render(container, game, g);
 		}
 		if(players.get(playersTurnIndex).getShots().size() >0){
 			players.get(playersTurnIndex).getShots().get(0).render(container, game, g);
 		}
 			
 		
 	}
 
 	/**
 	 * All gameplay logic should be triggered here.
 	 */
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 		if(input.isKeyDown(Input.KEY_Q)){
 			//regenerates terrain//for testing only
 			map = new TerrainMap(container.getWidth(), container.getHeight());
 			players = ((Select_Weapons_Menu) STATES.SELECT_WEAPONS_MENU.getState()).getTanks();
 		}
 		if(input.isKeyDown(Input.KEY_P)) {
 			//pause button
 			game.enterState(STATES.PAUSE_MENU.getId());
 		}
 
 		/***************
 		 * Updates go in this if statement below. This type of timestep loop allows the program
 		 * to be bound by a constant timestep and not performance of a computer.
 		 ***************/
 		timeCounter+=delta;
 		if(timeCounter>timeStep){
 			//updates players and shots
 			
 			//if not players turn, switch players
 			if(!players.get(playersTurnIndex).isTurn()){
 				if(playersTurnIndex == 0){
 					playersTurnIndex = 1;
 				}else if(playersTurnIndex == 1){
 					playersTurnIndex = 0;
 				}
 				players.get(playersTurnIndex).onTurnSwitch();
 				onTurnSwitch();
 			}
 			
 			//Updates players positions
 			for(int i = 0; i < numOfPlayers; i++){
 				players.get(i).update(input);
 			}
 			//allows player whose turn it is to move.
 			players.get(playersTurnIndex).move(input);
 
 			//test click bomb
 			/*
 			if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON)){
 				players.get(playersTurnIndex).getShots().get(0).init(new Vector2f(input.getMouseX(), input.getMouseY()),new Vector2f(1,5));
 				players.get(playersTurnIndex).setShooting(true);
 				System.out.println(playersTurnIndex);
 				//map.update();
 			}*/
 					
 			//decrease time counter
 			timeCounter-=timeStep;
 		}
 		
 	}
 	
 	/**
 	 * Anything that needs to happen on turn switches.
 	 * In the future this will include hud switching and network notifications.
 	 */
 	private void onTurnSwitch(){
 		input.clearKeyPressedRecord();
 	}
 
 	/**
 	 * Gets the id for the this state
 	 */
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return id;
 	}
 
 }
