 
 package edu.wpi.first.wpilibj.winnovation.utils;
 
 import com.sun.squawk.util.MathUtils;
 
 
 /**
  * A pose (x,y,th) for objects in 2D. This is a layer over Java's affine transform class
  * which makes it possible to view a pose as a transform and vice versa. With a little
  * practice, it is possible to use this class and never write an explicit conversion between
  * a 2D pose and a 2D affinetransform. Thus a call to myPose.transform() is legal when myPose
  * is a RealPose2D. Likewise, a RealPose2D can be passed an an argument to any Java runtime library
  * routine that expects an affineTransform.
  * @author alonzo
  *
  */
 public class RealPose2D extends AffineTransform {
 
 	static final long serialVersionUID = 2L;
 
 	private RealPose2D poseInverse;
 
 	/**
 	 * Constructs and initializes a RealPose2D to (0,0,0).
 	 *
 	 */
 	public RealPose2D(){
 		super();
 	}
 
 	/**
 	 * Constructs and initializes a RealPose2D to a deep copy of the supplied pose.
 	 * @param pose the pose to copy
 	 */
 	public RealPose2D(RealPose2D pose){
 		super(pose);
 	}
 
 	/**
 	 * Constructs and initializes a RealPose2D to the supplied coordinates.
 	 * @param pt a point containing the (x,y) coordinates
 	 * @param th orientation coordinate
 	 */
 	public RealPose2D(Point2D pt, double th){
 		super();
 		setPose(pt.getX(),pt.getY(),th);
 	}
 
 	/**
 	 * Constructs and initializes a RealPose2D to the supplied coordinates.
 	 * @param x first coordinate
 	 * @param y second coordinate
 	 * @param th orientation coordinate
 	 */
 	public RealPose2D(double x, double y, double th){
 		super();
 		setPose(x,y,th);
 	}
 
 	/**
 	 * Sets the value of this pose.
 	 * @param x first coordinate
 	 * @param y second coordinate
 	 * @param th orientation coordinate
 	 */
 	public void setPose(double x, double y, double th)
 	{
 		setToTranslation(x,y);
 		rotate(th);
 	}
 
 	/**
 	 * Sets the value of the position part of this pose.
 	 * @param x first coordinate
 	 * @param y second coordinate
 	 */
 	public void setLocation(double x, double y)
 	{
 		setToTranslation(x,y);
 		rotate(getTh());
 	}
 
 	/**
 	 * Sets the value of this pose. Performs correctly if this == pose.
 	 * @param pose the value to which to set it.
 	 */
 	public void setPose(RealPose2D pose)
 	{
 		double x = pose.getX();
 		double y = pose.getY();
 		double th = pose.getTh();
 		setToTranslation(x,y);
 		rotate(th);
 	}
 
 	/**
 	 * Transform a point based on the inverse of the transform encoded in this pose.
 	 * Overrides AffineTransform.inverseTransform since poses always have an inverse
 	 * so its not necessary to process the singular matrix exception. Creates a new
 	 * point if out == null. Works correctly if in == out.
 	 * @param in input point
 	 * @param out output point
 	 */
 	public Point2D inverseTransform(Point2D in, Point2D out){
 		if(out == null) out = new Point2D.Double();
 		computeInverse();
 		poseInverse.transform(in,out);
 		return(out);
 	}
 
 	/**
 	 * Transform a point based on the inverse of the rotation matrix encoded in this pose.
 	 * Useful for displacement vectors and forces whose transformation does not include
 	 * the translation part of the transform.
 	 *
 	 * Overrides AffineTransform.inverseDeltaTransform since poses always have an inverse
 	 * so its not necessary to process the singular matrix exception. Creates a new
 	 * point if out == null. Works correctly if in == out.
 	 * @param in input point
 	 * @param out output point
 	 */
 	public Point2D inverseDeltaTransform(Point2D in, Point2D out){
 		if(out == null) out = new Point2D.Double();
 		computeInverse();
 		poseInverse.deltaTransform(in,out);
 		return(out);
 	}
 
 	/**
 	 * Returns the x coordinate. Equivalent to AffineTransform.getTranslateX()
 	 * but shorter to type.
 	 * @return the x coordinate
 	 */
 	public double getX()
 	{
 		return getTranslateX();
 	}
 
 	/**
 	 * Returns the y coordinate. Equivalent to AffineTransform.getTranslateY()
 	 * but shorter to type.
 	 * @return the y coordinate
 	 */
 	public double getY()
 	{
 		return getTranslateY();
 	}
 
 	/**
 	 * Returns the theta coordinate.
 	 * @return the orientation coordinate
 	 */
 	public double getTh()
 	{
 		return getRotateTheta();
 	}
 
 	/**
 	 * Returns the unit vector in the x direction. This is equal to the first
 	 * column of the affineTransform.
 	 * @return the x unit vector
 	 */
 //	public Vector2D getXUnitVector()
 //	{
 //		double th = getRotateTheta();
 //		double cth = Math.cos(th);
 //		double sth = Math.sin(th);
 //		return new Vector2D(cth,sth);
 //	}
 //
 //	/**
 //	 * Returns the unit vector in the y direction. This is equal to the second
 //	 * column of the affineTransform.
 //	 * @return the y unit vector
 //	 */
 //	public Vector2D getYUnitVector()
 //	{
 //		double th = getRotateTheta();
 //		double cth = Math.cos(th);
 //		double sth = Math.sin(th);
 //		return new Vector2D(-sth,cth);
 //	}
 	/**
 	 * Returns a new Point2D which contains the x and y coordinates of this pose.
 	 * @return the Point2D encoding the translation part of this pose.
 	 */
 //	public RealPoint2D getPosition()
 //	{
 //		return new RealPoint2D(getX(),getY());
 //	}
 
 	/**
 	 * Returns the orientation. Identical to getTh() but intended to be complementary to
 	 * AffineTransform.getTranslateX() etc.
 	 * @return the orientation coordinate
 	 */
 	public double getRotateTheta()
 	{
                 double th = MathUtils.atan2(getShearY(),getScaleX());
 		return th;
 	}
 
 	/**
 	 * Returns a new pose2D containing the entire pose.
 	 *
 	 * @return a copy of the pose
 	 */
 	public RealPose2D getPose()
 	{
 		return new RealPose2D(getX(),getY(),getTh());
 	}
 
 	/**
 	 * Assigns the value of poseB to poseA.
 	 * PoseA <- PoseB
 	 * @param poseA destination pose
 	 * @param poseB source pose
 	 */
 	public static void assign(RealPose2D poseA, RealPose2D poseB)
 	{
 		poseA.setPose(poseB.getX(),poseB.getY(),poseB.getTh());
 	}
 
 	/**
 	 * Adds two poses together. Be careful with this one. It never never makes sense to
 	 * add a finite pose to a finite pose. You probably want to multiply two poses instead
 	 * to get a compound transform. There is one exception. When either or both poses contain
 	 * a differential rotation (an angular velocity times a dt), this is the method for you.
 	 * @param dx increment to x coordinate
 	 * @param dy increment to y coordinate
 	 * @param dth increment to th coordinate
 	 */
 	public void add(double dx, double dy, double dth){
 		double x = getTranslateX();
 		double y = getTranslateY();
 		double th = getRotateTheta();
 		x += dx;
 		y += dy;
 		th += dth;
 		//System.out.printf("dx: %f dy:%f dth:%f ",dx,dy,dth); // maybe add a \n here!!
 		th = Angle.normalize(th);
 		setPose(x,y,th);
 	}
 
 	/**
 	 * Subtracts two poses. Be careful with this one. The result is a differential
 	 * pose which could be suitable, for example, in computing a numerical derivative.
 	 */
 	public static void sub(RealPose2D poseA, RealPose2D poseB, RealPose2D poseC){
 		double dx  = poseB.getX()-poseC.getX();
 		double dy  = poseB.getY()-poseC.getY();
 		double dth = poseB.getTh()-poseC.getTh();
 		//System.out.printf("dx: %f dy:%f dth:%f ",dx,dy,dth); // maybe add a \n here!!
 		dth = Angle.normalize(dth);
 		poseA.setPose(dx,dy,dth);
 	}
 
 	/**
 	 * Computes the inverse of a pose. Used internally to invert a pose in closed form and
 	 * avoid matrix inversion.
 	 */
 	private void computeInverse()
 	{
     if (poseInverse == null)
       poseInverse = inverse();
     else {
       double x = getTranslateX();
       double y = getTranslateY();
       double th = getRotateTheta();
       double cth = Math.cos(th);
       double sth = Math.sin(th);
       double xInv = -cth*x-sth*y;
       double yInv =  sth*x-cth*y;
       double thInv = - th;
 		  poseInverse.setPose(xInv,yInv,thInv);
     }
 	}
 
 	/**
 	 * Returns a new pose which is the inverse of this.
 	 * @return the inverse pose.
 	 */
 	public RealPose2D inverse()
 	{
 		double x = getTranslateX();
 		double y = getTranslateY();
 		double th = getRotateTheta();
 		double cth = Math.cos(th);
 		double sth = Math.sin(th);
 		double xInv = -cth*x-sth*y;
 		double yInv =  sth*x-cth*y;
 		double thInv = - th;
 		return new RealPose2D(xInv,yInv,thInv);
 	}
 
 	/**
 	 * Tests if two poses are equal. Succeeds if all elements differ no more than
 	 * Double.MIN_VALUE.
 	 * @param poseA first pose
 	 * @param poseB second pose
 	 * @return true if they are equal
 	 */
 	public static boolean equal(RealPose2D poseA, RealPose2D poseB) {
 		if(Math.abs(poseA.getX()-poseB.getX()) > Double.MIN_VALUE) return false;
 		if(Math.abs(poseA.getY()-poseB.getY()) > Double.MIN_VALUE) return false;
 		if(Math.abs(poseA.getTh()-poseB.getTh()) > Double.MIN_VALUE) return false;
 		return true;
 	}
 
 	/**
 	 * Returns the distance between two poses based on their (x,y) coordinates.
 	 * @param poseA first pose
 	 * @param poseB second pose
 	 * @return the distance between the two poses
 	 */
 	public static double hypot(RealPose2D poseA, RealPose2D poseB) {
                 double x = poseA.getX()-poseB.getX();
                 double y = poseA.getY()-poseB.getY();
                 return Math.sqrt(x*x + y*y);
 	}
 
 	/**
 	 * Returns a new pose which is the product (of the equivalent AffineTransforms) of
 	 * the two supplied arguments.
 	 * @return the composite pose.
 	 */
 	public static RealPose2D multiply(RealPose2D poseA, RealPose2D poseB) {
 		RealPose2D C = new RealPose2D();
 		C.concatenate(poseA);
 		C.concatenate(poseB);
 		return C;
 	}
 	/**
 	 * Compute the product (of the equivalent AffineTransforms) of
 	 * the two supplied arguments and place it in the third. Semantics are:
 	 * 						C = A * B;
 	 */
 	public static void multiply(RealPose2D poseA, RealPose2D poseB, RealPose2D poseC) {
 		poseC.setPose(poseA);
 		poseC.concatenate(poseB);
 	}
 	/**
 	 * Multiplies a pose by a seocnd pose and places the result back in the original
 	 * pose. Equivalent to concatenate(pose) but more mnemonic.
 	 */
 	public void multiply(RealPose2D pose) {
 		concatenate(pose);
 	}
 
 	/**
 	 * Returns a convenient String for printing the contents of this pose.
 	 * @return the convenient string.
 	 */
 	public String toString()
         {
            return getTranslateX() + "," + getTranslateY() + "," + getRotateTheta();
 	}
 
 	/**
 	 * Returns a machine-readable String for printing the contents of this pose.
 	 * @return the convenient string.
 	 */
 	public String serialize()
 	{
		return getTranslateX() + "," + getTranslateY() + "," + getRotateTheta();
 	}
 }
