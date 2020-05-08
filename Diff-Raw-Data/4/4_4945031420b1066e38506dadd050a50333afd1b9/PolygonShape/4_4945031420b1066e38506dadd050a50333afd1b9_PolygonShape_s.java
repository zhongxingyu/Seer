 package draw5;
 
 import java.awt.Graphics;
 
 public class PolygonShape extends MultiPointsShape{
 	public void draw(Graphics g){
 		if(color != null){
 			g.setColor(color);
 		}
 		g.drawPolygon(x, y, x.length);
 	}
 	
	public void drawOutline(Graphics g, int[] x, int y[]){
		g.drawPolygon(x, y, x.length);
 	}
 }
