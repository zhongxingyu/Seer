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
 	
 	private CollisionManager collisionManager;
 	private List<AbstractObserver> observers = new ArrayList<AbstractObserver>();
 	private ILevel level;
 	//used to time pu rain events
 	private int rainTimer = 0;
 	
 	public AdaptationDriver(ILevel level, CollisionManager collisionManager) {
 		this.level = level;
 		this.collisionManager = collisionManager;
 	}
 
 
 
 
 
 	public List<AbstractObserver> getObservers(){
 		return observers;
 	}
 	
 	
 	
 
 
 	public void processAdaptations(int delta) {
 
 		rainTimer += delta;
 		
 		for(AbstractObserver observer: observers){	
 			
 			//used to remove adaptations once they've been implemented (power-up rain isn't removed)
 			List<Adaptation> removals = new ArrayList<Adaptation>();
 			
 			for(Adaptation adaptation: observer.getAdaptations()){
 				if(adaptation.getCode().equals("PowerUpRain") && rainTimer >= 2000){
					PowerUp pu = level.addPowerUp(new Vector2f((float) (Math.random()*level.getWidth()),0), (int) (Math.random()*4)+1);
					//collisionManager.addCollidable(pu);
 					rainTimer = 0;
 				}else if(adaptation.getCode().equals("IncreaseRedPowerUps")){
 					level.setExtraRedP(0.1);
 					removals.add(adaptation);
 				}else if(adaptation.getCode().equals("SlowDownPowerUps")){
 					level.changePowerUpSpeed(-0.04f);
 					removals.add(adaptation);
 				}
 			}
 			
 			for(Adaptation adaptation: removals){
 				observer.getAdaptations().remove(adaptation);
 			}
 			
 			
 		}
 		
 		
 	}
 	
 }
