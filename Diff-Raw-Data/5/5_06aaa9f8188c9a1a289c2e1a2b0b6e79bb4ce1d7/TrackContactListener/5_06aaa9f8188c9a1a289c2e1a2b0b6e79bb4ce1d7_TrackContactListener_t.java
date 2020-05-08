 package cargame.elements;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
import cargame.CarGame;
 import cargame.screens.GameScreen;
 import cargame.utils.Box2DUtils;
 
 public class TrackContactListener implements ContactListener {
 
 	private GameScreen gameScreen;
 	private Body sensor1;
 	private Body sensor2;
 	
 	private Map<Car,Boolean> wrongDirection;
 	
 	private Map<Car,Boolean> passedSensor1;
 	private Map<Car,Boolean> passedSensor2;
 	
 	private long initialLapTime;
 	
 	public TrackContactListener(GameScreen game) {
 		super();
 		
 		this.gameScreen = game;
 		
 		// Finish line sensors
 		passedSensor1 = new HashMap<Car, Boolean>();
 		passedSensor2 = new HashMap<Car, Boolean>();
 		sensor2 = Box2DUtils.createPolygonBody(game.getWorld(), new Vector2(135, 25), 1f, 15f, 0f, 0f, 0f, false,true);
 		sensor1 = Box2DUtils.createPolygonBody(game.getWorld(), new Vector2(140, 25), 1f, 15f, 0f, 0f, 0f, false,true);
 		
 		wrongDirection =  new HashMap<Car, Boolean>();
 	}
 	
 	public void startListener(){
 		wrongDirection.clear();
 		passedSensor1.clear();
 		passedSensor2.clear();
 		
 		// Move cars to start line
 		for(int i=0;i<this.gameScreen.getElements().size();i++){
 			Car car = (Car) this.gameScreen.getElements().get(i);
 			if(car != null){
 				wrongDirection.put(car, true);
 				car.getBody().setTransform(sensor1.getPosition().x+20, sensor1.getPosition().y +6 -this.gameScreen.getPlayerCar().getPlayer().id*10, (float)( sensor1.getAngle()-Math.PI/2.0));
 			}
 			
 		}
 	}
 	
 	public void startLapCounter(){
 		initialLapTime = System.currentTimeMillis();
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 
		if(!CarGame.getInstance().checkStatus(CarGame.STATUS_PLAYING)) return;
 		// Finish line sensors
 		Car car = getCarCollision(contact,sensor1);
         if(car != null && car == gameScreen.getPlayerCar()){
         	passedSensor1.put(car, true);
         	if(passedSensor2.containsKey(car) && passedSensor2.get(car)){
         		if(!wrongDirection.containsKey(car) || !wrongDirection.get(car)){
         			wrongDirection.put(car,true); 
         			car.setWrongDirection(true);
         		}
         		
         		passedSensor2.put(car, false);
         		passedSensor1.put(car, false);
         	}
         }
         car = getCarCollision(contact,sensor2);
         if(car != null && car == gameScreen.getPlayerCar()){
         	passedSensor2.put(car, true);
         	if(passedSensor1.containsKey(car) && passedSensor1.get(car)){
         		if(wrongDirection.containsKey(car) && wrongDirection.get(car)){
         			wrongDirection.put(car, false);
         			car.setWrongDirection(false);
         		}else{
         			car.addLap(System.currentTimeMillis() - initialLapTime);
         			initialLapTime = System.currentTimeMillis();
         		}
         		passedSensor1.put(car, false);
         		passedSensor2.put(car, false);
         	}
         }
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 	}
 	
 	private Car getCarCollision(Contact contact,Body sensor){
 		List<Element> gameElements = gameScreen.getElements();
 		
 		for(Element element: gameElements){
 			if(contact.getFixtureA().getBody().equals(element.getBody()) && contact.getFixtureB().getBody().equals(sensor)){
 				return (Car) element;
 			}
 			if(contact.getFixtureB().getBody().equals(element.getBody()) && contact.getFixtureA().getBody().equals(sensor)){
 				return (Car) element;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void postSolve(Contact arg0, ContactImpulse arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void preSolve(Contact arg0, Manifold arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }
