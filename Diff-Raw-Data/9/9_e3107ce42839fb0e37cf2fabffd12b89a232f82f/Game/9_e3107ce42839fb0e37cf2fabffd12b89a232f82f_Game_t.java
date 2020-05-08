 /**
  * The 'engine' package encloses every functionality the engine has and provides.
  */
 package com.lambda;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The 'Game' class contains methods every 'Game' needs to have in order
  * to work properly with the engine.
  * 
  * @author alex
  *
  */
 public abstract class Game {
 	
 	/**
 	 * The 'GameContainer' that holds this 'Game'.
 	 */
 	protected GameContainer gameContainer;
 	
 	/**
 	 * A 'Map' of 'GameStates' the 'Game' can switch from/to.
 	 */
 	private Map<Integer, GameState> states;
 	
 	/**
 	 * The ID of the currently active 'GameState'.
 	 */
 	private int activeState = -1;
 
 	/**
 	 * Initializes all objects the 'Game' needs to work properly.
 	 * 
 	 * @param gc The 'GameContainer' that holds this 'Game'.
 	 */
 	public void init(GameContainer gc) {
 		states = new HashMap<Integer, GameState>();
 		
 		setGameContainer(gc);
 	}
 
 	/**
 	 * Calls the 'render()'-method for the currently active 'GameState'. 
 	 * This will only work if there is a currently active 'GameState'.
 	 */
 	public void render() {
 		if(states.containsKey(activeState)) {
 			states.get(activeState).render();
 		}
 	}
 	
 	/**
 	 * Calls the 'update()'-method for the currently active 'GameState'.
 	 * This will only work if there is a currently active 'GameState'.
 	 * 
 	 * @param delta The time that has passed between the looping of two frames.
 	 */
 	public void update(double delta) {
 		if(states.containsKey(activeState)) {
 			states.get(activeState).update(delta);
 		}
 	}
 	
 	/**
 	 * Registers the 'GameState' 'state' with the ID 'id', so it can be used
 	 * by the 'Game'. Upon registering the 'init()'-method will be called.
 	 * This will only work if the 'GameState' is not 'null' and 
 	 * if the ID 'id' is non-existent within the already registered 'GameStates'.
 	 * 
 	 * @param state The 'GameState' to register for the 'Game'.
 	 * @param id The ID the newly registered 'GameState' is going to have.
 	 */
 	public void registerState(GameState state, int id) {
 		if(state != null && !states.containsKey(id)) {
 			states.put(id, state);
 			state.init(gameContainer, this);
 		}
 	}
 	
 	/**
 	 * Gives back the registered 'GameState' with the ID 'id'.
 	 * The 'GameState' must be registered and existent, otherwise 'null'
 	 * will be returned.
 	 * 
 	 * @param id The ID of the 'GameState'.
 	 * @return The 'GameState' with the ID 'id'.
 	 */
 	public GameState getState(int id) {
 		GameState state = null;
 		
 		if(states.containsKey(id)) {
 			state = states.get(id);
 		}
 		
 		return state;
 	}
 	
 	/**
 	 * Switches the current 'GameState' to another 'GameState' that is currently registered.
 	 * This will only work if the 'GameState' with the ID 'id' actually exists and is registered.
 	 * 
 	 * @param id The ID of the 'GameState' to switch to.
 	 */
 	public void switchState(int id) {
 		if(states.containsKey(id)) {
 			this.activeState = id;
 		}
 	}
 	
 	/**
 	 * Removes the 'GameState' specified by the ID 'id' from the list of registered
 	 * 'GameStates' so it is not usable anymore.
 	 * This will only work, if the 'GameState' exists and is registered.
 	 * 
 	 * @param id The ID of the 'GameState' to remove.
 	 */
 	public void removeState(int id) {
 		if(states.containsKey(id)) {
 			states.remove(id);
 		}
 	}
 
 	/**
 	 * Gives back the ID of the currently active 'GameState'.
 	 * 
 	 * @return The ID of the currently active 'GameState'.
 	 */
 	public int getActiveState() {
 		return activeState;
 	}
 	
 	/**
 	 * Sets the 'GameContainer' that holds the 'Game' to 'gc'.
 	 * This will only work if 'gc' is not 'null'.
 	 * 
 	 * @param gc The 'GameContainer' that holds the 'Game'.
 	 */
 	public void setGameContainer(GameContainer gc) {
 		if(gc != null) {
 			this.gameContainer = gc;
 		}
 	}
 	
 	/**
 	 * Gives back the 'GameContainer' that holds this 'Game'.
 	 * 
 	 * @return The 'GameContainer' that holds this 'Game'.
 	 */
 	public GameContainer getGameContainer() {
 		return gameContainer;
 	}
 }
