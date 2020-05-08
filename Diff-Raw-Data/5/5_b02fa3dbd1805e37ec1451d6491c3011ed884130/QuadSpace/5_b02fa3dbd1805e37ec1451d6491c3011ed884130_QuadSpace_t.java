 package nehsics.collide;
 import nehsics.bodies.*;
 import nehsics.math.*;
 import java.util.*;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.*;
 
 public final class QuadSpace extends ArrayList<Body> {
 	public static final long serialVersionUID = 123918391;
 	private final double[] box;
 	private final static int MINX = 0, MINY = 1, MAXX = 2, MAXY = 3;
 
 	/**
 	 * Generate automatically bounded space for these bodies.
 	 */
 	public QuadSpace(List<Body> bodies) {
 		box = boundingBoxFor(bodies);
 		addAll(bodies);
 	}
 
 	public double dim() {
 		return Math.min(box[MAXX]-box[MINX], box[MAXY]-box[MINY]);
 	}
 
 	/**
 	 * Generate quadspace containing only bodies within the bounds.
 	 */
 	private QuadSpace(QuadSpace parent, double[] bounds) {
 		box = bounds;
 		Rectangle2D.Double rect = new Rectangle2D.Double(
 			box[MINX], box[MINY], box[MAXX]-box[MINX], box[MAXY]-box[MINY]);	
 		for (Body body : parent)
 			if (body.intersectsRectangle(rect))
 				add(body);
 	}
 
 	public void paint(Graphics2D g2d) {
 		Rectangle2D.Double rect = new Rectangle2D.Double(
 			box[MINX], box[MINY], box[MAXX]-box[MINX], box[MAXY]-box[MINY]);	
 		g2d.setColor(Color.BLUE);
 		g2d.draw(rect);
		int alpha = 5*size();
		if (alpha > 255)
			alpha = 255;
		g2d.setColor(new Color(0,0,255,alpha));
 		g2d.fill(rect);
 	}
 
 	/**
 	 * @return Array of 4 quadspaces.
 	 */
 	public QuadSpace[] divide() {
 		QuadSpace[] spaces = new QuadSpace[4];
 		double midX = (box[MINX] + box[MAXX]) / 2;
 		double midY = (box[MINY] + box[MAXY]) / 2;
 
 		double[] q1 = {midX, midY, box[MAXX], box[MAXY]};
 		spaces[0] = new QuadSpace(this, q1);
 
 		double[] q2 = {box[MINX], midY, midX, box[MAXY]};
 		spaces[1] = new QuadSpace(this, q2);
 
 		double[] q3 = {box[MINX], box[MINY], midX, midY};
 		spaces[2] = new QuadSpace(this, q3);
 
 		double[] q4 = {midX, box[MINY], box[MAXX], midY};
 		spaces[3] = new QuadSpace(this, q4);
 
 		return spaces;
 	}
 
 	/**
 	 * @return Bounding box defined by MIN/MAX array constants.
 	 */
 	private static double[] boundingBoxFor(List<Body> bodies) {
 		Vector2d tmp = bodies.get(0).getPosition();
 		double r = bodies.get(0).getRadius();
 		double[] box = {tmp.getX()-r, tmp.getY()-r, tmp.getX()+r, tmp.getY()+r};
 		for (Body body : bodies) {
 			tmp = body.getPosition();
 			r = body.getRadius();
 			if (box[MINX] > tmp.getX()-r)
 				box[MINX] = tmp.getX()-r;
 			else if (box[MAXX] < tmp.getX()+r)
 				box[MAXX] = tmp.getX()+r;
 			if (box[MINY] > tmp.getY()-r)
 				box[MINY] = tmp.getY()-r;
 			else if (box[MAXY] < tmp.getY()+r)
 				box[MAXY] = tmp.getY()+r;
 		}
 		return box;
 	}
 }
