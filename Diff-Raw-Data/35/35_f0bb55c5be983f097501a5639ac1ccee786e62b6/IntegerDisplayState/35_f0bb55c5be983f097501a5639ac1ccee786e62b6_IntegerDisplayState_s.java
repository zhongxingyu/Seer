 package calculator.model.display.states;
 
 public class IntegerDisplayState extends DisplayState {
 
	private static DisplayState instance;
 
	public static DisplayState getInstance() {
 		if(instance == null) {
 			instance = new IntegerDisplayState();
 		}
 		return instance;
 	}
 
 	private IntegerDisplayState() {
 	}
 
 	@Override
 	public void addContent(DisplayStateSupport display, String suffix) {
 		if(isDezimalPoint(suffix)) {
 			display.setState(FloatingPointDisplayState.getInstance());
 		}
 		display.setContent(display.getContent() +  suffix);
 	}
 
 }
