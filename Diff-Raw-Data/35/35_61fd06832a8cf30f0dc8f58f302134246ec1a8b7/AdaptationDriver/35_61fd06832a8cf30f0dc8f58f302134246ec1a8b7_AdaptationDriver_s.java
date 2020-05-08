 package tutorials.slickout.dda;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import tutorials.slickout.dda.observer.AbstractObserver;
 import tutorials.slickout.dda.observer.LifeAndPaddleObserver;
 import tutorials.slickout.dda.sensor.AbstractSensor;
 import tutorials.slickout.gameplay.level.ILevel;
 
 public class AdaptationDriver {
 	
	int padHit=0;
	int bricksHit=0;
	int lastBricksTotal = 0;
	int padHitTotal = 0;
	List<AbstractObserver> observers = new ArrayList<AbstractObserver>();
	Driver driver;
 	private ILevel level;
 	//used to time pu rain events
	int rainTimer = 0;
 	
	public AdaptationDriver(ILevel level) {
 		this.level = level;
		driver = new Driver(level);
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
					level.addPowerUp(new Vector2f((float) (Math.random()*level.getWidth()),0));
 					rainTimer = 0;
 				}
 
 			}
 		}
 	}
 	
 }
