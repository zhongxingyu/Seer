 package dk.itu.grp11.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import dk.itu.grp11.main.Parser;
 import dk.itu.grp11.main.Point;
 import dk.itu.grp11.main.Road;
 
 public class ParserTest {
   //Testing to see whether (some) points get the right id and values assigned or not
   @Test
   public void test0() {
     dk.itu.grp11.main.Parser p = new Parser(new File("kdv_node_unload.txt"), new File("kdv_unload.txt"));
     Point[] points = p.parsePoints();
     
     assertEquals(points[0].getID(), 1); //The first point
     assertEquals(points[0].getX(), 595527.51786, 0);
     assertEquals(points[0].getY(), 6402050.98297, 0);
     
     assertEquals(points[points.length-1].getID(), 675902); //The last point
     assertEquals(points[points.length-1].getX(), 692067.66450, 0);
     assertEquals(points[points.length-1].getY(), 6049914.43018, 0);
   }
   
   @Test
   public void test1() {
     dk.itu.grp11.main.Parser p = new Parser(new File("kdv_node_unload.txt"), new File("kdv_unload.txt"));
     Road[] roads = p.parseRoads();
     
     //TODO Not finished
   }
 }
