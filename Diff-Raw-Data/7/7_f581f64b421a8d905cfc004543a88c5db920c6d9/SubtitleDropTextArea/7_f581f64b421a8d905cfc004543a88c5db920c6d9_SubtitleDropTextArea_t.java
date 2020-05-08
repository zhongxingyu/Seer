 package org.libreSubsApplet;
 
 import java.awt.Graphics;
 import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JTextArea;
 
 @SuppressWarnings("serial")
 public class SubtitleDropTextArea extends JTextArea {
 	
 
 	private final Image image;
 
 	public SubtitleDropTextArea() {
 		setOpaque(false);
 		setBorder(null);
 		setEditable(false);
 		final URL resource = SubtitleDropTextArea.class.getResource("/applet.png");
 		final ImageIcon background = new ImageIcon(resource);
 		image = background.getImage();
 	}
 	
 	@Override
 	public void paintComponent (final Graphics g)
     {
		final Shape clip = g.getClip();
		final Rectangle visibleArea = clip.getBounds();
        g.drawImage(image, visibleArea.x, visibleArea.y, image.getWidth(null), image.getHeight(null), this);
         super.paintComponent(g);
     }
 
 }
