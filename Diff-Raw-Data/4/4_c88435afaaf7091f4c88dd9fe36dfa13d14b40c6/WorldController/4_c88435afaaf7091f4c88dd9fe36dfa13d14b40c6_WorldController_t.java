 package com.me.mygdxgame;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.me.mygdxgame.Car;
 import com.me.mygdxgame.Car.State;
 import com.me.mygdxgame.World;
 
 public class WorldController {
 
 	enum Keys {
 		LEFT, RIGHT, UP, DOWN
 	}
 
 	private World 	world;
 	private WorldRenderer renderer;
 	private Car 	car;
 
 	static Map<Keys, Boolean> keys = new HashMap<WorldController.Keys, Boolean>();
 	static {
 		keys.put(Keys.LEFT, false);
 		keys.put(Keys.RIGHT, false);
 		keys.put(Keys.UP, false);
 		keys.put(Keys.DOWN, false);
 	};
 
 	public WorldController(World world, WorldRenderer renderer) {
 		this.world = world;
 		this.renderer = renderer;
 		this.car = world.getCar();
 	}
 
 	// ** Key presses and touches **************** //
 
 	public void leftPressed() {
 		keys.get(keys.put(Keys.LEFT, true));
 	}
 
 	public void rightPressed() {
 		keys.get(keys.put(Keys.RIGHT, true));
 	}
 
 	public void upPressed() {
 		keys.get(keys.put(Keys.UP, true));
 	}
 
 	public void downPressed() {
 		keys.get(keys.put(Keys.DOWN, true));
 	}
 	
 	public void leftReleased() {
 		keys.get(keys.put(Keys.LEFT, false));
 	}
 
 	public void rightReleased() {
 		keys.get(keys.put(Keys.RIGHT, false));
 	}
 
 	public void upReleased() {
 		keys.get(keys.put(Keys.UP, false));
 	}
 
 	public void downReleased() {
 		keys.get(keys.put(Keys.DOWN, false));
 	}
 
 	/** The main update method **/
 	public void update(float delta) {
 		processInput();

		car.update(delta);
		
 		world.targets.update(car);
 
 		for(Zombie z : world.zombies){
 			z.update(delta);
 		}
 
 	}
 
 	/** Change Bob's state and parameters based on input controls **/
 	private void processInput() {
 		for(Zombie z:world.getZombies()){
 			z.follow(car.getCenterPosition().x, car.getCenterPosition().y);
 		}
 		if (Gdx.app.getType().equals(ApplicationType.Android)){
 			if(Gdx.input.getAccelerometerY() > Constants.TURN_THRESHOLD){
 				rightPressed(); //+Constants.ROTATION_SCALAR;
 				leftReleased();
 			}
 			else if(Gdx.input.getAccelerometerY() < -1* Constants.TURN_THRESHOLD){
 				leftPressed();
 				rightReleased();
 			}
 			else{
 				rightReleased();
 				leftReleased();
 			}
 			if (Gdx.input.getAccelerometerX() > Constants.X_OFFSET + Constants.TURN_THRESHOLD){
 				downPressed();
 				upReleased();
 			}
 			else if(Gdx.input.getAccelerometerX() < Constants.X_OFFSET + -1*Constants.TURN_THRESHOLD){
 				upPressed();
 				downReleased();
 			}//*/
 			else {
 				upReleased();
 				downReleased();
 			}
 //			car.acceleration -= Gdx.input.getAccelerometerX()*5;
 			System.out.println(Gdx.input.getAccelerometerX());
 //			if(Math.abs(car.acceleration) > Constants.CAR_MAX_ACCELERATION){
 //				car.acceleration = (car.acceleration/Math.abs(car.acceleration))*Constants.CAR_MAX_ACCELERATION;
 //			}
 //			acceleration = (float) (Gdx.input.getAccelerometerX()*-1*Constants.ACCELEROMETER_SCALAR);
 			// need to check if both or none direction are pressed, then Bob is idle
 			
 		}	
 		if (keys.get(Keys.LEFT)) {
 			car.rotateCCW(renderer,keys.get(Keys.DOWN));
 		}
 		if (keys.get(Keys.RIGHT)) {
 			car.rotateCW(renderer,keys.get(Keys.DOWN));
 		}
 		
 		if (keys.get(Keys.UP)) {
 			car.acceleration(1);
 		}
 		if (keys.get(Keys.DOWN)) {
 			car.acceleration(-1);		
 		}
 		// need to check if both or none direction are pressed, then Bob is idle
 		if ((!keys.get(Keys.UP)  && !keys.get(Keys.DOWN))) {
 			car.applyFriction();
 		}
 	}
 
 	
 }
