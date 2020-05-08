 package info.jeppes.ZoneCore.TriggerBoxes;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 
 public abstract class PolygonTriggerBox extends TriggerBox{
 
     private ArrayList<Point2D> polygon;
     private double minY;
     private double maxY;
     private double radius = 0;
     private PrecisePoint simpleCentroid = null;
     
     public PolygonTriggerBox(ArrayList<Location> polygon, String name) throws Exception{
         super(polygon.get(0).getWorld(),name);
         recalculateRadiusAndCentroid();
         for(Location location : polygon){
             if(!location.getWorld().equals(getWorld())){
                 throw new Exception("Some locations are not in the same world");
             } else{
                 this.polygon.add(new PrecisePoint(location.getX(), location.getBlockZ()));
                 minY = Math.min(minY, location.getY());
                 maxY = Math.max(maxY, location.getY());
             }
         }
     }
     public PolygonTriggerBox(ArrayList<Location> polygon, String name, double minY, double maxY) throws Exception{
         super(polygon.get(0).getWorld(),name);
         recalculateRadiusAndCentroid();
         if(minY > maxY){
             this.minY = maxY;
             this.maxY = minY;
         } else {
             this.minY = minY;
             this.maxY = maxY;
         }
         for(Location location : polygon){
             if(!location.getWorld().equals(getWorld())){
                 throw new Exception("Some locations are not in the same world");
             } else{
                 this.polygon.add(new PrecisePoint(location.getX(), location.getBlockZ()));
             }
         }
     }
     public PolygonTriggerBox(ArrayList<Point2D> polygon, World world, String name, double minY, double maxY) throws Exception{
         super(world,name);
         recalculateRadiusAndCentroid();
         this.polygon = polygon;
         if(minY > maxY){
             this.minY = maxY;
             this.maxY = minY;
         } else {
             this.minY = minY;
             this.maxY = maxY;
         }
     }
     
     // return getArea of polygon
     public double getArea() { return Math.abs(getSignedArea()); }
 
     // return signed getArea of polygon
     public double getSignedArea() {
         double sum = 0.0;
         for (int i = 0; i < polygon.size()-1; i++) {
             sum = sum + (polygon.get(i).getX() * polygon.get(i+1).getY()) - (polygon.get(i).getY() * polygon.get(i+1).getX());
         }
         return 0.5 * sum;
     }
     
     private void recalculateMinAndMaxY(){
        boolean first = true;
         for(Point2D location : polygon){
            if(first){
                first = false;
                minY = location.getY();
                maxY = location.getY();
                continue;
            }
             minY = Math.min(minY, location.getY());
             maxY = Math.max(maxY, location.getY());
         }
     }
     
     private void recalculateRadiusAndCentroid(){
         double longestDiameter = 0;
         Point2D usedPoint1 = null;
         Point2D usedPoint2 = null;
         for(Point2D point : polygon){
             for(Point2D point2 : polygon){
                 double distance = point.distance(point2);
                 if(distance > longestDiameter){
                     longestDiameter = distance;
                     usedPoint1 = point;
                     usedPoint2 = point2;
                 }
             }
         }
         radius = longestDiameter/2;
         double angle = PrecisePoint.angleFrom(usedPoint1, usedPoint2);
         simpleCentroid = new PrecisePoint(usedPoint1.getX() + (Math.cos(angle) * radius),usedPoint1.getY() + (Math.sin(angle) * radius));
     }
     
     public ArrayList<Point2D> getPolygon(){
         return (ArrayList<Point2D>)polygon.clone();
     }
     public void setPolygon(ArrayList<Point2D> polygon){
         this.polygon = polygon;
         recalculateRadiusAndCentroid();
         recalculateMinAndMaxY();
     }
     
     public double getRadius() {
         return radius;
     }
     public Point2D getSimpleCentroid(){
         return simpleCentroid.clone();
     }
     
     @Override
     public boolean isInside(Location location) {
         //fast and easy check to see if the location is not inside the box
         if(location.getY() < minY || maxY < location.getY() || !location.getWorld().equals(getWorld())){
             return false;
         }
         int i;
         int j;
         boolean result = false;
         for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
             if ((polygon.get(i).getY() > location.getBlockZ()) != (polygon.get(j).getY() > location.getBlockZ()) &&
                 (location.getBlockX() < (polygon.get(j).getX() - polygon.get(i).getX()) * (location.getBlockZ() - polygon.get(i).getY()) / (polygon.get(j).getY()-polygon.get(i).getY()) + polygon.get(i).getX())) {
                 result = !result;
             }
         }
         return result;
     }
 
     @Override
     public Location getRandomLocationInsideBox() {
         Point2D simpleCentroid1 = getSimpleCentroid();
         for(int i = 0; i < 100; i++){
             double randomAngle = Math.random() * Math.PI * 2;
             double randomRadius = Math.random() * getRadius();
             double x = Math.cos(randomAngle) * randomRadius + simpleCentroid1.getX();
             double z = Math.sin(randomAngle) * randomRadius + simpleCentroid1.getY();
             double y = minY + Math.random() * (maxY - minY);
             Location location = new Location(getWorld(),x,y,z);
             if(isInside(location)){
                 return location;
             }
         }
         //in case it does not find a random point inside the polygon within 
         //100 tries, it will return the simple centroid
         return new Location(
                 getWorld(),                             //world
                 simpleCentroid.getX(),                  //X
                 minY + Math.random() * (maxY - minY),   //Y
                 simpleCentroid.getY());                 //Z
     }
     public Location getRandomLocationInsideBoxOnGround(){
         Location location = null;
         for(int i = 0; i < 100; i++){
             location = getRandomLocationInsideBox();
             ArrayList<Integer> possibleY = new ArrayList();
 
             boolean lastBlockEmpty = false;
             for(int y = (int) minY; y < maxY; y++){
                 int blockTypeIdAt = getWorld().getBlockTypeIdAt(location.getBlockX(), y, location.getBlockZ());
                 if(blockTypeIdAt == 0){
                     if(!lastBlockEmpty){
                         possibleY.add(y);
                     }
                     lastBlockEmpty = true;
                 } else {
                     lastBlockEmpty = false;
                 }
             }
             if(!possibleY.isEmpty()){
                 location.setY(possibleY.get((int)(Math.random() * possibleY.size())));
                 return location;
             }
         }
         return location;
     }
 }
