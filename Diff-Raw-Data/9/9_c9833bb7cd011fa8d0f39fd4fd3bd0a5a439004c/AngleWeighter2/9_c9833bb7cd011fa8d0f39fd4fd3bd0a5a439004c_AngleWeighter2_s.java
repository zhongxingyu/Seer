 package huisken.projection;
 
 import javax.vecmath.Point3f;
 
 public class AngleWeighter2 implements FusionWeight {
 
 	public static final int X_AXIS = 0;
 	public static final int Y_AXIS = 1;
 	public static final int Z_AXIS = 2;
 
 	private final int aperture;
 	private final Point3f center;
 	private final int angle;
 	private final int axis;
 	private boolean negativeAxis;
 
 	public AngleWeighter2(int axis, boolean negativeAxis, int angle, int aperture, Point3f center) {
 		this.axis = axis;
 		this.angle = angle;
 		this.aperture = aperture;
 		this.center = center;
 		this.negativeAxis = negativeAxis;
 	}
 
 	public double getAngle(double dx, double dy, double dz) {
 		double d1, d2;
 		switch(axis) {
 		case Z_AXIS: d1 =  dx; d2 =  dy; break;
 		case Y_AXIS: d1 =  dx; d2 = -dz; break;
 		case X_AXIS: d1 = -dz; d2 =  dy; break;
 		default: throw new IllegalArgumentException();
 		}
		double a = 180.0 * Math.atan2(d2, d1);
 		if(negativeAxis)
 			a = -a;
 		a -= angle;
		return a % 180;
 	}
 
 	public boolean inLowerOverlap(double angle) {
 		return angle > -aperture/2.0 - overlap/2.0 && angle < -aperture/2.0 + overlap/2.0;
 	}
 
 	public boolean inUpperOverlap(double angle) {
 		return angle > aperture/2.0 - overlap/2.0 && angle < aperture/2.0 + overlap/2.0;
 	}
 
 
 	/*
 	 *          Camera
 	 *            |
 	 *            |
 	 *
 	 *            x
 	 *         _________
 	 *      y /        /|
 	 *       /        / |
 	 *      /________/  |
 	 *      |        |  /
 	 *    z |        | /
 	 *      |________|/
 	 */
 	public static final double overlap = 10;
 	@Override
 	public float getWeight(float px, float py, float pz) {
 
 		double dx = px - center.x;
 		double dy = py - center.y;
 		double dz = pz - center.z;
 
 		double angle = getAngle(dx, dy, dz);
 
 		// inside
 		if(angle > -aperture/2.0 + overlap/2.0 && angle < aperture/2.0 - overlap/2.0)
 			return 1f;
 
 		// outside
 		if(angle < -aperture/2.0 - overlap/2.0 || angle > aperture/2.0 + overlap/2.0)
 			return 0f;
 
 
 		// within the overlap
 		double exp = 10 / overlap;
 		if(angle < 0) {
 			double t = angle + aperture/2.0;
 			return (float)(1.0 / (1 + Math.exp(-exp * t)));
 		} else {
 			double t = angle - aperture/2.0;
 			return 1f - (float)(1.0 / (1 + Math.exp(-exp * t)));
 		}
 	}
 }
