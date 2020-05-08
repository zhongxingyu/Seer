 /**
  * Contains all the high-level game functions and implements the actual gameplay.
  */
 package kaninator.game;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import kaninator.graphics.*;
 import kaninator.io.MapFactory;
 import kaninator.mechanics.DynamicObject;
 
 /**
  * Contains all the relevant methods for moving a model around in the game.
  * Used to strap physics and controllable movement onto a DynamicObject.
  * @see kaninator.mechanics.DynamicObject
  */
 public class Model
 {
 	private double normalSpeed;
 	private double diagonalSpeed;
 	private double jumpSpeed = 13;
 	private double gravity = 1.5;
 	
 	private double vel_x, vel_y, vel_height;
 	private DynamicObject model, shadow;
 	private Map map;
 
 	
 	/**
 	 * Constructs a model from the animations given in the parameters. Creates a couple of DynamicObjects that
 	 * correspond to the model, ie. a DynamicObject for the animation itself and one for the shadow connected
 	 * to the model.
 	 * @param animations The ArrayList of Animations that build up the model itself, cannot be null or of the size 0.
 	 * @param _map The map used for collision detection and height checking.
 	 * @param x The initial x coordinate for the model in the internal, isometric coordinate system.
 	 * @param y The initial y coordinate for the model in the internal, isometric coordinate system.
 	 * @param radius_constant How much the width of the Animations should be divided with to get a realistic estimate for the radius of the model.
 	 * @param speed The speed the model will be moving at in isometric coordinates.
 	 * @throws Exception If the animation is null or of the size 0, since the model cannot be created without these.
 	 */
 	public Model(ArrayList <Animation> animations, Map _map, double x, double y, double radius_constant, double speed) throws ModelException
 	{
 		if(animations == null || animations.size() < 1)
 			throw new ModelException("ERR: Model animation doesn't exist");
 		
 		map = _map;
 		
 		double radius = animations.get(0).getWidth()/radius_constant;
 		model = new DynamicObject(animations, radius);
 		model.setPos(x, y);
 
 		ArrayList<Animation> shadowAnimList = AnimationFactory.createAnimations(new Shadow(radius * 2));
 	
 		shadow = new DynamicObject(shadowAnimList, radius);
 		shadow.setPos(x, y);
 		
 		normalSpeed = speed;
 		diagonalSpeed = Math.sqrt(normalSpeed*normalSpeed/2.0);
 	}
 
 	
 	/**
 	 * Updates the model, in essence checking for collisions, moving it into the
 	 * correct direction and moving the shadow correspondingly.
 	 */
 	public void update()
 	{
 		vel_height -= gravity;
 		model.move_vert(vel_height);
 		double mapHeight = map.getHeight(model);
 		if(onGround())
 		{
 			vel_height = 0.0;
 			model.setHeight(mapHeight);
 		}
 
 		double old_x = model.get_x();
 		double old_y = model.get_y();
 		model.move_x(vel_x);
 		if(map.getHeight(model) >= model.getHeight() + MapFactory.getTileHeight()/2.0)
 			model.setPos(old_x, model.get_y());
 		
 		model.move_y(vel_y);
 		if(map.getHeight(model) >= model.getHeight() + MapFactory.getTileHeight()/2.0)
 			model.setPos(model.get_x(), old_y);
 		
 		shadow.setHeight(mapHeight);
 		shadow.setPos(model.get_x(), model.get_y());
 	}
 	
 	/**
 	 * Sets the speed on the internal, isometric, x-axis for the object.
 	 * @param direction 0 if there is no movement. Negative if the movement is in the negative direction. Positive if the movement is in the positive direction.
 	 */
 	public void move_x(int direction)
 	{
 		if(direction > 0)
 			direction = 1;
 		else if(direction < 0)
 			direction = -1;
 		
 		if(Math.abs(vel_x) <= 0.001 || Math.abs(vel_y) <= 0.001)
 		{
 			vel_x = normalSpeed * direction;
 		}
 		else
 		{
 			vel_y = diagonalSpeed * Math.signum(vel_y);
 			vel_x = diagonalSpeed * direction;
 		}
 	}
 	
 	/**
 	 * Sets the speed on the internal, isometric, y-axis for the object.
 	 * @param direction 0 if there is no movement. Negative if the movement is in the negative direction. Positive if the movement is in the positive direction.
 	 */
 	public void move_y(int direction)
 	{
 		if(direction > 0)
 			direction = 1;
 		else if(direction < 0)
 			direction = -1;
 		
 		if(Math.abs(vel_x) <= 0.001 || Math.abs(vel_y) <= 0.001)
 		{
 			vel_y = normalSpeed * direction;
 		}
 		else
 		{
 			vel_x = diagonalSpeed * Math.signum(vel_x);
 			vel_y = diagonalSpeed * direction;
 		}
 	}
 	
 	/**
 	 * Sets the velocity on the internal, isometric, x-axis without any regard to the speed attribute of the model.
 	 * @param _vel_x The new velocity on the x-axis
 	 */
 	public void setVelX(double _vel_x)
 	{
 		vel_x = _vel_x;
 	}
 	
 	/**
 	 * Sets the velocity on the internal, isometric, y-axis without any regard to the speed attribute of the model.
 	 * @param _vel_y The new velocity on the y-axis
 	 */
 	public void setVelY(double _vel_y)
 	{
 		vel_y = _vel_y;
 	}	
 	
 	/**
 	 * Sets vertical velocity for the model without any regard to normal jumping restrictions.
 	 * @param _vel_y The new vertical velocity.
 	 */
 	public void setVelHeight(double _vel_height)
 	{
 		vel_height = _vel_height;
 	}	
 	
 	/**
 	 * Sets the jump speed (the value the vertical velocity is set to when you first jump).
 	 * @param _jumpSpeed The new jump speed
 	 */
 	public void setJumpSpeed(double _jumpSpeed)
 	{
 		jumpSpeed = _jumpSpeed;
 	}
 	
 	
 	/**
 	 * Getter for the velocity on the x-axis for the model
 	 * @return The velocity on the x-axis.
 	 */
 	public double getVelX()
 	{
 		return vel_x;
 	}
 	
 	/**
 	 * Getter for the velocity on the y-axis for the model
 	 * @return The velocity on the y-axis.
 	 */
 	public double getVelY()
 	{
 		return vel_y;
 	}
 	
 	/**
 	 * Returns true if the model is not airborne.
 	 * @return True if the model is standing on the ground, otherwise false.
 	 */
 	public boolean onGround()
 	{
 		double mapHeight = map.getHeight(model);
		return vel_height <= 0.0 && model.getHeight() <= mapHeight;
 	}
 	
 	/**
 	 * Makes the model jump according to its jumpSpeed. Jumps only happen 
 	 * if the model is on the ground and not already moving vertically. 
 	 */
 	public void jump()
 	{
 		if(vel_height == 0.0 && Math.abs(model.getHeight() - map.getHeight(model)) < normalSpeed)
 			vel_height = jumpSpeed;
 	}
 	
 	/**
 	 * Gets the state of the model. In essence which animation it is currently drawing.
 	 * @return The state of the model.
 	 */
 	public int getState()
 	{
 		return model.getState();
 	}
 	
 	/**
 	 * Sets the state of the model. In essence which animation it is currently drawing.
 	 * @param _state The state of the model.
 	 */
 	public void setState(int _state)
 	{
 		model.setState(_state);
 	}
 	
 	/**
 	 * Advances the currently active Animation.
 	 * @see kaninator.graphics.Animation#advance()
 	 */
 	public void advanceAnimation()
 	{
 		model.getAnimation().advance();
 	}
 	
 	/**
 	 * Resets the currently active Animation.
 	 * @see kaninator.graphics.Animation#reset()
 	 */
 	public void reset()
 	{
 		model.getAnimation().reset();
 	}
 	
 	/**
 	 * Gets the DynamicObject representing the model, collision detection is usually performed against this object.
 	 * @return The main DynamicObject
 	 */
 	public DynamicObject getModel()
 	{
 		return model;
 	}
 	
 	/**
 	 * Gets all the DynamicObjects associated with the model. Ie. The model itself and its shadow.
 	 * @return A LinkedList of all the DynamicObjects associated with the model.
 	 */
 	public LinkedList<DynamicObject> getDynamicObjects()
 	{
 		LinkedList<DynamicObject> objects = new LinkedList<DynamicObject>();
 		objects.add(shadow);
 		objects.add(model);
 		return objects;
 	}
 }
