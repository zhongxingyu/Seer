 /*
  * Class Ellipsoid
  * TCSS 491 Computational Worlds
  * Steve Bradshaw
  */
 
 package tesseract.objects;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Transform3D;
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.geometry.Sphere;
 
 /**
  * This class creates an ellipsoid using the formula 
  * (x/a)^2  +  (y/b)^2  +  (z/c)^2 = 1 using a matrix3f transformation
  * on a basic Sphere.  This class sets 'a' to a constant 1.0 and allows
  * 'b' and 'c'  to alter the ellipsoid's shape along with the radius field 
  * Sphere.  Since this is a sphere, the normals are already calculated.
  * 
  * @author Steve Bradshaw
  * @version 1 Feb 2011
  */
 public class Ellipsoid extends ForceableObject {
 	
 	/**
 	 * Default mass.
 	 */
 	private static final float DEFAULT_MASS = 1;
 	
 	/**
 	 * Number of divisions in the sphere.
 	 */
 	private static final int DEFAULT_DIVISIONS = 50;
 	
 	/**
 	 * Create a new Ellipsoid.
 	 * 
 	 * @param position Initial position.
 	 * @param mass Initial mass.
 	 * @param radius the radius of the base sphere.
 	 * @param primflags an int for the base spere primflags.
 	 * @param divisions an in for the shape divisions.
 	 * @param appearance an Appearance object.
 	 * @param b a float for the b portion of the ellipsoid formula.
 	 * @param c a float for the c portion of the ellipsoid formula.
 	 */
 	public Ellipsoid(final Vector3f position, final float mass,
 			final float radius,	final int primflags, final int divisions,
 			final Appearance appearance, final float b, final float c) {
 		super(position, mass);
 		
 		createShape(radius, primflags, appearance, divisions, b, c);
 	}
 	
 	/**
 	 * Create a new Ellipsoid.
 	 * 
 	 * @param position Initial position.
 	 * @param radius a float for the size of the base sphere.
 	 */
 	public Ellipsoid(final Vector3f position, final float radius) {
 		super(position, DEFAULT_MASS);
 		
 		createDefaultEllipsoid(radius);
 	}
 	
 	/**
 	 * This creates a default Ellipsoid for the 2 argument constructor.
 	 * @param radius the siz of the ellipsoid
 	 */
 	private void createDefaultEllipsoid(final float radius) {
 		
 		Sphere sphere = new Sphere(radius, new Sphere().getPrimitiveFlags(),
 				DEFAULT_DIVISIONS);
 		Transform3D tmp = new Transform3D();
 		tmp.set(new Matrix3f(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.5f));
 		getTransformGroup().setTransform(tmp);
 		getTransformGroup().addChild(sphere);
 	}
 	
 	/**
 	 * This method creates multiple ellipsoidial shapes.
 	 * 
 	 * @param radius a float for the size of the base sphere
 	 * @param primflags an int for the base sphere
 	 * @param appearance an Appearance object
 	 * @param divisions an int for the number of divisons
 	 * @param b a float for the y axis transform
 	 * @param c a float for the z axis transfrom
 	 */
 	private void createShape(final float radius, final int primflags,
 			final Appearance appearance, final int divisions, final float b,
 			final float c) {
 		
 		Sphere sphere = new Sphere(radius, primflags, divisions, appearance);
 		Transform3D tmp = new Transform3D();
 		tmp.set(new Matrix3f(1.0f, 0.0f, 0.0f, 0.0f, b, 0.0f, 0.0f, 0.0f, c));
		getTransformGroup().setTransform(tmp);
		getTransformGroup().addChild(sphere);
 	}
 }
