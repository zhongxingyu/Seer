 package obstacle;
 
 public class ActiveForceField extends ForceFieldState {
 	
 	@Override
 	public boolean canPass() {
 		return false;
 	}
 
	@Override
	public void switchState(ForceField forceField) {
		forceField.setState(new InactiveForceField());
	}
 }
