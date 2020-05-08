 package ru.yandex.shad.belova.java.problem1;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 
 @RunWith(value = Parameterized.class)
 public class TestMyList {
     private MyListFactory.TYPE type;
     private IMyList list;
     
     @Parameters(name="{0}")
     public static List<Object[]> classes(){
         LinkedList<Object[]> classes = new LinkedList<Object[]>();
         classes.add(new Object[]{MyListFactory.TYPE.ARRAY});
         classes.add(new Object[]{MyListFactory.TYPE.LINKED_LIST});
         return classes;
     }
     
     public TestMyList(MyListFactory.TYPE type) {
         this.type = type;
     }
     
     @Rule
     public ExpectedException exception = ExpectedException.none();
     
     @Before
     public void setUp(){
         list = MyListFactory.createList(this.type);
     }
     
     @Test
     public void testAdd(){
         list.add(1);
         assertArrayEquals("Fail in adding/getting 1", new Object[]{1}, list.toArray());
         list.add(2);
         assertArrayEquals("Fail in adding element 2 with add", new Object[]{1,2}, list.toArray());
         list.add(3);
         assertArrayEquals("Fail in adding element 3 with add", new Object[]{1,2,3}, list.toArray());
         list.add(0, 5);
        assertArrayEquals("Fail in adding first element with add by index", new Object[]{5,1,2,3}, list.toArray());
         list.add(3, 10);
        assertArrayEquals("Fail in adding last element with add by index", new Object[]{5,1,2,10,3}, list.toArray());
         list.add(2, 4);
        assertArrayEquals("Fail in adding middle element with add by index", new Object[]{5,1,4,2,10,3}, list.toArray());
     }
     
     @Test
     public void testAddAll(){
         list.addAll(new Object[]{4,5,6});
         assertArrayEquals("Fail in adding elements with addAll", new Object[]{4,5,6}, list.toArray());
         list.addAll(0, new Object[]{-1,-2,-3});
         assertArrayEquals("Fail in adding first elements with addAll by index", new Object[]{-1,-2,-3,4,5,6}, list.toArray());
         list.addAll(6, new Object[]{100,200,300});
         assertArrayEquals("Fail in adding first elements with addAll by index", new Object[]{-1,-2,-3,4,5,6,10,100,200,300}, list.toArray());
         list.addAll(1, new Object[]{});
         assertArrayEquals("Fail in adding empty array", new Object[]{-1,-2,-3,4,5,6,10,100,200,300}, list.toArray());
         list.addAll(1, new Object[]{3,4});
         assertArrayEquals("Fail in adding empty array", new Object[]{-1,3,4,-2,-3,4,5,6,10,100,200,300}, list.toArray());
     
     }
     
     @Test
     public void testSet(){
         list.addAll(new Object[]{1,2,3});
         list.set(0, 5);
         assertArrayEquals("Fail in set element in index 0", new Object[]{5,2,3}, list.toArray());
         list.set(2, 10);
         assertArrayEquals("Fail in set element in index 2", new Object[]{5,2,10}, list.toArray());
     }
     
     @Test
     public void testGet(){
         list.addAll(new Object[]{1,2,3});
         assertEquals("Fail in get element in first index", 1, list.get(0));
         assertEquals("Fail in get element in last index", 3, list.get(2));
         assertEquals("Fail in get element in middle index", 2, list.get(1));
         }
     
     @Test
     public void testSetException(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.set(1,4);
     }
     
     @Test
     public void testAddException(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1,1);
     }
     
     @Test
     public void testAddException2(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.add(2,2);
     }
     
     @Test
     public void testAddException3(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.add(-1,6);
     }
     
     @Test
     public void testAddAllException(){
         exception.expect(IndexOutOfBoundsException.class);
         list.addAll(1,new Object[]{1,2,3});
     }
     
     @Test
     public void testAddAllException2(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.add(2,new Object[]{4,3,2});
     }
     
     @Test
     public void testAddAllException3(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.add(-1,new Object[]{0,1,5});
     }
     
     @Test
     public void testGetException1(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.get(1);
     }
     
     @Test
     public void testGetException2(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.get(-1);
     }
     @Test
     public void testGetException3(){
         exception.expect(IndexOutOfBoundsException.class);
         list.get(0);
     }
     
     @Test
     public void testToArray(){
         assertArrayEquals("On empty list failed", new Object[]{}, list.toArray());
         list.addAll(new Object[]{1,4,-1,1000});
         assertArrayEquals("On list failed", new Object[]{1,4,-1,1000}, list.toArray());
     }
     
     @Test
     public void testClear(){
         list.add(new Object[]{1,100});
         list.clear();
         assertArrayEquals("Clear failed", new Object[]{}, list.toArray());
     }
     
     @Test
     public void testRemove(){
         list.addAll(new Object[]{1,100,11,140,-2});
         list.remove(0);
         assertArrayEquals("Remove first failed", new Object[]{100,11,140,-2}, list.toArray());
         list.remove(3);
         assertArrayEquals("Remove last failed", new Object[]{100,11,140}, list.toArray());
         list.remove(1);
         assertArrayEquals("Remove middle failed", new Object[]{100,140}, list.toArray());
     }
     
     @Test
     public void testRemoveException1(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.remove(1);
     }
     
     @Test
     public void testRemoveException2(){
         exception.expect(IndexOutOfBoundsException.class);
         list.add(1);
         list.remove(-1);
     }
     
     @Test
     public void testRemoveException3(){
         exception.expect(IndexOutOfBoundsException.class);
         list.remove(0);
     }
     
     @Test
     public void testToString(){
         list.add(new Object[]{1,4,-1,1000});
         assertEquals("String equality failed", "1,4,-1,1000", list.toString());
     }
     
     @Test
     public void testSize(){
         assertEquals("Size after Empty list failed", 0, list.size());
         list.add(1);
         assertEquals("Size after Add failed", 1, list.size());
         list.addAll(new Object[]{1,2,3});
         assertEquals("Size after AddAll failed", 4, list.size());
         list.addAll(2, new Object[]{1,2,3});
         assertEquals("Size after Add all to index failed", 7, list.size());
         list.add(3,-1);
         assertEquals("Size after Add to index failed", 8, list.size());
         list.set(3, 4);
         assertEquals("Size after Set failed", 8, list.size());
         list.remove(3);
         assertEquals("Size after Remove failed", 7, list.size());
         list.clear();
         assertEquals("Size after Clear failed", 0, list.size());
     }
     
     @Test
     public void testIsEmpty(){
         assertTrue("On empty list", list.isEmpty());
         list.add(1);
         assertFalse("On full list", list.isEmpty());
     }
     
     @Test
     public void testIndexOf(){
         list.addAll(new Object[]{1,2,2,3,4,5,6,1});
         assertEquals("First-last problem", 0, list.indexOf(1));
         assertEquals("Behind problem", 1, list.indexOf(2));
         assertEquals("Non exist problem", -1, list.indexOf(7));
     }
  
 }
