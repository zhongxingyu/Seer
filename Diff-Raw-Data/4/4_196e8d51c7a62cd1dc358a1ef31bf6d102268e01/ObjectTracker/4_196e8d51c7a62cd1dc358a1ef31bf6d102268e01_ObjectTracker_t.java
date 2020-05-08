 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class ObjectTracker implements Sink<ImageMoments>, Source<CS440Image>
 {
 	private CS440Image frame;
 	private FrameReceiver receiver;
 	private ResultWindow results;
 	private List<Sink<CS440Image>> subscribers = new ArrayList<Sink<CS440Image>>();
 	
 	public ObjectTracker(ResultWindow window)
 	{
 		receiver = new FrameReceiver();
 		results = window;
 	}
 	
 	private class FrameReceiver implements Sink<CS440Image>
 	{
 		@Override
 		public void receive(CS440Image image)
 		{
 			frame = image;
 		}
 	}
 	
 	@Override
 	public void receive(ImageMoments moment) 
 	{
 		String text = "L1 = " + moment.L1 + ", L2 = " + moment.L2 + ", THETA = " + moment.theta + ", X = " + moment.x 
			+ ", Y = " + moment.y + ", M00 = " + moment.m00 + ", M10 = " + moment.m10 + ", M01 = " + moment.m01 
			+ ", M11 = " + moment.m11 + ", M20 = " + moment.m20 + ", M02 = " + moment.m02;
 		this.results.updateText(text);
 		this.DrawBoundingBox(moment);
 		for(Sink<CS440Image> subscriber : subscribers) {
 			subscriber.receive(frame);
 		}
 	}
 	
 	public FrameReceiver GetFrameReceiver()
 	{
 		return this.receiver;
 	}
 	
 	public void DrawBoundingBox(ImageMoments moment)
 	{
 		// Set bounding box
 		Color c = new Color(255, 255, 255);
 		BufferedImage img = frame.getRawImage();
 		Graphics2D g = img.createGraphics();
 		g.setColor(c);
 		
 		long x = Math.round(moment.x * (double)frame.width());
 		long y = Math.round(moment.y * (double)frame.height());
 		long l1 = Math.round(moment.L1);
 		long l2 = Math.round(moment.L2);
 		
 		g.draw3DRect((int)x, (int)y, (int)l1, (int)l2, true);
 	}
 	
 	public CS440Image GetTrackedFrame()
 	{
 		return frame;
 	}
 
 	@Override
 	public void subscribe(Sink<CS440Image> sink) {
 		subscribers.add(sink);
 	}
 }
