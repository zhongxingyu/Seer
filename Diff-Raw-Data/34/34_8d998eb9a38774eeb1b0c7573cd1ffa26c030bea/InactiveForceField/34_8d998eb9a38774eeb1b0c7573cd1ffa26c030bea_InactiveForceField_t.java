 package obstacle;
 
 public class InactiveForceField extends ForceFieldState {
 
 	@Override
 	public boolean canPass() {
 		return true;
 	}
 
	@Override
	public void switchState(ForceField forceField) {
		forceField.setState(new ActiveForceField());
	}
 }
