 package ru.ifmo.neerc.gui;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import javax.swing.JPanel;
 
 public class ImagePanel extends JPanel {
 	private Image background;
 	
 	public ImagePanel() {
 		this (null);
 	}
 	
 	public ImagePanel(Image background) {
 		this.background = background;
 	}
 	
 	public void setBackground(Image background) {
		synchronized (this) {
			this.background = background;
		}
 	}
 
 	public void paintComponent(Graphics g) {
 		Image img = null;
		synchronized (this) {
 			img = background;
 		}
 		
 		if (img != null) {
 			int xSize = this.getWidth();
 			int ySize = this.getHeight();
 			setLayout(null);
 			g.drawImage(img, 0, 0, xSize, ySize, 0, 0, img.getWidth(this), img
 					.getHeight(this), this);
 		}
 	}
 }
