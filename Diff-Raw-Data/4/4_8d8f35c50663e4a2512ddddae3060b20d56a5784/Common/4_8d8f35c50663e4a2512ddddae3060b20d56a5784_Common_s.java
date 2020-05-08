 package edu.agh.tunev.model;
 
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 public final class Common {
 
 	private final static int INTERSECTION_AREA_GRID_RESOLUTION = 1000;
 
 	/**
 	 * Normalizuje podany kąt do przedziału [0, 360).
 	 * 
 	 * @param angle
 	 * @return
 	 */
 	public static double normalizeDeg(double angle) {
 		return (360.0 + angle % 360.0) % 360.0;
 	}
 
 	/**
 	 * Oblicza kąt o danym położeniu względnym na odległości między podanymi
 	 * kątami. (Po węższej stronie).
 	 * 
 	 * @param angle1
 	 * @param angle2
 	 * @param ratio
 	 * @return
 	 */
 	public static double sectDeg(double angle1, double angle2, double ratio) {
 		if (ratio < 0 || ratio > 1)
 			throw new IllegalArgumentException("ratio must belong to [0;1]");
 
 		final double a1 = normalizeDeg(angle1);
 		final double a2 = normalizeDeg(angle2);
 
 		final double min = Math.min(a1, a2);
 		final double max = Math.max(a1, a2);
 
 		final double diff = max - min;
 		final double rdiff1 = normalizeDeg(diff);
 		final double rdiff2 = normalizeDeg(-diff);
 
 		if (rdiff1 < rdiff2)
 			return normalizeDeg(min + rdiff1 * ratio);
 		else
 			return normalizeDeg(min - rdiff2 * ratio);
 	}
 
 	/**
 	 * Creates an ellipse with given center point and rotation angle.
 	 * 
 	 * @param center
 	 *            coordinates of the shape's center.
 	 * @param a
 	 *            original (pre-rotation) OX dimension.
 	 * @param b
 	 *            original (pre-rotation) OY dimension.
 	 * @param deg
 	 *            counter-clockwise rotation around the {@code center}.
 	 * 
 	 * @return Shape representing the ellipse.
 	 */
 	public static Shape createEllipse(Point2D.Double center, double width,
 			double height, double deg) {
 		return AffineTransform.getRotateInstance(deg * Math.PI / 180.0,
 				center.x, center.y).createTransformedShape(
 				new Ellipse2D.Double(center.x - width / 2, center.y - height
 						/ 2, width, height));
 	}
 
 	/**
 	 * Zwraca wspólne pole dwóch Shape.
 	 */
 	public static double intersectionArea(Shape s1, Shape s2) {
 		// poniższe liczby definiują wymiary siatki na którą zostanie
 		// przeskalowana (rozciągnięta) część wspólna kształtów s1, s2
 		//
 		// oczywiście im większa siatka, tym dłużej zajmie policzenie tego
 		//
 		// złożoność tej funkcji to O(w*h)
 		final int w = INTERSECTION_AREA_GRID_RESOLUTION;
 		final int h = INTERSECTION_AREA_GRID_RESOLUTION;
 		// dla siatki 5000x5000 i dwóch identycznych elips 200x100:
 		//
 		// Shape e1 = ellipse(250, 250, 200, 100, 0);
 		// Shape e2 = ellipse(250, 250, 200, 100, 0);
 		//
 		// zwrócony wynik to:
 		// 15711.904
 		//
 		// a analityczny wynik:
 		// 200/2*100/2*Math.PI == 15707.9632679489661923
 		//
 		// procentowa różnica: -0.0251%
 		//
 		// oczywiście 5000x5000 to zabójcza wielkość, liczy się jakieś 2
 		// sekundy u mnie, nie wspominając o zajętej pamięci
 		//
 		// dla porównania: siatka 100x100 daje błąd 0.66%, więc wciąż
 		// spoko
 		//
 		// 10x10 -> 10.87%
 
 		final Area area = new Area(s1);
 		area.intersect(new Area(s2));
 		Rectangle2D bounds = area.getBounds2D();
 
 		final double dx = bounds.getWidth() / w;
 		final double dy = bounds.getHeight() / h;
 		final double tx = -bounds.getMinX();
 		final double ty = -bounds.getMinY();
 
 		final AffineTransform at = AffineTransform.getScaleInstance(1.0 / dx,
 				1.0 / dy);
 		at.concatenate(AffineTransform.getTranslateInstance(tx, ty));
 
 		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
 				w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
 
 		java.awt.Graphics2D g = image.createGraphics();
 		g.setPaint(java.awt.Color.WHITE);
 		g.fillRect(0, 0, w, h);
 		g.setPaint(java.awt.Color.BLACK);
 		g.fill(at.createTransformedShape(area));
 
 		int num = 0;
 
 		for (int i = 0; i < w; i++)
 			for (int j = 0; j < h; j++)
 				if ((image.getRGB(i, j) & 0x00ffffff) == 0)
 					num++;
 
 		return dx * dy * num;
 	}
 
 	/**
 	 * Finds the closest point on a line segment.
 	 * 
 	 * @param start
 	 *            segment start point
 	 * @param end
 	 *            segment end point
 	 * @param point
 	 *            point to found the closest point on segment
 	 * @return the closest point on a segment
 	 */
 	public static Point2D.Double getClosestPointOnSegment(Point2D.Double start,
 			Point2D.Double end, Point2D.Double point) {
 		double xDelta = end.x - start.x;
 		double yDelta = end.y - start.y;
 
 		if ((xDelta == 0) && (yDelta == 0)) {
 			throw new IllegalArgumentException(
 					"Segment start equals segment end");
 		}
 
 		double u = ((point.x - start.x) * xDelta + (point.y - start.y) * yDelta)
 				/ (xDelta * xDelta + yDelta * yDelta);
 
 		final Point2D.Double closestPoint;
 		if (u < 0) {
 			closestPoint = new Point2D.Double(start.x, start.y);
 		} else if (u > 1) {
 			closestPoint = new Point2D.Double(end.x, end.y);
 		} else {
 			closestPoint = new Point2D.Double(start.x + u * xDelta, start.y + u
 					* yDelta);
 		}
 
 		return closestPoint;
 	}
 
 	/**
 	 * postać normalna prostej na OXY (czyli tak jak widzą prostą ludzie) zob.
 	 * https://pl.wikipedia.org/wiki/Prosta#R.C3.B3wnanie_normalne
 	 */
 	public static class LineNorm {
 		/** odległość prostej od (0,0) */
 		public final double r;
 		/** kąt między prostą a OY (czyli między r i OY) */
 		public final double phi;
 
 		public LineNorm(double r, double phi) {
 			this.r = r;
 			this.phi = phi;
 		}
 
 		// to można by napisać w Scali:
 		// `case class LineNorm(phi: Double, r: Double)'
 		// bez żadnych konstruktorów, niczego... :p
 		/** postać normalna prostej przechodzącej przez punkty p2, p2 */
 		public static LineNorm create(Point2D.Double p1, Point2D.Double p2) {
 			final boolean vertical = equal(p1.x, p2.x);
 			
 			// równanie ogólne Ax+By+C=0
 			final double A = (vertical ? 1 : (p2.y - p1.y) / (p2.x - p1.x));
 			final double B = (vertical ? 0 : -1);
			final double C = (vertical ? p1.x : (p2.x*p1.y - p1.x*p2.y) / (p2.x - p1.x));
 			
 			// równanie normalne x cosphi + y sinphi - r = 0
 			final double r = Math.abs(C) / Math.sqrt(A * A + B * B);
 			final double phi = (C < 0 ? Math.atan2(B, A) : Math.atan2(-B, -A));
 			
 			return new LineNorm(r, phi);
 		}
 	}
 
 	/** stwierdza czy dwa double są równe z względną dokładnością 0.1% */
 	public static boolean equal(double a, double b) {
 		if (Math.abs(a - b) / Math.abs(a) < 0.001)
 			return true;
 		return false;
 	}
 
 	private Common() {
 		// you shall not instantiate ^-^
 	}
 
 }
