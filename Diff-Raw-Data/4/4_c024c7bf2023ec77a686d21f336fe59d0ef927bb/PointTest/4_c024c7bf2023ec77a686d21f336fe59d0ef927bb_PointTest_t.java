 package tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import src.utils.Point;
 
 /**
  * Tests for the {@link Point} class.
  */
 public class PointTest {
 
     @Test
     public void testPointConstruction() {
         Point point = new Point(15, 4);
         assertEquals(15, point.getX());
         assertEquals(4, point.getY());
 
         point = new Point(0, 0);
         assertEquals(0, point.getX());
         assertEquals(0, point.getY());
 
         point = new Point(-213, -2);
         assertEquals(-213, point.getX());
         assertEquals(-2, point.getY());
     }
 
     @Test
     public void testEquality() {
         Point point1 = new Point(3, 6);
         Point point2 = new Point(3, 6);
         Point point3 = new Point(1, 4);
 
        assertTrue(point1.equals(point1));
         assertTrue(point1.equals(point2));
        assertFalse(point1.equals(point3));
         assertFalse(point2.equals(point3));
     }
 
     @Test
     public void testDistanceFrom() {
         Point point1 = new Point(3, 6);
         Point point2 = new Point(6, 3);
         Point point3 = new Point(1, 4);
 
         assertEquals(0, point1.distanceFrom(point1), 0.01);
         assertEquals(4.24, point1.distanceFrom(point2), 0.01);
     }
 }
