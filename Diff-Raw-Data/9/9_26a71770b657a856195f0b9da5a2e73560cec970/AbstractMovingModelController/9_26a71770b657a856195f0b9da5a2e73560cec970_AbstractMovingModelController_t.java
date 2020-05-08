 package se.chalmers.tda367.group15.game.controllers;
 
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Rectangle2D.Float;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import se.chalmers.tda367.group15.game.models.AbstractCharacterModel;
 import se.chalmers.tda367.group15.game.models.AbstractMovingModel;
 import se.chalmers.tda367.group15.game.models.AbstractProjectileModel;
 import se.chalmers.tda367.group15.game.views.View;
 
 /**
  * abstract class that should be implemented by all controllers handling moving
  * models.
  * 
  * @author simon
  * 
  */
 public abstract class AbstractMovingModelController {
 
 	/**
 	 * The model that the controller is managing
 	 */
 	private AbstractMovingModel model;
 
 	/**
 	 * The view that the controller is managing
 	 */
 	private View view;
 
 	/**
 	 * A reference to the game's gameController
 	 */
 	private GameController gameController;
 
 	/**
 	 * Creates a new AbstractMovingModelController.
 	 * 
 	 * @param gameController
 	 */
 	protected AbstractMovingModelController(GameController gameController) {
 		this.setGameController(gameController);
 	}
 
 	/**
 	 * Method for rendering all views.
 	 * 
 	 * @param container
 	 *            The container holding this game.
 	 * @param g
 	 *            The graphics context that can be used to render. However,
 	 *            normal rendering routines can also be used.
 	 * @throws SlickException
 	 *             Throw to indicate a internal error
 	 */
 	public abstract void render(GameContainer container, Graphics g)
 			throws SlickException;
 
 	/**
 	 * Method for updating the logic of all the models
 	 * 
 	 * @param container
 	 *            The container holding this game.
 	 * @param delta
 	 *            The amount of time thats passed since last update in
 	 *            milliseconds
 	 * @param dynamicBounds
 	 *            the dynamic bounds of moving objects
 	 * @param staticBounds
 	 *            the static bounds of current map
 	 * @throws SlickException
 	 *             Throw to indicate a internal error
 	 */
 	public abstract void update(GameContainer container, int delta,
 			List<Float> staticBounds,
 			Map<AbstractMovingModel, Float> dynamicBounds)
 			throws SlickException;
 
 	/**
 	 * Method for getting the model that the controller is managing
 	 * 
 	 * @return the model
 	 */
 	public AbstractMovingModel getModel() {
 		return model;
 	}
 
 	/**
 	 * Method for getting the view that the controller is managing
 	 * 
 	 * @return the view
 	 */
 	public View getView() {
 		return view;
 	}
 
 	/**
 	 * Method for setting the model that the controller is managing
 	 * 
 	 * @param model
 	 *            to be set
 	 */
 	public void setModel(AbstractMovingModel model) {
 		this.model = model;
 	}
 
 	/**
 	 * Method for setting the view that the controller is managing
 	 * 
 	 * @param view
 	 *            to be set
 	 */
 	public void setView(View view) {
 		this.view = view;
 	}
 
 	/**
 	 * Method for checking if collision with model in (x,y) position is about to happen.
 	 * 
 	 * @param x
 	 *            the x coordinate
 	 * @param y
 	 *            the y coordinate
 	 * @param staticBounds
 	 *            list of rectangles representing static blocked object on the
 	 *            map
 	 * @param dynamicBounds
 	 *            map with moving models and their collision bounds
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isCollision(float x, float y,
 			List<Rectangle2D.Float> staticBounds,
 			Map<AbstractMovingModel, Rectangle2D.Float> dynamicBounds) {
 
 		return isCollision(x, y, model.getWidth(), model.getHeight(),
 				staticBounds, dynamicBounds);
 	}
 
 	/**
 	 * Method for checking if collision with width and height is about to
 	 * happen in position (x,y).
 	 * 
 	 * @param x
 	 *            the x coordinate
 	 * @param y
 	 *            the y coordinate
 	 * @param width
 	 *            The width of area to check
 	 * @param height
 	 *            The height of area to check
 	 * @param staticBounds
 	 *            list of rectangles representing static blocked object on the
 	 *            map
 	 * @param dynamicBounds
 	 *            map with moving models and their collision bounds
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isCollision(float x, float y, float width, float height,
 			List<Rectangle2D.Float> staticBounds,
 			Map<AbstractMovingModel, Rectangle2D.Float> dynamicBounds) {
 
 		Rectangle2D.Float bound1 = new Rectangle2D.Float(x, y, width, height);
 
 		return isCollision(staticBounds, dynamicBounds, bound1);
 	}
 
 	/**
 	 * Method for checking if collision with Rectangle2D object is about to
 	 * happen.
 	 * 
 	 * @param staticBounds
 	 *            list of rectangles representing static blocked object on the
 	 *            map
 	 * @param dynamicBounds
 	 *            map with moving models and their collision bounds
 	 * @param bound1
 	 *            the Rectangle2D witch to check if colliding
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isCollision(List<Rectangle2D.Float> staticBounds,
 			Map<AbstractMovingModel, Rectangle2D.Float> dynamicBounds,
 			Rectangle2D.Float bound1) {
 
 		// check static collisions
 		boolean staticCollision = isStaticCollision(staticBounds, bound1);
 
 		// check dynamic collisions
 		boolean dynamicCollsion = isDynamicCollision(dynamicBounds, bound1);
 
 		return staticCollision || dynamicCollsion;
 	}
 
 	/**
 	 * Method for checking if static collision with model is about to happen.
 	 * 
 	 * @param x
 	 *            the x coordinate
 	 * @param y
 	 *            the y coordinate
 	 * @param staticBounds
 	 *            list of rectangles representing static blocked object on the
 	 *            map
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isStaticCollision(float x, float y,
 			List<Rectangle2D.Float> staticBounds) {
 
 		Rectangle2D.Float bound1 = new Rectangle2D.Float(x, y,
 				model.getWidth(), model.getHeight());
 		return isStaticCollision(staticBounds, bound1);
 	}
 
 	/**
 	 * Method for checking if static collision with Rectangle2D object is about
 	 * to happen.
 	 * 
 	 * @param staticBounds
 	 *            list of rectangles representing static blocked object on the
 	 *            map
 	 * @param bound1
 	 *            the Rectangle2D witch to check if col23liding
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isStaticCollision(List<Rectangle2D.Float> staticBounds,
 			Rectangle2D.Float bound1) {
 
 		boolean staticCollision = false;
 		// check static collisions
 		for (Rectangle2D.Float bound2 : staticBounds) {
 			if (bound1.intersects(bound2)) {
 				staticCollision = true;
 				if(model instanceof AbstractProjectileModel) {
 					model.setAlive(false);
 				}
 			}
 			
 		}
 		return staticCollision;
 	}
 
 	/**
 	 * Method for checking if dynamic collision with model is about to happen.
 	 * 
 	 * @param x
 	 *            the x coordinate
 	 * @param y
 	 *            the y coordinate
 	 * @param dynamicBounds
 	 *            map with moving models and their collision bounds
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isDynamicCollision(float x, float y,
 			Map<AbstractMovingModel, Rectangle2D.Float> dynamicBounds) {
 
 		Rectangle2D.Float bound1 = new Rectangle2D.Float(x, y,
 				model.getWidth(), model.getHeight());
 		return isDynamicCollision(dynamicBounds, bound1);
 	}
 
 	/**
 	 * Method for checking if dynamic collision with Rectangle2D object is about
 	 * to happen.
 	 * 
 	 * @param dynamicBounds
 	 *            map with moving models and their collision bounds
 	 * @param bound1
 	 *            the Rectangle2D witch to check if colliding
 	 * @return true if collision, false otherwise
 	 */
 	public boolean isDynamicCollision(
 			Map<AbstractMovingModel, Rectangle2D.Float> dynamicBounds,
 			Rectangle2D.Float bound1) {
 
 		boolean dynamicCollsion = false;
 		// check dynamic collisions
 		for (AbstractMovingModel otherModel : dynamicBounds.keySet()) {
 			Rectangle2D.Float bound2 = otherModel.getBounds();
 			if (bound1.intersects(bound2) && this.model != otherModel) {
 				dynamicCollsion = true;
				if (model instanceof AbstractProjectileModel
						&& otherModel instanceof AbstractCharacterModel) {
					AbstractProjectileModel projectile = (AbstractProjectileModel) model;
 					int damage = projectile.getDamage();
					otherModel.takeDamage(damage);
 					projectile.setAlive(false);
 				}
 			}
 		}
 
 		return dynamicCollsion;
 	}
 
 	/**
 	 * Getter for the gameController associated with this controller.
 	 * 
 	 * @return
 	 */
 	protected GameController getGameController() {
 		return gameController;
 	}
 
 	/**
 	 * Setter for the gameController to be associated with this controller.
 	 * 
 	 * @param gameController
 	 */
 	protected void setGameController(GameController gameController) {
 		this.gameController = gameController;
 	}
 }
