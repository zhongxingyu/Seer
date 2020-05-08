 package nl.PAINt.shapes;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.geom.Rectangle2D;
 
 public class Triangle implements Shape {
 	Point p1, p2, p3;
 	private boolean selected;
 	private Point lockedCorner;
 	private Color color;
 	private Color lineColor;
 	private boolean isFilled;
 
 	public Triangle(Point p1, Point p2, Point p3, boolean filled) {
 		this.p1 = p1;
 		this.p2 = p2;
 		this.p3 = p3;
 		this.selected = false;
 		this.isFilled = filled;
 	}
 
 	@Override
 	public void draw(Graphics2D g2d) {
 		int[] x = { p1.x, p2.x, p3.x };
 		int[] y = { p1.y, p2.y, p3.y };
 
 		Polygon p = new Polygon(x, y, 3);
 
 		if (isFilled) {
 			g2d.setPaint(color);
 			g2d.fill(p);
 		}
 		g2d.setPaint(lineColor);
 		g2d.setStroke(new BasicStroke(3.0f));
 		g2d.drawPolygon(p);
 
 		if (this.selected) {
 			this.drawSelectionBox(g2d);
 		}
 	}
 
 	private void drawSelectionBox(Graphics2D g2d) {
 		int[] x = { p1.x, p2.x, p3.x };
 		int[] y = { p1.y, p2.y, p3.y };
 
 		Polygon p = new Polygon(x, y, 3);
 		g2d.setPaint(Color.BLACK);
 		final float dash[] = { 7.0f };
 		g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
 				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
 		g2d.drawPolygon(p);
 
		g2d.setPaint(Color.blue);
 		g2d.setStroke(new BasicStroke(0.0f));
 
 		Rectangle2D r2d = new Rectangle2D.Double(p1.x - 5, p1.y - 5, 10, 10);
 		g2d.fill(r2d);
 
 		r2d = new Rectangle2D.Double(p2.x - 5, p2.y - 5, 10, 10);
 		g2d.fill(r2d);
 
 		r2d = new Rectangle2D.Double(p3.x - 5, p3.y - 5, 10, 10);
 		g2d.fill(r2d);
 	}
 
 	public void setLastPoint(Point p) {
 		this.p3 = p;
 	}
 
 	public void setSecondPoint(Point p) {
 		this.p2 = p;
 	}
 
 	@Override
 	public boolean checkHit(Point point) {
 		int[] x = { p1.x, p2.x, p3.x };
 		int[] y = { p1.y, p2.y, p3.y };
 		Polygon p = new Polygon(x, y, 3);
 
 		return p.contains(point);
 	}
 
 	@Override
 	public void setSelectionBox(boolean bool) {
 		this.selected = bool;
 	}
 
 	@Override
 	public void move(double dx, double dy) {
 		int dxx = (int) dx;
 		int dyy = (int) dy;
 		this.p1 = movePoint(p1, dxx, dyy);
 		this.p2 = movePoint(p2, dxx, dyy);
 		this.p3 = movePoint(p3, dxx, dyy);
 
 	}
 
 	private Point movePoint(Point p, int dx, int dy) {
 		return new Point(p.x + dx, p.y + dy);
 	}
 
 	public void moveCorner(Point point) {
 		this.lockedCorner.x = point.x;
 		this.lockedCorner.y = point.y;
 	}
 
 	@Override
 	public boolean lockCorner(Point p) {
 		if (p.x >= p1.x - 5 && p.x <= p1.x + 5 && p.y >= p1.y - 5
 				&& p.y <= p1.y + 5) {
 			lockedCorner = p1;
 			return true;
 		}
 		if (p.x >= p2.x - 5 && p.x <= p2.x + 5 && p.y >= p2.y - 5
 				&& p.y <= p2.y + 5) {
 			lockedCorner = p2;
 			return true;
 		}
 		if (p.x >= p3.x - 5 && p.x <= p3.x + 5 && p.y >= p3.y - 5
 				&& p.y <= p3.y + 5) {
 			lockedCorner = p3;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void unlockCorner() {
 		lockedCorner = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setColor(java.awt.Color)
 	 */
 	@Override
 	public void setColor(Color color) {
 		this.color = color;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see nl.PAINt.shapes.Shape#setLineColor(java.awt.Color)
 	 */
 	@Override
 	public void setLineColor(Color color) {
 		this.lineColor = color;
 	}
 
 }
