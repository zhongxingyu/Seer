 package BattleBases;
 
 public class GruntWarrior extends AbstractWarrior {
 	
 	public GruntWarrior(){
 	}
 	
 	@Override
 	public void spawn(){
 		this.healthPoints = 100;
 		this.attackStrength = 10;
 		this.cost = 200;
 	}
 }
