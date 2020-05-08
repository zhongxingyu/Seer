 package game.util;
 
 
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 
 public class Polygon {
 
 	//private final int[] defaultXpoints;
 	//private final int[] defaultYpoints;
 	private int[] xpoints;
 	private int[] ypoints;
 	Point center;
	private int centerX=20;
	private int centerY=20;
 	double angleP;
 	Point[] currentPoints;
 	int[] originalXPoints;
 	int[] originalYPoints;
 	int xLocation, yLocation;
 
 	/*
 	 * Crée un polygone avec xpoints points en X,
 	 * ypoints points en Y et npoints nombres de points
 	 */
 	public Polygon(int[] xpoints, int[] ypoints){ //TODO remove width et height
 		//this.defaultXpoints = xpoints;
 		//this.defaultYpoints = ypoints;
 		this.xpoints=xpoints.clone();
 		this.ypoints=ypoints.clone();
 		this.originalXPoints = xpoints.clone();
 		this.originalYPoints = ypoints.clone();
 		center = getCenter();
 		angleP = 0;
 		xLocation = 0;
 		yLocation = 0;
 		currentPoints = getOriginalPoints();
 	}
 	
 	private Point getCenter(){ //TODO check
 		int centerX = 0, centerY = 0 ;
 		for(int i = 0 ; i<getNpoints() ; i++){
 			centerX += this.getXpoint(i);
 			centerY += this.getYpoint(i);
 		}
 		return new Point(centerX/getNpoints(),centerY/getNpoints());
 	}
 
 	private Point[] getOriginalPoints() {
 		Point[] originalPoints = new Point[getNpoints()];
 		for(int i = 0 ; i<originalPoints.length ; i++){
 			originalPoints[i] = new Point(originalXPoints[i],originalYPoints[i]);
 		}
 		return originalPoints;
 	}
 
 	public boolean intersects(Polygon other){
 		int tw = this.getWidth();
 		int th = this.getHeight();
 		int ow = other.getWidth();
 		int oh = other.getHeight();
 		if (ow <= 0 || oh <= 0 || tw <= 0 || th <= 0) {
 			return false;
 		}
 		int tx = this.getLowestX();
 		int ty = this.getLowestY();
 		int ox = other.getLowestX();
 		int oy = other.getLowestY();
 		ow += ox;
 		oh += oy;
 		tw += tx;
 		th += ty;
 		//      overflow || intersect
 		return ((ow < ox || ow > tx) &&
 				(oh < oy || oh > ty) &&
 				(tw < tx || tw > ox) &&
 				(th < ty || th > oy));
 	}
 
 	public void moveTo(int x, int y){
 		for(int i=0;i<getNpoints();i++){
 			     xpoints[i]+=x-centerX;
 			     ypoints[i]+=y-centerY;
 			   }
 			   centerX=x;
 			   centerY=y;
 		xLocation = x;
 		yLocation = y;
 	}
 
 	public double getAngleP(){
 		return angleP;
 	}
 
 	public void rotate(float angle){
 		rotatePointMatrix(getOriginalPoints(),(double)angle,currentPoints);
 		updatePolygon(currentPoints);
 	}
 
 	public void rotatePointMatrix(Point[] origPoints, double angle, Point[] storeTo){
 
         /* We ge the original points of the polygon we wish to rotate
          *  and rotate them with affine transform to the given angle. 
          *  After the opeariont is complete the points are stored to the 
          *  array given to the method.
         */
         AffineTransform.getRotateInstance
         (angle, center.x, center.y)
                 .transform(origPoints,0,storeTo,0,getNpoints());
 
 
     }
 
 	public void updatePolygon(Point[] polyPoints){
 
         for(int  i=0; i < polyPoints.length; i++){
         	setXpoint(i,polyPoints[i].x+xLocation);
         	setYpoint(i,polyPoints[i].y+yLocation);
         }
 
     }
 
 	public int getHeight(){
 		int min=Integer.MAX_VALUE;
 		int max=Integer.MIN_VALUE;
 		for(int i=0;i<getNpoints();i++){
 			if(ypoints[i]<min){
 				min=ypoints[i];
 			}
 			else if(ypoints[i]>max){
 				max=ypoints[i];
 			}
 		}
 		return max-min;
 	}
 
 	public int getWidth(){
 		int min=Integer.MAX_VALUE;
 		int max=Integer.MIN_VALUE;
 		for(int i=0;i<getNpoints();i++){
 			if(xpoints[i]<min){
 				min=xpoints[i];
 			}
 			else if(xpoints[i]>max){
 				max=xpoints[i];
 			}
 		}
 		return max-min;
 	}
 
 	public int getLowestX(){
 		int lo=Integer.MAX_VALUE;
 		for(int i=0;i<getNpoints();i++){
 			if(this.xpoints[i]<lo)
 				lo=this.xpoints[i];
 		}
 		return lo;
 	}
 	public int getLowestY(){
 		int lo=Integer.MAX_VALUE;
 		for(int i=0;i<getNpoints();i++){
 			if(this.ypoints[i]<lo)
 				lo=this.ypoints[i];
 		}
 		return lo;
 	}
 
 	public int getXpoint(int i){
 		return xpoints[i];
 	}
 
 	public void setXpoint(int i, int value){
 		xpoints[i] = value;
 	}
 
 	public int[] getXpoints() {
 		return xpoints;
 	}
 
 	public void setXpoints(int[] xpoints) {
 		this.xpoints = xpoints;
 	}
 
 	public int getYpoint(int i){
 		return ypoints[i];
 	}
 
 	public void setYpoint(int i, int value){
 		ypoints[i] = value;
 	}
 
 	public int[] getYpoints() {
 		return ypoints;
 	}
 
 	public void setYpoints(int[] ypoints) {
 		this.ypoints = ypoints;
 	}
 
 	public int getNpoints(){
 		return this.xpoints.length;
 	}
 }
