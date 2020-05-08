 package mikera.gui;
 
 import java.awt.Graphics;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 /**
  * Simple GUI component - panel with a tiled background
  * @author Mike
  *
  */
 public class BackgroundPanel extends JPanel {
 
 	private BufferedImage image=null;
 
 	public BackgroundPanel() {
 		super();
 	}
 	
 	public void setImage(BufferedImage b) {
 		this.image=b;
 	}
 	
 	public BufferedImage getImage() {
 		return this.image;
 	}
 	
 	public BackgroundPanel(LayoutManager layout) {
 		super(layout);
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		if (image==null) return;
 		
 		Rectangle r=g.getClipBounds();
 		int w=image.getWidth();
 		int h=image.getHeight();
 		
		for (int x=(r.x/w)*w; x<(r.x+r.width); x+=w) {
			for (int y=(r.y/h)*h; y<(r.y+r.height); y+=h) {
 				g.drawImage(image, x, y, null);
 			}
 		}
 	}
 }
