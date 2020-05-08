 package com.where.web;
 
 import com.where.domain.Point;
 import com.where.domain.Direction;
 import com.where.domain.alg.AbstractDirection;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.LinkedHashMultimap;
 import com.google.common.collect.SetMultimap;
 import com.google.common.collect.Lists;
 
 import java.util.*;
 import java.text.DecimalFormat;
 
 /**
  * @author Charles Kubicek
  *         <p/>
  *         {"points": { "pointsArray" : [
  *         { "lat" : 51.485412516629566, "lng" : -0.12172630804428243, "direction" : "Southbound", "description" : "At Vauxhall Platform 2"}
  *         ]}}
  *         <p/>
  *         corresponts to (7 decimal places on lat and long, directions are NSEW):
  *         <p/>
  *         {"p": { "a" : [
  *         { "t" : 51.4854125, "g" : -0.1217263, "d" : "S", "i" : "At Vauxhall Platform 2"}
  *         ]}}
  */
 public class JsonTransformer {
 
     private static final JsonLiteralValues JV = WmtProperties.SHORTENED_JSON_LITERAL_NAMES ? JsonLiteralValues.SHORT : JsonLiteralValues.LONG;
     private static final String ERROR_PROPERTY_NAME = "error";
     private static final String ERROR_PREFIX = "{\""+ERROR_PROPERTY_NAME+"\" : \"  ";
     private static final String ERROR_SUFFIX = "  \" }";
 
     public static String toJson(LinkedHashMap<AbstractDirection, List<Point>> points) {
         return makeJsonPoints(convertPoints(points)).toString();
     }
 
     public static String toJson(SetMultimap<AbstractDirection, Point> points) {
         return makeJsonPoints(points.values()).toString();
     }
 
     public static String toJsonError(String errorMsg){
         return new StringBuffer(ERROR_PREFIX).append(errorMsg).append(ERROR_SUFFIX).toString();
     }
 
     private static StringBuffer makeJsonPoints(Collection<Point> points) {
        StringBuffer buf = new StringBuffer("{");
        buf.append(JV.getPoints()).append(": { ");
        buf.append(JV.getPointArray()).append(" : [\n");
 
         for(ListIterator<Point> iter = Lists.newArrayList(points).listIterator(); iter.hasNext();){
             Point point = iter.next();
            System.out.println("JsonTransformer.makeJsonPoints hasNext() "+iter.hasNext());
             appendArrayEntry(buf, point, !iter.hasNext());
         }
 
         buf.append("]}}");
         return buf;
     }
 
     private static void appendArrayEntry(StringBuffer buf, Point point, boolean lastEntry) {
         buf.append("  { ").append(new FormattingPointDecorator(point).toArrayEntry());
         if(lastEntry){
             buf.append("}\n");
         }else{
             buf.append("},\n");
         }
     }
 
     private static class FormattingPointDecorator {
         private final Point point;
 
         private static final String SPACE_COMMA = ", ";
         private static final String SPACE_COLON_SPACE = " : ";
 
         public FormattingPointDecorator(Point point) {
             this.point = point;
         }
 
         public StringBuffer toArrayEntry() {
             StringBuffer buf = new StringBuffer();
             appendLat(buf);
             appendLng(buf);
             appendDirection(buf);
             appendDescription(buf);
             return buf;
         }
 
         private void appendDirection(StringBuffer buf) {
             nameValuePair(buf, JV.getDirection(), wrapInQuotes(formatDirection(point.getDirection())), false);
         }
 
         private void appendLat(StringBuffer buf) {
             nameValuePair(buf, JV.getLatitude(), latLngFormat.format(point.getLat()), false);
         }
 
         private void appendLng(StringBuffer buf) {
             nameValuePair(buf, JV.getLongitude(), latLngFormat.format(point.getLng()), false);
         }
 
         private void appendDescription(StringBuffer buf) {
             nameValuePair(buf, JV.getDescription(), wrapInQuotes(point.getDescription()), true);
         }
 
         private void nameValuePair(StringBuffer buf, String name, String value, boolean end) {
             buf.append(name).append(SPACE_COLON_SPACE).append(value);
             if (!end) buf.append(SPACE_COMMA);
         }
     }
 
     public static enum JsonLiteralValues {
         LONG("points", "pointsArray", "lat", "lng", "direction", "description"),
         SHORT("p", "a", "t", "g", "d", "i");
 
         private final String points;
         private final String pointArray;
         private final String latitude;
         private final String longitude;
         private final String direction;
         private final String description;
 
         private JsonLiteralValues(String points, String pointArray, String latitude, String longitude, String direction, String description) {
             this.points = wrapInQuotes(points);
             this.pointArray = wrapInQuotes(pointArray);
             this.latitude = wrapInQuotes(latitude);
             this.longitude = wrapInQuotes(longitude);
             this.direction = wrapInQuotes(direction);
             this.description = wrapInQuotes(description);
         }
 
         public String getPoints() {
             return points;
         }
 
         public String getPointArray() {
             return pointArray;
         }
 
         public String getLatitude() {
             return latitude;
         }
 
         public String getLongitude() {
             return longitude;
         }
 
         public String getDirection() {
             return direction;
         }
 
         public String getDescription() {
             return description;
         }
     }
 
 
     private static String wrapInQuotes(String st) {
         return "\"" + st + "\"";
     }
 
 
     //8 decimal places for now
     private static final DecimalFormat latLngFormat = new DecimalFormat("#0.000000");
 
     private static String formatDirection(Direction direction) {
         return direction.getName().substring(0, 1);
     }
 
     private static Set<Point> convertPoints(LinkedHashMultimap<AbstractDirection, Point> points) {
         Set<Point> result = new HashSet<Point>();
 
         for (Point p : points.values()) {
             result.add(p);
         }
         return result;
     }
 
     private static Set<Point> convertPoints(LinkedHashMap<AbstractDirection, List<Point>> points) {
         Set<Point> result = new HashSet<Point>();
 
         for (List<Point> ps : points.values()) {
             result.addAll(ps);
         }
         return result;
     }
 
     private static StringBuffer makeDescriptiveJsonPoints(Set<Point> points) {
         StringBuffer buf = new StringBuffer("{\"points\": { \"pointsArray\" : [\n");
 
         for (Point point : points) {
             buf.append("  { \"lat\" : " + point.getLat() + ", \"lng\" : " + point.getLng() + ", \"direction\" : \"" + point.getDirection().getName() + "\", \"description\" : \"" + point.getDescription() + "\"},\n");
         }
         buf.append("]}}");
         return buf;
     }
 }
