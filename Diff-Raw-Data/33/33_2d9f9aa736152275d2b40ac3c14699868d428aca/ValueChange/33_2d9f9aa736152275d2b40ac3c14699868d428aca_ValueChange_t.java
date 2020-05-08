 package edu.ucsc.eis.mario.events;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import edu.ucsc.eis.mario.sprites.Mario;
 
 public class ValueChange extends MarioEvent {
	public static final int COIN_CHANGE = 1;
 	private int changeType;
 	private int startValue;
 	private int endValue;
 	
 	public ValueChange(Mario mario, int changeType, int startValue, int endValue) {
 		super(mario);
 		this.changeType = changeType;
 		this.startValue = startValue;
 		this.endValue = endValue;
 	}
 
 	public int getChangeType() {
 		return changeType;
 	}
 
 	public int getStartValue() {
 		return startValue;
 	}
 
 	public int getEndValue() {
 		return endValue;
 	}
 	
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).
 			append("Start Value", this.startValue).
 			append("End Value", this.endValue).
 			append("Change type", this.changeType).
 			toString();
 	}
 
 }
