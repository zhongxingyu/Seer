 package se.chalmers.tda367.group15.game.controllers;
 
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Rectangle2D.Float;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.util.pathfinding.PathFindingContext;
 import org.newdawn.slick.util.pathfinding.TileBasedMap;
 
 import se.chalmers.tda367.group15.game.controllers.SoundEffectsController.GameMusic;
 import se.chalmers.tda367.group15.game.models.AbstractMovingModel;
 import se.chalmers.tda367.group15.game.models.AbstractProjectileModel;
 import se.chalmers.tda367.group15.game.models.BossModel;
 import se.chalmers.tda367.group15.game.models.CoworkerModel;
 import se.chalmers.tda367.group15.game.models.RoomModel;
 import se.chalmers.tda367.group15.game.models.SoldierModel;
 import se.chalmers.tda367.group15.game.settings.Constants;
 
 /**
  * Abstract class for representing a room containing a tiled map and controllers
  * for all moving models.
  * 
  * @author simon
  * 
  */
 public class RoomController implements TileBasedMap {
 
 	/**
 	 * Variable used to check if room is unlocked.
 	 */
 	private boolean roomUnlocked;
 
 	/**
 	 * The layer containing the blocked boolean
 	 */
 	private int blockedLayerID = 0;
 
 	/**
 	 * A reference to the game's gameController.
 	 */
 	private GameController gameController;
 
 	/**
 	 * The model of the room.
 	 */
 	private RoomModel roomModel;
 
 	/**
 	 * The tiled map to be used in the room
 	 */
 	private TiledMap map;
 
 	/**
 	 * List containing the static bounds represented by rectangles.
 	 */
 	private List<Rectangle2D.Float> staticBounds;
 
 	/**
 	 * Map containing models and it's corresponding collision rectangles.
 	 */
 	private Map<AbstractMovingModel, Rectangle2D.Float> dynamicBounds;
 
 	/**
 	 * List containing controllers for all moving models in the room.
 	 */
 	private List<AbstractMovingModelController> movingModelControllers;
 
 	// List used in update method to add new controllers
 	private List<AbstractMovingModelController> quedControllers = new ArrayList<AbstractMovingModelController>();
 
 	/**
 	 * Creates a new AbstractMovingModelController.
 	 * 
 	 * @param gameController
 	 */
 	protected RoomController(GameController gameController, RoomModel roomModel) {
 		staticBounds = new ArrayList<Rectangle2D.Float>();
 		dynamicBounds = new HashMap<AbstractMovingModel, Rectangle2D.Float>();
 		movingModelControllers = new ArrayList<AbstractMovingModelController>();
 		this.gameController = gameController;
 		this.roomModel = roomModel;
 		this.setMap(roomModel.getLockedMapPath());
 	}
 
 	/**
 	 * Method for adding a moving model to this room.
 	 * 
 	 * @param model
 	 *            The model to be added
 	 */
 	public void addMovingModel(CoworkerModel model) {
 		movingModelControllers.add(new CoworkerController(model, this,
 				gameController));
 		dynamicBounds.put(model, model.getBounds());
 	}
 
 	private void addMovingModel(SoldierModel model) {
 		movingModelControllers.add(new SoldierController(model, this,
 				gameController));
 		dynamicBounds.put(model, model.getBounds());
 	}
 
 	private void addMovingModel(BossModel model) {
 		movingModelControllers.add(new BossController(model, this,
 				gameController));
 		dynamicBounds.put(model, model.getBounds());
 	}
 
 	/**
 	 * Method for adding a projectile to the room.
 	 * 
 	 * @param projectile
 	 *            The projectile to be added
 	 */
 	public void addProjectile(AbstractProjectileModel projectile) {
 		quedControllers.add(new ProjectileController(getGameController(),
 				projectile));
 		dynamicBounds.put(projectile, projectile.getBounds());
 	}
 
 	/**
 	 * Method for adding a swing infront of the hero
 	 * 
 	 * @param swing
 	 *            The swing to be added
 	 */
 	public void addSwing(AbstractMovingModel swing) {
 		quedControllers
 				.add(new MeleeSwingController(getGameController(), swing));
 		dynamicBounds.put(swing, swing.getBounds());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean blocked(PathFindingContext context, int tx, int ty) {
 		// TODO How to use PathFindingContext?
 		try {
 			int tileID = map.getTileId(tx, ty, blockedLayerID);
 			String property = map.getTileProperty(tileID, "blocked", "false");
 			return !property.equals("false");
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return true;
 		}
 	}
 
 	/**
 	 * Method for getting controllers for all moving models in the room.
 	 * 
 	 * @return List of all moving model controllers.
 	 */
 	public List<AbstractMovingModelController> getControllers() {
 		return movingModelControllers;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public float getCost(PathFindingContext context, int tx, int ty) {
 		return 1;
 	}
 
 	/**
 	 * Method for getting a map of all dynamic collision bounds.
 	 * 
 	 * @return A map containing models and it's corresponding collision bounds.
 	 */
 	public Map<AbstractMovingModel, Rectangle2D.Float> getDynamicBounds() {
 		// update bounds
 		Iterator<AbstractMovingModel> it = dynamicBounds.keySet().iterator();
 
 		while (it.hasNext()) {
 			AbstractMovingModel model = it.next();
 			if (model.isAlive()) {
 				dynamicBounds.put(model, model.getBounds());
 			} else {
 				it.remove();
 			}
 		}
 		return dynamicBounds;
 	}
 
 	/**
 	 * Getter for the gameController associated with this controller.
 	 * 
 	 * @return the game controller
 	 */
 	protected GameController getGameController() {
 		return gameController;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getHeightInTiles() {
 		return map.getHeight(); // *map.getTileHeight(); if using pixels instead
 	}
 
 	/**
 	 * Method for getting the tiled map of the room.
 	 * 
 	 * @return A tiled map
 	 */
 	public TiledMap getMap() {
 		return map;
 	}
 
 	/**
 	 * Method for getting the static collision bounds of the room.
 	 * 
 	 * @return A list of static collision bounds.
 	 */
 	public List<Rectangle2D.Float> getStaticBounds() {
 		return staticBounds;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getWidthInTiles() {
 		return map.getWidth(); // *map.getTileWidth(); if using pixels instead
 	}
 
 	/**
 	 * Method for initializing the room.
 	 * 
 	 * @param container
 	 *            The container holding this game.
 	 * @throws SlickException
 	 *             Throw to indicate an internal error.
 	 */
 	public void init(GameContainer container) throws SlickException {
 		List<AbstractMovingModel> npcs = roomModel.getNpcModels();
 		for (AbstractMovingModel model : npcs) {
 			if (model instanceof CoworkerModel)
 				addMovingModel((CoworkerModel) model);
 			else if (model instanceof SoldierModel) {
 				addMovingModel((SoldierModel) model);
 			} else if (model instanceof BossModel) {
 				addMovingModel((BossModel) model);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void pathFinderVisited(int x, int y) {
 		// This is for debugging new heuristics.
 	}
 
 	/**
 	 * Method for rendering all model views in the room.
 	 * 
 	 * @param container
 	 *            The container holding this game.
 	 * @param g
 	 *            The graphics context that can be used to render. However,
 	 *            normal rendering routines can also be used.
 	 * @throws SlickException
 	 *             Throw to indicate an internal error
 	 */
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		// render map
 		map.render(0, 0);
 
 		// tell enemy controllers to render all enemy views
 		for (AbstractMovingModelController controller : movingModelControllers) {
 			controller.render(container, g);
 		}
 
 		if (Constants.SHOW_BOUNDS) {
 			g.setColor(Color.red);
 			for (Rectangle2D.Float e : staticBounds) {
 				g.drawRect((int) e.getX(), (int) e.getY(), (int) e.getWidth(),
 						(int) e.getHeight());
 			}
 		}
 	}
 
 	/**
 	 * Setter for the gameController to be associated with this controller.
 	 * 
 	 * @param gameController
 	 */
 	protected void setGameController(GameController gameController) {
 		this.gameController = gameController;
 	}
 
 	/**
 	 * Method for setting the map to be used in the room.
 	 * 
 	 * @param mapPath
 	 *            Path in filesystem to the tmx file representing a tiled map.
 	 */
 	public void setMap(String mapPath) {
 		try {
 			this.map = new TiledMap(mapPath);
 			int layerCount = map.getLayerCount();
 			blockedLayerID = -1;
 			for (int i = 0; i < layerCount; i++) {
 				if (map.getLayerProperty(i, "blocked", "false").equals("true")) {
 					blockedLayerID = i;
 				}
 			}
 
 			staticBounds.clear();
 			// add static collision bounds
 			for (int i = 0; i < map.getWidth(); i++) {
 				for (int j = 0; j < map.getHeight(); j++) {
 					int tileID = map.getTileId(i, j, blockedLayerID);
 					String property = map.getTileProperty(tileID, "blocked",
 							"false");
 					if (property.equals("true")) {
 						staticBounds.add(new Rectangle2D.Float(i * 32, j * 32,
 								32, 32));
 					}
 				}
 			}
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Method for updating the logic of all the rooms
 	 * 
 	 * @param container
 	 *            The container holding this game.
 	 * @param delta
 	 *            The amount of time thats passed since last update in
 	 *            milliseconds.
 	 * @param staticBounds
 	 *            the static collision bounds to be sent to all controllers of
 	 *            the room.
 	 * @param dynamicBounds
 	 *            the dynamic collision bounds to be sent to all controllers of
 	 *            the room.
 	 * @throws SlickException
 	 *             Throw to indicate an internal error.
 	 */
 	public void update(GameContainer container, int delta,
 			List<Float> staticBounds,
 			Map<AbstractMovingModel, Float> dynamicBounds)
 			throws SlickException {
 		// tell enemy controllers to move
 
 		Iterator<AbstractMovingModelController> it = movingModelControllers
 				.iterator();
 		while (it.hasNext()) {
 			AbstractMovingModelController controller = it.next();
 			if (controller.getModel().isAlive()) {
 				controller
 						.update(container, delta, staticBounds, dynamicBounds);
 			}
 		}
 
 		// add new qued controllers to the movingModelControllers list
 		it = quedControllers.iterator();
 		while (it.hasNext()) {
 			AbstractMovingModelController controller = it.next();
 			movingModelControllers.add(controller);
 		}
 		quedControllers.clear();
 
 		Collections.sort(movingModelControllers, new ControllerSorter());
 
 		if (allDead() && !roomUnlocked) {
			SoundEffectsController soundEffectsController = SoundEffectsController.instance();
			soundEffectsController.playGameMusic(GameMusic.BOSS_MUSIC);
 			setMap(roomModel.getUnlockedMapPath());
 			roomUnlocked = true;
 		}
 
 	}
 
 	// an inner class used to sort controllers
 	private class ControllerSorter implements
 			Comparator<AbstractMovingModelController> {
 
 		@Override
 		public int compare(AbstractMovingModelController c1,
 				AbstractMovingModelController c2) {
 			boolean m1Alive = c1.getModel().isAlive();
 			boolean m2Alive = c2.getModel().isAlive();
 
 			if ((m1Alive && m2Alive) || (!m1Alive && !m2Alive)) {
 				return 0;
 			} else if (m1Alive && !m2Alive) {
 				return 1;
 			} else {
 				return -1;
 			}
 		}
 
 	}
 
 	/**
 	 * Method for checking if all npc are dead.
 	 * 
 	 * @return true if all dead, false otherwise.
 	 */
 	public boolean allDead() {
 		for (AbstractMovingModel model : roomModel.getNpcModels()) {
 			if (model.isAlive()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Method for getting the room model for this controller.
 	 * 
 	 * @return the RoomModel object.
 	 */
 	public RoomModel getRoomModel() {
 		return roomModel;
 	}
 
 }
