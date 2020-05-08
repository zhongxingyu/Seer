 /*
  * DvRLib - Graph
  * Duncan van Roermund, 2010-2012
  * ListGraphTest.java
  */
 
 package dvrlib.graph;
 
 import java.util.Iterator;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
public class ListGraphTest {
    AbstractGraph instance;
 
    public AbstractGraph newInstance(int nodeCount) {
       return new ListGraph(nodeCount);
    }
 
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEdgeCount() {
       instance = newInstance(3);
       assertEquals(0, instance.edgeCount());
       instance.addEdge(0, 1, null);
       assertEquals(1, instance.edgeCount());
       instance.addEdge(2, 1, null);
       assertEquals(2, instance.edgeCount());
       instance.removeEdge(2, 1);
       assertEquals(1, instance.edgeCount());
       instance.removeEdge(0, 1);
       assertEquals(0, instance.edgeCount());
    }
 
    @SuppressWarnings("unchecked")
    @Test
    public void testHasEdge() {
       instance = newInstance(10);
       for(int i = 1; i < instance.nodeCount; i += 2) {
          for(int j = 0; j < i; j += 2) {
             instance.addEdge(i, j, null);
          }
       }
 
       // Test hasEdge(int, int)
       for(int i = 0; i < instance.nodeCount; i++) {
          for(int j = 0; j < instance.nodeCount; j++) {
             assertEquals((i % 2 == 1 && j % 2 == 0 && j < i), instance.hasEdge(i, j));
          }
       }
    }
 
    @SuppressWarnings("unchecked")
    @Test
    public void testDegrees() {
       instance = newInstance(6);
       assertDegrees(new int[]{0,0,0,0,0,0});
       assertTrue(instance.addEdge(0, 1, null));
       assertDegrees(new int[]{1,0,0,0,0,0});
       assertTrue(instance.addEdge(2, 1, null));
       assertDegrees(new int[]{1,0,1,0,0,0});
       assertTrue(instance.addEdge(2, 3, null));
       assertDegrees(new int[]{1,0,2,0,0,0});
       assertTrue(instance.addEdge(2, 5, null));
       assertDegrees(new int[]{1,0,3,0,0,0});
       assertTrue(instance.addEdge(4, 5, null));
       assertDegrees(new int[]{1,0,3,0,1,0});
 
       instance = newInstance(5);
       assertDegrees(new int[]{0,0,0,0,0});
       assertTrue(instance.addEdge(0, 1, null));
       assertDegrees(new int[]{1,0,0,0,0});
       assertTrue(instance.addEdge(2, 3, null));
       assertDegrees(new int[]{1,0,1,0,0});
       assertTrue(instance.addEdge(0, 4, null));
       assertDegrees(new int[]{2,0,1,0,0});
       assertTrue(instance.addEdge(2, 4, null));
       assertDegrees(new int[]{2,0,2,0,0});
       assertTrue(instance.addEdge(0, 3, null));
       assertDegrees(new int[]{3,0,2,0,0});
       assertNull(instance.removeEdge(2, 4));
       assertDegrees(new int[]{3,0,1,0,0});
       assertNull(instance.removeEdge(2, 3));
       assertDegrees(new int[]{3,0,0,0,0});
       assertNull(instance.removeEdge(0, 1));
       assertDegrees(new int[]{2,0,0,0,0});
       assertNull(instance.removeEdge(0, 4));
       assertDegrees(new int[]{1,0,0,0,0});
       assertNull(instance.removeEdge(0, 3));
       assertDegrees(new int[]{0,0,0,0,0});
    }
 
    public void assertDegrees(int ds[]) {
       int maxDegree = 0;
 
       // Test getDegree(int)
       for(int i = 0; i < ds.length; i++) {
          assertEquals(ds[i], instance.outDegree(i));
          maxDegree = Math.max(maxDegree, ds[i]);
       }
       // Test getMaxDegree()
       assertEquals(maxDegree, instance.maxOutDegree());
    }
 }
