 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import javax.swing.ImageIcon;
 
 
 public class GUILane 
 {
 	public Lane lane;
 	public Movement movement;
 	
	private String laneImagePath = "images/lane/lane.png";
 	private static ImageIcon image;
 	
 	public GUILane(Lane lane, double x, double y)
 	{
 		this.lane = lane;
 		movement = new Movement(new Point2D.Double(x,y), 0);
 		
 		// If the image hasn't been loaded for the first time, do so
 		if (image == null)
 		{
 			try {
				image = new ImageIcon(laneImagePath);
 			} catch (Exception e) {
 				System.err.println("Loading the lane image failed!");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void draw(Graphics2D g, long currentTime)
 	{
 		Painter.draw(g, image, currentTime, movement);
 	}
 }
