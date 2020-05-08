 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 public class SensorDrawingUtils {
 
     private final static double SENSOR_SCREEN_WIDTH = 20.0;
 
     public static Rectangle2D GetBoundsFromSensor(Sensor sensor) {
         Point2D upLeftCorner = ComputeSensorScreenPosition(sensor);
        return new Rectangle2D.Double(upLeftCorner.getX(), upLeftCorner.getY(), SENSOR_SCREEN_WIDTH, SENSOR_SCREEN_WIDTH);
     }
 
     public static double GetSensorScreenWidth() {
         return SENSOR_SCREEN_WIDTH;
     }
 
     public static void PaintSensorRange(Graphics g, Sensor sensor) {
         Shape antennaeShape = GetDirectedAntennaeShape(sensor);
         Shape debugOrientation = GetDebugOrientationLine(sensor);
 
         Graphics2D g2d = (Graphics2D) g;
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setColor(ColorTheme.White);
         g2d.draw(antennaeShape);
 
         g2d.setColor(Color.green);
         g2d.draw(debugOrientation);
 
         g2d.setColor(Color.red);
 
         double x = sensor.getPosition().getX();
         double y = sensor.getPosition().getY();
 
        g2d.draw(new Ellipse2D.Double(x-1, y-1, 2, 2));
     }
 
     private static Shape GetOmnidirectionalShape(Sensor sensor) {
         Point2D sensorCenter = ComputeSensorScreenPosition(sensor);
         double radius = Sensor.GetRange();
         double x = sensorCenter.getX() - radius / 2.0;
         double y = sensorCenter.getY() - radius / 2.0;
 
         return new Ellipse2D.Double(x, y, radius, radius);
     }
 
     private static Shape GetDebugOrientationLine(Sensor sensor) {
         Point2D position = sensor.getPosition();
 
         double angle = sensor.getOrientation();
         double range = Sensor.GetRange();
         double x = position.getX() + range * Math.cos(angle);
         double y = position.getY() + range * Math.sin(angle);
 
         return new Line2D.Double(position.getX(), position.getY(), x, y);
     }
 
 
     private static Shape GetDirectedAntennaeShape(Sensor sensor) {
         double width, height, radius;
         width = height = radius = Sensor.GetRange();
 
         double orientation = Math.toDegrees(sensor.getOrientation());
         double extent = Math.toDegrees(Sensor.GetAngle());
         double start  = orientation - extent / 2.0;
 
        Point2D sensorPosition = sensor.getPosition();
        double x = sensorPosition.getX() - radius / 2.0;
        double y = sensorPosition.getY() - radius / 2.0;
 
         return new Arc2D.Double(x, y, width, height, start, extent, Arc2D.PIE);
     }
 
 
    public static Point2D ComputeSensorScreenPosition(Sensor sensor) {
         Point2D sensorPosition = sensor.getPosition();
         double centerX = sensorPosition.getX() - SENSOR_SCREEN_WIDTH / 2.0;
         double centerY = sensorPosition.getY() - SENSOR_SCREEN_WIDTH / 2.0;
 
         return new Point2D.Double(centerX, centerY);
     }
 }
