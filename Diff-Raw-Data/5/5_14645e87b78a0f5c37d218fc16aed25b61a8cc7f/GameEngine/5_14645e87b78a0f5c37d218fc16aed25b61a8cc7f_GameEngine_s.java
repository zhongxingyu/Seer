 /**
  * This code is created by:
  * @author Esa Varemo (2012-2013)
  * It is released with license: 
  * @license This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  *          View it at: http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 
 package fi.dy.esav.GameEngine;
 
 import java.util.ArrayList;
 
 public class GameEngine {
 	
 	private ArrayList<Entity> entities = new ArrayList<Entity>();
 	private Stage stage;
 	
 	private Thread renderer;
 	private InputHandler inputhandler; //No modifier -> package-private
 	private InputState	 inputstate;   //No modifier -> package-private
 	
 
 	/**
 	 * Constructor method.
 	 */
 	public GameEngine() {
 		stage = new Stage(this);
 		stage.setSize(800, 600);
 	}
 	
 	/**
 	 * Start the engine (initialization + show)
 	 */
 	public void start() {
 		renderer = new Thread(new RenderThread(this));
 		this.getStage().setVisible(true);
 		renderer.start();
 	}
 	
 	/**
 	 * Close the window
 	 */
 	public void close() {
 		this.getStage().setVisible(false);
 	}
 	
 	/**
 	 * Stop the engine
 	 */
 	public void stop() {
 		renderer.stop();
 	}
 	
 	/**
 	 * @return the stage
 	 */
 	public Stage getStage() {
 		return stage;
 	}
 
 	/**
 	 * @param stage the stage to set
 	 */
 	public void setStage(Stage stage) {
 		this.stage = stage;
 	}
 	
 	/** 
 	 * Add an entity to the entities
 	 * @param ent Entity to be added
 	 * @return finishing status of the operation
 	 */
 	public boolean addEntity(Entity ent) {
 		return entities.add(ent);
 	}
 	
 	/**
 	 * Remove an entity from entities
 	 * @param ent entity to be removed
 	 * @return finishing status of the operation
 	 */
 	public boolean removeEntity(Entity ent) {
 		return entities.remove(ent);
 	}
 	
 	/**
 	 * Get all entities
 	 * @return ArrayList containing all the entities
 	 */
 	public ArrayList<Entity> getEntities() {
 		return this.entities;
 	}
 	
 	/**
 	 * Returns the input handler
 	 * Visibility: package-private
 	 * @return the input handler
 	 */
 	InputHandler getInputhandler() {
 		return inputhandler;
 	}
 
 	/**
 	 * Sets the inputHandler
 	 * Visibility: package-private
 	 * @param inputhandler the inputhandler to set
 	 */
 	void setInputhandler(InputHandler inputhandler) {
 		this.inputhandler = inputhandler;
 	}
 
 	/**
 	 * Return the state of inputs
 	 * @return the inputstate
 	 */
	public InputState getInputstate() {
 		return inputstate;
 	}
 
 	/**
 	 * Sets the input state container
 	 * @param inputstate the input state container
 	 */
	void setInputstate(InputState inputstate) {
 		this.inputstate = inputstate;
 	}
 	
 }
