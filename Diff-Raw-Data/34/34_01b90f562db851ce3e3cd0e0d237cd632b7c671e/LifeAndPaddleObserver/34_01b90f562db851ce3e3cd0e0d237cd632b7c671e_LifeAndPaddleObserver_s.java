 package tutorials.slickout.dda.observer;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import tutorials.slickout.dda.Adaptation;
 
 public class LifeAndPaddleObserver extends AbstractObserver {
 	
 	//totals
 	int livesLost;
 	int padHit;
 	//counters used for adaptation timing
 	int livesLostCounter = 0;
 	int padHitCounter = 0;
 
	//if 1 life lost after 10 bricks hit, pus are slowed down
 	@Override
 	public void update(Observable sensor, Object value) {
 		//if(sensor. instanceof PaddleHitSensor)
 		if(sensor.getClass().getSimpleName().equals("PaddleHitSensor")){
 			padHit = (Integer) value;
 			padHitCounter++;
 		}else if(sensor.getClass().getSimpleName().equals("LivesLostSensor")){
 			livesLost = (Integer) value;
 			livesLostCounter++;
 		}
 		
 		if(padHitCounter > 10){
			if(livesLostCounter >= 1){
 				try {
 					adaptations.add(new Adaptation("SlowDownPowerUps"));
 					System.out.println("SlowDownPowerUps Adaptation added");	
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}else{
 				System.out.println("No SlowDownPowerUps Adaptation added");	
 			}
 			padHitCounter = 0;
 			livesLostCounter =0;
 		}
 		
 		
 
 	}
 
 }
