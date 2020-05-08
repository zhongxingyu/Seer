 package ru.yandex.hackaton.server.geocoder.geo;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
import com.sun.deploy.util.StringUtils;
 import org.apache.commons.lang3.Validate;
 
 /**
  * @author Sergey Polovko
  */
 public class Line {
 
     private final List<Point> points;
 
     public Line(List<Point> points) {
         Validate.isTrue(points.size() >= 2);
         this.points = points;
     }
 
     public Line(Point... points) {
         this(Arrays.asList(points));
     }
 
     public List<Point> points() {
         return points;
     }
 
     public String toGml() {
         StringBuilder sb = new StringBuilder(128);
         for (Point point : points) {
             sb.append(point.toGml());
             sb.append(',');
         }
         sb.setLength(sb.length() - 1);
         return sb.toString();
     }
 
     public String toWkt() {
         StringBuilder sb = new StringBuilder(128);
         for (Point point : points) {
             sb.append(point.toWkt());
             sb.append(',');
         }
         sb.setLength(sb.length() - 1);
         return sb.toString();
     }
 
     public static Line parseGml(String gml) {
        String[] coordinatesList = StringUtils.splitString(gml, ",");
         Validate.isTrue(coordinatesList.length > 0);
 
         List<Point> points = new ArrayList<>();
         for (String coordinates : coordinatesList) {
             points.add(Point.parseGml(coordinates));
         }
 
         return new Line(points);
     }
 
     public static Line parseWkt(String wkt) {
        String[] coordinatesList = StringUtils.splitString(wkt, ",");
         Validate.isTrue(coordinatesList.length > 0);
 
         List<Point> points = new ArrayList<>();
         for (String coordinates : coordinatesList) {
             points.add(Point.parseWkt(coordinates));
         }
 
         return new Line(points);
     }
 }
