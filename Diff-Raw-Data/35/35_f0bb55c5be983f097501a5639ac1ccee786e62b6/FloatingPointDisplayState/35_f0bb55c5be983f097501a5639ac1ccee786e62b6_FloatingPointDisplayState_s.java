 package calculator.model.display.states;
 
 public class FloatingPointDisplayState extends DisplayState {
 
	private static DisplayState instance;
 
	public static DisplayState getInstance() {
 		if(instance == null) {
 			instance = new FloatingPointDisplayState();
 		}
 		return instance;
 	}
 
 	private FloatingPointDisplayState() {
 	}
 
 	@Override
 	public void addContent(DisplayStateSupport display, String suffix) {
 		if(isDezimalPoint(suffix)) {
 			return;
 		}
 		display.setContent(display.getContent() +  suffix);
 	}
 
 }
