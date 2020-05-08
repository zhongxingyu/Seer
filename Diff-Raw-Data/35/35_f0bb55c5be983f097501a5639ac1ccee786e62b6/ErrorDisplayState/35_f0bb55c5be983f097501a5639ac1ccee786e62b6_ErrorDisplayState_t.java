 package calculator.model.display.states;
 
 public class ErrorDisplayState extends DisplayState {
 
	private static ErrorDisplayState instance;
 
	public static ErrorDisplayState getInstance() {
 		if(instance == null) {
 			instance = new ErrorDisplayState();
 		}
 		return instance;
 	}
 	
 	private ErrorDisplayState() {
 	}
 
 	@Override
 	public void addContent(DisplayStateSupport display, String suffix) {
 		if(display.isInitialValue(suffix)) {
 			display.setState(ClearDisplayState.getInstance());
 			display.setContent(DisplayStateSupport.INITIAL_VALUE);
 		}
 		if(!isDezimalPoint(suffix)) {
 			display.setState(IntegerDisplayState.getInstance());
 			display.setContent(suffix);
 		} else {
 			display.setState(FloatingPointDisplayState.getInstance());
 			display.setContent(DisplayStateSupport.INITIAL_VALUE + suffix);
 		}
 	}
 	
 	@Override
 	public boolean isErrorState() {
 		return true;
 	}
 
 }
