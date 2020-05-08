 package tutorials.slickout.dda;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import tutorials.slickout.dda.observer.AbstractObserver;
 import tutorials.slickout.dda.observer.LifeAndPaddleObserver;
 import tutorials.slickout.dda.sensor.AbstractSensor;
import tutorials.slickout.gameplay.collision.CollisionManager;
 import tutorials.slickout.gameplay.level.ILevel;
import tutorials.slickout.gameplay.level.PowerUp;
 
 public class AdaptationDriver {
 	
	
	private List<AbstractObserver> observers = new ArrayList<AbstractObserver>();
 	private ILevel level;
 	//used to time pu rain events
	private int rainTimer = 0;
	private CollisionManager collisionManager;
 	
	public AdaptationDriver(ILevel level, CollisionManager collisionManager) {
 		this.level = level;
		this.collisionManager = collisionManager;
 	}
 
 
 
 
 
 	public List<AbstractObserver> getObservers(){
 		return observers;
 	}
 	
 	
 	
 
 
 	public void processAdaptations(int delta) {
 
 		rainTimer += delta;
 		
 		//System.out.println(timeSec);
 		//get values from observers
 		for(AbstractObserver observer: observers){
 			//
 			if(observer.getClass().getSimpleName().equals("LifeAndPaddleObserver")){
 			
 			//pu rain
 			}else if(observer.getClass().getSimpleName().equals("PaddleAndBricksObserver")){
				if(observer.getAdaptations()==1 && rainTimer >= 2000){					
					PowerUp pu = level.addPowerUp(new Vector2f((float) (Math.random()*level.getWidth()),0));
					collisionManager.addCollidable(pu);
 					rainTimer = 0;
 				}
 
 			}
 		}
 	}
 	
 }
