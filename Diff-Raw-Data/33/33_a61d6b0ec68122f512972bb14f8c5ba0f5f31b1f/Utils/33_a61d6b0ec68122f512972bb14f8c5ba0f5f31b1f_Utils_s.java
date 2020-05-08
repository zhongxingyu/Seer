 package mosquito.g4.utils;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.io.PrintStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Stack;
 
 import mosquito.g4.voronoi.LineParam;
 
 public class Utils {
     static Random r = new Random();
 
     public static boolean hasStraightPath(Point2D p1, Point2D p2,
             Iterable<Line2D> walls) {
         Line2D.Double path = new Line2D.Double(p1, p2);
 
         for (Line2D wall : walls) {
             if (path.intersectsLine(wall)) {
                 return false;
             }
         }
 
         return true;
     }
 
     public static Point2D getCenter(Line2D l) {
         return new Point2D.Double(.5 * (l.getX1() + l.getX2()),
                 .5 * (l.getY1() + l.getY2()));
     }
 
     public static Line2D getPerpendicularLine(Line2D l, double lineDistance) {
         double x1 = l.getX1(), x2 = l.getX2(), y1 = l.getY1(), y2 = l.getY2();
 
         double dx = x1 - x2;
         double dy = y1 - y2;
         double dist = Math.sqrt(dx * dx + dy * dy);
         dx /= dist;
         dy /= dist;
 
         double centerX = (x1 + x2) / 2;
         double centerY = (y1 + y2) / 2;
 
         double x3 = centerX + (lineDistance / 2) * dy;
         double y3 = centerY - (lineDistance / 2) * dx;
         double x4 = centerX - (lineDistance / 2) * dy;
         double y4 = centerY + (lineDistance / 2) * dx;
 
         return new Line2D.Double(x3, y3, x4, y4);
     }
 
     public static boolean withinBounds(int minDim, int maxDim, double d) {
         return d >= minDim && d < maxDim;
     }
 
     public static void print(PrintStream stream, int[][] array) {
         for (int i = 0; i < array.length; i++) {
             for (int j = 0; j < array[0].length; j++) {
                 int val = array[j][i];
                 stream.print(String.format(val < 10 ? "0%d " : "%d ", val));
             }
             stream.println();
         }
     }
 
     public static <T> T getRandomElement(List<T> list) {
         return list.get(r.nextInt(list.size()));
     }
 
     public static void print(PrintStream out, List<Line2D> lines) {
         for (Line2D line : lines) {
             out.println(line.getP1() + " " + line.getP2());
         }
     }
 
     public static void addAll(Map<Integer, List<Point2D>> pointsForSection,
             Stack<LineParam> temp) {
         while (!temp.isEmpty()) {
             LineParam entry = temp.pop();
 
             int section = entry.getSection();
             if (pointsForSection.get(section) == null) {
                 pointsForSection.put(section, new LinkedList<Point2D>());
             }
             pointsForSection.get(section).add(entry.getPoint());
         }
     }
 
     public static void addAll(Map<Integer, List<Point2D>> pointsForSection,
             List<LineParam> temp) {
         while (!temp.isEmpty()) {
             LineParam entry = temp.remove(0);
 
             int section = entry.getSection();
             if (pointsForSection.get(section) == null) {
                 pointsForSection.put(section, new LinkedList<Point2D>());
             }
             pointsForSection.get(section).add(entry.getPoint());
         }
     }
 }
