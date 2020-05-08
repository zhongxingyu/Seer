 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 public class KitGraphicsDisplay extends DeviceGraphicsDisplay {
 	
 	Location kitLocation;
 	
 	//RotationPart
 	public int finalDegree;
 	int currentDegree;
 	int degreeStep;
 	int rotationAxisX;
 	int rotationAxisY;
 	int position;
 	AffineTransform trans=new AffineTransform();
 	
 	public KitGraphicsDisplay (Client c, Location newLocation) {
 		kitLocation = newLocation;
 		
 		position=0;
 		finalDegree=270;
 		currentDegree=0;
 		degreeStep=1;
		rotationAxisX=220;
 		rotationAxisY=40;
 		trans.translate(0, 200);
 	}
 
 	public int getPosition() {
 		return position;
 	}
 
 	public void setPosition(int position) {
 		this.position = position;
 	}
 
 	public void setLocation (Location newLocation) {
 		kitLocation = newLocation;
 	}
 	
 	public Location getLocation () {
 		return kitLocation;
 	}
 
 	public void draw(JComponent c, Graphics2D g) {
 		g.drawImage(Constants.KIT_IMAGE, kitLocation.getX(), kitLocation.getY(), c);
 		
 	}
 	
 
 
 	public void receiveData(Request req) {
 		
 	}
 
 	//Drawing using AffineTransform part
 	public void resetcurrentDegree(){
 		currentDegree=0;
 	}
 	
 	public void setFinalDegree(int finalDegree) {
 		this.finalDegree = finalDegree;
 	}
 	
 	public void drawRotate(JComponent c, Graphics2D g){
 		rotate();
 		g.drawImage(Constants.KIT_IMAGE,trans, null );
 		
 	}
 	public void rotate(){
 		if(currentDegree!=finalDegree)
 		{
 			trans.rotate(Math.toRadians(1),rotationAxisX,rotationAxisY);
 			currentDegree++;
 		}
 		else
 		{
 			currentDegree=0;
 			finalDegree=0;
 		}
 	}
 	
 }
