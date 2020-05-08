 package balle.world.objects;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 
 import balle.misc.Globals;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Orientation;
 import balle.world.Velocity;
 
 public class RectangularObject extends MovingPoint implements FieldObject {
     
 	private final double      width;
     private final double      height;
     private final Orientation orientation;
     
    public RectangularObject(Coord position, Velocity velocity, Orientation orientation,
             double width, double height) {
         super(position, velocity);
         this.width = width;
         this.height = height;
         this.orientation = orientation;
     }
 
     public double getWidth() {
         return width;
     }
 
     public double getHeight() {
         return height;
     }
 
     public Orientation getOrientation() {
         return orientation;
     }
 
     @Override
     public boolean containsCoord(Coord point) {
         Coord dPoint = new Coord(point.getX()-getPosition().getX(),
         							point.getY()-getPosition().getY());
         
         dPoint = dPoint.rotate(getOrientation());
         if (dPoint.getX() < (-width/2.0f)) return false;
         if (dPoint.getX() > (width/2.0f)) return false;
         if (dPoint.getY() < (-height/2.0f)) return false;
         if (dPoint.getY() > (height/2.0f)) return false;
         return true;
     }
 
     @Override
     public boolean isNearWall(Pitch p) {
     	/*
     	 * Defines imaginary rectangle inside the pitch with size dependent on
     	 * the DISTANCE_TO_WALL constant. If the robot is within this rectangle
     	 * it is considered to be away from any walls.
     	 */
     	
     	double minX, maxX, minY, maxY;
     	minX = p.getMinX() + Globals.DISTANCE_TO_WALL;
     	maxX = p.getMaxX() - Globals.DISTANCE_TO_WALL;
     	minY = p.getMinY() + Globals.DISTANCE_TO_WALL;
     	maxY = p.getMaxY() - Globals.DISTANCE_TO_WALL;
     	
     	if (getPosition().getX() < minX)	return true;
     	if (getPosition().getX() > maxX)	return true;
     	if (getPosition().getY() < minY)	return true;
     	if (getPosition().getY() > maxY)	return true;
     	
     	return false;
     	
     }
 
     @Override
     public boolean isInCorner(Pitch p) {
         Coord c[] = new Coord[4];
         c[0] = new Coord(p.getMinX(), p.getMinY());
         c[1] = new Coord(p.getMinX(), p.getMaxY());
         c[2] = new Coord(p.getMaxX(), p.getMinY());
         c[3] = new Coord(p.getMaxX(), p.getMaxY());
         
         for (Coord each : c)
         	each = each.sub(getPosition());
         
         for (Coord each : c)
         	if (each.abs() < Globals.DISTANCE_TO_CORNER)
         		return true;
         
        return false;
     }
 
 	@Override
 	public boolean intersects(Line line) {
 		if (containsCoord(line.getA()) != containsCoord(line.getB())) return true;
 		
 		line = new Line(line.getA(), line.getB());
		line = line.rotate(getOrientation());
		line = line.add(getPosition());
 				
 		double minX, maxX, minY, maxY;
 		minX = -width/2.0;
 		maxX = width/2.0;
 		minY = -height/2.0;
 		maxY = height/2.0;
 		
 		double lMinX, lMaxX, lMinY, lMaxY;
 		lMinX = Math.min(line.getA().getX(), line.getB().getX());
 		lMaxX = Math.max(line.getA().getX(), line.getB().getX());
 		lMinY = Math.min(line.getA().getY(), line.getB().getY());
 		lMaxY = Math.max(line.getA().getY(), line.getB().getY());
 		
 		return lMinX < minX && maxX < lMaxX && lMinY < minY && maxY < lMaxY;
 	}
     
 
 }
