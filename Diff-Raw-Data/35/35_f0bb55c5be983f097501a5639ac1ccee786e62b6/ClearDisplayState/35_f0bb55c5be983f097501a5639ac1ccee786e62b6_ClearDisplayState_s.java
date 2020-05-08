 package calculator.model.display.states;
 
 public class ClearDisplayState extends DisplayState {
 
	private static DisplayState instance;
 
	public static DisplayState getInstance() {
 		if(instance == null) {
 			instance = new ClearDisplayState();
 		}
 		return instance;
 	}
 	
 	private ClearDisplayState() {
 	}
 
 	@Override
 	public void addContent(DisplayStateSupport display, String suffix) {
 		if(display.isInitialValue(suffix)) {
 			return;
 		}
 		if(!isDezimalPoint(suffix)) {
 			display.setState(IntegerDisplayState.getInstance());
 			display.setContent(suffix);
 		} else {
 			display.setState(FloatingPointDisplayState.getInstance());
 			display.setContent(DisplayStateSupport.INITIAL_VALUE + suffix);
 		}
 	}
 
 }
