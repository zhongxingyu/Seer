 package org.tedxuba.social.events;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 
 public abstract class SocialEventListener extends Observable {
 	private Map<String,Integer> lastCountMap = new HashMap<String, Integer>();
 	private Map<String,Integer> countDiffMap = new HashMap<String, Integer>();
 	
 	public abstract String getSocialNetworkName();
 	public abstract List<String> getEventNames();
 	
 	public void setLastCount(String eventName, Integer lastCount) {
 		this.lastCountMap.put(eventName, lastCount);
 	}
 	
 	public Integer getLastCount(String eventName) {
 		return lastCountMap.get(eventName);
 	}
 	
 	public Integer getCountDiff(String eventName) {
		return countDiffMap.get(eventName);
 	}
 	
 	public void setCountDiff(String eventName, Integer countDiff) {
 		this.countDiffMap.put(eventName, countDiff);
 	}
 	
 	public abstract void start();
 }
