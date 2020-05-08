 package draw5;
 
 import java.awt.Graphics;
 
 public class PolygonShape extends MultiPointsShape{
 	public void draw(Graphics g){
 		if(color != null){
 			g.setColor(color);
 		}
 		g.drawPolygon(x, y, x.length);
 	}
 	
	public void drawOutline(Graphics g, int x1, int y1, int x2, int y2){
		g.drawLine(x1, y1, x2, y2);
 	}
 }
