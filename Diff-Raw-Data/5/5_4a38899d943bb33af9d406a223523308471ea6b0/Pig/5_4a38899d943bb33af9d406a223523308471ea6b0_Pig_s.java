 import java.awt.Dimension;
 
 import javax.swing.ImageIcon;
 
 public class Pig extends Enemy {
 	private Dimension frameSize;
 
 	
 	public Pig() {
 	    imagePath = "res/images/pig.png";
     	ImageIcon ii = new ImageIcon(imagePath);
 	    image = ii.getImage();
 	    position = new Position(100, 500);
 	    speed = 1;
 		frameSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 
 	}
 	
 	public void run() {
 		move();
 	}
 	
 	public void move() {
 		boolean back = false;
 		
 		while(true) {
 			if (position.getX() > frameSize.getWidth() - image.getWidth(null)) back = true;
 			
 			if (!back)
				position.setX(++position.getX());
 			else
		    	position.setX(--position.getX());
 			//repaint();
 		}
 	}
 }
