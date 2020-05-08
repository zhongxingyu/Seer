 import java.awt.Component;
 import java.awt.Graphics;
 
 public abstract class Hitbox {
 	private double x;
 	private double y;
 	private double z;
 	private double depth;

	// TODO move a hitbox
 	public Hitbox(double x, double y, double z, double depth) {
 		setX(x);
 		setY(y);
 		setZ(z);
 		setDepth(depth);
 	}
 
 	public abstract int getShape();

 	public abstract void show(Graphics g, Component observer);
 
 	public abstract boolean intersects(Hitbox hb);
 
 	protected static boolean intersects(CircleHitbox ch, RectangleHitbox rh) {
 		if (!depthIntersects(ch, rh)) {
 			return false;
 		}
 		double cRad = ch.getRadius();
 		double cX = ch.getX();
 		double cY = ch.getY();
 		double rWidth = rh.getWidth();
 		double rHeight = rh.getHeight();
 		double rX = rh.getX();
 		double rY = rh.getY();
 
		if (cX > rX - 2 * cRad && cX < rX + rWidth) {
 			if (rY - cY <= cRad) {
 				return true;
			} else if (cY - (rY + rHeight) <= cRad) {
 				return true;
 			}
 		} else if (cY > rY && cY < rY + rHeight) {
 			if (rX - cX <= cRad) {
 				return true;
 			} else if (cX - rX + rWidth <= cRad) {
 				return true;
 			}
 		} else if (distance(cX, rX, cY, rY) < cRad) {
 			return true;
 		} else if (distance(cX, rX, cY, rY + rHeight) < cRad) {
 			return true;
 		} else if (distance(cX, rX + rWidth, cY, rY) < cRad) {
 			return true;
 		} else if (distance(cX, rX + rWidth, cY, rY + rHeight) < cRad) {
 			return true;
 		}
 
 		return false;
 	}
 
 	protected static boolean intersects(CircleHitbox ch1, CircleHitbox ch2) {
 		if (!depthIntersects(ch1, ch2)) {
 			return false;
 		}
 		double ch1X = ch1.getX();
 		double ch2X = ch2.getX();
 		double ch1Y = ch1.getY();
 		double ch2Y = ch2.getY();
 		double ch1Rad = ch1.getRadius();
 		double ch2Rad = ch2.getRadius();
 		return Hitbox.distance(ch1X, ch2X, ch1Y, ch2Y) <= ch1Rad + ch2Rad;
 	}
 
 	protected static boolean intersects(RectangleHitbox rh1, RectangleHitbox rh2) {
 		if (!depthIntersects(rh1, rh2)) {
 			return false;
 		}
 		double tw = rh1.getWidth();
 		double th = rh1.getHeight();
 		double rw = rh2.getWidth();
 		double rh = rh2.getHeight();
 		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
 			return false;
 		}
 		double tx = rh1.getX();
 		double ty = rh1.getY();
 		double rx = rh2.getX();
 		double ry = rh2.getY();
 		rw += rx;
 		rh += ry;
 		tw += tx;
 		th += ty;
 		// overflow || intersect
 		return ((rw < rx || rw > tx) && (rh < ry || rh > ty)
 				&& (tw < tx || tw > rx) && (th < ty || th > ry));
 	}
 
 	private static boolean depthIntersects(Hitbox h1, Hitbox h2) {
 		double tDepth = h1.getDepth();
 		double hDepth = h2.getDepth();
 		double tZ = h1.getZ();
 		double hZ = h2.getZ();
 		return Math.abs(tZ - hZ) <= tDepth + hDepth;
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	public double getZ() {
 		return z;
 	}
 
 	public void setZ(double z) {
 		this.z = z;
 	}
 
 	public double getDepth() {
 		return depth;
 	}
 
 	public void setDepth(double depth) {
 		this.depth = depth;
 	}
 
 	private static double distance(double x1, double x2, double y1, double y2) {
 		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
 	}
 }
