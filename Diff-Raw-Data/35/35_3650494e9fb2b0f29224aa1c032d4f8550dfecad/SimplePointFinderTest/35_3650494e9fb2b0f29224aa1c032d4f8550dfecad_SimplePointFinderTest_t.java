 package test;
 
 import main.PointFinder;
 import main.SimplePointFinder;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class SimplePointFinderTest {
 
     private static PointFinder pf;
 
     @BeforeClass
     public static void prepare() {
         pf = new SimplePointFinder();
     }
 
     @Test
     public void findPointRightTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(5, 5);
         Point2D c = new Point(2, 0);
 
         Assert.assertEquals(pf.findPoint(a, b, c), -1);
     }
 
     @Test
     public void findPointLeftTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(5, 5);
         Point2D c = new Point(0, 10);
 
         Assert.assertEquals(pf.findPoint(a, b, c), 1);
     }
 
     @Test
     public void findPointOnPathTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(5, 5);
         Point2D c = new Point(10, 10);
 
         Assert.assertEquals(pf.findPoint(a, b, c), 0);
     }
 
     @Test
     public void separatePointsTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(10, 0);
         Set<Point2D> pointSet = new HashSet<>();
 
         pointSet.add(new Point(15, 0));
         pointSet.add(new Point(-5, 0));
 
         pointSet.add(new Point(10, 5));
         pointSet.add(new Point(15, 5));
         pointSet.add(new Point(25, 5));
 
         pointSet.add(new Point(-5, -5));
         pointSet.add(new Point(0, -10));
 
         List<Set<Point2D>> separatedSets = pf.separatePoints(pointSet, a, b);
         Assert.assertEquals(separatedSets.size(), 3);
         Assert.assertEquals(3, separatedSets.get(0).size());
         Assert.assertEquals(2, separatedSets.get(1).size());
         Assert.assertEquals(2, separatedSets.get(2).size());
     }
 
     @Test
     public void isOnLineTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(10, 0);
         Point2D c = new Point(20, 0);
         Point2D d = new Point(5, 0);
 
         Assert.assertEquals(false, pf.isOnLine(a, b, c));
         Assert.assertEquals(true, pf.isOnLine(a, b, d));
     }
 
     @Test
     public void areCrossedasicTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(10, 10);
         Point2D c = new Point(0, 10);
         Point2D d = new Point(10, 0);
 
         Assert.assertEquals(1, pf.areCrossed(a, b, c, d));
     }
 
     @Test
     public void areCrossedStartOnVectorTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(10, 0);
         Point2D c = new Point(5, 0);
         Point2D d = new Point(5, 10);
 
         Assert.assertEquals(0, pf.areCrossed(a, b, c, d));
     }
 
     @Test
     public void areCrossedNotCrossedTest() {
         Point2D a = new Point(0, 0);
         Point2D b = new Point(1, 1);
         Point2D c = new Point(2, 0);
         Point2D d = new Point(2, 5);
 
         Assert.assertEquals(-1, pf.areCrossed(a, b, c, d));
         Assert.assertEquals(-1, pf.areCrossed(a, c, b, d));
     }
 
 //    @Test
 //    public void countCrossingsTest() {
 //        Point2D a = new Point(0, 0);
 //        Point2D b = new Point(2, 0);
 //        Point2D c = new Point(3, 1);
 //        Point2D d = new Point(1, 2);
 //        Point2D e = new Point(-1, 1);
 //        Set<Point2D> pointSet = new HashSet();
 //        pointSet.add(a);
 //        pointSet.add(b);
 //        pointSet.add(c);
 //        pointSet.add(d);
 //        pointSet.add(e);
 //
 //        Assert.assertEquals(5, pf.countCrossings(pointSet));
 //    }
     
     @Test
     public void parseCsvTest() {
     	List<Point2D> ps = pf.parseCsv("colinear.csv");
     	Assert.assertNotNull(ps);
     	Assert.assertEquals(221, ps.size());
     }
     
     @Test
     public void findColinearNumTest() {
     	List<Point2D> ps = pf.parseCsv("colinear.csv");
    	for (int i =0; i<ps.size(); i++) {
    		int num = pf.findColinearNum(ps, ps.get(i));
    		System.out.println(num);
    	}
    	
     }
     
 }
