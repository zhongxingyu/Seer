 package draw5;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 
 import scribble3.ScribbleCanvas;
 import draw1.TwoEndsShape;
 
 public class PolygonShapeTool extends scribble3.AbstractTool implements PolygonTool{
 	protected int x[], y[];
 	protected int index = 0;
 	protected ArrayList<Point> xy = new ArrayList<Point>();
 	protected PolygonShape curPolygon = new PolygonShape();
 	
 	public PolygonShapeTool(ScribbleCanvas canvas, String name, String tipText) {
 		super(canvas, name, tipText);
 	}
 	
 	public void addPointToArray(Point p){
 		Graphics g = canvas.getGraphics();
 		g.setXORMode(Color.darkGray);
 		g.setColor(Color.lightGray);
 		if(xy.isEmpty()){
 			curPolygon.drawOutline(g, p.x, p.y, p.x, p.y);
 		} else {
 			curPolygon.drawOutline(g, xy.get(index - 1).x, xy.get(index - 1).y, p.x, p.y);
 		}
 		xy.add(p);
 		index++;
 	}
 	
 	public void endArray(Point p){
 		//close the polygon
 		Graphics g = canvas.getGraphics();
 		curPolygon.drawOutline(g, xy.get(0).x, xy.get(0).y, xy.get(index - 1).x, xy.get(index - 1).y);
 		g.setXORMode(Color.darkGray);
 		g.setColor(Color.lightGray);
 		curPolygon.setColor(canvas.getCurColor());
		x = new int[index - 1];
		y = new int[index - 1]; 
		for(int i = 0; i < index - 1; i++){
			x[i] = xy.get(i).x;
			y[i] = xy.get(i).y;
		}
 		curPolygon.setX(x);
 		curPolygon.setY(y);
 		canvas.addShape(curPolygon);
 	    g.setPaintMode();
 	    canvas.repaint();
 	}
 	
 	public void startShape(Point p){}		
 	public void addPointToShape(Point p){}
 	public void endShape(Point p){}
 }
