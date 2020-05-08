 package main.components;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 
 import javax.swing.JDesktopPane;
 
 import utils.Logger;
 
 
 public class BGDesktopPane extends JDesktopPane {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 837183880331079234L;
 	private Image backImage = null; // member variable
 
 	public BGDesktopPane() {
 
 		try {
			backImage = new javax.swing.ImageIcon(this.getClass().getResource("/bg.jpg")).getImage();
 		} catch (Exception e) {
 			Logger.log("Could not find file in folder: "
					+ this.getClass().getResource("/bg.jpg"));
 		}
 
 		setVisible(true);
 		setEnabled(true);
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		if(backImage == null) {
 			super.paintComponent(g);
 		} else {
 			Graphics2D g2d = (Graphics2D) g;
 
 			double mw = backImage.getWidth(null);
 			double mh = backImage.getHeight(null);
 			double sw = getWidth() / mw;
 			double sh = getHeight() / mh;
 			int h,w,x,y;
 			if(sw < sh) {
 			 	w = (int) (mw * sw);
 			 	h = (int) (mh * sw);
 			} else {
 				w = (int) (mw * sh);
 				h = (int) (mh * sh);
 			}
 			x = (getWidth() - w) / 2;
 			y = (getHeight() - h) / 2;
 			g2d.drawImage(backImage, x, y, w, h, this);
 
 		}
 	}
 
 }
