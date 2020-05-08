 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 import edu.cmu.cs.diamond.opendiamond.Util;
 
 public class OneView extends JPanel {
     final private BufferedImage img;
 
     protected BufferedImage scaledImg;
 
     private int drawPosY;
 
     private int drawPosX;
 
     private double scale;
 
     final private int unscaledHeight;
 
     private int oldW;
 
     private int oldH;
 
     final private String viewName;
 
     public OneView(BufferedImage img, String viewName, int unscaledHeight) {
         super();
 
         setBackground(null);
 
         this.img = img;
 
         this.viewName = viewName;
 
         this.unscaledHeight = unscaledHeight;
 
         int w = img.getWidth();
         int h = unscaledHeight;
         setPreferredSize(new Dimension(w, h));
         setMinimumSize(new Dimension(w / 2, h / 2));
         setMaximumSize(new Dimension(w * 2, h * 2));
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
        if (oldW != getWidth() || oldH != getHeight()) {
             // System.out.println("drawing scaled image");
             drawScaledImg();
             // System.out.println("done");
         }
 
         g.drawImage(scaledImg, drawPosX, drawPosY, null);
         // g.setColor(Color.WHITE);
         // g.drawString(Double.toString(scale),
         // g.getFontMetrics().getMaxAscent() + 10, 20);
     }
 
     private void drawScaledImg() {
         Insets in = getInsets();
         final int cW = getWidth() - in.left - in.right;
         final int cH = getHeight() - in.top - in.bottom;
 
         final int w = img.getWidth();
         final int h = unscaledHeight;
 
         scale = Util.getScaleForResize(w, h, cW, cH);
 
         final int sW = (int) (w * scale);
         final int sH = (int) (img.getHeight() * scale);
 
         scaledImg = getGraphicsConfiguration().createCompatibleImage(sW, sH);
 
         Util.scaleImage(img, scaledImg, false);
 
         // center in X
         drawPosX = (cW - sW) / 2 + in.left;
         drawPosY = in.top;
     }
 
     public BufferedImage getImage() {
         return img;
     }
 
     public Point getImagePoint(Point p) {
         return new Point((int) ((p.x - drawPosX) / scale),
                 (int) ((p.y - drawPosY) / scale));
     }
 
     public String getViewName() {
         return viewName;
     }
 
     public double getScale() {
         return scale;
     }
 }
