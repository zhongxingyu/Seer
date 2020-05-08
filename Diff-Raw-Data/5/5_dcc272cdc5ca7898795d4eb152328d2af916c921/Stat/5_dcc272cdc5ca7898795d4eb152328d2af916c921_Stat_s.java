 package com.punchline.javalib.entities.components.abstracted;
 
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.entities.processes.Process;
 import com.punchline.javalib.entities.processes.ProcessState;
 import com.punchline.javalib.entities.components.Component;
 
 /**
  * Base class for Components such as a health bar, magic meter, or any other stat with a max value.
  * @author Natman64
  *
  */
 public abstract class Stat implements Component {
 	
 	//region Regeneration Process
 	
 	private class RegenerationProcess extends Process {
 
 		private Stat stat;
 		private double rate;
 		
 		public RegenerationProcess(Stat stat, double rate) {
 			this.stat = stat;
 			this.rate = rate;
 		}
 		
 		public void setRate(double rate) {
 			this.rate = rate;
 		}
 		
 		@Override
 		public void update(EntityWorld world, float deltaTime) {
 			double current = stat.current + rate * deltaTime;
 			stat.setCurrentValue(current);
 		}
 		
 	}
 	
 	//endregion
 	
 	//region Fields/Initialization
 	
 	/** This stat's current value. */
 	protected double current;
 	
 	/** This stat's max value. */
 	protected double max;
 	
 	/** This stat's regeneration process. */
 	protected RegenerationProcess regenerationProcess;
 	
 	/**
 	 * Constructs a StatBar.
 	 * @param max The StatBar's maximum value.
 	 */
 	public Stat(double max) {
 		this.max = max;
 		this.current = max;
 	}
 	
 	//endregion
 	
 	//region Accessors
 	
 	/**
 	 * @return The StatBar's current value.
 	 */
 	public double getCurrentValue() {
 		return current;
 	}
 	
 	/**
 	 * @return The StatBar's max value.
 	 */
 	public double getMaxValue() {
 		return max;
 	}
 	
 	/**
 	 * @return Whether the StatBar is empty.
 	 */
 	public boolean isEmpty() {
 		return current <= 0;
 	}
 	
 	/**
 	 * @return How full the StatBar is, from 0 to 1.
 	 */
 	public float fraction() {
 		return (float) (current / max);
 	}
 	
 	//endregion
 	
 	//region Mutators
 	
 	/**
 	 * Sets the current value of the StatBar.
 	 * @param current The desired value.
 	 */
 	public void setCurrentValue(double current) {
 		
 		if (current < this.current) onDrain(this.current - current); //Call drain event
 		if (current > this.current) onFill(current - this.current); //Call fill event
 		
		this.current = current;
		
 		if (current > max) current = max;
 		if (current < 0) { current = 0; onEmpty(); } //Call empty event
 		
 	}
 	
 	/**
 	 * Sets this stat's regeneration rate.
 	 * @param world The EntityWorld. Needed for attaching a RegenerationProcess.
 	 * @param rate The rate of regeneration, in seconds.
 	 */
 	public void setRegenerationRate(EntityWorld world, double rate) {
 		if (rate == 0) {
 			if (regenerationProcess != null) regenerationProcess.end(ProcessState.ABORTED);
 		} else {
 			if (regenerationProcess == null) regenerationProcess = new RegenerationProcess(this, rate);
 			else regenerationProcess.setRate(rate);
 			world.getProcessManager().attach(regenerationProcess);
 		}
 	}
 	
 	/**
 	 * Sets the max value of the StatBar.
 	 * @param max The desired value. If negative, nothing will happen.
 	 */
 	public void setMaxValue(double max) {
 		if (max < 0) return;
 		
 		this.max = max;
 		
 		if (current > max) current = max;
 	}
 	
 	/**
 	 * Drains the StatBar by the given amount.
 	 * @param amount The amount to drain.
 	 */
 	public void drain(double amount) {
 		amount = Math.abs(amount);
 		setCurrentValue(current - amount);
 	}
 	
 	/**
 	 * Sets the StatBar's current value to 0.
 	 */
 	public void drainEmpty() {
 		drain(current);
 	}
 	
 	/**
 	 * Fills the StatBar by the given amount.
 	 * @param amount The amount to fill.
 	 */
 	public void fill(double amount) {
 		amount = Math.abs(amount);
 		setCurrentValue(current + amount);
 	}
 	
 	/**
 	 * Sets the StatBar's current value to its maximum value.
 	 */
 	public void fillMax() {
 		setCurrentValue(max);
 	}
 	
 	//endregion
 	
 	//region Events
 	
 	/**
 	 * Called when the StatBar is drained by any amount.
 	 * @param amount The amount drained.
 	 */
 	protected void onDrain(double amount) { }
 	
 	/**
 	 * Called when the StatBar is filled by any amount.
 	 * @param amount The amount filled.
 	 */
 	protected void onFill(double amount) { }
 	
 	/**
 	 * Called when the StatBar becomes empty.
 	 */
 	protected void onEmpty() { }
 	
 	//endregion
 	
 }
