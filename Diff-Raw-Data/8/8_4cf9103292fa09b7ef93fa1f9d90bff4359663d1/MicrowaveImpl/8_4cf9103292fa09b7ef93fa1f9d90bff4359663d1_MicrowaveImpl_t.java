 package ar.com.maba.tesis.microwave;
 
 import ar.com.maba.tesis.preconditions.ClassDefinition;
 import ar.com.maba.tesis.preconditions.Pre;
 
 //@ClassDefinition(
 //	    builder = "(new ar.com.maba.tesis.microwave.MicrowaveImpl)", 
 //	    invariant = "(and " +
 //	    		"			(==> on (not doorOpened)) " +
 //	    		"			(==> on (and (power > 0) (time > 0)))" +
 //	    		"			(==> (not on) (and (= power 0) (= time 0)))" +
 //	    		"	)")
 
 @ClassDefinition(
 builder = "(new ar.com.maba.tesis.microwave.MicrowaveImpl)", 
 invariant = "(true)")
 public class MicrowaveImpl implements Microwave {
 	
 	private static Integer DEFAULT_TIME = 30;
 	private static Integer DEFAULT_POWER = 900;
 	
 	private boolean on = false;
 	private boolean doorOpened = false;
 	private Integer power = 0;
 	private Integer time = 0;
 
 	@Override
 	@Pre(value = "(and (not doorOpened) (not on))")
 	public void start() {
 		start(DEFAULT_TIME, DEFAULT_POWER);
 	}
 
 	@Override
 	@Pre(value = "(and (not doorOpened) (not on) (> p0 0))")
 	public void start(Integer time) {
 		start(time, DEFAULT_POWER);
 	}
 
 	@Override
 	@Pre(value = "(and (not doorOpened) (not on) (> p0 0) (> p1 0))")
 	public void start(Integer time, Integer power) {
		if (doorOpened) {
			throw new IllegalStateException("cannot start with door open");
		}
		if (on) {
			throw new IllegalStateException("it's already started");
 		}
 		on = true;
 		this.time = time;
 		this.power = power;
 	}
 
 	@Override
 	@Pre(value = "(and on (> time 0) (> power 0))")
 	public void stop() {
 		on = false;
 		time = 0;
 		power = 0;
 	}
 
 	@Override
 	@Pre(value = "(and on (> time 0) (> power 0))")
 	public void pause() {
 		on = false;
 	}
 
 	@Override
 	@Pre(value = "(not doorOpened)")
 	public void openDoor() {
 		if (doorOpened) {
 			System.out.println("openDoor()");
 			throw new IllegalStateException();
 		}
 		doorOpened = true;
 		on = false;
 	}
 
 	@Override
 	@Pre(value = "(doorOpened)")
 	public void closeDoor() {
 		if (!doorOpened) {
 			System.out.println("closeDoor()");
 			throw new IllegalStateException();
 		}
 		doorOpened = false;
 	}
 
 //	@Override
 //	public boolean isOn() {
 //		return on;
 //	}
 //
 //	@Override
 //	public long getTime() {
 //		return time;
 //	}
 //
 //	@Override
 //	public long getPower() {
 //		return power;
 //	}
 //
 //	@Override
 //	public boolean isDoorOpened() {
 //		return doorOpened;
 //	}
 }
