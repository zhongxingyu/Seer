 /**
  *
  */
 package org.jmist.packages;
 
 import java.io.Serializable;
 
 import org.jmist.framework.AbstractGeometry;
 import org.jmist.framework.Geometry;
 import org.jmist.framework.Intersection;
 import org.jmist.framework.IntersectionRecorder;
 import org.jmist.framework.WeightedSurfacePoint;
 import org.jmist.toolkit.Basis3;
 import org.jmist.toolkit.Box3;
 import org.jmist.toolkit.Interval;
 import org.jmist.toolkit.Point2;
 import org.jmist.toolkit.Point3;
 import org.jmist.toolkit.Polynomial;
 import org.jmist.toolkit.Ray3;
 import org.jmist.toolkit.Sphere;
 import org.jmist.toolkit.Vector3;
 import org.jmist.util.MathUtil;
 
 /**
  * A cylinder aligned along the y-axis.
  * @author bkimmel
  */
 public final class CylinderGeometry extends AbstractGeometry implements
 		Geometry, Serializable {
 
 	/**
 	 * Initializes the dimensions of this cylinder.
 	 * @param base		the center of the base of the cylinder
 	 * @param radius	the radius of the cylinder
 	 * @param height	the height of the cylinder
 	 */
 	public CylinderGeometry(Point3 base, double radius, double height) {
 		this.base = base;
 		this.radius = radius;
 		this.height = height;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.Geometry#generateRandomSurfacePoint()
 	 */
 	public WeightedSurfacePoint generateRandomSurfacePoint() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.Geometry#intersect(org.jmist.toolkit.Ray3, org.jmist.toolkit.Interval, org.jmist.framework.IntersectionRecorder)
 	 */
 	public void intersect(Ray3 ray, IntersectionRecorder recorder) {
 
 		Interval	I		= recorder.interval();
 		Point3		p;
 		double		t;
 
 		// first check for intersection of ray with the caps on the ends of the cylinder
 
 		// check bottom cap
 		t = (this.base.y() - ray.origin().y()) / ray.direction().y();
 		if (I.contains(t))
 		{
 			p = ray.pointAt(t);
 
 			if (this.base.squaredDistanceTo(p) < this.radius * this.radius)
 			{
 				Intersection x = super.newIntersection(ray, t, (ray.direction().y() > 0.0), CYLINDER_SURFACE_BASE)
 					.setLocation(p);
 
 				recorder.record(x);
 			}
 		}
 
 		// check top cap
 		t = (this.base.y() + this.height - ray.origin().y()) / ray.direction().y();
 		if (I.contains(t))
 		{
 			p = ray.pointAt(t);
 
 			double r = (p.x() - this.base.x()) * (p.x() - this.base.x()) + (p.z() - this.base.z()) * (p.z() - this.base.z());
 
 			if (r < this.radius * this.radius)
 			{
 				Intersection x = super.newIntersection(ray, t, (ray.direction().y() < 0.0), CYLINDER_SURFACE_TOP)
 					.setLocation(p);
 
 				recorder.record(x);
 			}
 		}
 
 		// now check for intersection of ray with the body
 		Vector3		orig	= this.base.vectorTo(ray.origin());
 		Vector3		dir		= ray.direction();
 
 		Polynomial	f		= new Polynomial(
 								orig.x() * orig.x() + orig.z() * orig.z() - this.radius * this.radius,
 								2.0 * (orig.x() * dir.x() + orig.z() * dir.z()),
 								dir.x() * dir.x() + dir.z() * dir.z()
 							);
 		double[]	x		= f.roots();
 
 		if (x.length == 2)
 		{
 			// for each solution, make sure the point lies between the base and the apex
 			p = ray.pointAt(x[0]);
 			if (MathUtil.inRangeOO(p.y(), this.base.y(), this.base.y() + this.height))
 			{
 				Intersection isect = super.newIntersection(ray, x[0], (x[0] < x[1]), CYLINDER_SURFACE_BODY)
 					.setLocation(p);
 
 				recorder.record(isect);
 			}
 
 			p = ray.pointAt(x[1]);
 			if (MathUtil.inRangeOO(p.y(), this.base.y(), this.base.y() + this.height))
 			{
 				Intersection isect = super.newIntersection(ray, x[1], (x[0] > x[1]), CYLINDER_SURFACE_BODY)
 					.setLocation(p);
 
 				recorder.record(isect);
 			}
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.Geometry#isClosed()
 	 */
 	public boolean isClosed() {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.Bounded3#boundingBox()
 	 */
 	public Box3 boundingBox() {
 
 		return new Box3(
 			this.base.x() - this.radius,
 			this.base.y(),
 			this.base.z() - this.radius,
 			this.base.x() + this.radius,
 			this.base.y() + this.height,
 			this.base.z() + this.radius
 		);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.Bounded3#boundingSphere()
 	 */
 	public Sphere boundingSphere() {
 
 		double	h = this.height / 2.0;
 		double	r = Math.sqrt(this.radius * this.radius + h * h);
 		Point3	c = new Point3(this.base.x(), this.base.y() + h, this.base.z());
 
 		return new Sphere(c, r);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.AbstractGeometry#getBasis(org.jmist.framework.AbstractGeometry.GeometryIntersection)
 	 */
 	@Override
 	protected Basis3 getBasis(GeometryIntersection x) {
 
 		Vector3 n = this.getNormal(x);
 		Vector3 r = this.base.vectorTo(x.location());
 		Vector3 u = new Vector3(-r.z(), 0.0, r.x());
 
 		return Basis3.fromWU(n, u, Basis3.Orientation.RIGHT_HANDED);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.AbstractGeometry#getNormal(org.jmist.framework.AbstractGeometry.GeometryIntersection)
 	 */
 	@Override
 	protected Vector3 getNormal(GeometryIntersection x) {
 
 		switch (x.surfaceId()) {
 
 		case CYLINDER_SURFACE_BASE:
 			return Vector3.J.opposite();
 
 		case CYLINDER_SURFACE_TOP:
 			return Vector3.J;
 
 		case CYLINDER_SURFACE_BODY:
 			Point3 p = x.location();
			return new Vector3(p.x() - this.base.x(), 0.0, p.y()
					- this.base.y()).unit();
 
 		default:
 			throw new IllegalArgumentException("Invalid surface ID.");
 
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.AbstractGeometry#getTextureCoordinates(org.jmist.framework.AbstractGeometry.GeometryIntersection)
 	 */
 	@Override
 	protected Point2 getTextureCoordinates(GeometryIntersection x) {
 
 		Vector3		r		= this.base.vectorTo(x.location());
 		double		tx		= (Math.PI + Math.atan2(r.z(), r.x())) / (2.0 * Math.PI);
 		double		ty;
 
 		switch (x.surfaceId()) {
 
 		case CYLINDER_SURFACE_BASE:
 			ty = Math.sqrt(r.x() * r.x() + r.z() * r.z()) / (4.0 * this.radius);
 			break;
 
 		case CYLINDER_SURFACE_TOP:
 			ty = 1.0 - Math.sqrt(r.x() * r.x() + r.z() * r.z())
 					/ (4.0 * this.radius);
 			break;
 
 		case CYLINDER_SURFACE_BODY:
 			ty = 0.25 + (r.y() / (2.0 * this.height));
 			break;
 
 		default:
 			throw new IllegalArgumentException("Invalid surface ID.");
 
 		}
 
 		return new Point2(tx, ty);
 
 	}
 
 	/** The point at the base of the cylinder */
 	private final Point3 base;
 
 	/** The radius of the cylinder */
 	private final double radius;
 
 	/** The height of the cylinder */
 	private final double height;
 
 	/** The surface ID for the base of the cylinder. */
 	private static final int CYLINDER_SURFACE_BASE = 0;
 
 	/** The surface ID for the top of the cylinder. */
 	private static final int CYLINDER_SURFACE_TOP = 1;
 
 	/** The surface ID for the body of the cylinder. */
 	private static final int CYLINDER_SURFACE_BODY = 2;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 1128440316229322913L;
 
 }
