 package obstacle;
 
 public abstract class ForceFieldState {
 	
 	public final int DURATION = 2;
 	
 	/**
 	 * Returns whether the force field can be passed.
 	 */
 	public abstract boolean canPass();

	public ForceFieldState getOppositeState() {
		if(this instanceof ActiveForceField)
			return new InactiveForceField();
		else
			return new ActiveForceField();
	}
 
 }
