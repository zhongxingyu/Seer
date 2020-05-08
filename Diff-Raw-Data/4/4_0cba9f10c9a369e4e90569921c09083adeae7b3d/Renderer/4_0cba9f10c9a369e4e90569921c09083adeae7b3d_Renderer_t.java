 package interaction;
 
 import game.Game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class Renderer {
   
 	
 
   public JPanel panel;
   private Game game;
   
   public Renderer(Game g, JFrame frame) {
 	  this.game = g;
 	  this.panel = (JPanel) frame.getContentPane();
   }
 
   
   public void render() {
 	  draw(panel.getGraphics());
   }
   
   private void draw(Graphics g) {
     double width = (double)panel.getWidth();
     double height = (double)panel.getHeight();
    double sizeX = (double)(panel.getWidth())/(double)game.f.tilesX;
    double sizeY = (double)(panel.getHeight())/(double)game.f.tilesY;
     
     BufferedImage back = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_BYTE_INDEXED);
     Graphics2D g2d = back.createGraphics();   
     
     g2d.setColor(new Color(0,0,0));
     g2d.fillRect(0, 0, (int)width, (int)height);
     
     for(int x=0;x<game.f.tilesX;x++) {
     	for(int y=0; y<game.f.tilesY;y++) {
     		switch(game.f.tiles[x][y]) {
     		case 1:
     			/* approach to add smoother edges
     			ArrayList<Point2D >nbs =
     				f.neighbours(f.getWorldPos(new Point(x,y)));
  
     			int ns_one = 0;
     			for(Point2D n : nbs) {
     				if(f.tileAt(n) != 1) ns_one++;
     			}
     			
     			if(ns_one == 2) {
     				if(gt(x-1,y) != 1 && gt(x,y-1) != 1) {
     					int[] xPoints = {(int) (x*size),(int) (x*size+size)+1,(int) (x*size+size)+1};
     					int[] yPoints = {(int) (y*size+size)+1,(int) (y*size+size),(int) (y*size)};
     					g.fillPolygon(xPoints, yPoints, 3);
     					
     				}
     			}
     			
     			else {
     			*/
     			g2d.setColor(new Color(255,0,0));
     			g2d.fill(new Rectangle2D.Double(
         				(double)x*sizeX,(double)y*sizeY,sizeX,sizeY));
     			
     			
     			break;
     		case 2:
     			g2d.setColor(new Color(0,0,255));
     			g2d.fill(new Rectangle2D.Double(
     					(double)x*sizeX,(double)y*sizeY,sizeX,sizeY));
     			break;
     		
     		}
 
     	}
     }
     
 
     ArrayList<Point2D> path = game.u.path;
     g2d.setColor(new Color(0,255,0));
     if(path != null) {
     	if(path.size() > 0) {
     		g2d.drawLine((int)(game.u.pos.getX()*width), 
 					 (int)(game.u.pos.getY()*height),
 					 (int)(path.get(0).getX()*width), 
 					 (int)(path.get(0).getY()*height));
     	}
     	for(int i=0;i<path.size()-1;i++) {
     		g2d.drawLine((int)(path.get(i).getX()*width), 
     					 (int)(path.get(i).getY()*height),
     					 (int)(path.get(i+1).getX()*width), 
     					 (int)(path.get(i+1).getY()*height));
     	}
     }
     
     g2d.fill(new Ellipse2D.Double((game.u.pos.getX()*width)-(sizeX/4),
     							  (game.u.pos.getY()*height)-(sizeY/4),
     							  sizeX/2,sizeY/2));
     
     
     
     g2d.draw(new Ellipse2D.Double(
     		  game.c.getX()*width-(sizeX/2),
 			  game.c.getY()*height-(sizeY/2),
 			  sizeX,sizeY));
     
     g2d.drawLine((int)(game.c.getX()*width), 
     			 (int)(game.c.getY()*height-(sizeY/2)-5), 
     			 (int)(game.c.getX()*width), 
     			 (int)(game.c.getY()*height+(sizeY/2)+5));
     
     g2d.drawLine((int)(game.c.getX()*width-(sizeY/2)-5), 
 			 	 (int)(game.c.getY()*height), 
 			 	 (int)(game.c.getX()*width+(sizeY/2)+5), 
 			 	 (int)(game.c.getY()*height));
     
     Graphics2D g2dpanel = (Graphics2D)g;
     g2dpanel.drawImage(back,null, 0,0);
   }
 
   public int gt(int x, int y) {
 	  try{
 		  return game.f.tiles[x][y];
 	  }
 	  catch(Exception e)
 	  {
 		  return 1;
 	  }
   }
  
 
 
 }
