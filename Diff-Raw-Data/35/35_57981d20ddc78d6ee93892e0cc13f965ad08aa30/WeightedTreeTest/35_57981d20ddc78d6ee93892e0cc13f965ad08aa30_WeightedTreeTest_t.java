 /*
  * DvRLib - Generic
  * Duncan van Roermund, 2010
  * WeightedTreeTest.java
  */
 
 package dvrlib.container;
 
 import dvrlib.generic.Tuple;
 
 import java.util.Iterator;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class WeightedTreeTest {
    @Test
    public void testSize() {
       WeightedTree<Integer> instance = new WeightedTree<Integer>();
       Tuple<Double, Integer> element;
 
       assertTrue(instance.isEmpty());
       assertEquals(0, instance.size());
       for(int i = 1; i < 9; i++) {
          instance.add((double) i, 10 - i);
 
          assertFalse(instance.isEmpty());
          assertEquals(i, instance.size());
 
          testBalance(instance.root);
       }
 
       element = instance.peekMin();
       assertEquals(1, element.a.intValue());
       assertEquals(9, element.b.intValue());
 
       for(int i = 8; i > 0; i--) {
          assertEquals(i, instance.size());
          assertFalse(instance.isEmpty());
 
          element = instance.peekMax();
          assertEquals(i, element.a.intValue());
          assertEquals(10 - i, element.b.intValue());
 
          element = instance.popMax();
          assertEquals(i, element.a.intValue());
          assertEquals(10 - i, element.b.intValue());
 
          testBalance(instance.root);
       }
 
       assertTrue(instance.isEmpty());
 
       for(int i = 8; i > 0; i--) {
          instance.add((double) i, 10 + i);
          testBalance(instance.root);
       }
 
       for(int i = 1; i < 9; i++) {
          element = instance.peekMin();
          assertEquals(i, element.a.intValue());
          assertEquals(10 + i, element.b.intValue());
 
          element = instance.popMin();
          assertEquals(i, element.a.intValue());
          assertEquals(10 + i, element.b.intValue());
 
          testBalance(instance.root);
       }
 
       assertTrue(instance.isEmpty());
 
       for(int i = 1; i < 20; i++) {
          instance.add((double) 1 + (i % 5), i);
 
          assertFalse(instance.isEmpty());
          assertEquals(i, instance.size());
 
          testBalance(instance.root);
       }
 
       assertTrue(instance.remove(2, 1));
       testBalance(instance.root);
       assertTrue(instance.remove(2, 6));
       testBalance(instance.root);
       assertTrue(instance.remove(2, 11));
       testBalance(instance.root);
       assertTrue(instance.remove(2, 16));
       testBalance(instance.root);
    }
 
    @Test
    public void testAdd() {
       WeightedTree<Number> instance;
       instance = testAdd(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
       instance = testAdd(new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1});
       instance = testAdd(new Integer[]{1, 9, 2, 8, 3, 7, 4, 6, 5});
       instance = testAdd(new Integer[]{5, 4, 6, 3, 7, 2, 8, 1, 9});
       instance = testAdd(new Integer[]{4, 2, 6, 3, 7, 8, 1, 5, 9});
       instance = testAdd(new Double []{0.1, 0.2, 0.3, 0.4, 0.5, 0.6});
       instance = testAdd(new Double []{0.6, 0.5, 0.4, 0.3, 0.2, 0.1});
       instance = testAdd(new Double []{0.1, 0.6, 0.2, 0.5, 0.3, 0.4});
       instance = testAdd(new Double []{0.3, 0.4, 0.2, 0.5, 0.1, 0.6});
 
       instance = testAdd(new Double []{0.5, 0.3, 0.2, 0.6, 0.1, 0.4});
       instance.popMax();
       testBalance(instance.root);
       assertEquals(0.1, instance.getMin().key, 0.0);
       assertEquals(0.5, instance.getMax().key, 0.0);
       instance.popMin();
       assertEquals(0.2, instance.getMin().key, 0.0);
       assertEquals(0.5, instance.getMax().key, 0.0);
       instance.popMax();
       testBalance(instance.root);
       assertEquals(0.2, instance.getMin().key, 0.0);
       assertEquals(0.4, instance.getMax().key, 0.0);
    }
    public WeightedTree<Number> testAdd(Number keys[]) {
       WeightedTree<Number> instance = new WeightedTree<Number>();
       assertNull(instance.root);
 
       double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
       for(Number n : keys) {
          double nd = n.doubleValue();
          instance.add(nd, n);
 
          if(nd < min)
             min = nd;
          if(nd > max)
             max = nd;
 
          testBalance(instance.root);
       }
 
       assertNotNull(instance.root);
       assertNull(instance.root.parent);
       assertEquals(min, instance.getMin().key, 0.0);
       assertEquals(max, instance.getMax().key, 0.0);
       assertEquals(keys.length, instance.size());
 
       return instance;
    }
    public void testBalance(WeightedTreeNode node) {
       if(node != null) {
          assertTrue(Math.abs(node.getLeftHeight() - node.getRightHeight()) < 2);
          if(node.left != null) {
             assertTrue(node.left.key <= node.key);
             testBalance(node.left);
          }
          if(node.right != null) {
             assertTrue(node.right.key > node.key);
             testBalance(node.right);
          }
       }
    }
 
    @Test
    public void testIterator() {
      WeightedTree<Integer> instance1 = new WeightedTree<Integer>(),
                            instance2 = new WeightedTree<Integer>();
      assertFalse(instance1.iterator().hasNext());
      assertFalse(instance2.iterator().hasNext());
 
       for(int i = 0; i < 9; i++) {
          for(int j = 0; j < 9; j++) {
            instance1.add((double) i, i + j);
            instance2.add((double) j, i + j);
          }
       }
      Iterator<Integer> it1 = instance1.iterator(),
                        it2 = instance2.iterator();
      for(int i = 8; i >= 0; i--) {
          for(int j = 0; j < 9; j++) {
            assertTrue(it1.hasNext());
            assertTrue(it2.hasNext());
            assertEquals(i + j, it1.next().intValue());
            assertEquals(i + j, it2.next().intValue());
          }
       }
      assertFalse(it1.hasNext());
      assertFalse(it2.hasNext());
    }
 
    @Test
    public void testWeighted() {
       WeightedTree<Integer> instance = new WeightedTree<Integer>();
       assertNull(instance.getWeighted(0.5));
 
       instance.add(0.3, 10);
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.0));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.5));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.9999999));
 
       instance.add(0.5, 15);
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.0));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.3749999));
       assertEqualsWeighted(0.5, 15, instance.getWeighted(0.3750001));
       assertEqualsWeighted(0.5, 15, instance.getWeighted(0.9999999));
 
       instance.add(0.2, 20);
       assertEqualsWeighted(0.2, 20, instance.getWeighted(0.0));
       assertEqualsWeighted(0.2, 20, instance.getWeighted(0.1999999));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.2000001));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.4999999));
       assertEqualsWeighted(0.5, 15, instance.getWeighted(0.5000001));
       assertEqualsWeighted(0.5, 15, instance.getWeighted(0.9999999));
 
       instance.add(0.5, 25);
       assertEqualsWeighted(0.2, 20, instance.getWeighted(0.0));
       assertEqualsWeighted(0.2, 20, instance.getWeighted(0.1333332));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.1333334));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.3333332));
       assertEqualsWeighted(0.5, 15, instance.getWeighted(0.3333334));
       assertEqualsWeighted(0.5, 15, instance.getWeighted(0.6666665));
       assertEqualsWeighted(0.5, 25, instance.getWeighted(0.6666667));
       assertEqualsWeighted(0.5, 25, instance.getWeighted(0.9999999));
 
       instance.remove(0.5, 15);
       assertEqualsWeighted(0.2, 20, instance.getWeighted(0.0));
       assertEqualsWeighted(0.2, 20, instance.getWeighted(0.1999999));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.2000001));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.4999999));
       assertEqualsWeighted(0.5, 25, instance.getWeighted(0.5000001));
       assertEqualsWeighted(0.5, 25, instance.getWeighted(0.9999999));
 
       instance.remove(0.2, 20);
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.0));
       assertEqualsWeighted(0.3, 10, instance.getWeighted(0.3749999));
       assertEqualsWeighted(0.5, 25, instance.getWeighted(0.3750001));
       assertEqualsWeighted(0.5, 25, instance.getWeighted(0.9999999));
    }
    public void assertEqualsWeighted(double k, int v, Tuple<Double, Integer> e) {
       assertEquals(k, e.a.doubleValue(), 0.0);
       assertEquals(v, e.b.intValue());
    }
 }
