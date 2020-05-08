 package cc.warlock.core.stormfront.internal;
 
 import cc.warlock.core.stormfront.IStormFrontProtocolHandler;
 
 public class ATagHandler extends DefaultTagHandler {
 
 	public ATagHandler(IStormFrontProtocolHandler handler) {
 		super(handler);
 	}
 	
 	@Override
 	public String[] getTagNames() {
 		return new String[] {"a"};
 	}
 
 	@Override
 	public void handleEnd(String newLine) {
		if(newLine != null && newLine.length() > 0) {
			handler.characters(newLine);
		}
 	}
 }
