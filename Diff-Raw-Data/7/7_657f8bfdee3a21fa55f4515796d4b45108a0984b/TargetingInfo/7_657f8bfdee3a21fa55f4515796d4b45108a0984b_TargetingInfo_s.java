 package src.core;
 
 /**
  * Encapsulates information about how a tower will target creeps.
  */
 public class TargetingInfo {
 	private Strategy strategy;
 	private boolean hitsFlying;
 
 	public enum Strategy {
 		CLOSEST, FURTHEST, STRONGEST, WEAKEST
 	}
 
 	public Strategy getStrategy() {
 		return strategy;
 	}
 
 	public void setStrategy(Strategy strategy) {
 		this.strategy = strategy;
 	}
 
 	public boolean isHitsFlying() {
 		return hitsFlying;
 	}
 
 	public void setHitsFlying(boolean hitsFlying) {
 		this.hitsFlying = hitsFlying;
 	}
 }
