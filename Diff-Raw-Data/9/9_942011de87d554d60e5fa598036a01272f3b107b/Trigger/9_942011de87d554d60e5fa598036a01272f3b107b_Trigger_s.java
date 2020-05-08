 import robocode.Condition;
 
 
abstract class Trigger extends Condition {
 	public Trigger(String name) {
 		super(name);
 	}
 	public abstract void action();
 }
