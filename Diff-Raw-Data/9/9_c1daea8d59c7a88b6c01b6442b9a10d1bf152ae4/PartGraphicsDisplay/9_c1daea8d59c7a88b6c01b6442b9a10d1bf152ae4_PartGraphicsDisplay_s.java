 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import javax.swing.JComponent;
import javax.swing.JFrame;

 import factory.data.PartType;
 
 import Networking.Request;
 import Utils.Location;
 
 public class PartGraphicsDisplay extends DeviceGraphicsDisplay {
 	Location partLocation;
 	
 	//NEED IMAGE NAMES
	Image partImage = Toolkit.getDefaultToolkit().getImage("PUT IMAGE NAME HERE");
 
 	public PartGraphicsDisplay (PartType pt) {
		
 	}
 	
 	public void setLocation (Location newLocation) {
 		partLocation = newLocation;
 	}
 
 	public void draw(JComponent c, Graphics2D g) {
 		g.drawImage(partImage, partLocation.getX(), partLocation.getY(), c);
 	}
 
 	public void receiveData(Request req) {
 	}
 	
 }
