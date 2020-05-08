 package com.punchline.javalib.entities.systems.generic;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.systems.InputSystem;
 import com.punchline.javalib.utils.Convert;
 
 /**
  * System for moving the camera based on keyboard input from the arrow keys.
  * @author Natman64
  * @created Jul 23, 2013
  */
 public class CameraMovementSystem extends InputSystem  {
 
 	private static final float CAMERA_SPEED = 200f;
 	private static final float MOVEMENT_BORDER = .125f;
 	
 	private Camera camera;
 	private Rectangle bounds;
 	
 	private boolean movingLeft = false;
 	private boolean movingRight = false;
 	private boolean movingDown = false;
 	private boolean movingUp = false;
 	
 	/**
 	 * Makes a CameraMovementSystem
 	 * @param input the game's current InputMultiplexer.
 	 * @param camera The camera that this system will control.
 	 */
 	public CameraMovementSystem(InputMultiplexer input, Camera camera, Rectangle bounds) {
 		super(input);
 		
 		this.camera = camera;
 		this.bounds = Convert.metersToPixels(bounds);
 	}
 
 	@Override
 	public boolean canProcess(Entity e) {
 		return false; //Doesn't actually process Entities
 	}
 
 	@Override
 	public void processEntities() {
 		super.processEntities();
 		
 		Vector3 movement = new Vector3(0, 0, 0);
 		
 		if (movingLeft) {
 			movement.x = - 1;
 		} else if (movingRight) {
 			movement.x = 1;
 		}
 		
 		if (movingUp){
 			movement.y = 1;
 		} else if (movingDown) {
 			movement.y = -1;
 		}
 		
 		movement.nor();
 		movement.scl(CAMERA_SPEED);
 		movement.scl(deltaSeconds());
 		
 		camera.translate(movement);
 		
 		float left = camera.position.x - camera.viewportWidth / 2;
 		float bottom = camera.position.y - camera.viewportHeight / 2;
 		float right = left + camera.viewportWidth;
 		float top = bottom + camera.viewportHeight;
 		
 		if (left < bounds.x) {
 			camera.position.x = bounds.x + camera.viewportWidth / 2;
 		}
 		
		if (top > bounds.y + bounds.height) {
			camera.position.y = bounds.y + bounds.height - camera.viewportHeight / 2;
 		}
 		
 		if (right > bounds.x + bounds.width) {
 			camera.position.x = bounds.x + bounds.width - camera.viewportWidth / 2;
 		}
 		
 		if (bottom < bounds.y) {
 			camera.position.y = bounds.y + camera.viewportHeight / 2;
 		}
 		
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.LEFT) {
 			movingLeft = true;
 			return true;
 		} else if (keycode == Keys.RIGHT){
 			movingRight = true;
 			return true;
 		}
 		
 		if (keycode == Keys.UP) {
 			movingUp = true;
 			return true;
 		} else if (keycode == Keys.DOWN) {
 			movingDown = true;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Keys.LEFT) {
 			movingLeft = false;
 			return true;
 		} else if (keycode == Keys.RIGHT){
 			movingRight = false;
 			return true;
 		}
 		
 		if (keycode == Keys.UP) {
 			movingUp = false;
 			return true;
 		} else if (keycode == Keys.DOWN) {
 			movingDown = false;
 			return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		
 		int windowWidth = Gdx.graphics.getWidth();
 		int windowHeight = Gdx.graphics.getHeight();
 		
 		int leftThreshold = (int)(windowWidth * MOVEMENT_BORDER);
 		int upThreshold = (int)(windowHeight * MOVEMENT_BORDER);
 		int downThreshold = windowHeight - upThreshold; 
 		int rightThreshold = windowWidth - leftThreshold;
 		
 		
 		if (screenX < leftThreshold) {
 			movingLeft = true;
 		} else if (screenX > rightThreshold) {
 			movingRight = true;
 		} else {
 			movingLeft = false;
 			movingRight = false;
 		}
 		
 		if (screenY > downThreshold) {
 			movingDown = true;
 		} else if (screenY < upThreshold) {
 			movingUp = true;
 		} else {
 			movingDown = false;
 			movingUp = false;
 		}
 		
 		return false; //Let other systems take mouse input.
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		if (Gdx.app.getType() == ApplicationType.Android) {
 			mouseMoved(screenX, screenY);
 		} else {
 			movingLeft = false;
 			movingRight = false;
 			movingDown = false;
 			movingUp = false;
 		}
 		
 		return false;
 	}
 	
 }
