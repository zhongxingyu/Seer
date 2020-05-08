 import java.util.HashMap;
 
 import robocode.AdvancedRobot;
 import robocode.CustomEvent;
 
 
 @SuppressWarnings("serial")
class TriggerSet extends HashMap<String, Trigger> {
 	
 	public void add(Trigger t) {
 		this.put(t.getName(), t);
 	}
 	
 	public void trigger(String name) {
 		if (this.containsKey(name))  {
 			this.get(name).action();
 		}
 	}
 	
 	public void trigger(CustomEvent event) {
 		this.trigger(event.getCondition().getName());
 	}
 	
 	public void addTo(AdvancedRobot robot) {
 		for(Trigger t : this.values()) {
 			System.out.println("Adding trigger: "+t.getName());
 			robot.addCustomEvent(t);
 		}
 	}
 }
