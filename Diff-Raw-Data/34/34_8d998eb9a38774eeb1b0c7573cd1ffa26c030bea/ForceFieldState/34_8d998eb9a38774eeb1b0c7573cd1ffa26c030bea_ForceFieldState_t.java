 package obstacle;
 
 public abstract class ForceFieldState {
 	
 	public final int DURATION = 2;
 	
 	/**
 	 * Returns whether the force field can be passed.
 	 */
 	public abstract boolean canPass();
	
	public abstract void switchState(ForceField forceField);
 
 }
