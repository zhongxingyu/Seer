 package usask.hci.fastdraw;
 
 import java.util.ArrayList;
 
 import android.graphics.PointF;
 import android.graphics.RectF;
 
 public class GestureDetector {
     public enum Gesture {
         UNKNOWN,
         UP, DOWN, LEFT, RIGHT,
         UP_DOWN, UP_LEFT, UP_RIGHT,
         DOWN_UP, DOWN_LEFT, DOWN_RIGHT,
         LEFT_RIGHT, LEFT_UP, LEFT_DOWN,
         RIGHT_LEFT, RIGHT_UP, RIGHT_DOWN
     }
     
     private class Template {
         private Gesture gesture;
         private ArrayList<PointF> points;
         
         Template(Gesture gesture, ArrayList<PointF> points) {
             this.gesture = gesture;
             this.points = new ArrayList<PointF>(points);
             
             this.points = resample(this.points, numPoints);
             this.points = rescale(this.points);
             this.points = translateToOrigin(this.points);
         }
     }
 
     // Number of points in resampled paths
     public static int numPoints = 16;
 
     // Threshold for detecting one-dimensional gestures. If the smaller of
     // width or height is less than this fraction of the larger dimension,
     // the gesture will be treated as one-dimensional.
     private static float oneDThreshold = 0.25f;
     
     // Scaling size
     private static float squareSize = 250.0f;
 
     private static int startAngleIndex = numPoints / 8;
 
     private ArrayList<PointF> points = new ArrayList<PointF>();
     private ArrayList<Template> templates = new ArrayList<Template>();
     
     public GestureDetector() {
         ArrayList<PointF> points = new ArrayList<PointF>();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(i, 0));
         addTemplate(Gesture.RIGHT, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(i, 0));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(0, -i));
         addTemplate(Gesture.RIGHT_UP, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(i, 0));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(0, i));
         addTemplate(Gesture.RIGHT_DOWN, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(i, 0));
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(-i, 0));
         addTemplate(Gesture.RIGHT_LEFT, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(-i, 0));
         addTemplate(Gesture.LEFT, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(-i, 0));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(0, -i));
         addTemplate(Gesture.LEFT_UP, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(-i, 0));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(0, i));
         addTemplate(Gesture.LEFT_DOWN, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(-i, 0));
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(i, 0));
         addTemplate(Gesture.LEFT_RIGHT, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(0, -i));
         addTemplate(Gesture.UP, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(0, -i));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(i, 0));
         addTemplate(Gesture.UP_RIGHT, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(0, -i));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(-i, 0));
         addTemplate(Gesture.UP_LEFT, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(0, -i));
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(0, i));
         addTemplate(Gesture.UP_DOWN, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(0, i));
         addTemplate(Gesture.DOWN, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(0, i));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(i, 0));
         addTemplate(Gesture.DOWN_RIGHT, points);
 
         points.clear();
         for (int i = -numPoints; i < 0; i++)
             points.add(new PointF(0, i));
         for (int i = 0; i < numPoints; i++)
             points.add(new PointF(-i, 0));
         addTemplate(Gesture.DOWN_LEFT, points);
 
         points.clear();
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(0, i));
         for (int i = -numPoints; i < numPoints; i++)
             points.add(new PointF(0, -i));
         addTemplate(Gesture.DOWN_UP, points);
     }
 
     // add the template to template list
     private void addTemplate(Gesture gesture, ArrayList<PointF> points) {
         templates.add(new Template(gesture, points));
     }
 
     private static float degreesToRadians(float degrees) {
         return (float) (degrees * Math.PI / 180.0);
     }
 
     private static float pathLength(ArrayList<PointF> points) {
         float length = 0;
         
         for (int i = 0; i < points.size() - 1; i++)
             length += distance(points.get(i), points.get(i + 1));
         
         return length;
     }
 
     private static float distance(PointF p1, PointF p2) {
         float dx = p2.x - p1.x;
         float dy = p2.y - p1.y;
         return (float) Math.sqrt(dx * dx + dy * dy);
     }
 
     private static ArrayList<PointF> resample(ArrayList<PointF> points, int n) {
         float intervalLength = pathLength(points) / (n - 1);
         float dist = 0;
 
         ArrayList<PointF> srcPts = new ArrayList<PointF>(points);
         ArrayList<PointF> dstPts = new ArrayList<PointF>(n);
         dstPts.add(srcPts.get(0));
 
         for (int i = 1; i < srcPts.size(); i++) {
             PointF pt1 = srcPts.get(i - 1);
             PointF pt2 = srcPts.get(i);            
             float pointDist = distance(pt1, pt2);
             
             if (dist + pointDist >= intervalLength) {
                 float x = pt1.x + ((intervalLength - dist) / pointDist) * (pt2.x - pt1.x);
                 float y = pt1.y + ((intervalLength - dist) / pointDist) * (pt2.y - pt1.y);
                 
                 PointF p = new PointF(x, y);
                 dstPts.add(p);
                 srcPts.add(i, p);
                 
                 dist = 0;
             } else {
                 dist += pointDist;
             }
         }
 
         if (dstPts.size() == n - 1) {
             dstPts.add(srcPts.get(srcPts.size() - 1));
         }
 
         return dstPts;
     }
 
     // Compute the 'distance' between two point paths by summing their
     // corresponding point distances.
     // Assume that each path has been resampled to the same number of points at
     // the same distance apart.
     private static float pathDistance(ArrayList<PointF> path1, ArrayList<PointF> path2) {
         float distance = 0;
         
         for (int i = 0; i < path1.size(); i++) {
             distance += distance(path1.get(i), path2.get(i));
         }
         
         return distance / path1.size();
     }
 
     // method to compute the bounding box.
     private static RectF boundingBox(ArrayList<PointF> points) {
         float minX = Float.MAX_VALUE;
         float maxX = Float.MIN_VALUE;
         float minY = Float.MAX_VALUE;
         float maxY = Float.MIN_VALUE;
 
         for (PointF p : points) {
             if (p.x < minX)
                 minX = p.x;
             if (p.x > maxX)
                 maxX = p.x;
 
             if (p.y < minY)
                 minY = p.y;
             if (p.y > maxY)
                 maxY = p.y;
         }
 
         return new RectF(minX, minY, maxX, maxY);
     }
 
     private static ArrayList<PointF> rescale(ArrayList<PointF> points) {
         RectF bound = boundingBox(points);
         boolean uniform = Math.min(bound.width() / bound.height(), bound.height() / bound.width()) <= oneDThreshold;
         
         ArrayList<PointF> newPoints = new ArrayList<PointF>(points.size());
 
         for (PointF p : points) {
             float x, y;
             
             if (uniform) {
                 float maxDimension = Math.max(bound.width(), bound.height());
                 x = p.x * squareSize / maxDimension;
                 y = p.y * squareSize / maxDimension;
             } else {
                 x = p.x * squareSize / bound.width();
                 y = p.y * squareSize / bound.height();
             }
 
             newPoints.add(new PointF(x, y));
         }
         
         return newPoints;
     }
 
     private static PointF centroid(ArrayList<PointF> points) {
         float xsum = 0;
         float ysum = 0;
 
         for (PointF p : points) {
             xsum += p.x;
             ysum += p.y;
         }
         
         return new PointF(xsum / points.size(), ysum / points.size());
     }
 
     // Translate the centroid of the points to the origin and translate all
     // other points to match it.
     private static ArrayList<PointF> translateToOrigin(ArrayList<PointF> points) {
         PointF c = centroid(points);
         ArrayList<PointF> newPoints = new ArrayList<PointF>(points.size());
 
         for (PointF p : points)
             newPoints.add(new PointF(p.x - c.x, p.y - c.y));
 
         return newPoints;
     }
 
     // Rotate the points about their centroid by the given angle.
     private static ArrayList<PointF> rotateByRadians(ArrayList<PointF> points, float theta) {
         ArrayList<PointF> newPoints = new ArrayList<PointF>(points.size());
         PointF c = centroid(points);
 
         float cos = (float) Math.cos(theta);
         float sin = (float) Math.sin(theta);
 
         for (PointF p : points) {
             float dx = p.x - c.x;
             float dy = p.y - c.y;
             float x = (dx * cos) - (dy * sin) + c.x;
             float y = (dx * sin) + (dy * cos) + c.y;
             newPoints.add(new PointF(x, y));
         }
 
         return newPoints;
     }
 
     private static PointF calcStartUnitVector(ArrayList<PointF> points) {
         PointF i1 = points.get(startAngleIndex);
         PointF i2 = points.get(0);
         PointF v = new PointF(i1.x - i2.x, i1.y - i2.y);
         float len = (float) Math.sqrt(v.x * v.x + v.y * v.y);
         return new PointF(v.x / len, v.y / len);
     }
 
     private static float angleBetweenUnitVectors(PointF v1, PointF v2) {
         return (float) Math.acos(v1.x * v2.x + v1.y * v2.y); // arc cosine of the vector dot product
     }
 
     private static float angleSimilarityThreshold = degreesToRadians(30.0f);
     private static float angleRange = degreesToRadians(15.0f);
     private static float anglePrecision = degreesToRadians(2.0f);
     
     public Gesture recognize() {
        if (points.isEmpty() || points.size() < 2)
             return Gesture.UNKNOWN;
 
         ArrayList<PointF> pointsx = resample(points, numPoints);
         pointsx = rescale(pointsx);
         pointsx = translateToOrigin(pointsx);
 
         PointF startUnitVector = calcStartUnitVector(pointsx);
 
         Gesture bestMatch = Gesture.UNKNOWN;
         double bestDistance = Double.POSITIVE_INFINITY;
 
         for (Template template : templates) {
             PointF startv = calcStartUnitVector(template.points);
 
             double dist;
             
             if (angleBetweenUnitVectors(startv, startUnitVector) <= angleSimilarityThreshold) {
                 dist = distanceAtBestAngle(pointsx, template.points, -angleRange, angleRange, anglePrecision);
             } else {
                 dist = Double.POSITIVE_INFINITY;
             }
 
             if (dist < bestDistance) {
                 bestMatch = template.gesture;
                 bestDistance = dist;
             }
         }
 
         return bestMatch;
     }
     
     private static float Phi = (-1.0f + (float) Math.sqrt(5.0)) / 2;
 
     // Do a golden section search algorithm, checking at different angles for the minimum distance
     private static float distanceAtBestAngle(ArrayList<PointF> points, ArrayList<PointF> templatePoints, float a, float b, float threshold) {
         float x1 = Phi * a + (1.0f - Phi) * b;
         float f1 = distanceAtAngle(points, templatePoints, x1);
         float x2 = (1.0f - Phi) * a + Phi * b;
         float f2 = distanceAtAngle(points, templatePoints, x2);
 
         while (Math.abs(b - a) > threshold) {
             if (f1 < f2) {
                 b = x2;
                 x2 = x1;
                 f2 = f1;
                 x1 = Phi * a + (1.0f - Phi) * b;
                 f1 = distanceAtAngle(points, templatePoints, x1);
             } else {
                 a = x1;
                 x1 = x2;
                 f1 = f2;
                 x2 = (1.0f - Phi) * a + Phi * b;
                 f2 = distanceAtAngle(points, templatePoints, x2);
             }
         }
         
         return Math.min(f1, f2);
     }
 
     private static float distanceAtAngle(ArrayList<PointF> points, ArrayList<PointF> templatePoints, float theta) {
         ArrayList<PointF> newPoints = rotateByRadians(points, theta);
         return pathDistance(newPoints, templatePoints);
     }
 
     public void clear() {
         points.clear();
     }
 
     public void addPoint(float x, float y) {
         points.add(new PointF(x, y));
     }
 }
