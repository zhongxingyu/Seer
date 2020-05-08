 import robocode.Condition;
 
 
public abstract class Trigger extends Condition {
 	public Trigger(String name) {
 		super(name);
 	}
 	public abstract void action();
 }
