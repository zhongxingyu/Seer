 import java.awt.Color;
 
 import acm.graphics.GCompound;
 import acm.graphics.GLabel;
 import acm.graphics.GRect;
 
 
 public class FancyLabel extends GCompound {
 	
 	private double PADDING = 4;
 
 	GLabel gLabel_;
 	GRect gRect_;
 	
 	public FancyLabel(String text) {
 		gLabel_ = new GLabel(text);
 		
 		gLabel_.setLocation(-gLabel_.getWidth() / 2.0, 0);
 		
 		gRect_ = new GRect(-gLabel_.getWidth() / 2.0 - PADDING / 2.0,
 				           -gLabel_.getHeight() + gLabel_.getDescent() - PADDING / 2.0,
 				            gLabel_.getWidth() + PADDING,
 				            gLabel_.getHeight() + PADDING);
 		
 		gRect_.setFilled(true);
 		gRect_.setFillColor(Color.YELLOW);
 		this.add(gRect_);
 		this.add(gLabel_);
 		
 	}
 }
 
